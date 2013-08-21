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

import java.io.*;

import com.indeed.charm.VCSClient;
import com.indeed.charm.VCSException;
import com.indeed.charm.ivy.IvyUtils;
import com.indeed.charm.model.CommitInfo;
import com.google.common.io.Files;
import com.google.common.base.Suppliers;
import com.google.common.base.Supplier;
import com.google.common.base.Charsets;

/**
 */
public class UpgradeBranchIvyDependencyAction extends BaseBranchAction {
    private static Logger log = Logger.getLogger(UpgradeBranchIvyDependencyAction.class);

    private Long jobId;
    private String user;
    private String password;
    private String messagePrefix = "";
    private String module;
    private String oldRev;
    private String newRev;
    private BackgroundJob<Boolean> job;

    @Override
    public String execute() throws Exception {
        if (jobId != null) {
            job = backgroundJobManager.getJobForId(jobId);
            if (job != null) {
                return SUCCESS;
            } else {
                jobId = null;
            }
        }
        if (user == null || password == null) {
            return LOGIN;
        }
        if (module == null || oldRev == null || newRev == null) {
            return ERROR;
        }
        final File branchDir = env.getBranchWorkingDirectory(project, branchDate, user);
        final VCSClient vcsClient = env.newClient(env, user, password);
        final BackgroundJob<Boolean> job = new BackgroundJob<Boolean>() {
            public Boolean call() throws Exception {
                try {
                    // TODO log messages are svn-specific
                    log("svn co " + env.getRootUrlString() + project + env.getBranchPath() + branchDate + " " + branchDir);
                    setStatus("Checking out " + project + " branch " + branchDate);
                    long r = vcsClient.checkoutBranch(project, branchDate, branchDir);
                    log("Checked out branch dir at revision " + r);
                    setStatus("Updating ivy.xml");
                    log("...update ivy.xml...");
                    IvyUtils util = new IvyUtils(env);
                    File ivyFile = new File(branchDir, env.getIvyFileName());
                    Reader ivyReader = Files.newReader(ivyFile, Charsets.UTF_8);
                    CharArrayWriter ivyWriter = new CharArrayWriter();
                    boolean changed = util.upgradeDependency(ivyReader, ivyWriter, env.getIvyOrg(), module, oldRev, newRev);
                    if (changed) {
                        Files.write(ivyWriter.toString(), ivyFile, Charsets.UTF_8);
                        log("...wrote new ivy.xml...");
                    } else {
                        log("...no changes made...");
                    }
                    CommitInfo info = vcsClient.commit(ivyFile, messagePrefix + " [upgrade " + module + " from " + oldRev + " to " + newRev + "]");
                    log("Commit complete");
                    log(info.toString());
                    return true;
                } catch (VCSException e) {
                    log.error("Failed to commit branch ivy update", e);
                    log("Failure: " + e.getMessage());
                } catch (IOException e) {
                    log.error("Failed to write ivy dependency update", e);
                    log("Failure: " + e.getMessage());
                }
                setStatus("FAILED");
                return false;
            }

            public String getTitle() {
                return "Upgrade " + module + " from " + oldRev + " to " + newRev + " on " + project + " " + branchDate;
            }

            @Override
            public void log(String message) {
                super.log(message);
                log.info(message);
            }
        };
        backgroundJobManager.submit(job);
        setJob(job);
        return SUCCESS;
    }

    public String getMessagePrefix() {
        return messagePrefix;
    }

    public void setMessagePrefix(String messagePrefix) {
        this.messagePrefix = messagePrefix;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getOldRev() {
        return oldRev;
    }

    public void setOldRev(String oldRev) {
        this.oldRev = oldRev;
    }

    public String getNewRev() {
        return newRev;
    }

    public void setNewRev(String newRev) {
        this.newRev = newRev;
    }

    public BackgroundJob<Boolean> getJob() {
        return job;
    }

    public void setJob(BackgroundJob<Boolean> job) {
        this.job = job;
    }

    public long getJobId() {
        return jobId;
    }

    public void setJobId(long jobId) {
        this.jobId = jobId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}