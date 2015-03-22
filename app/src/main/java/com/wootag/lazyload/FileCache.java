/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.TagFu.lazyload;

import java.io.File;

import android.content.Context;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

public class FileCache {

    private static final Logger LOG = LoggerManager.getLogger();

    private File cacheDir;

    public FileCache(final Context context) {

        // Find the dir to save cached images
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            this.cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), "LazyList");
        } else {
            this.cacheDir = context.getCacheDir();
        }
        if (!this.cacheDir.exists()) {
            this.cacheDir.mkdirs();
        }
    }

    public File getFile(final String url) {

        // I identify images by hashcode. Not a perfect solution, good for the demo.
        final String filename = String.valueOf(url.hashCode());
        return new File(this.cacheDir, filename);

    }

    public void clear() {

        final File[] files = this.cacheDir.listFiles();
        for (final File f : files) {
            f.delete();
        }
    }

}
