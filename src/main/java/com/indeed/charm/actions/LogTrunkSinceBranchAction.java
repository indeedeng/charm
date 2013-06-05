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

import com.google.common.collect.*;
import com.indeed.charm.model.LogEntry;
import com.indeed.charm.VCSClient;
import com.indeed.charm.VCSException;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;

/**
 */
public class LogTrunkSinceBranchAction extends BaseBranchLogAction {
    private static Logger log = Logger.getLogger(LogTrunkSinceBranchAction.class);

    protected static final Pattern NUMERIC_REVISION_PATTERN = Pattern.compile("\\br?([1-9]\\d*)\\b");

    private Multimap<Long, LogEntry> possibleMerges;
    private Set<String> foundAdditionalFields;

    @Override
    public String execute() throws Exception {
        try {
            if (getBranchDate() == null) {
                setBranchDate(vcsClient.listBranches(project, 1, VCSClient.Ordering.REVERSE_BRANCH).get(0).getName());
            }
            if (getPath() == null) {
                setPath(".");
            }

            loadPossibleMerges();
            final ExtractIssueKeyVisitor issueKeyExtractor = new ExtractIssueKeyVisitor(env);
            final DisplayLogVisitor logVisitor = new WarningLogVisitor(env.getTrunkWarnIfMissingPatterns()) {
                @Override
                public void visit(LogEntry entry) {
                    issueKeyExtractor.visit(entry);
                    super.visit(entry);
                    entry.setBranchMergeRevisions(possibleMerges.get(entry.getRevision()));
                }
            };
            vcsClient.visitTrunkChangeLogSinceBranch(logVisitor, getProject(), getBranchDate(), 0, getPath());
            foundAdditionalFields = issueKeyExtractor.process();
            setLogEntries(ImmutableList.copyOf(logVisitor.getEntries().values()));
        } catch (VCSException e) {
            log.error("Failed to get trunk log since branch", e);
        }
        return SUCCESS;
    }

    protected void loadPossibleMerges() throws VCSException {
        final Multimap<Long, LogEntry> possibles = HashMultimap.create();
        final LogEntryVisitor visitor = new LogEntryVisitor() {
            public void visit(LogEntry entry) {
                final String message = entry.getLogMessage();
                Matcher matcher = NUMERIC_REVISION_PATTERN.matcher(message);
                while (matcher.find()) {
                    final long rev = Long.parseLong(matcher.group(1));
                    Collection<LogEntry> existing = possibles.get(rev);
                    if (existing == null || !existing.contains(entry)) {
                        possibles.put(rev, entry);
                    }
                }
            }
        };
        vcsClient.visitBranchChangeLog(visitor, getProject(), getBranchDate(), true, 0, getPath());
        this.possibleMerges = possibles;
    }

    public String getName() {
        return project + env.getTrunkPath();
    }

    public String getSince() {
        return "branch " + branchDate;
    }

    public boolean isShowMergeToBranchLink() {
        final String path = getPath();
        return path == null || path.equals(".");
    }

    public Set<String> getFoundAdditionalFields() {
        return foundAdditionalFields;
    }
}
