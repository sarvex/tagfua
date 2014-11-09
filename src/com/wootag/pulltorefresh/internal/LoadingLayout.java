/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes. Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 *******************************************************************************/
package com.wootag.pulltorefresh.internal;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wootag.R;
import com.wootag.pulltorefresh.ILoadingLayout;
import com.wootag.pulltorefresh.PullToRefreshBase.Mode;
import com.wootag.pulltorefresh.PullToRefreshBase.Orientation;

public abstract class LoadingLayout extends FrameLayout implements ILoadingLayout {

    static final String LOG_TAG = "PullToRefresh-LoadingLayout";

    static final Interpolator ANIMATION_INTERPOLATOR = new LinearInterpolator();

    private final FrameLayout mInnerLayout;

    protected final ImageView mHeaderImage;
    protected final ProgressBar mHeaderProgress;

    private boolean mUseIntrinsicAnimation;

    private final TextView mHeaderText;
    private final TextView mSubHeaderText;

    protected final Mode mMode;
    protected final Orientation mScrollDirection;

    private CharSequence mPullLabel;
    private CharSequence mRefreshingLabel;
    private CharSequence mReleaseLabel;

    public LoadingLayout(final Context context, final Mode mode, final Orientation scrollDirection,
            final TypedArray attrs) {

        super(context);
        this.mMode = mode;
        this.mScrollDirection = scrollDirection;

        switch (scrollDirection) {
        case HORIZONTAL:
            LayoutInflater.from(context).inflate(R.layout.pull_to_refresh_header_horizontal, this);
            break;
        case VERTICAL:
        default:
            LayoutInflater.from(context).inflate(R.layout.pull_to_refresh_header_vertical, this);
            break;
        }

        this.mInnerLayout = (FrameLayout) this.findViewById(R.id.fl_inner);
        this.mHeaderText = (TextView) this.mInnerLayout.findViewById(R.id.pull_to_refresh_text);
        this.mHeaderProgress = (ProgressBar) this.mInnerLayout.findViewById(R.id.pull_to_refresh_progress);
        this.mSubHeaderText = (TextView) this.mInnerLayout.findViewById(R.id.pull_to_refresh_sub_text);
        this.mHeaderImage = (ImageView) this.mInnerLayout.findViewById(R.id.pull_to_refresh_image);
        this.mHeaderText.setTextColor(this.getResources().getColor(R.color.twitter_bg_color));
        this.mSubHeaderText.setTextColor(this.getResources().getColor(R.color.twitter_bg_color));
        final FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) this.mInnerLayout.getLayoutParams();

        switch (mode) {
        case PULL_FROM_END:
            lp.gravity = scrollDirection == Orientation.VERTICAL ? Gravity.TOP : Gravity.LEFT;

            // Load in labels
            this.mPullLabel = context.getString(R.string.pull_to_refresh_pull_label);
            this.mRefreshingLabel = context.getString(R.string.pull_to_refresh_refreshing_label);
            this.mReleaseLabel = context.getString(R.string.pull_to_refresh_release_label);
            break;

        case PULL_FROM_START:
        default:
            lp.gravity = scrollDirection == Orientation.VERTICAL ? Gravity.BOTTOM : Gravity.RIGHT;

            // Load in labels
            this.mPullLabel = context.getString(R.string.pull_to_refresh_pull_label);
            this.mRefreshingLabel = context.getString(R.string.pull_to_refresh_refreshing_label);
            this.mReleaseLabel = context.getString(R.string.pull_to_refresh_release_label);
            break;
        }

        if (attrs.hasValue(R.styleable.PullToRefresh_ptrHeaderBackground)) {
            final Drawable background = attrs.getDrawable(R.styleable.PullToRefresh_ptrHeaderBackground);
            if (null != background) {
                ViewCompat.setBackground(this, background);
            }
        }

        if (attrs.hasValue(R.styleable.PullToRefresh_ptrHeaderTextAppearance)) {
            final TypedValue styleID = new TypedValue();
            attrs.getValue(R.styleable.PullToRefresh_ptrHeaderTextAppearance, styleID);
            this.setTextAppearance(styleID.data);
        }
        if (attrs.hasValue(R.styleable.PullToRefresh_ptrSubHeaderTextAppearance)) {
            final TypedValue styleID = new TypedValue();
            attrs.getValue(R.styleable.PullToRefresh_ptrSubHeaderTextAppearance, styleID);
            this.setSubTextAppearance(styleID.data);
        }

