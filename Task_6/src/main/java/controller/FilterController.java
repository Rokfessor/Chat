package controller;

import Utils.FilterData;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class FilterController {
    public Dialog<List<FilterData>> dialog;
    public VBox vBox;
    private List<String> names;
    private List<Integer> types;
    private final List<FilterData> filters = new ArrayList<>();

    public void setData(List<String> names, List<Integer> types) {
        this.names = names;
        this.types = types;
        start();
    }

    public void start() {
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image("/images/filter.png"));
        stage.setTitle("Filter");

        for (int i = 0; i < types.size(); i++) {
            int type = types.get(i);
            String name = names.get(i);
            FilterData filterData = new FilterData();
            HBox hbox = new HBox(5);
            TextField textField = new TextField();
            CheckBox checkBox = new CheckBox();
            ComboBox<String> comboBox = new ComboBox<>();
            ObservableList<String> list;

            switch (type) {
                case Types.NUMERIC:
                case Types.BLOB:
                case Types.TIMESTAMP:
                    list = FXCollections.observableArrayList("=", "!=", ">", "<", ">=", "<=");
                    comboBox.setValue("=");
                    filterData.setSign("=");
                    break;
                case Types.VARCHAR:
                    list = FXCollections.observableArrayList("=", "Contains", "!=");
                    comboBox.setValue("=");
                    filterData.setSign("=");
                    break;

                default:
                    throw new IllegalStateException("Unexpected value: " + type);
            }

            filterData.setShow(true);
            checkBox.selectedProperty().setValue(true);
            checkBox.setOnAction(e -> filterData.setShow(checkBox.selectedProperty().get()));

            comboBox.setItems(list);
            comboBox.setOnAction(e -> filterData.setSign(comboBox.getValue()));

            textField.setPromptText(name);
            textField.setOnKeyReleased(e -> filterData.setFilter(textField.getText()));

            filterData.setColumnName(name);
            filterData.setColumnType(type);

            filters.add(filterData);

            hbox.getChildren().addAll(checkBox, comboBox, textField);
            vBox.getChildren().add(hbox);
        }
        dialog.setResultConverter(c -> filters);
    }
}