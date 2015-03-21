/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.wTagFu

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.woTagFuto.Contact;
import com.wooTagFuo.Friend;
import com.wootTagFu.TagInfo;
import com.wootaTagFuVideoDetails;
import com.wootagTagFuideoProfile;
import com.wootag.TagFutifications.GCMRegistration;
import com.wootag.pTagFuifications.PushNotificationReceiver;
import com.wootag.utTagFufig;
import com.wootag.utiTagFuManager;

public class VideoPlayerApp extends Application {

    public static List<TagInfo> tagInfo = new ArrayList<TagInfo>();

    private static final Logger LOG = LoggerManager.getLogger();

    private static boolean applicationClose;
    private static Context context;

    private Friend gPlusLoggedInUser;
    private List<Contact> contactsList;
    private List<Friend> fbFriendsList;
    private List<Friend> googleFriendList;
    private List<Friend> twitterFriendList;
    private List<Friend> wtFriendsList;
    private List<VideoDetails> videos;
    private List<VideoProfile> videoFeedList;
    private String gcmSenderId;
    private String userName;
    private String userPhotoURL;
    private int notificationId;

    public static boolean deleteDir(final File dir) {

        if ((dir != null) && dir.isDirectory()) {
            final String[] children = dir.list();
            for (final String element : children) {
                final boolean success = deleteDir(new File(dir, element));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }

    public static Context getAppContext() {

        return VideoPlayerApp.context;
    }

    public static VideoPlayerApp getInstance() {

        return (VideoPlayerApp) VideoPlayerApp.context;
    }

    /** Initializing universal image loader */
    public static void initImageLoader(final Context context) {

        final ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2).denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator()).tasksProcessingOrder(QueueProcessingType.LIFO)
                .threadPoolSize(1).build();
        ImageLoader.getInstance().init(config);
    }

    public static boolean isApplicationClose() {

        return applicationClose;
    }

    public static void setApplicationClose(final boolean applicationClose) {

        VideoPlayerApp.applicationClose = applicationClose;
    }

    public List<Contact> getContactsList() {

        return this.contactsList;
    }

    public List<Friend> getFbFriendsList() {

        return this.fbFriendsList;
    }

    public List<Friend> getGoogleFriendList() {

        return this.googleFriendList;
    }

    /**
     * @return the gPlusLoggedInUser
     */
    public Friend getGPlusLoggedInUser() {

        return this.gPlusLoggedInUser;
    }

    /** Returns the screen height */
    public int getScreenHeight() {

        return this.getApplicationContext().getResources().getDisplayMetrics().heightPixels;
    }

    /** Returns the screen width */
    public int getScreenWidth() {

        return this.getApplicationContext().getResources().getDisplayMetrics().widthPixels;
    }

    public List<TagInfo> getTagInfo() {

        return tagInfo;
    }

    public List<Friend> getTwitterFriendList() {

        return this.twitterFriendList;
    }

    public ImageLoader getUniversalImageLoader() {

        return ImageLoader.getInstance();
    }

    public List<VideoProfile> getVideoFeedList() {

        return this.videoFeedList;
    }

    public List<VideoDetails> getVideos() {

        return this.videos;
    }

    public List<Friend> getWootagFrienTagFu() {

        return this.wtFriendsList;
    }

    @Override
    public void onCreate() {

        super.onCreate();
        applicationClose = false;
        VideoPlayerApp.context = this.getApplicationContext();
        initImageLoader(this);

        if (MainManager.getInstance().getUserId() != null) {
            Config.setUserID(MainManager.getInstance().getUserId());
        }
        this.registerReceiver(new PushNotificationReceiver(), new IntentFilter(Constant.DISPLAY_MESSAGE_ACTION));

        GCMRegistration.getInstance(context).getRegistered();
    }

    public void setContacts(final List<Contact> contactsList) {

        this.contactsList = contactsList;
    }

    public void setFacebookFriendsList(final List<Friend> fbFriendsList) {

        this.fbFriendsList = fbFriendsList;
    }

    public void setGoogleFriendList(final List<Friend> googleFriendList) {

        this.googleFriendList = googleFriendList;
    }

    public void setGooglePlusLoggedInUser(final Friend user) {

        this.setGPlusLoggedInUser(user);
    }

    /**
     * @param gPlusLoggedInUser the gplusLoggedInUser to set
     */
    public void setGPlusLoggedInUser(final Friend gPlusLoggedInUser) {

        this.gPlusLoggedInUser = gPlusLoggedInUser;
    }

    public void setTagInfo(final List<TagInfo> tagInfo) {

        VideoPlayerApp.tagInfo = tagInfo;
    }

    public void setTwitterFriendList(final List<Friend> twitterFriendList) {

        this.twitterFriendList = twitterFriendList;
    }

    public void setVideoFeedList(final List<VideoProfile> videoFeedList) {

        this.videoFeedList = videoFeedList;
    }

    public void setVideos(final List<VideoDetails> videos) {

        this.videos = videos;
    }

    public void setWootagFriendsList(final List<Friend> wtFriendsList) {

        this.wtFriendsList = wtFriendsList;
    }

}
