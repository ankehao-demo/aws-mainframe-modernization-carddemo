# CardDemo COBOL-to-Java Modernization — Implementation Prompts

This document contains detailed, self-contained implementation prompts for **Phase 0
(Discovery & Foundation)** and **Phase 1 (Authentication & Navigation)** of the CardDemo
COBOL-to-Java modernization plan.

Each prompt is written as a task specification that a developer (or an AI coding agent)
can execute independently. References to the original mainframe source code point to files
in this repository (`ankehao-demo/aws-mainframe-modernization-carddemo`). File paths are
given relative to the repo root unless otherwise stated.

**Target stack** for the modernized application:

- Java 21
- Spring Boot 3.x (Web, Data JPA, Security, Batch)
- PostgreSQL (or Aurora PostgreSQL) as the primary database
- Flyway for schema migrations
- Maven multi-module project
- JUnit 5 + Testcontainers for tests
- React (or Angular / Thymeleaf) for the UI
- GitHub Actions for CI

**Source repository references used throughout:**

- Online COBOL programs: `app/cbl/CO*.cbl`
- Batch COBOL programs: `app/cbl/CB*.cbl`
- Copybooks: `app/cpy/*.cpy`
- BMS maps: `app/bms/*.bms`
- Sample data: `app/data/EBCDIC/`
- VSAM catalog listing: `app/catlg/LISTCAT.txt`

---

## Phase 0 — Discovery & Foundation

The goal of Phase 0 is to understand the existing system, design the target data model,
bootstrap the Java project, and build the tooling needed to migrate existing data.

### Prompt 0a — Dependency Mapping

**Context**

The CardDemo application is a CICS/COBOL credit card management system. Online programs
(`CO*.cbl`) handle interactive transactions via BMS maps; batch programs (`CB*.cbl`)
handle nightly processing. All online programs share a common COMMAREA layout defined in
`app/cpy/COCOM01Y.cpy` (see lines 19–45: `CARDDEMO-COMMAREA`). Before modernization can
begin, we need a single structured artifact that captures how every program, copybook,
VSAM file, BMS map, and CICS transaction relates to one another.

**Task**

Generate a complete dependency map of the CardDemo COBOL application. For every COBOL
program in `app/cbl/` (both online `CO*.cbl` and batch `CB*.cbl`), document:

1. **Copybook usage** — which copybooks from `app/cpy/` the program `COPY`s in. Typical
   ones include:
   - `COCOM01Y.cpy` — shared COMMAREA for all online programs
   - `CVACT01Y.cpy` — ACCOUNT-RECORD
   - `CVACT02Y.cpy` — CARD-RECORD
   - `CVACT03Y.cpy` — CARD-XREF-RECORD
   - `CVCUS01Y.cpy` — CUSTOMER-RECORD
   - `CVCRD01Y.cpy` — CCARD work area
   - `CVTRA05Y.cpy` — TRAN-RECORD
   - `CSUSR01Y.cpy` — SEC-USER-DATA
   - UI helpers: `COTTL01Y.cpy`, `CSMSG01Y.cpy`, `CSMSG02Y.cpy`, `CSDAT01Y.cpy`
2. **VSAM file access** — which VSAM KSDS files the program reads from or writes to.
   Canonical files (see `app/catlg/LISTCAT.txt`): `USRSEC`, `ACCTDATA`, `CARDDATA`,
   `CUSTDATA`, `TRANSACT`, `CARDXREF`, `TCATBALF`, `DALYTRAN`, `DALYREJS`. Indicate access
   mode (read, write, rewrite, delete, browse).
3. **BMS map usage** — which maps from `app/bms/` the program sends/receives (e.g.,
   `COSGN00.bms`, `COMEN01.bms`, `COADM01.bms`, `COACTVW.bms`, `COCRDLI.bms`,
   `COTRN00.bms`, `COTRN01.bms`, `COTRN02.bms`, `COBIL00.bms`, `CORPT00.bms`,
   `COUSR00.bms`–`COUSR03.bms`).
4. **CICS transaction IDs handled** — e.g., `CC00` (sign-on), `CM00` (main menu),
   `CA00` (admin menu), `CAVW`/`CAUP` (account view/update), `CCLI`/`CCDL`/`CCUP`
   (card list/detail/update), `CT00`/`CT01`/`CT02` (transaction list/view/add),
   `CB00` (bill pay), `CR00` (reports), `CU00`–`CU03` (user management).
5. **Program-to-program call relationships** — any `EXEC CICS XCTL` or `EXEC CICS LINK`
   (or static COBOL `CALL`) that transfers control to another program.
6. **COMMAREA contract** — document the fields in `CARDDEMO-COMMAREA` (lines 19–45 of
   `app/cpy/COCOM01Y.cpy`) that each online program reads or writes. In particular:
   - `CDEMO-FROM-TRANID` / `CDEMO-FROM-PROGRAM`
   - `CDEMO-TO-TRANID` / `CDEMO-TO-PROGRAM`
   - `CDEMO-USER-ID`, `CDEMO-USER-TYPE` (level‑88s: `CDEMO-USRTYP-ADMIN = 'A'`,
     `CDEMO-USRTYP-USER = 'U'`)
   - `CDEMO-PGM-CONTEXT` (level‑88s: `CDEMO-PGM-ENTER = 0`, `CDEMO-PGM-REENTER = 1`)
   - `CDEMO-CUSTOMER-INFO`, `CDEMO-ACCOUNT-INFO`, `CDEMO-CARD-INFO`

