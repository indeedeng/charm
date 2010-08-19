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

import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 */
public class BackgroundJobManager {

    private final List<BackgroundJob> backgroundJobs = Lists.newLinkedList();
    // TODO: configurable thread pool
    private final ExecutorService service = Executors.newFixedThreadPool(10, new ThreadFactory() {
        public Thread newThread(Runnable r) {
            return new Thread(null, r, BackgroundJobManager.class.getSimpleName());
        }
    });
    private final Map<Long, BackgroundJob> history = new MapMaker()
            .softValues()
            .makeMap();
    private final AtomicLong lastId = new AtomicLong(0);

    public <T> void submit(BackgroundJob<T> job) {
        long id = lastId.incrementAndGet();
        job.setId(id);
        Future<T> future = service.submit(job);
        job.setFuture(future);
        backgroundJobs.add(job);
        history.put(id, job);
    }

    public List<BackgroundJob> getRecentJobs() {
        List<BackgroundJob> recent = Lists.newArrayListWithCapacity(backgroundJobs.size());
        ListIterator<BackgroundJob> jobs = backgroundJobs.listIterator();
        while (jobs.hasNext()) {
            BackgroundJob job = jobs.next();
            recent.add(job); // inactive jobs get to be returned once...
            if (job.getFuture().isDone() || job.getFuture().isCancelled()) {
                jobs.remove();
            }
        }
        return recent;
    }

    @SuppressWarnings("unchecked")
    public <T> BackgroundJob<T> getJobForId(long id) {
        return history.get(id);
    }

}
