package com.mmaozi.resti.resource;

import com.mmaozi.di.container.Container;
import com.mmaozi.resti.context.HttpMethod;
import com.mmaozi.resti.context.HttpRequestCtx;
import com.mmaozi.resti.context.HttpResponseCtx;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceDispatcherTest {

    private final ResourceSerializer serializer = new ResourceSerializer();
    @Mock
    private Container container;
    @Mock
    private InputStream inputStream;
    @Mock
    private OutputStream outputStream;

    @Test
    void should_parse_root_entry_while_handle_request() throws IOException {
        when(container.getInstance(TestClass.class)).thenReturn(new TestClass());

        ResourceDispatcher dispatcher = new ResourceDispatcher(container, serializer);

        dispatcher.build(Collections.singletonList(TestClass.class));

        HttpResponseCtx httpResponse = getDefaultResponseCtx();
        HttpRequestCtx httpRequest = getDefaultRequestCtx();
        dispatcher.handle(httpRequest, httpResponse);

        verify(outputStream).write("10".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void should_parse_nested_entry_while_handle_request() throws IOException {
        when(container.getInstance(TestClass.class)).thenReturn(new TestClass());

        ResourceDispatcher dispatcher = new ResourceDispatcher(container, serializer);

        dispatcher.build(List.of(TestClass.class, NextClass.class));

        HttpResponseCtx httpResponse = getDefaultResponseCtx();
        HttpRequestCtx httpRequest = getDefaultRequestCtx();
        httpRequest.setOriginalUri("/next");
        dispatcher.handle(httpRequest, httpResponse);

        verify(outputStream).write("\"hello\"".getBytes(StandardCharsets.UTF_8));
    }

    private HttpRequestCtx getDefaultRequestCtx() {
        return HttpRequestCtx.builder()
                             .inputStream(inputStream)
                             .originalUri("/")
                             .headers(Collections.emptyMap())
                             .queryParams(Collections.emptyMap())
                             .method(HttpMethod.GET).build();
    }

    private HttpResponseCtx getDefaultResponseCtx() {
        return HttpResponseCtx.builder()
                              .outputStream(outputStream)
                              .headers(new HashMap<>())
                              .build();
    }

    public static class NextClass {
        @GET
        public String get() {
            return "hello";
        }
    }

    @Path("/")
    public static class TestClass {

        @GET
        public Integer get() {
            return 10;
        }

        @Path("/next")
        public NextClass nextClass() {
            return new NextClass();
        }
    }
}