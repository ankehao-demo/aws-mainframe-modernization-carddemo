# Jira Epic: Modernize Account ID from 11 Digits to 16 Digits

---

## Epic

**Epic Title:** Modernize Account ID from 11 Digits to 16 Digits
**Epic Key:** CARDDEMO-ACCTMOD
**Priority:** High
**Epic Description:**
The CardDemo application currently stores and processes customer account IDs as 11-digit numbers (`PIC 9(11)`). This epic covers all work required to expand the account ID field to 16 digits (`PIC 9(16)`) across the entire application stack — including shared copybooks, BMS screen maps, COBOL programs (batch and online), DB2 declarations, VSAM file definitions, data migration, and documentation. The change touches ~40+ source files and requires a coordinated data migration for existing VSAM and DB2 data.

**Acceptance Criteria:**
- All account ID fields across the application accept, store, and display 16-digit values
- All existing 11-digit account data is migrated (left-padded with zeros) to 16-digit format
- All validation messages reference "16 digit" instead of "11 digit"
- All BMS screens render correctly within 80-column terminal width
- All batch jobs process files with the new 16-digit key layout
- All programs compile cleanly with no size mismatch errors
- Documentation is updated to reflect the new format

---

## Subtasks

### Subtask 1: Update Shared Copybooks
**Key:** CARDDEMO-ACCTMOD-01
**Type:** Subtask
**Priority:** Highest (blocking — all other code changes depend on this)
**Story Points:** 3
**Description:**
Update the four shared copybooks that define the account ID data structure. These are included by multiple programs, so changes here propagate widely.

**Files to modify:**
1. `app/cpy/COCOM01Y.cpy` — Line 38: Change `10 CDEMO-ACCT-ID PIC 9(11).` → `PIC 9(16).`
2. `app/cpy/CVCRD01Y.cpy` — Line 34: Change `10 CC-ACCT-ID PIC X(11)` → `PIC X(16)`. Line 36: Change `10 CC-ACCT-ID-N REDEFINES CC-ACCT-ID PIC 9(11).` → `PIC 9(16).`
3. `app/cpy/CVTRA07Y.cpy` — Line 18: Change `05 TRAN-REPORT-ACCOUNT-ID PIC X(11).` → `PIC X(16).` Also update the header field width from `PIC X(12)` to `PIC X(17)` and adjust FILLER fields to maintain report width.
4. `app/app-authorization-ims-db2-mq/cpy/CIPAUSMY.cpy` — Line 19: Change `05 PA-ACCT-ID PIC S9(11) COMP-3.` → `PIC S9(16) COMP-3.` Reduce FILLER on line 31 by 3 bytes to maintain segment size (packed decimal grows from 6 to 9 bytes).

**Acceptance Criteria:**
- All four copybooks compile cleanly
- COMMAREA layout (COCOM01Y) is consistent across all including programs
- Report layout (CVTRA07Y) header and detail lines align correctly

**Blocked by:** None
**Blocks:** Subtasks 3, 4, 5, 6

---

### Subtask 2: Update DB2 Declarations
**Key:** CARDDEMO-ACCTMOD-02
**Type:** Subtask
**Priority:** High
**Story Points:** 2
**Description:**
Update the DB2 table declaration and create an ALTER TABLE migration script.

**Files to modify:**
1. `app/app-authorization-ims-db2-mq/dcl/AUTHFRDS.dcl` — Line 49: Change `ACCT_ID DECIMAL(11, 0),` → `DECIMAL(16, 0),`. Line 85: Change `10 ACCT-ID PIC S9(11)V USAGE COMP-3.` → `PIC S9(16)V USAGE COMP-3.`

**Additional deliverable:**
- Create a DB2 ALTER TABLE DDL script to migrate the production column from DECIMAL(11,0) to DECIMAL(16,0) and update existing data with leading zeros.

**Acceptance Criteria:**
- DCL file compiles cleanly
- ALTER TABLE script tested successfully against a test DB2 instance

**Blocked by:** None
**Blocks:** Subtask 5 (Authorization module programs)

---

