package model;

import Utils.AuthorizationData;
import Utils.DataPackets;
import Utils.MessageFactory;
import clientMessage.MessageDocument;

import java.io.*;
import java.net.Socket;

//C:\Users\mvarlamov.CP\Desktop\1.png
//C:\Users\mvarlamov.CP\Desktop\2.png

public class ConnectionManager {
    private AuthorizationData authorizationData;
    private Socket socket = null;
    private DataInputStream dataIn;
    private DataOutputStream dataOut;
    private static ConnectionManager connectionManager;

    public static ConnectionManager getInstance() {
        if (connectionManager == null)
            connectionManager =  new ConnectionManager();

        return connectionManager;
    }

    public AuthorizationData getAuthorizationData() {
        return authorizationData;
    }

    private ConnectionManager(){}

    public void createConnection(AuthorizationData authorizationData) throws IOException, NumberFormatException {
        this.authorizationData = authorizationData;
        socket = new Socket(authorizationData.getIp(), Integer.parseInt(authorizationData.getPort()));
        dataIn = new DataInputStream(socket.getInputStream());
        dataOut = new DataOutputStream(socket.getOutputStream());
    }

    public void sendMessage(String text, File image) throws IOException {
        if (socket != null) {
            MessageDocument message = MessageFactory.createMessage(text, authorizationData.getName(), socket.getLocalAddress()
                    , image);
            DataPackets dataPackets = new DataPackets(message.toString().getBytes());

            dataOut.writeUTF(dataPackets.getDataInfoPacket());
            dataOut.flush();

            if (dataIn.readInt() == 0) {
                byte[][] packets = dataPackets.getPackets();

                for (int i = 0; i < dataPackets.getPacketsCount(); i++) {
                    dataOut.write(packets[i]);
                    int status = dataIn.readInt();
                    System.out.println("Status: " + status);
                    if (status == 2)
                        throw new IOException("Packet delivery error");
                }
            }
        } else
            throw new IOException("No connection");
    }
}