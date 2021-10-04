package com.mmaozi.resti.resource.paramresolver;

import com.alibaba.fastjson.JSON;
import com.mmaozi.resti.exception.RestiBaseException;
import com.mmaozi.resti.resource.HttpContext;
import com.mmaozi.resti.resource.ParseContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.ws.rs.BeanParam;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.function.BiFunction;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BeanParamResolver extends ParamResolver {

    public static final ParamResolver INSTANCE = new BeanParamResolver();

    @Override
    protected Class<? extends Annotation> getAnnotationType() {
        return BeanParam.class;
    }

    @Override
    protected BiFunction<HttpContext, ParseContext, Object> resolve(Parameter param, Annotation annotation) {
        return (h, p) -> {
            try {
                return JSON.parseObject(h.getRawBody().readNBytes(h.getRawBody().available()), param.getType());
            } catch (IOException e) {
                throw new RestiBaseException("IO error", e);
            }
        };
    }
}
