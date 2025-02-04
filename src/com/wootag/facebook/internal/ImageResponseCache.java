/**
 * Copyright 2010-present Facebook. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package com.TagFu.facebook.internal;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import android.content.Context;
import android.util.Log;

import com.wootag.facebook.LoggingBehavior;

class ImageResponseCache {

    static final String TAG = ImageResponseCache.class.getSimpleName();

    private volatile static FileLruCache imageCache;

    private static boolean isCDNURL(final URI url) {

        if (url != null) {
            final String uriHost = url.getHost();

            if (uriHost.endsWith("fbcdn.net")) {
                return true;
            }

            if (uriHost.startsWith("fbcdn") && uriHost.endsWith("akamaihd.net")) {
                return true;
            }
        }

        return false;
    }

    static void clearCache(final Context context) {

        getCache(context).clearCache();
    }

    synchronized static FileLruCache getCache(final Context context) {

        if (imageCache == null) {
            imageCache = new FileLruCache(context.getApplicationContext(), TAG, new FileLruCache.Limits());
        }
        return imageCache;
    }

    // Get stream from cache, or return null if the image is not cached.
    // Does not throw if there was an error.
    static InputStream getCachedImageStream(final URI url, final Context context) {

        InputStream imageStream = null;
        if (url != null) {
            if (isCDNURL(url)) {
                try {
                    final FileLruCache cache = getCache(context);
                    imageStream = cache.get(url.toString());
                } catch (final IOException e) {
                    Logger.log(LoggingBehavior.CACHE, Log.WARN, TAG, e.toString());
                }
            }
        }

        return imageStream;
    }

    static InputStream interceptAndCacheImageStream(final Context context, final HttpURLConnection connection)
            throws IOException {

        InputStream stream = null;
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            final URL url = connection.getURL();
            stream = connection.getInputStream(); // Default stream in case caching fails
            try {
                if (isCDNURL(url.toURI())) {
                    final FileLruCache cache = getCache(context);

                    // Wrap stream with a caching stream
                    stream = cache.interceptAndPut(url.toString(), new BufferedHttpInputStream(stream, connection));
                }
            } catch (final IOException e) {
                // Caching is best effort
            } catch (final URISyntaxException e) {
                // Caching is best effort
            }
        }
        return stream;
    }

    private static class BufferedHttpInputStream extends BufferedInputStream {

        HttpURLConnection connection;

        BufferedHttpInputStream(final InputStream stream, final HttpURLConnection connection) {

            super(stream, Utility.DEFAULT_STREAM_BUFFER_SIZE);
            this.connection = connection;
        }

        @Override
        public void close() throws IOException {

            super.close();
            Utility.disconnectQuietly(this.connection);
        }
    }
}
