package ChatAnwendung.Impl.Handler.InputHandlers;

import ChatAnwendung.Api.RoutingEntry;
import ChatAnwendung.Api.RoutingTable;
import ChatAnwendung.Impl.BCPPacket;
import ChatAnwendung.Impl.Exceptions.ArgumentException;
import ChatAnwendung.Impl.Exceptions.InvalidMessageException;
import ChatAnwendung.Impl.Exceptions.NotAUIDException;
import ChatAnwendung.Impl.Exceptions.UnknowUIDException;
import ChatAnwendung.Impl.Handler.Common.ExceptionHandler;
import ChatAnwendung.Impl.InputCommands;
import ChatAnwendung.Impl.MessageQueue;
import ChatAnwendung.Impl.PacketTypes;
import ChatAnwendung.Impl.persistence.Connection;
import ChatAnwendung.Impl.persistence.ConnectionsList;
import ChatAnwendung.Impl.persistence.RoutingTableImpl;
import ChatAnwendung.Impl.persistence.Storage;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;

@Slf4j
public class InputHandler implements Runnable {

    private final BlockingQueue<String> inputQueue;
    private final RoutingTable routingTable;
    private final ConnectionsList connectionList;
    private final Storage storage;
    private final BlockingQueue<DatagramPacket> senderQueue;


    public InputHandler(BlockingQueue<String> inputQueue, RoutingTable routingTabl, ConnectionsList connectionList, Storage storage, BlockingQueue<DatagramPacket> senderQueue) {
        this.inputQueue = inputQueue;
        this.routingTable = routingTabl;
        this.connectionList = connectionList;
        this.storage = storage;
        this.senderQueue = senderQueue;
    }

    @Override
    public void run() {

        boolean interrupted = false;
        String input;

        while (!interrupted){

            String[] command = new String[1];
            InputCommands commandType;

            try {
                input = inputQueue.take();
                command = input.split(" ");
                commandType = InputCommands.valueOf(command[0].toUpperCase());
            } catch (InterruptedException e) {
                interrupted = true;
                continue;
            } catch (IllegalArgumentException e){
                System.out.print("Unknown Command: " + command[0]);
                continue;
            }

            if(!commandType.isLogOutCommand()){
                continue;
            }

            switch (commandType){
                case InputCommands.CONNECT -> handleConnect(command);

                case InputCommands.DISCONNECT -> handleDisconnect(command);

                case InputCommands.HELLO -> handleHello(command);

                case InputCommands.GOODBYE -> handleGoodbye(command);

                case InputCommands.SEND -> handleSend(command);

                case InputCommands.FILE -> handleFile(command);

                case InputCommands.LIST -> handleList(command);

                case InputCommands.EXIT -> handleExit(command);

                case InputCommands.HELP -> handleHelp(command);
            }

        }

    }

    private void handleHelp(String[] command) {
        StringBuilder builder = new StringBuilder();

        if(command.length < 2){
            builder.append(HelpInputHandler.help());
            builder.append(exitHelp());
            builder.append(fileHelp());
            builder.append(goodbyeHelp());
            builder.append(helloHelp());
            builder.append(messageHelp());
            builder.append(connectHelp());
            builder.append(disconnectHelp());
            builder.append(listHelp());
        } else {
            switch (InputCommands.valueOf(command[1].toUpperCase())){
                case InputCommands.HELP-> builder.append(helpHelp());

                case InputCommands.EXIT -> builder.append(exitHelp());

                case InputCommands.FILE -> builder.append(fileHelp());

                case InputCommands.GOODBYE -> builder.append(goodbyeHelp());

                case InputCommands.HELLO -> builder.append(HelloInputHandler.help());

                case InputCommands.SEND -> builder.append(messageHelp());

                case InputCommands.CONNECT -> builder.append(ConnectHandler.help());

                case InputCommands.DISCONNECT -> builder.append(disconnectHelp());

                case InputCommands.LIST -> builder.append(listHelp());
            }
        }

        System.out.println(builder);
    }

    private String listHelp() {
        return null;
    }

    private String disconnectHelp() {
        return null;
    }

    private String connectHelp() {
        return "connect: Verbindet diesen User direkt mit einen anderen\n" +
                "\tAufbau: [ip-Adresse im Format xxx.xxx.xxx.xxx] [port]\n" +
                "\tFehler: ungültige Ip-Adresse oder port, ungültige Formatierung";

    }

    private String messageHelp() {
        return "send: Es wird eine Nachricht an einen bestimmten Teilnehmer geschickt. Die Nachricht darf maximal 1300 zeichen beinhalten (Weißzeichen mitgezählt)\n" +
                "\tAufbau: send [EmpfängerID] \"[Nachricht]\"\n" +
                "\tFehler: Wenn die UID falsch ist oder die Nachricht zu lange, wird keine Nachricht verschickt.\n";

    }

