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
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.TagFu.facebook.FacebookException;
import com.TagFu.facebook.Request;
import com.TagFu.facebook.Settings;
import com.TagFu.facebook.model.GraphObject;

/**
 * com.facebook.internal is solely for the use of other packages within the Facebook SDK for Android. Use of any of the
 * classes in this package is unsupported, and they may be modified or removed without warning at any time.
 */
public final class Utility {

    static final String LOG_TAG = "FacebookSDK";
    private static final String HASH_ALGORITHM_MD5 = "MD5";
    private static final String HASH_ALGORITHM_SHA1 = "SHA-1";
    private static final String URL_SCHEME = "https";
    private static final String SUPPORTS_ATTRIBUTION = "supports_attribution";
    private static final String SUPPORTS_IMPLICIT_SDK_LOGGING = "supports_implicit_sdk_logging";
    private static final String NUX_CONTENT = "gdpv4_nux_content";
    private static final String NUX_ENABLED = "gdpv4_nux_enabled";

    private static final String[] APP_SETTING_FIELDS = new String[] { SUPPORTS_ATTRIBUTION,
            SUPPORTS_IMPLICIT_SDK_LOGGING, NUX_CONTENT, NUX_ENABLED };
    private static final String APPLICATION_FIELDS = "fields";

    // This is the default used by the buffer streams, but they trace a warning if you do not specify.
    public static final int DEFAULT_STREAM_BUFFER_SIZE = 8192;

    private static Map<String, FetchedAppSettings> fetchedAppSettings = new ConcurrentHashMap<String, FetchedAppSettings>();

    public static <T> boolean areObjectsEqual(final T a, final T b) {

        if (a == null) {
            return b == null;
        }
        return a.equals(b);
    }

    public static <T> List<T> arrayList(final T... ts) {

        final ArrayList<T> arrayList = new ArrayList<T>(ts.length);
        for (final T t : ts) {
            arrayList.add(t);
        }
        return arrayList;
    }

    public static <T> List<T> asListNoNulls(final T... array) {

        final ArrayList<T> result = new ArrayList<T>();
        for (final T t : array) {
            if (t != null) {
                result.add(t);
            }
        }
        return result;
    }

    public static Uri buildUri(final String authority, final String path, final Bundle parameters) {

        final Uri.Builder builder = new Uri.Builder();
        builder.scheme(URL_SCHEME);
        builder.authority(authority);
        builder.path(path);
        for (final String key : parameters.keySet()) {
            final Object parameter = parameters.get(key);
            if (parameter instanceof String) {
                builder.appendQueryParameter(key, (String) parameter);
            }
        }
        return builder.build();
    }

    public static void clearCaches(final Context context) {

        ImageDownloader.clearCache(context);
    }

    public static void clearFacebookCookies(final Context context) {

        // setCookie acts differently when trying to expire cookies between builds of Android that are using
        // Chromium HTTP stack and those that are not. Using both of these domains to ensure it works on both.
        clearCookiesForDomain(context, "facebook.com");
        clearCookiesForDomain(context, ".facebook.com");
        clearCookiesForDomain(context, "https://facebook.com");
        clearCookiesForDomain(context, "https://.facebook.com");
    }

    public static void closeQuietly(final Closeable closeable) {

        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (final IOException ioe) {
            // ignore
        }
    }

    public static void deleteDirectory(final File directoryOrFile) {

        if (!directoryOrFile.exists()) {
            return;
        }

        if (directoryOrFile.isDirectory()) {
            for (final File child : directoryOrFile.listFiles()) {
                deleteDirectory(child);
            }
        }
        directoryOrFile.delete();
    }

    public static void disconnectQuietly(final URLConnection connection) {

        if (connection instanceof HttpURLConnection) {
            ((HttpURLConnection) connection).disconnect();
        }
    }

    // Return a hash of the android_id combined with the appid. Intended to dedupe requests on the server side
    // in order to do counting of users unknown to Facebook. Because we put the appid into the key prior to hashing,
    // we cannot do correlation of the same user across multiple apps -- this is intentional. When we transition to
    // the Google advertising ID, we'll get rid of this and always send that up.
    public static String getHashedDeviceAndAppID(final Context context, final String applicationId) {

        final String androidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);

