package ChatAnwendung.Impl.Handler.InputHandlers;

import ChatAnwendung.Impl.Exceptions.LoginException;
import ChatAnwendung.Impl.Handler.ExceptionHandler;
import ChatAnwendung.Impl.MessageQueue;
import ChatAnwendung.Impl.PacketTypes;
import ChatAnwendung.Impl.Storage;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;

public class HelloInputHandler extends AbstractInputHandler {

    public HelloInputHandler(String[] command) {
        super(command, HelloInputHandler.class.getName());
    }

    @Override
    public void run() {
        if(Storage.getInstance().isLogin()){
             CompletableFuture.runAsync(new ExceptionHandler(new LoginException(), this.getClass()));
             return;
        }
        Storage.getInstance().login();

        try {
            InetAddress adress = InetAddress.getByAddress(new byte[] {(byte)255, (byte)255, (byte)255, (byte)255});

            if(!Storage.getInstance().isDebugMode()){
                for(int i = 1024; i < Math.pow(2, 16); i++){
                    byte[] payload = new byte[0];
                    DatagramPacket packet = makeDatagramPackage(PacketTypes.HELLO, Storage.getInstance().getBroadCastId(), 0, 0, payload, adress, i);
                    MessageQueue.getInstance().push(packet);
                }
            }
        } catch (UnknownHostException e) {
            CompletableFuture.runAsync(new ExceptionHandler(e, this.getClass()));
        }
    }


    public static String help(){

        return "hello: Der Hello command führt eine neu anmeldung durch. Dieser darf nur ausgeführt werden wenn man sich vorher abgemeldet hat mit den \"bye\" command.\n" +
                "\tAufbau: hello\n" +
                "\tFehler: Wenn man schon angemeldet ist, passiert nichts und dem User wird durch eine Nachricht in Kenntniss gesetzt\n";

    }
}
