package ChatAnwendung.Impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantLock;

public class DownloadFiles {

    private static  DownloadFiles INSTANCE;

    private final ScheduledExecutorService timer = Executors.newScheduledThreadPool(3);

    private Map<Long, Map<Integer, File>> downloadedFiles;

    private ReentrantLock mutex;

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
        mutex.lock();
        File result = downloadedFiles.get(uID).get(fileID);
        mutex.unlock();
        return result;
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
