/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.TagFu;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.TagFu.adapter.UploadingFileQueueAdapter;
import com.TagFu.connectivity.VideoDataBase;
import com.TagFu.dto.VideoInfo;
import com.TagFu.util.Config;
import com.TagFu.util.MainManager;

/**
 * @author sarvex
 */
public class UploadingFileQueueActivity extends Activity {

    public static UploadingFileQueueActivity uploadingFileQueueActivity;

    private static final Logger LOG = LoggerManager.getLogger();
    private final BroadcastReceiver fileUploadNotificationReciver = new BroadcastReceiver() {

        /*
         * (non-Javadoc)
         * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
         */
        /*
         * (non-Javadoc)
         * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
         */
        @Override
        public void onReceive(final android.content.Context context, final Intent intent) {

            final String action = intent.getAction();

            if (action != null) {
                if (Constant.UPLOADED_PERCENTAGE.equalsIgnoreCase(action)) {
                    final UploadingFileQueueAdapter adapter = (UploadingFileQueueAdapter) UploadingFileQueueActivity.this.listView
                            .getAdapter();
                    if (adapter != null) {
                        UploadingFileQueueActivity.this.pendingVideos = UploadingFileQueueActivity.this
                                .getPendingUploads();
                        UploadingFileQueueActivity.this.listView.setAdapter(new UploadingFileQueueAdapter(
                                UploadingFileQueueActivity.this, 0, UploadingFileQueueActivity.this.pendingVideos));

                        adapter.notifyDataSetChanged();
                    }
                } else if (Constant.VIDEO_UPLOADED.equalsIgnoreCase(action)) {
                    final UploadingFileQueueAdapter adapter = (UploadingFileQueueAdapter) UploadingFileQueueActivity.this.listView
                            .getAdapter();
                    if (adapter != null) {
                        UploadingFileQueueActivity.this.pendingVideos = UploadingFileQueueActivity.this
                                .getPendingUploads();
                        UploadingFileQueueActivity.this.listView.setAdapter(new UploadingFileQueueAdapter(
                                UploadingFileQueueActivity.this, 0, UploadingFileQueueActivity.this.pendingVideos));
                        // VideoPlayerConstants.uploadedPercentage = 0;
                        adapter.notifyDataSetChanged();
                    }
                } else if (Constant.WAITING_FOR_PUBLISH.equalsIgnoreCase(action)
                        || Constant.FILE_UPLOADED.equalsIgnoreCase(action)) {
                    final UploadingFileQueueAdapter adapter = (UploadingFileQueueAdapter) UploadingFileQueueActivity.this.listView
                            .getAdapter();
                    if (adapter != null) {
                        UploadingFileQueueActivity.this.pendingVideos = UploadingFileQueueActivity.this
                                .getPendingUploads();
                        Config.setUploadedPercentage(100);
                        UploadingFileQueueActivity.this.listView.setAdapter(new UploadingFileQueueAdapter(
                                UploadingFileQueueActivity.this, 0, UploadingFileQueueActivity.this.pendingVideos));
                        adapter.notifyDataSetChanged();
                    }
                } else if (Constant.ACTION_FILE_UPLOAD_PROGRESS.equalsIgnoreCase(action)) {
                    final UploadingFileQueueAdapter adapter = (UploadingFileQueueAdapter) UploadingFileQueueActivity.this.listView
                            .getAdapter();
                    if (adapter != null) {
                        final int uploadedPercentage = intent.getExtras().getInt(Constant.ACTION_FILE_UPLOAD_PROGRESS);
                        Config.setUploadedPercentage(uploadedPercentage);
                        adapter.notifyDataSetChanged();
                    }
                }

            }

        }
    };

    protected ListView listView;
    protected List<VideoInfo> pendingVideos;

    private void registerBroadcastReceiver() {

        final IntentFilter messageNotificationAndSubscriptionFilter = new IntentFilter();
        messageNotificationAndSubscriptionFilter.addAction(Constant.UPLOADED_PERCENTAGE);
        messageNotificationAndSubscriptionFilter.addAction(Constant.ACTION_FILE_UPLOAD_PROGRESS);
        messageNotificationAndSubscriptionFilter.addAction(Constant.VIDEO_UPLOADED);
        messageNotificationAndSubscriptionFilter.addAction(Constant.WAITING_FOR_PUBLISH);
        messageNotificationAndSubscriptionFilter.addAction(Constant.FILE_UPLOADED);
        this.registerReceiver(this.fileUploadNotificationReciver, messageNotificationAndSubscriptionFilter);

    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.uploading_file_queue);
        uploadingFileQueueActivity = this;
        this.pendingVideos = new ArrayList<VideoInfo>();
        this.pendingVideos = this.getPendingUploads();
        final Button search = (Button) this.findViewById(R.id.settings);
        search.setVisibility(View.GONE);
        final TextView heading = (TextView) this.findViewById(R.id.heading);
        heading.setText("Pending Uploads");
        final Button menu = (Button) this.findViewById(R.id.menu);
        menu.setVisibility(View.GONE);
        final Button back = (Button) this.findViewById(R.id.back);
        back.setVisibility(View.VISIBLE);
        back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                UploadingFileQueueActivity.this.finish();
            }
        });
        // LoadingTextView=(TextView)findViewById(R.id.text);
        this.listView = (ListView) this.findViewById(R.id.fileQueueListView);
        this.listView.setAdapter(new UploadingFileQueueAdapter(this, 0, this.pendingVideos));

        this.registerBroadcastReceiver();
    }

    @Override
    protected void onDestroy() {

        if (this.fileUploadNotificationReciver != null) {
            this.unregisterReceiver(this.fileUploadNotificationReciver);
        }

        super.onDestroy();
    }

    /** Returns all pending uplaods from db */
    List<VideoInfo> getPendingUploads() {

        final ArrayList<VideoInfo> pendingVideos = new ArrayList<VideoInfo>();
        final List<VideoInfo> videoInfos = VideoDataBase.getInstance(this.getApplicationContext())
                .getAllNonUploadList();
        if ((videoInfos != null) && (videoInfos.size() > 0)) {
            for (int i = 0; i < videoInfos.size(); i++) {
                final VideoInfo video = videoInfos.get(i);
                if (MainManager.getInstance().getUserId() != null) {
                    final int loggedInUserId = Integer.parseInt(MainManager.getInstance().getUserId().trim());
                    if ((loggedInUserId == video.getUserid()) && (video.getUploadStatus() != 1)) {
                        if ((video.getUploadStatus() == 2) || (video.getUploadStatus() == 3)) {
                            Config.setUploadedPercentage(VideoDataBase.getInstance(this).getVideoUploadPercentage(
                                    video.getVideoClientId(), this));
                        }
                        pendingVideos.add(video);
                    }
                }
            }
        }
        return pendingVideos;
    }
}
