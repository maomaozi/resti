package com.mmaozi.resti.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.ws.rs.core.Response;
import java.io.OutputStream;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HttpResponseCtx {
    private Response.StatusType status;
    private Map<String, String> headers;

    private OutputStream outputStream;
}
