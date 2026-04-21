# Migration Plan: COTRN02C → Spring Boot Microservice

This document is a detailed migration plan for the `COTRN02C` CICS online COBOL program (transaction ID `CT02` — "Add Transaction") to a modern Java 21 + Spring Boot microservice.

It is designed so a Java developer with no prior COBOL/CICS experience can use it to implement the service. References are given to the source COBOL and copybooks under [`app/cbl/`](../app/cbl/), [`app/cpy/`](../app/cpy/), [`app/cpy-bms/`](../app/cpy-bms/), and [`app/bms/`](../app/bms/).

---

## 1. Full Program Analysis

### 1.1 Program at a glance

| Attribute | Value |
|---|---|
| Program name | `COTRN02C` |
| Source | [`app/cbl/COTRN02C.cbl`](../app/cbl/COTRN02C.cbl) (~784 lines) |
| CICS Transaction ID | `CT02` |
| BMS Mapset / Map | `COTRN02` / `COTRN2A` ([`app/bms/COTRN02.bms`](../app/bms/COTRN02.bms)) |
| Function | Add a new transaction record to the `TRANSACT` VSAM KSDS |
| Called subprograms | `CSUTLDTC` (date validation via `CEEDAYS`) |
| VSAM datasets read | `ACCTDAT`, `CCXREF`, `CXACAIX`, `TRANSACT` |
| VSAM datasets written | `TRANSACT` |
| Conversational model | Pseudo-conversational (each interaction returns control to CICS with `TRANSID=CT02` + COMMAREA) |

Note: `ACCTDAT` is declared in working storage (`WS-ACCTDAT-FILE`) but is not actually read by this program. The real lookups are against `CCXREF` (by card number) and `CXACAIX` (alternate index on account ID).

### 1.2 Copybooks used

| Copybook | Purpose |
|---|---|
| [`COCOM01Y.cpy`](../app/cpy/COCOM01Y.cpy) | `CARDDEMO-COMMAREA` — state passed between CICS programs (from/to program, user ID/type, account/card selection, `CDEMO-CT02-INFO` block) |
| [`COTRN02.CPY`](../app/cpy-bms/COTRN02.CPY) | BMS symbolic map (`COTRN2AI` input / `COTRN2AO` output) generated from the BMS source |
| [`COTTL01Y.cpy`](../app/cpy/COTTL01Y.cpy) | Screen titles (`CCDA-TITLE01`, `CCDA-TITLE02`) |
| [`CSDAT01Y.cpy`](../app/cpy/CSDAT01Y.cpy) | Date/time working storage (`WS-CURDATE`, `WS-CURTIME`, `WS-TIMESTAMP`) |
| [`CSMSG01Y.cpy`](../app/cpy/CSMSG01Y.cpy) | Common messages (`CCDA-MSG-INVALID-KEY`, etc.) |
| [`CVTRA05Y.cpy`](../app/cpy/CVTRA05Y.cpy) | `TRAN-RECORD` layout (350 bytes) — TRANSACT file record |
| [`CVACT01Y.cpy`](../app/cpy/CVACT01Y.cpy) | `ACCOUNT-RECORD` layout (300 bytes) — ACCTDAT file record |
| [`CVACT03Y.cpy`](../app/cpy/CVACT03Y.cpy) | `CARD-XREF-RECORD` layout (50 bytes) — CCXREF file record |
| `DFHAID` / `DFHBMSCA` | IBM-supplied CICS AID keys and BMS colour/attribute constants |

### 1.3 Control flow

```
MAIN-PARA (entry point)
 ├── if EIBCALEN = 0 → XCTL to COSGN00C (sign on)
 ├── first entry (not CDEMO-PGM-REENTER)
 │     └── if a pre-selected transaction was passed in COMMAREA → PROCESS-ENTER-KEY
 │         → SEND-TRNADD-SCREEN (always)
 └── subsequent entries (CDEMO-PGM-REENTER = true)
       ├── RECEIVE-TRNADD-SCREEN (read user input from 3270)
       └── EVALUATE EIBAID (which key the user pressed)
             ├── ENTER → PROCESS-ENTER-KEY
             ├── PF3   → XCTL back to previous program (default COMEN01C)
             ├── PF4   → CLEAR-CURRENT-SCREEN
             ├── PF5   → COPY-LAST-TRAN-DATA (prefill from most recent transaction)
             └── other → invalid key error
       → EXEC CICS RETURN TRANSID('CT02') with COMMAREA
```

#### Paragraph-by-paragraph summary

| Paragraph | Responsibility |
|---|---|
| `MAIN-PARA` | Entry dispatcher: handles first entry vs re-entry, AID key dispatch, CICS RETURN |
| `PROCESS-ENTER-KEY` | Validate → read confirm flag → if `Y` PERFORM `ADD-TRANSACTION` |
| `VALIDATE-INPUT-KEY-FIELDS` | Either account ID or card number must be entered. If account ID given, look up card via `CXACAIX`; if card given, look up account via `CCXREF` |
| `VALIDATE-INPUT-DATA-FIELDS` | All field-level validation (required fields, numeric format, amount pattern, date format, `CSUTLDTC` call) |
| `ADD-TRANSACTION` | Generate next `TRAN-ID` by STARTBR/READPREV/ENDBR on `TRANSACT`, increment by 1, build `TRAN-RECORD`, `WRITE` |
| `COPY-LAST-TRAN-DATA` | Prefill the screen from the last transaction on file, then call `PROCESS-ENTER-KEY` |
| `RETURN-TO-PREV-SCREEN` | `XCTL` to a previous program name (default `COSGN00C`/`COMEN01C`) |
| `SEND-TRNADD-SCREEN` | Populate header, `SEND MAP`, `RETURN TRANSID` |
| `RECEIVE-TRNADD-SCREEN` | `RECEIVE MAP` from 3270 |
| `POPULATE-HEADER-INFO` | Fill screen header with title, program, current date/time |
| `READ-CXACAIX-FILE` | `READ DATASET(CXACAIX)` by `XREF-ACCT-ID` |
| `READ-CCXREF-FILE` | `READ DATASET(CCXREF)` by `XREF-CARD-NUM` |
| `STARTBR-TRANSACT-FILE` | `STARTBR DATASET(TRANSACT)` with `RIDFLD=HIGH-VALUES` |
| `READPREV-TRANSACT-FILE` | `READPREV` to fetch the record with the highest key |
| `ENDBR-TRANSACT-FILE` | `ENDBR DATASET(TRANSACT)` |
| `WRITE-TRANSACT-FILE` | `WRITE DATASET(TRANSACT)` — on success builds the success message, on `DUPKEY/DUPREC` returns a conflict error |
| `CLEAR-CURRENT-SCREEN` | Calls `INITIALIZE-ALL-FIELDS` then re-sends the screen |
| `INITIALIZE-ALL-FIELDS` | Clear every input field on the map |

