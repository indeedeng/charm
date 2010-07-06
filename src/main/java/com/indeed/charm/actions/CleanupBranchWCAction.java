package com.indeed.charm.actions;

import java.io.File;

import org.apache.log4j.Logger;

/**
 */
public class CleanupBranchWCAction extends BaseBranchAction {
    private static Logger log = Logger.getLogger(CleanupBranchWCAction.class);
    
    private String user;

    private void del(File dir) {
        for (File file : dir.listFiles()) {
            if (!file.isDirectory()) {
                log.info("delete " + file.getAbsolutePath());
                file.delete();
            } else {
                del(file);
            }
        }
        for (File file : dir.listFiles()) {
            log.info("delete " + file.getAbsolutePath());
            file.delete();
        }
    }

    @Override
    public String execute() throws Exception {
        final File branchDir = env.getBranchWorkingDirectory(project, branchDate, user);
        BranchJob job = new BranchJob() {
            protected String getTitle() {
                return "Cleanup Working Directory " + branchDir;
            }

            public Boolean call() throws Exception {
                try {
                    del(branchDir);
                } catch (Exception e) {
                    log.error("Failed to clean up working copy " + branchDir, e);
                }
                return true;
            }
        };
        branchJobManager.submit(job);
        return SUCCESS;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
