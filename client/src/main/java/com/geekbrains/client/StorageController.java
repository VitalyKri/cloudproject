package com.geekbrains.client;

import com.geekbrains.core.message.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import javax.swing.*;
import java.net.URL;
import java.util.ResourceBundle;


public class StorageController implements Initializable {
    public TableView<FileUploadFile> tableViewClient;
    public TableView<FileUploadFile> tableViewServer;

    public TableColumn<FileUploadFile, String> typeId1;
    public TableColumn<FileUploadFile, String> typeId2;
    public TableColumn<FileUploadFile, String> nameColumn1;
    public TableColumn<FileUploadFile, String> nameColumn2;
    public TableColumn<FileUploadFile, String> sizeColumn1;
    public TableColumn<FileUploadFile, String> sizeColumn2;

    public ListView<String> statuses;
    public TextField console;
    private NettyClient net;
    private FileManager fileManager;

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        net.stop();
    }

    public void initializeScene(NettyClient net) {

        this.net = net;
        net.setCallback(this::onReceive);
        undateNettyClient();
        undateNettyServer();
        try {
            updateInfoDirectoryClient("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        typeId1.setCellValueFactory(param -> new SimpleStringProperty(
                (param.getValue().getType() == 0 ? "dir" : "file")
        ));
        typeId2.setCellValueFactory(param -> new SimpleStringProperty(
                (param.getValue().getType() == 0 ? "dir" : "file")
        ));
        nameColumn1.setCellValueFactory(param -> new SimpleStringProperty(
                (param.getValue().getName())
        ));
        nameColumn2.setCellValueFactory(param -> new SimpleStringProperty(
                (param.getValue().getName())
        ));
        sizeColumn1.setCellValueFactory(param -> new SimpleStringProperty(
                ((String.valueOf(param.getValue().getSize()/1024) + " Kb."))
        ));
        sizeColumn2.setCellValueFactory(param -> new SimpleStringProperty(
                ((String.valueOf(param.getValue().getSize()/1024) + " Kb."))
        ));


    }

    private void undateNettyClient() {
        if (net == null) {
            fileManager = new FileManager(10240);
            net = new NettyClient(this::onReceive, 8189, fileManager);
        } else {
            if (net.getFileManager() == null) {
                fileManager = new FileManager(10240);
                net.setFileManager(fileManager);
            } else {
                fileManager = net.getFileManager();
            }
        }
    }

    private void undateNettyServer() {
        net.sendMessage(new ServiceMessage("cd", ""));
    }

    private void addStatus(String msg) {

        Platform.runLater(() -> {
            statuses.getItems().clear();
            statuses.getItems().add(msg);
        });
    }


    public void clickTable(MouseEvent event) throws Exception {

        if (event.getClickCount() > 1) {
            TableView<FileUploadFile> source = (TableView<FileUploadFile>) event.getSource();
            FileUploadFile selectedItem = source.getSelectionModel().getSelectedItem();
            if (source == tableViewClient) {
                useCdCommandClient(selectedItem);
            } else {
                useCdCommandServer(selectedItem);
            }
        }

    }

    private void useCdCommandClient(FileUploadFile fileUploadFile) throws Exception {

        if (fileUploadFile.getType() == FileUploadFile.DIRECTORY) {
            updateInfoDirectoryClient(fileUploadFile.getName());
        }
    }

    private void updateInfoDirectoryClient(String msg) throws Exception {
        ServiceMessage serviceMessage = new ServiceMessage();
        serviceMessage.setMessage(msg);

        ListFilesMessage listFilesMessage = (ListFilesMessage) fileManager.processCd(serviceMessage);
        listFilesMessage.setTypeData(ListFilesMessage.CLIENT_DATA);

        addInfo(listFilesMessage, tableViewClient);
    }

    private void addInfo(ListFilesMessage listFilesMessage, TableView<FileUploadFile> tableView) {

        ObservableList<FileUploadFile> fileUploadFiles = FXCollections.observableList(listFilesMessage.getFileUploadFiles());

        Platform.runLater(() -> {
            statuses.getItems().clear();
            tableView.setItems(fileUploadFiles);
        });

    }

    private void useCdCommandServer(FileUploadFile fileUploadFile) throws Exception {

        if (fileUploadFile.getType() == FileUploadFile.DIRECTORY) {
            net.sendMessage(new ServiceMessage("cd", fileUploadFile.getName()));
        }

    }
    private void useLsCommandServer() throws Exception {

            net.sendMessage(new ServiceMessage("ls"));

    }


    private void updateFilesList() throws Exception{
        useLsCommandServer();
        updateInfoDirectoryClient("");
    }

    public void copyFileFromServer(ActionEvent event) {

        FileUploadFile selectedItem = tableViewServer.getSelectionModel().getSelectedItem();
        if (selectedItem == null || selectedItem.getName().equals("")) {
            showMessageDialog("Нет данных");
            return;
        }
        net.copyFileFromServer(selectedItem.getName());

    }

    public void copyFileToServer(ActionEvent event) {
        FileUploadFile selectedItem = tableViewClient.getSelectionModel().getSelectedItem();
        if (selectedItem == null || selectedItem.getName().equals("")) {
            showMessageDialog("Нет данных");
            return;
        }
        net.copyFileToServer(selectedItem.getName());
    }

    public void createDirectory(ActionEvent event) {
        if (console.getText().equals("")) {
            showMessageDialog("Нет заполнено имя папки. Укажите его в консоле.");
            return;
        }
        net.sendMessage(new ServiceMessage("mkdir", console.getText()));
    }

    public void createFile(ActionEvent event) {
        if (console.getText().equals("")) {
            showMessageDialog("Нет заполнено имя файла. Укажите его в консоле.");
            return;
        }
        net.sendMessage(new ServiceMessage("touch", console.getText()));
    }

    public void readFile(ActionEvent event) {
        FileUploadFile selectedItem = tableViewServer.getSelectionModel().getSelectedItem();
        if (selectedItem == null || selectedItem.getType() == FileUploadFile.DIRECTORY) {
            showMessageDialog("Не выбран файл.");
            return;
        }
        net.sendMessage(new ServiceMessage("cat", selectedItem.getName()));
    }

    public void delete(ActionEvent event) {
        FileUploadFile selectedItem = tableViewServer.getSelectionModel().getSelectedItem();
        if (selectedItem == null || selectedItem.getType() == FileUploadFile.DIRECTORY) {
            showMessageDialog("Не выбран файл.");
            return;
        }
        net.sendMessage(new ServiceMessage("delete", selectedItem.getName()));
    }

    public void showMessageDialog(String message) {
        JFrame jFrame = new JFrame();
        JOptionPane.showMessageDialog(jFrame, message);
    }

    private void onReceive(Message msg) {
        try {
            if (msg instanceof FileUploadFile) {
                FileUploadFile message = (FileUploadFile) msg;
                long percent;
                if (message.getSize() ==0 && (percent = (message.getStart() * 100 / message.getSize())) % 5 == 0) {
                    addStatus("File load: " + (percent) + "%.");
                }
                net.onReceive(message);
            } else if (msg instanceof ServiceMessage) {
                ServiceMessage message = (ServiceMessage) msg;
                if (message.getCommand()== TermitalCommand.SUCCESS){
                    updateFilesList();
                }
                addStatus(message.getCommand().toString() + " " + message.getMessage());
            } else if (msg instanceof ListFilesMessage) {
                ListFilesMessage message = (ListFilesMessage) msg;
                addInfo(message, tableViewServer);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
