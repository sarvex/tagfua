/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wTagFu

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ToggleButton;

import org.json.JSONException;
import org.json.JSONObject;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.woTagFuto.ErrorResponse;
import com.wooTagFuo.VideoDetails;
import com.wootTagFuel.Backend;
import com.wootaTagFumage;
import com.wootag.util.Alerts;

public class AccessPermissionActivity extends Activity {

    private static final Logger LOG = LoggerManager.getLogger();

    private Button backButton;
    private ImageView updateAccessPermission;
    private ImageView videoImage;
    private VideoDetails videoDetails;
    private int publicVideo;

    protected AccessPermissionActivity context;
    protected ToggleButton shareFollowersVideo;
    protected ToggleButton sharePrivateVideo;
    protected ToggleButton sharePublicVideo;

    public JSONObject getJSONRequest() throws JSONException {

        final JSONObject request = new JSONObject();
        final JSONObject obj = new JSONObject();

        obj.put(Constant.VIDEO_ID, this.videoDetails.getVideoID());
        obj.put(Constant.PUBLIC, this.publicVideo);
        request.put(Constant.VIDEO, obj);

        return request;

    }

    private void loadImage(final VideoDetails currentVideo) {

        if ((currentVideo != null) && (currentVideo.getVideothumbPath() != null)) {
            Image.displayImage(currentVideo.getVideothumbPath(), this, this.videoImage, 1);
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_access_permission);
        this.context = this;

        final Bundle bundle = this.getIntent().getExtras();
        if ((bundle != null) && bundle.containsKey(Constant.VIDEO)) {
            this.videoDetails = (VideoDetails) bundle.getSerializable(Constant.VIDEO);
        }

        this.shareFollowersVideo = (ToggleButton) this.findViewById(R.id.tooglefollowersbutton);
        this.sharePrivateVideo = (ToggleButton) this.findViewById(R.id.toogleprivatebutton);
        this.sharePublicVideo = (ToggleButton) this.findViewById(R.id.tooglepublicbutton);
        this.updateAccessPermission = (ImageView) this.findViewById(R.id.updateaccesspermission);
        this.backButton = (Button) this.findViewById(R.id.back);
        this.videoImage = (ImageView) this.findViewById(R.id.videoImage);

        this.loadImage(this.videoDetails);

        switch (this.videoDetails.getPublicVideo()) {
        case 0:
            this.sharePrivateVideo.setChecked(true);
            this.shareFollowersVideo.setChecked(false);
            this.sharePublicVideo.setChecked(false);
            break;

        case 1:
            this.sharePublicVideo.setChecked(true);
            this.shareFollowersVideo.setChecked(true);
            this.sharePrivateVideo.setChecked(false);
            break;

        case 2:
            this.shareFollowersVideo.setChecked(true);
            this.sharePublicVideo.setChecked(false);
            this.sharePrivateVideo.setChecked(false);
            break;

        default:
            break;
        }

        this.backButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                AccessPermissionActivity.this.finish();
            }
        });

        this.sharePublicVideo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                AccessPermissionActivity.this.sharePublicVideo.setChecked(true);
                AccessPermissionActivity.this.shareFollowersVideo.setChecked(true);
                AccessPermissionActivity.this.sharePrivateVideo.setChecked(false);
            }
        });
        this.shareFollowersVideo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                AccessPermissionActivity.this.shareFollowersVideo.setChecked(true);
                AccessPermissionActivity.this.sharePublicVideo.setChecked(false);
                AccessPermissionActivity.this.sharePrivateVideo.setChecked(false);
            }
        });
        this.sharePrivateVideo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                AccessPermissionActivity.this.sharePrivateVideo.setChecked(true);
                AccessPermissionActivity.this.shareFollowersVideo.setChecked(false);
                AccessPermissionActivity.this.sharePublicVideo.setChecked(false);
            }
        });
        this.updateAccessPermission.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                AccessPermissionActivity.this.updateAccessPermission();
            }
        });
    }

    protected void updateAccessPermission() {

        if (this.sharePrivateVideo.isChecked()) {
            this.publicVideo = 1;
        } else if (this.sharePublicVideo.isChecked()) {
            this.publicVideo = 0;
        } else if (this.shareFollowersVideo.isChecked()) {
            this.publicVideo = 2;
        }
        new updateAccessPermissionsAsyne(true).execute();
    }

    public class updateAccessPermissionsAsyne extends AsyncTask<Void, Void, Void> {

        private final boolean isProgressShow;
        private ProgressDialog progress;
        private Object returnObj;

        public updateAccessPermissionsAsyne(final boolean isProgressShow) {

            this.isProgressShow = isProgressShow;
        }

        @Override
        protected Void doInBackground(final Void... params) {

            try {
                this.returnObj = Backend.updateAccessPermissionPassword(AccessPermissionActivity.this.context,
                        AccessPermissionActivity.this.getJSONRequest().toString());
            } catch (final JSONException exception) {
                LOG.e(exception);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {

            super.onPostExecute(result);
            if (this.isProgressShow) {
                this.progress.dismiss();
            }
            if (this.returnObj == null) {
                Alerts.showInfoOnly(Constant.NO_RESPONSE_AVAILABLE, AccessPermissionActivity.this.context);

            } else {

                if (this.returnObj instanceof Boolean) {
                    Alerts.showInfoOnly(Constant.UPDATED_ACCESS_PERMISSIONS, AccessPermissionActivity.this.context);
                } else if (this.returnObj instanceof ErrorResponse) {
                    final ErrorResponse error = (ErrorResponse) this.returnObj;
                    Alerts.showInfoOnly(error.toString(), AccessPermissionActivity.this.context);
                }
            }
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            if (this.isProgressShow) {
                this.progress = ProgressDialog.show(AccessPermissionActivity.this.context, "", "", true);
                this.progress
                        .setContentView(((LayoutInflater) AccessPermissionActivity.this.context
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.progress_bar,
                                null, false));
                this.progress.setCancelable(false);
                this.progress.setCanceledOnTouchOutside(false);
                this.progress.show();
            }
        }
    }
}
