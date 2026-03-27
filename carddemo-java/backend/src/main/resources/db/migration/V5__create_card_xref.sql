-- Card cross-reference table: migrated from CVACT03Y.cpy (CARD-XREF-RECORD)
CREATE TABLE card_xref (
    card_num   VARCHAR(16)  NOT NULL,
    cust_id    BIGINT,
    acct_id    BIGINT,
    CONSTRAINT pk_card_xref PRIMARY KEY (card_num),
    CONSTRAINT fk_card_xref_cust FOREIGN KEY (cust_id) REFERENCES customers(cust_id),
    CONSTRAINT fk_card_xref_acct FOREIGN KEY (acct_id) REFERENCES accounts(acct_id)
);

CREATE INDEX idx_card_xref_acct_id ON card_xref(acct_id);
CREATE INDEX idx_card_xref_cust_id ON card_xref(cust_id);
