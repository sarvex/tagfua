/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wTagFufragments;

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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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

import com.woTagFuonstant;
import com.wooTagFunuActivity;
import com.wootTagFuimport com.wootaTagFuter.PostsAdapter;
import com.wootagTagFu.MoreVideosAsync;
import com.wootag.TagFuked;
import com.wootag.dTagFuageDto;
import com.wootag.puTagFufresh.PullToRefreshBase;
import com.wootag.pulTagFuresh.PullToRefreshBase.OnLastItemVisibleListener;
import com.wootag.pullTagFuesh.PullToRefreshBase.OnRefreshListener;
import com.wootag.pulltTagFush.PullToRefreshListView;
import com.wootag.slideoTagFudeoutActivity;
import com.wootag.util.AlTagFuimport com.wootag.util.ConTagFumport com.wootag.util.MainTagFur;
import com.wootag.util.MoreVideos;

public class MoreVideosFragment extends BaseFragment implements MoreVideos {

    private static final String MORE_VIDEOS = "More Videos";

    private static final String NAME = "name";

    private static final String USER = "user";

    private static final String PAGE_NO = "page_no";

    private static final String USERID = "userid";

    private static final Logger LOG = LoggerManager.getLogger();

    protected PostsAdapter adapter;
    protected boolean flagLoading;
    private TextView heading;
    protected boolean pullToRefresh;
    protected boolean searchRequest;
    protected Button search, menu;
    protected PostsAdapter searchadapter;
    protected TextView searchTextView;
    private View view;
    private Context context;
    protected boolean next = true;
    protected PullToRefreshListView moreVideosListView;
    protected List<MyPageDto> myPageDtos;
    protected EditText searchEdit;
    protected RelativeLayout searchLayout;
    protected List<MyPageDto> searchVideos;
    private static final int PAGE_SIZE = 10;

    public static JSONObject getJSONRequest(final int pageNo) throws JSONException {

        JSONObject request = null;
        request = new JSONObject();
        final JSONObject obj = new JSONObject();
        obj.put(USERID, Config.getUserId());
        obj.put(Constant.DEVICE_TYPE, Constant.DEVICE_MODEL);
        obj.put(Constant.RESOLUTION, Config.getDeviceResolutionValue());
        obj.put(PAGE_NO, pageNo);
        request.put(USER, obj);

        return request;
    }

