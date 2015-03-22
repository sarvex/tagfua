/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.TagFu.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.TagFu.Constant;
import com.TagFu.model.Backend;

public class RefreshAsyncTask extends AsyncTask<Void, Void, Void> {

    private static final Logger LOG = LoggerManager.getLogger();

    private ProgressDialog progressDialog;
    private final String request;
    private final Context context;
    private final String type;

    public RefreshAsyncTask(final Context context, final String type, final String request) {

        this.request = request;
        this.context = context;
        this.type = type;
    }

    @Override
    protected Void doInBackground(final Void... params) {

        if (Constant.PUBLIC_FEED.equalsIgnoreCase(this.type)) {
            Backend.refreshFeeds(this.context, this.request, this.type);
        } else if (Constant.MY_PAGE_CACHE.equalsIgnoreCase(this.type)) {
            Backend.refreshFeeds(this.context, this.request, this.type);
        } else if (Constant.NOTIFICATION_CACHE.equalsIgnoreCase(this.type)) {
            Backend.refreshFeeds(this.context, this.request, this.type);
        }
        return null;
    }

}
