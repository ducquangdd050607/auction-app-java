USE auction_app;

INSERT INTO users (id, username, password_hash, password_salt, full_name, email, role, active, created_at, updated_at)
VALUES (
           'admin-uuid-0000-0000-0000-000000000000', -- Fix cứng 1 ID để dễ liên kết khóa ngoại
           'admin',
           '55de331cf42d0ff4df08119acddbf6e36036704cd1c01a468ec0dbaaf03f7336',
           '21feb0f2220c7f8e593d523d64a815e6',
           'Admin Blue88',
           'admin@blue88.com',
           'ADMIN',
           true,
           NOW(),
           NOW()
       );

INSERT INTO Wallet (id, user_id, balance, currency, status, created_at, updated_at)
VALUES (
           UUID(),
           'admin-uuid-0000-0000-0000-000000000000',
           999999999.0000,
           'VND',
           'active',
           NOW(),
           NOW()
       );
