package com.indeed.charm.actions;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 */
public abstract class BranchJob implements Callable<Boolean> {
    private Future<Boolean> future;
    private String status = "PENDING";
    private final StringBuilder log = new StringBuilder();

    private Long id;

    public void log(String message) {
        log.append(message).append("\n");
    }

    public String getLog() {
        return log.toString();
    }
    
    public String getStatus() {
        if (future != null) {
            if (future.isDone()) {
                setStatus("DONE");
            } else if (future.isCancelled()) {
                setStatus("CANCELLED");
            }
        }
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Future<Boolean> getFuture() {
        return future;
    }

    public void setFuture(Future<Boolean> future) {
        this.future = future;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String toString() {
        return id + ": " + status;
    }

    public boolean isRunning() {
        return future == null || (!future.isDone() && !future.isCancelled());
    }

    protected abstract String getTitle();
}
