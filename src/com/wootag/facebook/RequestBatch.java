/**
 * Copyright 2010-present Facebook. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package com.TagFu.facebook;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import android.os.Handler;

/**
 * RequestBatch contains a list of Request objects that can be sent to Facebook in a single round-trip.
 */
public class RequestBatch extends AbstractList<Request> {

    private static AtomicInteger idGenerator = new AtomicInteger();

    private Handler callbackHandler;
    private List<Request> requests = new ArrayList<Request>();
    private int timeoutInMilliseconds;
    private final String id = String.valueOf(idGenerator.incrementAndGet());
    private List<Callback> callbacks = new ArrayList<Callback>();
    private String batchApplicationId;

    /**
     * Constructor. Creates an empty batch.
     */
    public RequestBatch() {

        this.requests = new ArrayList<Request>();
    }

    /**
     * Constructor.
     *
     * @param requests the requests to add to the batch
     */
    public RequestBatch(final Collection<Request> requests) {

        this.requests = new ArrayList<Request>(requests);
    }

    /**
     * Constructor.
     *
     * @param requests the requests to add to the batch
     */
    public RequestBatch(final Request... requests) {

        this.requests = Arrays.asList(requests);
    }

    /**
     * Constructor.
     *
     * @param requests the requests to add to the batch
     */
    public RequestBatch(final RequestBatch requests) {

        this.requests = new ArrayList<Request>(requests);
        this.callbackHandler = requests.callbackHandler;
        this.timeoutInMilliseconds = requests.timeoutInMilliseconds;
        this.callbacks = new ArrayList<Callback>(requests.callbacks);
    }

    @Override
    public final void add(final int location, final Request request) {

        this.requests.add(location, request);
    }

    @Override
    public final boolean add(final Request request) {

        return this.requests.add(request);
    }

    /**
     * Adds a batch-level callback which will be called when the entire batch has finished executing.
     *
     * @param callback the callback
     */
    public void addCallback(final Callback callback) {

        if (!this.callbacks.contains(callback)) {
            this.callbacks.add(callback);
        }
    }

    @Override
    public final void clear() {

        this.requests.clear();
    }

    /**
     * Executes this batch on the current thread and returns the responses.
     * <p/>
     * This should only be used if you have transitioned off the UI thread.
     *
     * @return a list of Response objects representing the results of the requests; responses are returned in the same
     *         order as the requests were specified.
     * @throws FacebookException If there was an error in the protocol used to communicate with the service
     * @throws IllegalArgumentException if the passed in RequestBatch is empty
     * @throws NullPointerException if the passed in RequestBatch or any of its contents are null
     */
    public final List<Response> executeAndWait() {

        return this.executeAndWaitImpl();
    }

    /**
     * Executes this batch asynchronously. This function will return immediately, and the batch will be processed on a
     * separate thread. In order to process results of a request, or determine whether a request succeeded or failed, a
     * callback must be specified (see {@link Request#setCallback(com.wTagFufacebook.Request.Callback)})
     * <p/>
     * This should only be called from the UI thread.
     *
     * @return a RequestAsyncTask that is executing the request
     * @throws IllegalArgumentException if this batch is empty
     * @throws NullPointerException if any of the contents of this batch are null
     */
    public final RequestAsyncTask executeAsync() {

        return this.executeAsyncImpl();
    }

    @Override
    public final Request get(final int i) {

        return this.requests.get(i);
    }

    /**
     * Gets the timeout to wait for responses from the server before a timeout error occurs.
     *
     * @return the timeout, in milliseconds; 0 (the default) means do not timeout
     */
    public int getTimeout() {

        return this.timeoutInMilliseconds;
    }

    @Override
    public final Request remove(final int location) {

        return this.requests.remove(location);
    }

    /**
     * Removes a batch-level callback.
     *
     * @param callback the callback
     */
    public void removeCallback(final Callback callback) {

        this.callbacks.remove(callback);
    }

    @Override
    public final Request set(final int location, final Request request) {

        return this.requests.set(location, request);
    }

    /**
     * Sets the timeout to wait for responses from the server before a timeout error occurs.
     *
     * @param timeoutInMilliseconds the timeout, in milliseconds; 0 means do not timeout
     */
    public void setTimeout(final int timeoutInMilliseconds) {

        if (timeoutInMilliseconds < 0) {
            throw new IllegalArgumentException("Argument timeoutInMilliseconds must be >= 0.");
        }
        this.timeoutInMilliseconds = timeoutInMilliseconds;
    }

    @Override
    public final int size() {

        return this.requests.size();
    }

    List<Response> executeAndWaitImpl() {

        return Request.executeBatchAndWait(this);
    }

    RequestAsyncTask executeAsyncImpl() {

        return Request.executeBatchAsync(this);
    }

    final String getBatchApplicationId() {

        return this.batchApplicationId;
    }

    final Handler getCallbackHandler() {

        return this.callbackHandler;
    }

    final List<Callback> getCallbacks() {

        return this.callbacks;
    }

    final String getId() {

        return this.id;
    }

    final List<Request> getRequests() {

        return this.requests;
    }

    final void setBatchApplicationId(final String batchApplicationId) {

        this.batchApplicationId = batchApplicationId;
    }

    final void setCallbackHandler(final Handler callbackHandler) {

        this.callbackHandler = callbackHandler;
    }

    /**
     * Specifies the interface that consumers of the RequestBatch class can implement in order to be notified when the
     * entire batch completes execution. It will be called after all per-Request callbacks are called.
     */
    public interface Callback {

        /**
         * The method that will be called when a batch completes.
         *
         * @param batch the RequestBatch containing the Requests which were executed
         */
        void onBatchCompleted(RequestBatch batch);
    }

    /**
     * Specifies the interface that consumers of the RequestBatch class can implement in order to be notified when the
     * batch makes progress. The frequency of the callbacks can be controlled using
     * {@link com.wootag.facebook.Settings#setOnProgressThreshold(long)}.
     */
    public interface OnProgressCallback extends Callback {

        /**
         * The method that will be called when a batch makes progress.
         *
         * @param batch the RequestBatch containing the Requests which were executed
         * @param current the current value of the progress
         * @param max the max (target) value of the progress
         */
        void onBatchProgress(RequestBatch batch, long current, long max);
    }
}
