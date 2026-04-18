package com.auctionapp.auctionappjava.common.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public final class MoneyUtils {
    private static final NumberFormat FORMAT = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    private MoneyUtils() {
    }

    public static String format(BigDecimal amount) {
        return amount == null ? FORMAT.format(0) : FORMAT.format(amount);
    }
}
