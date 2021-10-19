package com.mmaozi.resti.server;

import com.mmaozi.di.scope.annotations.Prototype;
import com.mmaozi.resti.context.HttpMethod;
import com.mmaozi.resti.context.HttpRequestCtx;
import com.mmaozi.resti.context.HttpResponseCtx;
import com.mmaozi.resti.context.HttpStatusFactory;
import com.mmaozi.resti.exception.StreamIOException;
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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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

    private ExecutorService executorService;
    private Future<?> dispatchTask;

    @Inject
    public RestiHttpRequestHandler(ResourceDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        executorService = Executors.newFixedThreadPool(1);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        executorService.shutdown();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof io.netty.handler.codec.http.HttpRequest) {
            resolveRequest((io.netty.handler.codec.http.HttpRequest) msg);
            dispatchTask = executorService.submit(() -> {
                dispatcher.handle(httpRequest, httpResponse);
                try {
                    channelResponseOs.close();
                } catch (IOException e) {
                    throw new StreamIOException("IO Error", e);
                }
            });
        } else if (msg instanceof HttpContent) {

            ByteBuf body = ((HttpContent) msg).content();

            if (body.readableBytes() > 0) {
                body.readBytes(channelRequestOs, body.readableBytes());
            }

            if (msg instanceof LastHttpContent) {
                this.channelRequestOs.close();

                DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        HttpStatusFactory.getStatus(httpResponse.getStatus()),
                        readResponse(ctx));

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

    private void resolveRequest(io.netty.handler.codec.http.HttpRequest request) throws IOException {

        initStream();

        Map<String, String> headerMap = request.headers()
                                               .entries()
                                               .stream()
                                               .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        httpRequest = HttpRequestCtx.builder()
                                    .originalUri(request.uri())
                                    .method(HttpMethod.valueOf(request.method().name()))
                                    .headers(headerMap)
                                    .inputStream(channelRequestIs)
                                    .build();

        httpResponse = HttpResponseCtx.builder()
                                      .status(Response.Status.OK)
                                      .headers(new HashMap<>())
                                      .outputStream(channelResponseOs)
                                      .build();
    }

    private void initStream() throws IOException {
        channelRequestOs = new PipedOutputStream();
        channelRequestIs = new PipedInputStream(channelRequestOs);
        channelResponseOs = new PipedOutputStream();
        channelResponseIs = new PipedInputStream(channelResponseOs);
    }

    private ByteBuf readResponse(ChannelHandlerContext ctx) throws Exception {
        ByteBuf buffer = ctx.alloc().buffer(1024);
        while (!dispatchTask.isDone()) {
            buffer.writeBytes(channelResponseIs.readNBytes(channelResponseIs.available()));
        }

        dispatchTask.get();

        buffer.writeBytes(channelResponseIs.readAllBytes());
        return buffer;
    }

    private FullHttpResponse defaultErrorResponse(ChannelHandlerContext ctx, Throwable cause) {
        byte[] stackTrace = ExceptionUtils.getStackTrace(cause).getBytes(StandardCharsets.UTF_8);
        ByteBuf buffer = ctx.alloc().buffer(stackTrace.length);
        buffer.writeBytes(stackTrace);
        try {
            channelResponseOs.close();
        } catch (IOException ex) {
            ;
        }

        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, INTERNAL_SERVER_ERROR, buffer);
    }
}