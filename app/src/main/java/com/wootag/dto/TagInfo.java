/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.TagFu.dto;

import java.io.Serializable;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

public class TagInfo implements Serializable {

    private static final Logger LOG = LoggerManager.getLogger();

    private static final long serialVersionUID = -6194698270958620208L;
    private String name;
    private String link;
    private String displayTime;
    private String color;
    private String fbId;
    private String twId;
    private String TagFuId;
    private String gPlusId;
    private long tagId;
    private int tagTimeOutFrame;
    private int videoPlaybackTime;
    private String clientVideoId;
    private float tagX, tagY;
    private int screenWidth, screenHeight;
    private int videoWidth, videoHeight;
    private float videoResX, videoResY;
    private float screenResX, screenResY;
    private String serverVideoId;
    private int servertagId;
    private int uploadStatus;
    private int viewId;
    private String productName;
    private String productLink;
    private String productPrice;
    private String productDescription;
    private String productSold;
    private String productCurrency;
    private String productCategory;
    private boolean visible;

    public String getClientVideoId() {

        return this.clientVideoId;
    }

    public String getColor() {

        return this.color;
    }

    public String getDisplayTime() {

        return this.displayTime;
    }

    public String getFbId() {

        return this.fbId;
    }

    public String getgPlusId() {

        return this.gPlusId;
    }

    public String getLink() {

        return this.link;
    }

    public String getName() {

        return this.name;
    }

    public String getProductCategory() {

        return this.productCategory;
    }

    public String getProductCurrency() {

        return this.productCurrency;
    }

    public String getProductDescription() {

        return this.productDescription;
    }

    public String getProductLink() {

        return this.productLink;
    }

    public String getProductName() {

        return this.productName;
    }

    public String getProductPrice() {

        return this.productPrice;
    }

    public String getProductSold() {

        return this.productSold;
    }

    public int getScreenHeight() {

        return this.screenHeight;
    }

    public float getScreenResX() {

        return this.screenResX;
    }

    public float getScreenResY() {

        return this.screenResY;
    }

    public int getScreenWidth() {

        return this.screenWidth;
    }

    public int getServertagId() {

        return this.servertagId;
    }

    public String getServerVideoId() {

        return this.serverVideoId;
    }

    public long getTagId() {

        return this.tagId;
    }

    public int getTagTimeOutFrame() {

        return this.tagTimeOutFrame;
    }

    public float getTagX() {

        return this.tagX;
    }

    public float getTagY() {

        return this.tagY;
    }

    public String getTwId() {

        return this.twId;
    }

    public int getUploadStatus() {

        return this.uploadStatus;
    }

    public int getVideoHeight() {

        return this.videoHeight;
    }

    public int getVideoPlaybackTime() {

        return this.videoPlaybackTime;
    }

    public float getVideoResX() {

        return this.videoResX;
    }

    public float getVideoResY() {

        return this.videoResY;
    }

    public int getVideoWidth() {

        return this.videoWidth;
    }

    public int getViewId() {

        return this.viewId;
    }

    public String getTagFuId() {

        return this.TagFuId;
    }

    public boolean isVisible() {

        return this.visible;
    }

    public void setClientVideoId(final String clientVideoId) {

        this.clientVideoId = clientVideoId;
    }

    public void setColor(final String color) {

        this.color = color;
    }

    public void setDisplayTime(final String displayTime) {

        this.displayTime = displayTime;
    }

    public void setFbId(final String fbId) {

        this.fbId = fbId;
    }

    public void setgPlusId(final String gPlusId) {

        this.gPlusId = gPlusId;
    }

    public void setLink(final String link) {

        this.link = link;
    }

    public void setName(final String name) {

        this.name = name;
    }

    public void setProductCategory(final String productCategory) {

        this.productCategory = productCategory;
    }

    public void setProductCurrency(final String productCurrency) {

        this.productCurrency = productCurrency;
    }

    public void setProductDescription(final String productDescription) {

        this.productDescription = productDescription;
    }

    public void setProductLink(final String productLink) {

        this.productLink = productLink;
    }

    public void setProductName(final String productName) {

        this.productName = productName;
    }

    public void setProductPrice(final String productPrice) {

        this.productPrice = productPrice;
    }

    public void setProductSold(final String productSold) {

        this.productSold = productSold;
    }

    public void setScreenHeight(final int screenHeight) {

        this.screenHeight = screenHeight;
    }

    public void setScreenResX(final float screenResX) {

        this.screenResX = screenResX;
    }

    public void setScreenResY(final float screenResY) {

        this.screenResY = screenResY;
    }

    public void setScreenWidth(final int screenWidth) {

        this.screenWidth = screenWidth;
    }

    public void setServertagId(final int servertagId) {

        this.servertagId = servertagId;
    }

    public void setServerVideoId(final String serverVideoId) {

        this.serverVideoId = serverVideoId;
    }

    public void setTagId(final long tagId) {

        this.tagId = tagId;
    }

    public void setTagTimeOutFrame(final int tagTimeOutFrame) {

        this.tagTimeOutFrame = tagTimeOutFrame;
    }

    public void setTagX(final float tagX) {

        this.tagX = tagX;
    }

    public void setTagY(final float tagY) {

        this.tagY = tagY;
    }

    public void setTwId(final String twId) {

        this.twId = twId;
    }

    public void setUploadStatus(final int uploadStatus) {

        this.uploadStatus = uploadStatus;
    }

    public void setVideoHeight(final int videoHeight) {

        this.videoHeight = videoHeight;
    }

    public void setVideoPlaybackTime(final int videoPlaybackTime) {

        this.videoPlaybackTime = videoPlaybackTime;
    }

    public void setVideoResX(final float videoResX) {

        this.videoResX = videoResX;
    }

    public void setVideoResY(final float videoResY) {

        this.videoResY = videoResY;
    }

    public void setVideoWidth(final int videoWidth) {

        this.videoWidth = videoWidth;
    }

    public void setViewId(final int viewId) {

        this.viewId = viewId;
    }

    public void setVisible(final boolean visible) {

        this.visible = visible;
    }

    public void setTagFuId(final String TagFuId) {

        this.TagFuId = TagFuId;
    }

}
