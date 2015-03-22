/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.TagFu.connectivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.TagFu.dto.Comment;
import com.TagFu.dto.ErrorResponse;
import com.TagFu.dto.FacebookUser;
import com.TagFu.dto.Friend;
import com.TagFu.dto.Liked;
import com.TagFu.dto.MoreVideos;
import com.TagFu.dto.MyPage;
import com.TagFu.dto.MyPageDto;
import com.TagFu.dto.Notification;
import com.TagFu.dto.People;
import com.TagFu.dto.Playback;
import com.TagFu.dto.PushNotificationSetting;
import com.TagFu.dto.RecentLikes;
import com.TagFu.dto.SignUpDto;
import com.TagFu.dto.SuggestedUsersDto;
import com.TagFu.dto.TagInfo;
import com.TagFu.dto.TagResponse;
import com.TagFu.dto.Trends;
import com.TagFu.dto.User;
import com.TagFu.dto.VideoDetails;
import com.TagFu.dto.VideoProfile;
import com.TagFu.util.Config;
import com.TagFu.util.Util;

public final class Parser {

    private static final String EMPTY = "";
    private static final String LOGIN_ERROR_PARSING = "login error parsing";
    private static final String _1 = "1";
    private static final String _100 = "100";
    private static final String BIRTHDAY = "birthday";
    private static final String CLIENT_SIDE_TAG_IDS = "clinetsidetag_ids";
    private static final String CLIENT_TAG_ID = "clienttagid";
    private static final String CLIENT_VIDEO_ID = "clientVideoId";
    private static final String CODE = "code";
    private static final String COMENTS = "coments";
    private static final String COMMENT_ID = "comment_id";
    private static final String COMMENT_TEXT = "comment_text";
    private static final String COMMENTS = "comments";
    private static final String COORDINATE_X = "coordinate_x";
    private static final String COORDINATE_Y = "coordinate_y";
    private static final String CREATED_DATE = "created_date";
    private static final String CURRENCY = "currency";
    private static final String DATA = "data";
    private static final String DESCRIPTION = "description";
    private static final String DEVICE_ID = "device_id";
    private static final String EDUCATION = "education";
    private static final String EMPLOYER = "employer";
    private static final String ENABLE_PN = "enable_pn";
    private static final String ERROR = "error";
    private static final String ERROR_CODE = "error_code";
    private static final String FB_SHARE_URL = "fb_share_url";
    private static final String FEEDS = "feeds";
    private static final String FIRST_NAME = "first_name";
    private static final String FOLLOWERS = "followers";
    private static final String FOLLOWING = "following";
    private static final String FRIENDS = "friends";
    private static final String HOMETOWN = "hometown";
    private static final String HTTPS_GRAPH_FACEBOOK_COM = "https://graph.facebook.com/";
    private static final String ID = "id";
    private static final String LIKE_LIST = "likelist";
    private static final String LIKES = "likes";
    private static final String LOCATION = "location";
    private static final String MENTIONS = "mentions";
    private static final String MESSAGE = "message";
    private static final String MORE_VIDEOS = "more_videos";
    private static final String MSG = "msg";
    private static final String MY_OTHER_STUFF = "myotherstuff";
    private static final String NAME = "name";
    private static final String NO_FEEDS_AVAILABLE = "No feeds available.";
    private static final String NO_PEOPLE_AVAILABLE = "No people available.";
    private static final String NO_VIDEOS = "No Videos";
    private static final String NOTIFICATIONS = "notifications";
    private static final String PEOPLE = "people";
    private static final String PHOTO_PATH = "photo_path";
    private static final String PICTURE = "/picture";
    private static final String PRIVATE_GROUP = "pvtgroup";
    private static final String PRODUCT_CATEGORY = "productCategory";
    private static final String PRODUCT_DESCRIPTION = "productDescription";
    private static final String PRODUCT_LINK = "productLink";
    private static final String PRODUCT_NAME = "productName";
    private static final String PRODUCT_PRICE = "productPrice";
    private static final String PUBLIC = "public";
    private static final String RECENT_COMMENTS = "recent_comments";
    private static final String RECENT_LIKED_BY = "recent_liked_by";
    private static final String RESULT = "result";
    private static final String SCHOOL = "school";
    private static final String SHARE_URL = "share_url";
    private static final String SOLD = "sold";
    private static final String SUGGESTED_USERS = "suggested_users";
    private static final String TAG_COLOR = "tag_color";
    private static final String TAG_DURATION = "tag_duration";
    private static final String TAG_FBLINK = "tag_fblink";
    private static final String TAG_GPLINK = "tag_gplink";
    private static final String TAG_ID = "tag_id";
    private static final String TAG_LINK = "tag_link";
    private static final String TAG_NAME = "tag_name";
    private static final String TAG_TWLINK = "tag_twlink";
    private static final String TAG_USER_COMMENT = "tag_user_comment";
    private static final String TAG_WTLINK = "tag_wtlink";
    private static final String TAGS = "tags";
    private static final String TITLE = "title";
    private static final String TRENDS = "trends";
    private static final String TYPE = "type";
    private static final String UID = "uid";
    private static final String UPDATED_TIME = "updated_time";
    private static final String USER = "user";
    private static final String USER_ID = "user_id";
    private static final String USER_NAME = "user_name";
    private static final String USER_PHOTO = "user_photo";
    private static final String USERNAME = "username";
    private static final String VIDEO = "video";
    private static final String VIDEO_CURRENT_TIME = "video_current_time";
    private static final String VIDEO_ID = "video_id";
    private static final String VIDEO_THUMB_PATH = "video_thumb_path";
    private static final String VIDEO_URL = "video_url";
    private static final String VIDEOS = "videos";
    private static final String TagFu_FRIENDS = "TagFufriends";
    private static final String TagFu_ID = "TagFu_id";
    private static final String WORK = "work";
    private static final String YES = "yes";

    private static final Logger LOG = LoggerManager.getLogger();
    private final Context context;

    public Parser(final Context context) {

        this.context = context;
    }

    public static Object atUsers(final String response) throws JSONException {

        Object result = null;
        List<Friend> friendList = null;
        int errorcode = -1;
        final JSONObject json = new JSONObject(response);
        if (json.has(ERROR_CODE)) {
            errorcode = json.getInt(ERROR_CODE);
            if (errorcode == 0) {
                if (json.has(TAG_USER_COMMENT)) {
                    friendList = new ArrayList<Friend>();
                    final JSONArray suggestedUsersArray = json.getJSONArray(TAG_USER_COMMENT);
                    if ((suggestedUsersArray != null) && (suggestedUsersArray.length() > 0)) {
                        for (int users = 0; users < suggestedUsersArray.length(); users++) {
                            final Friend user = new Friend();
                            final JSONObject obj = suggestedUsersArray.getJSONObject(users);
                            if (obj.has(USER_ID)) {
                                user.setFriendID(obj.getString(USER_ID));
                            }
                            if (obj.has(USER_NAME)) {
                                final String s = obj.getString(USER_NAME);
                                // s=s.substring(1, s.length()-1);
                                user.setFriendName(s);// obj.getString("user_name")

                            }
                            if (obj.has(PHOTO_PATH)) {
                                user.setFriendImage(obj.getString(PHOTO_PATH));
                            }
                            friendList.add(user);
                        }
                    }
                }

            } else {
                final ErrorResponse error = new ErrorResponse();
                error.setErrorCode(String.valueOf(errorcode));
                if (json.has(MSG)) {
                    error.setMessage(json.getString(MSG));
                }
                result = error;
            }

            result = friendList;
        }

        return result;
    }

