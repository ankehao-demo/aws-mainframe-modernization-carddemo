# Phase 1: COTRN02C Migration -- Database Schema & Data Migration

This directory contains the SQL schema definitions and data migration tooling for converting the CardDemo VSAM file data into relational database tables. This is Phase 1 of migrating the COTRN02C (Add Transaction) COBOL/CICS program to Spring Boot.

## Overview

The COTRN02C program writes transaction records to a VSAM KSDS file called **TRANSACT** and reads card-to-account cross-reference data from the **CCXREF/CXACAIX** VSAM files. This migration creates equivalent PostgreSQL tables and provides scripts to load the existing VSAM data.

## Prerequisites

- **Python 3.8+**
- **PostgreSQL** (or compatible database such as H2 for testing)
- `psycopg2-binary` (only required for direct database insertion mode)

Install Python dependencies:

```bash
pip install -r migration/scripts/requirements.txt
```

## Directory Structure

```
migration/
  sql/
    V1__create_schema.sql      # Flyway migration: table & index definitions
    V2__seed_data.sql           # Auto-generated seed data (output of migrate script)
  scripts/
    migrate_vsam_data.py        # VSAM-to-SQL data converter
    validate_migration.py       # Post-migration validation
    requirements.txt            # Python dependencies
  README.md                     # This file
```

## Step 1: Apply the Schema (V1 Migration)

Apply the Flyway-compatible schema migration to create the `transactions` and `card_xref` tables:

```bash
# Using Flyway
flyway -url=jdbc:postgresql://localhost:5432/carddemo migrate

# Or manually with psql
psql -h localhost -U postgres -d carddemo -f migration/sql/V1__create_schema.sql
```

## Step 2: Run the Data Migration

### Generate SQL Seed File (default)

```bash
python migration/scripts/migrate_vsam_data.py
```

This reads the VSAM data files and produces `migration/sql/V2__seed_data.sql` containing INSERT statements. Then apply it:

```bash
psql -h localhost -U postgres -d carddemo -f migration/sql/V2__seed_data.sql
```

### Direct Database Insertion

```bash
python migration/scripts/migrate_vsam_data.py \
  --direct \
  --db-url postgresql://user:password@localhost:5432/carddemo
```

### Custom File Paths

If the data files are in non-standard locations:

```bash
python migration/scripts/migrate_vsam_data.py \
  --transact-file /path/to/TRANSACT.txt \
  --cardxref-file /path/to/CARDXREF.txt
```

The script automatically searches for data files at:
- `app/data/TRANSACT.txt`
- `app/data/ASCII/dailytran.txt`
- `app/data/CARDXREF.txt`
- `app/data/ASCII/cardxref.txt`

## Step 3: Validate the Migration

### Validate Against SQL File

```bash
python migration/scripts/validate_migration.py
```

### Validate Against Database

```bash
python migration/scripts/validate_migration.py \
  --direct \
  --db-url postgresql://user:password@localhost:5432/carddemo
```

The validation script checks:
1. **Record counts** -- source file lines vs. destination INSERT count (or DB row count)
2. **Spot-checks** -- field-level comparison of first, last, and random sample records
3. **Referential integrity** -- all `tran_card_num` values in `transactions` have matching `card_xref` entries

## COBOL Copybook to SQL Column Mapping

### `transactions` table (from CVTRA05Y.cpy -- TRAN-RECORD, 350 bytes)

