package com.mmaozi.resti.path;

import static java.util.Objects.nonNull;

import java.util.Map;
import lombok.Data;
import lombok.RequiredArgsConstructor;

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