        if (androidId == null) {
            return null;
        }
        return sha1hash(androidId + applicationId);
    }

    public static String getMetadataApplicationId(final Context context) {

        Validate.notNull(context, "context");

        Settings.loadDefaultsFromMetadata(context);

        return Settings.getApplicationId();
    }

    public static Method getMethodQuietly(final Class<?> clazz, final String methodName,
            final Class<?>... parameterTypes) {

        try {
            return clazz.getMethod(methodName, parameterTypes);
        } catch (final NoSuchMethodException ex) {
            return null;
        }
    }

    public static Method getMethodQuietly(final String className, final String methodName,
            final Class<?>... parameterTypes) {

        try {
            final Class<?> clazz = Class.forName(className);
            return getMethodQuietly(clazz, methodName, parameterTypes);
        } catch (final ClassNotFoundException ex) {
            return null;
        }
    }

    // Returns either a JSONObject or JSONArray representation of the 'key' property of 'jsonObject'.
    public static Object getStringPropertyAsJSON(JSONObject jsonObject, final String key,
            final String nonJSONPropertyKey) throws JSONException {

        Object value = jsonObject.opt(key);
        if ((value != null) && (value instanceof String)) {
            final JSONTokener tokener = new JSONTokener((String) value);
            value = tokener.nextValue();
        }

        if ((value != null) && !((value instanceof JSONObject) || (value instanceof JSONArray))) {
            if (nonJSONPropertyKey != null) {
                // Facebook sometimes gives us back a non-JSON value such as
                // literal "true" or "false" as a result.
                // If we got something like that, we present it to the caller as
                // a GraphObject with a single
                // property. We only do this if the caller wants that behavior.
                jsonObject = new JSONObject();
                jsonObject.putOpt(nonJSONPropertyKey, value);
                return jsonObject;
            }
            throw new FacebookException("Got an unexpected non-JSON object.");
        }

        return value;

    }

    public static Object invokeMethodQuietly(final Object receiver, final Method method, final Object... args) {

        try {
            return method.invoke(receiver, args);
        } catch (final IllegalAccessException ex) {
            return null;
        } catch (final InvocationTargetException ex) {
            return null;
        }
    }

    public static <T> boolean isNullOrEmpty(final Collection<T> collection) {

        return (collection == null) || (collection.size() == 0);
    }

    public static boolean isNullOrEmpty(final String s) {

        return (s == null) || (s.length() == 0);
    }

    // Returns true iff all items in subset are in superset, treating null and
    // empty collections as
    // the same.
    public static <T> boolean isSubset(final Collection<T> subset, final Collection<T> superset) {

        if ((superset == null) || (superset.size() == 0)) {
            return ((subset == null) || (subset.size() == 0));
        }

        final HashSet<T> hash = new HashSet<T>(superset);
        for (final T t : subset) {
            if (!hash.contains(t)) {
                return false;
            }
        }
        return true;
    }

    public static void putObjectInBundle(final Bundle bundle, final String key, final Object value) {

        if (value instanceof String) {
            bundle.putString(key, (String) value);
        } else if (value instanceof Parcelable) {
            bundle.putParcelable(key, (Parcelable) value);
        } else if (value instanceof byte[]) {
            bundle.putByteArray(key, (byte[]) value);
        } else {
            throw new FacebookException("attempted to add unsupported type to Bundle");
        }
    }

    // Note that this method makes a synchronous Graph API call, so should not be called from the main thread.
    public static FetchedAppSettings queryAppSettings(final String applicationId, final boolean forceRequery) {

        // Cache the last app checked results.
        if (!forceRequery && fetchedAppSettings.containsKey(applicationId)) {
            return fetchedAppSettings.get(applicationId);
        }

        final Bundle appSettingsParams = new Bundle();
        appSettingsParams.putString(APPLICATION_FIELDS, TextUtils.join(",", APP_SETTING_FIELDS));

        final Request request = Request.newGraphPathRequest(null, applicationId, null);
        request.setParameters(appSettingsParams);

        final GraphObject supportResponse = request.executeAndWait().getGraphObject();
        final FetchedAppSettings result = new FetchedAppSettings(safeGetBooleanFromResponse(supportResponse,
                SUPPORTS_ATTRIBUTION), safeGetBooleanFromResponse(supportResponse, SUPPORTS_IMPLICIT_SDK_LOGGING),
                safeGetStringFromResponse(supportResponse, NUX_CONTENT), safeGetBooleanFromResponse(supportResponse,
                        NUX_ENABLED));

        fetchedAppSettings.put(applicationId, result);

        return result;
    }

    public static String readStreamToString(final InputStream inputStream) throws IOException {

        BufferedInputStream bufferedInputStream = null;
        InputStreamReader reader = null;
        try {
            bufferedInputStream = new BufferedInputStream(inputStream);
            reader = new InputStreamReader(bufferedInputStream);
            final StringBuilder stringBuilder = new StringBuilder();

            final int bufferSize = 1024 * 2;
            final char[] buffer = new char[bufferSize];
            int n = 0;
            while ((n = reader.read(buffer)) != -1) {
                stringBuilder.append(buffer, 0, n);
            }

            return stringBuilder.toString();
        } finally {
            closeQuietly(bufferedInputStream);
            closeQuietly(reader);
        }
    }

    public static void setAppEventAttributionParameters(final GraphObject params,
            final AttributionIdentifiers attributionIdentifiers, final String hashedDeviceAndAppId,
            final boolean limitEventUsage) {

        // Send attributionID if it exists, otherwise send a hashed device+appid specific value as the advertiser_id.
        if ((attributionIdentifiers != null) && (attributionIdentifiers.getAttributionId() != null)) {
            params.setProperty("attribution", attributionIdentifiers.getAttributionId());
        }

        if ((attributionIdentifiers != null) && (attributionIdentifiers.getAndroidAdvertiserId() != null)) {
            params.setProperty("advertiser_id", attributionIdentifiers.getAndroidAdvertiserId());
            params.setProperty("advertiser_tracking_enabled",
                    Boolean.valueOf(!attributionIdentifiers.isTrackingLimited()));
        } else if (hashedDeviceAndAppId != null) {
            params.setProperty("advertiser_id", hashedDeviceAndAppId);
        }

        params.setProperty("application_tracking_enabled", Boolean.valueOf(!limitEventUsage));
    }

    public static boolean stringsEqualOrEmpty(final String a, final String b) {

        final boolean aEmpty = TextUtils.isEmpty(a);
        final boolean bEmpty = TextUtils.isEmpty(b);

        if (aEmpty && bEmpty) {
            // Both null or empty, they match.
            return true;
        }
        if (!aEmpty && !bEmpty) {
            // Both non-empty, check equality.
            return a.equals(b);
        }
        // One empty, one non-empty, can't match.
        return false;
    }

    public static <T> Collection<T> unmodifiableCollection(final T... ts) {

        return Collections.unmodifiableCollection(Arrays.asList(ts));
    }

    private static void clearCookiesForDomain(final Context context, final String domain) {

        // This is to work around a bug where CookieManager may fail to instantiate if CookieSyncManager
        // has never been created.
        final CookieSyncManager syncManager = CookieSyncManager.createInstance(context);
        syncManager.sync();

        final CookieManager cookieManager = CookieManager.getInstance();

        final String cookies = cookieManager.getCookie(domain);
        if (cookies == null) {
            return;
        }

        final String[] splitCookies = cookies.split(";");
        for (final String cookie : splitCookies) {
            final String[] cookieParts = cookie.split("=");
            if (cookieParts.length > 0) {
                final String newCookie = cookieParts[0].trim() + "=;expires=Sat, 1 Jan 2000 00:00:01 UTC;";
                cookieManager.setCookie(domain, newCookie);
            }
        }
        cookieManager.removeExpiredCookie();
    }

    private static String hashBytes(final MessageDigest hash, final byte[] bytes) {

        hash.update(bytes);
        final byte[] digest = hash.digest();
        final StringBuilder builder = new StringBuilder();
        for (final int b : digest) {
            builder.append(Integer.toHexString((b >> 4) & 0xf));
            builder.append(Integer.toHexString((b >> 0) & 0xf));
        }
        return builder.toString();
    }

    private static String hashWithAlgorithm(final String algorithm, final byte[] bytes) {

        MessageDigest hash;
        try {
            hash = MessageDigest.getInstance(algorithm);
        } catch (final NoSuchAlgorithmException e) {
            return null;
        }
        return hashBytes(hash, bytes);
    }

    private static String hashWithAlgorithm(final String algorithm, final String key) {

        return hashWithAlgorithm(algorithm, key.getBytes());
    }

    private static boolean safeGetBooleanFromResponse(final GraphObject response, final String propertyName) {

        Object result = false;
        if (response != null) {
            result = response.getProperty(propertyName);
        }
        if (!(result instanceof Boolean)) {
            result = false;
        }
        return (Boolean) result;
    }

    private static String safeGetStringFromResponse(final GraphObject response, final String propertyName) {

        Object result = "";
        if (response != null) {
            result = response.getProperty(propertyName);
        }
        if (!(result instanceof String)) {
            result = "";
        }
        return (String) result;
    }

    static Map<String, Object> convertJSONObjectToHashMap(final JSONObject jsonObject) throws JSONException {

        final HashMap<String, Object> map = new HashMap<String, Object>();
        final JSONArray keys = jsonObject.names();
        for (int i = 0; i < keys.length(); ++i) {
            String key;
            key = keys.getString(i);
            Object value = jsonObject.get(key);
            if (value instanceof JSONObject) {
                value = convertJSONObjectToHashMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    static String md5hash(final String key) {

        return hashWithAlgorithm(HASH_ALGORITHM_MD5, key);
    }

    static String sha1hash(final byte[] bytes) {

        return hashWithAlgorithm(HASH_ALGORITHM_SHA1, bytes);
    }

    static String sha1hash(final String key) {

        return hashWithAlgorithm(HASH_ALGORITHM_SHA1, key);
    }

    public static class FetchedAppSettings {

        private final boolean supportsAttribution;
        private final boolean supportsImplicitLogging;
        private final String nuxContent;
        private final boolean nuxEnabled;

        private FetchedAppSettings(final boolean supportsAttribution, final boolean supportsImplicitLogging,
                final String nuxContent, final boolean nuxEnabled) {

            this.supportsAttribution = supportsAttribution;
            this.supportsImplicitLogging = supportsImplicitLogging;
            this.nuxContent = nuxContent;
            this.nuxEnabled = nuxEnabled;
        }

        public String getNuxContent() {

            return this.nuxContent;
        }

        public boolean getNuxEnabled() {

            return this.nuxEnabled;
        }

        public boolean supportsAttribution() {

            return this.supportsAttribution;
        }

        public boolean supportsImplicitLogging() {

            return this.supportsImplicitLogging;
        }
    }
}
