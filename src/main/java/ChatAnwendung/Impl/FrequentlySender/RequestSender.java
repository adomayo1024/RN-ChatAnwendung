package ChatAnwendung.Impl.FrequentlySender;

import ChatAnwendung.Api.RoutingTable;
import ChatAnwendung.Impl.BCPPacket;
import ChatAnwendung.Impl.persistence.DownloadFiles;
import ChatAnwendung.Impl.persistence.File;
import ChatAnwendung.Impl.PacketTypes;
import ChatAnwendung.Impl.persistence.Storage;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;

@Slf4j
public class RequestSender implements Runnable {

    private File file;

    private int lastSequenz = 0;

    private int timesOfRequestOfLastSequenz = 0;
    private final DownloadFiles downloadFiles;
    private final  RoutingTable routingTable;
    private final Storage storage;
    private final BlockingQueue<DatagramPacket> sendeQueue;

    public RequestSender(File file, DownloadFiles downloadFiles, RoutingTable routingTable, Storage storage, BlockingQueue<DatagramPacket> sendeQueue) {
        this.file = file;
        this.downloadFiles = downloadFiles;
        this.routingTable = routingTable;
        this.storage = storage;
        this.sendeQueue = sendeQueue;
    }

    @Override
    public void run() {

        log.debug("Start with sending Request");

        int sequenz = file.getNextNeededChunk();
        lastSequenz = sequenz;
        if(sequenz == -1 || !timeSinceLastFileDataPackageMoreThanASecond()){
            return;
        }
        if (lastSequenz == sequenz && timesOfRequestOfLastSequenz == 3) {

            downloadFiles.removeFile(file.getSrcUID(), file.getFileId());
            log.info("Removed File because of 3 Request of the same chunk in a row without an answer");
        }
        byte[] payload = new byte[0];
        long destUID = file.getSrcUID();
        InetAddress destAdress = routingTable.getNextHopAdressForUID(destUID);
        int destPort = routingTable.getNextHopPortForUID(destUID);
        int fileID = file.getFileId();
        BCPPacket bcpPacket = new BCPPacket(
                (byte) 1, //version
                PacketTypes.RESENDREQUEST, //type
                (byte) 32, // ttl
                (byte) 0, // hops
                storage.getID(), //srcNodId
                destUID, //destNodeId
                sequenz, //sequenz
                fileID, //fileId
                0L, //crc
                (short)payload.length, //payloadLength
                payload, //payload
                destAdress, //address
                destPort); //port
        DatagramPacket packet = bcpPacket.makeDatagramPacket();
        sendeQueue.add(packet);

        timesOfRequestOfLastSequenz = 1;

        log.debug("Sent Request for FileID {} and sequence: {}from the User: {}", fileID, sequenz, Long.toUnsignedString(destUID));
    }

    private boolean timeSinceLastFileDataPackageMoreThanASecond(){
        return System.currentTimeMillis() - file.getRecievedLastChunk() > 1_000;
    }
}