### 1.4 Screen fields (map `COTRN2A`)

All field lengths are taken from [`app/bms/COTRN02.bms`](../app/bms/COTRN02.bms) / [`app/cpy-bms/COTRN02.CPY`](../app/cpy-bms/COTRN02.CPY):

| Map name | Label | Length | Validation performed in COBOL |
|---|---|---|---|
| `ACTIDIN` | Enter Acct # | 11 | Numeric; if present, look up card via `CXACAIX` |
| `CARDNIN` | Card # | 16 | Numeric; if present, look up account via `CCXREF` |
| `TTYPCD`  | Type CD | 2 | Required, numeric |
| `TCATCD`  | Category CD | 4 | Required, numeric |
| `TRNSRC`  | Source | 10 | Required |
| `TDESC`   | Description | 60 | Required |
| `TRNAMT`  | Amount | 12 | Required; format `-99999999.99` (sign + 8 digits + `.` + 2 digits) |
| `TORIGDT` | Orig Date | 10 | Required; format `YYYY-MM-DD`; validated by `CSUTLDTC` |
| `TPROCDT` | Proc Date | 10 | Required; format `YYYY-MM-DD`; validated by `CSUTLDTC` |
| `MID`     | Merchant ID | 9 | Required, numeric |
| `MNAME`   | Merchant Name | 30 | Required |
| `MCITY`   | Merchant City | 25 | Required |
| `MZIP`    | Merchant Zip | 10 | Required |
| `CONFIRM` | Confirm add | 1 | `Y` or `N` only |
| `ERRMSG`  | Error message | 78 | Set by program for user feedback |

### 1.5 CICS commands used

| Command | Location | Purpose |
|---|---|---|
| `EXEC CICS RETURN TRANSID(CT02) COMMAREA(...)` | `MAIN-PARA`, `SEND-TRNADD-SCREEN` | Pseudo-conversational return |
| `EXEC CICS XCTL PROGRAM(CDEMO-TO-PROGRAM) COMMAREA(...)` | `RETURN-TO-PREV-SCREEN` | Hand control to another program (sign-on / menu) |
| `EXEC CICS SEND MAP('COTRN2A') MAPSET('COTRN02') FROM(COTRN2AO) ERASE CURSOR` | `SEND-TRNADD-SCREEN` | Write a 3270 screen |
| `EXEC CICS RECEIVE MAP('COTRN2A') MAPSET('COTRN02') INTO(COTRN2AI)` | `RECEIVE-TRNADD-SCREEN` | Read a 3270 screen |
| `EXEC CICS READ DATASET(CXACAIX/CCXREF) ... RESP/RESP2` | `READ-CXACAIX-FILE`, `READ-CCXREF-FILE` | Keyed VSAM read |
| `EXEC CICS STARTBR / READPREV / ENDBR DATASET(TRANSACT)` | `STARTBR/READPREV/ENDBR-TRANSACT-FILE` | Position to last record in key order |
| `EXEC CICS WRITE DATASET(TRANSACT) FROM(TRAN-RECORD)` | `WRITE-TRANSACT-FILE` | Insert transaction record |

### 1.6 External utility: `CSUTLDTC`

Source: [`app/cbl/CSUTLDTC.cbl`](../app/cbl/CSUTLDTC.cbl).

Wraps IBM Language Environment service `CEEDAYS`. Linkage:

- `LS-DATE` `PIC X(10)` — date to test (e.g. `2024-06-15`)
- `LS-DATE-FORMAT` `PIC X(10)` — picture mask (e.g. `YYYY-MM-DD`)
- `LS-RESULT` `PIC X(80)` — formatted result containing:
  - Cols 1–4: severity code (`0000` = valid)
  - Cols 16–19: message number (COTRN02C treats `2513` as non-fatal)
  - Trailing text: human-readable result (`'Date is valid'`, `'Invalid month'`, `'Datevalue error'`, …)

COTRN02C treats any severity ≠ `0000` and message number ≠ `2513` as a validation failure.

### 1.7 File ↔ entity mapping

| Dataset (DD name / cluster role) | Key | Copybook | Usage in COTRN02C |
|---|---|---|---|
| `TRANSACT` (KSDS, key = `TRAN-ID`) | `TRAN-ID` `PIC X(16)` | [`CVTRA05Y.cpy`](../app/cpy/CVTRA05Y.cpy) | Browse last record, write new record |
| `CCXREF` (KSDS, key = `XREF-CARD-NUM`) | `XREF-CARD-NUM` `PIC X(16)` | [`CVACT03Y.cpy`](../app/cpy/CVACT03Y.cpy) | Lookup account from card |
| `CXACAIX` (AIX path over CCXREF, key = `XREF-ACCT-ID`) | `XREF-ACCT-ID` `PIC 9(11)` | [`CVACT03Y.cpy`](../app/cpy/CVACT03Y.cpy) | Lookup card from account |
| `ACCTDAT` (KSDS, key = `ACCT-ID`) | `ACCT-ID` `PIC 9(11)` | [`CVACT01Y.cpy`](../app/cpy/CVACT01Y.cpy) | Referenced by name only; not actually read in this program |

### 1.8 Validation rules (complete list)

1. Account ID and Card Number — at least one must be supplied; whichever is supplied must be numeric; if supplied, must resolve via the corresponding cross-reference file.
2. `TTYPCD` — required, numeric.
3. `TCATCD` — required, numeric.
4. `TRNSRC` — required (text).
5. `TDESC` — required (text).
6. `TRNAMT` — required; must match the fixed mask `[+-]99999999.99` (sign at col 1, 8 digits, `.`, 2 digits).
7. `TORIGDT` — required; must match `NNNN-NN-NN`; must be a valid calendar date per `CSUTLDTC`.
8. `TPROCDT` — required; must match `NNNN-NN-NN`; must be a valid calendar date per `CSUTLDTC`.
9. `MID` — required, numeric.
10. `MNAME`, `MCITY`, `MZIP` — required (text).
11. `CONFIRM` must be `Y` to actually insert; `N`/blank → prompt again; anything else → "Invalid value. Valid values are (Y/N)".
12. On `DUPREC`/`DUPKEY` from the WRITE → user-facing error "Tran ID already exist...".

