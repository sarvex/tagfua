/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wTagFufragments;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Strings;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.woTagFuonstant;
import com.wooTagFu
import com.wootTagFupter.PostsAdapter;
import com.wootaTagFuc.SearchVideoAsync;
import com.wootagTagFuiked;
import com.wootag.TagFuPageDto;
import com.wootag.pTagFuefresh.PullToRefreshBase;
import com.wootag.puTagFufresh.PullToRefreshBase.OnLastItemVisibleListener;
import com.wootag.pulTagFuresh.PullToRefreshBase.OnRefreshListener;
import com.wootag.pullTagFuesh.PullToRefreshListView;
import com.wootag.util.TagFu;
import com.wootag.util.MoreVideos;

public class TrendVideosFragment extends BaseFragment implements MoreVideos {

    private static final String TRENDS = "Trends";
    private static final String TREND_NAME = "trendname";
    public static TrendVideosFragment searchVideosActivity;
    public static TrendVideosFragment trendVideosActivity;

    private static final Logger LOG = LoggerManager.getLogger();

    private static final int PAGE_SIZE = 10;

    protected Button search;
    private Button menu;
    private Context context;
    protected EditText searchEdit;
    protected List<MyPageDto> myPageDtos;
    private PostsAdapter adapter;
    private PullToRefreshListView moreVideosListView;
    protected RelativeLayout searchLayout;
    protected String trendname;
    private String userId;
    protected String userid;
    private TextView heading;
    private View view;
    boolean flagLoading;
    boolean pullToRefresh;

    public static JSONObject getJSONRequest(final int pageNo, final String text) throws JSONException {

        JSONObject request = null;
        request = new JSONObject();
        final JSONObject obj = new JSONObject();
        obj.put(Constant.NAME, text);
        obj.put(Constant.USERID, Config.getUserId());
        obj.put(Constant.DEVICE_TYPE, Constant.DEVICE_MODEL);
        obj.put(Constant.RESOLUTION, Config.getDeviceResolutionValue());
        obj.put(Constant.BROWSE_BY, Constant.TRENDS);
        obj.put(Constant.PAGE_NO, pageNo);
        request.put(Constant.USER, obj);

        return request;
    }

    @Override
    public void likedList(final List<Liked> likedPeople) {

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        this.view = inflater.inflate(R.layout.my_more_videos, container, false);
        this.context = this.getActivity();
        trendVideosActivity = this;
        this.menu = (Button) this.view.findViewById(R.id.menu);
        this.search = (Button) this.view.findViewById(R.id.settings);
        this.searchEdit = (EditText) this.view.findViewById(R.id.searchEditText);
        this.searchLayout = (RelativeLayout) this.view.findViewById(R.id.searchRL);
        this.moreVideosListView = (PullToRefreshListView) this.view.findViewById(R.id.moreVideosListView);
        this.myPageDtos = new ArrayList<MyPageDto>();
        this.heading = (TextView) this.view.findViewById(R.id.heading);

        this.searchLayout.setVisibility(View.GONE);
        this.search.setVisibility(View.GONE);
        this.menu.setVisibility(View.GONE);
        final Button back = (Button) this.view.findViewById(R.id.back);
        back.setVisibility(View.VISIBLE);

        final Bundle bundle = this.getArguments();
        if ((bundle != null) && bundle.containsKey(TREND_NAME)) {
            this.trendname = bundle.getString(TREND_NAME);
        }
        try {
            final SearchVideoAsync task = new SearchVideoAsync(this.getActivity(), TrendVideosFragment.getJSONRequest(
                    1, this.trendname).toString(), Constant.TRENDS, true, null);
            task.delegate = this;
            task.execute();
        } catch (final JSONException exception) {
            LOG.e(exception);
        }
        if (!Strings.isNullOrEmpty(this.trendname)) {
            this.heading.setText(this.trendname);
        } else {
            this.heading.setText(TRENDS);
        }
        back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                BaseFragment.tabActivity.removeFromBackStack();
            }
        });

        this.search.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (TrendVideosFragment.this.searchLayout.isShown()) {
                    TrendVideosFragment.this.searchLayout.setVisibility(View.GONE);
                    TrendVideosFragment.this.search.setBackgroundResource(R.drawable.search1);

                } else {
                    TrendVideosFragment.this.searchLayout.setVisibility(View.VISIBLE);
                    TrendVideosFragment.this.search.setBackgroundResource(R.drawable.cancelbutton);
                }
            }
        });
        this.moreVideosListView.setOnRefreshListener(new OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(final PullToRefreshBase<ListView> refreshView) {

                TrendVideosFragment.this.pullToRefresh = true;
                try {
                    final SearchVideoAsync task = new SearchVideoAsync(TrendVideosFragment.this.getActivity(),
                            TrendVideosFragment.getJSONRequest(1, TrendVideosFragment.this.userid).toString(),
                            TrendVideosFragment.this.trendname, false, null);
                    task.delegate = TrendVideosFragment.this;
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

                if (!TrendVideosFragment.this.flagLoading) {
                    TrendVideosFragment.this.flagLoading = true;
                    final int offset = TrendVideosFragment.this.myPageDtos.size();
                    if ((offset % TrendVideosFragment.PAGE_SIZE) == 0) {
                        final int pageNo = (offset / TrendVideosFragment.PAGE_SIZE) + 1;
                        try {
                            final SearchVideoAsync task = new SearchVideoAsync(TrendVideosFragment.this.getActivity(),
                                    TrendVideosFragment.getJSONRequest(pageNo,
                                            TrendVideosFragment.this.searchEdit.getText().toString()).toString(),
                                    TrendVideosFragment.this.trendname, true, null);
                            task.delegate = TrendVideosFragment.this;
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
