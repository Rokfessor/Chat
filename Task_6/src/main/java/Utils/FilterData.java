package Utils;

public class FilterData {
    String sign;
    String columnName;
    String filter;
    Boolean show;
    int columnType;

    public Boolean getShow() {
        return show;
    }

    public void setShow(Boolean show) {
        this.show = show;
    }

    public String getSign() {
        return sign;
    }

    public int getColumnType() {
        return columnType;
    }

    public void setColumnType(int columnType) {
        this.columnType = columnType;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    @Override
    public String toString() {
        return "\nFilterData{\n" +
                "sign='" + sign + "'" +
                ",\ncolumnName='" + columnName + "'" +
                ",\nfilterData='" + filter + "'" +
                ",\ncolumnType=" + columnType +
                "\n}";
    }
}
