/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.fragments;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wootag.Constant;
import com.wootag.MenuActivity;
import com.wootag.R;
import com.wootag.adapter.PostsAdapter;
import com.wootag.async.SearchVideoAsync;
import com.wootag.dto.Liked;
import com.wootag.dto.MyPageDto;
import com.wootag.pulltorefresh.PullToRefreshBase;
import com.wootag.pulltorefresh.PullToRefreshBase.OnLastItemVisibleListener;
import com.wootag.pulltorefresh.PullToRefreshBase.OnRefreshListener;
import com.wootag.pulltorefresh.PullToRefreshListView;
import com.wootag.slideout.SlideoutActivity;
import com.wootag.util.Config;
import com.wootag.util.MoreVideos;

public class SearchVideosFragment extends BaseFragment implements MoreVideos {

    private static final String ID = "id";
    private static final String TYPE2 = "type";
    private static final String TEXT = "text";
    private static final String SEARCH2 = "Search";
    private static final String USER = "user";
    private static final String PAGE_NO = "page_no";
    private static final String LOGIN_ID = "login_id";
    private static final String USERID = "userid";
    private static final String NAME = "name";

    public static SearchVideosFragment searchVideosActivity;

    private static final Logger LOG = LoggerManager.getLogger();
    protected PostsAdapter adapter;
    boolean flagLoading;
    private TextView heading;
    boolean pullToRefresh;
    private String rootFragment = "";
    protected Button search;
    protected Button menu;
    protected TextView searchTextView;
    private View view;
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

