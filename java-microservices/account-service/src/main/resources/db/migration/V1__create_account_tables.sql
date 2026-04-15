CREATE TABLE accounts (
    account_id           BIGINT       PRIMARY KEY,
    active_status        CHAR(1),
    current_balance      DECIMAL(12,2),
    credit_limit         DECIMAL(12,2),
    cash_credit_limit    DECIMAL(12,2),
    open_date            VARCHAR(10),
    expiration_date      VARCHAR(10),
    reissue_date         VARCHAR(10),
    current_cycle_credit DECIMAL(12,2),
    current_cycle_debit  DECIMAL(12,2),
    address_zip          VARCHAR(10),
    group_id             VARCHAR(10)
);

CREATE TABLE customers (
    customer_id          BIGINT       PRIMARY KEY,
    first_name           VARCHAR(25),
    middle_name          VARCHAR(25),
    last_name            VARCHAR(25),
    address_line_1       VARCHAR(50),
    address_line_2       VARCHAR(50),
    address_line_3       VARCHAR(50),
    address_state_cd     CHAR(2),
    address_country_cd   CHAR(3),
    address_zip          VARCHAR(10),
    phone_num_1          VARCHAR(15),
    phone_num_2          VARCHAR(15),
    ssn                  BIGINT,
    govt_issued_id       VARCHAR(20),
    date_of_birth        VARCHAR(10),
    eft_account_id       VARCHAR(10),
    primary_card_holder_ind CHAR(1),
    fico_credit_score    INTEGER
);

CREATE TABLE card_xref (
    card_number  VARCHAR(16) PRIMARY KEY,
    customer_id  BIGINT,
    account_id   BIGINT
);
CREATE INDEX idx_card_xref_account ON card_xref(account_id);
CREATE INDEX idx_card_xref_customer ON card_xref(customer_id);
