/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.TagFu.async;

import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.TagFu.connectivity.FacebookHttpConnectionManager;
import com.TagFu.dto.FacebookUser;
import com.TagFu.dto.Friend;
import com.TagFu.dto.Playback;
import com.TagFu.util.AsyncResponse;
import com.TagFu.util.Config;

public class FacebookShareAsync extends AsyncTask<Void, Void, Void> {

    private static final Logger LOG = LoggerManager.getLogger();

    private volatile boolean running = true;
    private static final int ERROR = 0;
    private static final int SUCCESS = 2;
    private static int status = -1;
    private Exception raisedException;
    private ProgressDialog progressDialog;
    private final FacebookHttpConnectionManager con;
    private List<Friend> fbfriendList;
    public AsyncResponse delegate;
    private final String fbId;
    private FacebookUser user;

    public FacebookShareAsync(final Context context, final Playback video, final String id) {

        this.fbId = id;
        this.con = new FacebookHttpConnectionManager(context);
    }

    @Override
    protected Void doInBackground(final Void... params) {

        while (this.running) {
            this.con.shareLink(this.fbId, null, Config.getFacebookAccessToken());
            status = SUCCESS;
            this.running = false;
        }
        return null;

    }

    @Override
    protected void onPostExecute(final Void result) {

        if (this.progressDialog != null) {
            this.progressDialog.dismiss();
        }

    }

}
