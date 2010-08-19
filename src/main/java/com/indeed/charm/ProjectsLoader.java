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

import com.google.common.collect.Sets;
import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableList;
import com.indeed.charm.model.Project;
import com.indeed.charm.model.DirEntry;

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
            for (DirEntry entry : vcsClient.listDir(root, VCSClient.Ordering.NORMAL)) {
                names.add(root + entry.getName());
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
