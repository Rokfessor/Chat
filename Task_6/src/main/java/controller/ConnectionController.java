package controller;

import javafx.fxml.Initializable;
import javafx.scene.control.Dialog;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import model.DBLoginData;

import java.net.URL;
import java.util.ResourceBundle;

public class ConnectionController implements Initializable {
    public TextField addressField;
    public TextField nameField;
    public PasswordField passwordField;
    public Dialog<DBLoginData> dialog;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image("/images/connection.png"));
        dialog.setResultConverter(param ->
            new DBLoginData(nameField.getText(), passwordField.getText(), addressField.getText()));
    }
}
