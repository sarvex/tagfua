/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.TagFu.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.TagFu.Constant;
import com.TagFu.VideoPlayerApp;
import com.TagFu.cache.CacheManager;
import com.TagFu.cache.CacheTransactionException;
import com.TagFu.connectivity.HttpConnectionManager;
import com.TagFu.connectivity.Parser;
import com.TagFu.connectivity.VideoDataBase;
import com.TagFu.dto.Comment;
import com.TagFu.dto.ErrorResponse;
import com.TagFu.dto.Notification;
import com.TagFu.dto.Playback;
import com.TagFu.dto.TagInfo;
import com.TagFu.dto.VideoInfo;
import com.TagFu.dto.VideoProfile;
import com.TagFu.util.Config;
import com.TagFu.util.Util;

public final class Backend {

    // TODO get parameter consistency
    private static final String VIDEO_RES_X = "video_res_x";
    private static final String VIDEO_RES_Y = "video_res_y";
    private static final String VIDEO_HEIGHT = "video_height";
    private static final String VIDEO_WIDTH = "video_width";
    private static final String SCREEN_RES_X = "screen_res_x";
    private static final String SCREEN_RES_Y = "screen_res_y";
    private static final String PRODUCT_CATEGORY = "productCategory";
    private static final String PRODUCT_DESCRIPTION = "productDescription";
    private static final String PRODUCT_LINK = "productLink";
    private static final String PRODUCT_NAME = "productName";
    private static final String PRODUCT_PRICE = "productPrice";
    private static final String TITLE = "Title";
    private static final String PUBLIC = "Public";
    private static final String UPLOAD_DATE = "Upload_date";
    private static final String DESCRIPTION = "Description";
    private static final String CLIENT_VIDEO_ID = "clientvideoId";
    // TODO get parameter consistency

    private static final String ACCEPT_PRIVATE_GROUP = Constant.COMMON_URL_MOBILE + "accept_pvtgroup/";
    private static final String ADD_REPORT = Constant.COMMON_URL_MOBILE + "addReport";
    private static final String ADD_TAGS = Constant.COMMON_URL_MOBILE + "addtags";
    private static final String BROWSE = Constant.COMMON_URL_MOBILE + "browse";
    private static final String BROWSE_DETAIL = Constant.COMMON_URL_MOBILE + "browsedetail";
    private static final String BUY_PRODUCT = Constant.COMMON_URL_MOBILE + "buy/";
    private static final String CHECK_FOLLOWING = Constant.COMMON_URL_MOBILE + "checkfollowing/";
    private static final String CHECK_PRIVATE_GROUP = Constant.COMMON_URL_MOBILE + "checkpvtgroup/";
    private static final String COMMENT_VIDEO = Constant.COMMON_URL_MOBILE + "commentvideo";
    private static final String DECLINE_PRIVATE_GROUP = Constant.COMMON_URL_MOBILE + "decline_pvtgroup/";
    private static final String DELETE_COMMENT = Constant.COMMON_URL_MOBILE + "deletecomment/";
    private static final String DELETE_TAG = Constant.COMMON_URL_MOBILE + "deletetag/";
    private static final String DELETE_VIDEO = Constant.COMMON_URL_MOBILE + "deletevideo/";
    private static final String DESCRIBE_PRODUCT = Constant.COMMON_URL_MOBILE + "product_info/";
    private static final String DISLIKE_VIDEO = Constant.COMMON_URL_MOBILE + "dislikevideo/";
    private static final String FEEDBACK = Constant.COMMON_URL_MOBILE + "addFeedback/";
    private static final String FILE_UPLOAD = Constant.COMMON_URL_MOBILE + "fileupload";
    private static final String FIND_FRIENDS = Constant.COMMON_URL_MOBILE + "findfriends/";
    private static final String FOLLOW = Constant.COMMON_URL_MOBILE + "follow/";
    private static final String FOLLOW_PRIVATE_GROUP = Constant.COMMON_URL_MOBILE + "follow_pvtgroup/";
    private static final String FORGOT_PASSWORD = Constant.COMMON_URL_MOBILE + "forgotpassword";
    private static final String GET_ALL_TRENDS = Constant.COMMON_URL_MOBILE + "getAllTrends/";
    private static final String INDIVIDUAL_VIEWS = Constant.COMMON_URL + "video/individual_views_live/";
    private static final String LIKE_VIDEO = Constant.COMMON_URL_MOBILE + "likevideo/";
    private static final String LOGIN = Constant.COMMON_URL_MOBILE + "login";
    private static final String LOVED_PEOPLE = Constant.COMMON_URL_MOBILE + "likelist/";
    private static final String MORE_VIDEOS = Constant.COMMON_URL_MOBILE + "mypagevideos";
    private static final String MY_ACCOUNT = Constant.COMMON_URL_MOBILE + "myaccount/";
    private static final String MY_OTHER_STUFF = Constant.COMMON_URL_MOBILE + "myotherstuff/";
    private static final String MY_PAGE = Constant.COMMON_URL_MOBILE + "mypage";
    private static final String MY_PAGE_SEARCH = Constant.COMMON_URL_MOBILE + "mypagesearch";
    private static final String MY_PAGE_VIDEOS = Constant.COMMON_URL_MOBILE + "mypagevideos";
    private static final String NOTIFICATION = Constant.COMMON_URL_MOBILE + "notifications/";
    private static final String NOTIFICATION_SETTING = Constant.COMMON_URL_MOBILE + "notificationsettings/";
    private static final String OTHER_PAGE = Constant.COMMON_URL_MOBILE + "otherpage";
    private static final String OTHER_PAGE_SEARCH = Constant.COMMON_URL_MOBILE + "otherpagesearch";
    private static final String PENDING_PRIVATE_REQUEST = Constant.COMMON_URL_MOBILE + "pending_pvtgrouplist/";
    private static final String PLAYBACK = Constant.COMMON_URL_MOBILE + "playback/";
    private static final String PRIVATE_FEED_SEARCH = Constant.COMMON_URL_MOBILE + "pvtgroup_videofeedsearch";
    private static final String PRIVATE_VIDEO_FEED = Constant.COMMON_URL_MOBILE + "pvtgroup_videofeed";
    private static final String REMOVE_NOTIFICATION = Constant.COMMON_URL_MOBILE + "remove_notification/";
    private static final String REQUEST_PRODUCT = Constant.COMMON_URL_MOBILE + "purchase_request/";
    private static final String SEARCH = Constant.COMMON_URL_MOBILE + "search";
    private static final String SEARCH_TRENDS = Constant.COMMON_URL_MOBILE + "searchTrends";
    private static final String SELL_PRODUCT = Constant.COMMON_URL_MOBILE + "sell/";
    private static final String SHARE_VIEWS = Constant.COMMON_URL + "video/share_views/";
    private static final String SIGNUP = Constant.COMMON_URL_MOBILE + "signup";
    private static final String SOCIAL_LOGIN = Constant.COMMON_URL_MOBILE + "sociallogin";
    private static final String SUGGESTED_USERS = Constant.COMMON_URL_MOBILE + "suggested_users/";
    private static final String TAG_INTERACTIONS = Constant.COMMON_URL + "video/tag_interactions/";
    private static final String TAG_USER_COMMENTS = Constant.COMMON_URL_MOBILE + "tagusercomments/";
    private static final String TRENDS = Constant.COMMON_URL_MOBILE + "trends";
    private static final String UNFOLLOW = Constant.COMMON_URL_MOBILE + "unfollow/";
    private static final String UPDATENOTIFICATION_SETTING = Constant.COMMON_URL_MOBILE + "updatenotificationsettings/";
    private static final String UPDATE_MY_ACCOUNT = Constant.COMMON_URL_MOBILE + "update_myaccount/";
    private static final String UPDATE_PASSWORD = Constant.COMMON_URL_MOBILE + "update_mypassword/";
    private static final String UPDATE_TAGS = Constant.COMMON_URL_MOBILE + "updatetags";
    private static final String UPDATE_VIDEO_ACCESS = Constant.COMMON_URL_MOBILE + "update_videoaccess/";
    private static final String UPLOAD_VIDEO = Constant.COMMON_URL_MOBILE + "upload_video";
    private static final String UPLOAD_VIDEO_PARTS = Constant.COMMON_URL_MOBILE + "upload_video_parts";
    private static final String VIDEO_DETAILS = Constant.COMMON_URL_MOBILE + "videodetails/";
    private static final String VIDEO_FEED = Constant.COMMON_URL_MOBILE + "videofeed";
    private static final String VIDEO_FEED_SEARCH = Constant.COMMON_URL_MOBILE + "videofeedsearch";

