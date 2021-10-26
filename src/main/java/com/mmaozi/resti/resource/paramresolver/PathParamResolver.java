package com.mmaozi.resti.resource.paramresolver;

import com.alibaba.fastjson.JSON;
import com.mmaozi.resti.context.HttpRequestCtx;
import com.mmaozi.resti.context.ParseContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.ws.rs.PathParam;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.function.BiFunction;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PathParamResolver extends ParamResolver {

    public static final ParamResolver INSTANCE = new PathParamResolver();

    @Override
    protected Class<? extends Annotation> getAnnotationType() {
        return PathParam.class;
    }

    @Override
    protected BiFunction<HttpRequestCtx, ParseContext, Object> resolve(Parameter param, Annotation annotation) {
        return (h, p) -> {
            String pathParam = p.findPathParam(((PathParam) annotation).value());
            return param.getType().equals(String.class) ? pathParam : JSON.parseObject(pathParam, param.getType());
        };
    }
}
