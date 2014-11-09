/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.wootag;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import twitter4j.auth.AccessToken;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wootag.dto.Friend;
import com.wootag.twitter.OAuthRequestTokenTask;
import com.wootag.util.Config;
import com.wootag.util.MainManager;

/**
 * Prepares a OAuthConsumer and OAuthProvider OAuthConsumer is configured with the consumer key & consumer secret.
 * OAuthProvider is configured with the 3 OAuth endpoints. Execute the OAuthRequestTokenTask to retrieve the request,
 * and authorize the request. After the request is authorized, a callback is made here.
 */
public class PrepareRequestTokenActivity extends Activity {

    protected static final Logger LOG = LoggerManager.getLogger();

    public static PrepareRequestTokenActivity prepareRequestTokenActivity;
    private OAuthConsumer consumer;
    private OAuthProvider provider;
    private List<Friend> twitterFriendList;

    @Override
    public void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Config.setTwitterRequestMade(false);
        prepareRequestTokenActivity = this;
        this.getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // try {
        this.consumer = new DefaultOAuthConsumer(Constant.CONSUMER_KEY, Constant.CONSUMER_SECRET);// CommonsHttpOAuthConsumer
        this.provider = new DefaultOAuthProvider(Constant.REQUEST_URL, Constant.ACCESS_URL, Constant.AUTHORIZE_URL);//
        // } catch (final Exception e) {
        // LOG.i(this.TAG, "Error creating consumer / provider" + e.toString());
        // LOG.e(this.TAG, "Error creating consumer / provider", e);
        // }
        LOG.i("Starting task to retrieve request token.");
        // LOG.i(TAG, "Starting task to retrieve request token.");
        new OAuthRequestTokenTask(this, this.consumer, this.provider).execute();
    }

    /**
     * Called when the OAuthRequestTokenTask finishes (user has authorized the request token). The callback URL will be
     * intercepted here.
     */
    @Override
    public void onNewIntent(final Intent intent) {

        super.onNewIntent(intent);
        final Uri uri = intent.getData();
        if ((uri != null) && uri.getScheme().equals(Constant.OAUTH_CALLBACK_SCHEME)) {
            LOG.i("call back received" + uri);
            LOG.i("retriving access token");
            new RetrieveAccessTokenTask(this, this.consumer, this.provider).execute(uri);
            this.finish();
        }
    }

    @Override
    protected void onRestart() {

        super.onRestart();

        final Intent intent = new Intent(Constant.CANCEL_OPERATION);
        this.finish();
        VideoPlayerApp.getAppContext().sendBroadcast(intent);
    }

    @Override
    protected void onResume() {

        super.onResume();
        LOG.i("on resume prepare token.");
    }

    public class RetrieveAccessTokenTask extends AsyncTask<Uri, Void, Void> {

        private static final String PROCESSING = "Processing";
        private static final String EMPTY = "";
        private static final String O_AUTH_ACCESS_TOKEN_RETRIEVAL_ERROR = "OAuth - Access Token Retrieval Error";
        private static final String ERROR = "error";
        private final Context context;
        private final OAuthProvider provider;
        private final OAuthConsumer consumer;
        private String status;
        private ProgressDialog progressDialog;
        private Exception raisedException;

        public RetrieveAccessTokenTask(final Context context, final OAuthConsumer consumer, final OAuthProvider provider) {

            this.context = context;
            this.consumer = consumer;
            this.provider = provider;
        }

        /**
         * Retrieve the oauth_verifier, and store the oauth and oauth_token_secret for future API calls.
         */
        @Override
        protected Void doInBackground(final Uri... params) {

            final Uri uri = params[0];
            final String oauth_verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);

            try {
                this.provider.retrieveAccessToken(this.consumer, oauth_verifier);
                MainManager.getInstance().setTwitterOAuthtoken(this.consumer.getToken());
                MainManager.getInstance().setTwitterSecretKey(this.consumer.getTokenSecret());
                final String token = MainManager.getInstance().getTwitterOAuthtoken();
                final String secret = MainManager.getInstance().getTwitterSecretKey();
                final AccessToken twitterAccessToken = new AccessToken(token, secret);
                this.consumer.setTokenWithSecret(token, secret);
                LOG.i("twitter access token " + token);
                this.status = "success";
                LOG.i("list received");

            } catch (final OAuthCommunicationException exception) {
                LOG.e(exception);
                this.status = ERROR;
                this.raisedException = exception;
                LOG.e(O_AUTH_ACCESS_TOKEN_RETRIEVAL_ERROR, exception);
            } catch (final OAuthMessageSignerException exception) {
                LOG.e(exception);
                this.status = ERROR;
                this.raisedException = exception;
                LOG.e(O_AUTH_ACCESS_TOKEN_RETRIEVAL_ERROR, exception);
            } catch (final OAuthNotAuthorizedException exception) {
                LOG.e(exception);
                this.status = ERROR;
                this.raisedException = exception;
                LOG.e(O_AUTH_ACCESS_TOKEN_RETRIEVAL_ERROR, exception);
            } catch (final OAuthExpectationFailedException exception) {
                LOG.e(exception);
                this.status = ERROR;
                this.raisedException = exception;
                LOG.e(O_AUTH_ACCESS_TOKEN_RETRIEVAL_ERROR, exception);
            }

            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {

            super.onPostExecute(result);
            this.progressDialog.dismiss();
            LOG.i("on post excecute prepare request token");
            if ((this.status != null) && this.status.equalsIgnoreCase(ERROR)) {
                final Intent intent = new Intent(Constant.TWITTER_EXCEPTION);
                PrepareRequestTokenActivity.this.finish();
                VideoPlayerApp.getAppContext().sendBroadcast(intent);
            } else {
                MainManager.getInstance().setTwitterAuthorization(1);
                final Intent intent = new Intent(Constant.TWITTER_FRIEND_LIST);
                PrepareRequestTokenActivity.this.finish();
                // VideoPlayerApp.getAppContext().sendBroadcast(intent);
                this.context.sendBroadcast(intent);
            }

        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            this.progressDialog = ProgressDialog.show(this.context, EMPTY, EMPTY, true);
            final View view = ((LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.progress_bar, null, false);
            final TextView progressText = (TextView) view.findViewById(R.id.progressText);
            progressText.setText(PROCESSING);
            this.progressDialog.setContentView(view);
            this.progressDialog.setCancelable(false);
            this.progressDialog.show();
        }

    }

}
