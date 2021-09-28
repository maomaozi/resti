package com.mmaozi.resti.path;

import com.mmaozi.resti.exception.MethodInvokeException;
import com.mmaozi.resti.path.ParametrizedUri.MatchedParametrizedUri;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "of")
public class ResourceMethodHandler {

    private final ParametrizedUri parametrizedUri;
    private final ResourceFunction resourceFunction;


    public HandlerResponse tryHandleUri(HttpContext httpContext, ParseContext parseContext, Object resourceInstance) {
        MatchedParametrizedUri matchedUri = parametrizedUri.tryMatch(parseContext.getUri());

        if (Objects.isNull(matchedUri) || !matchedUri.getRemainingUri().equals("")) {
            return HandlerResponse.NOT_MATCH;
        }

        ParseContext currentParseContext = ParseContext
            .of(parseContext, matchedUri.getRemainingUri(), matchedUri.getParameters());

        try {
            Object result = resourceFunction.invoke(resourceInstance, httpContext, currentParseContext);
            return HandlerResponse.of(true, result);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new MethodInvokeException("Invoke resource method failed", e);
        }
    }
}
