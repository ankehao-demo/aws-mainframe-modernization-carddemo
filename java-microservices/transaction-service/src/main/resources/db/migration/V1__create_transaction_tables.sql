CREATE TABLE transactions (
    transaction_id       VARCHAR(16) PRIMARY KEY,
    type_code            VARCHAR(2),
    category_code        INTEGER,
    source               VARCHAR(10),
    description          VARCHAR(100),
    amount               DECIMAL(11,2),
    merchant_id          BIGINT,
    merchant_name        VARCHAR(50),
    merchant_city        VARCHAR(50),
    merchant_zip         VARCHAR(10),
    card_number          VARCHAR(16),
    account_id           BIGINT,
    original_timestamp   VARCHAR(26),
    processed_timestamp  VARCHAR(26)
);
CREATE INDEX idx_tran_card ON transactions(card_number);
CREATE INDEX idx_tran_account ON transactions(account_id);

CREATE TABLE daily_transactions (
    transaction_id       VARCHAR(16) PRIMARY KEY,
    type_code            VARCHAR(2),
    category_code        INTEGER,
    source               VARCHAR(10),
    description          VARCHAR(100),
    amount               DECIMAL(11,2),
    merchant_id          BIGINT,
    merchant_name        VARCHAR(50),
    merchant_city        VARCHAR(50),
    merchant_zip         VARCHAR(10),
    card_number          VARCHAR(16),
    original_timestamp   VARCHAR(26),
    processed_timestamp  VARCHAR(26)
);

CREATE TABLE transaction_types (
    type_code        VARCHAR(2) PRIMARY KEY,
    type_description VARCHAR(50)
);

CREATE TABLE transaction_categories (
    type_code            VARCHAR(2),
    category_code        INTEGER,
    category_description VARCHAR(50),
    PRIMARY KEY (type_code, category_code)
);

CREATE TABLE disclosure_groups (
    account_group_id VARCHAR(10),
    type_code        VARCHAR(2),
    category_code    INTEGER,
    interest_rate    DECIMAL(6,2),
    PRIMARY KEY (account_group_id, type_code, category_code)
);

CREATE TABLE category_balances (
    account_id    BIGINT,
    type_code     VARCHAR(2),
    category_code INTEGER,
    balance       DECIMAL(11,2),
    PRIMARY KEY (account_id, type_code, category_code)
);
