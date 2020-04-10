package controller;

import Utils.AuthorizationData;
import javafx.fxml.Initializable;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class AuthorizationController implements Initializable {
    public Dialog<AuthorizationData> dialog;
    public TextField serverIpField;
    public TextField serverPortField;
    public TextField userNameField;
    public DialogPane dialogPane;

    public void initialize(URL location, ResourceBundle resources) {
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image("/images/AuthorizationIcon.png"));
        stage.setTitle("Authorization");
        dialog.getDialogPane().getStylesheets().add("view/css/styles.css");

        dialog.setResultConverter(param -> {
            if (!serverIpField.getText().equals("") && !serverPortField.getText().equals("")
                    && !userNameField.getText().equals(""))
                return new AuthorizationData(userNameField.getText(), serverIpField.getText(), serverPortField.getText());
            else
                return null;
        });
    }
}