    public static Object myPage(final String response) throws JSONException {

        Object result = null;
        List<MyPageDto> myvideos = null;
        final MyPage myPageReponse = new MyPage();
        int errorcode = -1;

        final JSONObject json = new JSONObject(response);
        if (json.has(ERROR)) {
            final ErrorResponse errorResponse = new ErrorResponse();
            final JSONObject errorObj = json.getJSONObject(ERROR);
            if (errorObj.has(CODE)) {
                errorResponse.setErrorCode(errorObj.getString(CODE));
                errorResponse.setMessage(errorObj.getString(MESSAGE));
            }
            result = errorResponse;
        } else if (json.has(ERROR_CODE)) {
            errorcode = json.getInt(ERROR_CODE);
            if (errorcode == 0) {
                if (json.has(USER)) {
                    final JSONObject obj = json.getJSONObject(USER);
                    myPageReponse.load(obj);

                    if (obj.has(VIDEOS)) {
                        myvideos = new ArrayList<MyPageDto>();
                        final JSONArray videosArray = obj.getJSONArray(VIDEOS);
                        if ((videosArray != null) && (videosArray.length() > 0)) {
                            for (int i = 0; i < videosArray.length(); i++) {
                                final MyPageDto videos = new MyPageDto();
                                final JSONObject videoObj = videosArray.getJSONObject(i);
                                videos.load(videoObj);
                                if (videoObj.has(RECENT_LIKED_BY)) {
                                    final List<RecentLikes> videoRecentLikes = new ArrayList<RecentLikes>();
                                    final JSONArray likesArray = videoObj.getJSONArray(RECENT_LIKED_BY);
                                    for (int likes = 0; likes < likesArray.length(); likes++) {
                                        final RecentLikes like = new RecentLikes();
                                        like.load(likesArray.getJSONObject(likes));
                                        videoRecentLikes.add(like);
                                    }
                                    RecentLikes loggedInuserLike = null;
                                    for (int temp = 0; temp < videoRecentLikes.size(); temp++) {
                                        final RecentLikes like = videoRecentLikes.get(temp);
                                        if (Config.getUserId().equalsIgnoreCase(like.getUserId())) {
                                            loggedInuserLike = like;
                                            videoRecentLikes.remove(temp);
                                            break;
                                        }

                                    }
                                    if (loggedInuserLike != null) {
                                        videoRecentLikes.add(0, loggedInuserLike);
                                    }
                                    videos.setRecentLikedBy(videoRecentLikes);
                                }
                                if (videoObj.has(RECENT_COMMENTS)) {
                                    final List<Comment> videocomments = new ArrayList<Comment>();
                                    final JSONArray commentArray = videoObj.getJSONArray(RECENT_COMMENTS);
                                    for (int comment = 0; comment < commentArray.length(); comment++) {
                                        final Comment coment = new Comment();
                                        coment.load(commentArray.getJSONObject(comment));
                                        videocomments.add(coment);
                                    }

                                    videos.setRecentComments(videocomments);
                                }

                                myvideos.add(videos);
                            }
                        }
                    }
                    if (obj.has(MORE_VIDEOS)) {
                        final List<MoreVideos> moreVideos = new ArrayList<MoreVideos>();
                        final JSONArray morevideosArray = obj.getJSONArray(MORE_VIDEOS);
                        if (moreVideos != null) {
                            for (int morevideos = 0; morevideos < morevideosArray.length(); morevideos++) {
                                final MoreVideos video = new MoreVideos();
                                video.load(morevideosArray.getJSONObject(morevideos));
                                moreVideos.add(video);
                            }
                        }
                        myPageReponse.setMoreVideos(moreVideos);
                    }
                    if (obj.has(SUGGESTED_USERS)) {
                        final List<SuggestedUsersDto> suggestedUsers = new ArrayList<SuggestedUsersDto>();
                        final JSONArray suggestedUsersArray = obj.getJSONArray(SUGGESTED_USERS);
                        if (suggestedUsersArray != null) {
                            for (int users = 0; users < suggestedUsersArray.length(); users++) {
                                final SuggestedUsersDto user = new SuggestedUsersDto();
                                user.load(suggestedUsersArray.getJSONObject(users));
                                suggestedUsers.add(user);
                            }
                        }
                        myPageReponse.setSuggestedUsers(suggestedUsers);
                    }
                    myPageReponse.setVideoList(myvideos);
                }
                result = myPageReponse;
            } else {
                final ErrorResponse error = new ErrorResponse();
                error.setErrorCode(String.valueOf(errorcode));
                if (json.has(MSG)) {
                    error.setMessage(json.getString(MSG));
                }
                result = error;
            }
        }

        return result;
    }

    public static Object myPageVideos(final String response) throws JSONException {

        Object result = null;
        List<MyPageDto> myvideos = null;
        int errorcode = -1;
        final JSONObject json = new JSONObject(response);
        if (json.has(ERROR_CODE)) {
            errorcode = json.getInt(ERROR_CODE);
            if (errorcode == 0) {
                if (json.has(VIDEOS)) {
                    myvideos = new ArrayList<MyPageDto>();
                    final JSONArray videosArray = json.getJSONArray(VIDEOS);
                    if ((videosArray != null) && (videosArray.length() > 0)) {
                        for (int i = 0; i < videosArray.length(); i++) {
                            final MyPageDto videos = new MyPageDto();
                            final JSONObject videoObj = videosArray.getJSONObject(i);
                            videos.load(videoObj);
                            if (videoObj.has(RECENT_LIKED_BY)) {
                                final List<RecentLikes> videoRecentLikes = new ArrayList<RecentLikes>();
                                final JSONArray likesArray = videoObj.getJSONArray(RECENT_LIKED_BY);
                                for (int likes = 0; likes < likesArray.length(); likes++) {
                                    final RecentLikes like = new RecentLikes();
                                    like.load(likesArray.getJSONObject(likes));
                                    videoRecentLikes.add(like);
                                }

                                RecentLikes loggedInuserLike = null;
                                for (int temp = 0; temp < videoRecentLikes.size(); temp++) {
                                    final RecentLikes like = videoRecentLikes.get(temp);
                                    if (Config.getUserId().equalsIgnoreCase(like.getUserId())) {
                                        loggedInuserLike = like;
                                        videoRecentLikes.remove(temp);
                                        break;
                                    }

                                }
                                if (loggedInuserLike != null) {
                                    videoRecentLikes.add(0, loggedInuserLike);
                                }

                                videos.setRecentLikedBy(videoRecentLikes);
                            }
                            if (videoObj.has(RECENT_COMMENTS)) {
                                final List<Comment> videocomments = new ArrayList<Comment>();
                                final JSONArray commentArray = videoObj.getJSONArray(RECENT_COMMENTS);
                                for (int comment = 0; comment < commentArray.length(); comment++) {
                                    final Comment coment = new Comment();
                                    coment.load(commentArray.getJSONObject(comment));
                                    videocomments.add(coment);
                                }
                                videos.setRecentComments(videocomments);
                            }

                            myvideos.add(videos);
                        }
                    }
                } else {
                    final ErrorResponse error = new ErrorResponse();
                    error.setErrorCode(_100);
                    error.setMessage(json.getString(NO_VIDEOS));
                    result = error;
                }
                result = myvideos;
            } else {
                final ErrorResponse error = new ErrorResponse();
                error.setErrorCode(String.valueOf(errorcode));

                if (json.has(MSG)) {
                    error.setMessage(json.getString(MSG));
                }
                result = error;
            }
        }

        return result;
    }

    public static Object myTrendVideos(final String response) throws JSONException {

        Object result = null;
        List<MyPageDto> myvideos = null;
        int errorcode = -1;
        final JSONObject json = new JSONObject(response);
        if (json.has(ERROR)) {
            final ErrorResponse errorResponse = new ErrorResponse();
            final JSONObject errorObj = json.getJSONObject(ERROR);
            if (errorObj.has(CODE)) {
                errorResponse.setErrorCode(errorObj.getString(CODE));
                errorResponse.setMessage(errorObj.getString(MESSAGE));
            }
            result = errorResponse;
        } else if (json.has(ERROR_CODE)) {
            errorcode = json.getInt(ERROR_CODE);
            if (errorcode == 0) {
                if (json.has(TRENDS)) {
                    myvideos = new ArrayList<MyPageDto>();
                    final JSONArray videosArray = json.getJSONArray(TRENDS);
                    if ((videosArray != null) && (videosArray.length() > 0)) {
                        for (int i = 0; i < videosArray.length(); i++) {
                            final MyPageDto videos = new MyPageDto();
                            final JSONObject videoObj = videosArray.getJSONObject(i);
                            videos.load(videoObj);
                            if (videoObj.has(RECENT_LIKED_BY)) {
                                final List<RecentLikes> videoRecentLikes = new ArrayList<RecentLikes>();
                                final JSONArray likesArray = videoObj.getJSONArray(RECENT_LIKED_BY);
                                for (int likes = 0; likes < likesArray.length(); likes++) {
                                    final RecentLikes like = new RecentLikes();
                                    like.load(likesArray.getJSONObject(likes));
                                    videoRecentLikes.add(like);
                                }

                                RecentLikes loggedInuserLike = null;
                                for (int temp = 0; temp < videoRecentLikes.size(); temp++) {
                                    final RecentLikes like = videoRecentLikes.get(temp);
                                    if (Config.getUserId().equalsIgnoreCase(like.getUserId())) {
                                        loggedInuserLike = like;
                                        videoRecentLikes.remove(temp);
                                        break;
                                    }

                                }
                                if (loggedInuserLike != null) {
                                    videoRecentLikes.add(0, loggedInuserLike);
                                }

                                videos.setRecentLikedBy(videoRecentLikes);
                            }
                            if (videoObj.has(RECENT_COMMENTS)) {
                                final List<Comment> videocomments = new ArrayList<Comment>();
                                final JSONArray commentArray = videoObj.getJSONArray(RECENT_COMMENTS);
                                for (int comment = 0; comment < commentArray.length(); comment++) {
                                    final Comment coment = new Comment();
                                    coment.load(commentArray.getJSONObject(comment));
                                    videocomments.add(coment);
                                }
                                videos.setRecentComments(videocomments);
                            }

                            myvideos.add(videos);
                        }
                    }
                } else {
                    final ErrorResponse error = new ErrorResponse();
                    error.setErrorCode(_100);
                    error.setMessage(json.getString(NO_VIDEOS));
                    result = error;
                }
                result = myvideos;
            } else {
                final ErrorResponse error = new ErrorResponse();
                error.setErrorCode(String.valueOf(errorcode));

                if (json.has(MSG)) {
                    error.setMessage(json.getString(MSG));
                }
                result = error;
            }
        }

        return result;
    }

