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
