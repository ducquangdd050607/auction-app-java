INSERT IGNORE INTO users(id,created_at,updated_at,username,password_hash,password_salt,full_name,email,role,active) VALUES
('00000000-0000-0000-0000-000000000001',NOW(),NOW(),'admin','5e6cce97da51f50e0c633f07fdf4c7e77a14a9379f17f42afdcc21a0aed27795','salt-admin','Admin Demo','admin@auction.local','ADMIN',TRUE),
('00000000-0000-0000-0000-000000000002',NOW(),NOW(),'seller','f67945d251d321808d75bca7f426ee813f546f6a16df382d1e2d6903e2a8b762','salt-seller','Seller Demo','seller@auction.local','SELLER',TRUE),
('00000000-0000-0000-0000-000000000003',NOW(),NOW(),'bidder','8f9e7ceda8ea9e4ff4079f4eeec2630a920b89d3ed38fa04c134a15b3b9f196a','salt-bidder','Bidder Demo','bidder@auction.local','BIDDER',TRUE);
INSERT IGNORE INTO wallets(id,created_at,updated_at,user_id,balance) VALUES
('10000000-0000-0000-0000-000000000001',NOW(),NOW(),'00000000-0000-0000-0000-000000000001',100000000),
('10000000-0000-0000-0000-000000000002',NOW(),NOW(),'00000000-0000-0000-0000-000000000002',100000000),
('10000000-0000-0000-0000-000000000003',NOW(),NOW(),'00000000-0000-0000-0000-000000000003',100000000);
