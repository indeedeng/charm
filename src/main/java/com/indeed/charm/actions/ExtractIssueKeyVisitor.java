/*
 * Copyright (C) 2011 Indeed Inc.
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

package com.indeed.charm.actions;

import com.indeed.charm.ReleaseEnvironment;
import com.indeed.charm.issues.IssueTracker;
import com.indeed.charm.model.LogEntry;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author jack@indeed.com (Jack Humphrey)
 */
public class ExtractIssueKeyVisitor implements LogEntryVisitor {
    private final IssueTracker issueTracker;
    private final Pattern issueTrackerKeyPattern;
    private final Multimap<String, LogEntry> entries;

    public ExtractIssueKeyVisitor(ReleaseEnvironment env) {
        issueTracker = env.getIssueTracker();
        issueTrackerKeyPattern = env.getIssueTrackerKeyPattern();
        entries = HashMultimap.create();
    }

    public void visit(LogEntry entry) {
        if (issueTrackerKeyPattern != null) {
            Matcher matcher = issueTrackerKeyPattern.matcher(entry.getLogMessage());
            while (matcher.find()) {
                final String issueKey = matcher.group();
                entries.put(issueKey, entry);
            }
        }
    }

    public Set<String> process() {
        Set<String> foundFields = Sets.newHashSet();
        final Set<String> keys = entries.keySet();
        if (keys.isEmpty()) {
            return foundFields;
        }
        final Map<String, Map<String, String>> data = issueTracker.loadDataForKeys(keys);
        for (Map.Entry<String, LogEntry> entry : entries.entries()) {
            Map<String, String> addlFields = data.get(entry.getKey());
            if (addlFields != null) {
                for (Map.Entry<String, String> field : addlFields.entrySet()) {
                    LogEntry logEntry = entry.getValue();
                    String value = logEntry.getAdditionalField(field.getKey());
                    if (value == null) {
                        value = field.getValue();
                    } else {
                        if (!value.contains(field.getValue())) {
                            value += (value.length() > 0 ? ", " : "") + field.getValue();
                        }
                    }
                    logEntry.setAdditionalField(field.getKey(), value);
                    foundFields.add(field.getKey());
                }
            }
        }
        return foundFields;
    }
}
