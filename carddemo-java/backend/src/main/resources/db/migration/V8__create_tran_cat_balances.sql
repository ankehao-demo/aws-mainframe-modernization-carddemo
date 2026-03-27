-- Transaction category balances: migrated from CVTRA01Y.cpy (TRAN-CAT-BAL-RECORD)
CREATE TABLE tran_cat_balances (
    acct_id    BIGINT       NOT NULL,
    type_cd    VARCHAR(2)   NOT NULL,
    cat_cd     INT          NOT NULL,
    balance    DECIMAL(12,2),
    CONSTRAINT pk_tran_cat_balances PRIMARY KEY (acct_id, type_cd, cat_cd)
);
