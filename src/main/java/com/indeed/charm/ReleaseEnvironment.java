package com.indeed.charm;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.Date;
import java.util.List;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.indeed.charm.svn.SubversionClient;

/**
 */
public class ReleaseEnvironment {
    private static Logger log = Logger.getLogger(ReleaseEnvironment.class);

    private Properties properties;
    private List<LinkifyPattern> linkifyPatterns;

    public ReleaseEnvironment() {
        properties = new Properties();
        try {
            InputStream in = new FileInputStream(System.getProperty("charm.properties", ""));
            if (in != null) {
                properties.load(in);
                log.info(properties);
            }
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
                    builder.add(new LinkifyPattern(pattern, replacement));
                }
            }
            linkifyPatterns = builder.build();
        }
        return linkifyPatterns;
    }


    public String linkify(String value) {
        for (LinkifyPattern linkifier : getLinkifyPatterns()) {
            value = linkifier.apply(value);
        }
        return value;
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
}
