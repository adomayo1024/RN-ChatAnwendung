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

    public InputReaderImpl(BlockingQueue<String> inputQueue) {
        this.inputQueue = inputQueue;
    }

    @Override
    public void run() {
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            Storage.getInstance().setReader(reader);
            String stdIn;
            while ((stdIn = reader.readLine()) != null && !Thread.currentThread().isInterrupted()) {
                inputHandler.handle(stdIn);
            }
        } catch (IOException e) {
            log.debug( "Input Reader is terminated");
        }
    }
}
