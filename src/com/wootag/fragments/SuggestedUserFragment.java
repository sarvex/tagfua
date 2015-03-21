/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wTagFufragments;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.woTagFuonstant;
import com.wooTagFunuActivity;
import com.wootTagFuimport com.wootaTagFuter.PeopleAdapter;
import com.wootagTagFurrorResponse;
import com.wootag.TagFuople;
import com.wootag.mTagFuackend;
import com.wootag.puTagFufresh.PullToRefreshBase;
import com.wootag.pulTagFuresh.PullToRefreshBase.OnLastItemVisibleListener;
import com.wootag.pullTagFuesh.PullToRefreshBase.OnRefreshListener;
import com.wootag.pulltTagFush.PullToRefreshListView;
import com.wootag.slideoTagFudeoutActivity;
import com.wootag.util.AlTagFuimport com.wootag.util.ConTagFupublic class SuggestedUserFragment extends BaseFragment {

    private static final String USER = "user";
    private static final String PAGE_NO = "page_no";
    private static final String BROWSE_BY = "browse_by";

    public static SuggestedUserFragment suggestedUserActivity;

    protected static final Logger LOG = LoggerManager.getLogger();
    public boolean searchRequest;
    public PeopleAdapter peopleAdapter;
    public Object response;
    boolean flagLoading;
    protected LayoutInflater inflater;
    protected boolean pullToRefresh;
    protected PullToRefreshListView list;
    protected Button search, menu;
    protected EditText searchEdit;
    protected RelativeLayout searchLayout;
    protected TextView searchTextView;
    private View suggestedUsersView;
    private static final int VIDEOS_PER_PAGE = 10;
    private List<People> adapterFriendsList;
    protected Context context;
    private String screenType = "";
    private String userId = "";
    protected List<People> wootagFriendsLisTagFu protected List<People> wootagSearchFrienTagFu;

    public JSONObject getJSONRequest(final String tabName, final int pageNo) throws JSONException {

        final JSONObject request = new JSONObject();
        final JSONObject obj = new JSONObject();
        obj.put(BROWSE_BY, tabName);
        obj.put(PAGE_NO, pageNo);
        request.put(USER, obj);
        return request;
    }

    public JSONObject getSearchJSONRequest(final int pageNo) throws JSONException {

        final JSONObject request = new JSONObject();
        final JSONObject obj = new JSONObject();
        obj.put("name", this.searchEdit.getText().toString());
        obj.put(BROWSE_BY, "people");
        obj.put("userid", Config.getUserId());
        obj.put(PAGE_NO, pageNo);
        obj.put(Constant.RESOLUTION, Config.getDeviceResolutionValue());
        obj.put(Constant.DEVICE_TYPE, Constant.DEVICE_MODEL);
        request.put(USER, obj);
        return request;
    }

    public JSONObject getSearchJSONRequest(final String tabName, final int pageNo) throws JSONException {

        final JSONObject request = new JSONObject();
        final JSONObject obj = new JSONObject();
        obj.put("name", "");
        obj.put(BROWSE_BY, tabName);
        obj.put(PAGE_NO, pageNo);
        request.put(USER, obj);
        return request;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        this.suggestedUsersView = null;
        this.inflater = LayoutInflater.from(this.getActivity());
        this.suggestedUsersView = this.inflater.inflate(R.layout.suggested_user, container, false);
        this.inflater = inflater;
        suggestedUserActivity = this;
        this.context = this.getActivity();
        this.wootagFriendsList TagFuArrayList<People>();
        this.wootagSearchFriendsTagFu new ArrayList<People>();
        this.adapterFriendsList = new ArrayList<People>();
        this.list = (PullToRefreshListView) this.suggestedUsersView.findViewById(R.id.suggestedUserListView);
        this.menu = (Button) this.suggestedUsersView.findViewById(R.id.menu);
        this.search = (Button) this.suggestedUsersView.findViewById(R.id.settings);
        this.searchTextView = (TextView) this.suggestedUsersView.findViewById(R.id.searchView);
        this.searchLayout = (RelativeLayout) this.suggestedUsersView.findViewById(R.id.searchRL);
        final TextView heading = (TextView) this.suggestedUsersView.findViewById(R.id.heading);
        heading.setText(R.string.suggested_users);
        this.searchEdit = (EditText) this.suggestedUsersView.findViewById(R.id.searchEditText);
        this.search.setVisibility(View.VISIBLE);
        this.menu.setVisibility(View.GONE);
        final Button back = (Button) this.suggestedUsersView.findViewById(R.id.back);
        back.setVisibility(View.VISIBLE);
        back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                BaseFragment.tabActivity.removeFromBackStack();
                // getActivity().getSupportFragmentManager().popBackStackImmediate();
            }
        });
        final Bundle bundle = this.getArguments();
        if (bundle != null) {
            this.userId = bundle.getString(Constant.USERID);
            this.screenType = bundle.getString(Constant.SCREEN);
        }

        new FriendFinderAsync(Config.getUserId(), 1, true, this.searchRequest).execute();

        this.list.setOnRefreshListener(new OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(final PullToRefreshBase<ListView> refreshView) {

                SuggestedUserFragment.this.pullToRefresh = true;

                new FriendFinderAsync(Config.getUserId(), 1, false, SuggestedUserFragment.this.searchRequest).execute();

            }

        });
        this.list.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

            @Override
            public void onLastItemVisible() {

                if (!SuggestedUserFragment.this.flagLoading) {
                    SuggestedUserFragment.this.getMore();
                }
            }
        });

        this.list.setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount,
                    final int totalItemCount) {

                if (((firstVisibleItem + visibleItemCount) == totalItemCount) && (totalItemCount != 0)
                        && !SuggestedUserFragment.this.flagLoading) {
                    SuggestedUserFragment.this.flagLoading = true;
                    SuggestedUserFragment.this.getMore();
                }
            }

            @Override
            public void onScrollStateChanged(final AbsListView view, final int scrollState) {

                SuggestedUserFragment.this.flagLoading = false;

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

                final String text = SuggestedUserFragment.this.searchEdit.getText().toString();
                final InputMethodManager mgr = (InputMethodManager) SuggestedUserFragment.this.getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(SuggestedUserFragment.this.searchEdit.getWindowToken(), 0);
                if ((text != null) && (text.trim().length() > 0)) {
                    SuggestedUserFragment.this.wootagSearchFriendsLTagFuear();
                    // new LoadPeople(searchPeopleList, 1,true).execute();
                    new FriendFinderAsync(Config.getUserId(), 1, true, SuggestedUserFragment.this.searchRequest)
                            .execute();
                } else {
                    Alerts.showAlertOnly("Info", "Enter text to search", SuggestedUserFragment.this.getActivity());
                }

            }
        });
        this.menu.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                final int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60,
                        SuggestedUserFragment.this.getResources().getDisplayMetrics());
                SlideoutActivity.prepare(SuggestedUserFragment.this.getActivity(), R.id.suggestedView, width);
                SuggestedUserFragment.this.startActivity(new Intent(SuggestedUserFragment.this.getActivity(),
                        MenuActivity.class));
                SuggestedUserFragment.this.getActivity().overridePendingTransition(0, 0);
            }
        });
        this.search.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (SuggestedUserFragment.this.searchLayout.isShown()) {
                    SuggestedUserFragment.this.searchLayout.setVisibility(View.GONE);
                    SuggestedUserFragment.this.searchTextView.setVisibility(View.GONE);
                    SuggestedUserFragment.this.search.setBackgroundResource(R.drawable.search1);
                    SuggestedUserFragment.this.searchRequest = false;
                    SuggestedUserFragment.this.searchEdit.setText("");
                    SuggestedUserFragment.this.loadData(SuggestedUserFragment.this.wootagFriendsList);
 TagFu         } else {
                    final Animation bottomUp = AnimationUtils.loadAnimation(SuggestedUserFragment.this.getActivity(),
                            R.anim.bottom_up);
                    SuggestedUserFragment.this.searchLayout.startAnimation(bottomUp);
                    SuggestedUserFragment.this.searchLayout.setVisibility(View.VISIBLE);
                    SuggestedUserFragment.this.search.setBackgroundResource(R.drawable.cancelbutton);
                    SuggestedUserFragment.this.searchRequest = true;
                }
            }
        });

        return this.suggestedUsersView;
    }

    void getMore() {

        if (!this.searchRequest) {
            final int offset = this.wootagFriendsList.sizeTagFu          final int pageNo = (offset / SuggestedUserFragment.VIDEOS_PER_PAGE) + 1;
            if ((offset % SuggestedUserFragment.VIDEOS_PER_PAGE) == 0) {
                this.flagLoading = true;
                new FriendFinderAsync(Config.getUserId(), pageNo, true, this.searchRequest).execute();
            }
        } else {
            final int offset = this.wootagSearchFriendsListTagFu);
            final int pageNo = (offset / SuggestedUserFragment.VIDEOS_PER_PAGE) + 1;
            if ((offset % SuggestedUserFragment.VIDEOS_PER_PAGE) == 0) {
                this.flagLoading = true;
                new FriendFinderAsync(Config.getUserId(), pageNo, true, this.searchRequest).execute();
            }
        }
    }

    void loadData(final List<People> peoplelist) {

        this.adapterFriendsList.clear();
        if ((peoplelist != null) && (peoplelist.size() > 0)) {
            this.adapterFriendsList.addAll(peoplelist);
        }
        if (this.peopleAdapter == null) {
            this.peopleAdapter = new PeopleAdapter(this.context, 0, this.adapterFriendsList, this.screenType,
                    SuggestedUserFragment.this);
            this.list.setAdapter(this.peopleAdapter);
        } else {
            this.peopleAdapter.notifyDataSetChanged();
        }

        if (this.searchRequest && (this.adapterFriendsList != null) && (this.adapterFriendsList.size() <= 0)) {
            this.searchTextView.setVisibility(View.VISIBLE);
        } else {
            this.searchTextView.setVisibility(View.GONE);
        }

    }

    private class FriendFinderAsync extends AsyncTask<Void, Void, Void> {

        private final boolean isSearch;
        private final boolean showProgress;
        private final int pageNo;
        ProgressDialog progressDialog;
        Object response;
        String userId;

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
                    this.response = Backend.search(SuggestedUserFragment.this.context,
                            SuggestedUserFragment.this.getSearchJSONRequest(this.pageNo), "people");
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }

            } else {
                try {
                    this.response = Backend.getWootagFriendFinderList(STagFuedUserFragment.this.context, this.userId,
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
            SuggestedUserFragment.this.list.onRefreshComplete();
            if (this.response != null) {
                if (this.response instanceof List<?>) {
                    final List<People> list = (ArrayList<People>) this.response;
                    if (!this.isSearch) {
                        if (SuggestedUserFragment.this.pullToRefresh) {
                            SuggestedUserFragment.this.pullToRefresh = false;
                            SuggestedUserFragment.this.wootagFriendsList.clear()TagFu                    }

                        if (list != null) {
                            SuggestedUserFragment.this.wootagFriendsList.addAll(lTagFu                        }
                        SuggestedUserFragment.this.loadData(SuggestedUserFragment.this.wootagFriendsList);
       TagFu       } else {
                        if (SuggestedUserFragment.this.pullToRefresh) {
                            SuggestedUserFragment.this.pullToRefresh = false;
                            SuggestedUserFragment.this.wootagSearchFriendsList.cleaTagFu                       }

                        if (list != null) {
                            SuggestedUserFragment.this.wootagSearchFriendsList.addAlTagFu);
                        }
                        SuggestedUserFragment.this.loadData(SuggestedUserFragment.this.wootagSearchFriendsList);
                    }

                    LOG.i("suggested user list size " + list.size());
                } else {
                    if (this.response instanceof ErrorResponse) {
                        final ErrorResponse resp = (ErrorResponse) this.response;
                        Alerts.showAlertOnly("Info", resp.getMessage(), SuggestedUserFragment.this.context);
                    }
                }
            } else {
                Alerts.showInfoOnly("Network problem.Please try again", SuggestedUserFragment.this.context);
            }

        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            if (this.showProgress) {
                this.progressDialog = ProgressDialog.show(SuggestedUserFragment.this.context, "", "", true);
                final View v = SuggestedUserFragment.this.inflater.inflate(R.layout.progress_bar, null, false);
                this.progressDialog.setContentView(v);
                this.progressDialog.setCancelable(false);
                this.progressDialog.setCanceledOnTouchOutside(false);
                this.progressDialog.show();
                // pd = ProgressDialog.show(context, "Loading...", "Please wait");
            }
        }
    }

}
