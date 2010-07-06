package com.indeed.charm.model;

/**
 */
public class Project {
    private final String name;
    private final boolean releaseBranches;
    private final boolean publishedTags;

    public Project(String name, boolean releaseBranches, boolean publishedTags) {
        this.name = name;
        this.releaseBranches = releaseBranches;
        this.publishedTags = publishedTags;
    }
    
    public String getName() {
        return name;
    }

    public boolean isReleaseBranches() {
        return releaseBranches;
    }

    public boolean isPublishedTags() {
        return publishedTags;
    }
}
