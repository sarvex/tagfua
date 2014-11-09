/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.connectivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;
import com.wootag.util.Util;

public class ConnectivityChangedReceiver extends BroadcastReceiver {

    private static final String BACKGROUND_FILE_TRANSFER_SERVICE = "BackgroundFileTransferService";

    private static final Logger LOG = LoggerManager.getLogger();

    @Override
    public void onReceive(final Context context, final Intent intent) {

        LOG.d("connction changed receiver.");

        if (Util.isConnected(context)) {
            LOG.d("Connected.");

            if (Util.isServiceRunning(context, BACKGROUND_FILE_TRANSFER_SERVICE)) {
                LOG.d("BFTS is already running.");
            } else {
                LOG.d("Starting BFTS.");
                WakefulIntentService.sendWakefulWork(context, WootagUploadService.class);
            }

        } else {
            LOG.d("Disconnected.");
        }
    }
}
