package com.indeed.charm.actions;

import com.opensymphony.xwork2.ActionSupport;
import com.indeed.charm.ProjectsLoader;
import com.indeed.charm.VCSClient;
import com.indeed.charm.ReleaseEnvironment;
import com.indeed.charm.LinkifyPattern;

import javax.servlet.ServletContext;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.struts2.util.ServletContextAware;

/**
 */
public abstract class VCSActionSupport extends ActionSupport implements ServletContextAware {
    protected ReleaseEnvironment env;
    protected VCSClient vcsClient;
    protected ProjectsLoader projectsLoader;
    protected BranchJobManager branchJobManager;

    protected String project;

    public void setServletContext(ServletContext servletContext) {
        env = (ReleaseEnvironment) servletContext.getAttribute(ReleaseEnvironment.class.getSimpleName());
        vcsClient = (VCSClient) servletContext.getAttribute(VCSClient.class.getSimpleName());
        projectsLoader = (ProjectsLoader) servletContext.getAttribute(ProjectsLoader.class.getSimpleName());
        branchJobManager = (BranchJobManager) servletContext.getAttribute(BranchJobManager.class.getSimpleName());
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
