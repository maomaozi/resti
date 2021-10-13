package com.mmaozi.resti.resource.response;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(staticName = "of")
public class DefaultNotFoundResponse {
    private final String uri;
}