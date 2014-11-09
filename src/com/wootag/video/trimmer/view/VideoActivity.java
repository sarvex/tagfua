/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.wootag.video.trimmer.view;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.MediaColumns;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wootag.AndroidVideoCapture;
import com.wootag.R;
import com.wootag.connectivity.VideoDataBase;
import com.wootag.dto.VideoInfo;
import com.wootag.util.Alerts;
import com.wootag.util.MainManager;

public class VideoActivity extends Activity {

    private static final int ACTIVITY_CHOOSE_FILE = 1;

    private static final Logger LOG = LoggerManager.getLogger();
    private Cursor videocursor;
    private ListView videolist;

    public String getRealPathFromURI(final Context context, final Uri contentUri) {

        Cursor cursor = null;
        try {
            final String[] proj = { MediaColumns.DATA };
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            final int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.record_options);
        final RelativeLayout record = (RelativeLayout) this.findViewById(R.id.recordVideo);//
        final RelativeLayout chooseFromLibrary = (RelativeLayout) this.findViewById(R.id.chooseFromLibrary);
        final RelativeLayout cancel = (RelativeLayout) this.findViewById(R.id.cancelUploadvideo);
        cancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                VideoActivity.this.finish();
            }
        });

        chooseFromLibrary.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (VideoActivity.this.shouldAllowToTrim()) {
                    final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("video/*");
                    VideoActivity.this.startActivityForResult(Intent.createChooser(intent, "Pick video from"),
                            VideoActivity.ACTIVITY_CHOOSE_FILE);
                } else {
                    Alerts.showAlert("Info", "You have a video currently uploading,"
                            + " please wait till it get finished. We are working to improve this!", VideoActivity.this);
                }
            }
        });

        record.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                final Intent recordIntent = new Intent(VideoActivity.this, AndroidVideoCapture.class);
                VideoActivity.this.finish();
                VideoActivity.this.startActivity(recordIntent);
            }
        });

    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {

        if ((requestCode == ACTIVITY_CHOOSE_FILE) && (resultCode == RESULT_OK)) {
            String mimeType = null;
            String videoPath = null;

            LOG.d("Pick Video Intent data: " + data);
            if ((data != null) && (data.getData() != null)) {
                final Uri uri = data.getData();

                if ((uri != null) && "content".equals(uri.getScheme())) {
                    final Cursor cursor = this.getContentResolver().query(uri,
                            new String[] { MediaColumns.DATA, MediaColumns.MIME_TYPE }, null, null, null);

                    if (cursor != null) {
                        final int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
                        final int mime_column_index = cursor.getColumnIndexOrThrow(MediaColumns.MIME_TYPE);
                        cursor.moveToFirst();
                        videoPath = cursor.getString(column_index);
                        mimeType = cursor.getString(mime_column_index);
                        if (TextUtils.isEmpty(videoPath)) {
                            Toast.makeText(
                                    this,
                                    "Could not get the local media path."
                                            + " Please pick another video, or use another video source.",
                                    Toast.LENGTH_LONG).show();
                            cursor.close();
                            return;
                        }
                        cursor.close();
                    }

                } else if ((uri != null) && "file".equals(uri.getScheme())) {
                    mimeType = data.getType();
                    videoPath = uri.getPath();
                }

                if (((mimeType != null) && (videoPath != null)) && !mimeType.startsWith("video/")) {
                    Toast.makeText(this, "The item you picked is not a video. Please pick a video.", Toast.LENGTH_LONG)
                            .show();
                    return;
                }
            }
            final Intent intent = new Intent(VideoActivity.this, ViewVideo.class);
            intent.putExtra("videofilename", videoPath);
            this.finish();
            this.startActivity(intent);
        }
    }

    boolean shouldAllowToTrim() {

        boolean isAllowToTrim = true;
        final List<VideoInfo> videoInfos = VideoDataBase.getInstance(this.getApplicationContext())
                .getAllNonUploadList();
        if ((videoInfos != null) && (videoInfos.size() > 0)) {
            for (int i = 0; i < videoInfos.size(); i++) {
                final VideoInfo video = videoInfos.get(i);
                if (MainManager.getInstance().getUserId() != null) {
                    final int loggedInUserId = Integer.parseInt(MainManager.getInstance().getUserId().trim());
                    if ((loggedInUserId == video.getUserid()) && (video.getUploadStatus() != 1)) {
                        if ((video.getUploadStatus() == 2)
                                || ((video.getUploadStatus() == 3) && (video.getRetry() == 0))) {
                            isAllowToTrim = false;
                            break;
                        }
                    }
                }
            }
        }
        return isAllowToTrim;
    }
}
