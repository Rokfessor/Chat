package Utils;

import clientMessage.Message;
import clientMessage.MessageDocument;
import org.apache.xmlbeans.impl.util.HexBin;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.util.GregorianCalendar;

public class MessageFactory {
    public static MessageDocument createMessage(String text, String name, InetAddress ip, File image){
        MessageDocument messageDocument = MessageDocument.Factory.newInstance();
        Message message = messageDocument.addNewMessage();
        message.setBody(text);
        message.setName(name);
        message.setTime(new GregorianCalendar());
        message.setUserIP(new StringBuilder(ip.toString()).deleteCharAt(0).toString());

        if (image != null) {
            try {
                byte[] bytes = Files.readAllBytes(image.toPath());
                message.setImageHex(HexBin.bytesToString(bytes));
            } catch (IOException e) {
                message.setImageHex("");
            }
        }
        System.err.println(messageDocument);

        return messageDocument;
    }
}
