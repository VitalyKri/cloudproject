package com.geekbrains.core.message;


import lombok.Data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;

@Data
public class FileUploadFile extends AbstractMessage {

    byte[] bytes;
    long start;
    boolean isLast;
    File file;

    long size;
    String name;

    private int type;

    public FileUploadFile() {
    }

    public FileUploadFile(File file) {
        if (file==null){
            this.size = 0;
            this.type = FileUploadFile.DIRECTORY;
            this.name = "//";
        } else {
            this.size = file.length();
            this.type = file.isDirectory() ? FileUploadFile.DIRECTORY : FileUploadFile.FILE;;
            this.name = file.getName();
        }

    }

    public final static int DIRECTORY = 0;
    public final static int FILE = 1;

    public FileUploadFile(String command, File file) {
        super(command);
        this.start = 0;
        this.file = file;
        this.size = this.file.length();
    }

    public void setLast(){
        isLast = start==size;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public boolean updateByte(int size, Path path) {
        if (size != 0) {
            try {
                RandomAccessFile raf = new RandomAccessFile(path.resolve(name).toFile(), "r");
                raf.seek(start);
                int a = (int) (raf.length() - start);
                if (a < size) {
                    size = a;
                }
                bytes = new byte[size];
                raf.read(bytes);
                raf.close();
                isLast = size ==0 ;
                return size > 0;

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void addToStart(int sizeByte) {
        this.start += sizeByte;
    }
}

