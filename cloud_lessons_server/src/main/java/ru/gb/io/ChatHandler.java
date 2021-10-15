package ru.gb.io;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

public class ChatHandler implements Runnable{
    private static int cnt = 0;
    private final String userName;
    private final Socket socket;
    private final Server server;
    private DataInput dis;
    private DataOutputStream dos;
    private final SimpleDateFormat format;

    public ChatHandler(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        cnt++;
        userName = "User #" + cnt;
        format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        try {
            while (true) {
                String message = dis.readUTF();
                server.broadCastMessage(getFormatMessage(message));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getFormatMessage(String message) {
        return getTime() + " [" + userName + "]: " + System.lineSeparator() + message;
    }

    private String getTime() {
        return format.format(new Date());
    }

    public void sendMessage(String message) throws IOException {
        dos.writeUTF(message);
        dos.flush();

    }
}
