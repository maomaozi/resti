package com.mmaozi.resti.path;


import com.mmaozi.di.container.Container;
import com.mmaozi.di.utils.ReflectionUtils;
import com.mmaozi.resti.example.MyApp;
import com.mmaozi.resti.exception.RestiBaseException;
import com.mmaozi.resti.path.ParametrizedUri.MatchedParametrizedUri;

import javax.ws.rs.Path;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class RestiApplication {

    private final Container container = new Container();

    private final List<ResourceHandler> rootResourceHandler = new ArrayList<>();
    private final Map<Class<?>, ResourceHandler> subResourceHandlers = new HashMap<>();

    public static void main(String[] args) {
        RestiApplication restiApplication = new RestiApplication();
        restiApplication.build();
        HttpContext httpContext = new HttpContext(HttpMethod.GET, "/customers/123/orders", Collections.emptyMap(),
                Collections.emptyMap(), Collections.emptyMap(), null);


        Object result = restiApplication.handle(httpContext);
    }

    public void build() {
        List<Class<?>> classes = scanPackages(MyApp.class);

        for (Class<?> clazz : classes) {
            container.register(clazz);

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
                     .map(JaxRsHttpMethodFactory::getMethod)
                     .filter(Objects::nonNull)
                     .findFirst()
                     .map(httpMethod -> {
                         ResourceMethodHandler handler = new ResourceMethodHandler(isNull(pathAnnotation) ? "" : pathAnnotation.value(), method);
                         resourceHandler.addHttpMethodHandler(httpMethod, handler);
                         return true;
                     })
                     .orElse(false);
    }

    public Object handle(HttpContext httpContext) {
        for (ResourceHandler resourceHandler : rootResourceHandler) {
            MatchedParametrizedUri matchedParametrizedUri = resourceHandler.match(httpContext.getOriginalUri());

            if (isNull(matchedParametrizedUri)) {
                continue;
            }

            Object instance = container.getInstance(resourceHandler.getResourceClass());

            ParseContext parseContext = ParseContext.of(
                    null,
                    matchedParametrizedUri.getRemainingUri(),
                    matchedParametrizedUri.getParameters());

            do {
                HandlerResponse handlerResponse = resourceHandler.handleUri(httpContext, parseContext, instance);
                if (!handlerResponse.isMatch()) {
                    break;
                }
                parseContext = parseContext.getChildContext();
                instance = handlerResponse.getResult();
                resourceHandler = subResourceHandlers.get(instance.getClass());
            } while (!parseContext.getUri().equals(""));

            return resourceHandler.handleMethod(httpContext, parseContext, instance)
                                  .getResult();
        }

        // should return 404 reponse here
        return null;
    }

    private List<Class<?>> scanPackages(Class<?> clazz) {
        return ReflectionUtils.findAllClassesInPackage(clazz.getPackage()).stream()
                              .map((String className) -> {
                                  try {
                                      return Class.forName(className);
                                  } catch (ClassNotFoundException ex) {
                                      throw new RestiBaseException("");
                                  }
                              })
                              .collect(Collectors.toList());
    }

}