| COBOL Field          | PIC Clause    | Positions | SQL Column           | SQL Type        | Notes                                      |
| -------------------- | ------------- | --------- | -------------------- | --------------- | ------------------------------------------ |
| --                   | --            | --        | `tran_id`            | BIGSERIAL PK    | Auto-incrementing; replaces COBOL TRAN-ID  |
| TRAN-ID              | X(16)         | 1-16      | `legacy_tran_id`     | VARCHAR(16)     | Original COBOL key, preserved for tracing  |
| TRAN-TYPE-CD         | X(02)         | 17-18     | `tran_type_cd`       | VARCHAR(2)      |                                            |
| TRAN-CAT-CD          | 9(04)         | 19-22     | `tran_cat_cd`        | VARCHAR(4)      |                                            |
| TRAN-SOURCE          | X(10)         | 23-32     | `tran_source`        | VARCHAR(10)     |                                            |
| TRAN-DESC            | X(100)        | 33-132    | `tran_desc`          | VARCHAR(100)    |                                            |
| TRAN-AMT             | S9(09)V99     | 133-143   | `tran_amt`           | DECIMAL(11,2)   | Signed overpunch; implied 2 decimal places |
| TRAN-MERCHANT-ID     | 9(09)         | 144-152   | `tran_merchant_id`   | VARCHAR(9)      |                                            |
| TRAN-MERCHANT-NAME   | X(50)         | 153-202   | `tran_merchant_name` | VARCHAR(50)     |                                            |
| TRAN-MERCHANT-CITY   | X(50)         | 203-252   | `tran_merchant_city` | VARCHAR(50)     |                                            |
| TRAN-MERCHANT-ZIP    | X(10)         | 253-262   | `tran_merchant_zip`  | VARCHAR(10)     |                                            |
| TRAN-CARD-NUM        | X(16)         | 263-278   | `tran_card_num`      | VARCHAR(16)     |                                            |
| TRAN-ORIG-TS         | X(26)         | 279-304   | `tran_orig_ts`       | VARCHAR(26)     | Format: YYYY-MM-DD HH:MM:SS.ffffff        |
| TRAN-PROC-TS         | X(26)         | 305-330   | `tran_proc_ts`       | VARCHAR(26)     | Format: YYYY-MM-DD HH:MM:SS.ffffff        |
| FILLER               | X(20)         | 331-350   | --                   | --              | Not migrated                               |
| --                   | --            | --        | `created_at`         | TIMESTAMP       | Auto-populated on insert                   |

### `card_xref` table (from CVACT03Y.cpy -- CARD-XREF-RECORD, 50 bytes)

| COBOL Field     | PIC Clause | Positions | SQL Column | SQL Type    | Notes                          |
| --------------- | ---------- | --------- | ---------- | ----------- | ------------------------------ |
| XREF-CARD-NUM   | X(16)      | 1-16      | `card_num` | VARCHAR(16) PK | Primary key                |
| XREF-CUST-ID    | 9(09)      | 17-25     | `cust_id`  | VARCHAR(9)  |                                |
| XREF-ACCT-ID    | 9(11)      | 26-36     | `acct_id`  | VARCHAR(11) |                                |
| FILLER           | X(14)      | 37-50     | --         | --          | Not migrated                   |

## TRAN-ID Migration Strategy

In the original COBOL application, `TRAN-ID` (PIC X(16)) is a sequential key generated by the COTRN02C program using a STARTBR/READPREV/increment pattern on the VSAM KSDS file. In the relational database:

- **New primary key**: `tran_id` (BIGSERIAL) provides auto-incrementing IDs managed by the database sequence, which is simpler and more idiomatic for SQL.
- **Legacy key preserved**: `legacy_tran_id` (VARCHAR(16)) stores the original COBOL TRAN-ID value for traceability during migration and for cross-referencing with any existing reports or audit trails.
- After migration stabilizes, the `legacy_tran_id` column can optionally be dropped.

## Data Format Notes

- **Fixed-width records**: Both VSAM files use fixed-width records (350 bytes for TRANSACT, 50 bytes for CARDXREF) with no field delimiters.
- **Signed numeric fields**: `TRAN-AMT` uses COBOL zoned-decimal with ASCII overpunch encoding for the sign in the last byte (e.g., `{` = +0, `}` = -0, `A`-`I` = +1 to +9, `J`-`R` = -1 to -9).
- **Implied decimal**: `TRAN-AMT` PIC S9(09)V99 has an implied decimal point -- the last 2 digits represent cents.
- **Timestamps**: `TRAN-ORIG-TS` and `TRAN-PROC-TS` are stored as 26-character strings in the format `YYYY-MM-DD HH:MM:SS.ffffff`.
- **Missing data files**: If `TRANSACT.txt` or `CARDXREF.txt` do not exist, the migration script will issue a warning and skip that file. The schema is still valid and ready for data.
