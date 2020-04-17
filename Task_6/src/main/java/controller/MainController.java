package controller;

import Utils.Alerts;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import model.DBHandler;
import model.DBLoginData;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

//"jdbc:oracle:thin:@localhost:3333:XE", "system", "qwerty"

public class MainController implements Initializable {
    public ListView<String> tablesList;
    public Label userNameLabel;
    public Label tableNameLabel;
    public Label addressLabel;
    public TabPane tabPane;
    private DBHandler database;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setConnection();
        tablesList.setOnMouseReleased(e -> {

            try {
                String name = tablesList.getSelectionModel().getSelectedItem();
                boolean flag = true;

                for (int i = 0; i < tabPane.getTabs().size(); i++) {
                    if (tabPane.getTabs().get(i).getText().equals(name)) {
                        flag = false;
                        break;
                    }
                }

                if (flag) {
                    if (tabPane.getTabs().size() < 10) {
                        Tab tab = new Tab(name);
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/TabContentView.fxml"));
                        VBox vBox = loader.load();
                        TabContentController tcc = loader.getController();
                        tcc.initialize(database, name);
                        tab.setContent(vBox);
                        tabPane.getTabs().add(tab);
                        tabPane.getSelectionModel().selectLast();
                    } else Alerts.showInfoAlert("Only 10 tables can be opened");
                } else {
                    for (int i = 0; i < tabPane.getTabs().size(); i++) {
                        if (tabPane.getTabs().get(i).getText().equals(name)) {
                            tabPane.getSelectionModel().select(tabPane.getTabs().get(i));
                            break;
                        }
                    }
                }

                tableNameLabel.setText(name);
            } catch (SQLException | IOException ex) {
                Alerts.showErrorAlert(ex.getMessage());
            }

        });
    }

    public void fillTablesList() {
        if (database != null) {
            try {
                ObservableList<String> list = FXCollections.observableArrayList(database.getTablesList());
                tablesList.setItems(list);
            } catch (SQLException | NullPointerException e) {
                Alerts.showErrorAlert(e.getMessage());
            }
        }
    }

    public boolean setConnection() {
        try {
            Dialog<DBLoginData> dialog = FXMLLoader.load(getClass().getResource("/view/ConnectionView.fxml"));
            Optional<DBLoginData> login = dialog.showAndWait();

            if (login.isPresent()) {
                database = new DBHandler();
                database.setConnection(login.get());
                userNameLabel.setText(login.get().getName());
                addressLabel.setText(login.get().getAddress());
                fillTablesList();
                return true;
            }
        } catch (ClassNotFoundException | SQLException | IOException e) {
            Alerts.showErrorAlert(e.getMessage());
        }
        return false;
    }
}