/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wTagFuutil;

import twitter4j.Twitter;

import com.wootag.dto.ProductDetails;

public final class Config {

    private static ProductDetails productDetails;
    private static String currentUploadVideoId = "";
    private static String deviceResolutionValue;
    private static String deviceToken = "";
    private static String facebookAccessToken = "";
    private static String facebookLoggedUserId = "";
    private static String googlePlusLoggedUserId = "";
    private static String socialSite = "socialsite";
    private static String twitterRequestFor = "";
    private static String twitterScreenId;
    private static String userId = "";
    private static Twitter twitterObject;
    private static boolean myPageVisit;
    private static boolean newlyCreatedVideo;
    private static boolean notificationPageVisit;
    private static boolean playbackEnd;
    private static boolean playerRequestStart;
    private static boolean privateGroupEditMode;
    private static boolean twitterRequestMade;
    private static int currentTabIndex;
    private static int followingCount;
    private static int privateGroupCount;
    private static int uploadedPercentage;
    private static int videoCurrentPosition;

    public static int getCurrentTabIndex() {

        return currentTabIndex;
    }

    public static String getCurrentUploadVideoId() {

        return currentUploadVideoId;
    }

    public static String getDeviceResolutionValue() {

        return deviceResolutionValue;
    }

    public static String getDeviceToken() {

        return deviceToken;
    }

    public static String getFacebookAccessToken() {

        return facebookAccessToken;
    }

    public static String getFacebookLoggedUserId() {

        return facebookLoggedUserId;
    }

    public static int getFollowingCount() {

        return followingCount;
    }

    public static String getGoogleplusLoggedUserId() {

        return googlePlusLoggedUserId;
    }

    public static int getPrivateGroupCount() {

        return privateGroupCount;
    }

    public static ProductDetails getProductDetails() {

        return productDetails;
    }

    public static String getSocialSite() {

        return socialSite;
    }

    public static Twitter getTwitterObject() {

        return twitterObject;
    }

    public static String getTwitterRequestFor() {

        return twitterRequestFor;
    }

    public static String getTwitterScreenId() {

        return twitterScreenId;
    }

    public static int getUploadedPercentage() {

        return uploadedPercentage;
    }

    public static String getUserId() {

        return userId;
    }

    public static int getVideoCurrentPosition() {

        return videoCurrentPosition;
    }

    public static boolean isMypageVisit() {

        return myPageVisit;
    }

    public static boolean isNewlyCreatedVideo() {

        return newlyCreatedVideo;
    }

    public static boolean isNotificationPageVisit() {

        return notificationPageVisit;
    }

    public static boolean isPlaybackEnd() {

        return playbackEnd;
    }

    public static boolean isPlayerRequestStart() {

        return playerRequestStart;
    }

    public static boolean isPrivateGroupEditMode() {

        return privateGroupEditMode;
    }

    public static boolean isTwitterRequestMade() {

        return twitterRequestMade;
    }

    public static void setCurrentTabIndex(final int currentTabIndex) {

        Config.currentTabIndex = currentTabIndex;
    }

    public static void setCurrentUploadVideoID(final String currentUploadVideoId) {

        Config.currentUploadVideoId = currentUploadVideoId;
    }

    public static void setDeviceResolutionValue(final String deviceResolutionValue) {

        Config.deviceResolutionValue = deviceResolutionValue;
    }

    public static void setDeviceToken(final String deviceToken) {

        Config.deviceToken = deviceToken;
    }

    public static void setFacebookAccessToken(final String facebookAccessToken) {

        Config.facebookAccessToken = facebookAccessToken;
    }

    public static void setFacebookLoggedUserId(final String facebookLoggedUserId) {

        Config.facebookLoggedUserId = facebookLoggedUserId;
    }

    public static void setFollowingCount(final int followingCount) {

        Config.followingCount = followingCount;
    }

    public static void setGoogleplusLoggedUserId(final String googleplusLoggedUserId) {

        Config.googlePlusLoggedUserId = googleplusLoggedUserId;
    }

    public static void setMyPageVisit(final boolean myPageVisit) {

        Config.myPageVisit = myPageVisit;
    }

    public static void setNewlyCreatedVideo(final boolean newlyCreatedVideo) {

        Config.newlyCreatedVideo = newlyCreatedVideo;
    }

    public static void setNotificationPageVisit(final boolean notificationPageVisit) {

        Config.notificationPageVisit = notificationPageVisit;
    }

    public static void setPlaybackEnd(final boolean playbackEnd) {

        Config.playbackEnd = playbackEnd;
    }

    public static void setPlayerRequestStart(final boolean playerRequestStart) {

        Config.playerRequestStart = playerRequestStart;
    }

    public static void setPrivateGroupCount(final int privateGroupCount) {

        Config.privateGroupCount = privateGroupCount;
    }

    public static void setPrivateGroupEditMode(final boolean privateGroupEditMode) {

        Config.privateGroupEditMode = privateGroupEditMode;
    }

    public static void setProductDetails(final ProductDetails productDetails) {

        Config.productDetails = productDetails;
    }

    public static void setSocialSite(final String socialSite) {

        Config.socialSite = socialSite;
    }

    public static void setTwitterObject(final Twitter twitterObject) {

        Config.twitterObject = twitterObject;
    }

    public static void setTwitterRequestFor(final String twitterRequestFor) {

        Config.twitterRequestFor = twitterRequestFor;
    }

    public static void setTwitterRequestMade(final boolean twitterRequestMade) {

        Config.twitterRequestMade = twitterRequestMade;
    }

    public static void setTwitterScreenId(final String twitterScreenId) {

        Config.twitterScreenId = twitterScreenId;
    }

    public static void setUploadedPercentage(final int uploadedPercentage) {

        Config.uploadedPercentage = uploadedPercentage;
    }

    public static void setUserID(final String userId) {

        Config.userId = userId;
    }

    public static void setVideoCurrentPosition(final int videoCurrentPosition) {

        Config.videoCurrentPosition = videoCurrentPosition;
    }

    private Config() {

    }
}
