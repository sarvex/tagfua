/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.util;

import java.util.List;

import com.wootag.dto.Friend;
import com.wootag.dto.FacebookUser;

public interface AsyncResponse {

    void processFinish(List<Friend> output, String socialMediasite);

    void friendInfoProcessFinish(FacebookUser info, boolean friend, String socialsite);
}
