package com.indeed.charm.actions;

import com.indeed.charm.model.LogEntry;

/**
 */
public interface LogEntryVisitor {
    public void visit(LogEntry entry);
}
