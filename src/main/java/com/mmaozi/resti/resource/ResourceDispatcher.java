package com.mmaozi.resti.resource;


import com.mmaozi.di.container.Container;
import com.mmaozi.resti.context.HttpMethodFactory;
import com.mmaozi.resti.context.HttpRequestCtx;
import com.mmaozi.resti.context.HttpResponseCtx;
import com.mmaozi.resti.context.ParseContext;
import com.mmaozi.resti.exception.StreamIOException;
import com.mmaozi.resti.resource.ResourceUri.MatchedParametrizedUri;
import com.mmaozi.resti.resource.response.DefaultNotFoundResponse;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
public class ResourceDispatcher {

    private final List<ResourceHandler> rootResourceHandler = new ArrayList<>();
    private final Map<Class<?>, ResourceHandler> subResourceHandlers = new HashMap<>();
    private final Container container;
    private final ResourceSerializer serializer;

    @Inject
    public ResourceDispatcher(Container container, ResourceSerializer serializer) {
        this.container = container;
        this.serializer = serializer;
    }

    public void build(List<Class<?>> classes) {

        for (Class<?> clazz : classes) {

            log.info("Register user class {}", clazz);

            ResourceHandler resourceHandler = new ResourceHandler(clazz);
            if (!tryScanResourceMethodsAndLocator(clazz, resourceHandler)) {
                continue;
            }

            subResourceHandlers.put(clazz, resourceHandler);

            Path pathAnnotation = clazz.getAnnotation(Path.class);
            if (nonNull(pathAnnotation)) {
                resourceHandler.setResourceUri(pathAnnotation.value());
                rootResourceHandler.add(resourceHandler);
            }
        }
    }

    private boolean tryScanResourceMethodsAndLocator(Class<?> clazz, ResourceHandler resourceHandler) {
        boolean isResourceHandler = false;
        for (Method method : clazz.getMethods()) {
            Path pathAnnotation = method.getAnnotation(Path.class);
            boolean isMethodHandler = tryResolveMethodHandler(resourceHandler, method, pathAnnotation);
            boolean hasPath = nonNull(pathAnnotation);

            if (hasPath && !isMethodHandler) {
                resourceHandler.addSubResourcesProvider(pathAnnotation.value(), new ResourceFunction(method));
            }

            isResourceHandler = isResourceHandler || hasPath || isMethodHandler;
        }

        return isResourceHandler;
    }

    private boolean tryResolveMethodHandler(ResourceHandler resourceHandler, Method method, Path pathAnnotation) {
        return Arrays.stream(method.getAnnotations())
                     .map(Annotation::annotationType)
                     .map(HttpMethodFactory::getMethod)
                     .filter(Objects::nonNull)
                     .findFirst()
                     .map(httpMethod -> {
                         ResourceMethodHandler handler = new ResourceMethodHandler(isNull(pathAnnotation) ? "" : pathAnnotation.value(), method);
                         resourceHandler.addHttpMethodHandler(httpMethod, handler);
                         return true;
                     })
                     .orElse(false);
    }

    public void handle(HttpRequestCtx httpRequest, HttpResponseCtx httpResponse) {
        for (ResourceHandler resourceHandler : rootResourceHandler) {
            MatchedParametrizedUri matchedParametrizedUri = resourceHandler.match(httpRequest.getOriginalUri());

            if (isNull(matchedParametrizedUri)) {
                continue;
            }

            Object instance = container.getInstance(resourceHandler.getResourceClass());

            ParseContext parseContext = ParseContext.of(
                    null,
                    matchedParametrizedUri.getRemainingUri(),
                    matchedParametrizedUri.getParameters());

            do {
                ResourceResponse resourceResponse = resourceHandler.handleUri(httpRequest, parseContext, instance);
                if (!resourceResponse.isMatch()) {
                    break;
                }
                parseContext = parseContext.getChildContext();
                instance = resourceResponse.getResult();
                resourceHandler = subResourceHandlers.get(instance.getClass());
            } while (!parseContext.getUri().equals(""));

            Object bean = resourceHandler.handleMethod(httpRequest, parseContext, instance)
                                         .getResult();

            handleResponse(httpResponse, bean, Response.Status.OK);
            return;
        }

        handleResponse(httpResponse, DefaultNotFoundResponse.of(httpRequest.getOriginalUri()), Response.Status.NOT_FOUND);
    }

    private void handleResponse(HttpResponseCtx httpResponse, Object bean, Response.StatusType defaultStatus) {
        try {
            if (bean instanceof Response) {
                Response response = (Response) bean;
                httpResponse.setStatus(response.getStatusInfo());
                httpResponse.getOutputStream().write(serializer.serialize(response.getEntity()));
//            response.getHeaders().forEach((key, value)->httpResponse.getHeaders().put(key, value));
            } else {
                httpResponse.getOutputStream().write(serializer.serialize(bean));
                httpResponse.setStatus(defaultStatus);
            }
        } catch (IOException e) {
            throw new StreamIOException("IO Error", e);
        }
    }

}
