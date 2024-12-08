package network;



import common.Packet;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.DataInputStream;
import java.io.IOException;

@Getter
@Setter
@ToString
public class Message{
    private byte type;
    private byte code;
    private byte detail;
    private int length;
    private String data;

    public Message(){
        this.type = 0;
        this.code = 0;
        this.detail = 0;
        this.data = null;
    }

    public static Message makeMessage(byte type, byte code, byte detail, String data){
        Message msg = new Message();

        msg.setType(type);
        msg.setCode(code);
        msg.setDetail(detail);
        int bodyLen = data.getBytes().length;
        msg.setLength(bodyLen);
        msg.setData(data);
        return msg;
    }
    public static Message readMessage(DataInputStream in){
        Message msg = new Message();
        try{

            byte[] header = new byte[Packet.LEN_HEADER];
            in.read(header);
            makeMessageHeader(msg, header);

            byte[] body = new byte[msg.getLength()];
            in.read(body);
            makeMessageBody(msg, body);

            return msg;

        }catch (IOException e) {
            e.printStackTrace();
        }
        return msg;
    }
    public static void makeMessageHeader(Message msg, byte[] header){
        int index = 0;
        byte typeCodeByte = header[index];
        byte[] typeCode = Packet.byteToBits(typeCodeByte);
        index = index + 1;

        msg.setType(typeCode[0]);
        msg.setCode(typeCode[1]);

        byte detail = header[index];
        index = index + 1;
        msg.setDetail(detail);

        byte[] lengthByte = new byte[Packet.LEN_BODY];
        for(int i = 0; i < Packet.LEN_BODY; i++){
            lengthByte[i] = header[index];
            index = index + 1;
        }
        int length = Packet.bytesToInt(lengthByte);
        msg.setLength(length);
    }
    public static void makeMessageBody(Message msg, byte[] body){
        int index = 0;

        byte[] dataByte = new byte[msg.getLength()];
        for(int i = 0; i < msg.getLength(); i++){
            dataByte[i] = body[index];
            index++;
        }
        String data = new String(dataByte);
        msg.setData(data);

    }
    public static void printMessage(Message msg) {
        System.out.println(">> New Packet Received");
        System.out.println("   Type    : " + msg.getType());
        System.out.println("   Code    : " + msg.getCode());
        System.out.println("   Detail     : " + msg.getDetail());
        System.out.println("   Data : " + msg.getData());
        System.out.println();
    }
}
