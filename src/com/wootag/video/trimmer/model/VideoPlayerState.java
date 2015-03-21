/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.video.trimmer.model;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

public class VideoPlayerState {

    private static final Logger LOG = LoggerManager.getLogger();

    private String filename;
    private int start, stop;
    private int currentTime;
    private String messageText;

    public String getMessageText() {

        return this.messageText;
    }

    public void setMessageText(final String messageText) {

        this.messageText = messageText;
    }

    public String getFilename() {

        return this.filename;
    }

    public void setFilename(final String filename) {

        this.filename = filename;
    }

    public int getStart() {

        return this.start;
    }

    public void setStart(final int start) {

        this.start = start;
    }

    public int getStop() {

        return this.stop;
    }

    public void setStop(final int stop) {

        this.stop = stop;
    }

    public void reset() {

        this.start = this.stop = 0;
    }

    public int getDuration() {

        return this.stop - this.start;
    }

    public int getCurrentTime() {

        return this.currentTime;
    }

    public void setCurrentTime(final int currentTime) {

        this.currentTime = currentTime;
    }

    public boolean isValid() {

        return this.stop > this.start;
    }
}
