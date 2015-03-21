/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.wootag;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Scroller;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

public final class HorizontalPager extends ViewGroup {

    private static final Logger LOG = LoggerManager.getLogger();

    private static final int ANIMATION_SCREEN_SET_DURATION_MILLIS = 500;
    private static final int FRACTION_OF_SCREEN_WIDTH_FOR_SWIPE = 4;
    private static final int INVALID_SCREEN = -1;
    private static final int SNAP_VELOCITY_DIP_PER_SECOND = 600;
    private static final int VELOCITY_UNIT_PIXELS_PER_SECOND = 1000;
    private static final int TOUCH_STATE_REST = 0;
    private static final int TOUCH_STATE_HORIZONTAL_SCROLLING = 1;
    private static final int TOUCH_STATE_VERTICAL_SCROLLING = -1;

    private OnScreenSwitchListener onScreenSwitchListener;
    private Scroller scroller;
    private VelocityTracker velocityTracker;
    private boolean mFirstLayout = true;
    private float lastMotionX;
    private float lastMotionY;
    private int currentScreen;
    private int densityAdjustedSnapVelocity;
    private int maximumVelocity;
    private int nextScreen = INVALID_SCREEN;
    private int touchSlop;
    private int touchState = TOUCH_STATE_REST;

    private int lastSeenLayoutWidth = -1;

    /**
     * Simple constructor to use when creating a view from code.
     *
     * @param context The Context the view is running in, through which it can access the current theme, resources, etc.
     */
    public HorizontalPager(final Context context) {

        super(context);
        this.init();
    }

    public HorizontalPager(final Context context, final AttributeSet attrs) {

        super(context, attrs);
        this.init();
    }

    @Override
    public void computeScroll() {

        if (this.scroller.computeScrollOffset()) {
            this.scrollTo(this.scroller.getCurrX(), this.scroller.getCurrY());
            this.postInvalidate();
        } else if (this.nextScreen != INVALID_SCREEN) {
            this.currentScreen = Math.max(0, Math.min(this.nextScreen, this.getChildCount() - 1));

            // Notify observer about screen change
            if (this.onScreenSwitchListener != null) {
                this.onScreenSwitchListener.onScreenSwitched(this.currentScreen);
            }

            this.nextScreen = INVALID_SCREEN;
        }
    }

