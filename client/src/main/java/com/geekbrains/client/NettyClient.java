package com.geekbrains.client;

import com.geekbrains.client.handler.ClientInitializer;
import com.geekbrains.core.message.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Data;


import java.io.File;

@Data
public class NettyClient {

    private Channel channel;
    private Callback callback;
    private FileManager fileManager;
    private int port;
    Thread thread;

    public NettyClient(Callback callback, int port, FileManager fileManager) {

        EventLoopGroup workGroud = new NioEventLoopGroup();
        this.fileManager = fileManager;
        Bootstrap bootstrap = new Bootstrap();
        this.callback = callback;
        this.port = port;
        thread = new Thread(() -> {
            try {
                channel = bootstrap.group(workGroud)
                        .channel(NioSocketChannel.class)
                        .handler(new ClientInitializer(this::onReceive)).connect("localhost", this.port).sync().channel();

                channel.closeFuture().sync();

            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                workGroud.shutdownGracefully();
            }
        });
        thread.start();

    }

    public void onReceive(Message msg) {
        try {
            if (msg instanceof FileUploadFile) {
                Message message = fileManager.readFileUploadFileClient((FileUploadFile) msg);
                if (((AbstractMessage) message).getCommand() != TermitalCommand.SUCCESS) {
                    this.sendMessage(message);
                    return;
                } else {
                    callback.onReceive(message);
                }
            }
            callback.onReceive(msg);
        } catch (
                Exception e) {
            e.printStackTrace();
        }
    }


    public void sendMessage(Message msg) {
        if (msg instanceof Message) {
            channel.writeAndFlush(msg);
        }

    }

    public void copyFileToServer(String fileName) {
        FileUploadFile fileUploadFile =
                new FileUploadFile("copyToServer", new File(fileManager
                        .getPath()
                        .resolve(fileName).toString()));
        fileUploadFile.setName(fileName);
        fileUploadFile.updateByte(fileManager.getSizeRead(),fileManager.getPath());
        sendMessage(fileUploadFile);
    }

    public void copyFileFromServer(String fileName) {
        FileUploadFile fileUploadFile =
                new FileUploadFile("copyFromServer", new File(fileManager
                        .getPath()
                        .resolve(fileName).toString()));
        fileUploadFile.setName(fileName);
        sendMessage(fileUploadFile);
    }

    public void stop() {
        thread.stop();
    }





}
