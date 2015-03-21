/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.RelativeLayout;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

public class CheckableRelativeLayout extends RelativeLayout implements Checkable {

    private static final Logger LOG = LoggerManager.getLogger();

    private boolean checked;
    private List<Checkable> checkableViews;

    public CheckableRelativeLayout(final Context context, final AttributeSet attrs) {

        super(context, attrs);
        this.initialise(attrs);
    }

    public CheckableRelativeLayout(final Context context, final AttributeSet attrs, final int defStyle) {

        super(context, attrs, defStyle);
        this.initialise(attrs);
    }

    public CheckableRelativeLayout(final Context context, final int checkableId) {

        super(context);
        this.initialise(null);
    }

    /*
     * @see android.widget.Checkable#isChecked()
     */
    @Override
    public boolean isChecked() {

        return this.checked;
    }

    /*
     * @see android.widget.Checkable#setChecked(boolean)
     */
    @Override
    public void setChecked(final boolean checked) {

        this.checked = checked;
        for (final Checkable c : this.checkableViews) {
            c.setChecked(checked);
        }
    }

    /*
     * @see android.widget.Checkable#toggle()
     */
    @Override
    public void toggle() {

        this.checked ^= true;
        for (final Checkable c : this.checkableViews) {
            c.toggle();
        }
    }

    /**
     * Add to our checkable list all the children of the view that implement the interface Checkable
     */
    private void findCheckableChildren(final View view) {

        if (view instanceof Checkable) {
            this.checkableViews.add((Checkable) view);
        }

        if (view instanceof ViewGroup) {
            final ViewGroup vg = (ViewGroup) view;
            final int childCount = vg.getChildCount();
            for (int i = 0; i < childCount; ++i) {
                this.findCheckableChildren(vg.getChildAt(i));
            }
        }
    }

    /**
     * Read the custom XML attributes
     */
    private void initialise(final AttributeSet attrs) {

        this.checked = false;
        this.checkableViews = new ArrayList<Checkable>(5);
    }

    @Override
    protected void onFinishInflate() {

        super.onFinishInflate();

        final int childCount = this.getChildCount();
        for (int i = 0; i < childCount; ++i) {
            this.findCheckableChildren(this.getChildAt(i));
        }
    }
}