    private static final Logger LOG = LoggerManager.getLogger();

    private static CacheManager cacheManager;
    private static boolean continueFlag = true;
    private static boolean partSucess = true;

    public static Object accountDetails(final Context context, final String json) throws JSONException {

        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        if (Constant.IS_SERVER_REQUEST) {
            final String response = connection.httpPOST(MY_ACCOUNT, json, null, null);
            if (response != null) {
                result = Parser.parseMyAccountResponseJson(response);
            } else {
                result = Backend.returnErrorResponse();
            }
        }

        return result;
    }

    public static Object addPrivateGroupRequest(final Context context, final String userId, final String othersId)
            throws JSONException {

        LOG.v(Constant.CONNECTING_TO_SERVER);
        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        final String response = connection.httpGet(ACCEPT_PRIVATE_GROUP + userId + Constant.SLASH + othersId, null,
                Backend.getBodyParams());
        LOG.v(Constant.RESPONSE + response);
        if (response != null) {
            result = Parser.parseReportVideo(response);
        } else {
            result = Backend.returnErrorResponse();
        }

        return result;

    }

    public static Object addTags(final Context context, final List<TagInfo> tagList) throws JSONException {

        // List<TagResponse> tags=null;
        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        String json = null;
        json = Backend.getAddTagJson(tagList, Config.getUserId());
        final String response = connection.httpPOST(ADD_TAGS, json, null, null);
        if (response != null) {
            result = Parser.parseTagResponseJson(response);
        } else {
            result = Backend.returnErrorResponse();
        }
        return result;
    }

    public static Object browseDetails(final Context context, final String json) throws JSONException {

        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        if (Constant.IS_SERVER_REQUEST) {
            final String response = connection.httpPOST(BROWSE_DETAIL, json, null, null);
            if (response != null) {
                result = Parser.parseVideosDetails(response);
            } else {
                result = Backend.returnErrorResponse();
            }
        } else {
            result = Parser.parseVideosDetails(Util
                    .jsontoString(Constant.COM_AYANSYS_SAMPLEVIDEOPLAYER_TEMP_VIDEODEATILS_JSON));
        }
        return result;
    }

    public static Object browseVideos(final Context context, final JSONObject request, final String type,
            final boolean firstTime, final boolean pullToRefresh) throws JSONException {

        String response = null;
        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        if (Constant.TRENDS.equalsIgnoreCase(type)) {
            response = connection.httpPOST(GET_ALL_TRENDS, request.toString(), null, null);
        } else {
            response = connection.httpPOST(BROWSE, request.toString(), null, null);
        }

        if (response != null) {
            if (Constant.VIDEOS.equalsIgnoreCase(type)) {
                // VideoPlayerApp.getInstance().writeStackTraceToLog("browse resposne "+response.toString(),
                // context);
                result = Parser.parseVideoFeedJson(response);
            } else if (Constant.TAGS.equalsIgnoreCase(type)) {
                result = Parser.parseTagsJson(response);
            } else if (Constant.PEOPLE.equalsIgnoreCase(type)) {
                result = Parser.parsePeopleResponse(response);
            } else if (Constant.TRENDS.equalsIgnoreCase(type)) {
                result = Parser.parsePagesResponse(response);
            }
        } else {
            result = Backend.returnErrorResponse();
        }
        return result;
    }

    public static Object buyProduct(final Context context, final String request) throws JSONException {

        LOG.v(Constant.CONNECTING_TO_SERVER);
        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        final String response = connection.httpPOST(BUY_PRODUCT, request, null, null);
        LOG.v(Constant.RESPONSE + response);
        if (response != null) {
            result = Parser.parseBuyProduct(response);
        } else {
            result = Backend.returnErrorResponse();
        }
        return result;

    }

    public static Object changePassword(final Context context, final String json) throws JSONException {

        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        final String response = connection.httpPOST(UPDATE_PASSWORD, json, null, null);
        if (response != null) {
            result = Parser.parseReportVideo(response);
        } else {
            result = Backend.returnErrorResponse();
        }

        return result;
    }

    public static Object deleteComment(final Context context, final String commentId, final String videoId)
            throws JSONException {

        LOG.v(Constant.CONNECTING_TO_SERVER);
        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        final String response = connection.httpGet(DELETE_COMMENT + commentId + Constant.SLASH + videoId, null, null);
        LOG.v(Constant.RESPONSE + response);
        if (response != null) {
            result = Parser.parseReportVideo(response);
        } else {
            result = Backend.returnErrorResponse();
        }

        return result;

    }

    public static boolean deleteTag(final Context context, final String tagid) throws JSONException {

        boolean result = false;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        final String response = connection.httpGet(DELETE_TAG + tagid, null, Backend.getBodyParams());
        if (response != null) {
            result = Parser.parseUpdateJson(response);
        }
        return result;
    }

    public static Object describeProduct(final Context context, final String request) throws JSONException {

        LOG.v(Constant.CONNECTING_TO_SERVER);
        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        final String response = connection.httpPOST(DESCRIBE_PRODUCT, request, null, null);
        LOG.v(Constant.RESPONSE + response);
        if (response != null) {
            result = Parser.parseDescribeProduct(response);
        } else {
            result = Backend.returnErrorResponse();
        }
        return result;

    }

    public static Object feedback(final Context context, final String json) throws JSONException {

        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        final String response = connection.httpPOST(FEEDBACK, json, null, null);
        if (response != null) {
            result = Parser.parseReportVideo(response);
        } else {
            result = Backend.returnErrorResponse();
        }

        return result;
    }

    /**
     * Returns true if files are merged properly at server side other wise it will return checksum error.
     *
     * @param context
     * @param VideoInfo object
     * @throws JSONException
     */
    public static boolean fileUpload(final Context context, final VideoInfo video) throws JSONException {

        boolean result = false;
        FileInputStream fileInputStream = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        final String urlServer = FILE_UPLOAD;
        try {
            final File file = new File(video.getLocalMediaPath());
            fileInputStream = new FileInputStream(file);
            final int bufferSize = 1024 * 1024;
            final int totalPart = ((int) file.length() / bufferSize) + ((file.length() % bufferSize) > 0 ? 1 : 0);
            final String md5hash = Util.getMD5Hash(fileInputStream);
            final String url = urlServer;
            final String json = Backend.getVideoPartJson(video, md5hash, totalPart, 0);
            final String response = connection.httpPOST(url, json, null, null);
            result = Parser.parseFileUploadResponse(response);

        } catch (final FileNotFoundException exception) {
            LOG.e(exception);
        } finally {
            if (null != fileInputStream) {
                try {
                    fileInputStream.close();
                } catch (final IOException exception) {
                    LOG.e(exception);
                }
            }
        }
        return result;
    }

