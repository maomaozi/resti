package com.mmaozi.resti.resource;

import com.mmaozi.resti.context.HttpRequestCtx;
import com.mmaozi.resti.context.ParseContext;
import com.mmaozi.resti.exception.MethodInvokeException;
import com.mmaozi.resti.resource.ResourceUri.MatchedParametrizedUri;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;


public class ResourceMethodHandler {

    private final ResourceUri resourceUri;
    private final ResourceFunction resourceFunction;

    public ResourceMethodHandler(String uri, Method method) {
        this.resourceUri = ResourceUri.build(uri);
        this.resourceFunction = new ResourceFunction(method);
    }

    public ResourceResponse tryHandleUri(HttpRequestCtx httpRequest, ParseContext parseContext, Object resourceInstance) {
        MatchedParametrizedUri matchedUri = resourceUri.tryMatch(parseContext.getUri());

        if (Objects.isNull(matchedUri) || !matchedUri.getRemainingUri().equals("")) {
            return ResourceResponse.NOT_MATCH;
        }

        ParseContext currentParseContext = ParseContext
                .of(parseContext, matchedUri.getRemainingUri(), matchedUri.getParameters());

        try {
            Object result = resourceFunction.invoke(resourceInstance, httpRequest, currentParseContext);
            return ResourceResponse.of(true, result);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new MethodInvokeException("Invoke resource method failed", e);
        }
    }
}