    private String helloHelp() {
        return null;
    }

    private String exitHelp() {
        return null;
    }

    private String helpHelp() {
        return null;
    }

    private String goodbyeHelp() {
        return null;
    }

    private String fileHelp() {
        return null;
    }

    private void handleExit(String[] command) {

    }

    private void handleList(String[] command) {
        log.debug("Start with list");

        boolean allFlagSet = false;
        boolean connectionFlagSet = false;

        for(String flag: command){
            switch (flag){
                case "--all":
                    allFlagSet = true;
                    break;
                case "--connect":
                    connectionFlagSet = true;
                    break;
                default:
            }
        }

        for(RoutingEntry entry : routingTable.getAllEntries()){
            if(entry.isRoutable() || allFlagSet){
                System.out.println(Long.toUnsignedString(entry.getUID()) + " Hops: " + entry.getHops() + " next Hop Address: " + entry.getNextHopAdress() +  " next Hop port: " + entry.getNextHopPort() + " is routable: " + entry.isRoutable());
            }
        }

        if(connectionFlagSet){
            for(Connection connection : connectionList.getAllConnections()){
                System.out.println(connection.address().toString() + ":" + connection.port());
            }
        }

        log.debug("End with list");
    }

    private void handleFile(String[] command) {
    }

    private void handleSend(String[] command) {
        log.debug("Start with Message sending");

        long destNodeId = 0;
        try {
            destNodeId = Long.parseUnsignedLong(command[1]);
        } catch (NumberFormatException e) {
            ExceptionHandler.handle(new NotAUIDException(e.getMessage()), this.getClass());
            return;
        }
        String msg = command[2];


        if(!validUID(destNodeId)) {
            ExceptionHandler.handle(new UnknowUIDException(destNodeId), this.getClass());
            return;
        }
        else if(!validMessage(msg)){
            ExceptionHandler.handle(new InvalidMessageException(msg), this.getClass());
            return;
        }


        byte[] payload = msg.getBytes(StandardCharsets.UTF_8);
        InetAddress adress = routingTable.getNextHopAdressForUID(destNodeId);
        int port = routingTable.getNextHopPortForUID(destNodeId);

        BCPPacket bcpPacket = new BCPPacket(
                (byte) 1,
                PacketTypes.MESSAGE,
                (byte) 32,
                (byte) 0,
                storage.getID(),
                destNodeId,
                0,
                0,
                0L,
                (short)payload.length,
                payload,
                adress,
                port);

        DatagramPacket packet = bcpPacket.makeDatagramPacket();


        senderQueue.add(packet);

        log.debug("Message send to {}", Long.toUnsignedString(destNodeId));
        log.info("Message send to {}", Long.toUnsignedString(destNodeId));
        System.out.println("Message send to " + Long.toUnsignedString(destNodeId));
    }

    private void handleGoodbye(String[] command) {
    }

    private void handleHello(String[] command) {
    }

    private void handleDisconnect(String[] command) {
        log.debug("start with disconnect: {} : {}", command[1], command[2]);

        InetAddress address;
        int port;

        try {
            address = InetAddress.getByName(command[1]);
            port = Integer.parseInt(command[2]);
            Connection connection = new Connection(address, port);

            connectionList.remove(connection);
        } catch (UnknownHostException e) {
            ExceptionHandler.handle(new ArgumentException(e.getMessage()), this.getClass());
        }

        log.info("Disconnect with: {}:{}", command[1], command[2]);
        System.out.println("Disconnect with: " + command[1] + ":" + command[2]);
        log.debug("end with disconnect: {} : {}", command[1], command[2]);

    }

    private void handleConnect(String[] command) {
        log.debug("start with connect: {} : {}", command[1], command[2]);

        InetAddress address;
        int port;

        try {
            address = InetAddress.getByName(command[1]);
            port = Integer.parseInt(command[2]);
            Connection connection = new Connection(address, port);

            connectionList.add(connection);
        } catch (UnknownHostException e) {
            ExceptionHandler.handle(new ArgumentException(e.getMessage()), this.getClass());
        }

        log.info("Connect with: {}:{}", command[1], command[2]);

        log.debug("end with connect: {} : {}", command[1], command[2]);
        log.info("Connect with: {}:{}", command[1], command[2]);
        System.out.println("Connect with: " + command[1] + ":" + command[2]);
    }


    private boolean validUID(Long uID) {
        return RoutingTableImpl.getInstance().isUIDavailable(uID);
    }

    private boolean validMessage(String msg){
        return msg.getBytes().length <= 1300;
    }
}