    public static Object follow(final Context context, final String userId, final String othersId) throws JSONException {

        LOG.v(Constant.CONNECTING_TO_SERVER);
        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        final String response = connection.httpGet(FOLLOW + userId + Constant.SLASH + othersId, null, null);
        LOG.v(Constant.RESPONSE + response);
        if (response != null) {
            result = Parser.parseReportVideo(response);
        } else {
            result = Backend.returnErrorResponse();
        }

        return result;

    }

    public static String forgotPassword(final Context context, final JSONObject request) {

        String result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        result = connection.httpPOST(FORGOT_PASSWORD, request.toString(), null, null);
        return result;
    }

    public static Object getAllNotifications(final Context context, final String userId, final boolean firstTime,
            final boolean pullToRefresh) throws JSONException {

        Object result = null;
        String response = Constant.EMPTY;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        Backend.cacheManager = CacheManager.getInstance(VideoPlayerApp.getAppContext());
        final File file = new File(CacheManager.cacheDir + Constant.NOTIFICATION_CACHE);
        if (firstTime || pullToRefresh) {
            if (file.exists() && firstTime) {
                try {
                    response = Backend.cacheManager.readString(Constant.NOTIFICATION_CACHE);
                } catch (final CacheTransactionException exception) {
                    LOG.e(exception);
                }
            } else {
                response = connection.httpGet(NOTIFICATION + userId, null, Backend.getBodyParams());
            }
        } else {
            response = connection.httpGet(NOTIFICATION + userId, null, Backend.getBodyParams());
        }

        LOG.v(Constant.RESPONSE + response);
        if (response != null) {
            result = Parser.parseNotifications(response);
            if (!(result instanceof ErrorResponse) && (firstTime || pullToRefresh)) {
                if (file.exists() && firstTime) {
                } else {
                    try {
                        Backend.cacheManager.write(response, Constant.NOTIFICATION_CACHE);
                    } catch (final CacheTransactionException exception) {
                        LOG.e(exception);
                    }
                }
            }
        } else {
            result = Backend.returnErrorResponse();
        }

        return result;

    }

    public static Object getAllNotificationsFromCache() throws JSONException {

        Object result = null;
        String response = Constant.EMPTY;
        Backend.cacheManager = CacheManager.getInstance(VideoPlayerApp.getAppContext());
        final File file = new File(CacheManager.cacheDir + Constant.NOTIFICATION_CACHE);
        if (file.exists()) {
            try {
                response = Backend.cacheManager.readString(Constant.NOTIFICATION_CACHE);
            } catch (final CacheTransactionException exception) {
                LOG.e(exception);
            }
            if (response != null) {
                result = Parser.parseNotifications(response);
            }
        }

        return result;

    }

    public static Object getAtUsers(final Context context, final String json) throws JSONException {

        String response = null;
        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        response = connection.httpPOST(TAG_USER_COMMENTS, json, null, null);
        if (response != null) {
            result = Parser.atUsers(response);
        } else {
            result = Backend.returnErrorResponse();
        }
        return result;
    }

    public static List<Comment> getCommentList(final Context context, final String url) throws JSONException {

        LOG.v(Constant.CONNECTING_TO_SERVER);
        List<Comment> list = new ArrayList<Comment>();
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        final String response = connection.httpGet(url, null, Backend.getBodyParams());
        LOG.v(Constant.RESPONSE + response);
        if (response != null) {
            list = Parser.parseUserCommentsJson(response);
        }

        return list;

    }

    public static Object getLovedPeopleList(final Context context, final String videoId, final int pageNo)
            throws JSONException {

        LOG.v(Constant.CONNECTING_TO_SERVER);
        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        final String response = connection.httpGet(LOVED_PEOPLE + videoId + Constant.SLASH + pageNo, null,
                Backend.getBodyParams());
        LOG.v(Constant.RESPONSE + response);
        if (response != null) {
            result = Parser.parseLovedPeopleJson(response);
        } else {
            result = Backend.returnErrorResponse();
        }

        return result;

    }

    public static Object getMoreVideos(final Context context, final String json) throws JSONException {

        String response = null;
        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        response = connection.httpPOST(MORE_VIDEOS, json, null, null);
        if (response != null) {
            result = Parser.myPageVideos(response);
        } else {
            result = Backend.returnErrorResponse();
        }
        return result;
    }

