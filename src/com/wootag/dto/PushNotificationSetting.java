/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.dto;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

public class PushNotificationSetting {

    private static final Logger LOG = LoggerManager.getLogger();

    private int comments;
    private int enablePn;
    private int feeds;
    private int followers;
    private int likes;
    private int mentions;

    public int getComments() {

        return this.comments;
    }

    public int getEnablePn() {

        return this.enablePn;
    }

    public int getFeeds() {

        return this.feeds;
    }

    public int getFollowers() {

        return this.followers;
    }

    public int getLikes() {

        return this.likes;
    }

    public int getMentions() {

        return this.mentions;
    }

    public void setComments(final int comments) {

        this.comments = comments;
    }

    public void setEnablePn(final int enablePn) {

        this.enablePn = enablePn;
    }

    public void setFeeds(final int feeds) {

        this.feeds = feeds;
    }

    public void setFollowers(final int followers) {

        this.followers = followers;
    }

    public void setLikes(final int likes) {

        this.likes = likes;
    }

    public void setMentions(final int mentions) {

        this.mentions = mentions;
    }

}
