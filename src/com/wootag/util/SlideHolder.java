/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.util;

import java.util.LinkedList;
import java.util.Queue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.FrameLayout;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

public class SlideHolder extends FrameLayout {

    private static final int DIRECTION_LEFT = 1;

    private static final int DIRECTION_RIGHT = -1;
    private static final Logger LOG = LoggerManager.getLogger();
    private static final int MODE_FINISHED = 2;
    private static final int MODE_READY = 0;
    private static final int MODE_SLIDE = 1;
    private static final int PARENT_LEFT = 0;
    private static final int PARENT_TOP = 0;

    private boolean closeOnRelease;
    private int direction = DIRECTION_LEFT;
    private boolean dispatchWhenOpened;
    private boolean enabled = true;

    private int endOffset;
    private byte frame;

    private int historicalX;
    private boolean interceptTouch = true;
    private boolean alwaysOpened;

    private Bitmap mCachedBitmap;
    private Canvas mCachedCanvas;
    private Paint mCachedPaint;
    private final Animation.AnimationListener mCloseListener = new Animation.AnimationListener() {

        @Override
        public void onAnimationEnd(final Animation animation) {

            SlideHolder.this.completeClosing();
        }

        @Override
        public void onAnimationRepeat(final Animation animation) {

        }

        @Override
        public void onAnimationStart(final Animation animation) {

        }
    };

    private OnSlideListener mListener;

    protected View menuView;

    int mode = MODE_READY;

    int mOffset;

    private final Animation.AnimationListener mOpenListener = new Animation.AnimationListener() {

        @Override
        public void onAnimationEnd(final Animation animation) {

            SlideHolder.this.completeOpening();
        }

        @Override
        public void onAnimationRepeat(final Animation animation) {

        }

        @Override
        public void onAnimationStart(final Animation animation) {

        }
    };

    private int mStartOffset;

    private final Queue<Runnable> mWhenReady = new LinkedList<Runnable>();

    public SlideHolder(final Context context) {

        super(context);

        this.initView();
    }

    public SlideHolder(final Context context, final AttributeSet attrs) {

        super(context, attrs);

        this.initView();
    }

    public SlideHolder(final Context context, final AttributeSet attrs, final int defStyle) {

        super(context, attrs, defStyle);

        this.initView();
    }

    public boolean close() {

        if (!this.isOpened() || this.alwaysOpened || (this.mode == MODE_SLIDE)) {
            return false;
        }

        if (!this.isReadyForSlide()) {
            this.mWhenReady.add(new Runnable() {

                @Override
                public void run() {

                    SlideHolder.this.close();
                }
            });

            return true;
        }

        this.initSlideMode();

        final Animation anim = new SlideAnimation(this.mOffset, this.endOffset);
        anim.setAnimationListener(this.mCloseListener);
        this.startAnimation(anim);

        this.invalidate();

        return true;
    }

    public boolean closeImmediately() {

        if (!this.isOpened() || this.alwaysOpened || (this.mode == MODE_SLIDE)) {
            return false;
        }

        if (!this.isReadyForSlide()) {
            this.mWhenReady.add(new Runnable() {

                @Override
                public void run() {

                    SlideHolder.this.closeImmediately();
                }
            });

            return true;
        }

        this.menuView.setVisibility(View.GONE);
        this.mode = MODE_READY;
        this.requestLayout();

        if (this.mListener != null) {
            this.mListener.onSlideCompleted(false);
        }

        return true;
    }

    @Override
    public boolean dispatchTouchEvent(final MotionEvent event) {

        if (((!this.enabled || !this.interceptTouch) && (this.mode == MODE_READY)) || this.alwaysOpened) {
            return super.dispatchTouchEvent(event);
        }

        if (this.mode != MODE_FINISHED) {
            this.onTouchEvent(event);

            if (this.mode != MODE_SLIDE) {
                super.dispatchTouchEvent(event);
            } else {
                final MotionEvent cancelEvent = MotionEvent.obtain(event);
                cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
                super.dispatchTouchEvent(cancelEvent);
                cancelEvent.recycle();
            }

            return true;
        }
        final int action = event.getAction();

        final Rect rect = new Rect();
        final View menu = this.getChildAt(0);
        menu.getHitRect(rect);

        if (!rect.contains((int) event.getX(), (int) event.getY())) {
            if ((action == MotionEvent.ACTION_UP) && this.closeOnRelease && !this.dispatchWhenOpened) {
                this.close();
                this.closeOnRelease = false;
            } else {
                if ((action == MotionEvent.ACTION_DOWN) && !this.dispatchWhenOpened) {
                    this.closeOnRelease = true;
                }

                this.onTouchEvent(event);
            }

            if (this.dispatchWhenOpened) {
                super.dispatchTouchEvent(event);
            }

            return true;
        }
        this.onTouchEvent(event);

        event.offsetLocation(-menu.getLeft(), -menu.getTop());
        menu.dispatchTouchEvent(event);
        return true;
    }

