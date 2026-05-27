package com.auctionapp.auctionappjava.client.controllers;

import static com.auctionapp.auctionappjava.common.util.MoneyUtils.purifyingText;
import static com.auctionapp.auctionappjava.common.util.MoneyUtils.settingMoneyFormat;

import com.auctionapp.auctionappjava.client.network.Client;
import com.auctionapp.auctionappjava.client.session.UserSession;
import com.auctionapp.auctionappjava.common.dto.AddItemRequest;
import com.auctionapp.auctionappjava.common.dto.Request;
import com.auctionapp.auctionappjava.common.dto.Response;
import com.auctionapp.auctionappjava.common.enums.ItemType;
import com.auctionapp.auctionappjava.common.util.AlertUtils;
import com.auctionapp.auctionappjava.common.util.CompressionUtils;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AddItemController implements Initializable {

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

  @FXML private RowConstraints extra1;
  @FXML private RowConstraints extra2;
  @FXML private ComboBox<String> cbCategory;
  @FXML private Label lblError;
  @FXML private Label lblExtraInfo1;
  @FXML private Label lblExtraInfo2;
  @FXML private TextField txtDescription;
  @FXML private TextField txtEndDate;
  @FXML private TextField txtExtraInfo1;
  @FXML private TextField txtExtraInfo2;
  @FXML private TextField txtItemName;
  @FXML private TextField txtOpenDate;
  @FXML private TextField txtStartingPrice;
  @FXML private TextField txtMinIncrement;
  @FXML private Button btnAddItem;
  @FXML private Button btnCancel;
  @FXML private ImageView imgPreview;

  private Image alertImage;
  private byte[] selectedImageData = null;
  private boolean isAddedSuccess;

  public boolean isAddedSuccess() {
    return isAddedSuccess;
  }

  @FXML
  void handleChooseImage(ActionEvent event) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Chọn ảnh sản phẩm");

    // Thiết lập lọc file
    FileChooser.ExtensionFilter imageFilter =
        new FileChooser.ExtensionFilter(
            "Các định dạng ảnh hỗ trợ (*.png, *.jpg, *.jpeg, *.gif)",
            "*.png",
            "*.jpg",
            "*.jpeg",
            "*.gif");
    // Gắn bộ lọc vào FileChooser
    fileChooser.getExtensionFilters().add(imageFilter);

    // Mở cửa sổ chọn file
    File file = fileChooser.showOpenDialog(null);
    if (file != null) {
      try {
        // Đọc file thành mảng byte
        byte[] rawImage = Files.readAllBytes(file.toPath());

        // Hiển thị Preview cho người dùng xem (dùng ảnh gốc cho nét)
        Image image = new Image(new ByteArrayInputStream(rawImage));
        imgPreview.setImage(image);

        // NÉN ẢNH LẠI để chuẩn bị gửi đi
        selectedImageData = CompressionUtils.compress(rawImage);
      } catch (Exception e) {
        AlertUtils.AnnouncementController("Lỗi", "Không thể đọc hoặc nén file ảnh!", null, null);
      }
    }
  }

  @FXML
  void handleAddItem(ActionEvent event) {
    String name = txtItemName.getText().trim();
    String description = txtDescription.getText().trim();
    String priceText = txtStartingPrice.getText().trim();
    String openDateText = txtOpenDate.getText().trim();
    String endDateText = txtEndDate.getText().trim();
    String category = cbCategory.getValue();
    String attribute1 = txtExtraInfo1.getText().trim();
    String attribute2 = txtExtraInfo2.getText().trim();
    String minIncrementText = txtMinIncrement.getText().trim();

    if (name.isEmpty()
        || priceText.isEmpty()
        || openDateText.isEmpty()
        || endDateText.isEmpty()
        || minIncrementText.isEmpty()
        || category == null) {
      lblError.setText("Vui lòng điền đầy đủ thông tin bắt buộc.");
      return;
    }

    BigDecimal startingPrice;
    BigDecimal minIncrement;

    try {
      startingPrice = purifyingText(priceText);
    } catch (NumberFormatException e) {
      lblError.setText("Giá khởi điểm phải là số dương hợp lệ.");
      return;
    }

    try {
      minIncrement = purifyingText(minIncrementText);
    } catch (NumberFormatException e) {
      lblError.setText("Bước giá phải là số dương hợp lệ.");
      return;
    }

    if (startingPrice.compareTo(BigDecimal.ZERO) <= 0) {
      lblError.setText("Giá khởi điểm phải là số dương hợp lệ.");
      return;
    }

    if (minIncrement.compareTo(BigDecimal.ZERO) <= 0) {
      lblError.setText("Bước giá phải là số dương hợp lệ.");
      return;
    }

    LocalDateTime openTime, endTime;
    try {
      openTime = LocalDateTime.parse(openDateText, DATE_FORMATTER);
      endTime = LocalDateTime.parse(endDateText, DATE_FORMATTER);
    } catch (DateTimeParseException e) {
      lblError.setText("Định dạng ngày phải là: dd/MM/yyyy HH:mm");
      return;
    }

    if (!endTime.isAfter(openTime)) {
      lblError.setText("Thời điểm kết thúc phải sau thời điểm mở.");
      return;
    }

    if (!endTime.isAfter(LocalDateTime.now())) {
      lblError.setText("Thời điểm kết thúc phải sau thời điểm hiện tại.");
      return;
    }

    if (txtDescription.getText().length() > 99) {
      lblError.setText("Quá số lượng chữ cái cho phép (99 kí tự)");
      return;
    }

    // Các thuộc tính
    String type = null;

    switch (category) {
      case "Nghệ thuật":
        type = ItemType.ART.name();
        break;
      case "Điện tử":
        type = ItemType.ELECTRONICS.name();
        break;
      case "Phương tiện":
        type = ItemType.VEHICLE.name();
        break;
    }

    AddItemRequest payload =
        new AddItemRequest(
            UserSession.getInstance().getCurrentUser().id(),
            name,
            description,
            startingPrice,
            minIncrement,
            type,
            openTime,
            endTime,
            attribute1,
            attribute2,
            selectedImageData);
    Request addItemReq = new Request("ADD_ITEM", payload);

    ImageView imageView = new ImageView(alertImage);
    imageView.setPreserveRatio(true); // Giữ nguyên tỉ lệ ảnh gốc
    imageView.setFitWidth(500); // Chỉ cần set chiều rộng, chiều cao sẽ tự nhảy theo

    Runnable mainMethod =
        () -> { // Test
          // Khóa nút ngay lập tức trước khi gọi mạng
          Platform.runLater(
              () -> {
                btnAddItem.setDisable(true);
                btnCancel.setDisable(true);
              });
          CompletableFuture.supplyAsync(
                  () -> {
                    try {
                      return Client.getInstance().sendRequest(addItemReq);
                    } catch (Exception e) {
                      return new Response(false, "Lỗi kết nối máy chủ!", null);
                    }
                  })
              .thenAccept(
                  response -> {
                    Platform.runLater(
                        () -> {
                          if (response.success()) {
                            isAddedSuccess = true;

                            Runnable closeForm =
                                () -> {
                                  Stage currentStage =
                                      (Stage) ((Node) event.getSource()).getScene().getWindow();
                                  currentStage.close();
                                };

                            AlertUtils.AnnouncementController(
                                "Thông báo", "Đã thêm sản phẩm thành công!", closeForm, imageView);

                          } else {
                            lblError.setText(response.message());
                            lblError.setVisible(true);
                            lblError.setTextFill(Color.web("#FF8A80"));

                            btnAddItem.setDisable(true);
                            btnCancel.setDisable(true);
                          }
                          btnAddItem.setDisable(false);
                          btnCancel.setDisable(false);
                        });
                  });
        };

    AlertUtils.AnnouncementController(
        "Chắc chưa?", "Bạn có chắc muốn thêm phiên đấu giá này không?", mainMethod, null);
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    settingMoneyFormat(txtStartingPrice);
    settingMoneyFormat(txtMinIncrement);

    isAddedSuccess = false;

    alertImage =
        new Image(getClass().getResourceAsStream("/com/auctionapp/auctionappjava/images/Mari.jpg"));

    String[] type = {"Chọn thể loại", "Nghệ thuật", "Điện tử", "Phương tiện"};
    cbCategory.getItems().addAll(type);
    cbCategory.setValue("Chọn thể loại");

    // Cập nhật nhãn extra info khi đổi loại
    cbCategory.setOnAction(
        e -> {
          String selected = cbCategory.getValue();
          if (selected == null) return;
          switch (selected) {
            case "Nghệ thuật":
              /*toggleExtraRows(true);*/
              lblExtraInfo1.setText("Tên nghệ sĩ:");
              txtExtraInfo1.setPromptText("Ví dụ: Nguyễn Văn A");
              lblExtraInfo2.setText("Thể loại:");
              txtExtraInfo2.setPromptText("Ví dụ: tranh vẽ, ảnh chụp,...");
              break;
            case "Điện tử":
              lblExtraInfo1.setText("Thương hiệu:");
              txtExtraInfo1.setPromptText("Ví dụ: Samsung,...");
              lblExtraInfo2.setText("Loại sản phẩm:");
              txtExtraInfo2.setPromptText("Ví dụ: smartphone, laptop,...");
              break;
            case "Phương tiện":
              lblExtraInfo1.setText("Nhà sản xuất:");
              txtExtraInfo1.setPromptText("Ví dụ: Nissan,...");
              lblExtraInfo2.setText("Ngày đăng kí:");
              txtExtraInfo2.setPromptText("Ngày đăng kí xe");
              break;

            default:
              lblExtraInfo1.setText("Thông tin thêm:");
          }
        });

    // Đổi con trỏ chuột thành hình bàn tay khi di chuột vào ảnh cho giống Web
    if (imgPreview != null) {
      imgPreview.setCursor(Cursor.HAND);

      // Bắt sự kiện Click chuột vào ảnh
      imgPreview.setOnMouseClicked(
          (MouseEvent event) -> {
            // Nếu chưa chọn ảnh thì không làm gì cả
            if (imgPreview.getImage() == null) return;

            // 3. Tạo một cửa sổ (Stage) mới để phóng to ảnh
            Stage zoomStage = new Stage();
            zoomStage.initModality(
                Modality
                    .APPLICATION_MODAL); // Khóa form ở dưới, bắt buộc xem xong mới được quay lại
            zoomStage.setTitle("Xem chi tiết ảnh");

            // 4. Tạo một ImageView mới chứa cùng bức ảnh đó nhưng to hơn
            ImageView zoomedImageView = new ImageView(imgPreview.getImage());
            zoomedImageView.setPreserveRatio(true);

            // Set kích thước tối đa để ảnh không bị tràn màn hình
            zoomedImageView.setFitWidth(800);
            zoomedImageView.setFitHeight(600);

            // 5. Bọc ảnh vào một StackPane để căn giữa
            StackPane root = new StackPane(zoomedImageView);

            // Click vào bất kỳ đâu trên cửa sổ phóng to này sẽ tự động đóng nó lại
            root.setOnMouseClicked(e -> zoomStage.close());

            // Hiển thị lên giữa màn hình
            Scene scene = new Scene(root, 900, 700);
            zoomStage.setScene(scene);
            zoomStage.centerOnScreen();
            zoomStage.showAndWait();
          });
    }
  }

  @FXML
  void handleCancel(ActionEvent event) {
    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    stage.close();
  }
}
