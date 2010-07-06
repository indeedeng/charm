package com.indeed.charm.svn;

import com.indeed.charm.model.LogEntry;
import com.indeed.charm.model.LogEntryPath;
import com.google.common.collect.Maps;
import com.google.common.base.Function;

import java.util.Map;
import java.util.Date;
import java.util.Collection;

import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;

/**
 */
public class SVNLogEntryWrapper implements LogEntry {
    private final SVNLogEntry entry;
    private final String revisionUrlFormat;
    private Collection<LogEntry> branchMergeRevisions;
    private String logMessage;

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
        return logMessage != null ? logMessage : entry.getMessage();
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

    public String toString() {
        return entry.toString();
    }
}
