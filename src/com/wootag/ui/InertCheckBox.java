/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.CheckBox;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

public class InertCheckBox extends CheckBox {

    private static final Logger LOG = LoggerManager.getLogger();

    public InertCheckBox(final Context context, final AttributeSet attrs, final int defStyle) {

        super(context, attrs, defStyle);
    }

    public InertCheckBox(final Context context, final AttributeSet attrs) {

        super(context, attrs);
    }

    public InertCheckBox(final Context context) {

        super(context);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {

        return false;
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {

        return false;
    }

    @Override
    public boolean onKeyMultiple(final int keyCode, final int repeatCount, final KeyEvent event) {

        return false;
    }

    @Override
    public boolean onKeyPreIme(final int keyCode, final KeyEvent event) {

        return false;
    }

    @Override
    public boolean onKeyShortcut(final int keyCode, final KeyEvent event) {

        return false;
    }

    @Override
    public boolean onKeyUp(final int keyCode, final KeyEvent event) {

        return false;
    }

    @Override
    public boolean onTrackballEvent(final MotionEvent event) {

        return false;
    }
}
