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

import org.json.JSONException;
import org.json.JSONObject;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.woTagFuonnectivity.FacebookHttpConnectionManager;
import com.wootag.util.Alerts;

public class UploadToFacebookAsync extends AsyncTask<Void, Void, Void> {

    private static final String MESSAGE2 = "message";

    private static final String ERROR2 = "error";

    private static final String VIDEO_HAS_BEEN_UPLOADED_SUCCESSFULLY_ON_TO_FACEBOOK = "Video has been uploaded successfully on to facebook";

    private static final String ID = "id";

    private static final Logger LOG = LoggerManager.getLogger();

    private final Context context;
    // private String videoPath;
    // private String videoName;
    private volatile boolean running = true;
    private static final int ERROR = 0;
    private static final int CANCELLED = 1;
    private static final int SUCCESS = 2;
    private static final int FAILED = 3;
    private static int status = -1;
    private Exception raisedException;
    private ProgressDialog progressDialog;
    private String message;
    private final FacebookHttpConnectionManager connection;
    // private int checking;
    private final String friendId;

    public UploadToFacebookAsync(final String videoPath, final Context context, final String videoName, final String id) {

        this.context = context;
        // this.videoName = videoName;
        // this.videoPath = videoPath;
        this.friendId = id;
        this.connection = new FacebookHttpConnectionManager(this.context);
    }

    @Override
    protected Void doInBackground(final Void... params) {

        while (this.running) {
            try {
                // message = connection.uploadtoFacebookVideo(context,friendId, videoPath,videoName);
                if (this.message != null) {
                    final JSONObject json = new JSONObject(this.message);
                    if (json.has(ID)) {
                        this.message = VIDEO_HAS_BEEN_UPLOADED_SUCCESSFULLY_ON_TO_FACEBOOK;
                    } else if (json.has(ERROR2)) {
                        final String error = json.getString(ERROR2);
                        if (error != null) {
                            final JSONObject errorJson = new JSONObject(error);
                            if (errorJson.has(MESSAGE2)) {
                                this.message = errorJson.getString(MESSAGE2);
                            }
                        }
                    }
                }
                status = SUCCESS;
                this.running = false;
            } catch (final JSONException exception) {
                status = ERROR;
                this.raisedException = exception;
                this.running = false;
                LOG.e(exception);
            }
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
        if (status == SUCCESS) {
            Alerts.showAlertOnly("Info", this.message, this.context);
        } else if (status == ERROR) {
            Alerts.showAlertOnly("Error", this.raisedException.toString(), this.context);
        }

    }

    @Override
    protected void onPreExecute() {

        super.onPreExecute();
        this.progressDialog = new ProgressDialog(this.context);
        this.progressDialog.setMessage("Uploading ....");
        this.progressDialog.setIndeterminate(true);
        this.progressDialog.setCancelable(true);
        this.progressDialog.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(final DialogInterface dialog) {

                UploadToFacebookAsync.this.cancel(true);
            }
        });
        this.progressDialog.show();
    }
}
