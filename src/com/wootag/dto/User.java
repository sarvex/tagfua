/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.dto;

import org.json.JSONException;
import org.json.JSONObject;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wootag.Constant;
import com.wootag.util.Stream;

public class User {

    private static final Logger LOG = LoggerManager.getLogger();

    private String userName;
    private String userId;
    private String userPickURL;
    private String emailId;
    private String country;
    private String profession;
    private String website;
    private String photoPath;
    private String bannerPath;
    private String gender;
    private String phone;
    private String bio;

    public String getBannerPath() {

        return this.bannerPath;
    }

    public String getBio() {

        return this.bio;
    }

    public String getCountry() {

        return this.country;
    }

    public String getEmailId() {

        return this.emailId;
    }

    public String getGender() {

        return this.gender;
    }

    public String getPhone() {

        return this.phone;
    }

    public String getPhotoPath() {

        return this.photoPath;
    }

    public String getProfession() {

        return this.profession;
    }

    public String getUserId() {

        return this.userId;
    }

    public String getUserName() {

        return this.userName;
    }

    public String getUserPickUrl() {

        return this.userPickURL;
    }

    public String getWebsite() {

        return this.website;
    }

    public void load(final JSONObject response) throws JSONException {

        this.userId = Stream.getString(response, Constant.USER_ID);
        this.userName = Stream.getString(response, Constant.NAME);
        this.emailId = Stream.getString(response, Constant.EMAIL);
        this.country = Stream.getString(response, Constant.COUNTRY);
        this.profession = Stream.getString(response, Constant.PROFESSION);
        this.website = Stream.getString(response, Constant.WEBSITE);
        this.photoPath = Stream.getString(response, Constant.PHOTO_PATH);
        this.bannerPath = Stream.getString(response, Constant.BANNER_PATH);
        this.gender = Stream.getString(response, Constant.GENDER);
        this.phone = Stream.getString(response, Constant.PHONE);
        this.bio = Stream.getString(response, Constant.BIO);

    }

    public void setBannerPath(final String bannerPath) {

        this.bannerPath = bannerPath;
    }

    public void setBio(final String bio) {

        this.bio = bio;
    }

    public void setCountry(final String country) {

        this.country = country;
    }

    public void setEmailId(final String emailId) {

        this.emailId = emailId;
    }

    public void setGender(final String gender) {

        this.gender = gender;
    }

    public void setPhone(final String phone) {

        this.phone = phone;
    }

    public void setPhotoPath(final String photoPath) {

        this.photoPath = photoPath;
    }

    public void setProfession(final String profession) {

        this.profession = profession;
    }

    public void setUserId(final String userId) {

        this.userId = userId;
    }

    public void setUserName(final String userName) {

        this.userName = userName;
    }

    public void setUserPickURL(final String userPickURL) {

        this.userPickURL = userPickURL;
    }

    public void setWebsite(final String website) {

        this.website = website;
    }

}