    /**
     * Returns the index of the currently displayed screen.
     *
     * @return The index of the currently displayed screen.
     */
    public int getCurrentScreen() {

        return this.currentScreen;
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent event) {

        /*
         * By Yoni Samlan: Modified onInterceptTouchEvent based on standard ScrollView's onIntercept. The logic is
         * designed to support a nested vertically scrolling view inside this one; once a scroll registers for X-wise
         * scrolling, handle it in this view and don't let the children, but once a scroll registers for y-wise
         * scrolling, let the children handle it exclusively.
         */
        final int action = event.getAction();
        boolean intercept = false;

        switch (action) {
        case MotionEvent.ACTION_MOVE:
            /*
             * If we're in a horizontal scroll event, take it (intercept further events). But if we're
             * mid-vertical-scroll, don't even try; let the children deal with it. If we haven't found a scroll event
             * yet, check for one.
             */
            if (this.touchState == TOUCH_STATE_HORIZONTAL_SCROLLING) {
                /*
                 * We've already started a horizontal scroll; set intercept to true so we can take the remainder of all
                 * touch events in onTouchEvent.
                 */
                intercept = true;
            } else if (this.touchState == TOUCH_STATE_VERTICAL_SCROLLING) {
                // Let children handle the events for the duration of the scroll event.
                intercept = false;
            } else { // We haven't picked up a scroll event yet; check for one.

                /*
                 * If we detected a horizontal scroll event, start stealing touch events (mark as scrolling). Otherwise,
                 * see if we had a vertical scroll event -- if so, let the children handle it and don't look to
                 * intercept again until the motion is done.
                 */

                final float x = event.getX();
                final int xDiff = (int) Math.abs(x - this.lastMotionX);
                final boolean xMoved = xDiff > this.touchSlop;

                if (xMoved) {
                    // Scroll if the user moved far enough along the X axis
                    this.touchState = TOUCH_STATE_HORIZONTAL_SCROLLING;
                    this.lastMotionX = x;
                }

                final float y = event.getY();
                final int yDiff = (int) Math.abs(y - this.lastMotionY);
                final boolean yMoved = yDiff > this.touchSlop;

                if (yMoved) {
                    this.touchState = TOUCH_STATE_VERTICAL_SCROLLING;
                }
            }

            break;
        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
            // Release the drag.
            this.touchState = TOUCH_STATE_REST;
            break;
        case MotionEvent.ACTION_DOWN:
            /*
             * No motion yet, but register the coordinates so we can check for intercept at the next MOVE event.
             */
            this.lastMotionY = event.getY();
            this.lastMotionX = event.getX();
            break;
        default:
            break;
        }

        return intercept;
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {

        if (this.velocityTracker == null) {
            this.velocityTracker = VelocityTracker.obtain();
        }
        this.velocityTracker.addMovement(event);

        final int action = event.getAction();
        final float x = event.getX();

        switch (action) {
        case MotionEvent.ACTION_DOWN:
            /*
             * If being flinged and user touches, stop the fling. isFinished will be false if being flinged.
             */
            if (!this.scroller.isFinished()) {
                this.scroller.abortAnimation();
            }

            // Remember where the motion event started
            this.lastMotionX = x;

            if (this.scroller.isFinished()) {
                this.touchState = TOUCH_STATE_REST;
            } else {
                this.touchState = TOUCH_STATE_HORIZONTAL_SCROLLING;
            }

            break;
        case MotionEvent.ACTION_MOVE:
            final int xDiff = (int) Math.abs(x - this.lastMotionX);
            final boolean xMoved = xDiff > this.touchSlop;

            if (xMoved) {
                // Scroll if the user moved far enough along the X axis
                this.touchState = TOUCH_STATE_HORIZONTAL_SCROLLING;
            }

            if (this.touchState == TOUCH_STATE_HORIZONTAL_SCROLLING) {
                // Scroll to follow the motion event
                final int deltaX = (int) (this.lastMotionX - x);
                this.lastMotionX = x;
                final int scrollX = this.getScrollX();

                if (deltaX < 0) {
                    if (scrollX > 0) {
                        this.scrollBy(Math.max(-scrollX, deltaX), 0);
                    }
                } else if (deltaX > 0) {
                    final int availableToScroll = this.getChildAt(this.getChildCount() - 1).getRight() - scrollX
                            - this.getWidth();

                    if (availableToScroll > 0) {
                        this.scrollBy(Math.min(availableToScroll, deltaX), 0);
                    }
                }
            }

            break;

        case MotionEvent.ACTION_UP:
            if (this.touchState == TOUCH_STATE_HORIZONTAL_SCROLLING) {
                final VelocityTracker velocityTracker = this.velocityTracker;
                velocityTracker.computeCurrentVelocity(VELOCITY_UNIT_PIXELS_PER_SECOND, this.maximumVelocity);
                final int velocityX = (int) velocityTracker.getXVelocity();

                if (velocityX > this.densityAdjustedSnapVelocity && this.currentScreen > 0) {
                    // Fling hard enough to move left
                    this.snapToScreen(this.currentScreen - 1);
                } else if (velocityX < -this.densityAdjustedSnapVelocity
                        && this.currentScreen < this.getChildCount() - 1) {
                    // Fling hard enough to move right
                    this.snapToScreen(this.currentScreen + 1);
                } else {
                    this.snapToDestination();
                }

                if (this.velocityTracker != null) {
                    this.velocityTracker.recycle();
                    this.velocityTracker = null;
                }
            }

            this.touchState = TOUCH_STATE_REST;

            break;
        case MotionEvent.ACTION_CANCEL:
            this.touchState = TOUCH_STATE_REST;
            break;
        default:
            break;
        }

        return true;
    }

    /**
     * Sets the current screen.
     *
     * @param currentScreen The new screen.
     * @param animate True to smoothly scroll to the screen, false to snap instantly
     */
    public void setCurrentScreen(final int currentScreen, final boolean animate) {

        this.currentScreen = Math.max(0, Math.min(currentScreen, this.getChildCount() - 1));
        if (animate) {
            this.snapToScreen(currentScreen, ANIMATION_SCREEN_SET_DURATION_MILLIS);
        } else {
            this.scrollTo(this.currentScreen * this.getWidth(), 0);
        }
        this.invalidate();
    }

    /**
     * Sets the {@link OnScreenSwitchListener}.
     *
     * @param onScreenSwitchListener The listener for switch events.
     */
    public void setOnScreenSwitchListener(final OnScreenSwitchListener onScreenSwitchListener) {

        this.onScreenSwitchListener = onScreenSwitchListener;
    }

    /**
     * Sets up the scroller and touch/fling sensitivity parameters for the pager.
     */
    private void init() {

        this.scroller = new Scroller(this.getContext());

        // Calculate the density-dependent snap velocity in pixels
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) this.getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(
                displayMetrics);
        this.densityAdjustedSnapVelocity = (int) (displayMetrics.density * SNAP_VELOCITY_DIP_PER_SECOND);

        final ViewConfiguration configuration = ViewConfiguration.get(this.getContext());
        this.touchSlop = configuration.getScaledTouchSlop();
        this.maximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    /**
     * Snaps to the screen we think the user wants (the current screen for very small movements; the next/prev screen
     * for bigger movements).
     */
    private void snapToDestination() {

        final int screenWidth = this.getWidth();
        final int scrollX = this.getScrollX();
        int whichScreen = this.currentScreen;
        final int deltaX = scrollX - screenWidth * this.currentScreen;

        // Check if they want to go to the prev. screen
        if (deltaX < 0 && this.currentScreen != 0 && screenWidth / FRACTION_OF_SCREEN_WIDTH_FOR_SWIPE < -deltaX) {
            whichScreen--;
            // Check if they want to go to the next screen
        } else if (deltaX > 0 && this.currentScreen + 1 != this.getChildCount()
                && screenWidth / FRACTION_OF_SCREEN_WIDTH_FOR_SWIPE < deltaX) {
            whichScreen++;
        }

        this.snapToScreen(whichScreen);
    }

    /**
     * Snap to a specific screen, animating automatically for a duration proportional to the distance left to scroll.
     *
     * @param whichScreen Screen to snap to
     */
    private void snapToScreen(final int whichScreen) {

        this.snapToScreen(whichScreen, -1);
    }

    /**
     * Snaps to a specific screen, animating for a specific amount of time to get there.
     *
     * @param whichScreen Screen to snap to
     * @param duration -1 to automatically time it based on scroll distance; a positive number to make the scroll take
     *            an exact duration.
     */
    private void snapToScreen(final int whichScreen, final int duration) {

        /*
         * Modified by Yoni Samlan: Allow new snapping even during an ongoing scroll animation. This is intended to make
         * HorizontalPager work as expected when used in conjunction with a RadioGroup used as "tabbed" controls. Also,
         * make the animation take a percentage of our normal animation time, depending how far they've already
         * scrolled.
         */
        this.nextScreen = Math.max(0, Math.min(whichScreen, this.getChildCount() - 1));
        final int newX = this.nextScreen * this.getWidth();
        final int delta = newX - this.getScrollX();

        if (duration < 0) {
            // E.g. if they've scrolled 80% of the way, only animation for 20% of the duration
            this.scroller.startScroll(this.getScrollX(), 0, delta, 0,
                    (int) (Math.abs(delta) / (float) this.getWidth() * ANIMATION_SCREEN_SET_DURATION_MILLIS));
        } else {
            this.scroller.startScroll(this.getScrollX(), 0, delta, 0, duration);
        }

        this.invalidate();
    }

    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {

        int childLeft = 0;
        final int count = this.getChildCount();

        for (int i = 0; i < count; i++) {
            final View child = this.getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                final int childWidth = child.getMeasuredWidth();
                child.layout(childLeft, 0, childLeft + childWidth, child.getMeasuredHeight());
                childLeft += childWidth;
            }
        }
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("ViewSwitcher can only be used in EXACTLY mode.");
        }

        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("ViewSwitcher can only be used in EXACTLY mode.");
        }

        // The children are given the same width and height as the workspace
        final int count = this.getChildCount();
        for (int i = 0; i < count; i++) {
            this.getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }

        final int width = MeasureSpec.getSize(widthMeasureSpec);
        if (this.mFirstLayout) {
            this.scrollTo(this.currentScreen * width, 0);
            this.mFirstLayout = false;
        }

        else if (width != this.lastSeenLayoutWidth) { // Width has changed
            /*
             * Recalculate the width and scroll to the right position to be sure we're in the right place in the event
             * that we had a rotation that didn't result in an activity restart (code by aveyD). Without this you can
             * end up between two pages after a rotation.
             */
            final Display display = ((WindowManager) this.getContext().getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay();
            final int displayWidth = display.getWidth();

            this.nextScreen = Math.max(0, Math.min(this.getCurrentScreen(), this.getChildCount() - 1));
            final int newX = this.nextScreen * displayWidth;
            final int delta = newX - this.getScrollX();

            this.scroller.startScroll(this.getScrollX(), 0, delta, 0, 0);
        }

        this.lastSeenLayoutWidth = width;
    }

    /**
     * Listener for the event that the HorizontalPager switches to a new view.
     */
    interface OnScreenSwitchListener {

        /**
         * Notifies listeners about the new screen. Runs after the animation completed.
         *
         * @param screen The new screen index.
         */
        void onScreenSwitched(int screen);
    }
}
