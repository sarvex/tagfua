/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.wTagFu

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.woTagFui.CustomDialog;
import com.wooTagFuil.Alerts;
import com.wootTagFul.Config;

public class SettingActivity extends Activity {

    private static final Logger LOG = LoggerManager.getLogger();

    private RelativeLayout editProfile;
    private RelativeLayout myTaggedVidos;
    private RelativeLayout reportProblem;
    private RelativeLayout privacyPolicy;
    private RelativeLayout terms;
    private RelativeLayout help;
    private RelativeLayout shareSettings;
    private RelativeLayout notificationSettings;
    protected SettingActivity context;
    private TextView heading;
    private Button menu;
    private Button search;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.setting_layout);
        this.context = this;
        this.editProfile = (RelativeLayout) this.findViewById(R.id.editmyprofile);
        this.myTaggedVidos = (RelativeLayout) this.findViewById(R.id.taggedvideos);

        this.reportProblem = (RelativeLayout) this.findViewById(R.id.reportproblem);
        this.privacyPolicy = (RelativeLayout) this.findViewById(R.id.privacypolicy);
        this.terms = (RelativeLayout) this.findViewById(R.id.terms);
        this.help = (RelativeLayout) this.findViewById(R.id.help);

        this.shareSettings = (RelativeLayout) this.findViewById(R.id.sharesettings);
        this.notificationSettings = (RelativeLayout) this.findViewById(R.id.notificationsettings);

        this.heading = (TextView) this.findViewById(R.id.heading);
        this.heading.setText("Options");
        this.menu = (Button) this.findViewById(R.id.menu);
        this.search = (Button) this.findViewById(R.id.settings);
        this.search.setVisibility(View.GONE);
        this.menu.setVisibility(View.GONE);
        final Button back = (Button) this.findViewById(R.id.back);
        back.setVisibility(View.VISIBLE);

        back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                SettingActivity.this.finish();
            }
        });

        this.editProfile.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                final Intent intent = new Intent(SettingActivity.this.context, AccountSettingActivity.class);
                SettingActivity.this.startActivity(intent);

            }
        });
        this.myTaggedVidos.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

            }
        });
        this.reportProblem.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                // try {
                SettingActivity.this.showOptionDialogs();

                // } catch (final Exception e) {
                // LOG.i(this.getClass().getName(), "exception " + e.toString());
                // }
            }
        });
        this.privacyPolicy.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                final Intent intent = new Intent(SettingActivity.this.context, WebViewActivity.class);
                intent.putExtra("link", "http://www.wootaTagFuuser/privacy");
                intent.putExtra("heading", "PRIVACY POLICY");
                SettingActivity.this.startActivity(intent);
            }
        });
        this.terms.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                final Intent intent = new Intent(SettingActivity.this.context, WebViewActivity.class);
                intent.putExtra("link", "http://www.wootagTagFuser/terms");
                intent.putExtra("heading", "TERMS OF SERVICE");
                SettingActivity.this.startActivity(intent);
            }
        });
        this.help.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                final Intent intent = new Intent(SettingActivity.this.context, WebViewActivity.class);
                intent.putExtra("link", "http://wootag.com/user/helpcenter");
                intent.putExtra("heading", "HELP CENTER");
                SettingActivity.this.startActivity(intent);
            }
        });
        this.shareSettings.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                final Intent intent = new Intent(SettingActivity.this.context, ShareSettingActivity.class);
                SettingActivity.this.startActivity(intent);
            }
        });
        this.notificationSettings.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                if (!Config.getUserId().equalsIgnoreCase("")) {
                    final int userId = Integer.parseInt(Config.getUserId());
                    if (userId > 0) {
                        final Intent intent = new Intent(SettingActivity.this.context,
                                PushNotificationSettingsActivity.class);
                        intent.putExtra("userid", Config.getUserId());
                        SettingActivity.this.startActivity(intent);
                    } else {
                        Alerts.showAlertOnly("Info", "user id not available", SettingActivity.this.context);
                    }
                } else {
                    Alerts.showAlertOnly("Info", "user id not available", SettingActivity.this.context);
                }
            }
        });

    }

    void showOptionDialogs() {

        // try {
        final View view = LayoutInflater.from(this.context).inflate(R.layout.options_list, null);
        final CustomDialog alertDialog = new CustomDialog(this.context, R.style.CustomStyle);
        // final AlertDialog alertDialog=new AlertDialog.Builder(context).create();
        alertDialog.setContentView(view);
        final WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;

        alertDialog.getWindow().setAttributes(layoutParams);
        alertDialog.show();
        alertDialog.setCanceledOnTouchOutside(false);

        final RelativeLayout delete = (RelativeLayout) view.findViewById(R.id.delete);
        final RelativeLayout reportVideo = (RelativeLayout) view.findViewById(R.id.report);
        final RelativeLayout share = (RelativeLayout) view.findViewById(R.id.share);
        final RelativeLayout spamOrAbuse = (RelativeLayout) view.findViewById(R.id.copysahreurl);
        final RelativeLayout brokenFeature = (RelativeLayout) view.findViewById(R.id.tag);
        final RelativeLayout cancel = (RelativeLayout) view.findViewById(R.id.cancel);

        final TextView spamOrAbuseTextView = (TextView) view.findViewById(R.id.copyURL);
        final TextView brokenFeatureTextView = (TextView) view.findViewById(R.id.tagVideo);
        spamOrAbuseTextView.setText("Spam or abuse");
        brokenFeatureTextView.setText("Broken feature");

        reportVideo.setVisibility(View.GONE);
        delete.setVisibility(View.GONE);
        share.setVisibility(View.GONE);

        cancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                alertDialog.dismiss();
            }
        });
        spamOrAbuse.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                alertDialog.dismiss();
            }
        });

        brokenFeature.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                // try {
                alertDialog.dismiss();
                final Intent intent = new Intent(SettingActivity.this.context, ReportAProblemActivity.class);
                SettingActivity.this.startActivity(intent);
                // } catch (final Exception e) {
                // e.printStackTrace();
                // }

            }
        });

        alertDialog.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(final DialogInterface dialog) {

                alertDialog.dismiss();
            }
        });

        // } catch (final Exception e) {
        // LOG.e("Info", "Exception " + e.toString());
        // }

    }
}
