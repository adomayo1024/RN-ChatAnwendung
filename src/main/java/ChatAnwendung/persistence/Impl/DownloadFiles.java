package ChatAnwendung.persistence.Impl;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
public class DownloadFiles {

    private static String filePathLin = "Downloads/";

    private static String filePathWin = "Downloads\\";

    private static String filePath = System.getProperty("os.name").toLowerCase().contains("windows") ? filePathWin : filePathLin;

    private ScheduledExecutorService timer;

    private Map<Long, Map<Integer, File>> downloadedFiles;

    private Map<Long, Map<Integer, Boolean>> finishedFiles;

    public DownloadFiles(ScheduledExecutorService timer) {
        this.timer = timer;
        downloadedFiles = new ConcurrentHashMap<>();
        finishedFiles = new ConcurrentHashMap<>();

        new java.io.File(filePath).mkdirs();
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
