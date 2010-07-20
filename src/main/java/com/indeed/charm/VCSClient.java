package com.indeed.charm;

import com.indeed.charm.actions.LogEntryVisitor;
import com.indeed.charm.actions.DiffStatusVisitor;
import com.indeed.charm.model.LogEntry;
import com.indeed.charm.model.CommitInfo;

import java.util.List;
import java.util.Date;
import java.io.File;
import java.io.IOException;
import java.io.ByteArrayOutputStream;

/**
 */
public interface VCSClient {
    public enum Ordering {
        NORMAL,
        REVERSE_AGE,
        REVERSE_VERSION,
        REVERSE_BRANCH
    }

    void visitBranchDiffStatus(DiffStatusVisitor visitor, String project, String date1, String date2) throws VCSException;

    void visitTagToTrunkDiffStatus(DiffStatusVisitor visitor, String project, String tag) throws VCSException;

    void visitTagToTagDiffStatus(DiffStatusVisitor visitor, String project, String tag1, String tag2) throws VCSException;

    void visitBranchToTrunkDiffStatus(DiffStatusVisitor visitor, String project, String branchDate) throws VCSException;

    void visitTagDiffStatus(DiffStatusVisitor visitor, String project, String version1, String version2) throws VCSException;

    void visitTrunkChangeLog(LogEntryVisitor visitor, String project, int limit, String... paths) throws VCSException;

    void visitBranchChangeLog(LogEntryVisitor visitor, String project, String branchDate, int limit, String... paths) throws VCSException;

    void visitTagChangeLog(LogEntryVisitor visitor, String project, String tag, int limit, String... paths) throws VCSException;

    long getBranchStartRevision(String project, String branchDate) throws VCSException;

    void visitTrunkChangeLogSinceBranch(LogEntryVisitor visitor, String project, String branchDate, int limit, String... paths) throws VCSException;

    long getTagFirstRevision(String project, String tag) throws VCSException;

    void visitTrunkChangeLogSinceTag(LogEntryVisitor visitor, String project, String tag, int limit, String... paths) throws VCSException;

    List<String> listBranches(String project, int limit, Ordering ordering) throws VCSException;

    List<String> listTags(String project, int limit, Ordering ordering) throws VCSException;

    List<String> listDir(String dir, Ordering ordering) throws VCSException;

    boolean hasFilesSince(String path, Date earliest);

    boolean checkExistsInHead(String path) throws VCSException;

    List<String> getTags(String project, int limit, Ordering ordering) throws VCSException;

    long checkoutBranch(String project, String branchDate, File workingDir) throws VCSException, IOException;

    CommitInfo mergeToBranch(String project, long revision, String branchDate, String messagePrefix, File workingDir) throws VCSException, IOException;

    LogEntry getTrunkLogEntry(String project, long revision) throws VCSException;

    LogEntry getBranchLogEntry(String project, String branchDate, long revision) throws VCSException;

    void getFile(String path, long revision, ByteArrayOutputStream outputStream) throws VCSException;

}