### 1.9 Transaction ID generation

1. `MOVE HIGH-VALUES TO TRAN-ID`
2. `STARTBR DATASET(TRANSACT) RIDFLD(TRAN-ID)`
3. `READPREV` → reads the logically-last key
4. `ENDBR`
5. `MOVE TRAN-ID TO WS-TRAN-ID-N` (numeric representation)
6. `ADD 1 TO WS-TRAN-ID-N`
7. Write new record with this ID.

Implication for migration: the COBOL contract is that `TRAN-ID` is a monotonically increasing 16-character numeric string. This must be preserved (or a compatibility strategy chosen — see §7).

---

## 2. Data Model Mapping

### 2.1 High-level entity table

| COBOL copybook (record) | Java entity | Table | Primary key |
|---|---|---|---|
| [`CVTRA05Y.cpy`](../app/cpy/CVTRA05Y.cpy) (`TRAN-RECORD`) | `Transaction` | `transactions` | `tran_id CHAR(16)` |
| [`CVACT01Y.cpy`](../app/cpy/CVACT01Y.cpy) (`ACCOUNT-RECORD`) | `Account` | `accounts` | `acct_id NUMERIC(11)` |
| [`CVACT03Y.cpy`](../app/cpy/CVACT03Y.cpy) (`CARD-XREF-RECORD`) | `CardCrossReference` | `card_xref` | `card_num CHAR(16)` |

### 2.2 `Transaction` (`TRAN-RECORD`, 350 bytes)

| COBOL field | PIC clause | Length | Java field | Java type | DDL |
|---|---|---|---|---|---|
| `TRAN-ID` | `PIC X(16)` | 16 | `tranId` | `String` | `CHAR(16) PRIMARY KEY` |
| `TRAN-TYPE-CD` | `PIC X(02)` | 2 | `tranTypeCd` | `String` | `CHAR(2) NOT NULL` |
| `TRAN-CAT-CD` | `PIC 9(04)` | 4 | `tranCatCd` | `Integer` | `INTEGER NOT NULL` |
| `TRAN-SOURCE` | `PIC X(10)` | 10 | `tranSource` | `String` | `VARCHAR(10) NOT NULL` |
| `TRAN-DESC` | `PIC X(100)` | 100 | `tranDesc` | `String` | `VARCHAR(100) NOT NULL` |
| `TRAN-AMT` | `PIC S9(09)V99` | 11 | `tranAmt` | `BigDecimal` | `NUMERIC(11,2) NOT NULL` |
| `TRAN-MERCHANT-ID` | `PIC 9(09)` | 9 | `merchantId` | `Long` | `NUMERIC(9) NOT NULL` |
| `TRAN-MERCHANT-NAME` | `PIC X(50)` | 50 | `merchantName` | `String` | `VARCHAR(50) NOT NULL` |
| `TRAN-MERCHANT-CITY` | `PIC X(50)` | 50 | `merchantCity` | `String` | `VARCHAR(50) NOT NULL` |
| `TRAN-MERCHANT-ZIP` | `PIC X(10)` | 10 | `merchantZip` | `String` | `VARCHAR(10) NOT NULL` |
| `TRAN-CARD-NUM` | `PIC X(16)` | 16 | `cardNum` | `String` | `CHAR(16) NOT NULL, FK card_xref.card_num` |
| `TRAN-ORIG-TS` | `PIC X(26)` | 26 | `origTs` | `OffsetDateTime` | `TIMESTAMP(6) WITH TIME ZONE NOT NULL` |
| `TRAN-PROC-TS` | `PIC X(26)` | 26 | `procTs` | `OffsetDateTime` | `TIMESTAMP(6) WITH TIME ZONE NOT NULL` |
| `FILLER` | `PIC X(20)` | 20 | (drop) | — | — |

Note: `TRAN-ORIG-TS` / `TRAN-PROC-TS` are declared `X(26)` (matching `CSDAT01Y.WS-TIMESTAMP` `YYYY-MM-DD hh:mm:ss.mmmmmm`) but COTRN02C currently moves the 10-char date from the screen into them and pads the remaining bytes with spaces. Migration should canonicalise these as `OffsetDateTime` in UTC and surface them as ISO 8601 in the API.

```java
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @Column(name = "tran_id", length = 16, nullable = false, updatable = false)
    private String tranId;

    @Column(name = "tran_type_cd", length = 2, nullable = false)
    private String tranTypeCd;

    @Column(name = "tran_cat_cd", nullable = false)
    private Integer tranCatCd;

    @Column(name = "tran_source", length = 10, nullable = false)
    private String tranSource;

    @Column(name = "tran_desc", length = 100, nullable = false)
    private String tranDesc;

    @Column(name = "tran_amt", precision = 11, scale = 2, nullable = false)
    private BigDecimal tranAmt;

    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    @Column(name = "merchant_name", length = 50, nullable = false)
    private String merchantName;

    @Column(name = "merchant_city", length = 50, nullable = false)
    private String merchantCity;

    @Column(name = "merchant_zip", length = 10, nullable = false)
    private String merchantZip;

    @Column(name = "card_num", length = 16, nullable = false)
    private String cardNum;

    @Column(name = "orig_ts", nullable = false)
    private OffsetDateTime origTs;

    @Column(name = "proc_ts", nullable = false)
    private OffsetDateTime procTs;
}
```

### 2.3 `Account` (`ACCOUNT-RECORD`, 300 bytes)

