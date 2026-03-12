#!/usr/bin/env python3
"""
validate_migration.py - Phase 1 COTRN02C Migration: Data validation

Validates that the migration from VSAM files to SQL was successful by:
  1. Counting records in source VSAM text files
  2. Counting records in the generated SQL seed file (or querying the database)
  3. Comparing record counts and reporting discrepancies
  4. Spot-checking sample records for field-level data integrity
  5. Checking referential integrity between card_xref and transactions

Usage:
  # Validate against generated SQL file
  python validate_migration.py

  # Validate against a live database
  python validate_migration.py --direct --db-url postgresql://user:pass@host:5432/dbname

  # Custom file paths
  python validate_migration.py --transact-file /path/to/dailytran.txt --cardxref-file /path/to/cardxref.txt
"""

import argparse
import os
import random
import re
import sys
from pathlib import Path

# Import parsers from the migration script (same directory)
sys.path.insert(0, os.path.dirname(__file__))
from migrate_vsam_data import (
    find_data_file,
    parse_cardxref_record,
    parse_transaction_record,
)


def count_file_records(filepath: str) -> int:
    """Count non-empty lines in a fixed-width data file."""
    if not os.path.isfile(filepath):
        return 0
    count = 0
    with open(filepath, "r", encoding="utf-8", errors="replace") as f:
        for line in f:
            if line.strip():
                count += 1
    return count


def count_sql_inserts(sql_file: str, table_name: str) -> int:
    """Count INSERT INTO <table_name> statements in an SQL file."""
    if not os.path.isfile(sql_file):
        return 0
    pattern = re.compile(
        rf"^\s*INSERT\s+INTO\s+{re.escape(table_name)}\b", re.IGNORECASE
    )
    count = 0
    with open(sql_file, "r", encoding="utf-8") as f:
        for line in f:
            if pattern.match(line):
                count += 1
    return count


def read_all_records(filepath: str, parser):
    """Read and parse all records from a fixed-width data file."""
    records = []
    if not os.path.isfile(filepath):
        return records
    with open(filepath, "r", encoding="utf-8", errors="replace") as f:
        for line in f:
            line = line.rstrip("\n").rstrip("\r")
            if not line.strip():
                continue
            try:
                records.append(parser(line))
            except Exception:
                pass
    return records


def extract_sql_values(sql_file: str, table_name: str) -> list:
    """Extract INSERT statement value tuples from an SQL file for a given table."""
    if not os.path.isfile(sql_file):
        return []
    pattern = re.compile(
        rf"INSERT\s+INTO\s+{re.escape(table_name)}\s+.*?VALUES\s*\((.+)\);",
        re.IGNORECASE,
    )
    values_list = []
    with open(sql_file, "r", encoding="utf-8") as f:
        for line in f:
            m = pattern.search(line)
            if m:
                values_list.append(m.group(1))
    return values_list


def spot_check_record(source_rec: dict, sql_values: str, table: str) -> list:
    """Compare a source record against its SQL INSERT values.

    Returns a list of discrepancy messages (empty if OK).
    """
    issues = []

    if table == "transactions":
        # Check a few key fields are present in the SQL values string
        checks = [
            ("legacy_tran_id", source_rec.get("legacy_tran_id", "")),
            ("tran_type_cd", source_rec.get("tran_type_cd", "")),
            ("tran_card_num", source_rec.get("tran_card_num", "")),
            ("tran_amt", source_rec.get("tran_amt", "")),
            ("tran_merchant_name", source_rec.get("tran_merchant_name", "")),
        ]
        for field_name, expected in checks:
            if expected and expected not in sql_values:
                issues.append(
                    f"  {field_name}: expected '{expected}' not found in SQL"
                )
    elif table == "card_xref":
        checks = [
            ("card_num", source_rec.get("card_num", "")),
            ("cust_id", source_rec.get("cust_id", "")),
            ("acct_id", source_rec.get("acct_id", "")),
        ]
        for field_name, expected in checks:
            if expected and expected not in sql_values:
                issues.append(
                    f"  {field_name}: expected '{expected}' not found in SQL"
                )

    return issues


def _parse_sql_values(val_str: str) -> list:
    """Parse a SQL VALUES clause into individual values, respecting quoted strings.

    Handles commas inside single-quoted strings correctly.
    """
    values = []
    current = []
    in_quote = False
    for ch in val_str:
        if ch == "'" and not in_quote:
            in_quote = True
        elif ch == "'" and in_quote:
            in_quote = False
        elif ch == "," and not in_quote:
            values.append("".join(current).strip().strip("'"))
            current = []
            continue
        current.append(ch)
    if current:
        values.append("".join(current).strip().strip("'"))
    return values


