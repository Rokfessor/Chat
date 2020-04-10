import Utils.AuthorizationData;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import model.ConnectionManager;

import java.io.IOException;
import java.util.Optional;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) {
        try {
            //попытка коннекта
            Dialog<AuthorizationData> dialog = FXMLLoader.load(getClass().getResource("/view/fxml/AuthorizationView.fxml"));
            Optional<AuthorizationData> op = dialog.showAndWait();
            AuthorizationData authorizationData = null;
            ConnectionManager connectionManager;

            if (op.isPresent()){
                authorizationData = op.get();
            }

            if (authorizationData != null) {
                connectionManager = ConnectionManager.getInstance();
                try {
                    connectionManager.createConnection(authorizationData);

                    //создание основного окна
                    Parent root = FXMLLoader.load(getClass().getResource("view/fxml/MainView.fxml"));
                    Scene scene = new Scene(root);

                    scene.getStylesheets().add((getClass().getResource("view/css/styles.css")).toExternalForm());
                    stage.setScene(scene);
                    stage.setTitle("Client");
                    stage.getIcons().add(new Image("/images/MainIcon.png"));
                    stage.setResizable(false);
                    stage.show();

                } catch (IOException | NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText(null);
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                }
            } else{
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText(null);
                alert.setContentText("No authorization data");
                alert.showAndWait();
            }
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }


    }
}
