import infoPacket.InfoPacket;
import infoPacket.InfoPacketDocument;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import serverMessage.Message;
import serverMessage.MessagesDocument;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.FileWriter;
import java.io.IOException;

public class XMLMessagesHandler {
    private String filePath;
    private MessagesDocument messagesDoc = MessagesDocument.Factory.newInstance();

    public XMLMessagesHandler(){}

    public InfoPacket parseInfoPacket(String data) throws XmlException {
        InfoPacketDocument infoMessage = InfoPacketDocument.Factory.parse(data);
        System.err.println(infoMessage.getInfoPacket());
        return infoMessage.getInfoPacket();
    }

    public Message parseMessage(String message) throws XmlException {
        clientMessage.MessageDocument clientMessDoc = clientMessage.MessageDocument.Factory.parse(message);
        serverMessage.Message serverMess = serverMessage.Message.Factory.newInstance();
        serverMess.setBody(clientMessDoc.getMessage().getBody());
        serverMess.setUserIP(clientMessDoc.getMessage().getUserIP());
        serverMess.setName(clientMessDoc.getMessage().getName());
        serverMess.setTime(clientMessDoc.getMessage().getTime());
        serverMess.setImageHex(clientMessDoc.getMessage().getImageHex());

        return serverMess;
    }

    public void messagesToXML() throws IOException {
        FileWriter fw = new FileWriter(filePath);
        messagesDoc.save(fw, new XmlOptions().setSavePrettyPrint());
        fw.flush();
    }

    public int messagesCountXML() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(filePath);
        XPathExpression expression = xPath.compile("count(//Messages/Message)");
        Number result = (Number) expression.evaluate(doc, XPathConstants.NUMBER);

        return result.intValue();
    }

    public void setFilePath(String filePath){
        this.filePath = filePath;
    }
}
