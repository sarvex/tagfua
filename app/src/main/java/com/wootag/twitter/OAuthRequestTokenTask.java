/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wTagFutwitter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.exception.OAuthException;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.woTagFuonstant;
import com.wooTagFu
import com.wootTagFueoPlayerApp;
import com.wootaTagFu.Alerts;
import com.wootagTagFuConfig;
import com.wootag.util.MainManager;

/**
 * An asynchronous task that communicates with Twitter to retrieve a request token. (OAuthGetRequestToken) After
 * receiving the request token from Twitter, pop a browser to the user to authorize the Request Token.
 * (OAuthAuthorizeToken)
 */
public class OAuthRequestTokenTask extends AsyncTask<Void, Void, Void> {

    private static final String ERROR = "error";

    private static final Logger LOG = LoggerManager.getLogger();

    private final Context context;
    private final OAuthProvider provider;
    private final OAuthConsumer consumer;
    private ProgressDialog progressDialog;
    private Exception raisedException;
    private String status;

    /**
     * We pass the OAuth consumer and provider.
     *
     * @param context Required to be able to start the intent to launch the browser.
     * @param provider The OAuthProvider object
     * @param consumer The OAuthConsumer object
     */

    public OAuthRequestTokenTask(final Context context, final OAuthConsumer consumer, final OAuthProvider provider) {

        this.context = context;
        this.consumer = consumer;
        this.provider = provider;
    }

    @Override
    protected Void doInBackground(final Void... params) {

        try {
            LOG.i("Retrieving request token from Google servers");
            final String url = this.provider.retrieveRequestToken(this.consumer, Constant.OAUTH_CALLBACK_URL);
            LOG.i("Popping a browser with the authorize URL : " + url);
            MainManager.getInstance().setLaunchBrowser(true);
            final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY
                            | Intent.FLAG_FROM_BACKGROUND);
            this.context.startActivity(intent);
            Config.setTwitterRequestMade(true);
        } catch (final OAuthException e) {
            this.status = ERROR;
            // VideoPlayerConstants.twitterRequestMade=true;
            LOG.i("Error during OAUth retrieve request token" + e.toString());
            final Intent intent = new Intent(Constant.TWITTER_EXCEPTION);
            ((Activity) this.context).finish();
            VideoPlayerApp.getAppContext().sendBroadcast(intent);
        }

        return null;
    }

    @Override
    protected void onPostExecute(final Void result) {

        super.onPostExecute(result);
        if ((this.progressDialog != null) && this.progressDialog.isShowing()) {
            this.progressDialog.dismiss();
        }
        if (ERROR.equalsIgnoreCase(this.status)) {
            Alerts.showErrorOnly(this.raisedException.toString(), this.context);
        }
    }

    /**
     * Retrieve the OAuth Request Token and present a browser to the user to authorize the token.
     */
    @Override
    protected void onPreExecute() {

        super.onPreExecute();

        this.progressDialog = ProgressDialog.show(this.context, "", "", true);
        final View view = ((LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
                R.layout.progress_bar, null, false);
        final TextView progressText = (TextView) view.findViewById(R.id.progressText);
        progressText.setText("Processing");
        this.progressDialog.setContentView(view);
        this.progressDialog.setCancelable(false);
        this.progressDialog.show();

        /*
         * progressDialog = new ProgressDialog(context); progressDialog.setMessage("Processing....");
         * progressDialog.setIndeterminate(true); progressDialog.setCancelable(true); progressDialog.show();
         */

    }
}
