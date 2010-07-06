package com.indeed.charm.actions;

import com.indeed.charm.model.DiffStatus;

/**
 */
public interface DiffStatusVisitor {
    void visit(DiffStatus diffStatus);
}
