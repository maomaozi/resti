package com.mmaozi.resti.path.paramresolver;

import com.alibaba.fastjson.JSON;
import com.mmaozi.resti.path.HttpContext;
import com.mmaozi.resti.path.ParseContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.function.BiFunction;
import javax.ws.rs.PathParam;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

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
