package com.mmaozi.resti.resource.paramresolver;

import com.alibaba.fastjson.JSON;
import com.mmaozi.resti.context.HttpRequestCtx;
import com.mmaozi.resti.context.ParseContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.ws.rs.QueryParam;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.function.BiFunction;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QueryParamResolver extends ParamResolver {

    public static final ParamResolver INSTANCE = new QueryParamResolver();

    @Override
    protected Class<? extends Annotation> getAnnotationType() {
        return QueryParam.class;
    }

    @Override
    protected BiFunction<HttpRequestCtx, ParseContext, Object> resolve(Parameter param, Annotation annotation) {
        return (h, p) -> JSON.parseObject(h.getQueryParams().get(((QueryParam) annotation).value()), param.getType());
    }
}
