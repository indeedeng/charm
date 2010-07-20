package com.indeed.charm.actions;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.File;

import com.indeed.charm.VCSClient;
import com.indeed.charm.VCSException;

/**
 */
public class CheckoutBranchAction extends BaseBranchAction {
    private static Logger log = Logger.getLogger(CheckoutBranchAction.class);

    private Long jobId;
    private String user;
    private String password;
    private Long revision;
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
        final File branchDir = env.getBranchWorkingDirectory(project, branchDate, user);
        final VCSClient vcsClient = env.newClient(env, user, password);
        final BackgroundJob<Boolean> job = new BackgroundJob<Boolean>() {
            public Boolean call() throws Exception {
                try {
                    log("Working Dir: " + branchDir);
                    setStatus("Checking out " + project + " branch " + branchDate);
                    final long revision = vcsClient.checkoutBranch(project, branchDate, branchDir);
                    setRevision(revision);
                    log("Checked out branch dir at revision " + revision);
                    return true;
                } catch (VCSException e) {
                    log.error("Failed to checkout branch", e);
                    log("Checkout failure: " + e.getMessage());
                } catch (IOException e) {
                    log.error("Failed to checkout branch", e);
                    log("Checkout failure: " + e.getMessage());
                }
                setStatus("FAILED");
                return false;
            }

            public String getTitle() {
                return "Checkout " + project + " Branch " + branchDate;
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

    public Long getRevision() {
        return revision;
    }

    public void setRevision(Long revision) {
        this.revision = revision;
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
