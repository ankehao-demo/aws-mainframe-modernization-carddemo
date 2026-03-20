-- V3: Authorization module tables (Phase 4)
-- Replaces IMS HIDAM segments and DB2 AUTHFRDS table

CREATE TABLE pending_auth_summary (
    auth_id         VARCHAR(16) PRIMARY KEY,
    card_num        VARCHAR(16),
    acct_id         VARCHAR(11),
    auth_amount     DECIMAL(12, 2),
    auth_status     VARCHAR(2),
    auth_timestamp  TIMESTAMP,
    merchant_id     VARCHAR(9),
    merchant_name   VARCHAR(50)
);

CREATE TABLE pending_auth_detail (
    detail_id        IDENTITY PRIMARY KEY,
    auth_id          VARCHAR(16),
    detail_type      VARCHAR(10),
    detail_message   VARCHAR(255),
    detail_timestamp TIMESTAMP,
    FOREIGN KEY (auth_id) REFERENCES pending_auth_summary(auth_id)
);

CREATE TABLE fraud_record (
    fraud_id          IDENTITY PRIMARY KEY,
    auth_id           VARCHAR(16),
    card_num          VARCHAR(16),
    acct_id           VARCHAR(11),
    fraud_type        VARCHAR(20),
    fraud_amount      DECIMAL(12, 2),
    fraud_description VARCHAR(255),
    reported_date     TIMESTAMP,
    status            VARCHAR(10)
);

CREATE INDEX idx_auth_summary_card ON pending_auth_summary(card_num);
CREATE INDEX idx_auth_summary_acct ON pending_auth_summary(acct_id);
CREATE INDEX idx_auth_summary_status ON pending_auth_summary(auth_status);
CREATE INDEX idx_fraud_card ON fraud_record(card_num);
CREATE INDEX idx_fraud_acct ON fraud_record(acct_id);
