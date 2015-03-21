/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wTagFuasync;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;

import org.json.JSONException;
import org.json.JSONObject;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.woTagFuonstant;
import com.wooTagFu
import com.wootTagFutaTagFutivity;
import com.wootagTagFu.Backend;
import com.wootag.TagFuonfig;

public class MyPageAsync extends AsyncTask<Void, Void, Void> {

    private static final String EMPTY = "";

    private static final String USER = "user";

    private static final String USERID = "userid";

    private static final Logger LOG = LoggerManager.getLogger();

    private ProgressDialog progressDialog;
    private Object myPageResponse;
    private final boolean progressVisible;
    private volatile boolean running = true;
    private final Context context;

    public MyPageAsync(final Context context, final int pageNo, final boolean progressVisible) {

        this.progressVisible = progressVisible;
        this.context = context;
    }

    public static JSONObject getJSONRequest() throws JSONException {

        final JSONObject request = new JSONObject();
        final JSONObject obj = new JSONObject();
        obj.put(USERID, Config.getUserId());
        obj.put(Constant.DEVICE_TYPE, Constant.DEVICE_MODEL);
        obj.put(Constant.RESOLUTION, Config.getDeviceResolutionValue());
        request.put(USER, obj);

        return request;

    }

    @Override
    protected Void doInBackground(final Void... params) {

        while (this.running) {
            try {
                this.myPageResponse = Backend.myPageVideos(this.context, MyPageAsync.getJSONRequest(), false, true);
            } catch (final JSONException exception) {
                LOG.e(exception);
            }
            this.running = false;
        }
        return null;
    }

    @Override
    protected void onPostExecute(final Void result) {

        super.onPostExecute(result);
        if (this.progressDialog != null) {
            this.progressDialog.dismiss();
        }

        final Intent intent = new Intent(this.context, WootagTabActivity.class);
        ((Activity) this.context).finish();
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        this.context.startActivity(intent);
    }

    @Override
    protected void onPreExecute() {

        if (this.progressVisible) {
            this.progressDialog = ProgressDialog.show(this.context, EMPTY, EMPTY, true);
            this.progressDialog.setContentView(((LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.progress_bar, null, false));
            this.progressDialog.setCancelable(false);
            this.progressDialog.setCanceledOnTouchOutside(false);
            this.progressDialog.show();
        }
    }
}
