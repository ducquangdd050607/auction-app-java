package com.auctionapp.auctionappjava.client.config;

import com.auctionapp.auctionappjava.common.config.AppConstants;

public class ClientConfig {
    private final String host; private final int port; private final int timeoutMillis;
    public ClientConfig(String host, int port, int timeoutMillis){ this.host=host; this.port=port; this.timeoutMillis=timeoutMillis; }
    public static ClientConfig load(){ return new ClientConfig(pick("auction.client.host","AUCTION_SERVER_HOST","127.0.0.1"), parse(pick("auction.client.port","AUCTION_SERVER_PORT",String.valueOf(AppConstants.DEFAULT_SERVER_PORT)), AppConstants.DEFAULT_SERVER_PORT), parse(pick("auction.client.timeout.ms","AUCTION_CLIENT_TIMEOUT_MS","10000"),10000)); }
    private static String pick(String prop,String env,String fallback){ String v=System.getProperty(prop); if(v==null||v.isBlank()) v=System.getenv(env); return v==null||v.isBlank()?fallback:v.trim(); }
    private static int parse(String text,int fallback){ try{return Integer.parseInt(text);}catch(Exception e){return fallback;} }
    public String getHost(){return host;} public int getPort(){return port;} public int getTimeoutMillis(){return timeoutMillis;}
}