    public static Object parseBuyProduct(final String response) throws JSONException {

        Object result = null;
        final JSONObject json = new JSONObject(response);
        if (json.has(ERROR_CODE)) {
            final int errorcode = json.getInt(ERROR_CODE);
            if (errorcode == 0) {
                result = Boolean.TRUE;
            } else {
                final ErrorResponse error = new ErrorResponse();
                error.setErrorCode(String.valueOf(errorcode));

                if (json.has(MSG)) {
                    error.setMessage(json.getString(MSG));
                }
                result = error;
            }
        }

        return result;
    }

    public static Object parseCheckFollowersResponse(final String response) throws JSONException {

        int errorcode = -1;
        Object result = null;
        final JSONObject json = new JSONObject(response);
        if (json.has(ERROR)) {
            final ErrorResponse errorResponse = new ErrorResponse();
            final JSONObject errorObj = json.getJSONObject(ERROR);
            if (errorObj.has(CODE)) {
                errorResponse.setErrorCode(errorObj.getString(CODE));
                errorResponse.setMessage(errorObj.getString(MESSAGE));
            }
            result = errorResponse;
        } else if (json.has(ERROR_CODE)) {
            errorcode = json.getInt(ERROR_CODE);
            if (errorcode == 0) {
                if (json.has(FOLLOWING)) {
                    if (YES.equalsIgnoreCase(json.getString(FOLLOWING))) {
                        result = Boolean.TRUE;
                    }
                } else {
                    result = Boolean.FALSE;
                }

            } else {
                final ErrorResponse resp = new ErrorResponse();
                resp.setErrorCode(String.valueOf(json.getInt(ERROR_CODE)));
                if (json.has(MSG)) {
                    resp.setMessage(json.getString(MSG));
                }
                result = resp;
            }
        }

        return result;
    }

    public static Object parseCheckPvtGrpResponse(final String response) throws JSONException {

        int errorcode = -1;
        Object result = null;
        final JSONObject json = new JSONObject(response);
        if (json.has(ERROR)) {
            final ErrorResponse errorResponse = new ErrorResponse();
            final JSONObject errorObj = json.getJSONObject(ERROR);
            if (errorObj.has(CODE)) {
                errorResponse.setErrorCode(errorObj.getString(CODE));
                errorResponse.setMessage(errorObj.getString(MESSAGE));
            }
            result = errorResponse;
        } else if (json.has(ERROR_CODE)) {
            errorcode = json.getInt(ERROR_CODE);
            if (errorcode == 0) {
                if (json.has(FOLLOWING)) {
                    if (YES.equalsIgnoreCase(json.getString(FOLLOWING))) {
                        result = Boolean.TRUE;
                    }
                } else {
                    result = Boolean.FALSE;
                }
            } else {
                final ErrorResponse resp = new ErrorResponse();
                resp.setErrorCode(String.valueOf(json.getInt(ERROR_CODE)));
                if (json.has(MSG)) {
                    resp.setMessage(json.getString(MSG));
                }
                result = resp;
            }
        }

        return result;
    }

    public static Object parseComment(final String response) throws JSONException {

        int errorcode = -1;
        Object returnObject = null;
        Comment comment = null;
        final JSONObject json = new JSONObject(response);
        if (json.has(ERROR_CODE)) {
            errorcode = json.getInt(ERROR_CODE);
            if (errorcode == 0) {
                comment = new Comment();

                if (json.has(COMMENT_ID)) {
                    comment.setCommentId(json.getString(COMMENT_ID));
                }
                if (json.has(ID)) {
                    comment.setUserId(json.getString(ID));
                }
                if (json.has(NAME)) {
                    comment.setUserName(json.getString(NAME));
                }
                if (json.has(PHOTO_PATH)) {
                    comment.setUserPicUrl(json.getString(PHOTO_PATH));
                }
                if (json.has(COMMENT_TEXT)) {
                    comment.setComment(json.getString(COMMENT_TEXT));
                }
                returnObject = comment;
            } else {
                final ErrorResponse error = new ErrorResponse();
                error.setErrorCode(String.valueOf(errorcode));

                if (json.has(MSG)) {
                    error.setMessage(json.getString(MSG));
                }
                returnObject = error;
            }
        }

        return returnObject;
    }

    public static Object parsedeleteVideoJson(final String videoResponse) throws JSONException {

        int errorcode = -1;
        Object result = null;
        final JSONObject json = new JSONObject(videoResponse);
        if (json.has(ERROR_CODE)) {
            errorcode = json.getInt(ERROR_CODE);
        }
        if (errorcode == 0) {
            return Boolean.TRUE;
        }
        final ErrorResponse response = new ErrorResponse();
        if (json.has(MSG)) {
            response.setErrorCode(String.valueOf(errorcode));
            response.setMessage(json.getString(MSG));
        }
        result = response;
        return result;
    }

    public static Object parseDescribeProduct(final String response) throws JSONException {

        Object result = null;
        final JSONObject json = new JSONObject(response);
        if (json.has(ERROR_CODE)) {
            final int errorcode = json.getInt(ERROR_CODE);
            if (errorcode == 0) {
                result = Boolean.TRUE;
            } else {
                final ErrorResponse error = new ErrorResponse();
                error.setErrorCode(String.valueOf(errorcode));

                if (json.has(MSG)) {
                    error.setMessage(json.getString(MSG));
                }
                result = error;
            }
        }

        return result;
    }

    public static Object parseFeed(final String videoResponse) throws JSONException {

        List<MyPageDto> videos = null;
        Object result = null;
        int errorcode = -1;
        final JSONObject json = new JSONObject(videoResponse);
        if (json.has(ERROR)) {
            final ErrorResponse errorResponse = new ErrorResponse();
            final JSONObject errorObj = json.getJSONObject(ERROR);
            if (errorObj.has(CODE)) {
                errorResponse.setErrorCode(errorObj.getString(CODE));
                errorResponse.setMessage(errorObj.getString(MESSAGE));
            }
            result = errorResponse;
        } else if (json.has(ERROR_CODE)) {
            errorcode = json.getInt(ERROR_CODE);
            if (errorcode == 0) {
                LOG.i("video feed parser ");
                if (json.has(VIDEOS)) {
                    final JSONArray videoArray = json.getJSONArray(VIDEOS);
                    videos = new ArrayList<MyPageDto>();
                    for (int i = 0; i < videoArray.length(); i++) {
                        final MyPageDto videoObj = new MyPageDto();
                        final JSONObject video = videoArray.getJSONObject(i);
                        videoObj.load(video);
                        if (video.has(RECENT_LIKED_BY)) {
                            final List<RecentLikes> videoRecentLikes = new ArrayList<RecentLikes>();
                            final JSONArray likesArray = video.getJSONArray(RECENT_LIKED_BY);
                            for (int likes = 0; likes < likesArray.length(); likes++) {
                                final RecentLikes like = new RecentLikes();
                                like.load(likesArray.getJSONObject(likes));
                                videoRecentLikes.add(like);
                            }

                            RecentLikes loggedInuserLike = null;
                            for (int temp = 0; temp < videoRecentLikes.size(); temp++) {
                                final RecentLikes like = videoRecentLikes.get(temp);
                                if (Config.getUserId().equalsIgnoreCase(like.getUserId())) {
                                    loggedInuserLike = like;
                                    videoRecentLikes.remove(temp);
                                    break;
                                }

                            }
                            if (loggedInuserLike != null) {
                                videoRecentLikes.add(0, loggedInuserLike);
                            }

                            videoObj.setRecentLikedBy(videoRecentLikes);
                        }
                        if (video.has(RECENT_COMMENTS)) {
                            final List<Comment> videocomments = new ArrayList<Comment>();
                            final JSONArray commentArray = video.getJSONArray(RECENT_COMMENTS);
                            for (int comment = 0; comment < commentArray.length(); comment++) {
                                final Comment coment = new Comment();
                                coment.load(commentArray.getJSONObject(comment));
                                videocomments.add(coment);
                            }
                            videoObj.setRecentComments(videocomments);
                        }
                        videos.add(videoObj);
                    }
                    result = videos;
                } else {
                    final ErrorResponse response = new ErrorResponse();
                    response.setErrorCode(_1);
                    response.setMessage(NO_FEEDS_AVAILABLE);
                    result = response;
                }
            } else {
                final ErrorResponse response = new ErrorResponse();
                response.setErrorCode(String.valueOf(json.getInt(ERROR_CODE)));
                if (json.has(MSG)) {
                    response.setMessage(json.getString(MSG));
                }
                result = response;
            }
        }

        return result;
    }

    public static boolean parseFileUploadResponse(final String reponse) throws JSONException {

        int errorcode = -1;
        boolean status = false;
        final JSONObject json = new JSONObject(reponse);
        if (json.has(ERROR_CODE)) {
            errorcode = json.getInt(ERROR_CODE);
            if (errorcode == 0) {
                status = true;
            }
        }

        return status;
    }

