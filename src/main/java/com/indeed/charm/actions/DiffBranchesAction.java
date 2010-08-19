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

import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableList;
import com.indeed.charm.model.DiffStatus;
import com.indeed.charm.VCSException;
import org.apache.log4j.Logger;

import java.util.List;

/**
 */
public class DiffBranchesAction extends VCSActionSupport {
    private static final Logger log = Logger.getLogger(DiffBranchesAction.class);

    private String branchDate1;
    private String branchDate2;

    private List<DiffStatus> diffs;

    @Override
    public String execute() {
        try {
            final List<DiffStatus> tmp = Lists.newArrayList();
            vcsClient.visitBranchDiffStatus(
                    new DiffStatusVisitor() {
                        public boolean visit(DiffStatus diffStatus) {
                            if (!"none".equals(diffStatus.getModificationType())) {
                                tmp.add(diffStatus);
                            }
                            return true;
                        }
                    }, getProject(), getBranchDate1(), getBranchDate2());
            setDiffs(ImmutableList.copyOf(tmp));
        } catch (VCSException e) {
            log.error("Failed to find branch diffs", e);
        }

        return SUCCESS;
    }

    public String getBranchDate1() {
        return branchDate1;
    }

    public void setBranchDate1(String branchDate1) {
        this.branchDate1 = branchDate1;
    }

    public String getBranchDate2() {
        return branchDate2;
    }

    public void setBranchDate2(String branchDate2) {
        this.branchDate2 = branchDate2;
    }

    public void setDiffs(List<DiffStatus> diffs) {
        this.diffs = diffs;
    }

    public List<DiffStatus> getDiffs() {
        return diffs;
    }

    public String getPath1() {
        return project + env.getBranchPath() + branchDate1;
    }

    public String getPath2() {
        return project + env.getBranchPath() + branchDate2;
    }
}