    public int getMenuOffset() {

        return this.mOffset;
    }

    public boolean isAllowedInterceptTouch() {

        return this.interceptTouch;
    }

    public boolean isDispatchTouchWhenOpened() {

        return this.dispatchWhenOpened;
    }

    @Override
    public boolean isEnabled() {

        return this.enabled;
    }

    public boolean isOpened() {

        return this.alwaysOpened || (this.mode == MODE_FINISHED);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {

        final boolean handled = this.handleTouchEvent(event);

        this.invalidate();

        return handled;
    }

    public boolean open() {

        if (this.isOpened() || this.alwaysOpened || (this.mode == MODE_SLIDE)) {
            return false;
        }

        if (!this.isReadyForSlide()) {
            this.mWhenReady.add(new Runnable() {

                @Override
                public void run() {

                    SlideHolder.this.open();
                }
            });

            return true;
        }

        this.initSlideMode();

        final Animation anim = new SlideAnimation(this.mOffset, this.endOffset);
        anim.setAnimationListener(this.mOpenListener);
        this.startAnimation(anim);

        this.invalidate();

        return true;
    }

    public boolean openImmediately() {

        if (this.isOpened() || this.alwaysOpened || (this.mode == MODE_SLIDE)) {
            return false;
        }

        if (!this.isReadyForSlide()) {
            this.mWhenReady.add(new Runnable() {

                @Override
                public void run() {

                    SlideHolder.this.openImmediately();
                }
            });

            return true;
        }

        this.menuView.setVisibility(View.VISIBLE);
        this.mode = MODE_FINISHED;
        this.requestLayout();

        if (this.mListener != null) {
            this.mListener.onSlideCompleted(true);
        }

        return true;
    }

    /**
     * @param allow - if false, SlideHolder won't react to swiping gestures (but still will be able to work by manually
     *            invoking mathods)
     */
    public void setAllowInterceptTouch(final boolean allow) {

        this.interceptTouch = allow;
    }

    /**
     * @param opened - if true, SlideHolder will always be in opened state (which means that swiping won't work)
     */
    public void setAlwaysOpened(final boolean opened) {

        this.alwaysOpened = opened;

        this.requestLayout();
    }

    /**
     * @param direction - direction in which SlideHolder opens. Can be: DIRECTION_LEFT, DIRECTION_RIGHT
     */
    public void setDirection(final int direction) {

        this.closeImmediately();

        this.direction = direction;
    }

    /**
     * @param dispatch - if true, in open state SlideHolder will dispatch touch events to main layout (in other words -
     *            it will be clickable)
     */
    public void setDispatchTouchWhenOpened(final boolean dispatch) {

        this.dispatchWhenOpened = dispatch;
    }

    @Override
    public void setEnabled(final boolean enabled) {

        this.enabled = enabled;
    }

    public void setOnSlideListener(final OnSlideListener lis) {

        this.mListener = lis;
    }

    public void toggle() {

        if (this.isOpened()) {
            this.close();
        } else {
            this.open();
        }
    }

    public void toggle(final boolean immediately) {

        if (immediately) {
            this.toggleImmediately();
        } else {
            this.toggle();
        }
    }

    public void toggleImmediately() {

        if (this.isOpened()) {
            this.closeImmediately();
        } else {
            this.openImmediately();
        }
    }

    private void finishSlide() {

        if ((this.direction * this.endOffset) > 0) {
            if ((this.direction * this.mOffset) > ((this.direction * this.endOffset) / 2)) {
                if ((this.direction * this.mOffset) > (this.direction * this.endOffset)) {
                    this.mOffset = this.endOffset;
                }

                final Animation anim = new SlideAnimation(this.mOffset, this.endOffset);
                anim.setAnimationListener(this.mOpenListener);
                this.startAnimation(anim);
            } else {
                if ((this.direction * this.mOffset) < (this.direction * this.mStartOffset)) {
                    this.mOffset = this.mStartOffset;
                }

                final Animation anim = new SlideAnimation(this.mOffset, this.mStartOffset);
                anim.setAnimationListener(this.mCloseListener);
                this.startAnimation(anim);
            }
        } else {
            if ((this.direction * this.mOffset) < ((this.direction * this.mStartOffset) / 2)) {
                if ((this.direction * this.mOffset) < (this.direction * this.endOffset)) {
                    this.mOffset = this.endOffset;
                }

                final Animation anim = new SlideAnimation(this.mOffset, this.endOffset);
                anim.setAnimationListener(this.mCloseListener);
                this.startAnimation(anim);
            } else {
                if ((this.direction * this.mOffset) > (this.direction * this.mStartOffset)) {
                    this.mOffset = this.mStartOffset;
                }

                final Animation anim = new SlideAnimation(this.mOffset, this.mStartOffset);
                anim.setAnimationListener(this.mOpenListener);
                this.startAnimation(anim);
            }
        }
    }

    private boolean handleTouchEvent(final MotionEvent ev) {

        if (!this.enabled) {
            return false;
        }

        final float x = ev.getX();

        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            this.historicalX = (int) x;

            return true;
        }

        if (ev.getAction() == MotionEvent.ACTION_MOVE) {

            final float diff = x - this.historicalX;

            if ((((this.direction * diff) > 50) && (this.mode == MODE_READY))
                    || (((this.direction * diff) < -50) && (this.mode == MODE_FINISHED))) {
                this.historicalX = (int) x;

                this.initSlideMode();
            } else if (this.mode == MODE_SLIDE) {
                this.mOffset += diff;

                this.historicalX = (int) x;

                if (!this.isSlideAllowed()) {
                    this.finishSlide();
                }
            } else {
                return false;
            }
        }

        if (ev.getAction() == MotionEvent.ACTION_UP) {
            if (this.mode == MODE_SLIDE) {
                this.finishSlide();
            }

            this.closeOnRelease = false;

            return false;
        }

        return this.mode == MODE_SLIDE;
    }

