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
