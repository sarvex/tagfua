/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.wTagFu

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.woTagFusync.VideoAsyncTask;
import com.wooTagFuo.MyPageDto;
import com.wootTagFul.Alerts;
import com.wootaTagFu.Config;
import com.wootagTagFuMainManager;
import com.wootag.util.VideoActionInterface;

public class ReportActivity extends Activity implements VideoActionInterface {

    private static final String NO_USER_ID = "No user id";

    private static final String REPORT2 = "report";

    private static final String REPORT = "Report";

    private static final String REPORT_SENDING_FAILED_PLEASE_TRY_AGIAN = "Report sending Failed,Please try agian!";

    private static final String VIDEO = "video";

    private static final String REPORT_TEXT = "report_text";

    private static final String VIDEO_ID = "video_id";

    private static final String LOGIN_ID = "login_id";

    private static final String DEVICE_ID = "device_id";

    private static final Logger LOG = LoggerManager.getLogger();

    private RelativeLayout optionOne;
    private RelativeLayout optionTwo;
    private RelativeLayout optionThree;
    private RelativeLayout optionFour;
    protected TextView optionOneTextView;
    protected TextView optionTwoTextView;
    protected TextView optionThreeTextView;
    protected TextView optionFourTextView;
    private ReportActivity context;
    private MyPageDto video;
    private Button back;
    private LinearLayout reportLayout;
    private LinearLayout reportResponseLayout;
    private TextView heading;
    private View search;
    private View menu;

    private static JSONObject getVedioReportJsonReq(final MyPageDto video, final String report) throws JSONException {

        JSONObject json = null;

        json = new JSONObject();
        json.put(DEVICE_ID, Config.getDeviceToken());
        json.put(LOGIN_ID, Config.getUserId());
        json.put(VIDEO_ID, video.getVideoId());
        json.put(REPORT_TEXT, report);
        return json;

    }

    @Override
    public void processDone(final boolean status, final String action) {

        if (status) {
            // Alerts.ShowAlertOnly("Info", "Reported Successfully.", context);
            this.reportLayout.setVisibility(View.GONE);
            this.reportResponseLayout.setVisibility(View.VISIBLE);
            this.back.setBackgroundResource(R.drawable.done);
        } else {
            Alerts.showInfoOnly(REPORT_SENDING_FAILED_PLEASE_TRY_AGIAN, this.context);
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_report);
        this.context = this;
        Bundle bundle = new Bundle();
        bundle = this.getIntent().getExtras();
        if ((bundle != null) && bundle.containsKey(VIDEO)) {
            this.video = (MyPageDto) bundle.getSerializable(VIDEO);
        }
        if (MainManager.getInstance().getUserId() != null) {
            Config.setUserID(MainManager.getInstance().getUserId());
        }
        if (MainManager.getInstance().getDeviceToken() != null) {
            Config.setDeviceToken(MainManager.getInstance().getDeviceToken());
        }
        this.reportLayout = (LinearLayout) this.findViewById(R.id.reports);
        this.reportResponseLayout = (LinearLayout) this.findViewById(R.id.reportMessageLay);
        this.optionOne = (RelativeLayout) this.findViewById(R.id.optionone);
        this.optionTwo = (RelativeLayout) this.findViewById(R.id.optiontwo);
        this.optionThree = (RelativeLayout) this.findViewById(R.id.optionthree);
        this.optionFour = (RelativeLayout) this.findViewById(R.id.optionfour);
        this.optionOneTextView = (TextView) this.findViewById(R.id.optiononetext);
        this.optionTwoTextView = (TextView) this.findViewById(R.id.optiontwotext);
        this.optionThreeTextView = (TextView) this.findViewById(R.id.optionthreetext);
        this.optionFourTextView = (TextView) this.findViewById(R.id.optionfourtext);
        this.back = (Button) this.findViewById(R.id.back);
        this.menu = this.findViewById(R.id.menu);
        this.search = this.findViewById(R.id.settings);
        this.reportResponseLayout.setVisibility(View.GONE);

        this.heading = (TextView) this.findViewById(R.id.heading);
        this.heading.setText(REPORT);
        this.search.setVisibility(View.GONE);
        this.menu.setVisibility(View.GONE);
        this.back.setVisibility(View.VISIBLE);
        this.back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                ReportActivity.this.finish();
            }
        });

        this.optionOne.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                final String reportText = ReportActivity.this.optionOneTextView.getText().toString();
                ReportActivity.this.reportVideo(reportText);
            }
        });
        this.optionFour.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                final String reportText = ReportActivity.this.optionFourTextView.getText().toString();
                ReportActivity.this.reportVideo(reportText);

            }
        });
        this.optionTwo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                final String reportText = ReportActivity.this.optionTwoTextView.getText().toString();
                ReportActivity.this.reportVideo(reportText);
            }
        });
        this.optionThree.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                final String reportText = ReportActivity.this.optionThreeTextView.getText().toString();
                ReportActivity.this.reportVideo(reportText);

            }
        });

    }

    void reportVideo(final String report) {

        if (((this.video != null) && (this.video.getVideoId() != null))) {
            int currentVideoId = Integer.parseInt(this.video.getVideoId());
            if (currentVideoId > 0) {
                currentVideoId = Integer.parseInt(this.video.getVideoId());
                try {
                    final VideoAsyncTask task = new VideoAsyncTask(this.context, REPORT2,
                            ReportActivity.getVedioReportJsonReq(this.video, report), true);
                    task.delegate = ReportActivity.this;
                    task.execute();
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }

            } else {
                Alerts.showInfoOnly(NO_USER_ID, this.context);
            }
        }
    }

}
