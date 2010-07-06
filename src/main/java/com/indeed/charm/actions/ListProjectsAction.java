package com.indeed.charm.actions;

import com.indeed.charm.actions.VCSActionSupport;
import com.indeed.charm.model.Project;

import java.util.List;

/**
 */
public class ListProjectsAction extends VCSActionSupport {

    private List<Project> releaseProjects;
    private List<Project> libraryProjects;

    @Override
    public String execute() throws Exception {
        projectsLoader.waitForLoad();
        releaseProjects = projectsLoader.getReleaseProjects();
        libraryProjects = projectsLoader.getLibraryProjects();
        return SUCCESS;
    }

    public List<Project> getReleaseProjects() {
        return releaseProjects;
    }

    public List<Project> getLibraryProjects() {
        return libraryProjects;
    }
}
