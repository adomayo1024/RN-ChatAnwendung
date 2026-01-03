package ChatAnwendung.Impl.Handler.InputHandlers;

import ChatAnwendung.Api.RoutingEntry;
import ChatAnwendung.Impl.*;
import ChatAnwendung.Impl.persistence.RoutingTableImpl;
import ChatAnwendung.Impl.persistence.Storage;
import ChatAnwendung.Impl.persistence.ThreadPools;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;

@Slf4j
public class GoodbyeInputHandler extends AbstractInputHandler {
    public GoodbyeInputHandler(String[] command) {
        super(command);
    }

    @Override
    public void run() {

        log.debug("Start logout");

        ThreadPools.getInstance().getHeartBeatAndRoutingTableTimerFuture().cancel(true);
        ThreadPools.getInstance().getTimeoutFuture().cancel(true);
        Storage.getInstance().logout();
        ThreadPools.getInstance().setHeartBeatAndRoutingTableTimerFuture(null);
        ThreadPools.getInstance().setTimeoutFuture(null);

        log.debug("Finished with canceling heartbeats and timeout");

        for (RoutingEntry neighbour : RoutingTableImpl.getInstance().getAllDirectNeighbours()) {

            if(RoutingTableImpl.getInstance().isUIDavailable(neighbour.getUID())){
                byte[] payload = new byte[0];
                DatagramPacket packet = makeDatagramPackage(
                        PacketTypes.GOODBYE,
                        Storage.getInstance().getBroadCastId(),
                        0,
                        0,
                        payload,
                        neighbour.getNextHopAdress(),
                        neighbour.getNextHopPort());
                MessageQueue.getInstance().pushAtFirst(packet);

                log.debug("Goodbye packet send to {}", Long.toUnsignedString(neighbour.getUID()));
            }
        }

        System.out.println("Logout successful");

        RoutingTableImpl.getInstance().removeAll();
    }

    public static String help(){
        return "bye: Meldet den User ab, er kann keine Nachrichten mehr schicken oder empfangen\n" +
                "\tAufbau: bye\n" +
                "\tFehler: Wenn man schon abgemeldet ist, kann man sich nicht nochmal abmelden\n";
    }
}
