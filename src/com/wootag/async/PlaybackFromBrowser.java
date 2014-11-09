/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.async;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.widget.Toast;

import org.json.JSONException;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wootag.Constant;
import com.wootag.PlayerActivity;
import com.wootag.R;
import com.wootag.connectivity.VideoDataBase;
import com.wootag.dto.ErrorResponse;
import com.wootag.dto.Playback;
import com.wootag.dto.TagInfo;
import com.wootag.dto.VideoDetails;
import com.wootag.dto.VideoInfo;
import com.wootag.model.Backend;
import com.wootag.util.Alerts;

public class PlaybackFromBrowser extends AsyncTask<Void, Void, Void> {

    private static final String EMPTY = "";
    private static final String _0 = "0";
    private static final String _1 = "1";
    private static final String _2 = "2";

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
    private final String loggedInUserId;
    private boolean allowedToPlay;
    private String errorMessage;

    public PlaybackFromBrowser(final Context mcontext, final String id, final String loggedInUserId) {

        this.context = mcontext;
        this.videoId = id;
        this.loggedInUserId = loggedInUserId;
    }

    private void saveTags(final Playback playbackdata) {

        VideoDataBase.getInstance(this.context).updateVideoUrl(playbackdata.getVideoId(), playbackdata.getVideoUrl());
        if (playbackdata != null) {
            final List<TagInfo> playBacktags = playbackdata.getTags();
            if ((playBacktags != null) && (playBacktags.size() > 0)) {
                for (int i = 0; i < playBacktags.size(); i++) {
                    final TagInfo tag = playBacktags.get(i);
                    if (VideoDataBase.getInstance(this.context).getTagByTagId(playbackdata.getVideoId(),
                            String.valueOf(tag.getServertagId()), this.context) == 0) {
                        VideoDataBase.getInstance(this.context).saveTag(tag, this.context);
                    } else {
                        VideoDataBase.getInstance(this.context).updateTag(tag, this.context, true);
                    }
                }
            }
        }
    }

    @Override
    protected Void doInBackground(final Void... params) {

        while (this.running) {
            try {
                this.obj = Backend.playBack(this.context, this.videoId);
            } catch (final JSONException exception1) {
                LOG.e(exception1);
            }// videoId
            if (this.obj instanceof Playback) {
                this.playbackdata = (Playback) this.obj;
                this.saveTags(this.playbackdata);
                if (_1.equalsIgnoreCase(this.playbackdata.getPublicVideo())) {
                    this.allowedToPlay = true;
                } else if (_2.equalsIgnoreCase(this.playbackdata.getPublicVideo())) {
                    try {
                        final Object isFollwer = Backend.isCheckFollower(this.context, this.loggedInUserId,
                                this.playbackdata.getUid());
                        if (isFollwer instanceof Boolean) {
                            final boolean followerFlag = (Boolean) isFollwer;
                            if (followerFlag) {
                                this.allowedToPlay = true;
                            } else {
                                this.allowedToPlay = false;
                            }
                        } else if (isFollwer instanceof ErrorResponse) {
                            final ErrorResponse isfollowerResp = (ErrorResponse) isFollwer;
                            this.errorMessage = isfollowerResp.getMessage();
                            this.allowedToPlay = false;
                        }
                    } catch (final JSONException exception) {
                        LOG.e(exception);
                    }
                } else if (_0.equalsIgnoreCase(this.playbackdata.getPublicVideo())) {
                    try {
                        final Object isInPrivate = Backend.isPrivateGroup(this.context, this.loggedInUserId,
                                this.playbackdata.getUid());
                        if (isInPrivate instanceof Boolean) {
                            final boolean privateFlag = (Boolean) isInPrivate;
                            if (privateFlag) {
                                this.allowedToPlay = true;
                            } else {
                                this.allowedToPlay = false;
                            }
                        } else if (isInPrivate instanceof ErrorResponse) {
                            final ErrorResponse isfollowerResp = (ErrorResponse) isInPrivate;
                            this.errorMessage = isfollowerResp.getMessage();
                            this.allowedToPlay = false;
                        }
                    } catch (final JSONException exception) {
                        LOG.e(exception);
                    }
                }

            } else if (this.obj instanceof ErrorResponse) {
                final ErrorResponse isPlaybackResp = (ErrorResponse) this.obj;
                this.errorMessage = isPlaybackResp.getMessage();
                this.allowedToPlay = false;
            }
            this.running = false;
        }

        return null;
    }

    @Override
    protected void onPostExecute(final Void result) {

        super.onPostExecute(result);
        if ((this.progressDialog != null) && this.progressDialog.isShowing()) {
            this.progressDialog.dismiss();
        }
        if (!this.allowedToPlay) {
            Alerts.showAlert("Info", this.errorMessage, this.context);
        } else if (this.allowedToPlay) {
            if (this.playbackdata != null) {
                final Intent intent = new Intent(this.context, PlayerActivity.class);
                final VideoDetails video = new VideoDetails();
                video.setUserId(this.playbackdata.getUid());
                video.setVideoID(this.playbackdata.getVideoId());
                video.setVideothumbPath(this.playbackdata.getThumbPath());
                video.setVideoURL(this.playbackdata.getVideoUrl());
                video.setName(this.playbackdata.getUserName());
                video.setPhotoPath(this.playbackdata.getUserImagePath());
                video.setPhotoPath(this.playbackdata.getUserImagePath());
                video.setShareUrl(this.playbackdata.getShareUrl());
                intent.putExtra("video", video);
                intent.putExtra("frombrowser", true);
                intent.putExtra(Constant.PATH, this.playbackdata.getVideoUrl());
                intent.putExtra(Constant.CLIENT_ID, 0);
                intent.putExtra(Constant.SERVER_ID, this.playbackdata.getVideoId());
                intent.putExtra(Constant.TITLE, this.playbackdata.getVideoTitle());
                intent.putExtra(Constant.DESC, this.playbackdata.getVideoDescription());
                intent.putExtra(Constant.USERID, this.playbackdata.getUid());
                ((Activity) this.context).finish();
                this.context.startActivity(intent);
            }
        } else {
            Toast.makeText(this.context, "Problem with server Try after sometime", Toast.LENGTH_LONG).show();
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
