package com.mmaozi.resti;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;

public class HttpServerHandler extends ChannelInboundHandlerAdapter {

    private static final FullHttpResponse OK_RESPONSE = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, OK,
        Unpooled.EMPTY_BUFFER);

    static {
        OK_RESPONSE.headers().set(CONTENT_LENGTH, 0);
    }

    private HttpRequest request;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        if (msg instanceof HttpRequest) {
            this.request = (HttpRequest) msg;
        } else if (msg instanceof HttpContent) {
            ctx.writeAndFlush(OK_RESPONSE).addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}