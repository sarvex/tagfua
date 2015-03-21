/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wTagFudto;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.woTagFuonstant;
import com.wootag.util.Stream;

public class People implements Serializable {

    private static final Logger LOG = LoggerManager.getLogger();
    private static final long serialVersionUID = -2948476980475113414L;

    private String country;
    private String emailId;
    private String follow;
    private String id;
    private String lastLoginDate;
    private String peopleId;
    private String position;
    private String url;
    private String userName;

    public String getCountry() {

        return this.country;
    }

    public String getEmailId() {

        return this.emailId;
    }

    public String getFollow() {

        return this.follow;
    }

    public String getId() {

        return this.id;
    }

    public String getIsFollow() {

        return this.follow;
    }

    public String getLastLogindate() {

        return this.lastLoginDate;
    }

    public String getLastLoginDate() {

        return this.lastLoginDate;
    }

    public String getPeopleId() {

        return this.peopleId;
    }

    public String getPosition() {

        return this.position;
    }

    public String getUrl() {

        return this.url;
    }

    public String getUserName() {

        return this.userName;
    }

    public void load(final JSONObject response) throws JSONException {

        this.userName = Stream.getString(response, Constant.NAME);
        if (this.userName == null) {
            this.userName = Stream.getString(response, Constant.USER_NAME);
        }

        this.url = Stream.getString(response, Constant.PHOTO_PATH);
        if (this.url == null) {
            this.url = Stream.getString(response, Constant.USER_PHOTO);
        }

        this.id = Stream.getString(response, Constant.USER_ID);
        if (this.id == null) {
            this.id = Stream.getString(response, Constant.ID);
        }

        this.peopleId = Stream.getString(response, Constant.ID);
        this.emailId = Stream.getString(response, Constant.EMAIL);
        this.country = Stream.getString(response, Constant.COUNTRY);
        this.position = Stream.getString(response, Constant.POSITION);
        this.lastLoginDate = Stream.getString(response, Constant.LAST_LOGIN_DATE);
        this.follow = Stream.getString(response, Constant.FOLLOWING);

    }

    public void setCountry(final String country) {

        this.country = country;
    }

    public void setEmailId(final String emailId) {

        this.emailId = emailId;
    }

    public void setFollow(final String follow) {

        this.follow = follow;
    }

    public void setId(final String id) {

        this.id = id;
    }

    public void setIsFollow(final String isFollow) {

        this.follow = isFollow;
    }

    public void setLastLogindate(final String lastLogindate) {

        this.lastLoginDate = lastLogindate;
    }

    public void setLastLoginDate(final String lastLoginDate) {

        this.lastLoginDate = lastLoginDate;
    }

    public void setPeopleId(final String peopleId) {

        this.peopleId = peopleId;
    }

    public void setPosition(final String position) {

        this.position = position;
    }

    public void setUrl(final String url) {

        this.url = url;
    }

    public void setUserName(final String userName) {

        this.userName = userName;
    }

}
