/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.TagFu.connectivity;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

public class HttpResponseFormatDto {

    private static final Logger LOG = LoggerManager.getLogger();

    private int statusCode;
    private String data;

    public int getStatusCode() {

        return this.statusCode;
    }

    public void setStatusCode(final int statusCode) {

        this.statusCode = statusCode;
    }

    public String getData() {

        return this.data;
    }

    public void setData(final String data) {

        this.data = data;
    }

}
