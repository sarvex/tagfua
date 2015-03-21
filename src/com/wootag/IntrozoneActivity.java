/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.wTagFu

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wootag.util.MainManager;

public class IntrozoneActivity extends Activity {

    protected static final Logger LOG = LoggerManager.getLogger();

    private ImageView fifth;
    private ImageView first;
    private ImageView fourth;
    private ImageView login;
    private final HorizontalPager.OnScreenSwitchListener onScreenSwitchListener = new HorizontalPager.OnScreenSwitchListener() {

        @Override
        public void onScreenSwitched(final int screen) {

            LOG.d("HorizontalPager switched to screen: " + screen);
        }
    };

    protected HorizontalPager realViewSwitcher;
    private ImageView register;
    private ImageView second;
    private ImageView sixth;
    private TextView textView;
    private ImageView third;
    protected int introScrren = 1;
    private RelativeLayout notificationView;

    private RelativeLayout getFollowNotification(final int pageNo) {

        this.notificationView = (RelativeLayout) ((LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.activity_intro_zone, null);
        this.first = (ImageView) this.notificationView.findViewById(R.id.first);
        this.second = (ImageView) this.notificationView.findViewById(R.id.second);
        this.third = (ImageView) this.notificationView.findViewById(R.id.third);
        this.fourth = (ImageView) this.notificationView.findViewById(R.id.fourth);
        this.fifth = (ImageView) this.notificationView.findViewById(R.id.fifth);
        this.sixth = (ImageView) this.notificationView.findViewById(R.id.sixth);

        this.textView = (TextView) this.notificationView.findViewById(R.id.introzoneText);
        this.login = (ImageView) this.notificationView.findViewById(R.id.intrologin);
        this.register = (ImageView) this.notificationView.findViewById(R.id.introregister);
        this.login.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                MainManager.getInstance().setISFirstTimeInstall(true);
                final Intent intent = new Intent(IntrozoneActivity.this, SignInFragment.class);
                IntrozoneActivity.this.finish();
                IntrozoneActivity.this.startActivity(intent);

            }
        });
        this.register.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                MainManager.getInstance().setISFirstTimeInstall(true);
                final Intent intent = new Intent(IntrozoneActivity.this, SignUpFragment.class);
                IntrozoneActivity.this.finish();
                IntrozoneActivity.this.startActivity(intent);

            }
        });
        this.first.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                IntrozoneActivity.this.introScrren = 0;
                IntrozoneActivity.this.realViewSwitcher.setCurrentScreen(IntrozoneActivity.this.introScrren, true);

            }
        });
        this.second.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                IntrozoneActivity.this.introScrren = 1;
                IntrozoneActivity.this.realViewSwitcher.setCurrentScreen(IntrozoneActivity.this.introScrren, true);

            }
        });
        this.sixth.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                IntrozoneActivity.this.introScrren = 5;
                // setBg(notificationView,introScrren);
                // updateBreadscrumImage(introScrren);
                IntrozoneActivity.this.realViewSwitcher.setCurrentScreen(IntrozoneActivity.this.introScrren, true);

            }
        });
        this.third.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                IntrozoneActivity.this.introScrren = 2;
                IntrozoneActivity.this.realViewSwitcher.setCurrentScreen(IntrozoneActivity.this.introScrren, true);

            }
        });
        this.fourth.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                IntrozoneActivity.this.introScrren = 3;
                IntrozoneActivity.this.realViewSwitcher.setCurrentScreen(IntrozoneActivity.this.introScrren, true);

            }
        });
        this.fifth.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                IntrozoneActivity.this.introScrren = 4;
                IntrozoneActivity.this.realViewSwitcher.setCurrentScreen(IntrozoneActivity.this.introScrren, true);

            }
        });
        this.setBackground(this.notificationView, pageNo);
        return this.notificationView;

    }

    private RelativeLayout setBackground(final RelativeLayout viewBackground, final int screenType) {

        if (screenType == 1) {
            viewBackground.setBackgroundResource(R.drawable.intro_first);
            this.textView.setText("Make your videos alive & interactive");
            this.updateBreadscrumImage(screenType);
        } else if (screenType == 2) {
            viewBackground.setBackgroundResource(R.drawable.intro_second);
            this.textView.setText("Give your captured moment \n a different experience ");
            this.updateBreadscrumImage(screenType);
        } else if (screenType == 3) {
            viewBackground.setBackgroundResource(R.drawable.intro_third);
            this.textView.setText("Express and tag people,\n  place, product inside your videos");
            this.updateBreadscrumImage(screenType);
        } else if (screenType == 4) {
            viewBackground.setBackgroundResource(R.drawable.intro_fourth);
            this.textView.setText("Let your connection interact \n with your videos");
            this.updateBreadscrumImage(screenType);
        } else if (screenType == 5) {
            viewBackground.setBackgroundResource(R.drawable.intro_fifth);
            this.textView.setText("Share your moments");
            this.updateBreadscrumImage(screenType);
        } else if (screenType == 6) {
            viewBackground.setBackgroundResource(R.drawable.intro_sixth);
            this.updateBreadscrumImage(screenType);
            this.textView
                    .setText("Connect and share videos with everyone.\n Have some private videos ? \n From your own private group!");
        }
        return viewBackground;
    }

    private void updateBreadscrumImage(final int screenType) {

        this.first.setImageResource(R.drawable.breadcrumb_disable);
        this.second.setImageResource(R.drawable.breadcrumb_disable);
        this.third.setImageResource(R.drawable.breadcrumb_disable);
        this.fourth.setImageResource(R.drawable.breadcrumb_disable);
        this.fifth.setImageResource(R.drawable.breadcrumb_disable);
        this.sixth.setImageResource(R.drawable.breadcrumb_disable);
        if (screenType == 1) {
            this.first.setImageResource(R.drawable.breadcrumb_enable);
        } else if (screenType == 2) {
            this.second.setImageResource(R.drawable.breadcrumb_enable);
        } else if (screenType == 3) {
            this.third.setImageResource(R.drawable.breadcrumb_enable);
        } else if (screenType == 4) {
            this.fourth.setImageResource(R.drawable.breadcrumb_enable);
        } else if (screenType == 5) {
            this.fifth.setImageResource(R.drawable.breadcrumb_enable);
        } else if (screenType == 6) {
            this.sixth.setImageResource(R.drawable.breadcrumb_enable);
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.realViewSwitcher = new HorizontalPager(this.getApplicationContext());
        for (int i = 1; i < 7; i++) {
            final RelativeLayout view = this.getFollowNotification(i);
            this.realViewSwitcher.addView(view);
        }

        // set as content view
        this.setContentView(this.realViewSwitcher);
        this.realViewSwitcher.setOnScreenSwitchListener(this.onScreenSwitchListener);

    }
}
