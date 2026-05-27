package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.client.network.Client;
import com.auctionapp.auctionappjava.common.dto.*;
import com.auctionapp.auctionappjava.common.exception.AppException;
import com.auctionapp.auctionappjava.common.util.AlertUtils;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

// Giải thích:
// HOẠT ĐỘNG(ACTIVE): acc còn khỏe, còn chơi được
// CHẶN(BAN): acc cấm đăng nhập

public class UsersManagerController implements Initializable {

  // để confirm thực hiện đúng mục đích
  private boolean ban = false;

  private ObservableList<UserDetailResponse> usersData = FXCollections.observableArrayList();

  // Thêm danh sách bọc ngoài dùng để LỌC (FilteredList)
  private FilteredList<UserDetailResponse> filteredData;

  @FXML private HBox box;
  @FXML private Button btnBan;
  @FXML private Button btnCancel;
  @FXML private Button btnConfirm;
  @FXML private ComboBox<String> cbFilterAccountStatus;
  @FXML private ComboBox<String> cbFilterRoute;
  @FXML private TableView<UserDetailResponse> listUsers;
  @FXML private TableColumn<UserDetailResponse, Boolean> clmAccountStatus;
  @FXML private TableColumn<UserDetailResponse, BigDecimal> clmBalance;
  @FXML private TableColumn<UserDetailResponse, Integer> clmBids;
  @FXML private TableColumn<UserDetailResponse, String> clmName;
  @FXML private TableColumn<UserDetailResponse, String> clmRecentBid;
  @FXML private TableColumn<UserDetailResponse, String> clmRoute;
  @FXML private TextField txtSearch;
  @FXML private Button btnSearch;
  @FXML private Button btnReload;

  @FXML
  void handleBan(ActionEvent event) {
    orConfirm(true);
    ban = true;
  }

  @FXML
  void handleConfirm(ActionEvent event) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Chắc chưa?");
    alert.setHeaderText("Bạn có chắc chắn không?");

