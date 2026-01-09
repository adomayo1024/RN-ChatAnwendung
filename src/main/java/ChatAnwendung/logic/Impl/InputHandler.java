package ChatAnwendung.logic.Impl;

import ChatAnwendung.Exceptions.*;
import ChatAnwendung.persistence.Api.ConnectionList;
import ChatAnwendung.persistence.Api.RoutingEntry;
import ChatAnwendung.persistence.Api.RoutingTable;
import ChatAnwendung.Exceptions.ExceptionHandler;
import ChatAnwendung.logic.Enums.InputCommands;
import ChatAnwendung.logic.Enums.PacketTypes;
import ChatAnwendung.persistence.Api.Storage;
import ChatAnwendung.persistence.Impl.Connection;
import ChatAnwendung.persistence.Impl.ThreadPools;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class InputHandler implements Runnable {

    private static final int AMOUNT_OF_ARGUMENTS_FOR_CONNECT = 3;
    private static final int AMOUNT_OF_ARGUMENTS_FOR_DISCONNECT = 3;
    private static final int AMOUNT_OF_ARGUMENTS_FOR_SEND = 3;
    private static final int AMOUNT_OF_ARGUMENTS_FOR_FILE = 3;
    private static final int AMOUNT_OF_ARGUMENTS_FOR_HELLO = 1;
    private static final int AMOUNT_OF_ARGUMENTS_FOR_BYE = 1;
    private static final int AMOUNT_OF_ARGUMENTS_FOR_LIST = 1;
    private static final int AMOUNT_OF_ARGUMENTS_FOR_EXIT = 1;
    private static final int AMOUNT_OF_ARGUMENTS_FOR_INFO = 1;
    private static final int AMOUNT_OF_ARGUMENTS_FOR_HELP = 1;





    private final BlockingQueue<String> inputQueue;
    private final RoutingTable routingTable;
    private final ConnectionList connectionList;
    private final Storage storage;
    private final BlockingQueue<DatagramPacket> senderQueue;
    private final ThreadPools threadPools;


    public InputHandler(BlockingQueue<String> inputQueue, RoutingTable routingTabl, ConnectionList connectionList, Storage storage, BlockingQueue<DatagramPacket> senderQueue, ThreadPools threadPools) {
        this.inputQueue = inputQueue;
        this.routingTable = routingTabl;
        this.connectionList = connectionList;
        this.storage = storage;
        this.senderQueue = senderQueue;
        this.threadPools = threadPools;
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

            if(!storage.isLogin() && !commandType.isLogOutCommand()){
                ExceptionHandler.handle(new LoginException("You are not logged in"), this.getClass());
                continue;
            }
            if(!rightAmountOfArguments(command, commandType)){
                ExceptionHandler.handle(new ArgumentException("Wrong amount of arguments"), this.getClass());
                continue;
            }

            switch (commandType){
                case InputCommands.CONNECT -> handleConnect(command);

                case InputCommands.DISCONNECT -> handleDisconnect(command);

                case InputCommands.HELLO -> handleHello(command);

                case InputCommands.BYE -> handleGoodbye(command);

                case InputCommands.SEND -> handleSend(command);

                case InputCommands.FILE -> handleFile(command);

                case InputCommands.LIST -> handleList(command);

                case InputCommands.EXIT -> handleExit(command);

                case InputCommands.HELP -> handleHelp(command);

                case InputCommands.INFO -> handleInfo(command);
            }

        }

    }

    private String[] wrap(List<String> s) {
        boolean find = false;
        int pos = 0;
        List<Integer> removedPositions = new ArrayList<>();

        for(int i = 0; i < s.size(); i++){

            if(s.get(i).contains("\"")){
                if(!find){
                    pos = i;
                    s.set(i, "");
                } else {
                    removedPositions.add(i);
                }
                find =!find;
            } else {
                if(find){
                    s.set(pos, s.get(pos) + " " + s.get(i));
                    removedPositions.add(i);
                }
            }
        }

        for(Integer i : removedPositions.reversed()){
            s.remove(i.intValue());
        }


        return s.toArray(String[]::new);
    }

    private boolean rightAmountOfArguments(String[] command, InputCommands commandType) {

        boolean result = true;

        switch (commandType){
            case InputCommands.CONNECT -> {
                if(command.length != AMOUNT_OF_ARGUMENTS_FOR_CONNECT){
                    result = false;
                }
            }

            case InputCommands.DISCONNECT -> {
                if(command.length != AMOUNT_OF_ARGUMENTS_FOR_DISCONNECT){
                    result = false;
                }
            }

            case InputCommands.HELLO -> {
                if(command.length != AMOUNT_OF_ARGUMENTS_FOR_HELLO){
                    result = false;
                }
            }

            case InputCommands.BYE -> {
                if(command.length != AMOUNT_OF_ARGUMENTS_FOR_BYE){
                    result = false;
                }
            }

            case InputCommands.SEND -> {
                if(command.length < AMOUNT_OF_ARGUMENTS_FOR_SEND){
                    result = false;
                }
            }

            case InputCommands.FILE -> {
                if(command.length < AMOUNT_OF_ARGUMENTS_FOR_FILE){
                    result = false;
                }
            }

            case InputCommands.LIST -> {
                if(command.length != AMOUNT_OF_ARGUMENTS_FOR_LIST){
                    result = false;
                }
            }

            case InputCommands.EXIT -> {
                if(command.length != AMOUNT_OF_ARGUMENTS_FOR_EXIT){
                    result = false;
                }
            }

            case InputCommands.HELP -> {
                if(command.length < AMOUNT_OF_ARGUMENTS_FOR_HELP || command.length > AMOUNT_OF_ARGUMENTS_FOR_HELP + 1){
                    result = false;
                }
            }

            case InputCommands.INFO -> {
                if(command.length != AMOUNT_OF_ARGUMENTS_FOR_INFO){
                    result = false;
                }
            }
        }

        return result;
    }

    private void handleHelp(String[] command) {
        StringBuilder builder = new StringBuilder();

        if(command.length < 2){
            builder.append(helpHelp());
            builder.append(exitHelp());
            builder.append(fileHelp());
            builder.append(goodbyeHelp());
            builder.append(helloHelp());
            builder.append(messageHelp());
            builder.append(connectHelp());
            builder.append(disconnectHelp());
            builder.append(listHelp());
        } else {
            InputCommands commandType;

            try{
                commandType = InputCommands.valueOf(command[1].toUpperCase());
            } catch (IllegalArgumentException e){
                ExceptionHandler.handle(new ArgumentException("Unknown command: " + command[1]), this.getClass());
                return;
            }

            switch (commandType){
                case InputCommands.HELP-> builder.append(helpHelp());

                case InputCommands.EXIT -> builder.append(exitHelp());

                case InputCommands.FILE -> builder.append(fileHelp());

                case InputCommands.BYE -> builder.append(goodbyeHelp());

                case InputCommands.HELLO -> builder.append(helloHelp());

                case InputCommands.SEND -> builder.append(messageHelp());

                case InputCommands.CONNECT -> builder.append(connectHelp());

                case InputCommands.DISCONNECT -> builder.append(disconnectHelp());

                case InputCommands.LIST -> builder.append(listHelp());

                case InputCommands.INFO -> builder.append(infoHelp());
            }
        }

        System.out.println(builder);
    }

    private String infoHelp() {
        return """
                info: Gibt info über die eigene Id und auf welchen port man erreichbar ist.
                \tAufbau: info
                """;
    }

    private String listHelp() {
        return """
                list: Listet alle momentan erreichabren Nutzer auf, oder alle derzeitigen Connections
                \tAufbau: list <<--all>> <<--connect>>
                """;
    }

    private String disconnectHelp() {
        return """
                disconnect: Disconnect diese Anwendung mit einer physischen Addresse und Port.
                \tAufbau: disconnect [ip-Addresse im Format xxx.xxx.xxx.xxx] [port]
                \tFehler: ungültige Ip-Adresse oder port, ungültige Formatierung
                """;
    }

    private String connectHelp() {
        return """
                connect: Verbindet diesen User direkt mit einen anderen
                \tAufbau: connect [ip-Adresse im Format xxx.xxx.xxx.xxx] [port]
                \tFehler: ungültige Ip-Adresse oder port, ungültige Formatierung""";

    }

    private String messageHelp() {
        return """
                send: Es wird eine Nachricht an einen bestimmten Teilnehmer geschickt. Die Nachricht darf maximal 1300 zeichen beinhalten (Weißzeichen mitgezählt)
                \tAufbau: send [EmpfängerID] "[Nachricht]"
                \tFehler: Wenn die UID falsch ist oder die Nachricht zu lange, wird keine Nachricht verschickt.
                """;

    }

    private String helloHelp() {
        return """
                hello: Der Hello command führt eine neu anmeldung durch. Dieser darf nur ausgeführt werden wenn man sich vorher abgemeldet hat mit den "bye" command.
                \tAufbau: hello
                \tFehler: Wenn man schon angemeldet ist, passiert nichts und dem User wird durch eine Nachricht in Kenntniss gesetzt
                """;


    }

    private String exitHelp() {
        return """
                exit: Meldet den User ab und beendet das Programm kommplett
                \tAufbau: exit
                \tFehler:
                """;

    }

    private String helpHelp() {
        return """
                help: Gibt infos über die vorhanden Commands oder ausgewählt eines einzlenen.
                \tAufbau: help <<command>>
                """;
    }

    private String goodbyeHelp() {
        return """
                bye: Meldet den User ab, er kann keine Nachrichten mehr schicken oder empfangen
                \tAufbau: bye
                \tFehler: Wenn man schon abgemeldet ist, kann man sich nicht nochmal abmelden
                """;

    }

    private String fileHelp() {
        return """
                file: Verschickt eine Datei die angegeben ist an einen bestimmten User
                \tAufbau: file  "[absoluter Datei Pfad]" [User Id]
                \tFehler: Die angegeben Datei gibt es nicht, der angegeben User ist nicht bekannt.\s
                """;
    }

    private void handleExit(String[] command) {
        log.debug("Start with shutdown");

        try {
            if(storage.isLogin()){
                handleGoodbye(command);
            }
            System.in.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        log.debug("Initialization finished");

    }

    private void handleInfo(String[] command) {

        System.out.println("You have the ID: " + storage.getUnsignedID() + "\n" +
                "Port: " + storage.getPort());
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
            if(entry.getRoutable() || allFlagSet){
                System.out.println(Long.toUnsignedString(entry.getNodeId()) +
                        " | Hops: " + entry.getHops() +
                        " | next Hop Address: " + entry.getNextHopAddress() +
                        " | next Hop port: " +
                        entry.getNextHopPort() +
                        " | is routable: " + entry.getRoutable() +
                        "| last seen:" + (int) entry.getLastSeenShort() + "ms");
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
        log.debug("Start with file transfer");

        String path = command[1];

        long uID = 0;
        try {
            uID = Long.parseUnsignedLong(command[2]);
        } catch (NumberFormatException e) {
            ExceptionHandler.handle(new NotAUIDException(e.getMessage()), this.getClass());
            return;
        } catch (ArrayIndexOutOfBoundsException e) {
            ExceptionHandler.handle(new ArgumentException("Sender UID is missing"), this.getClass());
            return;
        }

        if(!validUID(uID))  {
            ExceptionHandler.handle(new UnknowUIDException(uID), this.getClass());
        }
        else {
            try (RandomAccessFile file = new RandomAccessFile(path, "r")){

                long length = file.length();
                int anzahlChunks = (int) Math.ceil(length / (float) BCPPacket.getMaximumFileSendingChunkSize());
                int fileId = storage.getNextFileID();
                InetAddress address = routingTable.getNextHopAdressForUID(uID);
                int port = routingTable.getNextHopPortForUID(uID);
                storage.addSendOpenFile(fileId, path);

                sendFileInitPacket(anzahlChunks, length, path, uID, fileId, address, port);

                log.debug("File init packet send");

                for(int sequenz = 0; sequenz < anzahlChunks; sequenz++){
                    byte[] payload = split(file, sequenz, anzahlChunks);
                    BCPPacket bcpPacket = new BCPPacket(
                            (byte) 1, //version
                            PacketTypes.FILE_DATA, //type
                            (byte) 32, // ttl
                            (byte) 0, // hops
                            storage.getID(), //srcNodId
                            uID, //destNodeId
                            sequenz, //sequenz
                            fileId, //fileId
                            0L, //crc
                            (short)payload.length, //payloadLength
                            payload, //payload
                            address, //address
                            port); //port

                    DatagramPacket packet = bcpPacket.makeDatagramPacket();
                    senderQueue.add(packet);

                    log.debug("File data packet number {} send", sequenz);
                }

                sendFileEnd(uID, fileId, address, port);
                System.out.println("File send");

                log.debug("End with file transfer");

                log.info("File send to User: {}", Long.toUnsignedString(uID));
                System.out.println("File send to User: " + Long.toUnsignedString(uID));
            } catch (IOException e) {
                ExceptionHandler.handle(e, this.getClass());
            }
        }
    }

    private byte[] split(RandomAccessFile file, int sequenz, int anzahlChunks) {
        byte[] chunk = null;

        try {
            if(anzahlChunks <= sequenz || sequenz < 0){
                throw new IllegalSequnzNumberException(sequenz);
            }
            else if(anzahlChunks - 1 == sequenz){
                int size = (int)(file.length() % BCPPacket.getMaximumFileSendingChunkSize());
                chunk = new byte[size];
            }
            else{
                chunk = new byte[BCPPacket.getMaximumFileSendingChunkSize()];
            }
            file.seek((long) sequenz * BCPPacket.getMaximumFileSendingChunkSize());
            file.read(chunk);
        } catch (IOException | IllegalSequnzNumberException e) {
            ExceptionHandler.handle(e, this.getClass());
        }
        return chunk;
    }

    private void sendFileEnd(long uID, int fileId, InetAddress address, int port) {
        byte[] payload = new byte[0];

        BCPPacket bcpPacket = new BCPPacket(
                (byte) 1, //version
                PacketTypes.File_End, //type
                (byte) 32, // ttl
                (byte) 0, // hops
                storage.getID(), //srcNodId
                uID, //destNodeId
                0, //sequenz
                fileId, //fileId
                0L, //crc
                (short)payload.length, //payloadLength
                payload, //payload
                address, //address
                port); //port

        DatagramPacket packet = bcpPacket.makeDatagramPacket();
        senderQueue.add(packet);
    }

    private void sendFileInitPacket(int anzahlChunks, long length, String path, long uID, int fileId, InetAddress address, int port) {
        byte[] payload = makeDataInitPayload(length, path);

        BCPPacket bcpPacket = new BCPPacket(
                (byte) 1, //version
                PacketTypes.FILE_INIT, //type
                (byte) 32, // ttl
                (byte) 0, // hops
                storage.getID(), //srcNodId
                uID, //destNodeId
                anzahlChunks, //sequenz
                fileId, //fileId
                0L, //crc
                (short)payload.length, //payloadLength
                payload, //payload
                address, //address
                port); //port

        DatagramPacket packet = bcpPacket.makeDatagramPacket();
        senderQueue.add(packet);
    }

    private byte[] makeDataInitPayload(long length, String path) {
        String[] splitPath;

        if(System.getProperty("os.name").toLowerCase().contains("win")){
            splitPath = path.split("\\\\");
        }
        else {
            splitPath = path.split("/");
        }

        String fileName = splitPath[splitPath.length - 1];
        byte[] payload = new byte[fileName.getBytes().length + 4];
        BCPPacket.addInt(0, (int)length, payload);
        System.arraycopy(fileName.getBytes(), 0, payload, 4, fileName.getBytes().length);
        return payload;
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
        StringBuilder msg = new StringBuilder(command[2]);
        for(int i = 3; i < command.length; i++){
            msg.append(" ").append(command[i]);
        }


        if(!validUID(destNodeId)) {
            ExceptionHandler.handle(new UnknowUIDException(destNodeId), this.getClass());
            return;
        }
        else if(!validMessage(msg.toString())){
            ExceptionHandler.handle(new InvalidMessageException(msg.toString()), this.getClass());
            return;
        }


        byte[] payload = msg.toString().getBytes(StandardCharsets.UTF_8);
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
        log.debug("Start logout");

        threadPools.getScheduleServicesFuture().cancel(true);
        storage.logout();
        threadPools.setScheduleServicesFuture(null);

        log.debug("Finished with canceling heartbeats and timeout");

        for (RoutingEntry neighbour : routingTable.getAllDirectNeighbours()) {

            if(routingTable.isUIDavailable(neighbour.getNodeId())){
                byte[] payload = new byte[0];
                BCPPacket bcpPacket = new BCPPacket(
                        (byte) 1,
                        PacketTypes.BYE,
                        (byte) 32,
                        (byte) 0,
                        storage.getID(),
                        neighbour.getNodeId(),
                        0,
                        0,
                        0L,
                        (short)payload.length,
                        payload,
                        neighbour.getNextHopAddress(),
                        neighbour.getNextHopPort());

                DatagramPacket packet = bcpPacket.makeDatagramPacket();

               senderQueue.add(packet);

                log.debug("Goodbye packet send to {}", Long.toUnsignedString(neighbour.getNodeId()));
            }
        }

        log.info("Logout successful");
        System.out.println("Logout successful");

        routingTable.removeAll();
    }

    private void handleHello(String[] command) {

        if(storage.isLogin()){
            ExceptionHandler.handle(new LoginException("Your are already logged in"), this.getClass());
        }
        else {
            storage.login();

            for (Connection connection : connectionList.getAllConnections()) {
                byte[] payload = new byte[0];
                BCPPacket bcpPacket = new BCPPacket(
                        (byte) 1, //version
                        PacketTypes.HELLO, //type
                        (byte) 1, // ttl
                        (byte) 0, // hops
                        storage.getID(), //srcNodId
                        storage.getBroadCastId(), //destNodeId
                        0, //sequenz
                        0, //fileId
                        0L, //crc
                        (short)payload.length, //payloadLength
                        payload, //payload
                        connection.address(), //address
                        connection.port()); //port

                DatagramPacket packet = bcpPacket.makeDatagramPacket();

                senderQueue.add(packet);

                log.debug("Hello packet send to {}", connection.address());
            }

            threadPools.setScheduleServicesFuture(threadPools.getScheduleServices().scheduleWithFixedDelay(new loopServices(routingTable, storage, senderQueue), 1, 5, TimeUnit.SECONDS));
            log.debug("Finished with login");

            log.info("Login successful");
            System.out.println("Login successful");
        }

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

            log.info("Disconnect with: {}:{}", command[1], command[2]);
            System.out.println("Disconnect with: " + command[1] + ":" + command[2]);
            log.debug("end with disconnect: {} : {}", command[1], command[2]);
        } catch (UnknownHostException | NumberFormatException e ) {
            ExceptionHandler.handle(new ArgumentException(e.getMessage()), this.getClass());
        }

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

            log.info("Connect with: {}:{}", command[1], command[2]);
            log.debug("end with connect: {} : {}", command[1], command[2]);
            System.out.println("Connect with: " + command[1] + ":" + command[2]);
        } catch (UnknownHostException | NumberFormatException e) {
            ExceptionHandler.handle(new ArgumentException(e.getMessage()), this.getClass());
        }
    }


    private boolean validUID(Long uID) {
        return routingTable.isUIDavailable(uID);
    }

    private boolean validMessage(String msg){
        return msg.getBytes().length <= BCPPacket.getMaximumFileSendingChunkSize();
    }
}
