package ChatAnwendung.Impl;

import ChatAnwendung.Api.InputHandler;
import ChatAnwendung.Api.InputReader;

import java.io.*;

public class InputReaderImpl implements InputReader, Runnable {

    private InputHandler inputHandler;

    public InputReaderImpl(InputHandler handler) {
        inputHandler = handler;
    }

    @Override
    public void run() {
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String stdIn;
            while ((stdIn = reader.readLine()) != null && !Thread.currentThread().isInterrupted()) {
                System.out.println("Recieved!");
                inputHandler.handle(stdIn);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
