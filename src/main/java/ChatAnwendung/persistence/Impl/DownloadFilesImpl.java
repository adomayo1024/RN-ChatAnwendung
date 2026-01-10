package ChatAnwendung.persistence.Impl;

import ChatAnwendung.persistence.Api.File;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
public class DownloadFilesImpl implements ChatAnwendung.persistence.Api.DownloadFiles {

    // spezieller Pfad für Linux zum Download Ordner
    private static String filePathLin = "Downloads/";

    // spezieller Pfad für Windows zum Download Ordner
    private static String filePathWin = "Downloads\\";

    //Der Pfad, der zur Laufzeit benutzt werden soll, abhängig vom Betriebssystem
    private static String filePath = System.getProperty("os.name").toLowerCase().contains("windows") ? filePathWin : filePathLin;

    // Der ExecutorService für die request Sender der Dateien
    private ScheduledExecutorService timer;

    // Speichert alle zur Zeit gedownloaded Files, zu der NodeId des Senders und der File ID
    private Map<Long, Map<Integer, File>> downloadedFiles;

    // Speichert ob die Datei mit der File ID schon heruntergeladen wurde vom Sender mit der NodeId
    private Map<Long, Map<Integer, Boolean>> finishedFiles;

    /**
     * Konstruktor der DownloadFilesImpl.
     * @param timer Der Executor Service für die request Sender der Dateien.
     */
    public DownloadFilesImpl(ScheduledExecutorService timer) {
        this.timer = timer;
        downloadedFiles = new ConcurrentHashMap<>();
        finishedFiles = new ConcurrentHashMap<>();

        new java.io.File(filePath).mkdirs();
    }

    public File getFile(long NodeId, int fileID){
        log.debug("Requesting File: {} from User: {} from DownloadFiles: {}", fileID, Long.toUnsignedString(NodeId), this);

        // Prüft, ob die Datei schon fertig heruntergeladen wurde, somit nicht gespeichert wird
        if(finishedFiles.containsKey(NodeId) && finishedFiles.get(NodeId).getOrDefault(fileID, false)){
            return null;
        }

        // Prüft, ob die Datei als wird heruntergeladen gespeichert wird
        if(!downloadedFiles.containsKey(NodeId) || !downloadedFiles.get(NodeId).containsKey(fileID)){
            return null;
        }

        File result = downloadedFiles.get(NodeId).get(fileID);
        log.debug("Got File: {} from User: {} from DownloadFiles: {}", fileID, Long.toUnsignedString(NodeId), this);
        return result;
    }

    public void setNewFile(long nodeId, int fileID, File file){

        // Gibt es schon eine Datei vom Sender mit derselben File Id
        if(!downloadedFiles.containsKey(nodeId)){
            downloadedFiles.put(nodeId, new ConcurrentHashMap<>());
        }
        downloadedFiles.get(nodeId).put(fileID, file);

        log.debug("Added new File: {} to User: {} to DownloadFiles: {}", fileID, Long.toUnsignedString(nodeId), this);
    }

    public ScheduledExecutorService getScheduledThreadPool(){
        return timer;
    }

    public void removeAll(){
        downloadedFiles.clear();
        finishedFiles.clear();
    }

    public void removeFile(long nodeId, int fileId) {
        finishedFiles.computeIfAbsent(nodeId, k -> new HashMap<>()).put(fileId, true);
        downloadedFiles.get(nodeId).remove(fileId);

        log.debug("Removed File: {} from User: {} from DownloadFiles: {}", fileId, Long.toUnsignedString(nodeId), this);
    }
}
