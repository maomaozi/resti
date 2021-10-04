package com.mmaozi.resti.resource.paramresolver;

import com.alibaba.fastjson.JSON;
import com.mmaozi.resti.context.HttpContext;
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
    protected BiFunction<HttpContext, ParseContext, Object> resolve(Parameter param, Annotation annotation) {
        return (h, p) -> JSON.parseObject(p.findPathParam(((PathParam) annotation).value()), param.getType());
    }
}
