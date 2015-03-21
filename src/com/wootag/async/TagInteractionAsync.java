/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wTagFuasync;

import android.content.Context;
import android.os.AsyncTask;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wootag.model.Backend;

public class TagInteractionAsync extends AsyncTask<Void, Void, Void> {

    private static final Logger LOG = LoggerManager.getLogger();

    private final Context context;
    private final String videoId;
    private final String socialPlatform;
    private final String interactions;
    private final String userId;
    private Object response;

    public TagInteractionAsync(final String videoId, final String socialPlatform, final String interactions,
            final String userId, final Context context) {

        this.videoId = videoId;
        this.socialPlatform = socialPlatform;
        this.interactions = interactions;
        this.userId = userId;
        this.context = context;
    }

    @Override
    protected Void doInBackground(final Void... params) {

        this.response = Backend.socialInteractions(this.context, this.videoId, this.socialPlatform, this.interactions,
                this.userId);
        LOG.i("response" + this.response);
        return null;
    }

}
