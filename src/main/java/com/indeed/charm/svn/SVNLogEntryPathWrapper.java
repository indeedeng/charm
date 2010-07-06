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

    public String toString() {
        return path.toString();
    }
}
