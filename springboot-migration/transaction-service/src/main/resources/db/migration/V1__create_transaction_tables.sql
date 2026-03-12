-- Flyway migration: Create all transaction-related tables
-- Mapped from COBOL copybooks: CVTRA05Y, CVTRA06Y, CVTRA03Y, CVTRA04Y, CVTRA02Y, CVTRA01Y
-- Replaces TRANSACT, DALYTRAN, TRANTYPE, TRANCATG, DISCGRP, CATBALANCE VSAM files

CREATE TABLE IF NOT EXISTS transaction_types (
    transaction_type_code VARCHAR(2)  NOT NULL PRIMARY KEY,
    description           VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS transaction_categories (
    transaction_type_code VARCHAR(2)   NOT NULL,
    category_code         VARCHAR(4)   NOT NULL,
    description           VARCHAR(100),
    PRIMARY KEY (transaction_type_code, category_code)
);

CREATE TABLE IF NOT EXISTS transactions (
    transaction_id            VARCHAR(16)    NOT NULL PRIMARY KEY,
    transaction_type_code     VARCHAR(2),
    transaction_category_code VARCHAR(4),
    transaction_source        VARCHAR(10),
    transaction_description   VARCHAR(100),
    transaction_amount        DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    merchant_id               VARCHAR(9),
    merchant_name             VARCHAR(50),
    merchant_city             VARCHAR(50),
    merchant_zip              VARCHAR(10),
    card_number               VARCHAR(16)    NOT NULL,
    transaction_timestamp     TIMESTAMP      NOT NULL,
    processed_timestamp       TIMESTAMP
);

-- Composite index replacing VSAM AIX alternate index
CREATE INDEX IF NOT EXISTS idx_transactions_card_timestamp
    ON transactions(card_number, transaction_timestamp);
CREATE INDEX IF NOT EXISTS idx_transactions_card_number
    ON transactions(card_number);

CREATE TABLE IF NOT EXISTS daily_transactions (
    transaction_id            VARCHAR(16)    NOT NULL PRIMARY KEY,
    transaction_type_code     VARCHAR(2),
    transaction_category_code VARCHAR(4),
    transaction_source        VARCHAR(10),
    transaction_description   VARCHAR(100),
    transaction_amount        DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    merchant_id               VARCHAR(9),
    merchant_name             VARCHAR(50),
    merchant_city             VARCHAR(50),
    merchant_zip              VARCHAR(10),
    card_number               VARCHAR(16)    NOT NULL,
    transaction_timestamp     TIMESTAMP      NOT NULL,
    processed_timestamp       TIMESTAMP
);

CREATE TABLE IF NOT EXISTS disclosure_groups (
    account_group_id          VARCHAR(10)    NOT NULL,
    transaction_type_code     VARCHAR(2)     NOT NULL,
    transaction_category_code VARCHAR(4)     NOT NULL,
    interest_rate             DECIMAL(8, 4)  NOT NULL DEFAULT 0.0000,
    PRIMARY KEY (account_group_id, transaction_type_code, transaction_category_code)
);

CREATE TABLE IF NOT EXISTS category_balances (
    account_id                BIGINT         NOT NULL,
    transaction_type_code     VARCHAR(2)     NOT NULL,
    category_code             VARCHAR(4)     NOT NULL,
    balance                   DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    PRIMARY KEY (account_id, transaction_type_code, category_code)
);

COMMENT ON TABLE transactions IS 'Transaction history — migrated from TRANSACT VSAM file';
COMMENT ON TABLE daily_transactions IS 'Daily transaction staging — migrated from DALYTRAN VSAM file';
COMMENT ON TABLE transaction_types IS 'Transaction type codes — migrated from TRANTYPE VSAM file';
COMMENT ON TABLE transaction_categories IS 'Transaction categories — migrated from TRANCATG VSAM file';
COMMENT ON TABLE disclosure_groups IS 'Interest rate disclosure groups — migrated from DISCGRP VSAM file';
COMMENT ON TABLE category_balances IS 'Balance by category — migrated from CATBALANCE VSAM file';
