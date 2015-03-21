/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.dto;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

public class SignUpDto {

    private static final Logger LOG = LoggerManager.getLogger();

    private String message;
    private String userId;
    private String userName;
    private String userPick;
    private int errorcode;

    public int getErrorcode() {

        return this.errorcode;
    }

    public String getMessage() {

        return this.message;
    }

    public String getUserId() {

        return this.userId;
    }

    public String getUserName() {

        return this.userName;
    }

    public String getUserPick() {

        return this.userPick;
    }

    public void setErrorcode(final int errorcode) {

        this.errorcode = errorcode;
    }

    public void setMessage(final String message) {

        this.message = message;
    }

    public void setUserId(final String userId) {

        this.userId = userId;
    }

    public void setUserName(final String userName) {

        this.userName = userName;
    }

    public void setUserPick(final String userPick) {

        this.userPick = userPick;
    }

}
