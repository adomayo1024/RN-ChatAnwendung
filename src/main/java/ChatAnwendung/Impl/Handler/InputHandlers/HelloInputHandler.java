package ChatAnwendung.Impl.Handler.InputHandlers;

import ChatAnwendung.Api.RoutingEntry;
import ChatAnwendung.Impl.*;
import ChatAnwendung.Impl.Exceptions.LoginException;
import ChatAnwendung.Impl.Handler.Common.ExceptionHandler;

import java.net.DatagramPacket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HelloInputHandler extends AbstractInputHandler {

    public HelloInputHandler(String[] command) {
        super(command, HelloInputHandler.class.getName());
    }

    @Override
    public void run() {
        if(Storage.getInstance().isLogin()){
             ExceptionHandler.handle(new LoginException(), this.getClass());
        }
        else {
            Storage.getInstance().login();

            //InetAddress adress = InetAddress.getByAddress(new byte[] {(byte)255, (byte)255, (byte)255, (byte)255});

            for(Connection connection : ConnectionsList.getInstance().getAllConnections()){
                byte[] payload = new byte[0];
                DatagramPacket packet = makeDatagramPackage(PacketTypes.HELLO, Storage.getInstance().getBroadCastId(), 0, 0, payload, connection.address(), connection.port());
                MessageQueue.getInstance().push(packet);
            }

            ThreadPools.getInstance().setHeartBeatTimerFuture(ThreadPools.getInstance().getHeartBeatTimer().scheduleWithFixedDelay(new HearbeatSender(), 1, 5, TimeUnit.SECONDS));
            ThreadPools.getInstance().setTimeoutFuture(CompletableFuture.runAsync(new TimeoutHandler(), ThreadPools.getInstance().getThreadPool()));
        }
    }


    public static String help(){

        return "hello: Der Hello command führt eine neu anmeldung durch. Dieser darf nur ausgeführt werden wenn man sich vorher abgemeldet hat mit den \"bye\" command.\n" +
                "\tAufbau: hello\n" +
                "\tFehler: Wenn man schon angemeldet ist, passiert nichts und dem User wird durch eine Nachricht in Kenntniss gesetzt\n";

    }
}
