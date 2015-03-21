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

public class SuggestedUsersDto implements Serializable {

    private static final Logger LOG = LoggerManager.getLogger();

    private static final long serialVersionUID = -8165759497053363279L;

    private String country;
    private String follwoing;
    private String id;
    private String name;
    private String photoPath;
    private String profession;
    private String website;

    public String getCountry() {

        return this.country;
    }

    public String getFollwoing() {

        return this.follwoing;
    }

    public String getId() {

        return this.id;
    }

    public String getName() {

        return this.name;
    }

    public String getPhotoPath() {

        return this.photoPath;
    }

    public String getProfession() {

        return this.profession;
    }

    public String getWebsite() {

        return this.website;
    }

    public void load(final JSONObject response) throws JSONException {

        this.id = Stream.getString(response, Constant.ID);
        if (this.id == null) {
            this.id = Stream.getString(response, Constant.USER_ID);
        }
        this.name = Stream.getString(response, Constant.NAME);
        this.photoPath = Stream.getString(response, Constant.PHOTO_PATH);
        this.country = Stream.getString(response, Constant.COUNTRY);
        this.profession = Stream.getString(response, Constant.PROFESSION);
        this.website = Stream.getString(response, Constant.WEBSITE);
        this.follwoing = Stream.getString(response, Constant.FOLLOWING);

    }

    public void setCountry(final String country) {

        this.country = country;
    }

    public void setFollwoing(final String follwoing) {

        this.follwoing = follwoing;
    }

    public void setId(final String id) {

        this.id = id;
    }

    public void setName(final String name) {

        this.name = name;
    }

    public void setPhotoPath(final String photoPath) {

        this.photoPath = photoPath;
    }

    public void setProfession(final String profession) {

        this.profession = profession;
    }

    public void setWebsite(final String website) {

        this.website = website;
    }

}
