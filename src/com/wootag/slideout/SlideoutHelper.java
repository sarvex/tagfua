package com.TagFu.slideout;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import com.wootag.R;

public class SlideoutHelper {

    private static Bitmap coverBitmap;
    private static int sWidth = -1;
    public boolean isOpen;
    public boolean isClose = true;

    private static final int DURATION_MS = 150;// 400ms

    protected ImageView cover;

    protected final Activity activity;

    private final boolean reverse;

    private Animation mStartAnimation;

    private Animation mStopAnimation;

    public SlideoutHelper(final Activity activity) {

        this(activity, false);
    }

    public SlideoutHelper(final Activity activity, final boolean reverse) {

        this.activity = activity;
        this.reverse = reverse;
    }

    public static void prepare(final Activity activity, final int id, final int width) {

        try {
            if (coverBitmap != null) {
                coverBitmap.recycle();
            }
            final Rect rectgle = new Rect();
            final Window window = activity.getWindow();
            window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
            final int statusBarHeight = rectgle.top;

            final ViewGroup v1 = (ViewGroup) activity.findViewById(id).getRootView();
            v1.setDrawingCacheEnabled(true);
            final Bitmap source = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);
            if (statusBarHeight != 0) {
                coverBitmap = Bitmap.createBitmap(source, 0, statusBarHeight, source.getWidth(), source.getHeight()
                        - statusBarHeight);
                source.recycle();
            } else {
                coverBitmap = source;
            }
            sWidth = width;
        } catch (final Exception e) {
            // TODO: handle exception
        }
    }

    public void activate() {

        this.activity.setContentView(R.layout.slideout);
        this.cover = (ImageView) this.activity.findViewById(R.id.slidedout_cover);
        this.cover.setImageBitmap(coverBitmap);
        this.cover.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                SlideoutHelper.this.close();
            }
        });
        final int x = (int) (sWidth * 1.2f);
        if (this.reverse) {
            final android.widget.AbsoluteLayout.LayoutParams lp = new android.widget.AbsoluteLayout.LayoutParams(
                    android.view.ViewGroup.LayoutParams.FILL_PARENT, android.view.ViewGroup.LayoutParams.FILL_PARENT,
                    x, 0);
            this.activity.findViewById(R.id.slideout_placeholder).setLayoutParams(lp);
        } else {
            final android.widget.AbsoluteLayout.LayoutParams lp = new android.widget.AbsoluteLayout.LayoutParams(
                    android.view.ViewGroup.LayoutParams.FILL_PARENT, android.view.ViewGroup.LayoutParams.FILL_PARENT,
                    0, 0);
            this.activity.findViewById(R.id.slideout_placeholder).setLayoutParams(lp);
        }
        this.initAnimations();
    }

    public void close() {

        if (!this.isOpen) {
            return;
        }
        this.isClose = false;
        this.cover.startAnimation(this.mStopAnimation);
    }

    public void open() {

        if (!this.isClose) {
            return;
        }
        this.isOpen = false;
        this.cover.startAnimation(this.mStartAnimation);
    }

    protected void initAnimations() {

        final int displayWidth = ((WindowManager) this.activity.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getWidth();
        final int shift = (this.reverse ? -1 : 1) * (sWidth - displayWidth);
        this.mStartAnimation = new TranslateAnimation(Animation.ABSOLUTE, 0, Animation.ABSOLUTE, -shift,
                Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0);

        this.mStopAnimation = new TranslateAnimation(Animation.ABSOLUTE, 0, Animation.ABSOLUTE, shift,
                Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0);
        this.mStartAnimation.setDuration(DURATION_MS);
        this.mStartAnimation.setFillAfter(true);
        this.mStartAnimation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationEnd(final Animation animation) {

                SlideoutHelper.this.cover.setAnimation(null);
                SlideoutHelper.this.isOpen = true;
                final android.widget.AbsoluteLayout.LayoutParams lp = new android.widget.AbsoluteLayout.LayoutParams(
                        android.view.ViewGroup.LayoutParams.FILL_PARENT,
                        android.view.ViewGroup.LayoutParams.FILL_PARENT, -shift, 0);
                SlideoutHelper.this.cover.setLayoutParams(lp);
            }

            @Override
            public void onAnimationRepeat(final Animation animation) {

            }

            @Override
            public void onAnimationStart(final Animation animation) {

            }
        });

        this.mStopAnimation.setDuration(DURATION_MS);
        this.mStopAnimation.setFillAfter(true);
        this.mStopAnimation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationEnd(final Animation animation) {

                SlideoutHelper.this.isClose = true;
                SlideoutHelper.this.activity.finish();
                SlideoutHelper.this.activity.overridePendingTransition(0, 0);
            }

            @Override
            public void onAnimationRepeat(final Animation animation) {

            }

            @Override
            public void onAnimationStart(final Animation animation) {

            }
        });
    }
}