**Deliverables**

- A structured table (Markdown) **and** a machine-readable JSON document with the same
  information. Suggested JSON shape:

  ```json
  {
    "programs": [
      {
        "name": "COSGN00C",
        "type": "online",
        "source": "app/cbl/COSGN00C.cbl",
        "transactionIds": ["CC00"],
        "copybooks": ["COCOM01Y.cpy", "CSUSR01Y.cpy", "COSGN00.cpy", "CSMSG01Y.cpy"],
        "vsamFiles": [{"name": "USRSEC", "accessModes": ["READ"]}],
        "bmsMaps": ["COSGN00.bms"],
        "xctlTargets": ["COMEN01C", "COADM01C"],
        "linkTargets": [],
        "commareaReads":  ["CDEMO-FROM-TRANID"],
        "commareaWrites": ["CDEMO-USER-ID","CDEMO-USER-TYPE","CDEMO-TO-PROGRAM"]
      }
    ],
    "files": [
      {"name": "USRSEC", "copybook": "CSUSR01Y.cpy", "primaryKey": "SEC-USR-ID"}
    ]
  }
  ```

- Save the Markdown table at `docs/phase0/dependency-map.md` and the JSON at
  `docs/phase0/dependency-map.json`.
- Include a summary diagram (Mermaid or PlantUML) showing the online program
  navigation graph derived from `XCTL` relationships. Start from `COSGN00C` and fan out
  to menu programs (`COMEN01C`, `COADM01C`) and their children.

**Acceptance criteria**

- Every `.cbl` file in `app/cbl/` appears as a row in the table.
- Every copybook referenced by a program is listed, even shared ones such as
  `COCOM01Y.cpy` and `COTTL01Y.cpy`.
- Every VSAM file named in `app/catlg/LISTCAT.txt` is either referenced by at least one
  program, or explicitly marked as unused.
- The JSON document validates against a JSON Schema you include at
  `docs/phase0/dependency-map.schema.json`.

---

### Prompt 0b — Database Schema Design

**Context**

CardDemo stores master data in VSAM KSDS files. The record layouts in `app/cpy/` are the
authoritative source of truth — the target PostgreSQL schema must preserve field
semantics, precision, and uniqueness. The VSAM primary keys (first field of each
copybook record) must become the primary keys of the new tables. Alternate indexes used
by batch and online programs must become secondary indexes (or dedicated junction
tables, for cross-references).

**Task**

Design a PostgreSQL (or Aurora PostgreSQL) relational schema that replaces the VSAM files.
Use the following copybooks as the source of truth for each table:

| Table | Source copybook | VSAM file | Primary key |
|-------|-----------------|-----------|-------------|
| `account` | `app/cpy/CVACT01Y.cpy` (lines 4–17) | `ACCTDATA` | `ACCT-ID` |
| `customer` | `app/cpy/CVCUS01Y.cpy` | `CUSTDATA` | `CUST-ID` |
| `card` | `app/cpy/CVACT02Y.cpy` | `CARDDATA` | `CARD-NUM` |
| `card_xref` | `app/cpy/CVACT03Y.cpy` (a.k.a. `CVCRD01Y` usage) | `CARDXREF` | `XREF-CARD-NUM` |
| `transaction` | `app/cpy/CVTRA05Y.cpy` | `TRANSACT` | `TRAN-ID` |
| `user_security` | `app/cpy/CSUSR01Y.cpy` | `USRSEC` | `SEC-USR-ID` |
| `transaction_category_balance` | `app/cpy/CVTRA01Y.cpy` (or equivalent) | `TCATBALF` | composite (`TCATBAL-ACCT-ID`, `TCATBAL-TRAN-TYPE-CD`, `TCATBAL-TRAN-CAT-CD`) |
| `transaction_type` | `app/cpy/CVTRA03Y.cpy` | `TRANTYPE` | `TRAN-TYPE-CD` |
| `transaction_category` | `app/cpy/CVTRA04Y.cpy` | `TRANCATG` | composite |
| `disclosure_group` | `app/cpy/CVTRA02Y.cpy` | `DISCGRP` | composite |
| `daily_transaction` | `app/cpy/CVTRA06Y.cpy` | `DALYTRAN` | `DALYTRAN-ID` |
| `daily_reject` | `app/cpy/CVTRA07Y.cpy` | `DALYREJS` | surrogate |

**Type-conversion rules**

| COBOL PICTURE | Example | PostgreSQL type |
|---|---|---|
| `PIC 9(n)` (n ≤ 18) | `ACCT-ID PIC 9(11)` | `BIGINT` (prefer) or `NUMERIC(n,0)` |
| `PIC S9(n)V99` | `ACCT-CURR-BAL PIC S9(10)V99` | `DECIMAL(12,2)` |
| `PIC X(n)` | `ACCT-ACTIVE-STATUS PIC X(01)` | `CHAR(1)` or `VARCHAR(n)` |
| `PIC X(10)` used as date | `ACCT-OPEN-DATE PIC X(10)` | `DATE` (parsed from `YYYY-MM-DD`) |
| `PIC X(26)` timestamp | `TRAN-PROC-TS PIC X(26)` | `TIMESTAMP(6)` |
| `FILLER` | `FILLER PIC X(178)` | **omit** from the table |

