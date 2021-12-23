package com.geekbrains.client;

import com.geekbrains.core.message.FileManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class StorageClient extends Application {

    private FileManager fileManager;
    private NettyClient net;

    @Override
    public void stop() throws Exception {
        super.stop();
        net.stop();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        fileManager = new FileManager(10240);
        net = new NettyClient(null, 8189, fileManager);
        FXMLLoader root = new FXMLLoader(getClass().getResource("authorization.fxml"));
        Scene scene = new Scene(root.load());
        primaryStage.setScene(scene);
        AuthorizationController authorizationController = root.getController();
        authorizationController.initializeScene(this.net);
        primaryStage.show();

    }


}