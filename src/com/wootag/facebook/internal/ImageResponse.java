/**
 * Copyright 2010-present Facebook. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package com.TagFu.facebook.internal;

import android.graphics.Bitmap;

public class ImageResponse {

    private final ImageRequest request;
    private final Exception error;
    private final boolean isCachedRedirect;
    private final Bitmap bitmap;

    ImageResponse(final ImageRequest request, final Exception error, final boolean isCachedRedirect, final Bitmap bitmap) {

        this.request = request;
        this.error = error;
        this.bitmap = bitmap;
        this.isCachedRedirect = isCachedRedirect;
    }

    public Bitmap getBitmap() {

        return this.bitmap;
    }

    public Exception getError() {

        return this.error;
    }

    public ImageRequest getRequest() {

        return this.request;
    }

    public boolean isCachedRedirect() {

        return this.isCachedRedirect;
    }
}
