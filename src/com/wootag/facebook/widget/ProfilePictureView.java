/**
 * Copyright 2010-present Facebook. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package com.wootag.facebook.widget;

import java.net.URISyntaxException;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.wootag.R;
import com.wootag.facebook.FacebookException;
import com.wootag.facebook.LoggingBehavior;
import com.wootag.facebook.internal.ImageDownloader;
import com.wootag.facebook.internal.ImageRequest;
import com.wootag.facebook.internal.ImageResponse;
import com.wootag.facebook.internal.Logger;
import com.wootag.facebook.internal.Utility;

/**
 * View that displays the profile photo of a supplied profile ID, while conforming to user specified dimensions.
 */
public class ProfilePictureView extends FrameLayout {

    /**
     * Tag used when logging calls are made by ProfilePictureView
     */
    public static final String TAG = ProfilePictureView.class.getSimpleName();

    /**
     * Indicates that the specific size of the View will be set via layout params. ProfilePictureView will default to
     * NORMAL X NORMAL, if the layout params set on this instance do not have a fixed size. Used in calls to
     * setPresetSize() and getPresetSize(). Corresponds with the preset_size Xml attribute that can be set on
     * ProfilePictureView.
     */
    public static final int CUSTOM = -1;

    /**
     * Indicates that the profile image should fit in a SMALL X SMALL space, regardless of whether the cropped or
     * un-cropped version is chosen. Used in calls to setPresetSize() and getPresetSize(). Corresponds with the
     * preset_size Xml attribute that can be set on ProfilePictureView.
     */
    public static final int SMALL = -2;

    /**
     * Indicates that the profile image should fit in a NORMAL X NORMAL space, regardless of whether the cropped or
     * un-cropped version is chosen. Used in calls to setPresetSize() and getPresetSize(). Corresponds with the
     * preset_size Xml attribute that can be set on ProfilePictureView.
     */
    public static final int NORMAL = -3;

    /**
     * Indicates that the profile image should fit in a LARGE X LARGE space, regardless of whether the cropped or
     * un-cropped version is chosen. Used in calls to setPresetSize() and getPresetSize(). Corresponds with the
     * preset_size Xml attribute that can be set on ProfilePictureView.
     */
    public static final int LARGE = -4;

    private static final int MIN_SIZE = 1;

    private static final boolean IS_CROPPED_DEFAULT_VALUE = true;
    private static final String SUPER_STATE_KEY = "ProfilePictureView_superState";
    private static final String PROFILE_ID_KEY = "ProfilePictureView_profileId";
    private static final String PRESET_SIZE_KEY = "ProfilePictureView_presetSize";
    private static final String IS_CROPPED_KEY = "ProfilePictureView_isCropped";
    private static final String BITMAP_KEY = "ProfilePictureView_bitmap";
    private static final String BITMAP_WIDTH_KEY = "ProfilePictureView_width";
    private static final String BITMAP_HEIGHT_KEY = "ProfilePictureView_height";
    private static final String PENDING_REFRESH_KEY = "ProfilePictureView_refresh";
    private String profileId;

    private int queryHeight = ImageRequest.UNSPECIFIED_DIMENSION;
    private int queryWidth = ImageRequest.UNSPECIFIED_DIMENSION;
    private boolean isCropped = IS_CROPPED_DEFAULT_VALUE;
    private Bitmap imageContents;
    private ImageView image;
    private int presetSizeType = CUSTOM;
    private ImageRequest lastRequest;
    private OnErrorListener onErrorListener;
    private Bitmap customizedDefaultProfilePicture;

    /**
     * Constructor
     *
     * @param context Context for this View
     */
    public ProfilePictureView(final Context context) {

        super(context);
        this.initialize(context);
    }

    /**
     * Constructor
     *
     * @param context Context for this View
     * @param attrs AttributeSet for this View. The attribute 'preset_size' is processed here
     */
    public ProfilePictureView(final Context context, final AttributeSet attrs) {

        super(context, attrs);
        this.initialize(context);
        this.parseAttributes(attrs);
    }

    /**
     * Constructor
     *
     * @param context Context for this View
     * @param attrs AttributeSet for this View. The attribute 'preset_size' is processed here
     * @param defStyle Default style for this View
     */
    public ProfilePictureView(final Context context, final AttributeSet attrs, final int defStyle) {

        super(context, attrs, defStyle);
        this.initialize(context);
        this.parseAttributes(attrs);
    }

    /**
     * Returns the current OnErrorListener for this instance of ProfilePictureView
     *
     * @return The OnErrorListener
     */
    public final OnErrorListener getOnErrorListener() {

        return this.onErrorListener;
    }

