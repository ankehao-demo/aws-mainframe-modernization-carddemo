# Extend Customer Account ID from 11 Digits to 16 Digits

## Epic

**Epic Title:** Extend Customer Account ID from 11 Digits to 16 Digits
**Epic Key:** CARDDEMO-ACCTID-16
**Priority:** High
**Description:**
Migrate the CardDemo application's customer account ID field from 11 digits (PIC 9(11)) to 16 digits (PIC 9(16)) across all application layers including COBOL copybooks, COBOL programs, BMS screen maps, BMS copybooks, JCL/VSAM definitions, DB2 DDL, MQ message structures, test data files, and documentation. This is a cross-cutting structural change that affects the COMMAREA layout, VSAM key lengths, screen field sizes, and fixed-width data files. All programs sharing the COMMAREA must be recompiled and deployed simultaneously. VSAM clusters must be deleted and redefined. Existing 11-digit account numbers will be left-padded with 5 zeros.

**Acceptance Criteria:**
- All account ID fields across the codebase accept and store 16-digit values
- All validation messages reference "16 digit" instead of "11 digit"
- All BMS screens display 16-character account ID fields
- VSAM clusters are redefined with updated key lengths and record sizes
- All test data files are reformatted with 16-digit account IDs
- DB2 schema updated to DECIMAL(16,0)
- All programs compile and link cleanly
- End-to-end functional testing passes with 16-digit account IDs

---

## Tasks (organized by phase)

### Task 1: Update Shared Copybooks

**Summary:** Update shared COBOL copybooks to extend account ID from PIC 9(11) to PIC 9(16)
**Type:** Task
**Priority:** Highest (must be done first — all other tasks depend on this)
**Story Points:** 3
**Description:**
Update the following copybooks:
- `app/cpy/COCOM01Y.cpy` line 38: Change `CDEMO-ACCT-ID` from `PIC 9(11)` to `PIC 9(16)`. NOTE: This shifts all fields after CDEMO-ACCT-ID in the COMMAREA by 5 bytes.
- `app/cpy/CVCRD01Y.cpy` lines 34-36: Change `CC-ACCT-ID` from `PIC X(11)` to `PIC X(16)` and `CC-ACCT-ID-N` from `PIC 9(11)` to `PIC 9(16)`
- `app/cpy/CVTRA07Y.cpy` line 18: Change `TRAN-REPORT-ACCOUNT-ID` from `PIC X(11)` to `PIC X(16)`. Adjust any FILLER fields in this copybook to accommodate the wider field.
- `app/cpy/CSUTLDWY.cpy`: Search for any PIC X(11)/PIC 9(11) account fields and change to PIC X(16)/PIC 9(16)

**Blocked By:** None
**Blocks:** Tasks 2-10

---

### Task 2: Update BMS Map Source Files

**Summary:** Update BMS screen map definitions to accommodate 16-digit account ID fields
**Type:** Task
**Priority:** High
**Story Points:** 5
**Description:**
For each BMS file, change the account ID field LENGTH from 11 to 16, update PICIN from '99999999999' to '9999999999999999', and verify/adjust POS values for subsequent fields to prevent overlap on 80-column screens:
- `app/bms/COACTVW.bms` — ACCTSID: LENGTH=11→16, PICIN='99999999999'→'9999999999999999'
- `app/bms/COACTUP.bms` — ACCTSID: LENGTH=11→16
- `app/bms/COCRDLI.bms` — ACCTSID and ACCTNO1-ACCTNO7: LENGTH=11→16 each
- `app/bms/COCRDSL.bms` — ACCTSID: LENGTH=11→16
- `app/bms/COCRDUP.bms` — ACCTSID: LENGTH=11→16
- `app/bms/COBIL00.bms` — ACTIDIN: LENGTH=11→16
- `app/bms/COTRN02.bms` — ACTIDIN: LENGTH=11→16

Review screen layout to ensure no field overlaps after extending by 5 characters.

**Blocked By:** Task 1
**Blocks:** Task 3

---

