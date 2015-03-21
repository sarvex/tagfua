/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wTagFuasync;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.woTagFu;
import com.wooTagFuo.ErrorResponse;
import com.wootTagFu.MyPageDto;
import com.wootaTagFul.Backend;
import com.wootagTagFuAlerts;
import com.wootag.TagFuoreVideos;
import com.wootag.util.Util;

public class MoreVideosAsync extends AsyncTask<Void, Void, Void> {

    private static final String EMPTY = "";

    private static final String _100 = "100";

    private static final Logger LOG = LoggerManager.getLogger();

    public MoreVideos delegate;
    private final Context context;
    private final TextView errorMessageTextView;
    private final boolean progressVisible;
    private final boolean search;
    private ProgressDialog progressDialog;
    private final String request;
    private Object returnObject;
    private volatile boolean running = true;

    public MoreVideosAsync(final Context context, final String request, final boolean search,
            final boolean progressVisible, final TextView errorMsgView) {

        this.request = request;
        this.context = context;
        this.search = search;
        this.progressVisible = progressVisible;
        this.errorMessageTextView = errorMsgView;

    }

    @Override
    protected Void doInBackground(final Void... params) {

        while (this.running) {
            if (this.search) {
                try {
                    this.returnObject = Backend.myPageSearch(this.context, this.request);
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }
            } else {
                try {
                    this.returnObject = Backend.getMoreVideos(this.context, this.request);
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }
            }
            this.running = false;
        }
        return null;
    }

    @Override
    protected void onPostExecute(final Void result) {

        super.onPostExecute(result);
        if (this.progressVisible) {
            this.progressDialog.dismiss();
        }
        if (this.errorMessageTextView != null) {
            this.errorMessageTextView.setVisibility(View.GONE);
            this.errorMessageTextView.setText(R.string.no_search_text);
        }
        if (this.returnObject != null) {
            if (this.returnObject instanceof ErrorResponse) {
                // ErrorResponse res = (ErrorResponse) returnObject;
                // Alerts.ShowAlertOnly("Info", res.getMessage(),context);

                final ErrorResponse res = (ErrorResponse) this.returnObject;
                if (Util.isConnected(this.context)) {
                    if (this.errorMessageTextView != null) {
                        this.errorMessageTextView.setVisibility(View.GONE);
                        this.errorMessageTextView.setText(R.string.no_search_text);
                    }
                    Alerts.showInfoOnly(res.getMessage(), this.context);
                } else {
                    if ((this.errorMessageTextView != null) && _100.equalsIgnoreCase(res.getErrorCode())) {
                        this.errorMessageTextView.setText(R.string.no_connectivity_text);
                        this.errorMessageTextView.setVisibility(View.VISIBLE);
                    } else {
                        Alerts.showInfoOnly(res.getMessage(), this.context);
                    }
                }

                this.delegate.videoList(null);
            } else if (this.returnObject instanceof List<?>) {
                final List<MyPageDto> currentList = (ArrayList<MyPageDto>) this.returnObject;
                if ((currentList != null) && (currentList.size() > 0)) {
                    this.delegate.videoList(currentList);
                } else {
                    this.delegate.videoList(null);
                }

            } else {
                Alerts.showInfoOnly("No response from server", this.context);
                this.delegate.videoList(null);
            }
        } else {
            Alerts.showInfoOnly("No response from server", this.context);
            this.delegate.videoList(null);
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