    /**
     * Gets the current preset size type
     *
     * @return The current preset size type, if set; CUSTOM if not
     */
    public final int getPresetSize() {

        return this.presetSizeType;
    }

    /**
     * Returns the profile Id for the current profile photo
     *
     * @return The profile Id
     */
    public final String getProfileId() {

        return this.profileId;
    }

    /**
     * Indicates whether the cropped version of the profile photo has been chosen
     *
     * @return True if the cropped version is chosen, false if not.
     */
    public final boolean isCropped() {

        return this.isCropped;
    }

    /**
     * Sets the profile photo to be the cropped version, or the original version
     *
     * @param showCroppedVersion True to select the cropped version False to select the standard version
     */
    public final void setCropped(final boolean showCroppedVersion) {

        this.isCropped = showCroppedVersion;
        // No need to force the refresh since we will catch the change in required dimensions
        this.refreshImage(false);
    }

    /**
     * The ProfilePictureView will display the provided image while the specified profile is being loaded, or if the
     * specified profile is not available.
     *
     * @param inputBitmap The bitmap to render until the actual profile is loaded.
     */
    public final void setDefaultProfilePicture(final Bitmap inputBitmap) {

        this.customizedDefaultProfilePicture = inputBitmap;
    }

    /**
     * Sets an OnErrorListener for this instance of ProfilePictureView to call into when certain exceptions occur.
     *
     * @param onErrorListener The Listener object to set
     */
    public final void setOnErrorListener(final OnErrorListener onErrorListener) {

        this.onErrorListener = onErrorListener;
    }

    /**
     * Apply a preset size to this profile photo
     *
     * @param sizeType The size type to apply: SMALL, NORMAL or LARGE
     */
    public final void setPresetSize(final int sizeType) {

        switch (sizeType) {
        case SMALL:
        case NORMAL:
        case LARGE:
        case CUSTOM:
            this.presetSizeType = sizeType;
            break;

        default:
            throw new IllegalArgumentException("Must use a predefined preset size");
        }

        this.requestLayout();
    }

    /**
     * Sets the profile Id for this profile photo
     *
     * @param profileId The profileId NULL/Empty String will show the blank profile photo
     */
    public final void setProfileId(final String profileId) {

        boolean force = false;
        if (Utility.isNullOrEmpty(this.profileId) || !this.profileId.equalsIgnoreCase(profileId)) {
            // Clear out the old profilePicture before requesting for the new one.
            this.setBlankProfilePicture();
            force = true;
        }

        this.profileId = profileId;
        this.refreshImage(force);
    }

    private int getPresetSizeInPixels(final boolean forcePreset) {

        int dimensionId;
        switch (this.presetSizeType) {
        case SMALL:
            dimensionId = R.dimen.com_facebook_profilepictureview_preset_size_small;
            break;
        case NORMAL:
            dimensionId = R.dimen.com_facebook_profilepictureview_preset_size_normal;
            break;
        case LARGE:
            dimensionId = R.dimen.com_facebook_profilepictureview_preset_size_large;
            break;
        case CUSTOM:
            if (!forcePreset) {
                return ImageRequest.UNSPECIFIED_DIMENSION;
            }
            dimensionId = R.dimen.com_facebook_profilepictureview_preset_size_normal;
            break;
        default:
            return ImageRequest.UNSPECIFIED_DIMENSION;
        }

        return this.getResources().getDimensionPixelSize(dimensionId);
    }

    private void initialize(final Context context) {

        // We only want our ImageView in here. Nothing else is permitted
        this.removeAllViews();

        this.image = new ImageView(context);

        final LayoutParams imageLayout = new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT);

        this.image.setLayoutParams(imageLayout);

