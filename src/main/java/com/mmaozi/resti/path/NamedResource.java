package com.mmaozi.resti.path;

public class NamedResource extends Resource {

    private final String name;

    public NamedResource(Resource parent, String name) {
        super(parent);
        this.name = name;
    }

    @Override
    public boolean isMatch(String value) {
        return name.equals(value);
    }
}
