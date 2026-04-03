package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    private Stage stage;
    private Parent root;
    private Scene scene;

    @FXML
    private Button btnUser;
    @FXML
    private Button btnLogin;
    @FXML
    private Button btnManageProducts;
    @FXML
    private Button btnRegister;
    @FXML
    private HBox guestNavBox;
    @FXML
    private Label lblUserAvatar;
    @FXML
    private StackPane userNavBox;
    @FXML
    private VBox navigator;


    @FXML
    void handleAboutUs(ActionEvent event) {

    }

    @FXML
    void handleLogin(ActionEvent event) throws IOException {

        Parent root = FXMLLoader.load(getClass().getResource("/fxml/LoginScreen.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void handleManageProducts(ActionEvent event) {

    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        updateLoginStatus(LoginController.isLoggedIn);
    }

    @FXML
    public void updateLoginStatus(boolean isLoggedIn) {
        if (isLoggedIn) {
            // Làm nút Login biến mất hoàn toàn
            btnLogin.setVisible(false);
            btnLogin.setManaged(false);

        } else {
            // Trạng thái chưa đăng nhập
            btnLogin.setVisible(true);
            btnLogin.setManaged(true);
        }
    }

}
