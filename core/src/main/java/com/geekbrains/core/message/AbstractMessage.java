package com.geekbrains.core.message;

import lombok.Data;

@Data
public class AbstractMessage implements Message {
    TermitalCommand command;
    private String userName;
    public AbstractMessage() {
    }

    public AbstractMessage(String command) {

        this.command = TermitalCommand.byCommand(command);
    }

    public TermitalCommand getCommand() {
        return command;
    }


}