### Task 3: Update BMS Copybooks (Generated Map I/O Areas)

**Summary:** Update BMS-generated copybooks to reflect 16-digit account ID fields
**Type:** Task
**Priority:** High
**Story Points:** 5
**Description:**
Update all PIC X(11) and PIC 99999999999 account ID fields to PIC X(16) and PIC 9999999999999999 in both input and output record definitions:
- `app/cpy-bms/COACTVW.CPY` — ACCTSIDI (line 60) and ACCTSIDO (line 284)
- `app/cpy-bms/COACTUP.CPY` — ACCTSIDI (line 60) and ACCTSIDO
- `app/cpy-bms/COCRDLI.CPY` — ACCTSIDI, ACCTNO1I-ACCTNO7I, and all corresponding output fields
- `app/cpy-bms/COCRDSL.CPY` — ACCTSIDI and ACCTSIDO
- `app/cpy-bms/COCRDUP.CPY` — ACCTSIDI and ACCTSIDO
- `app/cpy-bms/COBIL00.CPY` — ACTIDINI and ACTIDINO
- `app/cpy-bms/COTRN02.CPY` — ACTIDINI and ACTIDINO

Ideally these should be regenerated from the updated BMS maps (Task 2), but can be manually updated if BMS assembly is not available.

**Blocked By:** Task 2
**Blocks:** Task 4

---

### Task 4: Update CICS Online COBOL Programs

**Summary:** Update all CICS online COBOL programs for 16-digit account ID
**Type:** Task
**Priority:** High
**Story Points:** 8
**Description:**
For each program, search for all PIC 9(11) and PIC X(11) fields related to account IDs (field names containing ACCT-ID, ACCT-NUM, ACCOUNT) and change to PIC 9(16) / PIC X(16). Also update all hardcoded "11 digit" validation messages to "16 digit":

Programs to update:
- `app/cbl/COACTUPC.cbl` — ~10 PIC 9(11), ~8 PIC X(11), plus ~6 "11 digit" message strings
- `app/cbl/COACTVWC.cbl` — ~2 PIC 9(11), ~2 PIC X(11), plus ~6 "11 digit" message strings
- `app/cbl/COCRDLIC.cbl` — ~8 PIC 9(11), ~6 PIC X(11)
- `app/cbl/COCRDSLC.cbl` — ~4 PIC 9(11), ~4 PIC X(11), plus ~4 "11 digit" message strings
- `app/cbl/COCRDUPC.cbl` — ~6 PIC 9(11), ~8 PIC X(11), plus ~4 "11 digit" message strings
- `app/cbl/COTRN02C.cbl` — ~2 PIC 9(11), ~2 PIC X(11)
- `app/cbl/COBIL00C.cbl` — account ID references
- `app/cbl/CORPT00C.cbl` — ~2 PIC X(11)
- `app/cbl/CSUTLDTC.cbl` — ~2 PIC X(11)

Be careful to only change account-ID-related fields, not other unrelated PIC 9(11) or PIC X(11) fields.

**Blocked By:** Tasks 1, 3
**Blocks:** Task 9

---

### Task 5: Update Batch COBOL Programs

**Summary:** Update all batch COBOL programs for 16-digit account ID
**Type:** Task
**Priority:** High
**Story Points:** 5
**Description:**
Update FD-ACCT-ID and all related account ID fields from PIC 9(11)/PIC X(11) to PIC 9(16)/PIC X(16) in:
- `app/cbl/CBACT01C.cbl` — FD-ACCT-ID and related fields
- `app/cbl/CBACT04C.cbl` — FD-ACCT-ID and related fields
- `app/cbl/CBTRN01C.cbl` — FD-ACCT-ID and related fields
- `app/cbl/CBTRN02C.cbl` — FD-ACCT-ID and related fields
- `app/cbl/CBTRN03C.cbl` — account ID references
- `app/cbl/CBSTM03A.CBL` — account ID fields
- `app/cbl/CBSTM03B.CBL` — account ID fields

