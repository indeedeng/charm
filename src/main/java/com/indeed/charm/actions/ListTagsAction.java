package com.indeed.charm.actions;

import com.indeed.charm.VCSClient;
import com.indeed.charm.VCSException;
import com.indeed.charm.model.DiffStatus;

import java.util.List;

import org.apache.log4j.Logger;

/**
 */
public class ListTagsAction extends VCSActionSupport {
    private static Logger log = Logger.getLogger(ListTagsAction.class);

    private List<String> tags;
    private boolean trunkDifferent;

    @Override
    public String execute() throws Exception {
        tags = vcsClient.listTags(project, 20, VCSClient.Ordering.REVERSE_AGE);
        if (tags.size() > 0) {
            try {
                vcsClient.visitTagToTrunkDiffStatus(new DiffStatusVisitor() {
                    public boolean visit(DiffStatus diffStatus) {
                        if (!"none".equals(diffStatus.getModificationType())) {
                            trunkDifferent = true;
                            return false;
                        }
                        return true;
                    }
                }, getProject(), tags.get(0));
            } catch (VCSException e) {
                log.error("Error checking trunk diffs for tag " + tags.get(0));
            }
        }
        return SUCCESS;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public boolean isTrunkDifferent() {
        return trunkDifferent;
    }
}
