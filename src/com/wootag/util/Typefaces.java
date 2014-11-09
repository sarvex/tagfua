/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.util;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Typeface;

public final class Typefaces {

    // private static final Logger LOG = LoggerManager.getLogger();

    private static final Map<String, Typeface> CACHE = new HashMap<String, Typeface>();

    private Typefaces() {

    }

    public static Typeface get(final Context context, final String name) {

        synchronized (CACHE) {
            if (!CACHE.containsKey(name)) {
                final Typeface t = Typeface.createFromAsset(context.getAssets(), name);
                CACHE.put(name, t);
            }
            return CACHE.get(name);
        }
    }

}
