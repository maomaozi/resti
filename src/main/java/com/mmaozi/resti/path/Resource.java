package com.mmaozi.resti.path;

import java.util.HashMap;
import java.util.Map;
import lombok.Setter;

@Setter
public abstract class Resource {

    protected final Resource parent;
    protected final Map<String, Resource> children = new HashMap<>();
    protected Class<?> resourceClz;

    public Resource(Resource parent) {
        this.parent = parent;
    }

    public abstract boolean isMatch(String value);

    public Resource getChildResourceByValue(String value) {
        return children.get(value);
    }

    public Resource addChildResource(String value, Resource resource) {
        children.put(value, resource);
        return resource;
    }
}
