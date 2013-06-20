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

import com.google.common.collect.ImmutableMap;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNAuthenticationManager;
import org.tmatesoft.svn.core.wc.*;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.util.*;

import java.util.*;
import java.io.File;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Maps;
import com.indeed.charm.ReleaseEnvironment;
import com.indeed.charm.VCSClient;
import com.indeed.charm.VCSException;
import com.indeed.charm.model.LogEntry;
import com.indeed.charm.model.LogEntryPath;
import com.indeed.charm.model.CommitInfo;
import com.indeed.charm.model.DiffStatus;
import com.indeed.charm.model.DirEntry;
import com.indeed.charm.actions.LogEntryVisitor;
import com.indeed.charm.actions.DiffStatusVisitor;

/**
 */
public class SubversionClient implements VCSClient {
    private final ReleaseEnvironment env;
    private final String revisionUrlFormat;

    private final ISVNAuthenticationManager authManager;
    private final SVNClientManager clientManager;
    private final SVNRepository repo;

    private static Logger logger = Logger.getLogger(SubversionClient.class);

    private static class CustomSVNKitLogger extends SVNDebugLogAdapter {
        private static Level mapLevel(java.util.logging.Level level) {
            if (level.intValue() == java.util.logging.Level.OFF.intValue()) {
                return Level.OFF;
            } else if (level.intValue() == java.util.logging.Level.SEVERE.intValue()) {
                return Level.ERROR;
            } else if (level.intValue() == java.util.logging.Level.WARNING.intValue()) {
                return Level.WARN;
            } else if (level.intValue() == java.util.logging.Level.INFO.intValue()) {
                return Level.INFO;
            }
            return Level.DEBUG;
        }

        @Override
        public void log(SVNLogType logType, Throwable th, java.util.logging.Level logLevel) {
            logger.log(mapLevel(logLevel), "" + logType.getShortName(), th);
        }

        @Override
        public void log(SVNLogType logType, String message, java.util.logging.Level logLevel) {
            logger.log(mapLevel(logLevel), "" + logType.getShortName() + ": " + message);
        }

        @Override
        public void log(SVNLogType logType, String message, byte[] data) {
            logger.log(Level.DEBUG, "" + logType.getShortName() + ": " + message + " [data length " + data.length + "]");
        }
    }

    static {
        SVNDebugLog.setDefaultLog(new CustomSVNKitLogger());

        // TODO: how to configure this in charm.properties
        DAVRepositoryFactory.setup();
        SVNRepositoryFactoryImpl.setup();
    }

    public SubversionClient(ReleaseEnvironment env) throws VCSException {
        this.env = env;
        this.revisionUrlFormat = env.getRevisionUrlFormat();

        try {
            SVNURL url = SVNURL.parseURIDecoded(env.getRootUrl());
            repo = SVNRepositoryFactory.create(url, null);
            if (url.getProtocol().contains("ssh")) {
                authManager = SVNWCUtil.createDefaultAuthenticationManager(env.getTempDir(),
                        env.getSshUser(),
                        env.getSshPassword(),
                        env.getSshKeyfile(),
                        env.getSshPassphrase(),
                        false);
            } else {
                authManager = new BasicAuthenticationManager(env.getUser(), env.getPassword());
            }
            repo.setAuthenticationManager(authManager);
            clientManager = SVNClientManager.newInstance(null, authManager);
        } catch (SVNException e) {
            throw new VCSException(e);
        }
    }

    public SubversionClient(ReleaseEnvironment env, String user, String password) throws VCSException {
        this.env = env;
        this.revisionUrlFormat = env.getRevisionUrlFormat();

        try {
            SVNURL url = SVNURL.parseURIDecoded(env.getRootUrl());
            repo = SVNRepositoryFactory.create(url, null);
            authManager = new BasicAuthenticationManager(user, password);
            repo.setAuthenticationManager(authManager);
            clientManager = SVNClientManager.newInstance(null, authManager);
        } catch (SVNException e) {
            throw new VCSException(e);
        }
    }

