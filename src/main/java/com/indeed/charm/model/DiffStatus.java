package com.indeed.charm.model;

import java.io.File;

/**
 */
public interface DiffStatus {
    File getFile();
    String getKind();
    String getModificationType();
    String getPath();
    String getUrl();
}
