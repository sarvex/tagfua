/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.TagFu.dto;

import java.io.File;
import java.io.Serializable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.TagFu.Constant;
import com.TagFu.util.Util;

public class FormFile implements Serializable {

    private static final Logger LOG = LoggerManager.getLogger();

    private static final long serialVersionUID = 1L;

    private Boolean downloadRequested;
    private Boolean uploadRequested;
    private Integer transferPercentage;
    private Long fieldSpecId;
    private Long fileSize;
    private Long localFormId;
    private Long localId;
    private Long mediaId;
    private Long uploadPriority;
    private String localMediaPath;
    private String mimeType;

    public static long getSerialversionuid() {

        return serialVersionUID;
    }

    /**
     * @param context This is required for reading SD card status.
     * @return
     */
    public String getActionString(final Context context) {

        if (!Util.isSDCardValid(context, false)) {
            return "SD card not mouted";
        }

        boolean mediaExists = false;

        if (this.localMediaPath != null) {
            final File file = new File(this.localMediaPath);
            mediaExists = file.exists();
        }

        if (mediaExists) {
            String str = "";
            if (this.mediaId == null) {
                if (this.transferPercentage != null) {
                    if (this.transferPercentage == 0) {
                        str = "Upload started. ";
                    } else {
                        str = this.transferPercentage + "% uploaded. ";
                    }
                } else {
                    str = "Waiting in upload queue. ";
                }
            }
            if (str.length() > 0) {
                return str;
            }
            return "Tap to view";

        }
        if (this.mediaId != null) {
            if ((this.downloadRequested != null) && this.downloadRequested) {
                if (this.transferPercentage != null) {
                    if (this.transferPercentage > 0) {
                        return this.transferPercentage + "% downloaded. Tap to cancel download.";
                    }
                    return "Waiting in download queue. Tap to cancel download";
                }
                return "Waiting in download queue. Tap to cancel download";
            }
            return "Tap to download";
        }

        return "Unknown action";
    }

    /**
     * @param values fills this object, instead of creating a new one
     * @return
     */
    public ContentValues getContentValues(ContentValues values) {

        if (values == null) {
            values = new ContentValues();
        }

        Util.putNullOrValue(values, Constant.FIELD_SPEC_ID, this.fieldSpecId);
        Util.putNullOrValue(values, Constant.LOCAL_FORM_ID, this.localFormId);
        Util.putNullOrValue(values, Constant.MIME_TYPE, this.mimeType);
        Util.putNullOrValue(values, Constant.MEDIA_ID, this.mediaId);
        Util.putNullOrValue(values, Constant.LOCAL_MEDIA_PATH, this.localMediaPath);
        Util.putNullOrValue(values, Constant.DOWNLOAD_REQUESTED, this.downloadRequested);
        Util.putNullOrValue(values, Constant.UPLOAD_REQUESTED, this.uploadRequested);
        Util.putNullOrValue(values, Constant.UPLOAD_PRIORITY, this.uploadPriority);
        Util.putNullOrValue(values, Constant.TRANSFER_PERCENTAGE, this.transferPercentage);
        Util.putNullOrValue(values, Constant.FILE_SIZE, this.fileSize);

        return values;
    }

    public Boolean getDownloadRequested() {

        return this.downloadRequested;
    }

    public Long getFieldSpecId() {

        return this.fieldSpecId;
    }

    public Long getFileSize() {

        return this.fileSize;
    }

    public Long getLocalFormId() {

        return this.localFormId;
    }

    public Long getLocalId() {

        return this.localId;
    }

    public String getLocalMediaPath() {

        return this.localMediaPath;
    }

    public Long getMediaId() {

        return this.mediaId;
    }

    public String getMimeType() {

        return this.mimeType;
    }

    public Integer getTransferPercentage() {

        return this.transferPercentage;
    }

    public Long getUploadPriority() {

        return this.uploadPriority;
    }

    public Boolean getUploadRequested() {

        return this.uploadRequested;
    }

