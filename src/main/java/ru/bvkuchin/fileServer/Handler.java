package ru.bvkuchin.fileServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Handler implements Runnable {

    private boolean running;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private Socket socket;

    public Handler(Socket socket) throws IOException {
        running = true;
        this.socket = socket;
        this.inputStream = new DataInputStream(socket.getInputStream());
        this.outputStream = new DataOutputStream(socket.getOutputStream());
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

                message = inputStream.readUTF();
//                System.out.println("Received: " + message.trim());
                if (message.equals("stop")) {
                    stop();
                }
                outputStream.writeUTF("echo: " + message);

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
