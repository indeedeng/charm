package com.indeed.charm.actions;

import com.indeed.charm.VCSClient;
import com.indeed.charm.model.DirEntry;

import java.util.List;

/**
 */
public class ListBranchesAction extends VCSActionSupport {

    private List<DirEntry> branches;
    private boolean trunkDifferent;

    @Override
    public String execute() throws Exception {
        // TODO: make sort style configurable in charm.properties
        branches = vcsClient.listBranches(project, 20, VCSClient.Ordering.REVERSE_BRANCH);
        if (branches.size() > 0) {
            trunkDifferent = vcsClient.hasTrunkCommitsSinceBranch(getProject(), branches.get(0).getName());
        }
        return SUCCESS;
    }

    public List<DirEntry> getBranches() {
        return branches;
    }

    public boolean isTrunkDifferent() {
        return trunkDifferent;
    }
}
