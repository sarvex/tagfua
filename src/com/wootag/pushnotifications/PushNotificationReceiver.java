/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wTagFupushnotifications;

import android.content.Context;

import com.google.android.gcm.GCMBroadcastReceiver;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

public class PushNotificationReceiver extends GCMBroadcastReceiver {

    private static final Logger LOG = LoggerManager.getLogger();

    @Override
    protected String getGCMIntentServiceClassName(final Context context) {

        return "com.wootag.GCMIntentService";
    }

}
