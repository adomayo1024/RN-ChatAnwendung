package ChatAnwendung.Impl.Handler;


import ChatAnwendung.Impl.Exceptions.IllegalSequnzNumberException;
import ChatAnwendung.Impl.Exceptions.UnknowUIDException;
import ChatAnwendung.Impl.PacketTypes;
import ChatAnwendung.Impl.RoutingTableImpl;
import ChatAnwendung.Impl.Storage;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.concurrent.CompletableFuture;

public class FileHandler extends AbstractHandler {
    public FileHandler(String[] command) {
        super(command, FileHandler.class.getName());
    }

    @Override
    public void run() {

        String path = command[1];

        long uID = Long.parseLong(command[2]);

        if(!validUID(uID))  {
            CompletableFuture.runAsync(new ExceptionHandler(new UnknowUIDException(uID), this.getClass()));
        }



        // TODO wie der name aussehen muss
        try (RandomAccessFile file = new RandomAccessFile(path, "r")){

            long length = file.length();
            long anzahlChunks = (long) Math.ceil(length / 1300.0);
            int fileId = Storage.getInstance().getNextFileID();
            InetAddress adress = RoutingTableImpl.getInstance().getNextHopAdressForUID(uID);
            int port = RoutingTableImpl.getInstance().getNextHopPortForUID(uID);
            Storage.getInstance().setSendOpenFile(uID, fileId);


            sendFileInitPacket(length, path, uID, fileId, adress, port);


            for(int sequenz = 0; sequenz < anzahlChunks; sequenz++){
                byte[] payload = split(file, sequenz);
                DatagramPacket packet = makeDatagramPackage(
                        PacketTypes.FILE_DATE,
                        uID,
                        sequenz,
                        fileId,
                        payload,
                        adress,
                        port);
                MessageQueue.getInstance().push(packet);
            }

            sendFileEnd(uID, fileId, adress, port);
        } catch (IOException e) {
            CompletableFuture.runAsync(new ExceptionHandler(e, this.getClass()));
        }
    }

    private void sendFileEnd(long uID, int fileId, InetAddress adress, int port) {
        byte[] payload = new byte[0];
        DatagramPacket packet = makeDatagramPackage(
                PacketTypes.File_End,
                uID,
                0,
                fileId,
                payload,
                adress,
                port);
        MessageQueue.getInstance().push(packet);
    }

    private void sendFileInitPacket(long length, String path, long uID, int fileId, InetAddress adress, int port) {
        byte[] payload = makeDataInitPayload(length, path);
        DatagramPacket packet = makeDatagramPackage(
                PacketTypes.FILE_INIT,
                uID,
                0,
                fileId,
                payload,
                adress,
                port);
        MessageQueue.getInstance().push(packet);
    }

    public static String help(){

        return """
                file: Verschickt eine Datei die angegeben ist an einen bestimmten User
                \tAufbau: file  "[absoluter Datei Pfad]" [User Id]
                \tFehler: Die angegeben Datei gibt es nicht, der angegeben User ist nicht bekannt.\s
                """;
    }


    private boolean validUID(long uID){
        return RoutingTableImpl.getInstance().isUIDavailable(uID);
    }

    private byte[] split(RandomAccessFile file, long sequenz){
        byte[] chunk = null;

        try {
            long anzahlChunks = (long)Math.ceil(file.length() / 1300.0);
            if(anzahlChunks <= sequenz || sequenz < 0){
                throw new IllegalSequnzNumberException(sequenz);
            }
            else if(anzahlChunks - 1 == sequenz){
                int size = (int)(file.length() % 1300);
                chunk = new byte[size];
            }
            else{
                chunk = new byte[1300];
            }
            file.seek(sequenz);
            file.read(chunk);
        } catch (IOException | IllegalSequnzNumberException e) {
            CompletableFuture.runAsync(new ExceptionHandler(e, this.getClass()));
        }
        return chunk;
    }


    private byte[] makeDataInitPayload(long length, String path) {
        String[] splitPath = path.split("\\d+");
        String fileName = splitPath[splitPath.length - 1];
        byte[] payload= new byte[fileName.getBytes().length + 4];
        Header.addInt(0, (int)length, payload);
        System.arraycopy(fileName.getBytes(), 0, payload, 4, fileName.getBytes().length);
        return payload;
    }

}
