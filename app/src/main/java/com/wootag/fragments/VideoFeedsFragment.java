/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.TagFu.fragments;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.google.common.base.Strings;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.TagFu.Constant;
import com.TagFu.MenuActivity;
import com.TagFu.R;
import com.TagFu.SuggestedUserActivity;
import com.TagFu.VideoPlayerApp;
import com.TagFu.adapter.ContactAdapter;
import com.TagFu.adapter.FriendFinderAdapter;
import com.TagFu.adapter.PostsAdapter;
import com.TagFu.async.ContactAsync;
import com.TagFu.async.FollowAsyncTask;
import com.TagFu.async.VideoFeedAsync;
import com.TagFu.connectivity.VideoDataBase;
import com.TagFu.connectivity.TagFuUploadService;
import com.TagFu.dto.Contact;
import com.TagFu.dto.ErrorResponse;
import com.TagFu.dto.Friend;
import com.TagFu.dto.Liked;
import com.TagFu.dto.MyPage;
import com.TagFu.dto.MyPageDto;
import com.TagFu.dto.People;
import com.TagFu.dto.User;
import com.TagFu.dto.VideoInfo;
import com.TagFu.model.Backend;
import com.TagFu.pulltorefresh.PullToRefreshBase;
import com.TagFu.pulltorefresh.PullToRefreshBase.OnLastItemVisibleListener;
import com.TagFu.pulltorefresh.PullToRefreshBase.OnRefreshListener;
import com.TagFu.pulltorefresh.PullToRefreshListView;
import com.TagFu.pulltorefresh.PullToRefreshScrollView;
import com.TagFu.slideout.SlideoutActivity;
import com.TagFu.ui.Image;
import com.TagFu.util.Alerts;
import com.TagFu.util.Config;
import com.TagFu.util.ContactInterface;
import com.TagFu.util.FollowInterface;
import com.TagFu.util.InviteInterface;
import com.TagFu.util.MainManager;
import com.TagFu.util.MoreVideos;
import com.TagFu.util.Util;