    public static FacebookUser parseFriendInfo(final Context context, final String friendResponse) throws JSONException {

        final FacebookUser info = new FacebookUser();

        JSONObject friends = null;
        friends = new JSONObject(friendResponse);
        if (friends.has(WORK)) {
            final JSONArray workinfo = friends.getJSONArray(WORK);
            if (workinfo.length() > 0) {
                final String workPlaces[] = new String[workinfo.length()];
                for (int i = 0; i < workinfo.length(); i++) {
                    final JSONObject work = workinfo.getJSONObject(i);
                    if (work.has(EMPLOYER)) {
                        final JSONObject employerObj = work.getJSONObject(EMPLOYER);
                        String companyname = EMPTY;
                        if (employerObj.has(NAME)) {
                            companyname = employerObj.getString(NAME);
                        }
                        workPlaces[i] = companyname;
                    }

                }
                info.setEmployer(workPlaces);
            }
        }
        if (friends.has(EDUCATION)) {
            final JSONArray educationinfo = friends.getJSONArray(EDUCATION);
            if (educationinfo.length() > 0) {
                final String educationPlaces[] = new String[educationinfo.length()];
                for (int i = 0; i < educationinfo.length(); i++) {
                    final JSONObject education = educationinfo.getJSONObject(i);
                    if (education.has(TYPE) && education.has(SCHOOL)) {
                        final JSONObject schoolObj = education.getJSONObject(SCHOOL);
                        String schoolname = EMPTY;
                        if (schoolObj.has(NAME)) {
                            schoolname = schoolObj.getString(NAME);
                        }
                        educationPlaces[i] = schoolname;
                    }

                }
                info.setEducation(educationPlaces);
            }
        }
        if (friends.has(NAME)) {
            info.setUserName(friends.getString(NAME));
        }
        if (friends.has(ID)) {
            final String id = friends.getString(ID);
            info.setId(id);
            info.setProfilePick(HTTPS_GRAPH_FACEBOOK_COM + id + PICTURE);
        }
        if (friends.has(HOMETOWN)) {
            final JSONObject hometown = friends.getJSONObject(HOMETOWN);
            if (hometown.has(NAME)) {
                info.setFromPlace(hometown.getString(NAME));
            }
        }
        if (friends.has(LOCATION)) {
            final JSONObject location = friends.getJSONObject(LOCATION);
            if (location.has(NAME)) {
                info.setCurrentPlace(location.getString(NAME));
            }
        }
        if (friends.has(BIRTHDAY)) {
            info.setBirthDay(friends.getString(BIRTHDAY));
        }
        if (friends.has(UPDATED_TIME)) {
            info.setLastUpdate(Util.getLocalDateFromUTC(friends.getString(UPDATED_TIME)));
        }
        return info;
    }

    public static List<Friend> parseFriendList(final Context context, final String friendResponse) throws JSONException {

        final List<Friend> friendList = new ArrayList<Friend>();

        JSONObject friends = null;
        friends = new JSONObject(friendResponse);
        if (friends.has(DATA)) {
            final JSONArray friendArray = friends.getJSONArray(DATA);
            if (friendArray.length() > 0) {
                for (int i = 0; i < friendArray.length(); i++) {
                    final Friend friend = new Friend();
                    final JSONObject friendArrayJSON = friendArray.getJSONObject(i);
                    if (friendArrayJSON.has(ID)) {
                        final String id = friendArrayJSON.getString(ID);
                        friend.setFriendID(id);
                        friend.setFriendImage(HTTPS_GRAPH_FACEBOOK_COM + id + PICTURE);
                        friend.setNext(true);
                    }
                    if (friendArrayJSON.has(FIRST_NAME)) {
                        friend.setFriendName(friendArrayJSON.getString(FIRST_NAME));
                    }
                    if (friendArrayJSON.has(LOCATION)) {
                        final JSONObject locationObj = friendArrayJSON.getJSONObject(LOCATION);
                        if (locationObj.has(NAME)) {
                            friend.setLocation(locationObj.getString(NAME));
                        }
                    }

                    friendList.add(friend);
                }
            }
        }

        return friendList;
    }

    public static String parseLoginResponse(final String loginResponse) throws JSONException {

        int errorcode = -1;
        String userId = null;
        final JSONObject json = new JSONObject(loginResponse);

        if (json.has(RESULT)) {
            final JSONObject result = json.getJSONObject(RESULT);
            if (result.has(ERROR_CODE)) {
                errorcode = json.getInt(ERROR_CODE);
                if ((errorcode == 0) && json.has(USER_ID)) {
                    userId = json.getString(USER_ID);
                }
            }

        }
        return userId;
    }

    public static Object parseLovedPeopleJson(final String response) throws JSONException {

        List<Liked> totalLikes = null;
        Object result = null;
        int errorcode = -1;
        final JSONObject json = new JSONObject(response);
        if (json.has(ERROR)) {
            final ErrorResponse errorResponse = new ErrorResponse();
            final JSONObject errorObj = json.getJSONObject(ERROR);
            if (errorObj.has(CODE)) {
                errorResponse.setErrorCode(errorObj.getString(CODE));
                errorResponse.setMessage(errorObj.getString(MESSAGE));
            }
            result = errorResponse;
        } else if (json.has(ERROR_CODE)) {
            errorcode = json.getInt(ERROR_CODE);
            if (errorcode == 0) {
                if (json.has(LIKE_LIST)) {
                    totalLikes = new ArrayList<Liked>();
                    final JSONArray lovedPeoplearray = json.getJSONArray(LIKE_LIST);
                    if ((lovedPeoplearray != null) && (lovedPeoplearray.length() > 0)) {
                        for (int i = 0; i < lovedPeoplearray.length(); i++) {
                            final Liked likes = new Liked();
                            likes.load(lovedPeoplearray.getJSONObject(i));
                            totalLikes.add(likes);
                        }
                    }
                }
                result = totalLikes;
            } else {
                final ErrorResponse resp = new ErrorResponse();
                resp.setErrorCode(String.valueOf(json.getInt(ERROR_CODE)));
                if (json.has(MSG)) {
                    resp.setMessage(json.getString(MSG));
                }
                result = resp;
            }
        }

        return result;
    }

    public static Object parseMyAccountResponseJson(final String response) throws JSONException {

        int errorcode = -1;
        Object returnObject = null;
        final JSONObject json = new JSONObject(response);
        if (json.has(ERROR)) {
            final ErrorResponse errorResponse = new ErrorResponse();
            final JSONObject errorObj = json.getJSONObject(ERROR);
            if (errorObj.has(CODE)) {
                errorResponse.setErrorCode(errorObj.getString(CODE));
                errorResponse.setMessage(errorObj.getString(MESSAGE));
            }
            returnObject = errorResponse;
        } else if (json.has(ERROR_CODE)) {
            errorcode = json.getInt(ERROR_CODE);
            if (errorcode == 0) {
                User userProfile = null;
                if (json.has(USER)) {
                    final JSONObject userObj = json.getJSONObject(USER);
                    userProfile = new User();
                    userProfile.load(userObj);
                }
                returnObject = userProfile;
            } else {
                final ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setErrorCode(String.valueOf(errorcode));
                if (json.has(MSG)) {
                    errorResponse.setMessage(json.getString(MSG));
                }
                returnObject = errorResponse;
            }
        }

        return returnObject;
    }

    public static List<VideoProfile> parseMyPagePaginationVideosResponseJson(final String response)
            throws JSONException {

        List<VideoProfile> myvideos = new ArrayList<VideoProfile>();
        int errorcode = -1;

        final JSONObject json = new JSONObject(response);
        if (json.has(ERROR_CODE)) {
            errorcode = json.getInt(ERROR_CODE);
            if (errorcode == 0) {
                if (json.has(VIDEOS)) {
                    final JSONArray videosArray = json.getJSONArray(VIDEOS);
                    for (int i = 0; i < videosArray.length(); i++) {
                        final VideoProfile videoprofile = new VideoProfile();
                        videoprofile.load(videosArray.getJSONObject(i));
                        myvideos.add(videoprofile);
                    }
                }
            } else {
                myvideos = null;
            }
        }

        return myvideos;
    }

    public static Object parseNotifications(final String response) throws JSONException {

        Object result = null;
        final List<Notification> list = new ArrayList<Notification>();
        int errorcode = -1;
        final JSONObject json = new JSONObject(response);
        if (json.has(ERROR)) {
            final ErrorResponse errorResponse = new ErrorResponse();
            final JSONObject errorObj = json.getJSONObject(ERROR);
            if (errorObj.has(CODE)) {
                errorResponse.setErrorCode(errorObj.getString(CODE));
                errorResponse.setMessage(errorObj.getString(MESSAGE));
            }
            result = errorResponse;
        } else if (json.has(ERROR_CODE)) {
            errorcode = json.getInt(ERROR_CODE);
            if (errorcode == 0) {
                if (json.has(NOTIFICATIONS)) {
                    final JSONArray jArray = json.getJSONArray(NOTIFICATIONS);
                    for (int i = 0; i < jArray.length(); i++) {
                        final JSONObject obj = jArray.getJSONObject(i);
                        final Notification notification = new Notification();
                        notification.load(obj);
                        boolean notExpired = true;
                        if (obj.has(CREATED_DATE)) {
                            notExpired = Util.getNotificationLocalTime(obj.getString(CREATED_DATE));
                        }
                        if (notExpired) {
                            list.add(notification);
                        }
                    }
                    result = list;
                }
            } else {
                final ErrorResponse resp = new ErrorResponse();
                resp.setErrorCode(String.valueOf(errorcode));
                if (json.has(MSG)) {
                    resp.setMessage(json.getString(MSG));
                }
                result = resp;
            }
        }

        return result;
    }

