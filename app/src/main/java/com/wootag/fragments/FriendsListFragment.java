/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.TagFu.fragments;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender.SendIntentException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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

import com.TagFu.Constant;
import com.TagFu.R;
import com.TagFu.VideoPlayerApp;
import com.TagFu.async.FacebookFriendsAsync;
import com.TagFu.async.ShareViewsAsync;
import com.TagFu.dto.Contact;
import com.TagFu.dto.FacebookUser;
import com.TagFu.dto.Friend;
import com.TagFu.dto.User;
import com.TagFu.dto.VideoDetails;
import com.TagFu.facebook.FacebookException;
import com.TagFu.facebook.FacebookOperationCanceledException;
import com.TagFu.facebook.FacebookRequestError;
import com.TagFu.facebook.HttpMethod;
import com.TagFu.facebook.LoggingBehavior;
import com.TagFu.facebook.Request;
import com.TagFu.facebook.RequestAsyncTask;
import com.TagFu.facebook.Response;
import com.TagFu.facebook.Session;
import com.TagFu.facebook.Session.StatusCallback;
import com.TagFu.facebook.SessionState;
import com.TagFu.facebook.Settings;
import com.TagFu.facebook.model.GraphUser;
import com.TagFu.facebook.widget.WebDialog;
import com.TagFu.facebook.widget.WebDialog.OnCompleteListener;
import com.TagFu.twitter.TwitterAsync;
import com.TagFu.util.Alerts;
import com.TagFu.util.AsyncResponse;
import com.TagFu.util.Config;
import com.TagFu.util.TwitterUtils;

