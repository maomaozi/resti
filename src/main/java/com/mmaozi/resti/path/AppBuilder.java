package com.mmaozi.resti.path;


import static java.util.Objects.isNull;

import com.mmaozi.di.container.Container;
import com.mmaozi.di.utils.ReflectionUtils;
import com.mmaozi.resti.example.MyApp;
import com.mmaozi.resti.exception.RestiBaseException;
import com.mmaozi.resti.path.ParametrizedUri.MatchedParametrizedUri;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

public class AppBuilder {

    private static final Map<Class<? extends Annotation>, HttpMethod> methodMapping = Map
        .of(GET.class, HttpMethod.GET, POST.class, HttpMethod.POST);
    private final Container container = new Container();
    private final Map<ParametrizedUri, ResourceHandler> rootResourceHandler = new HashMap<>();
    private final Map<Class<?>, ResourceHandler> handlers = new HashMap<>();

    public static void main(String[] args) {
        AppBuilder appBuilder = new AppBuilder();
        appBuilder.build();
        HttpContext httpContext = new HttpContext(HttpMethod.GET, "/customers/123/orders", Collections.emptyMap(),
            Collections.emptyMap(), Collections.emptyMap(), null);
        appBuilder.run(httpContext);
    }

    public void build() {
        List<Class<?>> classes = scanPackages(MyApp.class);
        List<Class<?>> classWithAnnotationType = ReflectionUtils.getClassWithAnnotationType(Path.class, classes);

        for (Class<?> clazz : classes) {
            container.register(clazz);
            ResourceHandler resourceHandler = new ResourceHandler(clazz);

            for (Method method : clazz.getMethods()) {
                Path pathAnnotation = method.getAnnotation(Path.class);

                boolean isMethodHandler = false;
                for (Class<? extends Annotation> annotation : methodMapping.keySet()) {
                    Annotation methodAnnotation = method.getAnnotation(annotation);

                    if (isNull(methodAnnotation)) {
                        continue;
                    }

                    ParametrizedUri parametrizedUri = ParametrizedUri
                        .build(isNull(pathAnnotation) ? "" : pathAnnotation.value());

                    resourceHandler.addHttpMethodHandler(
                        methodMapping.get(annotation),
                        ResourceMethodHandler.of(parametrizedUri, new ResourceFunction(method)));

                    isMethodHandler = true;
                    break;
                }

                if (isNull(pathAnnotation) || isMethodHandler) {
                    continue;
                }
                resourceHandler.addSubResourcesProvider(
                    ParametrizedUri.build(pathAnnotation.value()),
                    new ResourceFunction(method));
            }

            handlers.put(clazz, resourceHandler);
            if (classWithAnnotationType.contains(clazz)) {
                rootResourceHandler
                    .put(ParametrizedUri.build(clazz.getAnnotation(Path.class).value()), resourceHandler);
            }
        }
    }

    void run(HttpContext httpContext) {
        for (Entry<ParametrizedUri, ResourceHandler> entry : rootResourceHandler.entrySet()) {
            MatchedParametrizedUri matchedParametrizedUri = entry.getKey().tryMatch(httpContext.getOriginalUri());

            if (isNull(matchedParametrizedUri)) {
                continue;
            }
            ResourceHandler resourceHandler = entry.getValue();
            Object instance = container.getInstance(resourceHandler.getResourceClass());

            ParseContext parseContext = ParseContext
                .of(null, matchedParametrizedUri.getRemainingUri(), matchedParametrizedUri.getParameters());

            do {
                HandlerResponse handlerResponse = resourceHandler.handleUri(httpContext, parseContext, instance);
                if (!handlerResponse.isMatch()) {
                    break;
                }
                parseContext = parseContext.getChildContext();
                instance = handlerResponse.getResult();
                resourceHandler = handlers.get(instance.getClass());
            } while (!parseContext.getUri().equals(""));

            resourceHandler.handleMethod(httpContext, parseContext, instance);
        }
    }

    private List<Class<?>> scanPackages(Class<?> clazz) {
        return ReflectionUtils.findAllClassesInPackage(clazz.getPackage()).stream()
            .filter(className -> !className.startsWith("com.mmaozi.di"))
            .map(this::getClass)
            .collect(Collectors.toList());
    }

    private Class<?> getClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ex) {
            throw new RestiBaseException("");
        }
    }
}