    public static Object parseNotificationSettingsResponseJson(final String response) throws JSONException {

        int errorcode = -1;
        Object returnObject = null;
        final JSONObject json = new JSONObject(response);
        if (json.has(ERROR)) {
            final ErrorResponse errorResponse = new ErrorResponse();
            final JSONObject errorObj = json.getJSONObject(ERROR);
            if (errorObj.has(CODE)) {
                errorResponse.setErrorCode(errorObj.getString(CODE));
                errorResponse.setMessage(errorObj.getString(MESSAGE));
            }
            returnObject = errorResponse;
        } else if (json.has(ERROR_CODE)) {
            errorcode = json.getInt(ERROR_CODE);
            if (errorcode == 0) {
                final PushNotificationSetting notificationSettings = new PushNotificationSetting();
                if (json.has(ENABLE_PN)) {
                    notificationSettings.setEnablePn(json.getInt(ENABLE_PN));
                }
                if (json.has(FOLLOWERS)) {
                    notificationSettings.setFollowers(json.getInt(FOLLOWERS));
                }
                if (json.has(COMMENTS)) {
                    notificationSettings.setComments(json.getInt(COMMENTS));
                }
                if (json.has(LIKES)) {
                    notificationSettings.setLikes(json.getInt(LIKES));
                }
                if (json.has(FEEDS)) {
                    notificationSettings.setFeeds(json.getInt(FEEDS));
                }
                if (json.has(MENTIONS)) {
                    notificationSettings.setMentions(json.getInt(MENTIONS));
                }
                returnObject = notificationSettings;
            } else {
                final ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setErrorCode(String.valueOf(errorcode));
                if (json.has(MSG)) {
                    errorResponse.setMessage(json.getString(MSG));
                }
                returnObject = errorResponse;
            }
        }

        return returnObject;
    }

    public static Object parseNotificationVideosDetails(final String response) throws JSONException {

        Object result = null;
        VideoDetails videoprofile = null;
        int errorcode = -1;

        final JSONObject json = new JSONObject(response);
        if (json.has(ERROR)) {
            final ErrorResponse errorResponse = new ErrorResponse();
            final JSONObject errorObj = json.getJSONObject(ERROR);
            if (errorObj.has(CODE)) {
                errorResponse.setErrorCode(errorObj.getString(CODE));
                errorResponse.setMessage(errorObj.getString(MESSAGE));
            }
            result = errorResponse;
        } else if (json.has(ERROR_CODE)) {
            errorcode = json.getInt(ERROR_CODE);
            if (errorcode == 0) {
                if (json.has(VIDEO)) {
                    final JSONObject videosObj = json.getJSONObject(VIDEO);
                    if (videosObj != null) {
                        videoprofile = new VideoDetails();
                        final JSONObject videoObj = videosObj;
                        videoprofile.load(videoObj);
                    }
                }
                if (json.has(COMENTS)) {
                    final List<Comment> list = new ArrayList<Comment>();
                    final JSONArray jArray = json.getJSONArray(COMENTS);
                    for (int i = 0; i < jArray.length(); i++) {
                        final JSONObject obj = jArray.getJSONObject(i);
                        final Comment comment = new Comment();
                        if (obj.has(USER_ID)) {
                            comment.setUserId(obj.getString(USER_ID));
                        }
                        if (obj.has(COMMENT_ID)) {
                            comment.setCommentId(obj.getString(COMMENT_ID));
                        }
                        if (obj.has(USER_NAME)) {
                            comment.setUserName(obj.getString(USER_NAME));
                        }
                        if (obj.has(PHOTO_PATH)) {
                            comment.setUserPicUrl(obj.getString(PHOTO_PATH));
                        }
                        if (obj.has(COMMENT_TEXT)) {
                            comment.setComment(obj.getString(COMMENT_TEXT));
                        }
                        list.add(comment);
                    }
                    videoprofile.setComments(list);
                }
                if (json.has(LIKES)) {
                    final List<Comment> list = new ArrayList<Comment>();
                    final JSONArray jArray = json.getJSONArray(LIKES);
                    for (int i = 0; i < jArray.length(); i++) {
                        final JSONObject obj = jArray.getJSONObject(i);
                        final Comment comment = new Comment();
                        if (obj.has(USER_ID)) {
                            comment.setUserId(obj.getString(USER_ID));
                        }
                        if (obj.has(USER_NAME)) {
                            comment.setUserName(obj.getString(USER_NAME));
                        }
                        if (obj.has(PHOTO_PATH)) {
                            comment.setUserPicUrl(obj.getString(PHOTO_PATH));
                        }
                        list.add(comment);
                    }
                    videoprofile.setLikes(list);
                }
                result = videoprofile;
            } else {
                final ErrorResponse error = new ErrorResponse();
                error.setErrorCode(String.valueOf(errorcode));

                if (json.has(MSG)) {
                    error.setMessage(json.getString(MSG));
                }
                result = error;
            }

        }

        return result;
    }

    public static Object parseOtherStuff(final String response) throws JSONException {

        Object result = null;
        final List<MoreVideos> moreVideos = new ArrayList<MoreVideos>();
        int errorcode = -1;
        final JSONObject json = new JSONObject(response);
        if (json.has(ERROR)) {
            final ErrorResponse errorResponse = new ErrorResponse();
            final JSONObject errorObj = json.getJSONObject(ERROR);
            if (errorObj.has(CODE)) {
                errorResponse.setErrorCode(errorObj.getString(CODE));
                errorResponse.setMessage(errorObj.getString(MESSAGE));
            }
            result = errorResponse;
        } else if (json.has(ERROR_CODE)) {
            errorcode = json.getInt(ERROR_CODE);
            if (errorcode == 0) {
                if (json.has(MY_OTHER_STUFF)) {
                    final JSONArray videosArray = json.getJSONArray(MY_OTHER_STUFF);
                    if ((videosArray != null) && (videosArray.length() > 0)) {
                        for (int i = 0; i < videosArray.length(); i++) {
                            final MoreVideos videos = new MoreVideos();
                            videos.load(videosArray.getJSONObject(i));
                            moreVideos.add(videos);
                        }
                    }
                }
                result = moreVideos;
            } else {
                final ErrorResponse error = new ErrorResponse();
                error.setErrorCode(String.valueOf(errorcode));

                if (json.has(MSG)) {
                    error.setMessage(json.getString(MSG));
                }
                result = error;
            }
        }

        return result;
    }

    public static Object parsePagesResponse(final String response) throws JSONException {

        Object result = null;
        List<Trends> trends = null;
        int errorcode = -1;
        final JSONObject json = new JSONObject(response);
        if (json.has(ERROR)) {
            final ErrorResponse errorResponse = new ErrorResponse();
            final JSONObject errorObj = json.getJSONObject(ERROR);
            if (errorObj.has(CODE)) {
                errorResponse.setErrorCode(errorObj.getString(CODE));
                errorResponse.setMessage(errorObj.getString(MESSAGE));
            }
            result = errorResponse;
        } else if (json.has(ERROR_CODE)) {
            errorcode = json.getInt(ERROR_CODE);
            if (errorcode == 0) {
                if (json.has(TRENDS)) {
                    trends = new ArrayList<Trends>();
                    final JSONArray trendsArray = json.getJSONArray(TRENDS);
                    if ((trendsArray != null) && (trendsArray.length() > 0)) {
                        for (int i = 0; i < trendsArray.length(); i++) {
                            final Trends trend = new Trends();
                            trend.load(trendsArray.getJSONObject(i));
                            trends.add(trend);
                        }
                    }

                }

                result = trends;
            } else {
                final ErrorResponse error = new ErrorResponse();
                error.setErrorCode(String.valueOf(errorcode));

                if (json.has(MSG)) {
                    error.setMessage(json.getString(MSG));
                }
                result = error;
            }
        }

        return result;
    }

    public static Object parsePendingPrivateRequestJson(final String response) throws JSONException {

        List<Liked> totalLikes = null;
        Object result = null;
        int errorcode = -1;
        final JSONObject json = new JSONObject(response);
        if (json.has(ERROR)) {
            final ErrorResponse errorResponse = new ErrorResponse();
            final JSONObject errorObj = json.getJSONObject(ERROR);
            if (errorObj.has(CODE)) {
                errorResponse.setErrorCode(errorObj.getString(CODE));
                errorResponse.setMessage(errorObj.getString(MESSAGE));
            }
            result = errorResponse;
        } else if (json.has(ERROR_CODE)) {
            errorcode = json.getInt(ERROR_CODE);
            if (errorcode == 0) {
                if (json.has(PRIVATE_GROUP)) {
                    totalLikes = new ArrayList<Liked>();
                    final JSONArray lovedPeoplearray = json.getJSONArray(PRIVATE_GROUP);
                    if ((lovedPeoplearray != null) && (lovedPeoplearray.length() > 0)) {
                        for (int i = 0; i < lovedPeoplearray.length(); i++) {
                            final Liked likes = new Liked();
                            likes.load(lovedPeoplearray.getJSONObject(i));
                            totalLikes.add(likes);
                        }
                    }
                }
                result = totalLikes;
            } else {
                final ErrorResponse resp = new ErrorResponse();
                resp.setErrorCode(String.valueOf(json.getInt(ERROR_CODE)));
                if (json.has(MSG)) {
                    resp.setMessage(json.getString(MSG));
                }
                result = resp;
            }
        }

        return result;
    }

