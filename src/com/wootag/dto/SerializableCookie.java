/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.dto;

import java.io.Serializable;
import java.util.Date;

import org.apache.http.cookie.Cookie;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

public class SerializableCookie implements Serializable {

    private static final Logger LOG = LoggerManager.getLogger();

    private static final long serialVersionUID = -4807848129076572162L;

    private Date expiryDate;
    private String domain;
    private String name;
    private String path;
    private String value;
    private int version;
    private transient Cookie cookie;

    public Cookie getCookie() {

        return this.cookie;
    }

    public String getDomain() {

        return this.domain;
    }

    public Date getExpiryDate() {

        return this.expiryDate;
    }

    public String getName() {

        return this.name;
    }

    public String getPath() {

        return this.path;
    }

    public String getValue() {

        return this.value;
    }

    public int getVersion() {

        return this.version;
    }

    public void parse(final Cookie cookie) {

        this.name = cookie.getName();
        this.path = cookie.getPath();
        this.domain = cookie.getDomain();
        this.expiryDate = cookie.getExpiryDate();
        this.value = cookie.getValue();
        this.version = cookie.getVersion();
        this.cookie = cookie;

    }

    public void setCookie(final Cookie cookie) {

        this.cookie = cookie;
    }

    public void setDomain(final String domain) {

        this.domain = domain;
    }

    public void setExpiryDate(final Date expiryDate) {

        this.expiryDate = expiryDate;
    }

    public void setName(final String name) {

        this.name = name;
    }

    public void setPath(final String path) {

        this.path = path;
    }

    public void setValue(final String value) {

        this.value = value;
    }

    public void setVersion(final int version) {

        this.version = version;
    }

}
