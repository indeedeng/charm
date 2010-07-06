package com.indeed.charm.actions;

import org.apache.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.indeed.charm.VCSClient;
import com.indeed.charm.VCSException;

/**
 */
public class LogBranchAction extends BaseBranchLogAction {
    private static Logger log = Logger.getLogger(LogBranchAction.class);

    private long revision;

    @Override
    public String execute() throws Exception {
        try {
            if (getBranchDate() == null) {
                setBranchDate(vcsClient.listBranches(project, 1, VCSClient.Ordering.REVERSE_BRANCH).get(0));
            }
            if (getPath() == null) {
                setPath(".");
            }

            setRevision(vcsClient.getBranchStartRevision(project, branchDate));

            final DisplayLogVisitor logVisitor = new DisplayLogVisitor(env);
            vcsClient.visitBranchChangeLog(logVisitor, getProject(), getBranchDate(), 0, getPath());
            setLogEntries(ImmutableList.copyOf(logVisitor.getEntries().values()));
        } catch (VCSException e) {
            log.error("Failed to get branch log", e);
        }
        return SUCCESS;
    }

    public long getRevision() {
        return revision;
    }

    public void setRevision(long revision) {
        this.revision = revision;
    }

    public String getName() {
        return project + env.getBranchPath() + branchDate;
    }

    public String getSince() {
        return "revision " + revision;
    }
}
