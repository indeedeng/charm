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

package com.indeed.charm.model;

import java.util.Map;
import java.util.Date;
import java.util.Collection;

/**
 */
public interface LogEntry {
    public long getRevision();
    public String getRevisionUrl();
    public String getAuthor();
    public String getLogMessage();
    public Date getDate();
    public Map<String, LogEntryPath> getPaths();

    public void setLogMessage(String logMessage);
    public Collection<LogEntry> getBranchMergeRevisions();
    public void setBranchMergeRevisions(Collection<LogEntry> revisions);

    public void setLogMessageMatches(Collection<String> matches);
    public Collection<String> getLogMessageMatches();
}
