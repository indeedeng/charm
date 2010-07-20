package com.indeed.charm.actions;

import com.indeed.charm.ivy.IvyLoader;
import com.indeed.charm.ivy.IvyDependency;

import java.util.List;

/**
 */
public class ShowIvyDependenciesAction extends VCSActionSupport {
    private String branchDate;
    private boolean showAll;
    private List<IvyDependency> dependencies;

    @Override
    public String execute() throws Exception {
        String subPath = branchDate != null ? env.getBranchPath() + branchDate + "/" : env.getTrunkPath();
        IvyLoader loader = new IvyLoader(vcsClient, project, subPath, env.getIvyOrg(), env.getIvyFileName(), env.getIvyProperties());
        dependencies = loader.loadDependencies();
        return SUCCESS;
    }

    public String getBranchDate() {
        return branchDate;
    }

    public void setBranchDate(String branchDate) {
        this.branchDate = branchDate;
    }

    public List<IvyDependency> getDependencies() {
        return dependencies;
    }

    public boolean isShowAll() {
        return showAll;
    }

    public void setShowAll(boolean showAll) {
        this.showAll = showAll;
    }
}
