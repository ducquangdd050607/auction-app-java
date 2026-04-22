package com.auctionapp.auctionappjava.common.util;

import com.auctionapp.auctionappjava.common.exception.ValidationException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

public final class ValidationUtils {

    private static final Pattern EMAIL =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private ValidationUtils() {}

    public static String requireText(String v, String f) {
        if (v == null || v.trim().isEmpty())
            throw new ValidationException(f + " không được bỏ trống");
        return v.trim();
    }

    public static void requireEmail(String e) {
        if (e == null || !EMAIL.matcher(e.trim()).matches())
            throw new ValidationException("Email không hợp lệ");
    }

    public static void requirePositive(BigDecimal v, String f) {
        if (v == null || v.compareTo(BigDecimal.ZERO) <= 0)
            throw new ValidationException(f + " phải > 0");
    }

    public static void requireTimeRange(LocalDateTime s, LocalDateTime e) {
        if (s == null || e == null || !e.isAfter(s))
            throw new ValidationException("Thời gian kết thúc phải sau thời gian bắt đầu");
    }
}