package com.auctionhub.client.util;

import com.auctionhub.common.enums.Role;

public final class RoleIconResolver {
    private RoleIconResolver() {
    }

    public static String iconFor(Role role) {
        return switch (role) {
            case BIDDER -> "💸";
            case SELLER -> "🛍";
            case ADMIN -> "🛡";
        };
    }
}
