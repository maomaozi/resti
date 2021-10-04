package com.mmaozi.resti.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.InputStream;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HttpContext {
    private HttpMethod method;
    private String originalUri;

    private Map<String, String> queryParams;
    private Map<String, String> headers;

    private Map<String, InputStream> formData;
    private InputStream rawBody;
}