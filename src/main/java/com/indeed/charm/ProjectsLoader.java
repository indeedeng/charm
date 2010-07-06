package com.indeed.charm;

import com.google.common.collect.Sets;
import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableList;
import com.indeed.charm.model.Project;

import java.util.concurrent.*;
import java.util.TreeSet;
import java.util.List;
import java.util.Date;

import org.apache.log4j.Logger;

/**
*/
public class ProjectsLoader implements Runnable {
    private static final Logger log = Logger.getLogger(ProjectsLoader.class);

    private final ReleaseEnvironment env;
    private final VCSClient vcsClient;

    private List<Project> releaseProjects;
    private List<Project> libraryProjects;

    public ProjectsLoader(ReleaseEnvironment env, VCSClient vcsClient) {
        this.env = env;
        this.vcsClient = vcsClient;

        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(this, 0, 10, TimeUnit.MINUTES);
    }

    public List<Project> getReleaseProjects() {
        return releaseProjects;
    }

    public List<Project> getLibraryProjects() {
        return libraryProjects;
    }

    public synchronized void waitForLoad() {
        if (releaseProjects == null || libraryProjects == null) {
            try {
                wait();
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    public void run() {
        try {
            refreshNow();
        } catch (VCSException e) {
            log.error("Failed to update projects list", e);
        }
    }

    public synchronized void refreshNow() throws VCSException {
        final String branchPath = env.getBranchPath();
        final String tagPath = env.getTagPath();
        TreeSet<String> names = Sets.newTreeSet();
        for (String root : env.getRoots()) {
            if (root.length() > 0 && !root.endsWith("/")) {
                root = root + "/";
            }
            for (String name : vcsClient.listDir(root, VCSClient.Ordering.NORMAL)) {
                names.add(root + name);
            }
        }
        List<Project> rels = Lists.newArrayListWithCapacity(names.size());
        List<Project> libs = Lists.newArrayListWithCapacity(names.size());
        for (String name : names) {
            Project project = new Project(name, checkNonEmpty(name + branchPath), checkNonEmpty(name + tagPath));
            if (project.isReleaseBranches()) {
                rels.add(project);
            } else if (project.isPublishedTags()) {
                libs.add(project);
            }
        }
        releaseProjects = ImmutableList.copyOf(rels);
        libraryProjects = ImmutableList.copyOf(libs);

        log.info("Loaded " + releaseProjects.size() + " release projects and " + libraryProjects.size() + " library projects");

        notifyAll();
    }

    private boolean checkNonEmpty(String path) throws VCSException {
        Date earliest = env.getEarliestReleaseDate();
        return vcsClient.checkExistsInHead(path) &&
                vcsClient.hasFilesSince(path, earliest);
    }
}
