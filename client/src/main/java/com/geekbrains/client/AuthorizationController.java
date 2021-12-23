package com.geekbrains.client;

import com.geekbrains.core.message.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;

import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AuthorizationController implements Initializable {
    public Stage stage;
    private Scene scene;
    private FileManager fileManager;
    private NettyClient net;

    public TextField loginField;

    public TextField passField;
    ActionEvent lastEvent;

    public void authorization(ActionEvent event) throws Exception {
        lastEvent = event;

        UserMessage userMessage = new UserMessage(loginField.getText(), passField.getText(), false);
        net.sendMessage(userMessage);
    }

    public void registration(ActionEvent event) throws Exception {
        lastEvent = event;
        UserMessage userMessage = new UserMessage(loginField.getText(), passField.getText(), true);
        net.sendMessage(userMessage);
    }

    public void switchToSceneStorage() throws Exception {

        Platform.runLater(() -> {
            FXMLLoader root = new FXMLLoader(getClass().getResource("storage.fxml"));

            stage = (Stage) ((Node) lastEvent.getSource()).getScene().getWindow();
            try {
                scene = new Scene(root.load());
            } catch (IOException e) {
                e.printStackTrace();
            }
            stage.setScene(scene);
            StorageController storageController = root.getController();
            storageController.initializeScene(this.net);
            stage.show();
        });

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void initializeScene(NettyClient net) {
        this.net = net;
        fileManager = this.net.getFileManager();
        this.net.setCallback(this::onReceive);
    }

    private void showDialogWindows(String message) {
        JFrame jFrame = new JFrame();
        JOptionPane.showMessageDialog(jFrame, message);
    }

    private void onReceive(Message msg) {

        try {
            if (msg instanceof ServiceMessage) {
                ServiceMessage message = (ServiceMessage) msg;
                if (message.getCommand() == TermitalCommand.ERROR) {
                    showDialogWindows(message.getMessage());
                } else if (message.getCommand() == TermitalCommand.SUCCESS) {
                    switchToSceneStorage();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
