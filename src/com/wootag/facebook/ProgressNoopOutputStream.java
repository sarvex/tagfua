/**
 * Copyright 2010-present Facebook. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package com.wootag.facebook;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import android.os.Handler;

class ProgressNoopOutputStream extends OutputStream implements RequestOutputStream {

    private final Map<Request, RequestProgress> progressMap = new HashMap<Request, RequestProgress>();
    private final Handler callbackHandler;

    private Request currentRequest;
    private RequestProgress currentRequestProgress;
    private int batchMax;

    ProgressNoopOutputStream(final Handler callbackHandler) {

        this.callbackHandler = callbackHandler;
    }

    @Override
    public void setCurrentRequest(final Request currentRequest) {

        this.currentRequest = currentRequest;
        this.currentRequestProgress = currentRequest != null ? this.progressMap.get(currentRequest) : null;
    }

    @Override
    public void write(final byte[] buffer) {

        this.addProgress(buffer.length);
    }

    @Override
    public void write(final byte[] buffer, final int offset, final int length) {

        this.addProgress(length);
    }

    @Override
    public void write(final int oneByte) {

        this.addProgress(1);
    }

    void addProgress(final long size) {

        if (this.currentRequestProgress == null) {
            this.currentRequestProgress = new RequestProgress(this.callbackHandler, this.currentRequest);
            this.progressMap.put(this.currentRequest, this.currentRequestProgress);
        }

        this.currentRequestProgress.addToMax(size);
        this.batchMax += size;
    }

    int getMaxProgress() {

        return this.batchMax;
    }

    Map<Request, RequestProgress> getProgressMap() {

        return this.progressMap;
    }
}
