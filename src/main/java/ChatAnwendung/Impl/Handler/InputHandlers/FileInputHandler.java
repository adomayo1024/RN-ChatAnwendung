package ChatAnwendung.Impl.Handler.InputHandlers;


import ChatAnwendung.Impl.Exceptions.ArgumentException;
import ChatAnwendung.Impl.Exceptions.IllegalSequnzNumberException;
import ChatAnwendung.Impl.Exceptions.NotAUIDException;
import ChatAnwendung.Impl.Exceptions.UnknowUIDException;
import ChatAnwendung.Impl.Handler.Common.ExceptionHandler;
import ChatAnwendung.Impl.Header;
import ChatAnwendung.Impl.MessageQueue;
import ChatAnwendung.Impl.PacketTypes;
import ChatAnwendung.Impl.persistence.RoutingTableImpl;
import ChatAnwendung.Impl.persistence.Storage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.InetAddress;

@Slf4j
public class FileInputHandler extends AbstractInputHandler {
    public FileInputHandler(String[] command) {
        super(command);
    }

    @Override
    public void run() {

        log.debug("Start with file transfer");

        String path = command[1];

        long uID = 0;
        try {
            uID = Long.parseLong(command[2]);
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
                int anzahlChunks = (int) Math.ceil(length / 1300.0);
                int fileId = Storage.getInstance().getNextFileID();
                InetAddress adress = RoutingTableImpl.getInstance().getNextHopAdressForUID(uID);
                int port = RoutingTableImpl.getInstance().getNextHopPortForUID(uID);
                Storage.getInstance().setSendOpenFile(fileId, path);
                byte[] wholeFile = new byte[1300 * anzahlChunks];

                sendFileInitPacket(anzahlChunks, length, path, uID, fileId, adress, port);

                log.debug("File init packet send");

                for(int sequenz = 0; sequenz < anzahlChunks; sequenz++){
                    byte[] payload = split(file, sequenz, anzahlChunks);
                    System.arraycopy(payload, 0, wholeFile, sequenz * 1300, payload.length);
                    DatagramPacket packet = makeDatagramPackage(
                            PacketTypes.FILE_DATA,
                            uID,
                            sequenz,
                            fileId,
                            payload,
                            adress,
                            port);
                    MessageQueue.getInstance().push(packet);

                    log.debug("File data packet number {} send", sequenz);
                }

                sendFileEnd(uID, fileId, adress, port);
            } catch (IOException e) {
                ExceptionHandler.handle(e, this.getClass());
            }
        }

        System.out.println("File send");

        log.debug("End with file transfer");


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

    private void sendFileInitPacket(int anzahlChunks, long length, String path, long uID, int fileId, InetAddress adress, int port) {
        byte[] payload = makeDataInitPayload(length, path);
        DatagramPacket packet = makeDatagramPackage(
                PacketTypes.FILE_INIT,
                uID,
                anzahlChunks,
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

    private byte[] split(RandomAccessFile file, long sequenz, long anzahlChunks){
        byte[] chunk = null;

        try {
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
            file.seek(sequenz * 1300);
            file.read(chunk);
        } catch (IOException | IllegalSequnzNumberException e) {
            ExceptionHandler.handle(e, this.getClass());
        }
        return chunk;
    }


    private byte[] makeDataInitPayload(long length, String path) {
        String[] splitPath = path.split("/");
        String fileName = splitPath[splitPath.length - 1];
        byte[] payload = new byte[fileName.getBytes().length + 4];
        Header.addInt(0, (int)length, payload);
        System.arraycopy(fileName.getBytes(), 0, payload, 4, fileName.getBytes().length);
        return payload;
    }

}
