# Báo cáo lỗi CSS/UI Style

File này ghi lại các lỗi và rủi ro CSS/UI style đã tìm thấy khi quét project ở chế độ READ-ONLY.

## Tổng hợp nhanh

| Mức độ | Số lỗi |
|---|---:|
| High | 2 |
| Medium | 7 |
| Low | 5 |

## Chi tiết lỗi

### 1. File CSS không được load

1. File: `src/main/resources/com/auctionapp/auctionappjava/css/Route.css`
2. Vấn đề: File CSS có khai báo `.confirm-btn` nhưng không thấy FXML hoặc Java nào load file này.
3. Mức độ: Low
4. Giải thích: `Route.css` đang là stylesheet chết, dễ gây nhầm khi sửa style button.
5. Hậu quả có thể xảy ra: Sửa style trong file này sẽ không có tác dụng trên UI.
6. Cách sửa đề xuất: Xóa nếu không dùng, hoặc load đúng vào màn hình cần style route/confirm button.

### 2. Màn Confirm Bidding không load CSS riêng

1. File: `src/main/resources/com/auctionapp/auctionappjava/views/ConfirmBiddingScreen.fxml`
2. Vấn đề: Root `BorderPane` không có thuộc tính `stylesheets`, gần như toàn bộ style nằm inline.
3. Mức độ: Medium
4. Giải thích: Các button, label, panel trong màn đặt cược không dùng stylesheet chung.
5. Hậu quả có thể xảy ra: Hover/pressed/disabled state thiếu nhất quán; khó chỉnh style tập trung.
6. Cách sửa đề xuất: Tạo hoặc tái dùng CSS screen-level, chuyển các style lặp sang class như `confirm-btn`, `preset-btn`, `link-btn`.

### 3. Màn Users Manager không load CSS riêng

1. File: `src/main/resources/com/auctionapp/auctionappjava/views/UsersManagerScreen.fxml`
2. Vấn đề: Root `BorderPane` không load stylesheet, nhiều button dùng inline style.
3. Mức độ: Medium
4. Giải thích: Các nút `Tìm`, `Chặn người dùng`, `Xác nhận`, `Hủy bỏ` không có hover/pressed/disabled style từ CSS.
5. Hậu quả có thể xảy ra: UX thiếu phản hồi khi hover/click, disabled state không rõ.
6. Cách sửa đề xuất: Gắn stylesheet phù hợp và đưa style button/table/header vào class CSS.

### 4. CSS inline quá nhiều

1. File: nhiều file FXML
2. Vấn đề: Inline style xuất hiện dày đặc.
3. Mức độ: Medium
4. Giải thích: Số lượng inline style nổi bật: `DashboardScreen.fxml` 63, `AddItemScreen.fxml` 23, `ConfirmBiddingScreen.fxml` 21, `AuctionDetailScreen.fxml` 20, `RankingListScreen.fxml` 17, `RegisterScreen.fxml` 17.
5. Hậu quả có thể xảy ra: Style khó bảo trì, khó đồng bộ dark/light contrast, hover, padding, radius.
6. Cách sửa đề xuất: Chuyển style lặp sang CSS class và dùng một file common/theme CSS cho token màu, font, radius.

### 5. Duplicate `.confirm-btn`

1. File: nhiều file CSS
2. Vấn đề: `.confirm-btn` được khai báo lặp ở nhiều stylesheet.
3. Mức độ: Medium
4. Giải thích: Class này có trong `AddItemScreen.css`, `AuctionListAndDetailScreen.css`, `ChangeInformation.css`, `DepositScreen.css`, `LoginAndRegisterScreen.css`, `RankingListScreen.css`, `Route.css`, `WalletAndChangePasswordScreen.css`.
5. Hậu quả có thể xảy ra: Cùng tên class nhưng màu, radius, font-size khác nhau theo màn; sửa một nơi không áp dụng toàn app.
6. Cách sửa đề xuất: Tách `.confirm-btn` chung vào CSS base, nếu cần biến thể thì đặt tên rõ như `confirm-btn-primary`, `confirm-btn-light`, `confirm-btn-yellow`.

### 6. Duplicate `.underline-link`, `.cancel-btn`, `.table-view`

1. File: nhiều file CSS
2. Vấn đề: Các selector dùng chung bị khai báo lặp.
3. Mức độ: Low
4. Giải thích: `.underline-link` lặp ở nhiều CSS; `.cancel-btn` lặp ở `AddItemScreen.css` và `DepositScreen.css`; selector `.table-view .table-cell` lặp ở list/ranking CSS.
5. Hậu quả có thể xảy ra: Link/button/table cell style không thống nhất giữa các màn.
6. Cách sửa đề xuất: Gom các selector dùng chung vào stylesheet base, giữ CSS màn hình chỉ cho layout/biến thể đặc thù.

### 7. CSS inline sai cú pháp

1. File: `src/main/resources/com/auctionapp/auctionappjava/views/UsersManagerScreen.fxml:42`
2. Vấn đề: Button tooltip dùng `style="-fx-border-style: circle;"`.
3. Mức độ: Low
4. Giải thích: `-fx-border-style` không nhận giá trị `circle`.
5. Hậu quả có thể xảy ra: JavaFX bỏ qua rule này, button `?` không được bo/trang trí như mong muốn.
6. Cách sửa đề xuất: Dùng `-fx-background-radius`, `-fx-border-radius`, `-fx-border-color`, `-fx-border-width`.

