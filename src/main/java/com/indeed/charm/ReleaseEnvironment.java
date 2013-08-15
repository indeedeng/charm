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

import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import com.google.common.base.Splitter;
import com.google.common.collect.*;

import com.indeed.charm.issues.IssueTracker;
import com.indeed.charm.svn.SubversionClient;

import javax.servlet.ServletContext;

/**
 */
public class ReleaseEnvironment {
    private static Logger log = Logger.getLogger(ReleaseEnvironment.class);
    public static Splitter versionSplitter = Splitter.on(".");

    private final String propertiesPath;
    private final ScheduledExecutorService propertiesLoader;

    private Properties properties;
    private long propertiesLastModified = 0;
    private List<ReplacementPattern> linkifyPatterns;
    private List<ReplacementPattern> commitTransformPatterns;
    private BiMap<String,String> repoNameMap;
    private BiMap<String,String> deployNameMap;
    private IssueTracker issueTracker;
    private Pattern issueTrackerKeyPattern;

    protected static String getCharmPropertiesPath(ServletContext context) {
        String path = null;
        if (context != null) {
            path = context.getInitParameter("charm.properties");
        }
        if (path == null || path.trim().length() == 0) {
            path = System.getProperty("charm.properties", null);
        }
        return path;
    }

    public ReleaseEnvironment() {
        this(null);
    }
    
