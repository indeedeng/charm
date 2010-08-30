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
import java.util.regex.Pattern;

import com.google.common.base.Splitter;
import com.google.common.collect.*;
import com.indeed.charm.svn.SubversionClient;

import javax.servlet.ServletContext;

/**
 */
public class ReleaseEnvironment {
    private static Logger log = Logger.getLogger(ReleaseEnvironment.class);
    public static Splitter versionSplitter = Splitter.on(".");

    private Properties properties;
    private List<LinkifyPattern> linkifyPatterns;
    private BiMap<String,String> repoNameMap;

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
    
    public ReleaseEnvironment(ServletContext context) {
        final String propertiesPath = getCharmPropertiesPath(context);
        if (propertiesPath != null) {
            properties = new Properties();
            try {
                InputStream in = new FileInputStream(propertiesPath);
                properties.load(in);
                log.info(properties);
            } catch (IOException e) {
                log.error("Failed to load properties", e);
            }
        } else {
            log.error("Missing charm.properties");
        }
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

    public List<LinkifyPattern> getLinkifyPatterns() {
        if (linkifyPatterns == null) {
            ImmutableList.Builder<LinkifyPattern> builder = ImmutableList.builder();
            String[] names = properties.getProperty("linkify.patterns", "JIRA,FISHEYE").split(",");
            for (String name : names) {
                if (name.trim().length() > 0) {
                    final String pattern = properties.getProperty("linkify.pattern." + name);
                    final String replacement = properties.getProperty("linkify.replacement." + name);
                    builder.add(new LinkifyPattern(name, pattern, replacement));
                }
            }
            linkifyPatterns = builder.build();
        }
        return linkifyPatterns;
    }


    public String linkify(String value, Collection<String> appliedPatterns) {
        for (LinkifyPattern linkifier : getLinkifyPatterns()) {
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

    private BiMap<String, String> getRepoNameMap() {
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
