-- CardDemo PostgreSQL Schema
-- Migrated from COBOL VSAM file layouts (copybooks in app/cpy/)

-- Account table: based on CVACT01Y.cpy (RECLN 300)
CREATE TABLE account (
    acct_id         BIGINT          PRIMARY KEY,          -- ACCT-ID PIC 9(11)
    active_status   VARCHAR(1)      NOT NULL,             -- ACCT-ACTIVE-STATUS PIC X(01)
    curr_bal        DECIMAL(12,2),                        -- ACCT-CURR-BAL PIC S9(10)V99
    credit_limit    DECIMAL(12,2),                        -- ACCT-CREDIT-LIMIT PIC S9(10)V99
    cash_credit_limit DECIMAL(12,2),                     -- ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99
    open_date       DATE,                                 -- ACCT-OPEN-DATE PIC X(10)
    expiration_date DATE,                                 -- ACCT-EXPIRAION-DATE PIC X(10)
    reissue_date    DATE,                                 -- ACCT-REISSUE-DATE PIC X(10)
    curr_cyc_credit DECIMAL(12,2),                        -- ACCT-CURR-CYC-CREDIT PIC S9(10)V99
    curr_cyc_debit  DECIMAL(12,2),                        -- ACCT-CURR-CYC-DEBIT PIC S9(10)V99
    addr_zip        VARCHAR(10),                          -- ACCT-ADDR-ZIP PIC X(10)
    group_id        VARCHAR(10)                           -- ACCT-GROUP-ID PIC X(10)
);

-- Customer table: based on CVCUS01Y.cpy (RECLN 500)
CREATE TABLE customer (
    cust_id         BIGINT          PRIMARY KEY,          -- CUST-ID PIC 9(09)
    first_name      VARCHAR(25),                          -- CUST-FIRST-NAME PIC X(25)
    middle_name     VARCHAR(25),                          -- CUST-MIDDLE-NAME PIC X(25)
    last_name       VARCHAR(25),                          -- CUST-LAST-NAME PIC X(25)
    addr_line1      VARCHAR(50),                          -- CUST-ADDR-LINE-1 PIC X(50)
    addr_line2      VARCHAR(50),                          -- CUST-ADDR-LINE-2 PIC X(50)
    addr_line3      VARCHAR(50),                          -- CUST-ADDR-LINE-3 PIC X(50)
    state_cd        VARCHAR(2),                           -- CUST-ADDR-STATE-CD PIC X(02)
    country_cd      VARCHAR(3),                           -- CUST-ADDR-COUNTRY-CD PIC X(03)
    zip_code        VARCHAR(10),                          -- CUST-ADDR-ZIP PIC X(10)
    phone_num1      VARCHAR(15),                          -- CUST-PHONE-NUM-1 PIC X(15)
    phone_num2      VARCHAR(15),                          -- CUST-PHONE-NUM-2 PIC X(15)
    ssn             VARCHAR(9),                           -- CUST-SSN PIC 9(09)
    govt_issued_id  VARCHAR(20),                          -- CUST-GOVT-ISSUED-ID PIC X(20)
    date_of_birth   DATE,                                 -- CUST-DOB-YYYY-MM-DD PIC X(10)
    eft_account_id  VARCHAR(10),                          -- CUST-EFT-ACCOUNT-ID PIC X(10)
    pri_card_holder_ind VARCHAR(1),                       -- CUST-PRI-CARD-HOLDER-IND PIC X(01)
    fico_score      INTEGER,                              -- CUST-FICO-CREDIT-SCORE PIC 9(03)
    active_status   VARCHAR(1)
);

-- Card table: based on CVACT02Y.cpy (RECLN 150)
CREATE TABLE card (
    card_num        VARCHAR(16)     PRIMARY KEY,          -- CARD-NUM PIC X(16)
    acct_id         BIGINT          NOT NULL REFERENCES account(acct_id),  -- CARD-ACCT-ID PIC 9(11)
    cvv_cd          INTEGER,                              -- CARD-CVV-CD PIC 9(03)
    embossed_name   VARCHAR(50),                          -- CARD-EMBOSSED-NAME PIC X(50)
    expiration_date DATE,                                 -- CARD-EXPIRAION-DATE PIC X(10)
    active_status   VARCHAR(1)                            -- CARD-ACTIVE-STATUS PIC X(01)
);

