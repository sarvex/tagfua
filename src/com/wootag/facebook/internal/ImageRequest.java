/**
 * Copyright 2010-present Facebook. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package com.wootag.facebook.internal;

import java.net.URI;
import java.net.URISyntaxException;

import android.content.Context;
import android.net.Uri;

public class ImageRequest {

    public static final int UNSPECIFIED_DIMENSION = 0;

    private static final String PROFILEPIC_URL_FORMAT = "https://graph.facebook.com/%s/picture";

    private static final String HEIGHT_PARAM = "height";
    private static final String WIDTH_PARAM = "width";
    private static final String MIGRATION_PARAM = "migration_overrides";
    private static final String MIGRATION_VALUE = "{october_2012:true}";
    private final Context context;

    private final URI imageUri;
    private final Callback callback;
    private final boolean allowCachedRedirects;
    private final Object callerTag;

    private ImageRequest(final Builder builder) {

        this.context = builder.context;
        this.imageUri = builder.imageUrl;
        this.callback = builder.callback;
        this.allowCachedRedirects = builder.allowCachedRedirects;
        this.callerTag = builder.callerTag == null ? new Object() : builder.callerTag;
    }

    public static URI getProfilePictureUrl(final String userId, int width, int height) throws URISyntaxException {

        Validate.notNullOrEmpty(userId, "userId");

        width = Math.max(width, UNSPECIFIED_DIMENSION);
        height = Math.max(height, UNSPECIFIED_DIMENSION);

        if ((width == UNSPECIFIED_DIMENSION) && (height == UNSPECIFIED_DIMENSION)) {
            throw new IllegalArgumentException("Either width or height must be greater than 0");
        }

        final Uri.Builder builder = new Uri.Builder().encodedPath(String.format(PROFILEPIC_URL_FORMAT, userId));

        if (height != UNSPECIFIED_DIMENSION) {
            builder.appendQueryParameter(HEIGHT_PARAM, String.valueOf(height));
        }

        if (width != UNSPECIFIED_DIMENSION) {
            builder.appendQueryParameter(WIDTH_PARAM, String.valueOf(width));
        }

        builder.appendQueryParameter(MIGRATION_PARAM, MIGRATION_VALUE);

        return new URI(builder.toString());
    }

    public Callback getCallback() {

        return this.callback;
    }

    public Object getCallerTag() {

        return this.callerTag;
    }

    public Context getContext() {

        return this.context;
    }

    public URI getImageUri() {

        return this.imageUri;
    }

    public boolean isCachedRedirectAllowed() {

        return this.allowCachedRedirects;
    }

    public static class Builder {

        protected final Context context;
        protected final URI imageUrl;
        protected Callback callback;
        protected boolean allowCachedRedirects;
        protected Object callerTag;

        public Builder(final Context context, final URI imageUrl) {

            Validate.notNull(imageUrl, "imageUrl");
            this.context = context;
            this.imageUrl = imageUrl;
        }

        public ImageRequest build() {

            return new ImageRequest(this);
        }

        public Builder setAllowCachedRedirects(final boolean allowCachedRedirects) {

            this.allowCachedRedirects = allowCachedRedirects;
            return this;
        }

        public Builder setCallback(final Callback callback) {

            this.callback = callback;
            return this;
        }

        public Builder setCallerTag(final Object callerTag) {

            this.callerTag = callerTag;
            return this;
        }
    }

    public interface Callback {

        /**
         * This method should always be called on the UI thread. ImageDownloader makes sure to do this when it is
         * responsible for issuing the ImageResponse
         *
         * @param response
         */
        void onCompleted(ImageResponse response);
    }
}
