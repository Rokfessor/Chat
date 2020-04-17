package controller;

import Utils.Alerts;
import Utils.DataHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RowDialogController {
    public VBox vBox;
    public Dialog<Map<String, Object>> dialog;
    private Map<String, Object> data;
    private List<String> columnName;
    private List<Integer> columnType;


    public void start() {
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image("/images/add.png"));
        stage.setTitle("Row viewer");
        dialog.setResultConverter(param -> {
            if (param == ButtonType.OK) {
                for (int i = 0; i < columnType.size(); i++){
                    if (columnType.get(i) == Types.BLOB){
                        ((ImageView)data.get(columnName.get(i))).setFitWidth(200);
                        ((ImageView)data.get(columnName.get(i))).setFitHeight(200);
                    }
                }
                return data;
            }
            return null;
        });
        setTextAreas();
    }

    public void setData(Map<String, Object> data, List<String> columnName, List<Integer> columnType){
        this.data = data;
        this.columnName = columnName;
        this.columnType = columnType;
        start();
    }

    private Object getDataAsObject(int type, String data) throws ParseException, IOException {
        switch (type) {
            case Types.TIMESTAMP:
                return new Timestamp(DataHandler.parseDate(data, "yyyy-MM-dd hh:mm:ss.SSS").getTimeInMillis());
            case Types.NUMERIC:
                return Integer.parseInt(data);
            case Types.VARCHAR:
                return data;
            case Types.BLOB:
                return DataHandler.loadImage(data);
        }
        return new Object();
    }

    public void setTextAreas() {
        for (int i = 0; i < data.size(); i++) {
            int currentColumnType = columnType.get(i);
            String currentColumnName = columnName.get(i);
            if (columnType.get(i) != Types.BLOB) {
                TextField textField = new TextField();
                textField.setPromptText(currentColumnName);

                if (data.get(currentColumnName) != null) {
                    textField.setText(data.get(currentColumnName).toString());
                }

                textField.setOnKeyReleased(event -> {
                    try {
                        data.replace(currentColumnName, getDataAsObject(currentColumnType,
                                textField.getText()));
                    } catch (ParseException | IOException ignored) {}
                });

                vBox.getChildren().add(textField);
            } else {
                VBox imagePane = new VBox();
                imagePane.setAlignment(Pos.CENTER);
                imagePane.setSpacing(5);
                imagePane.setPrefHeight(500);
                ImageView tmp = (ImageView) data.get(currentColumnName);
                if (tmp == null)
                    tmp = new ImageView();
                ImageView image = new ImageView(tmp.getImage());
                image.setPreserveRatio(true);
                if (image.getImage() != null) {
                    image.setFitHeight(400);
                    image.setFitWidth(400);
                }
                imagePane.getChildren().add(image);

                HBox buttons = new HBox();
                buttons.setSpacing(5);
                buttons.setAlignment(Pos.CENTER);

                Button add = new Button("Set new image");
                add.setOnMouseReleased(e ->{
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));
                    File file = fileChooser.showOpenDialog(new Stage());
                    try {
                        image.setImage(Objects.requireNonNull(DataHandler.loadImage(file.getPath())).getImage());
                        image.setFitHeight(400);
                        image.setFitWidth(400);
                        dialog.getDialogPane().getScene().getWindow().setWidth(vBox.getWidth());
                        data.replace(currentColumnName, image);

                    } catch (IOException ex) {
                        Alerts.showErrorAlert(ex.getMessage());
                    }
                });

                Button delete = new Button("Delete Image");
                delete.setOnMouseReleased(e -> {
                    image.setFitHeight(0);
                    image.setFitWidth(0);
                    image.setImage(null);
                    data.replace(currentColumnName, image);
                });

                buttons.getChildren().addAll(add, delete);
                imagePane.getChildren().add(buttons);
                vBox.getChildren().add(imagePane);
            }
        }
    }
}
