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

package com.indeed.charm.svn;

import com.indeed.charm.model.LogEntry;
import com.indeed.charm.model.LogEntryPath;
import com.google.common.collect.Maps;
import com.google.common.base.Function;

import java.util.Map;
import java.util.Date;
import java.util.Collection;

import org.apache.commons.lang.xwork.StringEscapeUtils;

import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;

/**
 */
public class SVNLogEntryWrapper implements LogEntry {
    private final SVNLogEntry entry;
    private final String revisionUrlFormat;
    private Collection<LogEntry> branchMergeRevisions;
    private String logMessage;
    private Collection<String> logMessageMatches;
    private Map<String, String> additionalFields = Maps.newHashMap();

    public SVNLogEntryWrapper(SVNLogEntry entry, String revisionUrlFormat) {
        this.entry = entry;
        this.revisionUrlFormat = revisionUrlFormat;
    }

    public long getRevision() {
        return entry.getRevision();
    }

    public String getRevisionUrl() {
        return String.format(revisionUrlFormat, entry.getRevision()); 
    }

    public String getAuthor() {
        return entry.getAuthor();
    }

    public String getLogMessage() {
        return logMessage != null ? logMessage : StringEscapeUtils.escapeHtml(entry.getMessage());
    }

    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }

    public Date getDate() {
        return entry.getDate();
    }

    @SuppressWarnings("unchecked")
    public Map<String, LogEntryPath> getPaths() {
        return Maps.transformValues(entry.getChangedPaths(), new Function<SVNLogEntryPath,  LogEntryPath>() {
            public LogEntryPath apply(SVNLogEntryPath path) {
                return new SVNLogEntryPathWrapper(path);
            }
        });
    }

    public Collection<LogEntry> getBranchMergeRevisions() {
        return branchMergeRevisions;
    }

    public void setBranchMergeRevisions(Collection<LogEntry> branchMergeRevisions) {
        this.branchMergeRevisions = branchMergeRevisions;
    }

    public void setLogMessageMatches(Collection<String> matches) {
        this.logMessageMatches = matches;
    }

    public Collection<String> getLogMessageMatches() {
        return logMessageMatches;
    }

    public String toString() {
        return entry.toString();
    }

    public Map<String, String> getAdditionalFields() {
        return additionalFields;
    }

    public String setAdditionalField(String fieldName, String fieldValue) {
        return additionalFields.put(fieldName, fieldValue);
    }

    public String getAdditionalField(String fieldName) {
        return additionalFields.get(fieldName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SVNLogEntryWrapper that = (SVNLogEntryWrapper)o;
        if (!entry.equals(that.entry)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return entry.hashCode();
    }
}
