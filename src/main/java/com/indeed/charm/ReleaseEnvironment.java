package com.indeed.charm;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.*;

import com.google.common.base.Splitter;
import com.google.common.collect.*;
import com.indeed.charm.svn.SubversionClient;

/**
 */
public class ReleaseEnvironment {
    private static Logger log = Logger.getLogger(ReleaseEnvironment.class);
    public static Splitter versionSplitter = Splitter.on(".");

    private Properties properties;
    private List<LinkifyPattern> linkifyPatterns;
    private BiMap<String,String> repoNameMap;

    public ReleaseEnvironment() {
        properties = new Properties();
        try {
            InputStream in = new FileInputStream(System.getProperty("charm.properties", ""));
            properties.load(in);
            log.info(properties);
        } catch (IOException e) {
            log.error("Failed to load properties", e);
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

    public static String normalizeVersion(String in, int width) {
        final String format = "%" + width + "s";
        StringBuilder builder = new StringBuilder();
        for (String v : versionSplitter.split(in)) {
            builder.append(String.format(format, v)).append(".");
        }
        return builder.toString();
    }
}
