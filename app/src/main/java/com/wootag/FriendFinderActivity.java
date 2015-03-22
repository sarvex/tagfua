/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.TagFu;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.TagFu.adapter.FriendFinderAdapter;
import com.TagFu.adapter.PeopleAdapter;
import com.TagFu.dto.ErrorResponse;
import com.TagFu.dto.Friend;
import com.TagFu.dto.People;
import com.TagFu.dto.User;
import com.TagFu.model.Backend;
import com.TagFu.util.Alerts;
import com.TagFu.util.Config;
import com.TagFu.util.InviteInterface;
import com.TagFu.util.Util;

public class FriendFinderActivity extends FriendsListActivity implements TextWatcher, InviteInterface {

    private static final String FIND_FRIEND = "Find Friend";
    private static final String DEVICE_TOKEN = "device_token";
    private static final String FRIENDS = "friends";
    private static final String USER_ID = "user_id";
    private static final String USER = "user";
    private static final String FRIENDFINDER2 = "friendfinder";
    private static final String PEOPLE = "people";
    private static final String PAGE_NO = "page_no";
    private static final String USERID = "userid";
    private static final String BROWSE_BY = "browse_by";
    private static final String NAME2 = "name";
    private static final String EMPTY = "";
    private static final String DESCRIPTION = "description";
    private static final String USER_NAME = "user_name";
    private static final String ID2 = "id";
    private static final String PHOTO_PATH = "photo_path";
    private static final String NO_FRIENDS_AVAILABLE = "No friends available.";

    public static FriendFinderActivity findFriendActivity;

    protected static final Logger LOG = LoggerManager.getLogger();
    private static final int VIDEOS_PER_PAGE = 10;

    private AutoCompleteTextView fbsearch;
    private Button back;
    private Button menu;
    private FriendFinderAdapter friendFinder;
    private ImageButton fbbackButton;
    private ImageView searchIcon;
    private List<Friend> adapterList;
    private List<People> adapterFriendsList;
    private ListView fbfrndList;
    private ListView friendListView;
    private PeopleAdapter friendAdapter;
    private RelativeLayout shareFacebook;
    private RelativeLayout shareGoogle;
    private RelativeLayout shareTwitter;
    private String gplusFriendId;
    private String gplusFriendname;
    private TextView heading;

    protected Button search;
    protected EditText searchEdit;
    protected LinearLayout fbFriendListLayout;
    protected LinearLayout socialActionsLayout;
    protected List<Friend> filterdList;
    protected List<People> TagFuFriendsList;
    protected List<People> TagFuSearchFriendsList;
    protected RelativeLayout searchLayout;
    protected TextView searchTextView;
    protected boolean flagLoading;
    protected boolean pullToRefresh;
    protected boolean searchRequest;
    protected int preLast;

    public static JSONArray getFriendListObject(final List<Friend> list) throws JSONException {

        final JSONArray friendArray = new JSONArray();
        for (int i = 0; i < list.size(); i++) {
            final JSONObject values = new JSONObject();
            final Friend friend = list.get(i);
            values.put(PHOTO_PATH, friend.getFriendImage());
            values.put(ID2, friend.getFriendId());
            values.put(USER_NAME, friend.getFriendName());
            values.put(DESCRIPTION, EMPTY);
            friendArray.put(values);
        }

        return friendArray;
    }

    public static JSONObject getSocialLoginRequest(final List<Friend> list) throws JSONException {

        final JSONObject obj = new JSONObject();
        obj.put(USER_ID, Config.getUserId());
        if ((list != null) && (list.size() > 0)) {
            obj.put(FRIENDS, FriendFinderActivity.getFriendListObject(list));
        } else {
            obj.put(FRIENDS, EMPTY);
        }
        obj.put(DEVICE_TOKEN, Config.getDeviceToken());
        obj.put(Constant.DEVICE_TYPE, Constant.DEVICE_MODEL);
        obj.put(Constant.RESOLUTION, Config.getDeviceResolutionValue());

        return obj;
    }

