/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;
import com.wootag.R;
import com.wootag.util.Typefaces;

public class CustomEditText extends EditText {

    private static final Logger LOG = LoggerManager.getLogger();

    public CustomEditText(final Context context) {

        super(context);
    }

    public CustomEditText(final Context context, final AttributeSet attrs) {

        super(context, attrs);
        this.setCustomFont(context, attrs);
    }

    public CustomEditText(final Context context, final AttributeSet attrs, final int defStyle) {

        super(context, attrs, defStyle);
        this.setCustomFont(context, attrs);
    }

    public boolean setCustomFont(final Context ctx, final String asset) {

        final Typeface tf = Typefaces.get(ctx, asset);

        this.setTypeface(tf);
        return true;
    }

    private void setCustomFont(final Context ctx, final AttributeSet attrs) {

        final TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.CustomTextView);
        final String customFont = a.getString(R.styleable.CustomTextView_customFont);
        this.setCustomFont(ctx, customFont);
        a.recycle();
    }

}
