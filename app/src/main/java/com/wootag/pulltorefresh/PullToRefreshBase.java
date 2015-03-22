/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes. Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 *******************************************************************************/
package com.TagFu.pulltorefresh;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.TagFu.R;
import com.TagFu.pulltorefresh.internal.FlipLoadingLayout;
import com.TagFu.pulltorefresh.internal.LoadingLayout;
import com.TagFu.pulltorefresh.internal.RotateLoadingLayout;
import com.TagFu.pulltorefresh.internal.Utils;
import com.TagFu.pulltorefresh.internal.ViewCompat;

public abstract class PullToRefreshBase<T extends View> extends LinearLayout implements IPullToRefresh<T> {

    // ===========================================================
    // Constants
    // ===========================================================

    static final boolean DEBUG = true;

    static final boolean USE_HW_LAYERS = false;

    static final String LOG_TAG = "PullToRefresh";

    static final float FRICTION = 2.0f;

    public static final int SMOOTH_SCROLL_DURATION_MS = 200;
    public static final int SMOOTH_SCROLL_LONG_DURATION_MS = 325;
    static final int DEMO_SCROLL_INTERVAL = 225;

    static final String STATE_STATE = "ptr_state";
    static final String STATE_MODE = "ptr_mode";
    static final String STATE_CURRENT_MODE = "ptr_current_mode";
    static final String STATE_SCROLLING_REFRESHING_ENABLED = "ptr_disable_scrolling";
    static final String STATE_SHOW_REFRESHING_VIEW = "ptr_show_refreshing_view";
    static final String STATE_SUPER = "ptr_super";

    // ===========================================================
    // Fields
    // ===========================================================

    private int mTouchSlop;
    private float mLastMotionX, mLastMotionY;
    private float mInitialMotionX, mInitialMotionY;

    private boolean beingDragged;
    private State mState = State.RESET;
    private Mode mMode = Mode.getDefault();

    private Mode mCurrentMode;
    T mRefreshableView;
    private FrameLayout mRefreshableViewWrapper;

    private boolean showViewWhileRefreshing = true;
    private boolean scrollingWhileRefreshingEnabled;
    private boolean filterTouchEvents = true;
    private boolean overScrollEnabled = true;
    private boolean layoutVisibilityChangesEnabled = true;

    protected Interpolator scrollAnimationInterpolator;
    private AnimationStyle mLoadingAnimationStyle = AnimationStyle.getDefault();

    private LoadingLayout mHeaderLayout;
    private LoadingLayout mFooterLayout;

    private OnRefreshListener<T> mOnRefreshListener;
    private OnRefreshListener2<T> mOnRefreshListener2;
    private OnPullEventListener<T> mOnPullEventListener;

    private SmoothScrollRunnable mCurrentSmoothScrollRunnable;

    // ===========================================================
    // Constructors
    // ===========================================================

    public PullToRefreshBase(final Context context) {

        super(context);
        this.init(context, null);
    }

    public PullToRefreshBase(final Context context, final AttributeSet attrs) {

        super(context, attrs);
        this.init(context, attrs);
    }

    public PullToRefreshBase(final Context context, final Mode mode) {

        super(context);
        this.mMode = mode;
        this.init(context, null);
    }

    public PullToRefreshBase(final Context context, final Mode mode, final AnimationStyle animStyle) {

        super(context);
        this.mMode = mode;
        this.mLoadingAnimationStyle = animStyle;
        this.init(context, null);
    }

    @Override
    public void addView(final View child, final int index, final ViewGroup.LayoutParams params) {

        if (DEBUG) {
            Log.d(LOG_TAG, "addView: " + child.getClass().getSimpleName());
        }

        final T refreshableView = this.getRefreshableView();

        if (refreshableView instanceof ViewGroup) {
            ((ViewGroup) refreshableView).addView(child, index, params);
        } else {
            throw new UnsupportedOperationException("Refreshable View is not a ViewGroup so can't addView");
        }
    }

    @Override
    public final boolean demo() {

        if (this.mMode.showHeaderLoadingLayout() && this.isReadyForPullStart()) {
            this.smoothScrollToAndBack(-this.getHeaderSize() * 2);
            return true;
        } else if (this.mMode.showFooterLoadingLayout() && this.isReadyForPullEnd()) {
            this.smoothScrollToAndBack(this.getFooterSize() * 2);
            return true;
        }

        return false;
    }

    @Override
    public final Mode getCurrentMode() {

        return this.mCurrentMode;
    }

    @Override
    public final boolean getFilterTouchEvents() {

        return this.filterTouchEvents;
    }

    @Override
    public final ILoadingLayout getLoadingLayoutProxy() {

        return this.getLoadingLayoutProxy(true, true);
    }

    @Override
    public final ILoadingLayout getLoadingLayoutProxy(final boolean includeStart, final boolean includeEnd) {

        return this.createLoadingLayoutProxy(includeStart, includeEnd);
    }

    @Override
    public final Mode getMode() {

        return this.mMode;
    }

    /**
     * @return Either {@link Orientation#VERTICAL} or {@link Orientation#HORIZONTAL} depending on the scroll direction.
     */
    public abstract Orientation getPullToRefreshScrollDirection();

    @Override
    public final T getRefreshableView() {

        return this.mRefreshableView;
    }

    @Override
    public final boolean getShowViewWhileRefreshing() {

        return this.showViewWhileRefreshing;
    }

    @Override
    public final State getState() {

        return this.mState;
    }

    @Override
    public final boolean isPullToRefreshEnabled() {

        return this.mMode.permitsPullToRefresh();
    }

    @Override
    public final boolean isPullToRefreshOverScrollEnabled() {

        return (VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD) && this.overScrollEnabled
                && OverscrollHelper.isAndroidOverScrollEnabled(this.mRefreshableView);
    }

