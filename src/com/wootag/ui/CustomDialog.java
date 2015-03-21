/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.ui;

import android.app.Dialog;
import android.content.Context;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

public class CustomDialog extends Dialog {

    private static final Logger LOG = LoggerManager.getLogger();

    public CustomDialog(final Context context, final int theme) {

        super(context, theme);

    }

}
