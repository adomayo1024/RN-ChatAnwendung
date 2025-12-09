package ChatAnwendung.Impl;

import ChatAnwendung.Impl.Handler.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DownloadFiles {

    private static  DownloadFiles INSTANCE;

    private final ScheduledExecutorService timer = Executors.newScheduledThreadPool(3);

    private Map<Long, Map<Integer, File>> downloadedFiles;

    private ReentrantLock mutex;

    private final Logger logger = Logger.getLogger(DownloadFiles.class.getName());

    private DownloadFiles() {
        downloadedFiles = new HashMap<>();
        mutex = new ReentrantLock(true);
    }


    public static DownloadFiles getInstance(){

        if(INSTANCE == null) {
            INSTANCE = new DownloadFiles();
        }
        return INSTANCE;
    }

    public File getFile(long uID, int fileID) throws NullPointerException{
        logger.log(Level.INFO, "Requesting File: " + fileID + " from User: " + Long.toUnsignedString(uID));
        if(!fileIsThere(uID, fileID)){
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                new ExceptionHandler(e, this.getClass());
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
