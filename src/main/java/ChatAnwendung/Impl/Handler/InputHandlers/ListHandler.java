package ChatAnwendung.Impl.Handler.InputHandlers;

import ChatAnwendung.Api.RoutingEntry;
import ChatAnwendung.Impl.Connection;
import ChatAnwendung.Impl.ConnectionsList;
import ChatAnwendung.Impl.persistence.RoutingTableImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ListHandler extends AbstractInputHandler {
    public ListHandler(String[] split) {
        super(split);
    }

    public static String help() {
        return "list";
    }

    @Override
    public void run(){

        log.debug("Start with list");

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
                System.out.println(Long.toUnsignedString(entry.getUID()) + " Hops: " + entry.getHops() + " is routable: " + entry.isRoutable());
            }
        }

        if(connectionFlagSet){
            for(Connection connection : ConnectionsList.getInstance().getAllConnections()){
                System.out.println(connection.address().toString() + ":" + connection.port());
            }
        }

        log.debug("End with list");
    }
}
