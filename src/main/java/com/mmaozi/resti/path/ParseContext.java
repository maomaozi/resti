package com.mmaozi.resti.path;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

import static java.util.Objects.nonNull;

@Data
@AllArgsConstructor(staticName = "of")
public class ParseContext {
    private ParseContext parentContext;
    private String uri;
    private Map<String, String> pathParams;

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
