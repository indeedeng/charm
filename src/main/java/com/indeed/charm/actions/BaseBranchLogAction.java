package com.indeed.charm.actions;

import java.util.List;

import com.indeed.charm.model.LogEntry;

/**
 */
public abstract class BaseBranchLogAction extends BaseBranchAction {
    private String path;
    private List<LogEntry> logEntries;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<LogEntry> getLogEntries() {
        return logEntries;
    }

    public void setLogEntries(List<LogEntry> logEntries) {
        this.logEntries = logEntries;
    }
}
