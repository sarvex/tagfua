/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.TagFu;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Strings;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.TagFu.async.LoginAsyncTask;
import com.TagFu.dto.Friend;
import com.TagFu.dto.User;
import com.TagFu.util.Alerts;
import com.TagFu.util.Config;
import com.TagFu.util.MainManager;

public class SignUpFragment extends FriendsListActivity {

    private static final String SIGNUP = "signup";

    private static final String ALL_FIELDS_VALID = "All Fields Valid";

    private static final String CONFIRM_PASSWORD_IS_WRONG = "Confirm Password is wrong";

    private static final String ENTER_PASSWORD = "Enter Password";

    private static final String ENTER_CONFIRM_PASSWORD = "Enter confirm Password";

    private static final String ENTER_VALID_EMAIL2 = "Enter Valid Email";

    private static final String ENTER_VALID_EMAIL = "Enter valid email";

    private static final String ENTER_USERNAME = "Enter username";

    private static final String FRIENDS = "friends";

    private static final String PROFILE_PICTURE = "profile_picture";

    private static final String SOCIAL_ID = "social_id";

    private static final String LOGIN_TYPE = "login_type";

    private static final String SZ_640 = "sz=640";

    private static final String SZ_50 = "sz=50";

    private static final String NOT_ABLE_TO_GET_THE_EMAIL_ID_FROM_SOCIAL_SITE = "Not able to get the email id from social site.";

    private static final String GOOGLE = "google";

    private static final String TWITTER = "twitter";

    private static final String FACEBOOK = "facebook";

    private static final String SOCIALLOGIN = "sociallogin";

    private static final String WIDTH_640_HEIGHT_640 = "?width=640&height=640";

    private static final String EMPTY = "";

    private static final String USER = "user";

    private static final String DEVICE_TOKEN = "device_token";

    private static final String PASSWORD = "password";

    private static final String EMAIL = "email";

    private static final String USERNAME = "username";

    private static final Logger LOG = LoggerManager.getLogger();

    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private EditText userName;
    private EditText emailEditText;
    private EditText passwordTxt;
    private EditText confirmPassword;

    private Button register;

    private static Pattern pattern;

    public static SignUpFragment signUpFragment;
    private Matcher matcher;

    private User socialLoginDeatils;

    private Button cancelRegister;

    private Button facebook;

    private Button gplus;

    static {
        pattern = Pattern.compile(EMAIL_PATTERN);
    }

    public JSONArray getFriendListObject(final List<Friend> list) {

        final JSONArray friendArray = new JSONArray();
        // try {
        for (int i = 0; i < list.size(); i++) {
            final Friend friend = list.get(i);
            friendArray.put(friend.getFriendId());
        }
        // } catch (final Exception e) {
        // e.printStackTrace();
        // }
        return friendArray;
    }

    public JSONObject getJSONObject() throws JSONException {

        final JSONObject obj = new JSONObject();
        final JSONObject values = new JSONObject();
        values.put(USERNAME, this.userName.getText().toString().trim());
        values.put(EMAIL, this.emailEditText.getText().toString().trim());
        values.put(PASSWORD, this.passwordTxt.getText().toString().trim());
        values.put(DEVICE_TOKEN, EMPTY);
        obj.put(Constant.DEVICE_TYPE, Constant.DEVICE_MODEL);
        obj.put(Constant.RESOLUTION, Config.getDeviceResolutionValue());
        obj.put(USER, values);

        return obj;
    }

    public JSONObject getSocialLoginRequest(final User userDetails, final String type, final List<Friend> list)
            throws JSONException {

        final JSONObject obj = new JSONObject();
        final JSONObject values = new JSONObject();
        values.put(LOGIN_TYPE, type);
        values.put(USERNAME, userDetails.getUserName());
        values.put(SOCIAL_ID, userDetails.getUserId());
        values.put(EMAIL, userDetails.getEmailId());
        values.put(PROFILE_PICTURE, userDetails.getUserPickUrl());
        if ((list != null) && (list.size() > 0)) {
            values.put(FRIENDS, this.getFriendListObject(list));//
        } else {
            values.put(FRIENDS, EMPTY);//
        }
        values.put(DEVICE_TOKEN, Config.getDeviceToken());
        values.put(Constant.DEVICE_TYPE, Constant.DEVICE_MODEL);
        values.put(Constant.RESOLUTION, Config.getDeviceResolutionValue());
        obj.put(USER, values);

        return obj;
    }

