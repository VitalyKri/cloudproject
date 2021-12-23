package com.geekbrains.core.message;

import java.util.Arrays;

public enum TermitalCommand {

    CD("cd"),
    MKDIR("mkdir"),
    LS("ls"),
    TOUCH("touch"),
    CAT("cat"),
    COPY_TO_SERVER("copytoserver"),
    COPY_FROM_SERVER("copyfromserver"),
    DELETE("delete"),
    SUCCESS("success"),
    ERROR("error"),
    USER("user");
    String command;
    TermitalCommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public static TermitalCommand byCommand(String command){
        return Arrays.asList(values())
                .stream().filter(cmd -> cmd.getCommand().equals(command.toLowerCase()))
                .findAny()
                .orElseThrow(() -> new RuntimeException("command not found"));
    }


}