**Blocked By:** Task 1
**Blocks:** Task 9

---

### Task 6: Update JCL — VSAM Cluster and SORT Definitions

**Summary:** Update JCL files for new VSAM key lengths, record sizes, and SORT field definitions
**Type:** Task
**Priority:** High
**Story Points:** 3
**Description:**
- `app/jcl/ACCTFILE.jcl` line 40: Change `KEYS(11 0)` to `KEYS(16 0)` and `RECORDSIZE(300 300)` to `RECORDSIZE(305 305)`
- `app/jcl/TCATBALF.jcl` line 40: Change `KEYS(17 0)` to `KEYS(22 0)` and `RECORDSIZE(50 50)` to `RECORDSIZE(55 55)`
- `app/jcl/XREFFILE.jcl` line 74: Change AIX `KEYS(11,25)` to `KEYS(16,25)` — verify the offset is still correct after the xref record layout change
- `app/jcl/PRTCATBL.jcl` lines 47-56: Change `TRANCAT-ACCT-ID,1,11,ZD` to `TRANCAT-ACCT-ID,1,16,ZD` and shift all subsequent SYMNAMES field offsets by 5 (TRANCAT-TYPE-CD from 12 to 17, TRANCAT-CD from 14 to 19, TRAN-CAT-BAL from 18 to 23). Update the OUTREC FIELDS accordingly.

**Blocked By:** None
**Blocks:** Task 8

---

### Task 7: Update DB2 Schema and IMS/DB2/MQ Variant

**Summary:** Update DB2 DDL, IMS/DB2/MQ copybooks, BMS maps, and COBOL programs
**Type:** Task
**Priority:** High
**Story Points:** 5
**Description:**
DB2 DDL:
- `app/app-authorization-ims-db2-mq/dcl/AUTHFRDS.dcl` line 49: Change `ACCT_ID DECIMAL(11, 0)` to `ACCT_ID DECIMAL(16, 0)`
- `app/app-authorization-ims-db2-mq/dcl/AUTHFRDS.dcl` line 85: Change `PIC S9(11)V USAGE COMP-3` to `PIC S9(16)V USAGE COMP-3`

IMS/DB2/MQ BMS and copybooks:
- `app/app-authorization-ims-db2-mq/bms/COPAU00.bms` — update account ID field lengths from 11 to 16
- `app/app-authorization-ims-db2-mq/cpy-bms/COPAU00.cpy` — update PIC X(11) to PIC X(16) for account fields
- `app/app-authorization-ims-db2-mq/cpy/CIPAUSMY.cpy` — update account ID field

IMS/DB2/MQ COBOL programs (update all account ID PIC clauses):
- `app/app-authorization-ims-db2-mq/cbl/CBPAUP0C.cbl`
- `app/app-authorization-ims-db2-mq/cbl/COPAUA0C.cbl`
- `app/app-authorization-ims-db2-mq/cbl/COPAUS0C.cbl`
- `app/app-authorization-ims-db2-mq/cbl/COPAUS1C.cbl`
- `app/app-authorization-ims-db2-mq/cbl/COPAUS2C.cbl`
- `app/app-authorization-ims-db2-mq/cbl/DBUNLDGS.CBL`
- `app/app-authorization-ims-db2-mq/cbl/PAUDBLOD.CBL`
- `app/app-authorization-ims-db2-mq/cbl/PAUDBUNL.CBL`

**Blocked By:** Task 1
**Blocks:** Task 9

---

### Task 8: Update VSAM/MQ Variant Programs and Message Structures

**Summary:** Update MQ message structures and VSAM/MQ variant COBOL programs
**Type:** Task
**Priority:** High
**Story Points:** 3
**Description:**
- `app/app-vsam-mq/cbl/COACCT01.cbl` — update account ID fields from PIC X(11)/PIC 9(11) to PIC X(16)/PIC 9(16)
- `app/app-vsam-mq/cbl/CODATE01.cbl` — update account ID fields similarly
- Search for any MQ message structure copybooks in `app/app-vsam-mq/` that define ACCOUNT-NUMBER as PIC X(11) and change to PIC X(16)

