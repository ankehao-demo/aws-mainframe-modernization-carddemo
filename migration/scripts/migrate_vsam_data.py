#!/usr/bin/env python3
"""
migrate_vsam_data.py - Phase 1 COTRN02C Migration: VSAM to SQL data converter

Reads fixed-width VSAM data files (TRANSACT/dailytran and CARDXREF) and produces
SQL INSERT statements or inserts directly into a PostgreSQL database.

Record layouts are defined by COBOL copybooks:
  - CVTRA05Y.cpy  (TRAN-RECORD, 350 bytes)
  - CVACT03Y.cpy  (CARD-XREF-RECORD, 50 bytes)

Usage:
  # Generate SQL seed file
  python migrate_vsam_data.py

  # Direct database insertion
  python migrate_vsam_data.py --direct --db-url postgresql://user:pass@host:5432/dbname

  # Custom file paths
  python migrate_vsam_data.py --transact-file /path/to/dailytran.txt --cardxref-file /path/to/cardxref.txt
"""

import argparse
import os
import sys
from pathlib import Path

# ---------------------------------------------------------------------------
# COBOL zoned-decimal / overpunch sign decoding (ASCII representation)
# ---------------------------------------------------------------------------
# PIC S9(n)V99 in COBOL stores the sign in the last byte using overpunch.
# In ASCII representation the last digit is replaced with a letter:
#   Positive: { = 0, A = 1, B = 2, C = 3, D = 4, E = 5, F = 6, G = 7, H = 8, I = 9
#   Negative: } = 0, J = 1, K = 2, L = 3, M = 4, N = 5, O = 6, P = 7, Q = 8, R = 9

POSITIVE_OVERPUNCH = {
    "{": "0", "A": "1", "B": "2", "C": "3", "D": "4",
    "E": "5", "F": "6", "G": "7", "H": "8", "I": "9",
}
NEGATIVE_OVERPUNCH = {
    "}": "0", "J": "1", "K": "2", "L": "3", "M": "4",
    "N": "5", "O": "6", "P": "7", "Q": "8", "R": "9",
}


def decode_signed_numeric(raw: str, decimal_places: int = 2) -> str:
    """Decode a COBOL zoned-decimal signed numeric field to a decimal string.

    Args:
        raw: The raw fixed-width string (e.g. '0000005047G').
        decimal_places: Number of implied decimal places (V99 -> 2).

    Returns:
        A string like '50478' which, with decimal_places=2, represents 504.78.
        Negative values are prefixed with '-'.
    """
    if not raw or raw.isspace():
        return "0"

    last_char = raw[-1]
    sign = 1

    if last_char in POSITIVE_OVERPUNCH:
        digit = POSITIVE_OVERPUNCH[last_char]
        sign = 1
    elif last_char in NEGATIVE_OVERPUNCH:
        digit = NEGATIVE_OVERPUNCH[last_char]
        sign = -1
    elif last_char.isdigit():
        digit = last_char
        sign = 1
    else:
        # Unknown character - treat as zero
        digit = "0"

    digits = raw[:-1] + digit

    # Remove leading zeros but keep at least (decimal_places + 1) digits
    digits = digits.lstrip("0") or "0"

    # Insert implied decimal point
    if decimal_places > 0:
        # Pad with leading zeros if needed
        while len(digits) <= decimal_places:
            digits = "0" + digits
        integer_part = digits[:-decimal_places]
        fractional_part = digits[-decimal_places:]
        result = f"{integer_part}.{fractional_part}"
    else:
        result = digits

    if sign < 0:
        result = "-" + result

    return result


def sql_escape(value: str) -> str:
    """Escape a string for safe inclusion in an SQL literal."""
    return value.replace("'", "''")


# ---------------------------------------------------------------------------
# Record parsers
# ---------------------------------------------------------------------------

