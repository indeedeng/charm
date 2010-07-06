package com.indeed.charm.model;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: jackh
 * Date: Jul 5, 2010
 * Time: 6:22:59 PM
 * To change this template use File | Settings | File Templates.
 */
public interface CommitInfo {
    long getNewRevision();

    String getAuthor();

    Date getDate();

    String getErrorMessage();
}
