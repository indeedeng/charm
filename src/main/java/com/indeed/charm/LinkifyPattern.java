package com.indeed.charm;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 */
public class LinkifyPattern {

    private Pattern pattern;
    private String replacement;


    LinkifyPattern(String pattern, String replacement) {
        this.pattern = Pattern.compile(pattern);
        this.replacement = replacement;
    }

    public String apply(String s) {
        Matcher m = pattern.matcher(s);
        return m.replaceAll(replacement);
    }

    public Pattern getPattern() {
        return pattern;
    }

    public String getReplacement() {
        return replacement;
    }
}
