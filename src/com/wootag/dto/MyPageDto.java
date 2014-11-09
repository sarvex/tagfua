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
import com.wootag.util.Util;

public class MyPageDto implements Serializable {

    private static final long serialVersionUID = 8160971145917684080L;
    private static final Logger LOG = LoggerManager.getLogger();

    private List<Comment> recentComments;
    private List<RecentLikes> recentLikedBy;
    private String facebookShareUrl;
    private String latestTagExpression;
    private String numberOfComments;
    private String numberOfLikes;
    private String numberOfTags;
    private String numberOfViews;
    private int publicVideo;
    private String shareUrl;
    private String uploadDate;
    private String userId;
    private String userName;
    private String userPickUrl;
    private String videoDescription;
    private String videoDuration;
    private String videoId;
    private String videoThumbPath;
    private String videoTitle;
    private String videoUrl;
    private boolean commented;
    private boolean liked;

    public MyPageDto() {

        this.recentComments = new ArrayList<Comment>();
        this.recentLikedBy = new ArrayList<RecentLikes>();

    }

    public String getFbShareUrl() {

        return this.facebookShareUrl;
    }

    public String getLatestTagExpression() {

        return this.latestTagExpression;
    }

    public String getNumberOfComments() {

        return this.numberOfComments;
    }

    public String getNumberOfLikes() {

        return this.numberOfLikes;
    }

    public String getNumberOfTags() {

        return this.numberOfTags;
    }

    public String getNumberOfViews() {

        return this.numberOfViews;
    }

    public int getPublicVideo() {

        return this.publicVideo;
    }

    public List<Comment> getRecentComments() {

        return this.recentComments;
    }

    public List<RecentLikes> getRecentLikedBy() {

        return this.recentLikedBy;
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

    public String getVideoDescription() {

        return this.videoDescription;
    }

    public String getVideoDuration() {

        return this.videoDuration;
    }

    public String getVideoId() {

        return this.videoId;
    }

    public String getVideoThumbPath() {

        return this.videoThumbPath;
    }

    public String getVideoTitle() {

        return this.videoTitle;
    }

    public String getVideoUrl() {

        return this.videoUrl;
    }

    public boolean hasCommented() {

        return this.commented;
    }

    public boolean hasLiked() {

        return this.liked;
    }

    public void load(final JSONObject response) throws JSONException {

        this.videoId = Stream.getString(response, Constant.VIDEO_ID);
        this.videoUrl = Stream.getString(response, Constant.VIDEO_URL);
        this.videoThumbPath = Stream.getString(response, Constant.VIDEO_THUMB_PATH);
        this.videoTitle = Stream.getString(response, Constant.TITLE);
        this.uploadDate = Stream.getString(response, Constant.UPLOAD_DATE);
        this.latestTagExpression = Stream.getString(response, Constant.LATEST_TAG_EXPRESSION);
        this.videoDuration = Stream.getString(response, Constant.DURATION);
        this.videoDescription = Stream.getString(response, Constant.DESCRIPTION);
        this.numberOfViews = Stream.getString(response, Constant.NUMBER_OF_VIEWS);
        this.numberOfTags = Stream.getString(response, Constant.NUMBER_OF_TAGS);
        this.numberOfLikes = Stream.getString(response, Constant.NUMBER_OF_LIKES);
        this.numberOfComments = Stream.getString(response, Constant.NUMBER_OF_COMMENTS);
        this.publicVideo = Integer.parseInt(Stream.getString(response, Constant.PUBLIC));
        this.userId = Stream.getString(response, Constant.USER_ID);
        this.shareUrl = Stream.getString(response, Constant.SHARE_URL);
        this.facebookShareUrl = Stream.getString(response, Constant.FACEBOOK_SHARE_URL);
        this.userName = Stream.getString(response, Constant.USER_NAME);
        this.userPickUrl = Stream.getString(response, Constant.USER_PIC_URL);
        this.liked = Constant.YES.equalsIgnoreCase(Stream.getString(response, Constant.HAS_LIKED));
        this.commented = Constant.YES.equalsIgnoreCase(Stream.getString(response, Constant.HAS_COMMENTED));

        if (this.userPickUrl == null) {
            this.userPickUrl = Stream.getString(response, Constant.USER_PHOTO);
        }
        if (this.uploadDate != null) {
            this.uploadDate = Util.getLocalTime(this.uploadDate);
        }

    }

    public void setCommented(final boolean hasCommentd) {

        this.commented = hasCommentd;
    }

    public void setFacebookShareUrl(final String facebookShareUrl) {

        this.facebookShareUrl = facebookShareUrl;
    }

    public void setLatestTagExpression(final String latestTagExpression) {

        this.latestTagExpression = latestTagExpression;
    }

    public void setLiked(final boolean liked) {

        this.liked = liked;
    }

    public void setNumberOfComments(final String numberOfComments) {

        this.numberOfComments = numberOfComments;
    }

    public void setNumberOfLikes(final String numberOfLikes) {

        this.numberOfLikes = numberOfLikes;
    }

    public void setNumberOfTags(final String numberOfTags) {

        this.numberOfTags = numberOfTags;
    }

    public void setNumberOfViews(final String numberOfViews) {

        this.numberOfViews = numberOfViews;
    }

    public void setPublicVideo(final int publicVideo) {

        this.publicVideo = publicVideo;
    }

    public void setRecentComments(final List<Comment> recentComments) {

        this.recentComments = recentComments;
    }

    public void setRecentLikedBy(final List<RecentLikes> recentLikedBy) {

        this.recentLikedBy = recentLikedBy;
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

    public void setVideoDescription(final String videoDescription) {

        this.videoDescription = videoDescription;
    }

    public void setVideoDuration(final String videoDuration) {

        this.videoDuration = videoDuration;
    }

    public void setVideoId(final String videoId) {

        this.videoId = videoId;
    }

    public void setVideoThumbPath(final String videoThumbPath) {

        this.videoThumbPath = videoThumbPath;
    }

    public void setVideoTitle(final String videoTitle) {

        this.videoTitle = videoTitle;
    }

    public void setVideoUrl(final String videoUrl) {

        this.videoUrl = videoUrl;
    }
}
