package ChatAnwendung.Impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class File {

    private int anzahlChunks;

    private int length;

    private String name;

    private int fileId;

    private long srcUID;

    private boolean[] writedChunks;

    private int requestSendedWithoutAResponse;

    private ScheduledFuture<?> requestTimer;

    private final ScheduledExecutorService executor;

    private ReentrantLock mutex;

    private long recievedLastChunk;

    public File(int anzahlChunks, int length, String name, int fileId, long srcUID, ScheduledExecutorService executor){
        this.anzahlChunks = anzahlChunks;
        this.length = length;
        this.name = name;
        this.fileId = fileId;
        this.writedChunks = new boolean[anzahlChunks];
        this.srcUID = srcUID;
        this.executor = executor;
        this.mutex = new ReentrantLock(true);
        requestTimer = executor.scheduleAtFixedRate(new RequestSender(this), 1, 1, TimeUnit.SECONDS);
        makeFile();
        recievedLastChunk = 0;
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
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            finally {
                mutex.unlock();
            }

            writedChunks[sequenz] = true;
            dekrementRequestCountWithoutResponse();
            recievedLastChunk = System.currentTimeMillis();
        }

        boolean finished = finished();
        if(finished) {
            executor.shutdown();
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
        boolean written= writedChunks[i];
        while(written && i < writedChunks.length){
            written = writedChunks[i++];
        }
        if(written){
            i = -1;
        }
        return i;

    }

    public void inkrementRequestCountWithoutResponse(){
        requestSendedWithoutAResponse++;
    }

    public void dekrementRequestCountWithoutResponse(){
        requestSendedWithoutAResponse--;

        if(requestSendedWithoutAResponse < 0){
            requestSendedWithoutAResponse = 0;
        }
    }

    public long getRecievedLastChunk(){
        return recievedLastChunk;
    }



}
