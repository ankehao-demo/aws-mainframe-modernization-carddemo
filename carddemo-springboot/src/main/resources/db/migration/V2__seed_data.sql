-- V2: Seed data parsed from ASCII fixed-width files in app/data/ASCII/
-- Field positions derived from COBOL copybook definitions

-- Transaction Types (from trantype.txt - CVTRA03Y: type_cd X(02), type_desc X(50))
INSERT INTO transaction_type (type_cd, type_desc) VALUES ('01', 'Purchase');
INSERT INTO transaction_type (type_cd, type_desc) VALUES ('02', 'Payment');
INSERT INTO transaction_type (type_cd, type_desc) VALUES ('03', 'Credit');
INSERT INTO transaction_type (type_cd, type_desc) VALUES ('04', 'Balance Transfer');
INSERT INTO transaction_type (type_cd, type_desc) VALUES ('05', 'Interest');

-- Transaction Categories (from trancatg.txt - CVTRA04Y: type_cd X(02), cat_cd 9(04), desc X(50))
INSERT INTO transaction_category (type_cd, cat_cd, cat_type_desc) VALUES ('01', 1, 'Regular Sales Draft');
INSERT INTO transaction_category (type_cd, cat_cd, cat_type_desc) VALUES ('01', 2, 'Regular Cash Advance');
INSERT INTO transaction_category (type_cd, cat_cd, cat_type_desc) VALUES ('01', 3, 'Convenience Check Debit');
INSERT INTO transaction_category (type_cd, cat_cd, cat_type_desc) VALUES ('01', 4, 'Balance Transfer Debit');
INSERT INTO transaction_category (type_cd, cat_cd, cat_type_desc) VALUES ('01', 5, 'Interest Charge');
INSERT INTO transaction_category (type_cd, cat_cd, cat_type_desc) VALUES ('02', 1, 'Regular Payment');
INSERT INTO transaction_category (type_cd, cat_cd, cat_type_desc) VALUES ('02', 2, 'Balance Transfer Credit');
INSERT INTO transaction_category (type_cd, cat_cd, cat_type_desc) VALUES ('03', 1, 'Merchandise Return Credit');
INSERT INTO transaction_category (type_cd, cat_cd, cat_type_desc) VALUES ('03', 2, 'Cash Advance Fee Credit');

-- User Security (default users)
INSERT INTO user_security (user_id, first_name, last_name, password, user_type) VALUES ('USER0001', 'FIRST01', 'LAST01', 'PASSWORD', 'U');
INSERT INTO user_security (user_id, first_name, last_name, password, user_type) VALUES ('USER0002', 'FIRST02', 'LAST02', 'PASSWORD', 'U');
INSERT INTO user_security (user_id, first_name, last_name, password, user_type) VALUES ('USER0003', 'FIRST03', 'LAST03', 'PASSWORD', 'U');
INSERT INTO user_security (user_id, first_name, last_name, password, user_type) VALUES ('ADMIN001', 'ADMIN', 'USER', 'PASSWORD', 'A');