    public static JSONObject getMypageVideos(final Context context, final String userid) throws JSONException {

        String response = null;
        JSONObject result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        response = connection.httpGet(MY_PAGE + Constant.SLASH + userid, null, Backend.getBodyParams());
        if (response != null) {
            result = new JSONObject(response);
            if (result.has(Constant.ERROR_CODE)) {
                final int errorcode = result.getInt(Constant.ERROR_CODE);
                if (errorcode == 0) {
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }
        return result;
    }

    public static List<VideoProfile> getMypageVideosPagination(final Context context, final JSONObject json)
            throws JSONException {

        String response = null;
        List<VideoProfile> myvideos = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        response = connection.httpPOST(MY_PAGE_VIDEOS, json.toString(), null, null);
        if (response != null) {
            myvideos = Parser.parseMyPagePaginationVideosResponseJson(response);
        }
        return myvideos;
    }

    public static Object getPrivateFeedFromCache() throws JSONException {

        String response = null;
        Object result = null;
        Backend.cacheManager = CacheManager.getInstance(VideoPlayerApp.getAppContext());
        final File file = new File(CacheManager.cacheDir + Constant.PRIVATE_FEEDS_CACHE);
        if (file.exists()) {
            try {
                response = Backend.cacheManager.readString(Constant.PRIVATE_FEEDS_CACHE);
            } catch (final CacheTransactionException exception) {
                LOG.e(exception);
            }
            if (response != null) {
                result = Parser.parseFeed(response);
            }
        }
        return result;
    }

    public static Object getPushNotificationSettings(final Context context, final String userid) throws JSONException {

        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        final String response = connection.httpGet(NOTIFICATION_SETTING + userid, null, Backend.getBodyParams());
        if (response != null) {
            result = Parser.parseNotificationSettingsResponseJson(response);
        } else {
            result = Backend.returnErrorResponse();
        }

        return result;
    }

    public static Object getPVTPenidngRequestsList(final Context context, final String userId, final int pageNo)
            throws JSONException {

        Object list = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        final String response = connection.httpGet(PENDING_PRIVATE_REQUEST + userId + Constant.SLASH + pageNo, null,
                Backend.getBodyParams());
        if (response != null) {
            list = Parser.parsePendingPrivateRequestJson(response);
        } else {
            list = Backend.returnErrorResponse();
        }

        return list;

    }

    public static Object getUsersList(final Context context, final String URL, final String type) throws JSONException {

        LOG.v(Constant.CONNECTING_TO_SERVER);
        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        final String response = connection.httpGet(URL, null, Backend.getBodyParams());
        LOG.v(Constant.RESPONSE + response);
        if (response != null) {
            result = Parser.parseTagFuFriendsResponseJson(response, type);
            // list = Parser.parseWoo(response, type);
        } else {
            result = Backend.returnErrorResponse();
        }

        return result;

    }

    public static Object getVideoFeedFromCache() throws JSONException {

        String response = null;
        Object result = null;
        Backend.cacheManager = CacheManager.getInstance(VideoPlayerApp.getAppContext());
        final File file = new File(CacheManager.cacheDir + Constant.VIDEO_FEEDS_CACHE);
        if (file.exists()) {
            try {
                response = Backend.cacheManager.readString(Constant.VIDEO_FEEDS_CACHE);
            } catch (final CacheTransactionException exception) {
                LOG.e(exception);
            }
            if (response != null) {
                result = Parser.parseFeed(response);
            }
        }
        return result;
    }

    public static Object getTagFuFriendFinderList(final Context context, final String userId, final int pageNo)
            throws JSONException {

        LOG.v(Constant.CONNECTING_TO_SERVER);
        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        final String response = connection.httpGet(SUGGESTED_USERS + userId + Constant.SLASH + pageNo, null,
                Backend.getBodyParams());
        LOG.v(Constant.RESPONSE + response);
        if (response != null) {
            result = Parser.suggestedUsers(response);
        } else {
            result = Backend.returnErrorResponse();
        }

        return result;

    }

    public static Object getTagFuSocialFriendsList(final Context context, final String request) throws JSONException {

        LOG.v(Constant.CONNECTING_TO_SERVER);
        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        final String response = connection.httpPOST(FIND_FRIENDS, request, null, null);
        LOG.v(Constant.RESPONSE + response);
        if (response != null) {
            result = Parser.parseSocialFriendFinderList(response);
        } else {
            result = Backend.returnErrorResponse();
        }

        return result;

    }

    public static Object isCheckFollower(final Context context, final String loginId, final String userId)
            throws JSONException {

        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        final String url = CHECK_FOLLOWING + loginId + Constant.SLASH + userId;
        final HashMap<String, String> peraHashMap = new HashMap<String, String>();
        peraHashMap.put(Constant.DEVICE, Constant.DEVICE_MODEL);
        peraHashMap.put(Constant.RESOLUTION, Config.getDeviceResolutionValue());
        final String response = connection.httpGet(url, null, peraHashMap);
        if (response != null) {
            result = Parser.parseCheckFollowersResponse(response);
        } else {
            result = Backend.returnErrorResponse();
        }
        return result;
    }

    public static Object isPrivateGroup(final Context context, final String loginId, final String userId)
            throws JSONException {

        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        final String url = CHECK_PRIVATE_GROUP + loginId + Constant.SLASH + userId;
        final HashMap<String, String> peraHashMap = new HashMap<String, String>();
        peraHashMap.put(Constant.DEVICE, Constant.DEVICE_MODEL);
        peraHashMap.put(Constant.RESOLUTION, Config.getDeviceResolutionValue());
        final String response = connection.httpGet(url, null, peraHashMap);
        if (response != null) {
            result = Parser.parseCheckPvtGrpResponse(response);
        } else {
            result = Backend.returnErrorResponse();
        }
        return result;
    }

    public static String login(final Context context, final JSONObject request) {

        String result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        result = connection.httpPOST(LOGIN, request.toString(), null, null);
        return result;
    }

    /**
     * Returns Object ( list of video objects)
     *
     * @param context
     * @param json request
     */
    public static Object myPageSearch(final Context context, final String json) throws JSONException {

        String response = null;
        Object returnObj = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        response = connection.httpPOST(MY_PAGE_SEARCH, json, null, null);

        if (response != null) {
            returnObj = Parser.myPageVideos(response);
        } else {
            returnObj = Backend.returnErrorResponse();
        }
        return returnObj;
    }

    public static Object myPageVideos(final Context context, final JSONObject request, final boolean firstTime,
            final boolean pullTorefresh) throws JSONException {

        Object result = null;
        String response = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        Backend.cacheManager = CacheManager.getInstance(VideoPlayerApp.getAppContext());
        final File file = new File(CacheManager.cacheDir + Constant.MY_PAGE_CACHE);
        if (firstTime || pullTorefresh) {
            if (file.exists() && firstTime) {
                try {
                    response = Backend.cacheManager.readString(Constant.MY_PAGE_CACHE);
                } catch (final CacheTransactionException exception) {
                    LOG.e(exception);
                }
            } else {
                response = connection.httpPOST(MY_PAGE, request.toString(), null, null);
            }
        } else {
            response = connection.httpPOST(MY_PAGE, request.toString(), null, null);
        }

        if (response != null) {
            result = Parser.myPage(response);
            if (!(result instanceof ErrorResponse) && (firstTime || pullTorefresh)) {
                if (file.exists() && firstTime) {
                } else {
                    try {
                        Backend.cacheManager.write(response, Constant.MY_PAGE_CACHE);
                    } catch (final CacheTransactionException exception) {
                        LOG.e(exception);
                    }
                }
            }
        } else {
            result = Backend.returnErrorResponse();
        }

        return result;
    }

    public static Object mypageVideosFromCache() throws JSONException {

        Object result = null;
        String response = null;
        Backend.cacheManager = CacheManager.getInstance(VideoPlayerApp.getAppContext());
        final File file = new File(CacheManager.cacheDir + Constant.MY_PAGE_CACHE);
        if (file.exists()) {
            try {
                response = Backend.cacheManager.readString(Constant.MY_PAGE_CACHE);
            } catch (final CacheTransactionException exception) {
                LOG.e(exception);
            }
            if (response != null) {
                result = Parser.myPage(response);
            }
        }

        return result;
    }

    public static Object myTrendVideos(final Context context, final String json) throws JSONException {

        String response = null;
        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        response = connection.httpPOST(TRENDS, json, null, null);
        if (response != null) {
            result = Parser.myTrendVideos(response);
        } else {
            result = Backend.returnErrorResponse();
        }
        return result;
    }

    public static Object notificationVideoDetails(final Context context, final String videoId, final int type)
            throws JSONException {

        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        if (Constant.IS_SERVER_REQUEST) {
            final String response = connection.httpGet(VIDEO_DETAILS + videoId + Constant.SLASH + type, null,
                    Backend.getBodyParams());
            if (response != null) {
                result = Parser.parseNotificationVideosDetails(response);
            } else {
                result = Backend.returnErrorResponse();
            }
        }

        return result;
    }

    public static Object otherSearch(final Context context, final String json) throws JSONException {

        String response = null;
        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        response = connection.httpPOST(OTHER_PAGE_SEARCH, json, null, null);

        if (response != null) {
            result = Parser.myPageVideos(response);
        } else {
            result = Backend.returnErrorResponse();
        }
        return result;
    }

    public static Object otherStuff(final Context context, final String userid, final String pagNo)
            throws JSONException {

        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        final String response = connection.httpGet(MY_OTHER_STUFF + userid + Constant.SLASH + pagNo, null,
                Backend.getBodyParams());
        if (response != null) {
            result = Parser.parseOtherStuff(response);
        } else {
            result = Backend.returnErrorResponse();
        }

        return result;
    }

    public static Object otherUserVideos(final Context context, final JSONObject request) throws JSONException {

        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        final String response = connection.httpPOST(OTHER_PAGE, request.toString(), null, null);
        if (response != null) {
            result = Parser.myPage(response);
        } else {
            result = Backend.returnErrorResponse();
        }
        return result;
    }

    public static Object playBack(final Context context, final String videoId) throws JSONException {

        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        final String url = PLAYBACK + videoId;
        final HashMap<String, String> peraHashMap = new HashMap<String, String>();
        peraHashMap.put(Constant.DEVICE, Constant.DEVICE_MODEL);
        peraHashMap.put(Constant.RESOLUTION, Config.getDeviceResolutionValue());
        final String response = connection.httpGet(url, null, peraHashMap);
        if (response != null) {
            result = Parser.parsePlaybackResponseJson(response);
        } else {
            result = Backend.returnErrorResponse();
        }
        return result;
    }

    public static Object postComment(final Context context, final String json) throws JSONException {

        LOG.v(Constant.CONNECTING_TO_SERVER);
        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        final String response = connection.httpPOST(COMMENT_VIDEO, json, null, null);
        LOG.v(Constant.RESPONSE + response);
        if (response != null) {
            result = Parser.parseComment(response);
        } else {
            result = Backend.returnErrorResponse();
        }

        return result;

    }

    public static Object privateGroupRequest(final Context context, final String userId, final String othersId)
            throws JSONException {

        LOG.v(Constant.CONNECTING_TO_SERVER);
        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        final String response = connection.httpGet(FOLLOW_PRIVATE_GROUP + userId + Constant.SLASH + othersId, null,
                Backend.getBodyParams());
        LOG.v(Constant.RESPONSE + response);
        if (response != null) {
            result = Parser.parseReportVideo(response);
        } else {
            result = Backend.returnErrorResponse();
        }

        return result;

    }

    public static Object privateVideoFeed(final Context context, final String request, final boolean firstTime,
            final boolean pullToRefresh) throws JSONException {

        String response = null;
        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        Backend.cacheManager = CacheManager.getInstance(VideoPlayerApp.getAppContext());
        final File file = new File(CacheManager.cacheDir + Constant.PRIVATE_FEEDS_CACHE);
        if (firstTime || pullToRefresh) {
            if (file.exists() && firstTime) {
                try {
                    response = Backend.cacheManager.readString(Constant.PRIVATE_FEEDS_CACHE);
                } catch (final CacheTransactionException exception) {
                    LOG.e(exception);
                }
            } else {
                response = connection.httpPOST(PRIVATE_VIDEO_FEED, request, null, null);
            }
        } else {
            response = connection.httpPOST(PRIVATE_VIDEO_FEED, request, null, null);
        }
        if (response != null) {
            result = Parser.parseFeed(response);

            if (!(result instanceof ErrorResponse) && (firstTime || pullToRefresh)) {
                if (file.exists() && firstTime) {
                } else {
                    try {
                        Backend.cacheManager.write(response, Constant.PRIVATE_FEEDS_CACHE);
                    } catch (final CacheTransactionException exception) {
                        LOG.e(exception);
                    }
                }
            }
        } else {
            result = Backend.returnErrorResponse();
        }

        return result;
    }

    public static Object privateVideoFeedSearch(final Context context, final String request) throws JSONException {

        String response = null;
        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        response = connection.httpPOST(PRIVATE_FEED_SEARCH, request, null, null);
        if (response != null) {
            result = Parser.parseFeed(response);
        } else {
            result = Backend.returnErrorResponse();
        }
        return result;
    }

    public static void refreshFeeds(final Context context, final String request, final String type) {

        String response = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        Backend.cacheManager = CacheManager.getInstance(VideoPlayerApp.getAppContext());
        if (Constant.PUBLIC_FEED.equalsIgnoreCase(type)) {
        } else if (Constant.MY_PAGE_CACHE.equalsIgnoreCase(type)) {
            response = connection.httpPOST(MY_PAGE, request, null, null);
            if (response != null) {
                try {
                    Backend.cacheManager.write(response, Constant.MY_PAGE_CACHE);
                } catch (final CacheTransactionException exception) {
                    LOG.e(exception);
                }
            }
        } else if (Constant.NOTIFICATION_CACHE.equalsIgnoreCase(type)) {
            response = connection.httpGet(NOTIFICATION + request, null, Backend.getBodyParams());
            try {
                Backend.cacheManager.write(response, Constant.NOTIFICATION_CACHE);
            } catch (final CacheTransactionException exception) {
                LOG.e(exception);
            }

        }
    }

    public static Object removeNotification(final Context context, final String loginid, final Notification notification)
            throws JSONException {

        String response = null;
        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        response = connection.httpGet(REMOVE_NOTIFICATION + notification.getNoticeId(), null, null);
        if (response != null) {
            result = Boolean.valueOf(Parser.parseUpdateJson(response));
        }
        return result;
    }

    public static Object reportVideo(final Context context, final String json) throws JSONException {

        LOG.v(Constant.CONNECTING_TO_SERVER);
        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        final String response = connection.httpPOST(ADD_REPORT, json, null, null);
        LOG.v(Constant.RESPONSE + response);
        if (response != null) {
            result = Parser.parseReportVideo(response);
        } else {
            result = Backend.returnErrorResponse();
        }

        return result;

    }

    public static Object requestProduct(final Context context, final String request) throws JSONException {

        LOG.v(Constant.CONNECTING_TO_SERVER);
        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        final String response = connection.httpPOST(REQUEST_PRODUCT, request, null, null);
        LOG.v(Constant.RESPONSE + response);
        if (response != null) {
            result = Parser.parseRequestProduct(response);
        } else {
            result = Backend.returnErrorResponse();
        }
        return result;

    }

    public static Object search(final Context context, final JSONObject request, final String type)
            throws JSONException {

        String response = null;
        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        if (Constant.IS_SERVER_REQUEST) {
            if (Constant.TRENDS.equalsIgnoreCase(type)) {
                response = connection.httpPOST(SEARCH_TRENDS, request.toString(), null, null);
            } else {
                response = connection.httpPOST(SEARCH, request.toString(), null, null);
            }
        } else {
            response = Util.jsontoString(Constant.COM_AYANSYS_SAMPLEVIDEOPLAYER_TEMP_VIDEOFEED_JSON);
        }
        if (response != null) {
            if (Constant.VIDEOS.equalsIgnoreCase(type)) {
                result = Parser.parseVideoFeedJson(response);

            } else if (Constant.TAGS.equalsIgnoreCase(type)) {
                result = Parser.parseTagsJson(response);

            } else if (Constant.PEOPLE.equalsIgnoreCase(type)) {
                result = Parser.parsePeopleResponse(response);

            } else if (Constant.TRENDS.equalsIgnoreCase(type)) {
                result = Parser.parsePagesResponse(response);
            }

        } else {
            result = Backend.returnErrorResponse();
        }
        return result;
    }

    public static Object sellProduct(final Context context, final String request) throws JSONException {

        LOG.v(Constant.CONNECTING_TO_SERVER);
        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        final String response = connection.httpPOST(SELL_PRODUCT, request, null, null);
        LOG.v(Constant.RESPONSE + response);
        if (response != null) {
            result = Parser.parseSellProduct(response);
        } else {
            result = Backend.returnErrorResponse();
        }
        return result;

    }

    public static Object shareViews(final Context context, final String videoId, final String socialPlatform,
            final String count, final String userId) {

        LOG.v(Constant.CONNECTING_TO_SERVER);
        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        final String url = SHARE_VIEWS + videoId + Constant.SLASH + socialPlatform + Constant.SLASH + count
                + Constant.SLASH + userId;
        LOG.v("url", "url : " + url);
        final String response = connection.httpGet(SHARE_VIEWS + videoId + Constant.SLASH + socialPlatform
                + Constant.SLASH + count + Constant.SLASH + userId, null, null);
        LOG.v(Constant.RESPONSE + response);
        if (response != null) {
            // obj = Parser.parseReportVideo(response);
        } else {
            result = Backend.returnErrorResponse();
        }

        return result;

    }

    public static String signUp(final Context context, final JSONObject request) {

        String result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        result = connection.httpPOST(SIGNUP, request.toString(), null, null);
        return result;
    }

    public static Object socialInteractions(final Context context, final String videoId, final String socialPlatform,
            final String interactions, final String userId) {

        LOG.v(Constant.CONNECTING_TO_SERVER);
        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        LOG.v("url : " + TAG_INTERACTIONS + videoId + Constant.SLASH + socialPlatform + Constant.SLASH + interactions
                + Constant.SLASH + userId);
        final String response = connection.httpGet(TAG_INTERACTIONS + videoId + Constant.SLASH + socialPlatform
                + Constant.SLASH + interactions + Constant.SLASH + userId, null, null);
        LOG.v(Constant.RESPONSE + response);
        if (response != null) {
            // obj = Parser.parseReportVideo(response);
        } else {
            result = Backend.returnErrorResponse();
        }

        return result;

    }

    public static String socialLogin(final Context context, final JSONObject request) {

        String result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        result = connection.httpPOST(SOCIAL_LOGIN, request.toString(), null, null);
        return result;
    }

    public static Object suggestedUsers(final Context context, final String userId, final String pageNo)
            throws JSONException {

        String response = null;
        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        response = connection
                .httpGet(SUGGESTED_USERS + userId + Constant.SLASH + pageNo, null, Backend.getBodyParams());
        if (response != null) {
            result = Parser.suggestedUsers(response);
        } else {
            result = Backend.returnErrorResponse();
        }
        return result;
    }

    public static Object trends(final Context context, final String request) throws JSONException {

        String response = null;
        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        response = connection.httpPOST(BROWSE, request, null, null);
        if (response == null) {
            result = Backend.returnErrorResponse();
        } else {
            result = Parser.myPageVideos(response);
        }

        return result;
    }

    public static Object unFollow(final Context context, final String userId, final String othersId)
            throws JSONException {

        LOG.v(Constant.CONNECTING_TO_SERVER);
        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        final String response = connection.httpGet(UNFOLLOW + userId + Constant.SLASH + othersId, null,
                Backend.getBodyParams());
        LOG.v(Constant.RESPONSE + response);
        if (response != null) {
            result = Parser.parseReportVideo(response);
        } else {
            result = Backend.returnErrorResponse();
        }

        return result;

    }

    public static Object unPrivateGroup(final Context context, final String userId, final String othersId)
            throws JSONException {

        LOG.v(Constant.CONNECTING_TO_SERVER);
        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        final String response = connection.httpGet(DECLINE_PRIVATE_GROUP + userId + Constant.SLASH + othersId, null,
                Backend.getBodyParams());
        LOG.v(Constant.RESPONSE + response);
        if (response != null) {
            result = Parser.parseReportVideo(response);
        } else {
            result = Backend.returnErrorResponse();
        }

        return result;

    }

    public static Object updateAccessPermissionPassword(final Context context, final String json) throws JSONException {

        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        final String response = connection.httpPOST(UPDATE_VIDEO_ACCESS, json, null, null);
        if (response != null) {
            result = Parser.parseReportVideo(response);
        } else {
            result = Backend.returnErrorResponse();
        }

        return result;
    }

    public static Object updateAccount(final Context context, final String json) throws JSONException {

        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        final String response = connection.httpPOST(UPDATE_MY_ACCOUNT, json, null, null);
        if (response != null) {
            result = Parser.parseReportVideo(response);
        } else {
            result = Backend.returnErrorResponse();
        }

        return result;
    }

    public static void updatePushNotificationSettings(final Context context, final JSONObject json) {

        final HttpConnectionManager connection = new HttpConnectionManager(context);
        connection.httpPOST(UPDATENOTIFICATION_SETTING, json.toString(), null, null);

    }

    public static boolean updateTags(final Context context, final List<TagInfo> tagList) throws JSONException {

        boolean result = false;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        String json = null;
        json = Backend.getUpdateTagJson(tagList, Config.getUserId());

        final String response = connection.httpPOST(UPDATE_TAGS, json, null, null);
        if (response != null) {
            result = Parser.parseUpdateJson(response);
        }
        return result;
    }

    /**
     * Return true if part upload successfully otherwsie it will return false.
     *
     * @param context
     * @param videoInfo object
     * @param partNumber
     * @throws JSONException
     */
    public static boolean uploadMultiPartVideo(final Context context, final VideoInfo video, final int partNumber)
            throws JSONException {

        Backend.continueFlag = true;
        Backend.partSucess = true;
        boolean allSucceeded = false;
        Config.setCurrentUploadVideoID(video.getVideoClientId());
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        final String urlServer = UPLOAD_VIDEO_PARTS;
        final File file = new File(video.getLocalMediaPath());
        byte[] buffer = null;
        final int bufferSize = 1024 * 1024;
        final int remainBytes = (int) (file.length() % bufferSize);
        final int totalPart = ((int) file.length() / bufferSize) + ((file.length() % bufferSize) > 0 ? 1 : 0);
        LOG.i("upload total parts " + totalPart);
        int part = 1;

        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);

            while (Backend.continueFlag) {
                if (Backend.partSucess) {

                    if (part == totalPart) {
                        buffer = new byte[remainBytes];
                        fileInputStream.read(buffer, 0, remainBytes);
                        LOG.i("upload lastpart " + buffer.length);
                    } else if (part < (totalPart)) {
                        buffer = new byte[bufferSize];
                        fileInputStream.read(buffer, 0, bufferSize);
                        LOG.i("upload parts " + buffer.length);
                    }
                }

                final String url = urlServer;
                final String md5hash = Util.getMD5Hash(buffer);
                int tryCount = 3;
                while ((tryCount > 0) && Backend.continueFlag) {
                    LOG.i("upload For part: " + (part) + " of " + totalPart);

                    final String filename = video.getTitle() + Constant.DOT + video.getFileExtension();
                    final Map<String, String> params = new HashMap<String, String>();
                    params.put(Constant.CHECKSUM, md5hash);
                    params.put(Constant.USERID, String.valueOf(video.getUserid()));
                    params.put(CLIENT_VIDEO_ID, Config.getCurrentUploadVideoId());
                    params.put(Constant.FILE_NAME, filename);
                    params.put(Constant.PART_NO, String.valueOf(part));
                    params.put(Constant.TOTAL_COUNT, String.valueOf(totalPart));
                    if (part <= partNumber) {
                        Backend.partSucess = true;
                        break;
                    } else if (connection.httpPOSTData(url, buffer, null, params) != 0) {
                        Backend.partSucess = true;
                        break;
                    } else {
                        Backend.partSucess = false;
                        tryCount--;
                    }
                }

                if (Backend.partSucess) {
                    if (part == totalPart) {
                        Backend.continueFlag = false;
                        allSucceeded = true;
                    }
                    final Intent intent = new Intent(Constant.ACTION_FILE_UPLOAD_PROGRESS);
                    Config.setUploadedPercentage((part * 100) / totalPart);
                    VideoDataBase.getInstance(VideoPlayerApp.getAppContext()).updateVideoUploadPercentage(
                            video.getVideoClientId(), Config.getUploadedPercentage(), part);
                    intent.putExtra(Constant.ACTION_FILE_UPLOAD_PROGRESS, (part * 100) / totalPart);
                    context.sendBroadcast(intent);
                    final int percent = VideoDataBase.getInstance(context).getVideoUploadPercentage(
                            video.getVideoClientId(), context);
                    part++;
                } else {
                    allSucceeded = false;
                    break;
                }
            }
        } catch (final FileNotFoundException exception) {
            LOG.e(exception);
        } catch (final IOException exception) {
            LOG.e(exception);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (final IOException exception) {
                    LOG.e(exception);
                }
            }
        }

