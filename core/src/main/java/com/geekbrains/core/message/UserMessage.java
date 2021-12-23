package com.geekbrains.core.message;

import lombok.Data;

@Data
public class UserMessage extends AbstractMessage {
    String login;
    int hashPass;
    boolean isCreate;

    public UserMessage(String login, String pass, boolean isCreate) {
        this.login = login;
        this.hashPass = pass.hashCode();
        this.isCreate = isCreate;
    }
}