        // Text Color attrs need to be set after TextAppearance attrs
        if (attrs.hasValue(R.styleable.PullToRefresh_ptrHeaderTextColor)) {
            final ColorStateList colors = attrs.getColorStateList(R.styleable.PullToRefresh_ptrHeaderTextColor);
            if (null != colors) {
                this.setTextColor(colors);
            }
        }
        if (attrs.hasValue(R.styleable.PullToRefresh_ptrHeaderSubTextColor)) {
            final ColorStateList colors = attrs.getColorStateList(R.styleable.PullToRefresh_ptrHeaderSubTextColor);
            if (null != colors) {
                this.setSubTextColor(colors);
            }
        }

        // Try and get defined drawable from Attrs
        Drawable imageDrawable = null;
        if (attrs.hasValue(R.styleable.PullToRefresh_ptrDrawable)) {
            imageDrawable = attrs.getDrawable(R.styleable.PullToRefresh_ptrDrawable);
        }

        // Check Specific Drawable from Attrs, these overrite the generic
        // drawable attr above
        switch (mode) {
        case PULL_FROM_START:
        default:
            if (attrs.hasValue(R.styleable.PullToRefresh_ptrDrawableStart)) {
                imageDrawable = attrs.getDrawable(R.styleable.PullToRefresh_ptrDrawableStart);
            } else if (attrs.hasValue(R.styleable.PullToRefresh_ptrDrawableTop)) {
                Utils.warnDeprecation("ptrDrawableTop", "ptrDrawableStart");
                imageDrawable = attrs.getDrawable(R.styleable.PullToRefresh_ptrDrawableTop);
            }
            break;

        case PULL_FROM_END:
            if (attrs.hasValue(R.styleable.PullToRefresh_ptrDrawableEnd)) {
                imageDrawable = attrs.getDrawable(R.styleable.PullToRefresh_ptrDrawableEnd);
            } else if (attrs.hasValue(R.styleable.PullToRefresh_ptrDrawableBottom)) {
                Utils.warnDeprecation("ptrDrawableBottom", "ptrDrawableEnd");
                imageDrawable = attrs.getDrawable(R.styleable.PullToRefresh_ptrDrawableBottom);
            }
            break;
        }

        // If we don't have a user defined drawable, load the default
        if (null == imageDrawable) {
            imageDrawable = context.getResources().getDrawable(this.getDefaultDrawableResId());
        }

        // Set Drawable, and save width/height
        this.setLoadingDrawable(imageDrawable);

