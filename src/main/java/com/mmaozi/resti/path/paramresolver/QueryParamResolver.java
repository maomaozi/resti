package com.mmaozi.resti.path.paramresolver;

import com.mmaozi.resti.path.HttpContext;
import com.mmaozi.resti.path.ParseContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.function.BiFunction;
import javax.ws.rs.QueryParam;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QueryParamResolver extends ParamResolver {

    public static final ParamResolver INSTANCE = new QueryParamResolver();

    @Override
    protected Class<? extends Annotation> getAnnotationType() {
        return QueryParam.class;
    }

    @Override
    protected BiFunction<HttpContext, ParseContext, Object> resolve(Parameter param, Annotation annotation) {
        return (h, p) -> h.getQueryParams().get(((QueryParam) annotation).value());
    }
}
