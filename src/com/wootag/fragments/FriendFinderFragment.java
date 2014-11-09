/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.fragments;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wootag.Constant;
import com.wootag.R;
import com.wootag.VideoPlayerApp;
import com.wootag.adapter.ContactAdapter;
import com.wootag.adapter.FriendFinderAdapter;
import com.wootag.adapter.PeopleAdapter;
import com.wootag.async.ContactAsync;
import com.wootag.dto.Contact;
import com.wootag.dto.ErrorResponse;
import com.wootag.dto.Friend;
import com.wootag.dto.People;
import com.wootag.dto.User;
import com.wootag.model.Backend;
import com.wootag.util.Alerts;
import com.wootag.util.Config;
import com.wootag.util.ContactInterface;
import com.wootag.util.InviteInterface;
import com.wootag.util.Util;

public class FriendFinderFragment extends FriendsListFragment implements TextWatcher, InviteInterface, ContactInterface {

    private static final String FIND_FRIEND = "Find Friend";

    private static final String PLEASE_SELECT_ATLEAST_ONE_CONTACT_TO_INVITE = "Please select atleast one contact to invite";

    private static final String NO_FRIENDS_AVAILABLE = "No friends available.";

    public static FriendFinderFragment findFriendActivity;

    protected static final Logger LOG = LoggerManager.getLogger();
    private static final int VIDEOS_PER_PAGE = 10;

    private AutoCompleteTextView fbsearch;
    private Button back;
    private Button menu;
    protected Button search;
    protected ContactAdapter contactAdapter;
    protected EditText searchEdit;
    private FriendFinderAdapter friendFinder;
    private ImageButton fbbackButton;
    private ImageButton inviteDone;
    private ImageView searchIcon;
    protected LinearLayout fbFriendListLayout;
    protected LinearLayout socialActionsLayout;
    protected List<Contact> contactFilterdList;
    protected List<Contact> contactList;
    private List<Friend> adapterList;
    protected List<Friend> filterdList;
    private List<People> adapterFriendsList;
    protected List<People> wootagFriendsList;
    protected List<People> wootagSearchFriendsList;
    private ListView contactListView;
    private ListView fbfrndList;
    private ListView friendListView;
    private PeopleAdapter friendAdapter;
    protected RelativeLayout searchLayout;
    private RelativeLayout shareContact;
    private RelativeLayout shareFacebook;
    private RelativeLayout shareGoogle;
    private RelativeLayout shareTwitter;
    private String gplusFriendId;
    private String gplusFriendname;
    private String screenType;
    private TextView heading;
    protected TextView searchTextView;
    private View friendFinderView;
    boolean flagLoading;
    boolean pullToRefresh;
    boolean searchRequest;
    protected int preLast;

