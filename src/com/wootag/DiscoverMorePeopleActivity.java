/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.wTagFu

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.woTagFudapter.PeopleAdapter;
import com.wooTagFuo.ErrorResponse;
import com.wootTagFu.People;
import com.wootaTagFul.Backend;
import com.wootagTagFuout.SlideoutActivity;
import com.wootag.TagFulerts;
import com.wootag.util.Util;

public class DiscoverMorePeopleActivity extends Activity {

    private static final String EMPTY = "";
    private static final String NAME = "name";
    private static final String USER = "user";
    private static final String PAGE_NO = "page_no";
    private static final String BROWSE_BY = "browse_by";

    public static DiscoverMorePeopleActivity discoverMorePeopleActivity;

    protected static final Logger LOG = LoggerManager.getLogger();
    private static final int VIDEOS_PER_PAGE = 10;

    private Button search;
    private Button menu;
    private List<People> searchPeopleList;

    protected List<People> peopleTabList;
    protected ListView list;
    protected Object response;
    protected PeopleAdapter peopleAdapter;
    protected RelativeLayout searchLayout;
    protected boolean flagLoading;
    protected boolean searchRequest;

    public static JSONObject getJSONRequest(final String tabName, final int pageNo) throws JSONException {

        final JSONObject request = new JSONObject();
        final JSONObject obj = new JSONObject();
        obj.put(BROWSE_BY, tabName);
        obj.put(PAGE_NO, pageNo);
        request.put(USER, obj);
        return request;
    }

    public static JSONObject getSearchJSONRequest(final String tabName, final int pageNo) throws JSONException {

        final JSONObject request = new JSONObject();
        final JSONObject obj = new JSONObject();
        obj.put(NAME, EMPTY);
        obj.put(BROWSE_BY, tabName);
        obj.put(PAGE_NO, pageNo);
        request.put(USER, obj);
        return request;
    }

    public void loadPeopleList(final List<People> currentList, final boolean isClear) {

        this.peopleTabList.clear();

        if ((currentList != null) && (currentList.size() > 0)) {
            this.peopleTabList.addAll(currentList);
        }
        if (this.peopleAdapter != null) {
            if (isClear) {
                this.list.setAdapter(this.peopleAdapter);
            }
            this.peopleAdapter.notifyDataSetChanged();
        }

    }

    @Override
    protected void onCreate(final Bundle arg0) {

        super.onCreate(arg0);
        this.setContentView(R.layout.discover_more_people);
        discoverMorePeopleActivity = this;
        this.searchPeopleList = new ArrayList<People>();
        this.peopleTabList = new ArrayList<People>();
        this.list = (ListView) this.findViewById(R.id.discoverMorePeopleListView);
        this.menu = (Button) this.findViewById(R.id.menu);
        this.search = (Button) this.findViewById(R.id.settings);
        this.searchLayout = (RelativeLayout) this.findViewById(R.id.searchRL);

        this.list.setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount,
                    final int totalItemCount) {

                if (((firstVisibleItem + visibleItemCount) == totalItemCount) && (totalItemCount != 0)
                        && !DiscoverMorePeopleActivity.this.flagLoading) {
                    DiscoverMorePeopleActivity.this.flagLoading = true;
                    DiscoverMorePeopleActivity.this.getMore();
                }
            }

