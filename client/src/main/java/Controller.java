import javafx.fxml.Initializable;
import javafx.scene.control.TreeView;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private final String clientPath = "client/ClientStorage";
    private final String serverPath = "server/ServerStorage";
    private Socket socket;
    private static DataInputStream is;
    private static DataOutputStream os;

    public TreeView<String> foldersTreeView;
    public TreeView<String> serverFoldersTreeView;

    public static void stop() {
        try {
            os.writeUTF("quit");
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Обработчик нажатия кнопки upload на клиенте.
     * По имени файла выстраивает путь к нему из каталога ClientStorage
     */
    public void uploadFile() {
        String filePath = clientPath + Utils.getPathFromTree(
                foldersTreeView
                        .getSelectionModel()
                        .getSelectedItem()
        );
        System.out.println(filePath);
    }

    public void downloadFile() {
        String fileName = serverFoldersTreeView
                .getSelectionModel()
                .getSelectedItem()
                .getValue();
        System.out.println(fileName);
    }

    public void initialize(URL location, ResourceBundle resources) {
        foldersTreeView.setRoot(Utils.getNodesForDirectory(new File(clientPath)));
        serverFoldersTreeView.setRoot(Utils.getNodesForDirectory(new File(serverPath)));

        try {
            socket = new Socket("localhost", 8189);
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                while (true) {
                    try {
                        String message = is.readUTF();
                        if (message.equals("quit")) {
                            break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}