package com.auctionapp.auctionappjava.server.config;

import java.io.InputStream;
import java.util.Properties;

public final class ServerProperties {

  // Tạo đối tượng Properties để lưu trữ cấu hình
  private static final Properties properties = new Properties();

  // Khối static sẽ chạy 1 lần duy nhất khi class này được gọi
  static {
    // Tìm và đọc file config.properties từ thư mục resources (classpath)
    try (InputStream input =
        ServerProperties.class.getClassLoader().getResourceAsStream("config.properties")) {
      if (input == null) {
        System.err.println(
            "Error: Khong tim thay file config.properties, he thong se su dung gia tri mac dinh");
      } else {
        properties.load(input); // Tải toàn bộ key-value vào bộ nhớ
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  /*
   * Lấy giá trị từ file properties.
   * Tham số thứ 2 ("...") là giá trị mặc định (fallback) trong trường hợp
   * file properties bị xóa hoặc mất key.
   */
  public static final String DB_URL =
      properties.getProperty("auction.db.url", "jdbc:mysql://localhost:3306/auction_app");

  public static final String DB_USER = properties.getProperty("auction.db.user", "root");

  public static final String DB_PASSWORD = properties.getProperty("auction.db.password", "");

  public static final int SERVER_PORT =
      Integer.parseInt(properties.getProperty("auction.server.port", "8080"));

  /*
   * Private constructor để không cho tạo object từ class config này.
   */
  private ServerProperties() {}
}
