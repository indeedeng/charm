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
import com.indeed.charm.VCSException;
import com.indeed.charm.model.DiffStatus;
import com.indeed.charm.model.DirEntry;

import java.util.List;

import org.apache.log4j.Logger;

/**
 */
public class ListTagsAction extends VCSActionSupport {
    private List<DirEntry> tags;
    private boolean trunkDifferent;

    @Override
    public String execute() throws Exception {
        // TODO: make sort style configurable in charm.properties
        tags = vcsClient.listTags(project, 20, VCSClient.Ordering.REVERSE_AGE);
        if (tags.size() > 0) {
            trunkDifferent = vcsClient.hasTrunkCommitsSinceTag(getProject(), tags.get(0).getName());
        }
        return SUCCESS;
    }

    public List<DirEntry> getTags() {
        return tags;
    }

    public boolean isTrunkDifferent() {
        return trunkDifferent;
    }
}
