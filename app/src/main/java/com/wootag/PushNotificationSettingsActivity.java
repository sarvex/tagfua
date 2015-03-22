/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.TagFu;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.TagFu.dto.ErrorResponse;
import com.TagFu.dto.PushNotificationSetting;
import com.TagFu.model.Backend;
import com.TagFu.util.Alerts;
import com.TagFu.util.Config;

public class PushNotificationSettingsActivity extends Activity {

    static final Logger LOG = LoggerManager.getLogger();

    private Button menu;
    private Button search;
    protected PushNotificationSettingsActivity context;
    protected RadioButton comemntOnCheckbox;
    private RadioButton commentOffCheckbox;
    private RadioButton likesOffCheckbox;
    protected RadioButton likesOnCheckbox;
    private RadioButton mentionsOffCheckbox;
    protected RadioButton mentionsOnCheckbox;
    protected RadioGroup commentRadiobuttongroup;
    protected RadioGroup likesRadionButtonGroup;
    protected RadioGroup mentionsRadiobuttongroup;
    private RelativeLayout searchLayout;
    private TextView heading;
    protected String userId;

    public void loadAccountDetails(final PushNotificationSetting info) {

        if (info.getComments() == 1) {
            this.commentOffCheckbox.setChecked(false);
            this.comemntOnCheckbox.setChecked(true);
        } else {
            this.commentOffCheckbox.setChecked(true);
            this.comemntOnCheckbox.setChecked(false);
        }
        if (info.getLikes() == 1) {
            this.likesOffCheckbox.setChecked(false);
            this.likesOnCheckbox.setChecked(true);
        } else {
            this.likesOffCheckbox.setChecked(true);
            this.likesOnCheckbox.setChecked(false);
        }
        if (info.getMentions() == 1) {
            this.mentionsOffCheckbox.setChecked(false);
            this.mentionsOnCheckbox.setChecked(true);
        } else {
            this.mentionsOffCheckbox.setChecked(true);
            this.mentionsOnCheckbox.setChecked(false);
        }

    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_push_notification_settings);
        this.context = this;

        this.mentionsOffCheckbox = (RadioButton) this.findViewById(R.id.mentionsoff);
        this.mentionsOnCheckbox = (RadioButton) this.findViewById(R.id.mentionson);

        this.likesOffCheckbox = (RadioButton) this.findViewById(R.id.likesoff);
        this.likesOnCheckbox = (RadioButton) this.findViewById(R.id.likeson);

        this.commentOffCheckbox = (RadioButton) this.findViewById(R.id.commentoff);
        this.comemntOnCheckbox = (RadioButton) this.findViewById(R.id.commenton);

        this.heading = (TextView) this.findViewById(R.id.heading);
        this.heading.setText("Notifications");
        this.searchLayout = (RelativeLayout) this.findViewById(R.id.searchRL);
        this.menu = (Button) this.findViewById(R.id.menu);
        this.search = (Button) this.findViewById(R.id.settings);
        this.searchLayout.setVisibility(View.GONE);
        this.search.setVisibility(View.GONE);
        this.menu.setVisibility(View.GONE);
        final Button back = (Button) this.findViewById(R.id.back);
        back.setVisibility(View.VISIBLE);

        this.likesRadionButtonGroup = (RadioGroup) this.findViewById(R.id.likesRadiobuttongroup);
        this.commentRadiobuttongroup = (RadioGroup) this.findViewById(R.id.commentRadiobuttongroup);
        this.mentionsRadiobuttongroup = (RadioGroup) this.findViewById(R.id.mentionsRadiobuttongroup);

