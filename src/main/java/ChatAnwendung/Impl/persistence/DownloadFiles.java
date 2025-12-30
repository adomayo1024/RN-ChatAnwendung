package ChatAnwendung.Impl.persistence;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class DownloadFiles {

    private static  DownloadFiles INSTANCE;

    private  ScheduledExecutorService timer;

    private Map<Long, Map<Integer, File>> downloadedFiles;

    private Map<Long, Map<Integer, Semaphore>> semaphorForFiles;

    private Map<Long, Map<Integer, AtomicInteger>> wartendenThreads;

    private Map<Long, Map<Integer, Boolean>> finishedFiles;

    private ReentrantLock mutex;

    private DownloadFiles() {
        downloadedFiles = new HashMap<>();
        semaphorForFiles = new HashMap<>();
        wartendenThreads = new HashMap<>();
        finishedFiles = new HashMap<>();
        mutex = new ReentrantLock(true);
        timer = ThreadPools.getInstance().getFileRequestTimer();
    }


    public static DownloadFiles getInstance(){

        if(INSTANCE == null) {
            INSTANCE = new DownloadFiles();
            log.debug("Created new DownloadFiles: {}", INSTANCE);
        }
        return INSTANCE;
    }

    public File getFile(long uID, int fileID) throws NullPointerException{
        log.debug("Requesting File: {} from User: {} from DownloadFiles: {}", fileID, Long.toUnsignedString(uID), this);

        if(finishedFiles.containsKey(uID) && finishedFiles.get(uID).getOrDefault(fileID, false)){
            return null;
        }

        if(!downloadedFiles.containsKey(uID) || !downloadedFiles.get(uID).containsKey(fileID)){
            waitForFile(uID, fileID);
        }

        mutex.lock();
        File result = downloadedFiles.get(uID).get(fileID);
        mutex.unlock();
        log.debug("Got File: {} from User: {} from DownloadFiles: {}", fileID, Long.toUnsignedString(uID), this);
        return result;
    }

    private void waitForFile(long uID, int fileID) {

            Semaphore sem = semaphorForFiles.computeIfAbsent(uID, k -> new HashMap<>()).computeIfAbsent(fileID, k -> new Semaphore(0));

            if(sem.availablePermits() <= 0){
                try {
                    wartendenThreads.computeIfAbsent(uID, k -> new HashMap<>()).computeIfAbsent(fileID, k -> new AtomicInteger(0)).incrementAndGet();
                    sem.acquire();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
    }

    public void setNewFile(long uID, int fileID, File file){
        mutex.lock();
        if(!downloadedFiles.containsKey(uID)){
            downloadedFiles.put(uID, new HashMap<>());
            semaphorForFiles.put(uID, new HashMap<>());
        }
        downloadedFiles.get(uID).put(fileID, file);
        int wartendeThreads = wartendenThreads.getOrDefault(uID, new HashMap<>()).getOrDefault(fileID, new AtomicInteger(1)).get();
        semaphorForFiles.get(uID).computeIfAbsent(fileID, k -> new Semaphore(0)).release(wartendeThreads);
        mutex.unlock();

        log.debug("Added new File: {} to User: {} to DownloadFiles: {}", fileID, Long.toUnsignedString(uID), this);
    }

    public ScheduledExecutorService getScheduledThreadPool(){
        return timer;
    }

    public void removeFile(long srcUID, int fileId) {
        mutex.lock();
        finishedFiles.computeIfAbsent(srcUID, k -> new HashMap<>()).put(fileId, true);
        downloadedFiles.get(srcUID).remove(fileId).stopRequesting();
        mutex.unlock();

        log.debug("Removed File: {} from User: {} from DownloadFiles: {}", fileId, Long.toUnsignedString(srcUID), this);
    }
}