        // We want to prevent up-scaling the image, but still have it fit within
        // the layout bounds as best as possible.
        this.image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        this.addView(this.image);
    }

    private void parseAttributes(final AttributeSet attrs) {

        final TypedArray a = this.getContext().obtainStyledAttributes(attrs,
                R.styleable.com_facebook_profile_picture_view);
        this.setPresetSize(a.getInt(R.styleable.com_facebook_profile_picture_view_preset_size, CUSTOM));
        this.isCropped = a.getBoolean(R.styleable.com_facebook_profile_picture_view_is_cropped,
                IS_CROPPED_DEFAULT_VALUE);
        a.recycle();
    }

    private void refreshImage(final boolean force) {

        final boolean changed = this.updateImageQueryParameters();
        // Note: do not use Utility.isNullOrEmpty here as this will cause the Eclipse
        // Graphical Layout editor to fail in some cases
        if ((this.profileId == null)
                || (this.profileId.length() == 0)
                || ((this.queryWidth == ImageRequest.UNSPECIFIED_DIMENSION) && (this.queryHeight == ImageRequest.UNSPECIFIED_DIMENSION))) {
            this.setBlankProfilePicture();
        } else if (changed || force) {
            this.sendImageRequest(true);
        }
    }

    private void sendImageRequest(final boolean allowCachedResponse) {

        try {
            final ImageRequest.Builder requestBuilder = new ImageRequest.Builder(this.getContext(),
                    ImageRequest.getProfilePictureUrl(this.profileId, this.queryWidth, this.queryHeight));

            final ImageRequest request = requestBuilder.setAllowCachedRedirects(allowCachedResponse).setCallerTag(this)
                    .setCallback(new ImageRequest.Callback() {

                        @Override
                        public void onCompleted(final ImageResponse response) {

                            ProfilePictureView.this.processResponse(response);
                        }
                    }).build();

            // Make sure to cancel the old request before sending the new one to prevent
            // accidental cancellation of the new request. This could happen if the URL and
            // caller tag stayed the same.
            if (this.lastRequest != null) {
                ImageDownloader.cancelRequest(this.lastRequest);
            }
            this.lastRequest = request;

            ImageDownloader.downloadAsync(request);
        } catch (final URISyntaxException e) {
            Logger.log(LoggingBehavior.REQUESTS, Log.ERROR, TAG, e.toString());
        }
    }

    private void setBlankProfilePicture() {

        if (this.customizedDefaultProfilePicture == null) {
            final int blankImageResource = this.isCropped() ? R.drawable.com_facebook_profile_picture_blank_square
                    : R.drawable.com_facebook_profile_picture_blank_portrait;
            this.setImageBitmap(BitmapFactory.decodeResource(this.getResources(), blankImageResource));
        } else {
            // Update profile image dimensions.
            this.updateImageQueryParameters();
            // Resize inputBitmap to new dimensions of queryWidth and queryHeight.
            final Bitmap scaledBitmap = Bitmap.createScaledBitmap(this.customizedDefaultProfilePicture,
                    this.queryWidth, this.queryHeight, false);
            this.setImageBitmap(scaledBitmap);
        }
    }

    private void setImageBitmap(final Bitmap imageBitmap) {

        if ((this.image != null) && (imageBitmap != null)) {
            this.imageContents = imageBitmap; // Hold for save-restore cycles
            this.image.setImageBitmap(imageBitmap);
        }
    }

    private boolean updateImageQueryParameters() {

        int newHeightPx = this.getHeight();
        int newWidthPx = this.getWidth();
        if ((newWidthPx < MIN_SIZE) || (newHeightPx < MIN_SIZE)) {
            // Not enough space laid out for this View yet. Or something else is awry.
            return false;
        }

        final int presetSize = this.getPresetSizeInPixels(false);
        if (presetSize != ImageRequest.UNSPECIFIED_DIMENSION) {
            newWidthPx = presetSize;
            newHeightPx = presetSize;
        }

        // The cropped version is square
        // If full version is desired, then only one dimension is required.
        if (newWidthPx <= newHeightPx) {
            newHeightPx = this.isCropped() ? newWidthPx : ImageRequest.UNSPECIFIED_DIMENSION;
        } else {
            newWidthPx = this.isCropped() ? newHeightPx : ImageRequest.UNSPECIFIED_DIMENSION;
        }

        final boolean changed = (newWidthPx != this.queryWidth) || (newHeightPx != this.queryHeight);

        this.queryWidth = newWidthPx;
        this.queryHeight = newHeightPx;

        return changed;
    }

    @Override
    protected void onDetachedFromWindow() {

        super.onDetachedFromWindow();

        // Null out lastRequest. This way, when the response is returned, we can ascertain
        // that the view is detached and hence should not attempt to update its contents.
        this.lastRequest = null;
    }

    /**
     * In addition to calling super.Layout(), we also attempt to get a new image that is properly size for the layout
     * dimensions
     */
    @Override
    protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {

        super.onLayout(changed, left, top, right, bottom);

        // See if the image needs redrawing
        this.refreshImage(false);
    }

    /**
     * Overriding onMeasure to handle the case where WRAP_CONTENT might be specified in the layout. Since we don't know
     * the dimensions of the profile photo, we need to handle this case specifically.
     * <p/>
     * The approach is to default to a NORMAL sized amount of space in the case that a preset size is not specified.
     * This logic is applied to both width and height
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        final ViewGroup.LayoutParams params = this.getLayoutParams();
        boolean customMeasure = false;
        int newHeight = MeasureSpec.getSize(heightMeasureSpec);
        int newWidth = MeasureSpec.getSize(widthMeasureSpec);
        if ((MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY)
                && (params.height == ViewGroup.LayoutParams.WRAP_CONTENT)) {
            newHeight = this.getPresetSizeInPixels(true); // Default to a preset size
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(newHeight, MeasureSpec.EXACTLY);
            customMeasure = true;
        }

        if ((MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY)
                && (params.width == ViewGroup.LayoutParams.WRAP_CONTENT)) {
            newWidth = this.getPresetSizeInPixels(true); // Default to a preset size
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(newWidth, MeasureSpec.EXACTLY);
            customMeasure = true;
        }

        if (customMeasure) {
            // Since we are providing custom dimensions, we need to handle the measure
            // phase from here
            this.setMeasuredDimension(newWidth, newHeight);
            this.measureChildren(widthMeasureSpec, heightMeasureSpec);
        } else {
            // Rely on FrameLayout to do the right thing
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    /**
     * If the passed in state is a Bundle, an attempt is made to restore from it.
     *
     * @param state a Parcelable containing the current state
     */
    @Override
    protected void onRestoreInstanceState(final Parcelable state) {

        if (state.getClass() != Bundle.class) {
            super.onRestoreInstanceState(state);
        } else {
            final Bundle instanceState = (Bundle) state;
            super.onRestoreInstanceState(instanceState.getParcelable(SUPER_STATE_KEY));

            this.profileId = instanceState.getString(PROFILE_ID_KEY);
            this.presetSizeType = instanceState.getInt(PRESET_SIZE_KEY);
            this.isCropped = instanceState.getBoolean(IS_CROPPED_KEY);
            this.queryWidth = instanceState.getInt(BITMAP_WIDTH_KEY);
            this.queryHeight = instanceState.getInt(BITMAP_HEIGHT_KEY);

            this.setImageBitmap((Bitmap) instanceState.getParcelable(BITMAP_KEY));

            if (instanceState.getBoolean(PENDING_REFRESH_KEY)) {
                this.refreshImage(true);
            }
        }
    }

    /**
     * Some of the current state is returned as a Bundle to allow quick restoration of the ProfilePictureView object in
     * scenarios like orientation changes.
     *
     * @return a Parcelable containing the current state
     */
    @Override
    protected Parcelable onSaveInstanceState() {

        final Parcelable superState = super.onSaveInstanceState();
        final Bundle instanceState = new Bundle();
        instanceState.putParcelable(SUPER_STATE_KEY, superState);
        instanceState.putString(PROFILE_ID_KEY, this.profileId);
        instanceState.putInt(PRESET_SIZE_KEY, this.presetSizeType);
        instanceState.putBoolean(IS_CROPPED_KEY, this.isCropped);
        instanceState.putParcelable(BITMAP_KEY, this.imageContents);
        instanceState.putInt(BITMAP_WIDTH_KEY, this.queryWidth);
        instanceState.putInt(BITMAP_HEIGHT_KEY, this.queryHeight);
        instanceState.putBoolean(PENDING_REFRESH_KEY, this.lastRequest != null);

        return instanceState;
    }

    void processResponse(final ImageResponse response) {

        // First check if the response is for the right request. We may have:
        // 1. Sent a new request, thus super-ceding this one.
        // 2. Detached this view, in which case the response should be discarded.
        if (response.getRequest() == this.lastRequest) {
            this.lastRequest = null;
            final Bitmap responseImage = response.getBitmap();
            final Exception error = response.getError();
            if (error != null) {
                final OnErrorListener listener = this.onErrorListener;
                if (listener != null) {
                    listener.onError(new FacebookException("Error in downloading profile picture for profileId: "
                            + this.getProfileId(), error));
                } else {
                    Logger.log(LoggingBehavior.REQUESTS, Log.ERROR, TAG, error.toString());
                }
            } else if (responseImage != null) {
                this.setImageBitmap(responseImage);

                if (response.isCachedRedirect()) {
                    this.sendImageRequest(false);
                }
            }
        }
    }

    /**
     * Callback interface that will be called when a network or other error is encountered while retrieving profile
     * pictures.
     */
    public interface OnErrorListener {

        /**
         * Called when a network or other error is encountered.
         *
         * @param error a FacebookException representing the error that was encountered.
         */
        void onError(FacebookException error);
    }
}
