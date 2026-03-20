-- V1: CardDemo database schema derived from COBOL copybooks (VSAM KSDS files)

-- Account table from CVACT01Y copybook (RECLN 300)
CREATE TABLE account (
    acct_id            CHAR(11)       NOT NULL PRIMARY KEY,
    active_status      CHAR(1),
    curr_bal           DECIMAL(12,2)  DEFAULT 0,
    credit_limit       DECIMAL(12,2)  DEFAULT 0,
    cash_credit_limit  DECIMAL(12,2)  DEFAULT 0,
    open_date          DATE,
    expiration_date    DATE,
    reissue_date       DATE,
    curr_cyc_credit    DECIMAL(12,2)  DEFAULT 0,
    curr_cyc_debit     DECIMAL(12,2)  DEFAULT 0,
    addr_zip           VARCHAR(10),
    group_id           VARCHAR(10)
);

-- Customer table from CVCUS01Y copybook (RECLN 500)
CREATE TABLE customer (
    cust_id                CHAR(9)        NOT NULL PRIMARY KEY,
    first_name             VARCHAR(25),
    middle_name            VARCHAR(25),
    last_name              VARCHAR(25),
    addr_line_1            VARCHAR(50),
    addr_line_2            VARCHAR(50),
    addr_line_3            VARCHAR(50),
    addr_state_cd          CHAR(2),
    addr_country_cd        CHAR(3),
    addr_zip               VARCHAR(10),
    phone_num_1            VARCHAR(15),
    phone_num_2            VARCHAR(15),
    ssn                    CHAR(9),
    govt_issued_id         VARCHAR(20),
    dob                    DATE,
    eft_account_id         VARCHAR(10),
    pri_card_holder_ind    CHAR(1),
    fico_credit_score      INTEGER
);

-- Card table from CVACT02Y copybook (RECLN 150)
CREATE TABLE card (
    card_num           CHAR(16)       NOT NULL PRIMARY KEY,
    acct_id            CHAR(11),
    cvv_cd             CHAR(3),
    embossed_name      VARCHAR(50),
    expiration_date    DATE,
    active_status      CHAR(1),
    CONSTRAINT fk_card_account FOREIGN KEY (acct_id) REFERENCES account(acct_id)
);

-- Card cross-reference table from CVACT03Y copybook (RECLN 50)
CREATE TABLE card_cross_ref (
    card_num           CHAR(16)       NOT NULL PRIMARY KEY,
    cust_id            CHAR(9),
    acct_id            CHAR(11),
    CONSTRAINT fk_xref_customer FOREIGN KEY (cust_id) REFERENCES customer(cust_id),
    CONSTRAINT fk_xref_account  FOREIGN KEY (acct_id) REFERENCES account(acct_id)
);

-- Transaction type table from CVTRA03Y copybook (RECLN 60)
CREATE TABLE transaction_type (
    type_cd            CHAR(2)        NOT NULL PRIMARY KEY,
    type_desc          VARCHAR(50)
);

-- Transaction category table from CVTRA04Y copybook (RECLN 60)
CREATE TABLE transaction_category (
    type_cd            CHAR(2)        NOT NULL,
    cat_cd             INTEGER        NOT NULL,
    cat_type_desc      VARCHAR(50),
    PRIMARY KEY (type_cd, cat_cd),
    CONSTRAINT fk_tcat_type FOREIGN KEY (type_cd) REFERENCES transaction_type(type_cd)
);

-- Transaction table from CVTRA05Y copybook (RECLN 350)
CREATE TABLE transaction (
    tran_id            CHAR(16)       NOT NULL PRIMARY KEY,
    type_cd            CHAR(2),
    cat_cd             INTEGER,
    source             VARCHAR(10),
    description        VARCHAR(100),
    amount             DECIMAL(11,2),
    merchant_id        CHAR(9),
    merchant_name      VARCHAR(50),
    merchant_city      VARCHAR(50),
    merchant_zip       VARCHAR(10),
    card_num           CHAR(16),
    orig_ts            TIMESTAMP,
    proc_ts            TIMESTAMP,
    CONSTRAINT fk_tran_card FOREIGN KEY (card_num) REFERENCES card(card_num)
);

-- Daily transaction table from CVTRA06Y copybook (RECLN 350) - staging
CREATE TABLE daily_transaction (
    tran_id            CHAR(16)       NOT NULL PRIMARY KEY,
    type_cd            CHAR(2),
    cat_cd             INTEGER,
    source             VARCHAR(10),
    description        VARCHAR(100),
    amount             DECIMAL(11,2),
    merchant_id        CHAR(9),
    merchant_name      VARCHAR(50),
    merchant_city      VARCHAR(50),
    merchant_zip       VARCHAR(10),
    card_num           CHAR(16),
    orig_ts            TIMESTAMP,
    proc_ts            TIMESTAMP
);

-- Disclosure group table from CVTRA02Y copybook (RECLN 50)
CREATE TABLE disclosure_group (
    group_id           VARCHAR(10)    NOT NULL,
    type_cd            CHAR(2)        NOT NULL,
    cat_cd             INTEGER        NOT NULL,
    int_rate           DECIMAL(6,2),
    PRIMARY KEY (group_id, type_cd, cat_cd)
);

-- Transaction category balance table from CVTRA01Y copybook (RECLN 50)
CREATE TABLE tran_category_balance (
    acct_id            CHAR(11)       NOT NULL,
    type_cd            CHAR(2)        NOT NULL,
    cat_cd             INTEGER        NOT NULL,
    balance            DECIMAL(11,2)  DEFAULT 0,
    PRIMARY KEY (acct_id, type_cd, cat_cd),
    CONSTRAINT fk_tcb_account FOREIGN KEY (acct_id) REFERENCES account(acct_id)
);

-- User security table from CSUSR01Y copybook (RECLN 80)
CREATE TABLE user_security (
    user_id            CHAR(8)        NOT NULL PRIMARY KEY,
    first_name         VARCHAR(20),
    last_name          VARCHAR(20),
    password           VARCHAR(8),
    user_type          CHAR(1)
);

-- Indexes for common query patterns (replacing VSAM AIX)
CREATE INDEX idx_card_acct_id ON card(acct_id);
CREATE INDEX idx_xref_acct_id ON card_cross_ref(acct_id);
CREATE INDEX idx_xref_cust_id ON card_cross_ref(cust_id);
CREATE INDEX idx_tran_card_num ON transaction(card_num);
CREATE INDEX idx_tran_type_cd ON transaction(type_cd);
CREATE INDEX idx_daily_tran_card_num ON daily_transaction(card_num);
CREATE INDEX idx_tcb_acct_id ON tran_category_balance(acct_id);
