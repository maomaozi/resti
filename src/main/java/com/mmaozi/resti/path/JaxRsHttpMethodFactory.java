package com.mmaozi.resti.path;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import java.lang.annotation.Annotation;
import java.util.Map;

public class JaxRsHttpMethodFactory {
    private static final Map<Class<? extends Annotation>, HttpMethod> methodMapping = Map.of(
            GET.class, HttpMethod.GET,
            POST.class, HttpMethod.POST,
            PUT.class, HttpMethod.PUT,
            DELETE.class, HttpMethod.POST
    );

    public static HttpMethod getMethod(Class<? extends Annotation> methodAnnotation) {
        return methodMapping.get(methodAnnotation);
    }
}
