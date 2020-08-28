import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    public ListView<String> listView;
    public TextField text;
    public Button send;
    private Socket socket;
    private static DataInputStream is;
    private static DataOutputStream os;
    private String clientPath = "client/";

    public static void stop() {
        try {
            os.writeUTF("quit");
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(ActionEvent actionEvent) {
        String message = text.getText();
        if (message.equals("quit")) {
            try {
                os.writeUTF("quit");
                os.flush();
                text.setText(is.readUTF());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String[] tokens = message.split(" ");
        String command = tokens[0];
        String fileName = tokens[1];
        if (command.equals("./upload")) {
            File file = new File("client/ClientStorage/" + fileName);
            try (FileInputStream fis = new FileInputStream(file)) {
                os.writeUTF(command);
                os.writeUTF(fileName);
                os.writeLong(file.length());
                byte[] buffer = new byte[256];
                int read = 0;
                while ((read = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
                String response = is.readUTF();
                if (response.equals("OK")) {
                    text.setText("File uploaded!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                os.writeUTF(command);
                os.writeUTF(fileName);
                String response = is.readUTF();
                if (response.equals("OK")) {
                    long fileLength = is.readLong();
                    File file = new File("client/ClientStorage/" + fileName);
                    file.createNewFile();
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        byte[] buffer = new byte[256];
                        if (fileLength < 256) {
                            fileLength += 256;
                        }
                        int read = 0;
                        for (int i = 0; i < fileLength / 256; i++) {
                            read = is.read(buffer);
                            fos.write(buffer, 0, read);
                        }
                        if (file.length() == fileLength) {
                            text.setText("File downloaded!");
                        }
                    }
                } else {
                    text.setText("file not found!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void initialize(URL location, ResourceBundle resources) {
        text.setOnAction(this::sendMessage);
        File dir = new File(clientPath);
        for (File file : dir.listFiles()) {
            listView.getItems().add(file.getName() + "        |       " + file.length() + " bytes");
        }
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
                        Platform.runLater(() -> listView.getItems().add(message));
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