/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.TagFu.connectivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.TagFu.R;
import com.TagFu.dto.TagInfo;
import com.TagFu.dto.VideoInfo;

public class VideoDataBase {

    private static final Logger LOG = LoggerManager.getLogger();
    private static final String ADD_COLUMN = " ADD COLUMN ";
    private static final String ALTER_TABLE = "ALTER TABLE ";
    private static final String AND = " AND ";
    private static final String BLANK = "";
    private static final String CLIENT_VIDEOID = "clientvideoid";
    private static final String COUNT_QUERY = "SELECT COUNT(*) FROM UploadQueueTable";
    private static final String COVER_PAGE = "coverpage";
    private static final String CURRENCY = "currency";
    private static final String CURRENT_PAGE = "currentpart";
    private static final String DATABASE_NAME = "UploadQueueDatabase";
    private static final String EQUAL = "=";
    private static final String EQUALS = " = ";
    private static final String EQUALS_QUESTION = "=?";
    private static final String EQUALS_QUESTION_ = " = ?";
    private static final String FILE_EXTENSION = "fileextension";
    private static final String FROM_UPLOAD_QUEUE_TABLE_WHERE_VIDEO_ID = "  FROM UploadQueueTable   WHERE  videoId  = ";
    private static final String INTEGER = " INTEGER, ";
    private static final String PARTS_UPLOADED = "partsuploaddone";
    private static final String PRODUCT_CATEGORY = "productcategory";
    private static final String PRODUCT_DESCRIPTION = "productdescription";
    private static final String PRODUCT_LINK = "productlink";
    private static final String PRODUCT_NAME = "productname";
    private static final String PRODUCT_PRICE = "productprice";
    private static final String PUBLIC_VIDEO = "publicvideo";
    private static final String QUEUE_TABLE = "UploadQueueTable";
    private static final String REAL = " REAL, ";
    private static final String RETRY = "retry";
    private static final String ROW_ID = "rowId";
    private static final String SCREEN_RES_X = "screenresx";
    private static final String SCREEN_RES_Y = "screenresy";
    private static final String SELECT = "SELECT ";
    private static final String SELECT_FROM = "SELECT * FROM ";
    private static final String SELECT_QUERY = "SELECT * FROM UploadQueueTable";
    private static final String SEQUENCE_COLUMN = "seq";
    private static final String SERVER_ID = "serverid";
    private static final String SERVER_TAG_ID = "servertagid";
    private static final String SERVER_URL = "serverUrl";
    private static final String SERVER_VIDEOID = "servervideoid";
    private static final String SHARE_FB = "sharefb";
    private static final String SHARE_GOOGLE_PLUS = "sharegoogleplus";
    private static final String SHARE_TWITTER = "sharetwitter";
    private static final String SOLD = "sold";
    private static final String SQLITE_SEQUENCE_TABLE = "sqlite_sequence";
    private static final String TABLE_NAME_COLUMN = "name";
    private static final String TAG_COLOR = "tag_color";
    private static final String TAG_DISPLAYTIME = "tag_displaytime";
    private static final String TAG_FACEBOOKID = "tag_facebookid";
    private static final String TAG_GPLUSID = "tag_gplusid";
    private static final String TAG_LINK = "tag_link";
    private static final String TAG_NAME = "tag_name";
    private static final String TAG_SCREENHEIGHT = "screenheight";
    private static final String TAG_SCREENWIDTH = "screenwidth";
    private static final String TAG_TABLE = "tag_tabel";
    private static final String TAG_TIMEOUTFRAME = "tag_timeoutframe";
    private static final String TAG_TWITTERID = "tag_twitterid";
    private static final String TAG_TagFuID = "tag_TagFuid";
    private static final String TAG_X = "tagx";
    private static final String TAG_Y = "tagy";
    private static final String TEXT = " TEXT, ";
    private static final String TEXT_DEFAULT_NULL = "  TEXT default null";
    private static final String UPLOAD_DEVICE = "uploaddevice";
    private static final String UPLOAD_STATUS = "uploadstatus";
    private static final String UPLOAD_URL_HIT_COUNT = "uploadurlhitcount";
    private static final String USER_ID = "userid";
    private static final String VIDEOPLAYBACKTIME = "videoplaybacktime";
    private static final String VIDEO_DESCRIPTION = "description";
    private static final String VIDEO_FILE_NAME = "videofilename";
    private static final String VIDEO_HEIGHT = "videoheight";
    private static final String VIDEO_ID = "videoId";
    private static final String VIDEO_MIME_TYPE = "videoMimeType";
    private static final String VIDEO_RES_X = "videoresx";
    private static final String VIDEO_RES_Y = "videoresy";
    private static final String VIDEO_TITLE = "title";
    private static final String VIDEO_UPLOAD_COMPLETED = "videouploadcompleted";
    private static final String VIDEO_UPLOAD_PERCENTAGE = "videoUploadPercentage";
    private static final String VIDEO_UPLOAD_STATUS = "videoUploadStatus";
    private static final String VIDEO_UPLOAD_TIME = "videouploadtime";
    private static final String VIDEO_URL = "videoUrl";
    private static final String VIDEO_WIDTH = "videowidth";
    private static final String WAITING_TO_UPLOAD = "waitingtoupload";
    private static final String WHERE = " WHERE ";
    private static final String TagFu = "/TagFu/";
    private static final String _ID = "_id";
    private static final boolean STORE_DATABASE_ON_SD_CARD = false;

    private static final String CREATE_QUEUE_TABLE = "create table IF NOT EXISTS " + QUEUE_TABLE + " (" + ROW_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT," + VIDEO_ID + " TEXT UNIQUE," + SERVER_ID + TEXT + VIDEO_URL + TEXT
            + SERVER_URL + TEXT + VIDEO_MIME_TYPE + TEXT + VIDEO_TITLE + TEXT + UPLOAD_DEVICE + TEXT + FILE_EXTENSION
            + TEXT + VIDEO_UPLOAD_TIME + TEXT + VIDEO_DESCRIPTION + TEXT + PUBLIC_VIDEO + INTEGER + USER_ID + INTEGER
            + WAITING_TO_UPLOAD + INTEGER + VIDEO_UPLOAD_PERCENTAGE + INTEGER + UPLOAD_URL_HIT_COUNT + INTEGER
            + PARTS_UPLOADED + INTEGER + CURRENT_PAGE + " INTEGER default 0," + RETRY + " INTEGER default 0,"
            + VIDEO_UPLOAD_COMPLETED + INTEGER + SHARE_FB + TEXT + SHARE_GOOGLE_PLUS + TEXT + SHARE_TWITTER + TEXT
            + COVER_PAGE + TEXT + VIDEO_FILE_NAME + TEXT + VIDEO_UPLOAD_STATUS + " INTEGER" + ")";

    private static VideoDataBase database;
    private final DBHelper dbhelper;

    public VideoDataBase(final Context context) {

        this.dbhelper = new DBHelper(context);
    }

    public static VideoDataBase getInstance(final Context con) {

        if (database == null) {
            database = new VideoDataBase(con);
        }
        return database;
    }

