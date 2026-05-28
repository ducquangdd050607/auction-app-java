package com.auctionapp.auctionappjava.server;

import com.auctionapp.auctionappjava.server.config.ServerProperties;
import com.auctionapp.auctionappjava.server.network.Server;

public class ServerLauncher {
  public static void main(String[] args) {
    Server.startServer(ServerProperties.SERVER_PORT, 50);
  }
}