| COBOL field | PIC clause | Java field | Java type | DDL |
|---|---|---|---|---|
| `ACCT-ID` | `PIC 9(11)` | `acctId` | `Long` | `NUMERIC(11) PRIMARY KEY` |
| `ACCT-ACTIVE-STATUS` | `PIC X(01)` | `activeStatus` | `String` | `CHAR(1) NOT NULL` |
| `ACCT-CURR-BAL` | `PIC S9(10)V99` | `currBal` | `BigDecimal` | `NUMERIC(12,2) NOT NULL` |
| `ACCT-CREDIT-LIMIT` | `PIC S9(10)V99` | `creditLimit` | `BigDecimal` | `NUMERIC(12,2) NOT NULL` |
| `ACCT-CASH-CREDIT-LIMIT` | `PIC S9(10)V99` | `cashCreditLimit` | `BigDecimal` | `NUMERIC(12,2) NOT NULL` |
| `ACCT-OPEN-DATE` | `PIC X(10)` | `openDate` | `LocalDate` | `DATE NOT NULL` |
| `ACCT-EXPIRAION-DATE` | `PIC X(10)` | `expirationDate` | `LocalDate` | `DATE NOT NULL` |
| `ACCT-REISSUE-DATE` | `PIC X(10)` | `reissueDate` | `LocalDate` | `DATE` |
| `ACCT-CURR-CYC-CREDIT` | `PIC S9(10)V99` | `currCycCredit` | `BigDecimal` | `NUMERIC(12,2) NOT NULL` |
| `ACCT-CURR-CYC-DEBIT` | `PIC S9(10)V99` | `currCycDebit` | `BigDecimal` | `NUMERIC(12,2) NOT NULL` |
| `ACCT-ADDR-ZIP` | `PIC X(10)` | `addrZip` | `String` | `VARCHAR(10)` |
| `ACCT-GROUP-ID` | `PIC X(10)` | `groupId` | `String` | `VARCHAR(10)` |
| `FILLER X(178)` | — | (drop) | — | — |

### 2.4 `CardCrossReference` (`CARD-XREF-RECORD`, 50 bytes)

| COBOL field | PIC clause | Java field | Java type | DDL |
|---|---|---|---|---|
| `XREF-CARD-NUM` | `PIC X(16)` | `cardNum` | `String` | `CHAR(16) PRIMARY KEY` |
| `XREF-CUST-ID` | `PIC 9(09)` | `custId` | `Long` | `NUMERIC(9) NOT NULL, INDEX` |
| `XREF-ACCT-ID` | `PIC 9(11)` | `acctId` | `Long` | `NUMERIC(11) NOT NULL, INDEX` |
| `FILLER X(14)` | — | (drop) | — | — |

The alternate index `CXACAIX` (by `acctId`) is replaced by a B-tree index on `card_xref(acct_id)` and a derived query method.

### 2.5 Generic COBOL → Java type rules

| COBOL PIC clause | Bytes | Java | JDBC/JPA | Notes |
|---|---|---|---|---|
| `PIC X(n)` | n | `String` | `CHAR(n)` / `VARCHAR(n)` | Trim trailing spaces from COBOL data on read |
| `PIC 9(n)` (n ≤ 9) | n | `Integer` | `INTEGER`/`NUMERIC(n,0)` | |
| `PIC 9(n)` (10 ≤ n ≤ 18) | n | `Long` | `BIGINT`/`NUMERIC(n,0)` | e.g. `ACCT-ID PIC 9(11)` |
| `PIC 9(n)` (n > 18) or any `PIC 9(16)` card number stored as text | n | `String` | `CHAR(n)` | Preserve leading zeros |
| `PIC S9(n)V99` | n+2 | `BigDecimal` | `NUMERIC(n+2,2)` | |
| `PIC S9(n)V99 COMP-3` | ceil((n+3)/2) | `BigDecimal` | `NUMERIC(n+2,2)` | See §7 — needs EBCDIC-aware conversion for any bulk VSAM import |
| `PIC 9(n) COMP` | 2 or 4 | `Integer`/`Long` | `INTEGER`/`BIGINT` | |
| `PIC X(10)` containing `YYYY-MM-DD` | 10 | `LocalDate` | `DATE` | |
| `PIC X(26)` containing `YYYY-MM-DD hh:mm:ss.ffffff` | 26 | `OffsetDateTime` | `TIMESTAMP(6)` | COTRN02C currently stores only a 10-char date in this field |

### 2.6 Proposed DDL (Flyway `V1__baseline.sql`)

```sql
CREATE TABLE accounts (
  acct_id              NUMERIC(11) PRIMARY KEY,
  active_status        CHAR(1)       NOT NULL,
  curr_bal             NUMERIC(12,2) NOT NULL,
  credit_limit         NUMERIC(12,2) NOT NULL,
  cash_credit_limit    NUMERIC(12,2) NOT NULL,
  open_date            DATE          NOT NULL,
  expiration_date      DATE          NOT NULL,
  reissue_date         DATE,
  curr_cyc_credit      NUMERIC(12,2) NOT NULL,
  curr_cyc_debit       NUMERIC(12,2) NOT NULL,
  addr_zip             VARCHAR(10),
  group_id             VARCHAR(10)
);

CREATE TABLE card_xref (
  card_num    CHAR(16)     PRIMARY KEY,
  cust_id     NUMERIC(9)   NOT NULL,
  acct_id     NUMERIC(11)  NOT NULL REFERENCES accounts(acct_id)
);
CREATE INDEX ix_card_xref_acct_id ON card_xref(acct_id);
CREATE INDEX ix_card_xref_cust_id ON card_xref(cust_id);

CREATE TABLE transactions (
  tran_id         CHAR(16) PRIMARY KEY,
  tran_type_cd    CHAR(2)       NOT NULL,
  tran_cat_cd     INTEGER       NOT NULL,
  tran_source     VARCHAR(10)   NOT NULL,
  tran_desc       VARCHAR(100)  NOT NULL,
  tran_amt        NUMERIC(11,2) NOT NULL,
  merchant_id     NUMERIC(9)    NOT NULL,
  merchant_name   VARCHAR(50)   NOT NULL,
  merchant_city   VARCHAR(50)   NOT NULL,
  merchant_zip    VARCHAR(10)   NOT NULL,
  card_num        CHAR(16)      NOT NULL REFERENCES card_xref(card_num),
  orig_ts         TIMESTAMP(6) WITH TIME ZONE NOT NULL,
  proc_ts         TIMESTAMP(6) WITH TIME ZONE NOT NULL,
  created_at      TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT now()
);
CREATE INDEX ix_transactions_card_num ON transactions(card_num);

-- Preserve numeric monotonicity of tran_id by driving it from a sequence (see §7.1)
CREATE SEQUENCE transactions_tran_id_seq AS BIGINT START 1 INCREMENT 1 MINVALUE 1;
```

---

## 3. API Design

REST API served under `/api/v1`. All endpoints return `application/json`; errors use RFC 7807 `application/problem+json`.

### 3.1 Endpoints

