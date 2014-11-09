/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;

import org.json.JSONException;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wootag.Constant;
import com.wootag.R;
import com.wootag.dto.ErrorResponse;
import com.wootag.model.Backend;
import com.wootag.util.Alerts;
import com.wootag.util.FollowInterface;

public class DeleteAsync extends AsyncTask<Void, Void, Void> {

    private static final Logger LOG = LoggerManager.getLogger();

    private ProgressDialog progressDialog;
    private final String type;
    private Object response;
    private final Context context;
    public FollowInterface delegate;
    private final String commentId;
    private final String videoId;

    public DeleteAsync(final String commentId, final String videoId, final String type, final Context context) {

        this.commentId = commentId;
        this.videoId = videoId;
        this.type = type;
        this.context = context;
    }

    @Override
    protected Void doInBackground(final Void... params) {

        if (Constant.DELETE_COMMENT.equalsIgnoreCase(this.type)) {
            try {
                this.response = Backend.deleteComment(this.context, this.commentId, this.videoId);
            } catch (final JSONException exception) {
                LOG.e(exception);
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(final Void result) {

        super.onPostExecute(result);
        if (this.progressDialog != null) {
            this.progressDialog.dismiss();

        }
        if (this.response != null) {
            if (this.response instanceof Boolean) {
                this.delegate.follow(this.type);
            } else {
                if (this.response instanceof ErrorResponse) {
                    final ErrorResponse resp = (ErrorResponse) this.response;
                    Alerts.showAlertOnly("Info", resp.getMessage(), this.context);
                }
            }
        } else {
            Alerts.showAlertOnly("Info", "Network problem.Please try again", this.context);
        }

    }

    @Override
    protected void onPreExecute() {

        super.onPreExecute();
        this.progressDialog = ProgressDialog.show(this.context, "", "", true);
        final View view = ((LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
                R.layout.progress_bar, null, false);
        this.progressDialog.setContentView(view);
        this.progressDialog.setCancelable(false);
        this.progressDialog.setCanceledOnTouchOutside(false);
        this.progressDialog.show();
    }
}