    @Override
    public void onClick(final View v) {

        // super.onClick(v);
        switch (v.getId()) {
        case R.id.signUpRegister:

            final String username = this.userName.getText().toString().trim();
            final String email = this.emailEditText.getText().toString().trim();
            final String password = this.passwordTxt.getText().toString().trim();
            final String confirmPass = this.confirmPassword.getText().toString().trim();

            if (username.equals(EMPTY)) {
                this.userName.setError(ENTER_USERNAME);
                return;
            } else if (email.equals(EMPTY)) {
                this.emailEditText.setError(ENTER_VALID_EMAIL);
            } else if (!this.validate(email)) {
                this.emailEditText.setError(ENTER_VALID_EMAIL2);
            } else if (password.equals(EMPTY)) {
                this.passwordTxt.setError(ENTER_PASSWORD);
            } else if (confirmPass.equals(EMPTY)) {
                this.confirmPassword.setError(ENTER_CONFIRM_PASSWORD);
            } else if (!password.equals(confirmPass)) {
                this.confirmPassword.setError(CONFIRM_PASSWORD_IS_WRONG);
            } else {
                try {
                    new LoginAsyncTask(this, SIGNUP, this.getJSONObject(), false, EMPTY).execute();
                    Toast.makeText(this, ALL_FIELDS_VALID, Toast.LENGTH_LONG).show();
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }
            }
            break;
        case R.id.fblogin:
            MainManager.getInstance().setRememberChecked(false);
            MainManager.getInstance().setUserName(EMPTY);
            MainManager.getInstance().setPassword(EMPTY);
            super.onClick(v);
            break;
        case R.id.twitterlogin:
            MainManager.getInstance().setRememberChecked(false);
            MainManager.getInstance().setUserName(EMPTY);
            MainManager.getInstance().setPassword(EMPTY);
            super.onClick(v);
            break;
        case R.id.gpluslogin:
            MainManager.getInstance().setRememberChecked(false);
            MainManager.getInstance().setUserName(EMPTY);
            MainManager.getInstance().setPassword(EMPTY);
            super.onClick(v);
            break;
        case R.id.cancelRegister:
            if (SignInFragment.signInFragment == null) {
                final Intent intent = new Intent(SignUpFragment.this, SignInFragment.class);
                this.finish();
                this.startActivity(intent);
            } else {
                this.finish();
            }
            break;
        default:
            break;

        }

    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.fragment_signup);
        signUpFragment = this;
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        this.userName = (EditText) this.findViewById(R.id.userNameRegisterEditText);
        this.emailEditText = (EditText) this.findViewById(R.id.emailRegisterEditText);
        this.passwordTxt = (EditText) this.findViewById(R.id.passwordRegisterEditText);
        this.confirmPassword = (EditText) this.findViewById(R.id.confirmRegisterEditText);
        this.register = (Button) this.findViewById(R.id.signUpRegister);
        this.register.setOnClickListener(this);
        this.cancelRegister = (Button) this.findViewById(R.id.cancelRegister);
        this.cancelRegister.setOnClickListener(this);

        this.facebook = (Button) this.findViewById(R.id.fblogin);
        this.gplus = (Button) this.findViewById(R.id.gpluslogin);
        // twitter = (Button) findViewById(R.id.twitterlogin);

