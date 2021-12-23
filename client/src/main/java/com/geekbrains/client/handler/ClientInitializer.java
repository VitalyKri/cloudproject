package com.geekbrains.client.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import com.geekbrains.core.message.Callback;


/**
 * Creates a newly configured {@link ChannelPipeline} for a new channel.
 */
public class ClientInitializer extends ChannelInitializer<SocketChannel> {

    private ObjectDecoder DECODER = new ObjectDecoder(ClassResolvers.cacheDisabled(null));
    private ObjectEncoder ENCODER = new ObjectEncoder();

    public static FileUploadClientHandler getClientHandler() {
        return CLIENT_HANDLER;
    }

    private static FileUploadClientHandler CLIENT_HANDLER;

    public ClientInitializer(Callback callback) {
        CLIENT_HANDLER = new FileUploadClientHandler(callback);
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();


        // Add the text line codec combination first,
        pipeline.addLast(ENCODER);
        pipeline.addLast(DECODER);

        // and then business logic.
        pipeline.addLast(CLIENT_HANDLER);
    }
}