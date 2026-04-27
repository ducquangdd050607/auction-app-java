package com.auctionapp.auctionappjava.common.util;
//Tránh lỗi NullPointerException khi làm việc với số tiền
import java.math.BigDecimal;

public final class MoneyUtils {

    private MoneyUtils() {}

    public static BigDecimal zeroIfNull(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}