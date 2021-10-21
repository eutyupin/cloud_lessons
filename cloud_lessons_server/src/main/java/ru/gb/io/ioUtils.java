package ru.gb.io;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;

public class ioUtils {
    public static void main(String[] args) throws URISyntaxException, IOException {
        String resource = ioUtils.class.getResource("1.txt").getFile();
        String copy = ioUtils.class.getResource("dir1").getFile();

        InputStream is = new FileInputStream(resource);
        OutputStream os = new FileOutputStream(copy + "/copy.txt");

        byte[] buffer = new byte[8192];
        int readBytes = 0;
        while (true) {
            readBytes = is.read(buffer);
            if (readBytes != -1) break;
            System.out.println(new String(buffer, 0, readBytes));
        }
    }
}
