/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.TagFu.async;

import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.widget.Toast;

import org.json.JSONException;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.TagFu.Constant;
import com.TagFu.PlayerActivity;
import com.TagFu.R;
import com.TagFu.connectivity.VideoDataBase;
import com.TagFu.dto.ErrorResponse;
import com.TagFu.dto.Playback;
import com.TagFu.dto.TagInfo;
import com.TagFu.dto.VideoDetails;
import com.TagFu.dto.VideoInfo;
import com.TagFu.model.Backend;
import com.TagFu.util.Alerts;
import com.TagFu.util.Config;

public class PlaybackAsync extends AsyncTask<Void, Void, Void> {

    private static final String EMPTY = "";

    private static final String PROBLEM_WITH_SERVER_TRY_AFTER_SOMETIME = "Problem with server Try after sometime";

    private static final Logger LOG = LoggerManager.getLogger();

    private final Context context;
    private boolean status;
    private volatile boolean running = true;
    private ProgressDialog progressDialog;
    private final String videoId;
    private VideoInfo video;
    private List<TagInfo> playbackTags;
    private Playback playbackdata;
    private Object obj;

    public PlaybackAsync(final Context mcontext, final String id) {

        this.context = mcontext;
        this.videoId = id;
    }

    @Override
    protected Void doInBackground(final Void... params) {

        while (this.running) {
            try {
                this.obj = Backend.playBack(this.context, this.videoId);
            } catch (final JSONException exception) {
                LOG.e(exception);
            }// videoId
            if (this.obj instanceof Playback) {
                this.playbackdata = (Playback) this.obj;
                //
                VideoDataBase.getInstance(this.context).updateVideoUrl(this.playbackdata.getVideoId(),
                        this.playbackdata.getVideoUrl());
                /**
                 * saving all the tags into local database
                 */
                if (this.playbackdata != null) {
                    final List<TagInfo> playBacktags = this.playbackdata.getTags();
                    if ((playBacktags != null) && (playBacktags.size() > 0)) {
                        for (int i = 0; i < playBacktags.size(); i++) {
                            final TagInfo tag = playBacktags.get(i);
                            if (VideoDataBase.getInstance(this.context).getTagByTagId(this.playbackdata.getVideoId(),
                                    String.valueOf(tag.getServertagId()), this.context) == 0) {
                                VideoDataBase.getInstance(this.context).saveTag(tag, this.context);
                            } else {
                                VideoDataBase.getInstance(this.context).updateTag(tag, this.context, true);
                            }
                        }
                        this.status = true;
                    }
                }
            }
            this.running = false;
        }

        return null;
    }

    @Override
    protected void onCancelled() {

        if (this.progressDialog != null) {
            this.progressDialog.dismiss();
        }
        this.status = false;
        this.running = false;
    }

    @Override
    protected void onPostExecute(final Void result) {

        super.onPostExecute(result);
        if (this.progressDialog != null) {
            this.progressDialog.dismiss();
        }
        if (this.obj instanceof ErrorResponse) {
            final ErrorResponse response = (ErrorResponse) this.obj;
            Alerts.showAlertOnly("Info", response.getMessage(), this.context);
        } else if (this.obj instanceof Playback) {
            // System.gc();

            if (this.playbackdata != null) {
                new VideoViewsAsync(this.videoId, Constant.ANDROID_PLATFORM, "0", Config.getUserId(), this.context)
                        .execute();
                final Intent intent = new Intent(this.context, PlayerActivity.class);
                final VideoDetails video = new VideoDetails();
                video.setUserId(this.playbackdata.getUid());
                video.setVideoTitle(this.playbackdata.getVideoTitle());
                video.setVideoID(this.playbackdata.getVideoId());
                video.setVideothumbPath(this.playbackdata.getThumbPath());
                video.setVideoURL(this.playbackdata.getVideoUrl());
                video.setName(this.playbackdata.getUserName());
                video.setPhotoPath(this.playbackdata.getUserImagePath());
                video.setShareUrl(this.playbackdata.getShareUrl());
                video.setFbShareUrl(this.playbackdata.getFbShareUrl());
                intent.putExtra("video", video);
                intent.putExtra(Constant.PATH, this.playbackdata.getVideoUrl());
                intent.putExtra(Constant.CLIENT_ID, 0);
                intent.putExtra(Constant.SERVER_ID, this.playbackdata.getVideoId());
                intent.putExtra(Constant.TITLE, this.playbackdata.getVideoTitle());
                intent.putExtra(Constant.DESC, this.playbackdata.getVideoDescription());
                intent.putExtra(Constant.USERID, this.playbackdata.getUid());
                this.context.startActivity(intent);
            }
        } else {
            Toast.makeText(this.context, PROBLEM_WITH_SERVER_TRY_AFTER_SOMETIME, Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onPreExecute() {

        super.onPreExecute();

        this.progressDialog = ProgressDialog.show(this.context, EMPTY, EMPTY, true);
        this.progressDialog.setContentView(((LayoutInflater) this.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.progress_bar, null, false));
        this.progressDialog.setCancelable(false);
        this.progressDialog.setCanceledOnTouchOutside(false);
        this.progressDialog.show();

    }

}
