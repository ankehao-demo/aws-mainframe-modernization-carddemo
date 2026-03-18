-- CardDemo Database Schema
-- Migrated from COBOL/VSAM mainframe application

-- Account master table (from CVACT01Y.cpy, RECLN 300)
CREATE TABLE account (
    acct_id              VARCHAR(11)       NOT NULL,
    acct_active_status   VARCHAR(1)        NOT NULL DEFAULT 'Y',
    acct_curr_bal        DECIMAL(12,2)  NOT NULL DEFAULT 0.00,
    acct_credit_limit    DECIMAL(12,2)  NOT NULL DEFAULT 0.00,
    acct_cash_credit_limit DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    acct_open_date       VARCHAR(10),
    acct_expiration_date VARCHAR(10),
    acct_reissue_date    VARCHAR(10),
    acct_curr_cyc_credit DECIMAL(12,2)  NOT NULL DEFAULT 0.00,
    acct_curr_cyc_debit  DECIMAL(12,2)  NOT NULL DEFAULT 0.00,
    acct_addr_zip        VARCHAR(10),
    acct_group_id        VARCHAR(10),
    version              BIGINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (acct_id)
);

-- Card master table (from CVACT02Y.cpy, RECLN 150)
CREATE TABLE card (
    card_num             VARCHAR(16)       NOT NULL,
    card_acct_id         VARCHAR(11)       NOT NULL,
    card_cvv_cd          VARCHAR(3)        NOT NULL,
    card_embossed_name   VARCHAR(50),
    card_expiration_date VARCHAR(10),
    card_active_status   VARCHAR(1)        NOT NULL DEFAULT 'Y',
    PRIMARY KEY (card_num),
    CONSTRAINT fk_card_account FOREIGN KEY (card_acct_id) REFERENCES account(acct_id)
);

CREATE INDEX idx_card_acct_id ON card(card_acct_id);

-- Customer master table (from CVCUS01Y.cpy, RECLN 500)
CREATE TABLE customer (
    cust_id              VARCHAR(9)        NOT NULL,
    cust_first_name      VARCHAR(25),
    cust_middle_name     VARCHAR(25),
    cust_last_name       VARCHAR(25),
    cust_addr_line_1     VARCHAR(50),
    cust_addr_line_2     VARCHAR(50),
    cust_addr_line_3     VARCHAR(50),
    cust_addr_state_cd   VARCHAR(2),
    cust_addr_country_cd VARCHAR(3),
    cust_addr_zip        VARCHAR(10),
    cust_phone_num_1     VARCHAR(15),
    cust_phone_num_2     VARCHAR(15),
    cust_ssn             VARCHAR(9),
    cust_govt_issued_id  VARCHAR(20),
    cust_dob_yyyy_mm_dd  VARCHAR(10),
    cust_eft_account_id  VARCHAR(10),
    cust_pri_card_holder_ind VARCHAR(1),
    cust_fico_credit_score INTEGER,
    PRIMARY KEY (cust_id)
);

-- Card cross-reference table (from CVACT03Y.cpy, RECLN 50)
CREATE TABLE card_xref (
    xref_card_num        VARCHAR(16)       NOT NULL,
    xref_cust_id         VARCHAR(9)        NOT NULL,
    xref_acct_id         VARCHAR(11)       NOT NULL,
    PRIMARY KEY (xref_card_num),
    CONSTRAINT fk_xref_customer FOREIGN KEY (xref_cust_id) REFERENCES customer(cust_id),
    CONSTRAINT fk_xref_account FOREIGN KEY (xref_acct_id) REFERENCES account(acct_id)
);

CREATE INDEX idx_xref_acct_id ON card_xref(xref_acct_id);
CREATE INDEX idx_xref_cust_id ON card_xref(xref_cust_id);

-- Transaction master table (from CVTRA05Y.cpy, RECLN 350)
CREATE TABLE transaction (
    tran_id              VARCHAR(16)       NOT NULL,
    tran_type_cd         VARCHAR(2),
    tran_cat_cd          INTEGER,
    tran_source          VARCHAR(10),
    tran_desc            VARCHAR(100),
    tran_amt             DECIMAL(11,2),
    tran_merchant_id     BIGINT,
    tran_merchant_name   VARCHAR(50),
    tran_merchant_city   VARCHAR(50),
    tran_merchant_zip    VARCHAR(10),
    tran_card_num        VARCHAR(16),
    tran_orig_ts         VARCHAR(26),
    tran_proc_ts         VARCHAR(26),
    PRIMARY KEY (tran_id)
);

CREATE INDEX idx_tran_card_num ON transaction(tran_card_num);

-- Daily transaction table (from CVTRA06Y.cpy, RECLN 350)
CREATE TABLE daily_transaction (
    id                   BIGSERIAL      NOT NULL,
    dalytran_id          VARCHAR(16),
    dalytran_type_cd     VARCHAR(2),
    dalytran_cat_cd      INTEGER,
    dalytran_source      VARCHAR(10),
    dalytran_desc        VARCHAR(100),
    dalytran_amt         DECIMAL(11,2),
    dalytran_merchant_id BIGINT,
    dalytran_merchant_name VARCHAR(50),
    dalytran_merchant_city VARCHAR(50),
    dalytran_merchant_zip VARCHAR(10),
    dalytran_card_num    VARCHAR(16),
    dalytran_orig_ts     VARCHAR(26),
    dalytran_proc_ts     VARCHAR(26),
    posted               BOOLEAN        NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
);