-- Account data (from acctdata.txt - CVACT01Y)
-- Format: acct_id 9(11), status X(1), curr_bal S9(10)V99, credit_limit S9(10)V99,
--         cash_credit_limit S9(10)V99, open_date X(10), exp_date X(10), reissue_date X(10),
--         curr_cyc_credit S9(10)V99, curr_cyc_debit S9(10)V99, addr_zip X(10), group_id X(10)
INSERT INTO account (acct_id, active_status, curr_bal, credit_limit, cash_credit_limit, open_date, expiration_date, reissue_date, curr_cyc_credit, curr_cyc_debit, addr_zip, group_id) VALUES ('00000000001', 'Y', 19400.00, 202000.00, 102000.00, '2014-11-20', '2025-05-20', '2025-05-20', 0.00, 0.00, 'A000000000', '');
INSERT INTO account (acct_id, active_status, curr_bal, credit_limit, cash_credit_limit, open_date, expiration_date, reissue_date, curr_cyc_credit, curr_cyc_debit, addr_zip, group_id) VALUES ('00000000002', 'Y', 15800.00, 613000.00, 544800.00, '2013-06-19', '2024-08-11', '2024-08-11', 0.00, 0.00, 'A000000000', '');
INSERT INTO account (acct_id, active_status, curr_bal, credit_limit, cash_credit_limit, open_date, expiration_date, reissue_date, curr_cyc_credit, curr_cyc_debit, addr_zip, group_id) VALUES ('00000000003', 'Y', 14700.00, 490900.00, 53800.00, '2013-08-23', '2024-01-10', '2024-01-10', 0.00, 0.00, 'A000000000', '');
INSERT INTO account (acct_id, active_status, curr_bal, credit_limit, cash_credit_limit, open_date, expiration_date, reissue_date, curr_cyc_credit, curr_cyc_debit, addr_zip, group_id) VALUES ('00000000004', 'Y', 18000.00, 179300.00, 102400.00, '2012-01-17', '2023-12-12', '2023-12-12', 0.00, 0.00, 'A000000000', '');
INSERT INTO account (acct_id, active_status, curr_bal, credit_limit, cash_credit_limit, open_date, expiration_date, reissue_date, curr_cyc_credit, curr_cyc_debit, addr_zip, group_id) VALUES ('00000000005', 'Y', 1000.00, 123500.00, 102400.00, '2011-04-09', '2024-03-28', '2024-03-28', 0.00, 0.00, 'A000000000', '');

-- Customer data (from custdata.txt - CVCUS01Y)
-- Format: cust_id 9(09), first_name X(25), middle_name X(25), last_name X(25),
--         addr1 X(50), addr2 X(50), addr3 X(50), state X(02), country X(03), zip X(10),
--         phone1 X(15), phone2 X(15), ssn 9(09), govt_id X(20), dob X(10),
--         eft_acct X(10), pri_card X(01), fico 9(03)
INSERT INTO customer (cust_id, first_name, middle_name, last_name, addr_line_1, addr_line_2, addr_line_3, addr_state_cd, addr_country_cd, addr_zip, phone_num_1, phone_num_2, ssn, govt_issued_id, dob, eft_account_id, pri_card_holder_ind, fico_credit_score) VALUES ('000000001', 'Immanuel', 'Madeline', 'Kessler', '618 Deshaun Route', 'Apt. 802', 'Altenwerthshire', 'NC', 'USA', '12546', '(908)119-8310', '(373)693-8684', '020973888', '00000000004936843', '1961-06-08', '0053581756', 'Y', 274);
INSERT INTO customer (cust_id, first_name, middle_name, last_name, addr_line_1, addr_line_2, addr_line_3, addr_state_cd, addr_country_cd, addr_zip, phone_num_1, phone_num_2, ssn, govt_issued_id, dob, eft_account_id, pri_card_holder_ind, fico_credit_score) VALUES ('000000002', 'Enrico', 'April', 'Rosenbaum', '4917 Myrna Flats', 'Apt. 453', 'West Bernita', 'IN', 'USA', '22770', '(429)706-9510', '(744)950-5272', '587518382', '00000000050621037', '1961-10-08', '0069194009', 'Y', 268);
INSERT INTO customer (cust_id, first_name, middle_name, last_name, addr_line_1, addr_line_2, addr_line_3, addr_state_cd, addr_country_cd, addr_zip, phone_num_1, phone_num_2, ssn, govt_issued_id, dob, eft_account_id, pri_card_holder_ind, fico_credit_score) VALUES ('000000003', 'Larry', 'Cody', 'Homenick', '362 Esta Parks', 'Apt. 390', 'New Gladys', 'GA', 'USA', '19852-6716', '(950)396-9024', '(685)168-8826', '317460867', '00000000005241930', '1987-11-30', '0006465789', 'Y', 616);
INSERT INTO customer (cust_id, first_name, middle_name, last_name, addr_line_1, addr_line_2, addr_line_3, addr_state_cd, addr_country_cd, addr_zip, phone_num_1, phone_num_2, ssn, govt_issued_id, dob, eft_account_id, pri_card_holder_ind, fico_credit_score) VALUES ('000000004', 'Bernita', 'Anahi', 'Bashirian', '9828 Willms Burg', 'Suite 747', 'Handfurt', 'ME', 'USA', '28929', '(474)803-8808', '(630)049-8498', '472670287', '00000000041379825', '1983-04-28', '0057072338', 'Y', 479);
INSERT INTO customer (cust_id, first_name, middle_name, last_name, addr_line_1, addr_line_2, addr_line_3, addr_state_cd, addr_country_cd, addr_zip, phone_num_1, phone_num_2, ssn, govt_issued_id, dob, eft_account_id, pri_card_holder_ind, fico_credit_score) VALUES ('000000005', 'Aniya', 'Alena', 'Von', '270 Conn Bridge', 'Suite 115', 'Lake Shawnabury', 'CO', 'USA', '86362', '(312)706-6992', '(929)213-8498', '416097471', '00000000027131797', '1974-09-08', '0024860098', 'Y', 517);

