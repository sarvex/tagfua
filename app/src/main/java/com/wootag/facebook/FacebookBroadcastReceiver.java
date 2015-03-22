package com.TagFu.facebook;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.TagFu.facebook.internal.NativeProtocol;

/**
 * This class implements a simple BroadcastReceiver designed to listen for broadcast notifications from the Facebook
 * app. At present, these notifications consistent of success/failure notifications for photo upload operations that
 * happen in the background. Applications may subclass this class and register it in their AndroidManifest.xml,
 * listening on the com.facebook.platform.AppCallResultBroadcast action.
 */
public class FacebookBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {

        final String appCallId = intent.getStringExtra(NativeProtocol.EXTRA_PROTOCOL_CALL_ID);
        final String action = intent.getStringExtra(NativeProtocol.EXTRA_PROTOCOL_ACTION);
        if ((appCallId != null) && (action != null)) {
            final Bundle extras = intent.getExtras();

            if (NativeProtocol.isErrorResult(intent)) {
                this.onFailedAppCall(appCallId, action, extras);
            } else {
                this.onSuccessfulAppCall(appCallId, action, extras);
            }
        }
    }

    protected void onFailedAppCall(final String appCallId, final String action, final Bundle extras) {

        // Default does nothing.
    }

    protected void onSuccessfulAppCall(final String appCallId, final String action, final Bundle extras) {

        // Default does nothing.
    }
}
