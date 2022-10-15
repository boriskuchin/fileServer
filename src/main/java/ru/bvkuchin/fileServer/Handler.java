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

    public Handler(Socket socket) throws IOException {
        this.buf = new byte[8192];
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
        while (running) {
            String message = null;
            try {
                int read = inputStream.read(buf);
                message = new String(buf, 0, read);
//                System.out.println("Received: " + message.trim());
                if (message.equals("stop")) {
                    stop();
                }
                String serverReplyMsg = "echo: " + message;
                outputStream.write(serverReplyMsg.getBytes(StandardCharsets.UTF_8));

            } catch (IOException e) {
                e.printStackTrace();
                try {
                    stop();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            }

        }
    }
}