        JSONObject request = null;
        request = new JSONObject();
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
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        this.view = inflater.inflate(R.layout.my_more_videos, container, false);
        searchVideosActivity = this;
        this.context = this.getActivity();
        this.menu = (Button) this.view.findViewById(R.id.menu);
        this.search = (Button) this.view.findViewById(R.id.settings);
        this.searchEdit = (EditText) this.view.findViewById(R.id.searchEditText);
        this.searchLayout = (RelativeLayout) this.view.findViewById(R.id.searchRL);
        this.moreVideosListView = (PullToRefreshListView) this.view.findViewById(R.id.moreVideosListView);
        this.myPageDtos = new ArrayList<MyPageDto>();
        this.searchLayout.setVisibility(View.VISIBLE);
        this.search.setBackgroundResource(R.drawable.cancelbutton);
        this.heading = (TextView) this.view.findViewById(R.id.heading);
        this.searchTextView = (TextView) this.view.findViewById(R.id.searchView);
        this.heading.setText(SEARCH2);
        final Bundle bundle = this.getArguments();
        if (bundle != null) {
            if (bundle.containsKey(TEXT)) {
                this.searchText = bundle.getString(TEXT);
            }
            if (bundle.containsKey(TYPE2)) {
                this.type = bundle.getString(TYPE2);
            }
            if (bundle.containsKey(ID)) {
                this.userId = bundle.getString(ID);
            }
            if (bundle.containsKey(Constant.ROOT_FRAGMENT)) {
                this.rootFragment = bundle.getString(Constant.ROOT_FRAGMENT);
            }
        }
        this.searchEdit.setText(this.searchText);
        try {
            final SearchVideoAsync task = new SearchVideoAsync(this.getActivity(), this.getJSONRequest(1,
                    this.searchText).toString(), this.type, true, this.searchTextView);
            task.delegate = this;
            task.execute();
        } catch (final JSONException exception) {
            LOG.e(exception);
        }
        this.menu.setVisibility(View.GONE);
        this.menu.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                final int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60,
                        SearchVideosFragment.this.getResources().getDisplayMetrics());
                SlideoutActivity.prepare(SearchVideosFragment.this.getActivity(), R.id.moreVideosView, width);
                SearchVideosFragment.this.startActivity(new Intent(SearchVideosFragment.this.getActivity(),
                        MenuActivity.class));
                SearchVideosFragment.this.getActivity().overridePendingTransition(0, 0);
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

                final String text = SearchVideosFragment.this.searchEdit.getText().toString();
                final InputMethodManager mgr = (InputMethodManager) SearchVideosFragment.this.getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(SearchVideosFragment.this.searchEdit.getWindowToken(), 0);
                if ((text != null) && (text.trim().length() > 0)) {
                    SearchVideosFragment.this.searchText = text;
                    SearchVideosFragment.this.myPageDtos.clear();
                    if (SearchVideosFragment.this.adapter != null) {
                        SearchVideosFragment.this.adapter.notifyDataSetChanged();
                    }
                    try {
                        final SearchVideoAsync task = new SearchVideoAsync(SearchVideosFragment.this.getActivity(),
                                SearchVideosFragment.this.getJSONRequest(1, text).toString(),
                                SearchVideosFragment.this.type, true, SearchVideosFragment.this.searchTextView);
                        task.delegate = SearchVideosFragment.this;
                        task.execute();
                    } catch (final JSONException exception) {
                        LOG.e(exception);
                    }
                }
            }
        });

        this.search.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (SearchVideosFragment.this.searchLayout.isShown()) {
                    SearchVideosFragment.this.searchLayout.setVisibility(View.GONE);
                    SearchVideosFragment.this.search.setBackgroundResource(R.drawable.search1);
                    final InputMethodManager mgr = (InputMethodManager) SearchVideosFragment.this.getActivity()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    mgr.hideSoftInputFromWindow(SearchVideosFragment.this.searchEdit.getWindowToken(), 0);

                    BaseFragment.tabActivity.removeFromBackStack();

                    // getActivity().getSupportFragmentManager().popBackStackImmediate();
                    SearchVideosFragment.this.searchEdit.setText("");
                } else {
                    SearchVideosFragment.this.searchLayout.setVisibility(View.VISIBLE);
                    SearchVideosFragment.this.search.setBackgroundResource(R.drawable.cancelbutton);
                }
            }
        });
        // Set a listener to be invoked when the list should be refreshed.
        this.moreVideosListView.setOnRefreshListener(new OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(final PullToRefreshBase<ListView> refreshView) {

                SearchVideosFragment.this.pullToRefresh = true;
                try {
                    final SearchVideoAsync task = new SearchVideoAsync(SearchVideosFragment.this.getActivity(),
                            SearchVideosFragment.this.getJSONRequest(1, SearchVideosFragment.this.searchText)
                                    .toString(), SearchVideosFragment.this.type, false,
                            SearchVideosFragment.this.searchTextView);
                    task.delegate = SearchVideosFragment.this;
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

                if (!SearchVideosFragment.this.flagLoading) {
                    SearchVideosFragment.this.flagLoading = true;
                    final int offset = SearchVideosFragment.this.myPageDtos.size();
                    if ((offset % SearchVideosFragment.PAGE_SIZE) == 0) {
                        final int pageNo = (offset / SearchVideosFragment.PAGE_SIZE) + 1;
                        try {
                            final SearchVideoAsync task = new SearchVideoAsync(SearchVideosFragment.this.getActivity(),
                                    SearchVideosFragment.this.getJSONRequest(pageNo,
                                            SearchVideosFragment.this.searchEdit.getText().toString()).toString(),
                                    SearchVideosFragment.this.type, true, SearchVideosFragment.this.searchTextView);
                            task.delegate = SearchVideosFragment.this;
                            task.execute();
                        } catch (final JSONException exception) {
                            LOG.e(exception);
                        }
                    }
                }
            }
        });

        return this.view;
    }

    @Override
    public void videoList(final List<MyPageDto> video) {

        this.flagLoading = false;
        if (this.pullToRefresh) {
            this.moreVideosListView.onRefreshComplete();
            if ((video != null) && (video.size() > 0)) {
                this.myPageDtos.clear();
                if ((video != null) && (video.size() > 0)) {
                    this.myPageDtos.addAll(video);
                }
            }

            if (this.adapter == null) {
                this.adapter = new PostsAdapter(this.context, 0, this.myPageDtos, this.rootFragment,
                        SearchVideosFragment.this);
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
                this.adapter = new PostsAdapter(this.context, 0, this.myPageDtos, this.rootFragment,
                        SearchVideosFragment.this);
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

        this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (this.pullToRefresh) {
            this.moreVideosListView.onRefreshComplete();
        }
    }

    @Override
    public void videoList(final List<MyPageDto> video, final String type) {

    }

    @Override
    public void videoList(final List<MyPageDto> video, final String type, final boolean next) {

    }
}
