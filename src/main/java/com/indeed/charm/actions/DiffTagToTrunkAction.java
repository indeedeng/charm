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

import java.util.List;
import java.util.TreeMap;

import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableList;
import com.indeed.charm.VCSClient;
import com.indeed.charm.VCSException;
import com.indeed.charm.model.DiffStatus;

/**
 */
public class DiffTagToTrunkAction extends VCSActionSupport {
    private static final Logger log = Logger.getLogger(DiffTagToTrunkAction.class);

    private String tag;
    private List<DiffStatus> diffs;

    @Override
    public String execute() throws Exception {
        try {
            if (getTag() == null) {
                setTag(vcsClient.listTags(project, 1, VCSClient.Ordering.REVERSE_AGE).get(0).getName());
            }
            final TreeMap<String, DiffStatus> tmp = Maps.newTreeMap();
            vcsClient.visitTagToTrunkDiffStatus(new DiffStatusVisitor() {
                        public boolean visit(DiffStatus diffStatus) {
                            if (!"none".equals(diffStatus.getModificationType())) {
                                tmp.put(diffStatus.getPath(), diffStatus);
                            }
                            return true;
                        }
                    }, getProject(), getTag());
            setDiffs(ImmutableList.copyOf(tmp.values()));
        } catch (VCSException e) {
            log.error("Failed to find tag to trunk diffs", e);
        }

        return SUCCESS;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public List<DiffStatus> getDiffs() {
        return diffs;
    }

    public void setDiffs(List<DiffStatus> diffs) {
        this.diffs = diffs;
    }

    public String getPath1() {
        return project + env.getTagPath() + tag;
    }

    public String getPath2() {
        return project + env.getTrunkPath();
    }

    public boolean isIncludeTrunkSinceTag() {
        return true;
    }
}
