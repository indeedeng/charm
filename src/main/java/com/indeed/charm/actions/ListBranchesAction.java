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