        this.facebook.setOnClickListener(this);
        this.gplus.setOnClickListener(this);
        // twitter.setOnClickListener(this);
        LOG.v("onCreate State");

    }

    @Override
    public void processFinish(final List<Friend> friendList, final String socialSite) {

        if ((this.socialLoginDeatils != null) && (this.socialLoginDeatils.getEmailId() != null)
                && (this.socialLoginDeatils.getEmailId().trim().length() > 0)) {
            if (Strings.isNullOrEmpty(this.socialLoginDeatils.getUserName())) {
                this.socialLoginDeatils.setUserName(this.socialLoginDeatils.getEmailId());
            }
            if (Constant.TWITTER.equalsIgnoreCase(socialSite)) {
                VideoPlayerApp.getInstance().setTwitterFriendList(friendList);
                try {
                    new LoginAsyncTask(this, SOCIALLOGIN, this.getSocialLoginRequest(this.socialLoginDeatils, TWITTER,
                            friendList), false, EMPTY).execute();
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }
            } else if (Constant.FACEBOOK.equalsIgnoreCase(socialSite)) {
                VideoPlayerApp.getInstance().setFacebookFriendsList(friendList);
                this.addsocialLoginContactToFriendList(this.socialLoginDeatils, friendList, socialSite);

                final String originalUrl = this.socialLoginDeatils.getUserPickUrl();
                String largeProfilePickUrl = originalUrl;
                largeProfilePickUrl = originalUrl + WIDTH_640_HEIGHT_640;
                this.socialLoginDeatils.setUserPickURL(largeProfilePickUrl);

                try {
                    new LoginAsyncTask(this, SOCIALLOGIN, this.getSocialLoginRequest(this.socialLoginDeatils, FACEBOOK,
                            friendList), false, EMPTY).execute();
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }
                LOG.i("fb oncomplete frnds.size() " + friendList.size());

            } else if (Constant.GOOGLE_PLUS.equalsIgnoreCase(socialSite)) {
                LOG.i("GooglePlus process finish received gplus ");
                VideoPlayerApp.getInstance().setGoogleFriendList(friendList);
                this.addsocialLoginContactToFriendList(this.socialLoginDeatils, friendList, socialSite);
                LOG.i("fb oncomplete frnds.size() " + friendList.size());
                final String originalUrl = this.socialLoginDeatils.getUserPickUrl();
                String largeProfilePickUrl = originalUrl;
                largeProfilePickUrl = originalUrl.replace(SZ_50, SZ_640);
                this.socialLoginDeatils.setUserPickURL(largeProfilePickUrl);
                try {
                    new LoginAsyncTask(this, SOCIALLOGIN, this.getSocialLoginRequest(this.socialLoginDeatils, GOOGLE,
                            friendList), false, EMPTY).execute();
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }
            }
        } else {
            Alerts.showInfoOnly(NOT_ABLE_TO_GET_THE_EMAIL_ID_FROM_SOCIAL_SITE, this.context);
        }
        this.gplus.setEnabled(true);
        this.facebook.setEnabled(true);

    }

    public void serverResponse(final String message) {

        Alerts.showInfoOnly(message, this);
    }

    @Override
    public void userDetailsFinished(final User userDetails, final String socialsite) {

        super.userDetailsFinished(userDetails, socialsite);
        // gplus.setEnabled(true);
        // facebook.setEnabled(true);
        this.socialLoginDeatils = userDetails;
        if (socialsite.equalsIgnoreCase(Constant.FACEBOOK)) {
            MainManager.getInstance().setFacebookEmail(this.socialLoginDeatils.getEmailId());
        } else if (socialsite.equalsIgnoreCase(Constant.TWITTER)) {
            MainManager.getInstance().setTwitterEmail(this.socialLoginDeatils.getEmailId());
        } else if (socialsite.equalsIgnoreCase(Constant.GOOGLE_PLUS)) {
            MainManager.getInstance().setGPlusEmail(this.socialLoginDeatils.getEmailId());
        }
    }

    public boolean validate(final String email) {

        this.matcher = pattern.matcher(email);
        return this.matcher.matches();

    }

    private void addsocialLoginContactToFriendList(final User socialLoginDeatils, final List<Friend> friendList,
            final String socialsite) {

        Friend fbContact = null;
        if (socialLoginDeatils != null) {
            fbContact = new Friend();
            fbContact.setFriendID(socialLoginDeatils.getUserId());
            fbContact.setFriendName("You");// socialLoginDeatils.getUserName()
            fbContact.setFriendImage(socialLoginDeatils.getUserPickUrl());
            fbContact.setLocation(socialLoginDeatils.getCountry());
        }
        if (socialsite.equalsIgnoreCase(Constant.TWITTER)) {
            if (friendList == null) {
                final List<Friend> list = new ArrayList<Friend>();
                list.add(fbContact);
                VideoPlayerApp.getInstance().setTwitterFriendList(list);
            } else {
                friendList.add(0, fbContact);
                VideoPlayerApp.getInstance().setTwitterFriendList(friendList);
            }
        } else if (socialsite.equalsIgnoreCase(Constant.FACEBOOK)) {
            if (friendList == null) {
                final List<Friend> list = new ArrayList<Friend>();
                list.add(fbContact);
                VideoPlayerApp.getInstance().setFacebookFriendsList(list);
            } else {
                friendList.add(0, fbContact);
                VideoPlayerApp.getInstance().setFacebookFriendsList(friendList);
            }
        } else if (socialsite.equalsIgnoreCase(Constant.GOOGLE_PLUS)) {
            if (friendList == null) {
                final List<Friend> list = new ArrayList<Friend>();
                list.add(fbContact);
                VideoPlayerApp.getInstance().setGoogleFriendList(list);
            } else {
                friendList.add(0, fbContact);
                VideoPlayerApp.getInstance().setGoogleFriendList(friendList);
            }
        }

    }

}
