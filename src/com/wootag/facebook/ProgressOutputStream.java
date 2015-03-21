/**
 * Copyright 2010-present Facebook. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package com.TagFu.facebook;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import android.os.Handler;

class ProgressOutputStream extends FilterOutputStream implements RequestOutputStream {

    private final Map<Request, RequestProgress> progressMap;
    protected final RequestBatch requests;
    private final long threshold;

    protected long batchProgress, lastReportedProgress;
    protected final long maxProgress;
    private RequestProgress currentRequestProgress;

    ProgressOutputStream(final OutputStream out, final RequestBatch requests,
            final Map<Request, RequestProgress> progressMap, final long maxProgress) {

        super(out);
        this.requests = requests;
        this.progressMap = progressMap;
        this.maxProgress = maxProgress;

        this.threshold = Settings.getOnProgressThreshold();
    }

    @Override
    public void close() throws IOException {

        super.close();

        for (final RequestProgress p : this.progressMap.values()) {
            p.reportProgress();
        }

        this.reportBatchProgress();
    }

    @Override
    public void setCurrentRequest(final Request request) {

        this.currentRequestProgress = request != null ? this.progressMap.get(request) : null;
    }

    @Override
    public void write(final byte[] buffer) throws IOException {

        this.out.write(buffer);
        this.addProgress(buffer.length);
    }

    @Override
    public void write(final byte[] buffer, final int offset, final int length) throws IOException {

        this.out.write(buffer, offset, length);
        this.addProgress(length);
    }

    @Override
    public void write(final int oneByte) throws IOException {

        this.out.write(oneByte);
        this.addProgress(1);
    }

    private void addProgress(final long size) {

        if (this.currentRequestProgress != null) {
            this.currentRequestProgress.addProgress(size);
        }

        this.batchProgress += size;

        if ((this.batchProgress >= (this.lastReportedProgress + this.threshold))
                || (this.batchProgress >= this.maxProgress)) {
            this.reportBatchProgress();
        }
    }

    private void reportBatchProgress() {

        if (this.batchProgress > this.lastReportedProgress) {
            for (final RequestBatch.Callback callback : this.requests.getCallbacks()) {
                if (callback instanceof RequestBatch.OnProgressCallback) {
                    final Handler callbackHandler = this.requests.getCallbackHandler();

                    // Keep copies to avoid threading issues
                    final RequestBatch.OnProgressCallback progressCallback = (RequestBatch.OnProgressCallback) callback;
                    if (callbackHandler == null) {
                        progressCallback.onBatchProgress(this.requests, this.batchProgress, this.maxProgress);
                    } else {
                        callbackHandler.post(new Runnable() {

                            @Override
                            public void run() {

                                progressCallback.onBatchProgress(ProgressOutputStream.this.requests,
                                        ProgressOutputStream.this.batchProgress, ProgressOutputStream.this.maxProgress);
                            }
                        });
                    }
                }
            }

            this.lastReportedProgress = this.batchProgress;
        }
    }

    long getBatchProgress() {

        return this.batchProgress;
    }

    long getMaxProgress() {

        return this.maxProgress;
    }
}
