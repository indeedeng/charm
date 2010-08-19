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

package com.indeed.charm.actions;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

import com.indeed.charm.ivy.IvyLoader;

/**
 */
public class ListDependencyConflictsAction extends VCSActionSupport {
    private static Logger log = Logger.getLogger(ListDependencyConflictsAction.class);

    private String format;
    private String branchDate;
    private String tag;
    private String spec;
    private boolean traverseThirdParty;

    private Long jobId;
    private BackgroundJob<IvyLoader.DepGraph> job;
    private List<IvyLoader.DependencyNode> conflictRecords;
    private List<IvyLoader.DependencyNode> nonConflictRecords;

    @Override
    public String execute() throws Exception {
        if (jobId != null) {
            job = backgroundJobManager.getJobForId(jobId);
            if (job != null) {
                if (!job.isRunning() && job.getFuture().isDone()) {
                    IvyLoader.DepGraph graph = job.getFuture().get();
                    conflictRecords = graph.listAllConflicts();
                    nonConflictRecords = graph.listNonConflicts();
                    if ("text".equals(format)) {
                        return "text";
                    }
                }
                return SUCCESS;
            } else {
                jobId = null;
            }
        }
        spec = tag != null ? tag : (branchDate == null ? "trunk" : branchDate);
        final BackgroundJob<IvyLoader.DepGraph> job = new BackgroundJob<IvyLoader.DepGraph>() {

            public IvyLoader.DepGraph call() throws Exception {
                try {
                    setStatus("ANALYZING");
                    log("Started at " + new Date());

                    IvyLoader.DepGraph graph = null;
                    String cacheKey = project + ';' + spec;
                    if (tag == null) {
                        long rev;
                        if (branchDate != null) {
                            rev = vcsClient.getLatestRevision(project + env.getBranchPath() + branchDate + '/' + env.getIvyFileName());
                        } else {
                            rev = vcsClient.getLatestRevision(project + env.getTrunkPath() + '/' + env.getIvyFileName());
                        }
                        if (rev != -1) {
                            cacheKey += "." + rev;
                            graph = depGraphCache.get(cacheKey);
                        }
                    } else {
                        graph = depGraphCache.get(cacheKey);
                    }
                    if (graph == null) {
                        graph = new IvyLoader.DepGraph(vcsClient, env, project, tag != null, spec, traverseThirdParty);
                        logBuilder.append("Loading dependencies...");
                        graph.build(logBuilder);
                        depGraphCache.put(cacheKey, graph);
                    } else {
                        log("Found cached dependency graph for " + cacheKey);
                    }
                    log("Finished at " + new Date());
                    setStatus("COMPLETE");
                    conflictRecords = graph.listAllConflicts();
                    nonConflictRecords = graph.listNonConflicts();
                    return graph;
                } catch (IOException e) {
                    log.error("Failed to merge to branchDate", e);
                    log("Merge failure: " + e.getMessage());
                }
                setStatus("FAILED");
                return null;
            }

            public String getTitle() {
                return "List dependency conflicts for " + project + " " + spec;
            }
        };
        backgroundJobManager.submit(job);
        setJob(job);
        if (!job.isRunning() && job.getFuture().get() != null) {
            if ("text".equals(format)) {
                return "text";
            }
        }
        return SUCCESS;
    }

    public BackgroundJob<IvyLoader.DepGraph> getJob() {
        return job;
    }

    public void setJob(BackgroundJob<IvyLoader.DepGraph> job) {
        this.job = job;
    }

    public long getJobId() {
        return jobId;
    }

    public void setJobId(long jobId) {
        this.jobId = jobId;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getBranchDate() {
        return branchDate;
    }

    public void setBranchDate(String branchDate) {
        this.branchDate = branchDate;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public List<IvyLoader.DependencyNode> getConflictRecords() {
        return conflictRecords;
    }

    public List<IvyLoader.DependencyNode> getNonConflictRecords() {
        return nonConflictRecords;
    }

    public String getHomeOrg() {
        return env.getIvyOrg();
    }

    public void setTraverseThirdParty(boolean traverseThirdParty) {
        this.traverseThirdParty = traverseThirdParty;
    }
}