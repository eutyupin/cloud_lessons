package ru.gb.nio;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class NioServer {

    private ServerSocketChannel server;
    private Selector selector;
    private ByteBuffer buffer;
    private Path currentPath = Paths.get("./").toAbsolutePath().getParent();

    public NioServer() throws Exception {
        buffer = ByteBuffer.allocate(256);
        server = ServerSocketChannel.open();
        server.bind(new InetSocketAddress(8189));
        selector = Selector.open();
        server.configureBlocking(false);
        server.register(selector, SelectionKey.OP_ACCEPT);
        while (server.isOpen()) {
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectedKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isAcceptable()){
                    handleAccept(key);
                }
                if (key.isReadable()) {
                    handleRead(key);
                }
                iterator.remove();
            }
        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        StringBuilder sb = new StringBuilder();
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
                sb.append((char) buffer.get());
            }
            buffer.clear();
        }
        channel.write(ByteBuffer.wrap(checkEnteredCommand(sb).getBytes(StandardCharsets.UTF_8)));
        String currentPathString = currentPath.toString() + " :> ";
        channel.write(ByteBuffer.wrap(currentPathString.getBytes(StandardCharsets.UTF_8)));
    }

    private String checkEnteredCommand(StringBuilder command) {
        boolean startWithCD = command.toString().toLowerCase().startsWith("cd..");

        if (command.toString().trim().equals("ls")) {
            return checkLSCommand();
        }
        if (command.toString().toLowerCase().startsWith("cd") && !startWithCD) {
            return checkCDCommand(command);
        }
        if (startWithCD) {
            checkCDDotDotCommand();
            return "";
        }
        if (command.toString().toLowerCase().startsWith("touch")) {
            return chekTouchCommand(command);
        }
        if(command.toString().toLowerCase().startsWith("cat")) {
            return checkCATCommand(command);
        }
        return command.toString();
    }

    private String checkLSCommand() {
        StringBuilder folderContainsToSB = new StringBuilder();
        for (File file : currentPath.toFile().listFiles()) {
            if (file.isDirectory()) {
                folderContainsToSB.append("Folder: ").append(file.getName()).append(System.lineSeparator());
            } else {
                folderContainsToSB.append("File:   ").append(file.getName()).append(System.lineSeparator());
            }
        }
        return folderContainsToSB.toString();
    }

    private String checkCDCommand(StringBuilder command) {
        if (command.substring(3).trim().equals("")) return System.lineSeparator() +
                "No directory name entered!" + System.lineSeparator();
        Path tempPath;
        try {
            tempPath = currentPath.resolve(command.substring(3).trim());
        } catch (InvalidPathException e) {
            return "Wrong directory name!" + System.lineSeparator();
        }
        if (tempPath.toFile().isDirectory()) {
            currentPath = currentPath.resolve(command.substring(3).trim());
        } else return System.lineSeparator() + "Wrong directory name!" + System.lineSeparator();
        return "";
    }

    private void checkCDDotDotCommand() {
        currentPath = currentPath.getParent();
    }

    private String chekTouchCommand(StringBuilder command) {
        if (command.substring(6).trim().length() > 0) {
            Path newFile = currentPath.resolve(command.substring(6).trim());
            if (!Files.exists(newFile)) {
                try {
                    Files.createFile(newFile);
                } catch (IOException e) {
                    return System.lineSeparator() + "Error file creating! Try again!" + System.lineSeparator();
                }
                return System.lineSeparator() + "File " + command.substring(6).trim() +
                        " was created" + System.lineSeparator();
            }
        } else return System.lineSeparator() + "Wrong file name. Try again. " + System.lineSeparator();

        return "";
    }

    private String checkCATCommand(StringBuilder command) {
        String fileName = command.toString().substring(4).trim();
        Path filePath = Paths.get(currentPath.toString(), fileName);
        if (Files.exists(filePath)) {
            try {
                StringBuilder text = new StringBuilder();
                List<String> textList = Files.readAllLines(filePath, StandardCharsets.UTF_8);
                for (String line : textList) {
                    text.append(line);
                    text.append(System.lineSeparator());
                }
                return text.toString();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return "File is empty" + System.lineSeparator();
    }

    private void handleAccept(SelectionKey key) throws Exception{
        SocketChannel channel = server.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ, "Server started... ");
    }

    public static void main(String[] args) throws Exception {
        new NioServer();
    }


}
