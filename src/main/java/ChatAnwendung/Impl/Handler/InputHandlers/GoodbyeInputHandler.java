package ChatAnwendung.Impl.Handler.InputHandlers;

import ChatAnwendung.Api.RoutingEntry;
import ChatAnwendung.Impl.MessageQueue;
import ChatAnwendung.Impl.PacketTypes;
import ChatAnwendung.Impl.RoutingTableImpl;
import ChatAnwendung.Impl.Storage;

import java.net.DatagramPacket;

public class GoodbyeInputHandler extends AbstractInputHandler {
    public GoodbyeInputHandler(String[] command) {
        super(command, GoodbyeInputHandler.class.getName());
    }

    @Override
    public void run() {

        for (RoutingEntry neighbour : RoutingTableImpl.getInstance().getAllDirectNeighbours()) {

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
        }
        Storage.getInstance().logout();
    }

    public static String help(){
        return "bye: Meldet den User ab, er kann keine Nachrichten mehr schicken oder empfangen\n" +
                "\tAufbau: bye\n" +
                "\tFehler: Wenn man schon abgemeldet ist, kann man sich nicht nochmal abmelden\n";
    }
}
