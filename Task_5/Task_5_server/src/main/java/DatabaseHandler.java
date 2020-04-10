import org.apache.xmlbeans.impl.util.HexBin;
import serverMessage.Message;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.*;

public class DatabaseHandler {
    private Connection connection;
    private String tableName;
    private String user;
    private String url;
    private String password;

    public DatabaseHandler(String url, String user, String password){
        this.user = user;
        this.url = url;
        this.password = password;
    }

    public DatabaseHandler(String url, String user, String password, String tableName){
        this(url, user, password);
        setTable(tableName);
    }

    public void close() throws SQLException {
        connection.close();
    }

    public void createConnection() throws SQLException, ClassNotFoundException {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        connection = DriverManager.getConnection(url, user, password);
    }

    public void setTable(String name){
        tableName = name;
    }

    public void createTable(String name) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate("  CREATE TABLE "+ user + "." + name +
                " (ID_ NUMBER GENERATED ALWAYS AS IDENTITY MINVALUE 1 MAXVALUE 9999999999999999999999999999 " +
                "INCREMENT BY 1 START WITH 1 NOT NULL ENABLE, " +
                "USER_IP VARCHAR2 (15 BYTE), " +
                "TIME_ TIMESTAMP (6), " +
                "BODY_ VARCHAR2 (255 BYTE), " +
                "NAME_ VARCHAR2 (20 BYTE), " +
                "IMAGE_ BLOB)");
    }

    private File outPicture(String hex) {
        if (hex == null || hex.isEmpty())
            return null;

        byte[] imgInBytes = HexBin.stringToBytes(hex);
        try (InputStream in = new ByteArrayInputStream(imgInBytes)){
            BufferedImage bufferedImage;
            bufferedImage = ImageIO.read(in);
            File file = new File("tmp.png");
            ImageIO.write(bufferedImage, "PNG", file);
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void messageToDB(Message message) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "insert into "+ tableName +"(USER_IP,TIME_,BODY_,NAME_,IMAGE_) values(?,?,?,?,?)");
        ps.setString(1, message.getUserIP());
        ps.setTimestamp(2, new java.sql.Timestamp(message.getTime().getTimeInMillis()));
        ps.setString(3, message.getBody());
        ps.setString(4, message.getName());

        File file = outPicture(message.getImageHex());

        if (file != null) {
            try (FileInputStream fis = new FileInputStream(file)) {
                ps.setBinaryStream(5, fis);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!file.delete()){
                System.err.println("tmp *.png file is not deleted!");
            }
        } else ps.setBlob(5, connection.createBlob());

        ps.executeUpdate();
    }

    public int messagesCount() throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery("SELECT COUNT (ID_) FROM " + tableName);
        result.next();

        return result.getInt(1);
    }
}