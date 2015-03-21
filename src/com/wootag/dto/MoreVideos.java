/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wTagFudto;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.woTagFuonstant;
import com.wootag.util.Stream;

public class MoreVideos implements Serializable {

    private static final Logger LOG = LoggerManager.getLogger();

    private static final long serialVersionUID = 1152240720661102313L;

    private String videoId;
    private String videoThumbPath;
    private String videoUrl;

    public String getVideoId() {

        return this.videoId;
    }

    public String getVideothumbPath() {

        return this.videoThumbPath;
    }

    public String getVideoUrl() {

        return this.videoUrl;
    }

    public void load(final JSONObject response) throws JSONException {

        this.videoId = Stream.getString(response, Constant.VIDEO_ID);
        this.videoUrl = Stream.getString(response, Constant.VIDEO_NAME);
        this.videoThumbPath = Stream.getString(response, Constant.VIDEO_THUMB_PATH);
    }

    public void setVideoId(final String videoId) {

        this.videoId = videoId;
    }

    public void setVideothumbPath(final String videothumbPath) {

        this.videoThumbPath = videothumbPath;
    }

    public void setVideoUrl(final String videoUrl) {

        this.videoUrl = videoUrl;
    }

}
