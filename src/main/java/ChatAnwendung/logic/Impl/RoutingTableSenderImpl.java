package ChatAnwendung.logic.Impl;

import ChatAnwendung.logic.Api.BCPPacket;
import ChatAnwendung.logic.Api.RoutingTableSender;
import ChatAnwendung.persistence.Api.RoutingEntry;
import ChatAnwendung.persistence.Api.RoutingTable;
import ChatAnwendung.logic.Enums.PacketTypes;
import ChatAnwendung.persistence.Api.Storage;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

@Slf4j
public class RoutingTableSenderImpl implements RoutingTableSender {

    //Maximale Anzahl an RoutingTableEntries, die mit einem Paket versendet werden dürfen.
    private final int maxAmountOfRoutingTableEntries = 54;

    //Die RoutingTable, welche Einträge versendet werden sollen
    private final RoutingTable routingTable;

    //Die Sendequeue, dass der Sender auch die Pakete senden kann
    private final BlockingQueue<DatagramPacket> sendeQueue;

    //Der Storage um die eigene NodeId zu erhalten
    private final Storage storage;

    public RoutingTableSenderImpl(RoutingTable routingTable, BlockingQueue<DatagramPacket> sendeQueue, Storage storage) {
        this.routingTable = routingTable;
        this.sendeQueue = sendeQueue;
        this.storage = storage;
    }

    public void run(){

        log.debug("Start with RoutingTable sending");

        //Alle RoutingTableEntries
        List<RoutingEntry> allEntries = routingTable.getAllEntries();

        //Alle direkten Nachbarn, an die die RoutingTable gesendet werden soll
        List<RoutingEntry> directNeighboursEntries = routingTable.getAllDirectNeighbours();

        //Alle einzelnen Byte-Arrays der RoutingTableEntries
        Map<RoutingEntry, byte[]> allRoutingTablePackets = new HashMap<>();
        ByteBuffer buffer = ByteBuffer.allocate(BCPPacket.ROUTING_TABLE_ENTRY_SIZE);

        //Es wird für alle RoutingTableEntries das ByteArray vor erstellt.
        for(RoutingEntry entry : allEntries){

            buffer.putLong(entry.getNodeId());
            buffer.put(entry.getHops());
            buffer.putLong(entry.getLastSeen());

            allRoutingTablePackets.put(entry, buffer.array());

            log.debug("RoutingEntry added to Map: {}", entry.getNodeId());
        }


        //Es werden an alle direkten Nachbarn die RoutingTable gesendet, mit Split Horizon Regel.
        for(RoutingEntry entry : directNeighboursEntries){

            //Es werden nur die relevanten RoutingTableEntries aus allen herausgenommen, die wo der next Hop nicht das Ziel
            //dieses RoutingTable Packets ist.
            List<RoutingEntry> relevantEntries = allEntries.stream()
                    .filter((r) -> !(r.getNextHopAddress().equals(entry.getNextHopAddress())) || r.getNextHopPort()!= entry.getNextHopPort())
                    .toList();
            int countEntries = relevantEntries.size();


            byte[] payload = new byte[Math.min(countEntries, maxAmountOfRoutingTableEntries) * BCPPacket.ROUTING_TABLE_ENTRY_SIZE];

            //Es werden alle, für diesen Nachbarn, relevanten RoutingTableEntries gesendet.
            for(int i = 0; i < countEntries; i++){

                //Wenn die Anzahl an RoutingTableEntries die maxAmountOfRoutingTableEntries überschreitet.
                //Werden die schon mal gesendet und die restlichen kommen in ein neues Paket.
                if(i != 0 && i % maxAmountOfRoutingTableEntries == 0){

                    sendRoutingTablePacket(entry, payload);

                    //Es muss ein neuer Payload allokiert werden, für die restlichen RoutingTableEntries.
                    payload = getNewPayload(countEntries - i);
                }

                // Es wird der RoutingTableEntrie in den Payload an die nächste Position kopiert.
                System.arraycopy(allRoutingTablePackets.get(relevantEntries.get(i)),
                        0,
                        payload,
                        (i % maxAmountOfRoutingTableEntries) * BCPPacket.ROUTING_TABLE_ENTRY_SIZE,
                        BCPPacket.ROUTING_TABLE_ENTRY_SIZE);
            }

            // Die restlichen RoutingTableEntries werden in einem neuen Paket gesendet.
            sendRoutingTablePacket(entry, payload);

            log.debug("Finished with sending RoutingTable to: {}", entry.getNodeId());
        }

        log.debug("Finished with RoutingTable sending");
    }

    /**
     * Gibt ein neues Byte-Array für den Payload wieder.
     * Die Größe wird bestimmt von, der Anzahl an Entry die noch zu senden sind.
     * @param amountOfLeftEntries Die Anzahl, wie viele Entries noch versendet werden müssen
     * @return Byte-Array mit der Größe von {@code amountOfLeftEntries}, oder {@code maxAmountOfRoutingTableEntries},
     * wenn {@code amountOfLeftEntries > maxAmountOfRoutingTableEntries} gilt.
     */
    private byte[] getNewPayload(int amountOfLeftEntries){
        if(amountOfLeftEntries > maxAmountOfRoutingTableEntries){
            return new byte[maxAmountOfRoutingTableEntries];
        }
        else {
            return new byte[amountOfLeftEntries];
        }
    }

    /**
     * Versendet ein RoutingTable Packet an den Nachbarn, mit den RoutingTables, die im payload enthalten sind.
     * @param entry Der RoutingTableEntry des Nachbarn, an den das Packet gesendet wird.
     * @param payload Der Payload der die RoutingTableEntries, die verschickt werden sollen, enthält.
     */
    private void sendRoutingTablePacket(RoutingEntry entry, byte[] payload) {
        BCPPacketImpl bcpPacket = new BCPPacketImpl(
                (byte) 1, //version
                PacketTypes.ROUTING_TABLE, //type
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
                entry.getNextHopPort()); //port
        DatagramPacket packet = bcpPacket.makeDatagramPacket();
        sendeQueue.add(packet);

        log.debug("RoutingTable packet send to {}", Long.toUnsignedString(entry.getNodeId()));
    }
}
