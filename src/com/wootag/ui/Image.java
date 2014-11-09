/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.ui;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.DiscCacheUtil;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.MemoryCacheUtil;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wootag.R;
import com.wootag.VideoPlayerApp;

public class Image {

    private static final Logger LOG = LoggerManager.getLogger();
    private static final ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

    /**
     * Clear all cached images from memory and disc
     */
    public static void clearImageFromCache() {

        VideoPlayerApp.getInstance().getUniversalImageLoader().getDiscCache().clear();
        VideoPlayerApp.getInstance().getUniversalImageLoader().getMemoryCache().clear();
        VideoPlayerApp.getInstance().getUniversalImageLoader().clearDiscCache();
        VideoPlayerApp.getInstance().getUniversalImageLoader().clearMemoryCache();
    }

    /**
     * Download the image from given url and set it to given imageview.
     *
     * @param url
     * @param activity
     * @param imageView
     * @param type for default image
     */
    public static void displayImage(final String url, final Activity activity, final ImageView imageView, final int type) {

        imageView.setTag(Integer.valueOf(type));
        Image.setImage(imageView);
        DisplayImageOptions options = null;

        switch (type) {
        case 0:
            options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.member)
                    .showImageForEmptyUri(R.drawable.member).showImageOnFail(R.drawable.member).cacheOnDisc(true)
                    .cacheInMemory(true).imageScaleType(ImageScaleType.EXACTLY).build();
            Image.displayProfileImage(url, imageView, type);
            break;
        case 1:
            options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.profile_banner)
                    .showImageForEmptyUri(R.drawable.profile_banner).showImageOnFail(R.drawable.profile_banner)
                    .cacheOnDisc(true).cacheInMemory(true).imageScaleType(ImageScaleType.EXACTLY).build();
            VideoPlayerApp.getInstance().getUniversalImageLoader()
                    .displayImage(url, imageView, options, animateFirstListener);
            break;
        case 2:
            options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.notif_banner)
                    .showImageForEmptyUri(R.drawable.notif_banner).showImageOnFail(R.drawable.notif_banner)
                    .cacheOnDisc(true).cacheInMemory(true).imageScaleType(ImageScaleType.NONE).build();
            VideoPlayerApp.getInstance().getUniversalImageLoader()
                    .displayImage(url, imageView, options, animateFirstListener);
            break;
        case 3:
        default:
            options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.defaultpicture)
                    .showImageForEmptyUri(R.drawable.defaultpicture).showImageOnFail(R.drawable.defaultpicture)
                    .cacheOnDisc(true).cacheInMemory(true).imageScaleType(ImageScaleType.NONE).build();
            try {
                Thread.sleep(1000);
            } catch (final InterruptedException exception) {
                LOG.e(exception);
            }
            Image.displayProfileImage(url, imageView, type);
            break;
        }
    }

    /**
     * Loading the image from given url every time.
     *
     * @param url
     * @param imageView
     * @param type for default image
     */
    public static void displayProfileImage(final String url, final ImageView imageView, final int type) {

        imageView.setTag(Integer.valueOf(type));
        Image.setImage(imageView);

        VideoPlayerApp.getInstance().getUniversalImageLoader()
                .displayImage(url, imageView, new SimpleImageLoadingListener() {

                    boolean cacheFound;

                    @Override
                    public void onLoadingComplete(final String imageUri, final View view, final Bitmap loadedImage) {

                        if (this.cacheFound) {
                            MemoryCacheUtil.removeFromCache(imageUri, VideoPlayerApp.getInstance()
                                    .getUniversalImageLoader().getMemoryCache());
                            DiscCacheUtil.removeFromCache(imageUri, VideoPlayerApp.getInstance()
                                    .getUniversalImageLoader().getDiscCache());

                            VideoPlayerApp.getInstance().getUniversalImageLoader()
                                    .displayImage(imageUri, (ImageView) view);
                        }
                    }

                    @Override
                    public void onLoadingStarted(final String imageUri, final View view) {

                        final List<String> memCache = MemoryCacheUtil.findCacheKeysForImageUri(imageUri, VideoPlayerApp
                                .getInstance().getUniversalImageLoader().getMemoryCache());
                        this.cacheFound ^= memCache.isEmpty();
                        if (!this.cacheFound) {
                            final File discCache = DiscCacheUtil.findInCache(imageUri, VideoPlayerApp.getInstance()
                                    .getUniversalImageLoader().getDiscCache());
                            if (discCache != null) {
                                this.cacheFound = discCache.exists();
                            }
                        }
                    }

                });

    }

    /**
     * Sets the default image for given image view based on tag.
     *
     * @param imageView
     */
    private static void setImage(final ImageView imageView) {

        if (imageView.getTag() instanceof Integer) {
            switch (((Integer) imageView.getTag()).intValue()) {
            case 0:
                imageView.setImageResource(R.drawable.member);
                break;
            case 1:
                imageView.setImageResource(R.drawable.profile_banner);
                break;
            case 2:
                imageView.setImageResource(R.drawable.notif_banner);
                break;
            case 3:
            default:
                imageView.setImageResource(R.drawable.defaultpicture);
                break;
            }
        }
    }

    protected static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

        private static final List<String> DISPLAYED_IMAGES = Collections.synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingComplete(final String imageUri, final View view, final Bitmap loadedImage) {

            if (loadedImage != null) {
                final ImageView imageView = (ImageView) view;
                final boolean firstDisplay = !DISPLAYED_IMAGES.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 500);
                    DISPLAYED_IMAGES.add(imageUri);
                }
            }
        }
    }

}
