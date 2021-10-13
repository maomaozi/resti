package com.mmaozi.resti.resource;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class ResourceSerializer {

    public byte[] serialize(Object bean) {
        return JSON.toJSONBytes(bean, SerializerFeature.EMPTY);
    }
}
