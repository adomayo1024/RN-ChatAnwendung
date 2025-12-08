package ChatAnwendung.Impl.Handler.RecieverHandlers;

import ChatAnwendung.Impl.Handler.AbstractHandler;
import ChatAnwendung.Impl.Handler.Header;

import java.net.DatagramPacket;

public class AbstractRecieveHanlder extends AbstractHandler {

    protected final DatagramPacket  packet;

    protected AbstractRecieveHanlder(String name, DatagramPacket packet) {
        super(name);
        this.packet = packet;
    }

    protected short getPayloadLength(byte[] data){
        return makeBytesTwoShort(data, Header.getPayloadLenghtPos());
    }

    protected long getSrcUID(byte[] data){
        return makeBytesTwoLong(data, Header.getSrcNodePos());
    }

    private long  makeBytesTwoLong(byte[] data, int pos){
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


    private short makeBytesTwoShort(byte[] data, int pos){
        byte b0 = data[pos];
        byte b1 = data[pos + 1];

        return (short) (((b0 & 0xFF) << 8) | (b1 & 0xFF));
    }
}
