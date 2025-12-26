package ChatAnwendung.Impl;

import ChatAnwendung.Api.InputHandler;
import ChatAnwendung.Api.InputReader;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Slf4j
public class InputReaderImpl implements InputReader, Runnable {

    private final InputHandler inputHandler;


    public InputReaderImpl(InputHandler handler) {
        inputHandler = handler;
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
