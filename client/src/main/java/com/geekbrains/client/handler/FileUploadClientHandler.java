package com.geekbrains.client.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import com.geekbrains.core.message.Callback;
import com.geekbrains.core.message.Message;
import com.geekbrains.core.message.ServiceMessage;

@Slf4j
@ChannelHandler.Sharable
public class FileUploadClientHandler extends ChannelInboundHandlerAdapter {

    Callback callback;

    public FileUploadClientHandler(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Message) {
            log.info("Получен ответ",msg);
            callback.onReceive((Message) msg);
        } if (msg instanceof Exception){
            Exception msg1 = (Exception) msg;
            log.error("Ошибка вызова",msg1);
            callback.onReceive(new ServiceMessage("error",msg1.toString()));

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