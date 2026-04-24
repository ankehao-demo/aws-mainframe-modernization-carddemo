-- Flyway migration: Create card tables replacing VSAM KSDS files
-- CARDDAT (primary card data) and CCXREF (card cross-reference)

CREATE TABLE cards (
    card_num VARCHAR(16) PRIMARY KEY,
    card_acct_id VARCHAR(11) NOT NULL,
    card_cvv_cd VARCHAR(3),
    card_embossed_name VARCHAR(50),
    card_expiration_date VARCHAR(10),
    card_active_status CHAR(1),
    version BIGINT DEFAULT 0
);

-- Replaces VSAM alternate index CARDAIX on account ID
CREATE INDEX idx_cards_acct_id ON cards(card_acct_id);

CREATE TABLE card_xref (
    xref_card_num VARCHAR(16) PRIMARY KEY,
    xref_cust_id VARCHAR(9),
    xref_acct_id VARCHAR(11)
);

-- Index for account-based lookups on cross-reference
CREATE INDEX idx_card_xref_acct ON card_xref(xref_acct_id);

-- Index for customer-based lookups on cross-reference
CREATE INDEX idx_card_xref_cust ON card_xref(xref_cust_id);
