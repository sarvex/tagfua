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

import org.json.JSONException;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.woTagFu;
import com.wooTagFuo.ErrorResponse;
import com.wootTagFu.Liked;
import com.wootaTagFul.Backend;
import com.wootagTagFuAlerts;
import com.wootag.util.MoreVideos;

public class LikedAsync extends AsyncTask<Void, Void, Void> {

    private static final String EMPTY = "";

    private static final Logger LOG = LoggerManager.getLogger();

    private ProgressDialog progressDialog;
    private String request;
    private final Context context;
    private Object returnObject;
    public MoreVideos delegate;
    private boolean serverRequest;
    private final int pageNo;
    private final String videoId;
    private final boolean progressVisible;

    public LikedAsync(final Context context, final int pageNo, final String videoId, final boolean progressVisible) {

        this.context = context;
        this.videoId = videoId;
        this.pageNo = pageNo;
        this.progressVisible = progressVisible;
    }

    @Override
    protected Void doInBackground(final Void... params) {

        try {
            this.returnObject = Backend.getLovedPeopleList(this.context, this.videoId, this.pageNo);
        } catch (final JSONException exception) {
            LOG.e(exception);
        }
        return null;
    }

    @Override
    protected void onPostExecute(final Void result) {

        super.onPostExecute(result);
        if (this.progressVisible && (this.progressDialog != null)) {
            this.progressDialog.dismiss();
        }
        if (this.returnObject != null) {
            if (this.returnObject instanceof ErrorResponse) {
                final ErrorResponse res = (ErrorResponse) this.returnObject;
                Alerts.showAlertOnly("Info", res.getMessage(), this.context);
            } else if (this.returnObject instanceof List<?>) {
                final List<Liked> currentList = (ArrayList<Liked>) this.returnObject;
                this.delegate.likedList(currentList);
            } else {
                Alerts.showAlertOnly("Info", "No response from server", this.context);
            }
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
