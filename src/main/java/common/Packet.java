package common;


import network.Message;

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

    // 기능 모음
    // 학생 기능
    public static final byte LOGIN = 0;
    public static final byte CHECK_SCHEDULE = 1;           // 선발 일정 및 비용 확인
    public static final byte APPLY_ADMISSION = 2;          // 입사 신청
    public static final byte CHECK_ADMISSION = 3;          // 합격 여부 및 호실 확인
    public static final byte CHECK_PAY_DORMITORY = 4;      // 생활관 비용 확인 및 납부
    public static final byte SUBMIT_CERTIFICATE = 5;       // 결핵진단서 제출
    public static final byte REQUEST_WITHDRAWAL = 6;       // 퇴사 신청
    public static final byte CHECK_REFUND = 7;            // 환불 확인

    // 관리자 기능 코드
    public static final byte REGISTER_SCHEDULE = 8;        // 선발 일정 등록
    public static final byte REGISTER_FEE = 9;            // 생활관 사용료 및 급식비 등록
    public static final byte VIEW_APPLICANTS = 10;         // 신청자 조회
    public static final byte SELECT_STUDENTS = 11;         // 입사자 선발 및 호실 배정
    public static final byte VIEW_PAID_STUDENTS = 12;      // 생활관 비용 납부자 조회
    public static final byte VIEW_UNPAID_STUDENTS = 13;    // 생활관 비용 미납부자 조회
    public static final byte CHECK_CERTIFICATES = 14;      // 결핵진단서 제출 확인
    public static final byte PROCESS_WITHDRAWAL = 15;
    // 퇴사 신청자 조회 및 환불

    public static final byte CHECK_DATE = 0;

    // Protocol Detail
    public static final byte NOT_USED = 0;
    public static final byte SUCCESS = 1;
    public static final byte FAIL = 2;
    public static final byte END_CONNECT = 3;


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
