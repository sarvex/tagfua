/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.TagFu;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.TagFu.ui.Image;

public class ProfilePickViewActivity extends Activity {

    private static final Logger LOG = LoggerManager.getLogger();

    private RelativeLayout profilePicLayout;
    private ImageView profileView;
    private String url;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_profile_pick_view);
        Bundle bundle = new Bundle();
        bundle = this.getIntent().getExtras();
        if ((bundle != null) && bundle.containsKey("url")) {
            this.url = bundle.getString("url");
        }
        // profile pic views
        this.profilePicLayout = (RelativeLayout) this.findViewById(R.id.profic);
        this.profileView = (ImageView) this.findViewById(R.id.profilepicview);
        if ((this.url != null) && !(this.url.trim().equalsIgnoreCase(""))) {
            Image.displayImage(this.url, this, this.profileView, 0);
        }
        this.profilePicLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                ProfilePickViewActivity.this.finish();
            }
        });

    }

}
