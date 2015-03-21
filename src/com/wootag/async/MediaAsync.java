/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wTagFuasync;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wootag.util.MediaScannerWrapper;

public class MediaAsync extends AsyncTask<Void, Void, Void> {

    private static final Logger LOG = LoggerManager.getLogger();

    private final Context context;
    private Status statusObj;
    private volatile boolean running = true;
    private static final int ERROR = 0;
    private static final int CANCELLED = 1;
    private static int status = -1;
    private Exception raisedException;
    private ProgressDialog progressDialog;
    private final String path;
    private MediaScannerWrapper wrap;

    public MediaAsync(final Context context, final String path) {

        this.context = context;
        this.path = path;
    }

    @Override
    protected Void doInBackground(final Void... params) {

        while (this.running) {
            // try {
            this.wrap = new MediaScannerWrapper(this.context, this.path, "video/3gp");
            this.wrap.scan();
            this.running = false;
            // } catch (final Exception ex) {
            // status = ERROR;
            // this.raisedException = ex;
            // this.running = false;
            // LOG.e(ex);
            // }
        }
        return null;

    }

    @Override
    protected void onCancelled() {

        if (this.progressDialog != null) {
            this.progressDialog.dismiss();
        }

        status = CANCELLED;
        this.running = false;
    }

    @Override
    protected void onPostExecute(final Void result) {

        super.onPostExecute(result);
        if (this.progressDialog != null) {
            this.progressDialog.dismiss();
        }

        // ((Activity) context).finish();

    }

    @Override
    protected void onPreExecute() {

        super.onPreExecute();
        this.progressDialog = new ProgressDialog(this.context);
        this.progressDialog.setMessage("Saving video ....");
        this.progressDialog.setIndeterminate(true);
        this.progressDialog.setCancelable(true);
        this.progressDialog.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(final DialogInterface dialog) {

                MediaAsync.this.cancel(true);
            }
        });
        this.progressDialog.show();
    }
}
