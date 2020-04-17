import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("view/MainView.fxml"));

        stage.setScene(new Scene(root, 1280, 720));
        stage.setTitle("DB Viewer");
        stage.getIcons().add(new Image("images/database.png"));
        stage.setResizable(true);
        stage.show();
    }
}
