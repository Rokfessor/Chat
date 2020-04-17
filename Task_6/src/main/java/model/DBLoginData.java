package model;

public class DBLoginData {
    private String name;
    private String password;
    private String address;

    public DBLoginData(String name, String password, String address){
        this.name = name;
        this.address = address;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getAddress() {
        return address;
    }
}
