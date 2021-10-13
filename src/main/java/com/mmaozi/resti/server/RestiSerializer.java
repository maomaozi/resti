package com.mmaozi.resti.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class RestiSerializer {

    public byte[] serialize(Object bean) {
        return JSON.toJSONBytes(bean, SerializerFeature.EMPTY);
    }
}
