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

import org.tmatesoft.svn.core.wc.ISVNDiffStatusHandler;
import org.tmatesoft.svn.core.wc.SVNDiffStatus;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNErrorCode;
import com.indeed.charm.actions.DiffStatusVisitor;

/**
 */
public class SVNDiffStatusVisitor implements ISVNDiffStatusHandler {
    private DiffStatusVisitor visitor;


    public SVNDiffStatusVisitor(DiffStatusVisitor visitor) {
        this.visitor = visitor;
    }

    public void handleDiffStatus(SVNDiffStatus svnDiffStatus) throws SVNException {
        if (!visitor.visit(new SVNDiffStatusWrapper(svnDiffStatus))) {
            throw new SVNException(SVNErrorMessage.create(SVNErrorCode.CANCELLED));
        }
    }
}
