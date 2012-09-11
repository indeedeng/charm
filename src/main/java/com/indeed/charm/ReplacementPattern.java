/*
 * Copyright (C) 2010 Indeed Inc.
 *
 * This file is part of CHARM.
 *
 * CHARM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CHARM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CHARM.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.indeed.charm;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 */
public class ReplacementPattern {

    private final String name;
    private final Pattern pattern;
    private final String replacement;


    ReplacementPattern(String name, String pattern, String replacement) {
        this.name = name;
        this.pattern = Pattern.compile(pattern);
        this.replacement = replacement;
    }

    public String apply(String s) {
        Matcher m = pattern.matcher(s);
        return m.replaceAll(replacement);
    }

    public String getName() {
        return name;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public String getReplacement() {
        return replacement;
    }
}
