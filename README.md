[![Java CI with Maven](https://github.com/ducquangdd050607/auction-app-java/actions/workflows/maven.yml/badge.svg)](https://github.com/ducquangdd050607/auction-app-java/actions/workflows/maven.yml)

# Auction App Java

## 1. Mô tả bài toán và phạm vi hệ thống

Auction App Java là ứng dụng đấu giá trực tuyến dạng desktop, cho phép người dùng đăng ký, đăng nhập, đăng sản phẩm, tham gia đấu giá và theo dõi kết quả đấu giá theo thời gian thực.

Phạm vi hệ thống:

- Client JavaFX cung cấp giao diện cho người dùng.
- Server socket xử lý kết nối client, request/response, nghiệp vụ đấu giá và cập nhật realtime.
- Cơ sở dữ liệu MySQL/TiDB lưu người dùng, sản phẩm, phiên đấu giá, lịch sử đặt giá, ví tiền và cấu hình auto bid.
- Hệ thống tập trung vào luồng đấu giá nội bộ, chưa tích hợp cổng thanh toán thật, vận chuyển hoặc dịch vụ bên thứ ba ngoài database.

## 2. Công nghệ sử dụng, môi trường chạy và yêu cầu cài đặt

Công nghệ chính:

- Java 25.
- JavaFX 21.0.6 cho giao diện desktop.
- Maven/Maven Wrapper để build và chạy ứng dụng.
- MySQL Connector/J 8.0.33 để kết nối MySQL/TiDB.
- JUnit Jupiter 6.0.3 cho unit test.
- Socket TCP và Object Stream cho giao tiếp Client/Server.

Môi trường chạy khuyến nghị:

- Windows, macOS hoặc Linux có cài JDK 25.
- Maven có thể dùng trực tiếp qua Maven Wrapper trong repo: `mvnw` hoặc `mvnw.cmd`.
- Database MySQL/TiDB đã tạo schema theo file `src/main/resources/db/mysql-schema.sql` và có account admin mẫu trong file `src/main/resources/db/insert-dummy-data.sql`.

Yêu cầu cài đặt:

1. Cài JDK 25 và cấu hình biến môi trường `JAVA_HOME`.
2. Đảm bảo máy có kết nối tới database MySQL/TiDB.
3. Nếu không dùng cấu hình database mặc định trong code, truyền các JVM options sau khi chạy server:

```powershell
-Dauction.db.url=jdbc:mysql://HOST:PORT/auction_app?sslMode=REQUIRED&serverTimezone=UTC
-Dauction.db.user=your_username
-Dauction.db.password=your_password
```

4. Build project:

```powershell
.\mvnw.cmd clean package
```

Trên macOS/Linux:

```bash
./mvnw clean package
```


## 3. Cấu trúc thư mục và các module chính

```text
auction-app-java/
|-- pom.xml
|-- mvnw, mvnw.cmd
|-- README.md
|-- src/
|   |-- main/
|   |   |-- java/
|   |   |   |-- module-info.java
|   |   |   `-- com/auctionapp/auctionappjava/
|   |   |       |-- client/
|   |   |       |   |-- ClientLauncher.java
|   |   |       |   |-- AppLauncher.java
|   |   |       |   |-- controllers/
|   |   |       |   |-- network/
|   |   |       |   `-- session/
|   |   |       |-- common/
|   |   |       |   |-- dto/
|   |   |       |   |-- enums/
|   |   |       |   |-- exception/
|   |   |       |   `-- util/
|   |   |       `-- server/
|   |   |           |-- ServerLauncher.java
|   |   |           |-- config/
|   |   |           |-- dao/
|   |   |           |-- factory/
|   |   |           |-- model/
|   |   |           |-- network/
|   |   |           |-- service/
|   |   |           `-- strategy/
|   |   `-- resources/
|   |       |-- db/mysql-schema.sql
|   |       `-- com/auctionapp/auctionappjava/
|   |           |-- css/
|   |           |-- images/
|   |           `-- views/
|   `-- test/
|       `-- java/com/auctionapp/auctionappjava/
`-- target/
```

Mô tả module:

- `client`: màn hình JavaFX, controller, session người dùng và kết nối tới server.
- `server`: server socket, xử lý request, service nghiệp vụ, DAO, model và các chiến lược đấu giá.
- `common`: DTO, enum, exception và utility dùng chung giữa client/server.
- `resources/views`: các file FXML của giao diện.
- `resources/css`: style cho giao diện.
- `resources/db`: script tạo bảng database.
- `test`: unit test cho factory, service, strategy và utility.

## 4. Vị trí các file jar

Sau khi chạy lệnh build:

```powershell
.\mvnw.cmd clean package
```

File jar của ứng dụng được tạo tại:

```text
target/auction-app-java-1.0-SNAPSHOT.jar
```

Lưu ý:

- Thư mục `target/` là thư mục build output, có thể chưa tồn tại nếu chưa build project.
- Các jar phụ thuộc không đặt trong repo, Maven sẽ tải về local repository của máy:
  - Windows: `%USERPROFILE%\.m2\repository`
  - macOS/Linux: `~/.m2/repository`
- Một số dependency jar chính:
  - JavaFX: `org/openjfx/javafx-controls/21.0.6`, `org/openjfx/javafx-fxml/21.0.6`
  - MySQL Connector/J: `com/mysql/mysql-connector-j/8.0.33`
  - JUnit Jupiter: `org/junit/jupiter`

## 5. Hướng dẫn chạy Server/Client theo thứ tự

Cần chạy Server trước, sau đó mới chạy Client.

### Cách 1: Chạy bằng IntelliJ IDEA

1. Import project Maven từ file `pom.xml`.
2. Đảm bảo JDK project là JDK 25.
3. Chạy database và đảm bảo schema đã được tạo bằng file `src/main/resources/db/mysql-schema.sql`.
4. Run class server:

```text
com.auctionapp.auctionappjava.server.ServerLauncher
```

Server lắng nghe ở cổng:

```text
127.0.0.1:8080
```

5. Sau khi server đã chạy, run class client:

```text
com.auctionapp.auctionappjava.client.network.Launcher
```

### Cách 2: Chạy bằng Terminal

Mở Terminal 1 để chạy server:

```powershell
.\mvnw.cmd -q -DskipTests compile
.\mvnw.cmd -q exec:java -Dexec.mainClass="com.auctionapp.auctionappjava.server.ServerLauncher"
```

Giữ Terminal 1 đang chạy, sau đó mở Terminal 2 để chạy client:

```powershell
.\mvnw.cmd javafx:run
```

Trên macOS/Linux, thay `.\mvnw.cmd` bằng `./mvnw`.

Thứ tự bắt buộc:

1. Khởi động database.
2. Tạo schema bằng `src/main/resources/db/mysql-schema.sql` nếu database chưa có bảng.
3. Khởi động Server.
4. Khởi động Client.
5. Đăng ký/đăng nhập và sử dụng các chức năng đấu giá.

## 6. Danh sách chức năng đã hoàn thành

- Đăng ký tài khoản.
- Đăng nhập và đăng xuất.
- Quản lý thông tin tài khoản.
- Đổi mật khẩu.
- Nạp tiền vào ví.
- Xem danh sách phiên đấu giá.
- Xem chi tiết phiên đấu giá.
- Đăng sản phẩm/phiên đấu giá.
- Đặt giá thủ công.
- Xác nhận đặt giá.
- Cấu hình đặt giá tự động.
- Xử lý auto bid trên server.
- Gia hạn thời gian chống đặt giá vào phút cuối bằng anti-sniping.
- Cập nhật realtime từ server về client.
- Xem bảng xếp hạng đặt giá.
- Xem lịch sử đấu giá.
- Phân tích xu hướng phiên đấu giá.
- Quản lý người dùng cho admin.
- Kết nối và thao tác database qua DAO/JDBC.
- Unit test cho các thành phần chính: factory, service, strategy và utility.

## 7. Link báo cáo PDF và video demo

- Báo cáo PDF: (https://drive.google.com/file/d/1_jNA2mtZv8_UfxjIoy-nEn6t_QpdCDXk/view?usp=drive_link).
- Video demo: [YouTube](https://www.youtube.com/watch?v=V7moKR0x_To).

