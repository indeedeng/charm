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
