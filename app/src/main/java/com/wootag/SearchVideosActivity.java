/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.TagFu;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.TagFu.adapter.PostsAdapter;
import com.TagFu.async.SearchVideoAsync;
import com.TagFu.dto.Liked;
import com.TagFu.dto.MyPageDto;
import com.TagFu.pulltorefresh.PullToRefreshBase;
import com.TagFu.pulltorefresh.PullToRefreshBase.OnLastItemVisibleListener;
import com.TagFu.pulltorefresh.PullToRefreshBase.OnRefreshListener;
import com.TagFu.pulltorefresh.PullToRefreshListView;
import com.TagFu.slideout.SlideoutActivity;
import com.TagFu.util.Config;
import com.TagFu.util.MoreVideos;
import com.TagFu.util.Util;

public class SearchVideosActivity extends Activity implements MoreVideos {

    private static final String USER = "user";
    private static final String PAGE_NO = "page_no";
    private static final String LOGIN_ID = "login_id";
    private static final String USERID = "userid";
    private static final String NAME = "name";

    public static SearchVideosActivity searchVideosActivity;

    private static final Logger LOG = LoggerManager.getLogger();
    protected PostsAdapter adapter;
    protected boolean flagLoading;
    private TextView heading;
    protected boolean pullToRefreshList;
    protected Button search, menu;
    protected TextView searchTextView;
    private Context context;
    private PullToRefreshListView moreVideosListView;
    protected List<MyPageDto> myPageDtos;
    protected EditText searchEdit;
    protected RelativeLayout searchLayout;
    protected String searchText;
    protected String type;
    private String userId;
    private static final int PAGE_SIZE = 10;

    public JSONObject getJSONRequest(final int pageNo, final String text) throws JSONException {

        final JSONObject request = new JSONObject();
        final JSONObject obj = new JSONObject();
        obj.put(NAME, text);
        obj.put(USERID, this.userId);
        obj.put(LOGIN_ID, Config.getUserId());
        obj.put(Constant.DEVICE_TYPE, Constant.DEVICE_MODEL);
        obj.put(Constant.RESOLUTION, Config.getDeviceResolutionValue());
        obj.put(PAGE_NO, pageNo);
        request.put(USER, obj);

        return request;
    }

    @Override
    public void likedList(final List<Liked> likedPeople) {

    }

    @Override
    public void videoList(final List<MyPageDto> video) {

        this.flagLoading = false;
        // try {
        if (this.pullToRefreshList) {
            this.moreVideosListView.onRefreshComplete();
            if ((video != null) && (video.size() > 0)) {
                this.myPageDtos.clear();
                if ((video != null) && (video.size() > 0)) {
                    this.myPageDtos.addAll(video);
                }
            }

            if (this.adapter == null) {
                this.adapter = new PostsAdapter(this.context, 0, this.myPageDtos, Constant.MORE_VIDEOS, null);
                this.moreVideosListView.setAdapter(this.adapter);
            } else {
                this.moreVideosListView.setAdapter(this.adapter);
                this.adapter.notifyDataSetChanged();
            }

            if ((this.myPageDtos != null) && (this.myPageDtos.size() > 0)) {
                this.searchTextView.setVisibility(View.GONE);
            } else {
                this.searchTextView.setVisibility(View.VISIBLE);
            }

        } else {
            if ((video != null) && (video.size() > 0)) {
                for (int i = 0; i < video.size(); i++) {
                    this.myPageDtos.add(video.get(i));
                }
            }
            if (this.adapter == null) {
                this.adapter = new PostsAdapter(this.context, 0, this.myPageDtos, Constant.MORE_VIDEOS, null);
                this.moreVideosListView.setAdapter(this.adapter);
            } else {
                this.adapter.notifyDataSetChanged();
            }
        }

        if ((this.myPageDtos != null) && (this.myPageDtos.size() > 0)) {
            this.searchTextView.setVisibility(View.GONE);
        } else {
            this.searchTextView.setVisibility(View.VISIBLE);
        }

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (this.pullToRefreshList) {
            this.moreVideosListView.onRefreshComplete();
        }
        // } catch (final Exception e) {
        // LOG.i(this.getClass().getName(), e.toString());
        // }
    }

    @Override
    public void videoList(final List<MyPageDto> video, final String type) {

    }

