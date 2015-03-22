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

public class Trends {

    public static final String TAG_ID = "tag_id";
    public static final String TAG_NAME = "tag_name";
    public static final String VIDEO_ID = "video_id";
    public static final String VIDEO_THUMB_PATH = "video_thumb_path";

    private static final Logger LOG = LoggerManager.getLogger();

    private String tagCount;
    private String tagId;
    private String tagName;
    private String videoId;
    private String videoThumbPath;

    public String getTagCount() {

        return this.tagCount;
    }

    public String getTagId() {

        return this.tagId;
    }

    public String getTagName() {

        return this.tagName;
    }

    public String getVideoId() {

        return this.videoId;
    }

    public String getVideoThumbPath() {

        return this.videoThumbPath;
    }

    public void load(final JSONObject response) throws JSONException {

        this.tagId = Stream.getString(response, TAG_ID);
        this.tagName = Stream.getString(response, TAG_NAME);
        this.tagCount = Stream.getString(response, Constant.TAG_COUNT);
        this.videoId = Stream.getString(response, VIDEO_ID);
        this.videoThumbPath = Stream.getString(response, VIDEO_THUMB_PATH);
    }

    public void setTagCount(final String tagCount) {

        this.tagCount = tagCount;
    }

    public void setTagId(final String tagId) {

        this.tagId = tagId;
    }

    public void setTagName(final String tagName) {

        this.tagName = tagName;
    }

    public void setVideoId(final String videoId) {

        this.videoId = videoId;
    }

    public void setVideoThumbPath(final String videoThumbPath) {

        this.videoThumbPath = videoThumbPath;
    }
}