    public static Object parsePeopleResponse(final String videoResponse) throws JSONException {

        List<People> people = null;
        Object result = null;
        int errorcode = -1;
        final JSONObject json = new JSONObject(videoResponse);
        if (json.has(ERROR)) {
            final ErrorResponse errorResponse = new ErrorResponse();
            final JSONObject errorObj = json.getJSONObject(ERROR);
            if (errorObj.has(CODE)) {
                errorResponse.setErrorCode(errorObj.getString(CODE));
                errorResponse.setMessage(errorObj.getString(MESSAGE));
            }
            result = errorResponse;
        } else if (json.has(ERROR_CODE)) {
            errorcode = json.getInt(ERROR_CODE);
            if (errorcode == 0) {
                LOG.i("people parser ");
                if (json.has(PEOPLE)) {
                    final JSONArray videoArray = json.getJSONArray(PEOPLE);
                    people = new ArrayList<People>();
                    for (int i = 0; i < videoArray.length(); i++) {
                        final People peopleObj = new People();
                        final JSONObject peopleInfo = videoArray.getJSONObject(i);
                        peopleObj.load(peopleInfo);
                        people.add(peopleObj);
                    }
                    result = people;
                } else {
                    final ErrorResponse response = new ErrorResponse();
                    response.setErrorCode(_1);
                    response.setMessage(NO_PEOPLE_AVAILABLE);
                    result = response;
                }
            } else {
                final ErrorResponse response = new ErrorResponse();
                response.setErrorCode(String.valueOf(json.getInt(ERROR_CODE)));
                if (json.has(MSG)) {
                    response.setMessage(json.getString(MSG));
                }
                result = response;
            }
        }

        return result;
    }

    public static Object parsePlaybackResponseJson(final String videoResponse) throws JSONException {

        List<TagInfo> tags = null;
        Playback playback = null;
        int errorcode = -1;
        Object result = null;
        final JSONObject json = new JSONObject(videoResponse);
        if (json.has(ERROR)) {
            final ErrorResponse errorResponse = new ErrorResponse();
            final JSONObject errorObj = json.getJSONObject(ERROR);
            if (errorObj.has(CODE)) {
                errorResponse.setErrorCode(errorObj.getString(CODE));
                errorResponse.setMessage(errorObj.getString(MESSAGE));
            }
            result = errorResponse;
        } else if (json.has(ERROR_CODE)) {
            errorcode = json.getInt(ERROR_CODE);
            if (errorcode == 0) {
                playback = new Playback();
                if (json.has(VIDEO_ID)) {
                    playback.setVideoId(json.getString(VIDEO_ID));
                }
                if (json.has(UID)) {
                    playback.setUid(json.getString(UID));
                }
                if (json.has(SHARE_URL)) {
                    playback.setShareUrl(json.getString(SHARE_URL));
                }
                if (json.has(FB_SHARE_URL)) {
                    playback.setFbShareUrl(json.getString(FB_SHARE_URL));
                }
                if (json.has(USERNAME)) {
                    playback.setUserName(json.getString(USERNAME));
                }
                if (json.has(USER_PHOTO)) {
                    playback.setUserImagePath(json.getString(USER_PHOTO));
                }
                if (json.has(VIDEO_THUMB_PATH)) {
                    playback.setThumbPath(json.getString(VIDEO_THUMB_PATH));
                }
                if (json.has(PUBLIC)) {
                    playback.setPublicVideo(json.getString(PUBLIC));
                }
                if (json.has(TITLE)) {
                    playback.setVideoTitle(json.getString(TITLE));
                }
                if (json.has(DESCRIPTION)) {
                    playback.setVideoDescription(json.getString(DESCRIPTION));
                }

                if (json.has(VIDEO_URL)) {
                    playback.setVideoUrl(json.getString(VIDEO_URL));
                }
                if (json.has(TAGS)) {
                    final JSONArray tagArray = json.getJSONArray(TAGS);
                    tags = new ArrayList<TagInfo>();
                    for (int i = 0; i < tagArray.length(); i++) {
                        int currentPosition = 0;
                        int displayTime = 0;
                        final TagInfo tagResponse = new TagInfo();
                        final JSONObject tag = tagArray.getJSONObject(i);
                        if (tag.has(ID)) {
                            final int id = Integer.parseInt(tag.getString(ID));
                            tagResponse.setServertagId(id);
                            LOG.i("servertagid " + id);
                        }
                        if (tag.has(VIDEO_ID)) {
                            tagResponse.setServerVideoId(tag.getString(VIDEO_ID));
                        }
                        if (tag.has(TAG_NAME)) {
                            final String latestTag = tag.getString(TAG_NAME);
                            tagResponse.setName(latestTag);
                        }
                        if (tag.has(TAG_COLOR)) {
                            tagResponse.setColor(tag.getString(TAG_COLOR));
                        }
                        if (tag.has(COORDINATE_X)) {
                            final float x = Float.parseFloat(tag.getString(COORDINATE_X));
                            tagResponse.setTagX(x);
                        }
                        if (tag.has(COORDINATE_Y)) {
                            final float y = Float.parseFloat(tag.getString(COORDINATE_Y));
                            tagResponse.setTagY(y);
                        }
                        if (tag.has(TAG_LINK)) {
                            tagResponse.setLink(tag.getString(TAG_LINK));
                        }
                        if (tag.has(TAG_DURATION)) {
                            final String time = tag.getString(TAG_DURATION);
                            displayTime = Integer.parseInt(time);
                            tagResponse.setDisplayTime(time);
                        }
                        if (tag.has(TAG_FBLINK)) {
                            tagResponse.setFbId(tag.getString(TAG_FBLINK));
                        }
                        if (tag.has(TAG_GPLINK)) {
                            tagResponse.setgPlusId(tag.getString(TAG_GPLINK));
                        }
                        if (tag.has(TAG_TWLINK)) {
                            tagResponse.setTwId(tag.getString(TAG_TWLINK));
                        }
                        if (tag.has(TAG_WTLINK)) {
                            tagResponse.setTagFuId(tag.getString(TAG_WTLINK));
                        }
                        if (tag.has(VIDEO_CURRENT_TIME)) {
                            final long time = Util.getLongFromTime(tag.getString(VIDEO_CURRENT_TIME));
                            currentPosition = (int) time;
                            if (currentPosition > 1000) {
                                final int playBackTime = currentPosition - 500;
                                tagResponse.setVideoPlaybackTime(playBackTime);
                            } else {
                                tagResponse.setVideoPlaybackTime(currentPosition);
                            }

                        }
                        if (tag.has(CLIENT_TAG_ID)) {
                            final long clienttagId = Long.parseLong(tag.getString(CLIENT_TAG_ID));
                            tagResponse.setTagId(clienttagId);
                        }
                        if (tag.has(DEVICE_ID)) {
                        }
                        tagResponse.setTagTimeOutFrame((currentPosition + (1000 * displayTime)));
                        tagResponse.setUploadStatus(1);

                        if (tag.has(PRODUCT_NAME)) {
                            tagResponse.setProductName(tag.getString(PRODUCT_NAME));
                        }
                        if (tag.has(PRODUCT_PRICE)) {
                            tagResponse.setProductPrice(tag.getString(PRODUCT_PRICE));
                        }
                        if (tag.has(PRODUCT_LINK)) {
                            tagResponse.setProductLink(tag.getString(PRODUCT_LINK));
                        }
                        if (tag.has(PRODUCT_DESCRIPTION)) {
                            tagResponse.setProductDescription(tag.getString(PRODUCT_DESCRIPTION));
                        }
                        if (tag.has(PRODUCT_CATEGORY)) {
                            tagResponse.setProductCategory(tag.getString(PRODUCT_CATEGORY));
                        }
                        if (tag.has(CURRENCY)) {
                            tagResponse.setProductCurrency(tag.getString(CURRENCY));
                        }
                        if (tag.has(SOLD)) {
                            tagResponse.setProductSold(tag.getString(SOLD));
                        }
                        tags.add(tagResponse);
                    }
                    playback.setTags(tags);

                }
                result = playback;
            } else {
                final ErrorResponse error = new ErrorResponse();
                error.setErrorCode(String.valueOf(errorcode));

                if (json.has(MSG)) {
                    error.setMessage(json.getString(MSG));
                }
                result = error;
            }
        }

        return result;
    }

    public static Object parseReportVideo(final String response) throws JSONException {

        int errorcode = -1;
        Object result = null;
        final JSONObject json = new JSONObject(response);
        if (json.has(ERROR_CODE)) {
            errorcode = json.getInt(ERROR_CODE);
            if (errorcode == 0) {
                result = true;
            } else {
                final ErrorResponse error = new ErrorResponse();
                error.setErrorCode(String.valueOf(errorcode));

                if (json.has(MSG)) {
                    error.setMessage(json.getString(MSG));
                }
                result = error;
            }
        }

        return result;
    }

    public static Object parseRequestProduct(final String response) throws JSONException {

        Object result = null;
        final JSONObject json = new JSONObject(response);
        if (json.has(ERROR_CODE)) {
            final int errorcode = json.getInt(ERROR_CODE);
            if (errorcode == 0) {
                result = true;
            } else {
                final ErrorResponse error = new ErrorResponse();
                error.setErrorCode(String.valueOf(errorcode));

                if (json.has(MSG)) {
                    error.setMessage(json.getString(MSG));
                }
                result = error;
            }
        }

        return result;
    }

