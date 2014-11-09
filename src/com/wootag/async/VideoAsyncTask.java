/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;

import org.json.JSONException;
import org.json.JSONObject;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wootag.R;
import com.wootag.dto.ErrorResponse;
import com.wootag.model.Backend;
import com.wootag.util.Alerts;
import com.wootag.util.VideoActionInterface;

public class VideoAsyncTask extends AsyncTask<Void, Void, Void> {

    private static final String EMPTY = "";

    private static final String FAILED = "Failed";

    private static final String DISLIKE = "dislike";

    private static final String REPORT = "report";

    private static final String DELETE = "delete";

    private static final String LIKE = "like";

    private static final Logger LOG = LoggerManager.getLogger();

    public VideoActionInterface delegate;
    private final Context context;
    private final boolean progressVisible;
    private ProgressDialog progressDialog;
    private final JSONObject request;
    private final String requestFor;
    // TextView view;
    private Object response;

    public VideoAsyncTask(final Context context, final String req, final JSONObject request,
            final boolean progressVisible) {

        this.context = context;
        this.requestFor = req;
        this.request = request;
        this.progressVisible = progressVisible;
        // this.view=textView;

    }

    @Override
    protected Void doInBackground(final Void... params) {

        if (LIKE.equalsIgnoreCase(this.requestFor)) {
            try {
                this.response = Backend.videoLike(this.context, this.request);
            } catch (final JSONException exception) {
                LOG.e(exception);
            }
        } else if (DELETE.equalsIgnoreCase(this.requestFor)) {
            try {
                this.response = Backend.videoDelete(this.context, this.request);
            } catch (final JSONException exception) {
                LOG.e(exception);
            }
        } else if (REPORT.equalsIgnoreCase(this.requestFor)) {
            try {
                this.response = Backend.reportVideo(this.context, this.request.toString());
            } catch (final JSONException exception) {
                LOG.e(exception);
            }
        } else if (DISLIKE.equalsIgnoreCase(this.requestFor)) {
            try {
                this.response = Backend.videoDislike(this.context, this.request);
            } catch (final JSONException exception) {
                LOG.e(exception);
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(final Void result) {

        super.onPostExecute(result);
        if ((this.progressDialog != null) && this.progressVisible) {
            this.progressDialog.dismiss();
        }
        if (LIKE.equalsIgnoreCase(this.requestFor)) {
            if (this.response instanceof Boolean) {
                final boolean status = (Boolean) this.response;
                if (status) {
                    this.delegate.processDone(status, LIKE);
                } else {
                    Alerts.showInfoOnly(FAILED, this.context);
                }
            } else {
                if (this.response instanceof ErrorResponse) {
                    final ErrorResponse res = (ErrorResponse) this.response;
                    Alerts.showInfoOnly(res.getMessage(), this.context);
                }
            }

        } else if (DISLIKE.equalsIgnoreCase(this.requestFor)) {
            if (this.response instanceof Boolean) {
                final boolean status = (Boolean) this.response;
                if (status) {
                    this.delegate.processDone(status, DISLIKE);
                } else {
                    Alerts.showInfoOnly(FAILED, this.context);
                }
            } else {
                if (this.response instanceof ErrorResponse) {
                    final ErrorResponse res = (ErrorResponse) this.response;
                    Alerts.showInfoOnly(res.getMessage(), this.context);
                }
            }
        } else if (DELETE.equalsIgnoreCase(this.requestFor)) {
            if (this.response instanceof Boolean) {
                final boolean status = (Boolean) this.response;
                if (status) {
                    // Alerts.ShowAlertOnly("Info", "Deleted successfully", context);
                    this.delegate.processDone(status, DELETE);
                } else {
                    Alerts.showInfoOnly(FAILED, this.context);
                }
            } else {
                if (this.response instanceof ErrorResponse) {
                    final ErrorResponse res = (ErrorResponse) this.response;
                    Alerts.showInfoOnly(res.getMessage(), this.context);
                }
            }

        } else if (REPORT.equalsIgnoreCase(this.requestFor)) {
            if (this.response instanceof Boolean) {
                final boolean status = (Boolean) this.response;
                this.delegate.processDone(status, REPORT);

            } else {
                if (this.response instanceof ErrorResponse) {
                    final ErrorResponse res = (ErrorResponse) this.response;
                    Alerts.showInfoOnly(res.getMessage(), this.context);
                }
            }

        }
    }

    @Override
    protected void onPreExecute() {

        super.onPreExecute();
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
