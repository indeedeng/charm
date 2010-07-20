package com.indeed.charm.actions;

import org.apache.log4j.Logger;
import com.google.common.collect.ImmutableList;
import com.indeed.charm.model.LogEntry;
import com.indeed.charm.VCSClient;
import com.indeed.charm.VCSException;

import java.util.List;
import java.util.Map;

/**
 */
public class LogTrunkSinceTagAction extends VCSActionSupport {
    private static Logger log = Logger.getLogger(LogTrunkSinceTagAction.class);

    private String tag;
    private String path;
    private List<LogEntry> logEntries;
    private Map<Long, String> logMessages;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<LogEntry> getLogEntries() {
        return logEntries;
    }

    public void setLogEntries(List<LogEntry> logEntries) {
        this.logEntries = logEntries;
    }

    public Map<Long, String> getLogMessages() {
        return logMessages;
    }

    public void setLogMessages(Map<Long, String> logMessages) {
        this.logMessages = logMessages;
    }

    @Override
    public String execute() throws Exception {
        try {
            if (tag == null) {
                tag = vcsClient.listTags(project, 1, VCSClient.Ordering.REVERSE_AGE).get(0).getName();
            }
            if (path == null) {
                path = ".";
            }

            final DisplayLogVisitor logVisitor = new DisplayLogVisitor(env);
            vcsClient.visitTrunkChangeLogSinceTag(logVisitor, getProject(), getTag(), 0, getPath());
            setLogEntries(ImmutableList.copyOf(logVisitor.getEntries().values()));
        } catch (VCSException e) {
            log.error("Failed to get trunk log since branch", e);
        }
        return SUCCESS;
    }

    public String getName() {
        return project + env.getTrunkPath();
    }

    public String getSince() {
        return "tag " + tag;
    }
}
