package com.indeed.charm.svn;

import org.tmatesoft.svn.core.wc.ISVNDiffStatusHandler;
import org.tmatesoft.svn.core.wc.SVNDiffStatus;
import org.tmatesoft.svn.core.SVNException;
import com.indeed.charm.actions.DiffStatusVisitor;

/**
 */
public class SVNDiffStatusVisitor implements ISVNDiffStatusHandler {
    private DiffStatusVisitor visitor;


    public SVNDiffStatusVisitor(DiffStatusVisitor visitor) {
        this.visitor = visitor;
    }

    public void handleDiffStatus(SVNDiffStatus svnDiffStatus) throws SVNException {
        visitor.visit(new SVNDiffStatusWrapper(svnDiffStatus));
    }
}