def parse_transaction_record(line: str) -> dict:
    """Parse a 350-byte TRAN-RECORD (CVTRA05Y.cpy) from a fixed-width line.

    Layout (1-indexed positions):
        1-16    TRAN-ID            PIC X(16)
        17-18   TRAN-TYPE-CD       PIC X(02)
        19-22   TRAN-CAT-CD        PIC 9(04)
        23-32   TRAN-SOURCE        PIC X(10)
        33-132  TRAN-DESC          PIC X(100)
        133-143 TRAN-AMT           PIC S9(09)V99  (11 bytes, signed overpunch)
        144-152 TRAN-MERCHANT-ID   PIC 9(09)
        153-202 TRAN-MERCHANT-NAME PIC X(50)
        203-252 TRAN-MERCHANT-CITY PIC X(50)
        253-262 TRAN-MERCHANT-ZIP  PIC X(10)
        263-278 TRAN-CARD-NUM      PIC X(16)
        279-304 TRAN-ORIG-TS       PIC X(26)
        305-330 TRAN-PROC-TS       PIC X(26)
        331-350 FILLER             PIC X(20)
    """
    # Pad line to 350 if shorter (graceful handling)
    if len(line) < 350:
        line = line.ljust(350)

    rec = {
        "legacy_tran_id":     line[0:16].strip(),
        "tran_type_cd":       line[16:18].strip(),
        "tran_cat_cd":        line[18:22].strip(),
        "tran_source":        line[22:32].strip(),
        "tran_desc":          line[32:132].strip(),
        "tran_amt_raw":       line[132:143],
        "tran_merchant_id":   line[143:152].strip(),
        "tran_merchant_name": line[152:202].strip(),
        "tran_merchant_city": line[202:252].strip(),
        "tran_merchant_zip":  line[252:262].strip(),
        "tran_card_num":      line[262:278].strip(),
        "tran_orig_ts":       line[278:304].strip(),
        "tran_proc_ts":       line[304:330].strip(),
    }

    # Decode the signed amount field
    rec["tran_amt"] = decode_signed_numeric(rec["tran_amt_raw"], decimal_places=2)

    return rec


def parse_cardxref_record(line: str) -> dict:
    """Parse a 50-byte CARD-XREF-RECORD (CVACT03Y.cpy) from a fixed-width line.

    Layout (1-indexed positions):
        1-16    XREF-CARD-NUM  PIC X(16)
        17-25   XREF-CUST-ID   PIC 9(09)
        26-36   XREF-ACCT-ID   PIC 9(11)
        37-50   FILLER         PIC X(14)
    """
    # Pad line to 50 if shorter
    if len(line) < 36:
        line = line.ljust(50)

    rec = {
        "card_num": line[0:16].strip(),
        "cust_id":  line[16:25].strip(),
        "acct_id":  line[25:36].strip(),
    }
    return rec


# ---------------------------------------------------------------------------
# SQL generation
# ---------------------------------------------------------------------------

def transaction_to_sql(rec: dict) -> str:
    """Generate an INSERT statement for a parsed transaction record."""
    return (
        "INSERT INTO transactions "
        "(legacy_tran_id, tran_type_cd, tran_cat_cd, tran_source, tran_desc, "
        "tran_amt, tran_merchant_id, tran_merchant_name, tran_merchant_city, "
        "tran_merchant_zip, tran_card_num, tran_orig_ts, tran_proc_ts) VALUES ("
        f"'{sql_escape(rec['legacy_tran_id'])}', "
        f"'{sql_escape(rec['tran_type_cd'])}', "
        f"'{sql_escape(rec['tran_cat_cd'])}', "
        f"'{sql_escape(rec['tran_source'])}', "
        f"'{sql_escape(rec['tran_desc'])}', "
        f"{rec['tran_amt']}, "
        f"'{sql_escape(rec['tran_merchant_id'])}', "
        f"'{sql_escape(rec['tran_merchant_name'])}', "
        f"'{sql_escape(rec['tran_merchant_city'])}', "
        f"'{sql_escape(rec['tran_merchant_zip'])}', "
        f"'{sql_escape(rec['tran_card_num'])}', "
        f"'{sql_escape(rec['tran_orig_ts'])}', "
        f"'{sql_escape(rec['tran_proc_ts'])}'"
        ");"
    )


