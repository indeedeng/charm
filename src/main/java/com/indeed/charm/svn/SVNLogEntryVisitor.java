package com.indeed.charm.svn;

import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.SVNLogEntry;
import com.indeed.charm.actions.LogEntryVisitor;

/**
 */
public class SVNLogEntryVisitor implements ISVNLogEntryHandler {
    private final LogEntryVisitor visitor;
    private final String revisionUrlFormat;

    public SVNLogEntryVisitor(LogEntryVisitor visitor, String revisionUrlFormat) {
        this.visitor = visitor;
        this.revisionUrlFormat = revisionUrlFormat;
    }

    public void handleLogEntry(SVNLogEntry svnLogEntry) throws SVNException {
        visitor.visit(new SVNLogEntryWrapper(svnLogEntry, revisionUrlFormat));
    }
}
