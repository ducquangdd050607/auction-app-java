package com.auctionapp.auctionappjava.common.config;
//cái class này chỉ chứa hằng số thôi ko có ý nghĩa nhiều lắm nhg vx có:)
import java.math.BigDecimal;
//BigDecimal lưu và tính toán số thực (số thập phân) với độ chính xác cao tuyệt đối
public final class AppConstants {
    public static final BigDecimal DEFAULT_MIN_INCREMENT = BigDecimal.valueOf(10);
    //Mỗi lần bid phải tăng ít nhất 10
    public static final int DEFAULT_SERVER_PORT = 9090;
    //Server chạy ở: http://localhost:9090 (tại máy cá nhân)
    public static final long DEFAULT_MONITOR_INTERVAL_MS = 1000L;
    //Server check trạng thái mỗi: 1s
    public static final long DEFAULT_ANTI_SNIPING_THRESHOLD_SECONDS = 36L;
    // Người dùng bid giây cuối cùng để thắng - tăng time(tránh canh giờ)
    // Nếu còn ≤ 36s mà có người bid
    public static final long DEFAULT_ANTI_SNIPING_EXTENSION_SECONDS = 60L;
    //tự động gia hạn thêm 60s
    private AppConstants() {
        //Không cho tạo object
    }
}
