package com.indeed.charm.actions;

import org.apache.log4j.Logger;

import com.google.common.collect.*;
import com.indeed.charm.model.LogEntry;
import com.indeed.charm.VCSClient;
import com.indeed.charm.VCSException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 */
public class LogTrunkSinceBranchAction extends BaseBranchLogAction {
    private static Logger log = Logger.getLogger(LogTrunkSinceBranchAction.class);

    protected static final Pattern NUMERIC_REVISION_PATTERN = Pattern.compile("\\br?([1-9]\\d*)\\b");

    private Multimap<Long, LogEntry> possibleMerges;

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
            final DisplayLogVisitor logVisitor = new DisplayLogVisitor(env) {
                @Override
                public void visit(LogEntry entry) {
                    super.visit(entry);
                    entry.setBranchMergeRevisions(possibleMerges.get(entry.getRevision()));
                }
            };
            vcsClient.visitTrunkChangeLogSinceBranch(logVisitor, getProject(), getBranchDate(), 0, getPath());
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
                    possibles.put(Long.parseLong(matcher.group(1)), entry);
                }
            }
        };
        vcsClient.visitBranchChangeLog(visitor, getProject(), getBranchDate(), 0, getPath());
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
}
