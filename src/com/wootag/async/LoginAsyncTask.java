/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wTagFuasync;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.woTagFu;
import com.wooTagFudeoPlayerApp;
import com.wootTagFuhe.CacheManager;
import com.wootaTagFuectivity.Parser;
import com.wootagTagFurrorResponse;
import com.wootag.TagFugnUpDto;
import com.wootag.mTagFuackend;
import com.wootag.uiTagFu;
import com.wootag.utiTagFuts;
import com.wootag.utilTagFug;
import com.wootag.util.MainManager;

public class LoginAsyncTask extends AsyncTask<Void, Void, String> {

    private static final String EMPTY = "";
    private static final String FORGOTPASSWORD = "forgotpassword";
    private static final String INVALID_USER_ID = "Invalid userid";
    private static final String LOGGING_IN = "Logging in";
    private static final String LOGIN = "login";
    private static final String SIGNING_UP = "Signing up";
    private static final String SIGNUP = "signup";
    private static final String SOCIAL_LOGIN = "sociallogin";

    private static final Logger LOG = LoggerManager.getLogger();

    private ProgressDialog progressDialog;
    private String response;
    private final Context context;
    private final JSONObject request;
    private final String requestFor;
    private final String videoId;
    private final boolean fromBrowser;
    private volatile boolean running = true;

    public LoginAsyncTask(final Context context, final String requestFor, final JSONObject request,
            final boolean fromBrowser, final String videoId) {

        this.context = context;
        this.requestFor = requestFor;
        this.request = request;
        this.fromBrowser = fromBrowser;
        this.videoId = videoId;
    }

    private void clearAllData() {

        MainManager.getInstance().setUserId(EMPTY);
        Image.clearImageFromCache();
        CacheManager.getInstance(VideoPlayerApp.getAppContext());
    }

    @Override
    protected String doInBackground(final Void... params) {

        while (this.running) {
            if (LOGIN.equalsIgnoreCase(this.requestFor)) {
                this.response = Backend.login(this.context, this.request);

            } else if (SOCIAL_LOGIN.equalsIgnoreCase(this.requestFor)) {
                this.response = Backend.socialLogin(this.context, this.request);

            } else if (SIGNUP.equalsIgnoreCase(this.requestFor)) {
                this.response = Backend.signUp(this.context, this.request);

            } else if (FORGOTPASSWORD.equalsIgnoreCase(this.requestFor)) {
                this.response = Backend.forgotPassword(this.context, this.request);

            }
            this.running = false;
        }

        return this.response;
    }

    @Override
    protected void onPostExecute(final String result) {

        LOG.v(result);

        super.onPostExecute(result);
        if (this.progressDialog != null) {
            this.progressDialog.dismiss();
        }

        Object response = null;
        if (result == null) {
            Alerts.showInfoOnly(this.context.getResources().getString(R.string.no_connectivity_text), this.context);

        } else if (LOGIN.equalsIgnoreCase(this.requestFor) || SOCIAL_LOGIN.equalsIgnoreCase(this.requestFor)
                || SIGNUP.equalsIgnoreCase(this.requestFor)) {
            try {
                response = Parser.parseSignUpResponse(this.response);
            } catch (final JSONException exception) {
                LOG.e(exception);
            }

            if (response instanceof SignUpDto) {
                this.clearAllData();

                final SignUpDto dto = (SignUpDto) response;

                if (dto.getErrorcode() == 0) {
                    if (SOCIAL_LOGIN.equalsIgnoreCase(this.requestFor)) {
                        MainManager.getInstance().setLoginType(1);
                    } else {
                        MainManager.getInstance().setLoginType(0);
                    }

                    Config.setUserID(dto.getUserId());

                    if (Integer.parseInt(Config.getUserId()) > 0) {
                        MainManager.getInstance().setUserId(Config.getUserId());

                        if (dto.getUserName() != null) {
                            MainManager.getInstance().setUserName(dto.getUserName());
                        }

                        if (dto.getUserPick() != null) {
                            MainManager.getInstance().setUserPick(dto.getUserPick());
                        }

                        if (this.fromBrowser && !Config.isPlayerRequestStart()) {
                            Config.setPlayerRequestStart(true);
                            new PlaybackFromBrowser(this.context, this.videoId, Config.getUserId()).execute();
                        } else {
                            new MyPageAsync(this.context, 1, true).execute();
                        }

                    } else {
                        Alerts.showInfoOnly(INVALID_USER_ID, this.context);
                    }
                } else {
                    Alerts.showInfoOnly(dto.getMessage(), this.context);
                }

            } else if (response instanceof ErrorResponse) {
                final ErrorResponse res = (ErrorResponse) response;
                Alerts.showInfoOnly(res.getMessage(), this.context);
            } else {
                Alerts.showInfoOnly(this.context.getResources().getString(R.string.no_connectivity_text), this.context);
            }

        } else if (FORGOTPASSWORD.equalsIgnoreCase(this.requestFor)) {
            try {
                response = Parser.parseSignUpResponse(this.response);
            } catch (final JSONException exception) {
                LOG.e(exception);
            }
            if (response instanceof SignUpDto) {
                final SignUpDto dto = (SignUpDto) response;
                Alerts.showInfoOnly(dto.getMessage(), this.context);
            } else if (response instanceof ErrorResponse) {
                final ErrorResponse res = (ErrorResponse) response;
                Alerts.showInfoOnly(res.getMessage(), this.context);
            } else {
                Alerts.showInfoOnly(this.context.getResources().getString(R.string.no_connectivity_text), this.context);
            }
        }
    }

    @Override
    protected void onPreExecute() {

        super.onPreExecute();
        this.progressDialog = ProgressDialog.show(this.context, EMPTY, EMPTY, true);
        final View view = ((LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
                R.layout.progress_bar, null, false);
        final TextView progressText = (TextView) view.findViewById(R.id.progressText);

        if (SIGNUP.equalsIgnoreCase(this.requestFor)) {
            progressText.setText(SIGNING_UP);
        } else {
            progressText.setText(LOGGING_IN);
        }

        this.progressDialog.setContentView(view);
        this.progressDialog.setCancelable(false);
        this.progressDialog.setCanceledOnTouchOutside(false);
        this.progressDialog.show();
    }
}