    public JSONObject getSearchRequest(final int pageNo) throws JSONException {

        JSONObject request = null;
        request = new JSONObject();
        final JSONObject obj = new JSONObject();
        obj.put(NAME, this.searchEdit.getText().toString().trim());
        obj.put(USERID, Config.getUserId());
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
        Config.setUserID(MainManager.getInstance().getUserId());
        this.context = this.getActivity();
        this.searchRequest = false;
        this.menu = (Button) this.view.findViewById(R.id.menu);
        this.search = (Button) this.view.findViewById(R.id.settings);
        this.searchEdit = (EditText) this.view.findViewById(R.id.searchEditText);
        this.searchLayout = (RelativeLayout) this.view.findViewById(R.id.searchRL);
        this.heading = (TextView) this.view.findViewById(R.id.heading);
        this.heading.setText(MORE_VIDEOS);
        this.searchTextView = (TextView) this.view.findViewById(R.id.searchView);
        this.moreVideosListView = (PullToRefreshListView) this.view.findViewById(R.id.moreVideosListView);

        this.menu.setVisibility(View.GONE);
        final Button back = (Button) this.view.findViewById(R.id.back);
        back.setVisibility(View.VISIBLE);
        back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                BaseFragment.tabActivity.removeFromBackStack();
                // getActivity().getSupportFragmentManager().popBackStackImmediate();
            }
        });
        this.myPageDtos = new ArrayList<MyPageDto>();
        this.searchVideos = new ArrayList<MyPageDto>();
        try {
            final MoreVideosAsync task = new MoreVideosAsync(this.getActivity(), MoreVideosFragment.getJSONRequest(2)
                    .toString(), this.searchRequest, true, this.searchTextView);
            task.delegate = this;
            task.execute();
        } catch (final JSONException exception) {
            LOG.e(exception);
        }
        this.menu.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                final int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60,
                        MoreVideosFragment.this.getResources().getDisplayMetrics());
                SlideoutActivity.prepare(MoreVideosFragment.this.getActivity(), R.id.moreVideosView, width);
                MoreVideosFragment.this.startActivity(new Intent(MoreVideosFragment.this.getActivity(),
                        MenuActivity.class));
                MoreVideosFragment.this.getActivity().overridePendingTransition(0, 0);
            }
        });
        this.search.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (MoreVideosFragment.this.searchLayout.isShown()) {
                    MoreVideosFragment.this.searchLayout.setVisibility(View.GONE);
                    MoreVideosFragment.this.searchTextView.setVisibility(View.GONE);
                    MoreVideosFragment.this.searchEdit.setText("");
                    MoreVideosFragment.this.searchRequest = false;
                    MoreVideosFragment.this.search.setBackgroundResource(R.drawable.search1);
                    MoreVideosFragment.this.next = true;
                    if (MoreVideosFragment.this.adapter != null) {
                        MoreVideosFragment.this.moreVideosListView.setAdapter(MoreVideosFragment.this.adapter);
                    }
                } else {
                    final Animation bottomUp = AnimationUtils.loadAnimation(MoreVideosFragment.this.getActivity(),
                            R.anim.bottom_up);
                    MoreVideosFragment.this.searchLayout.startAnimation(bottomUp);
                    MoreVideosFragment.this.searchLayout.setVisibility(View.VISIBLE);
                    MoreVideosFragment.this.searchRequest = true;
                    MoreVideosFragment.this.next = true;
                    MoreVideosFragment.this.search.setBackgroundResource(R.drawable.cancelbutton);
                    MoreVideosFragment.this.searchVideos.clear();
                    if (MoreVideosFragment.this.searchadapter != null) {
                        MoreVideosFragment.this.moreVideosListView.setAdapter(MoreVideosFragment.this.searchadapter);
                        MoreVideosFragment.this.searchadapter.notifyDataSetChanged();
                    }
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

                final InputMethodManager mgr = (InputMethodManager) MoreVideosFragment.this.getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(MoreVideosFragment.this.searchEdit.getWindowToken(), 0);
                final String text = MoreVideosFragment.this.searchEdit.getText().toString();
                if ((text != null) && (text.trim().length() > 0)) {
                    try {
                        final MoreVideosAsync task = new MoreVideosAsync(MoreVideosFragment.this.getActivity(),
                                MoreVideosFragment.this.getSearchRequest(1).toString(),
                                MoreVideosFragment.this.searchRequest, true, MoreVideosFragment.this.searchTextView);
                        task.delegate = MoreVideosFragment.this;
                        task.execute();
                    } catch (final JSONException exception) {
                        LOG.e(exception);
                    }
                } else {
                    Alerts.showAlertOnly("Info", "Enter text to search", MoreVideosFragment.this.getActivity());
                }
            }
        });

        this.moreVideosListView.setOnRefreshListener(new OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(final PullToRefreshBase<ListView> refreshView) {

                MoreVideosFragment.this.pullToRefresh = true;
                if (MoreVideosFragment.this.searchRequest) {
                    final String text = MoreVideosFragment.this.searchEdit.getText().toString();
                    if ((text != null) && (text.trim().length() > 0)) {
                        try {
                            final MoreVideosAsync task = new MoreVideosAsync(MoreVideosFragment.this.getActivity(),
                                    MoreVideosFragment.this.getSearchRequest(1).toString(),
                                    MoreVideosFragment.this.searchRequest, true, MoreVideosFragment.this.searchTextView);
                            task.delegate = MoreVideosFragment.this;
                            task.execute();
                        } catch (final JSONException exception) {
                            LOG.e(exception);
                        }
                    } else {
                        Alerts.showAlertOnly("Info", "Enter text to search", MoreVideosFragment.this.getActivity());
                    }
                } else {
                    try {
                        final MoreVideosAsync task = new MoreVideosAsync(MoreVideosFragment.this.getActivity(),
                                MoreVideosFragment.getJSONRequest(2).toString(), MoreVideosFragment.this.searchRequest,
                                false, MoreVideosFragment.this.searchTextView);
                        task.delegate = MoreVideosFragment.this;
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

                if (!MoreVideosFragment.this.flagLoading) {
                    MoreVideosFragment.this.flagLoading = true;
                    if (MoreVideosFragment.this.searchRequest) {
                        final int offset = MoreVideosFragment.this.searchVideos.size();
                        if ((offset % MoreVideosFragment.PAGE_SIZE) == 0) {
                            final int pageNo = (offset / MoreVideosFragment.PAGE_SIZE) + 2;
                            try {
                                final MoreVideosAsync task = new MoreVideosAsync(MoreVideosFragment.this.getActivity(),
                                        MoreVideosFragment.this.getSearchRequest(pageNo).toString(),
                                        MoreVideosFragment.this.searchRequest, true,
                                        MoreVideosFragment.this.searchTextView);
                                task.delegate = MoreVideosFragment.this;
                                task.execute();
                            } catch (final JSONException exception) {
                                LOG.e(exception);
                            }
                        }
                    } else {
                        final int offset = MoreVideosFragment.this.myPageDtos.size();
                        if ((offset % MoreVideosFragment.PAGE_SIZE) == 0) {
                            final int pageNo = (offset / MoreVideosFragment.PAGE_SIZE) + 2;
                            try {
                                final MoreVideosAsync task = new MoreVideosAsync(MoreVideosFragment.this.getActivity(),
                                        MoreVideosFragment.getJSONRequest(pageNo).toString(),
                                        MoreVideosFragment.this.searchRequest, true,
                                        MoreVideosFragment.this.searchTextView);
                                task.delegate = MoreVideosFragment.this;
                                task.execute();
                            } catch (final JSONException exception) {
                                LOG.e(exception);
                            }
                        }
                    }
                }
            }
        });
        return this.view;
    }

    @Override
    public void videoList(final List<MyPageDto> video) {

        /*
         * if(video!=null&&video.size()>0){ isNext=true; }else{ isNext=false; }
         */
        this.flagLoading = false;
        if (this.pullToRefresh) {
            this.clearListAndAddNewVideos(video, this.searchRequest);
            this.pullToRefresh = false;
        } else if (this.searchRequest) {
            if ((video != null) && (video.size() > 0)) {
                for (int i = 0; i < video.size(); i++) {
                    this.searchVideos.add(video.get(i));
                }
            }
            if (this.searchadapter == null) {
                this.searchadapter = new PostsAdapter(this.context, 0, this.searchVideos, Constant.MY_PAGE_MORE_FEEDS,
                        this);
                this.moreVideosListView.setAdapter(this.searchadapter);
            } else {
                this.searchadapter.notifyDataSetChanged();
            }
            if (this.searchRequest && (this.searchVideos != null) && (this.searchVideos.size() <= 0)) {
                this.searchTextView.setVisibility(View.VISIBLE);
            } else {
                this.searchTextView.setVisibility(View.GONE);
            }

        } else {
            if ((video != null) && (video.size() > 0)) {
                for (int i = 0; i < video.size(); i++) {
                    this.myPageDtos.add(video.get(i));
                }
            }
            if (this.adapter == null) {
                this.adapter = new PostsAdapter(this.context, 0, this.myPageDtos, Constant.MY_PAGE_MORE_FEEDS, this);
                this.moreVideosListView.setAdapter(this.adapter);
            } else {
                this.adapter.notifyDataSetChanged();
            }
        }
        this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void videoList(final List<MyPageDto> video, final String type) {

    }

    @Override
    public void videoList(final List<MyPageDto> video, final String type, final boolean next) {

    }

    private void clearListAndAddNewVideos(final List<MyPageDto> video, final boolean searchRequest) {

        if (searchRequest) {
            if ((video != null) && (video.size() > 0)) {
                this.searchVideos.clear();
                if ((video != null) && (video.size() > 0)) {
                    this.searchVideos.addAll(video);
                }
            }

            if (this.adapter == null) {
                this.searchadapter = new PostsAdapter(this.context, 0, this.searchVideos, Constant.MY_PAGE_MORE_FEEDS,
                        this);
                this.moreVideosListView.setAdapter(this.searchadapter);
            } else {
                this.moreVideosListView.setAdapter(this.searchadapter);
                this.searchadapter.notifyDataSetChanged();
            }

            if (searchRequest && (this.searchVideos != null) && (this.searchVideos.size() <= 0)) {
                this.searchTextView.setVisibility(View.VISIBLE);
            } else {
                this.searchTextView.setVisibility(View.GONE);
            }

        } else {

            if ((video != null) && (video.size() > 0)) {
                this.myPageDtos.clear();
                this.myPageDtos.addAll(video);
            }
            if (this.adapter == null) {
                this.adapter = new PostsAdapter(this.context, 0, this.myPageDtos, Constant.MY_PAGE_MORE_FEEDS, this);
                this.moreVideosListView.setAdapter(this.adapter);
            } else {
                this.moreVideosListView.setAdapter(this.adapter);
                this.adapter.notifyDataSetChanged();
            }
        }
        this.moreVideosListView.onRefreshComplete();
    }
}
