package ChatAnwendung.Impl.persistence;

import ChatAnwendung.Impl.Handler.Common.ExceptionHandler;
import ChatAnwendung.Impl.FrequentlySender.RequestSender;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class File {

    private int anzahlChunks;

    private int length;

    private String name;

    private int fileId;

    private long srcUID;

    private boolean[] writedChunks;

    private AtomicInteger requestSendedWithoutAResponse;

    private ScheduledFuture<?> requestTimer;

    private final ScheduledExecutorService executor;

    private ReentrantLock writedChunksMutex;

    private ReentrantLock fileMutex;

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
        this.writedChunksMutex = new ReentrantLock(true);
        this.fileMutex = new ReentrantLock(true);
        makeFile();
        recievedLastChunk = new AtomicLong(System.currentTimeMillis());
        requestSendedWithoutAResponse = new AtomicInteger(0);
    }

    private void makeFile() {
        fileMutex.lock();
        try(RandomAccessFile file = new RandomAccessFile(name, "rw")) {
            file.setLength(length);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            fileMutex.unlock();
        }

        log.debug("Created new File: {}", name);
    }

    public boolean addChunk(byte[] chunk, int sequenz) {

        boolean added = false;
        if(!writedChunks[sequenz]){
            fileMutex.lock();
            try(RandomAccessFile file = new RandomAccessFile(name, "rw")){
                int pos = sequenz * 1300;
                file.seek(pos);
                file.write(chunk);
                writedChunksMutex.lock();
                writedChunks[sequenz] = true;
                file.close();
                added = true;
                dekrementRequestCountWithoutResponse();
                recievedLastChunk.set(System.currentTimeMillis());
                log.debug("Added Chunk {} to File: {}", sequenz, name);
            } catch (IOException e) {
                log.debug("Error ecours: {}", e.getMessage());
                writedChunks[sequenz] = false;
                ExceptionHandler.handle(e, this.getClass());
            }
            finally {
                fileMutex.unlock();
                writedChunksMutex.unlock();
            }

        }

        return added;
    }

    public boolean finished(){
        boolean result;
        int i = 0;
        writedChunksMutex.lock();

        do{
            result = writedChunks[i++];
        }while(i < writedChunks.length && result);

        writedChunksMutex.unlock();

        return result;
    }

    public long getSrcUID() {
        return srcUID;
    }

    public int getFileId() {
        return fileId;
    }

    public int getNextNeededChunk(){
        int i = 0;
        writedChunksMutex.lock();
        boolean written = writedChunks[i];
        while(written && i < writedChunks.length){
            written = writedChunks[++i];
        }
        writedChunksMutex.unlock();
        if(written){
            i = -1;
        }

        log.debug("Next needed Chunk: {}", i);

        return i;

    }

    public void inkrementRequestCountWithoutResponse(){
        requestSendedWithoutAResponse.incrementAndGet();
    }

    public void dekrementRequestCountWithoutResponse(){
        requestSendedWithoutAResponse.decrementAndGet();

        if(requestSendedWithoutAResponse.intValue() < 0){
            requestSendedWithoutAResponse.set(0);
        }
    }

    public long getRecievedLastChunk(){
        return recievedLastChunk.get();
    }


    public String getName() {
        return name;
    }

    public void startRequesting(){
        requestFuture = executor.scheduleAtFixedRate(new RequestSender(this), 1, 1, TimeUnit.SECONDS);
    }

    public void stopRequesting() {
        requestFuture.cancel(true);
    }
}
