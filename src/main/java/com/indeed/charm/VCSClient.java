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

package com.indeed.charm;

import com.indeed.charm.actions.LogEntryVisitor;
import com.indeed.charm.actions.DiffStatusVisitor;
import com.indeed.charm.model.LogEntry;
import com.indeed.charm.model.CommitInfo;
import com.indeed.charm.model.DirEntry;

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

    boolean hasTrunkCommitsSinceTag(String project, String tag) throws VCSException;

    void visitTagToTagDiffStatus(DiffStatusVisitor visitor, String project, String tag1, String tag2) throws VCSException;

    void visitBranchToTrunkDiffStatus(DiffStatusVisitor visitor, String project, String branchDate) throws VCSException;

    boolean hasTrunkCommitsSinceBranch(String project, String branchDate) throws VCSException;

    void visitTagDiffStatus(DiffStatusVisitor visitor, String project, String version1, String version2) throws VCSException;

    void visitTrunkChangeLog(LogEntryVisitor visitor, String project, int limit, String... paths) throws VCSException;

    void visitBranchChangeLog(LogEntryVisitor visitor, String project, String branchDate, int limit, String... paths) throws VCSException;

    void visitBranchChangeLog(LogEntryVisitor visitor, String project, String branchDate, boolean extendToBranchPointUsingComment, int limit, String... paths) throws VCSException;

    void visitTagChangeLog(LogEntryVisitor visitor, String project, String tag, int limit, String... paths) throws VCSException;

    long getBranchStartRevision(String project, String branchDate) throws VCSException;

    long getBranchStartRevision(String project, String branchDate, boolean extendToBranchPointFromComment) throws VCSException;

    void visitTrunkChangeLogSinceBranch(LogEntryVisitor visitor, String project, String branchDate, int limit, String... paths) throws VCSException;

    LogEntry getTagFirstLogEntry(String project, String tag) throws VCSException;

    long getTagFirstRevision(String project, String tag) throws VCSException;

    void visitTrunkChangeLogSinceTag(LogEntryVisitor visitor, String project, String tag, int limit, String... paths) throws VCSException;

    List<DirEntry> listBranches(String project, int limit, Ordering ordering) throws VCSException;

    List<DirEntry> listTags(String project, int limit, Ordering ordering) throws VCSException;

    List<DirEntry> listDir(String dir, Ordering ordering) throws VCSException;

    boolean hasFilesSince(String path, Date earliest);

    boolean checkExistsInHead(String path) throws VCSException;

    long checkoutBranch(String project, String branchDate, File workingDir) throws VCSException, IOException;

    CommitInfo commit(File file, String message) throws VCSException;

    CommitInfo mergeToBranch(String project, long revision, String branchDate, String message, File workingDir) throws VCSException, IOException;

    LogEntry getTrunkLogEntry(String project, long revision) throws VCSException;

    LogEntry getBranchLogEntry(String project, String branchDate, long revision) throws VCSException;

    long getFile(String path, long revision, ByteArrayOutputStream outputStream) throws VCSException;

    long getLatestRevision(String path) throws VCSException;

}