    public void load(final Cursor cursor, final Context applicationContext) {

        this.localId = cursor.isNull(Constant._ID_INDEX) ? null : Long.valueOf(cursor.getLong(Constant._ID_INDEX));
        this.fieldSpecId = cursor.isNull(Constant.FIELD_SPEC_ID_INDEX) ? null : Long.valueOf(cursor
                .getLong(Constant.FIELD_SPEC_ID_INDEX));
        this.localFormId = cursor.isNull(Constant.LOCAL_FORM_ID_INDEX) ? null : Long.valueOf(cursor
                .getLong(Constant.LOCAL_FORM_ID_INDEX));
        this.mimeType = cursor.isNull(Constant.MIME_TYPE_INDEX) ? null : cursor.getString(Constant.MIME_TYPE_INDEX);
        this.mediaId = cursor.isNull(Constant.MEDIA_ID_INDEX) ? null : Long.valueOf(cursor
                .getLong(Constant.MEDIA_ID_INDEX));
        this.localMediaPath = cursor.isNull(Constant.LOCAL_MEDIA_PATH_INDEX) ? null : cursor
                .getString(Constant.LOCAL_MEDIA_PATH_INDEX);
        this.downloadRequested = cursor.isNull(Constant.DOWNLOAD_REQUESTED_INDEX) ? null : Boolean.valueOf(cursor
                .getString(Constant.DOWNLOAD_REQUESTED_INDEX));
        this.uploadRequested = cursor.isNull(Constant.UPLOAD_REQUESTED_INDEX) ? null : Boolean.valueOf(cursor
                .getString(Constant.UPLOAD_REQUESTED_INDEX));
        this.uploadPriority = cursor.isNull(Constant.UPLOAD_PRIORITY_INDEX) ? null : Long.valueOf(cursor
                .getLong(Constant.UPLOAD_PRIORITY_INDEX));
        this.transferPercentage = cursor.isNull(Constant.TRANSFER_PERCENTAGE_INDEX) ? null : Integer.valueOf(cursor
                .getInt(Constant.TRANSFER_PERCENTAGE_INDEX));
        this.fileSize = cursor.isNull(Constant.FILE_SIZE_INDEX) ? null : Long.valueOf(cursor
                .getLong(Constant.FILE_SIZE_INDEX));

    }

    public void setDownloadRequested(final Boolean downloadRequested) {

        this.downloadRequested = downloadRequested;
    }

    public void setFieldSpecId(final Long fieldSpecId) {

        this.fieldSpecId = fieldSpecId;
    }

    public void setFileSize(final Long fileSize) {

        this.fileSize = fileSize;
    }

    public void setLocalFormId(final Long localFormId) {

        this.localFormId = localFormId;
    }

    public void setLocalId(final Long localId) {

        this.localId = localId;
    }

    public void setLocalMediaPath(final String localMediaPath) {

        this.localMediaPath = localMediaPath;
    }

    public void setMediaId(final Long mediaId) {

        this.mediaId = mediaId;
    }

    public void setMimeType(final String mimeType) {

        this.mimeType = mimeType;
    }

    public void setTransferPercentage(final Integer transferPercentage) {

        this.transferPercentage = transferPercentage;
    }

    public void setUploadPriority(final Long uploadPriority) {

        this.uploadPriority = uploadPriority;
    }

    public void setUploadRequested(final Boolean uploadRequested) {

        this.uploadRequested = uploadRequested;
    }

    @Override
    public String toString() {

        return "FormFile [localId=" + this.localId + ", fieldSpecId=" + this.fieldSpecId + ", localFormId="
                + this.localFormId + ", mimeType=" + this.mimeType + ", mediaId=" + this.mediaId + ", localMediaPath="
                + this.localMediaPath + ", downloadRequested=" + this.downloadRequested + ", uploadRequested="
                + this.uploadRequested + ", uploadPriority=" + this.uploadPriority + ", transferPercentage="
                + this.transferPercentage + ", fileSize=" + this.fileSize + "]";
    }

}