    private void initSlideMode() {

        this.closeOnRelease = false;

        final View v = this.getChildAt(1);

        if (this.mode == MODE_READY) {
            this.mStartOffset = 0;
            this.endOffset = this.direction * this.getChildAt(0).getWidth();
        } else {
            this.mStartOffset = this.direction * this.getChildAt(0).getWidth();
            this.endOffset = 0;
        }

        this.mOffset = this.mStartOffset;

        if ((this.mCachedBitmap == null) || this.mCachedBitmap.isRecycled()
                || (this.mCachedBitmap.getWidth() != v.getWidth())) {
            this.mCachedBitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
            this.mCachedCanvas = new Canvas(this.mCachedBitmap);
        } else {
            this.mCachedCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
        }

        v.setVisibility(View.VISIBLE);

        this.mCachedCanvas.translate(-v.getScrollX(), -v.getScrollY());
        v.draw(this.mCachedCanvas);

        this.mode = MODE_SLIDE;

        this.menuView.setVisibility(View.VISIBLE);
    }

    private void initView() {

        this.mCachedPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
    }

    private boolean isReadyForSlide() {

        return ((this.getWidth() > 0) && (this.getHeight() > 0));
    }

    private boolean isSlideAllowed() {

        return (((this.direction * this.endOffset) > 0)
                && ((this.direction * this.mOffset) < (this.direction * this.endOffset)) && ((this.direction * this.mOffset) >= (this.direction * this.mStartOffset)))
                || ((this.endOffset == 0) && ((this.direction * this.mOffset) > (this.direction * this.endOffset)) && ((this.direction * this.mOffset) <= (this.direction * this.mStartOffset)));
    }

