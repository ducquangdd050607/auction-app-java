package com.auctionhub.client.service;

import java.util.List;
import java.util.Locale;

public class RuleBasedChatbotService implements ChatbotService {
    private static final List<String> SUGGESTED = List.of(
            "Cách đăng ký tài khoản?",
            "Các vai trò Bidder / Seller / Admin là gì?",
            "Cách tạo phiên đấu giá?",
            "Cách đặt giá và auto-bid?",
            "Vì sao bid của tôi bị từ chối?",
            "Ý nghĩa các trạng thái OPEN / RUNNING / FINISHED / PAID / CANCELED?",
            "Anti-sniping hoạt động ra sao?",
            "Làm sao đăng xuất an toàn?"
    );

    @Override
    public String answer(String question) {
        String normalized = question == null ? "" : question.toLowerCase(Locale.ROOT).trim();
        if (normalized.contains("đăng ký") || normalized.contains("register")) {
            return "Để đăng ký, mở màn hình Register, nhập username, họ tên, email, mật khẩu và chọn vai trò BIDDER hoặc SELLER. Hệ thống sẽ kiểm tra trùng username, định dạng email và mật khẩu xác nhận.";
        }
        if (normalized.contains("đăng nhập") || normalized.contains("login")) {
            return "Đăng nhập bằng username + password tại màn hình Login. Khi thành công, hệ thống tạo session phía client, điều hướng đúng dashboard theo role và chỉ khi đó mới cho truy cập các màn hình nội bộ.";
        }
        if (normalized.contains("vai trò") || normalized.contains("role") || normalized.contains("bidder") || normalized.contains("seller") || normalized.contains("admin")) {
            return "Bidder dùng để xem phiên và đặt giá. Seller dùng để tạo, sửa, hủy, quản lý sản phẩm đấu giá. Admin dùng để giám sát toàn hệ thống, xem user, theo dõi phiên và chuyển FINISHED -> PAID khi cần.";
        }
        if (normalized.contains("tạo phiên") || normalized.contains("create auction")) {
            return "Seller vào Seller Dashboard, nhập loại item, tên, mô tả, giá khởi điểm, bước giá tối thiểu, thời gian bắt đầu/kết thúc rồi bấm Create. Nếu thời gian bắt đầu ở tương lai thì phiên ở trạng thái OPEN, tới giờ sẽ tự chuyển RUNNING.";
        }
        if (normalized.contains("đặt giá") || normalized.contains("bid")) {
            return "Bid hợp lệ phải lớn hơn hoặc bằng giá hiện tại + bước giá tối thiểu. Khi có bid mới, server cập nhật leader, lịch sử bid, biểu đồ giá và đẩy realtime tới tất cả client đang subscribe cùng phiên.";
        }
        if (normalized.contains("từ chối") || normalized.contains("rejected") || normalized.contains("vì sao")) {
            return "Bid thường bị từ chối vì một trong các lý do: phiên chưa RUNNING hoặc đã FINISHED/CANCELED, bid không vượt giá tối thiểu, người bán tự bid vào phiên của mình, hoặc kết nối tới server bị gián đoạn.";
        }
        if (normalized.contains("open") || normalized.contains("running") || normalized.contains("finished") || normalized.contains("paid") || normalized.contains("canceled")) {
            return "OPEN: đã tạo nhưng chưa tới giờ bắt đầu. RUNNING: đang nhận bid. FINISHED: đã hết giờ và khóa bid mới, đã xác định winner. PAID: admin xác nhận đã thanh toán xong. CANCELED: phiên bị hủy và dừng xử lý.";
        }
        if (normalized.contains("anti-sniping") || normalized.contains("gia hạn")) {
            return "Anti-sniping giúp công bằng hơn: nếu có bid xuất hiện trong X giây cuối, server tự động cộng thêm Y giây vào end time. Mọi client xem phiên đều nhận end time mới ngay qua kênh realtime.";
        }
        if (normalized.contains("logout") || normalized.contains("đăng xuất")) {
            return "Khi bấm Logout, client sẽ gửi yêu cầu logout, xóa session hiện tại, ngắt socket listener một cách an toàn rồi quay về màn hình Login. Vì scene nội bộ bị thay thế hoàn toàn nên không thể back lại màn hình cũ nếu chưa login lại.";
        }
        return "Tôi là chatbot hỗ trợ rule-based. Bạn có thể hỏi về đăng ký, đăng nhập, phân quyền, tạo phiên, đặt giá, auto-bid, anti-sniping, trạng thái phiên và đăng xuất an toàn.";
    }

    @Override
    public List<String> suggestedQuestions() {
        return SUGGESTED;
    }
}
