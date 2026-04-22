package com.auctionapp.auctionappjava.common.util;

import java.math.BigDecimal;

public final class MoneyUtils {

    private MoneyUtils() {}

    public static BigDecimal zeroIfNull(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}