    /**
     * Inserting video details into database.
     *
     * @param VideoInfo dro
     * @param uplaod stauts while inserting it should be zero
     */
    public void addContenttoUploadQueuetable(final VideoInfo videoinfo, final int uploadStatus) {

        final SQLiteDatabase database = this.dbhelper.getWritableDatabase();
        final VideoInfo video = videoinfo;
        final ContentValues contentValues = new ContentValues();
        LOG.i("insert id " + " uploadStatus " + uploadStatus);

        contentValues.put(VIDEO_ID, video.getVideoClientId());
        contentValues.put(VIDEO_URL, video.getLocalMediaPath());
        contentValues.put(VIDEO_MIME_TYPE, video.getMimeType());
        contentValues.put(VIDEO_DESCRIPTION, video.getDescription());
        contentValues.put(VIDEO_UPLOAD_STATUS, Integer.valueOf(uploadStatus));
        contentValues.put(WAITING_TO_UPLOAD, Integer.valueOf(uploadStatus));
        contentValues.put(FILE_EXTENSION, video.getFileExtension());
        contentValues.put(UPLOAD_DEVICE, video.getUploadedDevice());
        contentValues.put(PUBLIC_VIDEO, Integer.valueOf(video.getPublicVideo()));
        contentValues.put(VIDEO_FILE_NAME, video.getFileName());
        contentValues.put(VIDEO_TITLE, video.getTitle());
        contentValues.put(COVER_PAGE, video.getVideoVocerPage());
        contentValues.put(VIDEO_UPLOAD_TIME, video.getUploadDate());
        contentValues.put(USER_ID, Integer.valueOf(video.getUserid()));
        contentValues.put(SERVER_ID, Integer.valueOf(0));
        contentValues.put(SHARE_FB, video.getShareFb());
        contentValues.put(SHARE_GOOGLE_PLUS, video.getShareGplus());
        contentValues.put(SHARE_TWITTER, video.getShareTwitter());
        database.insert(QUEUE_TABLE, null, contentValues);

        LOG.i("DATABASEINSERT data associated with " + video.getVideoClientId() + " is inserted");
    }

    public boolean databaseExist() {

        final File dbFile = new File(DATABASE_NAME);
        return dbFile.exists();
    }

    public void deleteLocalTag(final Context context, final String tagId, final String videoId, final boolean uploaded) {

        final SQLiteDatabase database = this.dbhelper.getWritableDatabase();
        int number = 0;
        if (uploaded) {
            number = database.delete(TAG_TABLE, _ID + EQUALS_QUESTION_ + AND + SERVER_VIDEOID + EQUALS_QUESTION_,
                    new String[] { tagId, videoId });
        } else {
            number = database.delete(TAG_TABLE, _ID + EQUALS_QUESTION_ + AND + CLIENT_VIDEOID + EQUALS_QUESTION_,
                    new String[] { tagId, videoId });
        }
        LOG.i("deletetag delete id" + number);
    }

    /**
     * Delete tag from database.
     *
     * @param context
     * @param tagId
     * @param videoId
     * @param isuploadedVideo
     */

    public void deleteTagById(final Context context, final String tagId, final String videoId, final boolean uploaded) {

        final SQLiteDatabase database = this.dbhelper.getWritableDatabase();
        int number = 0;
        if (uploaded) {
            number = database.delete(TAG_TABLE, SERVER_TAG_ID + EQUALS_QUESTION_ + AND + SERVER_VIDEOID
                    + EQUALS_QUESTION_, new String[] { tagId, videoId });
        } else {
            number = database.delete(TAG_TABLE, _ID + EQUALS_QUESTION_ + AND + CLIENT_VIDEOID + EQUALS_QUESTION_,
                    new String[] { tagId, videoId });
        }
        LOG.i("deletetag delete id" + number);
    }

    /**
     * Delete tags of specified video id.
     *
     * @param context
     * @param videoId
     * @param uploaded
     */
    public void deleteTagByVideoId(final Context context, final String videoId, final boolean uploaded) {

        final SQLiteDatabase database = this.dbhelper.getWritableDatabase();
        if (uploaded) {
            final int number = database.delete(TAG_TABLE, SERVER_VIDEOID + EQUALS_QUESTION_, new String[] { videoId });

        }
    }

