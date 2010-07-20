package com.indeed.charm.actions;

import com.indeed.charm.VCSClient;
import com.indeed.charm.VCSException;
import com.indeed.charm.model.DiffStatus;
import com.indeed.charm.model.DirEntry;

import java.util.List;

import org.apache.log4j.Logger;

/**
 */
public class ListTagsAction extends VCSActionSupport {
    private List<DirEntry> tags;
    private boolean trunkDifferent;

    @Override
    public String execute() throws Exception {
        // TODO: make sort style configurable in charm.properties
        tags = vcsClient.listTags(project, 20, VCSClient.Ordering.REVERSE_AGE);
        if (tags.size() > 0) {
            trunkDifferent = vcsClient.hasTrunkCommitsSinceTag(getProject(), tags.get(0).getName());
        }
        return SUCCESS;
    }

    public List<DirEntry> getTags() {
        return tags;
    }

    public boolean isTrunkDifferent() {
        return trunkDifferent;
    }
}
