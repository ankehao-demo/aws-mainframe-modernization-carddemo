-- =============================================================================
-- Flyway Migration V1: Create schema for COTRN02C migration (Phase 1)
--
-- Maps VSAM KSDS files to relational tables:
--   TRANSACT  (CVTRA05Y.cpy TRAN-RECORD)   -> transactions
--   CCXREF    (CVACT03Y.cpy CARD-XREF-RECORD) -> card_xref
--
-- PostgreSQL-compatible; broadly compatible with H2 for testing.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- transactions table
-- Source: TRANSACT VSAM KSDS  |  Copybook: CVTRA05Y.cpy  |  Record len: 350
-- ---------------------------------------------------------------------------
CREATE TABLE transactions (
    tran_id            BIGSERIAL       PRIMARY KEY,
    legacy_tran_id     VARCHAR(16),                          -- original COBOL TRAN-ID PIC X(16) for traceability
    tran_type_cd       VARCHAR(2)      NOT NULL,             -- PIC X(02)
    tran_cat_cd        VARCHAR(4)      NOT NULL,             -- PIC 9(04)
    tran_source        VARCHAR(10)     NOT NULL,             -- PIC X(10)
    tran_desc          VARCHAR(100)    NOT NULL,             -- PIC X(100)
    tran_amt           DECIMAL(11,2)   NOT NULL,             -- PIC S9(09)V99
    tran_merchant_id   VARCHAR(9)      NOT NULL,             -- PIC 9(09)
    tran_merchant_name VARCHAR(50)     NOT NULL,             -- PIC X(50)
    tran_merchant_city VARCHAR(50)     NOT NULL,             -- PIC X(50)
    tran_merchant_zip  VARCHAR(10)     NOT NULL,             -- PIC X(10)
    tran_card_num      VARCHAR(16)     NOT NULL,             -- PIC X(16)
    tran_orig_ts       VARCHAR(26)     NOT NULL,             -- PIC X(26) e.g. '2022-06-10 19:27:53.000000'
    tran_proc_ts       VARCHAR(26)     NOT NULL,             -- PIC X(26)
    created_at         TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_transactions_card_num ON transactions(tran_card_num);
CREATE INDEX idx_transactions_orig_ts  ON transactions(tran_orig_ts);

-- ---------------------------------------------------------------------------
-- card_xref table
-- Source: CCXREF VSAM KSDS + CXACAIX alternate index
-- Copybook: CVACT03Y.cpy  |  Record len: 50
-- ---------------------------------------------------------------------------
CREATE TABLE card_xref (
    card_num   VARCHAR(16)  PRIMARY KEY,                     -- XREF-CARD-NUM PIC X(16)
    cust_id    VARCHAR(9)   NOT NULL,                        -- XREF-CUST-ID  PIC 9(09)
    acct_id    VARCHAR(11)  NOT NULL                         -- XREF-ACCT-ID  PIC 9(11)
);

-- Replaces the CXACAIX alternate index (access by account id)
CREATE INDEX idx_card_xref_acct_id ON card_xref(acct_id);
