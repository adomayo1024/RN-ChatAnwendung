package ChatAnwendung.Impl.Handler.InputHandlers;

import ChatAnwendung.Impl.*;
import ChatAnwendung.Impl.Exceptions.LoginException;
import ChatAnwendung.Impl.Handler.Common.ExceptionHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class HelloInputHandler extends AbstractInputHandler {

    public HelloInputHandler(String[] command) {
        super(command);
    }

    @Override
    public void run() {

        log.debug("Start login");

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

                log.debug("Hello packet send to {}", connection.address());
            }

            ThreadPools.getInstance().setHeartBeatTimerFuture(ThreadPools.getInstance().getHeartBeatTimer().scheduleWithFixedDelay(new HearbeatSender(), 1, 5, TimeUnit.SECONDS));
            ThreadPools.getInstance().setTimeoutFuture(CompletableFuture.runAsync(new TimeoutHandler(), ThreadPools.getInstance().getThreadPool()));
            log.debug("Finished with login");
        }
    }


    public static String help(){

        return "hello: Der Hello command führt eine neu anmeldung durch. Dieser darf nur ausgeführt werden wenn man sich vorher abgemeldet hat mit den \"bye\" command.\n" +
                "\tAufbau: hello\n" +
                "\tFehler: Wenn man schon angemeldet ist, passiert nichts und dem User wird durch eine Nachricht in Kenntniss gesetzt\n";

    }
}