    @Override
    public final boolean isRefreshing() {

        return (this.mState == State.REFRESHING) || (this.mState == State.MANUAL_REFRESHING);
    }

    @Override
    public final boolean isScrollingWhileRefreshingEnabled() {

        return this.scrollingWhileRefreshingEnabled;
    }

    @Override
    public final boolean onInterceptTouchEvent(final MotionEvent event) {

        if (!this.isPullToRefreshEnabled()) {
            return false;
        }

        final int action = event.getAction();

        if ((action == MotionEvent.ACTION_CANCEL) || (action == MotionEvent.ACTION_UP)) {
            this.beingDragged = false;
            return false;
        }

        if ((action != MotionEvent.ACTION_DOWN) && this.beingDragged) {
            return true;
        }

        switch (action) {
        case MotionEvent.ACTION_MOVE: {
            // If we're refreshing, and the flag is set. Eat all MOVE events
            if (!this.scrollingWhileRefreshingEnabled && this.isRefreshing()) {
                return true;
            }

            if (this.isReadyForPull()) {
                final float y = event.getY(), x = event.getX();
                final float diff, oppositeDiff, absDiff;

                // We need to use the correct values, based on scroll
                // direction
                switch (this.getPullToRefreshScrollDirection()) {
                case HORIZONTAL:
                    diff = x - this.mLastMotionX;
                    oppositeDiff = y - this.mLastMotionY;
                    break;
                case VERTICAL:
                default:
                    diff = y - this.mLastMotionY;
                    oppositeDiff = x - this.mLastMotionX;
                    break;
                }
                absDiff = Math.abs(diff);

                if ((absDiff > this.mTouchSlop) && (!this.filterTouchEvents || (absDiff > Math.abs(oppositeDiff)))) {
                    if (this.mMode.showHeaderLoadingLayout() && (diff >= 1f) && this.isReadyForPullStart()) {
                        this.mLastMotionY = y;
                        this.mLastMotionX = x;
                        this.beingDragged = true;
                        if (this.mMode == Mode.BOTH) {
                            this.mCurrentMode = Mode.PULL_FROM_START;
                        }
                    } else if (this.mMode.showFooterLoadingLayout() && (diff <= -1f) && this.isReadyForPullEnd()) {
                        this.mLastMotionY = y;
                        this.mLastMotionX = x;
                        this.beingDragged = true;
                        if (this.mMode == Mode.BOTH) {
                            this.mCurrentMode = Mode.PULL_FROM_END;
                        }
                    }
                }
            }
            break;
        }
        case MotionEvent.ACTION_DOWN: {
            if (this.isReadyForPull()) {
                this.mLastMotionY = this.mInitialMotionY = event.getY();
                this.mLastMotionX = this.mInitialMotionX = event.getX();
                this.beingDragged = false;
            }
            break;
        }
        default:
            break;
        }

        return this.beingDragged;
    }

    @Override
    public final void onRefreshComplete() {

        if (this.isRefreshing()) {
            this.setState(State.RESET);
        }
    }

    @Override
    public final boolean onTouchEvent(final MotionEvent event) {

        if (!this.isPullToRefreshEnabled()) {
            return false;
        }

        // If we're refreshing, and the flag is set. Eat the event
        if (!this.scrollingWhileRefreshingEnabled && this.isRefreshing()) {
            return true;
        }

        if ((event.getAction() == MotionEvent.ACTION_DOWN) && (event.getEdgeFlags() != 0)) {
            return false;
        }

        switch (event.getAction()) {
        case MotionEvent.ACTION_MOVE: {
            if (this.beingDragged) {
                this.mLastMotionY = event.getY();
                this.mLastMotionX = event.getX();
                this.pullEvent();
                return true;
            }
            break;
        }

        case MotionEvent.ACTION_DOWN: {
            if (this.isReadyForPull()) {
                this.mLastMotionY = this.mInitialMotionY = event.getY();
                this.mLastMotionX = this.mInitialMotionX = event.getX();
                return true;
            }
            break;
        }

        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP: {
            if (this.beingDragged) {
                this.beingDragged = false;

                if ((this.mState == State.RELEASE_TO_REFRESH)
                        && ((null != this.mOnRefreshListener) || (null != this.mOnRefreshListener2))) {
                    this.setState(State.REFRESHING, true);
                    return true;
                }

                // If we're already refreshing, just scroll back to the top
                if (this.isRefreshing()) {
                    this.smoothScrollTo(0);
                    return true;
                }

                // If we haven't returned by here, then we're not in a state
                // to pull, so just reset
                this.setState(State.RESET);

                return true;
            }
            break;
        }
        default:
            break;
        }

        return false;
    }

    @Override
    public final void setFilterTouchEvents(final boolean filterEvents) {

        this.filterTouchEvents = filterEvents;
    }

    @Override
    public void setLongClickable(final boolean longClickable) {

        this.getRefreshableView().setLongClickable(longClickable);
    }

    @Override
    public final void setMode(final Mode mode) {

        if (mode != this.mMode) {
            if (DEBUG) {
                Log.d(LOG_TAG, "Setting mode to: " + mode);
            }
            this.mMode = mode;
            this.updateUIForMode();
        }
    }

    @Override
    public void setOnPullEventListener(final OnPullEventListener<T> listener) {

        this.mOnPullEventListener = listener;
    }

    @Override
    public final void setOnRefreshListener(final OnRefreshListener<T> listener) {

        this.mOnRefreshListener = listener;
        this.mOnRefreshListener2 = null;
    }

    @Override
    public final void setOnRefreshListener(final OnRefreshListener2<T> listener) {

        this.mOnRefreshListener2 = listener;
        this.mOnRefreshListener = null;
    }

    @Override
    public final void setPullToRefreshOverScrollEnabled(final boolean enabled) {

        this.overScrollEnabled = enabled;
    }

    @Override
    public final void setRefreshing() {

        this.setRefreshing(true);
    }

