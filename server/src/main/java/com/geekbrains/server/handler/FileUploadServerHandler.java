package com.geekbrains.server.handler;


import com.geekbrains.core.message.*;
import com.geekbrains.server.db.AuthService;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@ChannelHandler.Sharable
public class FileUploadServerHandler extends ChannelInboundHandlerAdapter {
    private FileManager fileManager;
    private String userName;
    private boolean isAuthorized = false;
    private AuthService authService;


    public FileUploadServerHandler(AuthService authService) {
        this.fileManager = new FileManager(10240, "server");
        fileManager.setServer(true);
        this.authService = authService;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.info("channelActive() server");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("Файл  обрабатывается server");
        try {
            if (!isAuthorized) {
                Message message = checkAuthorized(msg);
                ctx.writeAndFlush(message);
                return;
            }
            if (msg instanceof AbstractMessage) {
                ((AbstractMessage) msg).setUserName(userName);
            }
            if (msg instanceof FileUploadFile) {
                FileUploadFile fileUploadFile = (FileUploadFile) msg;
                Message message = fileManager.readFileUploadFileServer(fileUploadFile);
                ctx.writeAndFlush(message);

            } else if (msg instanceof ServiceMessage) {
                Message callbackMessage = fileManager.readServiceMessage((ServiceMessage) msg);
                if (callbackMessage instanceof ListFilesMessage) {
                    ((ListFilesMessage) callbackMessage).setTypeData(ListFilesMessage.SERVER_DATA);
                }
                ctx.writeAndFlush(callbackMessage);
            }

        } catch (Exception e) {
            log.error("Error readServiceMessage", e);
            ctx.writeAndFlush(e);
        }
    }

    private Message checkAuthorized(Object msg) throws Exception {
        if (msg instanceof UserMessage) {
            UserMessage userMessage = (UserMessage) msg;
            String login = userMessage.getLogin();
            int hashPass = userMessage.getHashPass();
            Path path = fileManager.getPath();

            if (login.equals("") || hashPass == 0) {
                throw new RuntimeException("Не заполнены параметры");
            }

            if (userMessage.isCreate()) {

                checkCreateDirectoryByLogin(login);
                authService.addUser(login, hashPass);
                fileManager.setPath(path.resolve(login));
                Files.createDirectory(path.resolve(login));
                isAuthorized = true;
                return new ServiceMessage("success", "auth");

            }
            if (authService.isCorrectAuthorization(
                    login,
                    hashPass)) {

                this.userName = userMessage.getLogin();
                fileManager.setPath(path.resolve(login));
                if (!fileManager.getPath().toFile().exists()) {
                    Files.createDirectory(fileManager.getPath());
                }
                isAuthorized = true;
                return new ServiceMessage("success", "auth");

            }
        }
        throw new RuntimeException("Пользователь не авторизовался");
    }

    private void checkCreateDirectoryByLogin(String login) throws IOException {
        Path path = fileManager.getPath();

        if (Files.notExists(path.resolve(login))) {
            Files.createDirectory(path.resolve(login));
            Files.deleteIfExists(path.resolve(login));
        } else {
            throw new RuntimeException("Нельзя создать пользователя");
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }


}