-- Card data (from carddata.txt - CVACT02Y)
-- Format: card_num X(16), acct_id 9(11), cvv 9(03), embossed_name X(50), exp_date X(10), status X(01)
INSERT INTO card (card_num, acct_id, cvv_cd, embossed_name, expiration_date, active_status) VALUES ('0500024453765740', '00000000005', '747', 'Aniya Von', '2023-03-09', 'Y');
INSERT INTO card (card_num, acct_id, cvv_cd, embossed_name, expiration_date, active_status) VALUES ('0683586198171516', '00000000002', '567', 'Ward Jones', '2025-07-13', 'Y');
INSERT INTO card (card_num, acct_id, cvv_cd, embossed_name, expiration_date, active_status) VALUES ('0923877193247330', '00000000002', '028', 'Enrico Rosenbaum', '2024-08-11', 'Y');

-- Card cross-reference data (from cardxref.txt - CVACT03Y)
-- Format: card_num X(16), cust_id 9(09), acct_id 9(11)
INSERT INTO card_cross_ref (card_num, cust_id, acct_id) VALUES ('0500024453765740', '000000005', '00000000005');
INSERT INTO card_cross_ref (card_num, cust_id, acct_id) VALUES ('0683586198171516', '000000002', '00000000002');
INSERT INTO card_cross_ref (card_num, cust_id, acct_id) VALUES ('0923877193247330', '000000002', '00000000002');

-- Disclosure group data (from discgrp.txt - CVTRA02Y)
-- Format: group_id X(10), type_cd X(02), cat_cd 9(04), int_rate S9(04)V99
INSERT INTO disclosure_group (group_id, type_cd, cat_cd, int_rate) VALUES ('A000000000', '01', 1, 15.00);
INSERT INTO disclosure_group (group_id, type_cd, cat_cd, int_rate) VALUES ('A000000000', '01', 2, 25.00);
INSERT INTO disclosure_group (group_id, type_cd, cat_cd, int_rate) VALUES ('A000000000', '01', 3, 25.00);

-- Transaction category balance data (from tcatbal.txt - CVTRA01Y)
-- Format: acct_id 9(11), type_cd X(02), cat_cd 9(04), balance S9(09)V99
INSERT INTO tran_category_balance (acct_id, type_cd, cat_cd, balance) VALUES ('00000000001', '01', 1, 0.00);
INSERT INTO tran_category_balance (acct_id, type_cd, cat_cd, balance) VALUES ('00000000002', '01', 1, 0.00);
INSERT INTO tran_category_balance (acct_id, type_cd, cat_cd, balance) VALUES ('00000000003', '01', 1, 0.00);
