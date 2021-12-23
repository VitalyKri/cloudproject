package com.geekbrains.server.handler;


import com.geekbrains.server.db.AuthService;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;


/**
 * Creates a newly configured {@link ChannelPipeline} for a new channel.
 */
public class ServerInitializer extends ChannelInitializer<SocketChannel> {

    private ObjectDecoder DECODER;
    private ObjectEncoder ENCODER;


    private FileUploadServerHandler SERVER_HANDLER;

    public ServerInitializer(AuthService authService) {
        DECODER = new ObjectDecoder(ClassResolvers.cacheDisabled(null));
        ENCODER = new ObjectEncoder();
        SERVER_HANDLER = new FileUploadServerHandler(authService);
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();


        // the encoder and decoder are static as these are sharable
        pipeline.addLast(ENCODER);
        pipeline.addLast(DECODER);

        // and then business logic.
        pipeline.addLast(SERVER_HANDLER);
    }
}