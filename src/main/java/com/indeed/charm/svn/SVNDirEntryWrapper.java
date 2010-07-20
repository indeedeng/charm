package com.indeed.charm.svn;

import org.tmatesoft.svn.core.SVNDirEntry;
import com.indeed.charm.model.DirEntry;

import java.util.Date;

/**
 */
public class SVNDirEntryWrapper implements DirEntry {
    private final SVNDirEntry entry;


    public SVNDirEntryWrapper(SVNDirEntry entry) {
        this.entry = entry;
    }

    public String getName() {
        return entry.getName();
    }

    public String getAuthor() {
        return entry.getAuthor();
    }

    public Date getDate() {
        return entry.getDate();
    }

    public long getRevision() {
        return entry.getRevision();
    }

    public long getSize() {
        return entry.getSize();
    }

    @Override
    public String toString() {
        return entry.toString();
    }
}
