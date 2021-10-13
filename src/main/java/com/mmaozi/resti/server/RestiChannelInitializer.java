package com.mmaozi.resti.server;

import com.mmaozi.di.container.Container;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;

import javax.inject.Inject;

public class RestiChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final Container container;

    @Inject
    public RestiChannelInitializer(Container container) {
        this.container = container;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline()
          .addLast(new HttpServerCodec())
          .addLast(container.getInstance(RestiHttpRequestHandler.class));
    }
}
