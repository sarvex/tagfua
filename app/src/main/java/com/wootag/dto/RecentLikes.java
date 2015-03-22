/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.TagFu.dto;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.TagFu.Constant;
import com.TagFu.util.Stream;

public class RecentLikes implements Serializable {

    private static final Logger LOG = LoggerManager.getLogger();

    private static final long serialVersionUID = -6874843950726331400L;
    private String userName;
    private String userId;

    public String getUserId() {

        return this.userId;
    }

    public String getUserName() {

        return this.userName;
    }

    public void load(final JSONObject response) throws JSONException {

        this.userName = Stream.getString(response, Constant.USER_NAME);
        this.userId = Stream.getString(response, Constant.USER_ID);
    }

    public void setUserId(final String userId) {

        this.userId = userId;
    }

    public void setUserName(final String userName) {

        this.userName = userName;
    }
}
