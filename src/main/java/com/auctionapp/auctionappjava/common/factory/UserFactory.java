package com.auctionapp.auctionappjava.common.factory;

import com.auctionapp.auctionappjava.common.enums.Role;
import com.auctionapp.auctionappjava.common.model.*;

public final class UserFactory {

    private UserFactory() {}

    public static User create(Role role) {
        return switch (role == null ? Role.BIDDER : role) {
            case ADMIN  -> new Admin();
            case SELLER -> new Seller();
            case BIDDER -> new Bidder();
        };
    }
}