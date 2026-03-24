# Phase 0: Discovery & Assessment - CardDemo Application

> **Status:** In Progress
> **Last Updated:** 2026-03-24
> **Source Repository:** `ankehao-demo/aws-mainframe-modernization-carddemo`

---

## Table of Contents

1. [Application Inventory](#1-application-inventory)
2. [Data Store Inventory](#2-data-store-inventory)
3. [Program-to-Data Dependency Matrix](#3-program-to-data-dependency-matrix)
4. [Copybook Dependency Map](#4-copybook-dependency-map)
5. [BMS Screen Map Inventory](#5-bms-screen-map-inventory)
6. [Batch Job Flow](#6-batch-job-flow)
7. [Technology Complexity Assessment](#7-technology-complexity-assessment)
8. [Optional Module Summary](#8-optional-module-summary)

---

## 1. Application Inventory

### 1.1 Online Programs (CICS)

The following COBOL programs are deployed as CICS transactions, defined in `app/csd/CARDDEMO.CSD`.

| Program | Transaction ID | Description | BMS Mapset | Source File |
|---------|---------------|-------------|------------|-------------|
| COSGN00C | CC00 | User sign-on / authentication | COSGN00 | `app/cbl/COSGN00C.cbl` |
| COMEN01C | CM00 | Main menu (regular users) | COMEN01 | `app/cbl/COMEN01C.cbl` |
| COADM01C | CA00 | Admin menu (privileged users) | COADM01 | `app/cbl/COADM01C.cbl` |
| COACTVWC | CAVW | Account view | COACTVW | `app/cbl/COACTVWC.cbl` |
| COACTUPC | CAUP | Account update | COACTUP | `app/cbl/COACTUPC.cbl` |
| COCRDLIC | CCLI | Credit card list | COCRDLI | `app/cbl/COCRDLIC.cbl` |
| COCRDSLC | CCDL | Credit card detail / search | COCRDSL | `app/cbl/COCRDSLC.cbl` |
| COCRDUPC | CCUP | Credit card update | COCRDUP | `app/cbl/COCRDUPC.cbl` |
| COTRN00C | CT00 | Transaction list | COTRN00 | `app/cbl/COTRN00C.cbl` |
| COTRN01C | CT01 | Transaction detail view | COTRN01 | `app/cbl/COTRN01C.cbl` |
| COTRN02C | CT02 | Add new transaction | COTRN02 | `app/cbl/COTRN02C.cbl` |
| COBIL00C | CB00 | Bill payment processing | COBIL00 | `app/cbl/COBIL00C.cbl` |
| CORPT00C | CR00 | Transaction reports | CORPT00 | `app/cbl/CORPT00C.cbl` |
| COUSR00C | CU00 | User list (admin) | COUSR00 | `app/cbl/COUSR00C.cbl` |
| COUSR01C | CU01 | Add user (admin) | COUSR01 | `app/cbl/COUSR01C.cbl` |
| COUSR02C | CU02 | Update user (admin) | COUSR02 | `app/cbl/COUSR02C.cbl` |
| COUSR03C | CU03 | Delete user (admin) | COUSR03 | `app/cbl/COUSR03C.cbl` |

**Additional CSD-defined programs (no direct transaction):**

| Program | Description | Source File |
|---------|-------------|-------------|
| COCRDSEC | Credit card search helper | (referenced in CSD, txn CDV1) |
| CSUTLDTC | Date/time utility | `app/cbl/CSUTLDTC.cbl` |
| COBSWAIT | Wait/sync utility | `app/cbl/COBSWAIT.cbl` |

**Total online programs:** 17 primary CICS transactions + 3 utility programs

### 1.2 Batch Programs

| Program | JCL Job | Description | Source File |
|---------|---------|-------------|-------------|
| CBTRN02C | POSTTRAN | Post daily transactions, update balances & category balances | `app/cbl/CBTRN02C.cbl` |
| CBACT04C | INTCALC | Interest calculation on account balances | `app/cbl/CBACT04C.cbl` |
| CBSTM03A | CREASTMT | Statement generation (main program) | `app/cbl/CBSTM03A.CBL` |
| CBSTM03B | CREASTMT | Statement generation (file I/O subroutine called by CBSTM03A) | `app/cbl/CBSTM03B.CBL` |
| CBACT01C | ACCTFILE | Account data file processing | `app/cbl/CBACT01C.cbl` |
| CBACT02C | CARDFILE | Card data file processing | `app/cbl/CBACT02C.cbl` |
| CBACT03C | XREFFILE | Card cross-reference file processing | `app/cbl/CBACT03C.cbl` |
| CBCUS01C | CUSTFILE | Customer data file processing | `app/cbl/CBCUS01C.cbl` |
| CBTRN01C | TRANFILE | Transaction master file load | `app/cbl/CBTRN01C.cbl` |
| CBTRN03C | TRANTYPE | Transaction type/category processing | `app/cbl/CBTRN03C.cbl` |

**Total batch programs:** 10 COBOL programs

### 1.3 Assembler Programs

| Program | Description | Source File |
|---------|-------------|-------------|
| COBDATFT | Date format conversion utility | `app/asm/COBDATFT.asm` |
| MVSWAIT | MVS wait/delay utility | `app/asm/MVSWAIT.asm` |

> **Migration Note:** These two assembler programs require special handling during migration. They must be replaced with equivalent runtime services or rewritten in COBOL for the target platform.

---

## 2. Data Store Inventory

### 2.1 VSAM Files (from CSD Definitions)

All VSAM files are defined in `app/csd/CARDDEMO.CSD` under the `CARDDEMO` group.

| CSD File Name | DSNAME | Type | Description | Access Modes |
|---------------|--------|------|-------------|--------------|
| ACCTDAT | `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS` | KSDS | Account master data | Read, Write, Update, Delete, Browse |
| CARDDAT | `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS` | KSDS | Card master data | Read, Write, Update, Delete, Browse |
| CUSTDAT | `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS` | KSDS | Customer master data | Read, Write, Update, Delete, Browse |
| CCXREF | `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS` | KSDS | Card-to-Account cross-reference | Read, Write, Update, Delete, Browse |
| TRANSACT | `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS` | KSDS | Transaction master | Read, Write, Update, Delete, Browse |
| USRSEC | `AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS` | KSDS | User security/credentials | Read, Write, Update, Delete, Browse |
| CARDAIX | `AWS.M2.CARDDEMO.CARDDATA.VSAM.AIX.PATH` | AIX PATH | Alternate index path for CARDDATA (by account) | Read, Write, Update, Delete, Browse |
| CXACAIX | `AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX.PATH` | AIX PATH | Alternate index path for CCXREF (by account key) | Read, Write, Update, Delete, Browse |

### 2.2 Additional VSAM Files (from JCL/Batch References)

These files are referenced in JCL jobs but not defined in the CSD (batch-only access):

| Dataset Name | Type | Referenced By | Description |
|-------------|------|---------------|-------------|
| `AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS` | KSDS | POSTTRAN, INTCALC | Transaction category balance file |
| `AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS` | KSDS | INTCALC | Disclosure group definitions |
| `AWS.M2.CARDDEMO.DALYTRAN.PS` | PS (Sequential) | POSTTRAN | Daily transaction input file |
| `AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS` | KSDS | TRANTYPE | Transaction type definitions |
| `AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS` | KSDS | TRANCATG | Transaction category definitions |

### 2.3 VSAM File Record Layouts

Record layouts are defined in copybooks under `app/cpy/`:

| Dataset | Copybook | Record Name | Record Length | Key Field | Key Length | Key Offset |
|---------|----------|-------------|---------------|-----------|------------|------------|
| ACCTDATA | `CVACT01Y.cpy` | ACCOUNT-RECORD | 300 bytes | ACCT-ID (PIC 9(11)) | 11 | 0 |
| CARDDATA | `CVACT02Y.cpy` | CARD-RECORD | 150 bytes | CARD-NUM (PIC X(16)) | 16 | 0 |
| CUSTDATA | `CVCUS01Y.cpy` | CUSTOMER-RECORD | 500 bytes | CUST-ID (PIC 9(09)) | 9 | 0 |
| CARDXREF | `CVACT03Y.cpy` | CARD-XREF-RECORD | 50 bytes | XREF-CARD-NUM (PIC X(16)) | 16 | 0 |
| TRANSACT | `CVTRA05Y.cpy` | TRAN-RECORD | 350 bytes | TRAN-ID (PIC X(16)) | 16 | 0 |
| USRSEC | `CSUSR01Y.cpy` | SEC-USER-DATA | 80 bytes | SEC-USR-ID (PIC X(08)) | 8 | 0 |
| DALYTRAN | `CVTRA06Y.cpy` | DALYTRAN-RECORD | 350 bytes | DALYTRAN-ID (PIC X(16)) | 16 | 0 |

### 2.4 Generation Data Groups (GDG)

From `app/catlg/LISTCAT.txt`:

| GDG Base | Description | Referenced By |
|----------|-------------|---------------|
| `AWS.M2.CARDDEMO.DALYREJS` | Daily rejected transactions | POSTTRAN |
| `AWS.M2.CARDDEMO.SYSTRAN` | System-generated transactions | INTCALC |
| `AWS.M2.CARDDEMO.TRANSACT.BKUP` | Transaction backup generations | TRANBKP |
| `AWS.M2.CARDDEMO.TRANSACT.DALY` | Daily transaction input generations | POSTTRAN |
| `AWS.M2.CARDDEMO.TRANREPT` | Transaction reports | TRANREPT |

### 2.5 Non-VSAM Datasets

| Dataset Name | Type | Description |
|-------------|------|-------------|
| `AWS.M2.CARDDEMO.ACCTDATA.PS` | PS | Account data (sequential/flat file) |
| `AWS.M2.CARDDEMO.CARDDATA.PS` | PS | Card data (sequential/flat file) |
| `AWS.M2.CARDDEMO.CUSTDATA.PS` | PS | Customer data (sequential/flat file) |
| `AWS.M2.CARDDEMO.LOADLIB` | PDS | Load library for compiled programs |
| `AWS.M2.CARDDEMO.BMS` | PDS | BMS map source library |
| `AWS.M2.CARDDEMO.BIND` | PDS | Bind/link library |
| `AWS.M2.CARDDEMO.STATEMNT.HTML` | PS | Statement output (HTML format) |
| `AWS.M2.CARDDEMO.STATEMNT.PS` | PS | Statement output (text format) |

---

## 3. Program-to-Data Dependency Matrix

### 3.1 Online Programs (CICS) to VSAM File Dependencies

Derived from COBOL source `FILE-CONTROL` / `FD` sections and `EXEC CICS READ/WRITE FILE(...)` statements:

| Program | ACCTDAT | CARDDAT | CUSTDAT | CCXREF | CXACAIX | TRANSACT | USRSEC | CARDAIX |
|---------|---------|---------|---------|--------|---------|----------|--------|---------|
| COSGN00C | | | | | | | R | |
| COMEN01C | | | | | | | | |
| COADM01C | | | | | | | | |
| COACTVWC | R | R | R | R | | | | R |
| COACTUPC | R/W | | R | R | | | | |
| COCRDLIC | | R | | | R | | | |
| COCRDSLC | | R | R | | | | | |
| COCRDUPC | | R/W | R | | | | | |
| COTRN00C | | | | | | R | | |
| COTRN01C | | | | | | R | | |
| COTRN02C | R | | | R | | R/W | | |
| COBIL00C | R | | | R | | R | | |
| CORPT00C | | | | | | R | | |
| COUSR00C | | | | | | | R | |
| COUSR01C | | | | | | | R/W | |
| COUSR02C | | | | | | | R/W | |
| COUSR03C | | | | | | | R/W | |

**Legend:** R = Read, W = Write, R/W = Read and Write

### 3.2 Batch Programs to Dataset Dependencies

Derived from JCL DD statements in `app/jcl/`:

| Program (JCL Job) | Input Datasets | Output Datasets |
|-------------------|----------------|-----------------|
| CBTRN02C (POSTTRAN) | TRANSACT.VSAM.KSDS (R/W), DALYTRAN.PS (R), CARDXREF.VSAM.KSDS (R), ACCTDATA.VSAM.KSDS (R/W), TCATBALF.VSAM.KSDS (R/W) | DALYREJS(+1) |
| CBACT04C (INTCALC) | TCATBALF.VSAM.KSDS (R), CARDXREF.VSAM.KSDS (R), CARDXREF.VSAM.AIX.PATH (R), ACCTDATA.VSAM.KSDS (R/W), DISCGRP.VSAM.KSDS (R) | SYSTRAN(+1) |
| CBSTM03A (CREASTMT) | TRANSACT.VSAM.KSDS (R), CARDXREF.VSAM.KSDS (R), ACCTDATA.VSAM.KSDS (R), CUSTDATA.VSAM.KSDS (R) | STATEMNT.PS, STATEMNT.HTML |
| CLOSEFIL | (CICS console commands) | Closes: TRANSACT, CCXREF, ACCTDAT, CXACAIX, USRSEC |
| OPENFIL | (CICS console commands) | Opens: TRANSACT, CCXREF, ACCTDAT, CXACAIX, USRSEC |

### 3.3 Utility/Data Load Jobs

| JCL Job | Program | Input | Output |
|---------|---------|-------|--------|
| ACCTFILE | CBACT01C | ACCTDATA.PS | ACCTDATA.VSAM.KSDS |
| CARDFILE | CBACT02C | CARDDATA.PS | CARDDATA.VSAM.KSDS |
| CUSTFILE | CBCUS01C | CUSTDATA.PS | CUSTDATA.VSAM.KSDS |
| XREFFILE | CBACT03C | CARDXREF.PS | CARDXREF.VSAM.KSDS |
| TRANFILE | CBTRN01C | TRANSACT.PS | TRANSACT.VSAM.KSDS |
| DUSRSECJ | IDCAMS | USRSEC.PS | USRSEC.VSAM.KSDS |
| TRANTYPE | CBTRN03C | (varies) | TRANTYPE.VSAM.KSDS |
| TRANCATG | IDCAMS | TRANCATG.PS | TRANCATG.VSAM.KSDS |
| TCATBALF | IDCAMS | TCATBALF.PS | TCATBALF.VSAM.KSDS |
| DISCGRP | IDCAMS | DISCGRP.PS | DISCGRP.VSAM.KSDS |
| TRANBKP | IDCAMS | TRANSACT.VSAM.KSDS | TRANSACT.BKUP(+1) |
| TRANIDX | IDCAMS | (define AIX) | TRANSACT.VSAM.AIX, CARDDATA.VSAM.AIX |
| DEFGDGB | IDCAMS | (define GDG bases) | GDG base entries |
| COMBTRAN | SORT | DALYTRAN files | Combined DALYTRAN.PS |

---

## 4. Copybook Dependency Map

### 4.1 Application Copybooks (`app/cpy/`)

| Copybook | Description | Used By Programs |
|----------|-------------|-----------------|
| COCOM01Y | CICS COMMAREA structure | COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| COTTL01Y | Screen title constants | COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| CSDAT01Y | Date formatting structure | COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| CSMSG01Y | Message area structure | COACTUPC, COACTVWC, COADM01C, COBIL00C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, CORPT00C, COSGN00C, COTRN00C, COTRN01C, COTRN02C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| CSMSG02Y | Extended message area | COACTUPC, COACTVWC, COCRDSLC, COCRDUPC |
| CSUSR01Y | User security record layout | COACTUPC, COACTVWC, COADM01C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C, COSGN00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| CVACT01Y | Account record (300 bytes) | CBACT01C, CBACT04C, CBSTM03A, CBTRN01C, CBTRN02C, COACTUPC, COACTVWC, COBIL00C, COTRN02C |
| CVACT02Y | Card record (150 bytes) | CBACT02C, CBTRN01C, COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC |
| CVACT03Y | Card cross-reference (50 bytes) | CBACT03C, CBACT04C, CBSTM03A, CBTRN01C, CBTRN02C, CBTRN03C, COACTUPC, COACTVWC, COBIL00C, COTRN02C |
| CVCUS01Y | Customer record (500 bytes) | CBCUS01C, CBTRN01C, COACTUPC, COACTVWC, COCRDSLC, COCRDUPC |
| CVTRA05Y | Transaction record (350 bytes) | CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, COBIL00C, CORPT00C, COTRN00C, COTRN01C, COTRN02C |
| CVTRA06Y | Daily transaction record (350 bytes) | CBTRN01C, CBTRN02C |
| CVTRA01Y | Transaction category balance | CBACT04C, CBTRN02C |
| CVTRA02Y | Disclosure group data | CBACT04C |
| CVTRA03Y | Transaction type definitions | CBTRN03C |
| CVTRA04Y | Transaction category definitions | CBTRN03C |
| CVTRA07Y | Transaction type extended | CBTRN03C |
| CVCRD01Y | Card display structure | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC |
| COADM02Y | Admin menu options | COADM01C |
| COMEN02Y | Main menu options | COMEN01C |
| CODATECN | Date conversion routines | CBACT01C |
| COSTM01 | Statement record layout | CBSTM03A |
| CUSTREC | Customer record (stmt variant) | CBSTM03A |
| CSLKPCDY | Lookup code structure | COACTUPC |
| CSSETATY | Set attribute utility (REPLACING) | COACTUPC (x36 instances) |
| CSSTRPFY | String strip/format utility | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC |
| CSUTLDPY | Utility display paragraph | COACTUPC |
| CSUTLDWY | Utility working storage | COACTUPC |
| UNUSED1Y | Unused placeholder | (none) |

### 4.2 System Copybooks (CICS-provided)

| Copybook | Description | Used By |
|----------|-------------|---------|
| DFHAID | CICS attention identifier definitions | All online programs |
| DFHBMSCA | BMS attribute constants | All online programs |

---

## 5. BMS Screen Map Inventory

All BMS maps are located in `app/bms/` and defined as MAPSETs in `app/csd/CARDDEMO.CSD`.

| BMS Mapset | Map Name | Associated Program | Transaction | Description |
|------------|----------|-------------------|-------------|-------------|
| COSGN00 | COSGN0A | COSGN00C | CC00 | Sign-on screen |
| COMEN01 | COMEN1A | COMEN01C | CM00 | Main menu |
| COADM01 | COADM1A | COADM01C | CA00 | Admin menu |
| COACTVW | COACTVW | COACTVWC | CAVW | Account view |
| COACTUP | COACTUP | COACTUPC | CAUP | Account update |
| COCRDLI | COCRDLI | COCRDLIC | CCLI | Card list |
| COCRDSL | COCRDSL | COCRDSLC | CCDL | Card detail/search |
| COCRDUP | COCRDUP | COCRDUPC | CCUP | Card update |
| COTRN00 | COTRN0A | COTRN00C | CT00 | Transaction list |
| COTRN01 | COTRN1A | COTRN01C | CT01 | Transaction detail |
| COTRN02 | COTRN2A | COTRN02C | CT02 | Add transaction |
| COBIL00 | COBIL0A | COBIL00C | CB00 | Bill payment |
| CORPT00 | CORPT0A | CORPT00C | CR00 | Reports |
| COUSR00 | COUSR0A | COUSR00C | CU00 | User list |
| COUSR01 | COUSR1A | COUSR01C | CU01 | Add user |
| COUSR02 | COUSR2A | COUSR02C | CU02 | Update user |
| COUSR03 | COUSR3A | COUSR03C | CU03 | Delete user |

**Total BMS mapsets:** 17

---

## 6. Batch Job Flow

### 6.1 Nightly Batch Sequence (from `app/scheduler/CardDemo.ca7`)

The CA7 scheduler defines the following primary nightly batch chain on Schedule ID 030:

```
CLOSEFIL ──> CBPAUP0J ──> POSTTRAN ──> WAITSTEP ──> OPENFIL
                                             │
                                             └──> (Branch SCHID 031) CLOSEFIL1 ──> TRANCATG ──> WAITSTEP ──> CLOSEFIL ──> ...
                                             └──> (Branch SCHID 032) CLOSEFIL2 ──> TCATBALF ──> WAITSTEP ──> CLOSEFIL ──> ...
```

#### Primary Chain (SCHID 030):

| Step | Job Name | JCL Member | Program | Description | Triggers |
|------|----------|------------|---------|-------------|----------|
| 1 | CLOSEFIL | CLOSEFIL | SDSF | Close CICS files for exclusive batch access | CBPAUP0J |
| 2 | CBPAUP0J | CBPAUP0J | CBPAUP0C | Purge expired authorizations (optional IMS) | POSTTRAN |
| 3 | POSTTRAN | POSTTRAN | CBTRN02C | Post daily transactions to master files | WAITSTEP |
| 4 | WAITSTEP | WAITSTEP | (sync) | Synchronization point | OPENFIL |
| 5 | OPENFIL | OPENFIL | SDSF | Reopen CICS files for online access | (end of chain) |

#### Secondary Chain - Transaction Type Refresh (SCHID 030, parallel branch):

After CLOSEFIL, the following also triggers:

| Step | Job Name | Program | Description | Triggers |
|------|----------|---------|-------------|----------|
| 1 | TRANTYPE | CBTRN03C | Refresh transaction type VSAM from DB2 | WAITSTEP |
| 2 | WAITSTEP | (sync) | Synchronization | CLOSEFIL1/CLOSEFIL2 (branches) |

#### Branch SCHID 031:

| Step | Job Name | Description | Triggers |
|------|----------|-------------|----------|
| 1 | CLOSEFIL1 | Close files | TRANCATG |
| 2 | TRANCATG | Load transaction categories | WAITSTEP |
| 3 | WAITSTEP | Sync | CLOSEFIL -> READACCT chain |

#### Branch SCHID 032:

| Step | Job Name | Description | Triggers |
|------|----------|-------------|----------|
| 1 | CLOSEFIL2 | Close files | TCATBALF |
| 2 | TCATBALF | Load category balances | WAITSTEP |
| 3 | WAITSTEP | Sync | CLOSEFIL -> READACCT chain |

#### Data Verification Chain (SCHID 030, after branches converge):

```
CLOSEFIL ──> READACCT ──> READCARD ──> READCUST ──> READXREF ──> WAITSTEP ──> OPENFIL
```

#### Statement Generation Chain (SCHID 030):

```
CLOSEFIL ──> CREASTMT ──> TXT2PDF1 ──> WAITSTEP ──> OPENFIL
```

#### Report Chain (SCHID 031):

```
CLOSEFIL ──> PRTCATBL ──> WAITSTEP ──> OPENFIL
```

### 6.2 JCL Step Details

#### CLOSEFIL.jcl
- **Step:** CLCIFIL
- **Program:** SDSF (console command interface)
- **Action:** Issues CEMT SET FILE(...) CLOSE commands for TRANSACT, CCXREF, ACCTDAT, CXACAIX, USRSEC

#### POSTTRAN.jcl
- **Step:** STEP15
- **Program:** CBTRN02C
- **DD Statements:**
  - TRANFILE -> `TRANSACT.VSAM.KSDS` (SHR)
  - DALYTRAN -> `DALYTRAN.PS` (SHR)
  - XREFFILE -> `CARDXREF.VSAM.KSDS` (SHR)
  - DALYREJS -> `DALYREJS(+1)` (NEW, GDG)
  - ACCTFILE -> `ACCTDATA.VSAM.KSDS` (SHR)
  - TCATBALF -> `TCATBALF.VSAM.KSDS` (SHR)

#### INTCALC.jcl
- **Step:** STEP15
- **Program:** CBACT04C
- **PARM:** `2022071800` (processing date)
- **DD Statements:**
  - TCATBALF -> `TCATBALF.VSAM.KSDS` (SHR)
  - XREFFILE -> `CARDXREF.VSAM.KSDS` (SHR)
  - XREFFIL1 -> `CARDXREF.VSAM.AIX.PATH` (SHR)
  - ACCTFILE -> `ACCTDATA.VSAM.KSDS` (SHR)
  - DISCGRP -> `DISCGRP.VSAM.KSDS` (SHR)
  - TRANSACT -> `SYSTRAN(+1)` (NEW, GDG)

#### CREASTMT.JCL (Multi-step)
- **Step DELDEF01:** IDCAMS - Delete/define temporary TRXFL VSAM cluster
- **Step STEP010:** SORT - Sort TRANSACT by card number, create sequential copy
- **Step STEP020:** IDCAMS REPRO - Load sorted data into TRXFL VSAM
- **Step STEP030:** IEFBR14 - Delete previous statement output files
- **Step STEP040:** CBSTM03A - Generate statements
  - TRNXFILE -> `TRXFL.VSAM.KSDS` (SHR)
  - XREFFILE -> `CARDXREF.VSAM.KSDS` (SHR)
  - ACCTFILE -> `ACCTDATA.VSAM.KSDS` (SHR)
  - CUSTFILE -> `CUSTDATA.VSAM.KSDS` (SHR)
  - STMTFILE -> `STATEMNT.PS` (NEW)
  - HTMLFILE -> `STATEMNT.HTML` (NEW)
- **COND codes:** Steps 020-040 execute only if prior steps return CC=0

#### OPENFIL.jcl
- **Step:** OPCIFIL
- **Program:** SDSF
- **Action:** Issues CEMT SET FILE(...) OPEN commands for TRANSACT, CCXREF, ACCTDAT, CXACAIX, USRSEC

---

## 7. Technology Complexity Assessment

### 7.1 Technology Matrix

| Technology | Programs Using It | Complexity | Migration Impact |
|-----------|------------------|------------|-----------------|
| **COBOL** | All 29 programs | Standard | Direct replatform support |
| **CICS** | 17 online programs | Standard | Managed CICS runtime on M2 |
| **VSAM KSDS** | All programs | Standard | Managed VSAM on M2 |
| **VSAM AIX/PATH** | COACTVWC, COCRDLIC, CBACT04C | Moderate | Requires AIX definition migration |
| **BMS Maps** | 17 online programs | Standard | Direct replatform support |
| **JCL** | 10+ batch jobs | Standard | Convert to M2 batch definitions |
| **SORT utility** | CREASTMT, COMBTRAN | Standard | Platform SORT equivalent |
| **IDCAMS** | Multiple JCL jobs | Standard | M2 dataset management |
| **SDSF** | CLOSEFIL, OPENFIL | Low | Replace with M2 file management API |
| **GDG** | POSTTRAN, INTCALC, TRANBKP | Moderate | Map to versioned datasets |
| **Assembler** | COBDATFT, MVSWAIT | **High** | Requires rewrite or replacement |
| **CA7 Scheduler** | Batch orchestration | Moderate | Convert to AWS Step Functions |

### 7.2 Risk Assessment

| Risk Area | Level | Details |
|-----------|-------|---------|
| Assembler programs | **HIGH** | COBDATFT (date conversion) and MVSWAIT (wait utility) require rewrite. COBDATFT logic must be replicated in COBOL or target runtime service. MVSWAIT can be replaced with runtime-specific wait mechanism. |
| SDSF console commands | MEDIUM | CLOSEFIL/OPENFIL use SDSF to issue CEMT commands. Must be replaced with M2 API calls or equivalent file management mechanism. |
| GDG management | MEDIUM | Generation Data Groups need mapping to target platform versioning. 5 GDG bases identified. |
| SORT utility | LOW | Standard sort operations; most target platforms provide equivalent. |
| Inter-program CALL | LOW | CBSTM03A calls CBSTM03B. Standard COBOL CALL; supported on all platforms. |

### 7.3 Lines of Code Summary

| Category | Count | Estimated LOC |
|----------|-------|---------------|
| Online COBOL programs | 17 | ~25,000 |
| Batch COBOL programs | 10 | ~8,000 |
| Utility COBOL programs | 2 | ~500 |
| Assembler programs | 2 | ~200 |
| BMS maps | 17 | ~3,000 |
| JCL jobs | 36 | ~1,500 |
| Copybooks | 28 | ~500 |
| **Total** | **112 artifacts** | **~38,700** |

---

## 8. Optional Module Summary

Three optional modules extend the core CardDemo application with additional technology integrations. These are **out of scope for Phase 1** migration.

### 8.1 Authorization Module (IMS + DB2 + MQ)

- **Location:** `app/app-authorization-ims-db2-mq/`
- **Technologies:** IMS DB (HIDAM), DB2, MQ Series, CICS
- **Programs:** COPAUA0C (MQ auth processor), COPAUS0C/COPAUS1C (auth views), COPAUS2C (fraud marking), CBPAUP0C (batch purge)
- **BMS Maps:** COPAU00, COPAU01
- **Transactions:** CP00, CPVS, CPVD
- **IMS Database:** PSBPAUTB (PSB), DBPAUTP0/DBPAUTX0 (HIDAM databases)
- **DB2 Table:** AUTHFRDS (fraud tracking)
- **MQ Queues:** `AWS.M2.CARDDEMO.PAUTH.REQUEST`, `AWS.M2.CARDDEMO.PAUTH.REPLY`
- **Migration Complexity:** **Very High** - requires IMS, DB2, and MQ infrastructure

### 8.2 Transaction Type Module (DB2)

- **Location:** `app/app-transaction-type-db2/`
- **Technologies:** DB2, CICS
- **Programs:** COTRTUPC (transaction type update), COTRTLIC (transaction type list)
- **BMS Maps:** Additional maps for type management
- **Transactions:** CTTU, CTLI
- **DB2 Tables:** Transaction type reference tables
- **Migration Complexity:** **High** - requires DB2 database migration

### 8.3 VSAM-MQ Module

- **Location:** `app/app-vsam-mq/`
- **Technologies:** VSAM, MQ Series, CICS
- **Programs:** CODATE01 (date request via MQ), COACCT01 (account query via MQ)
- **Transactions:** CDRD, CDRA
- **Migration Complexity:** **High** - requires MQ infrastructure

---

## Summary Statistics

| Metric | Count |
|--------|-------|
| Core CICS Transactions | 17 |
| Batch Programs | 10 |
| Assembler Programs | 2 |
| BMS Screen Maps | 17 |
| VSAM KSDS Files | 7+ core, 3+ reference |
| VSAM AIX/PATH | 2 (CARDAIX, CXACAIX) |
| Copybooks | 28 |
| JCL Jobs | 36 |
| GDG Bases | 5 |
| Optional Modules (out of scope) | 3 |
| Estimated Total LOC | ~38,700 |

---

*Document generated as part of Phase 0: Discovery & Assessment for the CardDemo mainframe modernization initiative.*