def validate_referential_integrity_sql(sql_file: str) -> tuple:
    """Check that card numbers in transactions have corresponding card_xref entries.

    Returns (total_card_nums_in_tran, matched, unmatched_list).
    """
    # Extract card_nums from card_xref inserts
    xref_values = extract_sql_values(sql_file, "card_xref")
    xref_card_nums = set()
    for val_str in xref_values:
        parts = _parse_sql_values(val_str)
        if parts:
            xref_card_nums.add(parts[0])  # card_num is first column

    # Extract tran_card_num from transactions inserts
    tran_values = extract_sql_values(sql_file, "transactions")
    tran_card_nums = set()
    for val_str in tran_values:
        parts = _parse_sql_values(val_str)
        # tran_card_num is the 11th value (index 10) in the INSERT
        if len(parts) >= 11:
            tran_card_nums.add(parts[10])

    matched = tran_card_nums & xref_card_nums
    unmatched = tran_card_nums - xref_card_nums

    return len(tran_card_nums), len(matched), sorted(unmatched)


def validate_referential_integrity_db(db_url: str) -> tuple:
    """Check referential integrity directly in the database."""
    import psycopg2

    conn = psycopg2.connect(db_url)
    cur = conn.cursor()

    cur.execute("SELECT COUNT(DISTINCT tran_card_num) FROM transactions")
    total = cur.fetchone()[0]

    cur.execute(
        "SELECT COUNT(DISTINCT t.tran_card_num) "
        "FROM transactions t "
        "INNER JOIN card_xref x ON t.tran_card_num = x.card_num"
    )
    matched = cur.fetchone()[0]

    cur.execute(
        "SELECT DISTINCT t.tran_card_num "
        "FROM transactions t "
        "LEFT JOIN card_xref x ON t.tran_card_num = x.card_num "
        "WHERE x.card_num IS NULL "
        "ORDER BY t.tran_card_num"
    )
    unmatched = [row[0] for row in cur.fetchall()]

    cur.close()
    conn.close()

    return total, matched, unmatched


