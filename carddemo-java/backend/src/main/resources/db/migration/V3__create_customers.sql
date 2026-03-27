-- Customers table: migrated from CVCUS01Y.cpy (CUSTOMER-RECORD)
CREATE TABLE customers (
    cust_id              BIGINT      NOT NULL,
    first_name           VARCHAR(25),
    middle_name          VARCHAR(25),
    last_name            VARCHAR(25),
    addr_line1           VARCHAR(50),
    addr_line2           VARCHAR(50),
    addr_line3           VARCHAR(50),
    addr_state_cd        VARCHAR(2),
    addr_country_cd      VARCHAR(3),
    addr_zip             VARCHAR(10),
    phone_num1           VARCHAR(15),
    phone_num2           VARCHAR(15),
    ssn                  VARCHAR(9),
    govt_issued_id       VARCHAR(20),
    dob                  DATE,
    eft_account_id       VARCHAR(10),
    pri_card_holder_ind  VARCHAR(1),
    fico_credit_score    SMALLINT,
    CONSTRAINT pk_customers PRIMARY KEY (cust_id)
);
