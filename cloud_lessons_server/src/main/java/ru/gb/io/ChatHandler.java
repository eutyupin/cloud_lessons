package ru.gb.io;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ChatHandler implements Runnable{
    private static int cnt = 0;
    private byte[] byteArray = new byte[8192];
    private final String userName;
    private final Socket socket;
    private final Server server;
    private DataInput inputStream;
    private File file;
    private FileOutputStream fileOutputStream;
    private DataOutputStream outputStream;
    private BufferedInputStream inputFile;
    private final SimpleDateFormat format;
    private String fileName;
    private Long fileSize;

    public ChatHandler(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        cnt++;
        userName = "User#" + cnt;
        new File("./" + userName).mkdir();
        format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
        inputFile = new BufferedInputStream(socket.getInputStream());
        file = new File("./" + userName + "/temp_file");
        file.createNewFile();
        fileOutputStream = new FileOutputStream(file);
    }

    @Override
    public void run() {
        int i;

        try {
            while (true) {
                fileName = inputStream.readUTF();
                fileSize = inputStream.readLong();
                while((i = inputFile.read(byteArray)) != -1) {
                    fileOutputStream.write(byteArray,0, i);
                }
                fileOutputStream.close();
//                server.broadCastMessage(getFormatMessage(message));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getTime() {
        return format.format(new Date());
    }

}
