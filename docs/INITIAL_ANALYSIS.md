# Initial Assessment

## Ghi chú quan trọng
Trong phiên làm việc này **không có source code cũ được mount vào workspace**, nên không thể audit trực tiếp repo ban đầu theo kiểu file-by-file. Vì vậy hướng xử lý được chọn là:

1. Xác nhận không có mã nguồn hiện hữu trong môi trường.
2. Thiết kế lại từ đầu theo kiến trúc chuẩn đồ án.
3. Tạo một baseline project hoàn chỉnh để bạn có thể nộp, demo, hoặc dùng làm nhánh refactor khi nhập lại source cũ.

## Những vấn đề kiến trúc thường gặp mà baseline này giải quyết
- UI ôm business logic, khó test.
- Client truy cập DB trực tiếp hoặc logic phân tán không rõ server-only.
- Thiếu phân quyền rõ cho Bidder / Seller / Admin.
- Thiếu logout an toàn và route protection.
- Realtime đấu giá dùng refresh tay hoặc polling liên tục.
- Xử lý concurrency bid chưa thread-safe.
- Model chưa thể hiện OOP, pattern dùng hời hợt.
- Thiếu README, package tree, seed data, CI/CD và test.

## Kiến trúc đề xuất và đã triển khai
- **Client**: JavaFX + FXML + controller riêng theo MVC.
- **Server**: socket server + dispatcher + service + DAO + JDBC.
- **Shared/common**: entity, DTO, enum, util, pattern support.
- **Persistence**: mặc định H2 để clone chạy nhanh, có schema MySQL riêng để chuyển sang MySQL thật.
- **Realtime**: socket push + observer/event publisher.
- **Concurrency**: per-auction lock để tránh race condition khi nhiều người bid gần đồng thời.
- **Advanced features**: auto-bid, anti-sniping, line chart realtime, chatbot rule-based, logout an toàn.
