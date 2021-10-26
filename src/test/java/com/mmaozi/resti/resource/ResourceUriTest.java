package com.mmaozi.resti.resource;

import com.mmaozi.resti.resource.ResourceUri.MatchedParametrizedUri;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ResourceUriTest {
    @Test
    void should_match_url_correct() {
        ResourceUri resourceUri = ResourceUri.build("/");
        MatchedParametrizedUri matchedParametrizedUri = resourceUri.tryMatch("/");

        assertNotNull(matchedParametrizedUri);
        assertEquals("", matchedParametrizedUri.getRemainingUri());
    }

    @Test
    void should_match_multiple_level_url_correct() {
        ResourceUri resourceUri = ResourceUri.build("/hello/world");
        MatchedParametrizedUri matchedParametrizedUri = resourceUri.tryMatch("/hello/world/123");

        assertNotNull(matchedParametrizedUri);
        assertEquals("/123", matchedParametrizedUri.getRemainingUri());
    }

    @Test
    void should_parse_multiple_level_url_while_url_not_match() {
        ResourceUri resourceUri = ResourceUri.build("/hello");
        MatchedParametrizedUri matchedParametrizedUri = resourceUri.tryMatch("/hell/world/123");

        assertNull(matchedParametrizedUri);
    }

    @Test
    void should_match_path_param_url_correct() {
        ResourceUri resourceUri = ResourceUri.build("/{id}");
        MatchedParametrizedUri matchedParametrizedUri = resourceUri.tryMatch("/123");

        assertNotNull(matchedParametrizedUri);
        assertEquals("", matchedParametrizedUri.getRemainingUri());
        assertEquals("123", matchedParametrizedUri.getParameters().get("id"));
    }

    @Test
    void should_match_path_param_url_with_regex_correct() {
        ResourceUri resourceUri = ResourceUri.build("/{id:\\d+}");
        MatchedParametrizedUri matchedParametrizedUri = resourceUri.tryMatch("/123");

        assertNotNull(matchedParametrizedUri);
        assertEquals("", matchedParametrizedUri.getRemainingUri());
        assertEquals("123", matchedParametrizedUri.getParameters().get("id"));
    }

    @Test
    void should_match_path_param_url_with_regex_correct_while_not_match() {
        ResourceUri resourceUri = ResourceUri.build("/{id:\\D+}");
        MatchedParametrizedUri matchedParametrizedUri = resourceUri.tryMatch("/123");

        assertNull(matchedParametrizedUri);
    }
}