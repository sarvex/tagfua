/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.TagFu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.TagFu.util.Alerts;

public class BackGroundDialogs extends Activity {

    private static final String DISMISS = "DISMISS";

    private static final Logger LOG = LoggerManager.getLogger();

    public BroadcastReceiver receiver;
    public AlertDialog mAlert;

    /**
     * @brief Shows an alert message using a Dialog window.
     * @param reason :the message you wish to display in the alert
     */
    public void showAlert(final String reason) {

        Alerts.showAlert("Info", reason, this);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_back_ground_dialogs);

    }

    @Override
    protected void onPause() {

        if ((this.mAlert != null) && this.mAlert.isShowing()) {
            this.mAlert.dismiss();
        }
        this.finish();
        super.onPause();
    }

    @Override
    protected void onResume() {

        super.onResume();
        final Bundle extras = this.getIntent().getExtras();
        final String reason = extras.getString(Intent.EXTRA_TEXT);
        if (DISMISS.equalsIgnoreCase(reason)) {
            this.finish();
        } else {
            this.showAlert(reason);// invoke the new dialog to show
        }
    }
}
