package com.mmaozi.resti.server.response;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(staticName = "of")
public class DefaultInternalErrorResponse {
    private final int code = 500;
    private final String stackTrace;
}
