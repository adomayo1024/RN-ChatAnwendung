package ChatAnwendung.Impl.persistence;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import java.util.Map;

@Slf4j
public class Storage {

    private static Storage INSTANCE;

    private static final ReentrantLock getMutex = new ReentrantLock(true);

    private final Long broadcastId;

    @Getter
    private long ID;

    private final Logger logger = Logger.getLogger(Storage.class.getName());

    @Setter
    @Getter
    private int port;

    @Getter
    private boolean login;

    @Setter
    @Getter
    private BufferedReader reader;

    private Map<Integer, String> openSendFiles;

    private int fileCount;

    private final boolean DEBUG_MODE = false;

    private Storage() throws NoSuchAlgorithmException {
        broadcastId = -1L;
        login = false;
        openSendFiles = new HashMap<>();
        ID = SecureRandom.getInstanceStrong().nextLong();
    }

    public static Storage getInstance() {
        getMutex.lock();
        if(INSTANCE == null) {
            try {
                INSTANCE = new Storage();
                log.debug("Created new Storage: {}", INSTANCE);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
        getMutex.unlock();
        return INSTANCE;
    }

    public void setSendOpenFile(int fileId, String path){
        openSendFiles.put(fileId, path);
    }

    public int getNextFileID(){
        return fileCount++;
    }

    public long getBroadCastId(){
        return broadcastId;
    }

    public void login(){
        login = true;
    }

    public void logout() {
        login = false;
    }

    public boolean isDebugMode() {
        return DEBUG_MODE;
    }

    public String getUnsignedID() {
        return Long.toUnsignedString(ID);
    }

    public String getOpenFile(int fileID){
        return openSendFiles.get(fileID);
    }

}
