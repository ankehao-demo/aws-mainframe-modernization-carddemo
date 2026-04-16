INSERT INTO transaction_types (type_code, type_description) VALUES
('01', 'Purchase'), ('02', 'Payment'), ('03', 'Credit');

INSERT INTO transaction_categories (type_code, category_code, category_description) VALUES
('01', 1, 'Regular Sales Draft'), ('01', 2, 'Regular Cash Advance'), ('01', 3, 'Convenience Check Debit');

INSERT INTO disclosure_groups (account_group_id, type_code, category_code, interest_rate) VALUES
('A000000000', '01', 1, 15.00), ('A000000000', '01', 2, 25.00), ('A000000000', '01', 3, 25.00);

INSERT INTO category_balances (account_id, type_code, category_code, balance) VALUES
(1, '01', 1, 0.00), (2, '01', 1, 0.00), (3, '01', 1, 0.00);
