/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.wootag;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender.SendIntentException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.PlusClient.OnPeopleLoadedListener;
import com.google.android.gms.plus.PlusShare;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;
import com.google.common.base.Strings;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wootag.async.FacebookFriendsAsync;
import com.wootag.async.ShareViewsAsync;
import com.wootag.dto.FacebookUser;
import com.wootag.dto.Friend;
import com.wootag.dto.User;
import com.wootag.dto.VideoDetails;
import com.wootag.facebook.FacebookException;
import com.wootag.facebook.FacebookOperationCanceledException;
import com.wootag.facebook.HttpMethod;
import com.wootag.facebook.LoggingBehavior;
import com.wootag.facebook.Request;
import com.wootag.facebook.RequestAsyncTask;
import com.wootag.facebook.Response;
import com.wootag.facebook.Session;
import com.wootag.facebook.Session.StatusCallback;
import com.wootag.facebook.SessionState;
import com.wootag.facebook.Settings;
import com.wootag.facebook.model.GraphUser;
import com.wootag.facebook.widget.WebDialog;
import com.wootag.facebook.widget.WebDialog.OnCompleteListener;
import com.wootag.twitter.TwitterAsync;
import com.wootag.util.Alerts;
import com.wootag.util.AsyncResponse;
import com.wootag.util.Config;
import com.wootag.util.TwitterUtils;

