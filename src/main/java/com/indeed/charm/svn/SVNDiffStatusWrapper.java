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

import org.tmatesoft.svn.core.wc.SVNDiffStatus;
import com.indeed.charm.model.DiffStatus;

import java.io.File;

/**
 */
public class SVNDiffStatusWrapper implements DiffStatus {
    private final SVNDiffStatus status;


    public SVNDiffStatusWrapper(SVNDiffStatus status) {
        this.status = status;
    }

    public String getKind() {
        return status.getKind().toString();
    }

    public String getModificationType() {
        return status.getModificationType().toString();
    }

    public String getPath() {
        return status.getPath();
    }

    public String getUrl() {
        return status.getURL().toString();
    }

    public File getFile() {
        return status.getFile();
    }

    public String toString() {
        return status.toString();
    }
}
