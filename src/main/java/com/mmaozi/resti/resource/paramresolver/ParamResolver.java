package com.mmaozi.resti.resource.paramresolver;

import com.mmaozi.resti.context.HttpRequestCtx;
import com.mmaozi.resti.context.ParseContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Objects;
import java.util.function.BiFunction;

public abstract class ParamResolver {

    public BiFunction<HttpRequestCtx, ParseContext, Object> tryResolve(Parameter param) {
        Annotation annotation = param.getAnnotation(getAnnotationType());
        if (Objects.isNull(annotation)) {
            return null;
        }

        return resolve(param, annotation);
    }

    protected abstract Class<? extends Annotation> getAnnotationType();

    protected abstract BiFunction<HttpRequestCtx, ParseContext, Object> resolve(Parameter param, Annotation annotation);
}
