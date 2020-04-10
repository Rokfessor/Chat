package controller;

import com.sun.javafx.tk.Toolkit;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.ConnectionManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    public Label nameLabel;
    public VBox mainVBox;
    public VBox messagesVBox = new VBox();
    public HBox selectedImageBox = null;
    public ScrollPane scrollPane = new ScrollPane();
    public TextArea textArea = new TextArea();
    private File image = null;
    private ConnectionManager connectionManager = ConnectionManager.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nameLabel.setText(connectionManager.getAuthorizationData().getName());
        scrollPane.setContent(messagesVBox);
    }

    public void sendMessage() {
        if (textArea.getText().trim().length() > 0 || image != null) {
            try {
                connectionManager.sendMessage(textArea.getText(), image);
                messagesVBox.getChildren().add(drawMessage(textArea.getText(), image));
                messagesVBox.heightProperty().addListener(o -> scrollPane.setVvalue(1D));
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText(null);
                alert.setContentText(e.getMessage());
                alert.show();
            }
            image = null;
            deleteSelectedImageBox(false);
        }
        textArea.clear();
    }

    public VBox drawMessage(String text, File image) {
        ImageView imageView = null;
        Label label = null;
        VBox messageVBox = new VBox();
        messageVBox.getStyleClass().add("messageVBox");
        //Установка текста
        if (!textArea.getText().equals("")) {
            label = new Label(text);
            label.setWrapText(true);
            messageVBox.getChildren().add(label);
        }
        //Установка изображения
        if (image != null) {
            imageView = new ImageView(new Image(image.toURI().toString()));
            imageView.setPreserveRatio(true);
            if (imageView.getImage().getHeight() > imageView.getImage().getWidth()) {
                imageView.setFitHeight(Math.min(360, imageView.getImage().getHeight()));
            } else {
                imageView.setFitWidth(Math.min(360, imageView.getImage().getWidth()));
            }
            messageVBox.getChildren().add(imageView);
        }
        //Вычисление ширины сообщения
        double width = Math.max(label != null ?
                        Toolkit.getToolkit().getFontLoader().computeStringWidth(label.getText(), label.getFont()) : 0,
                imageView != null ? imageView.getFitWidth() : 0);
        messageVBox.setMaxWidth(Math.min(width + 10, 360));

        return messageVBox;
    }

    public void addImageToMessage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("(*.png)", "*.png"));
        image = fileChooser.showOpenDialog(new Stage());

        if (image != null) {
            deleteSelectedImageBox(true);
            selectedImageBox = new HBox(5);
            ImageView imageView = new ImageView(new Image(image.toURI().toString()));
            Button button = new Button("×");
            button.setOnMouseReleased(event -> deleteSelectedImageBox(false));
            imageView.setPreserveRatio(true);

            if (imageView.getImage().getHeight() > imageView.getImage().getWidth()) {
                imageView.setFitHeight(Math.min(100, imageView.getImage().getHeight()));
            } else {
                imageView.setFitWidth(Math.min(100, imageView.getImage().getWidth()));
            }

            selectedImageBox.getChildren().addAll(imageView, button);
            mainVBox.getChildren().add(selectedImageBox);
        }
    }

    private void deleteSelectedImageBox(boolean saveImage) {
        if (selectedImageBox != null) {
            mainVBox.getChildren().remove(selectedImageBox);
            selectedImageBox = null;
            if (!saveImage)
                image = null;
        }
    }
}