def cardxref_to_sql(rec: dict) -> str:
    """Generate an INSERT statement for a parsed card_xref record."""
    return (
        "INSERT INTO card_xref (card_num, cust_id, acct_id) VALUES ("
        f"'{sql_escape(rec['card_num'])}', "
        f"'{sql_escape(rec['cust_id'])}', "
        f"'{sql_escape(rec['acct_id'])}'"
        ");"
    )


# ---------------------------------------------------------------------------
# File processing
# ---------------------------------------------------------------------------

def process_file(filepath: str, parser, to_sql, label: str):
    """Read a fixed-width file, parse records, and return SQL + stats.

    Returns:
        (sql_lines, total, success, errors)
    """
    sql_lines = []
    records = []
    total = 0
    success = 0
    errors = 0

    if not os.path.isfile(filepath):
        print(f"WARNING: {label} file not found: {filepath}")
        return sql_lines, records, total, success, errors

    with open(filepath, "r", encoding="utf-8", errors="replace") as f:
        for line_num, raw_line in enumerate(f, start=1):
            # Strip newline but preserve fixed-width content
            line = raw_line.rstrip("\n").rstrip("\r")
            if not line.strip():
                continue

            total += 1
            try:
                rec = parser(line)
                sql_lines.append(to_sql(rec))
                records.append(rec)
                success += 1
            except Exception as e:
                errors += 1
                print(f"ERROR: {label} line {line_num}: {e}")

    return sql_lines, records, total, success, errors


def find_data_file(base_dir: str, candidates: list) -> str:
    """Search for a data file among candidate relative paths."""
    for candidate in candidates:
        path = os.path.join(base_dir, candidate)
        if os.path.isfile(path):
            return path
    return os.path.join(base_dir, candidates[0])


# ---------------------------------------------------------------------------
# Direct database insertion
# ---------------------------------------------------------------------------

def insert_direct(db_url: str, tran_records: list, xref_records: list):
    """Insert parsed records directly into PostgreSQL using psycopg2."""
    try:
        import psycopg2
    except ImportError:
        print("ERROR: psycopg2 is required for --direct mode.")
        print("  Install with: pip install psycopg2-binary")
        sys.exit(1)

    conn = psycopg2.connect(db_url)
    cur = conn.cursor()

    tran_count = 0
    for rec in tran_records:
        cur.execute(
            "INSERT INTO transactions "
            "(legacy_tran_id, tran_type_cd, tran_cat_cd, tran_source, tran_desc, "
            "tran_amt, tran_merchant_id, tran_merchant_name, tran_merchant_city, "
            "tran_merchant_zip, tran_card_num, tran_orig_ts, tran_proc_ts) "
            "VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)",
            (
                rec["legacy_tran_id"],
                rec["tran_type_cd"],
                rec["tran_cat_cd"],
                rec["tran_source"],
                rec["tran_desc"],
                rec["tran_amt"],
                rec["tran_merchant_id"],
                rec["tran_merchant_name"],
                rec["tran_merchant_city"],
                rec["tran_merchant_zip"],
                rec["tran_card_num"],
                rec["tran_orig_ts"],
                rec["tran_proc_ts"],
            ),
        )
        tran_count += 1

    xref_count = 0
    for rec in xref_records:
        cur.execute(
            "INSERT INTO card_xref (card_num, cust_id, acct_id) "
            "VALUES (%s, %s, %s)",
            (rec["card_num"], rec["cust_id"], rec["acct_id"]),
        )
        xref_count += 1

    conn.commit()
    cur.close()
    conn.close()
    print(f"Direct insert complete: {tran_count} transactions, {xref_count} card_xref records.")


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