CREATE INDEX idx_dalytran_card_num ON daily_transaction(dalytran_card_num);
CREATE INDEX idx_dalytran_posted ON daily_transaction(posted);

-- Daily transaction reject table (for batch processing rejects)
CREATE TABLE daily_transaction_reject (
    id                   BIGSERIAL      NOT NULL,
    daily_transaction_id BIGINT         NOT NULL,
    reject_reason_code   INTEGER        NOT NULL,
    reject_reason_desc   VARCHAR(100),
    reject_timestamp     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- User security table (from CSUSR01Y.cpy, RECLN 80)
CREATE TABLE user_security (
    usr_id               VARCHAR(8)        NOT NULL,
    usr_fname            VARCHAR(20),
    usr_lname            VARCHAR(20),
    usr_pwd              VARCHAR(8)        NOT NULL,
    usr_type             VARCHAR(1)        NOT NULL DEFAULT 'U',
    PRIMARY KEY (usr_id)
);

-- Transaction type table (from CVTRA03Y.cpy, RECLN 60)
CREATE TABLE transaction_type (
    tran_type            VARCHAR(2)        NOT NULL,
    tran_type_desc       VARCHAR(50),
    PRIMARY KEY (tran_type)
);

-- Transaction category table (from CVTRA04Y.cpy, RECLN 60)
CREATE TABLE transaction_category (
    tran_type_cd         VARCHAR(2)        NOT NULL,
    tran_cat_cd          INTEGER        NOT NULL,
    tran_cat_type_desc   VARCHAR(50),
    PRIMARY KEY (tran_type_cd, tran_cat_cd),
    CONSTRAINT fk_trancat_type FOREIGN KEY (tran_type_cd) REFERENCES transaction_type(tran_type)
);

-- Transaction category balance table (from CVTRA01Y.cpy, RECLN 50)
CREATE TABLE tran_cat_balance (
    trancat_acct_id      VARCHAR(11)       NOT NULL,
    trancat_type_cd      VARCHAR(2)        NOT NULL,
    trancat_cd           INTEGER        NOT NULL,
    tran_cat_bal         DECIMAL(11,2)  NOT NULL DEFAULT 0.00,
    PRIMARY KEY (trancat_acct_id, trancat_type_cd, trancat_cd),
    CONSTRAINT fk_tcb_account FOREIGN KEY (trancat_acct_id) REFERENCES account(acct_id)
);

-- Disclosure group table (from CVTRA02Y.cpy, RECLN 50)
CREATE TABLE disclosure_group (
    dis_acct_group_id    VARCHAR(10)    NOT NULL,
    dis_tran_type_cd     VARCHAR(2)        NOT NULL,
    dis_tran_cat_cd      INTEGER        NOT NULL,
    dis_int_rate         DECIMAL(6,2)   NOT NULL DEFAULT 0.00,
    PRIMARY KEY (dis_acct_group_id, dis_tran_type_cd, dis_tran_cat_cd)
);

-- Pending authorization summary (from IMS HIDAM PAUTSUM0 segment)
CREATE TABLE pending_auth_summary (
    id                   BIGSERIAL      NOT NULL,
    pa_acct_id           VARCHAR(11)       NOT NULL UNIQUE,
    pa_approved_count    INTEGER        NOT NULL DEFAULT 0,
    pa_approved_amount   DECIMAL(12,2)  NOT NULL DEFAULT 0.00,
    pa_declined_count    INTEGER        NOT NULL DEFAULT 0,
    pa_declined_amount   DECIMAL(12,2)  NOT NULL DEFAULT 0.00,
    pa_credit_limit      DECIMAL(12,2)  NOT NULL DEFAULT 0.00,
    pa_credit_available  DECIMAL(12,2)  NOT NULL DEFAULT 0.00,
    PRIMARY KEY (id)
);

CREATE INDEX idx_pa_summary_acct ON pending_auth_summary(pa_acct_id);

-- Pending authorization detail (from IMS HIDAM PAUTDTL1 segment)
CREATE TABLE pending_auth_detail (
    id                   BIGSERIAL      NOT NULL,
    summary_id           BIGINT         NOT NULL,
    pa_card_num          VARCHAR(16),
    pa_tran_id           VARCHAR(20),
    pa_tran_amt          DECIMAL(12,2),
    pa_merchant_id       VARCHAR(20),
    pa_merchant_name     VARCHAR(50),
    pa_auth_date         VARCHAR(10),
    pa_auth_time         VARCHAR(12),
    pa_resp_code         VARCHAR(4),
    pa_fraud_flag        VARCHAR(1)        DEFAULT 'N',
    PRIMARY KEY (id),
    CONSTRAINT fk_pad_summary FOREIGN KEY (summary_id) REFERENCES pending_auth_summary(id)
);

CREATE INDEX idx_pa_detail_summary ON pending_auth_detail(summary_id);

-- Authorization fraud table (from DB2 AUTHFRDS table)
CREATE TABLE auth_fraud (
    id                   BIGSERIAL      NOT NULL,
    af_card_num          VARCHAR(16)       NOT NULL,
    af_tran_id           VARCHAR(20),
    af_tran_amt          DECIMAL(12,2),
    af_merchant_id       VARCHAR(20),
    af_merchant_name     VARCHAR(50),
    af_fraud_date        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    af_fraud_type        VARCHAR(20),
    af_notes             VARCHAR(200),
    PRIMARY KEY (id)
);

CREATE INDEX idx_af_card_num ON auth_fraud(af_card_num);
