package ChatAnwendung.logic.Impl;

import ChatAnwendung.Exceptions.*;
import ChatAnwendung.logic.Api.InputHandler;
import ChatAnwendung.persistence.Api.ConnectionList;
import ChatAnwendung.persistence.Api.RoutingEntry;
import ChatAnwendung.persistence.Api.RoutingTable;
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
public class InputHandlerImpl implements InputHandler {

    // Die Anzahl an Argumenten, die erwartet werden, für die jeweiligen Commands.
    //Bei Send, File und Help ist es die Mindestanzahl
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




    //Die Inputqueue wo die Commands landen, die vom InputHandler empfangen werden.
    private final BlockingQueue<String> inputQueue;

    //Die RoutingTable, um nachzuschauen, wo die Pakete hin verschickt werden sollen
    private final RoutingTable routingTable;

    //Die ConnectionList, um nachzuschauen, welche Connections aktuell da sind, und um welche hinzuzufügen oder zu entfernen.
    private final ConnectionList connectionList;

    //Der storage um Informationen über sich selbst zu erhalten und zu speicher.
    private final Storage storage;

    //Die Sendequeue damit der Sender, die zu versenden Packete versendet.
    private final BlockingQueue<DatagramPacket> senderQueue;

    //Die ThreadPools, um Task zu starten und zu stoppen.
    private final ThreadPools threadPools;

    private boolean finished;


    public InputHandlerImpl(BlockingQueue<String> inputQueue, RoutingTable routingTable, ConnectionList connectionList, Storage storage, BlockingQueue<DatagramPacket> senderQueue, ThreadPools threadPools) {
        this.inputQueue = inputQueue;
        this.routingTable = routingTable;
        this.connectionList = connectionList;
        this.storage = storage;
        this.senderQueue = senderQueue;
        this.threadPools = threadPools;
        this.finished = false;
    }

    @Override
    public void run() {

        String input;

        //Loop wo Inputs verarbeitet werden.
        while (!finished){

            String[] command = new String[1];
            InputCommands commandType;

            //Es wird sich der nächste Input geholt.
            try {
                input = inputQueue.take();
                command = input.split(" ");
                commandType = InputCommands.valueOf(command[0].toUpperCase());
            } catch (InterruptedException e) {
                //Loop für das Verarbeiten von Inputs soll beendet werden.
                finished = true;
                continue;
            } catch (IllegalArgumentException e){
                log.info("Unknown Command: {}", command[0]);
                System.out.print("Unknown Command: " + command[0]);
                continue;
            }

            //Wenn man nicht eingeloggt ist, dürfen nicht alle Command benutzt werden.
            if(!storage.isLogin() && !commandType.isLogOutCommand()){
                ExceptionHandler.handle(new LoginException("You are not logged in"), this.getClass());
                continue;
            }
            //Prüft, ob die Anzahl der Argumente korrekt ist, für den jeweiligen Command stimmt.
            if(!rightAmountOfArguments(command, commandType)){
                ExceptionHandler.handle(new ArgumentException("Wrong amount of arguments"), this.getClass());
                continue;
            }

            //Verschiedene Verarbeitung für die jeweiligen Commands.
            switch (commandType){
                case InputCommands.CONNECT -> handleConnect(command);

                case InputCommands.DISCONNECT -> handleDisconnect(command);

                case InputCommands.HELLO -> handleHello(command);

                case InputCommands.BYE -> handleBye(command);

                case InputCommands.SEND -> handleSend(command);

                case InputCommands.FILE -> handleFile(command);

                case InputCommands.LIST -> handleList(command);

                case InputCommands.EXIT -> handleExit(command);

                case InputCommands.HELP -> handleHelp(command);

                case InputCommands.INFO -> handleInfo(command);
            }

        }

        log.info("InputHandler finished with work");

    }