public class FriendsListActivity extends Activity implements OnCancelListener, ConnectionCallbacks, AsyncResponse,
        OnConnectionFailedListener, OnPeopleLoadedListener, OnClickListener {

    private static final String SIGNING_IN = "Signing in";
    private static final String WWW_WOOTAG_COM_INVITE_HTML = "www.wootag.com/invite.html";
    private static final String RECORD_TAG_SELF_PEOPLE_PLACE_PRODUCT_INSIDE_YOUR_VIDEOS_AND_SHARE = "Record, Tag - self,people, place, product inside your videos and Share.";
    private static final String _1 = "1";
    private static final String ID = "id";
    private static final String POST_SUCCESSSFULLY_ON_YOUR_WALL = "Post successsfully on your wall";
    private static final String CAPTION = "caption";
    private static final String EMAIL = "email";
    private static final String EMPTY = "";
    private static final String ERROR_POSTING_LINK = "Error posting link.";
    private static final String FRIENDS_BIRTHDAY = "friends_birthday";
    private static final String FRIENDS_EDUCATION_HISTORY = "friends_education_history";
    private static final String FRIENDS_HOMETOWN = "friends_hometown";
    private static final String FRIENDS_LOCATION = "friends_location";
    private static final String FRIENDS_ONLINE_PRESENCE = "friends_online_presence";
    private static final String FRIENDS_PHOTOS = "friends_photos";
    private static final String FRIENDS_STATUS = "friends_status";
    private static final String FRIENDS_WORK_HISTORY = "friends_work_history";
    private static final String GRAPH_FACEBOOK_COM = "https://graph.facebook.com/";
    private static final String HTTP_SCHEMAS_GOOGLE_COM_ADD_ACTIVITY = "http://schemas.google.com/AddActivity";
    private static final String HTTP_SCHEMAS_GOOGLE_COM_BUY_ACTIVITY = "http://schemas.google.com/BuyActivity";
    private static final String LINK = "link";
    private static final String LOGIN_FAILED = "Login failed";
    private static final String MESSAGE = "message";
    private static final String NAME = "name";
    private static final String NAVIGATE_TO_PREPARE = "Navigate to prepare";
    private static final String OK = "OK";
    private static final String PICTURE = "picture";
    private static final String PICTURE2 = "/picture";
    private static final String POSTED_LINK = "Posted link.";
    private static final String POST_ID = "post_id";
    private static final String PUBLISH_CANCELLED = "Publish cancelled.";
    private static final String READ_STREAM = "read_stream";
    private static final String TEXT_PLAIN = "text/plain";
    private static final String TO = "to";
    private static final String USER_BIRTHDAY = "user_birthday";
    private static final String USER_EDUCATION_HISTORY = "user_education_history";
    private static final String USER_HOMETOWN = "user_hometown";
    private static final String USER_LOCATION = "user_location";
    private static final String USER_ONLINE_PRESENCE = "user_online_presence";
    private static final String USER_PHOTOS = "user_photos";
    private static final String USER_WORK_HISTORY = "user_work_history";

    private static final String[] PERMISSIONS = new String[] { READ_STREAM, EMAIL, USER_PHOTOS, FRIENDS_PHOTOS,
            USER_LOCATION, FRIENDS_LOCATION, USER_EDUCATION_HISTORY, FRIENDS_STATUS, FRIENDS_BIRTHDAY, USER_BIRTHDAY,
            USER_ONLINE_PRESENCE, FRIENDS_ONLINE_PRESENCE, FRIENDS_EDUCATION_HISTORY, USER_WORK_HISTORY,
            FRIENDS_WORK_HISTORY, FRIENDS_HOMETOWN, USER_HOMETOWN };

    public static FriendsListActivity friendsListActivity;
    public static Handler handler;
    protected static final Logger LOG = LoggerManager.getLogger();
    protected static Session session;
    protected String facebookRequest;
    protected static List<Friend> friendList;

    private static final int REQUEST_CODE_RESOLVE_ERR = 9000;
    private static final int GOOGLE_PLUS_SHARE_REQUEST_CODE = 163;
    private static boolean pendingRequest;

    private ConnectionResult connectionResult;
    private PlusClient plusClient;
    private ProgressDialog progressDialog;
    private String twitterRequest;
    private TextView progressText;
    private UpdateReceiver updateReceiver;
    protected Context context;
    protected String gPlusRequest;
    protected VideoDetails videoDetails;
    protected boolean twitterFirstAuthentication;

    @Override
    public void friendInfoProcessFinish(final FacebookUser info, final boolean friend, final String socialSite) {

        if (Constant.TWITTER.equalsIgnoreCase(socialSite)) {
            if (this.twitterRequest.equalsIgnoreCase(Constant.TWITTER_AUTHORIZE)) {
                this.saveFacebookUser(info, socialSite);

            } else {
                this.saveFacebookUser(info, socialSite);
                final TwitterAsync asyncTask = new TwitterAsync(EMPTY, this.context, EMPTY, EMPTY, EMPTY, null, EMPTY);
                asyncTask.delegate = FriendsListActivity.this;
                asyncTask.execute();

            }

        } else if (Constant.FACEBOOK.equalsIgnoreCase(socialSite)) {
            this.saveFacebookUser(info, socialSite);
            this.requestForFacebookFriendList();
        }

    }

    public VideoDetails getVideoDetails() {

        return this.videoDetails;
    }

    public void gPlusSignout() {

        if ((this.plusClient != null) && this.plusClient.isConnected()) {
            this.plusClient.clearDefaultAccount();
            this.plusClient.disconnect();
        }
    }

    @Override
    public void onCancel(final DialogInterface dialog) {

    }

    @Override
    public void onClick(final View view) {

        LOG.i("super on click");

        switch (view.getId()) {
        case R.id.fbshare:
            this.facebookRequest = Constant.FRIEND_LIST;
            LOG.i("super fb onclick");
            this.facebookLogin();
            break;

        case R.id.google:
            this.gPlusRequest = Constant.G_PLUS_FRIEND_LIST;
            this.gPlusLogin();
            break;

        case R.id.twittershare:
            this.twitterRequest = Constant.TWITTER_SHARE;
            this.twitterLogin();
            break;

        case R.id.googleshare:
            this.gPlusRequest = Constant.G_PLUS_SHARE;
            this.gPlusLogin();
            break;

        case R.id.gpluslogin:
            this.gPlusRequest = Constant.G_PLUS_FRIEND_LIST;
            this.gPlusLogin();
            break;

        case R.id.twitterlogin:
            this.twitterRequest = Constant.TWITTER_FEED;
            this.twitterFeed();
            break;

        case R.id.fblogin:
            this.facebookRequest = Constant.FEED;
            this.facebookLogin();
            break;

        case R.id.googlefrinedfinder:
            this.gPlusRequest = Constant.G_PLUS_FRIEND_FINDER;
            this.gPlusLogin();
            break;

        case R.id.fbfrinedfinder:
            this.facebookRequest = Constant.FACEBOOK_FRIEND_FINDER;
            this.facebookLogin();
            break;

        case R.id.twitterfrinedfinder:
            this.twitterRequest = Constant.TWITTER_FRIEND_FINDER;
            this.twitterLogin();
            break;

        case R.id.gpluesconnect:
            this.gPlusRequest = Constant.G_PLUS_AUTHORIZE;
            this.gPlusLogin();
            break;

        case R.id.fbconnect:
            this.facebookRequest = Constant.FACEBOOK_AUTHORIZE;
            this.facebookLogin();
            break;

        case R.id.twitterconnect:
            this.twitterRequest = Constant.TWITTER_AUTHORIZE;
            this.twitterFeed();
            break;

        case R.id.gplusAuthentication:
            this.gPlusRequest = Constant.G_PLUS_AUTHORIZE;
            this.gPlusLogin();
            break;

        case R.id.fbAuthentication:
            this.facebookRequest = Constant.FACEBOOK_AUTHORIZE;
            this.facebookLogin();
            break;

        case R.id.twitterAuthentication:
            this.twitterRequest = Constant.TWITTER_AUTHORIZE;
            this.twitterFeed();
            break;

        case R.id.gplustoggle:
            this.gPlusRequest = Constant.G_PLUS_AUTHORIZE;
            this.gPlusLogin();
            break;

        case R.id.fbtoggle:
            this.facebookRequest = Constant.FACEBOOK_AUTHORIZE;
            this.facebookLogin();
            break;

        case R.id.twtoggle:
            this.twitterRequest = Constant.TWITTER_AUTHORIZE;
            this.twitterFeed();
            break;

        default:
            break;
        }

    }

    @Override
    public void onConnected(final Bundle connectionHint) {

        // try {
        final String accountName = this.plusClient.getAccountName();

        if (Constant.G_PLUS_FRIEND_LIST.equalsIgnoreCase(this.gPlusRequest)) {

            this.progressText.setText(R.string.loading);
            this.saveUserDetails(this.plusClient.getCurrentPerson(), accountName);
            this.plusClient.loadVisiblePeople(this, null);

        } else if (Constant.G_PLUS_SHARE.equalsIgnoreCase(this.gPlusRequest)) {

            this.progressDialog.dismiss();
            this.googleShare();

        } else if (Constant.G_PLUS_FRIEND_FINDER.equalsIgnoreCase(this.gPlusRequest)) {

            this.progressText.setText(R.string.loading);
            this.plusClient.loadVisiblePeople(this, null);

        } else if (Constant.G_PLUS_AUTHORIZE.equalsIgnoreCase(this.gPlusRequest)) {

            this.progressDialog.dismiss();
            this.saveUserDetails(this.plusClient.getCurrentPerson(), accountName);

        } else if (Constant.G_PLUS_SHARE_VIDEO.equalsIgnoreCase(this.gPlusRequest)) {

            this.progressDialog.dismiss();
            this.plusClient.loadVisiblePeople(this, null);
            this.saveUserDetails(this.plusClient.getCurrentPerson(), accountName);
        }
    }

    @Override
    public void onConnectionFailed(final ConnectionResult result) {

        LOG.i("on connection failed" + result);
        if (result.hasResolution()) {
            try {
                result.startResolutionForResult(this, REQUEST_CODE_RESOLVE_ERR);
            } catch (final SendIntentException e) {
                this.plusClient.connect();
            }
        }
        this.connectionResult = result;
    }

    @Override
    public void onDisconnected() {

        LOG.d("disconnected");
        this.progressDialog.dismiss();
        Alerts.showInfoOnly("Connection Disconnected.Please try agin", this);
    }

    @Override
    public void onPeopleLoaded(final ConnectionResult status, final PersonBuffer personBuffer,
            final String nextPageToken) {

        LOG.i("on people  load");
        if (status.getErrorCode() == ConnectionResult.SUCCESS) {
            final List<Friend> gPlusFriendList = new ArrayList<Friend>();
            final int count = personBuffer.getCount();

            for (int i = 0; i < count; i++) {
                final Friend friend = new Friend();
                friend.setFriendID(personBuffer.get(i).getId());
                if (personBuffer.get(i).getImage() != null) {
                    friend.setFriendImage(personBuffer.get(i).getImage().getUrl());
                }

                friend.setFriendName(personBuffer.get(i).getDisplayName().toString());
                if (personBuffer.get(i).getCurrentLocation() != null) {
                    friend.setLocation(personBuffer.get(i).getCurrentLocation());
                }
                gPlusFriendList.add(friend);
            }
            friendList = gPlusFriendList;
            VideoPlayerApp.getInstance().setGoogleFriendList(friendList);
            if (Constant.G_PLUS_SHARE_VIDEO.equalsIgnoreCase(this.gPlusRequest)) {
                this.processFinish(gPlusFriendList, Constant.G_PLUS_SHARE_VIDEO);
            } else {
                this.processFinish(gPlusFriendList, Constant.GOOGLE_PLUS);
            }
            this.progressDialog.dismiss();
        } else {
            this.progressDialog.dismiss();
            Alerts.showExceptionOnly(String.valueOf(status.getErrorCode()), this.context);
        }
    }

    public void postOnFaceBookWall(final String fbId) {

        final Bundle postParams = new Bundle();
        if (!Strings.isNullOrEmpty(this.videoDetails.getLatestTagexpression())) {
            postParams.putString(NAME, this.videoDetails.getLatestTagexpression());
        } else {
            postParams.putString(NAME, this.videoDetails.getVideoTitle());
        }
        postParams.putString(LINK, this.videoDetails.getFbShareUrl());
        postParams.putString(PICTURE, this.videoDetails.getVideothumbPath());

        final Request.Callback callback = new Request.Callback() {

            @Override
            public void onCompleted(final Response response) {

                if (response.getError() == null) {
                    final JSONObject graphResponse = response.getGraphObject().getInnerJSONObject();
                    String postId = null;
                    try {
                        postId = graphResponse.getString(ID);
                        new ShareViewsAsync(FriendsListActivity.this.videoDetails.getVideoID(),
                                Constant.FACEBOOK_PLATFORM, _1, Config.getUserId(), FriendsListActivity.this.context)
                                .execute();
                        Toast.makeText(FriendsListActivity.this.context, POST_SUCCESSSFULLY_ON_YOUR_WALL,
                                Toast.LENGTH_LONG).show();
                    } catch (final JSONException e) {
                        LOG.e("Facebook Error JSON error " + e.getMessage());
                    }
                } else {
                    LOG.e("fb error " + response.getError());
                }
            }
        };// me

        final Request request = new Request(session, "me/feed", postParams, HttpMethod.POST, callback);
        final RequestAsyncTask task = new RequestAsyncTask(request);

        task.execute();

    }

    @Override
    public void processFinish(final List<Friend> friendList, final String socialSite) {

        if (Constant.TWITTER.equalsIgnoreCase(socialSite)) {
            this.sendList(friendList);
            VideoPlayerApp.getInstance().setTwitterFriendList(friendList);

        } else if (Constant.FACEBOOK.equalsIgnoreCase(socialSite)) {
            this.requestForLoogedInUserFacebookFeed(friendList);

        }
    }

    public void publishFeedDialog(final String fbId) {

        if (!Strings.isNullOrEmpty(Config.getFacebookLoggedUserId())
                && Config.getFacebookLoggedUserId().equalsIgnoreCase(fbId)) {
            this.postOnFaceBookWall(fbId);

        } else {
            final Bundle params = new Bundle();
            if (!Strings.isNullOrEmpty(this.videoDetails.getLatestTagexpression())) {
                params.putString(NAME, this.videoDetails.getLatestTagexpression());
            } else {
                params.putString(NAME, this.videoDetails.getVideoTitle());
            }

            params.putString(LINK, this.videoDetails.getFbShareUrl());
            params.putString(PICTURE, this.videoDetails.getVideothumbPath());
            params.putString(TO, fbId);
            final WebDialog feedDialog = new WebDialog.FeedDialogBuilder(this, Session.getActiveSession(), params)
                    .setOnCompleteListener(new OnCompleteListener() {

                        @Override
                        public void onComplete(final Bundle values, final FacebookException error) {

                            if (error == null) {
                                // When the story is posted, echo the success
                                // and the post Id.
                                final String postId = values.getString(POST_ID);
                                if (postId != null) {
                                    new ShareViewsAsync(FriendsListActivity.this.videoDetails.getVideoID(),
                                            Constant.FACEBOOK_PLATFORM, _1, Config.getUserId(),
                                            FriendsListActivity.this.context).execute();
                                    Alerts.showInfoOnly(POSTED_LINK, FriendsListActivity.this.context);
                                } else {
                                    // User clicked the Cancel button
                                    Alerts.showInfoOnly(PUBLISH_CANCELLED, FriendsListActivity.this.context);
                                }
                            } else if (error instanceof FacebookOperationCanceledException) {
                                // User clicked the "x" button
                                Alerts.showInfoOnly(PUBLISH_CANCELLED, FriendsListActivity.this.context);
                            } else {
                                // Generic, ex: network error
                                Alerts.showInfoOnly(ERROR_POSTING_LINK, FriendsListActivity.this.context);
                            }
                        }

                    }).build();
            feedDialog.show();
        }
    }

    public void publishVideoUrl(final String fbId) {

        final Bundle params = new Bundle();
        params.putString(NAME, this.videoDetails.getShareUrl());
        params.putString(CAPTION, this.videoDetails.getShareUrl());
        params.putString(LINK, this.videoDetails.getShareUrl());
        params.putString(PICTURE, this.videoDetails.getVideothumbPath());
        params.putString(TO, fbId);
        final WebDialog feedDialog = new WebDialog.FeedDialogBuilder(this, Session.getActiveSession(), params)
                .setOnCompleteListener(new OnCompleteListener() {

                    @Override
                    public void onComplete(final Bundle values, final FacebookException error) {

                        if (error == null) {
                            // When the story is posted, echo the success
                            // and the post Id.
                            final String postId = values.getString(POST_ID);
                            if (postId != null) {
                                Alerts.showInfo(POSTED_LINK, FriendsListActivity.this.context);
                            } else {
                                // User clicked the Cancel button
                                Alerts.showInfo(PUBLISH_CANCELLED, FriendsListActivity.this.context);
                            }
                        } else if (error instanceof FacebookOperationCanceledException) {
                            // User clicked the "x" button
                            Alerts.showInfo(PUBLISH_CANCELLED, FriendsListActivity.this.context);
                        } else {
                            // Generic, ex: network error
                            Alerts.showInfo(ERROR_POSTING_LINK, FriendsListActivity.this.context);
                        }
                    }

                }).build();
        feedDialog.show();
    }

    public void sendList(final List<Friend> list) {

    }

    public void sendRequestDialog(final String friendId) {

        final Bundle params = new Bundle();
        params.putString(MESSAGE, EMPTY);
        params.putString(TO, friendId);

        final WebDialog requestsDialog = new WebDialog.RequestsDialogBuilder(this.context, Session.getActiveSession(),
                params).setOnCompleteListener(new OnCompleteListener() {

            @Override
            public void onComplete(final Bundle values, final FacebookException error) {

                if (error != null) {
                    if (error instanceof FacebookOperationCanceledException) {
                        Toast.makeText(FriendsListActivity.this.context.getApplicationContext(), "Request cancelled",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(FriendsListActivity.this.context.getApplicationContext(), "Network Error",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    final String requestId = values.getString("request");
                    if (requestId != null) {
                        Toast.makeText(FriendsListActivity.this.context.getApplicationContext(), "Request sent",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(FriendsListActivity.this.context.getApplicationContext(), "Request cancelled",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }

        }).build();
        requestsDialog.show();
    }

    public void setVideoDetails(final VideoDetails videoDetails) {

        this.videoDetails = videoDetails;
    }

    public void userDetailsFinished(final User userDetails, final String socialsite) {

    }

    private void googleShare() {

        if ((this.videoDetails != null) && (this.videoDetails.getShareUrl() != null)) {
            String title = EMPTY;
            if (!Strings.isNullOrEmpty(this.videoDetails.getLatestTagexpression())) {
                title = this.videoDetails.getLatestTagexpression();
            } else {
                title = this.videoDetails.getVideoTitle();
            }
            final Uri uri = Uri.parse(this.videoDetails.getVideothumbPath());

            final Intent shareIntent = new PlusShare.Builder(FriendsListActivity.this, this.plusClient)
                    .setType(TEXT_PLAIN).setText(title).setContentDeepLinkId("/wootag/video", title, EMPTY, uri)
                    .setContentUrl(Uri.parse(this.videoDetails.getShareUrl())).getIntent();
            this.startActivityForResult(shareIntent, 0);
        } else {
            final Intent shareIntent = new PlusShare.Builder(FriendsListActivity.this, this.plusClient)
                    .setType(TEXT_PLAIN).setText("www.tagmoments.com").setContentUrl(Uri.parse("www.tagmoments.com"))
                    .getIntent();
            this.startActivityForResult(shareIntent, 0);
        }
    }

    private void requestForFacebookFeed() {

        Config.setFacebookAccessToken(session.getAccessToken());

        final Session session = Session.getActiveSession();

        final Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {

            @Override
            public void onCompleted(final GraphUser user, final Response response) {

                if ((session == Session.getActiveSession()) && (user != null)) {
                    final FacebookUser info = new FacebookUser();

                    String email = EMPTY;
                    if (user.asMap().containsKey(EMAIL)) {
                        email = user.asMap().get(EMAIL).toString();
                    }
                    final String id = (String) response.getGraphObject().getProperty(EMAIL);
                    info.setUserName(user.getName());
                    info.setId(user.getId());
                    Config.setFacebookLoggedUserId(user.getId());
                    info.setProfilePick(GRAPH_FACEBOOK_COM + user.getId() + PICTURE2);
                    info.setEmail(email);
                    if (Constant.FACEBOOK_AUTHORIZE.equalsIgnoreCase(FriendsListActivity.this.facebookRequest)) {
                        FriendsListActivity.this.saveFacebookUser(info, Constant.FACEBOOK);
                    } else if (Constant.FACEBOOK_SHARE.equalsIgnoreCase(FriendsListActivity.this.facebookRequest)) {
                        FriendsListActivity.this.saveFacebookUser(info, Constant.FACEBOOK);
                    } else {
                        FriendsListActivity.this.friendInfoProcessFinish(info, false, Constant.FACEBOOK);
                    }
                }
            }
        });
        Request.executeBatchAsync(request);
    }

    private void requestForFacebookFriendList() {

        Config.setFacebookAccessToken(session.getAccessToken());
        final FacebookFriendsAsync async = new FacebookFriendsAsync(this.context, Constant.FRIEND_LIST, EMPTY);
        async.delegate = FriendsListActivity.this;

        async.execute();

    }

    private void requestForLoogedInUserFacebookFeed(final List<Friend> friendList) {

        Config.setFacebookAccessToken(session.getAccessToken());

        Request.newMeRequest(session, new Request.GraphUserCallback() {

            @Override
            public void onCompleted(final GraphUser user, final Response response) {

                if (user != null) {
                    final Friend friend = new Friend();
                    Config.setFacebookLoggedUserId(user.getId());
                    // String email = user.asMap().get("email").toString();
                    friend.setFriendName("You");
                    friend.setFriendID(user.getId());
                    friend.setFriendImage(GRAPH_FACEBOOK_COM + user.getId() + PICTURE2);
                    if (user.getLocation().getLocation().getCity() != null) {
                        friend.setLocation(user.getLocation().getLocation().getCity());
                    } else {
                        friend.setLocation(EMPTY);
                    }
                    friendList.add(0, friend);
                    VideoPlayerApp.getInstance().setFacebookFriendsList(friendList);
                    FriendsListActivity.this.sendList(friendList);
                }
            }

        }).executeAsync();
    }

    private void saveUserDetails(final Person currentPerson, final String accountName) {

        final User userDetails = new User();
        if (currentPerson != null) {
            userDetails.setEmailId(accountName);
            if (currentPerson.getId() != null) {
                userDetails.setUserId(currentPerson.getId());
            }
            if (currentPerson.getDisplayName() != null) {
                userDetails.setUserName(currentPerson.getDisplayName());
            }
            if (currentPerson.getImage().getUrl() != null) {
                userDetails.setUserPickURL(currentPerson.getImage().getUrl());
            }
        }
        this.userDetailsFinished(userDetails, Constant.GOOGLE_PLUS);

    }

    private void twitterFeed() {

        if (TwitterUtils.isAuthenticated(this.context)) {
            final TwitterAsync asyncTask = new TwitterAsync(EMPTY, this.context, EMPTY, Constant.TWITTER_FEED, EMPTY,
                    null, EMPTY);
            asyncTask.delegate = FriendsListActivity.this;

            asyncTask.execute();
        } else {
            LOG.i(NAVIGATE_TO_PREPARE);
            TwitterUtils.navigateToPrepare(this.context);
        }
    }

    private void twitterLogin() {

        if (TwitterUtils.isAuthenticated(this.context)) {
            final TwitterAsync asyncTask = new TwitterAsync(EMPTY, this.context, EMPTY, EMPTY, EMPTY, null, EMPTY);
            asyncTask.delegate = FriendsListActivity.this;

            asyncTask.execute();
        } else {
            LOG.i(NAVIGATE_TO_PREPARE);
            TwitterUtils.navigateToPrepare(this.context);
        }
    }

    protected void facebookLogin() {

        session = this.createSession();
        Config.setSocialSite(Constant.FACEBOOK);
        Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

        LOG.e("session opened " + Session.getActiveSession().isOpened());

        if (Session.getActiveSession().isOpened()) {
            Config.setFacebookAccessToken(session.getAccessToken());
            if (Constant.FEED.equalsIgnoreCase(this.facebookRequest)) {
                this.requestForFacebookFeed();
            } else if (Constant.FACEBOOK_AUTHORIZE.equalsIgnoreCase(this.facebookRequest)) {
                this.requestForFacebookFeed();
            } else if (Constant.FACEBOOK_SHARE.equalsIgnoreCase(this.facebookRequest)) {
                this.requestForFacebookFeed();
            } else {
                this.requestForFacebookFriendList();
            }
        } else {
            LOG.i("login trying......");
            final StatusCallback callback = new StatusCallback() {

                @Override
                public void call(final Session session, final SessionState state, final Exception exception) {

                    if (exception != null) {
                        new AlertDialog.Builder(FriendsListActivity.this).setTitle(LOGIN_FAILED)
                                .setMessage(exception.getMessage()).setPositiveButton(OK, null).show();
                        FriendsListActivity.session = FriendsListActivity.this.createSession();
                    }
                }
            };

            pendingRequest = true;
            session.openForRead(new Session.OpenRequest(FriendsListActivity.this).setCallback(callback).setPermissions(
                    PERMISSIONS));

            LOG.i("fb session" + Session.getActiveSession().isOpened());

            if (Session.getActiveSession().isOpened()) {
                Config.setFacebookAccessToken(session.getAccessToken());
                if (Constant.FEED.equalsIgnoreCase(this.facebookRequest)) {
                    this.requestForFacebookFeed();
                }
                if (Constant.FACEBOOK_FRIEND_FINDER.equalsIgnoreCase(this.facebookRequest)) {
                    this.requestForFacebookFriendList();
                } else if (Constant.FACEBOOK_SHARE.equalsIgnoreCase(this.facebookRequest)) {
                    this.requestForFacebookFeed();
                } else if (Constant.FACEBOOK_AUTHORIZE.equalsIgnoreCase(this.facebookRequest)) {
                    this.requestForFacebookFeed();
                } else {
                    this.requestForFacebookFriendList();
                }
            }
        }

    }

    protected void gPlusLogin() {

        Config.setSocialSite(Constant.GOOGLE_PLUS);
        this.plusClient = new PlusClient.Builder(FriendsListActivity.this, FriendsListActivity.this,
                FriendsListActivity.this).setScopes(Scopes.PLUS_LOGIN)
                .setActions(HTTP_SCHEMAS_GOOGLE_COM_ADD_ACTIVITY, HTTP_SCHEMAS_GOOGLE_COM_BUY_ACTIVITY).build();
        this.plusClient.connect();

        this.progressDialog = ProgressDialog.show(this.context, EMPTY, EMPTY, true);
        final View view = ((LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
                R.layout.progress_bar, null, false);

        this.progressText = (TextView) view.findViewById(R.id.progressText);
        this.progressText.setText(SIGNING_IN);

        this.progressDialog.setContentView(view);
        this.progressDialog.setCancelable(true);

        if (this.connectionResult == null) {
            this.progressDialog.show();
        } else {
            try {
                LOG.i("connection starting");
                this.connectionResult.startResolutionForResult(FriendsListActivity.this, REQUEST_CODE_RESOLVE_ERR);
            } catch (final SendIntentException e) {
                // Try connecting again.
                LOG.i("trying connection restarting");
                this.connectionResult = null;
                this.plusClient.connect();
            }
        }
    }

    protected void gPlusShare(final String id, final String name) {

        final List<Person> recipients = new ArrayList<Person>();
        recipients.add(PlusShare.createPerson(id, name));

        String title = EMPTY;
        if (!Strings.isNullOrEmpty(this.videoDetails.getLatestTagexpression())) {
            title = this.videoDetails.getLatestTagexpression();
        } else {
            title = this.videoDetails.getVideoTitle();
        }
        final Intent shareIntent = new PlusShare.Builder(FriendsListActivity.this, this.plusClient).setType(TEXT_PLAIN)
                .setText(title).setRecipients(recipients).setContentUrl(Uri.parse(this.videoDetails.getShareUrl()))
                .getIntent();
        this.startActivityForResult(shareIntent, 0);

    }

    protected void inviteFriend(final String id, final String name) {

        final List<Person> recipients = new ArrayList<Person>();
        recipients.add(PlusShare.createPerson(id, name));

        final Intent shareIntent = new PlusShare.Builder(FriendsListActivity.this, this.plusClient).setType(TEXT_PLAIN)
                .setRecipients(recipients).setText(RECORD_TAG_SELF_PEOPLE_PLACE_PRODUCT_INSIDE_YOUR_VIDEOS_AND_SHARE)
                .setContentUrl(Uri.parse(WWW_WOOTAG_COM_INVITE_HTML)).getIntent();
        this.startActivityForResult(shareIntent, 0);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int responseCode, final Intent intent) {

        if ((requestCode == REQUEST_CODE_RESOLVE_ERR) && (responseCode == RESULT_OK)) {
            LOG.i("on activity result ");
            this.connectionResult = null;
            this.plusClient.connect();
        } else if (requestCode == Constant.FACEBOOK_REQUEST_CODE) {
            if (FriendsListActivity.session != null) {
                if (FriendsListActivity.session.onActivityResult(this, requestCode, responseCode, intent)
                        && pendingRequest && FriendsListActivity.session.getState().isOpened()) {
                    LOG.i("fb oncomplete iam fb dialog...onComplete" + session.getAccessToken());
                    Config.setFacebookAccessToken(session.getAccessToken());
                    if (Constant.FEED.equalsIgnoreCase(this.facebookRequest)) {
                        this.requestForFacebookFeed();
                    } else if (Constant.FACEBOOK_AUTHORIZE.equalsIgnoreCase(this.facebookRequest)) {
                        this.requestForFacebookFeed();
                    } else if (Constant.FACEBOOK_SHARE.equalsIgnoreCase(this.facebookRequest)) {
                        this.requestForFacebookFeed();
                    } else {
                        this.requestForFacebookFriendList();
                    }

                }
            } else {
                Alerts.showInfoOnly("Facebook authorization done, Please click on facebook button to sign in.",
                        this.context);
            }
        } else if (requestCode == 0) {
            if (responseCode == RESULT_OK) {
                Alerts.showInfoOnly("Successfully shared.", this.context);
            }
        } else if ((requestCode == FriendsListActivity.GOOGLE_PLUS_SHARE_REQUEST_CODE)
                && (responseCode == Activity.RESULT_OK) && (this.videoDetails != null)) {
            new ShareViewsAsync(this.videoDetails.getVideoID(), Constant.GOOGLE_PLUS_PLATFORM, _1, Config.getUserId(),
                    this.context).execute();
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.base);
        friendsListActivity = this;
        this.context = this;
        this.twitterFirstAuthentication = true;
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.TWITTER_FRIEND_LIST);
        filter.addAction(Constant.TWITTER_EXCEPTION);
        filter.addAction(Constant.CANCEL_OPERATION);
        filter.addAction("share");
        handler = new Handler();
        if (this.updateReceiver == null) {
            this.updateReceiver = new UpdateReceiver();
            this.getApplicationContext().registerReceiver(this.updateReceiver, filter);
        }

    }

    @Override
    protected void onDestroy() {

        if (this.updateReceiver != null) {
            this.getApplicationContext().unregisterReceiver(this.updateReceiver);
            this.updateReceiver = null;
        }
        super.onDestroy();
    }

    void authentication() {

        String request = EMPTY;
        if (Constant.TWITTER_FEED.equalsIgnoreCase(this.twitterRequest)
                || Constant.TWITTER_AUTHORIZE.equalsIgnoreCase(this.twitterRequest)) {
            request = Constant.TWITTER_FEED;
        }

        final TwitterAsync asyncTask = new TwitterAsync(EMPTY, this.context, EMPTY, request, EMPTY, null, EMPTY);
        asyncTask.delegate = FriendsListActivity.this;

        asyncTask.execute();
    }

    Session createSession() {

        Session activeSession = Session.getActiveSession();
        LOG.i("active session " + activeSession);
        if ((activeSession == null) || activeSession.getState().isClosed()) {
            activeSession = new Session.Builder(this).setApplicationId(Constant.FACEBOOK_APP_ID).build();// VideoPlayerConstants.FACEBOOK_APP_ID
            Session.setActiveSession(activeSession);
        }
        session = Session.getActiveSession();

        if (session.isOpened()) {
            final List<String> permissions = session.getPermissions();
            if (!permissions.contains(EMAIL)) {
                final Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(
                        FriendsListActivity.this, PERMISSIONS);
                session.requestNewReadPermissions(newPermissionsRequest);
            }
        }

        return session;
    }

    void saveFacebookUser(final FacebookUser currentPerson, final String socialSite) {

        final User userDetails = new User();
        if (currentPerson != null) {
            if (currentPerson.getEmail() != null) {
                userDetails.setEmailId(currentPerson.getEmail());
            }
            if (currentPerson.getId() != null) {
                userDetails.setUserId(currentPerson.getId());
            }
            if (currentPerson.getUserName() != null) {
                userDetails.setUserName(currentPerson.getUserName());
            }
            if (currentPerson.getProfilePick() != null) {
                userDetails.setUserPickURL(currentPerson.getProfilePick());
            }
        }
        this.userDetailsFinished(userDetails, socialSite);
    }

    public class UpdateReceiver extends BroadcastReceiver {

        private static final String UNABLE_TO_GET_THE_TWITTER_DATA = "Unable to get the twitter data.";
        private static final String SHARE = "share";
        private static final String YOU_ARE_NOT_LOGGED_INTO_TWITTER_PLEASE_CLOSE_AND_TRY_AGAIN = "You are not logged into twitter, Please close and try again";
        private static final String HAVE_AUTHENTICATED_THE_USER_PLEASE_CLICK_ON_TWITTER_ONCE_AGAIN_TO_RETRIEVE_CONTACTS = "Have authenticated the user.Please click on twitter once again to retrieve contacts.";

        @Override
        public void onReceive(final Context context, final Intent intent) {

            final String action = intent.getAction();
            // Bundle b = intent.getExtras();
            if (Constant.TWITTER_FRIEND_LIST.equalsIgnoreCase(action)) {
                handler.post(new Runnable() {

                    @Override
                    public void run() {

                        LOG.i("received intent success");
                        if (TwitterUtils.isAuthenticated(context)) {
                            LOG.i("twitter authenticated already");
                            FriendsListActivity.this.authentication();
                        } else {
                            LOG.i("intent received not authenticated");
                            if (FriendsListActivity.this.twitterFirstAuthentication) {
                                FriendsListActivity.this.twitterFirstAuthentication = false;
                                FriendsListActivity.this.authentication();
                            } else {
                                Alerts.showInfoOnly(
                                        HAVE_AUTHENTICATED_THE_USER_PLEASE_CLICK_ON_TWITTER_ONCE_AGAIN_TO_RETRIEVE_CONTACTS,
                                        context);
                            }
                        }
                    }
                });
            } else if (Constant.CANCEL_OPERATION.equalsIgnoreCase(action)) {
                handler.post(new Runnable() {

                    @Override
                    public void run() {

                        Alerts.showInfoOnly(YOU_ARE_NOT_LOGGED_INTO_TWITTER_PLEASE_CLOSE_AND_TRY_AGAIN, context);

                    }
                });
            } else if (SHARE.equalsIgnoreCase(action)) {
                handler.post(new Runnable() {

                    @Override
                    public void run() {

                        FriendsListActivity.this.facebookRequest = Constant.FACEBOOK_SHARE;
                        FriendsListActivity.this.facebookLogin();
                    }
                });
            } else {
                LOG.i("received intent error");
                Alerts.showInfoOnly(UNABLE_TO_GET_THE_TWITTER_DATA, context);
            }
        }
    }
}
