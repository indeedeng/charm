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

package com.indeed.charm.svn;

import org.tmatesoft.svn.core.SVNCommitInfo;

import java.util.Date;

import com.indeed.charm.model.CommitInfo;

/**
 */
public class SVNCommitInfoWrapper implements CommitInfo {
    private SVNCommitInfo info;

    public SVNCommitInfoWrapper(SVNCommitInfo info) {
        this.info = info;
    }

    public long getNewRevision() {
        return info.getNewRevision();
    }

    public String getAuthor() {
        return info.getAuthor();
    }

    public Date getDate() {
        return info.getDate();
    }

    public String getErrorMessage() {
        return info.getErrorMessage().toString();
    }

    public String toString() {
        return info.toString();
    }
}
