# CardDemo Java/Spring Boot Migration

This directory contains the complete Java/Spring Boot migration of the CardDemo mainframe COBOL application.
The migration replicates all functionality of the original CICS/VSAM/IMS/DB2 system using modern Java technologies.

## Technology Stack

| Component | Technology |
|-----------|-----------|
| Framework | Spring Boot 3.2.5 |
| Language | Java 17 |
| ORM | Spring Data JPA / Hibernate |
| Database | PostgreSQL 15 |
| Migrations | Flyway |
| Security | Spring Security + JWT (JJWT 0.12.5) |
| Batch | Spring Batch |
| Messaging | Spring AMQP / RabbitMQ |
| Build | Maven |
| Testing | JUnit 5, Mockito, Testcontainers |
| Coverage | JaCoCo |

## Quick Start

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose (for local database and messaging)

### Build

```bash
cd java-migration
mvn clean install
```

### Run Locally

```bash
# Start PostgreSQL and RabbitMQ
docker-compose up -d

# Run the application
mvn spring-boot:run
```

The application starts at `http://localhost:8080`.

### Run Tests

```bash
# Unit tests only
mvn test

# Unit + integration tests (requires Docker for Testcontainers)
mvn verify
```

## API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | Login with userId and password |

**Request:**
```json
{ "userId": "ADMIN001", "password": "ADMIN001" }
```
**Response:**
```json
{ "token": "eyJhb...", "userType": "ADMIN", "message": "Login successful" }
```

### Menu
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/menu` | Get role-based menu options |

### Accounts
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/accounts/{acctId}` | View account details |
| PUT | `/api/accounts/{acctId}` | Update account |

### Cards
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/cards` | List cards (paginated) |
| GET | `/api/cards/{cardNum}` | View card detail |
| PUT | `/api/cards/{cardNum}` | Update card |
| GET | `/api/cards/by-account/{acctId}` | List cards for account |

### Transactions
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/transactions?cardNum={cardNum}` | List transactions (paginated) |
| GET | `/api/transactions/{tranId}` | View transaction detail |
| POST | `/api/transactions` | Add new transaction |

### Bill Payment
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/bill-payment` | Process bill payment |

**Request:**
```json
{ "acctId": "00000000001", "confirm": "Y" }
```

### User Administration (Admin only)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/users` | List users |
| GET | `/api/admin/users/{userId}` | Get user |
| POST | `/api/admin/users` | Add user |
| PUT | `/api/admin/users/{userId}` | Update user |
| DELETE | `/api/admin/users/{userId}` | Delete user |

### Transaction Types (Admin only)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/transaction-types` | List transaction types |
| GET | `/api/admin/transaction-types/{typeCode}` | Get type |
| POST | `/api/admin/transaction-types` | Add type |
| PUT | `/api/admin/transaction-types/{typeCode}` | Update type |

### Reports
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/reports/transactions?cardNum={cardNum}` | Transaction report |

### Authorization
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/authorization` | Process authorization |
| POST | `/api/authorization/fraud` | Mark fraud |

## COBOL Program to Java Class Mapping

| COBOL Program | Java Class | Description |
|---------------|-----------|-------------|
| COSGN00C.cbl | AuthService / AuthController | User sign-on |
| COMEN01C.cbl | MenuController | Main menu |
| COADM01C.cbl | MenuController | Admin menu |
| COACTVWC.cbl | AccountService / AccountController | Account view |
| COACTUPC.cbl | AccountService / AccountController | Account update |
| COCRDLIC.cbl | CardService / CardController | Card listing |
| COCRDSLC.cbl | CardService / CardController | Card detail |
| COCRDUPC.cbl | CardService / CardController | Card update |
| COTRN00C.cbl | TransactionService / TransactionController | Transaction list |
| COTRN01C.cbl | TransactionService / TransactionController | Transaction detail |
| COTRN02C.cbl | TransactionService / TransactionController | Transaction add |
| COBIL00C.cbl | BillPaymentService / BillPaymentController | Bill payment |
| COUSR00C.cbl | UserAdminService / UserAdminController | User list |
| COUSR01C.cbl | UserAdminService / UserAdminController | User add |
| COUSR02C.cbl | UserAdminService / UserAdminController | User update |
| COUSR03C.cbl | UserAdminService / UserAdminController | User delete |
| CORPT00C.cbl | ReportService / ReportController | Reports |
| COTRTLIC.cbl | TransactionTypeService / TransactionTypeController | Transaction type list |
| COTRTUPC.cbl | TransactionTypeService / TransactionTypeController | Transaction type update |
| COPAUA0C.cbl | AuthorizationService / AuthorizationMessageListener | MQ authorization |
| COPAUS0C.cbl | AuthorizationService / AuthorizationController | Auth summary view |
| COPAUS2C.cbl | AuthorizationService / AuthorizationController | Fraud marking |
| CBPAUP0C.cbl | AuthPurgeJobConfig | Auth purge batch |
| CBTRN02C.cbl | TransactionPostingJobConfig / TransactionPostingProcessor | Transaction posting batch |
| CBACT04C.cbl | InterestCalculationJobConfig / InterestCalculationProcessor | Interest calculation batch |
| CBSTM03A.cbl | StatementGenerationJobConfig / StatementGenerationProcessor | Statement generation batch |
| COACCT01.cbl | AccountInquiryListener | MQ account inquiry |
| CODATE01.cbl | DateInquiryListener | MQ date inquiry |

