package com.mmaozi.resti.resource.paramresolver;

import com.alibaba.fastjson.JSON;
import com.mmaozi.resti.context.HttpRequestCtx;
import com.mmaozi.resti.context.ParseContext;
import com.mmaozi.resti.exception.RestiBaseException;
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
    protected BiFunction<HttpRequestCtx, ParseContext, Object> resolve(Parameter param, Annotation annotation) {
        return (h, p) -> {
            try {
                return JSON.parseObject(h.getInputStream().readNBytes(h.getInputStream().available()), param.getType());
            } catch (IOException e) {
                throw new RestiBaseException("IO error", e);
            }
        };
    }
}
