package ChatAnwendung.Impl;

import ChatAnwendung.Api.InputHandler;
import ChatAnwendung.Api.InputReader;
import ChatAnwendung.Impl.Handler.InputHandlers.InputHandlerImpl;
import ChatAnwendung.Impl.persistence.Storage;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.concurrent.BlockingQueue;

@Slf4j
public class InputReaderImpl implements InputReader, Runnable {

    private final BlockingQueue<String> inputQueue;

    private final Storage storage;

    public InputReaderImpl(BlockingQueue<String> inputQueue, Storage storage) {
        this.inputQueue = inputQueue;
        this.storage = storage;
    }

    @Override
    public void run() {
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            storage.setReader(reader);
            String stdIn;
            while ((stdIn = reader.readLine()) != null && !Thread.currentThread().isInterrupted()) {
                inputQueue.add(stdIn);
            }
        } catch (IOException e) {
            log.debug( "Input Reader is terminated");
        }
    }
}
