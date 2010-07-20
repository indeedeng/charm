package com.indeed.charm.actions;

/**
 */
public class CheckForTrunkDiffsAjaxAction extends VCSActionSupport {
    private String tag;
    private String branchDate;

    private boolean trunkDifferent;

    @Override
    public String execute() throws Exception {
        if (tag != null) {
            trunkDifferent = vcsClient.hasTrunkCommitsSinceTag(getProject(), tag);
        } else if (branchDate != null) {
            trunkDifferent = vcsClient.hasTrunkCommitsSinceBranch(getProject(), branchDate);
        }
        return SUCCESS;
    }

    @Override
    // duplicated for JSON
    public String getProject() {
        return super.getProject();
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getBranchDate() {
        return branchDate;
    }

    public void setBranchDate(String branchDate) {
        this.branchDate = branchDate;
    }

    public boolean isTrunkDifferent() {
        return trunkDifferent;
    }
}