| Method | Path | Replaces | Notes |
|---|---|---|---|
| `POST` | `/api/v1/transactions` | `ADD-TRANSACTION` + `WRITE-TRANSACT-FILE` | Creates a transaction; idempotency via header (see §3.3) |
| `GET` | `/api/v1/accounts/{accountId}/cards` | `READ-CXACAIX-FILE` | List card numbers for an account |
| `GET` | `/api/v1/cards/{cardNumber}` | `READ-CCXREF-FILE` | Resolve card → account/customer |
| `GET` | `/api/v1/transactions/last` | `STARTBR/READPREV/ENDBR-TRANSACT-FILE` | Used by the `PF5` "copy last" workflow |

Swagger/OpenAPI 3.1 spec is maintained in [`api-spec/openapi.yaml`](../api-spec/openapi.yaml) (new file). Controllers use `@Operation`/`@Schema` annotations to keep code and spec in sync.

### 3.2 `POST /api/v1/transactions`

Request:

```json
{
  "accountId": "00000000123",
  "cardNumber": "4111111111111111",
  "typeCd": "01",
  "categoryCd": "1001",
  "source": "POS",
  "description": "COFFEE SHOP PURCHASE",
  "amount": "-12.45",
  "origDate": "2024-06-15",
  "procDate": "2024-06-15",
  "merchantId": "000123456",
  "merchantName": "ACME COFFEE",
  "merchantCity": "SEATTLE",
  "merchantZip": "98101"
}
```

Validation rules mirror §1.8 exactly. One of `accountId` / `cardNumber` is required; if both are supplied they must resolve to the same account. Amounts are strings to preserve the `-99999999.99` wire format; the service parses to `BigDecimal` with scale 2 and range `[-99_999_999.99, 99_999_999.99]`.

Response `201 Created`:

```json
{
  "tranId": "0000000000000042",
  "typeCd": "01",
  "categoryCd": 1001,
  "source": "POS",
  "description": "COFFEE SHOP PURCHASE",
  "amount": "-12.45",
  "cardNumber": "4111111111111111",
  "merchant": {
    "id": 123456,
    "name": "ACME COFFEE",
    "city": "SEATTLE",
    "zip": "98101"
  },
  "origTs": "2024-06-15T00:00:00Z",
  "procTs": "2024-06-15T00:00:00Z"
}
```

Error status codes:

| Situation | HTTP | Problem `type` |
|---|---|---|
| Bean validation failure | `400` | `validation-error` |
| Account not found (account-only lookup) | `404` | `account-not-found` |
| Card not found (card-only lookup) | `404` | `card-not-found` |
| Account/card mismatch | `409` | `card-account-mismatch` |
| Tran ID collision on insert (`DUPKEY`/`DUPREC`) | `409` | `transaction-id-conflict` |
| Unhandled upstream error | `500` | `internal-error` |

### 3.3 Idempotency and the "confirm" step

In CICS the user pressed ENTER twice (once to validate, once with `CONFIRM=Y`). REST removes the screen so the client drives idempotency using an `Idempotency-Key: <uuid>` request header. The server stores the key with the generated `tranId`; repeated submissions with the same key return the original `201` response instead of creating a duplicate.

### 3.4 `GET /api/v1/accounts/{accountId}/cards`

Response `200 OK`:

```json
{
  "accountId": "00000000123",
  "cards": [
    { "cardNumber": "4111111111111111", "customerId": 987654321 }
  ]
}
```

Returns `404` with `type=account-not-found` when no cards resolve for the account (matching the COBOL `Account ID NOT found` message).

### 3.5 `GET /api/v1/cards/{cardNumber}`

Response `200 OK`:

```json
{
  "cardNumber": "4111111111111111",
  "accountId": "00000000123",
  "customerId": 987654321
}
```

Returns `404` with `type=card-not-found` when the card is absent (matching `Card Number NOT found`).

### 3.6 `GET /api/v1/transactions/last`

Returns the transaction with the highest `tranId`. Used by the `PF5 = Copy Last Tran` UX; the client receives the full `TransactionResponse` and pre-populates its form.

---

## 4. Business Logic Migration

| COBOL paragraph | Java method | Class |
|---|---|---|
| `MAIN-PARA` | (none — removed; the REST dispatch replaces it) | — |
| `PROCESS-ENTER-KEY` | `processTransaction(CreateTransactionRequest req)` | `TransactionService` |
| `VALIDATE-INPUT-KEY-FIELDS` | `validateAccountId(String)` / `validateCardNumber(String)` | `TransactionValidationService` |
| `VALIDATE-INPUT-DATA-FIELDS` | `validateDataFields(CreateTransactionRequest)` | `TransactionValidationService` |
| `ADD-TRANSACTION` | `createTransaction(CreateTransactionRequest)` | `TransactionService` |
| `COPY-LAST-TRAN-DATA` | `findLastTransaction()` | `TransactionService` |
| `READ-CXACAIX-FILE` | `findByAcctId(Long)` | `CardCrossReferenceRepository` |
| `READ-CCXREF-FILE` | `findById(String)` | `CardCrossReferenceRepository` |
| `STARTBR/READPREV/ENDBR-TRANSACT-FILE` | `findTopByOrderByTranIdDesc()` | `TransactionRepository` |
| `WRITE-TRANSACT-FILE` | `save(Transaction)` | `TransactionRepository` |
| `CSUTLDTC` call | `DateValidator.validate(String value, String pattern)` | `util.DateValidator` |
| `POPULATE-HEADER-INFO` | (dropped; REST is stateless) | — |
| `SEND-TRNADD-SCREEN` / `RECEIVE-TRNADD-SCREEN` | HTTP response / request binding | `TransactionController` |
| `RETURN-TO-PREV-SCREEN` | (dropped; handled by client router) | — |
| `CLEAR-CURRENT-SCREEN` / `INITIALIZE-ALL-FIELDS` | (client UI concern) | — |

### 4.1 `TransactionService` sketch

