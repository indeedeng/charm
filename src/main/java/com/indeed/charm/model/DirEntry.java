package com.indeed.charm.model;

import java.util.Date;

/**
 */
public interface DirEntry {
    public String getName();
    public String getAuthor();
    public Date getDate();
    public long getRevision();
    public long getSize();
}
