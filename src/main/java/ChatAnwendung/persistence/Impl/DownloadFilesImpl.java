package ChatAnwendung.persistence.Impl;

import ChatAnwendung.logic.Impl.RequestSenderImpl;
import ChatAnwendung.persistence.Api.DownloadFiles;
import ChatAnwendung.persistence.Api.File;
import ChatAnwendung.persistence.Api.RoutingTable;
import ChatAnwendung.persistence.Api.Storage;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public class DownloadFilesImpl implements ChatAnwendung.persistence.Api.DownloadFiles {

    // spezieller Pfad für Linux zum Download Ordner
    private static final String filePathLin = "Downloads/";

    // spezieller Pfad für Windows zum Download Ordner
    private static final String filePathWin = "Downloads\\";

    //Der Pfad, der zur Laufzeit benutzt werden soll, abhängig vom Betriebssystem
    private static final String filePath = System.getProperty("os.name").toLowerCase().contains("windows") ? filePathWin : filePathLin;

    // Der ExecutorService für die request Sender der Dateien
    private final ScheduledExecutorService timer;

    // alle Request Sende Tasks
    private final Map<File, ScheduledFuture<?>> scheduledFileRequests;

    // Speichert alle zurzeit gedownloaded Files, zu der NodeId des Senders und der File ID
    private final Map<Long, Map<Integer, File>> downloadedFiles;

    // Speichert, ob die Datei mit der File ID schon heruntergeladen wurde vom Sender mit der NodeId
    private final Map<Long, Map<Integer, Boolean>> finishedFiles;

    //Mutex zum locken von finishedFiles
    private final ReentrantReadWriteLock finishedFilesMutex;

    //Mutex zum locken von downloadFiles
    private final ReentrantReadWriteLock downloadFilesMutex;

    //Mutex zum locken von scheduledFileRequests
    private final ReentrantReadWriteLock scheduledFileRequestsMutex;

    /**
     * Konstruktor der DownloadFilesImpl.
     * @param timer Der Executor Service für die request Sender der Dateien.
     */
    public DownloadFilesImpl(ScheduledExecutorService timer) {
        this.timer = timer;
        downloadedFiles = new HashMap<>();
        finishedFiles = new HashMap<>();
        scheduledFileRequests = new HashMap<>();
        finishedFilesMutex = new ReentrantReadWriteLock(true);
        downloadFilesMutex = new ReentrantReadWriteLock(true);
        scheduledFileRequestsMutex = new ReentrantReadWriteLock(true);

        new java.io.File(filePath).mkdirs();
    }

    public File getFile(long NodeId, int fileID){
        log.debug("Requesting File: {} from User: {} from DownloadFiles: {}", fileID, Long.toUnsignedString(NodeId), this);

        finishedFilesMutex.readLock().lock();
        // Prüft, ob die Datei schon fertig heruntergeladen wurde, somit nicht gespeichert wird
        if(finishedFiles.containsKey(NodeId) && finishedFiles.get(NodeId).getOrDefault(fileID, false)){
            finishedFilesMutex.readLock().unlock();
            return null;
        }
        finishedFilesMutex.readLock().unlock();

        downloadFilesMutex.readLock().lock();
        // Prüft, ob die Datei als heruntergeladen gespeichert wird
        if(!downloadedFiles.containsKey(NodeId) || !downloadedFiles.get(NodeId).containsKey(fileID)){
            downloadFilesMutex.readLock().unlock();
            return null;
        }

        File result = downloadedFiles.get(NodeId).get(fileID);
        downloadFilesMutex.readLock().unlock();

        log.debug("Got File: {} from User: {} from DownloadFiles: {}", fileID, Long.toUnsignedString(NodeId), this);
        return result;
    }

    public void setNewFile(long nodeId, int fileID, File file){

        downloadFilesMutex.writeLock().lock();
        // Gibt es schon eine Datei vom Sender mit derselben File Id
        if(!downloadedFiles.containsKey(nodeId)){
            downloadedFiles.put(nodeId, new ConcurrentHashMap<>());
        }
        downloadedFiles.get(nodeId).put(fileID, file);

        downloadFilesMutex.writeLock().unlock();

        log.debug("Added new File: {} to User: {} to DownloadFiles: {}", fileID, Long.toUnsignedString(nodeId), this);
    }

    @Override
    public void startRequesting(File file, DownloadFiles downloadFiles, RoutingTable routingTable, Storage storage, BlockingQueue<DatagramPacket> sendeQueue) {
        int start = file.getAnzahlChunks() / 33;
        ScheduledFuture<?> future = timer.scheduleAtFixedRate(new RequestSenderImpl(file, downloadFiles, routingTable, storage, sendeQueue), start, 1000, TimeUnit.MILLISECONDS);

        scheduledFileRequestsMutex.writeLock().lock();
        scheduledFileRequests.put(file, future);
        scheduledFileRequestsMutex.writeLock().unlock();
    }

    @Override
    public void stopRequesting(File file) {
        scheduledFileRequests.get(file).cancel(true);
        scheduledFileRequests.remove(file);
    }

    public void removeFile(long nodeId, int fileId) {

        //Packt die file in die fertigen downloads
        finishedFilesMutex.writeLock().lock();

        finishedFiles.computeIfAbsent(nodeId, k -> new HashMap<>()).put(fileId, true);

        finishedFilesMutex.writeLock().unlock();

        //Entfernt die File von den download Files
        downloadFilesMutex.writeLock().lock();

        downloadedFiles.get(nodeId).remove(fileId);

        downloadFilesMutex.writeLock().unlock();

        log.debug("Removed File: {} from User: {} from DownloadFiles: {}", fileId, Long.toUnsignedString(nodeId), this);
    }

    public void removeAll(){
        downloadFilesMutex.writeLock().lock();

        downloadedFiles.clear();

        downloadFilesMutex.writeLock().unlock();

        finishedFilesMutex.writeLock().lock();

        finishedFiles.clear();

        finishedFilesMutex.writeLock().unlock();
    }
}
