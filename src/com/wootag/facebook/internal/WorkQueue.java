/**
 * Copyright 2010-present Facebook. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package com.wootag.facebook.internal;

import java.util.concurrent.Executor;

import com.wootag.facebook.Settings;

class WorkQueue {

    public static final int DEFAULT_MAX_CONCURRENT = 8;

    protected final Object workLock = new Object();
    protected WorkNode pendingJobs;

    private final int maxConcurrent;
    private final Executor executor;

    private WorkNode runningJobs;
    private int runningCount;

    WorkQueue() {

        this(DEFAULT_MAX_CONCURRENT);
    }

    WorkQueue(final int maxConcurrent) {

        this(maxConcurrent, Settings.getExecutor());
    }

    WorkQueue(final int maxConcurrent, final Executor executor) {

        this.maxConcurrent = maxConcurrent;
        this.executor = executor;
    }

    private void execute(final WorkNode node) {

        this.executor.execute(new Runnable() {

            @Override
            public void run() {

                try {
                    node.getCallback().run();
                } finally {
                    WorkQueue.this.finishItemAndStartNew(node);
                }
            }
        });
    }

    private void startItem() {

        this.finishItemAndStartNew(null);
    }

    WorkItem addActiveWorkItem(final Runnable callback) {

        return this.addActiveWorkItem(callback, true);
    }

    WorkItem addActiveWorkItem(final Runnable callback, final boolean addToFront) {

        final WorkNode node = new WorkNode(callback);
        synchronized (this.workLock) {
            this.pendingJobs = node.addToList(this.pendingJobs, addToFront);
        }

        this.startItem();
        return node;
    }

    void finishItemAndStartNew(final WorkNode finished) {

        WorkNode ready = null;

        synchronized (this.workLock) {
            if (finished != null) {
                this.runningJobs = finished.removeFromList(this.runningJobs);
                this.runningCount--;
            }

            if (this.runningCount < this.maxConcurrent) {
                ready = this.pendingJobs; // Head of the pendingJobs queue
                if (ready != null) {
                    // The Queue reassignments are necessary since 'ready' might have been
                    // added / removed from the front of either queue, which changes its
                    // respective head.
                    this.pendingJobs = ready.removeFromList(this.pendingJobs);
                    this.runningJobs = ready.addToList(this.runningJobs, false);
                    this.runningCount++;

                    ready.setIsRunning(true);
                }
            }
        }

        if (ready != null) {
            this.execute(ready);
        }
    }

    void validate() {

        synchronized (this.workLock) {
            // Verify that all running items know they are running, and counts match
            int count = 0;

            if (this.runningJobs != null) {
                WorkNode walk = this.runningJobs;
                do {
                    walk.verify(true);
                    count++;
                    walk = walk.getNext();
                } while (walk != this.runningJobs);
            }

            assert this.runningCount == count;
        }
    }

    private class WorkNode implements WorkItem {

        private final Runnable callback;
        private WorkNode next;
        private WorkNode prev;
        private boolean isRunning;

        WorkNode(final Runnable callback) {

            this.callback = callback;
        }

        @Override
        public boolean cancel() {

            synchronized (WorkQueue.this.workLock) {
                if (!this.isRunning()) {
                    WorkQueue.this.pendingJobs = this.removeFromList(WorkQueue.this.pendingJobs);
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean isRunning() {

            return this.isRunning;
        }

        @Override
        public void moveToFront() {

            synchronized (WorkQueue.this.workLock) {
                if (!this.isRunning()) {
                    WorkQueue.this.pendingJobs = this.removeFromList(WorkQueue.this.pendingJobs);
                    WorkQueue.this.pendingJobs = this.addToList(WorkQueue.this.pendingJobs, true);
                }
            }
        }

        WorkNode addToList(WorkNode list, final boolean addToFront) {

            assert this.next == null;
            assert this.prev == null;

            if (list == null) {
                list = this.next = this.prev = this;
            } else {
                this.next = list;
                this.prev = list.prev;
                this.next.prev = this.prev.next = this;
            }

            return addToFront ? this : list;
        }

        Runnable getCallback() {

            return this.callback;
        }

        WorkNode getNext() {

            return this.next;
        }

        WorkNode removeFromList(WorkNode list) {

            assert this.next != null;
            assert this.prev != null;

            if (list == this) {
                if (this.next == this) {
                    list = null;
                } else {
                    list = this.next;
                }
            }

            this.next.prev = this.prev;
            this.prev.next = this.next;
            this.next = this.prev = null;

            return list;
        }

        void setIsRunning(final boolean isRunning) {

            this.isRunning = isRunning;
        }

        void verify(final boolean shouldBeRunning) {

            assert this.prev.next == this;
            assert this.next.prev == this;
            assert this.isRunning() == shouldBeRunning;
        }
    }

    interface WorkItem {

        boolean cancel();

        boolean isRunning();

        void moveToFront();
    }
}
