/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.pushnotifications;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gcm.GCMRegistrar;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wootag.Constant;
import com.wootag.util.Config;
import com.wootag.util.GCMServerUtilities;
import com.wootag.util.Util;

public final class GCMRegistration {

    private static final Logger LOG = LoggerManager.getLogger();

    private static GCMRegistration registerToGCM;
    protected final Context context;
    protected AsyncTask<Void, Void, Void> mRegisterTask;

    private GCMRegistration(final Context context) {

        this.context = context;
    }

    public static synchronized GCMRegistration getInstance(final Context context) {

        if (registerToGCM == null) {
            registerToGCM = new GCMRegistration(context);
        }
        return registerToGCM;
    }

    public void getRegistered() {

        if (!Util.isConnected(this.context)) {
            return;
        }
        GCMRegistrar.checkDevice(this.context);

        // Make sure the manifest was properly set - comment out this line
        // while developing the app, then uncomment it when it's ready.
        GCMRegistrar.checkManifest(this.context);

        final String regId = GCMRegistrar.getRegistrationId(this.context);

        if (regId.equals("")) {

            GCMRegistrar.register(this.context, Constant.GCM_SENDER_ID);

        } else {
            if (GCMRegistrar.isRegisteredOnServer(this.context)) {
                // Toast.makeText(context, "Already registered with GCM",
                // Toast.LENGTH_LONG).show();
                Config.setDeviceToken(regId);
                LOG.i("push notifications register id" + regId);
            } else {
                final Context applicationContext = this.context;
                this.mRegisterTask = new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(final Void... params) {

                        GCMServerUtilities.register(applicationContext,
                                Util.getApplicationName(GCMRegistration.this.context),
                                Util.getImei(GCMRegistration.this.context), regId);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(final Void result) {

                        GCMRegistration.this.mRegisterTask = null;
                    }

                };
                this.mRegisterTask.execute(null, null, null);
            }
        }

    }

}
