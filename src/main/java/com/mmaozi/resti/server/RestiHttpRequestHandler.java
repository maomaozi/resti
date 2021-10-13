package com.mmaozi.resti.server;

import com.mmaozi.di.scope.annotations.Prototype;
import com.mmaozi.resti.context.HttpMethod;
import com.mmaozi.resti.context.HttpRequestCtx;
import com.mmaozi.resti.context.HttpResponseCtx;
import com.mmaozi.resti.context.HttpStatusFactory;
import com.mmaozi.resti.resource.ResourceDispatcher;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;

@Prototype
public class RestiHttpRequestHandler extends ChannelInboundHandlerAdapter {

    private HttpRequestCtx httpRequest;

    private final ResourceDispatcher dispatcher;
    private HttpResponseCtx httpResponse;
    private PipedOutputStream channelRequestOs;
    private InputStream channelRequestIs;
    private PipedOutputStream channelResponseOs;
    private InputStream channelResponseIs;

    @Inject
    public RestiHttpRequestHandler(ResourceDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        channelRequestOs = new PipedOutputStream();
        channelRequestIs = new PipedInputStream(channelRequestOs);
        channelResponseOs = new PipedOutputStream();
        channelResponseIs = new PipedInputStream(channelResponseOs);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        this.channelRequestOs.close();
        this.channelRequestIs.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException {

        if (msg instanceof io.netty.handler.codec.http.HttpRequest) {
            resolveRequest((io.netty.handler.codec.http.HttpRequest) msg);
        } else if (msg instanceof HttpContent) {

            ByteBuf body = ((HttpContent) msg).content();

            if (body.readableBytes() > 0) {
                body.readBytes(channelRequestOs, body.readableBytes());
            }

            if (msg instanceof LastHttpContent) {
                dispatcher.handle(httpRequest, httpResponse);

                DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        HttpStatusFactory.getStatus(httpResponse.getStatus()),
                        getResponseByteBuf(ctx));

                httpResponse.getHeaders()
                            .forEach((key, value) -> response.headers().set(key, value));

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

    private void resolveRequest(io.netty.handler.codec.http.HttpRequest request) {
        HttpMethod httpMethod = HttpMethod.valueOf(request.method().name());
        Map<String, String> headerMap = request.headers()
                                               .entries()
                                               .stream()
                                               .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        httpRequest = HttpRequestCtx.builder()
                                    .originalUri(request.uri())
                                    .method(httpMethod)
                                    .headers(headerMap)
                                    .inputStream(channelRequestIs)
                                    .build();

        httpResponse = HttpResponseCtx.builder()
                                      .status(Response.Status.OK)
                                      .headers(new HashMap<>())
                                      .outputStream(channelResponseOs)
                                      .build();
    }

    private ByteBuf getResponseByteBuf(ChannelHandlerContext ctx) throws IOException {
        byte[] bytes = channelResponseIs.readNBytes(channelResponseIs.available());
        ByteBuf buffer = ctx.alloc().buffer(bytes.length);
        buffer.writeBytes(bytes);
        return buffer;
    }

    private FullHttpResponse defaultErrorResponse(ChannelHandlerContext ctx, Throwable cause) {
        try {
            String stackTrace = ExceptionUtils.getStackTrace(cause);
            channelResponseOs.write(stackTrace.getBytes());
            return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, INTERNAL_SERVER_ERROR, getResponseByteBuf(ctx));
        } catch (Exception ex) {
            ByteBuf buffer = ctx.alloc().buffer(32);
            buffer.writeBytes("Unknown error".getBytes());
            return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, INTERNAL_SERVER_ERROR, buffer);
        }
    }
}