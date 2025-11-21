package org.example;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        int port = 6789;
        Path path = Path.of("neueDatei");// Der Port, auf dem der Server hört

        try (DatagramSocket socket = new DatagramSocket(port)) {
            System.out.println("UDP Server gestartet und hört auf Port " + port);

            while (true) {
                try {
                    byte[] buffer = new byte[1024];
                    DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                    socket.receive(request);

                    String received = new String(request.getData(), 0, request.getLength());
                    System.out.println("Empfangen von Port " + request.getPort() + ": " + received);

                    // Antwort senden
                    byte[] responseData = received.getBytes();
                    DatagramPacket response = new DatagramPacket(responseData, responseData.length, request.getAddress(), request.getPort());
                    socket.send(response);


                } catch (IOException e) {
                    System.out.println("IOException: " + e.getMessage());
                }
            }
        } catch (SocketException e) {
            System.out.println("SocketException: " + e.getMessage());
        }
    }
}