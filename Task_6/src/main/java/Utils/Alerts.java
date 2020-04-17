package Utils;

import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Alerts {
    private Alerts(){}

    public static void showErrorAlert (String text){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(text);
        ((Stage)alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image("images/error.png"));
        alert.showAndWait();
    }
    public static void showWarningAlert (String text){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }
}
