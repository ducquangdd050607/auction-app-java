package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    private Stage stage;
    private Parent root;
    private Scene scene;

    @FXML
    private Label lblError;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private TextField txtPrivateKey;

    @FXML
    private TextField txtUsername;

    @FXML
    void handleConfirm(ActionEvent event) throws IOException {
        if (txtUsername.getText().isEmpty() || txtPassword.getText().isEmpty()) {
            lblError.setText("Hãy điền đủ thông tin");
            lblError.setVisible(true);
        } else {
            isLoggedIn = true;
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/MainScreen.fxml"));
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        }
    }

    @FXML
    void handleRegister(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/RegisterScreen.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

}
