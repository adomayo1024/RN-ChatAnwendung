package ChatAnwendung.Impl;

import ChatAnwendung.Api.RoutingTable;

import java.util.ArrayList;
import java.util.List;

public class RoutingTableImpl implements RoutingTable {

    private static List<RoutingEntryImpl> entries;

    public RoutingTableImpl() {
        entries = new ArrayList<>();
    }

    public void add(RoutingEntryImpl entry) {
        entries.add(entry);
    }

    public List<RoutingEntryImpl> getAllEntries(){
        return entries;
    }
}
