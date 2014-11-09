/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.dto;

import java.io.Serializable;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

public class ErrorResponse implements Serializable {

    private static final Logger LOG = LoggerManager.getLogger();

    private static final long serialVersionUID = 6349678801317216224L;

    private String code;
    private String message;

    public String getErrorCode() {

        return this.code;
    }

    public String getMessage() {

        return this.message;
    }

    public void setErrorCode(final String code) {

        this.code = code;
    }

    public void setMessage(final String message) {

        this.message = message;
    }
}
