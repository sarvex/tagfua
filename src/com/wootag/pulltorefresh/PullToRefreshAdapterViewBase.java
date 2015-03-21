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
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

import com.wTagFuR;
import com.woTagFuulltorefresh.internal.EmptyViewMethodAccessor;
import com.wootag.pulltorefresh.internal.IndicatorLayout;

public abstract class PullToRefreshAdapterViewBase<T extends AbsListView> extends PullToRefreshBase<T> implements
        OnScrollListener {

    private boolean mLastItemVisible;

    private OnScrollListener mOnScrollListener;
    private OnLastItemVisibleListener mOnLastItemVisibleListener;
    private View mEmptyView;
    private IndicatorLayout mIndicatorIvTop;

    private IndicatorLayout mIndicatorIvBottom;
    private boolean mShowIndicator;

    private boolean mScrollEmptyView = true;

    public PullToRefreshAdapterViewBase(final Context context) {

        super(context);
        this.mRefreshableView.setOnScrollListener(this);
    }

    public PullToRefreshAdapterViewBase(final Context context, final AttributeSet attrs) {

        super(context, attrs);
        this.mRefreshableView.setOnScrollListener(this);
    }

    public PullToRefreshAdapterViewBase(final Context context, final Mode mode) {

        super(context, mode);
        this.mRefreshableView.setOnScrollListener(this);
    }

    public PullToRefreshAdapterViewBase(final Context context, final Mode mode, final AnimationStyle animStyle) {

        super(context, mode, animStyle);
        this.mRefreshableView.setOnScrollListener(this);
    }

    private static FrameLayout.LayoutParams convertEmptyViewLayoutParams(final ViewGroup.LayoutParams lp) {

        FrameLayout.LayoutParams newLp = null;

        if (null != lp) {
            newLp = new FrameLayout.LayoutParams(lp);

            if (lp instanceof LinearLayout.LayoutParams) {
                newLp.gravity = ((LinearLayout.LayoutParams) lp).gravity;
            } else {
                newLp.gravity = Gravity.CENTER;
            }
        }

        return newLp;
    }

    /**
     * Gets whether an indicator graphic should be displayed when the View is in a state where a Pull-to-Refresh can
     * happen. An example of this state is when the Adapter View is scrolled to the top and the mode is set to
     * {@link Mode#PULL_FROM_START}. The default value is <var>true</var> if
     * {@link PullToRefreshBase#isPullToRefreshOverScrollEnabled() isPullToRefreshOverScrollEnabled()} returns false.
     *
     * @return true if the indicators will be shown
     */
    public boolean getShowIndicator() {

        return this.mShowIndicator;
    }

    @Override
    public final void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount,
            final int totalItemCount) {

        if (DEBUG) {
            Log.d(LOG_TAG, "First Visible: " + firstVisibleItem + ". Visible Count: " + visibleItemCount
                    + ". Total Items:" + totalItemCount);
        }

        /**
         * Set whether the Last Item is Visible. lastVisibleItemIndex is a zero-based index, so we minus one
         * totalItemCount to check
         */
        if (null != this.mOnLastItemVisibleListener) {
            this.mLastItemVisible = (totalItemCount > 0)
                    && ((firstVisibleItem + visibleItemCount) >= (totalItemCount - 1));
        }

        // If we're showing the indicator, check positions...
        if (this.getShowIndicatorInternal()) {
            this.updateIndicatorViewsVisibility();
        }

        // Finally call OnScrollListener if we have one
        if (null != this.mOnScrollListener) {
            this.mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

    @Override
    public final void onScrollStateChanged(final AbsListView view, final int state) {

        /**
         * Check that the scrolling has stopped, and that the last item is visible.
         */
        if ((state == OnScrollListener.SCROLL_STATE_IDLE) && (null != this.mOnLastItemVisibleListener)
                && this.mLastItemVisible) {
            this.mOnLastItemVisibleListener.onLastItemVisible();
        }

        if (null != this.mOnScrollListener) {
            this.mOnScrollListener.onScrollStateChanged(view, state);
        }
    }

    /**
     * Pass-through method for {@link PullToRefreshBase#getRefreshableView() getRefreshableView()}.
     * {@link AdapterView#setAdapter(android.widget.Adapter)} setAdapter(adapter)}. This is just for convenience!
     *
     * @param adapter - Adapter to set
     */
    public void setAdapter(final ListAdapter adapter) {

        ((AdapterView<ListAdapter>) this.mRefreshableView).setAdapter(adapter);
    }

    /**
     * Sets the Empty View to be used by the Adapter View.
     * <p/>
     * We need it handle it ourselves so that we can Pull-to-Refresh when the Empty View is shown.
     * <p/>
     * Please note, you do <strong>not</strong> usually need to call this method yourself. Calling setEmptyView on the
     * AdapterView will automatically call this method and set everything up. This includes when the Android Framework
     * automatically sets the Empty View based on it's ID.
     *
     * @param newEmptyView - Empty View to be used
     */
    public final void setEmptyView(final View newEmptyView) {

        final FrameLayout refreshableViewWrapper = this.getRefreshableViewWrapper();

        if (null != newEmptyView) {
            // New view needs to be clickable so that Android recognizes it as a
            // target for Touch Events
            newEmptyView.setClickable(true);

            final ViewParent newEmptyViewParent = newEmptyView.getParent();
            if ((null != newEmptyViewParent) && (newEmptyViewParent instanceof ViewGroup)) {
                ((ViewGroup) newEmptyViewParent).removeView(newEmptyView);
            }

            // We need to convert any LayoutParams so that it works in our
            // FrameLayout
            final FrameLayout.LayoutParams lp = convertEmptyViewLayoutParams(newEmptyView.getLayoutParams());
            if (null != lp) {
                refreshableViewWrapper.addView(newEmptyView, lp);
            } else {
                refreshableViewWrapper.addView(newEmptyView);
            }
        }

        if (this.mRefreshableView instanceof EmptyViewMethodAccessor) {
            ((EmptyViewMethodAccessor) this.mRefreshableView).setEmptyViewInternal(newEmptyView);
        } else {
            this.mRefreshableView.setEmptyView(newEmptyView);
        }
        this.mEmptyView = newEmptyView;
    }

    /**
     * Pass-through method for {@link PullToRefreshBase#getRefreshableView() getRefreshableView()}.
     * {@link AdapterView#setOnItemClickListener(OnItemClickListener) setOnItemClickListener(listener)}. This is just
     * for convenience!
     *
     * @param listener - OnItemClickListener to use
     */
    public void setOnItemClickListener(final OnItemClickListener listener) {

        this.mRefreshableView.setOnItemClickListener(listener);
    }

    public final void setOnLastItemVisibleListener(final OnLastItemVisibleListener listener) {

        this.mOnLastItemVisibleListener = listener;
    }

    public final void setOnScrollListener(final OnScrollListener listener) {

        this.mOnScrollListener = listener;
    }

    public final void setScrollEmptyView(final boolean doScroll) {

        this.mScrollEmptyView = doScroll;
    }

    /**
     * Sets whether an indicator graphic should be displayed when the View is in a state where a Pull-to-Refresh can
     * happen. An example of this state is when the Adapter View is scrolled to the top and the mode is set to
     * {@link Mode#PULL_FROM_START}
     *
     * @param showIndicator - true if the indicators should be shown.
     */
    public void setShowIndicator(final boolean showIndicator) {

        this.mShowIndicator = showIndicator;

        if (this.getShowIndicatorInternal()) {
            // If we're set to Show Indicator, add/update them
            this.addIndicatorViews();
        } else {
            // If not, then remove then
            this.removeIndicatorViews();
        }
    }

    private void addIndicatorViews() {

        final Mode mode = this.getMode();
        final FrameLayout refreshableViewWrapper = this.getRefreshableViewWrapper();

        if (mode.showHeaderLoadingLayout() && (null == this.mIndicatorIvTop)) {
            // If the mode can pull down, and we don't have one set already
            this.mIndicatorIvTop = new IndicatorLayout(this.getContext(), Mode.PULL_FROM_START);
            final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.rightMargin = this.getResources().getDimensionPixelSize(R.dimen.indicator_right_padding);
            params.gravity = Gravity.TOP | Gravity.RIGHT;
            refreshableViewWrapper.addView(this.mIndicatorIvTop, params);

        } else if (!mode.showHeaderLoadingLayout() && (null != this.mIndicatorIvTop)) {
            // If we can't pull down, but have a View then remove it
            refreshableViewWrapper.removeView(this.mIndicatorIvTop);
            this.mIndicatorIvTop = null;
        }

        if (mode.showFooterLoadingLayout() && (null == this.mIndicatorIvBottom)) {
            // If the mode can pull down, and we don't have one set already
            this.mIndicatorIvBottom = new IndicatorLayout(this.getContext(), Mode.PULL_FROM_END);
            final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.rightMargin = this.getResources().getDimensionPixelSize(R.dimen.indicator_right_padding);
            params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
            refreshableViewWrapper.addView(this.mIndicatorIvBottom, params);

        } else if (!mode.showFooterLoadingLayout() && (null != this.mIndicatorIvBottom)) {
            // If we can't pull down, but have a View then remove it
            refreshableViewWrapper.removeView(this.mIndicatorIvBottom);
            this.mIndicatorIvBottom = null;
        }
    }

    private boolean getShowIndicatorInternal() {

        return this.mShowIndicator && this.isPullToRefreshEnabled();
    }

    private boolean isFirstItemVisible() {

        final Adapter adapter = this.mRefreshableView.getAdapter();

        if ((null == adapter) || adapter.isEmpty()) {
            if (DEBUG) {
                Log.d(LOG_TAG, "isFirstItemVisible. Empty View.");
            }
            return true;

        }
        /**
         * This check should really just be: mRefreshableView.getFirstVisiblePosition() == 0, but PtRListView internally
         * use a HeaderView which messes the positions up. For now we'll just add one to account for it and rely on the
         * inner condition which checks getTop().
         */
        if (this.mRefreshableView.getFirstVisiblePosition() <= 1) {
            final View firstVisibleChild = this.mRefreshableView.getChildAt(0);
            if (firstVisibleChild != null) {
                return firstVisibleChild.getTop() >= this.mRefreshableView.getTop();
            }
        }

        return false;
    }

    private boolean isLastItemVisible() {

        final Adapter adapter = this.mRefreshableView.getAdapter();

        if ((null == adapter) || adapter.isEmpty()) {
            if (DEBUG) {
                Log.d(LOG_TAG, "isLastItemVisible. Empty View.");
            }
            return true;
        }

        final int lastItemPosition = this.mRefreshableView.getCount() - 1;
        final int lastVisiblePosition = this.mRefreshableView.getLastVisiblePosition();

        if (DEBUG) {
            Log.d(LOG_TAG, "isLastItemVisible. Last Item Position: " + lastItemPosition + " Last Visible Pos: "
                    + lastVisiblePosition);
        }

        /**
         * This check should really just be: lastVisiblePosition == lastItemPosition, but PtRListView internally uses a
         * FooterView which messes the positions up. For me we'll just subtract one to account for it and rely on the
         * inner condition which checks getBottom().
         */
        if (lastVisiblePosition >= (lastItemPosition - 1)) {
            final int childIndex = lastVisiblePosition - this.mRefreshableView.getFirstVisiblePosition();
            final View lastVisibleChild = this.mRefreshableView.getChildAt(childIndex);
            if (lastVisibleChild != null) {
                return lastVisibleChild.getBottom() <= this.mRefreshableView.getBottom();
            }
        }

        return false;
    }

    private void removeIndicatorViews() {

        if (null != this.mIndicatorIvTop) {
            this.getRefreshableViewWrapper().removeView(this.mIndicatorIvTop);
            this.mIndicatorIvTop = null;
        }

        if (null != this.mIndicatorIvBottom) {
            this.getRefreshableViewWrapper().removeView(this.mIndicatorIvBottom);
            this.mIndicatorIvBottom = null;
        }
    }

    private void updateIndicatorViewsVisibility() {

        if (null != this.mIndicatorIvTop) {
            if (!this.isRefreshing() && this.isReadyForPullStart()) {
                if (!this.mIndicatorIvTop.isVisible()) {
                    this.mIndicatorIvTop.show();
                }
            } else {
                if (this.mIndicatorIvTop.isVisible()) {
                    this.mIndicatorIvTop.hide();
                }
            }
        }

        if (null != this.mIndicatorIvBottom) {
            if (!this.isRefreshing() && this.isReadyForPullEnd()) {
                if (!this.mIndicatorIvBottom.isVisible()) {
                    this.mIndicatorIvBottom.show();
                }
            } else {
                if (this.mIndicatorIvBottom.isVisible()) {
                    this.mIndicatorIvBottom.hide();
                }
            }
        }
    }

    @Override
    protected void handleStyledAttributes(final TypedArray a) {

        // Set Show Indicator to the XML value, or default value
        this.mShowIndicator = a.getBoolean(R.styleable.PullToRefresh_ptrShowIndicator,
                !this.isPullToRefreshOverScrollEnabled());
    }

    @Override
    protected boolean isReadyForPullEnd() {

        return this.isLastItemVisible();
    }

    @Override
    protected boolean isReadyForPullStart() {

        return this.isFirstItemVisible();
    }

    @Override
    protected void onPullToRefresh() {

        super.onPullToRefresh();

        if (this.getShowIndicatorInternal()) {
            switch (this.getCurrentMode()) {
            case PULL_FROM_END:
                this.mIndicatorIvBottom.pullToRefresh();
                break;
            case PULL_FROM_START:
                this.mIndicatorIvTop.pullToRefresh();
                break;
            default:
                // NO-OP
                break;
            }
        }
    }

    @Override
    protected void onRefreshing(final boolean doScroll) {

        super.onRefreshing(doScroll);

        if (this.getShowIndicatorInternal()) {
            this.updateIndicatorViewsVisibility();
        }
    }

    @Override
    protected void onReleaseToRefresh() {

        super.onReleaseToRefresh();

        if (this.getShowIndicatorInternal()) {
            switch (this.getCurrentMode()) {
            case PULL_FROM_END:
                this.mIndicatorIvBottom.releaseToRefresh();
                break;
            case PULL_FROM_START:
                this.mIndicatorIvTop.releaseToRefresh();
                break;
            default:
                // NO-OP
                break;
            }
        }
    }

    @Override
    protected void onReset() {

        super.onReset();

        if (this.getShowIndicatorInternal()) {
            this.updateIndicatorViewsVisibility();
        }
    }

    @Override
    protected void onScrollChanged(final int l, final int t, final int oldl, final int oldt) {

        super.onScrollChanged(l, t, oldl, oldt);
        if ((null != this.mEmptyView) && !this.mScrollEmptyView) {
            this.mEmptyView.scrollTo(-l, -t);
        }
    }

    @Override
    protected void updateUIForMode() {

        super.updateUIForMode();

        // Check Indicator Views consistent with new Mode
        if (this.getShowIndicatorInternal()) {
            this.addIndicatorViews();
        } else {
            this.removeIndicatorViews();
        }
    }
}
