/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.connectivity;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

public class ApnDto {

    private static final Logger LOG = LoggerManager.getLogger();

    private String apnName;
    private String name;
    private String proxy;
    private int id;
    private int port;

    public String getApnName() {

        return this.apnName;
    }

    public int getId() {

        return this.id;
    }

    public String getName() {

        return this.name;
    }

    public int getPort() {

        return this.port;
    }

    public String getProxy() {

        return this.proxy;
    }

    public void setApnName(final String apnName) {

        this.apnName = apnName;
    }

    public void setId(final int id) {

        this.id = id;
    }

    public void setName(final String name) {

        this.name = name;
    }

    public void setPort(final int port) {

        this.port = port;
    }

    public void setProxy(final String proxy) {

        this.proxy = proxy;
    }

}