### Subtask 3: Update BMS Screen Maps
**Key:** CARDDEMO-ACCTMOD-03
**Type:** Subtask
**Priority:** High
**Story Points:** 5
**Description:**
Update all 8 BMS map source files to change account ID field lengths from 11 to 16. Adjust field positions (POS) on each screen to accommodate the extra 5 characters while staying within 80-column terminal width.

**Files to modify:**
1. `app/bms/COACTVW.bms` — ACCTSID: LENGTH=11→16, PICIN='99999999999'→'9999999999999999'. Shift subsequent fields right by 5.
2. `app/bms/COACTUP.bms` — ACCTSID: LENGTH=11→16. Shift stopper field from POS=(5,50) to (5,55).
3. `app/bms/COCRDLI.bms` — ACCTSID: LENGTH=11→16. ACCTNO1-ACCTNO7: all LENGTH=11→16. Adjust column positions for list rows and header separator line widths.
4. `app/bms/COCRDSL.bms` — ACCTSID: LENGTH=11→16. Adjust stopper field.
5. `app/bms/COCRDUP.bms` — ACCTSID: LENGTH=11→16. Adjust stopper field.
6. `app/bms/COTRN02.bms` — ACTIDIN: LENGTH=11→16. Shift "(or)" label and Card # field right by 5.
7. `app/bms/COBIL00.bms` — ACTIDIN: LENGTH=11→16. Adjust stopper field.
8. `app/app-authorization-ims-db2-mq/bms/COPAU00.bms` — ACCTID: LENGTH=11→16. Shift stopper from POS=(5,31) to (5,36).

**Acceptance Criteria:**
- All BMS maps assemble cleanly
- No field overlaps on any screen
- All screens fit within 80x24 terminal dimensions

**Blocked by:** None
**Blocks:** Subtask 4

---

### Subtask 4: Regenerate BMS Generated Copybooks
**Key:** CARDDEMO-ACCTMOD-04
**Type:** Subtask
**Priority:** High
**Story Points:** 3
**Description:**
After BMS maps are updated (Subtask 3), regenerate or manually update the generated copybooks in `app/cpy-bms/`. All account ID fields (ACCTSIDI, ACCTNO1I-ACCTNO7I, ACTIDIN, ACCTIDI, etc.) must change from `PIC X(11)` to `PIC X(16)`.

**Files to modify/regenerate:**
1. `app/cpy-bms/COACTUP.CPY`
2. `app/cpy-bms/COACTVW.CPY`
3. `app/cpy-bms/COBIL00.CPY`
4. `app/cpy-bms/COCRDLI.CPY`
5. `app/cpy-bms/COCRDSL.CPY`
6. `app/cpy-bms/COCRDUP.CPY`
7. `app/cpy-bms/COTRN02.CPY`
8. `app/app-authorization-ims-db2-mq/cpy-bms/COPAU00.cpy`

**Acceptance Criteria:**
- Generated copybooks match the updated BMS maps exactly
- All COBOL programs that COPY these files compile without errors

**Blocked by:** Subtask 3
**Blocks:** Subtasks 5, 6

---

### Subtask 5: Update Online CICS COBOL Programs
**Key:** CARDDEMO-ACCTMOD-05
**Type:** Subtask
**Priority:** High
**Story Points:** 8
**Description:**
Update all online (CICS) COBOL programs that define local account ID fields as `PIC 9(11)` or `PIC X(11)`. Also update all validation error messages from "11 digit" to "16 digit".

**Files to modify:**
1. `app/cbl/COACTUPC.cbl` — Change all local ACCT-ID fields from PIC 9(11)/X(11) to 9(16)/X(16). Update ~6 validation messages from "11 digit" to "16 digit".
2. `app/cbl/COACTVWC.cbl` — Same field and message changes (~6 occurrences).
3. `app/cbl/COCRDLIC.cbl` — Change account ID field sizes.
4. `app/cbl/COCRDSLC.cbl` — Change field sizes and ~4 validation messages.
5. `app/cbl/COCRDUPC.cbl` — Change field sizes and ~4 validation messages.
6. `app/cbl/COTRN02C.cbl` — Change account ID field sizes.
7. `app/cbl/COBIL00C.cbl` — Change account ID field sizes.
8. `app/cbl/CORPT00C.cbl` — Change account ID field sizes.
9. `app/cbl/CSUTLDTC.cbl` — Change any account ID fields.

