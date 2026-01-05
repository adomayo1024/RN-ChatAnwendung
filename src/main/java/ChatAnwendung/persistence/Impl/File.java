package ChatAnwendung.persistence.Impl;

import ChatAnwendung.Exceptions.IllegalSequnzNumberException;
import ChatAnwendung.persistence.Api.RoutingTable;
import ChatAnwendung.Exceptions.ExceptionHandler;
import ChatAnwendung.logic.Impl.RequestSender;
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
public class File {

    private int anzahlChunks;

    private int length;

    @Getter
    private String name;

    private static String filePathLin = "Downloads/";

    private static String filePathWin = "Downloads\\";

    private static String filePath = System.getProperty("os.name").toLowerCase().contains("windows") ? filePathWin : filePathLin;

    @Getter
    private int fileId;

    @Getter
    private long srcUID;

    private boolean[] writedChunks;

    private byte[] chunksSended;

    private final ScheduledExecutorService executor;

    private ReentrantReadWriteLock writedChunksMutex;

    private AtomicLong recievedLastChunk;

    private ScheduledFuture<?> requestFuture;

    public File(int anzahlChunks, int length, String name, int fileId, long srcUID, ScheduledExecutorService executor){
        this.anzahlChunks = anzahlChunks;
        this.length = length;
        this.name = name;
        this.fileId = fileId;
        this.writedChunks = new boolean[anzahlChunks];
        this.srcUID = srcUID;
        this.executor = executor;
        this.writedChunksMutex = new ReentrantReadWriteLock(true);
        this.chunksSended = new byte[length];
        recievedLastChunk = new AtomicLong(System.currentTimeMillis());
        log.debug("Created File: {}", this);
    }

    public boolean addChunk(byte[] chunk, int sequenz) {

//        boolean added = false;
//        if(!writedChunks[sequenz]){
//            fileMutex.lock();
//            try(RandomAccessFile file = new RandomAccessFile(filePath + name, "rw")){
//                int pos = sequenz * 1300;
//                file.seek(pos);
//                file.write(chunk);
//                writedChunksMutex.lock();
//                writedChunks[sequenz] = true;
//                file.close();
//                added = true;
//                recievedLastChunk.set(System.currentTimeMillis());
//                log.debug("Added Chunk {} to File: {}", sequenz, name);
//            } catch (IOException e) {
//                log.debug("Error ecours: {}", e.getMessage());
//                writedChunks[sequenz] = false;
//                ExceptionHandler.handle(e, this.getClass());
//            }
//            finally {
//                fileMutex.unlock();
//                writedChunksMutex.unlock();
//            }
//
//        }

        boolean added = false;

        writedChunksMutex.readLock().lock();
        if(!writedChunks[sequenz]){
            writedChunksMutex.readLock().unlock();
            writedChunksMutex.writeLock().lock();
            System.arraycopy(chunk, 0, chunksSended, sequenz * 1300, chunk.length);
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
    public void safeFile(){
        if(finished()){
            try(RandomAccessFile file = new RandomAccessFile(filePath + name, "rw")){
                file.write(chunksSended);
            } catch (FileNotFoundException e) {
                ExceptionHandler.handle(e, this.getClass());
            } catch (IOException e) {
                ExceptionHandler.handle(e, this.getClass());
            }
        }

        log.debug("File {} saved", name);
        System.out.println("File " + name + " saved");
    }

    public boolean finished(){
        boolean result;
        int i = 0;
        writedChunksMutex.readLock().lock();

        do{
            result = writedChunks[i++];
        }while(i < writedChunks.length && result);

        writedChunksMutex.readLock().unlock();

        return result;
    }

    public byte[] getChunk(int sequenz){
        if(anzahlChunks <= sequenz || sequenz < 0){
            ExceptionHandler.handle(new IllegalSequnzNumberException(sequenz), this.getClass());
        }

        byte[] chunk = null;

        if(anzahlChunks - 1 == sequenz){
            int size = (length % 1300);
            chunk = new byte[size];
        }
        else{
            chunk = new byte[1300];
        }

        try(RandomAccessFile file = new RandomAccessFile(filePath + name, "r")) {
            file.seek(sequenz * 1300L);
            file.read(chunk);
        } catch (IOException e) {
            ExceptionHandler.handle(e, this.getClass());
        }
        return chunk;

        }

    public List<Integer> getMissingChunks(){
        writedChunksMutex.readLock().lock();
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

    public long getRecievedLastChunk(){
        return recievedLastChunk.get();
    }


    public void startRequesting(DownloadFiles downloadFiles, RoutingTable routingTable, Storage storage, BlockingQueue<DatagramPacket> sendeQueue){
        requestFuture = executor.scheduleAtFixedRate(new RequestSender(this, downloadFiles, routingTable, storage, sendeQueue), 3, 1, TimeUnit.SECONDS);
    }

    public void stopRequesting() {
        requestFuture.cancel(true);
    }
}

