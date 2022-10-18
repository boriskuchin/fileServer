package ru.bvkuchin.client;

import ru.bvkuchin.client.controllers.ChatController;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class IONet {

    private final byte[] buffer;
    private ChatController chatcontroller;
    private OutputStream outputStream;
    private InputStream inputStream;
    private Socket socket;

    public IONet(Socket socket, ChatController chatController) throws IOException {
        this.chatcontroller = chatController;
        this.socket = socket;
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
        this.buffer = new byte[1024*8];
        Thread readThread = new Thread(this::readMessage);
        readThread.setDaemon(true);
        readThread.start();
    }


    public void sendMsg(String msg) throws IOException {
        outputStream.write(msg.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
    }

    public void readMessage()  {
        try {
            while (true) {
                int read = inputStream.read(buffer);
                String message = new String(buffer, 0, read);
                chatcontroller.addTextToList(message);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendFile(File file) throws FileNotFoundException {
        int read;
        InputStream is = new FileInputStream(file);
        try {
            while ((read = is.read(buffer)) > 0) {
                outputStream.write(buffer, 0, read);
                System.out.println(read + "байт передано. Осталось " + is.available());

            }
            System.out.println("фаил отправлен");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() throws IOException {
        outputStream.close();
        inputStream.close();
        socket.close();
    }
}
