package ChatAnwendung.logic.Impl;

import ChatAnwendung.persistence.Api.RoutingEntry;
import ChatAnwendung.persistence.Api.RoutingTable;
import ChatAnwendung.logic.Enums.PacketTypes;
import ChatAnwendung.persistence.Api.Storage;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.util.concurrent.BlockingQueue;

@Slf4j
public class HearbeatSender{


    private final RoutingTable routingTable;

    private final Storage storage;

    private final BlockingQueue<DatagramPacket> sendeQueue;

    public HearbeatSender(RoutingTable routingTable, Storage storage, BlockingQueue<DatagramPacket> sendeQueue) {
        this.routingTable = routingTable;
        this.storage = storage;
        this.sendeQueue = sendeQueue;
    }

    public void run() {

        log.debug("Start with heartbeat sending");

        if(storage.isLogin()){
            for(RoutingEntry entry : routingTable.getAllDirectNeighbours()){
                if(entry.getRoutable()){
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