**Important:** Only change fields that are account-ID-related. Not every `PIC 9(11)` is an account ID — grep for field names like FD-ACCT-ID, CDEMO-ACCT-ID, CC-ACCT-ID, etc.

**Acceptance Criteria:**
- All programs compile cleanly
- No validation message still references "11 digit"
- Account ID input/output works correctly on all screens

**Blocked by:** Subtasks 1, 4
**Blocks:** Subtask 9

---

### Subtask 6: Update Batch COBOL Programs
**Key:** CARDDEMO-ACCTMOD-06
**Type:** Subtask
**Priority:** High
**Story Points:** 5
**Description:**
Update all batch COBOL programs that define local account ID fields in FD (file descriptor) sections and working storage.

**Files to modify:**
1. `app/cbl/CBACT01C.cbl` — Lines 54, 58, 73: Change FD-ACCT-ID, OUT-ACCT-ID, ARR-ACCT-ID from `PIC 9(11)` → `PIC 9(16)`.
2. `app/cbl/CBACT04C.cbl` — All ACCT-ID `PIC 9(11)` fields → `PIC 9(16)`.
3. `app/cbl/CBTRN01C.cbl` — Account ID fields in transaction file records.
4. `app/cbl/CBTRN02C.cbl` — Account ID fields in transaction file records.
5. `app/cbl/CBTRN03C.cbl` — Account ID fields.
6. `app/cbl/CBSTM03A.CBL` — Statement generation account ID fields.
7. `app/cbl/CBSTM03B.CBL` — Statement generation account ID fields.

**Acceptance Criteria:**
- All batch programs compile cleanly
- File I/O works with new 16-digit key record layouts

**Blocked by:** Subtasks 1, 4
**Blocks:** Subtask 9

---

### Subtask 7: Update Authorization and VSAM-MQ Module Programs
**Key:** CARDDEMO-ACCTMOD-07
**Type:** Subtask
**Priority:** High
**Story Points:** 5
**Description:**
Update all programs in the authorization (IMS-DB2-MQ) and VSAM-MQ sub-applications.

**Files to modify (Authorization module):**
1. `app/app-authorization-ims-db2-mq/cbl/COPAUS0C.cbl`
2. `app/app-authorization-ims-db2-mq/cbl/COPAUA0C.cbl`
3. `app/app-authorization-ims-db2-mq/cbl/COPAUS1C.cbl`
4. `app/app-authorization-ims-db2-mq/cbl/COPAUS2C.cbl`
5. `app/app-authorization-ims-db2-mq/cbl/CBPAUP0C.cbl`
6. `app/app-authorization-ims-db2-mq/cbl/DBUNLDGS.CBL`
7. `app/app-authorization-ims-db2-mq/cbl/PAUDBLOD.CBL`
8. `app/app-authorization-ims-db2-mq/cbl/PAUDBUNL.CBL`

**Files to modify (VSAM-MQ module):**
9. `app/app-vsam-mq/cbl/COACCT01.cbl`
10. `app/app-vsam-mq/cbl/CODATE01.cbl`

Change all `PIC 9(11)` and `PIC X(11)` account ID fields to `PIC 9(16)` / `PIC X(16)`.

**Acceptance Criteria:**
- All programs compile cleanly
- Authorization flow works end-to-end with 16-digit account IDs

**Blocked by:** Subtasks 1, 2, 4
**Blocks:** Subtask 9

---

### Subtask 8: Update VSAM File Definitions, JCL, and Data Migration
**Key:** CARDDEMO-ACCTMOD-08
**Type:** Subtask
**Priority:** High
**Story Points:** 8
**Description:**
Update VSAM cluster definitions and create a data migration batch job.

