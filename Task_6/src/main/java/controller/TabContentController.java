package controller;

import Utils.Alerts;
import Utils.FilterData;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.DBHandler;
import model.Table;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TabContentController {
    public TableView<Map<String, Object>> tableView;
    public ObservableList<Map<String, Object>> items;
    Table table = null;
    private DBHandler database = null;

    public void initialize(DBHandler dbHandler, String tableName) throws SQLException {
        database = dbHandler;
        table = database.getTable(tableName);
        fillTableView();
    }

    public void fillTableView() {
        items = FXCollections.observableArrayList(table.getData());
        tableView.setItems(items);
        tableView.getColumns().clear();

        for (int i = 0; i < table.getColumnCount(); i++) {
            if (table.getColumnType(i) != Types.BLOB) {
                TableColumn<Map<String, Object>, String> column = new TableColumn<>(
                        table.getColumnName(i));
                column.setCellValueFactory(new MapValueFactory(table.getColumnName(i)));
                tableView.getColumns().add(column);
            }
            //Обработка BLOB
            else {
                TableColumn<Map<String, Object>, ImageView> column = new TableColumn<>(
                        table.getColumnName(i));
                column.setCellValueFactory(new MapValueFactory(table.getColumnName(i)));
                tableView.getColumns().add(column);
            }
        }
    }

    public void refreshDB() {
        try {
            database.fillTable(table);
            fillTableView();
        } catch (SQLException | RuntimeException e) {
            Alerts.showErrorAlert(e.getMessage());
        }
    }

    public void DeleteRow() {
        Map<String, Object> row = tableView.getSelectionModel().getSelectedItem();
        if (row != null) {
            try {
                database.deleteRow(row, table);
                tableView.getItems().remove(row);
            } catch (SQLException | RuntimeException e) {
                Alerts.showErrorAlert(e.getMessage());
            }
        } else Alerts.showErrorAlert("The row is not selected");
    }

    public void AddRow() throws IOException {
        try {
            Map<String, Object> row = showRowDialog(true);
            database.addRow(row, table);
            tableView.getItems().add(row);
        } catch (SQLException | RuntimeException e) {
            Alerts.showErrorAlert(e.getMessage());
        }
    }

    public void updateRow(MouseEvent mouseEvent) throws IOException {
        if (mouseEvent.getClickCount() == 2) {
            int oldRowIndex = table.getData().indexOf(tableView.getSelectionModel().getSelectedItem());
            Map<String, Object> newRow = showRowDialog(false);
            if (newRow != null) {
                try {
                    database.updateRow(newRow, oldRowIndex, table);
                    tableView.getSelectionModel().getSelectedItem().putAll(newRow);
                    tableView.refresh();
                } catch (SQLException | RuntimeException e) {
                    Alerts.showErrorAlert(e.getMessage());
                }
            }
        }
    }

    private Map<String, Object> showRowDialog(boolean newRow) throws IOException{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/RowView.fxml"));
        Dialog<Map<String, Object>> dialog = loader.load();
        RowDialogController controller = loader.getController();

        if (table != null) {
            if (newRow) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 0; i < table.getColumnCount(); i++) {
                    row.put(table.getColumnName(i), null);
                }
                controller.setData(row, table.getColumnNames(), table.getColumnTypes());
            } else {
                controller.setData(new HashMap<>(tableView.getSelectionModel().getSelectedItems().get(0)),
                        table.getColumnNames(), table.getColumnTypes());
            }

            Optional<Map<String, Object>> optional = dialog.showAndWait();

            return optional.orElse(null);
        } else throw new RuntimeException("The table is not selected");
    }

    public void setFilter() throws IOException {
        if (table != null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/FilterView.fxml"));
            Dialog<List<FilterData>> dialog = loader.load();
            FilterController controller = loader.getController();
            controller.setData(table.getColumnNames(), table.getColumnTypes());
            Optional<List<FilterData>> optional = dialog.showAndWait();
            if (optional.isPresent()) {
                try {
                    database.fillTableByFilter(optional.get(), table);
                } catch (SQLException | ParseException | NumberFormatException e) {
                    Alerts.showErrorAlert(e.getMessage());
                }
            }
            fillTableView();
        } else {
            Alerts.showErrorAlert("The table is not selected");
        }
    }

    public void exportToCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("(*.txt)", "*.txt"));
        File file = fileChooser.showSaveDialog(new Stage());
        try {
            database.exportToCSV(file, table);
        } catch (IOException | RuntimeException e) {
            Alerts.showErrorAlert(e.getMessage());
        }
    }

    public void exportToXLS() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("(*.xls)", "*.xls"));
        File file = fileChooser.showSaveDialog(new Stage());
        try {
            database.exportToXLS(file, table);
        } catch (IOException | RuntimeException e) {
            Alerts.showErrorAlert(e.getMessage());
        }
    }
}
