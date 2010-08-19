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
        BackgroundJob<Boolean> job = new BackgroundJob<Boolean>() {
            public String getTitle() {
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
        backgroundJobManager.submit(job);
        return SUCCESS;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
