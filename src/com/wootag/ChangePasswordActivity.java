/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.wootag;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wootag.dto.ErrorResponse;
import com.wootag.model.Backend;
import com.wootag.util.Alerts;
import com.wootag.util.Config;

public class ChangePasswordActivity extends Activity {

    private static final String NEW_PASSWORD_AND_CONFIRM_NEW_PASSWORD_SHOULD_BE_SAME = "new password and confirm new password should  be same!";
    private static final String NEW_PASSWORD_SHOULD_BE_MINIMUM_5_CHARECTERS = "new password should  be minimum 5 charecters!";
    private static final String OLD_PASSWORD_SHOULD_NOT_BE_EMPTY = "Old password should not be empty!";
    private static final String USER = "user";
    private static final String UPDATED_PASSWORD = "updated_password";
    private static final String CURRENT_PASSWORD = "current_password";
    private static final String USER_ID = "user_id";

    protected static final Logger LOG = LoggerManager.getLogger();

    private Button cancel;
    private Button save;

    protected Context context;
    protected EditText confirmNewPwd;
    protected EditText newPwd;
    protected EditText oldPwd;

    public JSONObject getJSONRequest() throws JSONException {

        final JSONObject request = new JSONObject();
        final JSONObject obj = new JSONObject();
        obj.put(USER_ID, Config.getUserId());
        obj.put(CURRENT_PASSWORD, this.oldPwd.getText().toString());
        obj.put(UPDATED_PASSWORD, this.newPwd.getText().toString());
        request.put(USER, obj);

        return request;

    }

    private void loadViews() {

        this.oldPwd = (EditText) this.findViewById(R.id.oldpwd);
        this.newPwd = (EditText) this.findViewById(R.id.newpwd);
        this.confirmNewPwd = (EditText) this.findViewById(R.id.confirmnewpwd);
        this.cancel = (Button) this.findViewById(R.id.cancel);
        this.save = (Button) this.findViewById(R.id.save);
        this.cancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                ChangePasswordActivity.this.finish();

            }
        });
        this.save.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                final String oldPwdString = ChangePasswordActivity.this.oldPwd.getText().toString();
                final String newPwdString = ChangePasswordActivity.this.newPwd.getText().toString();
                final String confirmPwdString = ChangePasswordActivity.this.confirmNewPwd.getText().toString();
                if ((oldPwdString != null) && (oldPwdString.length() <= 0)) {
                    Alerts.showInfoOnly(OLD_PASSWORD_SHOULD_NOT_BE_EMPTY, ChangePasswordActivity.this.context);
                } else if ((newPwdString != null) && (newPwdString.length() < 5)) {
                    Alerts.showInfoOnly(NEW_PASSWORD_SHOULD_BE_MINIMUM_5_CHARECTERS,
                            ChangePasswordActivity.this.context);
                } else if ((newPwdString != null) && !newPwdString.equalsIgnoreCase(confirmPwdString)) {
                    Alerts.showInfoOnly(NEW_PASSWORD_AND_CONFIRM_NEW_PASSWORD_SHOULD_BE_SAME,
                            ChangePasswordActivity.this.context);
                } else {
                    new changePasswordAsyne(true).execute();

                }

            }
        });
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_change_password);
        this.context = this;
        this.loadViews();
    }

    public class changePasswordAsyne extends AsyncTask<Void, Void, Void> {

        private static final String EMPTY = "";
        private final boolean isProgressShow;
        private ProgressDialog progressDialog;
        private Object returnObj;

        public changePasswordAsyne(final boolean isProgressShow) {

            this.isProgressShow = isProgressShow;
        }

        @Override
        protected Void doInBackground(final Void... params) {

            try {
                this.returnObj = Backend.changePassword(ChangePasswordActivity.this.context,
                        ChangePasswordActivity.this.getJSONRequest().toString());

            } catch (final JSONException exception) {
                LOG.e(exception);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {

            super.onPostExecute(result);
            if (this.isProgressShow) {
                this.progressDialog.dismiss();
            }

            if (this.returnObj != null) {
                if (this.returnObj instanceof Boolean) {
                    Alerts.showInfoOnly("Password updated successfully.", ChangePasswordActivity.this.context);
                } else if (this.returnObj instanceof ErrorResponse) {
                    final ErrorResponse error = (ErrorResponse) this.returnObj;
                    Alerts.showInfoOnly(error.toString(), ChangePasswordActivity.this.context);
                }
            } else {
                Alerts.showInfoOnly("No Response available", ChangePasswordActivity.this.context);
            }
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            if (this.isProgressShow) {
                // pd = ProgressDialog.show(context, "Loading..",
                // "Please wait");

                this.progressDialog = ProgressDialog.show(ChangePasswordActivity.this.context, EMPTY, EMPTY, true);
                this.progressDialog
                        .setContentView(((LayoutInflater) ChangePasswordActivity.this.context
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.progress_bar,
                                null, false));
                this.progressDialog.setCancelable(false);
                this.progressDialog.setCanceledOnTouchOutside(false);
                this.progressDialog.show();
            }
        }

    }
}
