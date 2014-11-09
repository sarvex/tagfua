/**
 * Copyright 2010-present Facebook. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package com.wootag.facebook;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.UUID;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.util.Pair;

/**
 * <p>
 * Implements a <a href="http://developer.android.com/reference/android/content/ContentProvider.html">
 * ContentProvider</a> that can be used to provide binary attachments (e.g., images) to calls made via @link
 * FacebookDialog}. The {@link NativeAppCallAttachmentStore} class provides methods to attach and clean up the
 * attachments.
 * <p>
 * Note that this ContentProvider is only necessary if an application wishes to attach images, etc., that are stored in
 * memory and do not have another way to be referenced by a content URI. For images obtained from, e.g., the Camera or
 * Gallery, that already have a content URI associated with them, use of this class is not necessary.
 * </p>
 * <p>
 * If an application wishes to attach images that are stored in-memory within the application, this content provider
 * must be listed in the application's AndroidManifest.xml, and it should be named according to the pattern
 * <code>"com.facebook.app.NativeAppCallContentProvider{FACEBOOK_APP_ID}"</code>. See the
 * {@link NativeAppCallContentProvider#getAttachmentUrl(String) getContentProviderName} method.
 * </p>
 */
public class NativeAppCallContentProvider extends ContentProvider {

    private static final String TAG = NativeAppCallContentProvider.class.getName();
    private static final String ATTACHMENT_URL_BASE = "content://com.facebook.app.NativeAppCallContentProvider";

    private final AttachmentDataSource dataSource;

    public NativeAppCallContentProvider() {

        this(new NativeAppCallAttachmentStore());
    }

    NativeAppCallContentProvider(final AttachmentDataSource dataSource) {

        this.dataSource = dataSource;
    }

    /**
     * Returns the name of the content provider formatted correctly for constructing URLs.
     *
     * @param applicationId the Facebook application ID of the application
     * @return the String to use as the authority portion of a content URI.
     */
    public static String getAttachmentUrl(final String applicationId, final UUID callId, final String attachmentName) {

        return String.format("%s%s/%s/%s", ATTACHMENT_URL_BASE, applicationId, callId.toString(), attachmentName);
    }

    @Override
    public int delete(final Uri uri, final String s, final String[] strings) {

        return 0;
    }

    @Override
    public String getType(final Uri uri) {

        return null;
    }

    @Override
    public Uri insert(final Uri uri, final ContentValues contentValues) {

        return null;
    }

    @Override
    public boolean onCreate() {

        return true;
    }

    @Override
    public ParcelFileDescriptor openFile(final Uri uri, final java.lang.String mode) throws FileNotFoundException {

        final Pair<UUID, String> callIdAndAttachmentName = this.parseCallIdAndAttachmentName(uri);
        if (callIdAndAttachmentName == null) {
            throw new FileNotFoundException();
        }

        try {
            final File file = this.dataSource.openAttachment(callIdAndAttachmentName.first,
                    callIdAndAttachmentName.second);

            return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        } catch (final FileNotFoundException exception) {
            Log.e(TAG, "Got unexpected exception:" + exception);
            throw exception;
        }
    }

    @Override
    public Cursor query(final Uri uri, final String[] strings, final String s, final String[] strings2, final String s2) {

        return null;
    }

    @Override
    public int update(final Uri uri, final ContentValues contentValues, final String s, final String[] strings) {

        return 0;
    }

    Pair<UUID, String> parseCallIdAndAttachmentName(final Uri uri) {

        try {
            // We don't do explicit format checking here. Malformed URIs may generate NullPointerExceptions or
            // array bounds exceptions, which we'll catch and return null. All of these will result in a
            // FileNotFoundException being thrown in openFile.
            final String callIdAndAttachmentName = uri.getPath().substring(1);
            final String[] parts = callIdAndAttachmentName.split("/");

            final String callIdString = parts[0];
            final String attachmentName = parts[1];
            final UUID callId = UUID.fromString(callIdString);

            return new Pair<UUID, String>(callId, attachmentName);
        } catch (final Exception exception) {
            return null;
        }
    }

    interface AttachmentDataSource {

        File openAttachment(UUID callId, String attachmentName) throws FileNotFoundException;
    }
}
