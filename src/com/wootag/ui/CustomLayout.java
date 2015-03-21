/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

public class CustomLayout extends RelativeLayout {

    private static final Logger LOG = LoggerManager.getLogger();

    private static int parentWidth;

    public CustomLayout(final Context context) {

        super(context);
    }

    public CustomLayout(final Context context, final AttributeSet attrs) {

        super(context, attrs);
    }

    public CustomLayout(final Context context, final AttributeSet attrs, final int defStyle) {

        super(context, attrs, defStyle);
    }

    public static int getWidthOfSlider() {

        return parentWidth;
    }

    @Override
    protected synchronized void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        parentWidth = MeasureSpec.getSize(widthMeasureSpec);
    }
}
