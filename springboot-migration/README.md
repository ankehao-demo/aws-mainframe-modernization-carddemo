# CardDemo Spring Boot Migration

Migration of the CardDemo COBOL/CICS mainframe application to a Spring Boot 3.x microservice architecture.

## Architecture Overview

The monolithic COBOL/CICS application has been decomposed into the following microservices:

```
┌─────────────────────────────────────────────────────────┐
│                    API Gateway (:8080)                   │
│              Spring Cloud Gateway routing                │
└────┬──────┬──────┬──────┬──────┬──────┬────────────────┘
     │      │      │      │      │      │
┌────▼──┐┌──▼───┐┌─▼──┐┌──▼───┐┌─▼──┐┌──▼───┐
│ Auth  ││Acct  ││Card││Trans ││Bill││Report│
│:8081  ││:8082 ││:8083││:8084 ││:8085││:8086│
└───┬───┘└──┬───┘└─┬──┘└──┬───┘└─┬──┘└──┬───┘
    └───────┴──────┴──────┴──────┴──────┘
                      │
              ┌───────▼───────┐
              │  PostgreSQL   │
              │    :5432      │
              └───────────────┘
```

### Module Overview

| Module | Port | Description | COBOL Source |
|--------|------|-------------|--------------|
| `common-lib` | — | Shared entities, DTOs, repositories, mappers | Copybooks in `app/cpy/` |
| `auth-service` | 8081 | Authentication + user management | COSGN00C, COUSR00C-03C |
| `account-service` | 8082 | Account + customer management | COACTVWC, COACTUPC |
| `card-service` | 8083 | Card management | COCRDLIC, COCRDSLC, COCRDUPC |
| `transaction-service` | 8084 | Transaction processing (online + batch) | COTRN00C-02C, CBTRN02C, CBACT04C, CBTRN03C |
| `billing-service` | 8085 | Bill payment | COBIL00C |
| `report-service` | 8086 | Report generation + statements | CORPT00C, CBSTM03A |
| `api-gateway` | 8080 | Spring Cloud Gateway routing | — |
| `data-migration` | — | EBCDIC to PostgreSQL data loader | — |

## Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose (for local development)
- PostgreSQL 16+ (or use Docker Compose)

## Build

```bash
cd springboot-migration
mvn clean install
```

## Run with Docker Compose

```bash
cd springboot-migration
docker-compose up -d
```

This starts PostgreSQL, Redis, all microservices, and the API gateway.

## Run Individual Services

1. Start PostgreSQL:
```bash
docker-compose up -d postgres redis
```

2. Run a specific service:
```bash
cd auth-service
mvn spring-boot:run
```

## API Endpoint Summary

### Authentication (Auth Service — :8081)
| Method | Endpoint | Description | COBOL Source |
|--------|----------|-------------|--------------|
| POST | `/api/auth/login` | User login, returns JWT | COSGN00C.cbl |
| GET | `/api/users` | List users (admin) | COUSR00C.cbl |
| POST | `/api/users` | Create user (admin) | COUSR01C.cbl |
| PUT | `/api/users/{userId}` | Update user (admin) | COUSR02C.cbl |
| DELETE | `/api/users/{userId}` | Delete user (admin) | COUSR03C.cbl |

### Account Management (Account Service — :8082)
| Method | Endpoint | Description | COBOL Source |
|--------|----------|-------------|--------------|
| GET | `/api/accounts/{accountId}` | View account | COACTVWC.cbl |
| GET | `/api/accounts?customerId=X` | List accounts | COACTVWC.cbl |
| PUT | `/api/accounts/{accountId}` | Update account | COACTUPC.cbl |

### Card Management (Card Service — :8083)
| Method | Endpoint | Description | COBOL Source |
|--------|----------|-------------|--------------|
| GET | `/api/accounts/{accountId}/cards` | List cards for account | COCRDLIC.cbl |
| GET | `/api/cards/{cardNumber}` | View card details | COCRDSLC.cbl |
| PUT | `/api/cards/{cardNumber}` | Update card | COCRDUPC.cbl |

### Transaction Processing (Transaction Service — :8084)
| Method | Endpoint | Description | COBOL Source |
|--------|----------|-------------|--------------|
| GET | `/api/transactions?accountId=X` | List transactions | COTRN00C.cbl |
| GET | `/api/transactions/{id}` | View transaction | COTRN01C.cbl |
| POST | `/api/transactions` | Create transaction | COTRN02C.cbl |

### Billing (Billing Service — :8085)
| Method | Endpoint | Description | COBOL Source |
|--------|----------|-------------|--------------|
| POST | `/api/billing/payments` | Make payment | COBIL00C.cbl |

