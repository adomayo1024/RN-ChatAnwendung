package ChatAnwendung.persistence.Api;

import java.net.DatagramPacket;
import java.util.concurrent.BlockingQueue;

/**
 * DownloadFiles speichert alle bisher eingegangenen Pakete, der Files, die gerade heruntergeladen werden.
 */
public interface DownloadFiles {

    /**
     * Gibt die File, mit der File ID zurück, welches der User mit der NodeId verschickt hat.
     * @param NodeId Die Node ID des Senders
     * @param fileID Die ID der Datei
     * @return Die File mit der File ID, die vom User mit der NodeId verschickt wurde.
     */
    File getFile(long NodeId, int fileID);

    /**
     * Entfernt die Datei aus den Files, die zurzeit gedownloaded werden.
     * @param nodeId Die Id des Senders
     * @param fileID Die ID der Datei
     */
    void removeFile(long nodeId, int fileID);

    /**
     * Fügt eine neue Datei hinzu, die zurzeit heruntergeladen wird.
     * @param nodeId Die Id des Senders
     * @param fileID die Id der Datei
     * @param file Die File, die zurzeit heruntergeladen wird
     */
    void setNewFile(long nodeId, int fileID, File file);

    /**
     * Startet Requesting nach fehlenden Chunks der Datei mit der File ID, vom User mit der NodeId.
     * @param file Die Datei, wo das Requesting gestartet werden soll.
     * @param downloadFiles Damit der Requester die Datei entfernen kann, aus den DownloadFiles.
     * @param routingTable Damit der Requester nach gucken kann, wo er den Request senden soll.
     * @param storage Damit der Request die NodeId des Hosts herausfinden kann.
     * @param sendeQueue Die Queue, wo der Request das Request Packet packen, soll damit es verschickt werden.
     */
    void startRequesting(File file, DownloadFiles downloadFiles, RoutingTable routingTable, Storage storage, BlockingQueue<DatagramPacket> sendeQueue);

    /**
     * Beendet das Requesting nach fehlenden Chunks der Datei mit der File ID, vom User mit der NodeId.
     * @param file Die Datei, wo das Requesting beendet werden soll.
     */
    void stopRequesting(File file);

    /**
     * Entfernt alle Dateien aus dem DownloadFiles.
     */
    void removeAll();
}
