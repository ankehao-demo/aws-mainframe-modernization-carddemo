-- Seed data for testing
-- Users (from COBOL USRSEC file)
INSERT INTO users (usr_id, first_name, last_name, password, user_type) VALUES
('USER0001', 'JOHN', 'DOE', 'PASSWORD', 'U'),
('USER0002', 'JANE', 'SMITH', 'PASSWORD', 'U'),
('ADMIN001', 'ADMIN', 'USER', 'PASSWORD', 'A');

-- Transaction types
INSERT INTO tran_types (type_cd, type_desc) VALUES
('01', 'Purchase'),
('02', 'Payment'),
('03', 'Credit'),
('04', 'Authorization'),
('05', 'Refund');

-- Transaction categories
INSERT INTO tran_categories (type_cd, cat_cd, cat_desc) VALUES
('01', 1, 'Regular Sales Draft'),
('01', 2, 'Regular Cash Advance'),
('01', 3, 'Convenience Check Debit'),
('01', 4, 'ATM Cash Advance'),
('01', 5, 'Interest Amount');
