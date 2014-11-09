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
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.wootag.R;
import com.wootag.pulltorefresh.PullToRefreshBase;

public class IndicatorLayout extends FrameLayout implements AnimationListener {

    static final int DEFAULT_ROTATION_ANIMATION_DURATION = 150;

    private final Animation mInAnim, mOutAnim;
    private final ImageView mArrowImageView;

    private final Animation mRotateAnimation, mResetRotateAnimation;

    public IndicatorLayout(final Context context, final PullToRefreshBase.Mode mode) {

        super(context);
        this.mArrowImageView = new ImageView(context);

        final Drawable arrowD = this.getResources().getDrawable(R.drawable.indicator_arrow);
        this.mArrowImageView.setImageDrawable(arrowD);

        final int padding = this.getResources().getDimensionPixelSize(R.dimen.indicator_internal_padding);
        this.mArrowImageView.setPadding(padding, padding, padding, padding);
        this.addView(this.mArrowImageView);

        int inAnimResId, outAnimResId;
        switch (mode) {
        case PULL_FROM_END:
            inAnimResId = R.anim.slide_in_from_bottom;
            outAnimResId = R.anim.slide_out_to_bottom;
            this.setBackgroundResource(R.drawable.indicator_bg_bottom);

            // Rotate Arrow so it's pointing the correct way
            this.mArrowImageView.setScaleType(ScaleType.MATRIX);
            final Matrix matrix = new Matrix();
            matrix.setRotate(180f, arrowD.getIntrinsicWidth() / 2f, arrowD.getIntrinsicHeight() / 2f);
            this.mArrowImageView.setImageMatrix(matrix);
            break;
        default:
        case PULL_FROM_START:
            inAnimResId = R.anim.slide_in_from_top;
            outAnimResId = R.anim.slide_out_to_top;
            this.setBackgroundResource(R.drawable.indicator_bg_top);
            break;
        }

        this.mInAnim = AnimationUtils.loadAnimation(context, inAnimResId);
        this.mInAnim.setAnimationListener(this);

        this.mOutAnim = AnimationUtils.loadAnimation(context, outAnimResId);
        this.mOutAnim.setAnimationListener(this);

        final Interpolator interpolator = new LinearInterpolator();
        this.mRotateAnimation = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        this.mRotateAnimation.setInterpolator(interpolator);
        this.mRotateAnimation.setDuration(DEFAULT_ROTATION_ANIMATION_DURATION);
        this.mRotateAnimation.setFillAfter(true);

        this.mResetRotateAnimation = new RotateAnimation(-180, 0, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        this.mResetRotateAnimation.setInterpolator(interpolator);
        this.mResetRotateAnimation.setDuration(DEFAULT_ROTATION_ANIMATION_DURATION);
        this.mResetRotateAnimation.setFillAfter(true);

    }

    public void hide() {

        this.startAnimation(this.mOutAnim);
    }

    public final boolean isVisible() {

        final Animation currentAnim = this.getAnimation();
        if (null != currentAnim) {
            return this.mInAnim == currentAnim;
        }

        return this.getVisibility() == View.VISIBLE;
    }

    @Override
    public void onAnimationEnd(final Animation animation) {

        if (animation == this.mOutAnim) {
            this.mArrowImageView.clearAnimation();
            this.setVisibility(View.GONE);
        } else if (animation == this.mInAnim) {
            this.setVisibility(View.VISIBLE);
        }

        this.clearAnimation();
    }

    @Override
    public void onAnimationRepeat(final Animation animation) {

        // NO-OP
    }

    @Override
    public void onAnimationStart(final Animation animation) {

        this.setVisibility(View.VISIBLE);
    }

    public void pullToRefresh() {

        this.mArrowImageView.startAnimation(this.mResetRotateAnimation);
    }

    public void releaseToRefresh() {

        this.mArrowImageView.startAnimation(this.mRotateAnimation);
    }

    public void show() {

        this.mArrowImageView.clearAnimation();
        this.startAnimation(this.mInAnim);
    }

}
