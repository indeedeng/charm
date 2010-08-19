package com.indeed.charm.actions;

import com.indeed.charm.ivy.IvyLoader;
import com.indeed.charm.ivy.IvyDependency;

import java.util.List;

/**
 */
public class ShowIvyDependenciesAction extends VCSActionSupport {
    private String branchDate;
    private String tag;
    private boolean showAll;
    private List<IvyDependency> dependencies;

    @Override
    public String execute() throws Exception {
        final IvyLoader loader = new IvyLoader(vcsClient, env);
        if (tag != null) {
            final IvyLoader.IvyReleaseResolver resolver = new IvyLoader.IvyReleaseResolver(env.getIvyOrg(), project, tag, env);
            dependencies = loader.loadDependencies(resolver);
        } else if (branchDate != null && !"trunk".equals(branchDate)) {
            final IvyLoader.BranchResolver resolver = new IvyLoader.BranchResolver(project, branchDate, vcsClient, env);
            dependencies = loader.loadDependencies(resolver);
        } else {
            final IvyLoader.TrunkResolver resolver = new IvyLoader.TrunkResolver(project, vcsClient, env);
            dependencies = loader.loadDependencies(resolver);
        }
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

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
