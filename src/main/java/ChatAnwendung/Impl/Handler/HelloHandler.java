package ChatAnwendung.Impl.Handler;

import ChatAnwendung.Impl.PacketTypes;
import ChatAnwendung.Impl.Storage;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class HelloHandler extends AbstractHandler {

    public HelloHandler(String[] command) {
        super(command, HelloHandler.class.getName());
    }

    @Override
    public void run() {

        try {
            InetAddress adress = InetAddress.getByAddress(new byte[] {(byte)255, (byte)255, (byte)255, (byte)255});

            for(int i = 1024; i < Math.pow(2, 16); i++){
                byte[] payload = new byte[0];
                DatagramPacket packet = makeDatagramPackage(PacketTypes.HELLO, -1, 0, 0, payload, adress, i);
                Storage.getInstance().addSendPackage(packet);
            }
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }


    public static String help(){

        return "hello: Der Hello command führt eine neu anmeldung durch. Dieser darf nur ausgeführt werden wenn man sich vorher abgemeldet hat mit den \"bye\" command.\n" +
                "\tAufbau: hello\n" +
                "\tFehler: Wenn man schon angemeldet ist, passiert nichts und dem User wird durch eine Nachricht in Kenntniss gesetzt\n";

    }
}
