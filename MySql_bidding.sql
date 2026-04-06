DROP DATABASE IF EXISTS auction_app;
CREATE DATABASE auction_app CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE auction_app;

-- =========================
-- 1. USERS
-- =========================
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    role ENUM('BIDDER', 'SELLER', 'ADMIN') NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'BANNED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- =========================
-- 2. CATEGORIES
-- =========================
CREATE TABLE categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

-- =========================
-- 3. PRODUCTS / AUCTIONS
-- 1 product = 1 auction item
-- =========================
CREATE TABLE products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    seller_id INT NOT NULL,
    category_id INT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    starting_price DECIMAL(12,2) NOT NULL,
    current_price DECIMAL(12,2) NOT NULL,
    min_bid_increment DECIMAL(12,2) NOT NULL DEFAULT 1.00,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    winner_id INT NULL,
    status ENUM('OPEN', 'RUNNING', 'FINISHED', 'PAID', 'CANCELED') DEFAULT 'OPEN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_products_seller
        FOREIGN KEY (seller_id) REFERENCES users(id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id) REFERENCES categories(id)
        ON DELETE SET NULL ON UPDATE CASCADE,

    CONSTRAINT fk_products_winner
        FOREIGN KEY (winner_id) REFERENCES users(id)
        ON DELETE SET NULL ON UPDATE CASCADE,

    CONSTRAINT chk_products_price
        CHECK (starting_price > 0),

    CONSTRAINT chk_products_current_price
        CHECK (current_price >= 0),

    CONSTRAINT chk_products_increment
        CHECK (min_bid_increment > 0),

    CONSTRAINT chk_products_time
        CHECK (end_time > start_time)
);