    @Override
    public void afterTextChanged(final Editable s) {

        final String text = this.fbsearch.getText().toString();
        if (Constant.FACEBOOK.equalsIgnoreCase(Config.getSocialSite())) {
            this.setSearchAdapter(VideoPlayerApp.getInstance().getFbFriendsList(), text, Constant.FACEBOOK);
        } else if (Constant.GOOGLE_PLUS.equalsIgnoreCase(Config.getSocialSite())) {
            this.setSearchAdapter(VideoPlayerApp.getInstance().getGoogleFriendList(), text, Constant.GOOGLE_PLUS);
        } else if (Constant.TWITTER.equalsIgnoreCase(Config.getSocialSite())) {
            this.setSearchAdapter(VideoPlayerApp.getInstance().getTwitterFriendList(), text, Constant.TWITTER);
        }

    }

    @Override
    public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

    }

    public void clearList() {

        if ((this.adapterList != null) && (this.adapterList.size() > 0)) {
            this.adapterList.clear();
            ((BaseAdapter) this.fbfrndList.getAdapter()).notifyDataSetChanged();
        }

    }

    public JSONObject getSearchJSONRequest(final int pageNo) throws JSONException {

        final JSONObject request = new JSONObject();
        final JSONObject obj = new JSONObject();
        obj.put(NAME2, this.searchEdit.getText().toString());
        obj.put(BROWSE_BY, PEOPLE);
        obj.put(USERID, Config.getUserId());
        obj.put(PAGE_NO, pageNo);
        obj.put(Constant.RESOLUTION, Config.getDeviceResolutionValue());
        obj.put(Constant.DEVICE_TYPE, Constant.DEVICE_MODEL);
        request.put(USER, obj);
        return request;
    }

    @Override
    public void invite(final String id, final String name) {

        this.gplusFriendId = id;
        this.gplusFriendname = name;
        this.gPlusRequest = Constant.G_PLUS_AUTHORIZE;
        this.gPlusLogin();

    }

    @Override
    public void onClick(final View v) {

        if (v.getId() == R.id.fbfrinedfinder) {
            LOG.i("fb on click ");
            this.clearList();
            Config.setSocialSite(Constant.FACEBOOK);
            this.fbFriendListLayout.setVisibility(View.VISIBLE);
            if ((VideoPlayerApp.getInstance().getFbFriendsList() != null)
                    && (VideoPlayerApp.getInstance().getFbFriendsList().size() > 0)) {
                friendList = VideoPlayerApp.getInstance().getFbFriendsList();

                try {
                    new SocialFriendFinderAsync(FriendFinderActivity.getSocialLoginRequest(friendList).toString())
                            .execute();
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }

            } else {
                super.onClick(v);
            }
        } else if (v.getId() == R.id.twitterfrinedfinder) {
            Config.setSocialSite(Constant.TWITTER);
            if ((VideoPlayerApp.getInstance().getTwitterFriendList() != null)
                    && (VideoPlayerApp.getInstance().getTwitterFriendList().size() > 0)) {
                friendList = VideoPlayerApp.getInstance().getTwitterFriendList();

                try {
                    new SocialFriendFinderAsync(FriendFinderActivity.getSocialLoginRequest(friendList).toString())
                            .execute();
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }

            } else {
                super.onClick(v);
            }
        } else if (v.getId() == R.id.googlefrinedfinder) {
            Config.setSocialSite(Constant.GOOGLE_PLUS);
            if ((VideoPlayerApp.getInstance().getGoogleFriendList() != null)
                    && (VideoPlayerApp.getInstance().getGoogleFriendList().size() > 0)) {
                friendList = VideoPlayerApp.getInstance().getGoogleFriendList();

                try {
                    new SocialFriendFinderAsync(FriendFinderActivity.getSocialLoginRequest(friendList).toString())
                            .execute();
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }

            } else {
                super.onClick(v);
            }
        }

    }

    @Override
    public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {

    }

    @Override
    public void processFinish(final List<Friend> friendList, final String socialSite) {

        if ((friendList != null) && (friendList.size() > 0)) {
            if (Constant.TWITTER.equalsIgnoreCase(socialSite)) {
                VideoPlayerApp.getInstance().setTwitterFriendList(friendList);
                try {
                    new SocialFriendFinderAsync(FriendFinderActivity.getSocialLoginRequest(friendList).toString())
                            .execute();
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }

            } else if (Constant.FACEBOOK.equalsIgnoreCase(socialSite)) {
                super.processFinish(friendList, socialSite);

            } else if (Constant.GOOGLE_PLUS.equalsIgnoreCase(socialSite)) {
                VideoPlayerApp.getInstance().setGoogleFriendList(friendList);
                try {
                    new SocialFriendFinderAsync(FriendFinderActivity.getSocialLoginRequest(friendList).toString())
                            .execute();
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }

            }
        } else {
            Alerts.showInfoOnly(NO_FRIENDS_AVAILABLE, this.context);
        }

    }

    @Override
    public void sendList(final List<Friend> list) {

        VideoPlayerApp.getInstance().setFacebookFriendsList(list);

        try {
            new SocialFriendFinderAsync(FriendFinderActivity.getSocialLoginRequest(list).toString()).execute();
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

    private LinearLayout getHeaderView() {

        final LinearLayout view = (LinearLayout) ((LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.friend_finder_social_site, null);
        this.shareGoogle = (RelativeLayout) view.findViewById(R.id.googlefrinedfinder);
        this.shareFacebook = (RelativeLayout) view.findViewById(R.id.fbfrinedfinder);
        this.shareTwitter = (RelativeLayout) view.findViewById(R.id.twitterfrinedfinder);

        this.shareGoogle.setOnClickListener(this);
        this.shareFacebook.setOnClickListener(this);
        this.shareTwitter.setOnClickListener(this);
        return view;

    }

    private void loadViews() {

        this.fbsearch = (AutoCompleteTextView) this.findViewById(R.id.invitefriendsearch);
        this.searchIcon = (ImageView) this.findViewById(R.id.socialiconimageview);
        this.fbsearch.addTextChangedListener(this);

        this.menu = (Button) this.findViewById(R.id.menu);
        this.search = (Button) this.findViewById(R.id.settings);
        this.back = (Button) this.findViewById(R.id.back);
        this.searchEdit = (EditText) this.findViewById(R.id.searchEditText);
        this.searchLayout = (RelativeLayout) this.findViewById(R.id.searchRL);
        this.heading = (TextView) this.findViewById(R.id.heading);
        this.heading.setText(FIND_FRIEND);
        this.menu.setVisibility(View.GONE);
        this.search.setVisibility(View.VISIBLE);
        this.back.setVisibility(View.VISIBLE);
        this.socialActionsLayout = (LinearLayout) this.findViewById(R.id.suggestedUsersTtextView);
        this.friendListView = (ListView) this.findViewById(R.id.usersLsit);
        this.friendListView.addHeaderView(this.getHeaderView());

        // social site friend list layout views
        this.fbfrndList = (ListView) this.findViewById(R.id.fbfriendslist);
        this.fbbackButton = (ImageButton) this.findViewById(R.id.fbback);
        friendList = new ArrayList<Friend>();
        this.adapterList = new ArrayList<Friend>();
        this.searchTextView = (TextView) this.findViewById(R.id.searchView);
        this.fbFriendListLayout = (LinearLayout) this.findViewById(R.id.fbfriendListLayout);
        this.search.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (FriendFinderActivity.this.searchLayout.isShown()) {
                    FriendFinderActivity.this.searchLayout.setVisibility(View.GONE);
                    FriendFinderActivity.this.search.setBackgroundResource(R.drawable.search1);
                    FriendFinderActivity.this.searchRequest = false;
                    FriendFinderActivity.this.searchTextView.setVisibility(View.GONE);
                    FriendFinderActivity.this.searchEdit.setText(EMPTY);
                    FriendFinderActivity.this.loadData(FriendFinderActivity.this.TagFuFriendsList);
                } else {
                    FriendFinderActivity.this.searchLayout.setVisibility(View.VISIBLE);
                    FriendFinderActivity.this.search.setBackgroundResource(R.drawable.cancelbutton);
                    FriendFinderActivity.this.searchRequest = true;
                }
            }
        });
        this.searchEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    this.performSearch();
                    return true;
                }
                return false;
            }

            private void performSearch() {

                if ((FriendFinderActivity.this.TagFuSearchFriendsList != null)
                        && (FriendFinderActivity.this.TagFuSearchFriendsList.size() > 0)) {
                    FriendFinderActivity.this.TagFuSearchFriendsList.clear();
                }
                final String text = FriendFinderActivity.this.searchEdit.getText().toString();
                final InputMethodManager mgr = (InputMethodManager) FriendFinderActivity.this
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(FriendFinderActivity.this.searchEdit.getWindowToken(), 0);
                if ((text != null) && (text.trim().length() > 0)) {
                    final FriendFinderAsync req = new FriendFinderAsync(Config.getUserId(), 1, true,
                            FriendFinderActivity.this.searchRequest);

                    req.execute();

                } else {
                    Alerts.showInfoOnly("Enter text to search", FriendFinderActivity.this);
                }

            }
        });
        this.back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                FriendFinderActivity.this.finish();
            }
        });

        this.fbbackButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                FriendFinderActivity.this.fbFriendListLayout.setVisibility(View.GONE);
                FriendFinderActivity.this.socialActionsLayout.setVisibility(View.VISIBLE);
            }
        });

        this.friendListView.setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount,
                    final int totalItemCount) {

                final int lastItem = firstVisibleItem + visibleItemCount;
                if ((lastItem == totalItemCount) && (FriendFinderActivity.this.preLast != lastItem)) { // to avoid
                    // multiple calls
                    // for last item
                    LOG.d("Last");
                    FriendFinderActivity.this.preLast = lastItem;
                    if (!FriendFinderActivity.this.flagLoading) {
                        if (FriendFinderActivity.this.searchRequest) {
                            final int offset = FriendFinderActivity.this.TagFuSearchFriendsList.size();
                            if ((offset % FriendFinderActivity.VIDEOS_PER_PAGE) == 0) {
                                FriendFinderActivity.this.flagLoading = true;
                                final int pageNo = (offset / FriendFinderActivity.VIDEOS_PER_PAGE) + 1;
                                new FriendFinderAsync(Config.getUserId(), pageNo, true,
                                        FriendFinderActivity.this.searchRequest).execute();
                            }
                        } else {
                            final int offset = FriendFinderActivity.this.TagFuFriendsList.size();
                            if ((offset % FriendFinderActivity.VIDEOS_PER_PAGE) == 0) {
                                FriendFinderActivity.this.flagLoading = true;
                                final int pageNo = (offset / FriendFinderActivity.VIDEOS_PER_PAGE) + 1;
                                new FriendFinderAsync(Config.getUserId(), pageNo, true,
                                        FriendFinderActivity.this.searchRequest).execute();
                            }
                        }
                    }
                }

            }

            @Override
            public void onScrollStateChanged(final AbsListView view, final int scrollState) {

            }
        });

    }

    private void setSearchAdapter(final List<Friend> frndList, final String text, final String socialSite) {

        if ((text != null) && (frndList != null) && (frndList.size() > 0)) {
            this.clearList();
            new Thread(new Runnable() {

                @Override
                public void run() {

                    FriendFinderActivity.this.filterdList = new ArrayList<Friend>();
                    for (int i = 0; i < frndList.size(); i++) {
                        final Friend frnd = frndList.get(i);
                        if (frnd.getFriendName().toLowerCase(Locale.getDefault())
                                .indexOf(text.toLowerCase(Locale.getDefault())) != -1) {
                            FriendFinderActivity.this.filterdList.add(frnd);
                        }
                    }
                }
            }).start();
            try {
                Thread.sleep(50);
            } catch (final InterruptedException e) {
                Alerts.showAlert("Exception", e.toString(), this.context);
            }
            if ((this.filterdList != null) && (this.filterdList.size() > 0)) {
                this.setFriendListAdapter(this.filterdList);
            }
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.friend_finder);
        findFriendActivity = this;
        this.TagFuFriendsList = new ArrayList<People>();
        this.TagFuSearchFriendsList = new ArrayList<People>();
        this.adapterFriendsList = new ArrayList<People>();
        this.loadViews();
        final FriendFinderAsync req = new FriendFinderAsync(Config.getUserId(), 1, true, this.searchRequest);
        req.execute();

    }

    protected void setFriendListAdapter(final List<Friend> friendlist) {

        if (Constant.FACEBOOK.equalsIgnoreCase(Config.getSocialSite())) {
            this.searchIcon.setImageResource(R.drawable.sharefacebook);
        } else if (Constant.FACEBOOK.equalsIgnoreCase(Config.getSocialSite())) {
            this.searchIcon.setImageResource(R.drawable.sharetwitter);
        } else if (Constant.FACEBOOK.equalsIgnoreCase(Config.getSocialSite())) {
            this.searchIcon.setImageResource(R.drawable.sharegoogleplus);
        }

        this.socialActionsLayout.setVisibility(View.GONE);
        this.fbFriendListLayout.setVisibility(View.VISIBLE);
        if ((friendlist != null) && (friendlist.size() > 0)) {
            this.adapterList.clear();
            this.adapterList.addAll(friendlist);
        }
        this.friendFinder = new FriendFinderAdapter(this.context, R.layout.facebook_user, this.adapterList,
                Config.getSocialSite(), null, EMPTY);
        this.friendFinder.delegate = FriendFinderActivity.this;
        this.fbfrndList.setAdapter(this.friendFinder);
        this.friendFinder.notifyDataSetChanged();

    }

    void loadData(final List<People> list) {

        this.adapterFriendsList.clear();
        if ((list != null) && (list.size() > 0)) {
            this.adapterFriendsList.addAll(list);
        }
        if (this.friendAdapter == null) {
            this.friendAdapter = new PeopleAdapter(FriendFinderActivity.this, 0, this.adapterFriendsList,
                    FRIENDFINDER2, null);
            this.friendListView.setAdapter(this.friendAdapter);
        } else {
            this.friendAdapter.notifyDataSetChanged();
        }

        if (this.searchRequest && (this.adapterFriendsList != null) && (this.adapterFriendsList.size() <= 0)) {
            this.searchTextView.setVisibility(View.VISIBLE);
        } else {
            this.searchTextView.setVisibility(View.GONE);
        }

    }

    private class FriendFinderAsync extends AsyncTask<Void, Void, Void> {

        private static final String NETWORK_PROBLEM_PLEASE_TRY_AGAIN = "Network problem.Please try again";
        private static final String _100 = "100";
        private final boolean isSearch;
        private final boolean showProgress;
        private final int pageNo;
        private ProgressDialog progressDialog;
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
                    this.response = Backend.search(FriendFinderActivity.this.context,
                            FriendFinderActivity.this.getSearchJSONRequest(this.pageNo), PEOPLE);
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }

            } else {
                try {
                    this.response = Backend.getTagFuFriendFinderList(FriendFinderActivity.this.context, this.userId,
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
            if ((this.progressDialog != null) && this.showProgress) {
                this.progressDialog.dismiss();
            }
            FriendFinderActivity.this.flagLoading = false;
            if (FriendFinderActivity.this.searchTextView != null) {
                FriendFinderActivity.this.searchTextView.setVisibility(View.GONE);
                FriendFinderActivity.this.searchTextView.setText(R.string.no_search_text);
            }
            // friendListView.onRefreshComplete();
            if (this.response != null) {
                if (this.response instanceof List<?>) {
                    final List<People> list = (ArrayList<People>) this.response;
                    if (!this.isSearch) {
                        if (FriendFinderActivity.this.pullToRefresh) {
                            FriendFinderActivity.this.pullToRefresh = false;
                            FriendFinderActivity.this.TagFuFriendsList.clear();
                        }

                        if (list != null) {
                            FriendFinderActivity.this.TagFuFriendsList.addAll(list);
                        }
                        FriendFinderActivity.this.loadData(FriendFinderActivity.this.TagFuFriendsList);
                    } else {
                        if (FriendFinderActivity.this.pullToRefresh) {
                            FriendFinderActivity.this.pullToRefresh = false;
                            FriendFinderActivity.this.TagFuSearchFriendsList.clear();
                        }

                        if (list != null) {
                            FriendFinderActivity.this.TagFuSearchFriendsList.addAll(list);
                        }
                        FriendFinderActivity.this.loadData(FriendFinderActivity.this.TagFuSearchFriendsList);
                    }

                    LOG.i("suggested user list size " + list.size());
                } else {
                    if (this.response instanceof ErrorResponse) {
                        final ErrorResponse resp = (ErrorResponse) this.response;
                        if (Util.isConnected(FriendFinderActivity.this.context)) {
                            if (FriendFinderActivity.this.searchTextView != null) {
                                FriendFinderActivity.this.searchTextView.setVisibility(View.GONE);
                                FriendFinderActivity.this.searchTextView.setText(R.string.no_search_text);
                            }
                            Alerts.showInfoOnly(resp.getMessage(), FriendFinderActivity.this.context);
                        } else {
                            if ((FriendFinderActivity.this.searchTextView != null)
                                    && _100.equalsIgnoreCase(resp.getErrorCode())) {
                                FriendFinderActivity.this.searchTextView.setText(R.string.no_connectivity_text);
                                FriendFinderActivity.this.searchTextView.setVisibility(View.VISIBLE);
                            } else {
                                Alerts.showInfoOnly(resp.getMessage(), FriendFinderActivity.this.context);
                            }
                        }
                    }
                }
            } else {
                Alerts.showInfoOnly(NETWORK_PROBLEM_PLEASE_TRY_AGAIN, FriendFinderActivity.this.context);
            }
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            if (this.showProgress) {
                this.progressDialog = ProgressDialog.show(FriendFinderActivity.this.context, EMPTY, EMPTY, true);
                this.progressDialog
                        .setContentView(((LayoutInflater) FriendFinderActivity.this.context
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.progress_bar,
                                null, false));
                this.progressDialog.setCancelable(false);
                this.progressDialog.setCanceledOnTouchOutside(false);
                this.progressDialog.show();
            }
        }
    }

    private class SocialFriendFinderAsync extends AsyncTask<Void, Void, Void> {

        private static final String NETWORK_PROBLEM_PLEASE_TRY_AGAIN = "Network problem.Please try again";
        private static final String FRINED_LIST_SIZE = "frined list size ";
        private static final String NO_FRIEND_AVAILABLE = "No Friend available";
        private ProgressDialog progressDialog;
        private final String request;
        private Object response;

        public SocialFriendFinderAsync(final String request) {

            this.request = request;
        }

        @Override
        protected Void doInBackground(final Void... params) {

            try {
                this.response = Backend.getTagFuSocialFriendsList(FriendFinderActivity.this.context, this.request);
            } catch (final JSONException exception) {
                LOG.e(exception);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {

            super.onPostExecute(result);
            if (this.progressDialog != null) {
                this.progressDialog.dismiss();

            }
            // friendListView.onRefreshComplete();
            if (this.response != null) {
                if (this.response instanceof List<?>) {
                    final List<Friend> list = (ArrayList<Friend>) this.response;
                    if ((list != null) && (list.size() > 0)) {
                        LOG.i(FRINED_LIST_SIZE + list.size());
                        FriendFinderActivity.this.setFriendListAdapter(list);
                    } else {
                        Alerts.showInfoOnly(NO_FRIEND_AVAILABLE, FriendFinderActivity.this.context);
                    }
                } else {
                    if (this.response instanceof ErrorResponse) {
                        final ErrorResponse resp = (ErrorResponse) this.response;
                        Alerts.showInfoOnly(resp.getMessage(), FriendFinderActivity.this.context);
                    }
                }
            } else {
                Alerts.showInfoOnly(NETWORK_PROBLEM_PLEASE_TRY_AGAIN, FriendFinderActivity.this.context);
            }

        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            this.progressDialog = ProgressDialog.show(FriendFinderActivity.this.context, EMPTY, EMPTY, true);
            this.progressDialog.setContentView(((LayoutInflater) FriendFinderActivity.this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.progress_bar, null, false));
            this.progressDialog.setCancelable(false);
            this.progressDialog.setCanceledOnTouchOutside(false);
            this.progressDialog.show();
        }
    }

}