```java
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactions;
    private final CardCrossReferenceRepository cardXref;
    private final TransactionValidationService validator;
    private final TransactionIdGenerator idGenerator;
    private final Clock clock;

    @Transactional
    public TransactionResponse createTransaction(CreateTransactionRequest req, String idempotencyKey) {
        validator.validate(req);
        CardCrossReference xref = validator.resolveCardXref(req);
        String nextId = idGenerator.next();
        Transaction t = Transaction.builder()
            .tranId(nextId)
            .tranTypeCd(req.typeCd())
            .tranCatCd(Integer.parseInt(req.categoryCd()))
            .tranSource(req.source())
            .tranDesc(req.description())
            .tranAmt(new BigDecimal(req.amount()))
            .merchantId(Long.parseLong(req.merchantId()))
            .merchantName(req.merchantName())
            .merchantCity(req.merchantCity())
            .merchantZip(req.merchantZip())
            .cardNum(xref.getCardNum())
            .origTs(parseDate(req.origDate()).atStartOfDay().atOffset(ZoneOffset.UTC))
            .procTs(parseDate(req.procDate()).atStartOfDay().atOffset(ZoneOffset.UTC))
            .build();
        try {
            return TransactionResponse.from(transactions.save(t));
        } catch (DataIntegrityViolationException e) {
            throw new TransactionIdConflictException(nextId, e);
        }
    }

    @Transactional(readOnly = true)
    public Optional<TransactionResponse> findLastTransaction() {
        return transactions.findTopByOrderByTranIdDesc().map(TransactionResponse::from);
    }
}
```

### 4.2 `TransactionValidationService`

Responsibilities (mapped 1:1 to `VALIDATE-INPUT-KEY-FIELDS` and `VALIDATE-INPUT-DATA-FIELDS`):

- At least one of `accountId` / `cardNumber` is present.
- Each supplied identifier is numeric and resolves via the repository; if both are supplied they must resolve to the same `acctId`.
- All required text/numeric fields are populated.
- `amount` matches the regex `^[+-]\d{8}\.\d{2}$` and parses within the `NUMERIC(11,2)` range.
- `origDate` / `procDate` match `^\d{4}-\d{2}-\d{2}$` **and** pass `DateValidator.validate(value, "YYYY-MM-DD")`.

Failures throw `ValidationException` carrying the field name and the exact user-facing message from COTRN02C (e.g. `"Amount should be in format -99999999.99"`). `GlobalExceptionHandler` (see §6) translates these into `400 Bad Request` responses.

### 4.3 `DateValidator` (replacement for `CSUTLDTC`)

```java
public final class DateValidator {
    public record Result(boolean valid, String severity, String message) {}

    private DateValidator() {}

    public static Result validate(String value, String pattern) {
        if (value == null || value.isBlank()) {
            return new Result(false, "3000", "Insufficient");
        }
        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern(pattern.replace("YYYY", "yyyy"))
                .withResolverStyle(ResolverStyle.STRICT);
            LocalDate.parse(value, fmt);
            return new Result(true, "0000", "Date is valid");
        } catch (DateTimeParseException e) {
            return new Result(false, "3000", "Datevalue error");
        }
    }
}
```

Rationale: `CEEDAYS` is a Language Environment service unavailable outside z/OS. `java.time` with `ResolverStyle.STRICT` provides equivalent calendar-aware validation (rejects 2024-02-30, 2024-13-01, etc.).

### 4.4 `TransactionIdGenerator`

Wraps the `transactions_tran_id_seq` sequence. Returns the next value zero-padded to 16 characters:

```java
@Component
@RequiredArgsConstructor
public class TransactionIdGenerator {
    private final EntityManager em;

    @Transactional
    public String next() {
        Long n = ((Number) em.createNativeQuery("SELECT nextval('transactions_tran_id_seq')")
            .getSingleResult()).longValue();
        return String.format("%016d", n);
    }
}
```

See §7.1 for the trade-offs against the COBOL "read last + increment" strategy.

### 4.5 Repositories

```java
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    Optional<Transaction> findTopByOrderByTranIdDesc();
    List<Transaction> findByCardNumOrderByOrigTsDesc(String cardNum, Pageable pageable);
}

public interface CardCrossReferenceRepository extends JpaRepository<CardCrossReference, String> {
    List<CardCrossReference> findByAcctId(Long acctId);
}

public interface AccountRepository extends JpaRepository<Account, Long> {}
```

---

## 5. CICS Command Replacement

| CICS command in COTRN02C | Spring Boot / Java equivalent | Notes |
|---|---|---|
| `EXEC CICS READ DATASET(CCXREF)`   | `cardXref.findById(cardNum)` returning `Optional<CardCrossReference>` | `DFHRESP(NOTFND)` → `Optional.empty()` → `CardNotFoundException` → `404` |
| `EXEC CICS READ DATASET(CXACAIX)`  | `cardXref.findByAcctId(acctId)` | Secondary index replaces AIX |
| `EXEC CICS WRITE DATASET(TRANSACT)` | `transactionRepository.save(t)` | `DFHRESP(DUPKEY/DUPREC)` → `DataIntegrityViolationException` → `409` |
| `EXEC CICS STARTBR / READPREV / ENDBR DATASET(TRANSACT)` | `transactionRepository.findTopByOrderByTranIdDesc()` (or `ORDER BY tran_id DESC LIMIT 1`) | |
| `EXEC CICS SEND MAP` / `RECEIVE MAP` | HTTP response / request binding in `TransactionController` | Field-level validation is handled by `jakarta.validation` annotations |
| `EXEC CICS RETURN TRANSID(CT02) COMMAREA(...)` | (none) | Stateless REST — no pseudo-conversational turn-taking |
| `EXEC CICS XCTL PROGRAM(...) COMMAREA(...)` | Internal `@Service` call, or inter-service HTTP when logically a different bounded context | |
| `DFHCOMMAREA` / `CARDDEMO-COMMAREA` | `CreateTransactionRequest` / `TransactionResponse` DTOs; short-term session state via signed JWT or an `Idempotency-Key` header | No shared memory area is preserved across requests |
| `EIBAID` (`DFHENTER`, `DFHPF3`, `DFHPF4`, `DFHPF5`) | Different endpoints: `POST /transactions` (ENTER), client-side navigation (PF3), client-side clear (PF4), `GET /transactions/last` (PF5) | Maps each AID key to a distinct interaction in the modern client |
| `DFHRESP(NOTFND)` / `DFHRESP(NORMAL)` | `Optional` + `DataAccessException` subtypes | See §7.5 for complete mapping |

---

## 6. Proposed Spring Boot Project Structure

