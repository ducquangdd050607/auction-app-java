package com.auctionapp.auctionappjava.server.config;

public final class ServerProperties {

    public static final String DB_URL = System.getProperty(
            "auction.db.url",
            "jdbc:mysql://gateway01.ap-southeast-1.prod.alicloud.tidbcloud.com:4000/auction"
                    + "?sslMode=REQUIRED"
                    + "&serverTimezone=UTC"
    );

    public static final String DB_USER = System.getProperty(
            "auction.db.user",
            "3VUEtAJK3q1XAAe.root"
    );

    public static final String DB_PASSWORD = System.getProperty(
            "auction.db.password",
            "MI0ErNk4d1qx3bdR"
    );

    public static final int SERVER_PORT = Integer.getInteger(
            "auction.server.port",
            9090
    );

    private ServerProperties() {
    }
}
