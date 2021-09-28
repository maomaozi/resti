package com.mmaozi.resti.path;

import com.mmaozi.resti.exception.MethodInvokeException;
import com.mmaozi.resti.path.ParametrizedUri.MatchedParametrizedUri;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ResourceHandler {

    private final Map<ParametrizedUri, ResourceHandler> subResourceHandler = new HashMap<>();
    private final Map<HttpMethod, List<ResourceMethodHandler>> httpMethodHandler = new HashMap<>();

    private ParametrizedFunction resourceLocatorFunction;

    public void addHttpMethodHandler(HttpMethod method, ResourceMethodHandler handler) {
        httpMethodHandler.computeIfAbsent(method, m -> new ArrayList<>()).add(handler);
    }

    public void subResourceHandler(String regex, ResourceHandler handler) {
        subResourceHandler.put(ParametrizedUri.build(regex), handler);
    }

    public boolean handleUri(HttpMethod method, String uri, Class<?> resourceClass) {

        for (Map.Entry<ParametrizedUri, ResourceHandler> entry : subResourceHandler.entrySet()) {
            ParametrizedUri parametrizedUri = entry.getKey();
            MatchedParametrizedUri matchedUri = parametrizedUri.tryMatch(uri);

            if (Objects.isNull(matchedUri)) {
                continue;
            }

            Class<?> subResourceClass;
            try {
                subResourceClass = (Class<?>) resourceLocatorFunction.invoke(resourceClass, matchedUri.getParameters());
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new MethodInvokeException("Invoke subresource locator method failed", e);
            }

            return subResourceHandler.get(parametrizedUri).handleUri(method, matchedUri.getRemainingUri(), subResourceClass);
        }

        return httpMethodHandler.get(method).stream()
                                .map(handler -> handler.tryHandleUri(uri))
                                .filter(x -> x)
                                .findFirst()
                                .orElse(false);
    }
}