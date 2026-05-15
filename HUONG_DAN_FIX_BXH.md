# Hướng dẫn sửa màn hình BXH / Ranking realtime

## Đã sửa gì?

Màn hình `BXH` trước đó chỉ hiển thị header auction, table trống và chart trắng. Bản này đã sửa để:

- Khi mở BXH, client gửi `GET_BID_HISTORY` lên server.
- Server lấy lịch sử bid thật từ bảng `bids` qua `BidHistoryService -> BidDao -> JdbcBidDao`.
- Table bên trái render lịch sử bid thật: STT, tên bidder, giá đặt, thời gian đặt.
- LineChart bên phải render giá bid thật theo thời gian.
- Khi có realtime event `BID_PLACED`, table và chart tự thêm dòng/điểm mới.
- Không dùng sample data, fake data, random data hay mock history.
- Khi rời màn BXH, client gửi `UNSUBSCRIBE_AUCTION` và gỡ realtime listener.

## File đã sửa

1. `src/main/java/com/auctionapp/auctionappjava/client/controllers/RankingListController.java`
   - Thêm load bid history từ DB qua request `GET_BID_HISTORY`.
   - Thêm render table bid history.
   - Thêm render LineChart.
   - Thêm subscribe realtime `SUBSCRIBE_AUCTION`.
   - Thêm xử lý event `BID_PLACED`, `AUCTION_EXTENDED`, `AUCTION_FINISHED`.
   - Thêm chống duplicate bằng key bid.

2. `src/main/resources/com/auctionapp/auctionappjava/views/RankingListScreen.fxml`
   - Sửa kích thước table.
   - Sửa chart title: `Biểu đồ giá đấu realtime`.
   - Sửa axis:
     - X: `Thời gian`
     - Y: `Giá đấu`
   - Tắt animation chart để update realtime ổn định hơn.

3. `src/main/java/com/auctionapp/auctionappjava/client/controllers/AuctionDetailController.java`
   - Khi bấm BXH, controller cũ unsubscribe realtime trước khi chuyển màn để tránh duplicate listener.

## Cách copy vào project cũ

Nếu project cũ của bạn đã có đủ backend realtime/chart từ bản trước, chỉ cần copy đè 3 file trên vào đúng đường dẫn.

Nếu project cũ là bản rất cũ, chưa có AutoBid/Reatime/BidHistory backend, hãy copy toàn bộ project trong zip này hoặc copy thêm các file backend đã tạo ở bản trước:

- `common/dto/AuctionRealtimeEvent.java`
- `common/dto/BidHistoryPointDto.java`
- `common/dto/BidHistoryChartResponse.java`
- `server/service/BidHistoryService.java`
- `server/realtime/AuctionRealtimeHub.java`
- `server/realtime/ClientConnection.java`
- sửa `server/network/ClientHandler.java` để có `GET_BID_HISTORY`, `SUBSCRIBE_AUCTION`, `UNSUBSCRIBE_AUCTION`
- sửa `client/network/Client.java` để có realtime listener thread

## Cách test nhanh

1. Chạy server.
2. Chạy client A và client B.
3. Mở cùng một auction.
4. Bấm `BXH`.
5. Kiểm tra:
   - table load lịch sử bid cũ từ DB.
   - chart có đường giá thật theo thời gian.
6. Ở client khác đặt bid mới.
7. Màn BXH phải tự cập nhật:
   - table thêm dòng mới.
   - chart thêm điểm mới.
   - giá cao nhất và người dẫn đầu cập nhật.

## Lưu ý

- Nếu table vẫn trống nhưng DB có bid, kiểm tra server console xem request `GET_BID_HISTORY` có lỗi không.
- Nếu chart không realtime, kiểm tra server có broadcast `BID_PLACED` sau khi lưu bid không.
- Nếu mở BXH báo không có auction, kiểm tra trước khi mở màn đã gọi `AuctionSession.getInstance().setCurrentAuction(auction)` chưa.
