package network;


public class Packet{
    public static final int LEN_HEADER = 6;
    // TYPE : 1바이트(Type + Code)중에서 상위 4비트 사용(비트 단위의 자료형이 없음)
    // CODE : 1바이트(Type + COde)중에서 하위 4비트 사용(비트 단위의 자료형이 없음)
    public static final int LEN_TYPECODE = 1;
    public static final int LEN_DETAIL = 1;
    public static final int LEN_BODY = 4;

    // Protocol Type
    public static final byte REQUEST = 1;
    public static final byte RESPONSE = 2;
    public static final byte RESULT = 3;

    // Protocol Code
    public static final byte Login = 0;

    // Protocol Detail
    public static final byte NOT_USED = 0;
    public static final byte SUCCESS = 1;
    public static final byte FAIL = 2;

    private byte[] packet;
    private int dataLength = 0;

    public static byte[] intToBytes(int data) {
        return new byte[] {
                (byte)((data >> 24) & 0xff),
                (byte)((data >> 16) & 0xff),
                (byte)((data >> 8) & 0xff),
                (byte)((data >> 0) & 0xff),
        };
    }
    public static int bytesToInt(byte[] data) {
        return (
                (0xff & data[0]) << 24 |
                (0xff & data[1]) << 16 |
                (0xff & data[2]) << 8  |
                        (0xff & data[3])
        );
    }
    public static byte bitsToByte(byte data1, byte data2) {
        return (byte)(
                (0x0f & data1) << 4 |  // Type: upper 4 bits
                        (0x0f & data2) << 0    // Code: lower 4 bits
        );
    }

    public static byte[] byteToBits(byte data) {
        return new byte[] {
                (byte)((data >> 4) & 0x0f),
                (byte)((data >> 0) & 0x0f),
        };
    }


    public static byte[] makePacket(Message msg){
        byte type = msg.getType();
        byte code = msg.getCode();
        byte detail = msg.getDetail();
        int length = msg.getLength();
        String data = msg.getData();

        byte[] packet = new byte[LEN_HEADER + length];
        int index = 0;

        byte typeCodeByte = bitsToByte(type, code);
        packet[index] = typeCodeByte;
        index = index + 1;

        packet[index] = detail;
        index = index + 1;

        byte[] bodyLenByte = intToBytes(length);
        System.arraycopy(bodyLenByte, 0, packet, index, bodyLenByte.length);
        index = index + bodyLenByte.length;

        if(length > 0){
            byte[] dataByte = data.getBytes();
            System.arraycopy(dataByte, 0, packet, index, dataByte.length);
        }

        return packet;
    }
    public byte[] getPacket(){
        return packet;
    }
    public void setData(String data){
        System.arraycopy(data.trim().getBytes(), 0, packet, LEN_HEADER, data.trim().getBytes().length);
        packet[LEN_HEADER + data.trim().getBytes().length] = '\0';

    }

}
