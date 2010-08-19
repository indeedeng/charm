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
public class CheckUpgradeDependencyConflictsAction extends VCSActionSupport {
    private static Logger log = Logger.getLogger(CheckUpgradeDependencyConflictsAction.class);

    private String branchDate;
    private String module;
    private String rev;

    private Long jobId;
    private BackgroundJob<IvyLoader.DepGraph> job;
    private List<IvyLoader.DependencyNode> conflictRecords;

    @Override
    public String execute() throws Exception {
        if (jobId != null) {
            job = backgroundJobManager.getJobForId(jobId);
            if (job != null) {
                if (!job.isRunning() && job.getFuture().isDone()) {
                    IvyLoader.DepGraph graph = job.getFuture().get();
                    conflictRecords = graph.findConflicts(module, rev);
                }
                return SUCCESS;
            } else {
                jobId = null;
            }
        }
        final BackgroundJob<IvyLoader.DepGraph> job = new BackgroundJob<IvyLoader.DepGraph>() {

            public IvyLoader.DepGraph call() throws Exception {
                try {
                    setStatus("ANALYZING");
                    log("Started at " + new Date());

                    IvyLoader.DepGraph graph = null;
                    String cacheKey = project + ';' + branchDate;
                    long r = vcsClient.getLatestRevision(project + env.getBranchPath() + branchDate + '/' + env.getIvyFileName());
                    if (r != -1) {
                        cacheKey += "." + rev;
                        graph = depGraphCache.get(cacheKey);
                    }
                    if (graph == null) {
                        graph = new IvyLoader.DepGraph(vcsClient, env, project, false, branchDate, false);
                        logBuilder.append("Loading dependencies...");
                        graph.build(logBuilder);
                        depGraphCache.put(cacheKey, graph);
                    } else {
                        log("Found cached dependency graph for " + cacheKey);
                    }
                    log("Finished at " + new Date());
                    setStatus("COMPLETE");
                    conflictRecords = graph.findConflicts(module, rev);
                    return graph;
                } catch (IOException e) {
                    log.error("Failed to merge to branchDate", e);
                    log("Merge failure: " + e.getMessage());
                }
                setStatus("FAILED");
                return null;
            }

            public String getTitle() {
                return "Check upgrade dependency conflicts for " + module + " " + rev + " in " + project + " " + branchDate;
            }
        };
        backgroundJobManager.submit(job);
        setJob(job);
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

    public String getBranchDate() {
        return branchDate;
    }

    public void setBranchDate(String branchDate) {
        this.branchDate = branchDate;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getRev() {
        return rev;
    }

    public void setRev(String rev) {
        this.rev = rev;
    }

    public List<IvyLoader.DependencyNode> getConflictRecords() {
        return conflictRecords;
    }

    public String getHomeOrg() {
        return env.getIvyOrg();
    }
}
