package com.mmaozi.resti.path;

import java.util.regex.Pattern;

public class RegexResource extends Resource {

    private final Pattern pattern;

    public RegexResource(Resource parent, String pattern) {
        super(parent);
        this.pattern = Pattern.compile(pattern);
    }

    @Override
    public boolean isMatch(String value) {
        return pattern.asMatchPredicate().test(value);
    }
}
