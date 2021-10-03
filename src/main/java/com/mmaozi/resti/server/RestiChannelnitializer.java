package com.mmaozi.resti.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;

import javax.inject.Inject;

public class RestiChannelnitializer extends ChannelInitializer<SocketChannel> {

    private final RestiHttpRequestHandler requestHandler;

    @Inject
    public RestiChannelnitializer(RestiHttpRequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline().addLast(new HttpServerCodec()).addLast(requestHandler);
    }
}
