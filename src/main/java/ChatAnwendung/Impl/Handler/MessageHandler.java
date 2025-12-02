package ChatAnwendung.Impl.Handler;

import ChatAnwendung.Impl.Exceptions.InvalidMessageException;
import ChatAnwendung.Impl.Exceptions.UnknowUIDException;
import ChatAnwendung.Impl.PacketTypes;
import ChatAnwendung.Impl.Storage;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class MessageHandler extends AbstractHandler {


    public MessageHandler(String[] command) {
        super(command, MessageHandler.class.getName());
    }

    @Override
    public void run()  {

        logger.log(Level.INFO, "Begin with Message");
        long uID = Long.parseLong(command[1]);
        String msg = command[2];

        //TODO richtiges Exception handling betreiben
        try {
            if(!validUID(uID)) {
                throw new UnknowUIDException(uID);
            }
            if(!validMessage(msg)){
                throw new InvalidMessageException(msg);
            }
        } catch (UnknowUIDException e) {
            throw new RuntimeException(e);
        } catch (InvalidMessageException e) {
            throw new RuntimeException(e);
        }

        byte[] payload = msg.getBytes(StandardCharsets.UTF_8);
        InetAddress adress = Storage.getInstance().getnextHopAdressForUid(uID);
        int port = Storage.getInstance().getNextHopPortForUID(uID);

        DatagramPacket packet = makeDatagramPackage(PacketTypes.MESSAGE, uID, 0, 0, payload, adress, port);

        logger.log(Level.INFO, "Message will be send");

        Storage.getInstance().addSendPackage(packet);
    }


    private boolean validUID(Long uID) {
        return Storage.getInstance().isUIDavailable(uID);
    }

    private boolean validMessage(String msg){
        return msg.getBytes().length <= 1300;
    }



    public static String help()  {

        return "send: Es wird eine Nachricht an einen bestimmten Teilnehmer geschickt. Die Nachricht darf maximal 1300 zeichen beinhalten (Weißzeichen mitgezählt)\n" +
                "\tAufbau: send [EmpfängerID] \"[Nachricht]\"\n" +
                "\tFehler: Wenn die UID falsch ist oder die Nachricht zu lange, wird keine Nachricht verschickt.\n";
    }
}
