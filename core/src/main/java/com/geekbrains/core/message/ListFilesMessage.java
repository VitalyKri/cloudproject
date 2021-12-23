package com.geekbrains.core.message;

import lombok.Data;

import java.util.List;

@Data
public class ListFilesMessage extends AbstractMessage {

    List<FileUploadFile> fileUploadFiles;
    int TypeData;
    public static final int SERVER_DATA = 0;
    public static final int CLIENT_DATA = 1;

    public ListFilesMessage(String command, List<FileUploadFile> fileUploadFiles) {
        super(command);
        this.fileUploadFiles = fileUploadFiles;
    }
}
