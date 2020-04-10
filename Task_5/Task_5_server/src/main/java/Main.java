import java.io.IOException;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {
        DatabaseHandler DBHandler = new DatabaseHandler("jdbc:oracle:thin:@localhost:3333:XE", "SYSTEM", "oracle");
        DBHandler.createConnection();
        DBHandler.setTable("MESSAGES");
        DBHandler.createTable("ddd"); //Если нет таблички

        Server server = new Server(3344, DBHandler);

        server.saveMessagesInXML("src\\main\\resources\\messages.xml");
        server.saveMessagesInDB();
        server.start();
    }
}
