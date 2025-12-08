package ChatAnwendung.Impl;

import java.io.BufferedReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;

public class Storage {

    private static Storage INSTANCE;

    private final ExecutorService threadPool;

    private final Long broadcastId;

    private long ID;

    private final Logger logger = Logger.getLogger(Storage.class.getName());

    private int port;

    private boolean login;

    private BufferedReader reader;

    private Map<Integer, String> openSendFiles;

    private int fileCount;

    private final boolean DEBUG_MODE = false;

    private final SendMode sendMode = SendMode.SELF;

    private Storage() throws NoSuchAlgorithmException {
        threadPool = Executors.newFixedThreadPool(10);
        broadcastId = -1L;
        login = false;
        openSendFiles = new HashMap<>();
        ID = SecureRandom.getInstanceStrong().nextLong();
    }

    public static Storage getInstance() {
        if(INSTANCE == null) {
            try {
                INSTANCE = new Storage();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

        return INSTANCE;
    }

    public SendMode getSendMode() {
        return sendMode;
    }

    public void setSendOpenFile(int fileId, String path){
        openSendFiles.put(fileId, path);
    }

    public int getNextFileID(){
        return fileCount++;
    }

    public long getID() {
        return ID;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort(){
        return port;
    }

    public ExecutorService getThreadPool() {
        return threadPool;
    }

    public long getBroadCastId(){
        return broadcastId;
    }

    public void shutDown(){
        threadPool.shutdownNow();
    }

    public void login(){
        login = true;
    }

    public void logout() {
        login = false;
    }

    public boolean isLogin() {
        return login;
    }

    public void setReader(BufferedReader reader){
        this.reader = reader;
    }

    public BufferedReader getReader(){
        return reader;
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
