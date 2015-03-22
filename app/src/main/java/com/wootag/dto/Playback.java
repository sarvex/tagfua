/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.TagFu.dto;

import java.io.Serializable;
import java.util.List;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

public class Playback implements Serializable {

    private static final Logger LOG = LoggerManager.getLogger();
    private static final long serialVersionUID = -2907607286462853747L;

    private List<TagInfo> tags;
    private String fbShareUrl;
    private String publicVideo;
    private String shareUrl;
    private String thumbPath;
    private String uid;
    private String userImagePath;
    private String userName;
    private String videoDescription;
    private String videoId;
    private String videoTitle;
    private String videoUrl;

    public String getFbShareUrl() {

        return this.fbShareUrl;
    }

    public String getPublicVideo() {

        return this.publicVideo;
    }

    public String getShareUrl() {

        return this.shareUrl;
    }

    public List<TagInfo> getTags() {

        return this.tags;
    }

    public String getThumbPath() {

        return this.thumbPath;
    }

    public String getUid() {

        return this.uid;
    }

    public String getUserImagePath() {

        return this.userImagePath;
    }

    public String getUserName() {

        return this.userName;
    }

    public String getVideoDescription() {

        return this.videoDescription;
    }

    public String getVideoId() {

        return this.videoId;
    }

    public String getVideoTitle() {

        return this.videoTitle;
    }

    public String getVideoUrl() {

        return this.videoUrl;
    }

    public void setFbShareUrl(final String fbShareUrl) {

        this.fbShareUrl = fbShareUrl;
    }

    public void setPublicVideo(final String publicVideo) {

        this.publicVideo = publicVideo;
    }

    public void setShareUrl(final String shareUrl) {

        this.shareUrl = shareUrl;
    }

    public void setTags(final List<TagInfo> tags) {

        this.tags = tags;
    }

    public void setThumbPath(final String thumbPath) {

        this.thumbPath = thumbPath;
    }

    public void setUid(final String uid) {

        this.uid = uid;
    }

    public void setUserImagePath(final String userImagePath) {

        this.userImagePath = userImagePath;
    }

    public void setUserName(final String userName) {

        this.userName = userName;
    }

    public void setVideoDescription(final String videoDescription) {

        this.videoDescription = videoDescription;
    }

    public void setVideoId(final String videoId) {

        this.videoId = videoId;
    }

    public void setVideoTitle(final String videoTitle) {

        this.videoTitle = videoTitle;
    }

    public void setVideoUrl(final String videoUrl) {

        this.videoUrl = videoUrl;
    }

}