            @Override
            public void onScrollStateChanged(final AbsListView view, final int scrollState) {

                DiscoverMorePeopleActivity.this.flagLoading = false;

            }
        });

        this.menu.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                final int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60,
                        DiscoverMorePeopleActivity.this.getResources().getDisplayMetrics());
                SlideoutActivity.prepare(DiscoverMorePeopleActivity.this, R.id.moreVideosView, width);
                DiscoverMorePeopleActivity.this.startActivity(new Intent(DiscoverMorePeopleActivity.this,
                        MenuActivity.class));
                DiscoverMorePeopleActivity.this.overridePendingTransition(0, 0);
            }
        });
        this.search.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                if (DiscoverMorePeopleActivity.this.searchLayout.isShown()) {
                    DiscoverMorePeopleActivity.this.searchLayout.setVisibility(View.GONE);
                    DiscoverMorePeopleActivity.this.searchRequest = false;
                } else {
                    DiscoverMorePeopleActivity.this.searchLayout.setVisibility(View.VISIBLE);
                    DiscoverMorePeopleActivity.this.searchRequest = true;
                }
            }
        });
    }

    @Override
    protected void onDestroy() {

        Util.clearImageCache(this);
        super.onDestroy();
    }

    void getMore() {

        final int offset = this.searchPeopleList.size();
        if ((offset % DiscoverMorePeopleActivity.VIDEOS_PER_PAGE) == 0) {
            this.searchRequest = true;
            new LoadPeople(this.searchPeopleList, offset, "people", this.searchRequest).execute();
        }
    }

    public class LoadPeople extends AsyncTask<Void, Void, Object> {

        private final int offset;
        private ProgressDialog pprogressDialog;
        private final List<People> people;
        private final String tab;

        public LoadPeople(final List<People> list, final int offset, final String tabType, final boolean isSearch) {

            this.people = list;
            this.offset = offset;
            this.tab = tabType;

        }

        @Override
        protected Object doInBackground(final Void... params) {

            final int pageNo = (this.offset / DiscoverMorePeopleActivity.VIDEOS_PER_PAGE) + 1;
            try {
                if (DiscoverMorePeopleActivity.this.searchRequest) {
                    DiscoverMorePeopleActivity.this.response = Backend.search(DiscoverMorePeopleActivity.this,
                            DiscoverMorePeopleActivity.getSearchJSONRequest(this.tab, pageNo), this.tab);
                } else {
                    DiscoverMorePeopleActivity.this.response = Backend.browseVideos(DiscoverMorePeopleActivity.this,
                            DiscoverMorePeopleActivity.getJSONRequest(this.tab, pageNo), this.tab, false, false);
                }
            } catch (final JSONException exception) {
                LOG.e(exception);
            }
            return DiscoverMorePeopleActivity.this.response;
        }

        @Override
        protected void onPostExecute(final Object result) {

            super.onPostExecute(result);
            this.pprogressDialog.dismiss();
            if (result != null) {
                if (DiscoverMorePeopleActivity.this.response instanceof ErrorResponse) {
                    final ErrorResponse res = (ErrorResponse) DiscoverMorePeopleActivity.this.response;
                    Alerts.showAlertOnly("Info", res.getMessage(), DiscoverMorePeopleActivity.this);
                    DiscoverMorePeopleActivity.this.loadPeopleList(this.people, false);
                } else if (DiscoverMorePeopleActivity.this.response instanceof List<?>) {
                    final List<People> newList = (ArrayList<People>) DiscoverMorePeopleActivity.this.response;
                    if (DiscoverMorePeopleActivity.this.peopleAdapter == null) {
                        if ((newList != null) && (newList.size() > 0)) {
                            for (int i = 0; i < newList.size(); i++) {
                                DiscoverMorePeopleActivity.this.peopleTabList.add(newList.get(i));
                            }
                        }
                        DiscoverMorePeopleActivity.this.peopleAdapter = new PeopleAdapter(
                                DiscoverMorePeopleActivity.this, 0, DiscoverMorePeopleActivity.this.peopleTabList,
                                "morepeople", null);
                        LOG.i("commentList set adaptere");
                        DiscoverMorePeopleActivity.this.list.setAdapter(DiscoverMorePeopleActivity.this.peopleAdapter);
                    } else {
                        if ((newList != null) && (newList.size() > 0)) {
                            for (int i = 0; i < newList.size(); i++) {
                                this.people.add(newList.get(i));
                            }
                            DiscoverMorePeopleActivity.this.loadPeopleList(this.people, false);
                        }
                    }
                }
            } else {
                Toast.makeText(DiscoverMorePeopleActivity.this, "Problem with Server", Toast.LENGTH_LONG).show();
            }
            DiscoverMorePeopleActivity.this.flagLoading = false;
        }

        @Override
        protected void onPreExecute() {

            this.pprogressDialog = ProgressDialog.show(DiscoverMorePeopleActivity.this, EMPTY, EMPTY, true);
            this.pprogressDialog.setContentView(((LayoutInflater) DiscoverMorePeopleActivity.this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.progress_bar, null, false));
            this.pprogressDialog.setCancelable(false);
            this.pprogressDialog.setCanceledOnTouchOutside(false);
            this.pprogressDialog.show();
        }
    }
}
