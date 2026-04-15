CREATE TABLE cards (
    card_number     VARCHAR(16) PRIMARY KEY,
    account_id      BIGINT,
    cvv_code        INTEGER,
    embossed_name   VARCHAR(50),
    expiration_date VARCHAR(10),
    active_status   CHAR(1)
);
CREATE INDEX idx_cards_account ON cards(account_id);
