package ChatAnwendung.persistence.Impl;

import ChatAnwendung.logic.Impl.BCPPacketImpl;
import ChatAnwendung.persistence.Api.File;
import ChatAnwendung.Exceptions.ExceptionHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public class FileImpl implements File {

    // Dateiname
    @Getter
    private String name;

    @Getter
    private int anzahlChunks;

    //spezieller Pfad für Linux zu den Downloads Ordner
    private static final String filePathLin = "Downloads/";

    //spezieller Pfad für Windows zu den Downloads Ordner
    private static final String filePathWin = "Downloads\\";

    //Der Pfad, der zur Laufzeit benutzt werden soll, abhängig vom Betriebssystem
    private static final String filePath = System.getProperty("os.name").toLowerCase().contains("windows") ? filePathWin : filePathLin;

    // die Id des files
    @Getter
    private int fileId;

    // die NodeId des Senders
    @Getter
    private long srcNodeId;

    // Welche Chunks schon gespeichert wurden
    private final boolean[] writtenChunks;

    // Array, wo alle Chunks temporär gespeichert werden, bis alle empfangen wurden
    private final byte[] chunksSent;

    // Mutex für den Zugriff von writtenChunks und chunksSent
    private final ReentrantReadWriteLock writtenChunksMutex;

    // Zeitpunkt des neuesten empfangenen Chunks
    private final AtomicLong receivedLastChunk;

    /**
     * Konstruktor, welcher ein File erstellt, welche heruntergeladen werden soll.
     * @param anzahlChunks Die Anzahl der Chunks, welche die Datei haben wird
     * @param length die Länge der Datei in Bytes
     * @param name der Dateiname
     * @param fileId die Id des Files
     * @param srcNodeId die NodeId des Senders
     */
    public FileImpl(int anzahlChunks, int length, String name, int fileId, long srcNodeId){
        this.name = name;
        this.anzahlChunks = anzahlChunks;
        this.fileId = fileId;
        this.writtenChunks = new boolean[anzahlChunks];
        this.chunksSent = new byte[length];
        this.srcNodeId = srcNodeId;
        this.writtenChunksMutex = new ReentrantReadWriteLock(true);
        receivedLastChunk = new AtomicLong(System.currentTimeMillis());
        log.debug("Created File: {}", this);
    }

    @Override
    public boolean addChunk(byte[] chunk, int sequenz) {

        boolean added = false;

        writtenChunksMutex.readLock().lock();

        // Prüft, ob der Chunk schon gespeichert wurde
        if(!writtenChunks[sequenz]){

            writtenChunksMutex.readLock().unlock();
            writtenChunksMutex.writeLock().lock();

            // Speichert den Chunk
            System.arraycopy(chunk, 0, chunksSent, sequenz * BCPPacketImpl.MAXIMUM_PAYLOAD_SIZE, chunk.length);
            writtenChunks[sequenz] = true;
            added = true;


            writtenChunksMutex.writeLock().unlock();
            receivedLastChunk.set(System.currentTimeMillis());


            log.debug("Added Chunk {} to File: {}", sequenz, name);
        }else {
            writtenChunksMutex.readLock().unlock();
        }
        return added;
    }
    @Override
    public void safeFile(){
        // Prüft, ob alle Chunks gespeichert wurden
        if(finished()){


            writtenChunksMutex.readLock().lock();

            //Schreibt alle Chunks in eine Datei
            try(RandomAccessFile file = new RandomAccessFile(filePath + name, "rw")){
                file.write(chunksSent);
            } catch (IOException e) {
                ExceptionHandler.handle(e, this.getClass());
            }
            finally {
                writtenChunksMutex.readLock().unlock();
            }
        }

        log.debug("File {} saved", name);
        System.out.println("File " + name + " saved");
    }

    @Override
    public boolean finished(){
        boolean result;
        int i = 0;

        writtenChunksMutex.readLock().lock();

        // Prüft, ob alle Chunks aufgespeichert gesetzt wurden
        do{
            result = writtenChunks[i++];
        }while(i < writtenChunks.length && result);

        writtenChunksMutex.readLock().unlock();

        return result;
    }

    @Override
    public List<Integer> getMissingChunks(){

        writtenChunksMutex.readLock().lock();

        //Prüft, welche Chunks noch nicht aufgespeichert gesetzt wurden
        List<Integer> missingChunks = new ArrayList<>();
        for(int i = 0; i < writtenChunks.length; i++){
            if(!writtenChunks[i]){
                missingChunks.add(i);
            }
            //Prüft, ob 1000 Chunks erreicht wurde
            if(missingChunks.size() >= 1000){
                break;
            }
        }
        writtenChunksMutex.readLock().unlock();

        log.debug("Next needed Chunks: {}", missingChunks.getFirst());

        return missingChunks;

    }

    @Override
    public long getReceivedLastChunk(){
        return receivedLastChunk.get();
    }

    @Override
    public int getProzent() {
        int countReceived = 0;
        for(boolean b : writtenChunks){
            if(b){
                countReceived++;
            }
        }
        return (int) Math.round((double) countReceived / writtenChunks.length * 100);
    }
}

