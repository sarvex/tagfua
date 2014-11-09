/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.wootag;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;

import com.crashlytics.android.Crashlytics;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wootag.async.MyPageAsync;
import com.wootag.util.Config;
import com.wootag.util.MainManager;

public class SplashActivity extends Activity {

    protected static final Logger LOG = LoggerManager.getLogger();

    public static SplashActivity splash;
    protected SplashActivity context;

    public String getReloution() {

        final DisplayMetrics displaymetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        final int screenHeight = displaymetrics.heightPixels;
        final int screenWidth = displaymetrics.widthPixels;
        return screenWidth + "x" + screenHeight;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Crashlytics.start(this);
        this.setContentView(R.layout.activity_splash);
        Config.setDeviceResolutionValue(this.getReloution());
        MainManager.getInstance().setTwitterAuthorization(0);
        this.context = this;
        splash = this;
        Config.setNotificationPageVisit(false);
        Config.setMyPageVisit(false);
        new Thread(new Tasks()).start();

    }

    class Tasks implements Runnable {

        @Override
        public void run() {

            /** to show the flash screen 3 sec */
            for (int i = 0; i <= 2; i++) {
                try {
                    Thread.sleep(1000);
                } catch (final InterruptedException e) {
                    LOG.e(e);
                }
            }
            // MainManager.getInstance().setUserId("4") ;
            final String userID = MainManager.getInstance().getUserId();
            if ((userID != null) && (userID.length() > 0)) {
                final int userId = Integer.parseInt(userID);
                if (userId > 0) {
                    Config.setUserID(String.valueOf(userId));

                    SplashActivity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            new MyPageAsync(SplashActivity.this.context, 1, false).execute();
                        }
                    });

                }
            } else {
                if (!MainManager.getInstance().isFirstTimeInstall()) {
                    final Intent intent = new Intent(SplashActivity.this.context, IntrozoneActivity.class);
                    SplashActivity.this.finish();
                    SplashActivity.this.context.startActivity(intent);
                } else {
                    final Intent intent = new Intent(SplashActivity.this.context, SignInFragment.class);
                    SplashActivity.this.finish();
                    SplashActivity.this.context.startActivity(intent);
                }
            }

        }
    }

}
