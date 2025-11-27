package ChatAnwendung;

import ChatAnwendung.Api.InputReader;
import ChatAnwendung.Impl.Handler.InputHandlerImple;
import ChatAnwendung.Impl.InputReaderImpl;

import java.net.*;
import java.util.concurrent.CompletableFuture;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {

        try(DatagramSocket socket = new DatagramSocket(0)){

            CompletableFuture<Void> task = CompletableFuture.runAsync(new InputReaderImpl(new InputHandlerImple()));
            Thread.sleep(60_000);
            task.cancel(true);

        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }




}