package com.mmaozi.resti.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.mmaozi.di.scope.annotations.Prototype;
import com.mmaozi.resti.context.HttpContext;
import com.mmaozi.resti.context.HttpMethod;
import com.mmaozi.resti.resource.ResourceDispatcher;
import com.mmaozi.resti.server.response.DefaultInternalErrorResponse;
import com.mmaozi.resti.server.response.DefaultNotFoundResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.inject.Inject;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static java.util.Objects.isNull;

@Prototype
public class RestiHttpRequestHandler extends ChannelInboundHandlerAdapter {

    @Inject
    public RestiHttpRequestHandler(ResourceDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    private final ResourceDispatcher dispatcher;
    private HttpContext context;

    PipedOutputStream os;
    InputStream is;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        os = new PipedOutputStream();
        is = new PipedInputStream(os);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        this.os.close();
        this.is.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof HttpRequest) {
            resolveRequest((HttpRequest) msg);
        } else if (msg instanceof HttpContent) {

            ByteBuf body = ((HttpContent) msg).content();

            if (body.readableBytes() > 0) {
                body.readBytes(os, body.readableBytes());
            }

            if (msg instanceof LastHttpContent) {
                Object result = dispatcher.handle(context);
                FullHttpResponse response = isNull(result) ?
                        defaultNotFoundResponse(ctx, context.getOriginalUri()) : resolveResponse(ctx, result);
                ctx.writeAndFlush(response)
                   .addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.writeAndFlush(defaultErrorResponse(ctx, cause))
           .addListener(ChannelFutureListener.CLOSE);
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
                             .rawBody(is)
                             .build();
    }

    private FullHttpResponse resolveResponse(ChannelHandlerContext ctx, Object bean) {
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, OK, serialize(ctx, bean));
    }

    private ByteBuf serialize(ChannelHandlerContext ctx, Object bean) {
        byte[] bytes = JSON.toJSONBytes(bean, SerializerFeature.EMPTY);

        ByteBuf buffer = ctx.alloc().buffer(bytes.length);
        buffer.writeBytes(bytes);
        return buffer;
    }

    private FullHttpResponse defaultErrorResponse(ChannelHandlerContext ctx, Throwable cause) {
        String stackTrace = ExceptionUtils.getStackTrace(cause);
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, INTERNAL_SERVER_ERROR,
                serialize(ctx, DefaultInternalErrorResponse.of(stackTrace)));
    }

    private FullHttpResponse defaultNotFoundResponse(ChannelHandlerContext ctx, String uri) {
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, NOT_FOUND, serialize(ctx, DefaultNotFoundResponse.of(uri)));
    }
}