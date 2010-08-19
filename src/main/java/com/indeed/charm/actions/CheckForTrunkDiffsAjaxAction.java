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