### Reports (Report Service — :8086)
| Method | Endpoint | Description | COBOL Source |
|--------|----------|-------------|--------------|
| POST | `/api/reports/transactions` | Generate transaction report | CORPT00C.cbl |
| POST | `/api/reports/statements` | Launch statement generation | CBSTM03A.CBL |

## Batch Jobs

The following JCL/COBOL batch programs have been ported to Spring Batch:

| Spring Batch Job | JCL Source | COBOL Source | Description |
|-----------------|------------|--------------|-------------|
| `postTransactionJob` | POSTTRAN.jcl | CBTRN02C.cbl | Post daily transactions and update balances |
| `interestCalculationJob` | INTCALC.jcl | CBACT04C.cbl | Calculate and apply monthly interest |
| `transactionReportJob` | TRANREPT.jcl | CBTRN03C.cbl | Generate transaction reports |
| `statementGenerationJob` | CREASTMT.JCL | CBSTM03A.CBL | Generate customer statements |

## COBOL to Java Mapping

### Data Type Mapping

| COBOL Type | Java Type | Notes |
|-----------|-----------|-------|
| PIC X(n) | String | Character fields |
| PIC 9(n) | long / int | Numeric display |
| PIC 9(n)V9(m) | BigDecimal | Decimal with implied point |
| COMP / COMP-4 | long / int | Binary |
| COMP-3 | BigDecimal | Packed decimal |
| PIC X(10) date | LocalDate | Date fields |
| PIC X(26) timestamp | LocalDateTime | Timestamp fields |

### VSAM to PostgreSQL Mapping

| VSAM File | PostgreSQL Table | Primary Key |
|-----------|-----------------|-------------|
| USRSEC | users | user_id |
| ACCTDATA | accounts | account_id |
| CARDDATA | cards | card_number |
| CUSTDATA | customers | customer_id |
| CARDXREF | cards (FK) | card_number |
| TRANSACT | transactions | transaction_id |
| DALYTRAN | daily_transactions | transaction_id |
| TRANTYPE | transaction_types | transaction_type_code |
| TRANCATG | transaction_categories | (type_code, category_code) |
| DISCGRP | disclosure_groups | (group_id, type_code, cat_code) |
| TCATBALF | category_balances | (account_id, type_code, cat_code) |

### CICS Pattern to REST Mapping

| CICS Pattern | REST Equivalent |
|-------------|----------------|
| SEND MAP / RETURN TRANSID | Stateless REST endpoint |
| DFHCOMMAREA | JWT token (stateless) |
| EXEC CICS READ | JPA Repository findById() |
| EXEC CICS REWRITE | JPA Repository save() |
| EXEC CICS DELETE | JPA Repository deleteById() |
| EXEC CICS STARTBR/READNEXT | JPA Pageable query |
| RACF authentication | Spring Security + JWT |
| BMS screen maps | API request/response DTOs |

## Data Migration

The `data-migration` module reads EBCDIC-encoded data files from `app/data/EBCDIC/` and loads them into PostgreSQL.

```bash
cd data-migration
java -jar target/data-migration-1.0.0-SNAPSHOT.jar --data.dir=../../app/data/EBCDIC
```

The migration handles:
- EBCDIC → UTF-8 character conversion
- COMP-3 (packed decimal) → BigDecimal conversion
- Zoned decimal → BigDecimal conversion
- Binary (COMP) → long/int conversion

## Technology Stack

| Component | Technology |
|-----------|-----------|
| Framework | Spring Boot 3.2.5 |
| Language | Java 17+ |
| Build | Maven |
| Database | PostgreSQL 16 |
| Migration | Flyway |
| Security | Spring Security + JWT |
| API Gateway | Spring Cloud Gateway |
| Batch | Spring Batch |
| ORM | Spring Data JPA / Hibernate |
| Mapping | MapStruct |
| API Docs | springdoc-openapi (Swagger) |
| Containers | Docker Compose |

## Project Structure

```
springboot-migration/
├── pom.xml                    # Parent POM
├── docker-compose.yml         # Local development infrastructure
├── README.md                  # This file
├── common-lib/                # Shared library
│   └── src/main/java/com/carddemo/common/
│       ├── entity/            # JPA entities (from COBOL copybooks)
│       ├── dto/               # Data transfer objects
│       ├── repository/        # Spring Data JPA repositories
│       ├── mapper/            # MapStruct mappers
│       ├── exception/         # Global exception handling
│       └── config/            # JPA configuration
├── auth-service/              # Authentication (port 8081)
├── account-service/           # Account management (port 8082)
├── card-service/              # Card management (port 8083)
├── transaction-service/       # Transactions + batch (port 8084)
├── billing-service/           # Bill payment (port 8085)
├── report-service/            # Reports + statements (port 8086)
├── api-gateway/               # API Gateway (port 8080)
└── data-migration/            # EBCDIC data migration utility
```