    public void visitBranchDiffStatus(DiffStatusVisitor visitor, String project, String date1, String date2) throws VCSException {
        try {
            SVNDiffClient differ = new SVNDiffClient(authManager, null);
            differ.doDiffStatus(SVNURL.parseURIDecoded(env.getRootUrl() + project + env.getBranchPath() + date1),
                    SVNRevision.HEAD,
                    SVNURL.parseURIDecoded(env.getRootUrl() + project + env.getBranchPath() + date2),
                    SVNRevision.HEAD,
                    SVNDepth.INFINITY,
                    false,
                    new SVNDiffStatusVisitor(visitor));
        } catch (SVNException e) {
            if (e.getErrorMessage().getErrorCode() != SVNErrorCode.CANCELLED) {
                throw new VCSException(e);
            }
        }
    }

    public void visitTagToTrunkDiffStatus(DiffStatusVisitor visitor, String project, String tag) throws VCSException {
        try {
            SVNDiffClient differ = new SVNDiffClient(authManager, null);
            differ.doDiffStatus(SVNURL.parseURIDecoded(env.getRootUrl() + project + env.getTagPath() + tag),
                    SVNRevision.HEAD,
                    SVNURL.parseURIDecoded(env.getRootUrl() + project + env.getTrunkPath()),
                    SVNRevision.HEAD,
                    SVNDepth.INFINITY,
                    false,
                    new SVNDiffStatusVisitor(visitor));
        } catch (SVNException e) {
            if (e.getErrorMessage().getErrorCode() != SVNErrorCode.CANCELLED) {
                throw new VCSException(e);
            }
        }
    }

    public boolean hasTrunkCommitsSinceTag(String project, String tag) throws VCSException {
        LogEntry entry = getTagFirstLogEntry(project, tag);
        Map<String,LogEntryPath> paths = entry.getPaths();
        for (LogEntryPath path : paths.values()) {
            if (path.getCopyPath() == null) {
                // skip parts of the commit that don't have a copyfrom-path
                continue;
            } else if (!path.getPath().startsWith("/" + project)) {
                // skip parts of the commit that are in other projects
                continue;
            } else if (!path.getCopyPath().endsWith("trunk")) {
                throw new VCSException(new Exception("Not copied from trunk: " + path));
            } else {
                long taggedFrom = path.getCopyRevision();
                long latest = getLatestRevision(path.getCopyPath());
                return latest > taggedFrom;
            }
        }
        throw new VCSException(new Exception("No copyfrom-path found for " + entry));
    }

    public void visitTagToTagDiffStatus(DiffStatusVisitor visitor, String project, String tag1, String tag2) throws VCSException {
        try {
            SVNDiffClient differ = new SVNDiffClient(authManager, null);
            differ.doDiffStatus(SVNURL.parseURIDecoded(env.getRootUrl() + project + env.getTagPath() + tag1),
                    SVNRevision.HEAD,
                    SVNURL.parseURIDecoded(env.getRootUrl() + project + env.getTagPath() + tag2),
                    SVNRevision.HEAD,
                    SVNDepth.INFINITY,
                    false,
                    new SVNDiffStatusVisitor(visitor));
        } catch (SVNException e) {
            if (e.getErrorMessage().getErrorCode() != SVNErrorCode.CANCELLED) {
                throw new VCSException(e);
            }
        }
    }

    public void visitBranchToTrunkDiffStatus(DiffStatusVisitor visitor, String project, String branchDate) throws VCSException {
        try {
            SVNDiffClient differ = new SVNDiffClient(authManager, null);
            differ.doDiffStatus(SVNURL.parseURIDecoded(env.getRootUrl() + project + env.getBranchPath() + branchDate),
                    SVNRevision.HEAD,
                    SVNURL.parseURIDecoded(env.getRootUrl() + project + env.getTrunkPath()),
                    SVNRevision.HEAD,
                    SVNDepth.INFINITY,
                    false,
                    new SVNDiffStatusVisitor(visitor));
        } catch (SVNException e) {
            if (e.getErrorMessage().getErrorCode() != SVNErrorCode.CANCELLED) {
                throw new VCSException(e);
            }
        }
    }

    public boolean hasTrunkCommitsSinceBranch(String project, String branchDate) throws VCSException {
        final boolean[] trunkDiff = new boolean[1];
        visitBranchToTrunkDiffStatus(new DiffStatusVisitor() {
            public boolean visit(DiffStatus diffStatus) {
                if (!"none".equals(diffStatus.getModificationType())) {
                    trunkDiff[0] = true;
                    return false;
                }
                return true;
            }
        }, project, branchDate);
        return trunkDiff[0];
    }