    /**
     * Prüft für den gegebenen {@code commandType} ob die Anzahl an Argumenten in {@code command} korrekt ist.
     * Der Command selber wird dabei mitgezählt.
     * @param command Der Command mit den Argument
     * @param commandType Der Typ des Commands
     * @return True, wenn die Anzahl der Argumente korrekt ist, sonst false.
     */
    private boolean rightAmountOfArguments(String[] command, InputCommands commandType) {

        boolean result = true;

        //Überprüfung der Anzahl der Commands anhand des CommandTyps
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
                if(command.length < AMOUNT_OF_ARGUMENTS_FOR_LIST){
                    result = false;
                }
            }

            case InputCommands.EXIT -> {
                if(command.length != AMOUNT_OF_ARGUMENTS_FOR_EXIT){
                    result = false;
                }
            }

            case InputCommands.HELP -> {
                if(command.length < AMOUNT_OF_ARGUMENTS_FOR_HELP){
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

    /**
     * Verarbeitet den Help Command. Welcher auf der Konsole Informationen zu allen verfügbaren Commands ausgibt.
     * Man kann als Argumente auch einzelnen Command dazugeben, damit nur Informationen zu diesem Command ausgegeben werden
     * und nicht zu allen.
     * @param command Der Input des Users, welcher mit einem Help Command beginnt.
     */
    private void handleHelp(String[] command) {
        StringBuilder builder = new StringBuilder();

        // Wenn keine weiteren Argumente, printe zu allen Commands die Informationen aus.
        if(command.length < 2){
            builder.append(InputCommands.getAllHelpTexts());
        } else {
            InputCommands commandType;

            //Gehe alle Argumente durch und printe die Informationen zu diesen Commands.
            for (int i = 1; i < command.length; i++) {

                try {
                    // welcher Command als nächstes Argument
                    commandType = InputCommands.valueOf(command[i].toUpperCase());
                } catch (IllegalArgumentException e) {
                    // Unbekannter Command, weiter mit dem nächsten Argument
                    ExceptionHandler.handle(new ArgumentException("Unknown command: " + command[i]), this.getClass());
                    continue;
                }

                builder.append(commandType.getHelpText());
            }
        }

        log.info(builder.toString());
    }

    /**
     * Verarbeitet den Exit Command, wo das Programm beendet wird.
     * Wenn man noch angemeldet ist, wird erstmal {@code handleGoodbye()} ausgeführt, damit man abgemeldet wird.
     * @param command Der Command der Exit enthält
     */
    private void handleExit(String[] command) {
        log.debug("Start with shutdown");

        // Wenn der User noch angemeldet ist, wird er erstmal abgemeldet.
        if(storage.isLogin()){
            String[] byeCommand = {"bye"};
            handleBye(byeCommand);
        }

        //Schließt den InputStream vom InputReader, womit das Schließen des Programms beginnt.
        try {
            System.in.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            finished = true;
        }

        log.debug("Initialization finished");

    }

    /**
     * Gibt Informationen über den Node aus.
     * - NodeId
     * - Port
     * @param command Der Command der Info enthält.
     */
    private void handleInfo(String[] command) {

        log.info("You have the ID: {}\nPort: {}", storage.getUnsignedID(), storage.getPort());
    }

    /**
     * Verarbeitet den List command. Wo alle Einträge der routbaren Routingeinträge auflistet.
     * Mögliche Flags:
     * - --all: es werden Routingeinträge aufgelistet, auch diese, welche gerade nicht routbar sind.
     * - --connect: Es werden zusätzlich alle vorhandenen Verbindungen aufgelistet.
     * @param command Der Command, der mit dem list command beginnt, und mit zusätzlichen Flags versehen ist.
     */
    private void handleList(String[] command) {
        log.debug("Start with list");

        boolean allFlagSet = false;
        boolean connectionFlagSet = false;

        //Prüft, ob --all oder --connect Flag gesetzt sind.
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

        //Gibt alle Routingeinträge aus, die routbar sind, oder alle, wenn --all Flag gesetzt ist.
        for(RoutingEntry entry : routingTable.getAllEntries()){
            if(entry.getRoutable() || allFlagSet){
                log.info("{} | Hops: {} | next Hop Address: {} | next Hop port: {} | is routable: {}| last seen:{}ms", Long.toUnsignedString(entry.getNodeId()), entry.getHops(), entry.getNextHopAddress(), entry.getNextHopPort(), entry.getRoutable(), (int) entry.getLastSeenShort());
            }
        }

        //Gibt alle Verbindungen aus, wenn --connect Flag gesetzt ist.
        if(connectionFlagSet){
            for(Connection connection : connectionList.getAllConnections()){
                log.info("{}:{}", connection.address().toString(), connection.port());
            }
        }

        log.debug("End with list");
    }

    /**
     * Verarbeitet den File Command, wo eine Datei an einen anderen Nutzer verschickt wird.
     * @param command Der command beginnend mit file und danach dem file Pfad und der NodeId des Users, an der die Datei verschickt werden soll.
     */
    private void handleFile(String[] command) {
        log.debug("Start with file transfer");

        String path = command[1];

        long destNodeId;
        //Parst die NodeId vom Input
        try {
            destNodeId = Long.parseUnsignedLong(command[2]);
        } catch (NumberFormatException e) {
            ExceptionHandler.handle(new NotANodeIdException(e.getMessage()), this.getClass());
            return;
        }

        //Prüft, ob die NodeId bekannt ist.
        if(!validNodeId(destNodeId))  {
            ExceptionHandler.handle(new UnknowNodeIdException(destNodeId), this.getClass());
        }
        else {
            //File-Init,File-Data und File-End werden verschickt.
            try (RandomAccessFile file = new RandomAccessFile(path, "r")){

                //Gebrauchte Variablen
                long length = file.length();
                int anzahlChunks = (int) Math.ceil(length / (float) BCPPacketImpl.MAXIMUM_PAYLOAD_SIZE);
                int fileId = storage.getNextFileID();
                InetAddress address = routingTable.getNextHopAddressForUID(destNodeId);
                int port = routingTable.getNextHopPortForUID(destNodeId);
                storage.addSendOpenFile(fileId, path);

                // Es wird das FileInitPacket gesendet.
                sendFileInitPacket(anzahlChunks, length, path, destNodeId, fileId, address, port);

                log.debug("File init packet send");

                //Es werden alle Chunks in einzelnen Paketen versendet.
                for(int sequenz = 0; sequenz < anzahlChunks; sequenz++){
                    byte[] payload = getChunk(file, sequenz, anzahlChunks);
                    BCPPacketImpl bcpPacket = new BCPPacketImpl(
                            (byte) 1, //version
                            PacketTypes.FILE_DATA, //type
                            (byte) 32, // ttl
                            (byte) 0, // hops
                            storage.getID(), //srcNodId
                            destNodeId, //destNodeId
                            sequenz, //sequenz
                            fileId, //fileId
                            0L, //crc
                            (short)payload.length, //payloadLength
                            payload, //payload
                            address, //address
                            port); //port

                    DatagramPacket packet = bcpPacket.makeDatagramPacket();
                    try{
                        senderQueue.add(packet);
                    } catch (IllegalStateException e) {
                    }

                    log.debug("File data packet number {} send", sequenz);
                }

                //File-End wird gesendet.
                sendFileEnd(destNodeId, fileId, address, port);

                log.debug("End with file transfer");

                log.info("File send to User: {}", Long.toUnsignedString(destNodeId));
            }catch (IllegalStateException | IOException e){
                ExceptionHandler.handle(e, this.getClass());
            }
        }
    }

    /**
     * Gibt den Chunk, der File wieder, von der angegebenen Position. Wenn es der letzte Chunk der file ist,
     * kann es sein, dass das Byte-Array kleiner ist als {@code BCPPacketImpl.getMaximumPayloadSize()}
     * @param file Die File, von der der Chunk geholt werden soll.
     * @param sequenz Die Position des Chunks in der Datei.
     * @param anzahlChunks Die Anzahl der Chunks in der Datei.
     * @return Byte-Array des Chunks, oder null, wenn die Sequenz fehlerhaft ist, oder es zu einem IO/Error kommt.
     */
    private byte[] getChunk(RandomAccessFile file, int sequenz, int anzahlChunks) {
        byte[] chunk = null;

        try {
            //Prüft, ob die Sequenz korrekt ist.
            if(anzahlChunks <= sequenz || sequenz < 0){
                throw new IllegalSequenzNumberException(sequenz);
            }
            //Prüft, ob es der letzte Chunk ist.
            else if(anzahlChunks - 1 == sequenz){
                int size = (int)(file.length() % BCPPacketImpl.MAXIMUM_PAYLOAD_SIZE);
                chunk = new byte[size];
            }
            else{
                chunk = new byte[BCPPacketImpl.MAXIMUM_PAYLOAD_SIZE];
            }
            //List den Chunk aus der Datei.
            file.seek((long) sequenz * BCPPacketImpl.MAXIMUM_PAYLOAD_SIZE);
            file.read(chunk);
        } catch (IOException | IllegalSequenzNumberException e) {
            ExceptionHandler.handle(e, this.getClass());
        }
        return chunk;
    }

    /**
     * Versendet das File-End Paket, der Datei mit der {@code fileId} an den User.
     * @param nodeId Die NodeId des Users an der das File-End Paket gesendet werden soll.
     * @param fileId Die FileId der Datei, zu dem das File-End Paket gehört.
     * @param address Die Adresse des Users.
     * @param port Der Port des Users.
     */
    private void sendFileEnd(long nodeId, int fileId, InetAddress address, int port) {
        byte[] payload = new byte[0];

        //Paket wird erstellt.
        BCPPacketImpl bcpPacket = new BCPPacketImpl(
                (byte) 1, //version
                PacketTypes.File_End, //type
                (byte) 32, // ttl
                (byte) 0, // hops
                storage.getID(), //srcNodId
                nodeId, //destNodeId
                0, //sequenz
                fileId, //fileId
                0L, //crc
                (short)payload.length, //payloadLength
                payload, //payload
                address, //address
                port); //port

        DatagramPacket packet = bcpPacket.makeDatagramPacket();

        try {
            senderQueue.put(packet);
        } catch (InterruptedException e) {
            ExceptionHandler.handle(e, this.getClass());
        }

    }

    /**
     * Versendet das File-Init Paket, der Datei mit der {@code fileId} an den User mit der {@code nodeId}.
     * @param anzahlChunks Wie viele Chunks besitzt die Datei, wie viele File-Data Packete danach versendet werden.
     * @param length Die Größe der Datei in Bytes.
     * @param path Der Pfad zur Datei.
     * @param nodeId Die NodeId des Users der das File-Init Paket erhalten soll.
     * @param fileId Die FileId der Datei
     * @param address Die Adresse des Users.
     * @param port Der Port des Users.
     */
    private void sendFileInitPacket(int anzahlChunks, long length, String path, long nodeId, int fileId, InetAddress address, int port) throws IllegalStateException {
        //Es wird der Payload erzeugt
        byte[] payload = makeDataInitPayload(length, path);

        //Es wird das Paket erstellt.
        BCPPacketImpl bcpPacket = new BCPPacketImpl(
                (byte) 1, //version
                PacketTypes.FILE_INIT, //type
                (byte) 32, // ttl
                (byte) 0, // hops
                storage.getID(), //srcNodId
                nodeId, //destNodeId
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

    /**
     * Erstellt den Payload, für ein File-Init Packet, welcher die Größe und den Namen der Datei enthält.
     * @param length Die Größe der Datei in Bytes.
     * @param path Der Pfad zur Datei.
     * @return Byte-Array wo der Payload gespeichert ist.
     */
    private byte[] makeDataInitPayload(long length, String path) {
        String[] splitPath;

        //Prüft, wo nach separiert werden soll, je nach Betriebssystem.
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            splitPath = path.split("\\\\");
        }
        else {
            splitPath = path.split("/");
        }

        //Erzeugt konkret den Payload.
        String fileName = splitPath[splitPath.length - 1];
        ByteBuffer payload = ByteBuffer.allocate(fileName.getBytes().length + 4);
        payload.putInt((int)length);
        payload.put(fileName.getBytes());
        return payload.array();
    }

    /**
     * Verarbeitet den Send command, wo eine Nachricht an einen anderen Nutzer verschickt wird.
     * @param command Der Command, welcher mit Send beginnt, danach die NodeId des Users, welcher die Nachricht erhalten soll
     * und danach die Nachricht enthält.
     */
    private void handleSend(String[] command) {
        log.debug("Start with Message sending");

        long destNodeId;

        //Parst die DestNodeId aus Command
        try {
            destNodeId = Long.parseUnsignedLong(command[1]);
        } catch (NumberFormatException e) {
            ExceptionHandler.handle(new NotANodeIdException(e.getMessage()), this.getClass());
            return;
        }
        //Baut die Nachricht wieder zusammen
        StringBuilder msg = new StringBuilder(command[2]);
        for(int i = 3; i < command.length; i++){
            msg.append(" ").append(command[i]);
        }

        //Prüft, ob die destNodeId bekannt ist.
        if(!validNodeId(destNodeId)) {
            ExceptionHandler.handle(new UnknowNodeIdException(destNodeId), this.getClass());
            return;
        }
        //Prüft, ob die Nachricht valide ist.
        else if(!validMessage(msg.toString())){
            ExceptionHandler.handle(new InvalidMessageException(msg.toString()), this.getClass());
            return;
        }


        byte[] payload = msg.toString().getBytes(StandardCharsets.UTF_8);
        InetAddress address = routingTable.getNextHopAddressForUID(destNodeId);
        int port = routingTable.getNextHopPortForUID(destNodeId);

        //Erstellung des Packets
        BCPPacketImpl bcpPacket = new BCPPacketImpl(
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
                address,
                port);

        DatagramPacket packet = bcpPacket.makeDatagramPacket();


        try{
            senderQueue.add(packet);
        }catch (IllegalStateException e){
            ExceptionHandler.handle(e, this.getClass());
            log.info("Error happened by Sending the message. Try again later.");
            return;
        }

        log.info("Message send to {}", Long.toUnsignedString(destNodeId));
    }

    /**
     * Verarbeitet den Bye Command, wo der User abgemeldet wird.
     * @param command Der Command mit "bye" beginnend.
     */
    private void handleBye(String[] command) {
        log.debug("Start logout");

        //Es werden die ScheduledServices beendet (TimeoutHandler, HeartbeatSender, RoutingTableSender)
        threadPools.getScheduleServicesFuture().cancel(true);
        threadPools.setScheduleServicesFuture(null);
        storage.logout();

        log.debug("Finished with canceling heartbeats and timeout");

        //Verschickt ein Bye-Paket an alle direkten Nachbarn.
        for (RoutingEntry neighbour : routingTable.getAllDirectNeighbours()) {

            //Prüft, ob der direkte Nachbar noch erreichbar ist.
            if(routingTable.isNodeIdAvailable(neighbour.getNodeId())){
                byte[] payload = new byte[0];
                BCPPacketImpl bcpPacket = new BCPPacketImpl(
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

                try{
                    senderQueue.add(packet);
                } catch (IllegalStateException e){
                    ExceptionHandler.handle(e, this.getClass());
                }


                log.debug("Goodbye packet send to {}", Long.toUnsignedString(neighbour.getNodeId()));
            }
        }

        log.info("Logout successful");

        routingTable.removeAll();
    }

    /**
     * Verarbeitet hello Command. Der den User anmeldet. Und an alle Connections ein Hello Paket sendet.
     * @param command Der Command beginnend mit hello.
     */
    private void handleHello(String[] command) {

        //Prüft, ob man schon angemeldet ist.
        if(storage.isLogin()){
            ExceptionHandler.handle(new LoginException("Your are already logged in"), this.getClass());
        }
        else {

            //Schickt an alle Connections ein Hello-Paket, mit der BroadCast-Id
            for (Connection connection : connectionList.getAllConnections()) {
                byte[] payload = new byte[0];
                BCPPacketImpl bcpPacket = new BCPPacketImpl(
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

                try {
                    senderQueue.add(packet);
                } catch (IllegalStateException e) {
                    ExceptionHandler.handle(e, this.getClass());
                    log.info("Error happened by login. Try again later.");
                }
                log.debug("Hello packet send to {}", connection.address());
            }
            storage.login();
            //Start die ScheduledServices (TimeoutHandler, HeartbeatSender, RoutingTableSender)
            threadPools.setScheduleServicesFuture(threadPools.getScheduleServices().scheduleWithFixedDelay(new ScheduledTasksHandlerImpl(routingTable, storage, senderQueue), 1, 5, TimeUnit.SECONDS));

            log.info("Login successful");
        }

    }

    /**
     * Verarbeitet den disconnect command. Dadurch wird eine Connection entfernt.
     * @param command Command mit "connect" beginnend, danach die Ip-Adresse und der Port der Connection.
     */
    private void handleDisconnect(String[] command) {
        log.debug("start with disconnect: {} : {}", command[1], command[2]);

        InetAddress address;
        int port;

        //Parst die Ip-Adresse und den Port aus dem Command.
        try {
            address = InetAddress.getByName(command[1]);
            port = Integer.parseInt(command[2]);
            Connection connection = new Connection(address, port);

            //Entfernt die connection aus der Liste.
            connectionList.remove(connection);

            log.info("Disconnect with: {}:{}", command[1], command[2]);
        } catch (UnknownHostException | NumberFormatException e ) {
            ExceptionHandler.handle(new ArgumentException(e.getMessage()), this.getClass());
        }

    }

    /**
     * Verarbeitet den Connect Command. Welcher eine neue Connection erstellt.
     * @param command Der Command beginnend mit "connect" danach die Ip-Adresse und der Port der Connection.
     */
    private void handleConnect(String[] command) {
        log.debug("start with connect: {} : {}", command[1], command[2]);

        InetAddress address;
        int port;

        //Parst die Ip-Adresse und den Port aus dem Command.
        try {
            address = InetAddress.getByName(command[1]);
            port = Integer.parseInt(command[2]);
            Connection connection = new Connection(address, port);

            //Fügt die Connection zur Liste hinzu.
            connectionList.add(connection);

            log.info("Connect with: {}:{}", command[1], command[2]);
        } catch (UnknownHostException | NumberFormatException e) {
            ExceptionHandler.handle(new ArgumentException(e.getMessage()), this.getClass());
        }
    }


    /**
     * Überprüft ob die NodeId bekannt ist.
     * @param nodeId Die NodeId die überprüft werden soll.
     * @return True, wenn die NodeId bekannt ist, sonst false.
     */
    private boolean validNodeId(Long nodeId) {
        return routingTable.isNodeIdAvailable(nodeId);
    }

    /**
     * Überprüft, ob die Nachricht, die versendet werden möchte, eine gültige Nachricht ist.
     * Gültig ist sie, wenn:
     * - Sie kleiner als 1 300 Zeichen ist.
     * @param msg Die Nachricht, die überprüft werden soll.
     * @return True, wenn sie gültig ist, sonst false.
     */
    private boolean validMessage(String msg){
        return msg.getBytes().length <= BCPPacketImpl.MAXIMUM_PAYLOAD_SIZE;
    }
}