**Relationships and constraints**

- `card.card_acct_id` → `account.acct_id` (FK, NOT NULL)
- `card_xref.xref_cust_id` → `customer.cust_id` (FK, NOT NULL)
- `card_xref.xref_acct_id` → `account.acct_id` (FK, NOT NULL)
- `card_xref.xref_card_num` → `card.card_num` (FK, NOT NULL, also the PK of `card_xref`)
- `transaction.tran_card_num` → `card.card_num` (FK, NOT NULL) — replaces the VSAM AIX on
  `TRANSACT` by card number.
- `transaction_category_balance.acct_id` → `account.acct_id` (FK)
- All money columns: `NOT NULL DEFAULT 0`
- All status/flag columns: `NOT NULL` with a CHECK constraint on valid values
- All identifier columns: `NOT NULL`

**Indexes (mirroring VSAM KSDS key / AIX structures)**

- `account(acct_id)` — primary key
- `card(card_num)` — primary key
- `card(card_acct_id)` — secondary index (replaces `CARDDATA.VSAM.AIX` on `CARD-ACCT-ID`)
- `card_xref(xref_acct_id)`, `card_xref(xref_cust_id)` — secondary indexes
- `transaction(tran_id)` — primary key
- `transaction(tran_card_num, tran_proc_ts)` — composite index for browse/pagination
- `user_security(sec_usr_id)` — primary key
- `user_security(sec_usr_type)` — helpful for admin filtering

**Deliverables**

- Flyway migration SQL files placed in `carddemo-common/src/main/resources/db/migration/`
  (assuming the module structure defined in Prompt 0c), named with the `V{n}__{desc}.sql`
  convention. Suggested split:
  - `V1__create_reference_tables.sql` (transaction types/categories, disclosure groups)
  - `V2__create_master_tables.sql` (customer, account, card)
  - `V3__create_cross_reference.sql` (card_xref)
  - `V4__create_transaction_tables.sql` (transaction, daily_transaction, daily_reject)
  - `V5__create_balance_tables.sql` (transaction_category_balance)
  - `V6__create_user_security.sql` (user_security)
  - `V7__create_indexes.sql` (all secondary indexes)
- A dbdiagram.io / Mermaid ER diagram committed at `docs/phase0/schema-er.md`.
- A type-mapping matrix at `docs/phase0/type-mapping.md` documenting every copybook field
  and its PostgreSQL counterpart, including justification for any deviation from the
  table above.

**Acceptance criteria**

- `mvn -pl carddemo-common flyway:migrate` runs cleanly against a fresh PostgreSQL 15+
  database.
- Every copybook field (other than `FILLER`) is represented as a column.
- Every primary key from a VSAM KSDS is the primary key of the corresponding table.
- Referential integrity is enforced between `card`, `account`, `customer`, `card_xref`,
  and `transaction`.
- Unit tests using Testcontainers verify that a minimal set of representative records can
  be inserted and joined without violating any constraint.

---

### Prompt 0c — Java Project Bootstrap

**Context**

All subsequent phases build on a shared Java project. We need a multi-module Spring Boot
3.x project that enforces clean module boundaries matching the domains in the original
mainframe application (auth, account, transaction, batch, admin). Common code (JPA
entities from Prompt 0b, DTOs, exceptions) lives in a shared module.

**Task**

Create a Spring Boot 3.x multi-module Maven project named `carddemo` with the following
modules:

| Module | Responsibility |
|---|---|
| `carddemo-common` | Shared JPA entities (generated from the Prompt 0b schema), DTOs, custom exceptions, Flyway migration resources |
| `carddemo-auth` | Authentication module (Phase 1) — replaces `COSGN00C` |
| `carddemo-account` | Account / customer / card management (Phase 2) — replaces `COACTVWC`, `COACTUPC`, `COCRDLIC`, `COCRDSLC`, `COCRDUPC` |
| `carddemo-transaction` | Online transaction processing (Phase 3) — replaces `COTRN00C`/`01C`/`02C`, `COBIL00C`, `CORPT00C` |
| `carddemo-batch` | Spring Batch jobs (Phase 4) — replaces `CBTRN01C`, `CBTRN02C`, `CBTRN03C`, `CBACT01C`–`04C`, `CBSTM03A/B`, `CBCUS01C` |
| `carddemo-admin` | User management (Phase 5) — replaces `COADM01C`, `COUSR00C`–`COUSR03C` |
| `carddemo-web` | REST controllers, Spring Security configuration, `@SpringBootApplication` entry point |
| `carddemo-migration` | Data-migration utilities (Prompt 0d) |

**Required dependencies** (managed via a root `pom.xml` with
`<dependencyManagement>` using the Spring Boot BOM):

- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-security`
- `spring-boot-starter-validation`
- `spring-boot-starter-actuator`
- `spring-boot-starter-batch` (in `carddemo-batch`)
- `org.postgresql:postgresql`
- `org.flywaydb:flyway-core` and `flyway-database-postgresql`
- `org.projectlombok:lombok` (provided)
- `org.mapstruct:mapstruct` + `mapstruct-processor`
- `io.jsonwebtoken:jjwt-api`, `jjwt-impl`, `jjwt-jackson` (for JWT in Phase 1)
- `org.springdoc:springdoc-openapi-starter-webmvc-ui` (OpenAPI UI)
- Test: `spring-boot-starter-test`, `org.testcontainers:junit-jupiter`,
  `org.testcontainers:postgresql`, `org.springframework.security:spring-security-test`

**Configuration**

- `carddemo-web/src/main/resources/application.yml` with Spring profiles:
  - `dev` — local PostgreSQL (`jdbc:postgresql://localhost:5432/carddemo`), debug logging,
    CORS open for the frontend dev server.
  - `test` — Testcontainers JDBC URL (`jdbc:tc:postgresql:15:///carddemo`), Flyway clean
    enabled between runs.
  - `prod` — values sourced from environment variables
    (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`,
    `SPRING_DATASOURCE_PASSWORD`, `CARDDEMO_JWT_SECRET`, etc.), no hard-coded secrets.
- Enable Flyway in every profile; point it at `classpath:db/migration` (migrations live in
  `carddemo-common`).
- Configure `spring.jpa.open-in-view=false` and `spring.jpa.hibernate.ddl-auto=validate`
  (Flyway owns the schema).

**Project layout**

```
carddemo/
├── pom.xml                              # parent, packaging=pom
├── carddemo-common/
│   └── src/main/java/com/carddemo/common/{entity,dto,exception}
│   └── src/main/resources/db/migration/V*.sql
├── carddemo-auth/
├── carddemo-account/
├── carddemo-transaction/
├── carddemo-batch/
├── carddemo-admin/
├── carddemo-web/
│   └── src/main/java/com/carddemo/CardDemoApplication.java
│   └── src/main/resources/application.yml
├── carddemo-migration/
└── .github/workflows/ci.yml
```

**CI pipeline (GitHub Actions)**

Create `.github/workflows/ci.yml` with a `build` job that:

1. Checks out the repository.
2. Sets up Java 21 (Temurin).
3. Caches `~/.m2/repository`.
4. Runs `mvn -B -ntp verify`.
5. On `main`, builds a Docker image using a multi-stage `Dockerfile`
   (`eclipse-temurin:21-jre` runtime, non-root user, layered JAR) and publishes it to the
   configured registry.

**Deliverables**

- A compiling multi-module project that boots via
  `mvn -pl carddemo-web spring-boot:run` and exposes
  `GET /actuator/health` returning `{"status":"UP"}`.
- OpenAPI UI reachable at `/swagger-ui/index.html` in `dev`.
- A passing CI build on the first commit.
- A `docker build .` at the repo root produces a runnable image that starts the app.

**Acceptance criteria**

- All modules build with `mvn -B -ntp verify` (Java 21, Maven 3.9+).
- No test uses `H2` — integration tests use Testcontainers PostgreSQL.
- `carddemo-common` declares no dependency on any other `carddemo-*` module.
- Feature modules (`carddemo-auth`, `carddemo-account`, …) depend on `carddemo-common`
  but not on each other, except where Phase-specific design requires (document any
  exceptions).
- `application.yml` contains no hard-coded production secrets.

---

### Prompt 0d — Data Migration Scripts

**Context**

Sample data is provided in EBCDIC-encoded fixed-length files under `app/data/EBCDIC/`
(e.g., `AWS.M2.CARDDEMO.ACCTDATA.PS`, `AWS.M2.CARDDEMO.CUSTDATA.PS`,
`AWS.M2.CARDDEMO.CARDDATA.PS`, `AWS.M2.CARDDEMO.TRANSACT.PS`,
`AWS.M2.CARDDEMO.CARDXREF.PS`, `AWS.M2.CARDDEMO.USRSEC.PS`). To stand up the modern
system with realistic data, we need a migration utility that reads each EBCDIC file and
inserts rows into the corresponding PostgreSQL table created in Prompt 0b.

**Task**

Build data-migration utilities in the `carddemo-migration` module that convert the
EBCDIC sample data files into PostgreSQL rows. The utility must handle:

1. **EBCDIC → ASCII character conversion** — use the `IBM037` (or `Cp037` /
   `Cp1047` — make this configurable per file) charset. Apply only to character fields,
   never to packed decimal or binary fields.
2. **Fixed-width record parsing** — each copybook defines the record length and the byte
   offsets of its fields. For example, `CVACT01Y.cpy` yields a 300-byte `account` record
   whose fields occupy well-defined offsets. Parse records by seeking offsets, not by
   splitting on delimiters.
3. **Zoned decimal fields (`PIC 9(n)`)** — convert the ASCII digit string to a
   `BigInteger` / `long`.
4. **Signed zoned decimal (`PIC S9(n)`)** — detect the sign nibble in the last byte
   (hex `C`/`F` → positive, `D` → negative) and interpret accordingly.
5. **Packed decimal / COMP-3 (`PIC S9(n)V99 COMP-3`)** — unpack two digits per byte;
   interpret the trailing sign nibble; scale by the implied decimal places.
6. **Implicit decimal (`PIC S9(10)V99` without COMP-3)** — divide the parsed integer by
   `100` to yield a `BigDecimal` with scale 2.
7. **Date fields (`PIC X(10)`, format `YYYY-MM-DD`)** — parse with
   `DateTimeFormatter.ISO_LOCAL_DATE`; reject records whose date strings are invalid and
   route them to the error report (do not silently drop).
8. **FILLER fields** — skip entirely; they are padding and contain no business data.
9. **Validation and error reporting** — for every malformed record, emit a structured
   error record capturing:
   - source file name
   - record number (1-based)
   - raw hex of the offending bytes
   - the field that failed parsing
   - a human-readable error message

   Write errors to a CSV file
   (`./migration-out/errors-{entity}-{timestamp}.csv`) and increment a counter visible in
   the final summary.

**Deliverable form**

- A Spring Boot `CommandLineRunner` (profile `migration`) or a standalone
  `main`-method utility, runnable as:

  ```
  java -jar carddemo-migration/target/carddemo-migration.jar \
    --input-dir=./app/data/EBCDIC \
    --charset=IBM037 \
    --spring.datasource.url=... --spring.datasource.username=... --spring.datasource.password=...
  ```

- Per-entity parser classes (`AccountRecordParser`, `CustomerRecordParser`,
  `CardRecordParser`, `CardXrefRecordParser`, `TransactionRecordParser`,
  `UserSecurityRecordParser`) implementing a common interface:

  ```java
  public interface RecordParser<T> {
      int recordLength();
      T parse(byte[] record) throws RecordParseException;
  }
  ```

- A reusable `FixedWidthEbcdicReader` that:
  - memory-maps the input file,
  - hands each `recordLength()`-sized slice to the parser,
  - collects rows into configurable batch sizes (default 1000),
  - uses `JdbcTemplate.batchUpdate(...)` for bulk inserts (wrapped in a transaction).
- Summary output at end of run:

  ```
  === Migration summary ===
  account   : read=10000 inserted=9998 errors=2
  customer  : read=10000 inserted=10000 errors=0
  card      : read=10000 inserted=10000 errors=0
  card_xref : read=10000 inserted=10000 errors=0
  transaction: read=50000 inserted=49997 errors=3
  user_security: read=2 inserted=2 errors=0
  ```

- Idempotency: the utility must accept a `--truncate` flag that clears the target tables
  before loading (respecting FK order), or a `--upsert` flag that uses
  `INSERT ... ON CONFLICT DO UPDATE`.

**Tests**

- Unit tests for each parser using hand-crafted byte arrays that exercise:
  - happy path
  - positive and negative signed decimals
  - malformed packed decimals
  - invalid dates
  - trimming of trailing EBCDIC spaces (`0x40`)
- Integration test (Testcontainers PostgreSQL) that reads a small fixture file and
  verifies both the inserted row count and the contents of a sample row.

**Acceptance criteria**

- All EBCDIC files in `app/data/EBCDIC/` can be loaded without error (or with documented,
  explainable errors recorded in the error CSV).
- Row counts match between source files and target tables, minus recorded errors.
- Sum of `account.acct_curr_bal` from the loaded data matches the sum computed directly
  from the EBCDIC file by an independent one-off script, within rounding tolerance.
- The loader is re-runnable on the same input without producing duplicates (either via
  `--truncate` or `--upsert`).

---

## Phase 1 — Authentication & Navigation

The goal of Phase 1 is to replace the sign-on screen and menu programs with a modern
authentication service and a minimal frontend. The rest of the application’s features
(accounts, cards, transactions, admin) are added in later phases and are represented here
only as placeholder routes.

### Prompt 1a — AuthService (replacing `COSGN00C.cbl`)

**Context**

`app/cbl/COSGN00C.cbl` handles sign-on under CICS transaction `CC00`:

- Uses BMS map `COSGN00.bms` to collect `WS-USER-ID` (`PIC X(08)`) and `WS-USER-PWD`
  (`PIC X(08)`).
- Reads the `USRSEC` VSAM KSDS (copybook `CSUSR01Y.cpy`) keyed by `SEC-USR-ID` to validate
  the credentials.
- Sets `CDEMO-USER-TYPE` in the COMMAREA (`CARDDEMO-COMMAREA` from
  `app/cpy/COCOM01Y.cpy` lines 26–28): `'A'` for admin, `'U'` for regular user.
- Populates `CDEMO-USER-ID`, `CDEMO-FROM-TRANID = 'CC00'`, and `CDEMO-TO-PROGRAM` in the
  COMMAREA.
- On success, `EXEC CICS XCTL`s to the appropriate menu program: `COMEN01C` for a
  regular user, `COADM01C` for an admin.
- On failure, redisplays the sign-on map with an error (invalid user ID, wrong password,
  locked account).

**Task**

Implement a Spring Security–based authentication service in `carddemo-auth` that replaces
`COSGN00C`.

**Components to deliver**

1. **JPA entity `UserSecurityEntity`** (in `carddemo-common`) mapping the `user_security`
   table defined in Prompt 0b. Fields (from `CSUSR01Y.cpy`):
   - `secUsrId` (PK, `VARCHAR(8)`)
   - `secUsrFname`, `secUsrLname`
   - `secUsrPwd` (store a BCrypt hash — see migration guidance below)
   - `secUsrType` (`CHAR(1)`, values `'A'` or `'U'`)
   - Optional audit fields: `createdAt`, `updatedAt`, `lockedUntil`, `failedLoginCount`
2. **Repository** `UserSecurityRepository extends JpaRepository<UserSecurityEntity, String>`
   exposing `Optional<UserSecurityEntity> findBySecUsrId(String userId)`.
3. **`AuthService`** responsible for:
   - Credential validation (BCrypt compare).
   - Account lock handling: after N failed attempts (configurable, default 5), set
     `lockedUntil = now + lockoutDuration` and reject further attempts until that time.
   - On success: reset `failedLoginCount`, issue a JWT.
4. **Spring Security `UserDetailsService` implementation** that adapts
   `UserSecurityEntity` to Spring’s `UserDetails`, mapping `secUsrType`:
   - `'A'` → `ROLE_ADMIN`
   - `'U'` → `ROLE_USER`
5. **Password-hash migration path**
   - The legacy `USRSEC` file stores passwords in plaintext (`SEC-USR-PWD PIC X(08)`).
   - The Prompt 0d loader must rehash each legacy plaintext password with BCrypt (cost
     12) before inserting into `user_security`, unless a `--preserve-plaintext` flag is
     explicitly passed (for local-only demo mode — never in production).
   - Provide a `PasswordUpgradeService` that, on the first successful login with a
     plaintext-matching password (optional fallback path during transition), upgrades the
     stored value to BCrypt. This service must be disabled in `prod`.
6. **REST endpoints** (all under `/api/auth`):
   - `POST /api/auth/login`
     - Body: `{"userId": "ADMIN001", "password": "..."}`
     - Response 200: `{"token": "<jwt>", "userId": "ADMIN001", "userType": "ADMIN", "expiresAt": "..."}`
     - Response 401: `{"error": "INVALID_CREDENTIALS", "message": "Invalid user ID or password"}`
     - Response 423: `{"error": "ACCOUNT_LOCKED", "message": "Account is locked until ..."}`
   - `POST /api/auth/logout`
     - Stateless — records the current JWT’s `jti` in a short-lived blocklist (Redis in
       `prod`, in-memory Caffeine cache in `dev`/`test`). Returns 204.
   - `GET /api/auth/me`
     - Returns the authenticated user’s profile:
       `{"userId": "...", "firstName": "...", "lastName": "...", "userType": "ADMIN"}`.
     - 401 if no valid token.
7. **JWT generation**
   - Algorithm: `HS256`, secret from `CARDDEMO_JWT_SECRET` (min 256-bit).
   - Claims: `sub` = userId, `userType` = `ADMIN`|`USER`, `roles` = `["ROLE_ADMIN"]` or
     `["ROLE_USER"]`, `iat`, `exp` (default 8 hours, configurable via
     `carddemo.auth.jwt.ttl`).
   - A `JwtAuthenticationFilter` validates the `Authorization: Bearer ...` header, loads
     the `UserDetails`, and populates `SecurityContextHolder`.
8. **Error handling parity with the original program**
   - Invalid user ID → 401 `INVALID_CREDENTIALS` (map message to “Invalid user ID or
     password”; do not leak which field was wrong).
   - Wrong password → 401 `INVALID_CREDENTIALS` (same message — avoid user enumeration).
   - Account locked → 423 `ACCOUNT_LOCKED`.
   - Missing fields → 400 `VALIDATION_ERROR` with a per-field error list.

**Tests**

- **Unit tests**: `AuthServiceTest` covering happy path, wrong password, unknown user,
  lockout triggering, lockout expiry, role mapping, JWT claim contents.
- **Integration tests** (Testcontainers PostgreSQL):
  - `POST /api/auth/login` with a seeded admin user returns 200 and a JWT whose claims
    include `userType=ADMIN`.
  - The same JWT grants access to `GET /api/auth/me` and returns the seeded profile.
  - A request to a `@PreAuthorize("hasRole('ADMIN')")` endpoint with a `USER`-role token
    returns 403.
- Use `@WithMockUser` for unit-style MVC tests where the JWT plumbing is out of scope.

**Acceptance criteria**

- All new tests pass; overall code coverage on `carddemo-auth` is ≥80% for classes under
  `com.carddemo.auth`.
- No password or JWT secret is logged or returned in any error body.
- The `/api/auth/*` routes are the only unauthenticated routes; every other route
  requires a valid JWT unless explicitly marked public.
- Behavior matches the original COBOL program for the three canonical scenarios: unknown
  user, bad password, successful sign-on for an admin vs. a regular user.

---

### Prompt 1b — Menu Controllers (replacing `COMEN01C.cbl` and `COADM01C.cbl`)

**Context**

`app/cbl/COMEN01C.cbl` (transaction `CM00`) is the regular-user main menu and
`app/cbl/COADM01C.cbl` (transaction `CA00`) is the admin menu. Both programs:

- Receive a BMS map (`COMEN01.bms` / `COADM01.bms`) with the user’s selected option.
- Read `CDEMO-USER-TYPE` from the COMMAREA to decide which options are legal.
- `EXEC CICS XCTL` to the target program for the selected option.
- Use `CDEMO-PGM-CONTEXT` (level‑88s `CDEMO-PGM-ENTER = 0`, `CDEMO-PGM-REENTER = 1`,
  from `app/cpy/COCOM01Y.cpy` lines 30–32) to distinguish first entry from re-entry after
  an invalid option.

In the Java/SPA world, the SPA router or server-side session tracks navigation state, so
there is no direct COMMAREA equivalent. What we do need is a server-side description of
the menu so the frontend can render it based on the caller’s role.

**Task**

Implement REST endpoints in the `carddemo-web` (controller) and
`carddemo-auth` / new `carddemo-menu` module (service) that provide the menu structure
for the authenticated user.

**Menu content**

- **Regular-user menu** (`COMEN01C`) — expose options matching the original:
  1. Account View → `/accounts/{acctId}` → `ROLE_USER`
  2. Account Update → `/accounts/{acctId}/edit` → `ROLE_USER`
  3. Card List → `/cards` → `ROLE_USER`
  4. Card Detail → `/cards/{cardNum}` → `ROLE_USER`
  5. Card Update → `/cards/{cardNum}/edit` → `ROLE_USER`
  6. Transaction List → `/transactions` → `ROLE_USER`
  7. Transaction Detail → `/transactions/{tranId}` → `ROLE_USER`
  8. Transaction Add → `/transactions/new` → `ROLE_USER`
  9. Bill Payment → `/bill-pay` → `ROLE_USER`
  10. Reports → `/reports` → `ROLE_USER`

- **Admin menu** (`COADM01C`) — everything in the regular menu **plus**:
  1. User List → `/admin/users` → `ROLE_ADMIN`
  2. User Add → `/admin/users/new` → `ROLE_ADMIN`
  3. User Update → `/admin/users/{userId}/edit` → `ROLE_ADMIN`
  4. User Delete → `/admin/users/{userId}/delete` → `ROLE_ADMIN`

**Endpoints**

- `GET /api/menu`
  - Returns the menu applicable to the currently authenticated user.
  - Response shape:

    ```json
    {
      "userId": "ADMIN001",
      "userType": "ADMIN",
      "items": [
        {
          "id": "ACCT_VIEW",
          "label": "Account View",
          "description": "View details of a specific account",
          "route": "/accounts/:acctId",
          "requiredRole": "ROLE_USER",
          "legacyTranId": "CAVW",
          "legacyProgram": "COACTVWC"
        }
      ]
    }
    ```

  - 401 if unauthenticated.
- `GET /api/admin/menu`
  - Admin-only variant, protected with `@PreAuthorize("hasRole('ADMIN')")`.
  - Returns only the admin-specific items (for a frontend that wants them in a separate
    panel). Returns 403 for non-admin users.

**Implementation notes**

- Define the menu items in a `MenuCatalog` component (bean) so adding/removing options in
  later phases does not require controller changes.
- The service filters items based on the caller’s granted authorities — never rely on a
  role claim passed from the client.
- Include the legacy `tranId` and `program` on each item as documentation (handy for
  traceability during modernization reviews); mark the fields `@JsonInclude(NON_NULL)` so
  they can be omitted in future.
- Do **not** implement a server-side `CDEMO-PGM-CONTEXT` equivalent. Navigation state is a
  frontend concern (Prompt 1c).

**Tests**

- **Integration tests** using `MockMvc` with JWTs for each role:
  - `GET /api/menu` as a `USER` returns exactly the regular-user items above.
  - `GET /api/menu` as an `ADMIN` returns both regular and admin items.
  - `GET /api/admin/menu` as a `USER` returns 403.
  - `GET /api/admin/menu` as an `ADMIN` returns only the four admin items.
  - `GET /api/menu` without a token returns 401.
- **Contract test**: the OpenAPI schema contains `MenuResponse` and `MenuItem` and the
  live response validates against it.

**Acceptance criteria**

- A `USER` role never sees admin-only items even if the frontend requests them.
- Item IDs are stable strings (not array indices) so the frontend can key off them
  safely.
- Adding a new menu item in Phase 2+ requires a single change in `MenuCatalog` and a
  single additional test case.

---

### Prompt 1c — Frontend Skeleton (replacing `COSGN00.bms`, `COMEN01.bms`, `COADM01.bms`)

**Context**

The original CardDemo UI is rendered via BMS maps on 3270 terminals. The modern
replacement is a web SPA that calls the REST API from Prompts 1a and 1b. This prompt
covers the minimum frontend needed to sign in and navigate to the correct menu — the
feature pages behind menu items are placeholder stubs in this phase.

**Task**

Create a minimal frontend with the following choice of stack (pick one and justify in
`docs/phase1/frontend-stack-decision.md`; default is React with Vite):

- **React 18 + TypeScript + Vite + React Router 6** (default)
- **Angular 17+** — acceptable if the team standardizes on Angular
- **Thymeleaf** — acceptable only if server-rendering is an explicit requirement

**Pages and components**

1. **Login page** (replaces `app/bms/COSGN00.bms`) at route `/login`:
   - Inputs: User ID (`maxLength=8`, uppercase), Password (`maxLength=8`).
   - Submits `POST /api/auth/login`.
   - On success: store the JWT (see storage guidance below), fetch `/api/auth/me`,
     redirect to `/` (regular users) or `/admin` (admins).
   - On failure: display the API error message inline (do not expose whether the ID or
     password was the problem).
2. **Main menu page** (replaces `app/bms/COMEN01.bms`) at route `/`:
   - Requires `ROLE_USER` (guard).
   - Fetches `/api/menu` on mount; renders items as a list of navigable cards with
     `label` and `description`.
3. **Admin menu page** (replaces `app/bms/COADM01.bms`) at route `/admin`:
   - Requires `ROLE_ADMIN` (guard); non-admins redirected to `/`.
   - Fetches `/api/admin/menu`; renders the admin-only items.
4. **Placeholder pages** for every `route` returned by `/api/menu`:
   - Each placeholder shows the item `label`, `description`, the legacy `tranId` and
     `program` (for demo storytelling), and a `To be implemented in Phase N` notice.
   - Implemented as a single `PlaceholderPage` component keyed off the menu item ID.
5. **Logout** — a header button that calls `POST /api/auth/logout`, clears local JWT
   state, and redirects to `/login`.

**Cross-cutting concerns**

- **JWT storage**
  - Default: in-memory (React state + SWR/React Query) plus an HttpOnly cookie issued by
    a small Node BFF — preferred for `prod`.
  - Acceptable for local `dev`: `sessionStorage` with CSP and `SameSite=strict`.
  - Never use `localStorage`.
- **API client**
  - A single `apiClient` (Axios or Fetch wrapper) that:
    - Reads the current JWT from the chosen storage.
    - Sets `Authorization: Bearer <jwt>` on every request.
    - Intercepts `401` responses and redirects to `/login`.
    - Intercepts `423` (account locked) and shows a dedicated message.
- **Route guards**
  - `RequireAuth` — redirects to `/login` if no valid token.
  - `RequireRole('ADMIN')` — redirects to `/` if token lacks admin role.
- **Error and empty states**
  - Loading, error, and empty states for every data-driven page.
- **Accessibility**
  - Form labels, keyboard navigation, sufficient color contrast.
  - Login form works without a mouse.

**Project layout (React/Vite default)**

```
frontend/
├── index.html
├── vite.config.ts
├── tsconfig.json
├── package.json
├── src/
│   ├── main.tsx
│   ├── App.tsx
│   ├── api/
│   │   ├── client.ts
│   │   ├── auth.ts
│   │   └── menu.ts
│   ├── auth/
│   │   ├── AuthContext.tsx
│   │   ├── RequireAuth.tsx
│   │   └── RequireRole.tsx
│   ├── pages/
│   │   ├── LoginPage.tsx
│   │   ├── MainMenuPage.tsx
│   │   ├── AdminMenuPage.tsx
│   │   └── PlaceholderPage.tsx
│   └── components/
│       └── Header.tsx
└── README.md
```

**Tests**

- Component tests (Vitest + React Testing Library) for:
  - Login form validation and submission.
  - Route guards redirecting unauthenticated users to `/login`.
  - Admin guard redirecting non-admins from `/admin` to `/`.
- End-to-end test (Playwright) covering:
  - Admin login → admin menu visible → sign out → login page.
  - Regular-user login → main menu visible, admin menu not reachable.

**Deliverables**

- Frontend project committed at `frontend/` (sibling to the Java modules) with a
  README explaining `npm install`, `npm run dev`, and how to point the dev server at the
  Spring Boot backend (via Vite proxy).
- A decision document at `docs/phase1/frontend-stack-decision.md` explaining the stack
  choice.
- A Dockerfile for the frontend (multi-stage: `node:20-alpine` build → `nginx:alpine`
  serve) and an updated root CI workflow that builds and tests the frontend.

**Acceptance criteria**

- A fresh developer can `npm install && npm run dev`, sign in with seeded
  `ADMIN001` / `USER0001` credentials, and see the correct menu based on role.
- All placeholder pages render without error and visibly identify the phase in which
  they will be implemented.
- No JWT appears in `localStorage`, in URL query strings, or in any log message.
- Playwright E2E tests pass against a real Spring Boot backend seeded with the Prompt 0d
  data.

---

## Cross-Phase Notes

- Every prompt assumes the repository structure described in Prompt 0c. If a prompt
  produces files, it places them in the correct module.
- All code committed under these prompts follows the
  **Modern Java 21, Spring Boot and OpenAPI Project Standards** used by the team:
  Java 21, Spring Boot 3.x BOM, Bean Validation on inputs, global
  `@ControllerAdvice` error handler, structured JSON logging, OpenAPI annotations kept in
  sync with `api-spec/openapi.yaml`.
- Legacy COBOL, JCL, BMS, and copybook source files in `app/` must not be modified as
  part of these prompts. They remain the source of truth for behavior parity; any divergence
  must be called out in the accompanying PR.
- All prompts should be executable independently — if a developer runs Prompt 0b without
  first running Prompt 0a, the Flyway migrations must still stand on their own based on
  the copybooks alone.
