package ChatAnwendung.Impl.FrequentlySender;

import ChatAnwendung.Api.RoutingEntry;
import ChatAnwendung.Impl.Handler.Common.AbstractHandler;
import ChatAnwendung.Impl.Header;
import ChatAnwendung.Impl.MessageQueue;
import ChatAnwendung.Impl.PacketTypes;
import ChatAnwendung.Impl.persistence.RoutingTableImpl;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class RoutingTableSender extends AbstractHandler{


    private final int routingTableEntrySize = 17;
    private final int maxAmountOfRoutingTableEntries = 54;

    @Override
    public void run(){

        log.debug("Start with RoutingTable sending");

        List<RoutingEntry> allEntries = RoutingTableImpl.getInstance().getAllEntries();
        List<RoutingEntry> directNeighboursEntries = RoutingTableImpl.getInstance().getAllDirectNeighbours();
        Map<RoutingEntry, byte[]> allRoutingTablePackets = new HashMap<>();

        for(RoutingEntry entry : allEntries){
            byte[] routingEntryPayload = new byte[routingTableEntrySize];

            Header.addLong(0, entry.getUID(), routingEntryPayload);
            routingEntryPayload[8] = entry.getHops();
            Header.addLong(9, entry.getLastSeen(), routingEntryPayload);

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

                    DatagramPacket packet = makeDatagramPackage(PacketTypes.ROUTINGTABLE,
                            entry.getUID(),
                            0,
                            0,
                            payload,
                            entry.getNextHopAdress(),
                            entry.getNextHopPort()
                    );
                    MessageQueue.getInstance().push(packet);

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

            DatagramPacket packet = makeDatagramPackage(PacketTypes.ROUTINGTABLE,
                    entry.getUID(),
                    0,
                    0,
                    payload,
                    entry.getNextHopAdress(),
                    entry.getNextHopPort()
            );
            MessageQueue.getInstance().push(packet);
            log.debug("RoutingTable packet send to {}", Long.toUnsignedString(entry.getUID()));

            log.debug("Finished with sending RoutingTable to: {}", entry.getUID());
        }

        log.debug("Finished with RoutingTable sending");
    }
}
