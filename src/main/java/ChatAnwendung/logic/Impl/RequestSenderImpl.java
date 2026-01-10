package ChatAnwendung.logic.Impl;

import ChatAnwendung.logic.Api.RequestSender;
import ChatAnwendung.persistence.Api.*;
import ChatAnwendung.logic.Enums.PacketTypes;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.BlockingQueue;
/**
 * Ein RequestSender sendet für eine derzeitig herunterzuladende Datei Request für alle Chunks, die bisher nicht empfangen wurden.
 */
@Slf4j
public class RequestSenderImpl implements RequestSender {

    // Die File für die Request gesendet werden sollen.
    private final File file;

    private int timesOfRequestWithoutAnAnswer = 0;

    long timeStampOfNewestPackageReceivedSinceLastRequest;

    // DownloadFiles um die File zu entfernen, wenn keine Antwort nach 3 Request anfragen kam.
    private final DownloadFiles downloadFiles;

    //Um destAddress und destPort zu erhalten für die Request Pakete .
    private final  RoutingTable routingTable;

    //Um die eigene NodeId zu erhalten für die Request Pakete.
    private final Storage storage;

    //Die Sendequeue so das der Sender die Request Pakete senden kann.
    private final BlockingQueue<DatagramPacket> sendeQueue;

    public RequestSenderImpl(File file, DownloadFiles downloadFiles, RoutingTable routingTable, Storage storage, BlockingQueue<DatagramPacket> sendeQueue) {
        this.file = file;
        this.downloadFiles = downloadFiles;
        this.routingTable = routingTable;
        this.storage = storage;
        this.sendeQueue = sendeQueue;

        System.out.println("Start Requesting for File: " + file.getName() + "in 3 Seconds");
    }

    @Override
    public void run() {

        log.debug("Start Requesting");

        List<Integer> missingChunks = file.getMissingChunks();

        //Geprüft wird, ob das neueste Paket vor weniger als einer Sekunde empfangen wurde. Wenn ja kein Request
        if(!timeSinceLastFileDataPackageMoreThanASecond()){
            timesOfRequestWithoutAnAnswer = 0;
            return;
        }
        //Prüft, ob der Sender noch verfügbar ist, wenn nicht, wird der Download abgebrochen.
        else if(!routingTable.isNodeIdAvailable(file.getSrcNodeId())){
            downloadFiles.stopRequesting(file);
            downloadFiles.removeFile(file.getSrcNodeId(), file.getFileId());
            log.info("Removed File because of sender not available anymore");
            System.out.println("Removed File because of sender not available anymore");
        }
        // Es wird geprüft, ob seit dem letzten Request neue Pakete empfangen wurden.
        if(!receivedAnPacketSinceThatTime(timeStampOfNewestPackageReceivedSinceLastRequest)){
            timesOfRequestWithoutAnAnswer++;
            log.debug("Inkrement of timesOfRequestWithoutAnAnswer: {}", timesOfRequestWithoutAnAnswer);
        }
        // Wenn seit den letzten 3 Request kein neues Paket ankam, wird der Download abgebrochen.
        else if (timesOfRequestWithoutAnAnswer >= 3) {

            downloadFiles.stopRequesting(file);
            downloadFiles.removeFile(file.getSrcNodeId(), file.getFileId());
            log.info("Removed File because of 3 Request of the same chunk in a row without an answer");
            System.out.println("Removed File because of 3 Request of the same chunk in a row without an answer");
        }


        // Es wird für alle fehlenden Chunks eine Request gesendet.
        for(int sequenz : missingChunks){
            byte[] payload = new byte[0];
            long destUID = file.getSrcNodeId();
            InetAddress destAddress = routingTable.getNextHopAddressForUID(destUID);
            int destPort = routingTable.getNextHopPortForUID(destUID);
            int fileID = file.getFileId();
            BCPPacketImpl bcpPacket = new BCPPacketImpl(
                    (byte) 1, //version
                    PacketTypes.RESEND_REQUEST, //type
                    (byte) 32, // ttl
                    (byte) 0, // hops
                    storage.getID(), //srcNodId
                    destUID, //destNodeId
                    sequenz, //sequenz
                    fileID, //fileId
                    0L, //crc
                    (short)payload.length, //payloadLength
                    payload, //payload
                    destAddress, //address
                    destPort); //port
            DatagramPacket packet = bcpPacket.makeDatagramPacket();
            try {
                sendeQueue.add(packet);
            } catch (IllegalStateException e) {

            }

            log.debug("Sent Request for FileID {} and sequence: {}from the User: {}", fileID, sequenz, Long.toUnsignedString(destUID));
        }

        timeStampOfNewestPackageReceivedSinceLastRequest = file.getReceivedLastChunk();

    }

    /**
     * Prüft, ob das neueste empfangene Paket vor über einer Sekunde empfangen wurde.
     * @return True, wenn das neueste Paket vor über einer Sekunde empfangen wurde, sonst false.
     */
    private boolean timeSinceLastFileDataPackageMoreThanASecond(){
        return System.currentTimeMillis() - file.getReceivedLastChunk() > 1_000;
    }

    /**
     * Prüft, seit dem letzten Request neue Pakete empfangen wurden.
     * @param timeStamp der Zeitpunkt seit dem letzten Request.
     * @return True, wenn neue Pakete empfangen wurden, sonst false.
     */
    private boolean receivedAnPacketSinceThatTime(long timeStamp){
        return timeStamp < file.getReceivedLastChunk();
    }
}

