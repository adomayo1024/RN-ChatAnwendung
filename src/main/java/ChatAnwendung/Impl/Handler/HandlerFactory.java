package ChatAnwendung.Impl.Handler;

import ChatAnwendung.Api.Handler;
import ChatAnwendung.Impl.Exceptions.LoginException;
import ChatAnwendung.Impl.Handler.InputHandlers.*;
import ChatAnwendung.Impl.Header;
import ChatAnwendung.Impl.PacketTypes;
import ChatAnwendung.Impl.Handler.RecieverHandlers.*;

import java.net.DatagramPacket;


public class HandlerFactory {

    public static Handler getInputHandler(String stdIn) {

        String[] split = splitInput(stdIn);
        String command = split[0];

        Handler handler;
        switch (command) {
            case "exit":
                handler = new ExitInputHandler(split);
                break;
            case "send":
                handler = new MessageInputHandler(split);
                break;
            case "file":
                handler = new FileInputHandler(split);
                break;
            case "bye":
                handler = new GoodbyeInputHandler(split);
                break;
            case "hello":
                handler = new HelloInputHandler(split);
                break;
            case "help":
                handler = new HelpInputHandler(split);
                break;
            case "connect":
                handler = new ConnectHandler(split);
                break;
            case "disconnect":
                handler = new DisconnectHandler(split);
                break;
            case "list":
                handler = new ListHandler(split);
            case "":
                handler = new ExceptionHandler(new LoginException(), HandlerFactory.class);
                break;
            default:
                handler = new WrongCommandInputHandler(split);
                break;
        }
        return handler;
    }

    private static String[] splitInput(String stdIn){
        String[] split = stdIn.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].replaceAll("^\"|\"$", "");
        }

        return split;
    }


    public static Runnable getRecieverHandler(DatagramPacket packet, boolean isItForMe) {
        PacketTypes type = PacketTypes.values()[packet.getData()[Header.getTypePos()]];
        Handler handler;
        if(!isItForMe){
         handler = new FeedForwadingHanlder(packet);
        }
        else{
            switch (type){
                case FILE_INIT -> {
                    handler = new FileInitRecieveHandler(packet);
                }
                case File_End -> {
                    handler = new FileEndRecieveHandler(packet);
                }
                case MESSAGE -> {
                    handler = new MessageRecieveHandler(packet);
                }
                case FILE_DATA -> {
                    handler = new FileDataRecieveHandler(packet);
                }
                case RESENDREQUEST -> {
                    handler = new RequestRecieveHandler(packet);
                }
                case ROUTINGTABLE -> {
                    handler = new RoutingTableRecievetHandler(packet);
                }
                case HELLO -> {
                    handler = new HelloRecieveHandler(packet);
                }
                case WELCOME -> {
                    handler = new WelcomeRecieveHandler(packet);
                }
                case GOODBYE -> {
                    handler = new GoodbyeRecieveHandler(packet);
                }
                case HEARTBEAT -> {
                    handler = new HeartbeatRecieveHandler(packet);
                }
                default -> {
                    handler = null;
                }
            }
        }

        return handler;
    }
}