    public static JSONArray getFriendListObject(final List<Friend> list) throws JSONException {

        final JSONArray friendArray = new JSONArray();
        for (int i = 0; i < list.size(); i++) {
            final JSONObject values = new JSONObject();
            final Friend friend = list.get(i);
            values.put("photo_path", friend.getFriendImage());
            values.put("id", friend.getFriendId());
            values.put("user_name", friend.getFriendName());
            values.put("description", "");
            friendArray.put(values);
        }

        return friendArray;
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
        } else if (Constant.CONTACTS.equalsIgnoreCase(Config.getSocialSite())) {
            this.setSearchContactAdapter(VideoPlayerApp.getInstance().getContactsList(), text);
        }

    }

    @Override
    public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

    }

    public void clearContactList() {

        if ((this.contactList != null) && (this.contactList.size() > 0)) {
            this.contactList.clear();
            ((BaseAdapter) this.contactListView.getAdapter()).notifyDataSetChanged();
        }

    }

    public void clearList() {

        if ((this.adapterList != null) && (this.adapterList.size() > 0)) {
            this.adapterList.clear();
            ((BaseAdapter) this.fbfrndList.getAdapter()).notifyDataSetChanged();
        }

    }

    @Override
    public void contacts(final List<Contact> contacts) {

        if ((contacts != null) && (contacts.size() > 0)) {
            VideoPlayerApp.getInstance().setContacts(contacts);
            this.setContactAdapter(contacts);
        }
    }

    public JSONObject getSearchJSONRequest(final int pageNo) throws JSONException {

        final JSONObject request = new JSONObject();
        final JSONObject obj = new JSONObject();
        obj.put("name", this.searchEdit.getText().toString());
        obj.put("browse_by", "people");
        obj.put("userid", Config.getUserId());
        obj.put("page_no", pageNo);
        obj.put(Constant.RESOLUTION, Config.getDeviceResolutionValue());
        obj.put(Constant.DEVICE_TYPE, Constant.DEVICE_MODEL);
        request.put("user", obj);
        return request;
    }

    public JSONObject getSocialLoginRequest(final List<Friend> list) throws JSONException {

        final JSONObject obj = new JSONObject();
        obj.put("user_id", Config.getUserId());
        if ((list != null) && (list.size() > 0)) {
            obj.put("friends", FriendFinderFragment.getFriendListObject(list));
        } else {
            obj.put("friends", "");
        }
        obj.put("device_token", Config.getDeviceToken());
        obj.put(Constant.DEVICE_TYPE, Constant.DEVICE_MODEL);
        obj.put(Constant.RESOLUTION, Config.getDeviceResolutionValue());

        return obj;
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

        this.inviteDone.setVisibility(View.GONE);
        if (v.getId() == R.id.fbfrinedfinder) {
            if (this.contactListView != null) {
                this.contactListView.setVisibility(View.GONE);
            }
            this.fbfrndList.setVisibility(View.VISIBLE);
            LOG.i("fb on click ");
            this.clearList();
            Config.setSocialSite(Constant.FACEBOOK);
            this.fbFriendListLayout.setVisibility(View.VISIBLE);
            if ((VideoPlayerApp.getInstance().getFbFriendsList() != null)
                    && (VideoPlayerApp.getInstance().getFbFriendsList().size() > 0)) {
                friendList = VideoPlayerApp.getInstance().getFbFriendsList();
                // setFriendListAdapter(friendList);
                try {
                    new SocialFriendFinderAsync(this.getSocialLoginRequest(friendList).toString()).execute();
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }
            } else {
                super.onClick(v);
            }
        } else if (v.getId() == R.id.twitterfrinedfinder) {
            if (this.contactListView != null) {
                this.contactListView.setVisibility(View.GONE);
            }
            this.fbfrndList.setVisibility(View.VISIBLE);
            Config.setSocialSite(Constant.TWITTER);
            if ((VideoPlayerApp.getInstance().getTwitterFriendList() != null)
                    && (VideoPlayerApp.getInstance().getTwitterFriendList().size() > 0)) {
                friendList = VideoPlayerApp.getInstance().getTwitterFriendList();
                // setFriendListAdapter(friendList);
                try {
                    new SocialFriendFinderAsync(this.getSocialLoginRequest(friendList).toString()).execute();
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }
            } else {
                super.onClick(v);
            }
        } else if (v.getId() == R.id.googlefrinedfinder) {
            if (this.contactListView != null) {
                this.contactListView.setVisibility(View.GONE);
            }
            this.fbfrndList.setVisibility(View.VISIBLE);
            Config.setSocialSite(Constant.GOOGLE_PLUS);
            if ((VideoPlayerApp.getInstance().getGoogleFriendList() != null)
                    && (VideoPlayerApp.getInstance().getGoogleFriendList().size() > 0)) {
                friendList = VideoPlayerApp.getInstance().getGoogleFriendList();
                // setFriendListAdapter(friendList);
                try {
                    new SocialFriendFinderAsync(this.getSocialLoginRequest(friendList).toString()).execute();
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }
            } else {
                super.onClick(v);
            }
        } else if (v.getId() == R.id.contactfrinedfinder) {
            if (this.fbfrndList != null) {
                this.fbfrndList.setVisibility(View.GONE);
            }
            this.contactListView.setVisibility(View.VISIBLE);
            this.inviteDone.setVisibility(View.VISIBLE);
            Config.setSocialSite(Constant.CONTACTS);
            if ((VideoPlayerApp.getInstance().getContactsList() != null)
                    && (VideoPlayerApp.getInstance().getContactsList().size() > 0)) {
                for (int i = 0; i < VideoPlayerApp.getInstance().getContactsList().size(); i++) {
                    VideoPlayerApp.getInstance().getContactsList().get(i).setChecked(false);
                }
                this.setContactAdapter(VideoPlayerApp.getInstance().getContactsList());
            } else {
                final ContactAsync async = new ContactAsync(this.context);
                async.delegate = FriendFinderFragment.this;
                async.execute();
            }
        }

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        this.friendFinderView = inflater.inflate(R.layout.friend_finder, container, false);
        findFriendActivity = this;
        this.context = this.getActivity();
        final Bundle bundle = this.getArguments();
        if ((bundle != null) && bundle.containsKey(Constant.ROOT_FRAGMENT)) {
            this.screenType = bundle.getString(Constant.ROOT_FRAGMENT);
        }
        this.wootagFriendsList = new ArrayList<People>();
        this.wootagSearchFriendsList = new ArrayList<People>();
        this.adapterFriendsList = new ArrayList<People>();
        this.loadViews();
        final FriendFinderAsync req = new FriendFinderAsync(Config.getUserId(), 1, true, this.searchRequest);
        req.execute();
        return this.friendFinderView;
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
                    new SocialFriendFinderAsync(this.getSocialLoginRequest(friendList).toString()).execute();
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }
            } else if (Constant.FACEBOOK.equalsIgnoreCase(socialSite)) {
                super.processFinish(friendList, socialSite);
            } else if (Constant.GOOGLE_PLUS.equalsIgnoreCase(socialSite)) {
                VideoPlayerApp.getInstance().setGoogleFriendList(friendList);
                try {
                    new SocialFriendFinderAsync(this.getSocialLoginRequest(friendList).toString()).execute();
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

    private LinearLayout getHeaderView() {

        final LinearLayout view = (LinearLayout) ((LayoutInflater) this.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.friend_finder_social_site, null);
        this.shareGoogle = (RelativeLayout) view.findViewById(R.id.googlefrinedfinder);
        this.shareFacebook = (RelativeLayout) view.findViewById(R.id.fbfrinedfinder);
        this.shareTwitter = (RelativeLayout) view.findViewById(R.id.twitterfrinedfinder);
        this.shareContact = (RelativeLayout) view.findViewById(R.id.contactfrinedfinder);
        this.shareGoogle.setOnClickListener(this);
        this.shareFacebook.setOnClickListener(this);
        this.shareTwitter.setOnClickListener(this);
        this.shareContact.setOnClickListener(this);
        return view;
    }

    private void loadViews() {

        this.fbsearch = (AutoCompleteTextView) this.friendFinderView.findViewById(R.id.invitefriendsearch);
        this.searchIcon = (ImageView) this.friendFinderView.findViewById(R.id.socialiconimageview);
        this.fbsearch.addTextChangedListener(this);

        this.menu = (Button) this.friendFinderView.findViewById(R.id.menu);
        this.search = (Button) this.friendFinderView.findViewById(R.id.settings);
        this.back = (Button) this.friendFinderView.findViewById(R.id.back);
        // send = (ImageButton) findViewById(R.id.seatchImageButton);
        this.searchEdit = (EditText) this.friendFinderView.findViewById(R.id.searchEditText);
        this.searchLayout = (RelativeLayout) this.friendFinderView.findViewById(R.id.searchRL);
        this.heading = (TextView) this.friendFinderView.findViewById(R.id.heading);
        this.heading.setText(FIND_FRIEND);
        this.menu.setVisibility(View.GONE);
        this.search.setVisibility(View.VISIBLE);
        this.back.setVisibility(View.VISIBLE);
        this.socialActionsLayout = (LinearLayout) this.friendFinderView.findViewById(R.id.suggestedUsersTtextView);
        // VideoPlayerConstants.setMainLayoutMargin(this, socialActionsLayout);

        this.friendListView = (ListView) this.friendFinderView.findViewById(R.id.usersLsit);

        this.friendListView.addHeaderView(this.getHeaderView());

        // social site friend list layout views
        this.fbfrndList = (ListView) this.friendFinderView.findViewById(R.id.fbfriendslist);
        this.fbbackButton = (ImageButton) this.friendFinderView.findViewById(R.id.fbback);
        friendList = new ArrayList<Friend>();
        this.adapterList = new ArrayList<Friend>();
        this.searchTextView = (TextView) this.friendFinderView.findViewById(R.id.searchView);
        this.fbFriendListLayout = (LinearLayout) this.friendFinderView.findViewById(R.id.fbfriendListLayout);

        this.contactListView = (ListView) this.friendFinderView.findViewById(R.id.friendFindercontactsList);
        this.contactList = new ArrayList<Contact>();
        this.contactListView.setVisibility(View.GONE);
        this.inviteDone = (ImageButton) this.friendFinderView.findViewById(R.id.inviteDone);

        this.inviteDone.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (Constant.CONTACTS.equalsIgnoreCase(Config.getSocialSite())
                        && (FriendFinderFragment.this.contactList != null)
                        && (FriendFinderFragment.this.contactList.size() > 0)) {
                    final List<Contact> selectedContacts = new ArrayList<Contact>();
                    for (int i = 0; i < FriendFinderFragment.this.contactList.size(); i++) {
                        final Contact contact = FriendFinderFragment.this.contactList.get(i);
                        if (contact.isChecked()) {
                            selectedContacts.add(contact);
                        }
                    }
                    if ((selectedContacts != null) && (selectedContacts.size() > 0)) {
                        FriendFinderFragment.this.sendSms(selectedContacts);
                    } else {
                        Alerts.showInfoOnly(PLEASE_SELECT_ATLEAST_ONE_CONTACT_TO_INVITE,
                                FriendFinderFragment.this.context);
                    }
                }
            }
        });

        this.search.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (FriendFinderFragment.this.searchLayout.isShown()) {
                    FriendFinderFragment.this.searchLayout.setVisibility(View.GONE);
                    FriendFinderFragment.this.search.setBackgroundResource(R.drawable.search1);
                    FriendFinderFragment.this.searchRequest = false;
                    FriendFinderFragment.this.searchTextView.setVisibility(View.GONE);
                    FriendFinderFragment.this.searchEdit.setText("");
                    FriendFinderFragment.this.loadData(FriendFinderFragment.this.wootagFriendsList);
                } else {
                    FriendFinderFragment.this.searchLayout.setVisibility(View.VISIBLE);
                    FriendFinderFragment.this.search.setBackgroundResource(R.drawable.cancelbutton);
                    FriendFinderFragment.this.searchRequest = true;
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

                if ((FriendFinderFragment.this.wootagSearchFriendsList != null)
                        && (FriendFinderFragment.this.wootagSearchFriendsList.size() > 0)) {
                    FriendFinderFragment.this.wootagSearchFriendsList.clear();
                }
                final String text = FriendFinderFragment.this.searchEdit.getText().toString();
                final InputMethodManager mgr = (InputMethodManager) FriendFinderFragment.this.getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(FriendFinderFragment.this.searchEdit.getWindowToken(), 0);
                if ((text != null) && (text.trim().length() > 0)) {
                    final FriendFinderAsync req = new FriendFinderAsync(Config.getUserId(), 1, true,
                            FriendFinderFragment.this.searchRequest);
                    req.execute();
                } else {
                    Alerts.showInfoOnly("Enter text to search", FriendFinderFragment.this.context);
                }

            }
        });
        this.back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                // finish();
                BaseFragment.tabActivity.removeFromBackStack();
            }
        });

        this.fbbackButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                FriendFinderFragment.this.fbFriendListLayout.setVisibility(View.GONE);
                FriendFinderFragment.this.socialActionsLayout.setVisibility(View.VISIBLE);
            }
        });

        this.friendListView.setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount,
                    final int totalItemCount) {

                final int lastItem = firstVisibleItem + visibleItemCount;
                if ((lastItem == totalItemCount) && (FriendFinderFragment.this.preLast != lastItem)) { // to avoid
                    LOG.d("Last");
                    FriendFinderFragment.this.preLast = lastItem;
                    if (!FriendFinderFragment.this.flagLoading) {
                        if (FriendFinderFragment.this.searchRequest) {
                            final int offset = FriendFinderFragment.this.wootagSearchFriendsList.size();
                            if ((offset % FriendFinderFragment.VIDEOS_PER_PAGE) == 0) {
                                FriendFinderFragment.this.flagLoading = true;
                                final int pageNo = (offset / FriendFinderFragment.VIDEOS_PER_PAGE) + 1;
                                new FriendFinderAsync(Config.getUserId(), pageNo, true,
                                        FriendFinderFragment.this.searchRequest).execute();
                            }
                        } else {
                            final int offset = FriendFinderFragment.this.wootagFriendsList.size();
                            if ((offset % FriendFinderFragment.VIDEOS_PER_PAGE) == 0) {
                                FriendFinderFragment.this.flagLoading = true;
                                final int pageNo = (offset / FriendFinderFragment.VIDEOS_PER_PAGE) + 1;
                                new FriendFinderAsync(Config.getUserId(), pageNo, true,
                                        FriendFinderFragment.this.searchRequest).execute();
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

    private void setContactAdapter(final List<Contact> contacts) {

        this.socialActionsLayout.setVisibility(View.GONE);
        this.fbFriendListLayout.setVisibility(View.VISIBLE);
        this.searchIcon.setImageResource(R.drawable.contact);

        if ((contacts != null) && (contacts.size() > 0)) {
            this.contactList.clear();
            this.contactList.addAll(contacts);
        }
        this.contactAdapter = new ContactAdapter(this.context, R.layout.facebook_user, this.contactList,
                Config.getSocialSite(), false);
        this.contactListView.setAdapter(this.contactAdapter);
        this.contactListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(final AdapterView<?> arg0, final View v, final int arg2, final long arg3) {

                final Contact selectedContact = FriendFinderFragment.this.contactList.get(arg2);
                if (selectedContact.isChecked()) {
                    selectedContact.setChecked(false);
                } else {
                    selectedContact.setChecked(true);
                }
                if (FriendFinderFragment.this.contactAdapter != null) {
                    FriendFinderFragment.this.contactAdapter.notifyDataSetChanged();
                }
            }
        });

    }

    private void setSearchAdapter(final List<Friend> frndList, final String text, final String socialSite) {

        if ((text != null) && (frndList != null) && (frndList.size() > 0)) {
            this.clearList();
            new Thread(new Runnable() {

                @Override
                public void run() {

                    FriendFinderFragment.this.filterdList = new ArrayList<Friend>();
                    for (int i = 0; i < frndList.size(); i++) {
                        final Friend frnd = frndList.get(i);
                        if (frnd.getFriendName().toLowerCase(Locale.getDefault())
                                .indexOf(text.toLowerCase(Locale.getDefault())) != -1) {
                            FriendFinderFragment.this.filterdList.add(frnd);
                        }
                    }
                }
            }).start();
            try {
                Thread.sleep(50);
            } catch (final InterruptedException exception) {
                LOG.e(exception);
            }
            if ((this.filterdList != null) && (this.filterdList.size() > 0)) {
                this.setFriendListAdapter(this.filterdList);
            }

        }
    }

    private void setSearchContactAdapter(final List<Contact> frndList, final String text) {

        if ((text != null) && (frndList != null) && (frndList.size() > 0)) {
            this.clearContactList();
            new Thread(new Runnable() {

                @Override
                public void run() {

                    FriendFinderFragment.this.contactFilterdList = new ArrayList<Contact>();
                    for (int i = 0; i < frndList.size(); i++) {
                        final Contact frnd = frndList.get(i);
                        if (frnd.getContactName().toLowerCase(Locale.getDefault())
                                .indexOf(text.toLowerCase(Locale.getDefault())) != -1) {
                            FriendFinderFragment.this.contactFilterdList.add(frnd);
                        }
                    }
                }
            }).start();
            try {
                Thread.sleep(50);
            } catch (final InterruptedException e) {
                Alerts.showException(e.toString(), this.context);
            }
            if ((this.contactFilterdList != null) && (this.contactFilterdList.size() > 0)) {
                this.setContactAdapter(this.contactFilterdList);
            }

        }
    }

    protected void setFriendListAdapter(final List<Friend> friendlist) {

        if (Constant.FACEBOOK.equalsIgnoreCase(Config.getSocialSite())) {
            this.searchIcon.setImageResource(R.drawable.sharefacebook);
        } else if (Constant.TWITTER.equalsIgnoreCase(Config.getSocialSite())) {
            this.searchIcon.setImageResource(R.drawable.sharetwitter);
        } else if (Constant.GOOGLE_PLUS.equalsIgnoreCase(Config.getSocialSite())) {
            this.searchIcon.setImageResource(R.drawable.sharegoogleplus);
        }

        this.socialActionsLayout.setVisibility(View.GONE);
        this.fbFriendListLayout.setVisibility(View.VISIBLE);
        if ((friendlist != null) && (friendlist.size() > 0)) {
            this.adapterList.clear();
            this.adapterList.addAll(friendlist);
        }
        this.friendFinder = new FriendFinderAdapter(this.context, R.layout.facebook_user, this.adapterList,
                Config.getSocialSite(), FriendFinderFragment.this, this.screenType);
        this.friendFinder.delegate = FriendFinderFragment.this;
        this.fbfrndList.setAdapter(this.friendFinder);
        this.friendFinder.notifyDataSetChanged();

    }

    void loadData(final List<People> list) {

        this.adapterFriendsList.clear();
        if ((list != null) && (list.size() > 0)) {
            this.adapterFriendsList.addAll(list);
        }
        if (this.friendAdapter == null) {
            this.friendAdapter = new PeopleAdapter(this.context, 0, this.adapterFriendsList, this.screenType,
                    FriendFinderFragment.this);
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

    void sendSms(final List<Contact> selectedContacts) {

        StringBuilder uri = null;
        String contactName = "";
        uri = new StringBuilder("smsto:");
        for (int i = 0; i < selectedContacts.size(); i++) {
            if (i == 0) {
                contactName = selectedContacts.get(i).getContactName();
            }
            uri.append(selectedContacts.get(i).getContactNumber()).append(';');
        }
        final Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(uri.toString()));// "smsto:9949250565;8501070003;9505717173"
        // intent.putExtra("sms_body","www.wootag.com/invite.html");
        if (selectedContacts.size() <= 1) {
            intent.putExtra(
                    "sms_body",
                    "Hi "
                            + contactName
                            + ", Found this interesting app Wootag \n \nIt allows me to upload my video and tag the product I want to sell or myself or the location � All Inside the Video! I would love you to try www.wootag.com/invite.html");
        } else {
            intent.putExtra(
                    "sms_body",
                    "Hi All, Found this interesting app Wootag \n \n It allows me to upload my video and tag the product I want to sell or myself or the location � All Inside the Video! I would love you to try www.wootag.com/invite.html");
        }
        this.startActivity(intent);
    }

    private class FriendFinderAsync extends AsyncTask<Void, Void, Void> {

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
                    this.response = Backend.search(FriendFinderFragment.this.context,
                            FriendFinderFragment.this.getSearchJSONRequest(this.pageNo), "people");
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }

            } else {
                try {
                    this.response = Backend.getWootagFriendFinderList(FriendFinderFragment.this.context, this.userId,
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
            FriendFinderFragment.this.flagLoading = false;
            if (FriendFinderFragment.this.searchTextView != null) {
                FriendFinderFragment.this.searchTextView.setVisibility(View.GONE);
                FriendFinderFragment.this.searchTextView.setText(R.string.no_search_text);
            }
            // friendListView.onRefreshComplete();
            if (this.response != null) {
                if (this.response instanceof List<?>) {
                    final List<People> list = (ArrayList<People>) this.response;
                    if (!this.isSearch) {
                        if (FriendFinderFragment.this.pullToRefresh) {
                            FriendFinderFragment.this.pullToRefresh = false;
                            FriendFinderFragment.this.wootagFriendsList.clear();
                        }

                        if (list != null) {
                            FriendFinderFragment.this.wootagFriendsList.addAll(list);
                        }
                        FriendFinderFragment.this.loadData(FriendFinderFragment.this.wootagFriendsList);
                    } else {
                        if (FriendFinderFragment.this.pullToRefresh) {
                            FriendFinderFragment.this.pullToRefresh = false;
                            FriendFinderFragment.this.wootagSearchFriendsList.clear();
                        }

                        if (list != null) {
                            FriendFinderFragment.this.wootagSearchFriendsList.addAll(list);
                        }
                        FriendFinderFragment.this.loadData(FriendFinderFragment.this.wootagSearchFriendsList);
                    }

                    LOG.i("suggested user list size " + list.size());
                } else {
                    if (this.response instanceof ErrorResponse) {
                        final ErrorResponse resp = (ErrorResponse) this.response;
                        if (Util.isConnected(FriendFinderFragment.this.context)) {
                            if (FriendFinderFragment.this.searchTextView != null) {
                                FriendFinderFragment.this.searchTextView.setVisibility(View.GONE);
                                FriendFinderFragment.this.searchTextView.setText(R.string.no_search_text);
                            }
                            Alerts.showInfoOnly(resp.getMessage(), FriendFinderFragment.this.context);
                        } else {
                            if ((FriendFinderFragment.this.searchTextView != null)
                                    && _100.equalsIgnoreCase(resp.getErrorCode())) {
                                FriendFinderFragment.this.searchTextView.setText(R.string.no_connectivity_text);
                                FriendFinderFragment.this.searchTextView.setVisibility(View.VISIBLE);
                            } else {
                                Alerts.showInfoOnly(resp.getMessage(), FriendFinderFragment.this.context);
                            }
                        }

                        // Alerts.ShowAlertOnly("Info",resp.getMessage(), context);
                    }
                }
            } else {
                Alerts.showInfoOnly("Network problem.Please try again", FriendFinderFragment.this.context);
            }
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            if (this.showProgress) {
                this.progressDialog = ProgressDialog.show(FriendFinderFragment.this.context, "", "", true);
                this.progressDialog
                        .setContentView(((LayoutInflater) FriendFinderFragment.this.context
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.progress_bar,
                                null, false));
                this.progressDialog.setCancelable(false);
                this.progressDialog.setCanceledOnTouchOutside(false);
                this.progressDialog.show();
            }
        }
    }

    private class SocialFriendFinderAsync extends AsyncTask<Void, Void, Void> {

        private static final String EMPTY = "";
        private ProgressDialog progressDialog;
        private final String request;
        private Object response;

        public SocialFriendFinderAsync(final String request) {

            this.request = request;
        }

        @Override
        protected Void doInBackground(final Void... params) {

            try {
                this.response = Backend.getWootagSocialFriendsList(FriendFinderFragment.this.context, this.request);
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
                        LOG.i("frined list size " + list.size());
                        FriendFinderFragment.this.setFriendListAdapter(list);
                    } else {
                        Alerts.showInfoOnly("No Friend available", FriendFinderFragment.this.context);
                    }
                } else {
                    if (this.response instanceof ErrorResponse) {
                        final ErrorResponse resp = (ErrorResponse) this.response;
                        Alerts.showInfoOnly(resp.getMessage(), FriendFinderFragment.this.context);
                    }
                }
            } else {
                Alerts.showInfoOnly("Network problem.Please try again", FriendFinderFragment.this.context);
            }
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            this.progressDialog = ProgressDialog.show(FriendFinderFragment.this.context, EMPTY, EMPTY, true);
            this.progressDialog.setContentView(((LayoutInflater) FriendFinderFragment.this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.progress_bar, null, false));
            this.progressDialog.setCancelable(false);
            this.progressDialog.setCanceledOnTouchOutside(false);
            this.progressDialog.show();
        }
    }

}
