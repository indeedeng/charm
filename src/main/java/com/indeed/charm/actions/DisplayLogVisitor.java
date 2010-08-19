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

package com.indeed.charm.actions;

import com.indeed.charm.model.LogEntry;
import com.indeed.charm.ReleaseEnvironment;
import com.google.common.collect.Ordering;
import com.google.common.collect.Maps;
import com.google.common.collect.Lists;

import java.util.TreeMap;
import java.util.List;

public class DisplayLogVisitor implements LogEntryVisitor {
    private final TreeMap<Long, LogEntry> entries = Maps.newTreeMap(Ordering.natural().reverse());
    private final ReleaseEnvironment env;

    public DisplayLogVisitor(ReleaseEnvironment env) {
        this.env = env;
    }

    public void visit(LogEntry entry) {
        List<String> applied = Lists.newArrayList();
        entry.setLogMessage(env.linkify(entry.getLogMessage(), applied));
        entry.setLogMessageMatches(applied);
        entries.put(entry.getRevision(), entry);
    }

    public TreeMap<Long, LogEntry> getEntries() {
        return entries;
    }
}