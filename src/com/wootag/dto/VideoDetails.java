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

public class VideoDetails implements Serializable {

    private static final Logger LOG = LoggerManager.getLogger();
    private static final long serialVersionUID = 3022276265889221404L;

    private List<Comment> comments;
    private List<Comment> likes;
    private List<MoreVideos> otherStuff;
    private String clientvideoID;
    private String country;
    private String fbShareUrl;
    private String latestTagexpression;
    private String name;
    private String numberOfComments;
    private String numberOfLikes;
    private String numberOfTags;
    private String numberOfViews;
    private String photoPath;
    private String profession;
    private int publicVideo;
    private String shareUrl;
    private String uploadDate;
    private String userId;
    private String videoCoverPageTime;
    private String videoDesc;
    private String videoDuration;
    private String videoId;
    private String videoTitle;
    private String videoURL;
    private String videothumbPath;
    private String website;

    public VideoDetails() {

        this.otherStuff = new ArrayList<MoreVideos>();
    }

    public String getClientvideoID() {

        return this.clientvideoID;
    }

    public List<Comment> getComments() {

        return this.comments;
    }

    public String getCountry() {

        return this.country;
    }

    public String getFbShareUrl() {

        return this.fbShareUrl;
    }

    public String getLatestTagexpression() {

        return this.latestTagexpression;
    }

    public List<Comment> getLikes() {

        return this.likes;
    }

    public String getName() {

        return this.name;
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

    public List<MoreVideos> getOtherStuff() {

        return this.otherStuff;
    }

    public String getPhotoPath() {

        return this.photoPath;
    }

    public String getProfession() {

        return this.profession;
    }

    public int getPublicVideo() {

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

    public String getVideoCoverPageTime() {

        return this.videoCoverPageTime;
    }

    public String getVideoDesc() {

        return this.videoDesc;
    }

    public String getVideoDuration() {

        return this.videoDuration;
    }

    public String getVideoID() {

        return this.videoId;
    }

    public String getVideothumbPath() {

        return this.videothumbPath;
    }

    public String getVideoTitle() {

        return this.videoTitle;
    }

    public String getVideoURL() {

        return this.videoURL;
    }

    public String getWebsite() {

        return this.website;
    }

    public void load(final JSONObject response) throws JSONException {

        this.videoId = Stream.getString(response, Constant.VIDEO_ID);
        this.videoURL = Stream.getString(response, Constant.VIDEO_URL);
        this.videothumbPath = Stream.getString(response, Constant.VIDEO_THUMB_PATH);
        this.videoTitle = Stream.getString(response, Constant.TITLE);
        this.uploadDate = Stream.getString(response, Constant.UPLOAD_DATE);
        if (this.uploadDate != null) {
            this.uploadDate = com.wootag.util.Util.getLocalTime(this.uploadDate);
        }
        this.videoDuration = Stream.getString(response, Constant.DURATION);
        this.videoDesc = Stream.getString(response, Constant.DESCRIPTION);
        this.numberOfViews = Stream.getString(response, Constant.NUMBER_OF_VIEWS);
        this.numberOfTags = Stream.getString(response, Constant.NUMBER_OF_TAGS);
        this.numberOfLikes = Stream.getString(response, Constant.NUMBER_OF_LIKES);
        this.numberOfComments = Stream.getString(response, Constant.NUMBER_OF_COMMENTS);
        this.publicVideo = Integer.parseInt(Stream.getString(response, Constant.PUBLIC));
        this.userId = Stream.getString(response, Constant.USER_ID);
        this.name = Stream.getString(response, Constant.USER_NAME);
        this.shareUrl = Stream.getString(response, Constant.SHARE_URL);
        this.fbShareUrl = Stream.getString(response, Constant.FACEBOOK_SHARE_URL);
        this.photoPath = Stream.getString(response, Constant.PHOTO_PATH);
        if (this.photoPath == null) {
            this.photoPath = Stream.getString(response, Constant.USER_PHOTO);
        }
        this.country = Stream.getString(response, Constant.COUNTRY);
        this.profession = Stream.getString(response, Constant.PROFESSION);
        this.website = Stream.getString(response, Constant.WEBSITE);
        this.latestTagexpression = Stream.getString(response, Constant.LATEST_TAG_EXPRESSION);
    }

    public void setClientvideoID(final String clientvideoID) {

        this.clientvideoID = clientvideoID;
    }

    public void setComments(final List<Comment> comments) {

        this.comments = comments;
    }

    public void setCountry(final String country) {

        this.country = country;
    }

    public void setFbShareUrl(final String fbShareUrl) {

        this.fbShareUrl = fbShareUrl;
    }

    public void setLatestTagexpression(final String latestTagexpression) {

        this.latestTagexpression = latestTagexpression;
    }

    public void setLikes(final List<Comment> likes) {

        this.likes = likes;
    }

    public void setName(final String name) {

        this.name = name;
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

    public void setNumberOfView(final String numberOfViews) {

        this.numberOfViews = numberOfViews;
    }

    public void setOtherStuff(final List<MoreVideos> otherStuff) {

        this.otherStuff = otherStuff;
    }

    public void setPhotoPath(final String photoPath) {

        this.photoPath = photoPath;
    }

    public void setProfession(final String profession) {

        this.profession = profession;
    }

    public void setPublicVideo(final int publicVideo) {

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

    public void setVideoCoverPageTime(final String videoCoverPageTime) {

        this.videoCoverPageTime = videoCoverPageTime;
    }

    public void setVideoDesc(final String videoDesc) {

        this.videoDesc = videoDesc;
    }

    public void setVideoDuration(final String videoDuration) {

        this.videoDuration = videoDuration;
    }

    public void setVideoID(final String videoID) {

        this.videoId = videoID;
    }

    public void setVideothumbPath(final String videothumbPath) {

        this.videothumbPath = videothumbPath;
    }

    public void setVideoTitle(final String videoTitle) {

        this.videoTitle = videoTitle;
    }

    public void setVideoURL(final String videoURL) {

        this.videoURL = videoURL;
    }

    public void setWebsite(final String website) {

        this.website = website;
    }

}
