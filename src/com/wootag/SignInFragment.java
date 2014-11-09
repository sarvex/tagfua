/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.wootag;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Strings;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wootag.async.LoginAsyncTask;
import com.wootag.async.PlaybackFromBrowser;
import com.wootag.dto.Friend;
import com.wootag.dto.User;
import com.wootag.util.Alerts;
import com.wootag.util.Config;
import com.wootag.util.MainManager;

public class SignInFragment extends FriendsListActivity {

    private static final String YOU = "You";
    private static final String PASSWORD = "password";
    private static final String USER = "user";
    private static final String DEVICE_TOKEN = "device_token";
    private static final String FRIENDS = "friends";
    private static final String PROFILE_PICTURE = "profile_picture";
    private static final String EMAIL = "email";
    private static final String SOCIAL_ID = "social_id";
    private static final String USERNAME = "username";
    private static final String LOGIN_TYPE = "login_type";
    private static final String EMPTY = "";
    private static final String VIDEO_ID = "videoid";
    private static final Logger LOG = LoggerManager.getLogger();

    private EditText userName;
    private EditText password;
    private TextView forgotPassword;

    private Button loginButton;
    private Button registerButton;

    private Button facebookButton;
    private Button gPlusButton;
    private Button twitterButton;
    private User socialLoginDeatils;
    public static SignInFragment signInFragment;
    private boolean fromBrowser;

    private String videoId = EMPTY;

    private ImageView splashImageView;
    private ImageView bannerImageView;

    public String base64Password(final String password) {

        return Base64.encodeToString(password.getBytes(), Base64.NO_WRAP);
    }

    public void checkUserLoggedIn() {

        final String userId = MainManager.getInstance().getUserId();
        if ((userId != null) && (userId.length() > 0)) {
            /** if user is logged in need to be make playback hit and redirected to player page */
            if (!this.videoId.equalsIgnoreCase(EMPTY)) {
                this.splashImageView.setVisibility(View.VISIBLE);
                this.bannerImageView.setVisibility(View.VISIBLE);
                new PlaybackFromBrowser(this.context, this.videoId, userId).execute();
            }
        } else {
            this.fromBrowser = true;
        }
    }

    public JSONArray getFriendListObject(final List<Friend> list) {

        final JSONArray friendArray = new JSONArray();

        for (final Friend friend : list) {
            friendArray.put(friend.getFriendId());
        }
        return friendArray;
    }

    public JSONObject getJSONObject() throws JSONException {

        final JSONObject result = new JSONObject();
        final JSONObject values = new JSONObject();
        values.put(USERNAME, this.userName.getText().toString());
        values.put(PASSWORD, this.password.getText().toString());
        values.put(DEVICE_TOKEN, Config.getDeviceToken());
        values.put(Constant.DEVICE_TYPE, Constant.DEVICE_MODEL);
        values.put(Constant.RESOLUTION, Config.getDeviceResolutionValue());

        result.put(USER, values);

        return result;
    }

    public JSONObject getSocialLoginRequest(final User userDetails, final String type, final List<Friend> list)
            throws JSONException {

        final JSONObject result = new JSONObject();
        final JSONObject values = new JSONObject();
        values.put(LOGIN_TYPE, type);
        values.put(USERNAME, userDetails.getUserName());
        values.put(SOCIAL_ID, userDetails.getUserId());
        values.put(EMAIL, userDetails.getEmailId());
        values.put(PROFILE_PICTURE, userDetails.getUserPickUrl());
        if ((list != null) && (list.size() > 0)) {
            values.put(FRIENDS, this.getFriendListObject(list));
        } else {
            values.put(FRIENDS, EMPTY);//
        }
        values.put(DEVICE_TOKEN, Config.getDeviceToken());
        values.put(Constant.DEVICE_TYPE, Constant.DEVICE_MODEL);
        values.put(Constant.RESOLUTION, Config.getDeviceResolutionValue());

        result.put(USER, values);

        return result;
    }

    @Override
    public void onBackPressed() {

        this.finish();
        if (SplashActivity.splash != null) {
            SplashActivity.splash.finish();
            SplashActivity.splash = null;
        }
        super.onBackPressed();
    }

