/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.TagFu.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.TagFu.Constant;
import com.TagFu.connectivity.Parser;
import com.TagFu.util.Stream;

public class UserProfileDto implements Serializable {

    private static final Logger LOG = LoggerManager.getLogger();

    private static final long serialVersionUID = 1L;

    private String userBannerPath;
    private String userFollowers;
    private String userFollowing;
    private String userId;
    private String userLastUpdate;
    private String userLikes;
    private String userPhotoPath;
    private String userTags;
    private String userVideos;
    private String username;

    private List<VideoProfile> videos;

    public UserProfileDto() {

        this.videos = new ArrayList<VideoProfile>();
    }

    public String getUserBannerPath() {

        return this.userBannerPath;
    }

    public String getUserFollowers() {

        return this.userFollowers;
    }

    public String getUserFollowing() {

        return this.userFollowing;
    }

    public String getUserid() {

        return this.userId;
    }

    public String getUserLastUpdate() {

        return this.userLastUpdate;
    }

    public String getUserLikes() {

        return this.userLikes;
    }

    public String getUsername() {

        return this.username;
    }

    public String getUserPhotoPath() {

        return this.userPhotoPath;
    }

    public String getUserTags() {

        return this.userTags;
    }

    public String getUserVideos() {

        return this.userVideos;
    }

    public List<VideoProfile> getVideos() {

        return this.videos;
    }

    public void load(final JSONObject obj) throws JSONException {

        final JSONObject response = obj.getJSONObject("user");

        this.userId = Stream.getString(response, Constant.USER_ID);
        this.username = Stream.getString(response, Constant.NAME);
        this.userPhotoPath = Stream.getString(response, Constant.PHOTO_PATH);
        this.userBannerPath = Stream.getString(response, Constant.BANNER_PATH);
        this.userLastUpdate = Stream.getString(response, Constant.LAST_UPDATE);
        this.userTags = Stream.getString(response, Constant.TOTAL_TAGS);
        this.userLikes = Stream.getString(response, Constant.TOTAL_LIKES);
        this.userVideos = Stream.getString(response, Constant.TOTAL_VIDEOS);
        this.userFollowers = Stream.getString(response, Constant.TOTAL_FOLLOWERS);
        this.userFollowing = Stream.getString(response, Constant.TOTAL_FOLLOWING);

        this.videos = Parser.parseVideosResponseJson(obj.toString());

        LOG.v(this.getUserBannerPath());
        LOG.v(this.getUserPhotoPath());

    }

}