    public void visitTagDiffStatus(DiffStatusVisitor visitor, String project, String version1, String version2) throws VCSException {
        try {
            SVNDiffClient differ = new SVNDiffClient(authManager, null);
            differ.doDiffStatus(
                    SVNURL.parseURIDecoded(env.getRootUrl() + project + env.getTagPath() + version1),
                    SVNRevision.HEAD,
                    SVNURL.parseURIDecoded(env.getRootUrl() + project + env.getTagPath() + version2),
                    SVNRevision.HEAD,
                    SVNDepth.INFINITY,
                    false,
                    new SVNDiffStatusVisitor(visitor)
            );
        } catch (SVNException e) {
            if (e.getErrorMessage().getErrorCode() != SVNErrorCode.CANCELLED) {
                throw new VCSException(e);
            }
        }
    }

    public void visitTrunkChangeLog(LogEntryVisitor visitor, String project, int limit, String... paths) throws VCSException {
        try {
            final SVNURL trunkUrl = SVNURL.parseURIDecoded(env.getRootUrl() + project + env.getTrunkPath());
            final SVNLogClient logClient = new SVNLogClient(authManager, null);
            final SVNRevision revStart = SVNRevision.create(0);
            logClient.doLog(trunkUrl, paths, revStart, revStart, SVNRevision.HEAD, true, false, limit, new SVNLogEntryVisitor(visitor, revisionUrlFormat));
        } catch (SVNException e) {
            throw new VCSException(e);
        }
    }

    // pretty big hack to extend branch log back to a revision referred to in the branch creation message in form  "from rNNNNNNN"
    private static final Pattern FROM_REVISION_PATTERN = Pattern.compile("from r(\\d+)");

    public void visitBranchChangeLog(LogEntryVisitor visitor, String project, String branchDate, int limit, String... paths) throws VCSException {
        visitBranchChangeLog(visitor, project, branchDate, false, limit, paths);
    }

    public void visitBranchChangeLog(LogEntryVisitor visitor, String project, String branchDate, boolean extendToBranchPointUsingComment, int limit, String... paths) throws VCSException {
        try {
            final SVNURL branchUrl = SVNURL.parseURIDecoded(env.getRootUrl() + project + env.getBranchPath() + branchDate);
            final SVNLogClient logClient = new SVNLogClient(authManager, null);
            final SVNRevision revStart = SVNRevision.create(0);
            SVNLogEntryVisitor svnVisitor = new SVNLogEntryVisitor(visitor, revisionUrlFormat);
            logClient.doLog(branchUrl, paths, revStart, revStart, SVNRevision.HEAD, true, false, limit, svnVisitor);
            final SVNLogEntry oldestEntry = svnVisitor.getOldestEntry();
            if (extendToBranchPointUsingComment && oldestEntry != null) {
                final Matcher m = FROM_REVISION_PATTERN.matcher(oldestEntry.getMessage());
                if (m.find()) {
                    final int revisionNumber = Integer.parseInt(m.group(1));
                    final SVNRevision startRev = SVNRevision.create(oldestEntry.getRevision() - 1);
                    final SVNRevision endRev = SVNRevision.create(revisionNumber);
                    logClient.doLog(branchUrl, paths, startRev, endRev, SVNRevision.HEAD, false, false, limit, svnVisitor);
                }
            }
        } catch (SVNException e) {
            throw new VCSException(e);
        }
    }

    public void visitTagChangeLog(LogEntryVisitor visitor, String project, String tag, int limit, String... paths) throws VCSException {
        try {
            final SVNURL tagUrl = SVNURL.parseURIDecoded(env.getRootUrl() + project + env.getTagPath() + tag);
            final SVNLogClient logClient = new SVNLogClient(authManager, null);
            final SVNRevision revStart = SVNRevision.create(0);
            logClient.doLog(tagUrl, paths, revStart, revStart, SVNRevision.HEAD, true, true, limit, new SVNLogEntryVisitor(visitor, revisionUrlFormat));
        } catch (SVNException e) {
            throw new VCSException(e);
        }
    }

    public long getBranchStartRevision(String project, String branchDate) throws VCSException {
        return getBranchStartRevision(project, branchDate, false);
    }

    public long getBranchStartRevision(String project, String branchDate, boolean extendToBranchPointFromComment) throws VCSException {
        final long[] rev = new long[1];
        visitBranchChangeLog(new LogEntryVisitor() {
            public void visit(LogEntry entry) {
                rev[0] = entry.getRevision();
            }
        }, project, branchDate, extendToBranchPointFromComment, 1, ".");
        return rev[0];
    }

