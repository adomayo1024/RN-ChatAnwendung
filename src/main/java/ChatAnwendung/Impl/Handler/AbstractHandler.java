package ChatAnwendung.Impl.Handler;

import ChatAnwendung.Api.Handler;
import ChatAnwendung.Impl.PacketTypes;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.logging.Logger;

public abstract class AbstractHandler implements Handler {

    protected String[] command;

    protected final Logger logger;

    public AbstractHandler(String[] command, String name){
        this.command = command;
        logger = Logger.getLogger(name);
    }

    protected DatagramPacket makeDatagramPackage(PacketTypes type, long destId, int sequenz, int fileId, byte[] payload, InetAddress adress, int port) {

        byte[] header = Header.makeHeader((byte) type.ordinal(), destId, sequenz, fileId, (short) payload.length);

        byte[] packet = new byte[header.length + payload.length];

        System.arraycopy(header, 0, packet, 0, header.length);
        System.arraycopy(payload, 0, packet, header.length, payload.length);

        return new DatagramPacket(packet, packet.length, adress, port);
    }
}
