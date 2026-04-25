package com.auctionapp.auctionappjava.server.config;

import com.auctionapp.auctionappjava.common.config.AppConstants;

public final class ServerProperties {

    public static final String DB_URL = System.getProperty(
            "auction.db.url",
            "jdbc:mysql://gateway01.ap-southeast-1.prod.alicloud.tidbcloud.com:4000/auction_db?sslMode=REQUIRED"
    );

    public static final String DB_USER = System.getProperty(
            "auction.db.user",
            "2TeQgS3hxWhiG47.root"
    );

    public static final String DB_PASSWORD = System.getProperty(
            "auction.db.password",
            "85xxX81SEbau4wZZ"
    );

    public static final int SERVER_PORT = Integer.getInteger(
            "auction.server.port",
            AppConstants.DEFAULT_SERVER_PORT
    );

    private ServerProperties() {}
}