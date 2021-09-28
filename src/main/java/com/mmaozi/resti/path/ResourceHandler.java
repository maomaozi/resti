package com.mmaozi.resti.path;

import com.mmaozi.resti.exception.MethodInvokeException;
import com.mmaozi.resti.path.ParametrizedUri.MatchedParametrizedUri;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "of")
public class ResourceHandler {

    private final Map<ParametrizedUri, ResourceHandler> subResourceHandler = new HashMap<>();
    private final Map<HttpMethod, List<ResourceMethodHandler>> httpMethodHandler = new HashMap<>();

    // dynamic binding resource class and function
    private final ResourceFunction resourceFunction;

    public void addHttpMethodHandler(HttpMethod method, ResourceMethodHandler handler) {
        httpMethodHandler.computeIfAbsent(method, m -> new ArrayList<>()).add(handler);
    }

    public void subResourceHandler(String regex, ResourceHandler handler) {
        subResourceHandler.put(ParametrizedUri.build(regex), handler);
    }

    public HandlerResponse handleUri(HttpContext httpContext, ParseContext parseContext, Class<?> resourceClass) {

        for (Map.Entry<ParametrizedUri, ResourceHandler> entry : subResourceHandler.entrySet()) {
            ParametrizedUri parametrizedUri = entry.getKey();
            MatchedParametrizedUri matchedUri = parametrizedUri.tryMatch(parseContext.getUri());

            if (Objects.isNull(matchedUri)) {
                continue;
            }

            ParseContext subResourceParseContext = ParseContext
                .of(parseContext, matchedUri.getRemainingUri(), matchedUri.getParameters());

            Class<?> subResourceClass;
            try {
                subResourceClass = (Class<?>) resourceFunction
                    .invoke(resourceClass, httpContext, subResourceParseContext);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new MethodInvokeException("Invoke subresource locator method failed", e);
            }

            return subResourceHandler.get(parametrizedUri)
                .handleUri(httpContext, subResourceParseContext, subResourceClass);
        }

        return httpMethodHandler.get(httpContext.getMethod()).stream()
            .map(handler -> handler.tryHandleUri(httpContext, parseContext, resourceClass))
            .filter(HandlerResponse::isMatch)
            .findFirst()
            .orElse(HandlerResponse.NOT_MATCH);
    }
}