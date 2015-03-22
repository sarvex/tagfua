/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.TagFu.async;

import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;

import org.json.JSONException;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.TagFu.Constant;
import com.TagFu.R;
import com.TagFu.VideoPlayerApp;
import com.TagFu.connectivity.FacebookHttpConnectionManager;
import com.TagFu.dto.FacebookUser;
import com.TagFu.dto.Friend;
import com.TagFu.util.Alerts;
import com.TagFu.util.AsyncResponse;

public class FacebookFriendsAsync extends AsyncTask<Void, Void, Void> {

    private static final String EMPTY = "";

    private static final Logger LOG = LoggerManager.getLogger();

    private final Context context;
    private volatile boolean running = true;
    private static final int ERROR = 0;
    private static final int CANCELLED = 1;
    private static final int SUCCESS = 2;
    private static final int FAILED = 3;
    private static int status = -1;
    private Exception raisedException;
    private ProgressDialog progressDialog;
    private final FacebookHttpConnectionManager con;
    private List<Friend> fbfriendList;
    public AsyncResponse delegate;
    private final String fbId;
    private final String reqFor;
    private FacebookUser user;

    public FacebookFriendsAsync(final Context context, final String requestFor, final String id)// ,ListView list
    {

        this.context = context;
        this.reqFor = requestFor;
        this.fbId = id;
        this.con = new FacebookHttpConnectionManager(context);
    }

    private boolean isFriendToUser(final String fbid) {

        boolean friend = false;
        for (int i = 0; i < this.fbfriendList.size(); i++) {
            final Friend frnd = this.fbfriendList.get(i);
            if (fbid.equalsIgnoreCase(frnd.getFriendId())) {
                friend = true;
                break;
            }
        }
        return friend;
    }

    @Override
    protected Void doInBackground(final Void... params) {

        while (this.running) {
            if (Constant.FRIEND_LIST.equalsIgnoreCase(this.reqFor)) {
                try {
                    this.fbfriendList = this.con.getFacebookFriendsList();
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }
            } else if (Constant.FRIEND_INFO.equalsIgnoreCase(this.reqFor)) {
                try {
                    this.user = this.con.getFacebookFriendInfo(this.fbId);
                } catch (final JSONException exception1) {
                    LOG.e(exception1);
                }
                if ((VideoPlayerApp.getInstance().getFbFriendsList() != null)
                        && !VideoPlayerApp.getInstance().getFbFriendsList().isEmpty()) {
                    this.fbfriendList = VideoPlayerApp.getInstance().getFbFriendsList();
                } else {
                    try {
                        this.fbfriendList = this.con.getFacebookFriendsList();
                    } catch (final JSONException exception) {
                        LOG.e(exception);
                    }
                }
            } else if (Constant.FEED.equalsIgnoreCase(this.reqFor)) {
                try {
                    this.user = this.con.getFacebookFeed();
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }
            }
            status = SUCCESS;
            this.running = false;
        }
        return null;

    }

    @Override
    protected void onCancelled() {

        if (this.progressDialog != null) {
            this.progressDialog.dismiss();
        }
        status = CANCELLED;
        this.running = false;
    }

    @Override
    protected void onPostExecute(final Void result) {

        if (this.progressDialog != null) {
            this.progressDialog.dismiss();
        }
        if (status == SUCCESS) {
            if (Constant.FRIEND_LIST.equalsIgnoreCase(this.reqFor)) {
                this.delegate.processFinish(this.fbfriendList, Constant.FACEBOOK);
            } else if (Constant.FRIEND_INFO.equalsIgnoreCase(this.reqFor)) {
                this.delegate.friendInfoProcessFinish(this.user, this.isFriendToUser(this.fbId), Constant.FACEBOOK);
            } else if (Constant.FEED.equalsIgnoreCase(this.reqFor)) {
                this.delegate.friendInfoProcessFinish(this.user, false, Constant.FACEBOOK);
            }
        } else {
            Alerts.showInfoOnly("Failed", this.context);
        }

    }

    @Override
    protected void onPreExecute() {

        super.onPreExecute();

        this.progressDialog = ProgressDialog.show(this.context, EMPTY, EMPTY, true);
        final View view = ((LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
                R.layout.progress_bar, null, false);
        this.progressDialog.setContentView(view);
        this.progressDialog.setCancelable(false);
        this.progressDialog.setCanceledOnTouchOutside(false);
        this.progressDialog.show();

    }
}
