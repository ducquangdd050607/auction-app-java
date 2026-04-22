package com.auctionapp.auctionappjava.server.db;
import com.auctionapp.auctionappjava.server.config.ServerProperties;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseManager {

    // 1. Biến static lưu trữ "anh bảo vệ" duy nhất
    private static DatabaseManager instance;
    private final ServerProperties properties;

    // 2. Private Constructor: Cấm bên ngoài dùng từ khóa 'new'
    private DatabaseManager(ServerProperties properties) {
        this.properties = properties;
        try {
            // Nạp trực tiếp MySQL Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("Lỗi: Không tìm thấy thư viện MySQL Driver trong pom.xml.", ex);
        }
    }

    // 3. Hàm này chạy ĐÚNG 1 LẦN lúc khởi động Server để "tuyển bảo vệ"
    public static synchronized DatabaseManager initialize(ServerProperties properties) {
        if (instance == null) {
            instance = new DatabaseManager(properties);
        }
        return instance;
    }

    // 4. Các class khác khi cần Database sẽ gọi hàm này
    public static DatabaseManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("DatabaseManager chưa được khởi tạo. Hãy gọi initialize() ở hàm main trước.");
        }
        return instance;
    }

    // 5. Mở "đường ống" nối lên Aiven Cloud
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                properties.getDbUrl(),
                properties.getDbUsername(),
                properties.getDbPassword()
        );
    }
}