/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.TagFu.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;

import twitter4j.DirectMessage;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.TagFu.Constant;
import com.TagFu.R;
import com.TagFu.dto.FacebookUser;
import com.TagFu.util.Alerts;
import com.TagFu.util.AsyncResponse;
import com.TagFu.util.MainManager;

public class TwitterFriendInfoAsync extends AsyncTask<Void, Void, Void> {

    private static final String EMPTY = "";

    private static final String SENT_MESSAGE_SUCCESSFULLY = "Sent Message successfully";

    private static final String SENT_FRIEND_REQUEST = "Sent Friend Request";

    private static final Logger LOG = LoggerManager.getLogger();

    private static final int ERROR = 0;
    private static final int CANCELLED = 1;
    private static final int SUCCESS = 2;

    private AsyncResponse delegate;
    private Exception raisedException;
    private FacebookUser userInfo;
    private ProgressDialog progressDialog;
    private final Context context;
    private final String reqFor;
    private final String twId;
    private static int status = -1;
    private volatile boolean running = true;

    public TwitterFriendInfoAsync(final Context context, final String twId, final String requestfor, final String inf) {

        this.context = context;
        this.twId = twId;
        this.reqFor = requestfor;
    }

    @Override
    protected Void doInBackground(final Void... params) {

        while (this.running) {
            final String token = MainManager.getInstance().getTwitterOAuthtoken();
            final String secret = MainManager.getInstance().getTwitterSecretKey();
            final ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(Constant.CONSUMER_KEY);
            builder.setOAuthConsumerSecret(Constant.CONSUMER_SECRET);
            builder.setOAuthAccessToken(token);
            builder.setOAuthAccessTokenSecret(secret);
            builder.setJSONStoreEnabled(true);
            builder.setIncludeEntitiesEnabled(true);
            builder.setIncludeMyRetweetEnabled(true);
            builder.setIncludeRTsEnabled(true);

            final AccessToken aToken = new AccessToken(token, secret);
            final Twitter twitter = new TwitterFactory(builder.build()).getInstance(aToken);

            if (Constant.TWITTER_FRIEND_INFO.equalsIgnoreCase(this.reqFor)) {
                this.userInfo = new FacebookUser();

                try {
                    final User user = twitter.showUser(this.twId);
                    this.userInfo.setCurrentPlace(user.getLocation());
                    this.userInfo.setProfilePick(user.getProfileImageURL());
                    this.userInfo.setUserName(user.getName());
                    this.userInfo.setScreenName(user.getScreenName());
                } catch (final TwitterException exception) {
                    LOG.e(exception);
                    status = ERROR;
                    this.raisedException = exception;
                    this.running = false;
                }

            } else if (Constant.TWITTER_FOLLOW.equalsIgnoreCase(this.reqFor)) {
                try {
                    twitter.createFriendship(this.twId);
                } catch (final TwitterException exception) {
                    status = ERROR;
                    this.raisedException = exception;
                    this.running = false;
                    LOG.e(exception);
                }
            } else if (Constant.TWITTER_DIRECT_MESSAGE.equalsIgnoreCase(this.reqFor)) {
                try {
                    final DirectMessage message = twitter.sendDirectMessage(this.twId, "test purpose");
                } catch (final TwitterException exception) {
                    status = ERROR;
                    this.raisedException = exception;
                    this.running = false;
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

        super.onPostExecute(result);
        if (this.progressDialog != null) {
            this.progressDialog.dismiss();
        }
        if (status == SUCCESS) {
            if (Constant.TWITTER_FRIEND_INFO.equalsIgnoreCase(this.reqFor)) {
                this.delegate.friendInfoProcessFinish(this.userInfo, false, Constant.TWITTER);
            } else if (Constant.TWITTER_FOLLOW.equalsIgnoreCase(this.reqFor)) {
                Alerts.showInfoOnly(SENT_FRIEND_REQUEST, this.context);
            } else if (Constant.TWITTER_DIRECT_MESSAGE.equalsIgnoreCase(this.reqFor)) {
                Alerts.showInfoOnly(SENT_MESSAGE_SUCCESSFULLY, this.context);
            }
        } else if (status == ERROR) {
            Alerts.showErrorOnly(this.raisedException.toString(), this.context);
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
