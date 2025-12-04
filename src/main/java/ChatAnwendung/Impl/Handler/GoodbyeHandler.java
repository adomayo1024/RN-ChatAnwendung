package ChatAnwendung.Impl.Handler;

import ChatAnwendung.Api.Handler;
import ChatAnwendung.Api.RoutingEntry;
import ChatAnwendung.Impl.PacketTypes;
import ChatAnwendung.Impl.RoutingTableImpl;
import ChatAnwendung.Impl.Storage;

import javax.security.auth.login.LoginException;
import java.net.DatagramPacket;
import java.util.concurrent.CompletableFuture;

public class GoodbyeHandler extends AbstractHandler {
    public GoodbyeHandler(String[] command) {
        super(command, GoodbyeHandler.class.getName());
    }

    @Override
    public void run() {

        try {
            if (!Storage.getInstance().isLogin()) {
                throw new LoginException();
            }
        }catch (LoginException e) {
            CompletableFuture.runAsync(new ExceptionHandler(e, this.getClass()));
            return;
        }

        for (RoutingEntry neighbour : RoutingTableImpl.getInstance().getAllDirectNeighbours()) {

            byte[] payload = new byte[0];
            DatagramPacket packet = makeDatagramPackage(
                    PacketTypes.GOODBYE,
                    Storage.getInstance().getBroadCastId(),
                    0,
                    0,
                    payload,
                    neighbour.getAdress(),
                    neighbour.getPort());
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
