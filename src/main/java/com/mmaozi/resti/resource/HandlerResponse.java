package com.mmaozi.resti.resource;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class HandlerResponse {

    public static final HandlerResponse NOT_MATCH = HandlerResponse.of(false, null);

    private final boolean match;
    private final Object result;
}
