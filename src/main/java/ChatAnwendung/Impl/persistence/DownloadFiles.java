package ChatAnwendung.Impl.persistence;

import ChatAnwendung.Impl.Handler.Common.ExceptionHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class DownloadFiles {

    private static  DownloadFiles INSTANCE;

    private  ScheduledExecutorService timer;

    private Map<Long, Map<Integer, File>> downloadedFiles;

    private ReentrantLock mutex;

    private DownloadFiles() {
        downloadedFiles = new HashMap<>();
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
        // TODO kein busy waiting mit Semphore
        while(!fileIsThere(uID, fileID)){
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                ExceptionHandler.handle(e, this.getClass());
            }
        }

        mutex.lock();
        File result = downloadedFiles.get(uID).get(fileID);
        mutex.unlock();
        log.debug("Got File: {} from User: {} from DownloadFiles: {}", fileID, Long.toUnsignedString(uID), this);
        return result;
    }

    private boolean fileIsThere(long uID, int fileID) {

        log.debug("Checking if File: {} from User: {} is in DownloadFiles: {}", fileID, Long.toUnsignedString(uID), this);

        return downloadedFiles.containsKey(uID) && downloadedFiles.get(uID).containsKey(fileID);
    }

    public void setNewFile(long uID, int fileID, File file){
        mutex.lock();
        if(!downloadedFiles.containsKey(uID)){
            downloadedFiles.put(uID, new HashMap<>());
        }
        downloadedFiles.get(uID).put(fileID, file);
        mutex.unlock();

        log.debug("Added new File: {} to User: {} to DownloadFiles: {}", fileID, Long.toUnsignedString(uID), this);
    }

    public ScheduledExecutorService getScheduledThreadPool(){
        return timer;
    }

    public void removeFile(long srcUID, int fileId) {
        mutex.lock();
        downloadedFiles.get(srcUID).remove(fileId);
        mutex.unlock();

        log.debug("Removed File: {} from User: {} from DownloadFiles: {}", fileId, Long.toUnsignedString(srcUID), this);
    }
}