    public void visitTrunkChangeLogSinceBranch(LogEntryVisitor visitor, String project, String branchDate, int limit, String... paths) throws VCSException {
        try {
            final SVNURL trunkUrl = SVNURL.parseURIDecoded(env.getRootUrl() + project + env.getTrunkPath());
            final SVNLogClient logClient = new SVNLogClient(authManager, null);
            final SVNRevision revStart = SVNRevision.create(getBranchStartRevision(project, branchDate, true));
            logClient.doLog(trunkUrl, paths, revStart, revStart, SVNRevision.HEAD, true, false, limit, new SVNLogEntryVisitor(visitor, revisionUrlFormat));
        } catch (SVNException e) {
            throw new VCSException(e);
        }
    }

    public LogEntry getTagFirstLogEntry(String project, String tag) throws VCSException {
        final LogEntry[] ret = new LogEntry[1];
        visitTagChangeLog(new LogEntryVisitor() {
            public void visit(LogEntry entry) {
                ret[0] = entry;
            }
        }, project, tag, 1, ".");
        return ret[0];
    }

    public long getTagFirstRevision(String project, String tag) throws VCSException {
        LogEntry entry = getTagFirstLogEntry(project, tag);
        if (entry != null) {
            return entry.getRevision();
        }
        return -1;
    }

    public void visitTrunkChangeLogSinceTag(LogEntryVisitor visitor, String project, String tag, int limit, String... paths) throws VCSException {
        try {
            final SVNURL trunkUrl = SVNURL.parseURIDecoded(env.getRootUrl() + project + env.getTrunkPath());
            final SVNLogClient logClient = new SVNLogClient(authManager, null);
            final SVNRevision revStart = SVNRevision.create(getTagFirstRevision(project, tag));
            logClient.doLog(trunkUrl, paths, revStart, revStart, SVNRevision.HEAD, true, false, limit, new SVNLogEntryVisitor(visitor, revisionUrlFormat));
        } catch (SVNException e) {
            throw new VCSException(e);
        }
    }

    private static Comparator<? super SVNDirEntry> REVERSE_AGE_COMPARATOR =
            new Comparator<SVNDirEntry>() {
                public int compare(SVNDirEntry v1, SVNDirEntry v2) {
                    return v2.getDate().compareTo(v1.getDate());
                }
            };

    private static Comparator<? super SVNDirEntry> REVERSE_VERSION_COMPARATOR =
            new Comparator<SVNDirEntry>() {
                public int compare(SVNDirEntry v1, SVNDirEntry v2) {
                    String s1 = ReleaseEnvironment.normalizeVersion(v1.getName(), 4);
                    String s2 = ReleaseEnvironment.normalizeVersion(v2.getName(), 4);
                    return s2.compareTo(s1);
                }
            };

    private static Comparator<? super SVNDirEntry> REVERSE_BRANCH_COMPARATOR =
            new Comparator<SVNDirEntry>() {
                public int compare(SVNDirEntry b1, SVNDirEntry b2) {
                    String s1 = b1.getName();
                    String s2 = b2.getName();
                    return s2.compareTo(s1);
                }
            };

    private static EnumMap<Ordering, Comparator<? super SVNDirEntry>> orderings = Maps.newEnumMap(Ordering.class);

    static {
        orderings.put(Ordering.NORMAL, com.google.common.collect.Ordering.natural());
        orderings.put(Ordering.REVERSE_BRANCH, REVERSE_BRANCH_COMPARATOR);
        orderings.put(Ordering.REVERSE_VERSION, REVERSE_VERSION_COMPARATOR);
        orderings.put(Ordering.REVERSE_AGE, REVERSE_AGE_COMPARATOR);
    }

    public List<DirEntry> listBranches(String project, int limit, Ordering ordering) throws VCSException {
        try {
            final TreeSet<SVNDirEntry> branches = Sets.newTreeSet(orderings.get(ordering));
            repo.getDir(project + env.getBranchPath(), -1, null, branches);
            Iterator<SVNDirEntry> branchesIter = branches.iterator();
            List<DirEntry> ret = Lists.newArrayListWithExpectedSize(limit);
            for (int i = 0; i < limit && branchesIter.hasNext(); i++) {
                ret.add(new SVNDirEntryWrapper(branchesIter.next()));
            }
            return ret;
        } catch (SVNException e) {
            throw new VCSException(e);
        }
    }