    @Override
    public void onClick(final View view) {

        LOG.v("OnClick State");
        switch (view.getId()) {
        case R.id.loginButton:
            final String username = this.userName.getText().toString().trim();
            final String passwordTxt = this.password.getText().toString().trim();
            LOG.v("username " + username + " password : " + passwordTxt);
            if (username.equals(EMPTY)) {
                this.userName.setError("Enter username");
                return;
            } else if (passwordTxt.equals(EMPTY)) {
                this.password.setError("Enter password");
                return;
            } else {
                MainManager.getInstance().setUserEmail(username);
                MainManager.getInstance().setPassword(passwordTxt);

                try {
                    new LoginAsyncTask(this, "login", this.getJSONObject(), this.fromBrowser, this.videoId).execute();
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }
            }

            break;

        case R.id.loginRegisterButton:
            LOG.v("Register clicked State");
            final Intent signUpintent = new Intent(SignInFragment.this, SignUpFragment.class);
            this.startActivity(signUpintent);
            break;

        case R.id.forgotpassword:
            final Intent intent = new Intent(SignInFragment.this, ForgotPasswordFragment.class);
            this.startActivity(intent);
            break;

        case R.id.fblogin:
            MainManager.getInstance().setRememberChecked(false);
            MainManager.getInstance().setUserName(EMPTY);
            MainManager.getInstance().setPassword(EMPTY);
            super.onClick(view);
            break;

        case R.id.twitterlogin:
            MainManager.getInstance().setRememberChecked(false);
            MainManager.getInstance().setUserName(EMPTY);
            MainManager.getInstance().setPassword(EMPTY);
            super.onClick(view);
            break;

        case R.id.gpluslogin:
            MainManager.getInstance().setRememberChecked(false);
            MainManager.getInstance().setUserName(EMPTY);
            MainManager.getInstance().setPassword(EMPTY);
            super.onClick(view);
            break;
        default:
            break;

        }
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.signin);
        signInFragment = this;
        this.fromBrowser = false;
        Config.setPlayerRequestStart(false);
        final Intent intent = this.getIntent();
        /**
         * If browser is trying to launch this activity by using uri to playback a video frombrowser flag is changed to
         * true
         */
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            final Uri uri = intent.getData();
            if (uri.getQueryParameter(VIDEO_ID) != null) {
                this.videoId = uri.getQueryParameter(VIDEO_ID);
                this.fromBrowser = true;
            }
        }

        this.splashImageView = (ImageView) this.findViewById(R.id.splashImageView);
        this.splashImageView.setVisibility(View.GONE);
        this.bannerImageView = (ImageView) this.findViewById(R.id.splashBannerImage);
        this.bannerImageView.setVisibility(View.GONE);
        this.userName = (EditText) this.findViewById(R.id.userNameEditText);
        this.password = (EditText) this.findViewById(R.id.passwordEditText);
        this.loginButton = (Button) this.findViewById(R.id.loginButton);
        this.registerButton = (Button) this.findViewById(R.id.loginRegisterButton);
        this.forgotPassword = (TextView) this.findViewById(R.id.forgotpassword);

        this.facebookButton = (Button) this.findViewById(R.id.fblogin);
        this.gPlusButton = (Button) this.findViewById(R.id.gpluslogin);
        this.twitterButton = (Button) this.findViewById(R.id.twitterlogin);

        this.forgotPassword.setOnClickListener(this);
        this.loginButton.setOnClickListener(this);
        this.registerButton.setOnClickListener(this);
        this.facebookButton.setOnClickListener(this);
        this.gPlusButton.setOnClickListener(this);
        this.twitterButton.setOnClickListener(this);

        this.facebookButton.setEnabled(true);
        this.gPlusButton.setEnabled(true);

        if (this.fromBrowser) {
            /** If launching this activity from browser need to be check user logged in */
            this.checkUserLoggedIn();
        }

    }

    /** Once we get the list of social site friends this will be called */
    @Override
    public void processFinish(final List<Friend> friendList, final String socialSite) {

        if ((this.socialLoginDeatils != null) && (this.socialLoginDeatils.getEmailId() != null)
                && (this.socialLoginDeatils.getEmailId().trim().length() > 0)) {

            if (Strings.isNullOrEmpty(this.socialLoginDeatils.getUserName())) {
                this.socialLoginDeatils.setUserName(this.socialLoginDeatils.getEmailId());
            }

            if (Constant.TWITTER.equalsIgnoreCase(socialSite)) {
                LOG.i("process finish received ");

                VideoPlayerApp.getInstance().setTwitterFriendList(friendList);
                try {
                    new LoginAsyncTask(this, "sociallogin", this.getSocialLoginRequest(this.socialLoginDeatils,
                            Constant.TWITTER, friendList), this.fromBrowser, this.videoId).execute();
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }

            } else if (Constant.FACEBOOK.equalsIgnoreCase(socialSite)) {
                LOG.i("process finish received fb ");

                VideoPlayerApp.getInstance().setFacebookFriendsList(friendList);
                this.addSocialLoginContactToFriendList(this.socialLoginDeatils, friendList, socialSite);

                this.socialLoginDeatils.setUserPickURL(this.socialLoginDeatils.getUserPickUrl()
                        + "?width=640&height=640");

                try {
                    new LoginAsyncTask(this, "sociallogin", this.getSocialLoginRequest(this.socialLoginDeatils,
                            Constant.FACEBOOK, friendList), this.fromBrowser, this.videoId).execute();
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }
                LOG.i("fb oncomplete friends.size() " + friendList.size());

            } else if (Constant.GOOGLE_PLUS.equalsIgnoreCase(socialSite)) {
                LOG.i("GooglePlus process finish received gplus ");

                VideoPlayerApp.getInstance().setGoogleFriendList(friendList);
                this.addSocialLoginContactToFriendList(this.socialLoginDeatils, friendList, socialSite);

                this.socialLoginDeatils.setUserPickURL(this.socialLoginDeatils.getUserPickUrl().replace("sz=50",
                        "sz=640"));
                try {
                    new LoginAsyncTask(this, "sociallogin", this.getSocialLoginRequest(this.socialLoginDeatils,
                            "google", friendList), this.fromBrowser, this.videoId).execute();
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }
            }

        } else {
            Alerts.showInfoOnly("Not able to get the email id from social site.", this.context);
        }

        this.gPlusButton.setEnabled(true);
        this.facebookButton.setEnabled(true);
    }

    public void serverResponse(final String message) {

        Alerts.showInfoOnly(message, this);
    }

    @Override
    public void userDetailsFinished(final User userDetails, final String socialSite) {

        super.userDetailsFinished(userDetails, socialSite);
        this.socialLoginDeatils = userDetails;

        if (Constant.FACEBOOK.equalsIgnoreCase(socialSite)) {
            MainManager.getInstance().setFacebookEmail(this.socialLoginDeatils.getEmailId());

        } else if (Constant.TWITTER.equalsIgnoreCase(socialSite)) {
            MainManager.getInstance().setTwitterEmail(this.socialLoginDeatils.getEmailId());

        } else if (Constant.GOOGLE_PLUS.equalsIgnoreCase(socialSite)) {
            MainManager.getInstance().setGPlusEmail(this.socialLoginDeatils.getEmailId());
        }
    }

    /** adding logged in user details to friend list */
    private void addSocialLoginContactToFriendList(final User socialLoginDeatils, final List<Friend> friendList,
            final String socialSite) {

        Friend contact = null;

        if (socialLoginDeatils != null) {
            contact = new Friend();
            contact.setFriendID(socialLoginDeatils.getUserId());
            contact.setFriendName(YOU);// socialLoginDeatils.getUserName()
            contact.setFriendImage(socialLoginDeatils.getUserPickUrl());
            contact.setLocation(socialLoginDeatils.getCountry());
        }

        if (Constant.TWITTER.equalsIgnoreCase(socialSite)) {
            if (friendList == null) {
                final List<Friend> list = new ArrayList<Friend>();
                list.add(contact);
                VideoPlayerApp.getInstance().setTwitterFriendList(list);
            } else {
                friendList.add(0, contact);
                VideoPlayerApp.getInstance().setTwitterFriendList(friendList);
            }

        } else if (Constant.FACEBOOK.equalsIgnoreCase(socialSite)) {
            if (friendList == null) {
                final List<Friend> list = new ArrayList<Friend>();
                list.add(contact);
                VideoPlayerApp.getInstance().setFacebookFriendsList(list);
            } else {
                friendList.add(0, contact);
                VideoPlayerApp.getInstance().setFacebookFriendsList(friendList);
            }

        } else if (Constant.GOOGLE_PLUS.equalsIgnoreCase(socialSite)) {
            if (friendList == null) {
                final List<Friend> list = new ArrayList<Friend>();
                list.add(contact);
                VideoPlayerApp.getInstance().setGoogleFriendList(list);
            } else {
                friendList.add(0, contact);
                VideoPlayerApp.getInstance().setGoogleFriendList(friendList);
            }
        }

    }

    @Override
    protected void onRestart() {

        this.splashImageView.setVisibility(View.GONE);
        this.bannerImageView.setVisibility(View.GONE);
        super.onRestart();
    }

    @Override
    protected void onResume() {

        this.userName.setText(MainManager.getInstance().getUserEmail());
        this.password.setText(MainManager.getInstance().getPassword());

        super.onResume();
    }

}