    /**
     * Returns all videos list which are not uploaded yet.
     */
    public synchronized List<VideoInfo> getAllNonUploadList() {

        List<VideoInfo> videoInfos = null;
        final Cursor cursor = this.dbhelper.getWritableDatabase()
                .query(QUEUE_TABLE, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            videoInfos = new ArrayList<VideoInfo>();
            final VideoInfo videoInfo = new VideoInfo();
            videoInfo.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(VIDEO_DESCRIPTION)));
            videoInfo.setFileExtension(cursor.getString(cursor.getColumnIndexOrThrow(FILE_EXTENSION)));
            videoInfo.setFileName(cursor.getString(cursor.getColumnIndexOrThrow(VIDEO_FILE_NAME)));
            videoInfo.setLocalMediaPath(cursor.getString(cursor.getColumnIndexOrThrow(VIDEO_URL)));
            videoInfo.setVideoClientId(cursor.getString(cursor.getColumnIndexOrThrow(VIDEO_ID)));
            videoInfo.setMimeType(cursor.getString(cursor.getColumnIndexOrThrow(VIDEO_MIME_TYPE)));
            videoInfo.setUploadStatus(cursor.getInt(cursor.getColumnIndexOrThrow(VIDEO_UPLOAD_STATUS)));
            videoInfo.setUploadedDevice(cursor.getString(cursor.getColumnIndexOrThrow(UPLOAD_DEVICE)));
            videoInfo.setPublicVideo(cursor.getInt(cursor.getColumnIndexOrThrow(PUBLIC_VIDEO)));
            videoInfo.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(VIDEO_TITLE)));
            videoInfo.setUploadDate(cursor.getString(cursor.getColumnIndexOrThrow(VIDEO_UPLOAD_TIME)));
            videoInfo.setUserid(cursor.getInt(cursor.getColumnIndexOrThrow(USER_ID)));
            videoInfo.setServerVideoId(cursor.getString(cursor.getColumnIndexOrThrow(SERVER_ID)));

            videoInfo.setRetry(cursor.getInt(cursor.getColumnIndexOrThrow(RETRY)));
            videoInfo.setPartNumber(cursor.getInt(cursor.getColumnIndexOrThrow(CURRENT_PAGE)));

            videoInfo.setUploadPercentage(String.valueOf(cursor.getInt(cursor
                    .getColumnIndexOrThrow(VIDEO_UPLOAD_PERCENTAGE))));

            videoInfos.add(videoInfo);
        }

        cursor.close();

        return videoInfos;
    }

    /**
     * Return the list of tag objects.
     *
     * @param videoId
     * @param context
     * @param uploaded
     */
    public List<TagInfo> getAllTagsByVideoId(final String videoId, final Context context, final boolean uploaded) {

        final List<TagInfo> tagsList = new ArrayList<TagInfo>();

        String serverId = CLIENT_VIDEOID;
        if (uploaded) {
            serverId = SERVER_VIDEOID;
        }

        final Cursor cursor = this.dbhelper.getWritableDatabase().rawQuery(
                SELECT_FROM + TAG_TABLE + WHERE + serverId + EQUAL + videoId, null);

        final int tagName_index = cursor.getColumnIndex(TAG_NAME);
        final int id_index = cursor.getColumnIndex(_ID);
        final int serverTagId_index = cursor.getColumnIndex(SERVER_TAG_ID);
        final int link_index = cursor.getColumnIndex(TAG_LINK);
        final int displayTime_index = cursor.getColumnIndex(TAG_DISPLAYTIME);
        final int tagColor_index = cursor.getColumnIndex(TAG_COLOR);
        final int fbId_index = cursor.getColumnIndex(TAG_FACEBOOKID);
        final int twId_index = cursor.getColumnIndex(TAG_TWITTERID);
        final int TagFuId_index = cursor.getColumnIndex(TAG_TagFuID);
        final int gplusId_index = cursor.getColumnIndex(TAG_GPLUSID);
        final int tagTimeoutFrame_index = cursor.getColumnIndex(TAG_TIMEOUTFRAME);
        final int videoPlaybackTime_index = cursor.getColumnIndex(VIDEOPLAYBACKTIME);
        final int screenWidth_index = cursor.getColumnIndex(TAG_SCREENWIDTH);
        final int screenHeight_index = cursor.getColumnIndex(TAG_SCREENHEIGHT);
        final int tagX_index = cursor.getColumnIndex(TAG_X);
        final int tagY_index = cursor.getColumnIndex(TAG_Y);
        final int clientVideoId_index = cursor.getColumnIndex(CLIENT_VIDEOID);
        final int serverVideoId_index = cursor.getColumnIndex(SERVER_VIDEOID);
        final int videoHeight_index = cursor.getColumnIndex(VIDEO_HEIGHT);
        final int videowidth_index = cursor.getColumnIndex(VIDEO_WIDTH);
        final int videoResX_index = cursor.getColumnIndex(VIDEO_RES_X);
        final int videoResY_index = cursor.getColumnIndex(VIDEO_RES_Y);
        final int screenResX_index = cursor.getColumnIndex(SCREEN_RES_X);
        final int screenResY_index = cursor.getColumnIndex(SCREEN_RES_Y);
        final int uploadStatus_index = cursor.getColumnIndex(UPLOAD_STATUS);
        final int productName_index = cursor.getColumnIndex(PRODUCT_NAME);
        final int productDesc_index = cursor.getColumnIndex(PRODUCT_DESCRIPTION);
        final int productPrice_index = cursor.getColumnIndex(PRODUCT_PRICE);
        final int productLink_index = cursor.getColumnIndex(PRODUCT_LINK);
        final int productSold_index = cursor.getColumnIndex(SOLD);
        final int productCurrency_index = cursor.getColumnIndex(CURRENCY);
        final int productCategory_index = cursor.getColumnIndex(PRODUCT_CATEGORY);

        while (cursor.moveToNext()) {
            final TagInfo tag = new TagInfo();
            tag.setName(cursor.getString(tagName_index));
            tag.setTagId(cursor.getLong(id_index));
            tag.setServertagId(cursor.getInt(serverTagId_index));
            tag.setColor(cursor.getString(tagColor_index));
            tag.setLink(cursor.getString(link_index));
            tag.setDisplayTime(cursor.getString(displayTime_index));
            tag.setFbId(cursor.getString(fbId_index));
            tag.setgPlusId(cursor.getString(gplusId_index));
            tag.setTagFuId(cursor.getString(TagFuId_index));
            tag.setTwId(cursor.getString(twId_index));
            tag.setTagX(cursor.getFloat(tagX_index));
            tag.setTagY(cursor.getFloat(tagY_index));
            tag.setTagTimeOutFrame(cursor.getInt(tagTimeoutFrame_index));
            tag.setScreenHeight(cursor.getInt(screenWidth_index));
            tag.setScreenWidth(cursor.getInt(screenHeight_index));
            tag.setVideoPlaybackTime(cursor.getInt(videoPlaybackTime_index));
            tag.setClientVideoId(cursor.getString(clientVideoId_index));
            tag.setServerVideoId(cursor.getString(serverVideoId_index));
            tag.setVideoHeight(cursor.getInt(videoHeight_index));
            tag.setVideoWidth(cursor.getInt(videowidth_index));
            tag.setScreenResX(cursor.getFloat(screenResX_index));
            tag.setScreenResY(cursor.getFloat(screenResY_index));
            tag.setVideoResX(cursor.getFloat(videoResX_index));
            tag.setVideoResY(cursor.getFloat(videoResY_index));
            tag.setUploadStatus(cursor.getInt(uploadStatus_index));
            tag.setProductName(cursor.getString(productName_index));
            tag.setProductDescription(cursor.getString(productDesc_index));
            tag.setProductLink(cursor.getString(productLink_index));
            tag.setProductPrice(cursor.getString(productPrice_index));
            tag.setProductSold(cursor.getString(productSold_index));
            tag.setProductCurrency(cursor.getString(productCurrency_index));
            tag.setProductCategory(cursor.getString(productCategory_index));

            tagsList.add(tag);
        }

        cursor.close();

        return tagsList;
    }

    /**
     * Returns all atg count of given video id.
     *
     * @param videoId
     * @param context
     */
    public int getAllTagsCountByVideoId(final String videoId, final Context context) {

        final Cursor cursor = this.dbhelper.getWritableDatabase().rawQuery(
                SELECT_FROM + TAG_TABLE + WHERE + CLIENT_VIDEOID + EQUALS + videoId, null);
        final int count = cursor.getCount();
        cursor.close();
        return count;
    }

    /**
     * Returns the uplaoding video facebook user id to share that video to facebook user once it is published.
     *
     * @param videoId
     * @param context
     */
    public String getFacebookShareFlag(final String videoId, final Context context) {

        final Cursor cursor = this.dbhelper.getWritableDatabase().rawQuery(
                SELECT + SHARE_FB + FROM_UPLOAD_QUEUE_TABLE_WHERE_VIDEO_ID + "'" + videoId + "'", null);

        String count = "";
        if (cursor.moveToNext()) {
            count = cursor.getString(cursor.getColumnIndex(SHARE_FB));
        }

        cursor.close();
        return count;
    }

    /**
     * Returns the uplaoding video gplus user id to share that video to gplus user once it is published.
     *
     * @param videoId
     * @param context
     */
    public String getGPlusShareFlag(final String videoId, final Context context) {

        final SQLiteDatabase database = this.dbhelper.getWritableDatabase();
        final Cursor cursor = database.rawQuery(SELECT + SHARE_GOOGLE_PLUS + FROM_UPLOAD_QUEUE_TABLE_WHERE_VIDEO_ID
                + "'" + videoId + "'", null);

        String count = "";
        if (cursor.moveToNext()) {
            count = cursor.getString(cursor.getColumnIndex(SHARE_GOOGLE_PLUS));
        }
        cursor.close();
        return count;
    }

    public int getNoofRows() {

        int count = 0;

        final SQLiteDatabase database = this.dbhelper.getWritableDatabase();
        count = (int) DatabaseUtils.longForQuery(database, COUNT_QUERY, null);
        LOG.i("COUNTCHECK count is" + count);

        return count;
    }

    /**
     * Returns the uploading video's current part.
     *
     * @param videoId
     * @param context object
     */
    public int getPartNumber(final String videoId, final Context context) {

        final Cursor cursor = this.dbhelper.getWritableDatabase().rawQuery(
                SELECT + CURRENT_PAGE + FROM_UPLOAD_QUEUE_TABLE_WHERE_VIDEO_ID + "'" + videoId + "'", null);

        int partNo = 0;
        if (cursor.moveToNext()) {
            partNo = cursor.getInt(cursor.getColumnIndex(CURRENT_PAGE));
        }

        cursor.close();
        return partNo;
    }

    /**
     * Returns the total number of uploaded parts count of current upload.
     *
     * @param videoId
     * @param context object
     */
    public int getPartsUpload(final String videoId, final Context context) {

        final Cursor cursor = this.dbhelper.getWritableDatabase().rawQuery(
                "SELECT partsuploaddone  FROM UploadQueueTable   WHERE  videoId  = " + "'" + videoId + "'", null);

        int count = 0;
        if (cursor.moveToNext()) {
            count = cursor.getInt(cursor.getColumnIndex(PARTS_UPLOADED));
        }

        cursor.close();
        return count;
    }

    public int getTagByTagId(final String videoId, final String tagId, final Context context) {

        final Cursor cursor = this.dbhelper.getWritableDatabase().rawQuery(
                SELECT_FROM + TAG_TABLE + WHERE + SERVER_TAG_ID + EQUALS + tagId + AND + SERVER_VIDEOID + EQUALS
                        + videoId, null);

        final int count = cursor.getCount();
        cursor.close();
        return count;
    }

    /**
     * Returns all non published tags .
     *
     * @param videoId
     * @param context
     */
    public List<TagInfo> getTagsToUpload(final String videoId, final Context context) {

        final List<TagInfo> tagsList = new ArrayList<TagInfo>();

        final Cursor cursor = this.dbhelper.getWritableDatabase().rawQuery(
                SELECT_FROM + TAG_TABLE + WHERE + SERVER_VIDEOID + EQUAL + videoId + AND + UPLOAD_STATUS + "=0", null);

        final int tagName_index = cursor.getColumnIndex(TAG_NAME);
        final int id_index = cursor.getColumnIndex(_ID);
        final int serverTagId_index = cursor.getColumnIndex(SERVER_TAG_ID);
        final int link_index = cursor.getColumnIndex(TAG_LINK);
        final int displayTime_index = cursor.getColumnIndex(TAG_DISPLAYTIME);
        final int tagColor_index = cursor.getColumnIndex(TAG_COLOR);
        final int fbId_index = cursor.getColumnIndex(TAG_FACEBOOKID);
        final int twId_index = cursor.getColumnIndex(TAG_TWITTERID);
        final int TagFuId_index = cursor.getColumnIndex(TAG_TagFuID);
        final int gplusId_index = cursor.getColumnIndex(TAG_GPLUSID);
        final int tagTimeoutFrame_index = cursor.getColumnIndex(TAG_TIMEOUTFRAME);
        final int videoPlaybackTime_index = cursor.getColumnIndex(VIDEOPLAYBACKTIME);
        final int screenWidth_index = cursor.getColumnIndex(TAG_SCREENWIDTH);
        final int screenHeight_index = cursor.getColumnIndex(TAG_SCREENHEIGHT);
        final int tagX_index = cursor.getColumnIndex(TAG_X);
        final int tagY_index = cursor.getColumnIndex(TAG_Y);
        final int clientVideoId_index = cursor.getColumnIndex(CLIENT_VIDEOID);
        final int serverVideoId_index = cursor.getColumnIndex(SERVER_VIDEOID);
        final int videoHeight_index = cursor.getColumnIndex(VIDEO_HEIGHT);
        final int videowidth_index = cursor.getColumnIndex(VIDEO_WIDTH);
        final int videoResX_index = cursor.getColumnIndex(VIDEO_RES_X);
        final int videoResY_index = cursor.getColumnIndex(VIDEO_RES_Y);
        final int screenResX_index = cursor.getColumnIndex(SCREEN_RES_X);
        final int screenResY_index = cursor.getColumnIndex(SCREEN_RES_Y);
        final int uploadStatus_index = cursor.getColumnIndex(UPLOAD_STATUS);
        final int productName_index = cursor.getColumnIndex(PRODUCT_NAME);
        final int productDesc_index = cursor.getColumnIndex(PRODUCT_DESCRIPTION);
        final int productPrice_index = cursor.getColumnIndex(PRODUCT_PRICE);
        final int productLink_index = cursor.getColumnIndex(PRODUCT_LINK);
        final int productSold_index = cursor.getColumnIndex(SOLD);
        final int productCurrency_index = cursor.getColumnIndex(CURRENCY);
        final int productCategory_index = cursor.getColumnIndex(PRODUCT_CATEGORY);

        while (cursor.moveToNext()) {
            final TagInfo tag = new TagInfo();
            tag.setName(cursor.getString(tagName_index));
            tag.setTagId(cursor.getLong(id_index));
            tag.setServertagId(cursor.getInt(serverTagId_index));
            tag.setColor(cursor.getString(tagColor_index));
            tag.setLink(cursor.getString(link_index));
            tag.setDisplayTime(cursor.getString(displayTime_index));
            tag.setFbId(cursor.getString(fbId_index));
            tag.setgPlusId(cursor.getString(gplusId_index));
            tag.setTagFuId(cursor.getString(TagFuId_index));
            tag.setTwId(cursor.getString(twId_index));
            tag.setTagX(cursor.getFloat(tagX_index));
            tag.setTagY(cursor.getFloat(tagY_index));
            tag.setTagTimeOutFrame(cursor.getInt(tagTimeoutFrame_index));
            tag.setScreenHeight(cursor.getInt(screenWidth_index));
            tag.setScreenWidth(cursor.getInt(screenHeight_index));
            tag.setVideoPlaybackTime(cursor.getInt(videoPlaybackTime_index));
            tag.setClientVideoId(cursor.getString(clientVideoId_index));
            tag.setServerVideoId(cursor.getString(serverVideoId_index));
            tag.setVideoHeight(cursor.getInt(videoHeight_index));
            tag.setVideoWidth(cursor.getInt(videowidth_index));
            tag.setScreenResX(cursor.getFloat(screenResX_index));
            tag.setScreenResY(cursor.getFloat(screenResY_index));
            tag.setVideoResX(cursor.getFloat(videoResX_index));
            tag.setVideoResY(cursor.getFloat(videoResY_index));
            tag.setUploadStatus(cursor.getInt(uploadStatus_index));
            tag.setProductName(cursor.getString(productName_index));
            tag.setProductDescription(cursor.getString(productDesc_index));
            tag.setProductLink(cursor.getString(productLink_index));
            tag.setProductPrice(cursor.getString(productPrice_index));
            tag.setProductSold(cursor.getString(productSold_index));
            tag.setProductCurrency(cursor.getString(productCurrency_index));
            tag.setProductCategory(cursor.getString(productCategory_index));

            tagsList.add(tag);

        }
        cursor.close();

        return tagsList;
    }

    /**
     * Returns the uplaoding video twitter screen id to share that video to twitter user once it is published.
     *
     * @param videoId
     * @param context
     */
    public String getTwitterShareFlag(final String videoId, final Context context) {

        final Cursor cursor = this.dbhelper.getWritableDatabase().rawQuery(
                SELECT + SHARE_TWITTER + FROM_UPLOAD_QUEUE_TABLE_WHERE_VIDEO_ID + "'" + videoId + "'", null);

        String count = "";
        if (cursor.moveToNext()) {
            count = cursor.getString(cursor.getColumnIndex(SHARE_TWITTER));
        }
        cursor.close();
        return count;
    }

    /**
     * Returns the uploading video's hit count.(max 3 after need to reset zero and should be upload video from starting)
     *
     * @param CLIENT_ID
     * @param hitCount
     */
    public int getUploadHitCount(final String videoId, final Context context) {

        final Cursor cursor = this.dbhelper.getWritableDatabase().rawQuery(
                "SELECT uploadurlhitcount  FROM UploadQueueTable   WHERE  videoId  = " + "'" + videoId + "'", null);

        int count = 0;
        if (cursor.moveToNext()) {
            count = cursor.getInt(cursor.getColumnIndex(UPLOAD_URL_HIT_COUNT));
        }
        cursor.close();

        return count;
    }

    public int getVideoByClientVideoId(final String videoId, final Context context) {

        final Cursor cursor = this.dbhelper.getWritableDatabase().rawQuery(
                "SELECT * FROM UploadQueueTable  WHERE " + VIDEO_ID + EQUALS + videoId, null);
        final int count = cursor.getCount();
        cursor.close();
        return count;
    }

    /**
     * Returns the uplaoding video progress.
     *
     * @param videoId
     * @param context
     */
    public synchronized int getVideoUploadPercentage(final String videoId, final Context context) {

        final Cursor cursor = this.dbhelper.getWritableDatabase().rawQuery(
                "SELECT videoUploadPercentage  FROM UploadQueueTable   WHERE  videoId  = " + "'" + videoId + "'", null);

        int percentage = 0;
        if (cursor.moveToNext()) {
            percentage = cursor.getInt(cursor.getColumnIndex(VIDEO_UPLOAD_PERCENTAGE));
            LOG.i("percentage " + percentage);
        }
        cursor.close();
        return percentage;
    }

    /**
     * Returns the pending video state (0 if it is in pending ,1 for uploaded,2 for uploading parts, 3 for waiting for
     * publish)
     *
     * @param videoId
     * @param context
     */
    public int getVideoUploadState(final String videoId, final Context context) {

        final Cursor cursor = this.dbhelper.getWritableDatabase().rawQuery(
                "SELECT videouploadcompleted  FROM UploadQueueTable   WHERE  videoId  = " + "'" + videoId + "'", null);

        int count = 0;
        if (cursor.moveToNext()) {
            count = cursor.getInt(cursor.getColumnIndex(VIDEO_UPLOAD_COMPLETED));
            LOG.i("getVideoUploadState " + count);
        }

        cursor.close();
        return count;
    }

    public void removeContentFromDownloadQueue(final VideoInfo videoinfo) {

        final SQLiteDatabase database = this.dbhelper.getWritableDatabase();
        LOG.i("in remove calll");
        final int deltedId = database.delete(QUEUE_TABLE, VIDEO_ID + EQUALS_QUESTION_,
                new String[] { String.valueOf(videoinfo.getVideoClientId()) });
        LOG.i("delete id " + videoinfo.getMediaId() + " url" + videoinfo.getLocalMediaPath() + " downloadstatus "
                + "deltedId " + deltedId);
    }

    /**
     * delete video table from databse.
     */
    public void removeFromDownloadQueue() {

        final SQLiteDatabase database = this.dbhelper.getWritableDatabase();
        LOG.i("in remove calll");
        database.delete(QUEUE_TABLE, null, null);
    }

    /**
     * Save tag details into local database.
     *
     * @param TagInfo dto
     * @param context
     */
    public TagInfo saveTag(final TagInfo tag, final Context context) {

        final SQLiteDatabase database = this.dbhelper.getWritableDatabase();
        final ContentValues contentValues = new ContentValues();
        contentValues.put(TAG_NAME, tag.getName());
        contentValues.put(TAG_LINK, tag.getLink());
        contentValues.put(TAG_DISPLAYTIME, tag.getDisplayTime());
        contentValues.put(TAG_COLOR, tag.getColor());
        contentValues.put(TAG_FACEBOOKID, tag.getFbId());
        contentValues.put(TAG_TWITTERID, tag.getTwId());
        contentValues.put(TAG_TagFuID, tag.getTagFuId());
        contentValues.put(TAG_GPLUSID, tag.getgPlusId());
        contentValues.put(TAG_TIMEOUTFRAME, tag.getTagTimeOutFrame());
        contentValues.put(VIDEOPLAYBACKTIME, tag.getVideoPlaybackTime());
        contentValues.put(TAG_SCREENWIDTH, tag.getScreenWidth());
        contentValues.put(TAG_SCREENHEIGHT, tag.getScreenHeight());
        contentValues.put(TAG_X, tag.getTagX());
        contentValues.put(TAG_Y, tag.getTagY());
        contentValues.put(CLIENT_VIDEOID, tag.getClientVideoId());
        contentValues.put(SERVER_VIDEOID, tag.getServerVideoId());
        contentValues.put(VIDEO_HEIGHT, tag.getVideoHeight());
        contentValues.put(VIDEO_WIDTH, tag.getVideoWidth());
        contentValues.put(SCREEN_RES_X, tag.getScreenResX());
        contentValues.put(SCREEN_RES_Y, tag.getScreenResY());
        contentValues.put(VIDEO_RES_X, tag.getVideoResX());
        contentValues.put(VIDEO_RES_Y, tag.getVideoResY());
        contentValues.put(UPLOAD_STATUS, tag.getUploadStatus());
        contentValues.put(SERVER_TAG_ID, tag.getServertagId());

        contentValues.put(PRODUCT_NAME, tag.getProductName());
        contentValues.put(PRODUCT_CATEGORY, tag.getProductCategory());
        contentValues.put(PRODUCT_DESCRIPTION, tag.getProductDescription());
        contentValues.put(PRODUCT_LINK, tag.getProductLink());
        contentValues.put(PRODUCT_PRICE, tag.getProductPrice());
        contentValues.put(SOLD, tag.getProductSold());
        contentValues.put(CURRENCY, tag.getProductCurrency());

        final long rowid = database.insert(TAG_TABLE, null, contentValues);
        LOG.i("insert tag inserted server tag id successfully " + tag.getServertagId());
        LOG.i("insert tag inserted record successfully " + rowid);
        tag.setTagId(rowid);
        return tag;
    }

    /**
     * Returns list of videos from datbase.
     */

    public List<VideoInfo> selectallVideoFromTable() {

        List<VideoInfo> videos = null;
        final Cursor cursor = this.dbhelper.getWritableDatabase().rawQuery(SELECT_QUERY, null);

        final int idIndex = cursor.getColumnIndex(VIDEO_ID);
        final int urlIndex = cursor.getColumnIndex(VIDEO_URL);
        final int mimetypeIndex = cursor.getColumnIndex(VIDEO_MIME_TYPE);
        final int fileExtension = cursor.getColumnIndex(FILE_EXTENSION);
        final int uploadStatusIndex = cursor.getColumnIndex(VIDEO_UPLOAD_STATUS);
        final int waitingtouploadIndex = cursor.getColumnIndex(WAITING_TO_UPLOAD);
        final int titleIndex = cursor.getColumnIndex(VIDEO_TITLE);
        final int descIndex = cursor.getColumnIndex(VIDEO_DESCRIPTION);
        final int publicvideoIndex = cursor.getColumnIndex(PUBLIC_VIDEO);
        final int fileNameIndex = cursor.getColumnIndex(VIDEO_FILE_NAME);
        final int uploadDeviceIndex = cursor.getColumnIndex(UPLOAD_DEVICE);
        final int uploadTimeIndex = cursor.getColumnIndex(VIDEO_UPLOAD_TIME);
        final int userIdIndex = cursor.getColumnIndex(USER_ID);
        final int serverIdIndex = cursor.getColumnIndex(SERVER_ID);
        videos = new ArrayList<VideoInfo>();

        while (cursor.moveToNext()) {
            final VideoInfo videoDto = new VideoInfo();
            videoDto.setVideoClientId(cursor.getString(idIndex));
            videoDto.setLocalMediaPath(cursor.getString(urlIndex));
            videoDto.setMimeType(cursor.getString(mimetypeIndex));
            videoDto.setUploadStatus(cursor.getInt(uploadStatusIndex));
            videoDto.setServerVideoId(cursor.getString(serverIdIndex));
            videoDto.setFileExtension(cursor.getString(fileExtension));
            videoDto.setTitle(cursor.getString(titleIndex));
            videoDto.setDescription(cursor.getString(descIndex));
            videoDto.setPublicVideo(cursor.getInt(publicvideoIndex));
            videoDto.setFileName(cursor.getString(fileNameIndex));
            videoDto.setUploadedDevice(cursor.getString(uploadDeviceIndex));
            videoDto.setUploadDate(cursor.getString(uploadTimeIndex));
            videoDto.setUserid(cursor.getInt(userIdIndex));
            videos.add(videoDto);

        }

        cursor.close();

        return videos;
    }

    /**
     * Returns video object from database to uplaod.
     *
     * @param uploadStatus
     */
    public VideoInfo selectFirstRowFromTable(final int uploadStatus) {

        VideoInfo videoDto = null;
        final Cursor cursor = this.dbhelper.getWritableDatabase().rawQuery(
                "SELECT * FROM UploadQueueTable WHERE " + VIDEO_UPLOAD_STATUS + EQUALS + uploadStatus + AND + RETRY
                        + " = 0 ORDER BY rowid LIMIT 1", null);

        final int idIndex = cursor.getColumnIndex(VIDEO_ID);
        final int urlIndex = cursor.getColumnIndex(VIDEO_URL);
        final int mimetypeIndex = cursor.getColumnIndex(VIDEO_MIME_TYPE);
        final int fileExtension = cursor.getColumnIndex(FILE_EXTENSION);
        final int uploadStatusIndex = cursor.getColumnIndex(VIDEO_UPLOAD_STATUS);
        final int waitingtouploadIndex = cursor.getColumnIndex(WAITING_TO_UPLOAD);
        final int titleIndex = cursor.getColumnIndex(VIDEO_TITLE);
        final int descIndex = cursor.getColumnIndex(VIDEO_DESCRIPTION);
        final int publicvideoIndex = cursor.getColumnIndex(PUBLIC_VIDEO);
        final int fileNameIndex = cursor.getColumnIndex(VIDEO_FILE_NAME);
        final int uploadDeviceIndex = cursor.getColumnIndex(UPLOAD_DEVICE);
        final int uploadTimeIndex = cursor.getColumnIndex(VIDEO_UPLOAD_TIME);
        final int userIdIndex = cursor.getColumnIndex(USER_ID);
        final int serverIdIndex = cursor.getColumnIndex(SERVER_ID);

        final int coverPageIndex = cursor.getColumnIndex(COVER_PAGE);

        while (cursor.moveToNext()) {
            videoDto = new VideoInfo();
            videoDto.setVideoClientId(cursor.getString(idIndex));
            videoDto.setLocalMediaPath(cursor.getString(urlIndex));
            videoDto.setMimeType(cursor.getString(mimetypeIndex));
            videoDto.setUploadStatus(cursor.getInt(uploadStatusIndex));
            videoDto.setServerVideoId(cursor.getString(serverIdIndex));
            videoDto.setFileExtension(cursor.getString(fileExtension));
            videoDto.setTitle(cursor.getString(titleIndex));
            videoDto.setDescription(cursor.getString(descIndex));
            videoDto.setPublicVideo(cursor.getInt(publicvideoIndex));
            videoDto.setFileName(cursor.getString(fileNameIndex));
            videoDto.setUploadedDevice(cursor.getString(uploadDeviceIndex));
            videoDto.setUploadDate(cursor.getString(uploadTimeIndex));
            videoDto.setUserid(cursor.getInt(userIdIndex));
            videoDto.setVideoVocerPage(cursor.getString(coverPageIndex));

        }

        cursor.close();

        return videoDto;
    }

    /**
     * Update the uploading video's hit count.(max 3 after need to reset zero and should be upload video from starting)
     *
     * @param clientId
     * @param hitCount
     */
    public int updatehitCount(final String clientId, final int hitCount) {

        int returnValue = 0;
        final SQLiteDatabase database = this.dbhelper.getWritableDatabase();
        final ContentValues values = new ContentValues();
        values.put(UPLOAD_URL_HIT_COUNT, hitCount);
        // updating row
        returnValue = database.update(QUEUE_TABLE, values, VIDEO_ID + EQUALS_QUESTION,
                new String[] { String.valueOf(clientId) });
        return returnValue;
    }

    /**
     * Upadte video progress while uploading parts.
     *
     * @param clientId
     * @param progress percentage
     */
    public int updatePartsUpload(final String clientId, final int showProgress) {

        int returnValue = 0;
        final SQLiteDatabase database = this.dbhelper.getWritableDatabase();
        final ContentValues values = new ContentValues();
        values.put(PARTS_UPLOADED, showProgress);
        // updating row
        returnValue = database.update(QUEUE_TABLE, values, VIDEO_ID + EQUALS_QUESTION,
                new String[] { String.valueOf(clientId) });
        return returnValue;
    }

    /**
     * Update tag details into local database.
     *
     * @param TagInfo dto
     * @param context
     * @param isUplaodedVideo
     */
    public void updateTag(final TagInfo tag, final Context context, final boolean uploaded) {

        final SQLiteDatabase database = this.dbhelper.getWritableDatabase();
        final ContentValues contentValues = new ContentValues();
        contentValues.put(TAG_NAME, tag.getName());
        contentValues.put(TAG_LINK, tag.getLink());
        contentValues.put(TAG_DISPLAYTIME, tag.getDisplayTime());
        contentValues.put(TAG_COLOR, tag.getColor());
        contentValues.put(TAG_FACEBOOKID, tag.getFbId());
        contentValues.put(TAG_TWITTERID, tag.getTwId());
        contentValues.put(TAG_TagFuID, tag.getTagFuId());
        contentValues.put(TAG_GPLUSID, tag.getgPlusId());
        contentValues.put(TAG_TIMEOUTFRAME, tag.getTagTimeOutFrame());
        contentValues.put(VIDEOPLAYBACKTIME, tag.getVideoPlaybackTime());
        contentValues.put(TAG_SCREENWIDTH, tag.getScreenWidth());
        contentValues.put(TAG_SCREENHEIGHT, tag.getScreenHeight());
        contentValues.put(TAG_X, tag.getTagX());
        contentValues.put(TAG_Y, tag.getTagY());
        contentValues.put(SERVER_VIDEOID, tag.getServerVideoId());
        contentValues.put(VIDEO_HEIGHT, tag.getVideoHeight());
        contentValues.put(VIDEO_WIDTH, tag.getVideoWidth());
        contentValues.put(SCREEN_RES_X, tag.getScreenResX());
        contentValues.put(SCREEN_RES_Y, tag.getScreenResY());
        contentValues.put(VIDEO_RES_X, tag.getVideoResX());
        contentValues.put(VIDEO_RES_Y, tag.getVideoResY());
        contentValues.put(UPLOAD_STATUS, tag.getUploadStatus());
        contentValues.put(SERVER_TAG_ID, tag.getServertagId());

        contentValues.put(PRODUCT_NAME, tag.getProductName());
        contentValues.put(PRODUCT_CATEGORY, tag.getProductCategory());
        contentValues.put(PRODUCT_DESCRIPTION, tag.getProductDescription());
        contentValues.put(PRODUCT_LINK, tag.getProductLink());
        contentValues.put(PRODUCT_PRICE, tag.getProductPrice());
        contentValues.put(SOLD, tag.getProductSold());
        contentValues.put(CURRENCY, tag.getProductCurrency());

        int update = 0;
        if (uploaded) {
            update = database.update(TAG_TABLE, contentValues, SERVER_TAG_ID + EQUALS_QUESTION_ + AND + SERVER_VIDEOID
                    + EQUALS_QUESTION_,
                    new String[] { String.valueOf(tag.getServertagId()), String.valueOf(tag.getServerVideoId()) });
        } else {
            update = database.update(TAG_TABLE, contentValues, _ID + EQUALS_QUESTION_ + AND + CLIENT_VIDEOID
                    + EQUALS_QUESTION_,
                    new String[] { String.valueOf(tag.getTagId()), String.valueOf(tag.getClientVideoId()) });
        }
        LOG.i("update tag succesfully" + update);

    }

    public int updateTagWithServerId(final long serverId, final long clientId, final int uploadStatus,
            final Context context) {

        int returnValue = 0;
        final SQLiteDatabase database = this.dbhelper.getWritableDatabase();

        final ContentValues values = new ContentValues();
        // values.put(DBHelper.SERVER_VIDEOID, serverVideoId);
        values.put(SERVER_TAG_ID, serverId);
        values.put(UPLOAD_STATUS, uploadStatus);
        // updating row
        returnValue = database.update(TAG_TABLE, values, _ID + EQUALS_QUESTION,
                new String[] { String.valueOf(clientId) });
        return returnValue;
    }

    public int updateTagWithServerIdAndVideoServerId(final long serverVideoId, final long serverId,
            final long clientId, final int uploadStatus, final Context context) {

        int returnValue = 0;
        final SQLiteDatabase database = this.dbhelper.getWritableDatabase();
        final ContentValues values = new ContentValues();
        values.put(SERVER_VIDEOID, serverVideoId);
        values.put(SERVER_TAG_ID, serverId);
        values.put(UPLOAD_STATUS, uploadStatus);
        // updating row
        returnValue = database.update(TAG_TABLE, values, _ID + EQUALS_QUESTION,
                new String[] { String.valueOf(clientId) });
        return returnValue;
    }

    public int updateTagWithVideoServerId(final String serverVideoId, final String clientId, final Context context) {

        int returnValue = 0;
        final SQLiteDatabase database = this.dbhelper.getWritableDatabase();
        final ContentValues values = new ContentValues();
        values.put(SERVER_VIDEOID, serverVideoId);
        // updating row
        returnValue = database.update(TAG_TABLE, values, _ID + EQUALS_QUESTION,
                new String[] { String.valueOf(clientId) });
        return returnValue;
    }

    public int updateUploadVideoState(final String clientId, final int showProgress) {

        int returnValue = 0;
        final SQLiteDatabase database = this.dbhelper.getWritableDatabase();
        final ContentValues values = new ContentValues();
        values.put(VIDEO_UPLOAD_COMPLETED, showProgress);
        // updating row
        returnValue = database.update(QUEUE_TABLE, values, VIDEO_ID + EQUALS_QUESTION,
                new String[] { String.valueOf(clientId) });
        // closeDB();
        return returnValue;
    }

    public int updateVideoData(final long serverId, final String serverUrl, final String clientId,
            final int uploadStatus, final int retry) {

        int returnValue = 0;
        final SQLiteDatabase database = this.dbhelper.getWritableDatabase();
        final ContentValues values = new ContentValues();
        values.put(SERVER_ID, serverId);
        values.put(VIDEO_UPLOAD_STATUS, uploadStatus);
        values.put(SERVER_URL, serverUrl);
        values.put(RETRY, retry);

        // updating row
        returnValue = database.update(QUEUE_TABLE, values, VIDEO_ID + EQUALS_QUESTION,
                new String[] { String.valueOf(clientId) });
        return returnValue;
    }

    /**
     * Update the uploading video's upload percentage.(if file upload fails need to be set it to zero )
     *
     * @param clientId
     * @param percentage
     * @param partNumber
     */
    public int updateVideoUploadPercentage(final String clientId, final int percentage, final int partNumber) {

        int returnValue = 0;
        final SQLiteDatabase database = this.dbhelper.getWritableDatabase();
        final ContentValues values = new ContentValues();
        values.put(VIDEO_UPLOAD_PERCENTAGE, percentage);
        values.put(CURRENT_PAGE, partNumber);
        // updating row
        returnValue = database.update(QUEUE_TABLE, values, VIDEO_ID + EQUALS_QUESTION,
                new String[] { String.valueOf(clientId) });
        return returnValue;
    }

    /**
     * Update the video url with given server url once it is uploaded .
     *
     * @param server video id
     * @param server url
     */
    public int updateVideoUrl(final String serverId, final String serverUrl) {

        int returnValue = 0;
        final SQLiteDatabase database = this.dbhelper.getWritableDatabase();
        final ContentValues values = new ContentValues();
        values.put(SERVER_URL, serverUrl);
        // updating row
        returnValue = database.update(QUEUE_TABLE, values, SERVER_ID + EQUALS_QUESTION, new String[] { serverId });
        return returnValue;
    }

    private boolean checkColumnExists(final SQLiteDatabase database, final String tableName, final String columnName) {

        final Cursor cursor = database.rawQuery("pragma table_info(" + tableName + ")", null);

        while (cursor.moveToNext()) {
            final String existedColumnName = cursor.getString(1);
            if ((columnName != null) && (existedColumnName != null) && columnName.equalsIgnoreCase(existedColumnName)) {
                return true;
            }
        }

        cursor.close();
        return false;
    }

    private void insertSequence(final SQLiteDatabase database, final String tableName, final long sequence) {

        database.execSQL("INSERT INTO " + SQLITE_SEQUENCE_TABLE + "(" + TABLE_NAME_COLUMN + ", " + SEQUENCE_COLUMN
                + ") VALUES('" + tableName + "', " + sequence + ")");
    }

    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(final Context context) {

            super(context, (STORE_DATABASE_ON_SD_CARD ? Environment.getExternalStorageDirectory() + TagFu : BLANK)
                    + context.getString(R.string.db_name), null, Integer.parseInt(context
                    .getString(R.string.db_version)));
        }

        @Override
        public void onCreate(final SQLiteDatabase dbx) {

            dbx.execSQL(CREATE_QUEUE_TABLE);
            LOG.i("database created ");

            dbx.execSQL("DROP TABLE IF EXISTS log");
            dbx.execSQL("CREATE TABLE log(_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + "tag TEXT NOT NULL, "
                    + "message TEXT NOT NULL, " + "time TEXT NOT NULL)");

            final long now = System.currentTimeMillis();

            dbx.execSQL("DROP TABLE IF EXISTS " + TAG_TABLE);
            dbx.execSQL("CREATE TABLE " + TAG_TABLE + "(" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + SERVER_TAG_ID + INTEGER + TAG_NAME + TEXT + TAG_LINK + TEXT + TAG_DISPLAYTIME + TEXT + TAG_COLOR
                    + TEXT + TAG_FACEBOOKID + TEXT + TAG_GPLUSID + TEXT + TAG_TagFuID + TEXT + TAG_TWITTERID + TEXT
                    + TAG_TIMEOUTFRAME + INTEGER + VIDEOPLAYBACKTIME + INTEGER + TAG_SCREENWIDTH + INTEGER
                    + TAG_SCREENHEIGHT + INTEGER + CLIENT_VIDEOID + TEXT + SERVER_VIDEOID + TEXT + VIDEO_HEIGHT
                    + INTEGER + VIDEO_WIDTH + INTEGER + VIDEO_RES_X + REAL + VIDEO_RES_Y + REAL + SCREEN_RES_X + REAL
                    + SCREEN_RES_Y + REAL + PRODUCT_NAME + TEXT + PRODUCT_DESCRIPTION + TEXT + PRODUCT_LINK + TEXT
                    + PRODUCT_PRICE + TEXT + SOLD + TEXT + PRODUCT_CATEGORY + TEXT + CURRENCY + TEXT + UPLOAD_STATUS
                    + INTEGER + TAG_X + REAL + TAG_Y + " REAL)");
            VideoDataBase.this.insertSequence(dbx, TAG_TABLE, now);
        }

        @Override
        public void onUpgrade(final SQLiteDatabase database, final int oldVersion, final int newVersion) {

            // database.execSQL("Drop table " + QUEUE_TABLE);
            // onCreate(database);

            if (oldVersion <= 4) {
                if (!VideoDataBase.this.checkColumnExists(database, QUEUE_TABLE, RETRY)) {
                    database.execSQL(ALTER_TABLE + QUEUE_TABLE + ADD_COLUMN + RETRY + "  INTEGER default 0");
                }
                if (!VideoDataBase.this.checkColumnExists(database, QUEUE_TABLE, CURRENT_PAGE)) {
                    database.execSQL(ALTER_TABLE + QUEUE_TABLE + ADD_COLUMN + CURRENT_PAGE + "  INTEGER default 0");
                }
                if (!VideoDataBase.this.checkColumnExists(database, QUEUE_TABLE, COVER_PAGE)) {
                    database.execSQL(ALTER_TABLE + QUEUE_TABLE + ADD_COLUMN + COVER_PAGE + " TEXT default 0");
                }

            }
            if (oldVersion <= 5) {
                if (!VideoDataBase.this.checkColumnExists(database, TAG_TABLE, PRODUCT_NAME)) {
                    database.execSQL(ALTER_TABLE + TAG_TABLE + ADD_COLUMN + PRODUCT_NAME + TEXT_DEFAULT_NULL);
                }
                if (!VideoDataBase.this.checkColumnExists(database, TAG_TABLE, PRODUCT_CATEGORY)) {
                    database.execSQL(ALTER_TABLE + TAG_TABLE + ADD_COLUMN + PRODUCT_CATEGORY + TEXT_DEFAULT_NULL);
                }
                if (!VideoDataBase.this.checkColumnExists(database, TAG_TABLE, PRODUCT_DESCRIPTION)) {
                    database.execSQL(ALTER_TABLE + TAG_TABLE + ADD_COLUMN + PRODUCT_DESCRIPTION + TEXT_DEFAULT_NULL);
                }
                if (!VideoDataBase.this.checkColumnExists(database, TAG_TABLE, PRODUCT_LINK)) {
                    database.execSQL(ALTER_TABLE + TAG_TABLE + ADD_COLUMN + PRODUCT_LINK + TEXT_DEFAULT_NULL);
                }
                if (!VideoDataBase.this.checkColumnExists(database, TAG_TABLE, PRODUCT_PRICE)) {
                    database.execSQL(ALTER_TABLE + TAG_TABLE + ADD_COLUMN + PRODUCT_PRICE + TEXT_DEFAULT_NULL);
                }
                if (!VideoDataBase.this.checkColumnExists(database, TAG_TABLE, SOLD)) {
                    database.execSQL(ALTER_TABLE + TAG_TABLE + ADD_COLUMN + SOLD + TEXT_DEFAULT_NULL);
                }
                if (!VideoDataBase.this.checkColumnExists(database, TAG_TABLE, CURRENCY)) {
                    database.execSQL(ALTER_TABLE + TAG_TABLE + ADD_COLUMN + CURRENCY + TEXT_DEFAULT_NULL);
                }
            }
        }

    }

}
