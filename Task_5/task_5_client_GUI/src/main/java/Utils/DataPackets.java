package Utils;
import infoPacket.InfoPacket;
import infoPacket.InfoPacketDocument;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class DataPackets {
    private byte[][] packets;
    private byte[] data;
    private int packetMaxSize; //bytes
    private int packetsCount;
    private int lastPacketSize;

    public DataPackets(byte[] data){
       this(data, 65536);
    }

    public DataPackets(byte[] data, int packetMaxSize){
        this.packetMaxSize = Math.min(data.length, packetMaxSize);
        this.data = data;
        packetsCount = (int) Math.ceil((float) data.length / packetMaxSize);
        lastPacketSize = packetMaxSize - (packetsCount * packetMaxSize - data.length);
        packets = new byte[packetsCount][];
        for (int i = 0; i < packetsCount - 1; i++){
            packets[i] = new byte[packetMaxSize];
        }
        packets[packetsCount - 1] = new byte[lastPacketSize];
    }

    public int getPacketsCount(){
        return packetsCount;
    }

    public byte[][] getPackets(){
        int k = 0;

        for (int i = 0; i < packetsCount; i++){
            int thisPacketSize = Math.min(data.length - i * packetMaxSize , packetMaxSize);
            packets[i] = new byte[thisPacketSize];

            for (int j = 0; j < thisPacketSize; j++){
                packets[i][j] = data[k];
                k++;
            }
        }
        return packets;
    }

    public String getCheckSum(byte[] data,String algorithm) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
        byte[] digest = messageDigest.digest(data);
        return Arrays.toString(digest);
    }

    public String getDataInfoPacket(){
        InfoPacketDocument infoPacketDocument = InfoPacketDocument.Factory.newInstance();
        InfoPacket infoPacket = infoPacketDocument.addNewInfoPacket();

        try {
            infoPacket.setChecksum(getCheckSum(data, "MD5"));
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        infoPacket.setDataByteSize(data.length);
        infoPacket.setPacketsCount(packetsCount);
        infoPacket.setLastPacketSize(lastPacketSize);
        infoPacket.setPacketsSize(packetMaxSize);

        System.err.println(infoPacketDocument.toString());

        return infoPacketDocument.toString();
    }
}