    public List<DirEntry> listTags(String project, int limit, Ordering ordering) throws VCSException {
        try {
            final TreeSet<SVNDirEntry> tags = Sets.newTreeSet(orderings.get(ordering));
            repo.getDir(project + env.getTagPath(), -1, null, tags);
            Iterator<SVNDirEntry> tagsIter = tags.iterator();
            List<DirEntry> ret = Lists.newArrayListWithExpectedSize(limit);
            for (int i = 0; i < limit && tagsIter.hasNext(); i++) {
                ret.add(new SVNDirEntryWrapper(tagsIter.next()));
            }
            return ret;
        } catch (SVNException e) {
            throw new VCSException(e);
        }
    }

    public List<DirEntry> listDir(String dir, Ordering ordering) throws VCSException {
        try {
            final TreeSet<SVNDirEntry> entries = Sets.newTreeSet(orderings.get(ordering));
            repo.getDir(dir, -1, null, entries);
            List<DirEntry> ret = Lists.newArrayListWithExpectedSize(entries.size());
            for (SVNDirEntry entry : entries) {
                ret.add(new SVNDirEntryWrapper(entry));
            }
            return ret;
        } catch (SVNException e) {
            throw new VCSException(e);
        }
    }

    public boolean hasFilesSince(String path, final Date earliest) {
        final boolean[] result = new boolean[]{false};
        try {
            repo.getDir(path, -1, null, new ISVNDirEntryHandler() {
                public void handleDirEntry(SVNDirEntry svnDirEntry) throws SVNException {
                    if (svnDirEntry.getDate().after(earliest)) {
                        result[0] = true;
                        // stop iteration
                        throw new SVNException(SVNErrorMessage.UNKNOWN_ERROR_MESSAGE);
                    }
                }
            });
        } catch (SVNException e) {
            // ignore
        }
        return result[0];
    }

    public boolean checkExistsInHead(String path) throws VCSException {
        try {
            SVNNodeKind kind = repo.checkPath(path, repo.getLatestRevision());
            return kind != SVNNodeKind.NONE;
        } catch (SVNException e) {
            throw new VCSException(e);
        }
    }

    public long checkoutBranch(String project, String branchDate, File workingDir) throws VCSException, IOException {
        try {
            final SVNUpdateClient updateClient = clientManager.getUpdateClient();
            final SVNURL branchUrl = SVNURL.parseURIDecoded(env.getRootUrl() + project + env.getBranchPath() + branchDate);
            final SVNRevision revStart = SVNRevision.create(getBranchStartRevision(project, branchDate, false));
            return updateClient.doCheckout(branchUrl, workingDir, revStart, SVNRevision.HEAD, SVNDepth.INFINITY, false);
        } catch (SVNException e) {
            throw new VCSException(e);
        }
    }

    public CommitInfo mergeToBranch(String project, long revision, String branchDate, String commitMessage, File workingDir) throws VCSException, IOException {
        try {
            final long branchRev = checkoutBranch(project, branchDate, workingDir);
            if (branchRev > 0) {
                final SVNDiffClient diffClient = clientManager.getDiffClient();
                final SVNURL url = SVNURL.parseURIDecoded(env.getRootUrl() + project + env.getTrunkPath());
                final SVNRevision pegRev = SVNRevision.create(branchRev);
                final SVNRevision prevRev = SVNRevision.create(revision - 1);
                final SVNRevision rev = SVNRevision.create(revision);
                diffClient.doMerge(url, pegRev, prevRev, rev, workingDir, true, true, false, false);

                final SVNCommitClient commitClient = clientManager.getCommitClient();
                return new SVNCommitInfoWrapper(commitClient.doCommit(new File[]{workingDir}, false, commitMessage, null, null, false, false, SVNDepth.INFINITY));
            }
            return null;
        } catch (SVNException e) {
            throw new VCSException(e);
        }
    }

    public CommitInfo commit(File file, String message) throws VCSException {
        try {
            final SVNCommitClient commitClient = clientManager.getCommitClient();
            return new SVNCommitInfoWrapper(commitClient.doCommit(new File[]{ file }, false, message, null, null, false, false, SVNDepth.INFINITY));
        } catch (SVNException e) {
            throw new VCSException(e);
        }
    }

