/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.TagFu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.TagFu.facebook.Session;
import com.TagFu.util.MainManager;

public class ShareSettingLogOutActivity extends FriendsListActivity {

    private static final String SOCIALSITE = "socialsite";

    private static final Logger LOG = LoggerManager.getLogger();

    private TextView heading;
    private RelativeLayout searchLayout;
    private Button search;
    private Button menu;
    private RelativeLayout unlink;
    protected ShareSettingLogOutActivity context;
    private String socialSite = "";

    public void showDialog(final Activity activity, final String title) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to unlink this account?").setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(final DialogInterface dialog, final int id) {

                        ShareSettingLogOutActivity.this.logoutAccount();
                        ShareSettingLogOutActivity.this.finish();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(final DialogInterface dialog, final int id) {

                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void logoutFB() {

        final Session session = Session.getActiveSession();
        if ((session != null) && !session.isClosed()) {
            session.closeAndClearTokenInformation();
            session.close();
            Session.setActiveSession(null);
        }
    }

    private void logoutGplus() {

        this.gPlusSignout();
    }

    private void logoutTwitter() {

        MainManager.getInstance().setTwitterOAuthtoken(null);
        MainManager.getInstance().setTwitterSecretKey(null);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_share_setting_log_out);
        this.context = this;
        this.unlink = (RelativeLayout) this.findViewById(R.id.unlink);
        this.heading = (TextView) this.findViewById(R.id.heading);
        this.heading.setText("");
        // searchEdit = (EditText) findViewById(R.id.searchEditText);
        this.searchLayout = (RelativeLayout) this.findViewById(R.id.searchRL);
        this.menu = (Button) this.findViewById(R.id.menu);
        this.search = (Button) this.findViewById(R.id.settings);
        this.searchLayout.setVisibility(View.GONE);
        this.search.setVisibility(View.GONE);
        this.menu.setVisibility(View.GONE);
        final Button back = (Button) this.findViewById(R.id.back);
        back.setVisibility(View.VISIBLE);

        Bundle bundle = new Bundle();
        bundle = this.getIntent().getExtras();
        if ((bundle != null) && bundle.containsKey(SOCIALSITE)) {
            this.socialSite = bundle.getString(SOCIALSITE);
        }
        if (Constant.FACEBOOK.equalsIgnoreCase(this.socialSite)) {
            this.heading.setText(R.string.facebook);
        } else if (Constant.TWITTER.equalsIgnoreCase(this.socialSite)) {
            this.heading.setText(R.string.twitter);
        } else if (Constant.GOOGLE_PLUS.equalsIgnoreCase(this.socialSite)) {
            this.heading.setText("Google+");
        }

        this.unlink.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                ShareSettingLogOutActivity.this.showDialog(ShareSettingLogOutActivity.this.context, "Confirm");
            }
        });
        back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                ShareSettingLogOutActivity.this.finish();
            }
        });
    }

    void logoutAccount() {

        if (Constant.FACEBOOK.equalsIgnoreCase(this.socialSite)) {
            MainManager.getInstance().setFacebookEmail(null);
            this.logoutFB();
        } else if (Constant.TWITTER.equalsIgnoreCase(this.socialSite)) {
            MainManager.getInstance().setTwitterEmail(null);
            this.logoutTwitter();
        } else if (Constant.GOOGLE_PLUS.equalsIgnoreCase(this.socialSite)) {
            MainManager.getInstance().setGPlusEmail(null);
            this.logoutGplus();
        }
    }
}
