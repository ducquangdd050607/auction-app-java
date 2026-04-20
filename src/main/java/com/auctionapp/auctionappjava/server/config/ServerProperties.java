package com.auctionapp.auctionappjava.server.config;



import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ServerProperties {
    private final Properties properties = new Properties();

    public ServerProperties() {
        // Luồng đọc file application.properties từ thư mục resources
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                throw new IOException("Không tìm thấy file application.properties trong resources");
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Lỗi nghiêm trọng: Không thể nạp cấu hình hệ thống", ex);
        }
    }

    // 1. Kiểm tra xem có đang dùng MySQL hay không
    public boolean isMysql() {
        return "mysql".equalsIgnoreCase(properties.getProperty("app.db.vendor", "h2"));
    }

    // 2. Lấy URL kết nối (Nếu là mysql thì lấy app.db.mysql.url)
    public String getDbUrl() {
        return isMysql()
                ? properties.getProperty("app.db.mysql.url")
                : properties.getProperty("app.db.url");
    }

    // 3. Lấy Username
    public String getDbUsername() {
        return isMysql()
                ? properties.getProperty("app.db.mysql.username")
                : properties.getProperty("app.db.username");
    }

    // 4. Lấy Password
    public String getDbPassword() {
        return isMysql()
                ? properties.getProperty("app.db.mysql.password")
                : properties.getProperty("app.db.password");
    }
}