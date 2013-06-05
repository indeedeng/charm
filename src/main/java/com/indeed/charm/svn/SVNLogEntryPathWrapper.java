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

import com.indeed.charm.model.LogEntryPath;
import org.tmatesoft.svn.core.SVNLogEntryPath;

/**
 */
public class SVNLogEntryPathWrapper implements LogEntryPath {
    private final SVNLogEntryPath path;

    public SVNLogEntryPathWrapper(SVNLogEntryPath path) {
        this.path = path;
    }

    public String getType() {
        return "" + path.getType();
    }

    public String getPath() {
        return path.getPath();
    }

    public String getCopyPath() {
        return path.getCopyPath();
    }

    public long getCopyRevision() {
        return path.getCopyRevision();
    }

    public String toString() {
        return path.toString();
    }
}
