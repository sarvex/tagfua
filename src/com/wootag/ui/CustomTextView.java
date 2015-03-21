/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wTagFuui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;
import com.woTagFu;
import com.wootag.util.Typefaces;

public class CustomTextView extends TextView {

    private static final Logger LOG = LoggerManager.getLogger();

    public CustomTextView(final Context context) {

        super(context);
    }

    public CustomTextView(final Context context, final AttributeSet attrs) {

        super(context, attrs);
        this.setCustomFont(context, attrs);
    }

    public CustomTextView(final Context context, final AttributeSet attrs, final int defStyle) {

        super(context, attrs, defStyle);
        this.setCustomFont(context, attrs);
    }

    public boolean setCustomFont(final Context context, final String asset) {

        final Typeface tf = Typefaces.get(context, asset);
        this.setTypeface(tf);
        return true;
    }

    private void setCustomFont(final Context context, final AttributeSet attrs) {

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomTextView);
        final String customFont = a.getString(R.styleable.CustomTextView_customFont);
        this.setCustomFont(context, customFont);
        a.recycle();
    }

}
