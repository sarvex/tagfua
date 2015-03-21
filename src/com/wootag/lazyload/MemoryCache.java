/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.lazyload;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

public class MemoryCache {

    private static final Logger LOG = LoggerManager.getLogger();

    private final Map<String, SoftReference<Bitmap>> cache = new HashMap<String, SoftReference<Bitmap>>();

    public Bitmap get(final String id) {

        if (!this.cache.containsKey(id)) {
            return null;
        }
        final SoftReference<Bitmap> ref = this.cache.get(id);
        return ref.get();
    }

    public void put(final String id, final Bitmap bitmap) {

        this.cache.put(id, new SoftReference<Bitmap>(bitmap));
    }

    public void clear() {

        this.cache.clear();
    }
}
