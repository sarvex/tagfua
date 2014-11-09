/**
 * Copyright 2010-present Facebook. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package com.wootag.facebook.internal;

import java.lang.reflect.Method;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

/**
 * com.facebook.internal is solely for the use of other packages within the Facebook SDK for Android. Use of any of the
 * classes in this package is unsupported, and they may be modified or removed without warning at any time.
 */
public class AttributionIdentifiers {

    private static final String TAG = AttributionIdentifiers.class.getCanonicalName();
    private static final Uri ATTRIBUTION_ID_CONTENT_URI = Uri
            .parse("content://com.facebook.katana.provider.AttributionIdProvider");
    private static final String ATTRIBUTION_ID_COLUMN_NAME = "aid";
    private static final String ANDROID_ID_COLUMN_NAME = "androidid";
    private static final String LIMIT_TRACKING_COLUMN_NAME = "limit_tracking";

    // com.google.android.gms.common.ConnectionResult.SUCCESS
    private static final int CONNECTION_RESULT_SUCCESS = 0;

    private static final long IDENTIFIER_REFRESH_INTERVAL_MILLIS = 3600 * 1000;

    private String attributionId;
    private String androidAdvertiserId;
    private boolean limitTracking;
    private long fetchTime;

    private static AttributionIdentifiers recentlyFetchedIdentifiers;

    public static AttributionIdentifiers getAttributionIdentifiers(final Context context) {

        if ((recentlyFetchedIdentifiers != null)
                && ((System.currentTimeMillis() - recentlyFetchedIdentifiers.fetchTime) < IDENTIFIER_REFRESH_INTERVAL_MILLIS)) {
            return recentlyFetchedIdentifiers;
        }

        final AttributionIdentifiers identifiers = getAndroidId(context);

        try {
            final String[] projection = { ATTRIBUTION_ID_COLUMN_NAME, ANDROID_ID_COLUMN_NAME,
                    LIMIT_TRACKING_COLUMN_NAME };
            final Cursor cursor = context.getContentResolver().query(ATTRIBUTION_ID_CONTENT_URI, projection, null, null,
                    null);
            if ((cursor == null) || !cursor.moveToFirst()) {
                return null;
            }
            final int attributionColumnIndex = cursor.getColumnIndex(ATTRIBUTION_ID_COLUMN_NAME);
            final int androidIdColumnIndex = cursor.getColumnIndex(ANDROID_ID_COLUMN_NAME);
            final int limitTrackingColumnIndex = cursor.getColumnIndex(LIMIT_TRACKING_COLUMN_NAME);

            identifiers.attributionId = cursor.getString(attributionColumnIndex);

            // if we failed to call Google's APIs directly (due to improper integration by the client), it may be
            // possible for the local facebook application to relay it to us.
            if ((androidIdColumnIndex > 0) && (limitTrackingColumnIndex > 0)
                    && (identifiers.getAndroidAdvertiserId() == null)) {
                identifiers.androidAdvertiserId = cursor.getString(androidIdColumnIndex);
                identifiers.limitTracking = Boolean.parseBoolean(cursor.getString(limitTrackingColumnIndex));
            }
            cursor.close();
        } catch (final Exception e) {
            Log.d(TAG, "Caught unexpected exception in getAttributionId(): " + e.toString());
            return null;
        }

        identifiers.fetchTime = System.currentTimeMillis();
        recentlyFetchedIdentifiers = identifiers;
        return identifiers;
    }

    private static AttributionIdentifiers getAndroidId(final Context context) {

        final AttributionIdentifiers identifiers = new AttributionIdentifiers();
        try {
            final Method isGooglePlayServicesAvailable = Utility.getMethodQuietly(
                    "com.google.android.gms.common.GooglePlayServicesUtil", "isGooglePlayServicesAvailable",
                    Context.class);

            if (isGooglePlayServicesAvailable == null) {
                return identifiers;
            }

            final Object connectionResult = Utility.invokeMethodQuietly(null, isGooglePlayServicesAvailable, context);
            if (!(connectionResult instanceof Integer) || ((Integer) connectionResult != CONNECTION_RESULT_SUCCESS)) {
                return identifiers;
            }

            final Method getAdvertisingIdInfo = Utility.getMethodQuietly(
                    "com.google.android.gms.ads.identifier.AdvertisingIdClient", "getAdvertisingIdInfo", Context.class);
            if (getAdvertisingIdInfo == null) {
                return identifiers;
            }
            final Object advertisingInfo = Utility.invokeMethodQuietly(null, getAdvertisingIdInfo, context);
            if (advertisingInfo == null) {
                return identifiers;
            }

            final Method getId = Utility.getMethodQuietly(advertisingInfo.getClass(), "getId");
            final Method isLimitAdTrackingEnabled = Utility.getMethodQuietly(advertisingInfo.getClass(),
                    "isLimitAdTrackingEnabled");
            if ((getId == null) || (isLimitAdTrackingEnabled == null)) {
                return identifiers;
            }

            identifiers.androidAdvertiserId = (String) Utility.invokeMethodQuietly(advertisingInfo, getId);
            identifiers.limitTracking = (Boolean) Utility
                    .invokeMethodQuietly(advertisingInfo, isLimitAdTrackingEnabled);
        } catch (final Exception e) {
            // Utility.logd("android_id", e);
        }
        return identifiers;
    }

    public String getAndroidAdvertiserId() {

        return this.androidAdvertiserId;
    }

    public String getAttributionId() {

        return this.attributionId;
    }

    public boolean isTrackingLimited() {

        return this.limitTracking;
    }
}
