/**
 * Copyright 2010-present Facebook. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package com.TagFu.facebook.widget;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.wootag.R;

public class ToolTipPopup {

    /**
     * The default time that the tool tip will be displayed
     */
    public static final long DEFAULT_POPUP_DISPLAY_TIME = 6000;

    private final String mText;

    protected final WeakReference<View> anchorViewRef;
    private final Context context;
    protected PopupContentView popupContent;
    protected PopupWindow popupWindow;
    private Style mStyle = Style.BLUE;
    private long mNuxDisplayTime = DEFAULT_POPUP_DISPLAY_TIME;
    private final ViewTreeObserver.OnScrollChangedListener mScrollListener = new ViewTreeObserver.OnScrollChangedListener() {

        @Override
        public void onScrollChanged() {

            if ((ToolTipPopup.this.anchorViewRef.get() != null) && (ToolTipPopup.this.popupWindow != null)
                    && ToolTipPopup.this.popupWindow.isShowing()) {
                if (ToolTipPopup.this.popupWindow.isAboveAnchor()) {
                    ToolTipPopup.this.popupContent.showBottomArrow();
                } else {
                    ToolTipPopup.this.popupContent.showTopArrow();
                }
            }
        }
    };

    /**
     * Create a new ToolTipPopup
     *
     * @param text The text to be displayed in the tool tip
     * @param anchor The view to anchor this tool tip to.
     */
    public ToolTipPopup(final String text, final View anchor) {

        this.mText = text;
        this.anchorViewRef = new WeakReference<View>(anchor);
        this.context = anchor.getContext();
    }

    /**
     * Dismiss the tool tip
     */
    public void dismiss() {

        this.unregisterObserver();
        if (this.popupWindow != null) {
            this.popupWindow.dismiss();
        }
    }

    /**
     * Set the time (in milliseconds) the tool tip will be displayed. Any number less than or equal to 0 will cause the
     * tool tip to be displayed indefinitely
     *
     * @param displayTime The amount of time (in milliseconds) to display the tool tip
     */
    public void setNuxDisplayTime(final long displayTime) {

        this.mNuxDisplayTime = displayTime;
    }

    /**
     * Sets the {@link Style} of this tool tip.
     *
     * @param mStyle
     */
    public void setStyle(final Style mStyle) {

        this.mStyle = mStyle;
    }

    /**
     * Display this tool tip to the user
     */
    public void show() {

        if (this.anchorViewRef.get() != null) {
            this.popupContent = new PopupContentView(this.context);
            final TextView body = (TextView) this.popupContent
                    .findViewById(R.id.com_facebook_tooltip_bubble_view_text_body);
            body.setText(this.mText);
            if (this.mStyle == Style.BLUE) {
                this.popupContent.bodyFrame.setBackgroundResource(R.drawable.com_facebook_tooltip_blue_background);
                this.popupContent.bottomArrow.setImageResource(R.drawable.com_facebook_tooltip_blue_bottomnub);
                this.popupContent.topArrow.setImageResource(R.drawable.com_facebook_tooltip_blue_topnub);
                this.popupContent.xOut.setImageResource(R.drawable.com_facebook_tooltip_blue_xout);
            } else {
                this.popupContent.bodyFrame.setBackgroundResource(R.drawable.com_facebook_tooltip_black_background);
                this.popupContent.bottomArrow.setImageResource(R.drawable.com_facebook_tooltip_black_bottomnub);
                this.popupContent.topArrow.setImageResource(R.drawable.com_facebook_tooltip_black_topnub);
                this.popupContent.xOut.setImageResource(R.drawable.com_facebook_tooltip_black_xout);
            }

            final Window window = ((Activity) this.context).getWindow();
            final View decorView = window.getDecorView();
            final int decorWidth = decorView.getWidth();
            final int decorHeight = decorView.getHeight();
            this.registerObserver();
            this.popupContent.onMeasure(View.MeasureSpec.makeMeasureSpec(decorWidth, View.MeasureSpec.AT_MOST),
                    View.MeasureSpec.makeMeasureSpec(decorHeight, View.MeasureSpec.AT_MOST));
            this.popupWindow = new PopupWindow(this.popupContent, this.popupContent.getMeasuredWidth(),
                    this.popupContent.getMeasuredHeight());
            this.popupWindow.showAsDropDown(this.anchorViewRef.get());
            this.updateArrows();
            if (this.mNuxDisplayTime > 0) {
                this.popupContent.postDelayed(new Runnable() {

                    @Override
                    public void run() {

                        ToolTipPopup.this.dismiss();
                    }
                }, this.mNuxDisplayTime);
            }
            this.popupWindow.setTouchable(true);
            this.popupContent.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(final View v) {

                    ToolTipPopup.this.dismiss();
                }
            });
        }
    }

    private void registerObserver() {

        this.unregisterObserver();
        if (this.anchorViewRef.get() != null) {
            this.anchorViewRef.get().getViewTreeObserver().addOnScrollChangedListener(this.mScrollListener);
        }
    }

    private void unregisterObserver() {

        if (this.anchorViewRef.get() != null) {
            this.anchorViewRef.get().getViewTreeObserver().removeOnScrollChangedListener(this.mScrollListener);
        }
    }

    private void updateArrows() {

        if ((this.popupWindow != null) && this.popupWindow.isShowing()) {
            if (this.popupWindow.isAboveAnchor()) {
                this.popupContent.showBottomArrow();
            } else {
                this.popupContent.showTopArrow();
            }
        }
    }

    public static enum Style {
        /**
         * The tool tip will be shown with a blue style; including a blue background and blue arrows.
         */
        BLUE,

        /**
         * The tool tip will be shown with a black style; including a black background and black arrows.
         */
        BLACK
    }

    private class PopupContentView extends FrameLayout {

        protected ImageView topArrow;
        protected ImageView bottomArrow;
        protected View bodyFrame;
        protected ImageView xOut;

        public PopupContentView(final Context context) {

            super(context);
            this.init();
        }

        // Expose so popup content can be sized
        @Override
        public void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {

            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

        public void showBottomArrow() {

            this.topArrow.setVisibility(View.INVISIBLE);
            this.bottomArrow.setVisibility(View.VISIBLE);
        }

        public void showTopArrow() {

            this.topArrow.setVisibility(View.VISIBLE);
            this.bottomArrow.setVisibility(View.INVISIBLE);
        }

        private void init() {

            final LayoutInflater inflater = LayoutInflater.from(this.getContext());
            inflater.inflate(R.layout.com_facebook_tooltip_bubble, this);
            this.topArrow = (ImageView) this.findViewById(R.id.com_facebook_tooltip_bubble_view_top_pointer);
            this.bottomArrow = (ImageView) this.findViewById(R.id.com_facebook_tooltip_bubble_view_bottom_pointer);
            this.bodyFrame = this.findViewById(R.id.com_facebook_body_frame);
            this.xOut = (ImageView) this.findViewById(R.id.com_facebook_button_xout);
        }
    }
}