-- =========================
-- 4. BIDS
-- =========================
CREATE TABLE bids (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    bidder_id INT NOT NULL,
    bid_amount DECIMAL(12,2) NOT NULL,
    bid_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_auto_bid BOOLEAN DEFAULT FALSE,

    CONSTRAINT fk_bids_product
        FOREIGN KEY (product_id) REFERENCES products(id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT fk_bids_bidder
        FOREIGN KEY (bidder_id) REFERENCES users(id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT chk_bids_amount
        CHECK (bid_amount > 0)
);

-- =========================
-- 5. AUTO BIDS
-- =========================
CREATE TABLE auto_bids (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    bidder_id INT NOT NULL,
    max_bid DECIMAL(12,2) NOT NULL,
    increment_step DECIMAL(12,2) NOT NULL DEFAULT 1.00,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_auto_bids_product
        FOREIGN KEY (product_id) REFERENCES products(id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT fk_auto_bids_bidder
        FOREIGN KEY (bidder_id) REFERENCES users(id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT uq_auto_bid_product_bidder
        UNIQUE (product_id, bidder_id),

    CONSTRAINT chk_auto_bids_max
        CHECK (max_bid > 0),

    CONSTRAINT chk_auto_bids_increment
        CHECK (increment_step > 0)
);

-- =========================
-- 6. PAYMENTS
-- =========================
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    buyer_id INT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    payment_method ENUM('CASH', 'BANK_TRANSFER', 'CARD', 'E_WALLET') NOT NULL,
    payment_status ENUM('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED') DEFAULT 'PENDING',
    paid_at DATETIME NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_payments_product
        FOREIGN KEY (product_id) REFERENCES products(id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT fk_payments_buyer
        FOREIGN KEY (buyer_id) REFERENCES users(id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT uq_payments_product
        UNIQUE (product_id),

    CONSTRAINT chk_payments_amount
        CHECK (amount > 0)
);

-- =========================
-- 7. NOTIFICATIONS
-- =========================
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    product_id INT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT fk_notifications_product
        FOREIGN KEY (product_id) REFERENCES products(id)
        ON DELETE SET NULL ON UPDATE CASCADE
);

-- =========================
-- 8. AUCTION_STATUS_HISTORY
-- =========================
CREATE TABLE auction_status_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    old_status ENUM('OPEN', 'RUNNING', 'FINISHED', 'PAID', 'CANCELED') NULL,
    new_status ENUM('OPEN', 'RUNNING', 'FINISHED', 'PAID', 'CANCELED') NOT NULL,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    note VARCHAR(255),

    CONSTRAINT fk_status_history_product
        FOREIGN KEY (product_id) REFERENCES products(id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- =========================
-- INDEXES
-- =========================
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_products_seller ON products(seller_id);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_products_time ON products(start_time, end_time);
CREATE INDEX idx_bids_product ON bids(product_id);
CREATE INDEX idx_bids_bidder ON bids(bidder_id);
CREATE INDEX idx_bids_product_time ON bids(product_id, bid_time);
CREATE INDEX idx_auto_bids_product ON auto_bids(product_id);
CREATE INDEX idx_auto_bids_bidder ON auto_bids(bidder_id);
CREATE INDEX idx_notifications_user_read ON notifications(user_id, is_read);

-- =========================
-- TRIGGERS
-- =========================
DELIMITER $$

-- Khi tạo product thì current_price = starting_price nếu chưa đúng
CREATE TRIGGER trg_before_insert_products
BEFORE INSERT ON products
FOR EACH ROW
BEGIN
    IF NEW.current_price IS NULL OR NEW.current_price < NEW.starting_price THEN
        SET NEW.current_price = NEW.starting_price;
    END IF;
END$$

-- Kiểm tra logic bid trước khi insert
CREATE TRIGGER trg_before_insert_bids
BEFORE INSERT ON bids
FOR EACH ROW
BEGIN
    DECLARE v_status VARCHAR(20);
    DECLARE v_current_price DECIMAL(12,2);
    DECLARE v_increment DECIMAL(12,2);
    DECLARE v_end_time DATETIME;
    DECLARE v_seller_id INT;

    SELECT status, current_price, min_bid_increment, end_time, seller_id
    INTO v_status, v_current_price, v_increment, v_end_time, v_seller_id
    FROM products
    WHERE id = NEW.product_id;

    IF v_status NOT IN ('OPEN', 'RUNNING') THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Auction is not open for bidding';
    END IF;

    IF NOW() > v_end_time THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Auction has already ended';
    END IF;

    IF NEW.bidder_id = v_seller_id THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Seller cannot bid on own product';
    END IF;

    IF NEW.bid_amount < (v_current_price + v_increment) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Bid amount must be greater than current price plus minimum increment';
    END IF;
END$$

-- Sau khi insert bid hợp lệ thì cập nhật current_price, winner, status
CREATE TRIGGER trg_after_insert_bids
AFTER INSERT ON bids
FOR EACH ROW
BEGIN
    UPDATE products
    SET current_price = NEW.bid_amount,
        winner_id = NEW.bidder_id,
        status = 'RUNNING'
    WHERE id = NEW.product_id;
END$$

-- Ghi lịch sử thay đổi trạng thái auction
CREATE TRIGGER trg_after_update_product_status
AFTER UPDATE ON products
FOR EACH ROW
BEGIN
    IF OLD.status <> NEW.status THEN
        INSERT INTO auction_status_history(product_id, old_status, new_status, note)
        VALUES (NEW.id, OLD.status, NEW.status, 'Status changed');
    END IF;
END$$

DELIMITER ;

-- =========================
-- VIEWS
-- =========================

-- Danh sách product kèm seller, winner
CREATE VIEW v_product_detail AS
SELECT
    p.id,
    p.name,
    p.description,
    p.starting_price,
    p.current_price,
    p.min_bid_increment,
    p.start_time,
    p.end_time,
    p.status,
    p.created_at,
    s.id AS seller_id,
    s.username AS seller_username,
    s.full_name AS seller_name,
    c.name AS category_name,
    w.id AS winner_id,
    w.username AS winner_username
FROM products p
JOIN users s ON p.seller_id = s.id
LEFT JOIN categories c ON p.category_id = c.id
LEFT JOIN users w ON p.winner_id = w.id;

-- Lịch sử bid
CREATE VIEW v_bid_history AS
SELECT
    b.id,
    b.product_id,
    p.name AS product_name,
    b.bidder_id,
    u.username AS bidder_username,
    u.full_name AS bidder_name,
    b.bid_amount,
    b.bid_time,
    b.is_auto_bid
FROM bids b
JOIN products p ON b.product_id = p.id
JOIN users u ON b.bidder_id = u.id;

-- =========================
-- STORED PROCEDURES
-- =========================
DELIMITER $$

-- Procedure đặt bid an toàn hơn cho backend gọi
CREATE PROCEDURE place_bid (
    IN p_product_id INT,
    IN p_bidder_id INT,
    IN p_bid_amount DECIMAL(12,2),
    IN p_is_auto_bid BOOLEAN
)
BEGIN
    DECLARE v_status VARCHAR(20);
    DECLARE v_current_price DECIMAL(12,2);
    DECLARE v_increment DECIMAL(12,2);
    DECLARE v_end_time DATETIME;
    DECLARE v_seller_id INT;

    START TRANSACTION;

    SELECT status, current_price, min_bid_increment, end_time, seller_id
    INTO v_status, v_current_price, v_increment, v_end_time, v_seller_id
    FROM products
    WHERE id = p_product_id
    FOR UPDATE;

    IF v_status NOT IN ('OPEN', 'RUNNING') THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Auction is not open';
    END IF;

    IF NOW() > v_end_time THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Auction ended';
    END IF;

    IF p_bidder_id = v_seller_id THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Seller cannot bid on own product';
    END IF;

    IF p_bid_amount < (v_current_price + v_increment) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Invalid bid amount';
    END IF;

    INSERT INTO bids(product_id, bidder_id, bid_amount, is_auto_bid)
    VALUES (p_product_id, p_bidder_id, p_bid_amount, p_is_auto_bid);

    UPDATE products
    SET current_price = p_bid_amount,
        winner_id = p_bidder_id,
        status = 'RUNNING'
    WHERE id = p_product_id;

    COMMIT;
END$$

-- Kết thúc auction khi hết giờ
CREATE PROCEDURE close_expired_auctions()
BEGIN
    UPDATE products
    SET status = 'FINISHED'
    WHERE end_time <= NOW()
      AND status IN ('OPEN', 'RUNNING');
END$$

-- Thanh toán auction thắng
CREATE PROCEDURE mark_auction_paid (
    IN p_product_id INT,
    IN p_payment_method ENUM('CASH', 'BANK_TRANSFER', 'CARD', 'E_WALLET')
)
BEGIN
    DECLARE v_winner_id INT;
    DECLARE v_amount DECIMAL(12,2);
    DECLARE v_status VARCHAR(20);

    START TRANSACTION;

    SELECT winner_id, current_price, status
    INTO v_winner_id, v_amount, v_status
    FROM products
    WHERE id = p_product_id
    FOR UPDATE;

    IF v_status <> 'FINISHED' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Auction must be FINISHED before payment';
    END IF;

    IF v_winner_id IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'No winner found for this auction';
    END IF;

    INSERT INTO payments(product_id, buyer_id, amount, payment_method, payment_status, paid_at)
    VALUES (p_product_id, v_winner_id, v_amount, p_payment_method, 'SUCCESS', NOW());

    UPDATE products
    SET status = 'PAID'
    WHERE id = p_product_id;

    COMMIT;
END$$

DELIMITER ;

-- =========================
-- SAMPLE DATA
-- =========================
INSERT INTO categories(name, description) VALUES
('Electronics', 'Electronic products'),
('Art', 'Art products'),
('Vehicle', 'Vehicles'),
('Fashion', 'Fashion items');

INSERT INTO users(username, password, email, full_name, phone, role) VALUES
('admin1', '123456', 'admin1@gmail.com', 'System Admin', '0900000001', 'ADMIN'),
('seller1', '123456', 'seller1@gmail.com', 'Nguyen Van Seller', '0900000002', 'SELLER'),
('seller2', '123456', 'seller2@gmail.com', 'Tran Thi Seller', '0900000003', 'SELLER'),
('bidder1', '123456', 'bidder1@gmail.com', 'Le Van Bidder', '0900000004', 'BIDDER'),
('bidder2', '123456', 'bidder2@gmail.com', 'Pham Thi Bidder', '0900000005', 'BIDDER'),
('bidder3', '123456', 'bidder3@gmail.com', 'Hoang Van Bidder', '0900000006', 'BIDDER');

INSERT INTO products(
    seller_id, category_id, name, description,
    starting_price, current_price, min_bid_increment,
    start_time, end_time, status
) VALUES
(2, 1, 'iPhone 15 Pro Max', '256GB, like new',
 20000000, 20000000, 500000,
 NOW(), DATE_ADD(NOW(), INTERVAL 3 DAY), 'OPEN'),

(3, 2, 'Tranh son dau', 'Tranh phong canh ve tay',
 5000000, 5000000, 100000,
 NOW(), DATE_ADD(NOW(), INTERVAL 2 DAY), 'OPEN'),

(2, 4, 'Dong ho co', 'Dong ho co cao cap',
 3000000, 3000000, 50000,
 NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY), 'OPEN');

INSERT INTO bids(product_id, bidder_id, bid_amount, is_auto_bid) VALUES
(1, 4, 20500000, FALSE),
(1, 5, 21000000, FALSE),
(2, 6, 5100000, FALSE);

INSERT INTO auto_bids(product_id, bidder_id, max_bid, increment_step, is_active) VALUES
(1, 4, 25000000, 500000, TRUE),
(2, 5, 7000000, 100000, TRUE);

INSERT INTO notifications(user_id, product_id, title, message) VALUES
(4, 1, 'Bid placed successfully', 'You are currently leading the auction for iPhone 15 Pro Max'),
(5, 1, 'Outbid notification', 'Your bid has been surpassed by another bidder'),
(2, 1, 'New highest bid', 'Your product iPhone 15 Pro Max has received a new highest bid');

-- =========================
-- TEST QUERIES
-- =========================

-- Xem toàn bộ users
SELECT * FROM users;

-- Xem danh sách auction item
SELECT * FROM v_product_detail;

-- Xem lịch sử bid
SELECT * FROM v_bid_history ORDER BY bid_time DESC;

-- Đặt bid bằng procedure
-- CALL place_bid(1, 6, 21500000, FALSE);

-- Đóng auction đã hết hạn
-- CALL close_expired_auctions();

-- Đánh dấu đã thanh toán
-- CALL mark_auction_paid(1, 'BANK_TRANSFER');