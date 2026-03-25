-- Cards table: migrated from CVACT02Y.cpy (CARD-RECORD)
CREATE TABLE cards (
    card_num         VARCHAR(16)  NOT NULL,
    cust_id          BIGINT,
    acct_id          BIGINT,
    active_status    VARCHAR(1),
    expiration_date  DATE,
    CONSTRAINT pk_cards PRIMARY KEY (card_num),
    CONSTRAINT fk_cards_cust FOREIGN KEY (cust_id) REFERENCES customers(cust_id),
    CONSTRAINT fk_cards_acct FOREIGN KEY (acct_id) REFERENCES accounts(acct_id)
);
