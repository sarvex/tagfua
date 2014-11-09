/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.wootag;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

public class QuickLinkActivity extends Activity {

    private static final Logger LOG = LoggerManager.getLogger();

    public static QuickLinkActivity quickLinnkActivity;

    private ImageView userImage;
    private TextView userName;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_quick_link);
        quickLinnkActivity = this;
    }

}
