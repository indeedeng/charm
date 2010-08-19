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

import org.apache.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.indeed.charm.VCSClient;
import com.indeed.charm.VCSException;

/**
 */
public class LogBranchAction extends BaseBranchLogAction {
    private static Logger log = Logger.getLogger(LogBranchAction.class);

    private long revision;

    @Override
    public String execute() throws Exception {
        try {
            if (getBranchDate() == null) {
                setBranchDate(vcsClient.listBranches(project, 1, VCSClient.Ordering.REVERSE_BRANCH).get(0).getName());
            }
            if (getPath() == null) {
                setPath(".");
            }

            setRevision(vcsClient.getBranchStartRevision(project, branchDate));
            final DisplayLogVisitor logVisitor = new WarningLogVisitor(env.getBranchWarnIfMissingPatterns());
            vcsClient.visitBranchChangeLog(logVisitor, getProject(), getBranchDate(), 0, getPath());
            setLogEntries(ImmutableList.copyOf(logVisitor.getEntries().values()));
        } catch (VCSException e) {
            log.error("Failed to get branch log", e);
        }
        return SUCCESS;
    }

    public long getRevision() {
        return revision;
    }

    public void setRevision(long revision) {
        this.revision = revision;
    }

    public String getName() {
        return project + env.getBranchPath() + branchDate;
    }

    public String getSince() {
        return "revision " + revision;
    }
}
