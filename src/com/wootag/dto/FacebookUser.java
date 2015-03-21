/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.dto;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

public class FacebookUser {

    private static final Logger LOG = LoggerManager.getLogger();

    private String birthDay;
    private String currentPlace;
    private String email;
    private String fromPlace;
    private String id;
    private String lastUpdate;
    private String profilePick;
    private String screenName;
    private String statusUpdate;
    private String twitterUserDescription;
    private String twitterUserFollowerCount;
    private String url;
    private String userName;
    private String[] education;
    private String[] employer;
    private boolean onlinePresence;

    public String getBirthDay() {

        return this.birthDay;
    }

    public String getCurrentPlace() {

        return this.currentPlace;
    }

    public String[] getEducation() {

        return this.education;
    }

    public String getEmail() {

        return this.email;
    }

    public String[] getEmployer() {

        return this.employer;
    }

    public String getFromPlace() {

        return this.fromPlace;
    }

    public String getId() {

        return this.id;
    }

    public String getLastUpdate() {

        return this.lastUpdate;
    }

    public String getProfilePick() {

        return this.profilePick;
    }

    public String getScreenName() {

        return this.screenName;
    }

    public String getStatusUpdate() {

        return this.statusUpdate;
    }

    public String getTwitterUserDescription() {

        return this.twitterUserDescription;
    }

    public String getTwitterUserFollowerCount() {

        return this.twitterUserFollowerCount;
    }

    public String getUrl() {

        return this.url;
    }

    public String getUserName() {

        return this.userName;
    }

    public boolean isOnlinePresence() {

        return this.onlinePresence;
    }

    public void setBirthDay(final String birthDay) {

        this.birthDay = birthDay;
    }

    public void setCurrentPlace(final String currentPlace) {

        this.currentPlace = currentPlace;
    }

    public void setEducation(final String[] education) {

        this.education = education;
    }

    public void setEmail(final String email) {

        this.email = email;
    }

    public void setEmployer(final String[] employer) {

        this.employer = employer;
    }

    public void setFromPlace(final String fromPlace) {

        this.fromPlace = fromPlace;
    }

    public void setId(final String id) {

        this.id = id;
    }

    public void setLastUpdate(final String lastUpdate) {

        this.lastUpdate = lastUpdate;
    }

    public void setOnlinePresence(final boolean onlinePresence) {

        this.onlinePresence = onlinePresence;
    }

    public void setProfilePick(final String profilePick) {

        this.profilePick = profilePick;
    }

    public void setScreenName(final String screenName) {

        this.screenName = screenName;
    }

    public void setStatusUpdate(final String statusUpdate) {

        this.statusUpdate = statusUpdate;
    }

    public void setTwitterUserDescription(final String twitterUserDescription) {

        this.twitterUserDescription = twitterUserDescription;
    }

    public void setTwitterUserFollowerCount(final String twitterUserFollowerCount) {

        this.twitterUserFollowerCount = twitterUserFollowerCount;
    }

    public void setUrl(final String url) {

        this.url = url;
    }

    public void setUserName(final String userName) {

        this.userName = userName;
    }

}
