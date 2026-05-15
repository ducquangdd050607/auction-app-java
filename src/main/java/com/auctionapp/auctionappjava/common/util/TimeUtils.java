package com.auctionapp.auctionappjava.common.util;
//Cung cấp thời gian hiện tại cho toàn hệ thống
//Tạo 1 điểm truy cập thống nhất (centralized time source)
import java.time.LocalDateTime;

public final class TimeUtils {

    private TimeUtils() {}

    public static LocalDateTime now() {
        return LocalDateTime.now();
    }
}