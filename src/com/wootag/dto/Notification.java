/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.dto;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Strings;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wootag.Constant;
import com.wootag.util.Stream;

public class Notification implements Serializable {

    private static final long serialVersionUID = -8271279442689412343L;

    private static final Logger LOG = LoggerManager.getLogger();

    private String commentDescription;
    private String commentId;
    private String createdTime;
    private String message;
    private String noticeId;
    private String senderId;
    private String senderName;
    private String userPickUrl;
    private String videoId;
    private String videoThumbPath;
    private int type;
    private int viewId;

    public String getCommentDescription() {

        return this.commentDescription;
    }

    public String getCommentId() {

        return this.commentId;
    }

    public String getCreatedTime() {

        return this.createdTime;
    }

    public String getMessage() {

        return this.message;
    }

    public String getNoticeId() {

        return this.noticeId;
    }

    public String getSenderId() {

        return this.senderId;
    }

    public String getSenderName() {

        return this.senderName;
    }

    public int getType() {

        return this.type;
    }

    public String getUserPickUrl() {

        return this.userPickUrl;
    }

    public String getVideoId() {

        return this.videoId;
    }

    public String getVideoThumbPath() {

        return this.videoThumbPath;
    }

    public int getViewId() {

        return this.viewId;
    }

    public void load(final JSONObject response) throws JSONException {

        this.senderId = Stream.getString(response, Constant.SENDER_ID);
        this.senderName = Stream.getString(response, Constant.SENDER_NAME);
        this.message = Stream.getString(response, Constant.MESSAGE);
        this.createdTime = Stream.getString(response, Constant.CREATED_DATE);
        this.videoThumbPath = Stream.getString(response, Constant.VIDEO_THUMB_PATH);
        this.userPickUrl = Stream.getString(response, Constant.USER_PICK);
        this.commentDescription = Stream.getString(response, Constant.COMMENT_TEXT);
        this.noticeId = Stream.getString(response, Constant.NOTICE_ID);
        this.videoId = Stream.getString(response, Constant.VIDEO_ID);
        this.commentId = Stream.getString(response, Constant.COMMENT_ID);

        if (this.createdTime != null) {
            this.createdTime = com.wootag.util.Util.getLocalTime(this.createdTime);
        }
        if (this.commentDescription != null) {
            this.commentDescription = com.wootag.util.Util.decodeBase64(this.commentDescription);
        }

        final String notificationType = Stream.getString(response, Constant.TYPE);
        if (Strings.isNullOrEmpty(notificationType)) {
            this.type = Integer.parseInt(notificationType);
        }

    }

    public void setCommentDescription(final String commentDescription) {

        this.commentDescription = commentDescription;
    }

    public void setCommentId(final String commentId) {

        this.commentId = commentId;
    }

    public void setCreatedTime(final String createdTime) {

        this.createdTime = createdTime;
    }

    public void setMessage(final String message) {

        this.message = message;
    }

    public void setNoticeId(final String noticeId) {

        this.noticeId = noticeId;
    }

    public void setSenderId(final String senderId) {

        this.senderId = senderId;
    }

    public void setSenderName(final String senderName) {

        this.senderName = senderName;
    }

    public void setType(final int type) {

        this.type = type;
    }

    public void setUserPickUrl(final String userPickUrl) {

        this.userPickUrl = userPickUrl;
    }

    public void setVideoId(final String videoId) {

        this.videoId = videoId;
    }

    public void setVideoThumbPath(final String videoThumbPath) {

        this.videoThumbPath = videoThumbPath;
    }

    public void setViewId(final int viewId) {

        this.viewId = viewId;
    }

}
