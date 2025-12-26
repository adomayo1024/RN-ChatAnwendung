package ChatAnwendung.Impl.Handler.Common;

import ChatAnwendung.Api.Handler;
import ChatAnwendung.Impl.Header;
import ChatAnwendung.Impl.PacketTypes;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.logging.Logger;

@Slf4j
public abstract class AbstractHandler implements Handler {

    protected DatagramPacket makeDatagramPackage(PacketTypes type,  long destId, int sequenz, int fileId, byte[] payload, InetAddress adress, int port) {
        return makeDatagramPackage(type, (byte) 32, destId, sequenz, fileId, payload, adress, port);
    }

    protected DatagramPacket makeDatagramPackage(PacketTypes type, byte ttl, long destId, int sequenz, int fileId, byte[] payload, InetAddress adress, int port) {

        byte[] header = Header.makeHeader((byte) type.ordinal(), ttl, destId, sequenz, fileId, (short) payload.length, payload);

        byte[] packet = new byte[header.length + payload.length];

        System.arraycopy(header, 0, packet, 0, header.length);
        System.arraycopy(payload, 0, packet, header.length, payload.length);

        return new DatagramPacket(packet, packet.length, adress, port);
    }
}