def main():
    parser = argparse.ArgumentParser(
        description="Validate VSAM-to-SQL migration (Phase 1 COTRN02C)"
    )
    parser.add_argument("--transact-file", help="Path to TRANSACT/dailytran data file")
    parser.add_argument("--cardxref-file", help="Path to CARDXREF data file")
    parser.add_argument("--sql-file", help="Path to generated V2__seed_data.sql")
    parser.add_argument(
        "--direct", action="store_true", help="Validate against live database"
    )
    parser.add_argument("--db-url", help="PostgreSQL connection URL for --direct mode")
    args = parser.parse_args()

    script_dir = Path(__file__).resolve().parent
    project_root = script_dir.parent.parent

    transact_candidates = [
        "app/data/TRANSACT.txt",
        "app/data/ASCII/dailytran.txt",
        "app/data/ASCII/transact.txt",
    ]
    cardxref_candidates = [
        "app/data/CARDXREF.txt",
        "app/data/ASCII/cardxref.txt",
    ]

    transact_file = args.transact_file or find_data_file(
        str(project_root), transact_candidates
    )
    cardxref_file = args.cardxref_file or find_data_file(
        str(project_root), cardxref_candidates
    )
    sql_file = args.sql_file or str(
        project_root / "migration" / "sql" / "V2__seed_data.sql"
    )

    all_passed = True

    print("=" * 70)
    print("Migration Validation - Phase 1 (COTRN02C)")
    print("=" * 70)
    print()

    # ----- Step 1: Record count comparison -----
    print("1. Record Count Comparison")
    print("-" * 40)

    src_tran_count = count_file_records(transact_file)
    src_xref_count = count_file_records(cardxref_file)

    if args.direct:
        if not args.db_url:
            print("ERROR: --db-url is required for --direct mode.")
            sys.exit(1)
        import psycopg2

        conn = psycopg2.connect(args.db_url)
        cur = conn.cursor()
        cur.execute("SELECT COUNT(*) FROM transactions")
        dest_tran_count = cur.fetchone()[0]
        cur.execute("SELECT COUNT(*) FROM card_xref")
        dest_xref_count = cur.fetchone()[0]
        cur.close()
        conn.close()
        dest_label = "database"
    else:
        dest_tran_count = count_sql_inserts(sql_file, "transactions")
        dest_xref_count = count_sql_inserts(sql_file, "card_xref")
        dest_label = "SQL file"

    tran_match = src_tran_count == dest_tran_count
    xref_match = src_xref_count == dest_xref_count

    status_tran = "OK" if tran_match else "MISMATCH"
    status_xref = "OK" if xref_match else "MISMATCH"

    print(f"  TRANSACT : source={src_tran_count:>6}  {dest_label}={dest_tran_count:>6}  [{status_tran}]")
    print(f"  CARDXREF : source={src_xref_count:>6}  {dest_label}={dest_xref_count:>6}  [{status_xref}]")

    if not tran_match or not xref_match:
        all_passed = False
        print("  ** Record count discrepancy detected! **")

    print()

    # ----- Step 2: Spot-check sample records -----
    print("2. Spot-Check Sample Records")
    print("-" * 40)

    if not args.direct:
        # Spot-check transactions
        tran_records = read_all_records(transact_file, parse_transaction_record)
        tran_sql_values = extract_sql_values(sql_file, "transactions")

        if tran_records and tran_sql_values:
            sample_indices = _select_sample_indices(len(tran_records))
            tran_issues = 0
            for idx in sample_indices:
                if idx < len(tran_sql_values):
                    issues = spot_check_record(
                        tran_records[idx], tran_sql_values[idx], "transactions"
                    )
                    if issues:
                        tran_issues += 1
                        print(f"  TRANSACT record #{idx + 1}: ISSUES FOUND")
                        for issue in issues:
                            print(f"    {issue}")
                    else:
                        print(f"  TRANSACT record #{idx + 1}: OK")
            if tran_issues > 0:
                all_passed = False
        else:
            print("  TRANSACT: No records to spot-check (source or SQL file missing/empty)")

        # Spot-check card_xref
        xref_records = read_all_records(cardxref_file, parse_cardxref_record)
        xref_sql_values = extract_sql_values(sql_file, "card_xref")

        if xref_records and xref_sql_values:
            sample_indices = _select_sample_indices(len(xref_records))
            xref_issues = 0
            for idx in sample_indices:
                if idx < len(xref_sql_values):
                    issues = spot_check_record(
                        xref_records[idx], xref_sql_values[idx], "card_xref"
                    )
                    if issues:
                        xref_issues += 1
                        print(f"  CARDXREF record #{idx + 1}: ISSUES FOUND")
                        for issue in issues:
                            print(f"    {issue}")
                    else:
                        print(f"  CARDXREF record #{idx + 1}: OK")
            if xref_issues > 0:
                all_passed = False
        else:
            print("  CARDXREF: No records to spot-check (source or SQL file missing/empty)")
    else:
        print("  (Spot-check in --direct mode validates counts only; "
              "use SQL file mode for field-level checks)")

    print()

    # ----- Step 3: Referential integrity -----
    print("3. Referential Integrity Check")
    print("-" * 40)

    if args.direct:
        total_cards, matched, unmatched = validate_referential_integrity_db(args.db_url)
    else:
        total_cards, matched, unmatched = validate_referential_integrity_sql(sql_file)

    print(f"  Distinct card numbers in transactions: {total_cards}")
    print(f"  Matched in card_xref:                  {matched}")
    print(f"  Unmatched (no card_xref entry):         {len(unmatched)}")

    if unmatched:
        all_passed = False
        print("  ** Referential integrity issues found! **")
        if len(unmatched) <= 20:
            for cn in unmatched:
                print(f"    - {cn}")
        else:
            for cn in unmatched[:10]:
                print(f"    - {cn}")
            print(f"    ... and {len(unmatched) - 10} more")
    else:
        if total_cards > 0:
            print("  All transaction card numbers have matching card_xref entries.")
        else:
            print("  No transaction records to check.")

    print()

    # ----- Final result -----
    print("=" * 70)
    if all_passed:
        print("VALIDATION PASSED - All checks successful.")
    else:
        print("VALIDATION FAILED - Issues detected. Review output above.")
    print("=" * 70)

    sys.exit(0 if all_passed else 1)


def _select_sample_indices(total: int, sample_size: int = 5) -> list:
    """Select sample indices: first, last, and random middle records."""
    if total == 0:
        return []
    if total <= sample_size:
        return list(range(total))

    indices = {0, total - 1}
    random.seed(42)  # Deterministic for reproducibility
    while len(indices) < min(sample_size, total):
        indices.add(random.randint(1, total - 2))
    return sorted(indices)


if __name__ == "__main__":
    main()
