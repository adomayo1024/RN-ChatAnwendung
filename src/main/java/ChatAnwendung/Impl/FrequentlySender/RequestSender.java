package ChatAnwendung.Impl.FrequentlySender;

import ChatAnwendung.Impl.persistence.DownloadFiles;
import ChatAnwendung.Impl.persistence.File;
import ChatAnwendung.Impl.Handler.Common.AbstractHandler;
import ChatAnwendung.Impl.MessageQueue;
import ChatAnwendung.Impl.PacketTypes;
import ChatAnwendung.Impl.persistence.RoutingTableImpl;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.net.InetAddress;

@Slf4j
public class RequestSender extends AbstractHandler {

    private File file;

    private int lastSequenz = 0;

    private int timesOfRequestOfLastSequenz = 0;

    public RequestSender(File file) {
        this.file = file;
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

            DownloadFiles.getInstance().removeFile(file.getSrcUID(), file.getFileId());
            log.info("Removed File because of 3 Request of the same chunk in a row without an answer");
        }
        byte[] payload = new byte[0];
        long destUID = file.getSrcUID();
        InetAddress destAdress = RoutingTableImpl.getInstance().getNextHopAdressForUID(destUID);
        int destPort = RoutingTableImpl.getInstance().getNextHopPortForUID(destUID);
        int fileID = file.getFileId();
        DatagramPacket reqeuestPacket = makeDatagramPackage(
                PacketTypes.RESENDREQUEST,
                destUID,
                sequenz,
                fileID,
                payload,
                destAdress,
                destPort);
        MessageQueue.getInstance().push(reqeuestPacket);

        timesOfRequestOfLastSequenz = 1;

        log.debug("Sent Request for FileID {} and sequence: {}from the User: {}", fileID, sequenz, Long.toUnsignedString(destUID));
    }

    private boolean timeSinceLastFileDataPackageMoreThanASecond(){
        return System.currentTimeMillis() - file.getRecievedLastChunk() > 1_000;
    }
}
