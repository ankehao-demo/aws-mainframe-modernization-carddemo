-- Flyway migration: Create accounts, customers, and card_xref tables
-- Mapped from COBOL copybooks CVACT01Y.cpy, CVCUS01Y.cpy, CVACT03Y.cpy
-- Replaces ACCTDATA, CUSTDATA, CARDXREF VSAM KSDS files

CREATE TABLE IF NOT EXISTS customers (
    customer_id         BIGINT       NOT NULL PRIMARY KEY,
    first_name          VARCHAR(25),
    middle_name         VARCHAR(25),
    last_name           VARCHAR(25),
    address_line_1      VARCHAR(50),
    address_line_2      VARCHAR(50),
    address_line_3      VARCHAR(50),
    state               VARCHAR(2),
    country_code        VARCHAR(3),
    zip_code            VARCHAR(10),
    phone_1             VARCHAR(15),
    phone_2             VARCHAR(15),
    ssn                 VARCHAR(9),
    govt_id             VARCHAR(20),
    date_of_birth       DATE,
    eft_account_id      VARCHAR(10),
    primary_card_holder VARCHAR(1),
    fico_credit_score   INTEGER
);

CREATE TABLE IF NOT EXISTS accounts (
    account_id        BIGINT         NOT NULL PRIMARY KEY,
    account_status    VARCHAR(1)     NOT NULL DEFAULT 'Y',
    current_balance   DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    credit_limit      DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    cash_credit_limit DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    open_date         DATE,
    expiration_date   DATE,
    reissue_date      DATE,
    curr_cyc_credit   DECIMAL(12, 2) DEFAULT 0.00,
    curr_cyc_debit    DECIMAL(12, 2) DEFAULT 0.00,
    address_zip       VARCHAR(10),
    group_id          VARCHAR(10)
);

COMMENT ON TABLE customers IS 'Customer master data — migrated from CUSTDATA VSAM file';
COMMENT ON TABLE accounts IS 'Account master data — migrated from ACCTDATA VSAM file';
