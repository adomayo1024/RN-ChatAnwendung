package ChatAnwendung.logic.Impl;

import ChatAnwendung.logic.Api.HeartBeatSender;
import ChatAnwendung.persistence.Api.RoutingEntry;
import ChatAnwendung.persistence.Api.RoutingTable;
import ChatAnwendung.logic.Enums.PacketTypes;
import ChatAnwendung.persistence.Api.Storage;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.util.concurrent.BlockingQueue;

@Slf4j
public class HeartbeatSenderImpl implements HeartBeatSender {


    // Routing table für die direkten Nachbarn
    private final RoutingTable routingTable;

    // Storage für die eigenen NodeId
    private final Storage storage;

    // Damit der Sender die Pakete senden kann
    private final BlockingQueue<DatagramPacket> sendeQueue;

    /**
     * Konstruktor vom Heartbeat Sender.
     * @param routingTable Die Routingtable, damit alle direkten Nachbarn nach geschaut werden können.
     * @param storage Der Storage um die eigene NodeId zu ermitteln.
     * @param sendeQueue Die Sendequeue damit der Sender die Pakete senden kann.
     */
    public HeartbeatSenderImpl(RoutingTable routingTable, Storage storage, BlockingQueue<DatagramPacket> sendeQueue) {
        this.routingTable = routingTable;
        this.storage = storage;
        this.sendeQueue = sendeQueue;
    }

    public void sendHeartbeat() {

        log.debug("Start with heartbeat sending");

        // Prüft ob man eingeloggt ist, wenn nicht wird nichts gesendet
        if(storage.isLogin()){

            // Es wird ein Heartbeat an alle direkten Nachbarn gesendet
            for(RoutingEntry entry : routingTable.getAllDirectNeighbours()){

                // Wenn der direkte Nachbar zurzeit nicht router ist, wird auch nichts gesendet
                if(entry.getRoutable()){

                    // Erzeugung des Heartbeat Pakets
                    byte[] payload = new byte[0];
                    BCPPacketImpl bcpPacket = new BCPPacketImpl(
                            (byte) 1, //version
                            PacketTypes.HEARTBEAT, //type
                            (byte) 1, // ttl
                            (byte) 0, // hops
                            storage.getID(), //srcNodId
                            entry.getNodeId(), //destNodeId
                            0, //sequenz
                            0, //fileId
                            0L, //crc
                            (short)payload.length, //payloadLength
                            payload, //payload
                            entry.getNextHopAddress(), //address
                            entry.getNextHopPort());//port
                    DatagramPacket packet = bcpPacket.makeDatagramPacket();
                    sendeQueue.add(packet);

                    log.debug("Heartbeat packet send to {}", Long.toUnsignedString(entry.getNodeId()));
                }
            }
        }
    }
}
