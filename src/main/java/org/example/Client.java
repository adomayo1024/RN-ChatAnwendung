package org.example;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Client {

    public static void main(String[] args) throws IOException {

        //byte[] buffer = new byte[] {(byte) 192, (byte) 168, 1, 1};
        byte[] buffer = new byte[] {(byte) 127, (byte) 0, (byte) 0, (byte) 1};
        InetAddress serverAddress = InetAddress.getByAddress(buffer);
        int port = 6789; // Der Port, auf dem der Server hört

        try (DatagramSocket socket = new DatagramSocket()) {

            java.io.BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            String userInput;

            while ((userInput = stdIn.readLine()) != null) {
                byte[] sendData = userInput.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, port);
                socket.send(sendPacket);

                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);

                String received = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.println("Echo vom Server: " + received);
            }
        } catch (UnknownHostException e) {
            System.err.println("Host unbekannt: " + serverAddress);
            System.exit(1);


        }
    }

}
