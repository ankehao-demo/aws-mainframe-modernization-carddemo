INSERT INTO accounts (account_id, active_status, current_balance, credit_limit, cash_credit_limit, open_date, expiration_date, reissue_date, current_cycle_credit, current_cycle_debit, address_zip, group_id) VALUES
(1, 'Y', 19400.00, 202000.00, 102000.00, '2014-11-20', '2025-05-20', '2025-05-20', 0.00, 0.00, 'A000000000', ''),
(2, 'Y', 15800.00, 613000.00, 544800.00, '2013-06-19', '2024-08-11', '2024-08-11', 0.00, 0.00, 'A000000000', ''),
(3, 'Y', 14700.00, 490900.00, 53800.00, '2013-08-23', '2024-01-10', '2024-01-10', 0.00, 0.00, 'A000000000', '');

INSERT INTO customers (customer_id, first_name, middle_name, last_name, address_line_1, address_line_2, address_line_3, address_state_cd, address_country_cd, address_zip, phone_num_1, phone_num_2, ssn, govt_issued_id, date_of_birth, eft_account_id, primary_card_holder_ind, fico_credit_score) VALUES
(1, 'Immanuel', 'Madeline', 'Kessler', '618 Deshaun Route', 'Apt. 802', 'Altenwerthshire', 'NC', 'USA', '12546', '(908)119-8310', '(373)693-8684', 20973888, '0000000000493684', '1961-06-08', '0053581756', 'Y', 274),
(2, 'Enrico', 'April', 'Rosenbaum', '4917 Myrna Flats', 'Apt. 453', 'West Bernita', 'IN', 'USA', '22770', '(429)706-9510', '(744)950-5272', 587518382, '0000000005062103', '1961-10-08', '0069194009', 'Y', 268),
(3, 'Larry', 'Cody', 'Homenick', '362 Esta Parks', 'Apt. 390', 'New Gladys', 'GA', 'USA', '19852-6716', '(950)396-9024', '(685)168-8826', 317460867, '0000000000524193', '1987-11-30', '0006465789', 'Y', 616);

INSERT INTO card_xref (card_number, customer_id, account_id) VALUES
('0500024453765740', 5, 50),
('0683586198171516', 27, 270),
('0923877193247330', 2, 20);