```
transaction-service/
├── api-spec/
│   └── openapi.yaml                              ← OpenAPI 3.1 source of truth
├── src/main/java/com/carddemo/transaction/
│   ├── TransactionServiceApplication.java        ← @SpringBootApplication entry point
│   ├── controller/
│   │   ├── TransactionController.java            ← POST /api/v1/transactions, GET /last
│   │   ├── CardController.java                   ← GET /api/v1/cards/{id}
│   │   └── AccountCardsController.java           ← GET /api/v1/accounts/{id}/cards
│   ├── service/
│   │   ├── TransactionService.java               ← createTransaction / findLastTransaction
│   │   ├── TransactionValidationService.java     ← replaces VALIDATE-* paragraphs
│   │   └── TransactionIdGenerator.java           ← sequence-backed 16-digit generator
│   ├── repository/
│   │   ├── TransactionRepository.java
│   │   ├── AccountRepository.java
│   │   └── CardCrossReferenceRepository.java
│   ├── model/
│   │   ├── Transaction.java                      ← CVTRA05Y → JPA
│   │   ├── Account.java                          ← CVACT01Y → JPA
│   │   └── CardCrossReference.java               ← CVACT03Y → JPA
│   ├── dto/
│   │   ├── CreateTransactionRequest.java
│   │   ├── TransactionResponse.java
│   │   ├── CardResponse.java
│   │   └── AccountCardsResponse.java
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java           ← @ControllerAdvice, Problem+JSON
│   │   ├── ValidationException.java
│   │   ├── AccountNotFoundException.java
│   │   ├── CardNotFoundException.java
│   │   ├── CardAccountMismatchException.java
│   │   └── TransactionIdConflictException.java
│   └── util/
│       └── DateValidator.java                    ← CSUTLDTC replacement
├── src/main/resources/
│   ├── application.yml                           ← profiles: local / dev / stg / prod
│   └── db/migration/
│       ├── V1__baseline.sql                      ← §2.6 DDL
│       └── V2__seed_reference_data.sql
├── src/test/java/com/carddemo/transaction/
│   ├── controller/TransactionControllerTest.java ← @WebMvcTest
│   ├── service/TransactionServiceTest.java
│   ├── service/TransactionValidationServiceTest.java
│   ├── util/DateValidatorTest.java
│   └── integration/TransactionE2ETest.java       ← @SpringBootTest + Testcontainers
└── build.gradle                                  ← Gradle, Java 21, Spring Boot 3.x
```

Recommended dependencies (Gradle):

