package ru.bvkuchin.nioserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class NioServer {

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;

    private ByteBuffer buffer;

    public NioServer(int port)  {
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
        String response = "ECHO >> " + builder + System.lineSeparator();

        channel.write(ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8)));
    }

    private void handleAccept() throws IOException {
        System.out.println("Client accepted...");
        SocketChannel channel = serverSocketChannel.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
    }


    public static void main(String[] args) {
        new NioServer(1111);
    }
}
