-- Transactions table: migrated from CVTRA05Y.cpy (TRAN-RECORD)
CREATE TABLE transactions (
    tran_id          VARCHAR(16)    NOT NULL,
    type_cd          VARCHAR(2),
    cat_cd           INT,
    source           VARCHAR(10),
    description      VARCHAR(100),
    amount           DECIMAL(11,2),
    merchant_id      BIGINT,
    merchant_name    VARCHAR(50),
    merchant_city    VARCHAR(50),
    merchant_zip     VARCHAR(10),
    card_num         VARCHAR(16),
    orig_ts          TIMESTAMP,
    proc_ts          TIMESTAMP,
    CONSTRAINT pk_transactions PRIMARY KEY (tran_id),
    CONSTRAINT fk_transactions_card FOREIGN KEY (card_num) REFERENCES cards(card_num)
);
