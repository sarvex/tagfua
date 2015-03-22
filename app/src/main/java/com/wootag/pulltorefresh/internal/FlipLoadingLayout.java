/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes. Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 *******************************************************************************/
package com.TagFu.pulltorefresh.internal;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView.ScaleType;

import com.TagFu.R;
import com.TagFu.pulltorefresh.PullToRefreshBase.Mode;
import com.TagFu.pulltorefresh.PullToRefreshBase.Orientation;

public class FlipLoadingLayout extends LoadingLayout {

    static final int FLIP_ANIMATION_DURATION = 150;

    private final Animation mRotateAnimation, mResetRotateAnimation;

    public FlipLoadingLayout(final Context context, final Mode mode, final Orientation scrollDirection,
            final TypedArray attrs) {

        super(context, mode, scrollDirection, attrs);

        final int rotateAngle = mode == Mode.PULL_FROM_START ? -180 : 180;

        this.mRotateAnimation = new RotateAnimation(0, rotateAngle, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        this.mRotateAnimation.setInterpolator(ANIMATION_INTERPOLATOR);
        this.mRotateAnimation.setDuration(FLIP_ANIMATION_DURATION);
        this.mRotateAnimation.setFillAfter(true);

        this.mResetRotateAnimation = new RotateAnimation(rotateAngle, 0, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        this.mResetRotateAnimation.setInterpolator(ANIMATION_INTERPOLATOR);
        this.mResetRotateAnimation.setDuration(FLIP_ANIMATION_DURATION);
        this.mResetRotateAnimation.setFillAfter(true);
    }

    private float getDrawableRotationAngle() {

        float angle = 0f;
        switch (this.mMode) {
        case PULL_FROM_END:
            if (this.mScrollDirection == Orientation.HORIZONTAL) {
                angle = 90f;
            } else {
                angle = 180f;
            }
            break;

        case PULL_FROM_START:
            if (this.mScrollDirection == Orientation.HORIZONTAL) {
                angle = 270f;
            }
            break;

        default:
            break;
        }

        return angle;
    }

    @Override
    protected int getDefaultDrawableResId() {

        return R.drawable.default_ptr_flip;
    }

    @Override
    protected void onLoadingDrawableSet(final Drawable imageDrawable) {

        if (null != imageDrawable) {
            final int dHeight = imageDrawable.getIntrinsicHeight();
            final int dWidth = imageDrawable.getIntrinsicWidth();

            /**
             * We need to set the width/height of the ImageView so that it is square with each side the size of the
             * largest drawable dimension. This is so that it doesn't clip when rotated.
             */
            final ViewGroup.LayoutParams lp = this.mHeaderImage.getLayoutParams();
            lp.width = lp.height = Math.max(dHeight, dWidth);
            this.mHeaderImage.requestLayout();

            /**
             * We now rotate the Drawable so that is at the correct rotation, and is centered.
             */
            this.mHeaderImage.setScaleType(ScaleType.MATRIX);
            final Matrix matrix = new Matrix();
            matrix.postTranslate((lp.width - dWidth) / 2f, (lp.height - dHeight) / 2f);
            matrix.postRotate(this.getDrawableRotationAngle(), lp.width / 2f, lp.height / 2f);
            this.mHeaderImage.setImageMatrix(matrix);
        }
    }

    @Override
    protected void onPullImpl(final float scaleOfLayout) {

        // NO-OP
    }

    @Override
    protected void pullToRefreshImpl() {

        // Only start reset Animation, we've previously show the rotate anim
        if (this.mRotateAnimation == this.mHeaderImage.getAnimation()) {
            this.mHeaderImage.startAnimation(this.mResetRotateAnimation);
        }
    }

    @Override
    protected void refreshingImpl() {

        this.mHeaderImage.clearAnimation();
        this.mHeaderImage.setVisibility(View.INVISIBLE);
        this.mHeaderProgress.setVisibility(View.VISIBLE);
    }

    @Override
    protected void releaseToRefreshImpl() {

        this.mHeaderImage.startAnimation(this.mRotateAnimation);
    }

    @Override
    protected void resetImpl() {

        this.mHeaderImage.clearAnimation();
        this.mHeaderProgress.setVisibility(View.GONE);
        this.mHeaderImage.setVisibility(View.VISIBLE);
    }

}
