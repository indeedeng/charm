package com.indeed.charm.ivy;

import com.indeed.charm.VCSClient;
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
    public String toString() {
        return "{" +
                "org='" + org + '\'' +
                ", name='" + name + '\'' +
                ", rev='" + rev + '\'' +
                ", homeOrg='" + homeOrg + '\'' +
                ", latestRev='" + latestRev + '\'' +
                '}';
    }
}
