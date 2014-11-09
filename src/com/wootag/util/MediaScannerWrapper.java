/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.util;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

public class MediaScannerWrapper implements MediaScannerConnectionClient {

    private static final Logger LOG = LoggerManager.getLogger();

    private final Context ctx;
    private final MediaScannerConnection mConnection;
    private final String mMimeType;
    private final String mPath;

    public MediaScannerWrapper(final Context ctx, final String filePath, final String mime) {

        this.mPath = filePath;
        this.mMimeType = mime;
        this.ctx = ctx;
        this.mConnection = new MediaScannerConnection(ctx, this);
    }

    @Override
    public void onMediaScannerConnected() {

        this.mConnection.scanFile(this.mPath, this.mMimeType);
    }

    @Override
    public void onScanCompleted(final String path, final Uri uri) {

        this.mConnection.disconnect();
    }

    public void scan() {

        this.mConnection.connect();
    }

}
