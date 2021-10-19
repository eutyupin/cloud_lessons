package ru.gb.io;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Server {
    private ConcurrentLinkedDeque<ChatHandler> clients;

    public Server() {
        clients = new ConcurrentLinkedDeque<>();
        try (ServerSocket server = new ServerSocket(9999)) {
            System.out.println("Server started...");
            while (true) {
                Socket socket = server.accept();
                System.out.println("Client accepted");
                ChatHandler handler = new ChatHandler(socket, this);
                clients.add(handler);
                new  Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadCastMessage(String message) throws IOException {
        System.out.println(message);
        for (ChatHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public static void main(String[] args) {
        new Server();
    }

}
