package com.auctionapp.auctionappjava.client;

import com.auctionapp.auctionappjava.client.network.Client;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientLauncher extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        try {
            System.out.println("Đang thử kết nối tới Server Đấu giá...");
            // IP 127.0.0.1 dùng khi chạy server-client cùng 1 máy
            Client.getInstance().connect("127.0.0.1", 8080);
        } catch (Exception e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Lỗi kết nối");
            errorAlert.setHeaderText("Không thể kết nối đến máy chủ!");
            errorAlert.setContentText("Vui lòng kiểm tra lại mạng hoặc đảm bảo Server đang chạy.\nChi tiết lỗi: " + e.getMessage());
            errorAlert.showAndWait();
            System.err.println("Lỗi: Không thể kết nối đến Server! " + e.getMessage());
        }

        FXMLLoader fxmlLoader = new FXMLLoader(ClientLauncher.class.getResource("/com/auctionapp/auctionappjava/views/MainScreen.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 950, 650);
        stage.setTitle("Bíd88");
        stage.setScene(scene);
        stage.show();
        stage.setResizable(false);

        // thông báo tắt bằng "X"
        stage.setOnCloseRequest(event -> {
            // Ngăn chặn việc đóng cửa sổ ngay lập tức để chờ xác nhận
            event.consume();
            handleExit(stage);
        });
    }

    private void handleExit(Stage stage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận thoát");
        alert.setHeaderText("Bạn có chắc chắn muốn thoát ứng dụng không?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                stage.close();
            }
        });
    }

    @Override
    public void stop() throws Exception {
        // Gửi tín hiệu ngắt kết nối lên Server
        try {
            Client.getInstance().disconnect();
            System.out.println("Đã ngắt kết nối với máy chủ.");
        } catch (Exception e) {
            System.err.println("Lỗi khi ngắt kết nối: " + e.getMessage());
        }

        // Gọi super ở đây vì khi stage close thì Application (lớp cha của class này) cx sẽ chạy stop
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}