def main():
    parser = argparse.ArgumentParser(
        description="Migrate VSAM fixed-width data to SQL (Phase 1 COTRN02C migration)"
    )
    parser.add_argument(
        "--transact-file",
        help="Path to TRANSACT/dailytran data file (fixed-width, 350 bytes/record)",
    )
    parser.add_argument(
        "--cardxref-file",
        help="Path to CARDXREF data file (fixed-width, 50 bytes/record)",
    )
    parser.add_argument(
        "--output", "-o",
        help="Output SQL file path (default: migration/sql/V2__seed_data.sql)",
    )
    parser.add_argument(
        "--direct",
        action="store_true",
        help="Insert directly into database instead of generating SQL file",
    )
    parser.add_argument(
        "--db-url",
        help="PostgreSQL connection URL for --direct mode",
    )
    args = parser.parse_args()

    # Resolve project root (two levels up from this script)
    script_dir = Path(__file__).resolve().parent
    project_root = script_dir.parent.parent

    # Locate data files
    transact_candidates = [
        "app/data/TRANSACT.txt",
        "app/data/ASCII/dailytran.txt",
        "app/data/ASCII/transact.txt",
    ]
    cardxref_candidates = [
        "app/data/CARDXREF.txt",
        "app/data/ASCII/cardxref.txt",
    ]

    transact_file = args.transact_file or find_data_file(str(project_root), transact_candidates)
    cardxref_file = args.cardxref_file or find_data_file(str(project_root), cardxref_candidates)
    output_file = args.output or str(project_root / "migration" / "sql" / "V2__seed_data.sql")

    print("=" * 70)
    print("VSAM Data Migration - Phase 1 (COTRN02C)")
    print("=" * 70)
    print(f"Transaction file : {transact_file}")
    print(f"Card XREF file   : {cardxref_file}")
    if not args.direct:
        print(f"Output SQL file  : {output_file}")
    print()

    # Process TRANSACT / dailytran
    tran_sql, tran_records, tran_total, tran_ok, tran_err = process_file(
        transact_file, parse_transaction_record, transaction_to_sql, "TRANSACT"
    )

    # Process CARDXREF
    xref_sql, xref_records, xref_total, xref_ok, xref_err = process_file(
        cardxref_file, parse_cardxref_record, cardxref_to_sql, "CARDXREF"
    )

    # Summary
    print("-" * 70)
    print("Migration Summary")
    print("-" * 70)
    print(f"TRANSACT : {tran_total:>6} read | {tran_ok:>6} converted | {tran_err:>6} errors")
    print(f"CARDXREF : {xref_total:>6} read | {xref_ok:>6} converted | {xref_err:>6} errors")
    total_read = tran_total + xref_total
    total_ok = tran_ok + xref_ok
    total_err = tran_err + xref_err
    print(f"TOTAL    : {total_read:>6} read | {total_ok:>6} converted | {total_err:>6} errors")
    print("-" * 70)

    if args.direct:
        if not args.db_url:
            print("ERROR: --db-url is required when using --direct mode.")
            sys.exit(1)
        insert_direct(args.db_url, tran_records, xref_records)
    else:
        # Write SQL seed file
        os.makedirs(os.path.dirname(output_file), exist_ok=True)
        with open(output_file, "w", encoding="utf-8") as f:
            f.write("-- =============================================================================\n")
            f.write("-- Flyway Migration V2: Seed data from VSAM files (auto-generated)\n")
            f.write("-- Source: migrate_vsam_data.py\n")
            f.write("-- =============================================================================\n\n")

            if tran_sql:
                f.write("-- ---------------------------------------------------------------------------\n")
                f.write(f"-- transactions ({len(tran_sql)} records from TRANSACT/dailytran)\n")
                f.write("-- ---------------------------------------------------------------------------\n")
                for stmt in tran_sql:
                    f.write(stmt + "\n")
                f.write("\n")

            if xref_sql:
                f.write("-- ---------------------------------------------------------------------------\n")
                f.write(f"-- card_xref ({len(xref_sql)} records from CARDXREF)\n")
                f.write("-- ---------------------------------------------------------------------------\n")
                for stmt in xref_sql:
                    f.write(stmt + "\n")
                f.write("\n")

        print(f"\nSQL seed file written to: {output_file}")

    if tran_err + xref_err > 0:
        print(f"\nWARNING: {tran_err + xref_err} record(s) had errors. Review output above.")
        sys.exit(1)

    print("\nDone.")


if __name__ == "__main__":
    main()
