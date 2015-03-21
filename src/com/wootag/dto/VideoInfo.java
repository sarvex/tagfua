/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.dto;

import java.io.Serializable;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

public class VideoInfo implements Serializable {

    private static final Logger LOG = LoggerManager.getLogger();

    private static final long serialVersionUID = -7751511682429279102L;

    private Integer mediaType;
    private Integer transferPercentage;
    private Long fileSize;
    private String description;
    private String fileExtension;
    private String fileName;
    private String localMediaPath, serVerPath;
    private String mimeType;
    private String serverVideoId;
    private String shareFb;
    private String shareGplus;
    private String shareTwitter;
    private String title;
    private String uploadDate;
    private String uploadPercentage;
    private String uploadedDevice;
    private String videoClientId;
    private String videoVocerPage;
    private int mediaId;
    private int partNumber;
    private int publicVideo;
    private int retry;
    private int uploadStatus;
    private int userid;

    public String getDescription() {

        return this.description;
    }

    public String getFileExtension() {

        return this.fileExtension;
    }

    public String getFileName() {

        return this.fileName;
    }

    public Long getFileSize() {

        return this.fileSize;
    }

    public String getLocalMediaPath() {

        return this.localMediaPath;
    }

    public int getMediaId() {

        return this.mediaId;
    }

    public Integer getMediaType() {

        return this.mediaType;
    }

    public String getMimeType() {

        return this.mimeType;
    }

    public int getPartNumber() {

        return this.partNumber;
    }

    public int getPublicVideo() {

        return this.publicVideo;
    }

    public int getRetry() {

        return this.retry;
    }

    public String getSerVerPath() {

        return this.serVerPath;
    }

    public String getServerVideoId() {

        return this.serverVideoId;
    }

    public String getShareFb() {

        return this.shareFb;
    }

    public String getShareGplus() {

        return this.shareGplus;
    }

    public String getShareTwitter() {

        return this.shareTwitter;
    }

    public String getTitle() {

        return this.title;
    }

    public Integer getTransferPercentage() {

        return this.transferPercentage;
    }

    public String getUploadDate() {

        return this.uploadDate;
    }

    public String getUploadedDevice() {

        return this.uploadedDevice;
    }

    public String getUploadPercentage() {

        return this.uploadPercentage;
    }

    public int getUploadStatus() {

        return this.uploadStatus;
    }

    public int getUserid() {

        return this.userid;
    }

    public String getVideoClientId() {

        return this.videoClientId;
    }

    public String getVideoVocerPage() {

        return this.videoVocerPage;
    }

    public void setDescription(final String description) {

        this.description = description;
    }

    public void setFileExtension(final String fileExtension) {

        this.fileExtension = fileExtension;
    }

    public void setFileName(final String fileName) {

        this.fileName = fileName;
    }

    public void setFileSize(final Long fileSize) {

        this.fileSize = fileSize;
    }

    public void setLocalMediaPath(final String localMediaPath) {

        this.localMediaPath = localMediaPath;
    }

    public void setMediaId(final int i) {

        this.mediaId = i;
    }

    public void setMediaType(final Integer mediaType) {

        this.mediaType = mediaType;
    }

    public void setMimeType(final String mimeType) {

        this.mimeType = mimeType;
    }

    public void setPartNumber(final int partNumber) {

        this.partNumber = partNumber;
    }

    public void setPublicVideo(final int publicVideo) {

        this.publicVideo = publicVideo;
    }

    public void setRetry(final int retry) {

        this.retry = retry;
    }

    public void setSerVerPath(final String serVerPath) {

        this.serVerPath = serVerPath;
    }

    public void setServerVideoId(final String serverVideoId) {

        this.serverVideoId = serverVideoId;
    }

    public void setShareFb(final String shareFb) {

        this.shareFb = shareFb;
    }

    public void setShareGplus(final String shareGplus) {

        this.shareGplus = shareGplus;
    }

    public void setShareTwitter(final String shareTwitter) {

        this.shareTwitter = shareTwitter;
    }

    public void setTitle(final String title) {

        this.title = title;
    }

    public void setTransferPercentage(final Integer transferPercentage) {

        this.transferPercentage = transferPercentage;
    }

    public void setUploadDate(final String uploadDate) {

        this.uploadDate = uploadDate;
    }

    public void setUploadedDevice(final String uploadedDevice) {

        this.uploadedDevice = uploadedDevice;
    }

    public void setUploadPercentage(final String uploadPercentage) {

        this.uploadPercentage = uploadPercentage;
    }

    public void setUploadStatus(final int uploadStatus) {

        this.uploadStatus = uploadStatus;
    }

    public void setUserid(final int userid) {

        this.userid = userid;
    }

    public void setVideoClientId(final String videoClientId) {

        this.videoClientId = videoClientId;
    }

    public void setVideoVocerPage(final String videoVocerPage) {

        this.videoVocerPage = videoVocerPage;
    }

}
