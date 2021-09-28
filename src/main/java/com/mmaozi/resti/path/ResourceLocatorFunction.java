package com.mmaozi.resti.path;

import com.mmaozi.resti.path.paramresolver.ParamResolver;
import com.mmaozi.resti.path.paramresolver.PathParamResolver;
import com.mmaozi.resti.path.paramresolver.QueryParamResolver;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

public class ResourceLocatorFunction {

    private final Method method;

    private static final List<ParamResolver> resolvers = List
        .of(PathParamResolver.INSTANCE, QueryParamResolver.INSTANCE);
    private final List<BiFunction<HttpContext, ParseContext, Object>> valueExtractor = new ArrayList<>();

    public ResourceLocatorFunction(Method method) {
        this.method = method;
        Parameter[] parameters = method.getParameters();

        for (Parameter p : parameters) {
            BiFunction<HttpContext, ParseContext, Object> extractor = resolvers.stream()
                .map(resolver -> resolver.tryResolve(p))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse((httpContext, parseContext) -> null);

            valueExtractor.add(extractor);
        }
    }

    public Object invoke(Object obj, HttpContext httpContext, ParseContext parseContext)
        throws InvocationTargetException, IllegalAccessException {

        Object[] parameterValues = valueExtractor.stream()
            .map(extractor -> extractor.apply(httpContext, parseContext))
            .toArray();

        return method.invoke(obj, parameterValues);
    }
}
