/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wTagFuui;

import android.content.Context;
import android.widget.Button;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;
import com.wootag.dto.TagInfo;

public class CustomButton extends Button {

    private static final Logger LOG = LoggerManager.getLogger();

    private TagInfo tag;

    @Override
    public TagInfo getTag() {

        return this.tag;
    }

    public void setTag(final TagInfo tag) {

        this.tag = tag;
    }

    public CustomButton(final Context context) {

        super(context);
    }

    public CustomButton(final Context context, final TagInfo saveTag) {

        super(context);
        this.tag = saveTag;
    }

}
