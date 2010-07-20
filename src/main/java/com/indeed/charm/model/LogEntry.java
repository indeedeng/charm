package com.indeed.charm.model;

import java.util.Map;
import java.util.Date;
import java.util.Collection;

/**
 */
public interface LogEntry {
    public long getRevision();
    public String getRevisionUrl();
    public String getAuthor();
    public String getLogMessage();
    public Date getDate();
    public Map<String, LogEntryPath> getPaths();

    public void setLogMessage(String logMessage);
    public Collection<LogEntry> getBranchMergeRevisions();
    public void setBranchMergeRevisions(Collection<LogEntry> revisions);

    public void setLogMessageMatches(Collection<String> matches);
    public Collection<String> getLogMessageMatches();
}
