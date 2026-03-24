# Phase 1: Replatform Core COBOL/CICS/VSAM - CardDemo Application

> **Status:** In Progress
> **Last Updated:** 2026-03-24
> **Prerequisite:** [Phase 0: Discovery & Assessment](phase0-discovery.md)

---

## Table of Contents

1. [Scope Definition](#1-scope-definition)
2. [Target Architecture](#2-target-architecture)
3. [Data Migration Runbook](#3-data-migration-runbook)
4. [CSD Resource Migration](#4-csd-resource-migration)
5. [JCL-to-Target Mapping](#5-jcl-to-target-mapping)
6. [Batch Scheduling Migration](#6-batch-scheduling-migration)
7. [Validation Test Plan](#7-validation-test-plan)

---

## 1. Scope Definition

### 1.1 In Scope

Phase 1 covers the core COBOL/CICS/VSAM application components:

#### CICS Transactions (17)

| Transaction | Program | Function |
|------------|---------|----------|
| CC00 | COSGN00C | User sign-on |
| CM00 | COMEN01C | Main menu |
| CA00 | COADM01C | Admin menu |
| CAVW | COACTVWC | Account view |
| CAUP | COACTUPC | Account update |
| CCLI | COCRDLIC | Card list |
| CCDL | COCRDSLC | Card detail |
| CCUP | COCRDUPC | Card update |
| CT00 | COTRN00C | Transaction list |
| CT01 | COTRN01C | Transaction detail |
| CT02 | COTRN02C | Add transaction |
| CB00 | COBIL00C | Bill payment |
| CR00 | CORPT00C | Reports |
| CU00 | COUSR00C | User list |
| CU01 | COUSR01C | Add user |
| CU02 | COUSR02C | Update user |
| CU03 | COUSR03C | Delete user |

#### Batch Programs (~10)

| Program | Job | Function |
|---------|-----|----------|
| CBTRN02C | POSTTRAN | Post daily transactions |
| CBACT04C | INTCALC | Interest calculation |
| CBSTM03A | CREASTMT | Statement generation (main) |
| CBSTM03B | CREASTMT | Statement generation (I/O sub) |
| CBACT01C | ACCTFILE | Account file load |
| CBACT02C | CARDFILE | Card file load |
| CBACT03C | XREFFILE | Cross-reference file load |
| CBCUS01C | CUSTFILE | Customer file load |
| CBTRN01C | TRANFILE | Transaction file load |
| CBTRN03C | TRANTYPE | Transaction type processing |

#### BMS Maps (17)

All 17 mapsets defined in `app/bms/`: COSGN00, COMEN01, COADM01, COACTVW, COACTUP, COCRDLI, COCRDSL, COCRDUP, COTRN00, COTRN01, COTRN02, COBIL00, CORPT00, COUSR00, COUSR01, COUSR02, COUSR03.

#### Copybooks (28)

All copybooks in `app/cpy/` are in scope (see Phase 0 discovery for full list).

#### VSAM Files (7+ core files)

- ACCTDATA.VSAM.KSDS (accounts)
- CARDDATA.VSAM.KSDS (cards)
- CUSTDATA.VSAM.KSDS (customers)
- CARDXREF.VSAM.KSDS (cross-reference)
- TRANSACT.VSAM.KSDS (transactions)
- USRSEC.VSAM.KSDS (user security)
- TCATBALF.VSAM.KSDS (category balances)
- DISCGRP.VSAM.KSDS (disclosure groups)
- TRANTYPE.VSAM.KSDS (transaction types)
- TRANCATG.VSAM.KSDS (transaction categories)
- Plus AIX paths: CARDDATA.VSAM.AIX.PATH, CARDXREF.VSAM.AIX.PATH

#### Utility Programs (2)

- CSUTLDTC (date/time utility)
- COBSWAIT (wait utility)

### 1.2 Out of Scope

The following are explicitly **excluded** from Phase 1:

| Module | Location | Reason |
|--------|----------|--------|
| Authorization Module (IMS+DB2+MQ) | `app/app-authorization-ims-db2-mq/` | Requires IMS, DB2, and MQ infrastructure not available in base M2 replatform |
| Transaction Type Module (DB2) | `app/app-transaction-type-db2/` | Requires DB2 connectivity |
| VSAM-MQ Module | `app/app-vsam-mq/` | Requires MQ Series infrastructure |
| Assembler programs | `app/asm/COBDATFT.asm`, `app/asm/MVSWAIT.asm` | Require rewrite; deferred to Phase 2 |

### 1.3 Dependencies on Out-of-Scope Items

| In-Scope Item | Dependency | Mitigation |
|--------------|------------|------------|
| CBPAUP0J (triggered by CLOSEFIL in CA7) | Calls CBPAUP0C from IMS module | Skip CBPAUP0J step in batch chain; no impact on core processing |
| TRANTYPE job | May source from DB2 in full system | Use VSAM-only transaction type load via IDCAMS REPRO from PS file |
| Date conversion calls | Some programs may call COBDATFT | Provide COBOL stub or use Micro Focus date intrinsics |

---

## 2. Target Architecture

### 2.1 Platform: AWS Mainframe Modernization (M2)

The target platform is **AWS Mainframe Modernization** using the **Micro Focus (Rocket) replatform runtime**.

```
┌─────────────────────────────────────────────────────────────────┐
│                    AWS Mainframe Modernization                   │
│                                                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────────────────┐ │
│  │  Micro Focus │  │  Managed     │  │  AWS Step Functions   │ │
│  │  CICS Runtime│  │  VSAM Data   │  │  (Batch Orchestration)│ │
│  │              │  │  Store       │  │                       │ │
│  │  17 Txns     │  │  10+ VSAM    │  │  POSTTRAN             │ │
│  │  17 BMS Maps │  │  KSDS files  │  │  INTCALC              │ │
│  │  20 Programs │  │  2 AIX Paths │  │  CREASTMT             │ │
│  │              │  │  5 GDGs      │  │  Data Load Jobs       │ │
│  └──────┬───────┘  └──────┬───────┘  └───────────┬───────────┘ │
│         │                 │                       │             │
│  ┌──────┴─────────────────┴───────────────────────┴───────────┐ │
│  │              M2 Managed Runtime Environment                 │ │
│  │    COBOL Compiled Programs  |  Copybooks  |  Load Library   │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                    Amazon S3                                 │ │
│  │  Source Data (EBCDIC -> ASCII converted)                     │ │
│  │  Statement Output Files                                     │ │
│  │  Batch Input/Output Files                                   │ │
│  └─────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘

External Access:
  ├── TN3270 Terminal Emulator ──> CICS Listener (port 6000)
  └── AWS Console ──> M2 Management Interface
```

### 2.2 Component Mapping

| Source Component | Target Component |
|-----------------|-----------------|
| CICS Region | M2 Micro Focus CICS Runtime |
| CICS Transactions | M2 Transaction Definitions |
| BMS Maps | Compiled BMS (loaded into runtime) |
| VSAM KSDS Files | M2 Managed VSAM Data Store |
| VSAM AIX/PATH | M2 Managed Alternate Indexes |
| JCL Batch Jobs | M2 Batch Job Definitions / Step Functions |
| CA7 Scheduler | AWS Step Functions + EventBridge Scheduler |
| COBOL Load Library | M2 Application Bundle (compiled .gnt/.int files) |
| GDG Bases | S3 versioned objects or M2 GDG support |
| SDSF (CLOSEFIL/OPENFIL) | M2 File Management API |
| SORT Utility | Micro Focus MFSORT or equivalent |
| IDCAMS | M2 Dataset Management utilities |

### 2.3 Network Architecture

| Endpoint | Port | Protocol | Purpose |
|----------|------|----------|---------|
| CICS Listener | 6000 | TN3270 | Terminal access to online transactions |
| M2 Management | 443 | HTTPS | AWS Console / API management |
| S3 Data Store | 443 | HTTPS | Batch data input/output |

---

## 3. Data Migration Runbook

### 3.1 Overview

All VSAM datasets must be converted from **EBCDIC** encoding to **ASCII** encoding for the Micro Focus runtime on AWS M2. Record layouts are preserved; only character encoding changes.

### 3.2 Dataset Migration Steps

For each VSAM dataset, follow this sequence:

1. **Export** from source mainframe using IDCAMS REPRO to sequential (PS) format
2. **Transfer** PS file to AWS (S3 or EC2 staging area)
3. **Convert** encoding from EBCDIC to ASCII using record-aware conversion
4. **Load** into M2 managed VSAM data store using M2 import utilities
5. **Validate** record counts and key integrity

### 3.3 Record Layout Reference

Use these copybooks for record-aware EBCDIC-to-ASCII conversion. Only character fields (PIC X, PIC A) are converted; numeric fields (PIC 9, COMP, COMP-3) are preserved as-is.

| Dataset | Copybook | Record Length | Key Field | Key Length |
|---------|----------|--------------|-----------|------------|
| ACCTDATA | `app/cpy/CVACT01Y.cpy` | 300 bytes | ACCT-ID (PIC 9(11)) | 11 |
| CARDDATA | `app/cpy/CVACT02Y.cpy` | 150 bytes | CARD-NUM (PIC X(16)) | 16 |
| CUSTDATA | `app/cpy/CVCUS01Y.cpy` | 500 bytes | CUST-ID (PIC 9(09)) | 9 |
| CARDXREF | `app/cpy/CVACT03Y.cpy` | 50 bytes | XREF-CARD-NUM (PIC X(16)) | 16 |
| TRANSACT | `app/cpy/CVTRA05Y.cpy` | 350 bytes | TRAN-ID (PIC X(16)) | 16 |
| USRSEC | `app/cpy/CSUSR01Y.cpy` | 80 bytes | SEC-USR-ID (PIC X(08)) | 8 |
| DALYTRAN | `app/cpy/CVTRA06Y.cpy` | 350 bytes | DALYTRAN-ID (PIC X(16)) | 16 |
| TCATBALF | `app/cpy/CVTRA01Y.cpy` | (varies) | (see copybook) | (varies) |
| DISCGRP | `app/cpy/CVTRA02Y.cpy` | (varies) | (see copybook) | (varies) |
| TRANTYPE | `app/cpy/CVTRA03Y.cpy` | (varies) | (see copybook) | (varies) |
| TRANCATG | `app/cpy/CVTRA04Y.cpy` | (varies) | (see copybook) | (varies) |

### 3.4 Sample Data Files

Pre-existing sample data in both encodings is available in `app/data/`:

| Directory | Encoding | Files |
|-----------|----------|-------|
| `app/data/EBCDIC/` | EBCDIC | AWS.M2.CARDDEMO.ACCTDATA.PS, CARDDATA.PS, CUSTDATA.PS, etc. |
| `app/data/ASCII/` | ASCII | Pre-converted equivalents (if available) |

### 3.5 LISTCAT Reference

The complete dataset catalog is documented in `app/catlg/LISTCAT.txt`. Key statistics from the catalog:

| Dataset | Record Count | Key Length | Avg Record Length | CI Size |
|---------|-------------|------------|-------------------|---------|
| ACCTDATA.VSAM.KSDS | 50 | 11 | 300 | 18,432 |
| CARDDATA.VSAM.KSDS | 50 | 16 | 150 | 18,432 |
| CUSTDATA.VSAM.KSDS | (see LISTCAT) | 9 | 500 | (varies) |
| CARDXREF.VSAM.KSDS | (see LISTCAT) | 16 | 50 | (varies) |
| TRANSACT.VSAM.KSDS | (see LISTCAT) | 16 | 350 | (varies) |
| USRSEC.VSAM.KSDS | (see LISTCAT) | 8 | 80 | (varies) |

### 3.6 Alternate Index Migration

Two alternate indexes must be recreated on the target platform:

1. **CARDDATA.VSAM.AIX** (CSD name: CARDAIX)
   - Base cluster: `CARDDATA.VSAM.KSDS`
   - PATH: `CARDDATA.VSAM.AIX.PATH`
   - Purpose: Access cards by account ID
   - Attributes: UPGRADE

2. **CARDXREF.VSAM.AIX** (CSD name: CXACAIX)
   - Base cluster: `CARDXREF.VSAM.KSDS`
   - PATH: `CARDXREF.VSAM.AIX.PATH`
   - Purpose: Access cross-references by account key
   - Attributes: UPGRADE

---

## 4. CSD Resource Migration

### 4.1 Source CSD Overview

The `app/csd/CARDDEMO.CSD` file defines all CICS resources under the group **CARDDEMO**. These must be converted to the M2 Micro Focus runtime resource definition format.

### 4.2 FILE Definitions

Convert each `DEFINE FILE` entry to the target runtime's file definition:

| CSD File | DSNAME | Target Config Key | Notes |
|----------|--------|-------------------|-------|
| ACCTDAT | AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS | `datasets.ACCTDAT` | KSDS, SHARE, R/W |
| CARDDAT | AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS | `datasets.CARDDAT` | KSDS, SHARE, R/W |
| CUSTDAT | AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS | `datasets.CUSTDAT` | KSDS, SHARE, R/W |
| CCXREF | AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS | `datasets.CCXREF` | KSDS, SHARE, R/W |
| TRANSACT | AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS | `datasets.TRANSACT` | KSDS, SHARE, R/W |
| USRSEC | AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS | `datasets.USRSEC` | KSDS, SHARE, R/W |
| CARDAIX | AWS.M2.CARDDEMO.CARDDATA.VSAM.AIX.PATH | `datasets.CARDAIX` | AIX PATH |
| CXACAIX | AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX.PATH | `datasets.CXACAIX` | AIX PATH |

**Common CSD file attributes to preserve:**
- `DISPOSITION(SHARE)` -> Shared access mode
- `ADD(YES) BROWSE(YES) DELETE(YES) READ(YES) UPDATE(YES)` -> Full access
- `OPENTIME(FIRSTREF)` -> Open on first reference
- `LSRPOOLNUM(1)` -> LSR pool assignment
- `RECORDFORMAT(V)` -> Variable-length records

### 4.3 TRANSACTION Definitions

Convert each `DEFINE TRANSACTION` to M2 transaction configuration:

| CSD Txn | Program | Profile | Priority | Key Settings |
|---------|---------|---------|----------|-------------|
| CC00 | COSGN00C | DFHCICST | 1 | SPURGE(YES), TPURGE(YES) |
| CM00 | COMEN01C | DFHCICST | 1 | SPURGE(YES), TPURGE(YES) |
| CA00 | COADM01C | DFHCICST | 1 | SPURGE(YES), TPURGE(YES) |
| CAVW | COACTVWC | DFHCICST | 1 | SPURGE(YES), TPURGE(YES) |
| CAUP | COACTUPC | DFHCICST | 1 | SPURGE(YES), TPURGE(YES) |
| CCLI | COCRDLIC | DFHCICST | 1 | SPURGE(YES), TPURGE(YES) |
| CCDL | COCRDSLC | DFHCICST | 1 | SPURGE(YES), TPURGE(YES) |
| CCUP | COCRDUPC | DFHCICST | 1 | SPURGE(YES), TPURGE(YES) |
| CT00 | COTRN00C | DFHCICST | 1 | SPURGE(YES), TPURGE(YES) |
| CT01 | COTRN01C | DFHCICST | 1 | SPURGE(YES), TPURGE(YES) |
| CT02 | COTRN02C | DFHCICST | 1 | SPURGE(YES), TPURGE(YES) |
| CB00 | COBIL00C | DFHCICST | 1 | SPURGE(YES), TPURGE(YES) |
| CR00 | CORPT00C | DFHCICST | 1 | SPURGE(YES), TPURGE(YES) |
| CU00 | COUSR00C | DFHCICST | 1 | SPURGE(YES), TPURGE(YES) |
| CU01 | COUSR01C | DFHCICST | 1 | SPURGE(YES), TPURGE(YES) |
| CU02 | COUSR02C | DFHCICST | 1 | SPURGE(YES), TPURGE(YES) |
| CU03 | COUSR03C | DFHCICST | 1 | SPURGE(YES), TPURGE(YES) |

**Additional transaction (developer):**
- CDV1 -> COCRDSEC (developer/search transaction)

### 4.4 PROGRAM Definitions

All programs in the CSD `DEFINE PROGRAM` entries map to compiled COBOL modules in the M2 load library:

- **Language:** COBOL (all programs)
- **EXECKEY:** USER
- **CONCURRENCY:** QUASIRENT
- **API:** CICSAPI
- **Status:** ENABLED

### 4.5 MAPSET Definitions

All 17 mapsets map directly to compiled BMS modules:

- **USAGE:** NORMAL
- **STATUS:** ENABLED
- **Compilation:** BMS maps must be compiled with the Micro Focus BMS compiler

### 4.6 LIBRARY Definitions

| CSD Library | DSNAME | Target |
|------------|--------|--------|
| CARDDLIB | AWS.M2.CARDDEMO.LOADLIB | M2 application bundle load path |
| COM2DOLL | AWS.M2.CARDDEMO.LOADLIB | Disabled (backup reference) |

### 4.7 TDQUEUE Definition

| Queue | Type | Purpose | Target |
|-------|------|---------|--------|
| JOBS | Extrapartition (OUTPUT) | Submit batch jobs from CICS via internal reader | Map to M2 batch submission API or EventBridge |

---

## 5. JCL-to-Target Mapping

### 5.1 Core Batch Jobs

#### POSTTRAN (Post Daily Transactions)

| Attribute | Source (JCL) | Target (M2 Batch) |
|-----------|-------------|-------------------|
| Job Name | POSTTRAN | posttran |
| Program | CBTRN02C | CBTRN02C (compiled) |
| Step | STEP15 | Single step |
| Input: TRANFILE | `TRANSACT.VSAM.KSDS` (SHR) | M2 VSAM dataset |
| Input: DALYTRAN | `DALYTRAN.PS` (SHR) | S3 object or M2 PS dataset |
| Input: XREFFILE | `CARDXREF.VSAM.KSDS` (SHR) | M2 VSAM dataset |
| Input: ACCTFILE | `ACCTDATA.VSAM.KSDS` (SHR) | M2 VSAM dataset |
| Input: TCATBALF | `TCATBALF.VSAM.KSDS` (SHR) | M2 VSAM dataset |
| Output: DALYREJS | `DALYREJS(+1)` (NEW, GDG) | S3 versioned object |
| LOADLIB | `AWS.M2.CARDDEMO.LOADLIB` | M2 application bundle |

#### INTCALC (Interest Calculation)

| Attribute | Source (JCL) | Target (M2 Batch) |
|-----------|-------------|-------------------|
| Job Name | INTCALC | intcalc |
| Program | CBACT04C | CBACT04C (compiled) |
| PARM | `2022071800` (processing date) | Runtime parameter |
| Input: TCATBALF | `TCATBALF.VSAM.KSDS` | M2 VSAM dataset |
| Input: XREFFILE | `CARDXREF.VSAM.KSDS` | M2 VSAM dataset |
| Input: XREFFIL1 | `CARDXREF.VSAM.AIX.PATH` | M2 VSAM AIX path |
| Input: ACCTFILE | `ACCTDATA.VSAM.KSDS` | M2 VSAM dataset |
| Input: DISCGRP | `DISCGRP.VSAM.KSDS` | M2 VSAM dataset |
| Output: TRANSACT | `SYSTRAN(+1)` (GDG) | S3 versioned object |

#### CREASTMT (Create Statements - Multi-Step)

| Step | Source Program | Source Action | Target Equivalent |
|------|---------------|---------------|-------------------|
| DELDEF01 | IDCAMS | Delete/define TRXFL temp VSAM | M2 IDCAMS equivalent or pre-step script |
| STEP010 | SORT | Sort TRANSACT by card+tran key | Micro Focus MFSORT |
| STEP020 | IDCAMS REPRO | Load sorted data to TRXFL VSAM | M2 IDCAMS REPRO |
| STEP030 | IEFBR14 | Delete previous statement files | File cleanup step |
| STEP040 | CBSTM03A | Generate statements | CBSTM03A (compiled) |

**STEP040 DD Mapping:**

| DD Name | Source Dataset | Target |
|---------|---------------|--------|
| TRNXFILE | `TRXFL.VSAM.KSDS` | M2 temp VSAM |
| XREFFILE | `CARDXREF.VSAM.KSDS` | M2 VSAM dataset |
| ACCTFILE | `ACCTDATA.VSAM.KSDS` | M2 VSAM dataset |
| CUSTFILE | `CUSTDATA.VSAM.KSDS` | M2 VSAM dataset |
| STMTFILE | `STATEMNT.PS` (NEW) | S3 object |
| HTMLFILE | `STATEMNT.HTML` (NEW) | S3 object |

**COND Codes:** Steps 020-040 have `COND=(0,NE)` - execute only if all prior steps return CC=0. Map to Step Functions choice states.

### 5.2 File Management Jobs

#### CLOSEFIL / OPENFIL

| Attribute | Source | Target |
|-----------|--------|--------|
| Method | SDSF console commands (`CEMT SET FILE ... CLO/OPE`) | M2 File Management API calls |
| Files | TRANSACT, CCXREF, ACCTDAT, CXACAIX, USRSEC | Same logical files in M2 |

These jobs must be replaced with M2 API calls to disable/enable file access during batch windows.

### 5.3 Data Load Jobs

| JCL Job | Source Method | Target Method |
|---------|-------------|---------------|
| ACCTFILE | COBOL program CBACT01C reads PS, writes VSAM | M2 batch: run CBACT01C or use IDCAMS REPRO |
| CARDFILE | COBOL program CBACT02C reads PS, writes VSAM | M2 batch: run CBACT02C or use IDCAMS REPRO |
| CUSTFILE | COBOL program CBCUS01C reads PS, writes VSAM | M2 batch: run CBCUS01C or use IDCAMS REPRO |
| XREFFILE | COBOL program CBACT03C reads PS, writes VSAM | M2 batch: run CBACT03C or use IDCAMS REPRO |
| TRANFILE | COBOL program CBTRN01C reads PS, writes VSAM | M2 batch: run CBTRN01C or use IDCAMS REPRO |
| DUSRSECJ | IDCAMS REPRO from PS to VSAM | M2 IDCAMS equivalent |
| DEFVSAM | IDCAMS DEFINE CLUSTER | M2 dataset definition |
| TRANIDX | IDCAMS DEFINE AIX/BLDINDEX | M2 alternate index setup |
| DEFGDGB | IDCAMS DEFINE GDG | M2 GDG or S3 versioning |

### 5.4 Utility Jobs

| JCL Job | Purpose | Target |
|---------|---------|--------|
| TRANBKP | Backup TRANSACT to GDG | S3 copy with versioning |
| COMBTRAN | SORT/merge daily transaction files | MFSORT equivalent |
| TRANREPT | Transaction reporting | M2 batch or reporting service |
| PRTCATBL | Print category balance report | M2 batch output |
| TXT2PDF1 | Convert text statements to PDF | Lambda function or container |
| WAITSTEP | Synchronization step | Step Functions wait state |

---

## 6. Batch Scheduling Migration

### 6.1 Source: CA7 Scheduler

The CA7 scheduler definitions in `app/scheduler/CardDemo.ca7` define job chains using:
- **SCHID** (Schedule IDs): 030 (primary), 031 (branch 1), 032 (branch 2)
- **Completion triggers**: Job completion triggers the next job
- **JCLLIB**: `&CARDDEMOPRODJCL` (JCL library reference)

### 6.2 Target: AWS Step Functions + EventBridge

#### Nightly Batch State Machine

```json
{
  "Comment": "CardDemo Nightly Batch Processing",
  "StartAt": "CloseFiles",
  "States": {
    "CloseFiles": {
      "Type": "Task",
      "Comment": "Close CICS files for batch access",
      "Next": "PostTransactions"
    },
    "PostTransactions": {
      "Type": "Task",
      "Comment": "POSTTRAN - CBTRN02C",
      "Next": "ParallelRefresh"
    },
    "ParallelRefresh": {
      "Type": "Parallel",
      "Comment": "Parallel category and balance refresh",
      "Branches": [
        {
          "StartAt": "LoadTransactionCategories",
          "States": {
            "LoadTransactionCategories": {
              "Type": "Task",
              "Comment": "TRANCATG",
              "End": true
            }
          }
        },
        {
          "StartAt": "LoadCategoryBalances",
          "States": {
            "LoadCategoryBalances": {
              "Type": "Task",
              "Comment": "TCATBALF",
              "End": true
            }
          }
        }
      ],
      "Next": "InterestCalculation"
    },
    "InterestCalculation": {
      "Type": "Task",
      "Comment": "INTCALC - CBACT04C",
      "Next": "CreateStatements"
    },
    "CreateStatements": {
      "Type": "Task",
      "Comment": "CREASTMT - CBSTM03A (multi-step)",
      "Next": "OpenFiles"
    },
    "OpenFiles": {
      "Type": "Task",
      "Comment": "Reopen CICS files for online access",
      "End": true
    }
  }
}
```

#### EventBridge Schedule

| Schedule | Frequency | State Machine | Notes |
|----------|-----------|---------------|-------|
| CardDemo-Nightly | Daily at 07:00 UTC | CardDemo-NightlyBatch | Primary batch chain |
| CardDemo-DataVerify | Daily at 08:00 UTC | CardDemo-DataVerification | READACCT->READCARD->READCUST->READXREF |
| CardDemo-Statements | Daily at 09:00 UTC | CardDemo-StatementGeneration | CREASTMT->TXT2PDF1 |
| CardDemo-Reports | Weekly (Saturday) | CardDemo-WeeklyReports | PRTCATBL |

### 6.3 Migration Steps

1. **Map CA7 job dependencies** to Step Functions state transitions
2. **Convert SCHID branches** (031, 032) to Step Functions Parallel states
3. **Map completion triggers** to Step Functions Next/Choice states
4. **Convert WAITSTEP** sync points to Step Functions barrier/join patterns
5. **Set up EventBridge rules** for daily/weekly/monthly schedules
6. **Configure error handling** using Step Functions Catch/Retry

---

## 7. Validation Test Plan

### 7.1 Acceptance Criteria

All of the following must pass before Phase 1 is considered complete:

#### Online Transaction Tests

| Test ID | Transaction | Test Description | Expected Result |
|---------|------------|------------------|-----------------|
| OT-001 | CC00 | Sign on with USER0001/PASSWORD | Successful authentication, main menu displayed |
| OT-002 | CC00 | Sign on with ADMIN001/PASSWORD | Successful authentication, admin menu displayed |
| OT-003 | CC00 | Sign on with invalid credentials | Error message, remain on sign-on screen |
| OT-004 | CM00 | Navigate main menu options | All menu options accessible |
| OT-005 | CA00 | Navigate admin menu options | All admin options accessible |
| OT-006 | CAVW | View account details | Account data displayed correctly |
| OT-007 | CAUP | Update account, then view | Updated data persisted and visible |
| OT-008 | CCLI | List cards for an account | Card list displayed with correct data |
| OT-009 | CCDL | View card details | Card details match source data |
| OT-010 | CCUP | Update card information | Update persisted correctly |
| OT-011 | CT00 | List transactions | Transaction list displayed |
| OT-012 | CT01 | View transaction detail | Transaction details match source |
| OT-013 | CT02 | Add new transaction | Transaction created, visible in list |
| OT-014 | CB00 | Process bill payment | Payment recorded against account |
| OT-015 | CR00 | Generate transaction report | Report generated without errors |
| OT-016 | CU00 | List users (admin) | User list displayed |
| OT-017 | CU01 | Add new user (admin) | User created, visible in list |
| OT-018 | CU02 | Update user (admin) | User updated successfully |
| OT-019 | CU03 | Delete user (admin) | User removed from list |

#### Batch Processing Tests

| Test ID | Job | Test Description | Expected Result |
|---------|-----|------------------|-----------------|
| BT-001 | CLOSEFIL | Close CICS files | Files closed, CICS returns DISABLED status |
| BT-002 | POSTTRAN | Post daily transactions | Transactions posted, balances updated, rejects written to GDG |
| BT-003 | INTCALC | Calculate interest | Interest computed, system transactions generated |
| BT-004 | CREASTMT | Generate statements | HTML and text statements generated for all accounts |
| BT-005 | OPENFIL | Reopen CICS files | Files reopened, CICS returns ENABLED status |
| BT-006 | Full chain | Run complete nightly batch | CLOSEFIL->POSTTRAN->INTCALC->CREASTMT->OPENFIL completes RC=0 |

#### Data Integrity Tests

| Test ID | Test Description | Expected Result |
|---------|------------------|-----------------|
| DI-001 | VSAM record count comparison | Record counts match pre-migration counts for all datasets |
| DI-002 | Account data round-trip | View account (CAVW), update (CAUP), re-view: data integrity maintained |
| DI-003 | Card cross-reference integrity | CCXREF links resolve to valid ACCTDAT and CUSTDAT records |
| DI-004 | Transaction master integrity | All TRANSACT records have valid card numbers in CCXREF |
| DI-005 | User security validation | USRSEC records authenticate correctly via CC00 sign-on |
| DI-006 | Alternate index verification | CARDAIX and CXACAIX return correct records via alternate keys |
| DI-007 | GDG generation verification | Batch outputs create new GDG generations correctly |
| DI-008 | Character encoding validation | No EBCDIC artifacts in ASCII-converted character fields |

#### Performance Baseline Tests

| Test ID | Test Description | Baseline Metric |
|---------|------------------|-----------------|
| PT-001 | CC00 sign-on response time | < 2 seconds |
| PT-002 | CAVW account view response time | < 2 seconds |
| PT-003 | CT00 transaction list (50 records) | < 3 seconds |
| PT-004 | POSTTRAN batch throughput | Process 1000 transactions < 60 seconds |
| PT-005 | CREASTMT statement generation | Generate 50 statements < 120 seconds |

### 7.2 Test Data Requirements

| Dataset | Minimum Records | Source |
|---------|----------------|--------|
| ACCTDATA | 50 accounts | `app/data/EBCDIC/AWS.M2.CARDDEMO.ACCTDATA.PS` |
| CARDDATA | 50 cards | `app/data/EBCDIC/AWS.M2.CARDDEMO.CARDDATA.PS` |
| CUSTDATA | 50 customers | `app/data/EBCDIC/AWS.M2.CARDDEMO.CUSTDATA.PS` |
| CARDXREF | 50 cross-refs | `app/data/EBCDIC/AWS.M2.CARDDEMO.CARDXREF.PS` |
| TRANSACT | 100+ transactions | `app/data/EBCDIC/AWS.M2.CARDDEMO.TRANSACT.PS` |
| USRSEC | 10+ users | `app/data/EBCDIC/AWS.M2.CARDDEMO.USRSEC.PS` |
| DALYTRAN | 50+ daily txns | `app/data/EBCDIC/AWS.M2.CARDDEMO.DALYTRAN.PS` |

### 7.3 Sign-off Criteria

Phase 1 migration is complete when:

- [ ] All 19 online transaction tests pass (OT-001 through OT-019)
- [ ] All 6 batch processing tests pass (BT-001 through BT-006)
- [ ] All 8 data integrity tests pass (DI-001 through DI-008)
- [ ] All 5 performance baseline tests meet thresholds (PT-001 through PT-005)
- [ ] No ABEND or S0C7 errors during 24-hour soak test
- [ ] Batch chain completes end-to-end with RC=0
- [ ] Online transactions accessible via TN3270 terminal emulator

---

*Document generated as part of Phase 1: Replatform Core COBOL/CICS/VSAM for the CardDemo mainframe modernization initiative.*
