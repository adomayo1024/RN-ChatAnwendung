package ChatAnwendung.Impl;

import ChatAnwendung.Impl.Handler.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DownloadFiles {

    private static  DownloadFiles INSTANCE;

    private  ScheduledExecutorService timer;

    private Map<Long, Map<Integer, File>> downloadedFiles;

    private ReentrantLock mutex;

    private final Logger logger = Logger.getLogger(DownloadFiles.class.getName());

    private DownloadFiles() {
        downloadedFiles = new HashMap<>();
        mutex = new ReentrantLock(true);
        timer = ThreadPools.getInstance().getFileRequestTimer();
    }


    public static DownloadFiles getInstance(){

        if(INSTANCE == null) {
            INSTANCE = new DownloadFiles();
        }
        return INSTANCE;
    }

    public File getFile(long uID, int fileID) throws NullPointerException{
        logger.log(Level.INFO, "Requesting File: " + fileID + " from User: " + Long.toUnsignedString(uID));
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
        logger.log(Level.INFO, "Got File: " + fileID + " from User: " + Long.toUnsignedString(uID));
        return result;
    }

    private boolean fileIsThere(long uID, int fileID) {
        return downloadedFiles.containsKey(uID) && downloadedFiles.get(uID).containsKey(fileID);
    }

    public void setNewFile(long uID, int fileID, File file){
        mutex.lock();
        if(!downloadedFiles.containsKey(uID)){
            downloadedFiles.put(uID, new HashMap<>());
        }
        downloadedFiles.get(uID).put(fileID, file);
        mutex.unlock();
    }

    public ScheduledExecutorService getScheduledThreadPool(){
        return timer;
    }

    public void removeFile(long srcUID, int fileId) {
        mutex.lock();
        downloadedFiles.get(srcUID).remove(fileId);
        mutex.unlock();
    }
}
