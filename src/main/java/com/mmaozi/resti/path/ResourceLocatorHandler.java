package com.mmaozi.resti.path;

import com.mmaozi.resti.path.ParametrizedUri.MatchedParametrizedUri;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ResourceLocatorHandler {

    private final Map<ParametrizedUri, ResourceLocatorHandler> subResourceHandler = new HashMap<>();
    private final Map<HttpMethod, List<ResourceMethodHandler>> httpMethodHandler = new HashMap<>();

    public static void main(String[] args) {

    }

    public void addHttpMethodHandler(HttpMethod method, ResourceMethodHandler handler) {

    }

    public void subResourceHandler(String regex, List<String> param) {

    }

    public boolean handleUri(HttpMethod method, String uri) {

        for (Map.Entry<ParametrizedUri, ResourceLocatorHandler> entry : subResourceHandler.entrySet()) {
            ParametrizedUri parametrizedUri = entry.getKey();

            MatchedParametrizedUri matchedUri = parametrizedUri.tryMatch(uri);

            if (!Objects.isNull(matchedUri)) {
                return handleSubResourceLocator(matchedUri);
            }
        }

        return httpMethodHandler.get(method).stream()
                                .map(handler -> handler.tryHandleUri(uri))
                                .filter(x -> x)
                                .findFirst()
                                .orElse(false);
    }

    private boolean handleSubResourceLocator(MatchedParametrizedUri matchedUri) {
        return true;
    }
}
