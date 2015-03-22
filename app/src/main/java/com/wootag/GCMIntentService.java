/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.TagFu;

import android.content.Context;
import android.content.Intent;

import com.google.android.gcm.GCMBaseIntentService;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.TagFu.dto.Notification;
import com.TagFu.util.Config;
import com.TagFu.util.GCMServerUtilities;
import com.TagFu.util.MainManager;
import com.TagFu.util.Util;

public class GCMIntentService extends GCMBaseIntentService {

    private static final String NOTIFICATION_ID = "notification_id";
    private static final String SOUND2 = "sound";
    private static final String VIBRATE2 = "vibrate";
    private static final String TICKER_TEXT = "tickerText";
    private static final String SUBTITLE2 = "subtitle";
    private static final String TITLE2 = "title";
    private static final String MESSAGE2 = "message";
    private static final String FEEDNOTIFICATION = "feednotification";
    private static final String NOTIFICATION = "notification";
    private static final String _7 = "7";
    private static final Logger LOG = LoggerManager.getLogger();

    public GCMIntentService() {

        super(Constant.GCM_SENDER_ID);
    }

    /**
     * Method called on Error
     */
    @Override
    public void onError(final Context context, final String errorId) {

        LOG.i("Received error: " + errorId);
        // Utils.displayMessage(context, getString(R.string.gcm_error,
        // errorId));
    }

    private void sendNotificationToSpecificActivity(final Notification notificationDto, final Context context) {

        if (_7.equalsIgnoreCase(notificationDto.getNoticeId().trim())) {
            final Intent intent = new Intent(FEEDNOTIFICATION);
            context.sendBroadcast(intent);
        } else {
            final Intent intent = new Intent(NOTIFICATION);
            context.sendBroadcast(intent);
        }

    }

    /**
     * Method called on receiving a deleted message
     */
    @Override
    protected void onDeletedMessages(final Context context, final int total) {

        final String message = this.getString(R.string.gcm_deleted, Integer.valueOf(total));
        LOG.i(message);
    }

    /**
     * Method called on Receiving a new message
     */

    @Override
    protected void onMessage(final Context context, final Intent intent) {

        LOG.i("Received message");
        // int count=0;
        final String message = intent.getExtras().getString(MESSAGE2);
        final String title = intent.getExtras().getString(TITLE2);
        final String subTitle = intent.getExtras().getString(SUBTITLE2);
        final String ticker = intent.getExtras().getString(TICKER_TEXT);
        final String vibrate = intent.getExtras().getString(VIBRATE2);
        final String sound = intent.getExtras().getString(SOUND2);
        final String notificationId = intent.getExtras().getString(NOTIFICATION_ID);

        Util.displayMessage(context, message);
        // notifies user
        if (Util.isAppForground(this.getApplicationContext())) {
            final Notification notificationDto = new Notification();
            notificationDto.setMessage(message);
            notificationDto.setNoticeId(notificationId);
            this.sendNotificationToSpecificActivity(notificationDto, context);
        } else {
            if (!_7.equalsIgnoreCase(notificationId.trim())) {
                Util.generateNotification(context, message, title, subTitle, ticker, vibrate, sound);
            }
        }
    }

    @Override
    protected boolean onRecoverableError(final Context context, final String errorId) {

        // log message
        LOG.i("Received recoverable error: " + errorId);
        /*
         * Utils.displayMessage(context, getString(R.string.gcm_recoverable_error, errorId));
         */
        return super.onRecoverableError(context, errorId);
    }

    /**
     * Method called on device registered
     **/
    @Override
    protected void onRegistered(final Context context, final String registrationId) {

        LOG.i("Device registered: regId = " + registrationId);
        // Utils.displayMessage(context, "Your device registred with GCM");
        // GCMServerUtilities.register(context, Utils.getApplicationName(context),
        // Utils.getImei(context), registrationId);
        Config.setDeviceToken(registrationId);
        MainManager.getInstance().setDeviceToken(Config.getDeviceToken());
    }

    /**
     * Method called on device un registred
     */
    @Override
    protected void onUnregistered(final Context context, final String registrationId) {

        LOG.i("Device unregistered");
        // Utils.displayMessage(context, getString(R.string.gcm_unregistered));
        GCMServerUtilities.unregister(context, registrationId);
    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     */

}
