/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.wootag;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wootag.async.LoginAsyncTask;
import com.wootag.util.Alerts;

public class ForgotPasswordFragment extends Activity implements OnClickListener {

    private static final String USER = "user";

    private static final String EMAIL = "email";

    private static final String FORGOTPASSWORD = "forgotpassword";

    private static final String ENTER_VALID_EMAIL = "Enter valid email";

    private static final String EMPTY = "";

    private static final Logger LOG = LoggerManager.getLogger();

    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private EditText emailEditText;
    private Button emailPasssword;
    private Button cancel;

    private static Pattern pattern;
    public static ForgotPasswordFragment forgotPasswordFragment;
    private Matcher matcher;

    static {
        pattern = Pattern.compile(EMAIL_PATTERN);
    }

    public JSONObject getJSONObject() throws JSONException {

        final JSONObject obj = new JSONObject();
        final JSONObject values = new JSONObject();
        values.put(EMAIL, this.emailEditText.getText().toString().trim());
        obj.put(USER, values);

        return obj;
    }

    @Override
    public void onClick(final View view) {

        if (R.id.emailNewPassword == view.getId()) {
            final String email = this.emailEditText.getText().toString().trim();
            if (email.equals(EMPTY)) {
                this.emailEditText.setError(ENTER_VALID_EMAIL);
            } else {
                try {
                    new LoginAsyncTask(this, FORGOTPASSWORD, this.getJSONObject(), false, EMPTY).execute();
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }
            }
        } else {
            this.finish();
        }
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.forgot_password);
        forgotPasswordFragment = this;
        this.emailEditText = (EditText) this.findViewById(R.id.emailEditText);
        this.emailPasssword = (Button) this.findViewById(R.id.emailNewPassword);
        this.cancel = (Button) this.findViewById(R.id.cancel);
        this.emailPasssword.setOnClickListener(this);
        this.cancel.setOnClickListener(this);
        LOG.v("onCreate State");

    }

    public void serverResponse(final String msg) {

        Alerts.showInfoOnly(msg, this);
    }

    public boolean validate(final String email) {

        this.matcher = pattern.matcher(email);
        return this.matcher.matches();

    }
}
