# CardDemo Mainframe Migration Plan

> **Application:** CardDemo - Credit Card Management System
> **Source Platform:** IBM Mainframe (COBOL/CICS/VSAM)
> **Target Platform:** AWS Mainframe Modernization (M2) - Micro Focus Runtime
> **Repository:** `ankehao-demo/aws-mainframe-modernization-carddemo`

---

## Overview

This directory contains the migration documentation and artifacts for modernizing the CardDemo mainframe application to AWS. The migration follows a phased approach, moving from discovery through full cloud-native transformation.

## Migration Phases

| Phase | Name | Status | Description |
|-------|------|--------|-------------|
| **0** | [Discovery & Assessment](phase0-discovery.md) | **In Progress** | Complete application inventory, dependency analysis, and complexity assessment |
| **1** | [Replatform Core COBOL/CICS/VSAM](phase1-replatform.md) | **In Progress** | Migrate core CICS transactions, batch programs, and VSAM data to AWS M2 managed runtime |
| **2** | Modernize Data Layer | Planned | Migrate VSAM to Amazon Aurora/DynamoDB, introduce API layer |
| **3** | Refactor to Microservices | Planned | Decompose monolithic COBOL into domain-driven microservices |
| **4** | Cloud-Native Transformation | Planned | Complete re-architecture with serverless, event-driven patterns |

## Documents & Artifacts

### Phase 0: Discovery & Assessment

- **[phase0-discovery.md](phase0-discovery.md)** - Complete inventory and dependency analysis
  - Application inventory (17 online programs, 10 batch programs, 2 assembler)
  - Data store inventory (7+ VSAM KSDS files, 2 AIX paths, 5 GDG bases)
  - Program-to-data dependency matrix
  - Copybook dependency map (28 copybooks)
  - BMS screen map inventory (17 mapsets)
  - Batch job flow (CA7 scheduler chains)
  - Technology complexity assessment
  - Optional module summary (3 modules, out of scope for Phase 1)

### Phase 1: Replatform Core

- **[phase1-replatform.md](phase1-replatform.md)** - Strategy, architecture, and runbook
  - Scope definition (in/out of scope)
  - Target architecture (AWS M2 with Micro Focus runtime)
  - Data migration runbook (EBCDIC to ASCII conversion)
  - CSD resource migration (files, transactions, programs, mapsets)
  - JCL-to-target mapping
  - Batch scheduling migration (CA7 to Step Functions)
  - Validation test plan (38 test cases)

- **[../config/m2-app-definition.json](../../config/m2-app-definition.json)** - AWS M2 application definition
  - CICS listener configuration (TN3270, port 6000)
  - Dataset catalog (10 VSAM KSDS + 2 AIX paths + sequential files)
  - Batch job definitions (POSTTRAN, INTCALC, CREASTMT)
  - Source/load library locations

- **[../../scripts/migrate-vsam-data.sh](../../scripts/migrate-vsam-data.sh)** - VSAM data migration script
  - IDCAMS REPRO export documentation
  - S3 staging upload
  - EBCDIC-to-ASCII conversion (record-aware, using copybook layouts)
  - M2 data store import
  - Alternate index creation
  - Validation (record counts, checksums, key uniqueness)

## Migration Timeline & Checklist

### Phase 0: Discovery & Assessment

- [x] Inventory all COBOL programs in `app/cbl/`
- [x] Map CICS transactions from `app/csd/CARDDEMO.CSD`
- [x] Catalog all VSAM files and record layouts
- [x] Build program-to-data dependency matrix
- [x] Map copybook dependencies
- [x] Document BMS screen maps
- [x] Analyze batch job flow from CA7 scheduler
- [x] Assess technology complexity and migration risks
- [x] Document optional modules (out of scope)
- [ ] Peer review of discovery document
- [ ] Stakeholder sign-off

### Phase 1: Replatform Core

- [x] Define in-scope / out-of-scope boundaries
- [x] Design target architecture on AWS M2
- [x] Create M2 application definition (`config/m2-app-definition.json`)
- [x] Document data migration runbook
- [x] Create data migration script (`scripts/migrate-vsam-data.sh`)
- [x] Map CSD resources to M2 format
- [x] Map JCL jobs to M2 batch definitions
- [x] Design batch scheduling (Step Functions)
- [x] Define validation test plan (38 test cases)
- [ ] Provision AWS M2 environment
- [ ] Compile COBOL programs for Micro Focus runtime
- [ ] Compile BMS maps
- [ ] Execute VSAM data migration
- [ ] Deploy application to M2
- [ ] Execute validation test plan
- [ ] Performance baseline testing
- [ ] Stakeholder sign-off

### Phase 2: Modernize Data Layer (Planned)

- [ ] Design relational schema from VSAM copybooks
- [ ] Implement data access layer (COBOL -> SQL)
- [ ] Migrate VSAM to Aurora PostgreSQL
- [ ] Introduce REST API gateway
- [ ] Update batch programs for SQL access

### Phase 3: Refactor to Microservices (Planned)

- [ ] Domain decomposition (Account, Card, Transaction, User, Statement)
- [ ] Implement Java/Python microservices
- [ ] Containerize with ECS/EKS
- [ ] Implement event-driven communication (EventBridge/SQS)

### Phase 4: Cloud-Native Transformation (Planned)

- [ ] Serverless compute (Lambda)
- [ ] API Gateway for external access
- [ ] DynamoDB for high-throughput data
- [ ] CloudWatch observability
- [ ] Decommission M2 runtime

## Prerequisites

### For Phase 0 (Discovery)

- Access to source repository (`ankehao-demo/aws-mainframe-modernization-carddemo`)
- Understanding of COBOL, CICS, VSAM, JCL concepts

### For Phase 1 (Replatform)

- **AWS Account** with Mainframe Modernization (M2) service access
- **S3 Bucket** for application artifacts and data staging
- **Micro Focus COBOL Compiler** (or AWS M2 build environment) for compiling:
  - COBOL programs (`.cbl` files)
  - BMS maps (`.bms` files)
- **Source Mainframe Access** for:
  - IDCAMS REPRO export of VSAM datasets
  - LISTCAT verification of record counts
- **Network Configuration**:
  - TN3270 terminal emulator for online transaction testing
  - VPC with appropriate security groups for M2 runtime
- **IAM Roles** with permissions for:
  - `m2:*` (Mainframe Modernization service)
  - `s3:GetObject`, `s3:PutObject` (data staging bucket)
  - `states:*` (Step Functions for batch scheduling)
  - `events:*` (EventBridge for scheduling triggers)

## Key References

| Resource | Location |
|----------|----------|
| COBOL programs | `app/cbl/` |
| Copybooks | `app/cpy/` |
| BMS maps | `app/bms/` |
| JCL jobs | `app/jcl/` |
| CSD definitions | `app/csd/CARDDEMO.CSD` |
| CA7 scheduler | `app/scheduler/CardDemo.ca7` |
| VSAM catalog | `app/catlg/LISTCAT.txt` |
| Sample data (EBCDIC) | `app/data/EBCDIC/` |
| M2 app definition | `config/m2-app-definition.json` |
| Data migration script | `scripts/migrate-vsam-data.sh` |

---

*This migration plan is maintained as part of the CardDemo modernization initiative.*
