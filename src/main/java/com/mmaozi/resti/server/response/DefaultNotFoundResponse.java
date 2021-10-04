package com.mmaozi.resti.server.response;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(staticName = "of")
public class DefaultNotFoundResponse {
    private final int code = 404;
    private final String uri;
}
