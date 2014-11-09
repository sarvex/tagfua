/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;
import com.wootag.util.ScrollChangeListener;

/**
 * @author sarvex
 */
public class LoadingScrollView extends ScrollView {

    private static final Logger LOG = LoggerManager.getLogger();

    public ScrollChangeListener scrollChangeListener;

    public LoadingScrollView(final Context context, final AttributeSet attrs) {

        super(context, attrs);
    }

    public void setScrollChangeListener(final ScrollChangeListener scrollChangeListener) {

        this.scrollChangeListener = scrollChangeListener;
    }

    @Override
    protected void onScrollChanged(final int l, final int t, final int oldl, final int oldt) {

        super.onScrollChanged(l, t, oldl, oldt);

        if (this.scrollChangeListener != null) {
            this.scrollChangeListener.onScrollChanged(this, l, t, oldl, oldt);
        }

    }

}
