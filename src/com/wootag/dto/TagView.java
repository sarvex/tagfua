/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.dto;

import android.widget.LinearLayout;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

public class TagView {

    private static final Logger LOG = LoggerManager.getLogger();

    private LinearLayout layout;
    private long tagId;
    private boolean visible;

    public LinearLayout getLayout() {

        return this.layout;
    }

    public void setLayout(final LinearLayout layout) {

        this.layout = layout;
    }

    public long getTagId() {

        return this.tagId;
    }

    public void setTagId(final long tagId) {

        this.tagId = tagId;
    }

    public boolean isVisible() {

        return this.visible;
    }

    public void setVisible(final boolean visible) {

        this.visible = visible;
    }

}
