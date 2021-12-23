package com.geekbrains.core.message;


import lombok.Data;

@Data
public class ServiceMessage extends AbstractMessage {
    String message;
    Exception e;


    public ServiceMessage() {
    }

    public ServiceMessage(String command, Exception e) {
        super(command);
        this.e = e;
    }

    public ServiceMessage(String command) {
        super(command);
    }

    public ServiceMessage(String command, String message) {
        super(command);
        this.message = message;
    }




    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


}
