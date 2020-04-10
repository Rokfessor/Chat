package Utils;

public class AuthorizationData {
    private String name;
    private String ip;
    private String port;

    public AuthorizationData(String name, String ip, String port) {
        this.name = name;
        this.ip = ip;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    public String getPort() {
        return port;
    }
}
