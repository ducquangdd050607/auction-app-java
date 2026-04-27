package com.auctionapp.auctionappjava.server.config;

import com.auctionapp.auctionappjava.common.config.AppConstants;

public final class ServerProperties {

    /*
     * URL kết nối DB TiDB Cloud.
     *
     * Cấu trúc:
     * jdbc:mysql://HOST:PORT/DATABASE?sslMode=REQUIRED&serverTimezone=UTC
     *
     * - HOST: gateway01.ap-southeast-1.prod.alicloud.tidbcloud.com
     * - PORT: 4000
     * - DATABASE: auction_app
     * - sslMode=REQUIRED: TiDB Cloud public endpoint yêu cầu TLS/SSL
     * - serverTimezone=UTC: tránh lỗi timezone của JDBC
     */
    public static final String DB_URL = System.getProperty(
            "auction.db.url",
            "jdbc:mysql://gateway01.ap-southeast-1.prod.alicloud.tidbcloud.com:4000/auction_app?sslMode=REQUIRED&serverTimezone=UTC"
    );

    /*
     * Username lấy từ phần Connect của TiDB Cloud.
     *
     * Có thể override khi chạy app bằng JVM option:
     * -Dauction.db.user=your_username
     */
    public static final String DB_USER = System.getProperty(
            "auction.db.user",
            "3o8AnK1TAtQ7Tbu.root"
    );

    /*
     * Password DB.
     *
     * Lưu ý:
     * - Không nên commit password thật lên GitHub.
     * - Có thể override khi chạy app bằng:
     *   -Dauction.db.password=your_password
     */
    public static final String DB_PASSWORD = System.getProperty(
            "auction.db.password",
            "fIuI1VfsMgUpAUok"
    );

    /*
     * Port server socket của app.
     *
     * Mặc định lấy từ AppConstants.DEFAULT_SERVER_PORT.
     * Có thể override bằng:
     * -Dauction.server.port=9090
     */
    public static final int SERVER_PORT = Integer.getInteger(
            "auction.server.port",
            AppConstants.DEFAULT_SERVER_PORT
    );

    /*
     * Private constructor để không cho tạo object từ class config này.
     */
    private ServerProperties() {
    }
}