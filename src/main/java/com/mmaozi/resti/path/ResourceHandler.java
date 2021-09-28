package com.mmaozi.resti.path;

import com.mmaozi.resti.exception.MethodInvokeException;
import com.mmaozi.resti.path.ParametrizedUri.MatchedParametrizedUri;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ResourceHandler {

    private final Map<ParametrizedUri, ResourceFunction> subResourcesProvider = new HashMap<>();
    private final Map<HttpMethod, List<ResourceMethodHandler>> httpMethodHandler = new HashMap<>();
    private final Class<?> resourceClass;

    public void addHttpMethodHandler(HttpMethod method, ResourceMethodHandler handler) {
        httpMethodHandler.computeIfAbsent(method, m -> new ArrayList<>()).add(handler);
    }

    public void addSubResourcesProvider(ParametrizedUri parametrizedUri, ResourceFunction function) {
        subResourcesProvider.put(parametrizedUri, function);
    }

    public HandlerResponse handleUri(HttpContext httpContext, ParseContext parseContext, Object resourceInstance) {

        for (Entry<ParametrizedUri, ResourceFunction> entry : subResourcesProvider.entrySet()) {
            ParametrizedUri parametrizedUri = entry.getKey();

            MatchedParametrizedUri matchedUri = parametrizedUri.tryMatch(parseContext.getUri());

            if (Objects.isNull(matchedUri)) {
                continue;
            }

            ParseContext subResourceParseContext = ParseContext
                .of(parseContext, matchedUri.getRemainingUri(), matchedUri.getParameters());
            parseContext.setChildContext(subResourceParseContext);

            Object subResourceClass;
            try {
                ResourceFunction function = entry.getValue();
                subResourceClass = function.invoke(resourceInstance, httpContext, subResourceParseContext);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new MethodInvokeException("Invoke subresource locator method failed", e);
            }

            return HandlerResponse.of(true, subResourceClass);
        }

        return HandlerResponse.NOT_MATCH;
    }

    public HandlerResponse handleMethod(HttpContext httpContext, ParseContext parseContext, Object resourceInstance) {
        return httpMethodHandler.get(httpContext.getMethod()).stream()
            .map(handler -> handler.tryHandleUri(httpContext, parseContext, resourceInstance))
            .filter(HandlerResponse::isMatch)
            .findFirst()
            .orElse(HandlerResponse.NOT_MATCH);
    }
}