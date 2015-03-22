/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.TagFu.dto;

import org.json.JSONException;
import org.json.JSONObject;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.TagFu.Constant;
import com.TagFu.util.Stream;

public class Liked {

    private static final Logger LOG = LoggerManager.getLogger();

    private String following;
    private String userId;
    private String userName;
    private String userPhoto;
    private boolean privateAccepted;

    public String getFollowing() {

        return this.following;
    }

    public String getUserId() {

        return this.userId;
    }

    public String getUserName() {

        return this.userName;
    }

    public String getUserPhoto() {

        return this.userPhoto;
    }

    public boolean isPrivateAccepted() {

        return this.privateAccepted;
    }

    public void load(final JSONObject response) throws JSONException {

        this.userId = Stream.getString(response, Constant.USER_ID);
        this.userName = Stream.getString(response, Constant.USER_NAME);
        this.userPhoto = Stream.getString(response, Constant.USER_PHOTO);
        if (this.userPhoto == null) {
            this.userPhoto = Stream.getString(response, Constant.PHOTO_PATH);
        }
        this.following = Stream.getString(response, Constant.FOLLOWING);
    }

    public void setFollowing(final String following) {

        this.following = following;
    }

    public void setPvtAccepted(final boolean privateAccepted) {

        this.privateAccepted = privateAccepted;
    }

    public void setUserId(final String userId) {

        this.userId = userId;
    }

    public void setUserName(final String userName) {

        this.userName = userName;
    }

    public void setUserPhoto(final String userPhoto) {

        this.userPhoto = userPhoto;
    }

}