    @Override
    protected void dispatchDraw(final Canvas canvas) {

        try {
            if (this.mode == MODE_SLIDE) {
                final View main = this.getChildAt(1);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    /*
                     * On new versions we redrawing main layout only if it's marked as dirty
                     */
                    if (main.isDirty()) {
                        this.mCachedCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
                        main.draw(this.mCachedCanvas);
                    }
                } else {
                    /*
                     * On older versions we just redrawing our cache every 5th frame
                     */
                    if ((++this.frame % 5) == 0) {
                        this.mCachedCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
                        main.draw(this.mCachedCanvas);
                    }
                }

                /*
                 * Draw only visible part of menu
                 */

                final View menu = this.getChildAt(0);
                final int scrollX = menu.getScrollX();
                final int scrollY = menu.getScrollY();

                canvas.save();

                if (this.direction == DIRECTION_LEFT) {
                    canvas.clipRect(0, 0, this.mOffset, menu.getHeight(), Op.REPLACE);
                } else {
                    final int menuWidth = menu.getWidth();
                    final int menuLeft = menu.getLeft();

                    canvas.clipRect(menuLeft + menuWidth + this.mOffset, 0, menuLeft + menuWidth, menu.getHeight());
                }

                canvas.translate(menu.getLeft(), menu.getTop());
                canvas.translate(-scrollX, -scrollY);

                menu.draw(canvas);

                canvas.restore();

                canvas.drawBitmap(this.mCachedBitmap, this.mOffset, 0, this.mCachedPaint);
            } else {
                if (!this.alwaysOpened && (this.mode == MODE_READY)) {
                    this.menuView.setVisibility(View.GONE);
                }

                super.dispatchDraw(canvas);
            }
        } catch (final IndexOutOfBoundsException e) {
            LOG.e(e);
            /*
             * Possibility of crashes on some devices (especially on Samsung). Usually, when ListView is empty.
             */
        }
    }

    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {

        final int parentRight = r - l;
        final int parentBottom = b - t;

        final View menu = this.getChildAt(0);
        final int menuWidth = menu.getMeasuredWidth();

        if (this.direction == DIRECTION_LEFT) {
            menu.layout(PARENT_LEFT, PARENT_TOP, PARENT_LEFT + menuWidth, parentBottom);
        } else {
            menu.layout(parentRight - menuWidth, PARENT_TOP, parentRight, parentBottom);
        }

        if (this.alwaysOpened) {
            if (this.direction == DIRECTION_LEFT) {
                this.mOffset = menuWidth;
            } else {
                this.mOffset = 0;
            }
        } else if (this.mode == MODE_FINISHED) {
            this.mOffset = this.direction * menuWidth;
        } else if (this.mode == MODE_READY) {
            this.mOffset = 0;
        }

        final View main = this.getChildAt(1);
        main.layout(PARENT_LEFT + this.mOffset, PARENT_TOP, PARENT_LEFT + this.mOffset + main.getMeasuredWidth(),
                parentBottom);

        this.invalidate();

        Runnable rn;
        while ((rn = this.mWhenReady.poll()) != null) {
            rn.run();
        }
    }

    @Override
    protected void onMeasure(final int wSp, final int hSp) {

        this.menuView = this.getChildAt(0);

        if (this.alwaysOpened) {
            final View main = this.getChildAt(1);

            if ((this.menuView != null) && (main != null)) {
                this.measureChild(this.menuView, wSp, hSp);
                final LayoutParams lp = (LayoutParams) main.getLayoutParams();

                if (this.direction == DIRECTION_LEFT) {
                    lp.leftMargin = this.menuView.getMeasuredWidth();
                } else {
                    lp.rightMargin = this.menuView.getMeasuredWidth();
                }
            }
        }

        super.onMeasure(wSp, hSp);
    }

    void completeClosing() {

        this.mOffset = 0;
        this.requestLayout();

        this.post(new Runnable() {

            @Override
            public void run() {

                SlideHolder.this.mode = MODE_READY;
                SlideHolder.this.menuView.setVisibility(View.GONE);
            }
        });

        if (this.mListener != null) {
            this.mListener.onSlideCompleted(false);
        }
    }

    void completeOpening() {

        this.mOffset = this.direction * this.menuView.getWidth();
        this.requestLayout();

        this.post(new Runnable() {

            @Override
            public void run() {

                SlideHolder.this.mode = MODE_FINISHED;
                SlideHolder.this.menuView.setVisibility(View.VISIBLE);
            }
        });

        if (this.mListener != null) {
            this.mListener.onSlideCompleted(true);
        }
    }

    private class SlideAnimation extends Animation {

        private static final float SPEED = 0.6f;

        private final float mEnd;
        private final float mStart;

        public SlideAnimation(final float fromX, final float toX) {

            this.mStart = fromX;
            this.mEnd = toX;

            this.setInterpolator(new DecelerateInterpolator());

            final float duration = Math.abs(this.mEnd - this.mStart) / SPEED;
            this.setDuration((long) duration);
        }

        @Override
        protected void applyTransformation(final float interpolatedTime, final Transformation t) {

            super.applyTransformation(interpolatedTime, t);

            final float offset = ((this.mEnd - this.mStart) * interpolatedTime) + this.mStart;
            SlideHolder.this.mOffset = (int) offset;

            SlideHolder.this.postInvalidate();
        }

    }

    interface OnSlideListener {

        void onSlideCompleted(boolean opened);
    }

}
