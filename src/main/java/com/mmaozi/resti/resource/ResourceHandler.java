package com.mmaozi.resti.resource;

import com.mmaozi.resti.context.HttpContext;
import com.mmaozi.resti.context.HttpMethod;
import com.mmaozi.resti.context.ParseContext;
import com.mmaozi.resti.exception.MethodInvokeException;
import com.mmaozi.resti.resource.ResourceUri.MatchedParametrizedUri;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

@Getter
@RequiredArgsConstructor
public class ResourceHandler {

    private final Map<ResourceUri, ResourceFunction> subResourcesProvider = new HashMap<>();
    private final Map<HttpMethod, List<ResourceMethodHandler>> httpMethodHandler = new HashMap<>();
    private final Class<?> resourceClass;

    private ResourceUri resourceUri;

    public void addHttpMethodHandler(HttpMethod method, ResourceMethodHandler handler) {
        httpMethodHandler.computeIfAbsent(method, m -> new ArrayList<>()).add(handler);
    }

    public void addSubResourcesProvider(String uri, ResourceFunction function) {
        subResourcesProvider.put(ResourceUri.build(uri), function);
    }

    public MatchedParametrizedUri match(String uri) {
        return resourceUri.tryMatch(uri);
    }

    public ResourceResponse handleUri(HttpContext httpContext, ParseContext parseContext, Object resourceInstance) {

        for (Entry<ResourceUri, ResourceFunction> entry : subResourcesProvider.entrySet()) {
            ResourceUri resourceUri = entry.getKey();

            MatchedParametrizedUri matchedUri = resourceUri.tryMatch(parseContext.getUri());

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

            return ResourceResponse.of(true, subResourceClass);
        }

        return ResourceResponse.NOT_MATCH;
    }

    public ResourceResponse handleMethod(HttpContext httpContext, ParseContext parseContext, Object resourceInstance) {
        return httpMethodHandler.get(httpContext.getMethod()).stream()
                                .map(handler -> handler.tryHandleUri(httpContext, parseContext, resourceInstance))
                                .filter(ResourceResponse::isMatch)
                                .findFirst()
                                .orElse(ResourceResponse.NOT_MATCH);
    }

    public void setResourceUri(String uri) {
        resourceUri = ResourceUri.build(uri);
    }
}