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
