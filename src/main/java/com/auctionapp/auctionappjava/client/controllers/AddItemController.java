package com.auctionapp.auctionappjava.client.controllers;

import static com.auctionapp.auctionappjava.common.util.MoneyUtils.purifyingText;
import static com.auctionapp.auctionappjava.common.util.MoneyUtils.settingMoneyFormat;
import static com.auctionapp.auctionappjava.common.util.ValidationUtils.*;

import com.auctionapp.auctionappjava.client.network.Client;
import com.auctionapp.auctionappjava.client.session.UserSession;
import com.auctionapp.auctionappjava.common.dto.AddItemRequest;
import com.auctionapp.auctionappjava.common.dto.Request;
import com.auctionapp.auctionappjava.common.dto.Response;
import com.auctionapp.auctionappjava.common.enums.ItemType;
import com.auctionapp.auctionappjava.common.exception.ValidationException;
import com.auctionapp.auctionappjava.common.util.AlertUtils;
import com.auctionapp.auctionappjava.common.util.CompressionUtils;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AddItemController implements Initializable {

  @FXML private ComboBox<String> cbCategory;
  @FXML private Label lblError;
  @FXML private Label lblExtraInfo1;
  @FXML private Label lblExtraInfo2;
  @FXML private TextField txtDescription;
  @FXML private TextField txtExtraInfo1;
  @FXML private TextField txtExtraInfo2;
  @FXML private TextField txtItemName;
  @FXML private TextField txtStartingPrice;
  @FXML private TextField txtMinIncrement;
  @FXML private Button btnAddItem;
  @FXML private Button btnCancel;
  @FXML private ImageView imgPreview;
  @FXML private Spinner<Integer> cbHoursEnd;
  @FXML private Spinner<Integer> cbHoursStart;
  @FXML private Spinner<Integer> cbMinutesEnd;
  @FXML private Spinner<Integer> cbMinutesStart;
  @FXML private DatePicker datePickerEnd;
  @FXML private DatePicker datePickerStart;

  private Image alertImage;
  private byte[] selectedImageData = null;
  private boolean isAddedSuccess;

  public boolean isAddedSuccess() {
    return isAddedSuccess;
  }

  @FXML
  void handleChooseImage() {
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
    String category = cbCategory.getValue();
    String attribute1 = txtExtraInfo1.getText().trim();
    String attribute2 = txtExtraInfo2.getText().trim();
    String minIncrementText = txtMinIncrement.getText().trim();
    BigDecimal startingPrice;
    BigDecimal minIncrement;
    LocalDate startDate = datePickerStart.getValue();
    LocalTime startTime = LocalTime.of(cbHoursStart.getValue(), cbMinutesStart.getValue());
    LocalDateTime openTime = LocalDateTime.of(startDate, startTime);

    LocalDate endDate = datePickerEnd.getValue();
    LocalTime endTimeOfDate = LocalTime.of(cbHoursEnd.getValue(), cbMinutesEnd.getValue());
    LocalDateTime endTime = LocalDateTime.of(endDate, endTimeOfDate);

    try {
      // 1. Kiểm tra các trường văn bản bắt buộc không được bỏ trống
      requireText(name, "Tên sản phẩm");
      requireText(description, "Mô tả");
      requireText(priceText, "Giá bắt đầu");
      requireText(minIncrementText, "Bước giá");

      if (category == null || "Chọn thể loại".equals(category)) {
        throw new ValidationException("Thể loại không được bỏ trống");
      }

      // 2. Định dạng số từ chuỗi nhập vào
      try {
        startingPrice = purifyingText(priceText);
      } catch (NumberFormatException e) {
        throw new ValidationException("Giá khởi điểm phải là số hợp lệ");
      }

      try {
        minIncrement = purifyingText(minIncrementText);
      } catch (NumberFormatException e) {
        throw new ValidationException("Bước giá phải là số hợp lệ");
      }

      // 3. Kiểm tra số dương (> 0) bằng utils
      requirePositive(startingPrice, "Giá khởi điểm");
      requirePositive(minIncrement, "Bước giá");

      // 4. Kiểm tra logic khoảng thời gian bằng utils
      requireTimeRange(openTime, endTime);

      if (!endTime.isAfter(LocalDateTime.now())) {
        throw new ValidationException("Thời điểm kết thúc phải sau thời điểm hiện tại");
      }

      // 5. Kiểm tra giới hạn độ dài ký tự của mô tả (Tối đa 90 ký tự)
      if (description.length() > 90) {
        requireUnderMaximumLetters(description);
      }

    } catch (ValidationException e) {
      lblError.setText(e.getMessage());
      lblError.setVisible(true);
      lblError.setTextFill(Color.web("#FF8A80"));
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

    cbHoursStart.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 12));
    cbHoursEnd.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 12));

    cbMinutesStart.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
    cbMinutesEnd.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));

    setupSpinnerProperties(cbHoursStart, 23);
    setupSpinnerProperties(cbHoursEnd, 23);
    setupSpinnerProperties(cbMinutesStart, 59);
    setupSpinnerProperties(cbMinutesEnd, 59);

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

  private void setupSpinnerProperties(Spinner<Integer> spinner, int maxLimit) {
    // 1. Cho phép người dùng chỉnh sửa nhập text trực tiếp từ bàn phím
    spinner.setEditable(true);

    // 2. Chặn chữ
    TextField editor = spinner.getEditor();
    TextFormatter<String> numericFilter =
        new TextFormatter<>(
            change -> {
              if (!change.isContentChange()) {
                return change;
              }

              // Ký tự vừa gõ/paste vào
              String addedText = change.getText();

              // Nếu chứa ký tự không phải là số -> Từ chối lập tức (chữ không thể hiện lên màn
              // hình)
              if (!addedText.isEmpty() && !addedText.matches("\\d+")) {
                return null;
              }

              // Giới hạn số lượng ký tự nhập vào tối đa là 2
              String newText = change.getControlNewText();
              if (newText.length() > 2) {
                return null;
              }

              return change;
            });
    editor.setTextFormatter(numericFilter);
  }
}
