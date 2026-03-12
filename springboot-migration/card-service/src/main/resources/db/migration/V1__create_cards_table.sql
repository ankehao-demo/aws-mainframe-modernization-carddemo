-- Flyway migration: Create cards table
-- Mapped from COBOL copybooks CVACT02Y.cpy (CARD-RECORD) and CVACT03Y.cpy (CARD-XREF-RECORD)
-- Replaces CARDDATA and CARDXREF VSAM KSDS files

CREATE TABLE IF NOT EXISTS cards (
    card_number     VARCHAR(16)  NOT NULL PRIMARY KEY,
    account_id      BIGINT       NOT NULL,
    customer_id     BIGINT       NOT NULL,
    cvv_code        VARCHAR(3),
    embossed_name   VARCHAR(50),
    expiration_date DATE,
    card_status     VARCHAR(1)   NOT NULL DEFAULT 'Y'
);

CREATE INDEX IF NOT EXISTS idx_cards_account_id ON cards(account_id);
CREATE INDEX IF NOT EXISTS idx_cards_customer_id ON cards(customer_id);

COMMENT ON TABLE cards IS 'Card data — migrated from CARDDATA and CARDXREF VSAM files';
COMMENT ON COLUMN cards.customer_id IS 'FK from CARDXREF (XREF-CUST-ID)';
