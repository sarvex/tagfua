/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.video.trimmer.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

public class MyScrollView extends HorizontalScrollView {

    private static final Logger LOG = LoggerManager.getLogger();

    protected int intitPosition;
    private static final int CHECK = 100;
    protected onScrollStopListner onScrollstopListner;

    protected final Runnable scrollerTask;

    public MyScrollView(final Context context, final AttributeSet attrs) {

        super(context, attrs);
        this.scrollerTask = new Runnable() {

            @Override
            public void run() {

                final int newPosition = MyScrollView.this.getScrollX();
                if ((MyScrollView.this.intitPosition - newPosition) == 0) {
                    if (MyScrollView.this.onScrollstopListner != null) {
                        MyScrollView.this.onScrollstopListner.onScrollStoped();
                    }
                } else {
                    MyScrollView.this.intitPosition = MyScrollView.this.getScrollX();
                    MyScrollView.this.postDelayed(MyScrollView.this.scrollerTask, MyScrollView.CHECK);
                }
            }
        };
    }

    public void setOnScrollStopListner(final MyScrollView.onScrollStopListner listner) {

        this.onScrollstopListner = listner;
    }

    public void startScrollerTask() {

        this.intitPosition = this.getScrollX();
        MyScrollView.this.postDelayed(this.scrollerTask, MyScrollView.CHECK);
    }

    public interface onScrollStopListner {

        void onScrollStoped();
    }

}
