/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.wootag;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wootag.dto.User;
import com.wootag.util.Config;
import com.wootag.util.MainManager;

public class ShareSettingActivity extends FriendsListActivity {

    private static final Logger LOG = LoggerManager.getLogger();

    private TextView gPlusMail;
    private TextView fbMail;
    private RelativeLayout googleSahre;
    private RelativeLayout fbShare;
    private RelativeLayout twitterShare;
    private TextView heading;
    private RelativeLayout searchLayout;
    private Button search;
    private Button menu;
    private TextView twitterMail;

    @Override
    public void onClick(final View view) {

        switch (view.getId()) {

        case R.id.fbAuthentication:
            if (MainManager.getInstance().getFbEmail() == null) {
                super.onClick(view);
            } else {
                final Intent intent = new Intent(this.context, ShareSettingLogOutActivity.class);
                intent.putExtra("socialsite", Constant.FACEBOOK);
                this.startActivity(intent);
            }
            break;
        case R.id.twitterAuthentication:
            if (MainManager.getInstance().getTwitterEmail() == null) {
                super.onClick(view);
            } else {
                final Intent intent = new Intent(this.context, ShareSettingLogOutActivity.class);
                intent.putExtra("socialsite", Constant.TWITTER);
                this.startActivity(intent);
            }
            break;
        case R.id.gplusAuthentication:
            if (MainManager.getInstance().getGplusEmail() == null) {
                super.onClick(view);
            } else {
                final Intent intent = new Intent(this.context, ShareSettingLogOutActivity.class);
                intent.putExtra("socialsite", Constant.GOOGLE_PLUS);
                this.startActivity(intent);
            }
            break;

        default:
            break;
        }
    }

    @Override
    public void userDetailsFinished(final User userDetails, final String socialSite) {

        super.userDetailsFinished(userDetails, socialSite);
        if (Constant.FACEBOOK.equalsIgnoreCase(socialSite)) {
            if (userDetails.getEmailId() != null) {
                MainManager.getInstance().setFacebookEmail(userDetails.getEmailId());
                this.fbMail.setText(Config.getFacebookLoggedUserId());
                this.fbMail.setVisibility(View.VISIBLE);
            }
        } else if (Constant.TWITTER.equalsIgnoreCase(socialSite)) {
            MainManager.getInstance().setTwitterEmail(userDetails.getEmailId());
            this.twitterMail.setText(userDetails.getEmailId());
            this.twitterMail.setVisibility(View.VISIBLE);
        } else if (Constant.GOOGLE_PLUS.equalsIgnoreCase(socialSite) && (userDetails.getEmailId() != null)) {
            MainManager.getInstance().setGPlusEmail(userDetails.getEmailId());
            this.gPlusMail.setText(userDetails.getEmailId());
            this.gPlusMail.setVisibility(View.VISIBLE);
        }
    }

    private void loadViews() {

        this.fbMail = (TextView) this.findViewById(R.id.fbmail);
        this.gPlusMail = (TextView) this.findViewById(R.id.gplusmail);
        this.twitterMail = (TextView) this.findViewById(R.id.twittermail);
        this.fbShare = (RelativeLayout) this.findViewById(R.id.fbAuthentication);
        this.twitterShare = (RelativeLayout) this.findViewById(R.id.twitterAuthentication);
        this.googleSahre = (RelativeLayout) this.findViewById(R.id.gplusAuthentication);

        this.heading = (TextView) this.findViewById(R.id.heading);
        this.heading.setText(R.string.share_settings);
        // searchEdit = (EditText) findViewById(R.id.searchEditText);
        this.searchLayout = (RelativeLayout) this.findViewById(R.id.searchRL);
        this.menu = (Button) this.findViewById(R.id.menu);
        this.search = (Button) this.findViewById(R.id.settings);
        this.searchLayout.setVisibility(View.GONE);
        this.search.setVisibility(View.GONE);
        this.menu.setVisibility(View.GONE);
        final Button back = (Button) this.findViewById(R.id.back);
        back.setVisibility(View.VISIBLE);
        this.fbShare.setOnClickListener(this);
        this.twitterShare.setOnClickListener(this);
        this.googleSahre.setOnClickListener(this);
        if (MainManager.getInstance().getFbEmail() != null) {
            this.fbMail.setText(MainManager.getInstance().getFbEmail());
            this.fbMail.setVisibility(View.VISIBLE);
        }
        if (MainManager.getInstance().getGplusEmail() != null) {
            this.gPlusMail.setText(MainManager.getInstance().getGplusEmail());
            this.gPlusMail.setVisibility(View.VISIBLE);
        }
        if (MainManager.getInstance().getTwitterEmail() != null) {
            this.twitterMail.setText(MainManager.getInstance().getTwitterEmail());
            this.twitterMail.setVisibility(View.VISIBLE);
        }
        back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                ShareSettingActivity.this.finish();

            }
        });
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_share_setting);
        this.loadViews();
    }

    @Override
    protected void onRestart() {

        super.onRestart();

        if (MainManager.getInstance().getFbEmail() != null) {
            this.fbMail.setText(MainManager.getInstance().getFbEmail());
            this.fbMail.setVisibility(View.VISIBLE);
        } else {
            this.fbMail.setText("");
            this.fbMail.setVisibility(View.VISIBLE);
        }
        if (MainManager.getInstance().getGplusEmail() != null) {
            this.gPlusMail.setText(MainManager.getInstance().getGplusEmail());
            this.gPlusMail.setVisibility(View.VISIBLE);
        } else {
            this.gPlusMail.setText("");
            this.gPlusMail.setVisibility(View.VISIBLE);
        }
        if (MainManager.getInstance().getTwitterEmail() != null) {
            this.twitterMail.setText(MainManager.getInstance().getTwitterEmail());
            this.twitterMail.setVisibility(View.VISIBLE);
        } else {
            this.twitterMail.setText("");
            this.twitterMail.setVisibility(View.VISIBLE);
        }

    }

}
