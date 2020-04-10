import infoPacket.InfoPacket;
import org.apache.xmlbeans.XmlException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Arrays;

public class Server {
    private boolean XMLSave = false;
    private boolean DBSave = false;
    private int port;
    private DatabaseHandler DBHandler;
    private XMLMessagesHandler messagesHandler = new XMLMessagesHandler();
    private ServerSocket server;
    private Socket client;

    Server(int port) {
        this.port = port;
    }

    Server(int port, DatabaseHandler DBHandler) {
        this(port);
        this.DBHandler = DBHandler;
    }

    void start() throws IOException, SQLException {
        try {
            server = new ServerSocket(port);
            client = server.accept();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (DataOutputStream dataOut = new DataOutputStream(client.getOutputStream());
             DataInputStream dataIn = new DataInputStream(client.getInputStream())) {
            while (client.isConnected()) {
                String info = dataIn.readUTF();
                try {
                    InfoPacket infoMessage = messagesHandler.parseInfoPacket(info);
                    dataOut.writeInt(Status.INFO_MESSAGE_DELIVERED);

                    byte[] byteData = new byte[infoMessage.getDataByteSize()];
                    int maxPacketSize = infoMessage.getPacketsSize();

                    try {
                        for (int i = 0; i < infoMessage.getPacketsCount(); i++) {
                            if (i != infoMessage.getPacketsCount() - 1) {
                                dataIn.read(byteData, maxPacketSize * i, maxPacketSize);
                                dataOut.writeInt(Status.PACKET_DELIVERED);
                            } else {
                                dataIn.read(byteData, maxPacketSize * i, infoMessage.getLastPacketSize());
                            }
                        }
                    } catch (IOException e) {
                        dataOut.writeInt(Status.PACKET_DELIVERY_ERROR);
                    }
                    System.err.println(new String(byteData, StandardCharsets.UTF_8));

                    try{
                        if(compareWithCheckSum(new String(byteData, StandardCharsets.UTF_8),
                                infoMessage.getChecksum(), "MD5")){
                            try {
                                if (DBSave) DBHandler.messageToDB(messagesHandler.parseMessage(new String(byteData, StandardCharsets.UTF_8)));
                                dataOut.writeInt(Status.MESSAGE_DELIVERED_SUCCESSFUL);
                            } catch (XmlException e) {
                                dataOut.writeInt(Status.MESSAGE_PARSE_ERROR);
                                e.printStackTrace();
                            }
                        } else dataOut.writeInt(Status.CHECKSUM_DOESNT__MATCH);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                } catch (XmlException e) {
                    dataOut.writeInt(Status.INFO_MESSAGE_PARSE_ERROR);
                }
            }
        } catch (IOException e) {
            System.out.println("Client disconnected");
        }

        if (DBSave) {
            try {
                System.out.println("Count of messages in Database: " + DBHandler.messagesCount());
                DBHandler.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (XMLSave) {
            messagesHandler.messagesToXML();
            try {
                System.out.println("Count of messages in XML: " + messagesHandler.messagesCountXML());
            } catch (ParserConfigurationException | SAXException | XPathExpressionException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveMessagesInDB() {
        DBSave = true;
    }

    public void saveMessagesInXML(String path) {
        XMLSave = true;
        messagesHandler.setFilePath(path);
    }

    public boolean compareWithCheckSum(String message, String checkSum, String algorithm) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
        String digest = Arrays.toString(messageDigest.digest(message.getBytes()));

        System.err.println(checkSum + "\n" + digest);

        return checkSum.equals(digest);
    }
}