    alert
        .showAndWait()
        .ifPresent(
            response -> {
              if (response == ButtonType.OK) {
                // if (admin == true)...
                alert.close();

                btnConfirm.setVisible(false);
                btnConfirm.setManaged(false);
              } else { // xử lý hủy(chắc chỉ thế này)
                alert.close();
              }
            });
    orConfirm(false);
  }

  @FXML
  void handleCancel(ActionEvent event) {
    orConfirm(false);
    ban = false;
  }

  @FXML
  void handleSearch(ActionEvent event) {
    // Lấy điều kiện lọc
    String keyword = txtSearch.getText() == null ? "" : txtSearch.getText().toLowerCase().trim();
    String selectedStatus = cbFilterRoute.getValue();
    String selectedRole = cbFilterAccountStatus.getValue();

    // Cập nhật điều kiện lọc cho FilteredList
    filteredData.setPredicate(
        user -> {
          // 1. Khớp Tên
          boolean matchName =
              keyword.isEmpty()
                  || (user.fullName() != null && user.fullName().toLowerCase().contains(keyword));

          // 2. Khớp Trạng thái (Cần map tiếng Việt với Enum AuctionStatus của bạn)
          boolean matchStatus = false;
          if (selectedStatus == null || selectedStatus.equals("Tất cả trạng thái")) {
            matchStatus = true;
          } else {
            // Nếu accStatus == true -> HOẠT ĐỘNG, false -> CHẶN
            String translatedStatus = user.accStatus() ? "HOẠT ĐỘNG" : "CHẶN";
            matchStatus = translatedStatus.equals(selectedStatus);
          }

          // Điều kiện 3: Khớp Vai trò (Role)
          boolean matchRole = false;
          if (selectedRole == null || selectedRole.equals("Tất cả vai trò")) {
            matchRole = true;
          } else {
            matchRole = user.role() != null && user.role().equalsIgnoreCase(selectedRole);
          }

          // Dòng nào thỏa mãn cả 3 điều kiện thì mới được hiện lên bảng
          return matchName && matchStatus && matchRole;
        });
  }

  @FXML
  void handleReload(ActionEvent event) throws IOException {}

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    String[] status = {"Tất cả trạng thái", "HOẠT DỘNG", "CHẶN"}; // trạng thái tài khoản
    cbFilterAccountStatus.getItems().addAll(status);
    cbFilterAccountStatus.setValue("Tất cả trạng thái");

    String[] routes = {"Tất cả vai trò", "BIDDER", "ADMIN", "SELLER"}; // route
    cbFilterRoute.getItems().addAll(routes);
    cbFilterRoute.setValue("Tất cả vai trò");

    setupColumns();

    filteredData = new FilteredList<>(usersData, p -> true); // Mặc định hiển thị tất cả
    javafx.collections.transformation.SortedList<UserDetailResponse> sortedData =
        new javafx.collections.transformation.SortedList<>(filteredData);
    sortedData.comparatorProperty().bind(listUsers.comparatorProperty());

    // Nhét sortedData vào bảng
    listUsers.setItems(sortedData);

    loadUserFromServer();

    try {
      show();
    } catch (IOException e) {
      throw new AppException("Không thể khởi tạo màn hình quản lý người dùng", e);
    }

    setupRowDoubleClick();
  }

  public void orConfirm(boolean choose) {
    // nút xác nhận-hủy-khung chọn chỉ khi bấm remove/ban/promoteAdmin
    btnConfirm.setVisible(choose);
    btnConfirm.setManaged(choose);
    btnCancel.setVisible(choose);
    btnCancel.setManaged(choose);
    btnBan.setManaged(!choose);
    btnBan.setVisible(!choose);
  }

  public void show() throws IOException {
    orConfirm(false);
  }

  private void loadUserFromServer() {
    // Khóa search trong lúc load data
    txtSearch.setDisable(true);
    cbFilterRoute.setDisable(true);
    cbFilterAccountStatus.setDisable(true);
    btnSearch.setDisable(true);
    btnReload.setDisable(true);

    ProgressIndicator loadingSpinner = new ProgressIndicator();
    loadingSpinner.setMaxSize(50, 50);
    listUsers.setPlaceholder(loadingSpinner);
    usersData.clear();

    Request req = new Request("GET_USERS", null);

    CompletableFuture.supplyAsync(
            () -> {
              try {
                return Client.getInstance().sendRequest(req);
              } catch (Exception e) {
                e.printStackTrace();
                return new Response(false, "Lỗi kết nối Server", null);
              }
            })
        .thenAccept(
            response -> {
              Platform.runLater(
                  () -> {
                    // Mở lại search
                    txtSearch.setDisable(false);
                    cbFilterRoute.setDisable(false);
                    cbFilterAccountStatus.setDisable(false);
                    btnSearch.setDisable(false);
                    btnReload.setDisable(false);

                    if (response.success()) {
                      List<UserDetailResponse> listFromServer =
                          (List<UserDetailResponse>) response.data();

                      usersData.setAll(listFromServer);
                    } else {
                      Alert alert = new Alert(Alert.AlertType.ERROR, response.message());
                      alert.show();
                    }
                  });
            });
  }

  private void setUserStatusFromServer(UserDetailResponse userDetailResponse) {

    ManagerAndHistoryRequest banReq = new ManagerAndHistoryRequest(userDetailResponse.userId());
    Request req = new Request("DECIDE_STATUS", banReq);
    CompletableFuture.supplyAsync(
            () -> {
              try {
                return Client.getInstance().sendRequest(req);

              } catch (Exception e) {
                e.printStackTrace();
                return new Response(false, "Lỗi kết nối Server", null);
              }
            })
        .thenAccept(
            response -> {
              Platform.runLater(
                  () -> {
                    if (response.success()) {
                      // Phần này chịu trách nhiệm hot-switch status
                      for (int i = 0; i < usersData.size(); i++) {
                        UserDetailResponse user = usersData.get(i);

                        boolean status = user.accStatus();

                        if (user.userId().equals(userDetailResponse.userId())) {

                          UserDetailResponse updatedUser =
                              new UserDetailResponse(
                                  user.userId(),
                                  user.latestBid(),
                                  user.fullName(),
                                  user.role(),
                                  user.balance(),
                                  !status, // Đổi isActive thành cái gì đó ở đây
                                  user.bids());

                          // Cập nhật lại vào ObservableList tại vị trí cũ
                          usersData.set(i, updatedUser);
                          break; // Tìm thấy rồi thì thoát vòng lặp
                        }
                      }
                    } else {
                      Alert alert = new Alert(Alert.AlertType.ERROR, response.message());
                      alert.show();
                    }

                    if (usersData.isEmpty()) {
                      Label noDataLabel = new Label("Hiện tại chưa có user nào.");
                      noDataLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");
                      listUsers.setPlaceholder(noDataLabel);
                    }
                  });
            });
  }

  private void setupRowDoubleClick() {
    listUsers.setRowFactory(
        tv -> {
          TableRow<UserDetailResponse> row = new TableRow<>();

          row.setOnMouseClicked(
              event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {

                  if (ban) {

                    Runnable finalWarning =
                        () -> {
                          setUserStatusFromServer(row.getItem());
                        };

                    if (row.getItem().accStatus()) {
                      if (row.getItem().role().equals("ADMIN")) {
                        AlertUtils.AnnouncementController(
                            "KHÔNG ĐƯỢC CHẶN ADMIN", "ADMIN KHÔNG THỂ BỊ CHẶN", null, null);
                      } else {
                        AlertUtils.ConfirmAlertController(
                            null,
                            "CẢNH BÁO!",
                            "NGƯỜI DÙNG NÀY SẼ BỊ CHẶN",
                            "BẠN CÓ MUỐN KHÔNG?",
                            "ĐÃ XONG",
                            "NGƯỜI DÙNG NÀY ĐÃ BỊ CHẶN",
                            "",
                            finalWarning,
                            null);
                      }
                    } else {
                      AlertUtils.ConfirmAlertController(
                          null,
                          "CẢNH BÁO!",
                          "NGƯỜI DÙNG NÀY SẼ ĐƯỢC GỠ CHẶN",
                          "BẠN CÓ MUỐN KHÔNG?",
                          "ĐÃ XONG",
                          "NGƯỜI DÙNG NÀY ĐÃ ĐƯỢC GỠ CHẶN",
                          "",
                          finalWarning,
                          null);
                    }
                  } else {
                    System.out.println("Not ban");
                  }
                }
              });
          return row;
        });
  }

  private void setupColumns() {
    clmAccountStatus.setCellValueFactory(
        cell -> new SimpleObjectProperty<>(cell.getValue().accStatus()));
    clmName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().fullName()));
    clmRoute.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().role()));
    clmBids.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().bids()));
    clmBalance.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().balance()));
    clmRecentBid.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().latestBid()));
  }
}