    @Override
    public final void setRefreshing(final boolean doScroll) {

        if (!this.isRefreshing()) {
            this.setState(State.MANUAL_REFRESHING, doScroll);
        }
    }

    @Override
    public void setScrollAnimationInterpolator(final Interpolator interpolator) {

        this.scrollAnimationInterpolator = interpolator;
    }

    @Override
    public final void setScrollingWhileRefreshingEnabled(final boolean allowScrollingWhileRefreshing) {

        this.scrollingWhileRefreshingEnabled = allowScrollingWhileRefreshing;
    }

    @Override
    public final void setShowViewWhileRefreshing(final boolean showView) {

        this.showViewWhileRefreshing = showView;
    }

    private void addRefreshableView(final Context context, final T refreshableView) {

        this.mRefreshableViewWrapper = new FrameLayout(context);
        this.mRefreshableViewWrapper.addView(refreshableView, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        this.addViewInternal(this.mRefreshableViewWrapper, new LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private LinearLayout.LayoutParams getLoadingLayoutLayoutParams() {

        switch (this.getPullToRefreshScrollDirection()) {
        case HORIZONTAL:
            return new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        case VERTICAL:
        default:
            return new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private int getMaximumPullScroll() {

        switch (this.getPullToRefreshScrollDirection()) {
        case HORIZONTAL:
            return Math.round(this.getWidth() / FRICTION);
        case VERTICAL:
        default:
            return Math.round(this.getHeight() / FRICTION);
        }
    }

    private void init(final Context context, final AttributeSet attrs) {

        switch (this.getPullToRefreshScrollDirection()) {
        case HORIZONTAL:
            this.setOrientation(LinearLayout.HORIZONTAL);
            break;
        case VERTICAL:
        default:
            this.setOrientation(LinearLayout.VERTICAL);
            break;
        }

        this.setGravity(Gravity.CENTER);

        final ViewConfiguration config = ViewConfiguration.get(context);
        this.mTouchSlop = config.getScaledTouchSlop();

        // Styleables from XML
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PullToRefresh);

        if (a.hasValue(R.styleable.PullToRefresh_ptrMode)) {
            this.mMode = Mode.mapIntToValue(a.getInteger(R.styleable.PullToRefresh_ptrMode, 0));
        }

        if (a.hasValue(R.styleable.PullToRefresh_ptrAnimationStyle)) {
            this.mLoadingAnimationStyle = AnimationStyle.mapIntToValue(a.getInteger(
                    R.styleable.PullToRefresh_ptrAnimationStyle, 0));
        }

        // Refreshable View
        // By passing the attrs, we can add ListView/GridView params via XML
        this.mRefreshableView = this.createRefreshableView(context, attrs);
        this.addRefreshableView(context, this.mRefreshableView);

        // We need to create now layouts now
        this.mHeaderLayout = this.createLoadingLayout(context, Mode.PULL_FROM_START, a);
        this.mFooterLayout = this.createLoadingLayout(context, Mode.PULL_FROM_END, a);

        /**
         * Styleables from XML
         */
        if (a.hasValue(R.styleable.PullToRefresh_ptrRefreshableViewBackground)) {
            final Drawable background = a.getDrawable(R.styleable.PullToRefresh_ptrRefreshableViewBackground);
            if (null != background) {
                this.mRefreshableView.setBackground(background);
            }
        } else if (a.hasValue(R.styleable.PullToRefresh_ptrAdapterViewBackground)) {
            Utils.warnDeprecation("ptrAdapterViewBackground", "ptrRefreshableViewBackground");
            final Drawable background = a.getDrawable(R.styleable.PullToRefresh_ptrAdapterViewBackground);
            if (null != background) {
                this.mRefreshableView.setBackground(background);
            }
        }

        if (a.hasValue(R.styleable.PullToRefresh_ptrOverScroll)) {
            this.overScrollEnabled = a.getBoolean(R.styleable.PullToRefresh_ptrOverScroll, true);
        }

        if (a.hasValue(R.styleable.PullToRefresh_ptrScrollingWhileRefreshingEnabled)) {
            this.scrollingWhileRefreshingEnabled = a.getBoolean(
                    R.styleable.PullToRefresh_ptrScrollingWhileRefreshingEnabled, false);
        }

        // Let the derivative classes have a go at handling attributes, then
        // recycle them...
        this.handleStyledAttributes(a);
        a.recycle();

        // Finally update the UI for the modes
        this.updateUIForMode();
    }

    private boolean isReadyForPull() {

        switch (this.mMode) {
        case PULL_FROM_START:
            return this.isReadyForPullStart();
        case PULL_FROM_END:
            return this.isReadyForPullEnd();
        case BOTH:
            return this.isReadyForPullEnd() || this.isReadyForPullStart();
        default:
            return false;
        }
    }

    /**
     * Actions a Pull Event
     *
     * @return true if the Event has been handled, false if there has been no change
     */
    private void pullEvent() {

        final int newScrollValue;
        final int itemDimension;
        final float initialMotionValue, lastMotionValue;

        switch (this.getPullToRefreshScrollDirection()) {
        case HORIZONTAL:
            initialMotionValue = this.mInitialMotionX;
            lastMotionValue = this.mLastMotionX;
            break;
        case VERTICAL:
        default:
            initialMotionValue = this.mInitialMotionY;
            lastMotionValue = this.mLastMotionY;
            break;
        }

        switch (this.mCurrentMode) {
        case PULL_FROM_END:
            newScrollValue = Math.round(Math.max(initialMotionValue - lastMotionValue, 0) / FRICTION);
            itemDimension = this.getFooterSize();
            break;
        case PULL_FROM_START:
        default:
            newScrollValue = Math.round(Math.min(initialMotionValue - lastMotionValue, 0) / FRICTION);
            itemDimension = this.getHeaderSize();
            break;
        }

        this.setHeaderScroll(newScrollValue);

        if ((newScrollValue != 0) && !this.isRefreshing()) {
            final float scale = Math.abs(newScrollValue) / (float) itemDimension;
            switch (this.mCurrentMode) {
            case PULL_FROM_END:
                this.mFooterLayout.onPull(scale);
                break;
            case PULL_FROM_START:
            default:
                this.mHeaderLayout.onPull(scale);
                break;
            }

            if ((this.mState != State.PULL_TO_REFRESH) && (itemDimension >= Math.abs(newScrollValue))) {
                this.setState(State.PULL_TO_REFRESH);
            } else if ((this.mState == State.PULL_TO_REFRESH) && (itemDimension < Math.abs(newScrollValue))) {
                this.setState(State.RELEASE_TO_REFRESH);
            }
        }
    }

    /**
     * Smooth Scroll to position using the specific duration
     *
     * @param scrollValue - Position to scroll to
     * @param duration - Duration of animation in milliseconds
     */
    private final void smoothScrollTo(final int scrollValue, final long duration) {

        this.smoothScrollTo(scrollValue, duration, 0, null);
    }

    private final void smoothScrollToAndBack(final int y) {

        this.smoothScrollTo(y, SMOOTH_SCROLL_DURATION_MS, 0, new OnSmoothScrollFinishedListener() {

            @Override
            public void onSmoothScrollFinished() {

                PullToRefreshBase.this.smoothScrollTo(0, SMOOTH_SCROLL_DURATION_MS, DEMO_SCROLL_INTERVAL, null);
            }
        });
    }

    /**
     * Used internally for adding view. Need because we override addView to pass-through to the Refreshable View
     */
    protected final void addViewInternal(final View child, final int index, final ViewGroup.LayoutParams params) {

        super.addView(child, index, params);
    }

    /**
     * Used internally for adding view. Need because we override addView to pass-through to the Refreshable View
     */
    protected final void addViewInternal(final View child, final ViewGroup.LayoutParams params) {

        super.addView(child, -1, params);
    }

    protected LoadingLayout createLoadingLayout(final Context context, final Mode mode, final TypedArray attrs) {

        final LoadingLayout layout = this.mLoadingAnimationStyle.createLoadingLayout(context, mode,
                this.getPullToRefreshScrollDirection(), attrs);
        layout.setVisibility(View.INVISIBLE);
        return layout;
    }

    /**
     * Used internally for {@link #getLoadingLayoutProxy(boolean, boolean)}. Allows derivative classes to include any
     * extra LoadingLayouts.
     */
    protected LoadingLayoutProxy createLoadingLayoutProxy(final boolean includeStart, final boolean includeEnd) {

        final LoadingLayoutProxy proxy = new LoadingLayoutProxy();

        if (includeStart && this.mMode.showHeaderLoadingLayout()) {
            proxy.addLayout(this.mHeaderLayout);
        }
        if (includeEnd && this.mMode.showFooterLoadingLayout()) {
            proxy.addLayout(this.mFooterLayout);
        }

        return proxy;
    }

    /**
     * This is implemented by derived classes to return the created View. If you need to use a custom View (such as a
     * custom ListView), override this method and return an instance of your custom class.
     * <p/>
     * Be sure to set the ID of the view in this method, especially if you're using a ListActivity or ListFragment.
     *
     * @param context Context to create view with
     * @param attrs AttributeSet from wrapped class. Means that anything you include in the XML layout declaration will
     *            be routed to the created View
     * @return New instance of the Refreshable View
     */
    protected abstract T createRefreshableView(Context context, AttributeSet attrs);

    protected final void disableLoadingLayoutVisibilityChanges() {

        this.layoutVisibilityChangesEnabled = false;
    }

    protected final LoadingLayout getFooterLayout() {

        return this.mFooterLayout;
    }

    protected final int getFooterSize() {

        return this.mFooterLayout.getContentSize();
    }

    protected final LoadingLayout getHeaderLayout() {

        return this.mHeaderLayout;
    }

    protected final int getHeaderSize() {

        return this.mHeaderLayout.getContentSize();
    }

    protected int getPullToRefreshScrollDuration() {

        return SMOOTH_SCROLL_DURATION_MS;
    }

    protected int getPullToRefreshScrollDurationLonger() {

        return SMOOTH_SCROLL_LONG_DURATION_MS;
    }

    protected FrameLayout getRefreshableViewWrapper() {

        return this.mRefreshableViewWrapper;
    }

    /**
     * Allows Derivative classes to handle the XML Attrs without creating a TypedArray themsevles
     *
     * @param a - TypedArray of PullToRefresh Attributes
     */
    protected void handleStyledAttributes(final TypedArray a) {

    }

    /**
     * Implemented by derived class to return whether the View is in a state where the user can Pull to Refresh by
     * scrolling from the end.
     *
     * @return true if the View is currently in the correct state (for example, bottom of a ListView)
     */
    protected abstract boolean isReadyForPullEnd();

    /**
     * Implemented by derived class to return whether the View is in a state where the user can Pull to Refresh by
     * scrolling from the start.
     *
     * @return true if the View is currently the correct state (for example, top of a ListView)
     */
    protected abstract boolean isReadyForPullStart();

    /**
     * Called by {@link #onRestoreInstanceState(Parcelable)} so that derivative classes can handle their saved instance
     * state.
     *
     * @param savedInstanceState - Bundle which contains saved instance state.
     */
    protected void onPtrRestoreInstanceState(final Bundle savedInstanceState) {

    }

    /**
     * Called by {@link #onSaveInstanceState()} so that derivative classes can save their instance state.
     *
     * @param saveState - Bundle to be updated with saved state.
     */
    protected void onPtrSaveInstanceState(final Bundle saveState) {

    }

    /**
     * Called when the UI has been to be updated to be in the {@link State#PULL_TO_REFRESH} state.
     */
    protected void onPullToRefresh() {

        switch (this.mCurrentMode) {
        case PULL_FROM_END:
            this.mFooterLayout.pullToRefresh();
            break;
        case PULL_FROM_START:
            this.mHeaderLayout.pullToRefresh();
            break;
        default:
            // NO-OP
            break;
        }
    }

    /**
     * Called when the UI has been to be updated to be in the {@link State#REFRESHING} or
     * {@link State#MANUAL_REFRESHING} state.
     *
     * @param doScroll - Whether the UI should scroll for this event.
     */
    protected void onRefreshing(final boolean doScroll) {

        if (this.mMode.showHeaderLoadingLayout()) {
            this.mHeaderLayout.refreshing();
        }
        if (this.mMode.showFooterLoadingLayout()) {
            this.mFooterLayout.refreshing();
        }

        if (doScroll) {
            if (this.showViewWhileRefreshing) {

                // Call Refresh Listener when the Scroll has finished
                final OnSmoothScrollFinishedListener listener = new OnSmoothScrollFinishedListener() {

                    @Override
                    public void onSmoothScrollFinished() {

                        PullToRefreshBase.this.callRefreshListener();
                    }
                };

                switch (this.mCurrentMode) {
                case MANUAL_REFRESH_ONLY:
                case PULL_FROM_END:
                    this.smoothScrollTo(this.getFooterSize(), listener);
                    break;
                default:
                case PULL_FROM_START:
                    this.smoothScrollTo(-this.getHeaderSize(), listener);
                    break;
                }
            } else {
                this.smoothScrollTo(0);
            }
        } else {
            // We're not scrolling, so just call Refresh Listener now
            this.callRefreshListener();
        }
    }

    /**
     * Called when the UI has been to be updated to be in the {@link State#RELEASE_TO_REFRESH} state.
     */
    protected void onReleaseToRefresh() {

        switch (this.mCurrentMode) {
        case PULL_FROM_END:
            this.mFooterLayout.releaseToRefresh();
            break;
        case PULL_FROM_START:
            this.mHeaderLayout.releaseToRefresh();
            break;
        default:
            // NO-OP
            break;
        }
    }

    /**
     * Called when the UI has been to be updated to be in the {@link State#RESET} state.
     */
    protected void onReset() {

        this.beingDragged = false;
        this.layoutVisibilityChangesEnabled = true;

        // Always reset both layouts, just in case...
        this.mHeaderLayout.reset();
        this.mFooterLayout.reset();

        this.smoothScrollTo(0);
    }

    @Override
    protected final void onRestoreInstanceState(final Parcelable state) {

        if (state instanceof Bundle) {
            final Bundle bundle = (Bundle) state;

            this.setMode(Mode.mapIntToValue(bundle.getInt(STATE_MODE, 0)));
            this.mCurrentMode = Mode.mapIntToValue(bundle.getInt(STATE_CURRENT_MODE, 0));

            this.scrollingWhileRefreshingEnabled = bundle.getBoolean(STATE_SCROLLING_REFRESHING_ENABLED, false);
            this.showViewWhileRefreshing = bundle.getBoolean(STATE_SHOW_REFRESHING_VIEW, true);

            // Let super Restore Itself
            super.onRestoreInstanceState(bundle.getParcelable(STATE_SUPER));

            final State viewState = State.mapIntToValue(bundle.getInt(STATE_STATE, 0));
            if ((viewState == State.REFRESHING) || (viewState == State.MANUAL_REFRESHING)) {
                this.setState(viewState, true);
            }

            // Now let derivative classes restore their state
            this.onPtrRestoreInstanceState(bundle);
            return;
        }

        super.onRestoreInstanceState(state);
    }

    @Override
    protected final Parcelable onSaveInstanceState() {

        final Bundle bundle = new Bundle();

        // Let derivative classes get a chance to save state first, that way we
        // can make sure they don't overrite any of our values
        this.onPtrSaveInstanceState(bundle);

        bundle.putInt(STATE_STATE, this.mState.getIntValue());
        bundle.putInt(STATE_MODE, this.mMode.getIntValue());
        bundle.putInt(STATE_CURRENT_MODE, this.mCurrentMode.getIntValue());
        bundle.putBoolean(STATE_SCROLLING_REFRESHING_ENABLED, this.scrollingWhileRefreshingEnabled);
        bundle.putBoolean(STATE_SHOW_REFRESHING_VIEW, this.showViewWhileRefreshing);
        bundle.putParcelable(STATE_SUPER, super.onSaveInstanceState());

        return bundle;
    }

    @Override
    protected final void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {

        if (DEBUG) {
            Log.d(LOG_TAG, String.format("onSizeChanged. W: %d, H: %d", Integer.valueOf(w), Integer.valueOf(h)));
        }

        super.onSizeChanged(w, h, oldw, oldh);

        // We need to update the header/footer when our size changes
        this.refreshLoadingViewsSize();

        // Update the Refreshable View layout
        this.refreshRefreshableViewSize(w, h);

        /**
         * As we're currently in a Layout Pass, we need to schedule another one to layout any changes we've made here
         */
        this.post(new Runnable() {

            @Override
            public void run() {

                PullToRefreshBase.this.requestLayout();
            }
        });
    }

    /**
     * Re-measure the Loading Views height, and adjust internal padding as necessary
     */
    protected final void refreshLoadingViewsSize() {

        final int maximumPullScroll = (int) (this.getMaximumPullScroll() * 1.2f);

        int pLeft = this.getPaddingLeft();
        int pTop = this.getPaddingTop();
        int pRight = this.getPaddingRight();
        int pBottom = this.getPaddingBottom();

        if (this.getPullToRefreshScrollDirection() == Orientation.HORIZONTAL) {
            if (this.mMode.showHeaderLoadingLayout()) {
                this.mHeaderLayout.setWidth(maximumPullScroll);
                pLeft = -maximumPullScroll;
            } else {
                pLeft = 0;
            }

            if (this.mMode.showFooterLoadingLayout()) {
                this.mFooterLayout.setWidth(maximumPullScroll);
                pRight = -maximumPullScroll;
            } else {
                pRight = 0;
            }

        } else {
            if (this.mMode.showHeaderLoadingLayout()) {
                this.mHeaderLayout.setHeight(maximumPullScroll);
                pTop = -maximumPullScroll;
            } else {
                pTop = 0;
            }

            if (this.mMode.showFooterLoadingLayout()) {
                this.mFooterLayout.setHeight(maximumPullScroll);
                pBottom = -maximumPullScroll;
            } else {
                pBottom = 0;
            }
        }

        if (DEBUG) {
            Log.d(LOG_TAG,
                    String.format("Setting Padding. L: %d, T: %d, R: %d, B: %d", Integer.valueOf(pLeft),
                            Integer.valueOf(pTop), Integer.valueOf(pRight), Integer.valueOf(pBottom)));
        }
        this.setPadding(pLeft, pTop, pRight, pBottom);
    }

    protected final void refreshRefreshableViewSize(final int width, final int height) {

        // We need to set the Height of the Refreshable View to the same as
        // this layout
        final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) this.mRefreshableViewWrapper.getLayoutParams();

        if (this.getPullToRefreshScrollDirection() == Orientation.HORIZONTAL) {
            if (lp.width != width) {
                lp.width = width;
                this.mRefreshableViewWrapper.requestLayout();
            }
        } else {
            if (lp.height != height) {
                lp.height = height;
                this.mRefreshableViewWrapper.requestLayout();
            }
        }
    }

    /**
     * Helper method which just calls scrollTo() in the correct scrolling direction.
     *
     * @param value - New Scroll value
     */
    protected final void setHeaderScroll(int value) {

        if (DEBUG) {
            Log.d(LOG_TAG, "setHeaderScroll: " + value);
        }

        // Clamp value to with pull scroll range
        final int maximumPullScroll = this.getMaximumPullScroll();
        value = Math.min(maximumPullScroll, Math.max(-maximumPullScroll, value));

        if (this.layoutVisibilityChangesEnabled) {
            if (value < 0) {
                this.mHeaderLayout.setVisibility(View.VISIBLE);
            } else if (value > 0) {
                this.mFooterLayout.setVisibility(View.VISIBLE);
            } else {
                this.mHeaderLayout.setVisibility(View.INVISIBLE);
                this.mFooterLayout.setVisibility(View.INVISIBLE);
            }
        }

        if (USE_HW_LAYERS) {
            /**
             * Use a Hardware Layer on the Refreshable View if we've scrolled at all. We don't use them on the
             * Header/Footer Views as they change often, which would negate any HW layer performance boost.
             */
            ViewCompat.setLayerType(this.mRefreshableViewWrapper, value != 0 ? View.LAYER_TYPE_HARDWARE
                    : View.LAYER_TYPE_NONE);
        }

        if (this.getPullToRefreshScrollDirection() == Orientation.HORIZONTAL) {
            this.scrollTo(value, 0);
        } else {
            this.scrollTo(0, value);
        }
    }

    /**
     * Smooth Scroll to position using the default duration of {@value #SMOOTH_SCROLL_DURATION_MS} ms.
     *
     * @param scrollValue - Position to scroll to
     */
    protected final void smoothScrollTo(final int scrollValue) {

        this.smoothScrollTo(scrollValue, this.getPullToRefreshScrollDuration());
    }

    /**
     * Smooth Scroll to position using the default duration of {@value #SMOOTH_SCROLL_DURATION_MS} ms.
     *
     * @param scrollValue - Position to scroll to
     * @param listener - Listener for scroll
     */
    protected final void smoothScrollTo(final int scrollValue, final OnSmoothScrollFinishedListener listener) {

        this.smoothScrollTo(scrollValue, this.getPullToRefreshScrollDuration(), 0, listener);
    }

    /**
     * Smooth Scroll to position using the longer default duration of {@value #SMOOTH_SCROLL_LONG_DURATION_MS} ms.
     *
     * @param scrollValue - Position to scroll to
     */
    protected final void smoothScrollToLonger(final int scrollValue) {

        this.smoothScrollTo(scrollValue, this.getPullToRefreshScrollDurationLonger());
    }

    /**
     * Updates the View State when the mode has been set. This does not do any checking that the mode is different to
     * current state so always updates.
     */
    protected void updateUIForMode() {

        // We need to use the correct LayoutParam values, based on scroll
        // direction
        final LinearLayout.LayoutParams lp = this.getLoadingLayoutLayoutParams();

        // Remove Header, and then add Header Loading View again if needed
        if (this == this.mHeaderLayout.getParent()) {
            this.removeView(this.mHeaderLayout);
        }
        if (this.mMode.showHeaderLoadingLayout()) {
            this.addViewInternal(this.mHeaderLayout, 0, lp);
        }

        // Remove Footer, and then add Footer Loading View again if needed
        if (this == this.mFooterLayout.getParent()) {
            this.removeView(this.mFooterLayout);
        }
        if (this.mMode.showFooterLoadingLayout()) {
            this.addViewInternal(this.mFooterLayout, lp);
        }

        // Hide Loading Views
        this.refreshLoadingViewsSize();

        // If we're not using Mode.BOTH, set mCurrentMode to mMode, otherwise
        // set it to pull down
        this.mCurrentMode = (this.mMode != Mode.BOTH) ? this.mMode : Mode.PULL_FROM_START;
    }

    void callRefreshListener() {

        if (null != this.mOnRefreshListener) {
            this.mOnRefreshListener.onRefresh(this);
        } else if (null != this.mOnRefreshListener2) {
            if (this.mCurrentMode == Mode.PULL_FROM_START) {
                this.mOnRefreshListener2.onPullDownToRefresh(this);
            } else if (this.mCurrentMode == Mode.PULL_FROM_END) {
                this.mOnRefreshListener2.onPullUpToRefresh(this);
            }
        }
    }

    final void setState(final State state, final boolean... params) {

        this.mState = state;
        if (DEBUG) {
            Log.d(LOG_TAG, "State: " + this.mState.name());
        }

        switch (this.mState) {
        case RESET:
            this.onReset();
            break;
        case PULL_TO_REFRESH:
            this.onPullToRefresh();
            break;
        case RELEASE_TO_REFRESH:
            this.onReleaseToRefresh();
            break;
        case REFRESHING:
        case MANUAL_REFRESHING:
            this.onRefreshing(params[0]);
            break;
        case OVERSCROLLING:
            // NO-OP
            break;
        default:
            break;
        }

        // Call OnPullEventListener
        if (null != this.mOnPullEventListener) {
            this.mOnPullEventListener.onPullEvent(this, this.mState, this.mCurrentMode);
        }
    }

    final void smoothScrollTo(final int newScrollValue, final long duration, final long delayMillis,
            final OnSmoothScrollFinishedListener listener) {

        if (null != this.mCurrentSmoothScrollRunnable) {
            this.mCurrentSmoothScrollRunnable.stop();
        }

        final int oldScrollValue;
        if (this.getPullToRefreshScrollDirection() == Orientation.HORIZONTAL) {
            oldScrollValue = this.getScrollX();
        } else {
            oldScrollValue = this.getScrollY();
        }

        if (oldScrollValue != newScrollValue) {
            if (null == this.scrollAnimationInterpolator) {
                // Default interpolator is a Decelerate Interpolator
                this.scrollAnimationInterpolator = new DecelerateInterpolator();
            }
            this.mCurrentSmoothScrollRunnable = new SmoothScrollRunnable(oldScrollValue, newScrollValue, duration,
                    listener);

            if (delayMillis > 0) {
                this.postDelayed(this.mCurrentSmoothScrollRunnable, delayMillis);
            } else {
                this.post(this.mCurrentSmoothScrollRunnable);
            }
        }
    }

    public static enum AnimationStyle {
        /**
         * This is the default for Android-PullToRefresh. Allows you to use any drawable, which is automatically rotated
         * and used as a Progress Bar.
         */
        ROTATE,

        /**
         * This is the old default, and what is commonly used on iOS. Uses an arrow image which flips depending on where
         * the user has scrolled.
         */
        FLIP;

        static AnimationStyle getDefault() {

            return ROTATE;
        }

        /**
         * Maps an int to a specific mode. This is needed when saving state, or inflating the view from XML where the
         * mode is given through a attr int.
         *
         * @param modeInt - int to map a Mode to
         * @return Mode that modeInt maps to, or ROTATE by default.
         */
        static AnimationStyle mapIntToValue(final int modeInt) {

            switch (modeInt) {
            case 0x0:
            default:
                return ROTATE;
            case 0x1:
                return FLIP;
            }
        }

        LoadingLayout createLoadingLayout(final Context context, final Mode mode, final Orientation scrollDirection,
                final TypedArray attrs) {

            switch (this) {
            case ROTATE:
            default:
                return new RotateLoadingLayout(context, mode, scrollDirection, attrs);
            case FLIP:
                return new FlipLoadingLayout(context, mode, scrollDirection, attrs);
            }
        }
    }

    public static enum Mode {

        /**
         * Disable all Pull-to-Refresh gesture and Refreshing handling
         */
        DISABLED(0x0),

        /**
         * Only allow the user to Pull from the start of the Refreshable View to refresh. The start is either the Top or
         * Left, depending on the scrolling direction.
         */
        PULL_FROM_START(0x1),

        /**
         * Only allow the user to Pull from the end of the Refreshable View to refresh. The start is either the Bottom
         * or Right, depending on the scrolling direction.
         */
        PULL_FROM_END(0x2),

        /**
         * Allow the user to both Pull from the start, from the end to refresh.
         */
        BOTH(0x3),

        /**
         * Disables Pull-to-Refresh gesture handling, but allows manually setting the Refresh state via
         * {@link PullToRefreshBase#setRefreshing() setRefreshing()}.
         */
        MANUAL_REFRESH_ONLY(0x4);

        private int mIntValue;

        // The modeInt values need to match those from attrs.xml
        Mode(final int modeInt) {

            this.mIntValue = modeInt;
        }

        static Mode getDefault() {

            return PULL_FROM_START;
        }

        /**
         * Maps an int to a specific mode. This is needed when saving state, or inflating the view from XML where the
         * mode is given through a attr int.
         *
         * @param modeInt - int to map a Mode to
         * @return Mode that modeInt maps to, or PULL_FROM_START by default.
         */
        static Mode mapIntToValue(final int modeInt) {

            for (final Mode value : Mode.values()) {
                if (modeInt == value.getIntValue()) {
                    return value;
                }
            }

            // If not, return default
            return getDefault();
        }

        /**
         * @return true if this mode wants the Loading Layout Footer to be shown
         */
        public boolean showFooterLoadingLayout() {

            return (this == PULL_FROM_END) || (this == BOTH) || (this == MANUAL_REFRESH_ONLY);
        }

        /**
         * @return true if this mode wants the Loading Layout Header to be shown
         */
        public boolean showHeaderLoadingLayout() {

            return (this == PULL_FROM_START) || (this == BOTH);
        }

        int getIntValue() {

            return this.mIntValue;
        }

        /**
         * @return true if the mode permits Pull-to-Refresh
         */
        boolean permitsPullToRefresh() {

            return !((this == DISABLED) || (this == MANUAL_REFRESH_ONLY));
        }

    }

    // ===========================================================
    // Inner, Anonymous Classes, and Enumerations
    // ===========================================================

    /**
     * Simple Listener that allows you to be notified when the user has scrolled to the end of the AdapterView. See (
     * {@link PullToRefreshAdapterViewBase#setOnLastItemVisibleListener}.
     *
     * @author Chris Banes
     */
    public static interface OnLastItemVisibleListener {

        /**
         * Called when the user has scrolled to the end of the list
         */
        void onLastItemVisible();

    }

    /**
     * Listener that allows you to be notified when the user has started or finished a touch event. Useful when you want
     * to append extra UI events (such as sounds). See ( {@link PullToRefreshAdapterViewBase#setOnPullEventListener}.
     *
     * @author Chris Banes
     */
    public static interface OnPullEventListener<V extends View> {

        /**
         * Called when the internal state has been changed, usually by the user pulling.
         *
         * @param refreshView - View which has had it's state change.
         * @param state - The new state of View.
         * @param direction - One of {@link Mode#PULL_FROM_START} or {@link Mode#PULL_FROM_END} depending on which
         *            direction the user is pulling. Only useful when <var>state</var> is {@link State#PULL_TO_REFRESH}
         *            or {@link State#RELEASE_TO_REFRESH}.
         */
        void onPullEvent(final PullToRefreshBase<V> refreshView, State state, Mode direction);

    }

    /**
     * Simple Listener to listen for any callbacks to Refresh.
     *
     * @author Chris Banes
     */
    public static interface OnRefreshListener<V extends View> {

        /**
         * onRefresh will be called for both a Pull from start, and Pull from end
         */
        void onRefresh(final PullToRefreshBase<V> refreshView);

    }

    /**
     * An advanced version of the Listener to listen for callbacks to Refresh. This listener is different as it allows
     * you to differentiate between Pull Ups, and Pull Downs.
     *
     * @author Chris Banes
     */
    public static interface OnRefreshListener2<V extends View> {

        // TODO These methods need renaming to START/END rather than DOWN/UP

        /**
         * onPullDownToRefresh will be called only when the user has Pulled from the start, and released.
         */
        void onPullDownToRefresh(final PullToRefreshBase<V> refreshView);

        /**
         * onPullUpToRefresh will be called only when the user has Pulled from the end, and released.
         */
        void onPullUpToRefresh(final PullToRefreshBase<V> refreshView);

    }

    public static enum Orientation {
        VERTICAL, HORIZONTAL;
    }

    public static enum State {

        /**
         * When the UI is in a state which means that user is not interacting with the Pull-to-Refresh function.
         */
        RESET(0x0),

        /**
         * When the UI is being pulled by the user, but has not been pulled far enough so that it refreshes when
         * released.
         */
        PULL_TO_REFRESH(0x1),

        /**
         * When the UI is being pulled by the user, and <strong>has</strong> been pulled far enough so that it will
         * refresh when released.
         */
        RELEASE_TO_REFRESH(0x2),

        /**
         * When the UI is currently refreshing, caused by a pull gesture.
         */
        REFRESHING(0x8),

        /**
         * When the UI is currently refreshing, caused by a call to {@link PullToRefreshBase#setRefreshing()
         * setRefreshing()}.
         */
        MANUAL_REFRESHING(0x9),

        /**
         * When the UI is currently overscrolling, caused by a fling on the Refreshable View.
         */
        OVERSCROLLING(0x10);

        private int mIntValue;

        State(final int intValue) {

            this.mIntValue = intValue;
        }

        /**
         * Maps an int to a specific state. This is needed when saving state.
         *
         * @param stateInt - int to map a State to
         * @return State that stateInt maps to
         */
        static State mapIntToValue(final int stateInt) {

            for (final State value : State.values()) {
                if (stateInt == value.getIntValue()) {
                    return value;
                }
            }

            // If not, return default
            return RESET;
        }

        int getIntValue() {

            return this.mIntValue;
        }
    }

    static interface OnSmoothScrollFinishedListener {

        void onSmoothScrollFinished();
    }

    final class SmoothScrollRunnable implements Runnable {

        private final Interpolator mInterpolator;
        private final int mScrollToY;
        private final int mScrollFromY;
        private final long mDuration;
        private final OnSmoothScrollFinishedListener mListener;

        private boolean mContinueRunning = true;
        private long mStartTime = -1;
        private int mCurrentY = -1;

        public SmoothScrollRunnable(final int fromY, final int toY, final long duration,
                final OnSmoothScrollFinishedListener listener) {

            this.mScrollFromY = fromY;
            this.mScrollToY = toY;
            this.mInterpolator = PullToRefreshBase.this.scrollAnimationInterpolator;
            this.mDuration = duration;
            this.mListener = listener;
        }

        @Override
        public void run() {

            /**
             * Only set mStartTime if this is the first time we're starting, else actually calculate the Y delta
             */
            if (this.mStartTime == -1) {
                this.mStartTime = System.currentTimeMillis();
            } else {

                /**
                 * We do do all calculations in long to reduce software float calculations. We use 1000 as it gives us
                 * good accuracy and small rounding errors
                 */
                long normalizedTime = (1000 * (System.currentTimeMillis() - this.mStartTime)) / this.mDuration;
                normalizedTime = Math.max(Math.min(normalizedTime, 1000), 0);

                final int deltaY = Math.round((this.mScrollFromY - this.mScrollToY)
                        * this.mInterpolator.getInterpolation(normalizedTime / 1000f));
                this.mCurrentY = this.mScrollFromY - deltaY;
                PullToRefreshBase.this.setHeaderScroll(this.mCurrentY);
            }

            // If we're not at the target Y, keep going...
            if (this.mContinueRunning && (this.mScrollToY != this.mCurrentY)) {
                ViewCompat.postOnAnimation(PullToRefreshBase.this, this);
            } else {
                if (null != this.mListener) {
                    this.mListener.onSmoothScrollFinished();
                }
            }
        }

        public void stop() {

            this.mContinueRunning = false;
            PullToRefreshBase.this.removeCallbacks(this);
        }
    }

}
