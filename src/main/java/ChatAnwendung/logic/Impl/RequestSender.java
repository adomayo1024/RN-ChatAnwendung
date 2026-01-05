package ChatAnwendung.logic.Impl;

import ChatAnwendung.persistence.Api.RoutingTable;
import ChatAnwendung.persistence.Impl.DownloadFiles;
import ChatAnwendung.persistence.Impl.File;
import ChatAnwendung.logic.Enums.PacketTypes;
import ChatAnwendung.persistence.Impl.Storage;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.List;
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

        List<Integer> missingChunks = file.getMissingChunks();

        if(!timeSinceLastFileDataPackageMoreThanASecond()){
            timesOfRequestOfLastSequenz = 1;
            return;
        }
        if (timesOfRequestOfLastSequenz == 3) {

            downloadFiles.removeFile(file.getSrcUID(), file.getFileId());
            log.info("Removed File because of 3 Request of the same chunk in a row without an answer");
            System.out.println("Removed File because of 3 Request of the same chunk in a row without an answer");
        }


        for(int sequenz : missingChunks){
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

            log.debug("Sent Request for FileID {} and sequence: {}from the User: {}", fileID, sequenz, Long.toUnsignedString(destUID));
        }

        timesOfRequestOfLastSequenz++;


    }

    private boolean timeSinceLastFileDataPackageMoreThanASecond(){
        return System.currentTimeMillis() - file.getRecievedLastChunk() > 1_000;
    }
}

