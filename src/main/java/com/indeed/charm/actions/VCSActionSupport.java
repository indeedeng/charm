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

import com.opensymphony.xwork2.ActionSupport;
import com.indeed.charm.ProjectsLoader;
import com.indeed.charm.VCSClient;
import com.indeed.charm.ReleaseEnvironment;
import com.indeed.charm.ivy.IvyLoader;

import javax.servlet.ServletContext;

import org.apache.struts2.util.ServletContextAware;

import java.util.Map;

/**
 */
public abstract class VCSActionSupport extends ActionSupport implements ServletContextAware {
    protected ReleaseEnvironment env;
    protected VCSClient vcsClient;
    protected ProjectsLoader projectsLoader;
    protected BackgroundJobManager backgroundJobManager;
    protected Map<String, IvyLoader.DepGraph> depGraphCache;

    protected String project;

    public void setServletContext(ServletContext servletContext) {
        env = (ReleaseEnvironment) servletContext.getAttribute(ReleaseEnvironment.class.getSimpleName());
        vcsClient = (VCSClient) servletContext.getAttribute(VCSClient.class.getSimpleName());
        projectsLoader = (ProjectsLoader) servletContext.getAttribute(ProjectsLoader.class.getSimpleName());
        backgroundJobManager = (BackgroundJobManager) servletContext.getAttribute(BackgroundJobManager.class.getSimpleName());
        depGraphCache = (Map<String, IvyLoader.DepGraph>) servletContext.getAttribute("DepGraphCache");
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public boolean isIvyEnabled() {
        return env.isIvyEnabled();
    }
}