**Tasks:**
1. Update VSAM KSDS cluster definition JCL for ACCTFILE: Change KEYS(11,0) → KEYS(16,0). Increase RECORDSIZE by 5 bytes (from 300 to 305).
2. Update any other VSAM file definitions that embed account IDs (XREFFILE, TRANFILE) — adjust record layouts and key definitions.
3. Update `app/jcl/PRTCATBL.jcl` — adjust field positions in SORT/report control cards.
4. Create a new data migration batch job that:
   - Reads old ACCTFILE records (11-digit key)
   - Left-pads account IDs with zeros to 16 digits (e.g., `00012345678` → `0000000012345678`)
   - Writes to a new VSAM cluster with KEYS(16,0)
   - Performs the same migration for CARDXREF and TRANSACT files
5. Update test data in `app/jcl/ESDSRRDS.jcl` and any other seed data JCL to use 16-digit account IDs.

**Acceptance Criteria:**
- New VSAM clusters defined successfully
- Migration job runs without errors
- All migrated records readable by updated programs
- Test data loads successfully with 16-digit format

**Blocked by:** None (can be done in parallel with code changes)
**Blocks:** Subtask 9

---

### Subtask 9: Integration Testing
**Key:** CARDDEMO-ACCTMOD-09
**Type:** Subtask
**Priority:** High
**Story Points:** 8
**Description:**
End-to-end testing of the complete modernized application.

**Test plan:**
1. Recompile all BMS maps and verify generated copybooks match expected field sizes.
2. Recompile all COBOL programs — resolve any compiler errors from size mismatches.
3. Redefine VSAM clusters with new key lengths.
4. Run data migration job to convert test data.
5. Test online screens:
   - Account view (COACTVW) — verify 16-digit input and display
   - Account update (COACTUP) — verify 16-digit input and update
   - Card listing (COCRDLI) — verify 16-digit account column alignment
   - Card selection/update (COCRDSL/COCRDUP) — verify 16-digit flow
   - Transaction add (COTRN02) — verify 16-digit account input
   - Bill payment (COBIL00) — verify 16-digit account input
   - All screens fit within 80-column terminal width
6. Test batch jobs:
   - CBACT01C, CBACT04C — account file processing
   - CBTRN01C, CBTRN02C, CBTRN03C — transaction processing
   - CBSTM03A, CBSTM03B — statement generation
7. Test reports: verify column alignment in CVTRA07Y report output.
8. Test COMMAREA flow: verify account ID passes correctly between programs via COCOM01Y.
9. Test authorization module end-to-end with 16-digit account IDs.

**Acceptance Criteria:**
- All online transactions work correctly with 16-digit account IDs
- All batch jobs complete successfully
- Reports are properly formatted
- No truncation or data corruption observed

**Blocked by:** Subtasks 1-8
**Blocks:** Subtask 10

---

### Subtask 10: Update Documentation
**Key:** CARDDEMO-ACCTMOD-10
**Type:** Subtask
**Priority:** Medium
**Story Points:** 1
**Description:**
Update all documentation to reflect the new 16-digit account ID format.

**Files to modify:**
1. `README.md` — Update any references to account number format.
2. `app/app-vsam-mq/README.md` — Update references to `PIC X(11)` account fields.

**Acceptance Criteria:**
- All documentation accurately describes 16-digit account IDs
- No references to "11-digit" account numbers remain in documentation

**Blocked by:** Subtask 9
**Blocks:** None

---

## Total Estimated Story Points: 48

## Dependency Graph

```
Subtask 1 (Copybooks) ──┐
Subtask 2 (DB2)         ├──→ Subtask 5 (Online COBOL)  ──┐
Subtask 3 (BMS Maps) ───┤    Subtask 6 (Batch COBOL)   ──┤
         │               ├──→ Subtask 7 (Auth/VSAM-MQ)  ──├──→ Subtask 9 (Testing) → Subtask 10 (Docs)
         ▼               │                                 │
Subtask 4 (BMS CPY) ────┘    Subtask 8 (VSAM/Migration) ──┘
```

Subtasks 1, 2, 3, and 8 can begin in parallel. Subtask 4 depends on 3. Subtasks 5, 6, 7 depend on 1 and 4 (and 7 also on 2). Subtask 9 depends on all code changes. Subtask 10 depends on 9.
