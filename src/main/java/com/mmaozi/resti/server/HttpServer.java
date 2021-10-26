package com.mmaozi.resti.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

@Slf4j
public class HttpServer {

    private final ChannelInitializer<?> channelInitializer;

    @Inject
    public HttpServer(ChannelInitializer<?> channelInitializer) {
        this.channelInitializer = channelInitializer;
    }

    public void run() throws Exception {
        log.info("Initialize netty");
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .childHandler(channelInitializer)
             .option(ChannelOption.SO_BACKLOG, 512)
             .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind(8080).sync();
            log.info("Netty started at port 8080");
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}