package com.indeed.charm.actions;

/**
 */
public abstract class BaseBranchAction extends VCSActionSupport {
    protected String branchDate;

    public String getBranchDate() {
        return branchDate;
    }

    public void setBranchDate(String branchDate) {
        this.branchDate = branchDate;
    }
}
