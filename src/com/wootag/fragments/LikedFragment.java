/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wTagFufragments;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.woTagFuonstant;
import com.wooTagFunuActivity;
import com.wootTagFuimport com.wootaTagFuter.LikedAdapter;
import com.wootagTagFu.LikedAsync;
import com.wootag.TagFuked;
import com.wootag.dTagFuageDto;
import com.wootag.puTagFufresh.PullToRefreshBase;
import com.wootag.pulTagFuresh.PullToRefreshBase.OnLastItemVisibleListener;
import com.wootag.pullTagFuesh.PullToRefreshBase.OnRefreshListener;
import com.wootag.pulltTagFush.PullToRefreshListView;
import com.wootag.slideoTagFudeoutActivity;
import com.wootag.util.MoreVideos;

public class LikedFragment extends BaseFragment implements MoreVideos {

    public static LikedFragment likedActivity;

    private static final Logger LOG = LoggerManager.getLogger();
    private int currentPageNumber;
    boolean flagLoading;
    private TextView heading;
    boolean pullToRefresh;
    boolean searchRequest;
    private LikedAdapter likAdapter;
    private List<Liked> likedList;
    private PullToRefreshListView list;
    private static final int PAGE_SIZE = 10;
    private String rootFragment = "";
    private Button search, menu;
    protected RelativeLayout searchLayout;
    protected String videoId;
    private View view;

    @Override
    public void likedList(final List<Liked> likedPeople) {

        this.flagLoading = false;
        if (this.pullToRefresh) {
            this.list.onRefreshComplete();
            this.likedList.clear();
            if ((likedPeople != null) && (likedPeople.size() > 0)) {
                this.likedList.addAll(likedPeople);
                this.likAdapter = new LikedAdapter(this.getActivity(), 0, this.likedList, LikedFragment.this,
                        this.rootFragment);
                this.list.setAdapter(this.likAdapter);
            }
            if (this.likAdapter != null) {
                this.likAdapter.notifyDataSetChanged();
            }
        } else {

            if ((likedPeople != null) && (likedPeople.size() > 0)) {
                if (this.likAdapter == null) {
                    for (int i = 0; i < likedPeople.size(); i++) {
                        this.likedList.add(likedPeople.get(i));
                    }
                    this.likAdapter = new LikedAdapter(this.getActivity(), 0, this.likedList, LikedFragment.this,
                            this.rootFragment);
                    this.list.setAdapter(this.likAdapter);
                } else {
                    for (int i = 0; i < likedPeople.size(); i++) {
                        this.likedList.add(likedPeople.get(i));
                    }
                    this.likAdapter.notifyDataSetChanged();
                }
            }
        }

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        this.view = inflater.inflate(R.layout.loved, container, false);
        this.menu = (Button) this.view.findViewById(R.id.menu);
        this.search = (Button) this.view.findViewById(R.id.settings);
        this.searchLayout = (RelativeLayout) this.view.findViewById(R.id.searchRL);
        this.list = (PullToRefreshListView) this.view.findViewById(R.id.lovedListView);
        this.likedList = new ArrayList<Liked>();
        // Bundle b = getIntent().getExtras();

        final Bundle bundle = this.getArguments();

        this.search.setVisibility(View.GONE);
        if (bundle != null) {
            if (bundle.containsKey("videoid")) {
                this.videoId = bundle.getString("videoid");
            }
            if (bundle.containsKey(Constant.ROOT_FRAGMENT)) {
                this.rootFragment = bundle.getString(Constant.ROOT_FRAGMENT);
            }

            if (bundle.containsKey("count")) {
                if (bundle.getString("count") != null) {
                    final int noOfCounts = Integer.parseInt(bundle.getString("count"));
                    if (noOfCounts == 1) {
                        ((TextView) this.view.findViewById(R.id.lovedTextView)).setText(noOfCounts + " Like");
                    } else {
                        ((TextView) this.view.findViewById(R.id.lovedTextView)).setText(noOfCounts + " Likes");
                    }
                } else {
                    ((TextView) this.view.findViewById(R.id.lovedTextView)).setText("0 Likes");
                }
            }

        }
        this.heading = (TextView) this.view.findViewById(R.id.heading);
        this.heading.setText("Likes");
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

        final LikedAsync task = new LikedAsync(this.getActivity(), 1, this.videoId, true);
        task.delegate = this;
        task.execute();

        this.list.setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount,
                    final int totalItemCount) {

                if (((firstVisibleItem + visibleItemCount) == totalItemCount) && (totalItemCount != 0)
                        && !LikedFragment.this.flagLoading) {
                    LikedFragment.this.flagLoading = true;
                    LikedFragment.this.getMore();
                }
            }

            @Override
            public void onScrollStateChanged(final AbsListView view, final int scrollState) {

                LikedFragment.this.flagLoading = false;

            }
        });
        this.list.setOnRefreshListener(new OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(final PullToRefreshBase<ListView> refreshView) {

                LikedFragment.this.pullToRefresh = true;
                final LikedAsync task = new LikedAsync(LikedFragment.this.getActivity(), 1, LikedFragment.this.videoId,
                        false);
                task.delegate = LikedFragment.this;
                task.execute();

            }

        });
        this.list.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

            @Override
            public void onLastItemVisible() {

                if (!LikedFragment.this.flagLoading) {
                    LikedFragment.this.getMore();
                }
            }
        });

        this.menu.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                final int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, LikedFragment.this
                        .getResources().getDisplayMetrics());
                SlideoutActivity.prepare(LikedFragment.this.getActivity(), R.id.lovedView, width);
                LikedFragment.this.startActivity(new Intent(LikedFragment.this.getActivity(), MenuActivity.class));
                LikedFragment.this.getActivity().overridePendingTransition(0, 0);
            }
        });
        this.search.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (LikedFragment.this.searchLayout.isShown()) {
                    LikedFragment.this.searchLayout.setVisibility(View.GONE);
                    LikedFragment.this.searchRequest = false;
                } else {
                    LikedFragment.this.searchLayout.setVisibility(View.VISIBLE);
                    LikedFragment.this.searchRequest = true;
                }
            }
        });
        return this.view;
    }

    @Override
    public void videoList(final List<MyPageDto> video) {

    }

    @Override
    public void videoList(final List<MyPageDto> video, final String type) {

    }

    @Override
    public void videoList(final List<MyPageDto> video, final String type, final boolean next) {

    }

    void getMore() {

        final int offset = this.likedList.size();
        if ((offset % LikedFragment.PAGE_SIZE) == 0) {
            this.flagLoading = true;
            final int pageNo = (offset / LikedFragment.PAGE_SIZE) + 1;
            final LikedAsync task = new LikedAsync(this.getActivity(), pageNo, this.videoId, true);
            task.delegate = this;
            task.execute();
        }
    }
}
