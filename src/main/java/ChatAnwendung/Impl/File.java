package ChatAnwendung.Impl;

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

    private ReentrantLock mutex;

    private AtomicLong recievedLastChunk;

    public File(int anzahlChunks, int length, String name, int fileId, long srcUID, ScheduledExecutorService executor){
        this.anzahlChunks = anzahlChunks;
        this.length = length;
        this.name = name;
        this.fileId = fileId;
        this.writedChunks = new boolean[anzahlChunks];
        this.srcUID = srcUID;
        this.executor = executor;
        this.mutex = new ReentrantLock(true);
        makeFile();
        recievedLastChunk = new AtomicLong(System.currentTimeMillis());
        requestSendedWithoutAResponse = new AtomicInteger(0);
    }

    private void makeFile() {
        mutex.lock();
        try(RandomAccessFile file = new RandomAccessFile(name, "rw")) {
            file.setLength(length);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            mutex.unlock();
        }
    }

    public boolean addChunk(byte[] chunk, int sequenz) {
        if(!writedChunks[sequenz]){
            mutex.lock();
            try(RandomAccessFile file = new RandomAccessFile(name, "rw")){
                int pos = sequenz * 1300;
                file.seek(pos);
                file.write(chunk);
                writedChunks[sequenz] = true;
            } catch (IOException e) {
                writedChunks[sequenz] = false;
                ExceptionHandler.handle(e, this.getClass());
            }
            finally {
                mutex.unlock();
            }

            dekrementRequestCountWithoutResponse();
            recievedLastChunk.set(System.currentTimeMillis());
        }


        boolean finished = finished();
        if(finished) {
            executor.shutdown();
            System.out.println("File " + name + " is finished");
        }

        return finished;
    }

    public boolean finished(){
        boolean result;
        int i = 0;
        mutex.lock();

        do{
            result = writedChunks[i++];
        }while(i < writedChunks.length && result);

        mutex.unlock();

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
        boolean written = writedChunks[i];
        while(written && i < writedChunks.length){
            written = writedChunks[i++];
        }
        if(written){
            i = -1;
        }
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
        executor.scheduleAtFixedRate(new RequestSender(this), 1, 1, TimeUnit.SECONDS);
    }
}
