package ChatAnwendung.Impl.Handler.InputHandlers;

import ChatAnwendung.Api.RoutingEntry;
import ChatAnwendung.Api.RoutingTable;
import ChatAnwendung.Impl.Connection;
import ChatAnwendung.Impl.ConnectionsList;
import ChatAnwendung.Impl.RoutingTableImpl;

public class ListHandler extends AbstractInputHandler {
    public ListHandler(String[] split) {
        super(split, ListHandler.class.getName());
    }

    public static String help() {
        return "list";
    }

    @Override
    public void run(){
        boolean allFlagSet = false;
        boolean connectionFlagSet = false;

        for(String flag: command){
            switch (flag){
                case "--all":
                    allFlagSet = true;
                    break;
                case "--connect":
                    connectionFlagSet = true;
                    break;
                default:
            }
        }

        for(RoutingEntry entry : RoutingTableImpl.getInstance().getAllEntries()){
            if(entry.isRoutable() || allFlagSet){
                System.out.println(Long.toUnsignedString(entry.getUID()) + " Hops: " + entry.getHops());
            }
        }

        if(connectionFlagSet){
            for(Connection connection : ConnectionsList.getInstance().getAllConnections()){
                System.out.println(connection.address().toString() + ":" + connection.port());
            }
        }
    }
}
