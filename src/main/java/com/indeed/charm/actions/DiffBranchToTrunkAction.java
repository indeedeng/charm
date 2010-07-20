package com.indeed.charm.actions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.indeed.charm.VCSClient;
import com.indeed.charm.VCSException;
import com.indeed.charm.model.DiffStatus;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.TreeMap;

/**
 */
public class DiffBranchToTrunkAction extends VCSActionSupport {
    private static final Logger log = Logger.getLogger(DiffBranchToTrunkAction.class);

    private String branchDate;
    private List<DiffStatus> diffs;

    @Override
    public String execute() throws Exception {
        try {
            if (getBranchDate() == null) {
                setBranchDate(vcsClient.listBranches(project, 1, VCSClient.Ordering.REVERSE_BRANCH).get(0));
            }
            final TreeMap<String, DiffStatus> tmp = Maps.newTreeMap();
            vcsClient.visitBranchToTrunkDiffStatus(
                    new DiffStatusVisitor() {
                        public boolean visit(DiffStatus diffStatus) {
                            if (!"none".equals( diffStatus.getModificationType())) {
                                tmp.put(diffStatus.getPath(),  diffStatus);
                            }
                            return true;
                        }
                    }, getProject(), getBranchDate());
            setDiffs(ImmutableList.copyOf(tmp.values()));
        } catch (VCSException e) {
            log.error("Failed to find branch to trunk diffs", e);
        }

        return SUCCESS;
    }

    public String getBranchDate() {
        return branchDate;
    }

    public void setBranchDate(String branchDate) {
        this.branchDate = branchDate;
    }

    public List<DiffStatus> getDiffs() {
        return diffs;
    }

    public void setDiffs(List<DiffStatus> diffs) {
        this.diffs = diffs;
    }

    public String getPath1() {
        return project + env.getBranchPath() + branchDate;
    }

    public String getPath2() {
        return project + env.getTrunkPath();
    }

    public boolean isIncludeTrunkSinceBranch() {
        return true;
    }
}
