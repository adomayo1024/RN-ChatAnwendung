package ChatAnwendung.persistence.Impl;

import ChatAnwendung.persistence.Api.Storage;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class StorageImpl implements Storage {

    // Die Id die in einem Paket als srcNode Id verwendet werden soll, wenn es ein Broadcast ist
    @Getter
    private final Long broadCastId;

    // Die NodeId dieses Hosts
    @Getter
    private long ID;

    // Der Port, an dem der Host lauscht
    @Setter
    @Getter
    private int port;

    // Ob der Host eingeloggt ist
    @Getter
    private boolean login;

    // Der BufferedReader, der zum Lesen, der User Inputs verwendet wird
    @Setter
    @Getter
    private BufferedReader reader;

    // Speichert alle Datei Pfade, die dieser Host gesendet hat, gemappt auf die File ID
    private final Map<Integer, String> openSendFiles;

    // Wie viele Dateien gesendet wurden, dadurch wird die File ID der nächsten zu senden Dateien ermittelt
    private int fileCount;

    /**
     * Konstruktor eines Storages.
     * @throws NoSuchAlgorithmException Wird geschmissen, wenn zur Ermittlung der NodeId der benutzte Algorithmus nicht existiert.
     */
    public StorageImpl() throws NoSuchAlgorithmException {
        broadCastId = -1L;
        login = false;
        openSendFiles = new HashMap<>();
        ID = SecureRandom.getInstanceStrong().nextLong();
    }

    @Override
    public void addSendOpenFile(int fileId, String path){
        openSendFiles.put(fileId, path);
    }

    @Override
    public int getNextFileID(){
        return fileCount++;
    }

    @Override
    public void login(){
        login = true;
    }

    @Override
    public void logout() {
        login = false;
    }

    @Override
    public String getUnsignedID() {
        return Long.toUnsignedString(ID);
    }

    @Override
    public String getOpenFile(int fileID){
        return openSendFiles.get(fileID);
    }
}
