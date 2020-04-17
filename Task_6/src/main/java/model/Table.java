package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Table {
    private final String name;
    private String primaryKey;
    private final List<String> columnName = new ArrayList<>();
    private final List<Integer> columnType = new ArrayList<>();
    private final List<Map<String, Object>> data = new ArrayList<>();

    public Table(String name) {
        this.name = name;
    }

    public void clearData(){
        columnName.clear();
        columnType.clear();
        data.clear();
    }

    public Object getPrimaryKeyValue(int index){
        return data.get(index).get(primaryKey);
    }

    public void addData(Map<String, Object> row) {
        data.add(row);
    }

    public List<Map<String, Object>> getData() {
        return data;
    }

    public int getColumnCount() {
        return columnType.size();
    }

    public String getName() {
        return name;
    }

    public void addColumnType(Integer type) {
        columnType.add(type);
    }

    public List<Integer> getColumnTypes() {
        return columnType;
    }

    public int getColumnType(int id) {
        return columnType.get(id);
    }

    public int getColumnType(String columnName){
        return columnType.get(this.columnName.indexOf(columnName));
    }

    public void addColumnName(String name) {
        columnName.add(name);
    }

    public List<String> getColumnNames() {
        return columnName;
    }

    public String getColumnName(int id) {
        return columnName.get(id);
    }

    @Override
    public String toString() {
        return "Table{" + "\n" +
                "name='" + name + '\'' + ",\n" +
                "columnName=" + columnName + ",\n" +
                "columnType=" + columnType + ",\n" +
                "data=" + data + "\n" +
                '}';
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }
}
