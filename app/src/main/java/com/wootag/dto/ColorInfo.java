/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.TagFu.dto;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

public class ColorInfo {

    private static final Logger LOG = LoggerManager.getLogger();

    private final String countryName;
    private final int countryFlag;

    public ColorInfo(final String countryName, final int countryFlag) {

        this.countryName = countryName;
        this.countryFlag = countryFlag;
    }

    public int getCountryFlag() {

        return this.countryFlag;
    }

    public String getCountryName() {

        return this.countryName;
    }

    @Override
    public String toString() {

        return this.countryName;
    }
}
