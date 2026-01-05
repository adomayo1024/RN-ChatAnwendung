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

    private final Long broadCastId;

    @Getter
    private long ID;

    @Setter
    @Getter
    private int port;

    @Getter
    private boolean login;

    @Setter
    @Getter
    private BufferedReader reader;

    private final Map<Integer, String> openSendFiles;

    private int fileCount;

    private final boolean DEBUG_MODE = false;

    public Storage() throws NoSuchAlgorithmException {
        broadCastId = -1L;
        login = false;
        openSendFiles = new HashMap<>();
        ID = SecureRandom.getInstanceStrong().nextLong();
    }

    public void setSendOpenFile(int fileId, String path){
        openSendFiles.put(fileId, path);
    }

    public int getNextFileID(){
        return fileCount++;
    }

    public long getBroadCastId(){
        return broadCastId;
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
