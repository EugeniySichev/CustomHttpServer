package com.example;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Main {

    public static void main(String[] args) {
	new Server().bootstrap();
    }
}

class Server{
    private final static int BUFFER_SIZE = 256;
    private AsynchronousServerSocketChannel server;

    private final static String HEADERS =
            "HTTP/1.1 200 OK\n" +
            "Server: Test\n" +
                "Content-Type: text/html\n" +
                "Content-Length: %s\n" +
                "Connection: close\n\n";

    public void bootstrap(){
        try {
            server = AsynchronousServerSocketChannel.open();
            server.bind(new InetSocketAddress("127.0.0.1", 8080));

            Future<AsynchronousSocketChannel> future = server.accept();
            System.out.println("new client thread");

            AsynchronousSocketChannel clientChannel = future.get(30, TimeUnit.SECONDS);

            while (clientChannel != null && clientChannel.isOpen()){
                ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                StringBuilder builder = new StringBuilder();
                boolean keepReading = true;

                while(keepReading) {
                    clientChannel.read(buffer).get();

                    int position = buffer.position();
                    keepReading = position == BUFFER_SIZE;

                    byte[] array = keepReading
                            ? buffer.array()
                            : Arrays.copyOfRange(buffer.array(), 0, position);

                    builder.append(new String(buffer.array()));
                    buffer.clear();
                }

                String body = "<html><h1>Test test test</h1><html>";
                String page = String.format(HEADERS, body.length()) + body;
                ByteBuffer resp = ByteBuffer.wrap(page.getBytes());
                clientChannel.write(resp);

                clientChannel.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
}

