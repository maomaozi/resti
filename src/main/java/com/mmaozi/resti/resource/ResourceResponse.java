package com.mmaozi.resti.resource;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class ResourceResponse {

    public static final ResourceResponse NOT_MATCH = ResourceResponse.of(false, null);

    private final boolean match;
    private final Object result;
}