public class FriendsListFragment extends BaseFragment implements OnCancelListener, ConnectionCallbacks, AsyncResponse,
        OnConnectionFailedListener, OnPeopleLoadedListener, OnClickListener {

    private static final String WWW_TagFu_COM_INVITE_HTML = "www.TagFu.com/invite.html";
    private static final String RECORD_TAG_SELF_PEOPLE_PLACE_PRODUCT_INSIDE_YOUR_VIDEOS_AND_SHARE = "Record, Tag - self,people, place, product inside your videos and Share.";
    private static final String SIGNING_IN = "Signing in";
    private static final String YOU = "You";
    private static final String PICTURE_ = "/picture";
    private static final String HTTPS_GRAPH_FACEBOOK_COM = "https://graph.facebook.com/";
    private static final String LOGIN_FAILED = "Login failed";
    private static final String OK = "OK";
    private static final String ME_FEED = "me/feed";
    private static final String _1 = "1";
    private static final String POSTED_LINK = "Posted link.";
    private static final String CONNECTION_DISCONNECTED_PLEASE_TRY_AGIN = "Connection Disconnected.Please try agin";
    private static final String SUCCESSFULLY_SHARED = "Successfully shared.";
    private static final String EMPTY = "";
    private static final String HTTP_SCHEMAS_GOOGLE_COM_BUY_ACTIVITY = "http://schemas.google.com/BuyActivity";
    private static final String HTTP_SCHEMAS_GOOGLE_COM_ADD_ACTIVITY = "http://schemas.google.com/AddActivity";
    private static final String CAPTION = "caption";
    private static final String EMAIL = "email";
    private static final String FRIENDS_BIRTHDAY = "friends_birthday";
    private static final String FRIENDS_EDUCATION_HISTORY = "friends_education_history";
    private static final String FRIENDS_HOMETOWN = "friends_hometown";
    private static final String FRIENDS_LOCATION = "friends_location";
    private static final String FRIENDS_ONLINE_PRESENCE = "friends_online_presence";
    private static final String FRIENDS_PHOTOS = "friends_photos";
    private static final String FRIENDS_STATUS = "friends_status";
    private static final String FRIENDS_WORK_HISTORY = "friends_work_history";
    private static final int GOOGLE_PLUS_SHARE_REQUEST_CODE = 163;
    private static final String LINK = "link";
    protected static final String NAME = "name";
    private static final String PICTURE = "picture";
    private static final String POST_ID = "post_id";
    private static final String PUBLISH_CANCELLED = "Publish cancelled.";
    private static final String READ_STREAM = "read_stream";
    private static final int REQUEST_CODE_RESOLVE_ERR = 9000;
    private static final String TEXT_PLAIN = "text/plain";
    private static final String TO = "to";
    private static final String USER_BIRTHDAY = "user_birthday";
    private static final String USER_EDUCATION_HISTORY = "user_education_history";
    private static final String USER_HOMETOWN = "user_hometown";
    private static final String USER_LOCATION = "user_location";
    private static final String USER_ONLINE_PRESENCE = "user_online_presence";
    private static final String USER_PHOTOS = "user_photos";
    private static final String USER_WORK_HISTORY = "user_work_history";

    public static FriendsListFragment friendsListActivity;
    public static Handler handler;
    protected String fbRequest;
    private static boolean pendingRequest;
    static Session session;

    private static final String[] PERMISSIONS = new String[] { READ_STREAM, EMAIL, USER_PHOTOS, FRIENDS_PHOTOS,
            USER_LOCATION, FRIENDS_LOCATION, USER_EDUCATION_HISTORY, FRIENDS_STATUS, FRIENDS_BIRTHDAY, USER_BIRTHDAY,
            USER_ONLINE_PRESENCE, FRIENDS_ONLINE_PRESENCE, FRIENDS_EDUCATION_HISTORY, USER_WORK_HISTORY,
            FRIENDS_WORK_HISTORY, FRIENDS_HOMETOWN, USER_HOMETOWN };

    protected static final Logger LOG = LoggerManager.getLogger();
    protected static List<Friend> friendList;
    private ProgressDialog connectionProgressDialog;
    private ConnectionResult connectionResult;
    private boolean isTwitterFirstAuthentication;
    private ProgressDialog pDialog;
    private PlusClient plusClient;
    private TextView progressText;
    private UpdateReceiver receiver;

    private String twitterRequest = EMPTY;
    protected VideoDetails videoDetails;
    private View view;
    protected Context context;
    protected String gPlusRequest;

    public static Bitmap loadContactPhoto(final ContentResolver contentResolver, final long id) {

        final Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
        final InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(contentResolver, uri);
        if (input == null) {
            return null;// getBitmapFromURL("http://thinkandroid.wordpress.com");
        }
        return BitmapFactory.decodeStream(input);
    }

    public void fbSigned() {

    }

    @Override
    public void friendInfoProcessFinish(final FacebookUser info, final boolean friend, final String socialSite) {

        if (Constant.TWITTER.equalsIgnoreCase(socialSite)) {
            if (Constant.TWITTER_AUTHORIZE.equalsIgnoreCase(this.twitterRequest)) {
                this.saveFacebookUser(info, socialSite);
            } else {
                this.saveFacebookUser(info, socialSite);
                final TwitterAsync asyncTask = new TwitterAsync(EMPTY, this.getActivity().getApplicationContext(),
                        EMPTY, EMPTY, EMPTY, null, EMPTY);
                asyncTask.delegate = FriendsListFragment.this;
                asyncTask.execute();
            }

        } else if (Constant.FACEBOOK.equalsIgnoreCase(socialSite)) {
            this.saveFacebookUser(info, socialSite);
            this.requestForFacebookFriendList();
        }

    }

    public void getFacebookPages() {

        final Session session = Session.getActiveSession();
        final Request.Callback callback = new Request.Callback() {

            @Override
            public void onCompleted(final Response response) {

                // response should have the likes
                Toast.makeText(FriendsListFragment.this.context, response.toString(), Toast.LENGTH_LONG).show();
            }
        };
        final Request request = new Request(session, "me/pages", null, HttpMethod.GET, callback);
        final RequestAsyncTask task = new RequestAsyncTask(request);
        task.execute();
    }

    public VideoDetails getVideoDetails() {

        return this.videoDetails;
    }

    public void gplusSignout() {

        if (this.plusClient.isConnected()) {
            this.plusClient.clearDefaultAccount();
            this.plusClient.disconnect();
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int responseCode, final Intent data) {

        this.getActivity();
        if ((requestCode == REQUEST_CODE_RESOLVE_ERR) && (responseCode == Activity.RESULT_OK)) {
            this.connectionResult = null;
            this.plusClient.connect();
        } else if (requestCode == Constant.FACEBOOK_REQUEST_CODE) {
            if (FriendsListFragment.session.onActivityResult(this.getActivity(), requestCode, responseCode, data)
                    && pendingRequest && FriendsListFragment.session.getState().isOpened()) {
                Config.setFacebookAccessToken(session.getAccessToken());
                if (Constant.FEED.equalsIgnoreCase(this.fbRequest)) {
                    this.requestForFacebookFeed();
                } else if (Constant.FACEBOOK_AUTHORIZE.equalsIgnoreCase(this.fbRequest)) {
                    this.requestForFacebookFeed();
                } else if (Constant.FACEBOOK_SHARE.equalsIgnoreCase(this.fbRequest)) {
                    this.requestForFacebookFeed();
                } else {
                    this.requestForFacebookFriendList();
                }

            }
        } else if (requestCode == 0) {
            this.getActivity();
            if (responseCode == Activity.RESULT_OK) {
                Alerts.showInfoOnly(SUCCESSFULLY_SHARED, this.getActivity().getApplicationContext());
            } else {
            }
        } else if (requestCode == FriendsListFragment.GOOGLE_PLUS_SHARE_REQUEST_CODE) {
            this.getActivity();
            if (responseCode == Activity.RESULT_OK) {
                if (this.videoDetails != null) {
                    new ShareViewsAsync(this.videoDetails.getVideoID(), Constant.GOOGLE_PLUS_PLATFORM, _1,
                            Config.getUserId(), this.context).execute();
                }
            } else {
            }
        }
    }

    @Override
    public void onCancel(final DialogInterface dialog) {

    }

    @Override
    public void onClick(final View v) {

        LOG.i("super onclick");
        switch (v.getId()) {
        case R.id.fbshare:
            this.fbRequest = Constant.FRIEND_LIST;
            LOG.i("super fb onclick");
            this.fbLogin();
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
            this.fbRequest = Constant.FEED;
            this.fbLogin();
            break;
        case R.id.googlefrinedfinder:
            this.gPlusRequest = Constant.G_PLUS_FRIEND_FINDER;
            this.gPlusLogin();
            break;
        case R.id.fbfrinedfinder:
            this.fbRequest = Constant.FACEBOOK_FRIEND_FINDER;
            this.fbLogin();
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
            this.fbRequest = Constant.FACEBOOK_AUTHORIZE;
            this.fbLogin();
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
            this.fbRequest = Constant.FACEBOOK_AUTHORIZE;
            this.fbLogin();
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
            this.fbRequest = Constant.FACEBOOK_AUTHORIZE;
            this.fbLogin();
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

        final String accountName = this.plusClient.getAccountName();
        if (Constant.G_PLUS_FRIEND_LIST.equalsIgnoreCase(this.gPlusRequest)) {
            this.progressText.setText(R.string.loading);
            this.saveUserDetails(this.plusClient.getCurrentPerson(), accountName);
            this.plusClient.loadVisiblePeople(this, null);
        } else if (Constant.G_PLUS_SHARE.equalsIgnoreCase(this.gPlusRequest)) {
            this.connectionProgressDialog.dismiss();
            this.googleShare();
        } else if (Constant.G_PLUS_FRIEND_FINDER.equalsIgnoreCase(this.gPlusRequest)) {
            this.progressText.setText(R.string.loading);
            this.plusClient.loadVisiblePeople(this, null);
        } else if (Constant.G_PLUS_AUTHORIZE.equalsIgnoreCase(this.gPlusRequest)) {
            this.connectionProgressDialog.dismiss();
            this.saveUserDetails(this.plusClient.getCurrentPerson(), accountName);
        } else if (Constant.G_PLUS_SHARE_VIDEO.equalsIgnoreCase(this.gPlusRequest)) {
            this.connectionProgressDialog.dismiss();
            this.plusClient.loadVisiblePeople(this, null);
            this.saveUserDetails(this.plusClient.getCurrentPerson(), accountName);
        }
    }

    @Override
    public void onConnectionFailed(final ConnectionResult result) {

        if (result.hasResolution()) {
            try {
                result.startResolutionForResult(this.getActivity(), REQUEST_CODE_RESOLVE_ERR);
            } catch (final SendIntentException e) {
                this.plusClient.connect();
            }
        }
        this.connectionResult = result;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {

        friendsListActivity = this;
        this.context = this.getActivity();
        this.isTwitterFirstAuthentication = true;
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.TWITTER_FRIEND_LIST);
        filter.addAction(Constant.TWITTER_EXCEPTION);
        filter.addAction(Constant.CANCEL_OPERATION);
        filter.addAction("share");
        handler = new Handler();
        if (this.receiver == null) {
            this.receiver = new UpdateReceiver();
            this.getActivity().getApplicationContext().registerReceiver(this.receiver, filter);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        this.view = inflater.inflate(R.layout.base, container, false);

        return this.view;
    }

    @Override
    public void onDestroy() {

        if (this.receiver != null) {
            this.getActivity().getApplicationContext().unregisterReceiver(this.receiver);
            this.receiver = null;
        }
        super.onDestroy();
    }

    @Override
    public void onDisconnected() {

        this.connectionProgressDialog.dismiss();
        Alerts.showInfoOnly(CONNECTION_DISCONNECTED_PLEASE_TRY_AGIN, this.getActivity());
    }

    @Override
    public void onPeopleLoaded(final ConnectionResult status, final PersonBuffer personBuffer,
            final String nextPageToken) {

        if (status.getErrorCode() == ConnectionResult.SUCCESS) {
            try {
                final List<Friend> gPlusFriendList = new ArrayList<Friend>();
                final int count = personBuffer.getCount();
                // gPlusFriends=personBuffer;
                for (int i = 0; i < count; i++) {
                    final Friend frnd = new Friend();
                    frnd.setFriendID(personBuffer.get(i).getId());
                    if (personBuffer.get(i).getImage() != null) {
                        frnd.setFriendImage(personBuffer.get(i).getImage().getUrl());
                    }

                    frnd.setFriendName(personBuffer.get(i).getDisplayName().toString());
                    if (personBuffer.get(i).getCurrentLocation() != null) {
                        frnd.setLocation(personBuffer.get(i).getCurrentLocation());
                    }
                    gPlusFriendList.add(frnd);
                }
                friendList = gPlusFriendList;
                VideoPlayerApp.getInstance().setGoogleFriendList(friendList);
                if (Constant.G_PLUS_SHARE_VIDEO.equalsIgnoreCase(this.gPlusRequest)) {
                    this.processFinish(gPlusFriendList, Constant.G_PLUS_SHARE_VIDEO);
                } else {
                    this.processFinish(gPlusFriendList, Constant.GOOGLE_PLUS);
                }
            } finally {
                this.connectionProgressDialog.dismiss();
                personBuffer.close();
            }
        } else {
            this.connectionProgressDialog.dismiss();
            Alerts.showExceptionOnly(String.valueOf(status.getErrorCode()), this.getActivity().getApplicationContext());

        }
    }

    public void postOnFaceBookWall() {

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
                        postId = graphResponse.getString("id");
                        LOG.i("fbownpost post id " + postId);
                        new ShareViewsAsync(FriendsListFragment.this.videoDetails.getVideoID(),
                                Constant.FACEBOOK_PLATFORM, _1, Config.getUserId(), FriendsListFragment.this.context)
                                .execute();
                        Toast.makeText(FriendsListFragment.this.context, "Post successsfully on your wall",
                                Toast.LENGTH_LONG).show();
                    } catch (final JSONException e) {
                        LOG.i("Facebook Error JSON error " + e.getMessage());
                    }
                    final FacebookRequestError error = response.getError();
                }
            }
        };
        final Request request = new Request(session, ME_FEED, postParams, HttpMethod.POST, callback);
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
                && fbId.trim().equalsIgnoreCase(Config.getFacebookLoggedUserId().trim())) {
            this.postOnFaceBookWall();
        } else {
            final Bundle params = new Bundle();
            if (Strings.isNullOrEmpty(this.videoDetails.getLatestTagexpression())) {
                params.putString(NAME, this.videoDetails.getVideoTitle());
            } else {
                params.putString(NAME, this.videoDetails.getLatestTagexpression());
            }
            params.putString(LINK, this.videoDetails.getShareUrl());
            params.putString(PICTURE, this.videoDetails.getVideothumbPath());
            params.putString(TO, fbId);
            final WebDialog feedDialog = new WebDialog.FeedDialogBuilder(this.getActivity(),
                    Session.getActiveSession(), params).setOnCompleteListener(new OnCompleteListener() {

                @Override
                public void onComplete(final Bundle values, final FacebookException error) {

                    if (error == null) {
                        final String postId = values.getString(POST_ID);
                        if (postId != null) {
                            new ShareViewsAsync(FriendsListFragment.this.videoDetails.getVideoID(),
                                    Constant.FACEBOOK_PLATFORM, _1, Config.getUserId(),
                                    FriendsListFragment.this.context).execute();
                            Alerts.showInfoOnly(POSTED_LINK, FriendsListFragment.this.getActivity()
                                    .getApplicationContext());

                        } else {
                            // User clicked the Cancel button
                            Alerts.showInfoOnly(PUBLISH_CANCELLED, FriendsListFragment.this.getActivity()
                                    .getApplicationContext());
                        }
                    } else if (error instanceof FacebookOperationCanceledException) {
                        Alerts.showInfoOnly(PUBLISH_CANCELLED, FriendsListFragment.this.getActivity()
                                .getApplicationContext());
                    } else {
                        Alerts.showInfoOnly("Error posting link.", FriendsListFragment.this.getActivity()
                                .getApplicationContext());
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
        final WebDialog feedDialog = new WebDialog.FeedDialogBuilder(this.getActivity(), Session.getActiveSession(),
                params).setOnCompleteListener(new OnCompleteListener() {

            @Override
            public void onComplete(final Bundle values, final FacebookException error) {

                if (error == null) {
                    // When the story is posted, echo the success
                    // and the post Id.
                    final String postId = values.getString(POST_ID);
                    if (postId != null) {
                        Alerts.showInfo(POSTED_LINK, FriendsListFragment.this.getActivity().getApplicationContext());
                    } else {
                        // User clicked the Cancel button
                        Alerts.showInfo(PUBLISH_CANCELLED, FriendsListFragment.this.getActivity()
                                .getApplicationContext());
                    }
                } else if (error instanceof FacebookOperationCanceledException) {
                    // User clicked the "x" button
                    Alerts.showInfo(PUBLISH_CANCELLED, FriendsListFragment.this.getActivity().getApplicationContext());
                } else {
                    // Generic, ex: network error
                    Alerts.showInfo("Error posting link.", FriendsListFragment.this.getActivity()
                            .getApplicationContext());
                }
            }

        }).build();
        feedDialog.show();
    }

    public List<Contact> readContacts() {

        final List<Contact> contacts = new ArrayList<Contact>();
        final ContentResolver contentResolver = this.getActivity().getContentResolver();
        final Cursor cur = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                final String id = cur.getString(cur.getColumnIndex(BaseColumns._ID));
                final String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    LOG.i("conatcts name : " + name + ", ID : " + id);
                    // get the phone number
                    final Cursor pCur = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { id }, null);
                    while (pCur.moveToNext()) {
                        final Contact contact = new Contact();
                        final String phone = pCur.getString(pCur
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        final String image = pCur.getString(pCur
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_ID));
                        if (image != null) {
                            contact.setContactPicture(loadContactPhoto(contentResolver, Long.parseLong(id)));
                        }
                        contact.setContactName(name);
                        contact.setContactNumber(phone);
                        contacts.add(contact);
                    }
                    pCur.close();

                }
            }
        }
        return contacts;

    }

    public void sendList(final List<Friend> list) {

    }

    public void sendRequestDialog(final String friendId) {

        // friendId="100003973809545";
        final Bundle params = new Bundle();
        params.putString("message", EMPTY);
        params.putString(TO, friendId);

        final WebDialog requestsDialog = new WebDialog.RequestsDialogBuilder(
                this.getActivity().getApplicationContext(), Session.getActiveSession(), params).setOnCompleteListener(
                new OnCompleteListener() {

                    @Override
                    public void onComplete(final Bundle values, final FacebookException error) {

                        if (error != null) {
                            if (error instanceof FacebookOperationCanceledException) {
                                Toast.makeText(
                                        FriendsListFragment.this.getActivity().getApplicationContext()
                                                .getApplicationContext(), "Request cancelled", Toast.LENGTH_SHORT)
                                        .show();
                            } else {
                                Toast.makeText(
                                        FriendsListFragment.this.getActivity().getApplicationContext()
                                                .getApplicationContext(), "Network Error", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            final String requestId = values.getString("request");
                            if (requestId != null) {
                                Toast.makeText(
                                        FriendsListFragment.this.getActivity().getApplicationContext()
                                                .getApplicationContext(), "Request sent", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(
                                        FriendsListFragment.this.getActivity().getApplicationContext()
                                                .getApplicationContext(), "Request cancelled", Toast.LENGTH_SHORT)
                                        .show();
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

            final Intent shareIntent = new PlusShare.Builder(this.getActivity(), this.plusClient).setType(TEXT_PLAIN)
                    .setText(title).setContentDeepLinkId("/TagFu/video", title, EMPTY, uri)
                    .setContentUrl(Uri.parse(this.videoDetails.getShareUrl())).getIntent();
            this.startActivityForResult(shareIntent, 0);
        } else {
            final Intent shareIntent = new PlusShare.Builder(this.getActivity(), this.plusClient).setType(TEXT_PLAIN)
                    .setText("www.tagmoments.com").setContentUrl(Uri.parse("www.tagmoments.com")).getIntent();
            this.startActivityForResult(shareIntent, 0);
        }
    }

    private void gPlusShare(final String id, final String name) {

        final List<Person> recipients = new ArrayList<Person>();
        recipients.add(PlusShare.createPerson(id, name));

        String title = EMPTY;
        if (!Strings.isNullOrEmpty(this.videoDetails.getLatestTagexpression())) {
            title = this.videoDetails.getLatestTagexpression();
        } else {
            title = this.videoDetails.getVideoTitle();
        }
        final Intent shareIntent = new PlusShare.Builder(this.getActivity(), this.plusClient).setType(TEXT_PLAIN)
                .setText(title).setRecipients(recipients).setContentUrl(Uri.parse(this.videoDetails.getShareUrl()))
                .getIntent();
        this.startActivityForResult(shareIntent, 0);

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
                    info.setProfilePick(HTTPS_GRAPH_FACEBOOK_COM + user.getId() + PICTURE_);
                    info.setEmail(email);
                    // friendInfoprocessFinish(info,false,VideoPlayerConstants.FACEBOOK);
                    if (Constant.FACEBOOK_AUTHORIZE.equalsIgnoreCase(FriendsListFragment.this.fbRequest)) {
                        FriendsListFragment.this.saveFacebookUser(info, Constant.FACEBOOK);
                    } else if (Constant.FACEBOOK_SHARE.equalsIgnoreCase(FriendsListFragment.this.fbRequest)) {
                        FriendsListFragment.this.saveFacebookUser(info, Constant.FACEBOOK);
                    } else {
                        FriendsListFragment.this.friendInfoProcessFinish(info, false, Constant.FACEBOOK);
                    }
                }
            }
        });
        Request.executeBatchAsync(request);
    }

    private void requestForFacebookFriendList() {

        Config.setFacebookAccessToken(session.getAccessToken());
        final FacebookFriendsAsync async = new FacebookFriendsAsync(this.context, Constant.FRIEND_LIST, EMPTY);
        async.delegate = FriendsListFragment.this;
        async.execute();
    }

    private void requestForLoogedInUserFacebookFeed(final List<Friend> friendList) {

        Config.setFacebookAccessToken(session.getAccessToken());

        Request.newMeRequest(session, new Request.GraphUserCallback() {

            @Override
            public void onCompleted(final GraphUser user, final Response response) {

                if (user != null) {
                    final Friend friend = new Friend();
                    friend.setFriendName(YOU);
                    friend.setFriendID(user.getId());
                    Config.setFacebookLoggedUserId(user.getId());
                    friend.setFriendImage(HTTPS_GRAPH_FACEBOOK_COM + user.getId() + PICTURE_);
                    friend.setLocation(EMPTY);
                    if (user.getLocation().getLocation() != null) {
                        friend.setLocation(user.getLocation().getLocation().getCity());
                    }

                    friendList.add(0, friend);
                    VideoPlayerApp.getInstance().setFacebookFriendsList(friendList);
                    FriendsListFragment.this.sendList(friendList);
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

        if (TwitterUtils.isAuthenticated(this.getActivity().getApplicationContext())) {
            final TwitterAsync asyncTask = new TwitterAsync(EMPTY, this.getActivity().getApplicationContext(), EMPTY,
                    Constant.TWITTER_FEED, EMPTY, null, EMPTY);
            asyncTask.delegate = FriendsListFragment.this;
            asyncTask.execute();
        } else {
            LOG.i("Navigate to prepare");
            TwitterUtils.navigateToPrepare(this.getActivity().getApplicationContext());
        }
    }

    private void twitterLogin() {

        if (TwitterUtils.isAuthenticated(this.getActivity().getApplicationContext())) {
            final TwitterAsync asyncTask = new TwitterAsync(EMPTY, this.getActivity().getApplicationContext(), EMPTY,
                    EMPTY, EMPTY, null, EMPTY);
            asyncTask.delegate = FriendsListFragment.this;
            asyncTask.execute();
        } else {
            LOG.i("Navigate to prepare");
            TwitterUtils.navigateToPrepare(this.getActivity());
        }
    }

    protected void authentication() {

        if (Constant.TWITTER_FEED.equalsIgnoreCase(this.twitterRequest)) {
            final TwitterAsync asyncTask = new TwitterAsync(EMPTY, this.getActivity(), EMPTY, Constant.TWITTER_FEED,
                    EMPTY, null, EMPTY);
            asyncTask.delegate = FriendsListFragment.this;
            asyncTask.execute();
        } else if (Constant.TWITTER_AUTHORIZE.equalsIgnoreCase(this.twitterRequest)) {
            final TwitterAsync asyncTask = new TwitterAsync(EMPTY, this.getActivity(), EMPTY, Constant.TWITTER_FEED,
                    EMPTY, null, EMPTY);
            asyncTask.delegate = FriendsListFragment.this;
            asyncTask.execute();
        } else {
            final TwitterAsync asyncTask = new TwitterAsync(EMPTY, this.getActivity(), EMPTY, EMPTY, EMPTY, null, EMPTY);
            asyncTask.delegate = FriendsListFragment.this;
            asyncTask.execute();
        }
    }

    protected void fbLogin() {

        session = this.createSession();
        Config.setSocialSite(Constant.FACEBOOK);
        Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

        // System.out.println("session opend "+Session.getActiveSession().isOpened());
        if (Session.getActiveSession().isOpened()) {
            Config.setFacebookAccessToken(session.getAccessToken());
            if (Constant.FEED.equalsIgnoreCase(this.fbRequest)) {
                this.requestForFacebookFeed();
            } else if (Constant.FACEBOOK_AUTHORIZE.equalsIgnoreCase(this.fbRequest)) {
                this.requestForFacebookFeed();
            } else if (Constant.FACEBOOK_SHARE.equalsIgnoreCase(this.fbRequest)) {
                this.requestForFacebookFeed();
            } else {
                this.requestForFacebookFriendList();
            }
        } else {
            final StatusCallback callback = new StatusCallback() {

                @Override
                public void call(final Session session, final SessionState state, final Exception exception) {

                    if (exception != null) {
                        new AlertDialog.Builder(FriendsListFragment.this.getActivity()).setTitle(LOGIN_FAILED)
                                .setMessage(exception.getMessage()).setPositiveButton(OK, null).show();
                        FriendsListFragment.session = FriendsListFragment.this.createSession();
                    }
                }
            };
            pendingRequest = true;
            session.openForRead(new Session.OpenRequest(this.getActivity()).setCallback(callback).setPermissions(
                    PERMISSIONS));
            LOG.i("fb session" + Session.getActiveSession().isOpened());
            if (Session.getActiveSession().isOpened()) {
                Config.setFacebookAccessToken(session.getAccessToken());
                if (Constant.FEED.equalsIgnoreCase(this.fbRequest)) {
                    this.requestForFacebookFeed();
                }
                if (Constant.FACEBOOK_FRIEND_FINDER.equalsIgnoreCase(this.fbRequest)) {
                    this.requestForFacebookFriendList();
                } else if (Constant.FACEBOOK_SHARE.equalsIgnoreCase(this.fbRequest)) {
                    this.requestForFacebookFeed();
                } else if (Constant.FACEBOOK_AUTHORIZE.equalsIgnoreCase(this.fbRequest)) {
                    this.requestForFacebookFeed();
                } else {
                    this.requestForFacebookFriendList();
                }
            }
        }

    }

    protected void gPlusLogin() {

        Config.setSocialSite(Constant.GOOGLE_PLUS);
        this.plusClient = new PlusClient.Builder(this.getActivity().getApplicationContext(), this, this)
                .setScopes(Scopes.PLUS_LOGIN)
                .setActions(HTTP_SCHEMAS_GOOGLE_COM_ADD_ACTIVITY, HTTP_SCHEMAS_GOOGLE_COM_BUY_ACTIVITY).build();
        this.plusClient.connect();

        this.connectionProgressDialog = ProgressDialog.show(this.context, EMPTY, EMPTY, true);
        final View view = ((LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
                R.layout.progress_bar, null, false);
        this.progressText = (TextView) view.findViewById(R.id.progressText);
        this.progressText.setText(SIGNING_IN);
        this.connectionProgressDialog.setContentView(view);
        this.connectionProgressDialog.setCancelable(true);

        if (this.connectionResult == null) {
            this.connectionProgressDialog.show();
        } else {
            try {
                LOG.i("connection starting");
                this.connectionResult.startResolutionForResult(this.getActivity(), REQUEST_CODE_RESOLVE_ERR);
            } catch (final SendIntentException e) {
                // Try connecting again.
                LOG.i("trying connection restarting");
                this.connectionResult = null;
                this.plusClient.connect();
            }
        }
    }

    protected void inviteFriend(final String id, final String name) {

        final List<Person> recipients = new ArrayList<Person>();
        recipients.add(PlusShare.createPerson(id, name));

        final Intent shareIntent = new PlusShare.Builder(this.getActivity(), this.plusClient).setType(TEXT_PLAIN)
                .setRecipients(recipients).setText(RECORD_TAG_SELF_PEOPLE_PLACE_PRODUCT_INSIDE_YOUR_VIDEOS_AND_SHARE)
                .setContentUrl(Uri.parse(WWW_TagFu_COM_INVITE_HTML)).getIntent();
        this.startActivityForResult(shareIntent, 0);
    }

    Session createSession() {

        Session activeSession = Session.getActiveSession();
        LOG.i("active session " + activeSession);
        if ((activeSession == null) || activeSession.getState().isClosed()) {
            activeSession = new Session.Builder(this.getActivity()).setApplicationId(Constant.FACEBOOK_APP_ID).build();
            Session.setActiveSession(activeSession);
        }
        session = Session.getActiveSession();

        if (session.isOpened()) {
            final List<String> permissions = session.getPermissions();
            if (!permissions.contains(EMAIL)) {
                final Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(
                        FriendsListFragment.this, PERMISSIONS);
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
        private static final String TWITTER_SHARE = "share";
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

                        if (TwitterUtils.isAuthenticated(context)) {
                            FriendsListFragment.this.authentication();
                        } else {
                            if (FriendsListFragment.this.isTwitterFirstAuthentication) {
                                FriendsListFragment.this.isTwitterFirstAuthentication = false;
                                FriendsListFragment.this.authentication();
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
            } else if (TWITTER_SHARE.equalsIgnoreCase(action)) {
                handler.post(new Runnable() {

                    @Override
                    public void run() {

                        FriendsListFragment.this.fbRequest = Constant.FACEBOOK_SHARE;
                        FriendsListFragment.this.fbLogin();
                    }
                });
            } else {
                Alerts.showInfoOnly(UNABLE_TO_GET_THE_TWITTER_DATA, context);
            }
        }
    }
}