- `spring-boot-starter-web`
- `spring-boot-starter-validation`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-actuator`
- `spring-boot-starter-security`
- `org.springdoc:springdoc-openapi-starter-webmvc-ui`
- `org.flywaydb:flyway-core`
- `org.postgresql:postgresql` (runtime)
- `com.h2database:h2` (test)
- `org.testcontainers:postgresql` (test)
- `io.micrometer:micrometer-registry-prometheus`

---

## 7. Migration Risks and Considerations

### 7.1 Transaction ID generation

The COBOL strategy is "read the record with the highest key, add 1". That is:

- Not concurrency-safe on its own — VSAM record locking plus CICS serialisation on `CT02` happen to make it work at low throughput.
- Assumes `TRAN-ID` is numeric despite being `PIC X(16)` — inserts that don't originate from COTRN02C could break the invariant.

Options, in increasing order of compatibility effort:

| Strategy | Pros | Cons |
|---|---|---|
| **Database sequence + zero-padding to 16 chars** (recommended) | Concurrency-safe, monotonic, no extra lookups | Requires a migration seed so the sequence starts ≥ `MAX(tran_id::numeric)` after cutover |
| UUIDv7 stored as 16 uppercase hex chars | Globally unique, no coordination | Not numerically monotonic; breaks tooling that parses `tran_id` as a number |
| `SELECT MAX(tran_id) + 1 FOR UPDATE` | Closest literal match to COBOL | Serialises writes; defeats horizontal scaling |

For the "strangler" phase while COBOL is still writing to VSAM, either:
(a) carve `tran_id` numeric ranges — Spring Boot takes `[A, B)`, COBOL keeps `< A`; or
(b) point the COBOL program at the same database (via DB2/JDBC or a thin shim) so the sequence is authoritative.

### 7.2 EBCDIC → ASCII data conversion

Source VSAM files under [`app/data/EBCDIC/`](../app/data/EBCDIC/) must be translated before bulk import. For each file:

1. Export via IDCAMS `REPRO INFILE(KSDS) OUTFILE(PS)` to a sequential file.
2. Transfer in **binary** to the target system.
3. Parse fixed-width records by byte offset (lengths from the copybooks).
4. For `PIC X(n)` fields, translate EBCDIC code page 037 (or local CCSID) → UTF-8 and trim trailing spaces.
5. For zoned-decimal `PIC S9(n)V99` fields, unpack the sign nibble of the last byte.
6. For `COMP-3` (packed decimal) fields, unpack two digits per byte + sign nibble (see §7.3).

Java libraries: `com.ibm.jzos` (z/OS only), or `net.sf.jrecord`, or Apache Commons Codec + a hand-written `EbcdicCodec`. Validate by:

- Row-count checks against IDCAMS `LISTCAT`.
- Checksums on each numeric column (`SUM(tran_amt)`, `COUNT(*)`).
- Spot-check 10–50 known records.

### 7.3 `COMP-3` / packed-decimal fields

COTRN02C itself does not use `COMP-3` — the copybooks use plain display numeric (`PIC S9(n)V99`). But the wider CardDemo uses `COMP-3` in some places (e.g. audit extensions). When importing any file that uses it, handle the layout:

- Each byte contains **two** digits (high and low nibble).
- The last nibble is the sign: `C`/`F` → positive, `D` → negative, anything else is invalid.
- Total storage = `ceil((n + 1) / 2)` bytes for `PIC 9(n)`, `ceil((n + 2) / 2)` for `PIC S9(n)V99`.
- Store in Java as `BigDecimal` with the COBOL scale; never round or use `double`.

### 7.4 Pseudo-conversational CICS vs stateless REST

COTRN02C relies on the `CDEMO-PGM-REENTER` flag in the COMMAREA to know whether the user is starting fresh or responding. REST has no analogue. Equivalences:

| CICS behaviour | REST equivalent |
|---|---|
| First entry with empty screen | `GET /api/v1/transactions/form-defaults` (optional) or pure client state |
| User enters data and presses ENTER | `POST /api/v1/transactions/validate` (optional dry-run) |
| User confirms with `CONFIRM=Y` | `POST /api/v1/transactions` with `Idempotency-Key` |
| `PF5 = Copy Last Tran` | `GET /api/v1/transactions/last` |
| `PF3 = Back` | Client-side navigation |
| `PF4 = Clear` | Client-side form reset |

If the UI wants to preserve the two-step validate/confirm UX without violating REST statelessness, surface validation as a dedicated endpoint (`POST /transactions/validate`) that never inserts. The idempotency key covers accidental double submits.

### 7.5 Error handling — CICS `RESP` to HTTP status

| `DFHRESP(...)` | COBOL context | HTTP status | Problem `type` |
|---|---|---|---|
| `NORMAL` | successful read/write | 200 / 201 | — |
| `NOTFND` on `READ-CCXREF-FILE` | card lookup miss | 404 | `card-not-found` |
| `NOTFND` on `READ-CXACAIX-FILE` | account lookup miss | 404 | `account-not-found` |
| `ENDFILE` on `READPREV` | empty `TRANSACT` | 200 (first record) | — (sequence handles it) |
| `DUPKEY` / `DUPREC` on `WRITE` | tran ID collision | 409 | `transaction-id-conflict` |
| Any other on `READ`/`WRITE` | system error | 500 | `internal-error` |

Implementation: a `@ControllerAdvice` `GlobalExceptionHandler` maps each domain exception to `ProblemDetail` (`org.springframework.http.ProblemDetail`). The original COBOL user-facing messages (e.g. `"Account ID NOT found..."`) are preserved in `ProblemDetail.detail` to give parity with CICS behaviour during dual-run.

### 7.6 Testing strategy

Testing is a first-class deliverable because the migration is functionally equivalent, not a redesign. Cover each layer:

1. **Unit tests** — `TransactionValidationServiceTest`, `DateValidatorTest`, `TransactionIdGeneratorTest`. Drive every `EVALUATE` branch from §1.8 with one success and one failure case each. Minimum: 25 distinct validation cases.
2. **Controller tests** — `@WebMvcTest` with MockMvc; for every error in §7.5 assert both HTTP status **and** `ProblemDetail.detail`.
3. **Integration tests** — `@SpringBootTest` + Testcontainers (Postgres) + Flyway; full happy path and the three conflict scenarios (card-not-found, account-not-found, dup-key).
4. **Contract tests** — validate controller responses against `api-spec/openapi.yaml` using `swagger-request-validator`.
5. **Parallel-run tests (critical)** — for each VSAM-era transaction replayed through the new service, assert field-by-field equivalence of the resulting `Transaction` record. Suggested driver:

   ```
   forEach record in captured CICS trace:
     request  = toCreateTransactionRequest(record.input)
     expected = toTransactionRow(record.output)
     actual   = POST /api/v1/transactions (with Idempotency-Key)
     assertEquals(expected, actual) modulo tranId mapping
   ```

   Run this over at least one full business day of captured traffic before cutover; any divergence is a blocker.
6. **Regression dataset** — add the transactions covered in parallel-run to the Flyway `V2__seed_reference_data.sql` (anonymised) so future developers pick up the same coverage.

### 7.7 Security, observability, operability

- **AuthN/AuthZ**: preserve the `CDEMO-USER-TYPE` concept (`A`=admin, `U`=user) as a JWT claim. Spring Security rules: `POST /api/v1/transactions` requires authenticated user; `GET /cards/{id}` requires admin or the account owner.
- **Rate limiting / anti-replay**: enforce the `Idempotency-Key` header for all `POST` endpoints with a TTL ≥ the legacy screen timeout (suggested 24 h).
- **Observability**: Micrometer timers on each service method; structured JSON logs; correlation ID header propagated to the database (via `Connection.setClientInfo` on Postgres) so DB operations are traceable back to a request.
- **Auditing**: `created_at` column + an outbox table for replaying events into downstream analytics during the strangler phase.

### 7.8 Strangler-fig migration plan (recommended)

1. Stand up the Spring Boot service reading from a read-replica populated by CDC from the VSAM extract pipeline. Traffic: `GET` only.
2. Enable dual-write: COBOL `COTRN02C` continues to be the source of truth; Spring Boot writes to its own copy via an async event consumer and reconciles nightly.
3. Flip writes: put an API gateway in front, route `POST /api/v1/transactions` to the Spring Boot service, have Spring Boot publish an event back to VSAM through a shim until the COBOL consumer is retired.
4. Retire COTRN02C, CICS map `COTRN2A`, and VSAM datasets; shut down the shim; archive the VSAM data.

---

## Appendix A — End-to-end example

Legacy flow (CICS):

```
user → 3270 → CICS CT02 → COTRN02C → RECEIVE MAP
                          → VALIDATE-INPUT-KEY-FIELDS → READ CXACAIX → XREF-CARD-NUM
                          → VALIDATE-INPUT-DATA-FIELDS → CSUTLDTC × 2
                          → ADD-TRANSACTION → STARTBR/READPREV/ENDBR TRANSACT
                                            → WRITE TRANSACT
                          → SEND MAP ("Transaction added successfully. Your Tran ID is 0000000000000042.")
```

Target flow (Spring Boot):

```
client → POST /api/v1/transactions
                       ↓
           TransactionController
                       ↓
           TransactionValidationService
             └─ CardCrossReferenceRepository.findByAcctId(...)
             └─ DateValidator.validate(...) × 2
                       ↓
           TransactionService.createTransaction
             └─ TransactionIdGenerator.next() → "0000000000000042"
             └─ TransactionRepository.save(...)
                       ↓
           201 Created { "tranId": "0000000000000042", ... }
```

## Appendix B — File inventory referenced by this plan

| Area | File |
|---|---|
| Program source | [`app/cbl/COTRN02C.cbl`](../app/cbl/COTRN02C.cbl) |
| Date utility | [`app/cbl/CSUTLDTC.cbl`](../app/cbl/CSUTLDTC.cbl) |
| Record layouts | [`app/cpy/CVTRA05Y.cpy`](../app/cpy/CVTRA05Y.cpy), [`app/cpy/CVACT01Y.cpy`](../app/cpy/CVACT01Y.cpy), [`app/cpy/CVACT03Y.cpy`](../app/cpy/CVACT03Y.cpy) |
| Common copybooks | [`app/cpy/COCOM01Y.cpy`](../app/cpy/COCOM01Y.cpy), [`app/cpy/CSDAT01Y.cpy`](../app/cpy/CSDAT01Y.cpy), [`app/cpy/CSMSG01Y.cpy`](../app/cpy/CSMSG01Y.cpy), [`app/cpy/COTTL01Y.cpy`](../app/cpy/COTTL01Y.cpy) |
| Screen | [`app/bms/COTRN02.bms`](../app/bms/COTRN02.bms), [`app/cpy-bms/COTRN02.CPY`](../app/cpy-bms/COTRN02.CPY) |