    public ReleaseEnvironment(final ServletContext context) {
        propertiesPath = getCharmPropertiesPath(context);
        loadProperties();
        propertiesLoader = Executors.newSingleThreadScheduledExecutor(
                new ThreadFactory() {
                    public Thread newThread(Runnable runnable) {
                        return new Thread(runnable, "PropertiesLoader");
                    }
                }
        );
        propertiesLoader.scheduleAtFixedRate(new Runnable() {
            public void run() {
                loadProperties();
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    private final void loadProperties() {
        final Properties properties = new Properties();
        if (propertiesPath != null) {
            try {
                File propsFile = new File(propertiesPath);
                if (propertiesLastModified < propsFile.lastModified()) {
                    InputStream in = new FileInputStream(propsFile);
                    properties.load(in);
                    log.info(properties);
                    this.properties = properties;
                    propertiesLastModified = propsFile.lastModified();
                    unmemoize();
                    if (issueTracker != null) {
                        // reinitialize issue tracker only if already initialized
                        initializeIssueTracker();
                    }
                } else {
                    log.info("No changes found in " + propertiesPath);
                }
            } catch (IOException e) {
                log.error("Failed to load properties", e);
            }
        } else {
            try {
                InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("charm.properties");
                properties.load(in);
                this.properties = properties;
            } catch(IOException e) {
                log.error("Failed to load charm.properties from classpath", e);
            }
        }
    }
    
    private final synchronized void unmemoize() {
        linkifyPatterns = null;
        commitTransformPatterns = null;
        deployNameMap = null;
        repoNameMap = null;
    }

    public String getPropertiesPath() {
        return propertiesPath;
    }

    public File getPropertiesDir() {
        return new File(propertiesPath).getParentFile();
    }

    public File getBranchWorkingDirectory(String project, String branchDate, String user) {
        final File tmpDir = getTempDir();
        final File branchDir = new File(tmpDir, project + "-" + branchDate + "-" + user);
        branchDir.mkdir();
        return branchDir;
    }

    public File getTempDir() {
        return new File(properties.getProperty("tmp.dir", "/tmp"));
    }

    public String getRootUrl() {
        return properties.getProperty("repo.url", "");
    }

    public String getUser() {
        return properties.getProperty("repo.user", "");
    }

    public String getPassword() {
        return properties.getProperty("repo.password", "");
    }

    public String getSshUser() {
        return properties.getProperty("repo.ssh2.username", "");
    }

    public Integer getSshPort() {
        return Integer.parseInt(properties.getProperty("repo.ssh2.port", "22"));
    }

    public String getSshPassword() {
        return properties.getProperty("repo.ssh2.password", "");
    }

    public File getSshKeyfile() {
        return new File(properties.getProperty("repo.ssh2.key", ""));
    }

    public String getSshPassphrase() {
        return properties.getProperty("repo.ssh2.passphrase", "");
    }

    public String getTrunkPath() {
        return properties.getProperty("repo.trunk.path", "/trunk/");
    }

    public String getBranchPath() {
        return properties.getProperty("repo.branch.path", "/branches/deploy/");
    }

    public String getTagPath() {
        return properties.getProperty("repo.tag.path", "/tags/published/");
    }

    public String getRevisionUrlFormat() {
        return properties.getProperty("repo.rev.url", "#%1$d");
    }

    public synchronized List<ReplacementPattern> getLinkifyPatterns() {
        final Properties properties = this.properties;
        if (linkifyPatterns == null) {
            ImmutableList.Builder<ReplacementPattern> builder = ImmutableList.builder();
            String[] names = properties.getProperty("linkify.patterns", "JIRA,FISHEYE").split(",");
            for (String name : names) {
                if (name.trim().length() > 0) {
                    final String pattern = properties.getProperty("linkify.pattern." + name);
                    final String replacement = properties.getProperty("linkify.replacement." + name);
                    builder.add(new ReplacementPattern(name, pattern, replacement));
                }
            }
            linkifyPatterns = builder.build();
        }
        return linkifyPatterns;
    }

    public synchronized List<ReplacementPattern> getCommitTransformPatterns() {
        final Properties properties = this.properties;
        if (commitTransformPatterns == null) {
            ImmutableList.Builder<ReplacementPattern> builder = ImmutableList.builder();
            String[] names = properties.getProperty("commit.patterns", "").split(",");
            for (String name : names) {
                if (name.trim().length() > 0) {
                    final String pattern = properties.getProperty("commit.pattern." + name);
                    final String replacement = properties.getProperty("commit.replacement." + name);
                    builder.add(new ReplacementPattern(name, pattern, replacement));
                }
            }
            commitTransformPatterns = builder.build();
        }
        return commitTransformPatterns;
    }

    public String linkify(String value, Collection<String> appliedPatterns) {
        for (ReplacementPattern linkifier : getLinkifyPatterns()) {
            String newValue = linkifier.apply(value);
            if (!newValue.equals(value)) {
                value = newValue;
                if (appliedPatterns != null) {
                    appliedPatterns.add(linkifier.getName());
                }
            }
        }
        return value;
    }

    public String transformCommitMessage(String value, Collection<String> appliedPatterns) {
        for (ReplacementPattern commitTransformer : getCommitTransformPatterns()) {
            String newValue = commitTransformer.apply(value);
            if (!newValue.equals(value)) {
                value = newValue;
                if (appliedPatterns != null) {
                    appliedPatterns.add(commitTransformer.getName());
                }
            }
        }
        return value;
    }

    private synchronized BiMap<String, String> getRepoNameMap() {
        if (repoNameMap == null) {
            final BiMap<String, String> map = HashBiMap.create();
            Iterable<String> entries = Splitter.on(',').trimResults().split(properties.getProperty("repo.name.map", ""));
            for (String entry : entries) {
                Iterator<String> pair = Splitter.on(':').trimResults().split(entry).iterator();
                final String repo = pair.hasNext() ? pair.next() : null;
                final String name = pair.hasNext() ? pair.next() : null;
                if (repo != null && name != null) {
                    map.put(repo, name);
                }
            }
            repoNameMap = map;
        }
        return repoNameMap;
    }

    public String getNameForRepo(String repo) {
        return getRepoNameMap().get(repo);
    }

    public String getRepoForName(String name) {
        return getRepoNameMap().inverse().get(name);
    }

    public void putRepoName(String repo, String name) {
        getRepoNameMap().put(repo, name);
    }

    private synchronized BiMap<String, String> getDeployNameMap() {
        if (deployNameMap == null) {
            final BiMap<String, String> map = HashBiMap.create();
            Iterable<String> entries = Splitter.on(',').trimResults().split(properties.getProperty("branch.deploy.name.map", ""));
            for (String entry : entries) {
                Iterator<String> pair = Splitter.on(':').trimResults().split(entry).iterator();
                final String repo = pair.hasNext() ? pair.next() : null;
                final String name = pair.hasNext() ? pair.next() : null;
                if (repo != null && name != null) {
                    map.put(repo, name);
                }
            }
            deployNameMap = map;
        }
        return deployNameMap;
    }

    public String getDeployName(String projectName) {
        return getDeployNameMap().get(projectName);
    }

    public Date getEarliestReleaseDate() {
        long maxDays = Long.parseLong(properties.getProperty("release.history.maxdays", "365"));
        return new Date(System.currentTimeMillis() - (maxDays * 24 * 60 * 60 * 1000));
    }

    public Iterable<String> getRoots() {
        // TODO move "common" to properties file
        return Splitter.on(',').trimResults().split(properties.getProperty("repo.roots", ",common"));
    }

    public VCSClient newClient(ReleaseEnvironment env, String user, String password) throws VCSException {
        // TODO choose VCS type based on charm.properties
        return new SubversionClient(env, user, password);
    }

    public boolean isIvyEnabled() {
        return getIvyOrg().trim().length() > 0;
    }
    
    public String getIvyFileName() {
        return properties.getProperty("ivy.filename", "ivy.xml");
    }

    public String getIvyProperties() {
        return properties.getProperty("ivy.properties", "libraries.properties");
    }

    public String getIvyOrg() {
        return properties.getProperty("ivy.org", "");
    }

    public String getIvyRepoUrl() {
        return properties.getProperty("ivy.repo.url", "");
    }

    public Set<String> getTrunkWarnIfMissingPatterns() {
        String patterns = properties.getProperty("log.trunk.warn-if-missing", "");
        return Sets.newHashSet(Splitter.on(',').trimResults().split(patterns));
    }

    public Set<String> getBranchWarnIfMissingPatterns() {
        String patterns = properties.getProperty("log.branch.warn-if-missing", "");
        return Sets.newHashSet(Splitter.on(',').trimResults().split(patterns));
    }

    public int getCleanupIntervalMinutes() {
        try {
            final String value = properties.getProperty("tmp.cleanup-interval-minutes", "60");
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 60;
        }
    }

    public int getCleanupAgeDays() {
        try {
            final String value = properties.getProperty("tmp.cleanup-age-days", "7");
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 7;
        }
    }

    public static String normalizeVersion(String in, int width) {
        final String format = "%" + width + "s";
        StringBuilder builder = new StringBuilder();
        for (String v : versionSplitter.split(in)) {
            builder.append(String.format(format, v)).append(".");
        }
        return builder.toString();
    }

    public static void recursiveDelete(File dir) {
        for (File file : dir.listFiles()) {
            if (!file.isDirectory()) {
                log.info("delete " + file.getAbsolutePath());
                file.delete();
            } else {
                recursiveDelete(file);
            }
        }
        for (File file : dir.listFiles()) {
            log.info("delete " + file.getAbsolutePath());
            file.delete();
        }
        dir.delete();
    }

    public String getJiraUrl() {
        return properties.getProperty("jira.url", "");
    }

    public String getJiraUser() {
        return properties.getProperty("jira.user", "");
    }

    public String getJiraPassword() {
        return properties.getProperty("jira.password", "");
    }

    public String getJiraFixVersionLink() {
        return properties.getProperty("jira.fix-version.link", "");
    }

    public void initializeIssueTracker() {
        final String trackerClass = properties.getProperty("issuetracker.class");
        if (trackerClass != null) {
            try {
                Class<? extends IssueTracker> clz = Class.forName(trackerClass).asSubclass(IssueTracker.class);
                final IssueTracker tracker = clz.newInstance();
                if (tracker.init(this)) {
                    this.issueTracker = tracker;
                    log.info("Issue Tracker initialized.");
                }
            } catch (Exception e) {
                log.error("failed to initialize issue tracker from class: " + trackerClass, e);
            }
        }
    }

    public IssueTracker getIssueTracker() {
        return issueTracker;
    }

    public Pattern getIssueTrackerKeyPattern() {
        if (issueTracker != null) {
            if (issueTrackerKeyPattern == null) {
                final String patternString = properties.getProperty("issuetracker.key.pattern");
                if (patternString != null) {
                    issueTrackerKeyPattern = Pattern.compile(patternString);
                }
            }
            return issueTrackerKeyPattern;
        }
        return null;
    }

    public String getDeployLink() {
        return properties.getProperty("branch.deploy.link");
    }

    private class CleanupTask extends TimerTask {
        private final Pattern branchDirPattern = Pattern.compile("[\\w\\-]+\\-[0-9]+\\-[0-9]+\\-[0-9]+\\-[\\w]+");
        private final FilenameFilter branchDirNameFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return branchDirPattern.matcher(name).matches();
            }
        };

        public void run() {
            final File workingDirectory = getTempDir();
            for (File dir : workingDirectory.listFiles(branchDirNameFilter)) {
                if (dir.isDirectory()) {
                    log.info("Checking age of " + dir);
                    final long now = System.currentTimeMillis();
                    final long age = now - dir.lastModified();
                    if (age > (getCleanupAgeDays() * 24 * 60 * 60 * 1000)) {
                        log.info("Old enough, deleting " + dir);
                        recursiveDelete(dir);
                    }
                }
            }
        }
    }

    public void scheduleCleanupTask() {
        final Timer cleanupTimer = new Timer();
        final long interval =  getCleanupIntervalMinutes() * 60 * 1000;
        cleanupTimer.scheduleAtFixedRate(new CleanupTask(), 0, interval);
    }
}