### 8. Disabled button nhìn giống enabled

1. File: `src/main/resources/com/auctionapp/auctionappjava/css/Navigator.css:59`
2. Vấn đề: `.route-btn:disabled` đặt `-fx-opacity: 1.0`.
3. Mức độ: Medium
4. Giải thích: Nút `identity` bị disable trong `NavigatorController`, nhưng CSS giữ opacity như enabled.
5. Hậu quả có thể xảy ra: Người dùng tưởng nút vẫn bấm được.
6. Cách sửa đề xuất: Dùng màu nền/text khác hoặc opacity thấp hơn cho disabled state, nhưng vẫn đảm bảo contrast.

### 9. Button/menu quá nhỏ do kích thước FXML

1. File: `src/main/resources/com/auctionapp/auctionappjava/views/NavigatorButtons.fxml:37`, `:43`, `:85`
2. Vấn đề: `btnHistory prefHeight="0.0"`, `btnItemListAdmin prefHeight="6.0"`, notifier `prefWidth="11.0"`.
3. Mức độ: High
4. Giải thích: Dù CSS class có tồn tại, kích thước pref quá nhỏ làm style không cứu được layout.
5. Hậu quả có thể xảy ra: Text bị cắt, vùng click nhỏ, nút khó thao tác.
6. Cách sửa đề xuất: Dùng kích thước thống nhất cho nav button, bỏ prefHeight quá nhỏ, dùng `minHeight` nếu cần.

### 10. Button quá rộng so với form

1. File: `src/main/resources/com/auctionapp/auctionappjava/views/ChangeInformationScreen.fxml:62`
2. Vấn đề: Button `Xác nhận` có `prefWidth="617.0"` trong form root `prefWidth="393.0"`.
3. Mức độ: Medium
4. Giải thích: Width cố định vượt quá không gian thực của form.
5. Hậu quả có thể xảy ra: Button bị tràn hoặc bị clip trên một số kích thước cửa sổ.
6. Cách sửa đề xuất: Bỏ width cố định quá lớn, dùng `maxWidth="Infinity"` hoặc layout constraints.

### 11. Layout date/time dễ overflow

1. File: `src/main/resources/com/auctionapp/auctionappjava/views/AddItemScreen.fxml:66`, `:74`
2. Vấn đề: HBox `prefWidth="200.0"` chứa DatePicker `prefWidth="240.0"` cộng thêm Spinner và spacing.
3. Mức độ: Medium
4. Giải thích: Tổng width con lớn hơn width cha.
5. Hậu quả có thể xảy ra: Component bị chen, cắt hoặc lệch layout.
6. Cách sửa đề xuất: Tăng column width, dùng GridPane riêng cho date/time hoặc cho HBox co giãn hợp lý.

### 12. Dashboard load stylesheet trùng

1. File: `src/main/resources/com/auctionapp/auctionappjava/views/DashboardScreen.fxml:15`, `:17`
2. Vấn đề: `Navigator.css` được load ở root `BorderPane` và thêm lần nữa ở `VBox` con.
3. Mức độ: Low
4. Giải thích: Stylesheet bị khai báo trùng trong cùng một màn.
5. Hậu quả có thể xảy ra: Tăng chi phí apply CSS không cần thiết, khó hiểu nguồn style.
6. Cách sửa đề xuất: Chỉ load stylesheet ở root màn hình.

### 13. Border radius không đồng bộ

1. File: nhiều file CSS/FXML
2. Vấn đề: Radius của button/card không nhất quán: `.confirm-btn` có nơi `20px`, có nơi `6px`; inline style có `4`, `6`, `8`, `10`, `12`, `15`, `18`.
3. Mức độ: Low
4. Giải thích: Các màn nhìn không cùng một design system.
5. Hậu quả có thể xảy ra: UI thiếu đồng bộ, khó nhận diện primary/secondary action.
6. Cách sửa đề xuất: Chọn bộ radius chuẩn, ví dụ input `4`, button `6`, card `8`, rồi áp dụng qua CSS class.

### 14. Inline style ghi đè styleClass

1. File: `src/main/resources/com/auctionapp/auctionappjava/views/AddItemScreen.fxml:106`, `:107`
2. Vấn đề: Button vừa có `styleClass` vừa có inline style đầy đủ màu/font/radius.
3. Mức độ: Low
4. Giải thích: Inline style có độ ưu tiên cao, khiến class CSS khó phát huy tác dụng.
5. Hậu quả có thể xảy ra: Hover/pressed hoặc chỉnh class CSS không phản ánh đúng trên button.
6. Cách sửa đề xuất: Giữ style trong CSS class, chỉ dùng inline cho giá trị thật sự đặc biệt.

## Ghi chú kiểm tra

- Không phát hiện `styleClass` được dùng trong FXML/Java mà thiếu khai báo CSS tương ứng.
- Các selector CSS nhìn chung parse được, ngoại trừ inline rule sai `-fx-border-style: circle`.
- Một số class như `.chart-title`, `.axis`, `.chatbot-bot-message`, `.chatbot-user-message` không xuất hiện trực tiếp trong FXML vì có thể được JavaFX hoặc code Java gắn runtime; không kết luận là lỗi chắc chắn.
