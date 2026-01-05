package ChatAnwendung.Impl.persistence;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class DownloadFiles {

    private ScheduledExecutorService timer;

    private Map<Long, Map<Integer, File>> downloadedFiles;

    private Map<Long, Map<Integer, Semaphore>> semaphorForFiles;

    private Map<Long, Map<Integer, AtomicInteger>> wartendenThreads;

    private Map<Long, Map<Integer, Boolean>> finishedFiles;

    public DownloadFiles(ScheduledExecutorService timer) {
        this.timer = timer;
        downloadedFiles = new ConcurrentHashMap<>();
        finishedFiles = new ConcurrentHashMap<>();
    }

    public File getFile(long uID, int fileID) throws NullPointerException{
        log.debug("Requesting File: {} from User: {} from DownloadFiles: {}", fileID, Long.toUnsignedString(uID), this);

        if(finishedFiles.containsKey(uID) && finishedFiles.get(uID).getOrDefault(fileID, false)){
            return null;
        }

        if(!downloadedFiles.containsKey(uID) || !downloadedFiles.get(uID).containsKey(fileID)){
            return null;
        }

        File result = downloadedFiles.get(uID).get(fileID);
        log.debug("Got File: {} from User: {} from DownloadFiles: {}", fileID, Long.toUnsignedString(uID), this);
        return result;
    }

    public void setNewFile(long uID, int fileID, File file){
        if(!downloadedFiles.containsKey(uID)){
            downloadedFiles.put(uID, new ConcurrentHashMap<>());
        }
        downloadedFiles.get(uID).put(fileID, file);

        log.debug("Added new File: {} to User: {} to DownloadFiles: {}", fileID, Long.toUnsignedString(uID), this);
    }

    public ScheduledExecutorService getScheduledThreadPool(){
        return timer;
    }

    public void removeAll(){
        downloadedFiles.clear();
        finishedFiles.clear();


    }

    public void removeFile(long srcUID, int fileId) {
        finishedFiles.computeIfAbsent(srcUID, k -> new HashMap<>()).put(fileId, true);
        downloadedFiles.get(srcUID).remove(fileId).stopRequesting();

        log.debug("Removed File: {} from User: {} from DownloadFiles: {}", fileId, Long.toUnsignedString(srcUID), this);
    }
}
