/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.wTagFu

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.woTagFuodel.Backend;
import com.wooTagFuil.Alerts;
import com.wootTagFul.Config;
import com.wootag.util.MainManager;

public class ReportAProblemActivity extends Activity {

    private static final String FEEDBACK = "feedback";
    private static final String DEVICE_ID = "device_id";
    private static final String LOGIN_ID = "login_id";

    protected static final Logger LOG = LoggerManager.getLogger();

    private TextView heading;
    private Button search;
    private View searchLayout;
    private Button menu;
    protected EditText reportEditTextView;
    protected ReportAProblemActivity context;
    protected String report;

    public JSONObject getJSONRequest() throws JSONException {

        final JSONObject request = new JSONObject();
        request.put(LOGIN_ID, Config.getUserId());
        request.put(DEVICE_ID, Config.getDeviceToken());
        request.put(FEEDBACK, this.report);
        return request;

    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_report_a_problem);
        this.context = this;
        this.heading = (TextView) this.findViewById(R.id.heading);
        this.heading.setText("Report");
        this.searchLayout = this.findViewById(R.id.searchRL);
        this.menu = (Button) this.findViewById(R.id.menu);
        this.search = (Button) this.findViewById(R.id.settings);
        this.search.setBackgroundResource(R.drawable.share_video);
        this.searchLayout.setVisibility(View.GONE);
        this.search.setVisibility(View.VISIBLE);
        this.menu.setVisibility(View.GONE);
        this.reportEditTextView = (EditText) this.findViewById(R.id.reportEditText);
        final Button back = (Button) this.findViewById(R.id.back);
        back.setVisibility(View.VISIBLE);
        if (MainManager.getInstance().getUserId() != null) {
            Config.setUserID(MainManager.getInstance().getUserId());
        }
        if (MainManager.getInstance().getDeviceToken() != null) {
            Config.setDeviceToken(MainManager.getInstance().getDeviceToken());
        }
        back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                ReportAProblemActivity.this.finish();
            }
        });
        this.search.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                ReportAProblemActivity.this.report = ReportAProblemActivity.this.reportEditTextView.getText()
                        .toString();
                if ((ReportAProblemActivity.this.report != null) && (ReportAProblemActivity.this.report.length() > 0)) {
                    final InputMethodManager mgr = (InputMethodManager) ReportAProblemActivity.this
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    mgr.hideSoftInputFromWindow(ReportAProblemActivity.this.reportEditTextView.getWindowToken(), 0);
                    new ReportAProblemAsync().execute();
                    ReportAProblemActivity.this.finish();
                } else {
                    Alerts.showAlertOnly("Info", "Feedback should not be empty", ReportAProblemActivity.this);
                }
            }
        });

    }

    public class ReportAProblemAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(final Void... params) {

            try {
                Backend.feedback(ReportAProblemActivity.this.context, ReportAProblemActivity.this.getJSONRequest()
                        .toString());
            } catch (final JSONException e) {
                LOG.e(e);
            }
            return null;
        }

    }
}