        /** while check on any of the radio button in back end need to send update setting request to server */
        this.likesRadionButtonGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(final RadioGroup group, final int checkedId) {

                final RadioButton checkedRadioButton = (RadioButton) PushNotificationSettingsActivity.this.likesRadionButtonGroup
                        .findViewById(checkedId);
                final int checkedIndex = PushNotificationSettingsActivity.this.likesRadionButtonGroup
                        .indexOfChild(checkedRadioButton);
                new updatePushNotificationSettingDetails().execute();
            }
        });
        this.commentRadiobuttongroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(final RadioGroup group, final int checkedId) {

                final RadioButton checkedRadioButton = (RadioButton) PushNotificationSettingsActivity.this.commentRadiobuttongroup
                        .findViewById(checkedId);
                final int checkedIndex = PushNotificationSettingsActivity.this.commentRadiobuttongroup
                        .indexOfChild(checkedRadioButton);
                new updatePushNotificationSettingDetails().execute();
            }
        });
        this.mentionsRadiobuttongroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(final RadioGroup group, final int checkedId) {

                final RadioButton checkedRadioButton = (RadioButton) PushNotificationSettingsActivity.this.mentionsRadiobuttongroup
                        .findViewById(checkedId);
                final int checkedIndex = PushNotificationSettingsActivity.this.mentionsRadiobuttongroup
                        .indexOfChild(checkedRadioButton);
                new updatePushNotificationSettingDetails().execute();
            }
        });

        Bundle bundle = new Bundle();
        bundle = this.getIntent().getExtras();
        if ((bundle != null) && bundle.containsKey("userid")) {
            this.userId = bundle.getString("userid");
        }
        if ((this.userId != null) && !this.userId.equalsIgnoreCase("")) {
            new LoadPushNotificationSettingDetails(true).execute();
        }

        back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                PushNotificationSettingsActivity.this.finish();
            }
        });

    }

    public class LoadPushNotificationSettingDetails extends AsyncTask<Void, Void, Void> {

        private static final String NO_RESPONSE_AVAILABLE = "No Response available";
        private static final String EMPTY = "";
        private ProgressDialog progress;
        private final boolean progressVisible;
        private Object accountDetails;

        public LoadPushNotificationSettingDetails(final boolean progressVisible) {

            this.progressVisible = progressVisible;
        }

        @Override
        protected Void doInBackground(final Void... params) {

            try {
                this.accountDetails = Backend.getPushNotificationSettings(
                        PushNotificationSettingsActivity.this.context, PushNotificationSettingsActivity.this.userId);
            } catch (final JSONException exception) {
                LOG.e(exception);
            }

            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {

            super.onPostExecute(result);
            if (this.progressVisible) {
                this.progress.dismiss();
            }
            if (this.accountDetails != null) {
                if (this.accountDetails instanceof PushNotificationSetting) {
                    final PushNotificationSetting Info = (PushNotificationSetting) this.accountDetails;
                    PushNotificationSettingsActivity.this.loadAccountDetails(Info);
                } else if (this.accountDetails instanceof ErrorResponse) {
                    final ErrorResponse error = (ErrorResponse) this.accountDetails;
                    Alerts.showInfoOnly(error.toString(), PushNotificationSettingsActivity.this.context);
                }
            } else {
                Alerts.showInfoOnly(NO_RESPONSE_AVAILABLE, PushNotificationSettingsActivity.this.context);
            }
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            if (this.progressVisible) {
                this.progress = ProgressDialog.show(PushNotificationSettingsActivity.this.context, EMPTY, EMPTY, true);
                this.progress
                        .setContentView(((LayoutInflater) PushNotificationSettingsActivity.this.context
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.progress_bar,
                                null, false));
                this.progress.setCancelable(false);
                this.progress.setCanceledOnTouchOutside(false);
                this.progress.show();
            }
        }

    }

    public class updatePushNotificationSettingDetails extends AsyncTask<Void, Void, Void> {

        private static final String _1 = "1";
        private static final String PUSH = "push";
        private static final String MENTIONS2 = "mentions";
        private static final String FEEDS = "feeds";
        private static final String LIKES = "likes";
        private static final String COMMENTS = "comments";
        private static final String FOLLOWERS = "followers";
        private static final String ENABLE_PN = "enable_pn";
        private static final String USER_ID = "user_id";
        ProgressDialog progressDialog;
        boolean progressVisible;

        public JSONObject getJSONRequest() throws JSONException {

            int comment = 0;
            int likes = 0;
            int mentions = 0;

            if (PushNotificationSettingsActivity.this.comemntOnCheckbox.isChecked()) {
                comment = 1;
            }
            if (PushNotificationSettingsActivity.this.likesOnCheckbox.isChecked()) {
                likes = 1;
            }
            if (PushNotificationSettingsActivity.this.mentionsOnCheckbox.isChecked()) {
                mentions = 1;
            }
            final JSONObject request = new JSONObject();
            final JSONObject obj = new JSONObject();
            obj.put(USER_ID, Config.getUserId());//
            obj.put(ENABLE_PN, _1);
            obj.put(FOLLOWERS, _1);
            obj.put(COMMENTS, comment);
            obj.put(LIKES, likes);
            obj.put(FEEDS, _1);
            obj.put(MENTIONS2, mentions);
            obj.put(Constant.DEVICE_TYPE, Constant.DEVICE_MODEL);
            obj.put(Constant.RESOLUTION, Config.getDeviceResolutionValue());
            request.put(PUSH, obj);

            return request;

        }

        @Override
        protected Void doInBackground(final Void... params) {

            try {
                Backend.updatePushNotificationSettings(PushNotificationSettingsActivity.this.context,
                        this.getJSONRequest());
            } catch (final JSONException e) {
                LOG.e(e);
            }

            return null;
        }

    }

}
