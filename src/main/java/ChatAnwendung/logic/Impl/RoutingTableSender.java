package ChatAnwendung.logic.Impl;

import ChatAnwendung.persistence.Api.RoutingEntry;
import ChatAnwendung.persistence.Api.RoutingTable;
import ChatAnwendung.logic.Enums.PacketTypes;
import ChatAnwendung.persistence.Impl.Storage;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

@Slf4j
public class RoutingTableSender {


    private final int routingTableEntrySize = 17;
    private final int maxAmountOfRoutingTableEntries = 54;

    private final RoutingTable routingTable;

    private final BlockingQueue<DatagramPacket> sendeQueue;

    private final Storage storage;

    public RoutingTableSender(RoutingTable routingTable, BlockingQueue<DatagramPacket> sendeQueue, Storage storage) {
        this.routingTable = routingTable;
        this.sendeQueue = sendeQueue;
        this.storage = storage;
    }

    public void run(){

        log.debug("Start with RoutingTable sending");

        List<RoutingEntry> allEntries = routingTable.getAllEntries();
        List<RoutingEntry> directNeighboursEntries = routingTable.getAllDirectNeighbours();
        Map<RoutingEntry, byte[]> allRoutingTablePackets = new HashMap<>();

        for(RoutingEntry entry : allEntries){
            byte[] routingEntryPayload = new byte[routingTableEntrySize];

            BCPPacket.addLong(0, entry.getUID(), routingEntryPayload);
            routingEntryPayload[8] = entry.getHops();
            BCPPacket.addLong(9, entry.getLastSeen(), routingEntryPayload);

            allRoutingTablePackets.put(entry, routingEntryPayload);

            log.debug("RoutingEntry added to Map: {}", entry.getUID());
        }


        for(RoutingEntry entry : directNeighboursEntries){
            List<RoutingEntry> relevantEntries = allEntries.stream()
                    .filter((r) -> !(r.getNextHopAdress().equals(entry.getNextHopAdress())) || r.getNextHopPort()!= entry.getNextHopPort())
                    .toList();
            int countEntries = relevantEntries.size();


            byte[] payload = new byte[Math.min(countEntries, maxAmountOfRoutingTableEntries) * routingTableEntrySize];
            for(int i = 0; i < countEntries; i++){
                if(i != 0 && i % maxAmountOfRoutingTableEntries == 0){

                    BCPPacket bcpPacket = new BCPPacket(
                            (byte) 1, //version
                            PacketTypes.ROUTINGTABLE, //type
                            (byte) 1, // ttl
                            (byte) 0, // hops
                            storage.getID(), //srcNodId
                            entry.getUID(), //destNodeId
                            0, //sequenz
                            0, //fileId
                            0L, //crc
                            (short)payload.length, //payloadLength
                            payload, //payload
                            entry.getNextHopAdress(), //address
                            entry.getNextHopPort()); //port
                    DatagramPacket packet = bcpPacket.makeDatagramPacket();
                    sendeQueue.add(packet);

                    log.debug("RoutingTable packet send to {}", Long.toUnsignedString(entry.getUID()));

                    if(countEntries - i > maxAmountOfRoutingTableEntries){
                        payload = new byte[maxAmountOfRoutingTableEntries];
                    }
                    else {
                        payload = new byte[countEntries - i];
                    }
                }

                System.arraycopy(allRoutingTablePackets.get(relevantEntries.get(i)), 0, payload, (i % maxAmountOfRoutingTableEntries) * routingTableEntrySize, routingTableEntrySize);
            }

            BCPPacket bcpPacket = new BCPPacket(
                    (byte) 1, //version
                    PacketTypes.ROUTINGTABLE, //type
                    (byte) 1, // ttl
                    (byte) 0, // hops
                    storage.getID(), //srcNodId
                    entry.getUID(), //destNodeId
                    0, //sequenz
                    0, //fileId
                    0L, //crc
                    (short)payload.length, //payloadLength
                    payload, //payload
                    entry.getNextHopAdress(), //address
                    entry.getNextHopPort()); //port
            DatagramPacket packet = bcpPacket.makeDatagramPacket();
            sendeQueue.add(packet);
            log.debug("RoutingTable packet send to {}", Long.toUnsignedString(entry.getUID()));

            log.debug("Finished with sending RoutingTable to: {}", entry.getUID());
        }

        log.debug("Finished with RoutingTable sending");
    }
}
