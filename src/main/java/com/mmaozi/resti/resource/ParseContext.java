package com.mmaozi.resti.resource;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import static java.util.Objects.nonNull;

@Data
@RequiredArgsConstructor(staticName = "of")
public class ParseContext {

    private final ParseContext parentContext;
    private final String uri;
    private final Map<String, String> pathParams;
    private ParseContext childContext;

    public String findPathParam(String param) {
        ParseContext current = this;
        do {
            String res = pathParams.get(param);
            if (nonNull(res)) {
                return res;
            }

            current = current.getParentContext();
        } while (nonNull(current));

        return null;
    }
}
