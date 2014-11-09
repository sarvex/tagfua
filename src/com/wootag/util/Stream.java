/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

public final class Stream {

    private static final int BUFFER_SIZE = 1024;

    private static final Logger LOG = LoggerManager.getLogger();

    private Stream() {

    }

    public static void copyStream(final InputStream is, final OutputStream os) {

        final byte[] bytes = new byte[BUFFER_SIZE];
        try {
            for (;;) {
                final int count = is.read(bytes, 0, BUFFER_SIZE);
                if (count == -1) {
                    break;
                }
                os.write(bytes, 0, count);
            }
        } catch (final IOException exception) {
            LOG.e(exception);
        }
    }

    public static String getString(final JSONObject obj, final String key) throws JSONException {

        if (obj.isNull(key)) {
            return null;
        }
        if (TextUtils.isEmpty(obj.getString(key))) {
            return null;
        }
        return obj.getString(key);
    }
}
