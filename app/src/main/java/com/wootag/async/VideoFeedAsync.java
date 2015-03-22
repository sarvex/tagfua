/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.TagFu.async;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.TagFu.R;
import com.TagFu.connectivity.Parser;
import com.TagFu.dto.ErrorResponse;
import com.TagFu.dto.MyPageDto;
import com.TagFu.model.Backend;
import com.TagFu.util.Alerts;
import com.TagFu.util.MoreVideos;
import com.TagFu.util.Util;

public class VideoFeedAsync extends AsyncTask<Void, Void, Void> {

    private static final String COM_AYANSYS_SAMPLEVIDEOPLAYER_TEMP_FEED_JSON = "/com/ayansys/samplevideoplayer/temp/feed.json";

    private static final String EMPTY = "";

    private static final String PROBLEM_WITH_SERVER = "Problem with Server";

    private static final String FEEDNOTIFICATIONVISITED = "feednotificationvisited";

    private static final String PRIVATEFEED = "privatefeed";

    private static final Logger LOG = LoggerManager.getLogger();

    private ProgressDialog progressDialog;
    private final String request;
    private final Context context;
    private Object returnObject;
    public MoreVideos delegate;
    private static final boolean serverRequest = true;
    private final boolean search;
    private final boolean progressVisible;
    private final String type;
    private final boolean firstTime;
    private final boolean pullToRefresh;
    private final TextView errorView;

    public VideoFeedAsync(final Context context, final String type, final String request, final boolean search,
            final boolean progressVisible, final boolean firstTime, final boolean pullToRefresh,
            final TextView errorView) {

        this.request = request;
        this.context = context;
        this.search = search;
        this.firstTime = firstTime;
        this.pullToRefresh = pullToRefresh;
        this.type = type;
        this.progressVisible = progressVisible;
        this.errorView = errorView;
    }

    @Override
    protected Void doInBackground(final Void... params) {

        if (VideoFeedAsync.serverRequest) {
            if (this.search) {
                if (PRIVATEFEED.equalsIgnoreCase(this.type)) {
                    try {
                        this.returnObject = Backend.privateVideoFeedSearch(this.context, this.request);
                    } catch (final JSONException exception) {
                        LOG.e(exception);
                    }
                } else {
                    try {
                        this.returnObject = Backend.videoFeedSearch(this.context, this.request);
                    } catch (final JSONException exception) {
                        LOG.e(exception);
                    }
                }
            } else {
                if (PRIVATEFEED.equalsIgnoreCase(this.type)) {
                    try {
                        this.returnObject = Backend.privateVideoFeed(this.context, this.request, this.firstTime,
                                this.pullToRefresh);
                    } catch (final JSONException exception) {
                        LOG.e(exception);
                    }
                } else {
                    try {
                        this.returnObject = Backend.videoFeed(this.context, this.request, this.firstTime,
                                this.pullToRefresh);
                    } catch (final JSONException exception) {
                        LOG.e(exception);
                    }
                }
            }
        } else {
            String response;
            response = Util.jsontoString(COM_AYANSYS_SAMPLEVIDEOPLAYER_TEMP_FEED_JSON);
            if (response != null) {
                try {
                    this.returnObject = Parser.parseFeed(response);
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(final Void result) {

        super.onPostExecute(result);
        if (this.progressVisible) {
            this.progressDialog.dismiss();
        }
        if (this.returnObject != null) {
            if (this.returnObject instanceof ErrorResponse) {
                final ErrorResponse res = (ErrorResponse) this.returnObject;
                this.errorView.setVisibility(View.GONE);
                this.errorView.setText(R.string.no_search_text);
                Alerts.showInfoOnly(res.getMessage(), this.context);
                this.delegate.videoList(null, this.type, false);
            } else if (this.returnObject instanceof ArrayList) {
                this.errorView.setVisibility(View.GONE);
                this.errorView.setText(R.string.no_search_text);
                final List<MyPageDto> currentList = (ArrayList<MyPageDto>) this.returnObject;
                if ((currentList != null) && (currentList.size() > 0)) {
                    this.delegate.videoList(currentList, this.type, true);
                } else {
                    this.delegate.videoList(null, this.type, true);
                }
                final Intent intent = new Intent(FEEDNOTIFICATIONVISITED);
                this.context.sendBroadcast(intent);
            } else {
                Toast.makeText(this.context, PROBLEM_WITH_SERVER, Toast.LENGTH_LONG).show();
                this.delegate.videoList(null, this.type, true);
            }
        } else {
            this.delegate.videoList(null, this.type, false);
        }
    }

    @Override
    protected void onPreExecute() {

        if (this.progressVisible) {

            this.progressDialog = ProgressDialog.show(this.context, EMPTY, EMPTY, true);
            this.progressDialog.setContentView(((LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.progress_bar, null, false));
            this.progressDialog.setCancelable(false);
            this.progressDialog.setCanceledOnTouchOutside(false);
            this.progressDialog.show();
        }
    }
}
