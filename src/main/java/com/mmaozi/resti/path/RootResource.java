package com.mmaozi.resti.path;

public class RootResource extends Resource {

    public RootResource() {
        super(null);
    }

    @Override
    public boolean isMatch(String value) {
        return false;
    }
}
