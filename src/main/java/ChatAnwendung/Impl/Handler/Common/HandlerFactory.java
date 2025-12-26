package ChatAnwendung.Impl.Handler.Common;

import ChatAnwendung.Api.Handler;
import ChatAnwendung.Impl.Exceptions.LoginException;
import ChatAnwendung.Impl.Handler.InputHandlers.*;
import ChatAnwendung.Impl.Header;
import ChatAnwendung.Impl.PacketTypes;
import ChatAnwendung.Impl.Handler.RecieverHandlers.*;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;


@Slf4j
public class HandlerFactory {

    public static Handler getInputHandler(String stdIn) {

        String[] split = splitInput(stdIn);
        String command = split[0];

        Handler handler;
        switch (command) {
            case "exit":
                handler = new ExitInputHandler(split);
                log.debug("Input: exit");
                break;
            case "send":
                handler = new MessageInputHandler(split);
                log.debug( "Input: send");
                break;
            case "file":
                handler = new FileInputHandler(split);
                log.debug( "Input: file");
                break;
            case "bye":
                handler = new GoodbyeInputHandler(split);
                log.debug( "Input: bye");
                break;
            case "hello":
                handler = new HelloInputHandler(split);
                log.debug( "Input: hello");
                break;
            case "help":
                handler = new HelpInputHandler(split);
                log.debug( "Input: help");
                break;
            case "connect":
                handler = new ConnectHandler(split);
                log.debug( "Input: connect");
                break;
            case "disconnect":
                handler = new DisconnectHandler(split);
                log.debug( "Input: disconnect");
                break;
            case "list":
                handler = new ListHandler(split);
                log.debug( "Input: list");
                break;
            case "":
                handler = new ExceptionHandler(new LoginException(), HandlerFactory.class);
                log.debug( "Input: empty");
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
                    log.debug( "Revieced: File Init");
                }
                case File_End -> {
                    handler = new FileEndRecieveHandler(packet);
                    log.debug( "Revieced: File End");
                }
                case MESSAGE -> {
                    handler = new MessageRecieveHandler(packet);
                    log.debug( "Revieced: Message");
                }
                case FILE_DATA -> {
                    handler = new FileDataRecieveHandler(packet);
                    log.debug( "Revieced: File Data");
                }
                case RESENDREQUEST -> {
                    handler = new RequestRecieveHandler(packet);
                    log.debug( "Revieced: Resend Request");
                }
                case ROUTINGTABLE -> {
                    handler = new RoutingTableRecievetHandler(packet);
                    log.debug( "Revieced: Routing Table");
                }
                case HELLO -> {
                    handler = new HelloRecieveHandler(packet);
                    log.debug( "Revieced: Hello");
                }
                case WELCOME -> {
                    handler = new WelcomeRecieveHandler(packet);
                    log.debug( "Revieced: Welcome");
                }
                case GOODBYE -> {
                    handler = new GoodbyeRecieveHandler(packet);
                    log.debug( "Revieced: Goodbye");
                }
                case HEARTBEAT -> {
                    handler = new HeartbeatRecieveHandler(packet);
                    log.debug( "Revieced: Heartbeat");
                }
                default -> {
                    handler = null;
                }
            }
        }

        return handler;
    }
}
