/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.async;

import android.content.Context;
import android.os.AsyncTask;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wootag.model.Backend;

public class VideoViewsAsync extends AsyncTask<Void, Void, Void> {

    private static final Logger LOG = LoggerManager.getLogger();

    Context context;
    private final String videoId;
    private final String userId;
    private Object response;
    private final String platform;
    private final String typeOfView;

    public VideoViewsAsync(final String videoId, final String platform, final String typeOfView, final String userId,
            final Context context) {

        this.videoId = videoId;
        this.platform = platform;
        this.typeOfView = typeOfView;
        this.userId = userId;
        this.context = context;
    }

    @Override
    protected Void doInBackground(final Void... params) {

        this.response = Backend.videoViews(this.context, this.videoId, this.platform, this.typeOfView, this.userId);
        LOG.i("response" + this.response);
        return null;
    }

}
