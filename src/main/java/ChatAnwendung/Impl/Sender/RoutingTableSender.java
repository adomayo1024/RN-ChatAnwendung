package ChatAnwendung.Impl.Sender;

import ChatAnwendung.Api.RoutingEntry;
import ChatAnwendung.Impl.Handler.Common.AbstractHandler;
import ChatAnwendung.Impl.RoutingTableImpl;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class RoutingTableSender extends AbstractHandler{


    private final int routingTableEntrySize = 18;

    @Override
    public void run(){

        List<RoutingEntry> allEntries = RoutingTableImpl.getInstance().getAllEntries();
        List<RoutingEntry> directNeighboursEntries = RoutingTableImpl.getInstance().getAllDirectNeighbours();
        Map<RoutingEntry, List<RoutingEntry>> lol;

        int anzahlRoutingTablePacketsToOneUser = allEntries.size() / 54;

        for(int i = 0; i <= allEntries.size() / 54 ; i++){

            int anzhalEntries = 54;

            if(i >= anzahlRoutingTablePacketsToOneUser){
                anzhalEntries = allEntries.size() % 54;
            }

            byte[] payload = new byte[anzhalEntries];

            for(int j = 0; j < anzhalEntries; j++){
                RoutingEntry entry = allEntries.get(j);
                setRoutingTableEntry(entry, payload, j * routingTableEntrySize);
            }



        }

    }


    private void setRoutingTableEntry(RoutingEntry entry, byte[] payload, int pos){

    }
}