## VSAM File to Database Table Mapping

| VSAM File | Copybook | Database Table | Key |
|-----------|----------|---------------|-----|
| ACCTDATA.VSAM.KSDS | CVACT01Y | account | acct_id |
| CARDDATA.VSAM.KSDS | CVACT02Y | card | card_num |
| CUSTDATA.VSAM.KSDS | CVCUS01Y | customer | cust_id |
| CARDXREF.VSAM.KSDS | CVACT03Y | card_xref | xref_card_num |
| TRANSACT.VSAM.KSDS | CVTRA05Y | transaction | tran_id |
| DALYTRAN (sequential) | CVTRA06Y | daily_transaction | id (auto) |
| USRSEC.VSAM.KSDS | CSUSR01Y | user_security | usr_id |
| TRANTYPE.VSAM.KSDS | CVTRA03Y | transaction_type | tran_type |
| TRANCATG.VSAM.KSDS | CVTRA04Y | transaction_category | (tran_type_cd, tran_cat_cd) |
| TCATBALF.VSAM.KSDS | CVTRA01Y | tran_cat_balance | (trancat_acct_id, trancat_type_cd, trancat_cd) |
| DISCGRP.VSAM.KSDS | CVTRA02Y | disclosure_group | (dis_acct_group_id, dis_tran_type_cd, dis_tran_cat_cd) |
| IMS PAUTSUM0 | CIPAUSMY | pending_auth_summary | id |
| IMS PAUTDTL1 | CIPAUDTY | pending_auth_detail | id |
| DB2 AUTHFRDS | - | auth_fraud | id |

## Batch Job Schedule

The nightly batch chain runs in this order (configurable via `app.batch.cron.nightly-chain`):

| Order | Job | COBOL Equivalent | Description |
|-------|-----|-----------------|-------------|
| 1 | TransactionPostingJob | CBTRN02C / POSTTRAN | Post daily transactions |
| 2 | InterestCalculationJob | CBACT04C / INTCALC | Calculate interest |
| 3 | StatementGenerationJob | CBSTM03A / CREASTMT | Generate statements |
| 4 | TransactionReportJob | (new) | Generate CSV report |
| 5 | AuthPurgeJob | CBPAUP0C / CBPAUP0J | Purge expired auths |

Default schedule: `0 0 2 * * ?` (2:00 AM daily)

## Messaging Queues

| Queue | Exchange | Description |
|-------|----------|-------------|
| carddemo.auth.request | carddemo.exchange | Authorization requests |
| carddemo.auth.reply | carddemo.exchange | Authorization responses |
| carddemo.account.inquiry | carddemo.exchange | Account inquiries |
| carddemo.account.reply | carddemo.exchange | Account responses |
| carddemo.date.inquiry | carddemo.exchange | Date inquiries |
| carddemo.date.reply | carddemo.exchange | Date responses |

## Security

- JWT-based stateless authentication (replaces CICS pseudo-conversational COMMAREA state)
- Two roles: **ADMIN** (user type 'A') and **USER** (user type 'U')
- Admin endpoints (`/api/admin/**`) require ADMIN role
- All other endpoints require authentication
- Login endpoint (`/api/auth/login`) is public
- JWT token expiration: 1 hour (configurable)

## Default Credentials

| User ID | Password | Role |
|---------|----------|------|
| ADMIN001 | ADMIN001 | ADMIN |
| USER0001 | USER0001 | USER |

## Project Structure

```
java-migration/
├── pom.xml
├── docker-compose.yml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/aws/carddemo/
    │   │   ├── CardDemoApplication.java
    │   │   ├── config/          (SecurityConfig, BatchConfig, RabbitMQConfig, SchedulerConfig)
    │   │   ├── entity/          (14 JPA entities)
    │   │   ├── repository/      (14 Spring Data repositories)
    │   │   ├── dto/             (13 DTOs for requests/responses)
    │   │   ├── service/         (10 service classes)
    │   │   ├── controller/      (10 REST controllers)
    │   │   ├── batch/           (5 batch job configs + processors + scheduler)
    │   │   ├── messaging/       (3 message listeners)
    │   │   ├── security/        (JWT provider, filter, user details service)
    │   │   └── exception/       (Custom exceptions + global handler)
    │   └── resources/
    │       ├── application.yml
    │       ├── application-test.yml
    │       └── db/migration/
    │           ├── V1__create_schema.sql
    │           └── V2__seed_data.sql
    └── test/
        └── java/com/aws/carddemo/
            ├── service/         (8 service test classes)
            ├── controller/      (6 controller test classes)
            ├── repository/      (DataLoadTest, RepositoryTest)
            └── integration/     (10 integration test classes)
```
