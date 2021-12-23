package com.geekbrains.core.message;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Data
@Slf4j
public class FileManager {
    RandomAccessFile raf;
    int sizeRead;
    Path path;
    boolean isServer;

    public FileManager(int sizeRead) {
        path = new File("").toPath();
        this.sizeRead = sizeRead;
    }

    public FileManager(int sizeRead, String pathString) {
        path = new File(pathString).toPath();
        this.sizeRead = sizeRead;
    }


    public Message readFileUploadFileServer(FileUploadFile uploadFile) throws Exception {

        Message message = null;
        if (uploadFile.getCommand() == TermitalCommand.COPY_TO_SERVER) {
            log.info("Файл " + uploadFile.getName() + " передается на server");
            message = fileWrite(uploadFile);
            log.info("Обработано, путь к файлу:" + uploadFile.getName());
        } else if (uploadFile.getCommand() == TermitalCommand.COPY_FROM_SERVER) {
            log.info("Файл " + uploadFile.getName() + " передается c server");
            fileRead(uploadFile);
            message = uploadFile;
        }
        return message;
    }

    public Message readFileUploadFileClient(FileUploadFile uploadFile) throws Exception {

        Message message = null;
        if (uploadFile.getCommand() == TermitalCommand.COPY_FROM_SERVER) {
            log.info("Файл " + uploadFile.getName() + " передается на клиент");
            message = fileWrite(uploadFile);
            log.info("Обработано, путь к файлу:" + uploadFile.getName());
        } else if (uploadFile.getCommand() == TermitalCommand.COPY_TO_SERVER) {
            log.info("Файл " + uploadFile.getName() + " передается c клиента");
            fileRead(uploadFile);
            message = uploadFile;
        }
        return message;
    }

    // TODO нужно сделать загрузку с сервера и обновление статусов
    public Message fileWrite(FileUploadFile uploadFile) throws Exception {
        Message message = null;
        long start = uploadFile.getStart();
        byte[] bytes = uploadFile.getBytes();
        Path pathFile = path.resolve(uploadFile.getName());
        int sizeByte;

        File file = new File(pathFile.toAbsolutePath().toString());
        raf = new RandomAccessFile(file, "rw");
        raf.seek(start);
        raf.write(bytes);
        raf.close();

        if ((sizeByte = uploadFile.getBytes().length) > 0) {
            uploadFile.addToStart(sizeByte);
            uploadFile.setBytes(null);
        }

        if (!uploadFile.isLast()) {
            message = uploadFile;
        } else {
            message = new ServiceMessage("success","Загрузка файла ("+uploadFile.getName()+") успешна." );
        }
        return message;
    }

    public Message fileRead(FileUploadFile uploadFile) throws Exception {
        Message message = null;
        long start = uploadFile.getStart();
        if (start != -1) {
            uploadFile.updateByte(sizeRead,this.path);
            message = uploadFile;
        } else {
            message = new ServiceMessage("Error", "fileRead");
        }
        return message;
    }

    public Message readServiceMessage(ServiceMessage msg) throws Exception {
        TermitalCommand command = msg.getCommand();
        log.info("Выполняется команда:" + command.toString());
        Message message = null;

        if (command == TermitalCommand.MKDIR) {
            message = processMkDir(msg);
        } else if (command == TermitalCommand.LS) {
            message = processLs();
        } else if (command == TermitalCommand.TOUCH) {
            message = processTouch(msg);
        } else if (command == TermitalCommand.CAT) {
            message = processCat(msg);
        } else if (command == TermitalCommand.CD) {
            message = processCd(msg);
        } else if (command == TermitalCommand.CAT) {
            message = processCat(msg);
        } else if (command == TermitalCommand.DELETE) {
            message = processDelete(msg);
        }
        return message;

    }

    public Message processDelete(ServiceMessage msg) throws Exception {

        String msgMessage = msg.getMessage();
        if (Files.isWritable(path.resolve(msgMessage))) {
            Files.delete(path.resolve(msgMessage));
        } else {
            throw new RuntimeException("Нельзя удалить файл");
        }
        return processLs();
    }

    public Message processCd(ServiceMessage msg) throws Exception {
        String message = msg.getMessage();
        if (msg.getMessage().equals("//")) {
            Path parent = path.getParent();
            String userName = msg.getUserName();
            if (impossibleUpDirectoryServer()) {
                throw new RuntimeException("нет прав");
            }
            path = parent!=null?parent:  new File("").toPath();

        } else if (Files.isDirectory(path.resolve(message))) {

            if (msg.getMessage().contains(":")) {
                throw new RuntimeException("Нельзя выполнять команду");
            }
            path = path.resolve(message);

        } else {
            throw new RuntimeException("Это не папка");
        }
        return processLs();
    }

    public Message processCat(ServiceMessage msg) throws Exception {
        Path newPath = path.resolve(msg.getMessage());
        if (Files.isReadable(path.resolve(msg.getMessage()))) {

            BufferedReader bufferedReader = Files.newBufferedReader(newPath);
            StringBuilder stringBuilder = new StringBuilder("processCat ");
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            return new ServiceMessage("success", stringBuilder.toString());
        } else {
            throw new RuntimeException("Ошибка чтения. Нельзя прочесть файл " + newPath.toString());
        }
    }

    public Message processLs() throws Exception {

        List<FileUploadFile> fileUploadFiles = new ArrayList<>();
        File[] fileDir = new File(path.toAbsolutePath().toString()).listFiles();

        if (!impossibleUpDirectoryServer()) {
            fileUploadFiles.add(new FileUploadFile(null));
        }
        for (File file :
                fileDir) {
            FileUploadFile fileUploadFile = new FileUploadFile(file);
            fileUploadFiles.add(fileUploadFile);
        }
        return new ListFilesMessage("success", fileUploadFiles);
    }

    private boolean impossibleUpDirectoryServer() {
        Path parent = path.getParent();
        return parent != null && parent.toString().equals("server") && isServer;

    }

    public Message processMkDir(ServiceMessage msg) throws Exception {

        String param = msg.getMessage();
        if (param.length() == 0) {
            throw new RuntimeException("Нет параметра");
        }

        try {
            Files.createDirectory(path.resolve(param));
        } catch (RuntimeException | IOException e) {
            throw new RuntimeException(e);
        }
        return processLs();

    }

    public Message processTouch(ServiceMessage msg) throws Exception {

        String param = msg.getMessage();
        if (param.length() == 0) {
            throw new RuntimeException("Нет параметра");
        }

        try {
            Files.createFile(path.resolve(param));
        } catch (RuntimeException | IOException e) {
            throw new RuntimeException("Не найден параметр");
        }
        return processLs();
    }


}
