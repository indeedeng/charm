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

package com.indeed.charm.actions;

import org.apache.log4j.Logger;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.indeed.charm.model.LogEntry;
import com.indeed.charm.VCSClient;
import com.indeed.charm.VCSException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collection;

/**
 */
public class LogTrunkSinceTagAction extends VCSActionSupport {
    private static Logger log = Logger.getLogger(LogTrunkSinceTagAction.class);

    private String tag;
    private String path;
    private List<LogEntry> logEntries;
    private Map<Long, String> logMessages;
    private Set<Long> warnRevisions = Sets.newHashSet();

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

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

    public Map<Long, String> getLogMessages() {
        return logMessages;
    }

    public void setLogMessages(Map<Long, String> logMessages) {
        this.logMessages = logMessages;
    }

    @Override
    public String execute() throws Exception {
        try {
            if (tag == null) {
                tag = vcsClient.listTags(project, 1, VCSClient.Ordering.REVERSE_AGE).get(0).getName();
            }
            if (path == null) {
                path = ".";
            }

            final DisplayLogVisitor logVisitor = new WarningLogVisitor(env.getTrunkWarnIfMissingPatterns());
            vcsClient.visitTrunkChangeLogSinceTag(logVisitor, getProject(), getTag(), 0, getPath());
            setLogEntries(ImmutableList.copyOf(logVisitor.getEntries().values()));
        } catch (VCSException e) {
            log.error("Failed to get trunk log since branch", e);
        }
        return SUCCESS;
    }

    public String getName() {
        return project + env.getTrunkPath();
    }

    public String getSince() {
        return "tag " + tag;
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
