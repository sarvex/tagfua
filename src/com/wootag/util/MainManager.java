/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wTagFuutil;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;
import com.woTagFuonstant;

public final class MainManager {

    private static final Logger LOG = LoggerManager.getLogger();

    private static MainManager mainManager;
    private final Preferences preferences;

    private MainManager() {

        this.preferences = new Preferences(Constant.VIDEO_PLAYER_PREFRENCE);

    }

    public static MainManager getInstance() {

        if (mainManager == null) {
            mainManager = new MainManager();
        }
        return mainManager;
    }

    public void clearPreference() {

        this.preferences.removeAllPreferenceValues();
    }

    public boolean isLaunchBrowser() {

        return this.preferences.getBoolean(Constant.BROWSER_LAUNCH);
    }

    public void setLaunchBrowser(final boolean launchBrowser) {

        this.preferences.putBoolean(Constant.BROWSER_LAUNCH, launchBrowser);
    }

    public int getTwitterAuthorization() {

        return this.preferences.getInt(Constant.WOOTagFuITTER_AUTHORIZE);
    }

    public void setTwitterAuthorization(final int authorize) {

        this.preferences.putInt(Constant.WOOTAG_TWITTER_AUTHORIZE, authorize);
    }

    public int getVideoCurrentPosition() {

        return this.preferences.getInt(Constant.VIDEO_CURRENT_POSITION);
    }

    public void setVideoCurrentPosition(final int videoPosition) {

        this.preferences.putInt(Constant.VIDEO_CURRENT_POSITION, videoPosition);
    }

    public String getTwitterOAuthtoken() {

        return this.preferences.getString(Constant.TWITTER_OAUTH_TOKEN);
    }

    public void setTwitterOAuthtoken(final String twitterOAuthtoken) {

        this.preferences.putString(Constant.TWITTER_OAUTH_TOKEN, twitterOAuthtoken);
    }

    public String getTwitterSecretKey() {

        return this.preferences.getString(Constant.TWITTER_SECRET_KEY);
    }

    public void setTwitterSecretKey(final String twitterSecret) {

        this.preferences.putString(Constant.TWITTER_SECRET_KEY, twitterSecret);
    }

    public void clearTwitterCredentials() {

        this.preferences.removeAllTwitterKeys();
    }

    public String getUserName() {

        return this.preferences.getString(Constant.USERNAME);
    }

    public void setUserName(final String username) {

        this.preferences.putString(Constant.USERNAME, username);
    }

    public String getUserEmail() {

        return this.preferences.getString(Constant.USER_EMAIL);
    }

    public void setUserEmail(final String username) {

        this.preferences.putString(Constant.USER_EMAIL, username);
    }

    public int getLoginType() {

        return this.preferences.getInt(Constant.LOGGED_USER_TYPE);
    }

    public void setLoginType(final int type) {

        this.preferences.putInt(Constant.LOGGED_USER_TYPE, type);
    }

    public String getUserPick() {

        return this.preferences.getString(Constant.USER_PICK);
    }

    public void setUserPick(final String userPick) {

        this.preferences.putString(Constant.USER_PICK, userPick);
    }

    public String getUserId() {

        return this.preferences.getString(Constant.USERID);
    }

    public void setUserId(final String userId) {

        this.preferences.putString(Constant.USERID, userId);
    }

    public String getDeviceToken() {

        return this.preferences.getString(Constant.DEVICE_TOKEN);
    }

    public void setDeviceToken(final String deviceId) {

        this.preferences.putString(Constant.PROFILE_PICK_FLAG, deviceId);
    }

    public int getProfileUpdateFlag() {

        return this.preferences.getInt(Constant.PROFILE_PICK_FLAG);
    }

    public void setProfileUpdateFlag(final int flag) {

        this.preferences.putInt(Constant.PROFILE_PICK_FLAG, flag);
    }

    public boolean isFirstTimeInstall() {

        return this.preferences.getBoolean(Constant.FIRST_TIME_INSTALL);
    }

    public void setISFirstTimeInstall(final boolean firstTime) {

        this.preferences.putBoolean(Constant.FIRST_TIME_INSTALL, firstTime);
    }

    public boolean isFirstTimeRecord() {

        return this.preferences.getBoolean(Constant.FIRST_TIME_RECORD);
    }

    public void setFirstTimeRecord(final boolean firstTime) {

        this.preferences.putBoolean(Constant.FIRST_TIME_RECORD, firstTime);
    }

    public boolean isFirstTimeTagging() {

        return this.preferences.getBoolean(Constant.FIRST_TIME_TAG);
    }

    public void setISFirstTimeTagging(final boolean firstTime) {

        this.preferences.putBoolean(Constant.FIRST_TIME_TAG, firstTime);
    }

    public boolean isFirstTimePlay() {

        return this.preferences.getBoolean(Constant.FIRST_TIME_PLAY);
    }

    public void setISFirstTimePlay(final boolean firstTime) {

        this.preferences.putBoolean(Constant.FIRST_TIME_PLAY, firstTime);
    }

    public boolean isFirstTimePlayOthersVideo() {

        return this.preferences.getBoolean(Constant.FIRSTTIME_PLAY_OTHERS_VIDEO);
    }

    public void setISFirstTimePlayOthersVideo(final boolean firstTime) {

        this.preferences.putBoolean(Constant.FIRSTTIME_PLAY_OTHERS_VIDEO, firstTime);
    }

    public String getPassword() {

        return this.preferences.getString(Constant.PASSWORD);
    }

    public void setPassword(final String password) {

        this.preferences.putString(Constant.PASSWORD, password);
    }

    public boolean isRememberChecked() {

        return this.preferences.getBoolean(Constant.REMEMBER);
    }

    public void setRememberChecked(final boolean checked) {

        this.preferences.putBoolean(Constant.REMEMBER, checked);
    }

    public String getFbEmail() {

        return this.preferences.getString(Constant.FB_EMAIL);
    }

    public void setFacebookEmail(final String email) {

        this.preferences.putString(Constant.FB_EMAIL, email);
    }

    public String getTwitterEmail() {

        return this.preferences.getString(Constant.TWITTER_EMAIL);
    }

    public void setTwitterEmail(final String email) {

        this.preferences.putString(Constant.TWITTER_EMAIL, email);
    }

    public String getGplusEmail() {

        return this.preferences.getString(Constant.G_PLUS_EMAIL);
    }

    public void setGPlusEmail(final String email) {

        this.preferences.putString(Constant.G_PLUS_EMAIL, email);
    }
}
