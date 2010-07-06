package com.indeed.charm.actions;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

import com.indeed.charm.VCSClient;
import com.indeed.charm.VCSException;
import com.indeed.charm.model.CommitInfo;

/**
 */
public class MergeToBranchAction extends BaseBranchAction {
    private static Logger log = Logger.getLogger(CheckoutBranchAction.class);

    private Long jobId;
    private String user;
    private String password;
    private Long revision;
    private BranchJob job;

    @Override
    public String execute() throws Exception {
        if (jobId != null) {
            job = branchJobManager.getJobForId(jobId);
            return job != null ? SUCCESS : ERROR;
        }
        if (user == null || password == null) {
            return LOGIN;
        }
        if (revision == null) {
            return ERROR;
        }
        final File branchDir = env.getBranchWorkingDirectory(project, branchDate, user);
        final VCSClient vcsClient = env.newClient(env, user, password);
        final BranchJob job = new BranchJob() {
            public Boolean call() throws Exception {
                try {
                    // TODO log messages are svn-specific
                    log("svn co " + env.getRootUrl() + project + env.getBranchPath() + branchDate + " " + branchDir);
                    setStatus("Checking out " + project + " branch " + branchDate);
                    vcsClient.checkoutBranch(project, branchDate, branchDir);
                    log("Checked out branch dir at revision " + revision);
                    setStatus("Merging " + revision);
                    log("svn merge -r" + (revision-1) + ":" + revision + " && svn commit");
                    CommitInfo info = vcsClient.mergeToBranch(project, revision, branchDate, "", branchDir);
                    log("Merge/commit complete");
                    log(info.toString());
                    return true;
                } catch (VCSException e) {
                    log.error("Failed to merge to branch", e);
                    log("Merge failure: " + e.getMessage());
                } catch (IOException e) {
                    log.error("Failed to merge to branch", e);
                    log("Merge failure: " + e.getMessage());
                }
                setStatus("FAILED");
                return false;
            }

            protected String getTitle() {
                return "Merge " + revision + " to " + project + " Branch " + branchDate;
            }

            @Override
            public void log(String message) {
                super.log(message);
                log.info(message);
            }
        };
        branchJobManager.submit(job);
        setJob(job);
        return SUCCESS;
    }

    public Long getRevision() {
        return revision;
    }

    public void setRevision(Long revision) {
        this.revision = revision;
    }

    public BranchJob getJob() {
        return job;
    }

    public void setJob(BranchJob job) {
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
