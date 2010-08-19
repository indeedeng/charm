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
