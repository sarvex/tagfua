/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wTagFutwitter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wootag.Constant;

public class TwitterApp extends Activity {

    private static final Logger LOG = LoggerManager.getLogger();

    private static RequestToken requestToken;
    private static SharedPreferences sharedPreferences;
    private static Twitter twitter;

    /**
     * Remove Token, Secret from preferences
     */
    private static void disconnectTwitter() {

        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(Constant.OAUTH_TOKEN);
        editor.remove(Constant.PREF_KEY_SECRET);

        editor.commit();
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        sharedPreferences = this.getSharedPreferences(Constant.PREFERENCE_NAME, MODE_PRIVATE);

        /**
         * Handle OAuth Callback
         */
        final Uri uri = this.getIntent().getData();
        if ((uri != null) && uri.toString().startsWith(Constant.CALLBACK_URL)) {
            final String verifier = uri.getQueryParameter(Constant.OAUTH_VERIFIER);
            AccessToken accessToken;
            try {
                accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
                final Editor editor = sharedPreferences.edit();
                editor.putString(Constant.OAUTH_TOKEN, accessToken.getToken());
                editor.putString(Constant.PREF_KEY_SECRET, accessToken.getTokenSecret());
                editor.commit();
            } catch (final TwitterException exception) {
                LOG.e(exception);
            }
        }

    }

    private void askOAuth() {

        final ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setOAuthConsumerKey(Constant.CONSUMER_KEY);
        configurationBuilder.setOAuthConsumerSecret(Constant.CONSUMER_SECRET);
        final Configuration configuration = configurationBuilder.build();
        twitter = new TwitterFactory(configuration).getInstance();

        try {
            requestToken = twitter.getOAuthRequestToken(Constant.CALLBACK_URL);
            Toast.makeText(this, "Please authorize this app!", Toast.LENGTH_LONG).show();
            this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.getAuthenticationURL())));
        } catch (final TwitterException e) {
            LOG.e(e);
        }
    }

    /**
     * check if the account is authorized
     *
     * @return
     */
    private boolean isConnected() {

        return sharedPreferences.getString(Constant.OAUTH_TOKEN, null) != null;
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (this.isConnected()) {
            final String oauthAccessToken = sharedPreferences.getString(Constant.OAUTH_TOKEN, "");
            final String oAuthAccessTokenSecret = sharedPreferences.getString(Constant.PREF_KEY_SECRET, "");

            final ConfigurationBuilder confbuilder = new ConfigurationBuilder();
            final Configuration conf = confbuilder.setOAuthConsumerKey(Constant.CONSUMER_KEY)
                    .setOAuthConsumerSecret(Constant.CONSUMER_SECRET).setOAuthAccessToken(oauthAccessToken)
                    .setOAuthAccessTokenSecret(oAuthAccessTokenSecret).build();

        } else {
            this.askOAuth();
        }
    }

}
