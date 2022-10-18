package ru.bvkuchin.fileServer;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Handler implements Runnable {

    private boolean running;
    private byte[] buf;
    private InputStream inputStream;
    private OutputStream outputStream;
    private Socket socket;

    private OutputStream fileOs;

    public Handler(Socket socket) throws IOException {
        this.buf = new byte[1024*8];
        running = true;
        this.socket = socket;
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
    }

    public void stop() throws IOException {
        running = false;
        inputStream.close();
        outputStream.close();
        socket.close();
    }

    @Override
    public void run() {
        try {
            while (running) {
                int read = inputStream.read(buf);
                if (new String(buf, 0, read).split(">>")[0].equals("/filename")) {
                    fileOs = new FileOutputStream("/home/boris/geekbrains/fileServer/src/main/resources/ru/bvkuchin/fileServer/" + new String(buf, 0, read).split(">>")[1]);
                } else {
                    fileOs.write(buf, 0, read);
                    System.out.println(read + "байт получено");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
