/*
 * Copyright (C) 2011 Indeed Inc.
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

package com.indeed.charm.issues.jira;

import com.indeed.charm.ReleaseEnvironment;
import com.indeed.charm.issues.IssueTracker;
import com.indeed.jira.JiraController;
import com.indeed.jira.JiraQueryBuilder;
import com.indeed.ws.jira.model.RemoteIssue;
import com.indeed.ws.jira.model.RemoteVersion;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.apache.axis.AxisFault;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author jack@indeed.com (Jack Humphrey)
 */
public class JiraIssueTracker implements IssueTracker {
    private static final Logger log = Logger.getLogger(JiraIssueTracker.class);

    private JiraController controller;
    private MessageFormat fixVersionLinkFormat;

    public boolean init(ReleaseEnvironment env) {
        try {
            controller = new JiraController(env.getJiraUrl(), env.getJiraUser(), env.getJiraPassword());
        } catch (Exception e) {
            log.error("failed to initialize JIRA", e);
            return false;
        }
        fixVersionLinkFormat = new MessageFormat(env.getJiraFixVersionLink());
        return true;
    }

    public Map<String, Map<String, String>> loadDataForKeys(Set<String> keys) {
        JiraQueryBuilder query = getQueryBuilderForKeys(keys);
        final ImmutableMap.Builder<String, Map<String, String>> issueMap = ImmutableMap.builder();
        RemoteIssue[] results = null;
        try {
            results = controller.queryJira(query);
        } catch (AxisFault e) {
            // try removing problem keys first
            final String faultString = e.getFaultString();
            if (faultString != null) {
                log.info(faultString);
                boolean removed = false;
                for (Iterator<String> iterator = keys.iterator(); iterator.hasNext(); ) {
                    final String testKey = iterator.next();
                    if (faultString.contains("'" + testKey + "'")) {
                        iterator.remove();
                        removed = true;
                    }
                }
                if (removed) {
                    query = getQueryBuilderForKeys(keys);
                    try {
                        results = controller.queryJira(query);
                    } catch (Exception e2) {
                        // fall through to queryForEachKey below
                    }
                }
            }
            if (results == null) {
                results = queryForEachKey(keys);
            }
        } catch (RemoteException e) {
            results = queryForEachKey(keys);
        }
        if (results == null) {
            return issueMap.build();
        }
        for (RemoteIssue issue : results) {
            final String issueKey = issue.getKey();
            final String issueVersionHtml = getIssueVersionHtml(issue.getProject(), issue.getFixVersions());
            issueMap.put(issueKey, ImmutableMap.of("Fix Version", issueVersionHtml));
        }
        return issueMap.build();
    }

    private JiraQueryBuilder getQueryBuilderForKeys(Set<String> keys) {
        Iterator<String> iterator = keys.iterator();
        String[] addlKeys = new String[keys.size() - 1];
        String key = iterator.next();
        for (int i = 0; iterator.hasNext(); i++) {
            addlKeys[i] = iterator.next();
        }
        return new JiraQueryBuilder()
                .keyIn(key, addlKeys)
                .orderBy(JiraQueryBuilder.Field.KEY);
    }

    private RemoteIssue[] queryForEachKey(Set<String> keys) {
        List<RemoteIssue> results = Lists.newArrayListWithExpectedSize(keys.size());
        for (String key : keys) {
            final JiraQueryBuilder query = new JiraQueryBuilder()
                    .key(key);
            try {
                RemoteIssue[] r = controller.queryJira(query);
                if (r.length > 0) {
                    results.add(r[0]);
                }
            } catch (RemoteException e) {
                log.warn("JIRA lookup failed for " + key);
            }
        }
        return results.toArray(new RemoteIssue[0]);
    }

    private String getIssueVersionHtml(String project, RemoteVersion[] fixVersions) {
        final List<String> htmlLinks = Lists.newArrayListWithExpectedSize(fixVersions.length);
        for (RemoteVersion v : fixVersions) {
            final String id = v.getId();
            final String name = v.getName();
            final String link = fixVersionLinkFormat.format(new Object[] { project, id, name });
            htmlLinks.add(link);
        }
        return Joiner.on(", ").join(htmlLinks);
    }
}
