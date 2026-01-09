package ChatAnwendung.persistence.Api;

import ChatAnwendung.persistence.Impl.FileImpl;

import java.util.concurrent.ScheduledExecutorService;

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
     * Entfernt alle Datei aus dem DownloadFiles.
     */
    void removeAll();

    //------------- GETER -------------

    /**
     * Gibt den Executor Service wieder, über den die RequestSender laufen.
     * @return Der Executor Service.
     */
    ScheduledExecutorService getScheduledThreadPool();
}
