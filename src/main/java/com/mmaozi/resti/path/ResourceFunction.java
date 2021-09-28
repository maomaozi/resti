package com.mmaozi.resti.path;

import javax.ws.rs.BeanParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

public class ResourceFunction {
    private final Method method;
    private static final Map<Class, BiFunction<HttpContext, ParseContext, Object>> extractorFactory =
            Map.of(
                    PathParam.class, (h, p) -> p.findPathParam(""),
                    QueryParam.class, (h, p) -> h.getQueryParams().get(""),
                    HeaderParam.class, (h, p) -> h.getHeaders().get(""),
                    BeanParam.class, (h, p) -> h.getRawBody(),
                    FormParam.class, (h, p) -> h.getFormData().get("")
            );
    private final List<BiFunction<HttpContext, ParseContext, String>> valueExtractor = new ArrayList<>();

    public ResourceFunction(Method method) {
        this.method = method;
        Parameter[] parameters = method.getParameters();


        for (Parameter p : parameters) {

            extractorFactory.entrySet()
                            .stream()
                            .filter(entry -> Objects.nonNull(p.getAnnotation(entry.getKey())))
                            .map(Map.Entry::getValue)
                            .findFirst()
                            .orElse((httpContext, parseContext) -> parseContext.findPathParam()))

        }
    }

    public Object invoke(Object obj, HttpContext httpContext, ParseContext parseContext) throws InvocationTargetException, IllegalAccessException {

        Object[] parameterValues = new Object[parameters.length];


        return method.invoke(obj, parameterValues);
    }
}
