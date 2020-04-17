package model;

import Utils.Alerts;
import Utils.DataHandler;
import Utils.FilterData;

import javafx.scene.image.ImageView;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import java.sql.*;

import java.text.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBHandler {
    private Connection connection;
    private String userName;

    public DBHandler() throws ClassNotFoundException {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e){
            throw  new ClassNotFoundException("Class not found: " + e.getMessage() + "\n (скорее всего он лежит в " +
                    " resources\\jar)");
        }
    }

    public void setConnection(DBLoginData loginData) throws SQLException {
        connection = DriverManager.getConnection(loginData.getAddress(), loginData.getName(), loginData.getPassword());
        this.userName = loginData.getName();
    }

    public Table getTable(String name) throws SQLException {
        Table table = new Table(name);
        fillTable(table);
        return table;
    }

    private String getTablePrimaryKey(String tableName) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("select column_name from user_cons_columns where " +
                "table_name = ?");
        statement.setString(1, tableName);
        ResultSet result = statement.executeQuery();

        if (result.next()) {
            return result.getString(1);
        }

        return "";
    }

    public void fillTable(Table table) throws SQLException {
        if (table == null)
            throw new NullPointerException("The table is not selected");

        PreparedStatement ps = connection.prepareStatement("select * from " + table.getName());
        fillTable(ps, table);
    }

    public void fillTable(PreparedStatement preparedStatements, Table table) throws SQLException, NullPointerException {
        if (table == null)
            throw new NullPointerException("The table is not selected");

        table.clearData();
        ResultSet result = preparedStatements.executeQuery();

        for (int i = 1; i <= result.getMetaData().getColumnCount(); i++) {
            if (result.getMetaData().getColumnType(i) == Types.VARCHAR ||
                    result.getMetaData().getColumnType(i) == Types.NUMERIC ||
                    result.getMetaData().getColumnType(i) == Types.BLOB ||
                    result.getMetaData().getColumnType(i) == Types.TIMESTAMP) {
                table.addColumnType(result.getMetaData().getColumnType(i));
                table.addColumnName(result.getMetaData().getColumnName(i));
            }
        }
        table.setPrimaryKey(getTablePrimaryKey(table.getName()));

        while (result.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= table.getColumnCount(); i++) {
                switch (table.getColumnType(i - 1)) {
                    case Types.NUMERIC:
                        row.put(table.getColumnName(i - 1), result.getInt(i));
                        break;
                    case Types.VARCHAR:
                        row.put(table.getColumnName(i - 1), result.getString(i));
                        break;
                    case Types.TIMESTAMP:
                        row.put(table.getColumnName(i - 1), result.getTimestamp(i));
                        break;
                    case Types.BLOB:
                        row.put(table.getColumnName(i - 1), DataHandler.blobToImage(result.getBlob(i),
                                200, 200));
                        break;
                }
            }
            table.addData(row);
        }
    }

    public List<String> getTablesList() throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM " + userName + ".tab");
        List<String> tables = new ArrayList<>();

        while (result.next()) {
            tables.add(result.getString(1));
        }

        return tables;
    }

    public void addRow(Map<String, Object> row, Table table) throws SQLException, NullPointerException {
        if (table == null)
            throw new NullPointerException("The table is not selected");

        StringBuilder sb = new StringBuilder("INSERT INTO " + table.getName() + " (");
        List<String> tmpList = new ArrayList<>();

        for (int i = 0; i < table.getColumnCount(); i++) {
            String name = table.getColumnName(i);
            if (row.get(name) != null) {
                sb.append(name).append(", ");
                tmpList.add(name);
            }
        }
        if (sb.toString().contains(","))
            sb.deleteCharAt(sb.toString().lastIndexOf(','));
        sb.append(") VALUES (");

        for (int i = 0; i < tmpList.size(); i++) {
            sb.append("?,");
        }
        if (sb.toString().contains(","))
            sb.deleteCharAt(sb.toString().lastIndexOf(','));
        sb.append(")");

        PreparedStatement ps = connection.prepareStatement(sb.toString());

        for (int i = 1; i <= tmpList.size(); i++) {
            String name = tmpList.get(i - 1);

            switch (table.getColumnType(name)) {
                case Types.NUMERIC:
                    ps.setInt(i, (Integer) row.get(name));
                    break;
                case Types.VARCHAR:
                    ps.setString(i, (String) row.get(name));
                    break;
                case Types.TIMESTAMP:
                    ps.setTimestamp(i, (Timestamp) row.get(name));
                    break;
                case Types.BLOB:
                    Blob blob = connection.createBlob();
                    blob.setBytes(1, DataHandler.imageToBlob((ImageView) row.get(name)));
                    ps.setBlob(i, blob);
            }
        }

        ps.executeUpdate();
    }

    public void deleteRow(Map<String, Object> row, Table table) throws SQLException, NullPointerException {
        if (table == null)
            throw new NullPointerException("The table is not selected");

        PreparedStatement statement = connection.prepareStatement("DELETE FROM " + table.getName() + " WHERE " +
                table.getPrimaryKey() + " = ?");
        switch (table.getColumnType(table.getPrimaryKey())) {
            case Types.NUMERIC:
                statement.setInt(1, (Integer) row.get(table.getPrimaryKey()));
                break;
            case Types.VARCHAR:
                statement.setString(1, (String) row.get(table.getPrimaryKey()));
                break;
            case Types.TIMESTAMP:
                statement.setTimestamp(1, (Timestamp) row.get(table.getPrimaryKey()));
                break;
        }
        statement.executeUpdate();
        table.getData().remove(row);
    }

    public void fillTableByFilter(List<FilterData> filtersData, Table table) throws SQLException, ParseException,
            NumberFormatException, NullPointerException {
        if (table == null)
            throw new NullPointerException("The table is not selected");

        StringBuilder sb = new StringBuilder("SELECT ");
        for (FilterData filterData : filtersData) {
            if (filterData.getShow()) {
                sb.append(filterData.getColumnName()).append(", ");
            }
        }
        sb.deleteCharAt(sb.toString().lastIndexOf(','));
        sb.append(" FROM ").append(table.getName()).append(" WHERE ");
        for (FilterData filterData : filtersData) {
            if (filterData.getFilter() != null) {
                if (filterData.getSign().equals("Contains")) {
                    filterData.setFilter("%" + filterData.getFilter() + "%");
                    sb.append(filterData.getColumnName()).append(" LIKE ? and ");
                } else {
                    sb.append(filterData.getColumnName()).append(filterData.getSign()).append("? and ");
                }
            }
        }
        if (sb.toString().contains("and")) {
            int tmp = sb.toString().lastIndexOf("and");
            sb.delete(tmp, tmp + 3);
        } else {
            int tmp = sb.toString().lastIndexOf("WHERE");
            sb.delete(tmp, tmp + 5);
        }

        PreparedStatement ps = connection.prepareStatement(sb.toString());
        int k = 1;
        for (FilterData filterData : filtersData) {
            if (filterData.getFilter() != null) {
                switch (filterData.getColumnType()) {
                    case Types.NUMERIC:
                        try {
                            ps.setInt(k, Integer.parseInt(filterData.getFilter()));
                        } catch (NumberFormatException e) {
                            throw new NumberFormatException("Unparseable number: " + filterData.getFilter());
                        }
                        break;
                    case Types.VARCHAR:
                        ps.setString(k, filterData.getFilter());
                        break;
                    case Types.TIMESTAMP:
                        ps.setTimestamp(k,
                                new Timestamp(DataHandler.parseDate(filterData.getFilter(),
                                        "yyyy-MM-dd hh:mm:ss").getTimeInMillis()));
                }
                k++;
            }
        }
        fillTable(ps, table);
    }

    public void updateRow(Map<String, Object> newRow, int oldRowIndex, Table table) throws SQLException, NullPointerException {
        if (table == null)
            throw new NullPointerException("The table is not selected");

        StringBuilder sb = new StringBuilder("UPDATE " + table.getName() + " SET ");
        List<String> changedColumns = new ArrayList<>();
        boolean isChanged = false;

        for (int i = 0; i < table.getColumnCount(); i++) {
            String name = table.getColumnName(i);
            if (newRow.get(name) != table.getData().get(oldRowIndex).get(name)) {
                sb.append(" ").append(name).append(" = ? ,");
                changedColumns.add(table.getColumnName(i));
                isChanged = true;
            }
        }
        if (isChanged) {
            sb.append(" WHERE ").append(table.getPrimaryKey()).append(" = ?");
            sb.deleteCharAt(sb.toString().lastIndexOf(','));

            PreparedStatement ps = connection.prepareStatement(sb.toString());

            for (int i = 1; i <= changedColumns.size(); i++) {
                String name = changedColumns.get(i - 1);
                int type = table.getColumnType(name);

                switch (type) {
                    case Types.NUMERIC:
                        ps.setInt(i, (Integer) newRow.get(name));
                        break;
                    case Types.VARCHAR:
                        ps.setString(i, (String) newRow.get(name));
                        break;
                    case Types.TIMESTAMP:
                        ps.setTimestamp(i, (Timestamp) newRow.get(name));
                        break;
                    case Types.BLOB:
                        Blob blob = connection.createBlob();
                        byte[] bytes = DataHandler.imageToBlob((ImageView) newRow.get(name));
                        blob.setBytes(1, bytes);
                        ps.setBlob(i, blob);
                }
            }

            switch (table.getColumnType(table.getPrimaryKey())) {
                case Types.NUMERIC:
                    ps.setInt(changedColumns.size() + 1, (Integer) table.getPrimaryKeyValue(oldRowIndex));
                    break;
                case Types.VARCHAR:
                    ps.setString(changedColumns.size() + 1, (String) table.getPrimaryKeyValue(oldRowIndex));
                    break;
                case Types.TIMESTAMP:
                    ps.setTimestamp(changedColumns.size() + 1, (Timestamp) table.getPrimaryKeyValue(oldRowIndex));
                    break;
                case Types.BLOB:
                    Blob blob = connection.createBlob();
                    blob.setBytes(0, DataHandler.imageToBlob((ImageView) table.getPrimaryKeyValue(oldRowIndex)));
                    ps.setBlob(changedColumns.size() + 1, blob);
            }
            ps.executeUpdate();
            table.getData().set(oldRowIndex, newRow);
        }
    }


    public void exportToCSV(File file, Table table) throws IOException, NullPointerException {
        if (table == null)
            throw new NullPointerException("The table is not selected");

        FileWriter fileWriter = new FileWriter(file, false);
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < table.getData().size(); i++) {
            Map<String, Object> data = table.getData().get(i);
            for (int j = 0; j < table.getColumnCount(); j++) {
                if (data.get(table.getColumnName(j)) != null) {
                    switch (table.getColumnType(j)) {
                        case Types.NUMERIC:
                        case Types.TIMESTAMP:
                            sb.append(data.get(table.getColumnName(j))).append(";");
                            break;
                        case Types.VARCHAR:
                            sb.append("\"").append(data.get(table.getColumnName(j))).append("\";");
                            break;
                    }
                }
            }
            sb.append("\n");
        }
        fileWriter.write(sb.toString());
        fileWriter.flush();
        Alerts.showInfoAlert("Export to CSV was successful");
    }

    public void exportToXLS(File file, Table table) throws IOException, NullPointerException {
        if (table == null)
            throw new NullPointerException("The table is not selected");

        XSSFWorkbook workBook = new XSSFWorkbook();
        XSSFSheet workSheet = workBook.createSheet(table.getName());
        Row namesRow = workSheet.createRow(0);

        for (int i = 0; i < table.getColumnCount(); i++) {
            Cell cell = namesRow.createCell(i);
            if (table.getColumnType(i) != Types.BLOB)
                cell.setCellValue(table.getColumnName(i));
        }

        for (int i = 0; i < table.getData().size(); i++) {
            Row row = workSheet.createRow(i + 1);
            Map<String, Object> data = table.getData().get(i);
            for (int j = 0; j < table.getColumnCount(); j++) {
                Cell cell = row.createCell(j);
                if (data.get(table.getColumnName(j)) != null) {
                    switch (table.getColumnType(j)) {
                        case Types.NUMERIC:
                            cell.setCellValue((Integer) data.get(table.getColumnName(j)));
                            break;
                        case Types.VARCHAR:
                            cell.setCellValue((String) data.get(table.getColumnName(j)));
                            break;
                        case Types.TIMESTAMP:
                            cell.setCellValue(data.get(table.getColumnName(j)).toString());
                            break;
                    }
                }
            }
        }

        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            workBook.write(outputStream);
            Alerts.showInfoAlert("Export to XLS was successful");
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
    }
}