-- Card cross-reference table: based on CVACT03Y.cpy (RECLN 50)
CREATE TABLE card_xref (
    card_num        VARCHAR(16)     PRIMARY KEY,          -- XREF-CARD-NUM PIC X(16)
    cust_id         BIGINT          NOT NULL REFERENCES customer(cust_id),  -- XREF-CUST-ID PIC 9(09)
    acct_id         BIGINT          NOT NULL REFERENCES account(acct_id)    -- XREF-ACCT-ID PIC 9(11)
);

-- Transaction table: based on CVTRA05Y.cpy (RECLN 350)
-- Composite key matches VSAM KSDS key structure (card_num + tran_id)
CREATE TABLE transaction (
    card_num        VARCHAR(16)     NOT NULL,             -- TRAN-CARD-NUM PIC X(16)
    tran_id         VARCHAR(16)     NOT NULL,             -- TRAN-ID PIC X(16)
    type_cd         VARCHAR(2),                           -- TRAN-TYPE-CD PIC X(02)
    cat_cd          INTEGER,                              -- TRAN-CAT-CD PIC 9(04)
    source          VARCHAR(10),                          -- TRAN-SOURCE PIC X(10)
    description     VARCHAR(100),                         -- TRAN-DESC PIC X(100)
    amount          DECIMAL(11,2),                        -- TRAN-AMT PIC S9(09)V99
    merchant_id     BIGINT,                               -- TRAN-MERCHANT-ID PIC 9(09)
    merchant_name   VARCHAR(50),                          -- TRAN-MERCHANT-NAME PIC X(50)
    merchant_city   VARCHAR(50),                          -- TRAN-MERCHANT-CITY PIC X(50)
    merchant_zip    VARCHAR(10),                          -- TRAN-MERCHANT-ZIP PIC X(10)
    orig_ts         TIMESTAMP,                            -- TRAN-ORIG-TS PIC X(26)
    proc_ts         TIMESTAMP,                            -- TRAN-PROC-TS PIC X(26)
    PRIMARY KEY (card_num, tran_id)
);

-- Transaction category balance table: based on CVTRA01Y.cpy (RECLN 50)
CREATE TABLE tran_cat_balance (
    acct_id         BIGINT          NOT NULL,             -- TRANCAT-ACCT-ID PIC 9(11)
    type_cd         VARCHAR(2)      NOT NULL,             -- TRANCAT-TYPE-CD PIC X(02)
    cat_cd          INTEGER         NOT NULL,             -- TRANCAT-CD PIC 9(04)
    balance         DECIMAL(12,2),                        -- TRAN-CAT-BAL PIC S9(09)V99
    PRIMARY KEY (acct_id, type_cd, cat_cd)
);

-- User security table: based on CSUSR01Y.cpy
CREATE TABLE user_security (
    user_id         VARCHAR(8)      PRIMARY KEY,          -- SEC-USR-ID PIC X(08)
    password        VARCHAR(72),                          -- sized for BCrypt hash
    user_type       VARCHAR(1)      NOT NULL CHECK (user_type IN ('A', 'U')),  -- SEC-USR-TYPE
    first_name      VARCHAR(20),                          -- SEC-USR-FNAME PIC X(20)
    last_name       VARCHAR(20)                           -- SEC-USR-LNAME PIC X(20)
);

-- Discount group table: based on CVTRA02Y.cpy (RECLN 50)
CREATE TABLE discount_group (
    group_id        VARCHAR(10)     NOT NULL,             -- DIS-ACCT-GROUP-ID PIC X(10)
    tran_type_cd    VARCHAR(2)      NOT NULL,             -- DIS-TRAN-TYPE-CD PIC X(02)
    tran_cat_cd     INTEGER         NOT NULL,             -- DIS-TRAN-CAT-CD PIC 9(04)
    discount_rate   DECIMAL(7,2),                         -- DIS-INT-RATE PIC S9(04)V99
    PRIMARY KEY (group_id, tran_type_cd, tran_cat_cd)
);

-- Indexes on foreign keys and commonly queried columns
CREATE INDEX idx_card_acct_id ON card(acct_id);
CREATE INDEX idx_card_xref_acct_id ON card_xref(acct_id);
CREATE INDEX idx_card_xref_cust_id ON card_xref(cust_id);
CREATE INDEX idx_transaction_card_num ON transaction(card_num);
CREATE INDEX idx_tran_cat_balance_acct_id ON tran_cat_balance(acct_id);
