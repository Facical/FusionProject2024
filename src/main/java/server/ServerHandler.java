//package server;
//
//import common.Packet;
//import network.Message;
//
//import java.io.DataInputStream;
//import java.io.DataOutputStream;
//
//public class ServerHandler {
//
//    Message txMsg = null;
//    Message rxMsg = null;
//    byte[] packet = null;
//
//    public void RequestLoginMessage(){
//        txMsg = Message.makeMessage(Packet.REQUEST, Packet.Login, Packet.NOT_USED, "Login Request");
//        packet = Packet.makePacket(txMsg);
//        out.write(packet);
//        out.flush();
//    }
//}
