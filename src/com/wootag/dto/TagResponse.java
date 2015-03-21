/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.dto;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

public class TagResponse {

    private static final Logger LOG = LoggerManager.getLogger();

    private long clientTagId;
    private long serverTagId;

    public long getClientTagId() {

        return this.clientTagId;
    }

    public void setClientTagId(final long clientTagId) {

        this.clientTagId = clientTagId;
    }

    public long getServerTagId() {

        return this.serverTagId;
    }

    public void setServerTagId(final long serverTagId) {

        this.serverTagId = serverTagId;
    }

}