        this.reset();
    }

    public final int getContentSize() {

        switch (this.mScrollDirection) {
        case HORIZONTAL:
            return this.mInnerLayout.getWidth();
        case VERTICAL:
        default:
            return this.mInnerLayout.getHeight();
        }
    }

    public final void hideAllViews() {

        if (View.VISIBLE == this.mHeaderText.getVisibility()) {
            this.mHeaderText.setVisibility(View.INVISIBLE);
        }
        if (View.VISIBLE == this.mHeaderProgress.getVisibility()) {
            this.mHeaderProgress.setVisibility(View.INVISIBLE);
        }
        if (View.VISIBLE == this.mHeaderImage.getVisibility()) {
            this.mHeaderImage.setVisibility(View.INVISIBLE);
        }
        if (View.VISIBLE == this.mSubHeaderText.getVisibility()) {
            this.mSubHeaderText.setVisibility(View.INVISIBLE);
        }
    }

    public final void onPull(final float scaleOfLayout) {

        if (!this.mUseIntrinsicAnimation) {
            this.onPullImpl(scaleOfLayout);
        }
    }

    public final void pullToRefresh() {

        if (null != this.mHeaderText) {
            this.mHeaderText.setText(this.mPullLabel);
        }

        // Now call the callback
        this.pullToRefreshImpl();
    }

    public final void refreshing() {

        if (null != this.mHeaderText) {
            this.mHeaderText.setText(this.mRefreshingLabel);
        }

        if (this.mUseIntrinsicAnimation) {
            ((AnimationDrawable) this.mHeaderImage.getDrawable()).start();
        } else {
            // Now call the callback
            this.refreshingImpl();
        }

        if (null != this.mSubHeaderText) {
            this.mSubHeaderText.setVisibility(View.GONE);
        }
    }

    public final void releaseToRefresh() {

        if (null != this.mHeaderText) {
            this.mHeaderText.setText(this.mReleaseLabel);
        }

        // Now call the callback
        this.releaseToRefreshImpl();
    }

    public final void reset() {

        if (null != this.mHeaderText) {
            this.mHeaderText.setText(this.mPullLabel);
        }
        this.mHeaderImage.setVisibility(View.VISIBLE);

        if (this.mUseIntrinsicAnimation) {
            ((AnimationDrawable) this.mHeaderImage.getDrawable()).stop();
        } else {
            // Now call the callback
            this.resetImpl();
        }

        if (null != this.mSubHeaderText) {
            if (TextUtils.isEmpty(this.mSubHeaderText.getText())) {
                this.mSubHeaderText.setVisibility(View.GONE);
            } else {
                this.mSubHeaderText.setVisibility(View.VISIBLE);
            }
        }
    }

    public final void setHeight(final int height) {

        final ViewGroup.LayoutParams lp = this.getLayoutParams();
        lp.height = height;
        this.requestLayout();
    }

    @Override
    public void setLastUpdatedLabel(final CharSequence label) {

        this.setSubHeaderText(label);
    }

    @Override
    public final void setLoadingDrawable(final Drawable imageDrawable) {

        // Set Drawable
        this.mHeaderImage.setImageDrawable(imageDrawable);
        this.mUseIntrinsicAnimation = (imageDrawable instanceof AnimationDrawable);

        // Now call the callback
        this.onLoadingDrawableSet(imageDrawable);
    }

    @Override
    public void setPullLabel(final CharSequence pullLabel) {

        this.mPullLabel = pullLabel;
    }

    @Override
    public void setRefreshingLabel(final CharSequence refreshingLabel) {

        this.mRefreshingLabel = refreshingLabel;
    }

    @Override
    public void setReleaseLabel(final CharSequence releaseLabel) {

        this.mReleaseLabel = releaseLabel;
    }

    @Override
    public void setTextTypeface(final Typeface tf) {

        this.mHeaderText.setTypeface(tf);
    }

    public final void setWidth(final int width) {

        final ViewGroup.LayoutParams lp = this.getLayoutParams();
        lp.width = width;
        this.requestLayout();
    }

    public final void showInvisibleViews() {

        if (View.INVISIBLE == this.mHeaderText.getVisibility()) {
            this.mHeaderText.setVisibility(View.VISIBLE);
        }
        if (View.INVISIBLE == this.mHeaderProgress.getVisibility()) {
            this.mHeaderProgress.setVisibility(View.VISIBLE);
        }
        if (View.INVISIBLE == this.mHeaderImage.getVisibility()) {
            this.mHeaderImage.setVisibility(View.VISIBLE);
        }
        if (View.INVISIBLE == this.mSubHeaderText.getVisibility()) {
            this.mSubHeaderText.setVisibility(View.VISIBLE);
        }
    }

    private void setSubHeaderText(final CharSequence label) {

        if (null != this.mSubHeaderText) {
            this.mSubHeaderText.setTextColor(this.getResources().getColor(R.color.twitter_bg_color));// /////
            if (TextUtils.isEmpty(label)) {
                this.mSubHeaderText.setVisibility(View.GONE);
            } else {
                this.mSubHeaderText.setText(label);
                // Only set it to Visible if we're GONE, otherwise VISIBLE will
                // be set soon
                if (View.GONE == this.mSubHeaderText.getVisibility()) {
                    this.mSubHeaderText.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void setSubTextAppearance(final int value) {

        if (null != this.mSubHeaderText) {
            this.mSubHeaderText.setTextAppearance(this.getContext(), value);
        }
    }

    private void setSubTextColor(final ColorStateList color) {

        if (null != this.mSubHeaderText) {
            this.mSubHeaderText.setTextColor(color);
        }
    }

    private void setTextAppearance(final int value) {

        if (null != this.mHeaderText) {
            this.mHeaderText.setTextAppearance(this.getContext(), value);
        }
        if (null != this.mSubHeaderText) {
            this.mSubHeaderText.setTextAppearance(this.getContext(), value);
        }
    }

    private void setTextColor(final ColorStateList color) {

        if (null != this.mHeaderText) {
            this.mHeaderText.setTextColor(color);
        }
        if (null != this.mSubHeaderText) {
            this.mSubHeaderText.setTextColor(color);
        }
    }

    /**
     * Callbacks for derivative Layouts
     */

    protected abstract int getDefaultDrawableResId();

    protected abstract void onLoadingDrawableSet(Drawable imageDrawable);

    protected abstract void onPullImpl(float scaleOfLayout);

    protected abstract void pullToRefreshImpl();

    protected abstract void refreshingImpl();

    protected abstract void releaseToRefreshImpl();

    protected abstract void resetImpl();

}