    public static long parseResponseJson(final String videoResponse) throws JSONException {

        int errorcode = -1;
        long videoId = 0;
        final JSONObject json = new JSONObject(videoResponse);
        if (json.has(ERROR_CODE)) {
            errorcode = json.getInt(ERROR_CODE);
            if ((errorcode == 0) && json.has(CLIENT_VIDEO_ID)) {
                final String clientvideoId = json.getString(CLIENT_VIDEO_ID);
                Config.setCurrentUploadVideoID(clientvideoId);
                videoId = Long.parseLong(clientvideoId);
                LOG.i("video upload client video id " + clientvideoId);
            }
        }
        LOG.i("video upload errorcode " + errorcode);
        return videoId;
    }

    public static Object parseSellProduct(final String response) throws JSONException {

        Object result = null;
        final JSONObject json = new JSONObject(response);
        if (json.has(ERROR_CODE)) {
            final int errorcode = json.getInt(ERROR_CODE);
            if (errorcode == 0) {
                result = true;
            } else {
                final ErrorResponse error = new ErrorResponse();
                error.setErrorCode(String.valueOf(errorcode));

                if (json.has(MSG)) {
                    error.setMessage(json.getString(MSG));
                }
                result = error;
            }
        }

        return result;
    }

    public static Object parseSignUpResponse(final String loginResponse) throws JSONException {

        int errorcode = -1;
        Object result = null;
        SignUpDto userDetails = null;
        final JSONObject json = new JSONObject(loginResponse);
        if (json.has(ERROR)) {
            final ErrorResponse errorResponse = new ErrorResponse();
            final JSONObject errorObj = json.getJSONObject(ERROR);
            if (errorObj.has(CODE)) {
                errorResponse.setErrorCode(errorObj.getString(CODE));
                errorResponse.setMessage(errorObj.getString(MESSAGE));
            }
            result = errorResponse;
        } else {
            if (json.has(ERROR_CODE)) {
                errorcode = json.getInt(ERROR_CODE);
                if (errorcode == 0) {
                    userDetails = new SignUpDto();
                    userDetails.setErrorcode(errorcode);
                    if (json.has(USER_ID)) {
                        userDetails.setUserId(json.getString(USER_ID));
                    }
                    if (json.has(MSG)) {
                        userDetails.setMessage(json.getString(MSG));
                    }
                    if (json.has(USER_NAME)) {
                        userDetails.setUserName(json.getString(USER_NAME));
                    }
                    if (json.has(USER_PHOTO)) {
                        userDetails.setUserPick(json.getString(USER_PHOTO));
                    }
                    result = userDetails;
                } else {
                    final ErrorResponse response = new ErrorResponse();
                    response.setErrorCode(String.valueOf(errorcode));
                    if (json.has(MSG)) {
                        response.setMessage(json.getString(MSG));
                    }
                    result = response;
                }

            }
        }
        return result;
    }

    public static Object parseSocialFriendFinderList(final String response) throws JSONException {

        Object result = null;
        final List<Friend> list = new ArrayList<Friend>();
        int errorcode = -1;

        final JSONObject json = new JSONObject(response);
        if (json.has(ERROR_CODE)) {
            errorcode = json.getInt(ERROR_CODE);
            if (errorcode == 0) {
                if (json.has(FRIENDS)) {
                    final JSONArray jArray = json.getJSONArray(FRIENDS);
                    for (int i = 0; i < jArray.length(); i++) {
                        final JSONObject obj = jArray.getJSONObject(i);
                        final Friend friend = new Friend();
                        friend.setFriendID(obj.getString(ID));
                        friend.setFriendName(obj.getString(USER_NAME));
                        friend.setFriendImage(obj.getString(PHOTO_PATH));
                        friend.setTagFuId(obj.getString(TagFu_ID));
                        if (obj.has(FOLLOWING)) {
                            friend.setIsFollow(obj.getString(FOLLOWING));
                        }
                        list.add(friend);
                    }
                    result = list;

                }
            } else {
                final ErrorResponse error = new ErrorResponse();
                error.setErrorCode(String.valueOf(errorcode));

                if (json.has(MSG)) {
                    error.setMessage(json.getString(MSG));
                }
                result = error;
            }
        }

        return result;
    }

    public static Object parseTagResponseJson(final String videoResponse) throws JSONException {

        List<TagResponse> tags = null;
        Object returnObject = null;
        int errorcode = -1;
        final JSONObject json = new JSONObject(videoResponse);
        if (json.has(ERROR_CODE)) {
            errorcode = json.getInt(ERROR_CODE);
            if (errorcode == 0) {
                if (json.has(TAGS)) {
                    final JSONArray tagArray = json.getJSONArray(TAGS);
                    tags = new ArrayList<TagResponse>();
                    for (int i = 0; i < tagArray.length(); i++) {
                        final TagResponse tagResponse = new TagResponse();
                        final JSONObject tag = tagArray.getJSONObject(i);
                        if (tag.has(TAG_ID)) {
                            final String id = tag.getString(TAG_ID);
                            final long tagServerId = Long.parseLong(id);
                            tagResponse.setServerTagId(tagServerId);
                        }
                        if (tag.has(CLIENT_SIDE_TAG_IDS)) {
                            tagResponse.setClientTagId(tag.getLong(CLIENT_SIDE_TAG_IDS));
                        }
                        tags.add(tagResponse);
                    }
                    returnObject = tags;
                }
            } else {
                final ErrorResponse response = new ErrorResponse();
                response.setErrorCode(String.valueOf(errorcode));
                response.setMessage(response.getMessage());
                returnObject = response;
            }
        }

        return returnObject;
    }

    public static Object parseTagsJson(final String videoResponse) throws JSONException {

        List<VideoProfile> videos = null;
        Object result = null;
        int errorcode = -1;
        final JSONObject json = new JSONObject(videoResponse);
        if (json.has(ERROR)) {
            final ErrorResponse errorResponse = new ErrorResponse();
            final JSONObject errorObj = json.getJSONObject(ERROR);
            if (errorObj.has(CODE)) {
                errorResponse.setErrorCode(errorObj.getString(CODE));
                errorResponse.setMessage(errorObj.getString(MESSAGE));
            }
            result = errorResponse;
        } else if (json.has(ERROR_CODE)) {
            errorcode = json.getInt(ERROR_CODE);
            if (errorcode == 0) {
                LOG.i("video feed parser ");
                if (json.has(TAGS)) {
                    final JSONArray videoArray = json.getJSONArray(TAGS);
                    videos = new ArrayList<VideoProfile>();
                    for (int i = 0; i < videoArray.length(); i++) {
                        final VideoProfile videoObj = new VideoProfile();
                        final JSONObject video = videoArray.getJSONObject(i);
                        videoObj.load(video);
                        videos.add(videoObj);
                    }
                    result = videos;
                }

            } else {
                final ErrorResponse response = new ErrorResponse();
                response.setErrorCode(String.valueOf(json.getInt(ERROR_CODE)));
                if (json.has(MSG)) {
                    response.setMessage(json.getString(MSG));
                }
                result = response;
            }
        }

        return result;
    }

    public static boolean parseUpdateJson(final String videoResponse) throws JSONException {

        int errorcode = -1;
        boolean success = false;
        final JSONObject json = new JSONObject(videoResponse);
        if (json.has(ERROR_CODE)) {
            errorcode = json.getInt(ERROR_CODE);
            if (errorcode == 0) {
                success = true;
            } else {
                success = false;
            }
        }
        LOG.i("video upload errorcode " + errorcode);
        return success;
    }

    public static Object parseUploadJson(final String videoResponse, final File file) throws JSONException {

        Object playback = null;
        playback = Parser.parsePlaybackResponseJson(videoResponse);
        if ((playback != null) && !(playback instanceof ErrorResponse)) {
            final boolean deleted = file.delete();
            LOG.e("delete file delete file after upload " + deleted);
        }
        return playback;
    }

    public static List<Comment> parseUserCommentsJson(final String response) throws JSONException {

        final List<Comment> list = new ArrayList<Comment>();
        int errorcode = -1;
        final JSONObject json = new JSONObject(response);
        if (json.has(ERROR_CODE)) {
            errorcode = json.getInt(ERROR_CODE);
            if ((errorcode == 0) && json.has(COMENTS)) {
                final JSONArray jArray = json.getJSONArray(COMENTS);
                for (int i = 0; i < jArray.length(); i++) {
                    final JSONObject obj = jArray.getJSONObject(i);
                    final Comment comment = new Comment();
                    if (obj.has(USER_ID)) {
                        comment.setUserId(obj.getString(USER_ID));
                    }
                    if (obj.has(COMMENT_ID)) {
                        comment.setCommentId(obj.getString(COMMENT_ID));
                    }
                    if (obj.has(USER_NAME)) {
                        comment.setUserName(obj.getString(USER_NAME));
                    }
                    if (obj.has(PHOTO_PATH)) {
                        comment.setUserPicUrl(obj.getString(PHOTO_PATH));
                    }
                    if (obj.has(COMMENT_TEXT)) {
                        comment.setComment(obj.getString(COMMENT_TEXT));
                    }
                    list.add(comment);
                }
            }
        }

        return list;
    }

