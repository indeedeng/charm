package com.indeed.charm.actions;

import com.indeed.charm.model.LogEntry;
import com.indeed.charm.ReleaseEnvironment;
import com.google.common.collect.Ordering;
import com.google.common.collect.Maps;

import java.util.TreeMap;

public class DisplayLogVisitor implements LogEntryVisitor {
    private final TreeMap<Long, LogEntry> entries = Maps.newTreeMap(Ordering.natural().reverse());
    private final ReleaseEnvironment env;

    public DisplayLogVisitor(ReleaseEnvironment env) {
        this.env = env;
    }

    public void visit(LogEntry entry) {
        entry.setLogMessage(env.linkify(entry.getLogMessage()));
        entries.put(entry.getRevision(), entry);
    }

    public TreeMap<Long, LogEntry> getEntries() {
        return entries;
    }
}