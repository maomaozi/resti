package com.mmaozi.resti.context;

import io.netty.handler.codec.http.HttpResponseStatus;

import javax.ws.rs.core.Response;
import java.util.Map;

public class HttpStatusFactory {
    private static final Map<Response.StatusType, HttpResponseStatus> statusMapping = Map.of(
            Response.Status.OK, HttpResponseStatus.OK,
            Response.Status.NOT_FOUND, HttpResponseStatus.NOT_FOUND,
            Response.Status.BAD_REQUEST, HttpResponseStatus.BAD_REQUEST,
            Response.Status.INTERNAL_SERVER_ERROR, HttpResponseStatus.INTERNAL_SERVER_ERROR
    );

    public static HttpResponseStatus getStatus(Response.StatusType status) {
        return statusMapping.get(status);
    }
}
