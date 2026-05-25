CREATE TABLE IF NOT EXISTS users ( # người dùng
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE, # varchar(100) là kiểu dữ liệu quy định tối đa 100 ký tư
    password_hash VARCHAR(255) NOT NULL,
    password_salt VARCHAR(255) NOT NULL,
    full_name VARCHAR(200) NOT NULL,
    email VARCHAR(200) NOT NULL,
    role VARCHAR(30) NOT NULL, # vai trò bidder/seller/admin
    active BOOLEAN NOT NULL, # active tài khoản bị khóa(FALSE) hoặc đang hoạt động TRUE
    created_at DATETIME NOT NULL, # ngày tạo tài khoản
    updated_at DATETIME NOT NULL # các ngày sửa đổi
);

CREATE TABLE IF NOT EXISTS auction_items (  # chứa các sản phẩm đấu giá
    id VARCHAR(36) PRIMARY KEY,
    seller_id VARCHAR(36) NOT NULL,  # id người bán
    title VARCHAR(255) NOT NULL,     # tên sản phẩm
    description TEXT NOT NULL,       # mô tả sản phẩm
    starting_price DECIMAL(19, 2) NOT NULL, # giá khởi điểm
    item_type VARCHAR(30) NOT NULL,    # loại đồ: art, vehicle, electronic
    attribute_one VARCHAR(255),     # mô tả về đặc điểm thứ nhất
    attribute_two VARCHAR(255),
    image_data LONGBLOB NULL,
    created_at DATETIME NOT NULL,   # thời gian đăng sản phẩm này lên
    updated_at DATETIME NOT NULL,    # thay đổi về giá,...
    CONSTRAINT fk_items_seller FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS auctions ( /* lưu thông tin các phiên đấu giá*/
    id VARCHAR(36) PRIMARY KEY,  # STT của phiên
    item_id VARCHAR(36) NOT NULL,
    seller_id VARCHAR(36) NOT NULL,
    current_price DECIMAL(19, 2) NOT NULL, # giá hiện tại cập nhật liên tục
    leading_bidder_id VARCHAR(36) NULL, # người đang đặt giá cao nhất
    start_time DATETIME NOT NULL,    # phiên bắt đầu thời gian nào
    end_time DATETIME NOT NULL,
    status VARCHAR(30) NOT NULL,    # phiên đang chuẩn bị diễn ra, đang diễn ra, hay đã đóng
    minimum_increment DECIMAL(19, 2) NOT NULL,   # khoảng tăng tối thiểu
    winner_id VARCHAR(36) NULL,
    created_at DATETIME NOT NULL,  # thời gian tạo phiên
    updated_at DATETIME NOT NULL,    # các thay đổi của phiên
    CONSTRAINT fk_auctions_item FOREIGN KEY (item_id) REFERENCES auction_items(id) ON DELETE CASCADE,
    CONSTRAINT fk_auctions_seller FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS bids ( # trong các phiên sẽ có các lượt đặt giá -> bids lưu các lượt đặt giá
    id VARCHAR(36) PRIMARY KEY,  # STT người đặt giá
    auction_id VARCHAR(36) NOT NULL,  # id của phiên đấu giá
    bidder_id VARCHAR(36) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,    # số tiền được đặt
    auto_generated BOOLEAN NOT NULL,   # kiểm tra xem là đặt giá thủ công hay LÀ auto_bid
    note VARCHAR(255),  # ghi chú của người đặt giá
    created_at DATETIME NOT NULL, # thời gian đặt giá
    updated_at DATETIME NOT NULL, # sửa note
    CONSTRAINT fk_bids_auction FOREIGN KEY (auction_id) REFERENCES auctions(id) ON DELETE CASCADE,
    CONSTRAINT fk_bids_user FOREIGN KEY (bidder_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS auto_bid_configs ( # chức năng tự động đặt giá, lưu các thông tin liên quan tới auto_bid
    id VARCHAR(36) PRIMARY KEY,
    auction_id VARCHAR(36) NOT NULL, # id của
    bidder_id VARCHAR(36) NOT NULL,
    max_bid DECIMAL(19, 2) NOT NULL, # tự động đặt giá tới giá cao nhất bidder có thể đồng ý trả max bid
    increment_amount DECIMAL(19, 2) NOT NULL,
    enabled BOOLEAN NOT NULL, # mình có đồng ý sử dụng tính năng này ko ( bật hoặc tắt)
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL, # ghi lại lịch sử thay đổi của max_bid hoặc increment_amount
    CONSTRAINT uq_auto_bid UNIQUE (auction_id, bidder_id),
    CONSTRAINT fk_auto_bid_auction FOREIGN KEY (auction_id) REFERENCES auctions(id) ON DELETE CASCADE,
    CONSTRAINT fk_auto_bid_user FOREIGN KEY (bidder_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Wallet (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    balance DECIMAL(19, 4) NOT NULL DEFAULT 0.0000, # 'Số dư hiện tại của ví',
    currency VARCHAR(3) NOT NULL DEFAULT 'VND', # 'Mã tiền tệ (VD: VND, USD)',
    status ENUM('active', 'locked', 'closed') NOT NULL DEFAULT 'active', # 'Trạng thái của ví'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    constraint fk_user_id foreign key(user_id) references users(id) ON DELETE RESTRICT,
	UNIQUE KEY unique_user_currency (user_id, currency) # buộc mỗi ví của mỗi người dùng chỉ có 1 đơn vị tiền tệ
);