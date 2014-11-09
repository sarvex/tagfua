/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.dto;

import android.graphics.Bitmap;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

public class Contact {

    private static final Logger LOG = LoggerManager.getLogger();

    private Bitmap contactPicture;
    private String contactName;
    private String contactNumber;
    private String imagePath;
    private boolean checked;

    public String getContactName() {

        return this.contactName;
    }

    public String getContactNumber() {

        return this.contactNumber;
    }

    public Bitmap getContactPicture() {

        return this.contactPicture;
    }

    public String getImagePath() {

        return this.imagePath;
    }

    public boolean isChecked() {

        return this.checked;
    }

    public void setChecked(final boolean checked) {

        this.checked = checked;
    }

    public void setContactName(final String contactName) {

        this.contactName = contactName;
    }

    public void setContactNumber(final String contactNumber) {

        this.contactNumber = contactNumber;
    }

    public void setContactPicture(final Bitmap contactPicture) {

        this.contactPicture = contactPicture;
    }

    public void setImagePath(final String imagePath) {

        this.imagePath = imagePath;
    }

}
