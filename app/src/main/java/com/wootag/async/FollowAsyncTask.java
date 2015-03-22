/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.TagFu.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;

import org.json.JSONException;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.TagFu.Constant;
import com.TagFu.R;
import com.TagFu.dto.ErrorResponse;
import com.TagFu.model.Backend;
import com.TagFu.util.Alerts;
import com.TagFu.util.FollowInterface;

public class FollowAsyncTask extends AsyncTask<Void, Void, Void> {

    private static final String EMPTY = "";

    private static final String UNFOLLOW = "unfollow";

    private static final String FOLLOW = "follow";

    private static final Logger LOG = LoggerManager.getLogger();

    private ProgressDialog progressDialog;
    private final String followerId, userId, type;
    private Object response;
    private final Context context;
    public FollowInterface delegate;

    public FollowAsyncTask(final String followerId, final String userId, final String type, final Context context) {

        this.followerId = followerId;
        this.userId = userId;
        this.type = type;
        this.context = context;
    }

    @Override
    protected Void doInBackground(final Void... params) {

        if (FOLLOW.equalsIgnoreCase(this.type)) {
            try {
                this.response = Backend.follow(this.context, this.userId, this.followerId);
            } catch (final JSONException exception) {
                LOG.e(exception);
            }
        } else if (UNFOLLOW.equalsIgnoreCase(this.type)) {
            try {
                this.response = Backend.unFollow(this.context, this.userId, this.followerId);
            } catch (final JSONException exception) {
                LOG.e(exception);
            }
        } else if (Constant.PRIVATE_GROUP_REQUEST.equalsIgnoreCase(this.type)) {
            try {
                this.response = Backend.privateGroupRequest(this.context, this.userId, this.followerId);
            } catch (final JSONException exception) {
                LOG.e(exception);
            }
        } else if (Constant.UN_PRIVATE.equalsIgnoreCase(this.type)) {
            try {
                this.response = Backend.unPrivateGroup(this.context, this.userId, this.followerId);
            } catch (final JSONException exception) {
                LOG.e(exception);
            }
        } else if (Constant.ADD_PRIVATE_GROUP_REQUEST.equalsIgnoreCase(this.type)) {
            try {
                this.response = Backend.addPrivateGroupRequest(this.context, this.userId, this.followerId);
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

        this.progressDialog = ProgressDialog.show(this.context, EMPTY, EMPTY, true);
        this.progressDialog.setContentView(((LayoutInflater) this.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.progress_bar, null, false));
        this.progressDialog.setCancelable(false);
        this.progressDialog.setCanceledOnTouchOutside(false);
        this.progressDialog.show();
    }
}
