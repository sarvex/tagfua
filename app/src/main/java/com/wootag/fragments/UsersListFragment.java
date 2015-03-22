/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.TagFu.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.TagFu.Constant;
import com.TagFu.R;
import com.TagFu.adapter.FollowersListAdapter;
import com.TagFu.adapter.PrivateGroupAdapter;
import com.TagFu.dto.ErrorResponse;
import com.TagFu.dto.Friend;
import com.TagFu.model.Backend;
import com.TagFu.pulltorefresh.PullToRefreshBase;
import com.TagFu.pulltorefresh.PullToRefreshBase.OnRefreshListener;
import com.TagFu.pulltorefresh.PullToRefreshListView;
import com.TagFu.util.Alerts;
import com.TagFu.util.Config;
import com.TagFu.util.Util;

public class UsersListFragment extends BaseFragment {

    private static final String SLASH = "/";

    private static final String EMPTY = "";

    private static final String ID = "id";

    private static final String TYPE = "type";

    private static final String PRIVATE_REQUEST_PENDING = "Private Request Pending";

    private static final String PRIVATE_GROUP = " Private Group";

    private static final String _150 = "/150";

    private static final String FOLLOWINGS2 = "Followings";

    private static final String _FOLLOWING = " Following";

    private static final String FOLLOWINGS = "followings";

    private static final String _FOLLOWERS = " Followers";

    private static final String FOLLOWERS = "followers";

    private static final String PRIVATEPENDINGREQUEST = "privatependingrequest";

    private static final String COUNT2 = "count";

    public static UsersListFragment usersListActivity;

    protected static final Logger LOG = LoggerManager.getLogger();

