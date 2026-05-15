# Hướng dẫn cập nhật tính năng Auto-Bidding, Anti-sniping, Realtime và biểu đồ bid thật

## 1. Cách dùng file zip này

Có 2 cách sửa vào project cũ:

### Cách nhanh nhất
1. Giải nén file `auction-app-java-auto-realtime-chart.zip`.
2. Mở thư mục project bằng IntelliJ IDEA.
3. Kiểm tra file cấu hình DB trong `ServerProperties.java` cho đúng MySQL local của bạn.
4. Chạy lại schema nếu database cũ chưa có bảng `auto_bid_configs`.
5. Chạy server trước, sau đó chạy client.

### Cách ghép thủ công vào project cũ
Copy các file mới/thay đổi trong zip này sang project cũ theo đúng đường dẫn bên dưới.

## 2. File đã thêm mới

### DTO
- `src/main/java/com/auctionapp/auctionappjava/common/dto/ConfigureAutoBidRequest.java`
- `src/main/java/com/auctionapp/auctionappjava/common/dto/AuctionRealtimeEvent.java`
- `src/main/java/com/auctionapp/auctionappjava/common/dto/BidHistoryPointDto.java`
- `src/main/java/com/auctionapp/auctionappjava/common/dto/BidHistoryChartResponse.java`

### Realtime server
- `src/main/java/com/auctionapp/auctionappjava/server/realtime/ClientConnection.java`
- `src/main/java/com/auctionapp/auctionappjava/server/realtime/AuctionRealtimeHub.java`

### Service/engine
- `src/main/java/com/auctionapp/auctionappjava/server/service/AutoBidService.java`
- `src/main/java/com/auctionapp/auctionappjava/server/service/AutoBidEngine.java`
- `src/main/java/com/auctionapp/auctionappjava/server/service/BidHistoryService.java`

## 3. File đã sửa chính

- `src/main/java/com/auctionapp/auctionappjava/server/service/AuctionService.java`
- `src/main/java/com/auctionapp/auctionappjava/server/service/AuctionStatusService.java`
- `src/main/java/com/auctionapp/auctionappjava/server/network/ClientHandler.java`
- `src/main/java/com/auctionapp/auctionappjava/client/network/Client.java`
- `src/main/java/com/auctionapp/auctionappjava/client/controllers/AuctionDetailController.java`
- `src/main/java/com/auctionapp/auctionappjava/client/controllers/ConfirmBiddingController.java`
- `src/main/resources/com/auctionapp/auctionappjava/views/AuctionDetailScreen.fxml`
- `src/main/java/com/auctionapp/auctionappjava/server/dao/BidDao.java`
- `src/main/java/com/auctionapp/auctionappjava/common/enums/RequestAction.java`
- `src/main/java/com/auctionapp/auctionappjava/common/enums/EventType.java`
- `src/main/java/module-info.java`

## 4. Chỗ cần sửa thủ công nếu ghép vào project cũ

### Bước 1: Thêm request mới trong `ClientHandler`
Thêm các case:

```java
case "CONFIGURE_AUTO_BID":
    response = autoBidService.handleConfigureAutoBid((ConfigureAutoBidRequest) request.payload());
    break;
case "GET_BID_HISTORY":
    response = bidHistoryService.handleGetBidHistory(request.payload());
    break;
case "SUBSCRIBE_AUCTION":
    realtimeHub.subscribe(parseAuctionId(request.payload()), connection);
    response = new Response(true, "Đã subscribe realtime auction.", null);
    break;
case "UNSUBSCRIBE_AUCTION":
    realtimeHub.unsubscribe(parseAuctionId(request.payload()), connection);
    response = new Response(true, "Đã unsubscribe realtime auction.", null);
    break;
```

Nhớ dùng `ClientConnection` để ghi response/event chung một `ObjectOutputStream` có `synchronized`.

### Bước 2: Sửa `Client.java`
Không để `sendRequest()` tự `in.readObject()` nữa, vì realtime event có thể chen giữa response.

Client mới dùng:
- 1 thread listener riêng đọc mọi object từ server.
- Nếu object là `Response` thì bỏ vào queue.
- Nếu object là `AuctionRealtimeEvent` thì gọi listener của UI.

### Bước 3: Sửa `AuctionService.handlePlaceBid`
Thêm lock theo auction:

```java
ConcurrentHashMap<UUID, ReentrantLock> AUCTION_LOCKS
```

Flow mới:
1. Lock theo `auctionId`.
2. Reload auction mới nhất từ DB.
3. Validate status/time/currentPrice.
4. Lưu bid thật vào bảng `bids`.
5. Update `currentPrice`, `leadingBidderId`.
6. Broadcast `BID_PLACED`.
7. Nếu bid ở 30 giây cuối thì extend thêm 60 giây và broadcast `AUCTION_EXTENDED`.
8. Chạy auto-bid trong cùng lock.
9. Unlock.

### Bước 4: Biểu đồ không dùng sample data
Màn hình `AuctionDetailScreen.fxml` đã thêm `LineChart`.

Controller `AuctionDetailController` gọi:

```java
GET_BID_HISTORY
```

Server trả về `BidHistoryChartResponse`, lấy từ `BidDao.findByAuctionId(...)`.

Khi có event:

```java
BID_PLACED
```

controller tự thêm điểm mới vào chart.

## 5. Kiểm tra database

Project hiện dùng bảng `bids`, không phải `bid_transactions`.
Vì vậy query lấy lịch sử bid là:

```sql
SELECT * FROM bids
WHERE auction_id = ?
ORDER BY created_at ASC;
```

Bảng `auto_bid_configs` đã có trong schema. Nếu DB cũ thiếu bảng này thì chạy lại `src/main/resources/db/mysql-schema.sql`.

## 6. Cách test/demo

1. Chạy server.
2. Chạy 2 client.
3. Cả 2 client mở cùng 1 auction.
4. Chart ban đầu phải load lịch sử bid thật từ DB.
5. Client A đặt bid thủ công.
6. Client B thấy realtime:
   - giá hiện tại đổi
   - người dẫn đầu đổi
   - chart thêm điểm mới
7. Client B bật auto-bid, nhập maxBid và increment.
8. Client A đặt giá thấp hơn maxBid của B.
9. Server tự bid thay B, cả 2 chart thêm điểm auto-bid.
10. Đặt bid trong 30 giây cuối, auction tự gia hạn thêm 60 giây.

## 7. Lưu ý build

Trong môi trường này không chạy được Maven vì không có internet để Maven Wrapper tải Maven từ `repo.maven.apache.org`. Tôi đã kiểm tra compile phần `common + server` bằng `javac` và phần này không báo lỗi cú pháp.

Trên máy của bạn, hãy chạy:

```bash
mvn clean test
mvn clean install
```

Nếu máy bạn dùng JDK 21 thay vì JDK 25, sửa trong `pom.xml`:

```xml
<source>21</source>
<target>21</target>
```

hoặc cài JDK 25 đúng như file `pom.xml` hiện tại.
