package com.indeed.charm.actions;

import java.util.List;
import java.util.Set;
import java.util.Collection;

import com.indeed.charm.model.LogEntry;
import com.google.common.collect.Sets;

/**
 */
public abstract class BaseBranchLogAction extends BaseBranchAction {
    private String path;
    private List<LogEntry> logEntries;
    private Set<Long> warnRevisions = Sets.newHashSet();

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

    public Set<Long> getWarnRevisions() {
        return warnRevisions;
    }

    protected void addWarnRevision(long rev) {
        this.warnRevisions.add(rev);
    }

    protected class WarningLogVisitor extends DisplayLogVisitor {
        private final Set<String> warnIfMissing;

        protected WarningLogVisitor(Set<String> warnIfMissing) {
            super(env);
            this.warnIfMissing = warnIfMissing;
        }

        @Override
        public void visit(LogEntry entry) {
            super.visit(entry);

            final Collection<String> logMatches = entry.getLogMessageMatches();
            for (String possibleWarn : warnIfMissing) {
                if (!logMatches.contains(possibleWarn)) {
                    addWarnRevision(entry.getRevision());
                }
            }
        }
    }
}