    private static final String USERID = "userid";
    public String followersURL = Constant.COMMON_URL_MOBILE + "followers/";
    public String followingURL = Constant.COMMON_URL_MOBILE + "followings/";
    private TextView count;
    private ImageView edit;
    protected TextView errorMessageTextView;
    protected boolean flagLoading;
    protected LayoutInflater inflater;
    protected boolean pullToRefresh;
    private static final int PAGE_SIZE = 10;
    private int pendingRequests;
    private final String PrivateGroupList = Constant.COMMON_URL_MOBILE + "pvtgrouplist/";
    private LinearLayout requestView;
    protected String rootFragment = EMPTY;
    private int userCount;
    private View view;
    protected FollowersListAdapter adapter;
    protected List<Friend> list;
    protected PrivateGroupAdapter privateGroupAdapter;
    protected String type;
    private String userId;
    protected PullToRefreshListView usersList;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        this.view = null;
        this.view = inflater.inflate(R.layout.activity_list, container, false);
        usersListActivity = this;
        this.inflater = inflater;
        Config.setPrivateGroupEditMode(false);
        final TextView heading = (TextView) this.view.findViewById(R.id.heading);
        final TextView pendingRequest = (TextView) this.view.findViewById(R.id.pendingrequests);
        final Button menu = (Button) this.view.findViewById(R.id.menu);
        final Button search = (Button) this.view.findViewById(R.id.settings);
        this.list = new ArrayList<Friend>();
        this.usersList = (PullToRefreshListView) this.view.findViewById(R.id.followersList);
        this.requestView = (LinearLayout) this.view.findViewById(R.id.requestView);
        this.count = ((TextView) this.view.findViewById(R.id.usersCountTextview));
        this.edit = ((ImageView) this.view.findViewById(R.id.edit));
        this.errorMessageTextView = (TextView) this.view.findViewById(R.id.userlisterrormessageView);
        final Bundle in = this.getArguments();
        if (in != null) {
            this.type = in.getString(TYPE);
            this.userId = in.getString(ID);
            if (in.containsKey(COUNT2)) {
                this.userCount = Integer.parseInt(in.getString(COUNT2));
            }
            if (in.containsKey(Constant.ROOT_FRAGMENT)) {
                this.rootFragment = in.getString(Constant.ROOT_FRAGMENT);
            }
            if (in.containsKey(PRIVATEPENDINGREQUEST)) {
                this.pendingRequests = Integer.parseInt(in.getString(PRIVATEPENDINGREQUEST));
            }
        }
        if (FOLLOWERS.equalsIgnoreCase(this.type)) {
            heading.setText(R.string.followers);
            if (this.userCount > 0) {
                this.count.setText(this.userCount + _FOLLOWERS);
            } else {
                this.count.setText(R.string.followers);
            }
        } else if (FOLLOWINGS.equalsIgnoreCase(this.type)) {
            heading.setText(FOLLOWINGS2);
            if (this.userCount > 0) {
                this.count.setText(this.userCount + _FOLLOWING);
            } else {
                this.count.setText(FOLLOWINGS2);
            }
        } else {
            heading.setText(R.string.private_group);
            if (Constant.MY_PAGE.equalsIgnoreCase(this.rootFragment)) {
                this.edit.setVisibility(View.VISIBLE);
            } else {
                this.edit.setVisibility(View.GONE);
            }
            if (this.userCount > 0) {
                this.count.setText(this.userCount + _150 + PRIVATE_GROUP);
            } else {
                this.count.setText(R.string.private_group);
            }
            if (this.pendingRequests > 0) {
                pendingRequest.setText(this.pendingRequests + PRIVATE_REQUEST_PENDING);
                this.requestView.setVisibility(View.VISIBLE);
            } else {
                this.requestView.setVisibility(View.GONE);
            }
        }
        search.setVisibility(View.GONE);
        menu.setVisibility(View.GONE);
        final Button back = (Button) this.view.findViewById(R.id.back);
        back.setVisibility(View.VISIBLE);
        back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                BaseFragment.tabActivity.removeFromBackStack();
                // getActivity().getSupportFragmentManager().popBackStackImmediate();
            }
        });
        new UserListAsyncTask(1, true).execute();
        this.requestView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (Constant.MY_PAGE.equalsIgnoreCase(UsersListFragment.this.rootFragment)) {
                    final PendingPrivateRequestFragment fragment = new PendingPrivateRequestFragment();

                    BaseFragment.tabActivity.pushFragments(R.id.mypageTab, fragment,
                            Constant.PRIVATE_GROUP_PENDING_REQUESTS, UsersListFragment.this, Constant.MYPAGE);
                }
            }
        });
        this.edit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (Config.isPrivateGroupEditMode()) {
                    Config.setPrivateGroupEditMode(false);
                } else {
                    Config.setPrivateGroupEditMode(true);
                }
                if (UsersListFragment.this.privateGroupAdapter != null) {
                    UsersListFragment.this.privateGroupAdapter.notifyDataSetChanged();
                }
            }
        });
        this.usersList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(final AdapterView<?> arg0, final View arg1, final int arg2, final long arg3) {

                final Friend item = UsersListFragment.this.list.get(arg2 - 1);
                if (item.getFriendId() != null) {
                    final int id = Integer.parseInt(item.getFriendId());
                    if (id > 0) {
                        UsersListFragment.this.gotToOtherPage(id);
                    }

                } else {
                    Alerts.showAlertOnly("Info", "User id not available", UsersListFragment.this.getActivity());
                }
            }
        });
        // Set a listener to be invoked when the list should be refreshed.
        this.usersList.setOnRefreshListener(new OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(final PullToRefreshBase<ListView> refreshView) {

                UsersListFragment.this.pullToRefresh = true;
                new UserListAsyncTask(1, false).execute();
            }

        });
        this.usersList.setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount,
                    final int totalItemCount) {

                if (((firstVisibleItem + visibleItemCount) == totalItemCount) && (totalItemCount != 0)
                        && !UsersListFragment.this.flagLoading) {
                    UsersListFragment.this.flagLoading = true;
                    if ((UsersListFragment.this.list.size() % UsersListFragment.PAGE_SIZE) == 0) {
                        final int currentPageNo = (UsersListFragment.this.list.size() / UsersListFragment.PAGE_SIZE) + 1;
                        new UserListAsyncTask(currentPageNo, true).execute();
                    }
                }
            }

            @Override
            public void onScrollStateChanged(final AbsListView view, final int scrollState) {

                UsersListFragment.this.flagLoading = false;

            }
        });

        return this.view;
    }

    String getJsonRequest(final int pageNo, final String type) {

        String request = EMPTY;
        if (FOLLOWERS.equalsIgnoreCase(type)) {
            request = this.followersURL + Config.getUserId() + SLASH + this.userId + SLASH + pageNo;
        } else if (FOLLOWINGS.equalsIgnoreCase(type)) {
            request = this.followingURL + Config.getUserId() + SLASH + this.userId + SLASH + pageNo;
        } else {
            request = this.PrivateGroupList + this.userId + SLASH + pageNo;
        }
        return request;

    }

    void gotToOtherPage(final int id) {

        final OtherUserFragment fragment = new OtherUserFragment(); // object of next fragment
        final Bundle bundle = new Bundle();

        if (Constant.MY_PAGE.equalsIgnoreCase(this.rootFragment)) {
            bundle.putString(Constant.ROOT_FRAGMENT, Constant.MY_PAGE);
            bundle.putString(USERID, String.valueOf(id));
            fragment.setArguments(bundle);
            BaseFragment.tabActivity.pushFragments(R.id.mypageTab, fragment, Constant.OTHERS_PAGE,
                    UsersListFragment.this, Constant.MYPAGE);

        } else if (Constant.MY_PAGE_MORE_FEEDS.equalsIgnoreCase(this.rootFragment)) {
            bundle.putString(Constant.ROOT_FRAGMENT, Constant.MY_PAGE);
            bundle.putString(USERID, String.valueOf(id));
            fragment.setArguments(bundle);
            BaseFragment.tabActivity.pushFragments(R.id.mypageTab, fragment, Constant.OTHERS_PAGE,
                    UsersListFragment.this, Constant.MYPAGE);

        } else if (Constant.MORE_VIDEOS.equalsIgnoreCase(this.rootFragment)) {
            bundle.putString(Constant.ROOT_FRAGMENT, Constant.MY_PAGE);
            bundle.putString(USERID, String.valueOf(id));
            fragment.setArguments(bundle);
            BaseFragment.tabActivity.pushFragments(R.id.mypageTab, fragment, Constant.OTHERS_PAGE,
                    UsersListFragment.this, Constant.MYPAGE);

        } else if (Constant.VIDEO_FEEDS.equalsIgnoreCase(this.rootFragment)) {
            bundle.putString(Constant.ROOT_FRAGMENT, Constant.VIDEO_FEEDS);
            bundle.putString(USERID, String.valueOf(id));
            fragment.setArguments(bundle);
            BaseFragment.tabActivity.pushFragments(R.id.feedTab, fragment, Constant.OTHERS_PAGE,
                    UsersListFragment.this, Constant.HOME);

        } else if (Constant.NOTIFICATIONS_PAGE.equalsIgnoreCase(this.rootFragment)) {
            bundle.putString(Constant.ROOT_FRAGMENT, Constant.NOTIFICATIONS_PAGE);
            bundle.putString(USERID, String.valueOf(id));
            fragment.setArguments(bundle);
            BaseFragment.tabActivity.pushFragments(R.id.notificationTab, fragment, Constant.OTHERS_PAGE,
                    UsersListFragment.this, Constant.NOTIFICATIONS);

        } else if (Constant.BROWSE_PAGE.equalsIgnoreCase(this.rootFragment)) {
            bundle.putString(Constant.ROOT_FRAGMENT, Constant.BROWSE_PAGE);
            bundle.putString(USERID, String.valueOf(id));
            fragment.setArguments(bundle);
            BaseFragment.tabActivity.pushFragments(R.id.browsTab, fragment, Constant.OTHERS_PAGE,
                    UsersListFragment.this, Constant.BROWSE);
        }
    }

    public class UserListAsyncTask extends AsyncTask<Void, Void, Void> {

        private static final String PVTGROUP = "pvtgroup";
        private static final String _100 = "100";
        private final boolean progressVisible;
        List<Friend> currentList;
        int pageNumber;
        ProgressDialog progressDialog;

        Object response;

        public UserListAsyncTask(final int pageNo, final boolean progressVisible) {

            this.pageNumber = pageNo;
            this.progressVisible = progressVisible;
        }

        @Override
        protected Void doInBackground(final Void... params) {

            LOG.v("Service" + UsersListFragment.this.type);

            try {
                this.response = Backend.getUsersList(UsersListFragment.this.getActivity(),
                        UsersListFragment.this.getJsonRequest(this.pageNumber, UsersListFragment.this.type),
                        UsersListFragment.this.type);
            } catch (final JSONException exception) {
                LOG.e(exception);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {

            super.onPostExecute(result);
            if (this.progressVisible && (this.progressDialog != null)) {
                this.progressDialog.dismiss();
            }
            UsersListFragment.this.errorMessageTextView.setVisibility(View.GONE);
            UsersListFragment.this.usersList.onRefreshComplete();
            UsersListFragment.this.flagLoading = false;
            if (this.response instanceof ErrorResponse) {
                final ErrorResponse res = (ErrorResponse) this.response;
                if (Util.isConnected(UsersListFragment.this.getActivity())) {
                    UsersListFragment.this.errorMessageTextView.setVisibility(View.GONE);
                    Alerts.showInfoOnly(res.getMessage(), UsersListFragment.this.getActivity());
                } else {
                    if ((UsersListFragment.this.errorMessageTextView != null)
                            && _100.equalsIgnoreCase(res.getErrorCode())) {
                        UsersListFragment.this.errorMessageTextView.setText(R.string.no_connectivity_text);
                        UsersListFragment.this.errorMessageTextView.setVisibility(View.VISIBLE);
                    }
                }

            } else if (this.response instanceof List<?>) {
                this.currentList = (ArrayList<Friend>) this.response;
            }

            if (UsersListFragment.this.pullToRefresh) {
                UsersListFragment.this.pullToRefresh = false;
                UsersListFragment.this.usersList.onRefreshComplete();
                if ((this.currentList != null) && (this.currentList.size() > 0)) {
                    UsersListFragment.this.list = this.currentList;
                    if (PVTGROUP.equalsIgnoreCase(UsersListFragment.this.type)) {
                        UsersListFragment.this.privateGroupAdapter = new PrivateGroupAdapter(
                                UsersListFragment.this.getActivity(), UsersListFragment.this.list,
                                UsersListFragment.this.type, Config.isPrivateGroupEditMode());
                        UsersListFragment.this.usersList.setAdapter(UsersListFragment.this.privateGroupAdapter);
                    } else {
                        UsersListFragment.this.adapter = new FollowersListAdapter(UsersListFragment.this.getActivity(),
                                UsersListFragment.this.list);
                        UsersListFragment.this.usersList.setAdapter(UsersListFragment.this.adapter);
                    }

                }
            } else {
                if ((this.currentList != null) && (this.currentList.size() > 0)) {
                    if (PVTGROUP.equalsIgnoreCase(UsersListFragment.this.type)) {
                        if (UsersListFragment.this.privateGroupAdapter == null) {
                            for (int i = 0; i < this.currentList.size(); i++) {
                                UsersListFragment.this.list.add(this.currentList.get(i));
                            }
                            UsersListFragment.this.privateGroupAdapter = new PrivateGroupAdapter(
                                    UsersListFragment.this.getActivity(), UsersListFragment.this.list,
                                    UsersListFragment.this.type, Config.isPrivateGroupEditMode());
                            UsersListFragment.this.usersList.setAdapter(UsersListFragment.this.privateGroupAdapter);
                        } else {
                            for (int i = 0; i < this.currentList.size(); i++) {
                                UsersListFragment.this.list.add(this.currentList.get(i));
                            }
                            UsersListFragment.this.privateGroupAdapter.notifyDataSetChanged();
                        }
                    } else {
                        if (UsersListFragment.this.adapter == null) {
                            for (int i = 0; i < this.currentList.size(); i++) {
                                UsersListFragment.this.list.add(this.currentList.get(i));
                            }
                            UsersListFragment.this.adapter = new FollowersListAdapter(
                                    UsersListFragment.this.getActivity(), UsersListFragment.this.list);
                            UsersListFragment.this.usersList.setAdapter(UsersListFragment.this.adapter);
                        } else {
                            for (int i = 0; i < this.currentList.size(); i++) {
                                UsersListFragment.this.list.add(this.currentList.get(i));
                            }
                            UsersListFragment.this.adapter.notifyDataSetChanged();
                        }
                    }
                }

            }
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            if (this.progressVisible) {
                this.progressDialog = ProgressDialog.show(UsersListFragment.this.getActivity(), EMPTY, EMPTY, true);
                final View v = UsersListFragment.this.inflater.inflate(R.layout.progress_bar, null, false);
                this.progressDialog.setContentView(v);
                this.progressDialog.setCancelable(false);
                this.progressDialog.setCanceledOnTouchOutside(false);
                this.progressDialog.show();
            }
        }

    }
}
