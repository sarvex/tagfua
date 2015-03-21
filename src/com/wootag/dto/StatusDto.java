/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.dto;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

public class StatusDto {

    private static final Logger LOG = LoggerManager.getLogger();

    private boolean success;
    private String message;

    public boolean isSuccess() {

        return this.success;
    }

    public void setSuccess(final boolean success) {

        this.success = success;
    }

    public String getMessage() {

        return this.message;
    }

    public void setMessage(final String message) {

        this.message = message;
    }

}
