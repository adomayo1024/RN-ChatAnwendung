package ChatAnwendung.Impl;

import ChatAnwendung.Api.InputHandler;
import ChatAnwendung.Api.InputReader;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InputReaderImpl implements InputReader, Runnable {

    private InputHandler inputHandler;

    private Logger logger;

    public InputReaderImpl(InputHandler handler) {
        inputHandler = handler;
        logger = Logger.getLogger(InputReaderImpl.class.getName());
    }

    @Override
    public void run() {

        //inputHandler.handle("hello");

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            Storage.getInstance().setReader(reader);
            String stdIn;
            while ((stdIn = reader.readLine()) != null && !Thread.currentThread().isInterrupted()) {
                inputHandler.handle(stdIn);
            }
        } catch (IOException e) {
            logger.log(Level.INFO, "Input Reader is terminated");
        }
    }
}
