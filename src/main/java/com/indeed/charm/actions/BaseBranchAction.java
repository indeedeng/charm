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

import java.text.MessageFormat;

/**
 */
public abstract class BaseBranchAction extends VCSActionSupport {
    protected String branchDate;
    protected String branchDeployLink;

    public String getBranchDate() {
        return branchDate;
    }

    public void setBranchDate(String branchDate) {
        this.branchDate = branchDate;
    }

    public String getBranchDeployLink() {
        if (branchDeployLink == null) {
            final String deployLinkTemplate = env.getDeployLink();
            if (deployLinkTemplate != null) {
                final MessageFormat format = new MessageFormat(deployLinkTemplate);
                String project = getProject();
                String deployName = env.getDeployName(project);
                if (deployName != null) {
                    project = deployName;
                }
                branchDeployLink = format.format(new Object[] { project, getBranchDate() });
            }
        }
        return branchDeployLink;
    }

    public void setBranchDeployLink(String branchDeployLink) {
        this.branchDeployLink = branchDeployLink;
    }
}