**Blocked By:** Task 1
**Blocks:** Task 9

---

### Task 9: Reformat Test Data Files

**Summary:** Reformat all ASCII and EBCDIC test data files to use 16-digit account IDs
**Type:** Task
**Priority:** High
**Story Points:** 5
**Description:**
All data files contain fixed-width records where the account ID occupies 11 positions. Left-pad all existing 11-digit account IDs with 5 zeros ("00000" prefix) and shift all subsequent field data right by 5 positions to maintain correct record layout.

Files to update:
- `app/data/ASCII/acctdata.txt` — account ID in positions 1-11, extend to 1-16
- `app/data/ASCII/tcatbal.txt` — account ID in positions 1-11, extend to 1-16
- `app/data/ASCII/cardxref.txt` — account ID embedded within the record; identify offset and pad
- `app/data/ASCII/trxndata.txt` — if account ID is present, update similarly
- `app/data/ASCII/carddata.txt` — if account ID is present, update similarly
- All corresponding files in `app/data/EBCDIC/` directory

Verify total record lengths match the new RECORDSIZE values defined in the JCL (Task 6).

**Blocked By:** Task 6
**Blocks:** Task 10

---

### Task 10: Update Documentation

**Summary:** Update all documentation references from 11-digit to 16-digit account IDs
**Type:** Task
**Priority:** Medium
**Story Points:** 2
**Description:**
- `app/app-vsam-mq/README.md` lines 116-119: Change `ACCOUNT-NUMBER PIC X(11)` to `PIC X(16)` in the MQ message format documentation
- Search all README.md and other documentation files in the repository for references to "11 digit", "11-digit", "PIC X(11)", "PIC 9(11)" related to account numbers and update to 16
- Add a migration note documenting the change, the data migration approach (left-pad with zeros), and the deployment requirement (all programs must be deployed simultaneously due to COMMAREA layout change)

**Blocked By:** Tasks 4, 5, 7, 8, 9
**Blocks:** None

---

### Task 11: Integration Testing and Validation

**Summary:** Compile all programs, redefine VSAM files, load migrated data, and perform end-to-end testing
**Type:** Task
**Priority:** Highest
**Story Points:** 8
**Description:**
- Compile and link all modified COBOL programs (online and batch)
- Delete and redefine all affected VSAM clusters using updated JCL
- Load reformatted test data into VSAM files
- If DB2 variant: execute ALTER TABLE to change ACCT_ID column to DECIMAL(16,0)
- Verify all CICS transactions work correctly with 16-digit account IDs
- Verify batch jobs run successfully
- Test boundary conditions: account IDs with leading zeros, maximum value (9999999999999999), all-zeros rejection
- Verify screen display alignment on 80-column terminals
- Verify MQ message processing with new field width

**Blocked By:** All other tasks
**Blocks:** None

---

## Task Dependency Summary (Gantt-style ordering)

```
Task 1 (Copybooks) ──┬──> Task 2 (BMS Maps) ──> Task 3 (BMS Copybooks) ──> Task 4 (Online COBOL)
                      ├──> Task 5 (Batch COBOL)
                      ├──> Task 7 (DB2/IMS variant)
                      └──> Task 8 (VSAM/MQ variant)
Task 6 (JCL) ────────────> Task 9 (Test Data)
Tasks 4,5,7,8,9 ─────────> Task 10 (Documentation)
All Tasks ────────────────> Task 11 (Integration Testing)
```

## Labels/Tags

All tasks should be tagged with: `account-id-migration`, `carddemo`, `cobol`, `breaking-change`

## Sprint Estimate

Total story points: ~52 (recommend splitting across 2-3 sprints)
- Sprint 1: Tasks 1, 2, 3, 6 (foundation — 16 points)
- Sprint 2: Tasks 4, 5, 7, 8 (program changes — 21 points)
- Sprint 3: Tasks 9, 10, 11 (data, docs, testing — 15 points)