public class VideoFeedsFragment extends FriendsListFragment implements MoreVideos, TextWatcher, FollowInterface,
        InviteInterface, ContactInterface {

    private static final String _100 = "100";
    private static final String PLEASE_SELECT_ATLEAST_ONE_CONTACT_TO_INVITE = "Please select atleast one contact to invite";
    private static final String NO_ID_FOR_THIS_USER = "No Id for this user";
    private static final String YES = "yes";
    public static VideoFeedsFragment videoFeeds;
    private static final String ALL = "All";

    private static final String BACKGROUND_FILE_TRANSFER_SERVICE = "BackgroundFileTransferService";

    private static final String BFTS_IS_ALREADY_RUNNING = "BFTS is already running.";

    private static final String BROWSE_BY = "browse_by";
    private static final String DESCRIPTION = "description";
    private static final String DEVICE_TOKEN = "device_token";
    private static final String EMPTY = "";
    private static final String FEED_NOTIFICATION = "feednotification";
    private static final String FEED_NOTIFICATION_VISITED = "feednotificationvisited";
    private static final String FINISHED = "Finished!";
    private static final String FOLLOW = "follow";
    private static final String FOUND_THIS_INTERESTING_APP = ", Found this interesting app TagFu \n \nIt allows me to upload my video and tag the product I want to sell or myself or the location � All Inside the Video! I would love you to try www.TagFu.com/invite.html";
    private static final String FRIENDS = "friends";
    private static final String HI = "Hi ";
    private static final String HOME = "Home";
    private static final String ID = "id";
    protected static final Logger LOG = LoggerManager.getLogger();
    private static final String NAME = "name";
    private static final String NO = "no";
    private static final String NO_FRIENDS_AVAILABLE = "No friends available.";
    private static final String PAGE_NO = "page_no";
    private static final String PEOPLE = "people";
    private static final String PHOTO_PATH = "photo_path";
    private static final String PUBLIC = "public";
    private static final String SMS_BODY = "sms_body";
    private static final String SMSTO = "smsto:";
    private static final String STARTING_BFTS = "Starting BFTS.";
    private static final String UNFOLLOW = "unfollow";
    private static final String UPLOADED_WAITING_TO_PUBLISH = "Uploaded, waiting to publish!";
    private static final String USER = "user";
    private static final String USER_ID = "user_id";
    private static final String USER_NAME = "user_name";
    private static final String USERID = "userid";
    private static final String VIDEOS_PER_PAGE = "videos_per_page";
    private static final int VIDEOS_PER_PAGE_VALUE = 10;

    protected PostsAdapter adapter;
    private List<People> adapterFriendsList;
    private List<Friend> adapterList;
    protected ContactAdapter contactAdapter;
    protected List<Contact> contactFilterdList;
    protected List<Contact> contactList;
    private ListView contactListView;
    protected Context context;
    private ListView facebookFriendList;
    private ImageButton fbbackButton;
    protected LinearLayout fbFriendListLayout;
    private AutoCompleteTextView fbsearch;
    protected List<Friend> filterdList;
    protected boolean flagLoading;
    private ImageView followImageView;
    protected TextView followingFeedTextView;
    private FriendFinderAdapter friendFinder;
    protected PullToRefreshScrollView friendFinderScrollView;
    protected LinearLayout friendFinderView;
    private String gplusFriendId;
    private String gplusFriendname;
    private TextView heading;
    private LayoutInflater inflater;
    private VideoInfo info;
    private ImageButton inviteDone;
    protected boolean fromBackGround;
    protected boolean privateVideo;
    protected boolean publicVideo;
    protected boolean pullToRefresh;
    protected boolean searchRequest;
    protected LinearLayout moreSuggestedUserLayout;
    protected PullToRefreshListView moreVideosListView;
    protected List<MyPageDto> myPageDtos;
    protected List<MyPageDto> privateFeed;
    protected PostsAdapter privateFeedAdapter;
    protected ImageView privateFeedIcon;
    protected PullToRefreshListView privateFeedLsitview;
    protected List<MyPageDto> privatefeedSearch;
    protected PostsAdapter privateFeedSearchAdapter;
    protected RelativeLayout privateLayout;
    private LinearLayout privateTab;
    private TextView privateVideosTextView;
    protected TextView privateVideoTab;
    protected LinearLayout progressLayout;
    protected ImageView publicFeedIcon;
    protected RelativeLayout publicLayout;
    private LinearLayout publicTab;
    protected LinearLayout publicVideoFeedsLayout;
    protected TextView publicVideoTab;
    protected Button search, menu;
    protected PostsAdapter searchadapter;
    protected EditText searchEdit;
    private ImageView searchIcon;
    protected RelativeLayout searchLayout;
    protected TextView searchTextView;
    protected List<MyPageDto> searchVideos;
    private RelativeLayout shareContact;
    private RelativeLayout shareFacebook;
    private RelativeLayout shareGoogle;
    private RelativeLayout shareTwitter;
    private LinearLayout suggestedUserLayout;
    private TextView suggestedUsersTtextView;
    protected TextView text;
    private People user;
    protected ProgressBar videoProgress;
    private final BroadcastReceiver VideoUploadNotificationReciver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {

            final String action = intent.getAction();
            if (action != null) {
                if (Constant.HIDE_PROGRESS.equalsIgnoreCase(action)) {
                    final Handler handler = new Handler();
                    handler.post(new Runnable() {

                        @Override
                        public void run() {

                            VideoFeedsFragment.this.progressLayout.setVisibility(View.GONE);
                        }
                    });
                } else if (Constant.FILE_DELETED.equalsIgnoreCase(action)) {
                    final Handler handler = new Handler();
                    handler.post(new Runnable() {

                        @Override
                        public void run() {

                            VideoFeedsFragment.this.progressLayout.setVisibility(View.GONE);
                        }
                    });
                } else {

                    if (Constant.ACTION_FILE_UPLOAD_PROGRESS.equalsIgnoreCase(action)) {
                        final int uploadedPercentage = intent.getExtras().getInt(Constant.ACTION_FILE_UPLOAD_PROGRESS);
                        Config.setUploadedPercentage(uploadedPercentage);
                        final Handler handler = new Handler();
                        handler.post(new Runnable() {

                            @Override
                            public void run() {

                                if (VideoFeedsFragment.this.videoProgress != null) {
                                    VideoFeedsFragment.this.text.setText(R.string.uploading_);
                                    VideoFeedsFragment.this.videoProgress.setProgress(Config.getUploadedPercentage());
                                }

                            }
                        });
                    } else if (Constant.UPLOADED_PERCENTAGE.equalsIgnoreCase(action)) {
                        final Handler handler = new Handler();
                        handler.post(new Runnable() {

                            @Override
                            public void run() {

                                if (VideoFeedsFragment.this.videoProgress != null) {
                                    VideoFeedsFragment.this.progressLayout.setVisibility(View.VISIBLE);
                                    VideoFeedsFragment.this.text.setText(R.string.uploading_);
                                    VideoFeedsFragment.this.videoProgress.setVisibility(View.VISIBLE);
                                    VideoFeedsFragment.this.videoProgress.setProgress(0);
                                    VideoFeedsFragment.this.showProgress();

                                }

                            }
                        });
                    } else if (Constant.VIDEO_UPLOADED.equalsIgnoreCase(action)) {
                        final Handler handler = new Handler();
                        handler.post(new Runnable() {

                            @Override
                            public void run() {

                                /**
                                 * doing refresh feeds once video upload done
                                 */
                                if (VideoFeedsFragment.this.videoProgress != null) {
                                    VideoFeedsFragment.this.videoProgress.setVisibility(View.VISIBLE);
                                    VideoFeedsFragment.this.videoProgress.setProgress(100);
                                    VideoFeedsFragment.this.text.setText(FINISHED);
                                    VideoFeedsFragment.this.showProgress();
                                    VideoFeedsFragment.this.pullToRefresh = true;
                                    VideoFeedsFragment.this.fromBackGround = true;
                                    if ((intent != null) && (intent.getExtras() != null)
                                            && intent.getExtras().containsKey(PUBLIC)) {
                                        final int videoType = Integer.parseInt(intent.getExtras().getString(PUBLIC));
                                        if (videoType == 0) {
                                            try {
                                                final VideoFeedAsync task = new VideoFeedAsync(VideoFeedsFragment.this
                                                        .getActivity(), Constant.PRIVATE_FEED, VideoFeedsFragment.this
                                                        .getJSONRequest(1).toString(),
                                                        VideoFeedsFragment.this.searchRequest, false, false, true,
                                                        VideoFeedsFragment.this.searchTextView);
                                                task.delegate = VideoFeedsFragment.this;
                                                task.execute();
                                            } catch (final JSONException exception) {
                                                LOG.e(exception);
                                            }

                                        } else {
                                            try {
                                                final VideoFeedAsync task = new VideoFeedAsync(VideoFeedsFragment.this
                                                        .getActivity(), Constant.PUBLIC_FEED, VideoFeedsFragment.this
                                                        .getJSONRequest(1).toString(),
                                                        VideoFeedsFragment.this.searchRequest, false, false, true,
                                                        VideoFeedsFragment.this.searchTextView);
                                                task.delegate = VideoFeedsFragment.this;
                                                task.execute();
                                            } catch (final JSONException exception) {
                                                LOG.e(exception);
                                            }
                                        }
                                    }

                                }

                            }
                        });
                    } else if (Constant.FILE_UPLOADED.equalsIgnoreCase(action)) {
                        final Handler handler = new Handler();
                        handler.post(new Runnable() {

                            @Override
                            public void run() {

                                if (VideoFeedsFragment.this.videoProgress != null) {
                                    VideoFeedsFragment.this.videoProgress.setVisibility(View.VISIBLE);
                                    VideoFeedsFragment.this.videoProgress.setProgress(100);
                                    VideoFeedsFragment.this.text.setText(UPLOADED_WAITING_TO_PUBLISH);

                                }

                            }
                        });
                    } else if (FEED_NOTIFICATION.equalsIgnoreCase(action)) {
                        /**
                         * refreshing feed once get the feed notifications
                         */
                        Config.setUserID(MainManager.getInstance().getUserId());
                        VideoFeedsFragment.this.pullToRefresh = true;
                        VideoFeedsFragment.this.fromBackGround = true;
                        if (VideoFeedsFragment.this.publicVideo) {
                            try {
                                final VideoFeedAsync task = new VideoFeedAsync(VideoFeedsFragment.this.getActivity(),
                                        Constant.PUBLIC_FEED, VideoFeedsFragment.this.getJSONRequest(1).toString(),
                                        VideoFeedsFragment.this.searchRequest, false, false, true,
                                        VideoFeedsFragment.this.searchTextView);
                                task.delegate = VideoFeedsFragment.this;
                                task.execute();
                            } catch (final JSONException exception) {
                                LOG.e(exception);
                            }
                        } else {
                            try {
                                final VideoFeedAsync task = new VideoFeedAsync(VideoFeedsFragment.this.getActivity(),
                                        Constant.PRIVATE_FEED, VideoFeedsFragment.this.getJSONRequest(1).toString(),
                                        VideoFeedsFragment.this.searchRequest, false, false, false,
                                        VideoFeedsFragment.this.searchTextView);
                                task.delegate = VideoFeedsFragment.this;
                                task.execute();
                            } catch (final JSONException exception) {
                                LOG.e(exception);
                            }
                        }
                    }
                }
            }

        }
    };

    private View view;
    protected List<People> TagFuFriendsList;
    protected List<People> TagFuSearchFriendsList;

    public static JSONArray getFriendListObject(final List<Friend> list) throws JSONException {

        final JSONArray friendArray = new JSONArray();
        JSONObject values = null;
        Friend friend = null;
        for (int i = 0; i < list.size(); i++) {
            values = new JSONObject();
            friend = list.get(i);
            values.put(PHOTO_PATH, friend.getFriendImage());
            values.put(ID, friend.getFriendId());
            values.put(USER_NAME, friend.getFriendName());
            values.put(DESCRIPTION, EMPTY);
            friendArray.put(values);
        }

        return friendArray;
    }

    @Override
    public void afterTextChanged(final Editable arg0) {

        final String text = this.fbsearch.getText().toString();
        if (Constant.FACEBOOK.equalsIgnoreCase(Config.getSocialSite())) {
            this.setSearchAdapter(VideoPlayerApp.getInstance().getFbFriendsList(), text, Constant.FACEBOOK);
        } else if (Constant.GOOGLE_PLUS.equalsIgnoreCase(Config.getSocialSite())) {
            this.setSearchAdapter(VideoPlayerApp.getInstance().getGoogleFriendList(), text, Constant.GOOGLE_PLUS);
        } else if (Constant.TWITTER.equalsIgnoreCase(Config.getSocialSite())) {
            this.setSearchAdapter(VideoPlayerApp.getInstance().getTwitterFriendList(), text, Constant.TWITTER);
        } else if (Constant.CONTACTS.equalsIgnoreCase(Config.getSocialSite())) {
            this.setSearchContactAdapter(VideoPlayerApp.getInstance().getContactsList(), text);
        }
    }

    @Override
    public void beforeTextChanged(final CharSequence arg0, final int arg1, final int arg2, final int arg3) {

    }

    public void clearContactList() {

        if ((this.contactList != null) && !this.contactList.isEmpty()) {
            this.contactList.clear();
            ((BaseAdapter) this.contactListView.getAdapter()).notifyDataSetChanged();
        }

    }

    public void clearList() {

        if ((this.adapterList != null) && !this.adapterList.isEmpty()) {
            this.adapterList.clear();
            ((BaseAdapter) this.facebookFriendList.getAdapter()).notifyDataSetChanged();
        }

    }

    @Override
    public void contacts(final List<Contact> contacts) {

        if ((contacts != null) && !contacts.isEmpty()) {
            VideoPlayerApp.getInstance().setContacts(contacts);
            this.setContactAdapter(contacts);
        }
    }

    @Override
    public void follow(final String type) {

        if (FOLLOW.equalsIgnoreCase(type)) {
            this.user.setIsFollow(YES);
            this.followImageView.setImageResource(R.drawable.unfollow);
        } else {
            this.user.setIsFollow(NO);
            this.followImageView.setImageResource(R.drawable.add1);
        }
    }

    public JSONObject getJSONRequest(final int pageNo) throws JSONException {

        JSONObject request = null;
        if (this.publicVideo) {
            request = new JSONObject();
            final JSONObject obj = new JSONObject();
            obj.put(USERID, Config.getUserId());
            obj.put(Constant.DEVICE_TYPE, Constant.DEVICE_MODEL);
            obj.put(Constant.RESOLUTION, Config.getDeviceResolutionValue());
            obj.put(PAGE_NO, pageNo);
            obj.put(VIDEOS_PER_PAGE, VideoFeedsFragment.VIDEOS_PER_PAGE_VALUE);
            request.put(USER, obj);
        } else {
            request = new JSONObject();
            final JSONObject obj = new JSONObject();
            obj.put(USERID, Config.getUserId());
            obj.put(Constant.DEVICE_TYPE, Constant.DEVICE_MODEL);
            obj.put(Constant.RESOLUTION, Config.getDeviceResolutionValue());
            obj.put(PAGE_NO, pageNo);
            obj.put(VIDEOS_PER_PAGE, VideoFeedsFragment.VIDEOS_PER_PAGE_VALUE);
            request.put(USER, obj);
        }

        return request;
    }

    public String getReloution() {

        final DisplayMetrics displaymetrics = new DisplayMetrics();
        this.getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics.heightPixels + "x" + displaymetrics.widthPixels;
    }

    public JSONObject getSearchJSONRequest(final int pageNo) throws JSONException {

        final JSONObject request = new JSONObject();
        final JSONObject obj = new JSONObject();
        obj.put(NAME, this.searchEdit.getText().toString());
        obj.put(BROWSE_BY, PEOPLE);
        obj.put(USERID, Config.getUserId());
        obj.put(PAGE_NO, pageNo);
        obj.put(Constant.RESOLUTION, Config.getDeviceResolutionValue());
        obj.put(Constant.DEVICE_TYPE, Constant.DEVICE_MODEL);
        request.put(USER, obj);
        return request;
    }

    public JSONObject getSearchRequest(final int pageNo) throws JSONException {

        JSONObject result = null;
        if (this.publicVideo) {
            result = new JSONObject();
            final JSONObject obj = new JSONObject();
            obj.put(NAME, this.searchEdit.getText().toString().trim());
            obj.put(USERID, Config.getUserId());
            obj.put(Constant.DEVICE_TYPE, Constant.DEVICE_MODEL);
            obj.put(Constant.RESOLUTION, Config.getDeviceResolutionValue());
            obj.put(PAGE_NO, pageNo);
            // obj.put("videos_per_page", videosPerPage);
            result.put(USER, obj);
        } else {
            result = new JSONObject();
            final JSONObject obj = new JSONObject();
            obj.put(NAME, this.searchEdit.getText().toString().trim());
            obj.put(USERID, Config.getUserId());
            obj.put(Constant.DEVICE_TYPE, Constant.DEVICE_MODEL);
            obj.put(Constant.RESOLUTION, Config.getDeviceResolutionValue());
            obj.put(PAGE_NO, pageNo);
            // obj.put("videos_per_page", videosPerPage);
            result.put(USER, obj);
        }

        return result;
    }

    public JSONObject getSocialLoginRequest(final List<Friend> list) throws JSONException {

        final JSONObject result = new JSONObject();
        result.put(USER_ID, Config.getUserId());
        if ((list == null) || list.isEmpty()) {
            result.put(FRIENDS, EMPTY);
        } else {
            result.put(FRIENDS, VideoFeedsFragment.getFriendListObject(list));
        }
        result.put(DEVICE_TOKEN, Config.getDeviceToken());
        result.put(Constant.DEVICE_TYPE, Constant.DEVICE_MODEL);
        result.put(Constant.RESOLUTION, Config.getDeviceResolutionValue());

        return result;
    }

    @Override
    public void invite(final String friendId, final String name) {

        this.gplusFriendId = friendId;
        this.gplusFriendname = name;
        this.gPlusRequest = Constant.G_PLUS_AUTHORIZE;
        this.gPlusLogin();
    }

    /**
     * checking upload service is running or not
     */
    public boolean isServiceRunning(final Class<?> serviceClass) {

        boolean result = false;

        this.getActivity();
        final ActivityManager manager = (ActivityManager) this.getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (final RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                result = true;
                break;
            }
        }
        return result;
    }

    @Override
    public void likedList(final List<Liked> likedPeople) {

    }

    @Override
    public void onClick(final View view) {

        if (view.getId() == R.id.fbfrinedfinder) {

            if (this.contactListView != null) {
                this.contactListView.setVisibility(View.GONE);
            }

            this.facebookFriendList.setVisibility(View.VISIBLE);
            this.clearList();
            Config.setSocialSite(Constant.FACEBOOK);
            this.fbFriendListLayout.setVisibility(View.VISIBLE);

            if ((VideoPlayerApp.getInstance().getFbFriendsList() == null)
                    || VideoPlayerApp.getInstance().getFbFriendsList().isEmpty()) {

                super.onClick(view);
            } else {
                friendList = VideoPlayerApp.getInstance().getFbFriendsList();
                this.setFriendListAdapter(friendList);
            }

        } else if (view.getId() == R.id.twitterfrinedfinder) {

            if (this.contactListView != null) {
                this.contactListView.setVisibility(View.GONE);
            }

            this.facebookFriendList.setVisibility(View.VISIBLE);
            Config.setSocialSite(Constant.TWITTER);

            if ((VideoPlayerApp.getInstance().getTwitterFriendList() == null)
                    || VideoPlayerApp.getInstance().getTwitterFriendList().isEmpty()) {
                super.onClick(view);

            } else {
                friendList = VideoPlayerApp.getInstance().getTwitterFriendList();
                this.setFriendListAdapter(friendList);

            }

        } else if (view.getId() == R.id.googlefrinedfinder) {

            if (this.contactListView != null) {
                this.contactListView.setVisibility(View.GONE);
            }

            this.facebookFriendList.setVisibility(View.VISIBLE);
            Config.setSocialSite(Constant.GOOGLE_PLUS);

            if ((VideoPlayerApp.getInstance().getGoogleFriendList() == null)
                    || VideoPlayerApp.getInstance().getGoogleFriendList().isEmpty()) {
                super.onClick(view);

            } else {
                friendList = VideoPlayerApp.getInstance().getGoogleFriendList();
                this.setFriendListAdapter(friendList);
            }

        } else if (view.getId() == R.id.addImageView) {

            this.user = (People) view.getTag();
            this.followImageView = (ImageView) view;

            if (NO.equalsIgnoreCase(this.user.getIsFollow())) {
                final FollowAsyncTask task = new FollowAsyncTask(this.user.getId(), Config.getUserId(), FOLLOW,
                        this.getActivity());
                task.delegate = VideoFeedsFragment.this;
                task.execute();

            } else {
                final FollowAsyncTask task = new FollowAsyncTask(this.user.getId(), Config.getUserId(), UNFOLLOW,
                        this.getActivity());
                task.delegate = VideoFeedsFragment.this;
                task.execute();
            }

        } else if (view.getId() == R.id.contactfrinedfinder) {

            if (this.facebookFriendList != null) {
                this.facebookFriendList.setVisibility(View.GONE);
            }

            Config.setSocialSite(Constant.CONTACTS);

            if ((VideoPlayerApp.getInstance().getContactsList() == null)
                    || VideoPlayerApp.getInstance().getContactsList().isEmpty()) {
                final ContactAsync async = new ContactAsync(this.context);
                async.delegate = VideoFeedsFragment.this;
                async.execute();

            } else {
                for (int i = 0; i < VideoPlayerApp.getInstance().getContactsList().size(); i++) {
                    VideoPlayerApp.getInstance().getContactsList().get(i).setChecked(false);
                }
                this.setContactAdapter(VideoPlayerApp.getInstance().getContactsList());

            }
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        this.view = inflater.inflate(R.layout.video_feed, container, false);
        videoFeeds = this;
        this.searchRequest = false;
        this.fromBackGround = false;
        this.context = this.getActivity();
        this.pullToRefresh = false;
        Config.setUserID(MainManager.getInstance().getUserId());
        Config.setDeviceResolutionValue(this.getReloution());

        if (Util.isServiceRunning(this.context, BACKGROUND_FILE_TRANSFER_SERVICE)) {
            LOG.d(BFTS_IS_ALREADY_RUNNING);
        } else {
            LOG.d(STARTING_BFTS);
            WakefulIntentService.sendWakefulWork(this.context, TagFuUploadService.class);
        }

        this.inflater = inflater;
        this.menu = (Button) this.view.findViewById(R.id.menu);
        this.search = (Button) this.view.findViewById(R.id.settings);
        this.searchEdit = (EditText) this.view.findViewById(R.id.searchEditText);
        this.searchLayout = (RelativeLayout) this.view.findViewById(R.id.searchRL);
        this.heading = (TextView) this.view.findViewById(R.id.heading);
        this.searchTextView = (TextView) this.view.findViewById(R.id.feedsearchView);
        this.followingFeedTextView = (TextView) this.view.findViewById(R.id.followingfeedsTextView);
        this.progressLayout = (LinearLayout) this.view.findViewById(R.id.progressLayout);
        this.text = (TextView) this.view.findViewById(R.id.text);
        this.videoProgress = (ProgressBar) this.view.findViewById(R.id.videoprogress);

        this.heading.setText(HOME);
        this.showProgress();
        this.registerBroadcastReceiver();

        this.publicVideoTab = (TextView) this.view.findViewById(R.id.publicVideosTab);
        this.privateVideoTab = (TextView) this.view.findViewById(R.id.privateVideosTab);
        this.publicFeedIcon = (ImageView) this.view.findViewById(R.id.publicfeedicon);
        this.privateFeedIcon = (ImageView) this.view.findViewById(R.id.privatefeedicon);
        this.publicTab = (LinearLayout) this.view.findViewById(R.id.publicTab);
        this.privateTab = (LinearLayout) this.view.findViewById(R.id.privateTab);
        this.moreVideosListView = (PullToRefreshListView) this.view.findViewById(R.id.publicFeedListView);
        this.privateFeedLsitview = (PullToRefreshListView) this.view.findViewById(R.id.privateFeedListView);
        this.privateLayout = (RelativeLayout) this.view.findViewById(R.id.privateLayout);
        this.publicLayout = (RelativeLayout) this.view.findViewById(R.id.publicLayout);
        this.privateVideosTextView = (TextView) this.view.findViewById(R.id.privateVideosTextView);
        this.privateVideosTextView.setVisibility(View.GONE);
        this.publicVideo = true;
        this.publicVideoFeedsLayout = (LinearLayout) this.view.findViewById(R.id.bodyLayout);

        this.myPageDtos = new ArrayList<MyPageDto>();
        this.searchVideos = new ArrayList<MyPageDto>();

        this.privateFeed = new ArrayList<MyPageDto>();
        this.privatefeedSearch = new ArrayList<MyPageDto>();
        this.loadFriendFinderViews();

        this.getFeedResponseFromCache();

        this.searchEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(final TextView ignored, final int actionId, final KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    VideoFeedsFragment.this.performSearch();
                    return true;
                }
                return false;
            }
        });

        /**
         * Defining public video tab action and changing visibility and gone of public videos's listview
         */
        this.publicTab.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                VideoFeedsFragment.this.followingFeedTextView.setVisibility(View.GONE);
                VideoFeedsFragment.this.privateLayout.setVisibility(View.GONE);
                VideoFeedsFragment.this.publicLayout.setVisibility(View.VISIBLE);
                VideoFeedsFragment.this.friendFinderView.setVisibility(View.GONE);

                VideoFeedsFragment.this.publicFeedIcon.setImageResource(R.drawable.videofeeds_f);
                VideoFeedsFragment.this.privateFeedIcon.setImageResource(R.drawable.privatefeeds);
                VideoFeedsFragment.this.publicVideoTab.setTextColor(VideoFeedsFragment.this.getResources().getColor(
                        R.color.twitter_bg_color));
                VideoFeedsFragment.this.privateVideoTab.setTextColor(VideoFeedsFragment.this.getResources().getColor(
                        R.color.gray));

                VideoFeedsFragment.this.publicVideo = true;
                VideoFeedsFragment.this.privateVideo = false;

                if (VideoFeedsFragment.this.searchRequest) {
                    VideoFeedsFragment.this.searchadapter = new PostsAdapter(VideoFeedsFragment.this.context, 0,
                            VideoFeedsFragment.this.searchVideos, Constant.VIDEO_FEEDS, videoFeeds);
                    VideoFeedsFragment.this.moreVideosListView.setAdapter(VideoFeedsFragment.this.searchadapter);
                    VideoFeedsFragment.this.searchadapter.notifyDataSetChanged();
                    if ((VideoFeedsFragment.this.searchVideos == null)
                            || VideoFeedsFragment.this.searchVideos.isEmpty()) {

                        VideoFeedsFragment.this.flagLoading = true;
                        try {
                            final VideoFeedAsync task = new VideoFeedAsync(VideoFeedsFragment.this.getActivity(),
                                    Constant.PUBLIC_FEED, VideoFeedsFragment.this.getJSONRequest(1).toString(),
                                    VideoFeedsFragment.this.searchRequest, true, false, false,
                                    VideoFeedsFragment.this.searchTextView);
                            task.delegate = VideoFeedsFragment.this;
                            task.execute();
                        } catch (final JSONException exception) {
                            LOG.e(exception);
                        }
                    }
                } else {
                    VideoFeedsFragment.this.adapter = new PostsAdapter(VideoFeedsFragment.this.context, 0,
                            VideoFeedsFragment.this.myPageDtos, Constant.VIDEO_FEEDS, videoFeeds);
                    VideoFeedsFragment.this.moreVideosListView.setAdapter(VideoFeedsFragment.this.adapter);
                    VideoFeedsFragment.this.adapter.notifyDataSetChanged();
                    if ((VideoFeedsFragment.this.myPageDtos == null) || VideoFeedsFragment.this.myPageDtos.isEmpty()) {
                        if ((Config.getFollowingCount() > 0)
                                && ((VideoFeedsFragment.this.myPageDtos == null) || ((VideoFeedsFragment.this.myPageDtos != null) && (VideoFeedsFragment.this.myPageDtos
                                        .size() == 0)))) {
                            VideoFeedsFragment.this.followingFeedTextView.setVisibility(View.VISIBLE);
                            VideoFeedsFragment.this.followingFeedTextView.setText(VideoFeedsFragment.this
                                    .getResources().getString(R.string.follower_videos_availbility));
                        } else {
                            VideoFeedsFragment.this.friendFinderView.setVisibility(View.VISIBLE);
                            VideoFeedsFragment.this.publicLayout.setVisibility(View.GONE);
                            try {
                                final VideoFeedAsync task = new VideoFeedAsync(VideoFeedsFragment.this.getActivity(),
                                        Constant.PUBLIC_FEED, VideoFeedsFragment.this.getJSONRequest(1).toString(),
                                        VideoFeedsFragment.this.searchRequest, false, false, false,
                                        VideoFeedsFragment.this.searchTextView);
                                task.delegate = VideoFeedsFragment.this;
                                task.execute();
                            } catch (final JSONException exception) {
                                LOG.e(exception);
                            }
                        }

                    } else {
                        VideoFeedsFragment.this.friendFinderView.setVisibility(View.GONE);
                    }
                }
            }
        });

        this.privateTab.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                VideoFeedsFragment.this.followingFeedTextView.setVisibility(View.GONE);
                VideoFeedsFragment.this.publicLayout.setVisibility(View.GONE);
                VideoFeedsFragment.this.privateLayout.setVisibility(View.VISIBLE);
                VideoFeedsFragment.this.friendFinderView.setVisibility(View.GONE);
                VideoFeedsFragment.this.publicFeedIcon.setImageResource(R.drawable.videofeeds);
                VideoFeedsFragment.this.privateFeedIcon.setImageResource(R.drawable.privatefeeds_f);
                VideoFeedsFragment.this.publicVideoTab.setTextColor(VideoFeedsFragment.this.getResources().getColor(
                        R.color.gray));
                VideoFeedsFragment.this.privateVideoTab.setTextColor(VideoFeedsFragment.this.getResources().getColor(
                        R.color.twitter_bg_color));

                VideoFeedsFragment.this.publicVideo = false;
                VideoFeedsFragment.this.privateVideo = true;
                if (VideoFeedsFragment.this.searchRequest) {
                    VideoFeedsFragment.this.privateFeedSearchAdapter = new PostsAdapter(
                            VideoFeedsFragment.this.context, 0, VideoFeedsFragment.this.privatefeedSearch,
                            Constant.VIDEO_FEEDS, videoFeeds);
                    VideoFeedsFragment.this.privateFeedLsitview
                            .setAdapter(VideoFeedsFragment.this.privateFeedSearchAdapter);
                    VideoFeedsFragment.this.privateFeedSearchAdapter.notifyDataSetChanged();
                    if ((VideoFeedsFragment.this.privatefeedSearch == null)
                            || VideoFeedsFragment.this.privatefeedSearch.isEmpty()) {

                        VideoFeedsFragment.this.flagLoading = true;
                        try {
                            final VideoFeedAsync task = new VideoFeedAsync(VideoFeedsFragment.this.getActivity(),
                                    Constant.PRIVATE_FEED, VideoFeedsFragment.this.getJSONRequest(1).toString(),
                                    VideoFeedsFragment.this.searchRequest, true, false, false,
                                    VideoFeedsFragment.this.searchTextView);
                            task.delegate = VideoFeedsFragment.this;
                            task.execute();
                        } catch (final JSONException exception) {
                            LOG.e(exception);
                        }
                    }
                } else {
                    VideoFeedsFragment.this.privateFeedAdapter = new PostsAdapter(VideoFeedsFragment.this.context, 0,
                            VideoFeedsFragment.this.privateFeed, Constant.VIDEO_FEEDS, videoFeeds);
                    VideoFeedsFragment.this.privateFeedLsitview.setAdapter(VideoFeedsFragment.this.privateFeedAdapter);
                    VideoFeedsFragment.this.privateFeedAdapter.notifyDataSetChanged();
                    if ((VideoFeedsFragment.this.privateFeed == null) || VideoFeedsFragment.this.privateFeed.isEmpty()) {
                        VideoFeedsFragment.this.getPrivateFeedResponseFromCache();
                    }
                }

            }
        });

        // Set a listener to be invoked when the list should be refreshed.
        this.moreVideosListView.setOnRefreshListener(new OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(final PullToRefreshBase<ListView> refreshView) {

                VideoFeedsFragment.this.showProgress();
                VideoFeedsFragment.this.pullToRefresh = true;
                if (VideoFeedsFragment.this.searchRequest) {
                    try {
                        final VideoFeedAsync task = new VideoFeedAsync(VideoFeedsFragment.this.getActivity(),
                                Constant.PUBLIC_FEED, VideoFeedsFragment.this.getSearchRequest(1).toString(),
                                VideoFeedsFragment.this.searchRequest, false, false, false,
                                VideoFeedsFragment.this.searchTextView);
                        task.delegate = VideoFeedsFragment.this;
                        task.execute();
                    } catch (final JSONException exception) {
                        LOG.e(exception);
                    }
                } else {
                    try {
                        final Intent intent = new Intent(FEED_NOTIFICATION_VISITED);
                        VideoFeedsFragment.this.getActivity().sendBroadcast(intent);
                        final VideoFeedAsync task = new VideoFeedAsync(VideoFeedsFragment.this.getActivity(),
                                Constant.PUBLIC_FEED, VideoFeedsFragment.this.getJSONRequest(1).toString(),
                                VideoFeedsFragment.this.searchRequest, false, false, true,
                                VideoFeedsFragment.this.searchTextView);
                        task.delegate = VideoFeedsFragment.this;
                        task.execute();
                    } catch (final JSONException exception) {
                        LOG.e(exception);
                    }
                }
            }

        });

        // Add an end-of-list listener
        this.moreVideosListView.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

            @Override
            public void onLastItemVisible() {

                if (!VideoFeedsFragment.this.flagLoading) {
                    if (VideoFeedsFragment.this.searchRequest) {
                        final int offset = VideoFeedsFragment.this.searchVideos.size();
                        if ((offset % VideoFeedsFragment.VIDEOS_PER_PAGE_VALUE) == 0) {
                            VideoFeedsFragment.this.flagLoading = true;
                            final int pageNo = (offset / VideoFeedsFragment.VIDEOS_PER_PAGE_VALUE) + 1;
                            try {
                                final VideoFeedAsync task = new VideoFeedAsync(VideoFeedsFragment.this.getActivity(),
                                        Constant.PUBLIC_FEED, VideoFeedsFragment.this.getSearchRequest(pageNo)
                                                .toString(), VideoFeedsFragment.this.searchRequest, true, false, false,
                                        VideoFeedsFragment.this.searchTextView);
                                task.delegate = VideoFeedsFragment.this;
                                task.execute();
                            } catch (final JSONException exception) {
                                LOG.e(exception);
                            }
                        }

                    } else {
                        final int offset = VideoFeedsFragment.this.myPageDtos.size();
                        if ((offset % VideoFeedsFragment.VIDEOS_PER_PAGE_VALUE) == 0) {
                            VideoFeedsFragment.this.flagLoading = true;
                            final int pageNo = (offset / VideoFeedsFragment.VIDEOS_PER_PAGE_VALUE) + 1;
                            try {
                                final VideoFeedAsync task = new VideoFeedAsync(VideoFeedsFragment.this.getActivity(),
                                        Constant.PUBLIC_FEED,
                                        VideoFeedsFragment.this.getJSONRequest(pageNo).toString(),
                                        VideoFeedsFragment.this.searchRequest, true, false, false,
                                        VideoFeedsFragment.this.searchTextView);
                                task.delegate = VideoFeedsFragment.this;
                                task.execute();
                            } catch (final JSONException exception) {
                                LOG.e(exception);
                            }
                        }
                    }
                }
            }
        });

        // Set a listener to be invoked when the list should be refreshed.
        this.privateFeedLsitview.setOnRefreshListener(new OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(final PullToRefreshBase<ListView> refreshView) {

                VideoFeedsFragment.this.showProgress();
                VideoFeedsFragment.this.pullToRefresh = true;
                if (VideoFeedsFragment.this.searchRequest) {
                    try {
                        final VideoFeedAsync task = new VideoFeedAsync(VideoFeedsFragment.this.getActivity(),
                                Constant.PRIVATE_FEED, VideoFeedsFragment.this.getSearchRequest(1).toString(),
                                VideoFeedsFragment.this.searchRequest, false, false, false,
                                VideoFeedsFragment.this.searchTextView);
                        task.delegate = VideoFeedsFragment.this;
                        task.execute();
                    } catch (final JSONException exception) {
                        LOG.e(exception);
                    }
                } else {
                    try {
                        final VideoFeedAsync task = new VideoFeedAsync(VideoFeedsFragment.this.getActivity(),
                                Constant.PRIVATE_FEED, VideoFeedsFragment.this.getJSONRequest(1).toString(),
                                VideoFeedsFragment.this.searchRequest, false, false, false,
                                VideoFeedsFragment.this.searchTextView);
                        task.delegate = VideoFeedsFragment.this;
                        task.execute();
                    } catch (final JSONException exception) {
                        LOG.e(exception);
                    }
                }
            }

        });

        // Add an end-of-list listener
        this.privateFeedLsitview.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

            @Override
            public void onLastItemVisible() {

                if (!VideoFeedsFragment.this.flagLoading) {

                    if (VideoFeedsFragment.this.searchRequest) {
                        final int offset = VideoFeedsFragment.this.privatefeedSearch.size();
                        if ((offset % VideoFeedsFragment.VIDEOS_PER_PAGE_VALUE) == 0) {
                            VideoFeedsFragment.this.flagLoading = true;
                            final int pageNo = (offset / VideoFeedsFragment.VIDEOS_PER_PAGE_VALUE) + 1;
                            try {
                                final VideoFeedAsync task = new VideoFeedAsync(VideoFeedsFragment.this.getActivity(),
                                        Constant.PRIVATE_FEED, VideoFeedsFragment.this.getSearchRequest(pageNo)
                                                .toString(), VideoFeedsFragment.this.searchRequest, true, false, false,
                                        VideoFeedsFragment.this.searchTextView);
                                task.delegate = VideoFeedsFragment.this;
                                task.execute();
                            } catch (final JSONException exception) {
                                LOG.e(exception);
                            }
                        }

                    } else {
                        final int offset = VideoFeedsFragment.this.privateFeed.size();
                        if ((offset % VideoFeedsFragment.VIDEOS_PER_PAGE_VALUE) == 0) {
                            VideoFeedsFragment.this.flagLoading = true;
                            final int pageNo = (offset / VideoFeedsFragment.VIDEOS_PER_PAGE_VALUE) + 1;
                            try {
                                final VideoFeedAsync task = new VideoFeedAsync(VideoFeedsFragment.this.getActivity(),
                                        Constant.PRIVATE_FEED, VideoFeedsFragment.this.getJSONRequest(pageNo)
                                                .toString(), VideoFeedsFragment.this.searchRequest, true, false, false,
                                        VideoFeedsFragment.this.searchTextView);
                                task.delegate = VideoFeedsFragment.this;
                                task.execute();
                            } catch (final JSONException exception) {
                                LOG.e(exception);
                            }
                        }
                    }

                }
            }
        });

        this.menu.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                final int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60,
                        VideoFeedsFragment.this.getResources().getDisplayMetrics());
                SlideoutActivity.prepare(VideoFeedsFragment.this.getActivity(), R.id.videofeedView, width);
                VideoFeedsFragment.this.startActivity(new Intent(VideoFeedsFragment.this.getActivity(),
                        MenuActivity.class));
                VideoFeedsFragment.this.getActivity().overridePendingTransition(0, 0);
            }
        });

        this.search.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                if (VideoFeedsFragment.this.searchLayout.isShown()) {
                    VideoFeedsFragment.this.searchTextView.setVisibility(View.GONE);
                    VideoFeedsFragment.this.searchLayout.setVisibility(View.GONE);
                    VideoFeedsFragment.this.search.setBackgroundResource(R.drawable.search1);
                    VideoFeedsFragment.this.searchEdit.setText(EMPTY);
                    VideoFeedsFragment.this.searchRequest = false;
                    if (VideoFeedsFragment.this.friendFinderView.isShown()) {
                        VideoFeedsFragment.this.moreSuggestedUserLayout.setVisibility(View.VISIBLE);
                        VideoFeedsFragment.this.loadData(VideoFeedsFragment.this.TagFuFriendsList);
                    } else {
                        if (VideoFeedsFragment.this.publicVideo) {
                            /**
                             * if videos size is zero need to check mypage following count if is more than zero need to
                             * show this message other wise need to show friend finder view
                             */
                            if ((Config.getFollowingCount() > 0)
                                    && ((VideoFeedsFragment.this.myPageDtos == null) || ((VideoFeedsFragment.this.myPageDtos != null) && (VideoFeedsFragment.this.myPageDtos
                                            .size() == 0)))) {
                                VideoFeedsFragment.this.followingFeedTextView.setVisibility(View.VISIBLE);
                            }
                            if (VideoFeedsFragment.this.adapter != null) {
                                VideoFeedsFragment.this.moreVideosListView.setAdapter(VideoFeedsFragment.this.adapter);
                            }
                        } else {
                            if (VideoFeedsFragment.this.privateFeedAdapter != null) {
                                VideoFeedsFragment.this.privateFeedLsitview
                                        .setAdapter(VideoFeedsFragment.this.privateFeedAdapter);
                            }
                        }
                    }
                } else {
                    if (VideoFeedsFragment.this.friendFinderView.isShown()) {
                        VideoFeedsFragment.this.moreSuggestedUserLayout.setVisibility(View.GONE);
                    }
                    final Animation bottomUp = AnimationUtils.loadAnimation(VideoFeedsFragment.this.getActivity(),
                            R.anim.bottom_up);
                    VideoFeedsFragment.this.searchLayout.startAnimation(bottomUp);
                    VideoFeedsFragment.this.searchLayout.setVisibility(View.VISIBLE);
                    VideoFeedsFragment.this.searchRequest = true;
                    VideoFeedsFragment.this.search.setBackgroundResource(R.drawable.cancelbutton);
                    VideoFeedsFragment.this.searchVideos.clear();
                    if (VideoFeedsFragment.this.publicVideo) {
                        if (VideoFeedsFragment.this.searchadapter != null) {
                            VideoFeedsFragment.this.moreVideosListView
                                    .setAdapter(VideoFeedsFragment.this.searchadapter);
                            VideoFeedsFragment.this.searchadapter.notifyDataSetChanged();
                        }
                    } else {
                        if (VideoFeedsFragment.this.privateFeedSearchAdapter != null) {
                            VideoFeedsFragment.this.privateFeedLsitview
                                    .setAdapter(VideoFeedsFragment.this.privateFeedSearchAdapter);
                            VideoFeedsFragment.this.privateFeedSearchAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });

        return this.view;
    }

    @Override
    public void onDestroy() {

        if (this.VideoUploadNotificationReciver != null) {
            this.getActivity().getApplicationContext().unregisterReceiver(this.VideoUploadNotificationReciver);
        }

        Util.clearImageCache(this.context);

        super.onDestroy();
    }

    @Override
    public void onPause() {

        this.getActivity().overridePendingTransition(0, 0);
        super.onPause();
    }

    @Override
    public void onResume() {

        this.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.getActivity().overridePendingTransition(0, 0);
        if (MainManager.getInstance().getUserId() != null) {
            Config.setUserID(MainManager.getInstance().getUserId());
        }
        super.onResume();
    }

    @Override
    public void onTextChanged(final CharSequence ignored, final int start, final int before, final int count) {

    }

    @Override
    public void processFinish(final List<Friend> friendList, final String socialSite) {

        if ((friendList == null) || friendList.isEmpty()) {
            Alerts.showInfoOnly(NO_FRIENDS_AVAILABLE, this.context);

        } else {
            if (Constant.TWITTER.equalsIgnoreCase(socialSite)) {
                LOG.i("process finish received ");
                VideoPlayerApp.getInstance().setTwitterFriendList(friendList);
                try {
                    new SocialFriendFinderAsync(this.getSocialLoginRequest(friendList).toString()).execute();
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }

            } else if (Constant.FACEBOOK.equalsIgnoreCase(socialSite)) {
                LOG.i("process finish received fb ");
                super.processFinish(friendList, socialSite);
                LOG.i("fb oncomplete frnds.size() " + friendList.size());

            } else if (Constant.GOOGLE_PLUS.equalsIgnoreCase(socialSite)) {
                LOG.i("process finish received gplus ");
                VideoPlayerApp.getInstance().setGoogleFriendList(friendList);
                try {
                    new SocialFriendFinderAsync(this.getSocialLoginRequest(friendList).toString()).execute();
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }
                LOG.i("fb oncomplete frnds.size() " + friendList.size());
            }
        }

    }

    @Override
    public void sendList(final List<Friend> list) {

        VideoPlayerApp.getInstance().setFacebookFriendsList(list);
        try {
            new SocialFriendFinderAsync(this.getSocialLoginRequest(list).toString()).execute();
        } catch (final JSONException exception) {
            LOG.e(exception);
        }
    }

    @Override
    public void userDetailsFinished(final User userDetails, final String socialsite) {

        super.userDetailsFinished(userDetails, socialsite);
        if ((this.gplusFriendId != null) && (this.gplusFriendname != null)) {
            this.inviteFriend(this.gplusFriendId, this.gplusFriendname);
        }
    }

    @Override
    public void videoList(final List<MyPageDto> video) {

    }

    @Override
    public void videoList(final List<MyPageDto> video, final String type) {

    }

    @Override
    public void videoList(final List<MyPageDto> video, final String type, final boolean next) {

        this.flagLoading = false;
        this.followingFeedTextView.setVisibility(View.GONE);

        if (this.pullToRefresh) {
            this.moreVideosListView.onRefreshComplete();
            this.privateFeedLsitview.onRefreshComplete();
            this.friendFinderScrollView.onRefreshComplete();
        }
        if (Constant.PRIVATE_FEED.equalsIgnoreCase(type)) {
            if (this.privateVideo || this.fromBackGround) {
                this.fromBackGround = false;
                this.setPrivateFeed(video);
            }
        } else {
            if (this.pullToRefresh) {
                this.clearListAndAddNewVideos(video, this.searchRequest, next);
                this.pullToRefresh = false;
            } else if (this.searchRequest) {
                if ((video != null) && !video.isEmpty()) {
                    for (int i = 0; i < video.size(); i++) {
                        this.searchVideos.add(video.get(i));
                    }
                }
                if (this.searchadapter == null) {
                    this.searchadapter = new PostsAdapter(this.context, 0, this.searchVideos, Constant.VIDEO_FEEDS,
                            videoFeeds);
                    this.moreVideosListView.setAdapter(this.searchadapter);
                } else {
                    this.searchadapter.notifyDataSetChanged();
                }

                if ((this.searchVideos == null) || this.searchVideos.isEmpty()) {
                    this.searchTextView.setVisibility(View.VISIBLE);
                } else {
                    this.searchTextView.setVisibility(View.GONE);
                }

            } else {
                if ((video != null) && !video.isEmpty()) {
                    for (int i = 0; i < video.size(); i++) {
                        this.myPageDtos.add(video.get(i));
                    }
                }
                if ((this.myPageDtos == null) || ((this.myPageDtos != null) && (this.myPageDtos.size() == 0))) {
                    if (Config.getFollowingCount() > 0) {
                        this.followingFeedTextView.setVisibility(View.VISIBLE);
                        this.followingFeedTextView.setText(this.getResources().getString(
                                R.string.follower_videos_availbility));
                    } else {
                        this.followingFeedTextView.setVisibility(View.GONE);
                        if (!this.friendFinderView.isShown() && next) {
                            final FriendFinderAsync req = new FriendFinderAsync(Config.getUserId(), 1, false,
                                    this.searchRequest);
                            req.execute();
                            this.friendFinderView.setVisibility(View.VISIBLE);
                            this.publicLayout.setVisibility(View.GONE);
                            this.privateLayout.setVisibility(View.GONE);
                        }
                    }
                } else {
                    this.friendFinderView.setVisibility(View.GONE);
                    this.publicLayout.setVisibility(View.VISIBLE);
                    this.privateLayout.setVisibility(View.GONE);
                    if (this.adapter == null) {
                        this.adapter = new PostsAdapter(this.context, 0, this.myPageDtos, Constant.VIDEO_FEEDS,
                                videoFeeds);
                        this.moreVideosListView.setAdapter(this.adapter);
                    } else {
                        this.adapter.notifyDataSetChanged();
                    }
                }
            }
        }
        // }
        this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }

    /**
     * clearing the old list and add all new videos .this will be done while user using pulltorefresh
     */
    private void clearListAndAddNewVideos(final List<MyPageDto> video, final boolean searchRequest, final boolean next) {

        if (searchRequest) {
            this.searchadapter = new PostsAdapter(this.context, 0, this.searchVideos, Constant.VIDEO_FEEDS, videoFeeds);
            this.moreVideosListView.setAdapter(this.searchadapter);
            this.searchVideos.clear();
            if (this.searchadapter != null) {
                this.searchadapter.clear();
                this.searchadapter.notifyDataSetChanged();
            }
            if ((video != null) && !video.isEmpty()) {
                this.searchVideos.addAll(video);
            }
            if ((this.searchVideos == null) || this.searchVideos.isEmpty()) {
                this.searchTextView.setVisibility(View.VISIBLE);
            } else {
                this.searchTextView.setVisibility(View.GONE);
            }

        } else {
            if ((video == null) || ((video != null) && !video.isEmpty())) {
                if ((Config.getFollowingCount() > 0)
                        && ((this.myPageDtos == null) || ((this.myPageDtos != null) && (this.myPageDtos.size() == 0)))) {
                    this.followingFeedTextView.setVisibility(View.VISIBLE);
                    this.followingFeedTextView.setText(this.getResources().getString(
                            R.string.follower_videos_availbility));
                } else {
                    this.followingFeedTextView.setVisibility(View.GONE);
                    if (!this.friendFinderView.isShown() && next) {
                        this.myPageDtos.clear();
                        final FriendFinderAsync req = new FriendFinderAsync(Config.getUserId(), 1, false, searchRequest);
                        req.execute();
                        this.friendFinderView.setVisibility(View.VISIBLE);
                        this.publicLayout.setVisibility(View.GONE);
                        this.privateLayout.setVisibility(View.GONE);
                    }
                }
            } else {
                this.friendFinderView.setVisibility(View.GONE);
                this.publicLayout.setVisibility(View.VISIBLE);
                this.privateLayout.setVisibility(View.VISIBLE);
                if ((video != null) && !video.isEmpty()) {
                    this.myPageDtos = video;
                }
                this.adapter = new PostsAdapter(this.context, 0, this.myPageDtos, Constant.VIDEO_FEEDS, videoFeeds);
                this.moreVideosListView.setAdapter(this.adapter);
            }
            this.moreVideosListView.onRefreshComplete();
        }
    }

    private void clearListAndAddPrivateFeeds(final List<MyPageDto> video, final boolean searchRequest) {

        if (searchRequest) {
            this.privateFeedSearchAdapter = new PostsAdapter(this.context, 0, this.privatefeedSearch,
                    Constant.VIDEO_FEEDS, videoFeeds);
            this.privateFeedLsitview.setAdapter(this.privateFeedSearchAdapter);
            this.privatefeedSearch.clear();
            if (this.privateFeedSearchAdapter != null) {
                this.privateFeedSearchAdapter.clear();
                this.privateFeedSearchAdapter.notifyDataSetChanged();
            }
            if ((video != null) && !video.isEmpty()) {
                this.privatefeedSearch.addAll(video);
                if (this.privateFeedSearchAdapter != null) {
                    this.privateFeedSearchAdapter.notifyDataSetChanged();
                }
            }

            this.searchTextView.setText(this.getResources().getString(R.string.no_search_text));
            if ((this.privatefeedSearch == null) || this.privatefeedSearch.isEmpty()) {
                this.searchTextView.setVisibility(View.VISIBLE);
            } else {
                this.searchTextView.setVisibility(View.GONE);
            }
        } else {
            if ((video == null) || ((video != null) && !video.isEmpty() && Util.isConnected(this.context))) {
                if (Config.getPrivateGroupCount() == 0) {
                    if (this.privateFeed != null) {
                        this.privateFeed.clear();
                    }
                    final FriendFinderAsync req = new FriendFinderAsync(Config.getUserId(), 1, false, searchRequest);
                    req.execute();
                    this.friendFinderView.setVisibility(View.VISIBLE);
                    this.publicLayout.setVisibility(View.GONE);
                    this.privateLayout.setVisibility(View.GONE);
                }
            } else if ((this.privateFeed == null)
                    || ((this.privateFeed != null) && (this.privateFeed.size() <= 0) && !Util.isConnected(this.context))) {
                this.privateVideosTextView.setText(this.getResources().getString(
                        R.string.no_connectivity_feed_privattext));
                this.privateVideosTextView.setVisibility(View.VISIBLE);
            } else {
                if ((video != null) && !video.isEmpty()) {
                    this.privateFeed = video;
                }
                this.privateFeedAdapter = new PostsAdapter(this.context, 0, this.privateFeed, Constant.VIDEO_FEEDS,
                        this);
                this.privateFeedLsitview.setAdapter(this.privateFeedAdapter);
            }
        }
        this.privateFeedLsitview.onRefreshComplete();

    }

    private String getDescription(final People suggestedUsers) {

        final StringBuffer description = new StringBuffer();

        if (suggestedUsers.getPosition() != null) {
            description.append(suggestedUsers.getPosition()).append(" | ");
        }
        if (suggestedUsers.getCountry() != null) {
            description.append(suggestedUsers.getCountry()).append(" | ");
        }
        if (suggestedUsers.getEmailId() != null) {
            description.append(suggestedUsers.getEmailId());
        }

        return description.toString();
    }

    /**
     * getting the response from cache and push the screen then after making auto refresh
     */
    private void getFeedResponseFromCache() {

        // test
        Object myPageObject;
        try {
            myPageObject = Backend.mypageVideosFromCache();
            if (myPageObject instanceof MyPage) {
                final MyPage reposne = (MyPage) myPageObject;
                if (reposne.getTotalNoOfFollowing() != null) {
                    Config.setFollowingCount(Integer.parseInt(reposne.getTotalNoOfFollowing()));
                }
                if (reposne.getTotalNoOfPrivateGroupPeople() != null) {
                    Config.setPrivateGroupCount(Integer.parseInt(reposne.getTotalNoOfPrivateGroupPeople()));
                }
            }
        } catch (final JSONException exception1) {
            LOG.e(exception1);
        }

        // end test
        Object returnObject = null;
        try {
            returnObject = Backend.getVideoFeedFromCache();
            if (returnObject == null) {
                try {
                    final VideoFeedAsync task = new VideoFeedAsync(this.getActivity(), Constant.PUBLIC_FEED, this
                            .getJSONRequest(1).toString(), this.searchRequest, true, true, false, this.searchTextView);
                    task.delegate = this;
                    task.execute();
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }
            } else {
                try {
                    this.loadResponse(returnObject, Constant.PUBLIC_FEED);
                    this.pullToRefresh = true;
                    final VideoFeedAsync task = new VideoFeedAsync(this.getActivity(), Constant.PUBLIC_FEED, this
                            .getJSONRequest(1).toString(), false, false, false, true, this.searchTextView);
                    task.delegate = this;
                    task.execute();
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }
            }
        } catch (final JSONException exception) {
            LOG.e(exception);
        }

    }

    private View getSuggestedFriendsView(final People suggestedUsers) {

        final View convertView = this.inflater.inflate(R.layout.suggested_friends, null);
        final ImageView profileImageView = (ImageView) convertView.findViewById(R.id.profileImageView);
        final ImageView addImageView = (ImageView) convertView.findViewById(R.id.addImageView);
        final TextView nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
        final TextView profileDetailsTextView = (TextView) convertView.findViewById(R.id.profileDetailsTextView);

        final LinearLayout userDetails = (LinearLayout) convertView.findViewById(R.id.browseuserDetails);

        if (Strings.isNullOrEmpty(suggestedUsers.getUrl())) {
            profileImageView.setImageResource(R.drawable.member);

        } else {
            Image.displayImage(suggestedUsers.getUrl(), this.getActivity(), profileImageView, 0);
        }

        if (suggestedUsers.getUserName() == null) {
            nameTextView.setText(EMPTY);
        } else {
            nameTextView.setText(suggestedUsers.getUserName());
        }
        profileDetailsTextView.setText(this.getDescription(suggestedUsers));
        if (suggestedUsers.getIsFollow() != null) {
            if (NO.equalsIgnoreCase(suggestedUsers.getIsFollow())) {
                addImageView.setImageResource(R.drawable.add1);
            } else {
                addImageView.setImageResource(R.drawable.unfollow);
            }
        }
        userDetails.setTag(suggestedUsers);
        profileImageView.setTag(suggestedUsers);
        addImageView.setTag(suggestedUsers);
        addImageView.setOnClickListener(this);
        profileImageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View view) {

                final People peopleDetails = (People) view.getTag();
                final String userId = peopleDetails.getId();
                if (!Config.getUserId().equalsIgnoreCase(userId)) {
                    final int id = Integer.parseInt(userId);
                    if (id > 0) {
                        final OtherUserFragment fragment = new OtherUserFragment(); // object of next fragment
                        final Bundle bundle = new Bundle();
                        bundle.putString(USERID, String.valueOf(id));
                        bundle.putString(Constant.ROOT_FRAGMENT, Constant.VIDEO_FEEDS);
                        fragment.setArguments(bundle);
                        BaseFragment.tabActivity.pushFragments(R.id.feedTab, fragment, Constant.OTHERS_PAGE,
                                VideoFeedsFragment.this, Constant.HOME);

                    } else {
                        Alerts.showInfoOnly(NO_ID_FOR_THIS_USER, VideoFeedsFragment.this.context);
                    }
                }
            }
        });

        userDetails.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View view) {

                final People peopleDetails = (People) view.getTag();
                final String userId = peopleDetails.getId();
                if (!Config.getUserId().equalsIgnoreCase(userId)) {
                    final int id = Integer.parseInt(userId);
                    if (id > 0) {
                        final OtherUserFragment fragment = new OtherUserFragment(); // object of next fragment
                        final Bundle bundle = new Bundle();
                        bundle.putString(USERID, String.valueOf(id));
                        bundle.putString(Constant.ROOT_FRAGMENT, Constant.VIDEO_FEEDS);
                        fragment.setArguments(bundle);
                        BaseFragment.tabActivity.pushFragments(R.id.feedTab, fragment, Constant.OTHERS_PAGE,
                                VideoFeedsFragment.this, Constant.HOME);

                    } else {
                        Alerts.showInfoOnly(NO_ID_FOR_THIS_USER, VideoFeedsFragment.this.context);
                    }
                }
            }
        });

        return convertView;
    }

    private void loadFriendFinderViews() {

        this.fbsearch = (AutoCompleteTextView) this.view.findViewById(R.id.invitefriendsearch);
        this.searchIcon = (ImageView) this.view.findViewById(R.id.socialiconimageview);
        this.fbsearch.addTextChangedListener(this);

        this.TagFuFriendsList = new ArrayList<People>();
        this.TagFuSearchFriendsList = new ArrayList<People>();
        this.adapterFriendsList = new ArrayList<People>();
        this.friendFinderView = (LinearLayout) this.view.findViewById(R.id.friendfinderView);
        this.suggestedUserLayout = (LinearLayout) this.view.findViewById(R.id.suggestedUsersLayout);
        this.suggestedUsersTtextView = (TextView) this.view.findViewById(R.id.suggestedUsersTtextView);
        this.moreSuggestedUserLayout = (LinearLayout) this.view.findViewById(R.id.moresuggestedusers);
        this.friendFinderScrollView = (PullToRefreshScrollView) this.view.findViewById(R.id.friendfinderscrollview);

        this.shareGoogle = (RelativeLayout) this.view.findViewById(R.id.googlefrinedfinder);
        this.shareFacebook = (RelativeLayout) this.view.findViewById(R.id.fbfrinedfinder);
        this.shareTwitter = (RelativeLayout) this.view.findViewById(R.id.twitterfrinedfinder);
        this.shareContact = (RelativeLayout) this.view.findViewById(R.id.contactfrinedfinder);
        this.shareContact.setOnClickListener(this);

        this.shareGoogle.setOnClickListener(this);
        this.shareFacebook.setOnClickListener(this);
        this.shareTwitter.setOnClickListener(this);

        this.facebookFriendList = (ListView) this.view.findViewById(R.id.fbfriendslist);
        this.fbbackButton = (ImageButton) this.view.findViewById(R.id.fbback);
        friendList = new ArrayList<Friend>();
        this.adapterList = new ArrayList<Friend>();
        this.fbFriendListLayout = (LinearLayout) this.view.findViewById(R.id.fbfriendListLayout);

        this.contactListView = (ListView) this.view.findViewById(R.id.friendFindercontactsList);
        this.contactList = new ArrayList<Contact>();
        this.contactListView.setVisibility(View.GONE);
        this.inviteDone = (ImageButton) this.view.findViewById(R.id.inviteDone);

        this.inviteDone.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                if (Constant.CONTACTS.equalsIgnoreCase(Config.getSocialSite())
                        && (VideoFeedsFragment.this.contactList != null)
                        && !VideoFeedsFragment.this.contactList.isEmpty()) {
                    final List<Contact> selectedContacts = new ArrayList<Contact>();
                    for (int i = 0; i < VideoFeedsFragment.this.contactList.size(); i++) {
                        final Contact contact = VideoFeedsFragment.this.contactList.get(i);
                        if (contact.isChecked()) {
                            selectedContacts.add(contact);
                        }
                    }
                    if ((selectedContacts == null) || selectedContacts.isEmpty()) {
                        Alerts.showInfoOnly(PLEASE_SELECT_ATLEAST_ONE_CONTACT_TO_INVITE,
                                VideoFeedsFragment.this.context);
                    } else {
                        VideoFeedsFragment.this.sendSms(selectedContacts);
                    }
                }
            }
        });

        this.fbbackButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                VideoFeedsFragment.this.fbFriendListLayout.setVisibility(View.GONE);
                VideoFeedsFragment.this.publicVideoFeedsLayout.setVisibility(View.VISIBLE);
                // friendFinderScrollView.setVisibility(View.VISIBLE);
            }
        });
        this.moreSuggestedUserLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                final String userId = MainManager.getInstance().getUserId();
                if (!Strings.isNullOrEmpty(userId)) {
                    final Intent morePeopleIntent = new Intent(VideoFeedsFragment.this.context,
                            SuggestedUserActivity.class);
                    morePeopleIntent.putExtra(Constant.USERID, userId);
                    morePeopleIntent.putExtra(Constant.SCREEN, Constant.MY_PAGE);
                    VideoFeedsFragment.this.startActivity(morePeopleIntent);
                }
            }
        });
        this.friendFinderScrollView.setOnRefreshListener(new OnRefreshListener<ScrollView>() {

            @Override
            public void onRefresh(final PullToRefreshBase<ScrollView> refreshView) {

                VideoFeedsFragment.this.showProgress();
                VideoFeedsFragment.this.pullToRefresh = true;
                if (VideoFeedsFragment.this.publicVideo) {
                    if (VideoFeedsFragment.this.searchRequest) {
                        try {
                            final VideoFeedAsync task = new VideoFeedAsync(VideoFeedsFragment.this.getActivity(),
                                    Constant.PUBLIC_FEED, VideoFeedsFragment.this.getSearchRequest(1).toString(),
                                    VideoFeedsFragment.this.searchRequest, false, false, false,
                                    VideoFeedsFragment.this.searchTextView);
                            task.delegate = VideoFeedsFragment.this;
                            task.execute();
                        } catch (final JSONException exception) {
                            LOG.e(exception);
                        }
                    } else {
                        try {
                            final VideoFeedAsync task = new VideoFeedAsync(VideoFeedsFragment.this.getActivity(),
                                    Constant.PUBLIC_FEED, VideoFeedsFragment.this.getJSONRequest(1).toString(),
                                    VideoFeedsFragment.this.searchRequest, false, false, false,
                                    VideoFeedsFragment.this.searchTextView);
                            task.delegate = VideoFeedsFragment.this;
                            task.execute();
                        } catch (final JSONException exception) {
                            LOG.e(exception);
                        }
                    }
                } else if (VideoFeedsFragment.this.privateVideo) {
                    if (VideoFeedsFragment.this.searchRequest) {
                        try {
                            final VideoFeedAsync task = new VideoFeedAsync(VideoFeedsFragment.this.getActivity(),
                                    Constant.PRIVATE_FEED, VideoFeedsFragment.this.getSearchRequest(1).toString(),
                                    VideoFeedsFragment.this.searchRequest, false, false, false,
                                    VideoFeedsFragment.this.searchTextView);
                            task.delegate = VideoFeedsFragment.this;
                            task.execute();
                        } catch (final JSONException exception) {
                            LOG.e(exception);
                        }
                    } else {
                        try {
                            final VideoFeedAsync task = new VideoFeedAsync(VideoFeedsFragment.this.getActivity(),
                                    Constant.PRIVATE_FEED, VideoFeedsFragment.this.getJSONRequest(1).toString(),
                                    VideoFeedsFragment.this.searchRequest, false, false, false,
                                    VideoFeedsFragment.this.searchTextView);
                            task.delegate = VideoFeedsFragment.this;
                            task.execute();
                        } catch (final JSONException exception) {
                            LOG.e(exception);
                        }
                    }
                }
            }
        });
    }

    private void loadResponse(final Object returnObject, final String type) {

        if (returnObject instanceof ErrorResponse) {
            final ErrorResponse res = (ErrorResponse) returnObject;
            this.searchTextView.setVisibility(View.GONE);
            this.searchTextView.setText(R.string.no_search_text);
            if (Util.isConnected(this.context) && !_100.equalsIgnoreCase(res.getErrorCode())) {
                this.searchTextView.setText(res.getMessage());
                this.searchTextView.setVisibility(View.VISIBLE);
            }
            this.videoList(null, type, false);
        } else if (returnObject instanceof List<?>) {
            this.searchTextView.setVisibility(View.GONE);
            this.searchTextView.setText(R.string.no_search_text);

            final List<MyPageDto> currentList = (ArrayList<MyPageDto>) returnObject;
            if ((currentList == null) || currentList.isEmpty()) {
                this.videoList(null, type, true);

            } else {
                this.videoList(currentList, type, true);
            }

            final Intent intent = new Intent(FEED_NOTIFICATION_VISITED);
            this.context.sendBroadcast(intent);

        } else {
            this.videoList(null, type, true);
        }
    }

    /**
     * Registering broadcast receivers
     */
    private void registerBroadcastReceiver() {

        final IntentFilter messageNotificationAndSubscriptionFilter = new IntentFilter();
        messageNotificationAndSubscriptionFilter.addAction(Constant.ACTION_FILE_UPLOAD_PROGRESS);
        messageNotificationAndSubscriptionFilter.addAction(Constant.UPLOADED_PERCENTAGE);
        messageNotificationAndSubscriptionFilter.addAction(Constant.VIDEO_UPLOADED);
        messageNotificationAndSubscriptionFilter.addAction(Constant.HIDE_PROGRESS);
        messageNotificationAndSubscriptionFilter.addAction(Constant.FILE_UPLOADED);
        messageNotificationAndSubscriptionFilter.addAction(Constant.FILE_DELETED);
        messageNotificationAndSubscriptionFilter.addAction(FEED_NOTIFICATION);
        this.getActivity().getApplicationContext()
                .registerReceiver(this.VideoUploadNotificationReciver, messageNotificationAndSubscriptionFilter);

    }

    private void setContactAdapter(final List<Contact> contacts) {

        this.publicVideoFeedsLayout.setVisibility(View.GONE);
        this.fbFriendListLayout.setVisibility(View.VISIBLE);
        this.searchIcon.setImageResource(R.drawable.contact);

        this.contactListView.setVisibility(View.VISIBLE);
        this.inviteDone.setVisibility(View.VISIBLE);

        if ((contacts != null) && !contacts.isEmpty()) {
            this.contactList.clear();
            this.contactList.addAll(contacts);
        }
        this.contactAdapter = new ContactAdapter(this.context, R.layout.facebook_user, this.contactList,
                Config.getSocialSite(), false);
        this.contactListView.setAdapter(this.contactAdapter);
        if (this.contactAdapter != null) {
            this.contactAdapter.notifyDataSetChanged();
        }
        this.contactListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(final AdapterView<?> arg0, final View ignored, final int arg2, final long arg3) {

                final Contact selectedContact = VideoFeedsFragment.this.contactList.get(arg2);
                if (selectedContact.isChecked()) {
                    selectedContact.setChecked(false);
                } else {
                    selectedContact.setChecked(true);
                }
                if (VideoFeedsFragment.this.contactAdapter != null) {
                    VideoFeedsFragment.this.contactAdapter.notifyDataSetChanged();
                }
            }
        });

    }

    private void setPrivateFeed(final List<MyPageDto> video) {

        if (this.pullToRefresh) {
            this.clearListAndAddPrivateFeeds(video, this.searchRequest);
            this.pullToRefresh = false;

        } else if (this.searchRequest) {
            if ((video != null) && !video.isEmpty()) {
                for (int i = 0; i < video.size(); i++) {
                    this.privatefeedSearch.add(video.get(i));
                }
            }
            if (this.privateFeedSearchAdapter == null) {
                this.privateFeedSearchAdapter = new PostsAdapter(this.context, 0, this.privatefeedSearch,
                        Constant.VIDEO_FEEDS, videoFeeds);
                this.privateFeedLsitview.setAdapter(this.privateFeedSearchAdapter);
            } else {
                this.privateFeedSearchAdapter.notifyDataSetChanged();
            }
            this.searchTextView.setText(this.getResources().getString(R.string.no_search_text));
            if ((this.privatefeedSearch == null) || this.privatefeedSearch.isEmpty()) {
                this.searchTextView.setVisibility(View.VISIBLE);
            } else {
                this.searchTextView.setVisibility(View.GONE);
            }

        } else {
            if ((video != null) && !video.isEmpty()) {
                for (int i = 0; i < video.size(); i++) {
                    this.privateFeed.add(video.get(i));
                }
            }
            if ((this.privateFeed == null)
                    || ((this.privateFeed != null) && (this.privateFeed.size() <= 0) && Util.isConnected(this.context))) {
                this.privateVideosTextView.setText(this.getResources().getString(R.string.no_privatefeed_text));
                this.privateVideosTextView.setVisibility(View.VISIBLE);

                if (Config.getPrivateGroupCount() == 0) {
                    final FriendFinderAsync req = new FriendFinderAsync(Config.getUserId(), 1, false,
                            this.searchRequest);
                    req.execute();
                    this.friendFinderView.setVisibility(View.VISIBLE);
                    this.publicLayout.setVisibility(View.GONE);
                    this.privateLayout.setVisibility(View.GONE);
                }

            } else if ((this.privateFeed == null)
                    || ((this.privateFeed != null) && (this.privateFeed.size() <= 0) && !Util.isConnected(this.context))) {
                this.privateVideosTextView.setText(this.getResources().getString(
                        R.string.no_connectivity_feed_privattext));
                this.privateVideosTextView.setVisibility(View.VISIBLE);
            } else {
                this.friendFinderView.setVisibility(View.GONE);
                this.publicLayout.setVisibility(View.GONE);
                this.privateLayout.setVisibility(View.VISIBLE);

                this.privateVideosTextView.setText(this.getResources().getString(R.string.no_privatefeed_text));
                this.privateVideosTextView.setVisibility(View.GONE);
                if (this.privateFeedAdapter == null) {
                    this.privateFeedAdapter = new PostsAdapter(this.context, 0, this.privateFeed, Constant.VIDEO_FEEDS,
                            videoFeeds);
                    this.privateFeedLsitview.setAdapter(this.privateFeedAdapter);
                } else {
                    this.privateFeedAdapter.notifyDataSetChanged();
                }
            }
        }

    }

    private void setSearchAdapter(final List<Friend> frndList, final String text, final String socialSite) {

        if ((text != null) && (frndList != null) && !frndList.isEmpty()) {
            this.clearList();
            new Thread(new Runnable() {

                @Override
                public void run() {

                    VideoFeedsFragment.this.filterdList = new ArrayList<Friend>();
                    for (int i = 0; i < frndList.size(); i++) {
                        final Friend frnd = frndList.get(i);
                        if (frnd.getFriendName().toLowerCase(Locale.getDefault())
                                .indexOf(text.toLowerCase(Locale.getDefault())) != -1) {
                            VideoFeedsFragment.this.filterdList.add(frnd);
                        }
                    }
                }
            }).start();
            try {
                Thread.sleep(50);
            } catch (final InterruptedException e) {
                Alerts.showException(e.toString(), this.context);
            }
            if ((this.filterdList != null) && !this.filterdList.isEmpty()) {
                this.setFriendListAdapter(this.filterdList);
            }

        }
    }

    private void setSearchContactAdapter(final List<Contact> frndList, final String text) {

        if ((text != null) && (frndList != null) && !frndList.isEmpty()) {
            this.clearContactList();
            new Thread(new Runnable() {

                @Override
                public void run() {

                    VideoFeedsFragment.this.contactFilterdList = new ArrayList<Contact>();
                    for (int i = 0; i < frndList.size(); i++) {
                        final Contact frnd = frndList.get(i);
                        if (frnd.getContactName().toLowerCase(Locale.getDefault())
                                .indexOf(text.toLowerCase(Locale.getDefault())) != -1) {
                            VideoFeedsFragment.this.contactFilterdList.add(frnd);
                        }
                    }
                }
            }).start();
            try {
                Thread.sleep(50);
            } catch (final InterruptedException e) {
                Alerts.showException(e.toString(), this.context);
            }
            if ((this.contactFilterdList != null) && !this.contactFilterdList.isEmpty()) {
                this.setContactAdapter(this.contactFilterdList);
            }

        }
    }

    private void setSuggestedFriends(final List<People> suggestedUsers) {

        if ((suggestedUsers != null) && !suggestedUsers.isEmpty()) {
            for (int i = 0; i < suggestedUsers.size(); i++) {
                if (i > 4) {
                    break;
                }
                this.suggestedUserLayout.addView(this.getSuggestedFriendsView(suggestedUsers.get(i)));
            }
        }

    }

    protected void getPrivateFeedResponseFromCache() {

        try {
            final Object privateFeedObject = Backend.getPrivateFeedFromCache();
            if (privateFeedObject == null) {
                final VideoFeedAsync task = new VideoFeedAsync(this.getActivity(), Constant.PRIVATE_FEED, this
                        .getJSONRequest(1).toString(), this.searchRequest, true, true, false, this.searchTextView);
                task.delegate = VideoFeedsFragment.this;
                task.execute();
            } else {
                this.loadResponse(privateFeedObject, Constant.PRIVATE_FEED);
                this.pullToRefresh = true;
                final VideoFeedAsync task = new VideoFeedAsync(this.getActivity(), Constant.PRIVATE_FEED, this
                        .getJSONRequest(1).toString(), false, false, false, true, this.searchTextView);
                task.delegate = VideoFeedsFragment.this;
                task.execute();
            }
        } catch (final JSONException exception) {
            LOG.e(exception);
        }
    }

    protected void loadData(final List<People> list) {

        this.adapterFriendsList.clear();
        if ((list != null) && !list.isEmpty()) {
            this.adapterFriendsList.addAll(list);
        }

        this.suggestedUserLayout.removeAllViews();
        if ((this.adapterFriendsList == null) || this.adapterFriendsList.isEmpty()) {
            this.suggestedUsersTtextView.setVisibility(View.GONE);
            this.moreSuggestedUserLayout.setVisibility(View.GONE);
        } else {
            this.suggestedUsersTtextView.setVisibility(View.VISIBLE);
            if (this.searchRequest) {
                this.moreSuggestedUserLayout.setVisibility(View.GONE);
            } else {
                this.moreSuggestedUserLayout.setVisibility(View.VISIBLE);
            }
            this.setSuggestedFriends(this.adapterFriendsList);
        }
    }

    protected void sendSms(final List<Contact> selectedContacts) {

        StringBuilder uri = null;
        String contactName = EMPTY;
        uri = new StringBuilder(SMSTO);
        for (int i = 0; i < selectedContacts.size(); i++) {
            if (i == 0) {
                contactName = selectedContacts.get(i).getContactName();
            }
            uri.append(selectedContacts.get(i).getContactNumber());
            uri.append(';');
        }
        final Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(uri.toString()));
        // intent.putExtra("sms_body","www.TagFu.com/invite.html");
        if (selectedContacts.size() <= 1) {
            intent.putExtra(SMS_BODY, HI + contactName + FOUND_THIS_INTERESTING_APP);
        } else {
            intent.putExtra(SMS_BODY, HI + ALL + FOUND_THIS_INTERESTING_APP);
        }
        this.startActivity(intent);
    }

    protected void setFriendListAdapter(final List<Friend> friendlist) {

        if (Constant.FACEBOOK.equalsIgnoreCase(Config.getSocialSite())) {
            this.searchIcon.setImageResource(R.drawable.sharefacebook);
        } else if (Constant.TWITTER.equalsIgnoreCase(Config.getSocialSite())) {
            this.searchIcon.setImageResource(R.drawable.sharetwitter);
        } else if (Constant.GOOGLE_PLUS.equalsIgnoreCase(Config.getSocialSite())) {
            this.searchIcon.setImageResource(R.drawable.sharegoogleplus);
        }

        this.publicVideoFeedsLayout.setVisibility(View.GONE);
        this.fbFriendListLayout.setVisibility(View.VISIBLE);
        if ((friendlist != null) && !friendList.isEmpty()) {
            this.adapterList.clear();
            this.adapterList.addAll(friendlist);
        }
        this.friendFinder = new FriendFinderAdapter(this.context, R.layout.facebook_user, this.adapterList,
                Config.getSocialSite(), VideoFeedsFragment.this, Constant.VIDEO_FEEDS);
        this.friendFinder.delegate = VideoFeedsFragment.this;
        this.facebookFriendList.setAdapter(this.friendFinder);

    }

    void performSearch() {

        final InputMethodManager mgr = (InputMethodManager) this.getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(this.searchEdit.getWindowToken(), 0);
        final String text = this.searchEdit.getText().toString();
        if ((text != null) && (text.trim().length() > 0)) {
            if (this.friendFinderView.isShown()) {
                this.pullToRefresh = true;
                final FriendFinderAsync req = new FriendFinderAsync(Config.getUserId(), 1, false, this.searchRequest);
                req.execute();
            } else {
                if (this.publicVideo) {
                    this.pullToRefresh = true;
                    try {
                        final VideoFeedAsync task = new VideoFeedAsync(this.getActivity(), Constant.PUBLIC_FEED, this
                                .getSearchRequest(1).toString(), this.searchRequest, true, false, false,
                                this.searchTextView);
                        task.delegate = VideoFeedsFragment.this;
                        task.execute();
                    } catch (final JSONException exception) {
                        LOG.e(exception);
                    }
                } else {
                    try {
                        this.pullToRefresh = true;
                        final VideoFeedAsync task = new VideoFeedAsync(this.getActivity(), Constant.PRIVATE_FEED, this
                                .getSearchRequest(1).toString(), this.searchRequest, true, false, false,
                                this.searchTextView);
                        task.delegate = VideoFeedsFragment.this;
                        task.execute();
                    } catch (final JSONException exception) {
                        LOG.e(exception);
                    }
                }
            }
        } else {
            Alerts.showInfoOnly("Enter text to search", this.getActivity());
        }
    }

    /**
     * showing progress based on video upload status
     */
    void showProgress() {

        boolean progressVisible = false;
        final List<VideoInfo> videoInfos = VideoDataBase.getInstance(this.getActivity().getApplicationContext())
                .getAllNonUploadList();
        if ((videoInfos != null) && !videoInfos.isEmpty()) {
            for (int i = 0; i < videoInfos.size(); i++) {
                this.info = videoInfos.get(i);
                if (MainManager.getInstance().getUserId() != null) {
                    final int userId = Integer.parseInt(MainManager.getInstance().getUserId());
                    if (((userId == this.info.getUserid()) && (this.info.getUploadStatus() == 2))
                            || (this.info.getUploadStatus() == 3)) {
                        if ((VideoDataBase.getInstance(this.context).getVideoUploadState(this.info.getVideoClientId(),
                                this.context) == 0)
                                && (VideoDataBase.getInstance(this.context).getPartsUpload(
                                        this.info.getVideoClientId(), this.context) == 0)) {
                            this.progressLayout.setVisibility(View.VISIBLE);
                            final int uploadedPercentage = VideoDataBase.getInstance(this.context)
                                    .getVideoUploadPercentage(this.info.getVideoClientId(), this.context);
                            this.videoProgress.setProgress(uploadedPercentage);// VideoPlayerConstants.uploadedPercentage
                            progressVisible = true;
                            if (this.info.getUploadStatus() == 2) {
                                this.text.setText(R.string.uploading_);
                            } else if (this.info.getUploadStatus() == 3) {
                                this.text.setText(UPLOADED_WAITING_TO_PUBLISH);
                            }
                        }
                    }
                }
            }
        }
        if (!progressVisible) {
            this.progressLayout.setVisibility(View.GONE);
        }
    }

    private class FriendFinderAsync extends AsyncTask<Void, Void, Void> {

        private final boolean isSearch;
        private final boolean showProgress;
        private final int pageNo;
        private ProgressDialog progress;

        private Object response;
        private final String userId;

        public FriendFinderAsync(final String userId, final int pageNo, final boolean showProgress,
                final boolean searchRequest) {

            this.userId = userId;
            this.pageNo = pageNo;
            this.showProgress = showProgress;
            this.isSearch = searchRequest;
        }

        @Override
        protected Void doInBackground(final Void... params) {

            if (this.isSearch) {
                try {
                    this.response = Backend.search(VideoFeedsFragment.this.context,
                            VideoFeedsFragment.this.getSearchJSONRequest(this.pageNo), PEOPLE);
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }

            } else {
                try {
                    this.response = Backend.getTagFuFriendFinderList(VideoFeedsFragment.this.context, this.userId,
                            this.pageNo);
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {

            super.onPostExecute(result);
            if ((this.progress != null) && this.showProgress) {
                this.progress.dismiss();
            }
            VideoFeedsFragment.this.friendFinderScrollView.onRefreshComplete();
            if (this.response == null) {
                Alerts.showInfoOnly("Network problem.Please try again", VideoFeedsFragment.this.context);

            } else {
                if (this.response instanceof List<?>) {
                    final List<People> list = (ArrayList<People>) this.response;
                    if (this.isSearch) {
                        if (VideoFeedsFragment.this.pullToRefresh) {
                            VideoFeedsFragment.this.pullToRefresh = false;
                            VideoFeedsFragment.this.TagFuSearchFriendsList.clear();
                        }

                        if (list != null) {
                            VideoFeedsFragment.this.TagFuSearchFriendsList.addAll(list);
                        }
                        VideoFeedsFragment.this.loadData(VideoFeedsFragment.this.TagFuSearchFriendsList);

                    } else {

                        if (VideoFeedsFragment.this.pullToRefresh) {
                            VideoFeedsFragment.this.pullToRefresh = false;
                            VideoFeedsFragment.this.TagFuFriendsList.clear();
                        }

                        if (list != null) {
                            VideoFeedsFragment.this.TagFuFriendsList.addAll(list);
                        }
                        VideoFeedsFragment.this.loadData(VideoFeedsFragment.this.TagFuFriendsList);
                    }

                    LOG.i("suggested user list size " + list.size());
                } else {
                    if (this.response instanceof ErrorResponse) {
                        final ErrorResponse resp = (ErrorResponse) this.response;
                        Alerts.showInfoOnly(resp.getMessage(), VideoFeedsFragment.this.context);
                    }
                }
            }

        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            if (this.showProgress) {
                this.progress = ProgressDialog.show(VideoFeedsFragment.this.context, EMPTY, EMPTY, true);
                this.progress
                        .setContentView(((LayoutInflater) VideoFeedsFragment.this.context
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.progress_bar,
                                null, false));
                this.progress.setCancelable(false);
                this.progress.setCanceledOnTouchOutside(false);
                this.progress.show();
            }
        }
    }

    private class SocialFriendFinderAsync extends AsyncTask<Void, Void, Void> {

        private ProgressDialog progress;
        private final String request;
        private Object response;

        public SocialFriendFinderAsync(final String request) {

            this.request = request;
        }

        @Override
        protected Void doInBackground(final Void... params) {

            try {
                this.response = Backend.getTagFuSocialFriendsList(VideoFeedsFragment.this.context, this.request);
            } catch (final JSONException exception) {
                LOG.e(exception);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {

            super.onPostExecute(result);
            if (this.progress != null) {
                this.progress.dismiss();

            }
            if (this.response == null) {
                Alerts.showInfoOnly("Network problem.Please try again", VideoFeedsFragment.this.context);

            } else {
                if (this.response instanceof List<?>) {

                    final List<Friend> list = (ArrayList<Friend>) this.response;
                    if ((list == null) || list.isEmpty()) {
                        Alerts.showInfoOnly("No Friend available", VideoFeedsFragment.this.context);

                    } else {
                        LOG.i("frined list size " + list.size());
                        VideoFeedsFragment.this.setFriendListAdapter(list);
                    }
                } else {
                    if (this.response instanceof ErrorResponse) {
                        final ErrorResponse resp = (ErrorResponse) this.response;
                        Alerts.showInfoOnly(resp.getMessage(), VideoFeedsFragment.this.context);
                    }
                }
            }

        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            this.progress = ProgressDialog.show(VideoFeedsFragment.this.context, EMPTY, EMPTY, true);
            this.progress.setContentView(((LayoutInflater) VideoFeedsFragment.this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.progress_bar, null, false));
            this.progress.setCancelable(false);
            this.progress.setCanceledOnTouchOutside(false);
            this.progress.show();
        }
    }
}