    public static List<Friend> parseUsersResponseJson(final String response, final String type) throws JSONException {

        final List<Friend> list = new ArrayList<Friend>();
        int errorcode = -1;

        final JSONObject json = new JSONObject(response);
        if (json.has(ERROR_CODE)) {
            errorcode = json.getInt(ERROR_CODE);
            if ((errorcode == 0) && json.has(type)) {
                final JSONArray jArray = json.getJSONArray(type);
                for (int i = 0; i < jArray.length(); i++) {
                    final JSONObject obj = jArray.getJSONObject(i);
                    final Friend friend = new Friend();
                    friend.setFriendID(obj.getString(USER_ID));
                    friend.setFriendName(obj.getString(USER_NAME));
                    friend.setFriendImage(obj.getString(USER_PHOTO));
                    if (obj.has(FOLLOWING)) {
                        friend.setIsFollow(obj.getString(FOLLOWING));
                    }
                    list.add(friend);
                }
            }
        }

        return list;
    }

    public static Object parseVideoFeedJson(final String videoResponse) throws JSONException {

        List<VideoProfile> videos = null;
        Object result = null;
        int errorcode = -1;
        final JSONObject json = new JSONObject(videoResponse);
        if (json.has(ERROR)) {
            final ErrorResponse errorResponse = new ErrorResponse();
            final JSONObject errorObj = json.getJSONObject(ERROR);
            if (errorObj.has(CODE)) {
                errorResponse.setErrorCode(errorObj.getString(CODE));
                errorResponse.setMessage(errorObj.getString(MESSAGE));
            }
            result = errorResponse;
        } else if (json.has(ERROR_CODE)) {
            errorcode = json.getInt(ERROR_CODE);
            if (errorcode == 0) {
                LOG.i("video feed parser ");
                try {
                    if (json.has(VIDEOS)) {
                        final JSONArray videoArray = json.getJSONArray(VIDEOS);
                        videos = new ArrayList<VideoProfile>();
                        for (int i = 0; i < videoArray.length(); i++) {
                            final VideoProfile videoObj = new VideoProfile();
                            final JSONObject video = videoArray.getJSONObject(i);
                            videoObj.load(video);
                            videos.add(videoObj);
                        }
                        result = videos;
                    }

                } catch (final JSONException e) {
                    final ErrorResponse response = new ErrorResponse();
                    response.setErrorCode(_1);
                    response.setMessage(NO_FEEDS_AVAILABLE);
                    result = response;
                }
            } else {
                final ErrorResponse response = new ErrorResponse();
                response.setErrorCode(String.valueOf(json.getInt(ERROR_CODE)));
                if (json.has(MSG)) {
                    response.setMessage(json.getString(MSG));
                }
                result = response;
            }
        }

        return result;
    }

    public static Object parseVideosDetails(final String response) throws JSONException {

        Object result = null;
        VideoDetails videoprofile = null;
        int errorcode = -1;

        final JSONObject json = new JSONObject(response);
        if (json.has(ERROR)) {
            final ErrorResponse errorResponse = new ErrorResponse();
            final JSONObject errorObj = json.getJSONObject(ERROR);
            if (errorObj.has(CODE)) {
                errorResponse.setErrorCode(errorObj.getString(CODE));
                errorResponse.setMessage(errorObj.getString(MESSAGE));
            }
            result = errorResponse;
        } else if (json.has(ERROR_CODE)) {
            errorcode = json.getInt(ERROR_CODE);
            if (errorcode == 0) {
                if (json.has(VIDEOS)) {
                    final JSONArray videosArray = json.getJSONArray(VIDEOS);
                    if ((videosArray != null) && (videosArray.length() > 0)) {
                        for (int i = 0; i < videosArray.length(); i++) {
                            final JSONObject videosObj = videosArray.getJSONObject(i);
                            if (videosObj != null) {
                                videoprofile = new VideoDetails();
                                final JSONObject videoObj = videosObj;
                                videoprofile.load(videoObj);
                            }
                        }
                    }
                    if (json.has(MY_OTHER_STUFF)) {
                        final List<MoreVideos> moreVideos = new ArrayList<MoreVideos>();
                        final JSONArray morevideosArray = json.getJSONArray(MY_OTHER_STUFF);
                        for (int morevideos = 0; morevideos < morevideosArray.length(); morevideos++) {
                            final MoreVideos video = new MoreVideos();
                            video.load(morevideosArray.getJSONObject(morevideos));
                            moreVideos.add(video);
                        }
                        videoprofile.setOtherStuff(moreVideos);
                    }
                    result = videoprofile;
                }
            } else {
                final ErrorResponse error = new ErrorResponse();
                error.setErrorCode(String.valueOf(errorcode));

                if (json.has(MSG)) {
                    error.setMessage(json.getString(MSG));
                }
                result = error;
            }
        }

        return result;
    }

    public static List<VideoProfile> parseVideosResponseJson(final String response) throws JSONException {

        final List<VideoProfile> myvideos = new ArrayList<VideoProfile>();
        int errorcode = -1;

        final JSONObject json = new JSONObject(response);
        if (json.has(ERROR_CODE)) {
            errorcode = json.getInt(ERROR_CODE);
            if (errorcode == 0) {
                if (json.has(USER)) {
                    final JSONObject obj = json.getJSONObject(USER);
                    if (obj.has(VIDEOS)) {
                        final JSONArray videosArray = obj.getJSONArray(VIDEOS);
                        if ((videosArray != null) && (videosArray.length() > 0)) {
                            for (int i = 0; i < videosArray.length(); i++) {
                                final VideoProfile videoprofile = new VideoProfile();
                                videoprofile.load(videosArray.getJSONObject(i));
                                myvideos.add(videoprofile);
                            }
                        }
                    }
                }
            } else {

            }
        }

        return myvideos;
    }

    public static Object parseTagFuFriendsResponseJson(final String response, final String type) throws JSONException {

        Object result = null;
        final List<Friend> list = new ArrayList<Friend>();
        int errorcode = -1;

        final JSONObject json = new JSONObject(response);
        if (json.has(ERROR_CODE)) {
            errorcode = json.getInt(ERROR_CODE);
            if (errorcode == 0) {
                if (json.has(type)) {
                    final JSONArray jArray = json.getJSONArray(type);
                    for (int i = 0; i < jArray.length(); i++) {
                        final JSONObject obj = jArray.getJSONObject(i);
                        final Friend friend = new Friend();
                        friend.setFriendID(obj.getString(USER_ID));
                        friend.setFriendName(obj.getString(USER_NAME));
                        friend.setFriendImage(obj.getString(USER_PHOTO));
                        if (obj.has(FOLLOWING)) {
                            friend.setIsFollow(obj.getString(FOLLOWING));
                        }
                        list.add(friend);
                    }

                }
                result = list;
            } else {
                final ErrorResponse resp = new ErrorResponse();
                resp.setErrorCode(String.valueOf(errorcode));
                if (json.has(MSG)) {
                    resp.setMessage(json.getString(MSG));
                }
                result = resp;
            }

        }

        return result;
    }

    public static Object suggestedUsers(final String response) throws JSONException {

        Object result = null;
        List<People> suggestedUsers = null;
        int errorcode = -1;
        final JSONObject json = new JSONObject(response);
        if (json.has(ERROR_CODE)) {
            errorcode = json.getInt(ERROR_CODE);
            if (errorcode == 0) {
                if (json.has(SUGGESTED_USERS)) {
                    suggestedUsers = new ArrayList<People>();
                    final JSONArray suggestedUsersArray = json.getJSONArray(SUGGESTED_USERS);
                    if ((suggestedUsersArray != null) && (suggestedUsersArray.length() > 0)) {
                        for (int users = 0; users < suggestedUsersArray.length(); users++) {
                            final People user = new People();
                            user.load(suggestedUsersArray.getJSONObject(users));
                            suggestedUsers.add(user);
                        }
                    }
                }

                result = suggestedUsers;
            } else {
                final ErrorResponse error = new ErrorResponse();
                error.setErrorCode(String.valueOf(errorcode));

                if (json.has(MSG)) {
                    error.setMessage(json.getString(MSG));
                }
                result = error;
            }
        }

        return result;
    }

    public static Object TagFuFriends(final String response) throws JSONException {

        Object result = null;
        List<People> suggestedUsers = null;
        int errorcode = -1;
        final JSONObject json = new JSONObject(response);
        if (json.has(ERROR_CODE)) {
            errorcode = json.getInt(ERROR_CODE);
            if (errorcode == 0) {
                if (json.has(TagFu_FRIENDS)) {
                    suggestedUsers = new ArrayList<People>();
                    final JSONArray suggestedUsersArray = json.getJSONArray(TagFu_FRIENDS);
                    if ((suggestedUsersArray != null) && (suggestedUsersArray.length() > 0)) {
                        for (int users = 0; users < suggestedUsersArray.length(); users++) {
                            final People user = new People();
                            user.load(suggestedUsersArray.getJSONObject(users));
                            suggestedUsers.add(user);
                        }
                    }

                }

                result = suggestedUsers;
            } else {
                final ErrorResponse error = new ErrorResponse();
                error.setErrorCode(String.valueOf(errorcode));

                if (json.has(MSG)) {
                    error.setMessage(json.getString(MSG));
                }
                result = error;
            }
        }

        return result;
    }

}
