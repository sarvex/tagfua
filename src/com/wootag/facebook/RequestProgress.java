/**
 * Copyright 2010-present Facebook. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package com.TagFu.facebook;

import android.os.Handler;

class RequestProgress {

    private final Request request;
    private final Handler callbackHandler;
    private final long threshold;

    private long progress, lastReportedProgress, maxProgress;

    RequestProgress(final Handler callbackHandler, final Request request) {

        this.request = request;
        this.callbackHandler = callbackHandler;

        this.threshold = Settings.getOnProgressThreshold();
    }

    void addProgress(final long size) {

        this.progress += size;

        if ((this.progress >= (this.lastReportedProgress + this.threshold)) || (this.progress >= this.maxProgress)) {
            this.reportProgress();
        }
    }

    void addToMax(final long size) {

        this.maxProgress += size;
    }

    long getMaxProgress() {

        return this.maxProgress;
    }

    long getProgress() {

        return this.progress;
    }

    void reportProgress() {

        if (this.progress > this.lastReportedProgress) {
            final Request.Callback callback = this.request.getCallback();
            if ((this.maxProgress > 0) && (callback instanceof Request.OnProgressCallback)) {
                // Keep copies to avoid threading issues
                final long currentCopy = this.progress;
                final long maxProgressCopy = this.maxProgress;
                final Request.OnProgressCallback callbackCopy = (Request.OnProgressCallback) callback;
                if (this.callbackHandler == null) {
                    callbackCopy.onProgress(currentCopy, maxProgressCopy);
                } else {
                    this.callbackHandler.post(new Runnable() {

                        @Override
                        public void run() {

                            callbackCopy.onProgress(currentCopy, maxProgressCopy);
                        }
                    });
                }
                this.lastReportedProgress = this.progress;
            }
        }
    }
}
