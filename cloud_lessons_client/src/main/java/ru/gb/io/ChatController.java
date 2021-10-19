package ru.gb.io;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class ChatController implements Initializable {

    @FXML
    public TreeView filesTree;
    @FXML
    private TextField messageField;

    private Path userDirectory;
    private File fileList[];
    private File fileToSend;
    private byte[] byteArray = new byte[8192];
    private DataOutputStream outputStream;
    private Socket socket;
    private BufferedOutputStream buf;
    private TreeItem<String> rootUserItem;
    private BufferedInputStream fileInput;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            socket = new Socket("localhost", 9999);
            outputStream = new DataOutputStream(socket.getOutputStream());
            buf = new BufferedOutputStream(socket.getOutputStream());
            createTree();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createTree() {
        userDirectory = Paths.get(".").toAbsolutePath().normalize();
        fileList = userDirectory.toFile().listFiles();
        rootUserItem = new TreeItem<>(userDirectory.getFileName().toString());
        filesTree.setShowRoot(false);
        fillFilesTree(userDirectory.toFile(), rootUserItem);
        filesTree.setRoot(rootUserItem);
    }

    private void fillFilesTree(File target, TreeItem<String> item) {
        ImageView folderIcon = new ImageView(new Image(getClass().getResourceAsStream("folder.png")));
        ImageView fileIcon = new ImageView(new Image(getClass().getResourceAsStream("file.png")));
        if (target.isDirectory()) {
            TreeItem<String> treeItem = new TreeItem<>(target.getName());
            treeItem.setGraphic(folderIcon);
            item.getChildren().add(treeItem);
            for (File file : target.listFiles()) {
                fillFilesTree(file,treeItem);
            }
        } else {
            TreeItem<String> treeItem = new TreeItem<>(target.getName());
            treeItem.setGraphic(fileIcon);
            item.getChildren().add(treeItem);
        }
    }

    @FXML
    private void sendFile(ActionEvent actionEvent) {
        String fileName = messageField.getText();
        fileToSend = new File(fileName);
        if (!fileToSend.isDirectory() && fileToSend.exists()) {
            try {
                fileInput = new BufferedInputStream(new FileInputStream(fileToSend));
                outputStream.writeUTF(fileToSend.getName());
                outputStream.writeLong(fileToSend.length());
                outputStream.flush();
                int i;
                while ((i = fileInput.read(byteArray)) != -1) {
                   buf.write(byteArray,0, i);
                }
                fileInput.close();
                buf.flush();
                buf.close();

                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void mouseClickedTreeView(MouseEvent mouseEvent) {
        messageField.setText(getTreeItem(filesTree));
    }

    private String getTreeItem(TreeView tree) {
        String tempPathToFile = "";
        String value = "";
        MultipleSelectionModel<TreeItem<String>> items = tree.getSelectionModel();
        TreeItem<String> treeItemParent = items.getSelectedItem().getParent();
        while(treeItemParent != null){
            value = treeItemParent.getValue();
            if (value.equals(userDirectory.getFileName().toString())) value = "";
            else value += "\\";
            tempPathToFile =  value + tempPathToFile;
            treeItemParent = treeItemParent.getParent();
        }
        if (!items.getSelectedItem().getValue().equals(userDirectory.getFileName().toString())){
            tempPathToFile = userDirectory.toAbsolutePath().normalize() + "\\" +
                    tempPathToFile + items.getSelectedItem().getValue();
        }
        return tempPathToFile;
    }
}
