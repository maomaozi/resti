package com.mmaozi.resti.server;

import com.mmaozi.di.scope.annotations.Prototype;
import com.mmaozi.resti.resource.HttpContext;
import com.mmaozi.resti.resource.HttpMethod;
import com.mmaozi.resti.resource.ResourceDispatcher;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;

import javax.inject.Inject;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

@Prototype
public class RestiHttpRequestHandler extends ChannelInboundHandlerAdapter {

    private static final FullHttpResponse OK_RESPONSE = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, OK,
            Unpooled.EMPTY_BUFFER);

    static {
        OK_RESPONSE.headers().set(CONTENT_LENGTH, 0);
    }

    private final ResourceDispatcher dispatcher;
    private HttpContext context;

    @Inject
    public RestiHttpRequestHandler(ResourceDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        if (msg instanceof HttpRequest) {
            resolveRequest((HttpRequest) msg);
        } else if (msg instanceof HttpContent) {
            Object result = dispatcher.handle(context);
            ctx.writeAndFlush(OK_RESPONSE).addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private void resolveRequest(HttpRequest request) {
        HttpMethod httpMethod = HttpMethod.valueOf(request.method().name());
        Map<String, String> headerMap = request.headers()
                                               .entries()
                                               .stream()
                                               .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        context = HttpContext.builder()
                             .originalUri(request.uri())
                             .method(httpMethod)
                             .headers(headerMap)
                             .build();
    }
}