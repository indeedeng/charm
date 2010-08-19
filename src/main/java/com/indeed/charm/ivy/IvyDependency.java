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

package com.indeed.charm.ivy;

import com.indeed.charm.ReleaseEnvironment;

/**
 */
public class IvyDependency implements Comparable<IvyDependency> {
    private final String org;
    private final String name;
    private final String path;
    private final String rev;
    private final boolean homeOrg;
    private final String latestRev;

    public IvyDependency(String org, String name, String path, String rev, boolean homeOrg, String latestRev) {
        this.org = org;
        this.name = name;
        this.path = path;
        this.rev = "latest.integration".equals(rev) && homeOrg ? latestRev : rev;
        this.homeOrg = homeOrg;
        this.latestRev = latestRev;
    }

    public String getOrg() {
        return org;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getRev() {
        return rev;
    }

    public boolean isHomeOrg() {
        return homeOrg;
    }

    public String getLatestRev() {
        return latestRev;
    }

    public int compareTo(IvyDependency other) {
        int c = org.compareTo(other.org);
        if (c == 0) {
            c = name.compareTo(other.name);
        }
        if (c == 0) {
            c = ReleaseEnvironment.normalizeVersion(rev, 4).compareTo(ReleaseEnvironment.normalizeVersion(other.rev, 4));
        }
        return c;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IvyDependency that = (IvyDependency) o;

        if (homeOrg != that.homeOrg) return false;
        if (latestRev != null ? !latestRev.equals(that.latestRev) : that.latestRev != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (org != null ? !org.equals(that.org) : that.org != null) return false;
        if (path != null ? !path.equals(that.path) : that.path != null) return false;
        if (rev != null ? !rev.equals(that.rev) : that.rev != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = org != null ? org.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (rev != null ? rev.hashCode() : 0);
        result = 31 * result + (homeOrg ? 1 : 0);
        result = 31 * result + (latestRev != null ? latestRev.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return org + '/' + name + ';' + rev;
    }
}
