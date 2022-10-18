package ru.bvkuchin.fileServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerIO {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(9999);
        System.out.println("Server started...");
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                Thread t = new Thread(new Handler(socket));
                t.start();
                System.out.println("Client connected...");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}