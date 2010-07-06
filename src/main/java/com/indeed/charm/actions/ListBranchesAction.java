package com.indeed.charm.actions;

import com.indeed.charm.VCSClient;

import java.util.List;

/**
 */
public class ListBranchesAction extends VCSActionSupport {

    private List<String> branches;

    @Override
    public String execute() throws Exception {
        branches = vcsClient.listBranches(project, 20, VCSClient.Ordering.REVERSE_BRANCH);
        return SUCCESS;
    }

    public List<String> getBranches() {
        return branches;
    }

    public void setBranches(List<String> branches) {
        this.branches = branches;
    }
}