    public LogEntry getTrunkLogEntry(String project, long revision) throws VCSException {
        try {
            final SVNLogClient logClient = clientManager.getLogClient();
            final SVNURL url = SVNURL.parseURIDecoded(env.getRootUrl() + project + env.getTrunkPath());
            final SVNRevision rev = SVNRevision.create(revision);
            final LogEntry[] entry = new SVNLogEntryWrapper[1];
            logClient.doLog(url, new String[]{"."}, rev, rev, rev, true, true, false, 1, null,
                    new ISVNLogEntryHandler() {
                        public void handleLogEntry(SVNLogEntry svnLogEntry) throws SVNException {
                            entry[0] = new SVNLogEntryWrapper(svnLogEntry, env.getRevisionUrlFormat());
                        }
                    });
            return entry[0];
        } catch (SVNException e) {
            throw new VCSException(e);
        }
    }

    public LogEntry getBranchLogEntry(String project, String branchDate, long revision) throws VCSException {
        try {
            final SVNLogClient logClient = clientManager.getLogClient();
            final SVNURL url = SVNURL.parseURIDecoded(env.getRootUrl() + project + env.getBranchPath() + branchDate);
            final SVNRevision rev = SVNRevision.create(revision);
            final LogEntry[] entry = new SVNLogEntryWrapper[1];
            logClient.doLog(url, new String[]{"."}, rev, rev, rev, true, true, false, 1, null,
                    new ISVNLogEntryHandler() {
                        public void handleLogEntry(SVNLogEntry svnLogEntry) throws SVNException {
                            entry[0] = new SVNLogEntryWrapper(svnLogEntry, env.getRevisionUrlFormat());
                        }
                    });
            return entry[0];
        } catch (SVNException e) {
            throw new VCSException(e);
        }
    }

    public long getFile(String path, long revision, ByteArrayOutputStream outputStream) throws VCSException {
        long rev = -1;
        try {
            SVNProperties props = new SVNProperties();
            repo.getFile(path, revision, props, outputStream);
            final String revStr = props.getStringValue(SVNProperty.COMMITTED_REVISION);
            if (revStr != null) {
                try {
                    rev = Long.parseLong(revStr);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        } catch (SVNException e) {
            throw new VCSException(e);
        }
        return rev;
    }

    public long getLatestRevision(String path) throws VCSException {
        long rev = -1;
        try {
            SVNDirEntry entry = repo.info(path, -1);
            rev = entry.getRevision();
        } catch (SVNException e) {
            throw new VCSException(e);
        }
        return rev;
    }

    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.ERROR);
        VCSClient sc = new SubversionClient(new ReleaseEnvironment());
        System.out.println(sc.hasTrunkCommitsSinceTag(args[0], args[1]));
//        System.out.println(sc.getLatestRevision(args[0]));
//        final ISVNDiffStatusHandler dsHandler = new ISVNDiffStatusHandler() {
//            public void handleDiffStatus(SVNDiffStatus svnDiffStatus) throws VCSException {
//                System.out.println(svnDiffStatus.getModificationType() + " " + svnDiffStatus.getPath());
//            }
//        };
//
//        List<String> lastTwoBranches = sc.listBranches(args[0], 2, REVERSE_BRANCH_COMPARATOR);
//        System.out.println(lastTwoBranches);
//        sc.visitBranchDiffStatus("jasx", lastTwoBranches.get(1), lastTwoBranches.get(0), dsHandler);
//
//        System.out.println();
//        List<String> lastTwoTags = sc.getTags(args[0], 2, REVERSE_VERSION_COMPARATOR);
//        System.out.println(lastTwoTags);
//        sc.visitTagDiffStatus("jasx", lastTwoTags.get(1), lastTwoTags.get(0), dsHandler);

//        sc.visitTrunkChangeLogSinceBranch(new ISVNLogEntryHandler() {
//            public void handleLogEntry(SVNLogEntryWrapper svnLogEntry) throws VCSException {
//                System.out.println(svnLogEntry.toString());
//            }
//        }, args[0], args[1], Integer.parseInt(args[2]), args[3]);

//        System.out.println(sc.checkoutBranch(args[0], args[1], new File(args[2])));

//        System.out.println(sc.getTrunkLogEntry(args[0], Long.parseLong(args[1])).getChangedPaths().values());

//        File tmpDir = Files.createTempDir();
//        System.out.println(tmpDir);
//        System.out.println(sc.mergeToBranch(args[0], Long.parseLong(args[1]), args[2], "NOBUG ", tmpDir));
//        Files.deleteRecursively(tmpDir);
    }

}
