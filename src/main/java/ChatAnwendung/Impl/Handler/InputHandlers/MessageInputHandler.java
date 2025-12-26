package ChatAnwendung.Impl.Handler.InputHandlers;

import ChatAnwendung.Impl.Exceptions.InvalidMessageException;
import ChatAnwendung.Impl.Exceptions.NotAUIDException;
import ChatAnwendung.Impl.Exceptions.UnknowUIDException;
import ChatAnwendung.Impl.Handler.Common.ExceptionHandler;
import ChatAnwendung.Impl.MessageQueue;
import ChatAnwendung.Impl.PacketTypes;
import ChatAnwendung.Impl.RoutingTableImpl;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

@Slf4j
public class MessageInputHandler extends AbstractInputHandler {


    public MessageInputHandler(String[] command) {
        super(command);
    }

    @Override
    public void run()  {

        log.debug( "Begin with Message");
        long uID = 0;
        try {
            uID = Long.parseUnsignedLong(command[1]);
        } catch (NumberFormatException e) {
            ExceptionHandler.handle(new NotAUIDException(e.getMessage()), this.getClass());
            return;
        }
        String msg = command[2];



        if(!validUID(uID)) {
            ExceptionHandler.handle(new UnknowUIDException(uID), this.getClass());
            return;
        }
        else if(!validMessage(msg)){
            ExceptionHandler.handle(new InvalidMessageException(msg), this.getClass());
            return;
        }
        byte[] payload = msg.getBytes(StandardCharsets.UTF_8);
        InetAddress adress = RoutingTableImpl.getInstance().getNextHopAdressForUID(uID);
        int port = RoutingTableImpl.getInstance().getNextHopPortForUID(uID);

        DatagramPacket packet = makeDatagramPackage(PacketTypes.MESSAGE, uID, 0, 0, payload, adress, port);

        log.debug( "Message will be send");

        MessageQueue.getInstance().push(packet);
    }


    private boolean validUID(Long uID) {
        return RoutingTableImpl.getInstance().isUIDavailable(uID);
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
