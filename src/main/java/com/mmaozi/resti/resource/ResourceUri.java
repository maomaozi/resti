package com.mmaozi.resti.resource;

import com.mmaozi.resti.exception.IllegalUriException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE, staticName = "of")
public class ResourceUri {

    private static final Pattern regexUrlMatcher = Pattern.compile("^[\\w\\\\_+-.%\\s]*$|^\\{\\s*([\\D_][\\w\\d_]*)\\s*(:\\s*(.+[\\S])\\s*)?}$");
    private static final String ANY_VALUE = "(\\w+)";
    private final Pattern pattern;
    private final List<String> parameters;

    public static ResourceUri build(String rawUri) {
        if (isEmpty(rawUri)) {
            return ResourceUri.of(Pattern.compile("^/?"), Collections.emptyList());
        }

        List<String> parameters = new ArrayList<>();

        String parsedUrl = Arrays.stream(rawUri.split("/"))
                                 .filter(part -> !part.trim().equals(""))
                                 .map(regexUrlMatcher::matcher)
                                 .map(matcher -> parseRawUri(rawUri, parameters, matcher))
                                 .collect(Collectors.joining("/", "^/?", ""));

        return ResourceUri.of(Pattern.compile(parsedUrl), parameters);
    }

    private static String parseRawUri(String rawUri, List<String> parameters, Matcher matcher) {
        if (!matcher.find()) {
            throw new IllegalUriException("URI: [" + rawUri + "] is not valid");
        }

        String path = matcher.group();
        String pathVariable = matcher.group(1);
        String pathRegex = matcher.group(3);

        if (Objects.nonNull(pathRegex)) {
            parameters.add(pathVariable);
            return String.format("([^/]%s)", pathRegex);
        } else if (Objects.nonNull(pathVariable)) {
            parameters.add(pathVariable);
            return ANY_VALUE;
        } else {
            return path;
        }
    }

    public MatchedParametrizedUri tryMatch(String uri) {
        Matcher matcher = pattern.matcher(uri);

        if (!matcher.find()) {
            return null;
        }

        String remainingUri = matcher.replaceFirst("");

        Map<String, String> parameterValues = IntStream.range(0, this.parameters.size())
                                                       .boxed()
                                                       .collect(Collectors.toMap(
                                                               this.parameters::get,
                                                               i -> matcher.group(i + 1))
                                                       );

        return MatchedParametrizedUri.of(parameterValues, remainingUri);
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE, staticName = "of")
    public static class MatchedParametrizedUri {

        private final Map<String, String> parameters;
        private final String remainingUri;
    }
}
