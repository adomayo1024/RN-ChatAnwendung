package ChatAnwendung.Impl.Handler.InputHandlers;

import ChatAnwendung.Api.RoutingEntry;
import ChatAnwendung.Impl.*;
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

        ThreadPools.getInstance().getHeartBeatTimerFuture().cancel(false);
        ThreadPools.getInstance().getTimeoutFuture().cancel(true);
        Storage.getInstance().logout();
        ThreadPools.getInstance().setHeartBeatTimerFuture(null);
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

                log.debug("Goodbye packet send to {}", neighbour.getUID());
            }
        }

        RoutingTableImpl.getInstance().removeAllExceptHops1();
    }

    public static String help(){
        return "bye: Meldet den User ab, er kann keine Nachrichten mehr schicken oder empfangen\n" +
                "\tAufbau: bye\n" +
                "\tFehler: Wenn man schon abgemeldet ist, kann man sich nicht nochmal abmelden\n";
    }
}
