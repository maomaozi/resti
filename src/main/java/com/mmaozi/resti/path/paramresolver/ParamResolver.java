package com.mmaozi.resti.path.paramresolver;

import com.mmaozi.resti.path.HttpContext;
import com.mmaozi.resti.path.ParseContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Objects;
import java.util.function.BiFunction;

public abstract class ParamResolver {

    public BiFunction<HttpContext, ParseContext, Object> tryResolve(Parameter param) {
        Annotation annotation = param.getAnnotation(getAnnotationType());
        if (Objects.isNull(annotation)) {
            return null;
        }

        return resolve(param, annotation);
    }

    protected abstract Class<? extends Annotation> getAnnotationType();

    protected abstract BiFunction<HttpContext, ParseContext, Object> resolve(Parameter param, Annotation annotation);
}
