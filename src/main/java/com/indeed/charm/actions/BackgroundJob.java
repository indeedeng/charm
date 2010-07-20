package com.indeed.charm.actions;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 */
public abstract class BackgroundJob<T> implements Callable<T> {
    private Future<T> future;
    private String status = "PENDING";
    protected final StringBuilder logBuilder = new StringBuilder();

    private Long id;

    public void log(String message) {
        logBuilder.append(message).append("\n");
    }

    public String getLog() {
        return logBuilder.toString();
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

    public Future<T> getFuture() {
        return future;
    }

    public void setFuture(Future<T> future) {
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

    public abstract String getTitle();
}
