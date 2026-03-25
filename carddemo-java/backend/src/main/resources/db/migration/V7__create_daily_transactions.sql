-- Daily transactions table: migrated from CVTRA06Y.cpy (DALYTRAN-RECORD)
CREATE TABLE daily_transactions (
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
    CONSTRAINT pk_daily_transactions PRIMARY KEY (tran_id)
);
