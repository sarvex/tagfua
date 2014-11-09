/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.video.trimmer.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import net.video.trimmer.natives.VideoTrimmer;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

public class VideoTrimmingService extends IntentService {

    private static final Logger LOG = LoggerManager.getLogger();

    public static boolean trimming;
    private VideoTrimmer trimmer;

    public VideoTrimmingService() {

        super("VideoTrimmingService");
        this.setVideoTrimmer(new VideoTrimmer());
    }

    @Override
    public void onCreate() {

        super.onCreate();
        System.loadLibrary("ffmpeg");
        System.loadLibrary("video-trimmer");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {

        trimming = true;
        LOG.i("On bind of service");
        final Bundle extras = intent.getExtras();
        final String inputFileName = extras.getString("inputFileName");
        final String outFileName = extras.getString("outputFileName");
        final int start = extras.getInt("start");
        final int duration = extras.getInt("duration");

        final Messenger messenger = (Messenger) extras.get("messenger");
        LOG.i("Starting trimming");
        // System.gc();
        boolean error = false;

        final int returnStatus = this.trimmer.trim_(inputFileName, outFileName, start, duration);
        error = returnStatus != 0;

        final String messageText = error ? "Unable to trim the video. Check the error logs."
                : "Trimmed video succesfully to " + outFileName;
        LOG.i("Sending message: " + messageText);
        try {
            final Message message = new Message();
            message.getData().putString("text", messageText);
            messenger.send(message);
        } catch (final RemoteException e) {
            LOG.i("Exception while sending message");
        }
        trimming = false;
    }

    void setVideoTrimmer(final VideoTrimmer t) {

        this.trimmer = t;

    }
}
