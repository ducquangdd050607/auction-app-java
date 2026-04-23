package com.auctionapp.auctionappjava.client.service;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

public class RuleBasedChatbotService implements ChatbotService {
    private final Map<Predicate<String>, String> rules = new LinkedHashMap<>();
    public RuleBasedChatbotService() {
        rules.put(s -> has(s,"login") || has(s,"dang nhap"), "Vào màn hình Đăng nhập, nhập username/password. Nếu chưa có tài khoản, hãy đăng ký trước rồi chọn vai trò Bidder/Seller/Admin.");
        rules.put(s -> has(s,"register") || has(s,"dang ky"), "Đăng ký cần username, email và mật khẩu tối thiểu 6 ký tự. Tài khoản mới mặc định có ví để nạp tiền.");
        rules.put(s -> has(s,"admin") || has(s,"seller") || has(s,"bidder") || has(s,"vai tro"), "Vai trò: Bidder đặt giá và nạp ví; Seller tạo/quản lý phiên; Admin xem dashboard và quản lý user.");
        rules.put(s -> has(s,"tao auction") || has(s,"tao phien") || has(s,"dang san pham"), "Seller tạo auction bằng tên, mô tả, giá khởi điểm, bước giá, thời gian bắt đầu/kết thúc và loại sản phẩm.");
        rules.put(s -> has(s,"xem auction") || has(s,"danh sach") || has(s,"chi tiet"), "Mở Danh sách đấu giá để xem các phiên. Chọn một phiên để xem chi tiết, lịch sử bid và trạng thái realtime.");
        rules.put(s -> has(s,"dat gia") || has(s,"bid"), "Bid hợp lệ phải cao hơn giá hiện tại ít nhất bằng bước giá tối thiểu, phiên phải RUNNING và ví phải đủ số dư.");
        rules.put(s -> has(s,"auto bid") || has(s,"tu dong"), "Auto-bid cần maxBid và increment. Hệ thống tự trả giá đến maxBid, ưu tiên cấu hình tạo trước khi nhiều người cùng auto-bid.");
        rules.put(s -> has(s,"wallet") || has(s,"vi") || has(s,"nap tien") || has(s,"deposit"), "Vào Tài khoản → Nạp tiền. Số dư ví được kiểm tra trước khi đặt giá và được hoàn khi bạn bị người khác vượt giá.");
        rules.put(s -> has(s,"dashboard") || has(s,"thong ke"), "Dashboard hiển thị tổng quan phù hợp với vai trò: Admin xem users/auctions, Seller xem phiên của mình, Bidder xem phiên đã tham gia.");
        rules.put(s -> has(s,"trang thai") || has(s,"status"), "Trạng thái phiên: OPEN → RUNNING → FINISHED → PAID/CANCELED. Scheduler tự chuyển trạng thái theo thời gian.");
        rules.put(s -> has(s,"loi") || has(s,"khong ket noi") || has(s,"sai"), "Lỗi thường gặp: server chưa chạy, database chưa tạo, số dư không đủ, bid thấp hơn tối thiểu hoặc phiên đã đóng.");
    }
    @Override public String answer(String message) { String s=normalize(message); return rules.entrySet().stream().filter(e -> e.getKey().test(s)).map(Map.Entry::getValue).findFirst().orElse("Mình chưa hiểu ý bạn. Bạn có thể hỏi về đăng nhập, tạo auction, đặt giá, auto bid, ví, dashboard hoặc trạng thái phiên."); }
    private boolean has(String s,String token){ return s.contains(token); }
    private String normalize(String input){ if(input==null) return ""; String n= Normalizer.normalize(input, Normalizer.Form.NFD).replaceAll("\\p{M}",""); return n.toLowerCase(Locale.ROOT).trim(); }
}