        return allSucceeded;
    }

    /**
     * This is used for to upload a video once parts uplaod done Return Playback dto if this api success .
     *
     * @param context
     * @param Videoinfo object
     * @param hitCount (max 3)
     * @throws JSONException
     */
    public static Object uploadVideo(final Context context, final VideoInfo video, final int count)
            throws JSONException {

        Object result = null;
        FileInputStream fileInputStream = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        final String urlServer = UPLOAD_VIDEO;
        try {
            final File file = new File(video.getLocalMediaPath());
            fileInputStream = new FileInputStream(file);

            final int bufferSize = 1024 * 1024;// 1024
            final int totalPart = ((int) file.length() / bufferSize) + ((file.length() % bufferSize) > 0 ? 1 : 0);

            final String md5hash = Util.getMD5Hash(fileInputStream);
            final String url = urlServer;
            final String json = Backend.getVideoPartJson(video, md5hash, totalPart, count);
            int hitCount = 3;
            while (hitCount > 0) {
                final String response = connection.httpPOST(url, json, null, null);
                result = Parser.parseUploadJson(response, file);// parseUploadJson
                if ((result != null) && (result instanceof Playback)) {
                    break;
                }
                hitCount--;
            }
        } catch (final FileNotFoundException exception) {
            LOG.e(exception);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (final IOException exception) {
                    LOG.e(exception);
                }
            }
        }
        return result;
    }

    public static Object videoDelete(final Context context, final JSONObject request) throws JSONException {

        String response = null;
        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        LOG.i(request.getString(Constant.VIDEOID));
        response = connection.httpGet(
                DELETE_VIDEO + request.getString(Constant.USERID) + Constant.SLASH
                        + request.getString(Constant.VIDEOID), null, null);
        if (response != null) {
            result = Boolean.valueOf(Parser.parseUpdateJson(response));
        } else {
            result = Backend.returnErrorResponse();
        }
        return result;
    }

    public static Object videoDislike(final Context context, final JSONObject request) throws JSONException {

        String response = null;
        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        response = connection.httpGet(
                DISLIKE_VIDEO + request.getString(Constant.VIDEOID) + Constant.SLASH
                        + request.getString(Constant.USERID), null, Backend.getBodyParams());
        if (response != null) {
            result = Parser.parsedeleteVideoJson(response);
        } else {
            result = Backend.returnErrorResponse();
        }
        return result;
    }

    public static Object videoFeed(final Context context, final String request, final boolean firstTime,
            final boolean pullToRefresh) throws JSONException {

        String response = null;
        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        Backend.cacheManager = CacheManager.getInstance(VideoPlayerApp.getAppContext());
        final File file = new File(CacheManager.cacheDir + Constant.VIDEO_FEEDS_CACHE);
        if (firstTime || pullToRefresh) {
            if (file.exists() && firstTime) {
                try {
                    response = Backend.cacheManager.readString(Constant.VIDEO_FEEDS_CACHE);
                } catch (final CacheTransactionException exception) {
                    LOG.e(exception);
                }
            } else {
                response = connection.httpPOST(VIDEO_FEED, request, null, null);
            }
        } else {
            response = connection.httpPOST(VIDEO_FEED, request, null, null);
        }
        if (response != null) {
            result = Parser.parseFeed(response);

            if (!(result instanceof ErrorResponse) && (firstTime || pullToRefresh)) {
                if (file.exists() && firstTime) {
                } else {
                    try {
                        Backend.cacheManager.write(response, Constant.VIDEO_FEEDS_CACHE);
                    } catch (final CacheTransactionException exception) {
                        LOG.e(exception);
                    }
                }
            }
        } else {
            result = Backend.returnErrorResponse();
        }
        return result;
    }

    public static Object videoFeedSearch(final Context context, final String request) throws JSONException {

        String response = null;
        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        response = connection.httpPOST(VIDEO_FEED_SEARCH, request, null, null);
        if (response != null) {
            result = Parser.parseFeed(response);
        } else {
            result = Backend.returnErrorResponse();
        }
        return result;
    }

    public static Object videoLike(final Context context, final JSONObject request) throws JSONException {

        String response = null;
        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);

        LOG.i("serviceimpl " + request.getString(Constant.VIDEOID));
        response = connection.httpGet(
                LIKE_VIDEO + request.getString(Constant.VIDEOID) + Constant.SLASH + request.getString(Constant.USERID),
                null, Backend.getBodyParams());
        if (response != null) {
            result = Parser.parsedeleteVideoJson(response);
        } else {
            result = Backend.returnErrorResponse();
        }
        return result;
    }

    public static Object videoViews(final Context context, final String videoId, final String platform,
            final String tagClick, final String userId) {

        LOG.v(Constant.CONNECTING_TO_SERVER);
        Object result = null;
        final HttpConnectionManager connection = new HttpConnectionManager(context);
        final String url = INDIVIDUAL_VIEWS + videoId + Constant.SLASH + platform + Constant.SLASH + tagClick
                + Constant.SLASH + userId;
        LOG.v("url : " + url);
        final String response = connection.httpGet(INDIVIDUAL_VIEWS + videoId + Constant.SLASH + platform
                + Constant.SLASH + tagClick + Constant.SLASH + userId, null, null);
        LOG.v(Constant.RESPONSE + response);
        if (response != null) {
            // obj = Parser.parseReportVideo(response);
        } else {
            result = Backend.returnErrorResponse();
        }

        return result;

    }

    /**
     * Return string format of taglist json array
     *
     * @param tagList
     * @param userId
     */
    private static String getAddTagJson(final List<TagInfo> tagList, final String userId) throws JSONException {

        String result = Constant.EMPTY;
        final String deviceId = android.os.Build.MODEL;
        final JSONObject json = new JSONObject();
        json.put(Constant.UID2, userId);
        json.put(Constant.DEVICE_ID, deviceId);
        final JSONArray tagArray = new JSONArray();
        for (int i = 0; i < tagList.size(); i++) {
            final TagInfo tag = tagList.get(i);
            final JSONObject tagJson = new JSONObject();
            tagJson.put(Constant.VIDEO_ID, tag.getServerVideoId());
            tagJson.put(Constant.CLIENTTAGID, tag.getTagId());
            tagJson.put(Constant.TAG_NAME, tag.getName());
            tagJson.put(Constant.TAG_COLOR, tag.getColor());
            tagJson.put(Constant.COORDINATE_X, tag.getTagX());
            tagJson.put(Constant.COORDINATE_Y, tag.getTagY());
            tagJson.put(Constant.TAG_LINK, tag.getLink());
            tagJson.put(Constant.TAG_DURATION, tag.getDisplayTime());
            tagJson.put(Constant.TAG_FBLINK, tag.getFbId());
            tagJson.put(Constant.TAG_GPLINK, tag.getgPlusId());
            tagJson.put(Constant.TAG_TWLINK, tag.getTwId());
            tagJson.put(Constant.TAG_WTLINK, tag.getTagFuId());
            tagJson.put(Constant.VIDEO_CURRENT_TIME, Util.getTimeString(tag.getVideoPlaybackTime()));
            tagJson.put(VIDEO_RES_X, tag.getVideoResX());
            tagJson.put(VIDEO_RES_Y, tag.getVideoResY());
            tagJson.put(VIDEO_HEIGHT, tag.getVideoHeight());
            tagJson.put(VIDEO_WIDTH, tag.getVideoWidth());
            tagJson.put(SCREEN_RES_X, tag.getScreenResX());
            tagJson.put(SCREEN_RES_Y, tag.getScreenResY());
            tagJson.put(Constant.SCREEN_HEIGHT, tag.getScreenHeight());
            tagJson.put(Constant.SCREEN_WIDTH, tag.getScreenWidth());
            if (tag.getProductCurrency() != null) {
                tagJson.put(Constant.CURRENCY, tag.getProductCurrency());
            }
            if (tag.getProductCurrency() != null) {
                tagJson.put(PRODUCT_CATEGORY, tag.getProductCategory());
            }
            if (tag.getProductDescription() != null) {
                tagJson.put(PRODUCT_DESCRIPTION, tag.getProductDescription());
            }
            if (tag.getProductLink() != null) {
                tagJson.put(PRODUCT_LINK, tag.getProductLink());
            }
            if (tag.getProductName() != null) {
                tagJson.put(PRODUCT_NAME, tag.getProductName());
            }
            if (tag.getProductPrice() != null) {
                tagJson.put(PRODUCT_PRICE, tag.getProductPrice());
            }
            if (tag.getProductSold() != null) {
                tagJson.put(Constant.SOLD, tag.getProductSold());
            }

            tagArray.put(tagJson);
        }
        json.put(Constant.TAGS, tagArray);
        result = json.toString();
        return result;

    }

    private static Map<String, String> getBodyParams() {

        final HashMap<String, String> peraHashMap = new HashMap<String, String>();
        peraHashMap.put(Constant.RESOLUTION, Config.getDeviceResolutionValue());
        return peraHashMap;
    }

    /**
     * Returns string format of given list of tag.
     *
     * @param tagList
     * @param userId
     */
    private static String getUpdateTagJson(final List<TagInfo> tagList, final String userId) throws JSONException {

        String result = Constant.EMPTY;
        final JSONObject json = new JSONObject();
        final JSONArray tagArray = new JSONArray();
        for (int i = 0; i < tagList.size(); i++) {
            final TagInfo tag = tagList.get(i);
            final JSONObject tagJson = new JSONObject();
            tagJson.put(Constant.UID2, userId);
            tagJson.put(Constant.ID, tag.getServertagId());
            tagJson.put(Constant.CLIENTTAGID, tag.getTagId());
            tagJson.put(Constant.TAG_NAME, tag.getName());
            tagJson.put(Constant.VIDEO_ID, tag.getServerVideoId());
            tagJson.put(Constant.TAG_COLOR, tag.getColor());
            tagJson.put(Constant.COORDINATE_X, tag.getTagX());
            tagJson.put(Constant.COORDINATE_Y, tag.getTagY());
            tagJson.put(Constant.TAG_LINK, tag.getLink());
            tagJson.put(Constant.TAG_DURATION, tag.getDisplayTime());
            tagJson.put(Constant.TAG_FBLINK, tag.getFbId());
            tagJson.put(Constant.TAG_GPLINK, tag.getgPlusId());
            tagJson.put(Constant.TAG_TWLINK, tag.getTwId());
            tagJson.put(Constant.TAG_WTLINK, tag.getTagFuId());
            tagJson.put(Constant.VIDEO_CURRENT_TIME, Util.getTimeString(tag.getVideoPlaybackTime()));
            tagJson.put(VIDEO_RES_X, tag.getVideoResX());
            tagJson.put(VIDEO_RES_Y, tag.getVideoResY());
            tagJson.put(VIDEO_HEIGHT, tag.getVideoHeight());
            tagJson.put(VIDEO_WIDTH, tag.getVideoWidth());
            tagJson.put(SCREEN_RES_X, tag.getScreenResX());
            tagJson.put(SCREEN_RES_Y, tag.getScreenResY());
            tagJson.put(Constant.SCREEN_HEIGHT, tag.getScreenHeight());
            tagJson.put(Constant.SCREEN_WIDTH, tag.getScreenWidth());

            if (tag.getProductCurrency() != null) {
                tagJson.put(Constant.CURRENCY, tag.getProductCurrency());
            }
            if (tag.getProductCurrency() != null) {
                tagJson.put(PRODUCT_CATEGORY, tag.getProductCategory());
            }
            if (tag.getProductDescription() != null) {
                tagJson.put(PRODUCT_DESCRIPTION, tag.getProductDescription());
            }
            if (tag.getProductLink() != null) {
                tagJson.put(PRODUCT_LINK, tag.getProductLink());
            }
            if (tag.getProductName() != null) {
                tagJson.put(PRODUCT_NAME, tag.getProductName());
            }
            if (tag.getProductPrice() != null) {
                tagJson.put(PRODUCT_PRICE, tag.getProductPrice());
            }
            if (tag.getProductSold() != null) {
                tagJson.put(Constant.SOLD, tag.getProductSold());
            }

            tagArray.put(tagJson);
        }
        json.put(Constant.TAGS, tagArray);
        result = json.toString();
        return result;

    }

    private static String getVideoPartJson(final VideoInfo uploadvideo, final String checksum, final int totalParts,
            final int count) throws JSONException {

        final JSONObject result = new JSONObject();
        final JSONObject video = new JSONObject();

        result.put(Constant.VIDEO2, video);

        video.put(Constant.CHECKSUM, checksum);
        video.put(Constant.UID, uploadvideo.getUserid());
        video.put(CLIENT_VIDEO_ID, Config.getCurrentUploadVideoId());
        video.put(Constant.FILE_NAME, uploadvideo.getTitle() + Constant.MP4);
        video.put(TITLE, uploadvideo.getTitle());
        video.put(PUBLIC, uploadvideo.getPublicVideo());
        video.put(UPLOAD_DATE, uploadvideo.getUploadDate());
        video.put(DESCRIPTION, uploadvideo.getDescription());
        video.put(Constant.EXTENSION, uploadvideo.getFileExtension());
        video.put(Constant.UPLOADED_DEVICE, uploadvideo.getUploadedDevice());
        video.put(Constant.TOTAL_COUNT, totalParts);
        video.put(Constant.FRAME_TIME, uploadvideo.getVideoVocerPage());

        if (count != 0) {
            video.put(Constant.HIT_COUNT, count);
        }
        return result.toString();

    }

    private static ErrorResponse returnErrorResponse() {

        final ErrorResponse resp = new ErrorResponse();
        resp.setErrorCode(Constant._100);
        resp.setMessage(Constant.THE_INTERNET_CONNECTION_APPEARS_TO_BE_OFFLINE);
        return resp;
    }

}
