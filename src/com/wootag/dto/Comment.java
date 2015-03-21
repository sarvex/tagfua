/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wTagFudto;

import java.io.Serializable;

import android.text.SpannableString;

import org.json.JSONException;
import org.json.JSONObject;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.woTagFuonstant;
import com.wooTagFuil.Stream;
import com.wootag.util.Util;

public class Comment implements Serializable {

    private static final Logger LOG = LoggerManager.getLogger();

    private static final long serialVersionUID = -2864653488029487959L;

    private SpannableString spannableComment;
    private String comment;
    private String commentId;
    private String userId;
    private String userName;
    private String userPicUrl;
    private boolean editMode;

    public String getComment() {

        return this.comment;
    }

    public String getCommentId() {

        return this.commentId;
    }

    public SpannableString getSpannableComment() {

        return this.spannableComment;
    }

    public String getUserId() {

        return this.userId;
    }

    public String getUserName() {

        return this.userName;
    }

    public String getUserPicUrl() {

        return this.userPicUrl;
    }

    public boolean isEditMode() {

        return this.editMode;
    }

    public void load(final JSONObject response) throws JSONException {

        this.userName = Stream.getString(response, Constant.USER_NAME);
        this.userId = Stream.getString(response, Constant.USER_ID);
        this.userPicUrl = Stream.getString(response, Constant.USER_PHOTO_PATH);
        this.comment = Stream.getString(response, Constant.COMMENT_TEXT);
        this.commentId = Stream.getString(response, Constant.COMMENT_ID);

        if (this.comment != null) {
            this.comment = Util.decodeBase64(this.comment);
        }
    }

    public void setComment(final String comment) {

        this.comment = comment;
    }

    public void setCommentId(final String commentId) {

        this.commentId = commentId;
    }

    public void setEditMode(final boolean editMode) {

        this.editMode = editMode;
    }

    public void setSpannableComment(final SpannableString spannableComment) {

        this.spannableComment = spannableComment;
    }

    public void setUserId(final String userId) {

        this.userId = userId;
    }

    public void setUserName(final String userName) {

        this.userName = userName;
    }

    public void setUserPicUrl(final String userPicUrl) {

        this.userPicUrl = userPicUrl;
    }

}
