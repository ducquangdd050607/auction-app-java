# Quick Demo Script

## 1) Chuẩn bị
- Chạy server trước.
- Chạy 2 client song song để demo realtime bidding.
- Dùng tài khoản mẫu:
  - `admin / admin123`
  - `seller1 / seller123`
  - `bidder1 / bidder123`
  - `bidder2 / bidder123`

## 2) Kịch bản demo 3-5 phút
1. **Client A** đăng nhập bằng `seller1`.
   - Mở **Seller Dashboard**.
   - Cho giảng viên xem danh sách phiên có sẵn.
   - Tạo thêm 1 phiên mới hoặc chỉnh sửa một phiên OPEN.

2. **Client B** đăng nhập bằng `bidder1`.
   - Mở **Phiên đấu giá**.
   - Chọn phiên RUNNING `MacBook Pro M3`.
   - Cho xem chi tiết phiên, bid history, line chart.

3. **Client C** (nếu có) đăng nhập bằng `bidder2`.
   - Chọn cùng phiên `MacBook Pro M3`.
   - Đặt giá mới lớn hơn giá hiện tại.
   - Quan sát Client B cập nhật realtime ngay lập tức.

4. Demo **auto-bid**.
   - `bidder1` cấu hình `maxBid` + `increment`.
   - `bidder2` đặt giá thủ công.
   - Hệ thống tự động phản ứng, cập nhật leader và lịch sử bid.

5. Demo **anti-sniping**.
   - Chỉnh một phiên về gần thời điểm kết thúc.
   - Đặt giá trong vài chục giây cuối.
   - Cho xem `endTime` tự gia hạn trên UI và realtime event đẩy sang client khác.

6. Demo **Admin**.
   - Đăng nhập `admin`.
   - Mở **Admin Dashboard**.
   - Xem thống kê users/auctions.
   - Chuyển một phiên `FINISHED` sang `PAID`.

7. Demo **Chatbot + Logout**.
   - Mở chatbot ở panel phải.
   - Hỏi: "Vì sao bid bị từ chối?" hoặc "OPEN / RUNNING / FINISHED khác nhau thế nào?"
   - Bấm **Logout** và xác nhận app quay lại Login, không thể truy cập màn hình nội bộ nữa.

## 3) Điểm nhấn nên nói khi thuyết trình
- Server-only DB access, client chỉ giao tiếp qua socket.
- Realtime push dùng observer/event-based, không polling liên tục.
- Bid processing thread-safe nhờ per-auction lock.
- OOP + MVC + DAO + Service + Factory + Singleton + Strategy + Observer.
- Có logout an toàn, chatbot hỗ trợ, auto-bid, anti-sniping, line chart realtime.
