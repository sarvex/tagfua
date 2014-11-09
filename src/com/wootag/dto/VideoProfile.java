/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wootag.Constant;
import com.wootag.util.Stream;

public class VideoProfile implements Serializable {

    private static final Logger LOG = LoggerManager.getLogger();

    private static final long serialVersionUID = 1L;

    private List<String> comments;
    private String latestTag;
    private String publicVideo;
    private String shareUrl;
    private String uploadDate;
    private String userId;
    private String userName;
    private String userPickUrl;
    private String videoBannerURL;
    private String videoComments;
    private String videoDesc;
    private String videoID;
    private String videoLength;
    private String videoLikes;
    private String videoTags;
    private String videoTitle;
    private String videoURL;
    private String videoViews;

    public VideoProfile() {

        this.comments = new ArrayList<String>();

    }

    public List<String> getComments() {

        return this.comments;
    }

    public String getLatestTag() {

        return this.latestTag;
    }

    public String getPublicVideo() {

        return this.publicVideo;
    }

    public String getShareUrl() {

        return this.shareUrl;
    }

    public String getUploadDate() {

        return this.uploadDate;
    }

    public String getUserId() {

        return this.userId;
    }

    public String getUserName() {

        return this.userName;
    }

    public String getUserPickUrl() {

        return this.userPickUrl;
    }

    public String getVideoBannerURL() {

        return this.videoBannerURL;
    }

    public String getVideoComments() {

        return this.videoComments;
    }

    public String getVideoDesc() {

        return this.videoDesc;
    }

    public String getVideoID() {

        return this.videoID;
    }

    public String getVideoLength() {

        return this.videoLength;
    }

    public String getVideoLikes() {

        return this.videoLikes;
    }

    public String getVideoTags() {

        return this.videoTags;
    }

    public String getVideoTitle() {

        return this.videoTitle;
    }

    public String getVideoURL() {

        return this.videoURL;
    }

    public String getVideoViews() {

        return this.videoViews;
    }

    public void load(final JSONObject response) throws JSONException {

        this.videoID = Stream.getString(response, Constant.VIDEO_ID);
        this.videoURL = Stream.getString(response, Constant.VIDEO_URL);
        this.videoBannerURL = Stream.getString(response, Constant.VIDEO_THUMB_PATH);
        this.videoTitle = Stream.getString(response, Constant.TITLE);
        this.uploadDate = Stream.getString(response, Constant.UPLOAD_DATE);
        this.videoLength = Stream.getString(response, Constant.DURATION);
        this.videoDesc = Stream.getString(response, Constant.DESCRIPTION);
        this.videoViews = Stream.getString(response, Constant.NUMBER_OF_VIEWS);
        this.videoTags = Stream.getString(response, Constant.NUMBER_OF_TAGS);
        this.videoLikes = Stream.getString(response, Constant.NUMBER_OF_LIKES);
        this.videoComments = Stream.getString(response, Constant.NUMBER_OF_COMMENTS);
        this.userId = Stream.getString(response, Constant.NUMBER_OF_COMMENTS);
        this.userName = Stream.getString(response, Constant.USER_NAME);
        this.publicVideo = Stream.getString(response, Constant.PUBLIC);
        this.userId = Stream.getString(response, Constant.USER_ID);
        this.userPickUrl = Stream.getString(response, Constant.USER_PIC_URL);
        this.shareUrl = Stream.getString(response, Constant.SHARE_URL);

        if (this.userPickUrl == null) {
            this.userPickUrl = Stream.getString(response, Constant.USER_PHOTO);
        }
        this.latestTag = Stream.getString(response, Constant.LATEST_TAG_EXPRESSION);
    }

    public void setComments(final List<String> comments) {

        this.comments = comments;
    }

    public void setComments(final String comment) {

        if (this.comments != null) {
            this.comments.add(comment);
        }
    }

    public void setLatestTag(final String latestTag) {

        this.latestTag = latestTag;
    }

    public void setPublicVideo(final String publicVideo) {

        this.publicVideo = publicVideo;
    }

    public void setShareUrl(final String shareUrl) {

        this.shareUrl = shareUrl;
    }

    public void setUploadDate(final String uploadDate) {

        this.uploadDate = uploadDate;
    }

    public void setUserId(final String userId) {

        this.userId = userId;
    }

    public void setUserName(final String userName) {

        this.userName = userName;
    }

    public void setUserPickUrl(final String userPickUrl) {

        this.userPickUrl = userPickUrl;
    }

    public void setVideoBannerURL(final String videoBannerURL) {

        this.videoBannerURL = videoBannerURL;
    }

    public void setVideoComments(final String videoComments) {

        this.videoComments = videoComments;
    }

    public void setVideoDesc(final String videoDesc) {

        this.videoDesc = videoDesc;
    }

    public void setVideoID(final String videoID) {

        this.videoID = videoID;
    }

    public void setVideoLength(final String videoLength) {

        this.videoLength = videoLength;
    }

    public void setVideoLikes(final String videoLikes) {

        this.videoLikes = videoLikes;
    }

    public void setVideoTags(final String videoTags) {

        this.videoTags = videoTags;
    }

    public void setVideoTitle(final String videoTitle) {

        this.videoTitle = videoTitle;
    }

    public void setVideoURL(final String videoURL) {

        this.videoURL = videoURL;
    }

    public void setVideoViews(final String videoViews) {

        this.videoViews = videoViews;
    }

}