    @Override
    public void videoList(final List<MyPageDto> video, final String type, final boolean next) {

    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        // try {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.my_more_videos);
        searchVideosActivity = this;
        this.context = this;
        this.menu = (Button) this.findViewById(R.id.menu);
        this.search = (Button) this.findViewById(R.id.settings);
        // send = (ImageButton) findViewById(R.id.seatchImageButton);
        this.searchEdit = (EditText) this.findViewById(R.id.searchEditText);
        this.searchLayout = (RelativeLayout) this.findViewById(R.id.searchRL);
        this.moreVideosListView = (PullToRefreshListView) this.findViewById(R.id.moreVideosListView);
        this.myPageDtos = new ArrayList<MyPageDto>();
        this.searchLayout.setVisibility(View.VISIBLE);
        this.search.setBackgroundResource(R.drawable.cancelbutton);
        this.heading = (TextView) this.findViewById(R.id.heading);
        this.searchTextView = (TextView) this.findViewById(R.id.searchView);
        this.heading.setText("Search");

        final LinearLayout bodyLayout = (LinearLayout) this.findViewById(R.id.bodyLayout);
        final Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            if (bundle.containsKey("text")) {
                this.searchText = bundle.getString("text");
            }
            if (bundle.containsKey("type")) {
                this.type = bundle.getString("type");
            }
            if (bundle.containsKey("id")) {
                this.userId = bundle.getString("id");
            }
        }
        this.searchEdit.setText(this.searchText);
        try {
            final SearchVideoAsync task = new SearchVideoAsync(this,
                    this.getJSONRequest(1, this.searchText).toString(), this.type, true, this.searchTextView);
            task.delegate = this;
            task.execute();
        } catch (final JSONException exception) {
            LOG.e(exception);
        }
        this.menu.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                final int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60,
                        SearchVideosActivity.this.getResources().getDisplayMetrics());
                SlideoutActivity.prepare(SearchVideosActivity.this, R.id.moreVideosView, width);
                SearchVideosActivity.this.startActivity(new Intent(SearchVideosActivity.this, MenuActivity.class));
                SearchVideosActivity.this.overridePendingTransition(0, 0);
            }
        });

        this.searchEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(final TextView ignored, final int actionId, final KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    this.performSearch();
                    return true;
                }
                return false;
            }

            private void performSearch() {

                final String text = SearchVideosActivity.this.searchEdit.getText().toString();
                final InputMethodManager mgr = (InputMethodManager) SearchVideosActivity.this
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(SearchVideosActivity.this.searchEdit.getWindowToken(), 0);
                if ((text != null) && (text.trim().length() > 0)) {
                    SearchVideosActivity.this.searchText = text;
                    SearchVideosActivity.this.myPageDtos.clear();
                    if (SearchVideosActivity.this.adapter != null) {
                        SearchVideosActivity.this.adapter.notifyDataSetChanged();
                    }
                    try {
                        final SearchVideoAsync task = new SearchVideoAsync(SearchVideosActivity.this,
                                SearchVideosActivity.this.getJSONRequest(1, text).toString(),
                                SearchVideosActivity.this.type, true, SearchVideosActivity.this.searchTextView);
                        task.delegate = SearchVideosActivity.this;
                        task.execute();
                    } catch (final JSONException exception) {
                        LOG.e(exception);
                    }
                }
            }
        });

        this.search.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View view) {

                if (SearchVideosActivity.this.searchLayout.isShown()) {
                    SearchVideosActivity.this.searchLayout.setVisibility(View.GONE);
                    SearchVideosActivity.this.search.setBackgroundResource(R.drawable.search1);
                    SearchVideosActivity.this.finish();
                    SearchVideosActivity.this.searchEdit.setText("");
                    // isSearch = false;
                } else {
                    SearchVideosActivity.this.searchLayout.setVisibility(View.VISIBLE);
                    // isSearch = true;
                    SearchVideosActivity.this.search.setBackgroundResource(R.drawable.cancelbutton);
                }
            }
        });
        // Set a listener to be invoked when the list should be refreshed.
        this.moreVideosListView.setOnRefreshListener(new OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(final PullToRefreshBase<ListView> refreshView) {

                SearchVideosActivity.this.pullToRefreshList = true;
                try {
                    final SearchVideoAsync task = new SearchVideoAsync(SearchVideosActivity.this,
                            SearchVideosActivity.this.getJSONRequest(1, SearchVideosActivity.this.searchText)
                                    .toString(), SearchVideosActivity.this.type, false,
                            SearchVideosActivity.this.searchTextView);
                    task.delegate = SearchVideosActivity.this;
                    task.execute();
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }
            }

        });

        // Add an end-of-list listener
        this.moreVideosListView.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

            @Override
            public void onLastItemVisible() {

                if (!SearchVideosActivity.this.flagLoading) {
                    SearchVideosActivity.this.flagLoading = true;
                    final int offset = SearchVideosActivity.this.myPageDtos.size();
                    if ((offset % SearchVideosActivity.PAGE_SIZE) == 0) {
                        final int pageNo = (offset / SearchVideosActivity.PAGE_SIZE) + 1;
                        try {
                            final SearchVideoAsync task = new SearchVideoAsync(SearchVideosActivity.this,
                                    SearchVideosActivity.this.getJSONRequest(pageNo,
                                            SearchVideosActivity.this.searchEdit.getText().toString()).toString(),
                                    SearchVideosActivity.this.type, true, SearchVideosActivity.this.searchTextView);
                            task.delegate = SearchVideosActivity.this;
                            task.execute();
                        } catch (final JSONException exception) {
                            LOG.e(exception);
                        }
                    }
                }
            }
        });

    }

    @Override
    protected void onDestroy() {

        Util.clearImageCache(this.context);
        super.onDestroy();
    }
}
