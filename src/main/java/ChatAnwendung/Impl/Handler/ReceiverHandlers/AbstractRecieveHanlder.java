package ChatAnwendung.Impl.Handler.ReceiverHandlers;

import ChatAnwendung.Impl.Handler.Common.AbstractHandler;
import ChatAnwendung.Impl.BCPPacket;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;

@Slf4j
public class AbstractRecieveHanlder extends AbstractHandler {

    protected final DatagramPacket  packet;

    protected AbstractRecieveHanlder(DatagramPacket packet) {
        this.packet = packet;
    }


    protected short getPayloadLength(byte[] data){
        return makeBytesToShort(data, BCPPacket.getPayloadLengthPos());
    }

    protected long getSrcUID(byte[] data){
        return makeBytesToLong(data, BCPPacket.getSrcNodeIdPos());
    }

    protected long getDestId(byte[] data){
        return makeBytesToLong(data, BCPPacket.getDestNodeIdPos());
    }

    protected byte getType(byte[] data){
        return data[BCPPacket.getTypePos()];
    }

    protected byte getTtl(byte[] data){
        return data[BCPPacket.getTtlPos()];
    }

    protected byte getHops(byte[] data){
        return data[BCPPacket.getHopsPos()];
    }

    protected int getSequenz(byte[] data){
        return makeBytesToInt(data, BCPPacket.getSequenzPos());
    }

    protected int getFileId(byte[] data){
        return makeBytesToInt(data, BCPPacket.getFileIdPos());
    }

    protected long makeBytesToLong(byte[] data, int pos){
        byte b0 = data[pos];
        byte b1 = data[pos + 1];
        byte b2 = data[pos + 2];
        byte b3 = data[pos + 3];
        byte b4 = data[pos + 4];
        byte b5 = data[pos + 5];
        byte b6 = data[pos + 6];
        byte b7 = data[pos + 7];

        return ((long)(b0 & 0xFF) << 56) |
                ((long)(b1 & 0xFF) << 48) |
                ((long)(b2 & 0xFF) << 40) |
                ((long)(b3 & 0xFF) << 32) |
                ((long)(b4 & 0xFF) << 24) |
                ((long)(b5 & 0xFF) << 16) |
                ((long)(b6 & 0xFF) <<  8) |
                ((long)(b7 & 0xFF));
    }


    protected short makeBytesToShort(byte[] data, int pos){
        byte b0 = data[pos];
        byte b1 = data[pos + 1];

        return (short) (((b0 & 0xFF) << 8) | (b1 & 0xFF));
    }

    protected int makeBytesToInt(byte[] data, int pos){
        byte b0 = data[pos];
        byte b1 = data[pos + 1];
        byte b2 = data[pos + 2];
        byte b3 = data[pos + 3];

        return ((b0 & 0xFF) << 24) |
                ((b1 & 0xFF) << 16) |
                ((b2 & 0xFF) << 8) |
                (b3 & 0xFF);
    }
}
