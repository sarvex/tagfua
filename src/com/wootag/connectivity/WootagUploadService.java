/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.wootag.connectivity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;
import android.widget.TextView;

import org.json.JSONException;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.google.common.base.Strings;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wootag.Constant;
import com.wootag.PlayerActivity;
import com.wootag.R;
import com.wootag.ShareActivity;
import com.wootag.VideoPlayerApp;
import com.wootag.dto.Playback;
import com.wootag.dto.TagInfo;
import com.wootag.dto.TagResponse;
import com.wootag.dto.VideoDetails;
import com.wootag.dto.VideoInfo;
import com.wootag.model.Backend;
import com.wootag.util.Config;
import com.wootag.util.Util;

public class WootagUploadService extends WakefulIntentService {

    private static final String TWITTER = "twitter";
    private static final String GPLUS = "gplus";
    private static final String FB = "fb";
    private static final String COMMA = ",";
    private static final String EMPTY = "";
    private static final Logger LOG = LoggerManager.getLogger();
    private static final String VIDEO = "video";

    public int time;

    private final Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(final Message msg) {

            final AlertDialog.Builder dialog = new AlertDialog.Builder(WootagUploadService.this.getApplicationContext());
            final Playback inf = (Playback) msg.obj;
            dialog.setMessage(inf.getVideoTitle() + " Uploaded successfully.");
            final TextView hollaMessage = new TextView(WootagUploadService.this.getApplicationContext());
            dialog.setView(hollaMessage);
            dialog.setCancelable(false);

            final VideoDetails video = new VideoDetails();
            video.setUserId(inf.getUid());
            video.setVideoID(inf.getVideoId());
            video.setVideothumbPath(inf.getThumbPath());
            video.setVideoURL(inf.getVideoUrl());
            video.setName(inf.getUserName());
            video.setPhotoPath(inf.getUserImagePath());
            video.setVideoTitle(inf.getVideoTitle());
            video.setShareUrl(inf.getShareUrl());
            video.setFbShareUrl(inf.getFbShareUrl());
            if (video.getVideoDesc() != null) {
                video.setVideoDesc(video.getVideoDesc());
            }
            if (video.getLatestTagexpression() != null) {
                video.setLatestTagexpression(video.getLatestTagexpression());
            }

            dialog.setPositiveButton(R.string.tag, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(final DialogInterface dialog, final int index) {

                    dialog.dismiss();
                    if (inf.getVideoId() != null) {
                        final Intent intent = new Intent(WootagUploadService.this.getApplicationContext(),
                                PlayerActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(VIDEO, video);
                        intent.putExtra(Constant.PATH, inf.getVideoUrl());
                        intent.putExtra(Constant.CLIENT_ID, 0);
                        intent.putExtra(Constant.SERVER_ID, inf.getVideoId());
                        intent.putExtra(Constant.TITLE, inf.getVideoTitle());
                        intent.putExtra(Constant.DESC, inf.getVideoDescription());
                        intent.putExtra(Constant.USERID, inf.getUid());
                        WootagUploadService.this.getApplicationContext().startActivity(intent);

                        // new PlaybackAsync(getApplicationContext(),inf.getVideoId()).execute();

                    }
                }
            });

            dialog.setNeutralButton(R.string.share, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(final DialogInterface dialog, final int which) {

                    dialog.dismiss();
                    final Intent intent = new Intent(WootagUploadService.this.getApplicationContext(),
                            ShareActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(VIDEO, video);
                    WootagUploadService.this.startActivity(intent);

                }
            });
            dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(final DialogInterface dialog, final int index) {

                    dialog.dismiss();
                }
            });
            final AlertDialog alert = dialog.create();
            alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            alert.show();

            return false;

        }
    });

    private final Handler msgHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(final Message msg) {

            final AlertDialog.Builder dialog = new AlertDialog.Builder(WootagUploadService.this.getApplicationContext());
            final String inf = (String) msg.obj;
            dialog.setMessage(inf);
            final TextView hollaMessage = new TextView(WootagUploadService.this.getApplicationContext());
            dialog.setView(hollaMessage);
            dialog.setCancelable(false);

            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(final DialogInterface dialog, final int index) {

                    dialog.dismiss();
                }
            });

            final AlertDialog alert = dialog.create();
            alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            alert.show();

            return true;

        }
    });

    protected Timer timer;
    protected Timer uploadTimer;
    protected VideoInfo video;

    private VideoDataBase videoDatabase;

    public WootagUploadService() {

        super(WootagUploadService.class.getSimpleName());
    }

    private String getAllSocialSiteIds(final String socialSite, final Playback playbackdata,
            final List<TagInfo> playBacktags) {

        String fbIds = EMPTY;
        if ((playbackdata != null) && (playBacktags != null) && !playBacktags.isEmpty()) {
            if (Constant.FACEBOOK.equalsIgnoreCase(socialSite)) {
                for (int i = 0; i < playBacktags.size(); i++) {
                    final TagInfo tag = playBacktags.get(i);
                    if (!Strings.isNullOrEmpty(tag.getFbId())) {
                        if (Strings.isNullOrEmpty(fbIds)) {
                            fbIds = tag.getFbId();
                        } else {
                            fbIds = fbIds + COMMA + tag.getFbId();
                        }

                    }
                }
            } else if (Constant.TWITTER.equalsIgnoreCase(socialSite)) {
                for (int i = 0; i < playBacktags.size(); i++) {
                    final TagInfo tag = playBacktags.get(i);
                    if (!Strings.isNullOrEmpty(tag.getTwId())) {
                        if (Strings.isNullOrEmpty(fbIds)) {
                            fbIds = tag.getTwId();
                        } else {
                            fbIds = fbIds + COMMA + tag.getTwId();
                        }
                    }
                }
            } else if (Constant.GOOGLE_PLUS.equalsIgnoreCase(socialSite)) {
                for (int i = 0; i < playBacktags.size(); i++) {
                    final TagInfo tag = playBacktags.get(i);
                    if (!Strings.isNullOrEmpty(tag.getgPlusId())) {
                        if (Strings.isNullOrEmpty(fbIds)) {
                            fbIds = tag.getgPlusId();
                        } else {
                            fbIds = fbIds + COMMA + tag.getgPlusId();
                        }
                    }
                }
            }
        }
        return fbIds;
    }

    private boolean handleUploads(final VideoInfo video) throws JSONException {

        LOG.v("BackGround Service is running");
        boolean allSucceeded = true;
        boolean succeeded = false;
        Config.setUploadedPercentage(0);
        LOG.d("Uploading media: noteId=" + video.getMediaId() + ", localMediaPath=" + video.getLocalMediaPath());
        // try {
        final VideoInfo currentVideo = video;
        VideoDataBase.getInstance(this.getApplicationContext()).updateVideoData(0, currentVideo.getLocalMediaPath(),
                video.getVideoClientId(), 2, 0);
        // VideoDataBase.getInstance(getApplicationContext()).updateVideoUploadPercentage(video.getVideoClientId(),0);
        Intent intent = new Intent(Constant.UPLOADED_PERCENTAGE);
        this.sendBroadcast(intent);

        if ((VideoDataBase.getInstance(this.getApplicationContext()).getPartsUpload(video.getVideoClientId(),
                this.getApplicationContext()) == 0)
                && (this.timer == null)) {
            this.time = 0;
            this.timer = new Timer();
            this.timer.scheduleAtFixedRate(new partsUploadTimerTask(), 0, 1000);
        }
        final int partNo = VideoDataBase.getInstance(this.getApplicationContext()).getPartNumber(
                video.getVideoClientId(), this.getApplicationContext());
        succeeded = Backend.uploadMultiPartVideo(this.getApplicationContext(), video, partNo);
        if (succeeded) {
            final boolean fileUploadStatus = Backend.fileUpload(this.getApplicationContext(), currentVideo);
            if (this.timer != null) {
                this.timer.cancel();
                this.timer = null;
            }
            if (fileUploadStatus) {
                final long clientVideo = Long.parseLong(Config.getCurrentUploadVideoId());
                VideoDataBase.getInstance(this.getApplicationContext()).updateVideoData(clientVideo,
                        currentVideo.getLocalMediaPath(), video.getVideoClientId(), 3, 0);
                VideoDataBase.getInstance(this.getApplicationContext()).updatehitCount(video.getVideoClientId(), 1);
                final Intent fileUploaded = new Intent(Constant.FILE_UPLOADED);
                this.sendBroadcast(fileUploaded);
                this.startUploadTimer();
                final Object playbackResponse = Backend.uploadVideo(this.getApplicationContext(), currentVideo, 1);

                if (playbackResponse instanceof Playback) {
                    if (this.uploadTimer != null) {
                        this.uploadTimer.cancel();
                        this.uploadTimer = null;
                    }
                    final Playback videoInf = (Playback) playbackResponse;
                    if (videoInf != null) {
                        final long videoId = Long.parseLong(videoInf.getVideoId());
                        currentVideo.setServerVideoId(String.valueOf(videoId));
                        this.uploadTags(videoInf, video);

                        final String url = videoInf.getVideoUrl();
                        VideoDataBase.getInstance(this.getApplicationContext()).updateVideoData(videoId, url,
                                video.getVideoClientId(), 1, 0);

                        final Intent videoUpload = new Intent(Constant.VIDEO_UPLOADED);
                        videoUpload.putExtra("public", videoInf.getPublicVideo());
                        this.sendBroadcast(videoUpload);

                        // showAlertTagAndShareDialog(currentVideo,videoInf);

                        this.postVideoUrlOnSocialSite(videoInf, video.getVideoClientId());
                    }
                } else {
                    if (this.uploadTimer != null) {
                        this.uploadTimer.cancel();
                        this.uploadTimer = null;
                    }
                    // VideoDataBase.getInstance(getApplicationContext()).updateUploadVideoState(video.getVideoClientId(),0);
                    VideoDataBase.getInstance(this.getApplicationContext()).updateVideoData(clientVideo,
                            currentVideo.getLocalMediaPath(), video.getVideoClientId(), 3, 1);
                    VideoDataBase.getInstance(this.getApplicationContext()).updateVideoUploadPercentage(
                            video.getVideoClientId(), 100, 0);
                    // intent=new Intent(VideoPlayerConstants.ACTION_FILE_UPLOADED_PERCENTAGE);

                    intent = new Intent(Constant.WAITING_FOR_PUBLISH);
                    intent.putExtra(Constant.ACTION_FILE_UPLOAD_PROGRESS, 100);
                    this.sendBroadcast(intent);

                    this.showAlertDialog("We are experiencing trouble to publish your video. Access pending videos from quick link to retry or Delete and Save a copy of your video");

                    LOG.i("file parts uploaded waiting for publish n percentage 100");
                }
            } else {
                if (this.timer != null) {
                    this.timer.cancel();
                    this.timer = null;
                }
                VideoDataBase.getInstance(this.getApplicationContext()).updatePartsUpload(video.getVideoClientId(), 0);
                // VideoDataBase.getInstance(getApplicationContext()).updatePartsUpload(video.getVideoClientId(),0);
                VideoDataBase.getInstance(VideoPlayerApp.getAppContext()).updateVideoUploadPercentage(
                        video.getVideoClientId(), 0, 0);
                VideoDataBase.getInstance(this.getApplicationContext()).updateVideoData(0,
                        currentVideo.getLocalMediaPath(), video.getVideoClientId(), 0, 1);
                // VideoDataBase.getInstance(getApplicationContext()).updateVideoUploadPercentage(video.getVideoClientId(),0);
                intent = new Intent(Constant.UPLOADED_PERCENTAGE);
                this.sendBroadcast(intent);
                LOG.i("percentange makes to zero");
            }
        } else {
            if (this.timer != null) {
                this.timer.cancel();
                this.timer = null;
            }

            // VideoDataBase.getInstance(getApplicationContext()).updatePartsUpload(video.getVideoClientId(),0);
            VideoDataBase.getInstance(this.getApplicationContext()).updateVideoData(0,
                    currentVideo.getLocalMediaPath(), video.getVideoClientId(), 0, 1);
            // VideoDataBase.getInstance(getApplicationContext()).updateVideoUploadPercentage(video.getVideoClientId(),0);
            intent = new Intent(Constant.UPLOADED_PERCENTAGE);
            this.sendBroadcast(intent);
            LOG.i("percentange makes to zero");
        }
        // } catch (final Exception e) {
        // succeeded = false;
        // LOG.i(UPLOAD, "exception while uploading multiparts" + e.toString());
        // }
        if (!succeeded) {
            allSucceeded = false;
        }

        if (!Util.isConnected(this.getApplicationContext())) {
            LOG.d("Not connected to network. Returning from handleUploads method.");

            allSucceeded = false;
        }

        return allSucceeded;
    }

    private boolean isAppInForeground() {

        boolean result = false;

        final ActivityManager mActivityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        final List<RunningAppProcessInfo> list = mActivityManager.getRunningAppProcesses();
        final Iterator<RunningAppProcessInfo> iterator = list.iterator();
        while (iterator.hasNext()) {
            final RunningAppProcessInfo info = iterator.next();
            if ((info.uid == this.getApplicationInfo().uid)
                    && (info.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND)) {
                result = true;
                break;
            }
        }
        return result;
    }

    private void postVideoUrlOnSocialSite(final Playback videoInf, final String clientVideoId) {

        if (Util.isAppForground(this.getApplicationContext())) {
            final String fbOwnuserId = VideoDataBase.getInstance(this.getApplicationContext()).getFacebookShareFlag(
                    clientVideoId, this.getApplicationContext());
            final String gplusOwnuserId = VideoDataBase.getInstance(this.getApplicationContext()).getGPlusShareFlag(
                    clientVideoId, this.getApplicationContext());
            final String twitterOwnuserId = VideoDataBase.getInstance(this.getApplicationContext())
                    .getTwitterShareFlag(clientVideoId, this.getApplicationContext());
            final VideoDetails shareVideo = new VideoDetails();
            shareVideo.setUserId(videoInf.getUid());
            shareVideo.setVideoID(videoInf.getVideoId());
            shareVideo.setVideothumbPath(videoInf.getThumbPath());
            shareVideo.setVideoURL(videoInf.getVideoUrl());
            shareVideo.setShareUrl(videoInf.getShareUrl());
            shareVideo.setFbShareUrl(videoInf.getFbShareUrl());
            shareVideo.setVideoTitle(videoInf.getVideoTitle());
            final Intent shareIntent = new Intent(WootagUploadService.this, ShareActivity.class);
            shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            shareIntent.putExtra(VIDEO, shareVideo);

            final List<TagInfo> playBacktags = VideoDataBase.getInstance(this.getApplicationContext())
                    .getAllTagsByVideoId(this.video.getVideoClientId(), this.getApplicationContext(), false);
            String fbIds = this.getAllSocialSiteIds(Constant.FACEBOOK, videoInf, playBacktags);
            String twIds = this.getAllSocialSiteIds(Constant.TWITTER, videoInf, playBacktags);
            String gplusIds = this.getAllSocialSiteIds(Constant.GOOGLE_PLUS, videoInf, playBacktags);

            if (!Strings.isNullOrEmpty(fbOwnuserId)) {
                fbIds = fbOwnuserId + COMMA + fbIds;
            }
            if (!Strings.isNullOrEmpty(gplusOwnuserId)) {
                gplusIds = gplusOwnuserId + COMMA + gplusIds;
            }
            if (!Strings.isNullOrEmpty(twitterOwnuserId)) {
                twIds = twitterOwnuserId + COMMA + twIds;
            }

            if (!fbIds.trim().equalsIgnoreCase(EMPTY) || !gplusIds.trim().equalsIgnoreCase(EMPTY)
                    || !twIds.trim().equalsIgnoreCase(EMPTY)) {
                if (!fbIds.trim().equalsIgnoreCase(EMPTY)) {
                    shareIntent.putExtra(FB, fbIds);
                }
                if (!gplusIds.trim().equalsIgnoreCase(EMPTY)) {
                    shareIntent.putExtra(GPLUS, gplusIds);
                }
                if (!twIds.trim().equalsIgnoreCase(EMPTY)) {
                    shareIntent.putExtra(TWITTER, twIds);
                }
                this.getApplication().startActivity(shareIntent);
            }
        }

    }

    private void showAlertTagAndShareDialog(final VideoInfo currentVideo, final Playback videoInf) {

        final List<String> activities = Util.getAllActivities();
        String current = EMPTY;
        final ActivityManager activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        final List<ActivityManager.RunningTaskInfo> taskInfo = activityManager.getRunningTasks(1);
        final ComponentName componentInfo = taskInfo.get(0).topActivity;
        for (int i = 0; i < activities.size(); i++) {
            current = activities.get(i).toString();
            if (current.equalsIgnoreCase(taskInfo.get(0).topActivity.getClassName())) {
                break;
            }
        }

        if (current.equalsIgnoreCase(taskInfo.get(0).topActivity.getClassName())) {
            final Message myMessage = new Message();
            final Bundle resBundle = new Bundle();
            resBundle.putString("status", "SUCCESS");
            resBundle.putSerializable(VIDEO, currentVideo);
            // myMessage.obj=resBundle;
            myMessage.obj = videoInf;
            this.handler.sendMessage(myMessage);
        }

    }

    private void startUploadTimer() {

        if ((VideoDataBase.getInstance(this.getApplicationContext()).getVideoUploadState(this.video.getVideoClientId(),
                this.getApplicationContext()) == 0)
                && (this.uploadTimer == null)) {
            this.time = 0;
            this.uploadTimer = new Timer();
            this.uploadTimer.scheduleAtFixedRate(new uploadTimerTask(), 0, 1000);
        }
    }

    private void uploadTags(final Playback videoInf, final VideoInfo video) throws JSONException {

        final long videoId = Long.parseLong(videoInf.getVideoId());
        final String url = videoInf.getVideoUrl();
        VideoDataBase.getInstance(this.getApplicationContext()).updateVideoData(videoId, url, video.getVideoClientId(),
                1, 0);
        final List<VideoInfo> inf = VideoDataBase.getInstance(this.getApplicationContext()).selectallVideoFromTable();
        for (int i = 0; i < inf.size(); i++) {
            final VideoInfo vid = inf.get(i);
        }
        final List<TagInfo> allTags = VideoDataBase.getInstance(this.getApplicationContext()).getAllTagsByVideoId(
                video.getVideoClientId(), this.getApplicationContext(), false);
        // List<TagInfo> tags=Utils.getInstance().getTagsToUpload(video.getVideoClientId(),
        // getApplicationContext());
        if ((allTags != null) && !allTags.isEmpty()) {
            for (int j = 0; j < allTags.size(); j++) {

                final TagInfo updateTag = allTags.get(j);
                updateTag.setServerVideoId(String.valueOf(videoId));
                VideoDataBase.getInstance(this.getApplicationContext()).updateTagWithVideoServerId(
                        String.valueOf(videoId), updateTag.getClientVideoId(), this.getApplicationContext());
            }
            final Object response = Backend.addTags(this.getApplicationContext(), allTags);
            if (response instanceof List<?>) {
                final List<TagResponse> uploadedTags = (ArrayList<TagResponse>) response;
                if ((uploadedTags != null) && !uploadedTags.isEmpty()) {
                    for (int i = 0; i < uploadedTags.size(); i++) {
                        final TagResponse tag = uploadedTags.get(i);
                        VideoDataBase.getInstance(this.getApplicationContext()).updateTagWithServerIdAndVideoServerId(
                                videoId, tag.getServerTagId(), tag.getClientTagId(), 1, this.getApplicationContext());
                    }
                }
            }

        }
    }

    @Override
    protected void doWakefulWork(final Intent arg0) {

        while (true) {
            synchronized (this) {
                LOG.d("In doWakefulWork.");
                if (Util.getNetworkType(this.getApplicationContext()) != -1) {// &&
                    /**
                     * upload status 0 in pending queue upload status 1 uploaded upload status 2 current parts uploading
                     * upload status 3 parts uploaded successfully.required final upload request
                     */

                    this.videoDatabase = VideoDataBase.getInstance(this.getApplicationContext());
                    this.video = this.videoDatabase.selectFirstRowFromTable(3);
                    if (this.video == null) {
                        this.video = this.videoDatabase.selectFirstRowFromTable(2);
                        if (this.video == null) {
                            this.video = this.videoDatabase.selectFirstRowFromTable(0);
                        }
                        if (this.video != null) {
                            try {
                                this.handleUploads(this.video);
                            } catch (final JSONException exception) {
                                LOG.e(exception);
                            }
                        }
                    } else {
                        Config.setCurrentUploadVideoID(this.video.getServerVideoId());
                        final long clientVideo = Long.parseLong(Config.getCurrentUploadVideoId());
                        int hitCount = VideoDataBase.getInstance(this.getApplicationContext()).getUploadHitCount(
                                String.valueOf(clientVideo), this.getApplicationContext());
                        hitCount = hitCount + 1;
                        VideoDataBase.getInstance(this.getApplicationContext()).updatehitCount(
                                this.video.getVideoClientId(), hitCount);
                        this.startUploadTimer();
                        Object playbackResponse;
                        try {
                            playbackResponse = Backend.uploadVideo(this.getApplicationContext(), this.video, hitCount);
                            if (playbackResponse instanceof Playback) {
                                if (this.uploadTimer != null) {
                                    this.uploadTimer.cancel();
                                    this.uploadTimer = null;
                                }
                                final Playback videoInf = (Playback) playbackResponse;
                                if (videoInf != null) {
                                    final long videoId = Long.parseLong(videoInf.getVideoId());
                                    this.video.setServerVideoId(String.valueOf(videoId));
                                    this.uploadTags(videoInf, this.video);
                                    final long serverVideoId = Long.parseLong(videoInf.getVideoId());
                                    final String url = videoInf.getVideoUrl();
                                    VideoDataBase.getInstance(this.getApplicationContext()).updateVideoData(
                                            serverVideoId, url, this.video.getVideoClientId(), 1, 0);
                                    final Intent videoUploaded = new Intent(Constant.VIDEO_UPLOADED);
                                    videoUploaded.putExtra("public", videoInf.getPublicVideo());
                                    this.sendBroadcast(videoUploaded);
                                    // showAlertTagAndShareDialog(video,videoInf);
                                    this.postVideoUrlOnSocialSite(videoInf, this.video.getVideoClientId());

                                }
                            } else {
                                if (this.uploadTimer != null) {
                                    this.uploadTimer.cancel();
                                    this.uploadTimer = null;
                                }
                                VideoDataBase.getInstance(this.getApplicationContext()).updateVideoData(clientVideo,
                                        this.video.getLocalMediaPath(), this.video.getVideoClientId(), 3, 1);
                                VideoDataBase.getInstance(this.getApplicationContext()).updateVideoUploadPercentage(
                                        this.video.getVideoClientId(), 100, 0);
                                // Intent intent=new
                                // Intent(VideoPlayerConstants.ACTION_FILE_UPLOADED_PERCENTAGE);
                                final Intent intent = new Intent(Constant.WAITING_FOR_PUBLISH);
                                intent.putExtra(Constant.ACTION_FILE_UPLOAD_PROGRESS, 100);
                                this.sendBroadcast(intent);

                                this.showAlertDialog("We are experiencing trouble to publish your video. Access pending videos from quick link to retry or Delete and Save a copy of your video");
                            }
                        } catch (final JSONException exception) {
                            LOG.e(exception);
                        }

                    }
                }

                try {
                    Thread.sleep(1000);
                } catch (final InterruptedException exception) {
                    LOG.e(exception);
                }
            }
        }

    }

    void showAlertDialog(final String message) {

        if (this.isAppInForeground()) {
            final Message myMessage = new Message();
            final Bundle resBundle = new Bundle();
            resBundle.putString("status", "SUCCESS");
            myMessage.obj = message;
            this.msgHandler.sendMessage(myMessage);
        }
    }

    private class partsUploadTimerTask extends TimerTask {

        @Override
        public void run() {

            WootagUploadService.this.time = WootagUploadService.this.time + 1;
            // System.out.println("timer val :"+time);
            if (WootagUploadService.this.time >= 120) {
                if ((WootagUploadService.this.video != null)
                        && (WootagUploadService.this.video.getVideoClientId() != null)) {
                    VideoDataBase.getInstance(WootagUploadService.this.getApplicationContext()).updatePartsUpload(
                            WootagUploadService.this.video.getVideoClientId(), 1);

                    final Intent intent = new Intent(Constant.HIDE_PROGRESS);
                    WootagUploadService.this.sendBroadcast(intent);
                    WootagUploadService.this
                            .showAlertDialog(" We are having trouble with your internet access to upload, Don�t worry your video is safe in pending videos. Will upload automatically when u have internet access.");
                }
                WootagUploadService.this.timer.cancel();
                WootagUploadService.this.timer = null;
            }
        }
    }

    private class uploadTimerTask extends TimerTask {

        @Override
        public void run() {

            WootagUploadService.this.time = WootagUploadService.this.time + 1;
            // System.out.println(" upload timer val :"+time);
            if (WootagUploadService.this.time >= 60) {
                if ((WootagUploadService.this.video != null)
                        && (WootagUploadService.this.video.getVideoClientId() != null)) {
                    VideoDataBase.getInstance(WootagUploadService.this.getApplicationContext()).updateUploadVideoState(
                            WootagUploadService.this.video.getVideoClientId(), 1);

                    final Intent intent = new Intent(Constant.HIDE_PROGRESS);
                    WootagUploadService.this.sendBroadcast(intent);
                    WootagUploadService.this
                            .showAlertDialog("Your video is successfully uploaded, we have some trouble to publish the video. We are ON it and will notify soon.");
                }
                WootagUploadService.this.uploadTimer.cancel();
                WootagUploadService.this.uploadTimer = null;
            }
        }
    }

}
