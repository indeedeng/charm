package com.indeed.charm.actions;

import com.indeed.charm.VCSClient;

import java.util.List;

/**
 */
public class ListTagsAction extends VCSActionSupport {

    private List<String> tags;

    @Override
    public String execute() throws Exception {
        tags = vcsClient.listTags(project, 20, VCSClient.Ordering.REVERSE_VERSION);
        return SUCCESS;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
