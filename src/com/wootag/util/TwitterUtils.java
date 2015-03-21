/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wTagFuutil;

import android.content.Context;
import android.content.Intent;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.woTagFuonstant;
import com.wooTagFuepareRequestTokenActivity;
import com.wootag.twitter.TwitterAsync;

public final class TwitterUtils {

    protected static final Logger LOG = LoggerManager.getLogger();

    static boolean returnStatus;

    private TwitterUtils() {

    }

    public static void getTwitterFriendList(final Context context) {

        final TwitterAsync asyncTask = new TwitterAsync("", context, "", "", "", null, "");
        asyncTask.execute();
    }

    public static boolean isAuthenticated(final Context context) {

        final String token = MainManager.getInstance().getTwitterOAuthtoken();
        final String secret = MainManager.getInstance().getTwitterSecretKey();
        if ((token != null) && (secret != null)) {
            if (MainManager.getInstance().getTwitterAuthorization() == 0) {
                final AccessToken a = new AccessToken(token, secret);
                final Twitter twitter = new TwitterFactory().getInstance();
                twitter.setOAuthConsumer(Constant.CONSUMER_KEY, Constant.CONSUMER_SECRET);
                twitter.setOAuthAccessToken(a);

                final Thread t = new Thread(new Runnable() {

                    @Override
                    public void run() {

                        try {
                            twitter.getAccountSettings();
                        } catch (final TwitterException exception) {
                            LOG.e(exception);
                        }
                        returnStatus = true;
                    }

                });
                t.start();
                return returnStatus;
            }
            return true;
        }
        return false;
    }

    public static void navigateToPrepare(final Context context) {

        final Intent i = new Intent(context, PrepareRequestTokenActivity.class);
        i.putExtra("tweet_msg", "");
        context.startActivity(i);
    }

    public static void removeCredentials(final Context context) {

        final String token = MainManager.getInstance().getTwitterOAuthtoken();
        final String secret = MainManager.getInstance().getTwitterSecretKey();
        Twitter twitter = null;
        if ((token != null) && (secret != null)) {
            final AccessToken a = new AccessToken(token, secret);
            twitter = new TwitterFactory().getInstance();
            twitter.setOAuthConsumer(Constant.CONSUMER_KEY, Constant.CONSUMER_SECRET);
            twitter.setOAuthAccessToken(a);
        }

        twitter.setOAuthAccessToken(null);
        twitter.shutdown();
        MainManager.getInstance().clearTwitterCredentials();
    }

    public static void sendTweet(final String msg, final Context context) {

        // TwitterAsync asyncTask=new TwitterAsync(msg, context);
        // asyncTask.execute();
    }

}
