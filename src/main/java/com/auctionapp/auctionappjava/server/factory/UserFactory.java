package com.auctionapp.auctionappjava.server.factory;

import com.auctionapp.auctionappjava.common.enums.Role;
import com.auctionapp.auctionappjava.server.model.Admin;
import com.auctionapp.auctionappjava.server.model.Bidder;
import com.auctionapp.auctionappjava.server.model.Seller;
import com.auctionapp.auctionappjava.server.model.User;

public final class UserFactory {

  private UserFactory() {}

  public static User create(Role role) {
    return switch (role == null ? Role.BIDDER : role) {
      case ADMIN -> new Admin();
      case SELLER -> new Seller();
      case BIDDER -> new Bidder();
    };
  }
}
