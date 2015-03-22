/**
 * Copyright 2010-present Facebook. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package com.TagFu.facebook.widget;

import android.content.Context;
import android.content.Loader;
import android.os.Handler;

import com.TagFu.facebook.FacebookException;
import com.TagFu.facebook.FacebookRequestError;
import com.TagFu.facebook.Request;
import com.TagFu.facebook.RequestBatch;
import com.TagFu.facebook.Response;
import com.TagFu.facebook.internal.CacheableRequestBatch;
import com.TagFu.facebook.model.GraphObject;
import com.TagFu.facebook.model.GraphObjectList;

class GraphObjectPagingLoader<T extends GraphObject> extends Loader<SimpleGraphObjectCursor<T>> {

    private final Class<T> graphObjectClass;
    private boolean skipRoundtripIfCached;
    private Request originalRequest;
    private Request currentRequest;
    private Request nextRequest;
    private OnErrorListener onErrorListener;
    private SimpleGraphObjectCursor<T> cursor;
    private boolean appendResults;
    private boolean loading;

    public GraphObjectPagingLoader(final Context context, final Class<T> graphObjectClass) {

        super(context);

        this.graphObjectClass = graphObjectClass;
    }

    public void clearResults() {

        this.nextRequest = null;
        this.originalRequest = null;
        this.currentRequest = null;

        this.deliverResult(null);
    }

    @Override
    public void deliverResult(final SimpleGraphObjectCursor<T> cursor) {

        final SimpleGraphObjectCursor<T> oldCursor = this.cursor;
        this.cursor = cursor;

        if (this.isStarted()) {
            super.deliverResult(cursor);

            if ((oldCursor != null) && (oldCursor != cursor) && !oldCursor.isClosed()) {
                oldCursor.close();
            }
        }
    }

    public void followNextLink() {

        if (this.nextRequest != null) {
            this.appendResults = true;
            this.currentRequest = this.nextRequest;

            this.currentRequest.setCallback(new Request.Callback() {

                @Override
                public void onCompleted(final Response response) {

                    GraphObjectPagingLoader.this.requestCompleted(response);
                }
            });

            this.loading = true;
            final CacheableRequestBatch batch = this.putRequestIntoBatch(this.currentRequest,
                    this.skipRoundtripIfCached);
            Request.executeBatchAsync(batch);
        }
    }

    public SimpleGraphObjectCursor<T> getCursor() {

        return this.cursor;
    }

    public OnErrorListener getOnErrorListener() {

        return this.onErrorListener;
    }

    public boolean isLoading() {

        return this.loading;
    }

    public void refreshOriginalRequest(final long afterDelay) {

        if (this.originalRequest == null) {
            throw new FacebookException(
                    "refreshOriginalRequest may not be called until after startLoading has been called.");
        }
        this.startLoading(this.originalRequest, false, afterDelay);
    }

    public void setOnErrorListener(final OnErrorListener listener) {

        this.onErrorListener = listener;
    }

    public void startLoading(final Request request, final boolean skipRoundtripIfCached) {

        this.originalRequest = request;
        this.startLoading(request, skipRoundtripIfCached, 0);
    }

    private void addResults(final Response response) {

        final SimpleGraphObjectCursor<T> cursorToModify = ((this.cursor == null) || !this.appendResults) ? new SimpleGraphObjectCursor<T>()
                : new SimpleGraphObjectCursor<T>(this.cursor);

        final PagedResults result = response.getGraphObjectAs(PagedResults.class);
        final boolean fromCache = response.getIsFromCache();

        final GraphObjectList<T> data = result.getData().castToListOf(this.graphObjectClass);
        final boolean haveData = data.size() > 0;

        if (haveData) {
            this.nextRequest = response.getRequestForPagedResults(Response.PagingDirection.NEXT);

            cursorToModify.addGraphObjects(data, fromCache);
            cursorToModify.setMoreObjectsAvailable(true);
        }

        if (!haveData) {
            cursorToModify.setMoreObjectsAvailable(false);
            cursorToModify.setFromCache(fromCache);

            this.nextRequest = null;
        }

        // Once we get any set of results NOT from the cache, stop trying to get any future ones
        // from it.
        if (!fromCache) {
            this.skipRoundtripIfCached = false;
        }

        this.deliverResult(cursorToModify);
    }

    private CacheableRequestBatch putRequestIntoBatch(final Request request, final boolean skipRoundtripIfCached) {

        // We just use the request URL as the cache key.
        final CacheableRequestBatch batch = new CacheableRequestBatch(request);
        // We use the default cache key (request URL).
        batch.setForceRoundTrip(!skipRoundtripIfCached);
        return batch;
    }

    private void startLoading(final Request request, final boolean skipRoundtripIfCached, final long afterDelay) {

        this.skipRoundtripIfCached = skipRoundtripIfCached;
        this.appendResults = false;
        this.nextRequest = null;
        this.currentRequest = request;
        this.currentRequest.setCallback(new Request.Callback() {

            @Override
            public void onCompleted(final Response response) {

                GraphObjectPagingLoader.this.requestCompleted(response);
            }
        });

        // We are considered loading even if we have a delay.
        this.loading = true;

        final RequestBatch batch = this.putRequestIntoBatch(request, skipRoundtripIfCached);
        final Runnable r = new Runnable() {

            @Override
            public void run() {

                Request.executeBatchAsync(batch);
            }
        };
        if (afterDelay == 0) {
            r.run();
        } else {
            final Handler handler = new Handler();
            handler.postDelayed(r, afterDelay);
        }
    }

    @Override
    protected void onStartLoading() {

        super.onStartLoading();

        if (this.cursor != null) {
            this.deliverResult(this.cursor);
        }
    }

    void requestCompleted(final Response response) {

        final Request request = response.getRequest();
        if (request != this.currentRequest) {
            return;
        }

        this.loading = false;
        this.currentRequest = null;

        final FacebookRequestError requestError = response.getError();
        FacebookException exception = (requestError == null) ? null : requestError.getException();
        if ((response.getGraphObject() == null) && (exception == null)) {
            exception = new FacebookException("GraphObjectPagingLoader received neither a result nor an error.");
        }

        if (exception != null) {
            this.nextRequest = null;

            if (this.onErrorListener != null) {
                this.onErrorListener.onError(exception, this);
            }
        } else {
            this.addResults(response);
        }
    }

    public interface OnErrorListener {

        void onError(FacebookException error, GraphObjectPagingLoader<?> loader);
    }

    interface PagedResults extends GraphObject {

        GraphObjectList<GraphObject> getData();
    }
}
