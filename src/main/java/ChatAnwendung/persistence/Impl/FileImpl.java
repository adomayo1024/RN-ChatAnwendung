package ChatAnwendung.persistence.Impl;

import ChatAnwendung.logic.Impl.BCPPacketImpl;
import ChatAnwendung.persistence.Api.DownloadFiles;
import ChatAnwendung.persistence.Api.File;
import ChatAnwendung.persistence.Api.RoutingTable;
import ChatAnwendung.Exceptions.ExceptionHandler;
import ChatAnwendung.logic.Impl.RequestSenderImpl;
import ChatAnwendung.persistence.Api.Storage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public class FileImpl implements File {

    private int anzahlChunks;

    private int length;

    // Dateiname
    @Getter
    private String name;

    //spezieller Pfad für Linux zu den Downloads Ordner
    private static String filePathLin = "Downloads/";

    //spezieller Pfad für Windows zu den Downloads Ordner
    private static String filePathWin = "Downloads\\";

    //Der Pfad, der zur Laufzeit benutzt werden soll, abhängig vom Betriebssystem
    private static String filePath = System.getProperty("os.name").toLowerCase().contains("windows") ? filePathWin : filePathLin;

    // die Id des files
    @Getter
    private int fileId;

    // die NodeId des Senders
    @Getter
    private long srcNodeId;

    // welche chunks schon gespeichert wurden
    private boolean[] writedChunks;

    // array wo alle chunks temporär gespeichert werden, bis alle empfangen wurden
    private byte[] chunksSended;

    // der executor für den ReuqestSender, dieses files
    private final ScheduledExecutorService executor;

    // Mutex für den Zugrif von writedChunks und chunksSended
    private ReentrantReadWriteLock writedChunksMutex;

    // Zeitpunkt des neuesten empfangenen chunks
    private AtomicLong recievedLastChunk;

    // Future des RequestSender Threads
    private ScheduledFuture<?> requestFuture;

    /**
     * Konstruktor welcher ein File erstellt werlches heruntergelanden werden soll.
     * @param anzahlChunks die Anzahl der chunks welche die Datei haben wird
     * @param length die länge der Datei in Bytes
     * @param name der Dateiname
     * @param fileId die Id des Files
     * @param srcNodeId die NodeId des Senders
     * @param executor der Executor für den RequestSender
     */
    public FileImpl(int anzahlChunks, int length, String name, int fileId, long srcNodeId, ScheduledExecutorService executor){
        this.anzahlChunks = anzahlChunks;
        this.length = length;
        this.name = name;
        this.fileId = fileId;
        this.writedChunks = new boolean[anzahlChunks];
        this.chunksSended = new byte[length];
        this.srcNodeId = srcNodeId;
        this.executor = executor;
        this.writedChunksMutex = new ReentrantReadWriteLock(true);
        recievedLastChunk = new AtomicLong(System.currentTimeMillis());
        log.debug("Created File: {}", this);
    }

    @Override
    public boolean addChunk(byte[] chunk, int sequenz) {

        boolean added = false;

        writedChunksMutex.readLock().lock();

        // prüft, ob der chunk schon gespeichert wurde
        if(!writedChunks[sequenz]){

            writedChunksMutex.readLock().unlock();
            writedChunksMutex.writeLock().lock();

            // speichert den chunk
            System.arraycopy(chunk, 0, chunksSended, sequenz * BCPPacketImpl.getMaximumPayloadSize(), chunk.length);
            writedChunks[sequenz] = true;
            added = true;


            writedChunksMutex.writeLock().unlock();
            recievedLastChunk.set(System.currentTimeMillis());


            log.debug("Added Chunk {} to File: {}", sequenz, name);
        }else {
            writedChunksMutex.readLock().unlock();
        }
        return added;
    }
    @Override
    public void safeFile(){
        // prüft, ob alle chunks gespeichert wurden
        if(finished()){


            writedChunksMutex.readLock().lock();

            //schreibt alle chunks in eine Datei
            try(RandomAccessFile file = new RandomAccessFile(filePath + name, "rw")){
                file.write(chunksSended);
            } catch (FileNotFoundException e) {
                ExceptionHandler.handle(e, this.getClass());
            } catch (IOException e) {
                ExceptionHandler.handle(e, this.getClass());
            }
            finally {
                writedChunksMutex.readLock().unlock();
            }
        }

        log.debug("File {} saved", name);
        System.out.println("File " + name + " saved");
    }

    @Override
    public boolean finished(){
        boolean result;
        int i = 0;

        writedChunksMutex.readLock().lock();

        // Prüft ob alle chunks auf gespeichert gesetzt wurden
        do{
            result = writedChunks[i++];
        }while(i < writedChunks.length && result);

        writedChunksMutex.readLock().unlock();

        return result;
    }

    @Override
    public List<Integer> getMissingChunks(){

        writedChunksMutex.readLock().lock();

        //prüft welche chunks nocht nicht auf gespeichert gesetzt wurden
        List<Integer> missingChunks = new ArrayList<>();
        for(int i = 0; i < writedChunks.length; i++){
            if(!writedChunks[i]){
                missingChunks.add(i);
            }
        }
        writedChunksMutex.readLock().unlock();

        log.debug("Next needed Chunks: {}", missingChunks);

        return missingChunks;

    }

    @Override
    public long getReceivedLastChunk(){
        return recievedLastChunk.get();
    }


    @Override
    public void startRequesting(DownloadFiles downloadFiles, RoutingTable routingTable, Storage storage, BlockingQueue<DatagramPacket> sendeQueue){
        requestFuture = executor.scheduleAtFixedRate(new RequestSenderImpl(this, downloadFiles, routingTable, storage, sendeQueue), 3, 1, TimeUnit.SECONDS);
    }

    @Override
    public void stopRequesting() {
        requestFuture.cancel(true);
    }
}

