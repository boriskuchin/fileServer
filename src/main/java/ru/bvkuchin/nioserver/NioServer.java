package ru.bvkuchin.nioserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class NioServer {

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;

    private ByteBuffer buffer;

    String currendDir = System.getProperty("user.dir");


    public NioServer(int port) {
        try {
            buffer = ByteBuffer.allocate(10);
            serverSocketChannel = ServerSocketChannel.open();
            selector = Selector.open();
            serverSocketChannel.bind(new InetSocketAddress(port));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (serverSocketChannel.isOpen()) {
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isAcceptable()) {
                        handleAccept();
                    }
                    if (key.isReadable()) {
                        handleRead(key);
                    }
                    iterator.remove();
                    
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        StringBuilder builder = new StringBuilder();
        while (true) {
            int read = channel.read(buffer);
            if (read == -1) {
                channel.close();
                return;
            }
            if (read == 0) {
                break;
            }
            buffer.flip();
            while (buffer.hasRemaining()) {
                builder.append((char) buffer.get());
            }
            buffer.clear();

        }

        String[] response = builder.toString().trim().split(" ", 2);



        switch (response[0]) {
            case "ls":
                List<String> fileList = Files.list(Paths.get(currendDir))
                        .filter(file -> !Files.isDirectory(file))
                        .map(file -> file.getFileName())
                        .map(Path::toString)
                        .collect(Collectors.toList());

                List<String> dirList = Files.list(Paths.get(currendDir))
                        .filter(file -> Files.isDirectory(file))
                        .map(file -> file.getFileName())
                        .map(Path::toString)
                        .collect(Collectors.toList());

                for (String file : dirList) {
                    channel.write(ByteBuffer.wrap((file + "/" + System.lineSeparator()).getBytes(StandardCharsets.UTF_8)));
                }
                if (fileList.size() == 0) {
                    channel.write(ByteBuffer.wrap((System.lineSeparator() + currendDir + "> " ).getBytes(StandardCharsets.UTF_8)));
                }

                for (String file : fileList) {
                    channel.write(ByteBuffer.wrap((file + System.lineSeparator()).getBytes(StandardCharsets.UTF_8)));

                }
                if (fileList.size() != 0) {
                    channel.write(ByteBuffer.wrap((System.lineSeparator() + currendDir + "> ").getBytes(StandardCharsets.UTF_8)));
                }
                break;
            case "cd":
                if (response.length == 1) {
                    channel.write(ByteBuffer.wrap(("Provide arguments").getBytes(StandardCharsets.UTF_8)));
                    channel.write(ByteBuffer.wrap((System.lineSeparator() + currendDir + "> " ).getBytes(StandardCharsets.UTF_8)));
                    break;
                }
                if (response[1].equals("..")) {
                    currendDir = Paths.get(currendDir).toFile().getParentFile().getAbsolutePath();
                    channel.write(ByteBuffer.wrap((currendDir + "> " ).getBytes(StandardCharsets.UTF_8)));
                } else {
                    if (Paths.get(currendDir).resolve(response[1]).toFile().exists()) {
                        currendDir = Paths.get(currendDir).resolve(response[1]).toFile().getAbsolutePath();
                        channel.write(ByteBuffer.wrap((System.lineSeparator() + currendDir + "> " ).getBytes(StandardCharsets.UTF_8)));
                    } else {
                        channel.write(ByteBuffer.wrap(("No such folder exists").getBytes(StandardCharsets.UTF_8)));
                        channel.write(ByteBuffer.wrap((System.lineSeparator() + currendDir + "> " ).getBytes(StandardCharsets.UTF_8)));
                    }
                }

                break;
            case "cat":
                if (response.length == 1) {
                    channel.write(ByteBuffer.wrap(("Provide arguments").getBytes(StandardCharsets.UTF_8)));
                    channel.write(ByteBuffer.wrap((System.lineSeparator() + currendDir + "> " ).getBytes(StandardCharsets.UTF_8)));
                    break;
                }
                File file = Paths.get(currendDir).resolve(response[1]).toFile();
                if (file.exists() && !Files.isDirectory(file.toPath())) {
                    List<String> lines = Files.readAllLines(file.toPath());
                    for (String line : lines) {
                        channel.write(ByteBuffer.wrap((line + System.lineSeparator()).getBytes(StandardCharsets.UTF_8)));
                    }
                    channel.write(ByteBuffer.wrap((System.lineSeparator() + currendDir + "> ").getBytes(StandardCharsets.UTF_8)));
                } else {
                    channel.write(ByteBuffer.wrap(("No such file exist").getBytes(StandardCharsets.UTF_8)));
                    channel.write(ByteBuffer.wrap((System.lineSeparator() + currendDir + "> " ).getBytes(StandardCharsets.UTF_8)));
                }

                break;

            case "mkdir":
                if (response.length == 1) {
                    channel.write(ByteBuffer.wrap(("Provide arguments").getBytes(StandardCharsets.UTF_8)));
                    channel.write(ByteBuffer.wrap((System.lineSeparator() + currendDir + "> " ).getBytes(StandardCharsets.UTF_8)));
                    break;
                }
                File newDir = Paths.get(currendDir + File.separator + response[1]).toFile();
                if (newDir.mkdir()) {
                    channel.write(ByteBuffer.wrap(("Folder created").getBytes(StandardCharsets.UTF_8)));
                    channel.write(ByteBuffer.wrap((System.lineSeparator() + currendDir + "> " ).getBytes(StandardCharsets.UTF_8)));
                } else {
                    channel.write(ByteBuffer.wrap(("Folder NOT created").getBytes(StandardCharsets.UTF_8)));
                    channel.write(ByteBuffer.wrap((System.lineSeparator() + currendDir + "> " ).getBytes(StandardCharsets.UTF_8)));
                }
                break;
            case "touch":
                if (response.length == 1) {
                    channel.write(ByteBuffer.wrap(("Provide arguments").getBytes(StandardCharsets.UTF_8)));
                    channel.write(ByteBuffer.wrap((System.lineSeparator() + currendDir + "> " ).getBytes(StandardCharsets.UTF_8)));
                    break;
                }
                // если было передано только одно имя
                if (response[1].split(" ",2).length == 1) {
                    File newFile = Paths.get(currendDir + File.separator + response[1]).toFile();
                    if (newFile.exists()) {
                        channel.write(ByteBuffer.wrap(("File "+ response[1] +" already exists").getBytes(StandardCharsets.UTF_8)));
                        channel.write(ByteBuffer.wrap((System.lineSeparator() + currendDir + "> " ).getBytes(StandardCharsets.UTF_8)));
                        break;
                    }
                    if (newFile.createNewFile()) {
                        channel.write(ByteBuffer.wrap(("File "+ response[1] +" created").getBytes(StandardCharsets.UTF_8)));
                        channel.write(ByteBuffer.wrap((System.lineSeparator() + currendDir + "> " ).getBytes(StandardCharsets.UTF_8)));
                    } else {
                        channel.write(ByteBuffer.wrap(("File "+ response[1] +" NOT created").getBytes(StandardCharsets.UTF_8)));
                        channel.write(ByteBuffer.wrap((System.lineSeparator() + currendDir + "> " ).getBytes(StandardCharsets.UTF_8)));
                    }
                    break;
                }

            // если было передано имя и строка
            if (response[1].split(" ",2).length == 2) {
                String fileName = response[1].split(" ")[0];
                String message = response[1].split(" ")[1];
                File newFile = Paths.get(currendDir + File.separator + fileName).toFile();
                if (newFile.exists()) {
                    try (RandomAccessFile writer = new RandomAccessFile(newFile, "rw");
                         FileChannel fileChannel = writer.getChannel()) {
                        fileChannel.write(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)));
                        fileChannel.close();
                        channel.write(ByteBuffer.wrap(("Message has been written to an existing file " + fileName).getBytes(StandardCharsets.UTF_8)));
                        channel.write(ByteBuffer.wrap((System.lineSeparator() + currendDir + "> ").getBytes(StandardCharsets.UTF_8)));
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    if (newFile.createNewFile()) {
                        try (RandomAccessFile writer = new RandomAccessFile(newFile, "rw");
                             FileChannel fileChannel = writer.getChannel()) {
                            fileChannel.write(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)));
                            fileChannel.close();
                            channel.write(ByteBuffer.wrap((System.lineSeparator() + currendDir + "> ").getBytes(StandardCharsets.UTF_8)));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        channel.write(ByteBuffer.wrap(("File "+ fileName +" created and message has been written").getBytes(StandardCharsets.UTF_8)));
                        channel.write(ByteBuffer.wrap((System.lineSeparator() + currendDir + "> " ).getBytes(StandardCharsets.UTF_8)));
                    } else {
                        channel.write(ByteBuffer.wrap(("File "+ fileName +" NOT created").getBytes(StandardCharsets.UTF_8)));
                        channel.write(ByteBuffer.wrap((System.lineSeparator() + currendDir + "> " ).getBytes(StandardCharsets.UTF_8)));
                    }
                    break;
                }
            }
            break;

            case "quit":
                channel.write(ByteBuffer.wrap((System.lineSeparator() + currendDir + "> Buy-Buy" + System.lineSeparator()).getBytes(StandardCharsets.UTF_8)));
                channel.close();
                break;
            default:
                channel.write(ByteBuffer.wrap(("Incorrect command").getBytes(StandardCharsets.UTF_8)));
                channel.write(ByteBuffer.wrap((System.lineSeparator() + currendDir + "> " ).getBytes(StandardCharsets.UTF_8)));
        }
    }

    private void handleAccept() throws IOException {
        System.out.println("Client accepted...");
        SocketChannel channel = serverSocketChannel.accept();
        channel.write(ByteBuffer.wrap((currendDir + "> " ).getBytes(StandardCharsets.UTF_8)));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
    }


    public static void main(String[] args) {
        new NioServer(1111);
    }
}
