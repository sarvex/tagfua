/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.wTagFu

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.woTagFudapter.LikedAdapter;
import com.wooTagFuync.LikedAsync;
import com.wootTagFu.Liked;
import com.wootaTagFuMyPageDto;
import com.wootagTagFuorefresh.PullToRefreshBase;
import com.wootag.TagFurefresh.PullToRefreshBase.OnLastItemVisibleListener;
import com.wootag.pTagFuefresh.PullToRefreshBase.OnRefreshListener;
import com.wootag.puTagFufresh.PullToRefreshListView;
import com.wootag.sliTagFuSlideoutActivity;
import com.wootag.utilTagFuideos;
import com.wootag.util.Util;

public class LikedActivity extends Activity implements MoreVideos {

    public static LikedActivity likedActivity;

    private static final Logger LOG = LoggerManager.getLogger();
    // private ImageButton send;
    boolean flagLoading;
    private TextView heading;
    boolean pullToRefresh;
    boolean searchRequest;
    private Button search, menu;
    protected RelativeLayout searchLayout;
    private int currentPageNumber;
    private LikedAdapter likAdapter;
    private List<Liked> likedList;
    private PullToRefreshListView list;
    private static final int PAGE_SIZE = 10;
    protected String videoId;

    @Override
    public void likedList(final List<Liked> likedPeople) {

        this.flagLoading = false;
        if (this.pullToRefresh) {
            this.list.onRefreshComplete();
            this.likedList.clear();
            if ((likedPeople != null) && (likedPeople.size() > 0)) {
                this.likedList.addAll(likedPeople);
                this.likAdapter = new LikedAdapter(this, 0, this.likedList, null, "");
                this.list.setAdapter(this.likAdapter);
            }
            this.likAdapter.notifyDataSetChanged();
        } else {

            if ((likedPeople != null) && (likedPeople.size() > 0)) {
                if (this.likAdapter == null) {
                    for (int i = 0; i < likedPeople.size(); i++) {
                        this.likedList.add(likedPeople.get(i));
                    }
                    this.likAdapter = new LikedAdapter(this, 0, this.likedList, null, "");
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
    public void videoList(final List<MyPageDto> video) {

    }

    @Override
    public void videoList(final List<MyPageDto> video, final String type) {

    }

    @Override
    public void videoList(final List<MyPageDto> video, final String type, final boolean isNextRequest) {

    }

    @Override
    protected void onCreate(final Bundle bundle) {

        super.onCreate(bundle);
        this.setContentView(R.layout.loved);
        likedActivity = this;
        this.menu = (Button) this.findViewById(R.id.menu);
        this.search = (Button) this.findViewById(R.id.settings);
        // send = (ImageButton) findViewById(R.id.seatchImageButton);
        this.searchLayout = (RelativeLayout) this.findViewById(R.id.searchRL);
        this.list = (PullToRefreshListView) this.findViewById(R.id.lovedListView);
        this.likedList = new ArrayList<Liked>();
        final Bundle b = this.getIntent().getExtras();
        this.search.setVisibility(View.GONE);
        if (b != null) {
            if (b.containsKey("videoid")) {
                this.videoId = b.getString("videoid");
            }
            if (b.containsKey("count")) {
                if (b.getString("count") != null) {
                    final int noOfCounts = Integer.parseInt(b.getString("count"));
                    if (noOfCounts == 1) {
                        ((TextView) this.findViewById(R.id.lovedTextView)).setText(noOfCounts + " Like");
                    } else {
                        ((TextView) this.findViewById(R.id.lovedTextView)).setText(noOfCounts + " Likes");
                    }
                } else {
                    ((TextView) this.findViewById(R.id.lovedTextView)).setText("0 Likes");
                }
            }

        }
        this.heading = (TextView) this.findViewById(R.id.heading);
        this.heading.setText("Likes");
        this.menu.setVisibility(View.GONE);
        final Button back = (Button) this.findViewById(R.id.back);
        back.setVisibility(View.VISIBLE);
        back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                LikedActivity.this.finish();
            }
        });

        final LikedAsync task = new LikedAsync(this, 1, this.videoId, true);
        task.delegate = this;
        task.execute();

        this.list.setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount,
                    final int totalItemCount) {

                if (((firstVisibleItem + visibleItemCount) == totalItemCount) && (totalItemCount != 0)
                        && !LikedActivity.this.flagLoading) {
                    LikedActivity.this.flagLoading = true;
                    LikedActivity.this.getMore();
                }
            }

            @Override
            public void onScrollStateChanged(final AbsListView view, final int scrollState) {

                LikedActivity.this.flagLoading = false;

            }
        });
        this.list.setOnRefreshListener(new OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(final PullToRefreshBase<ListView> refreshView) {

                // try {
                LikedActivity.this.pullToRefresh = true;
                final LikedAsync task = new LikedAsync(LikedActivity.this, 1, LikedActivity.this.videoId, false);
                task.delegate = LikedActivity.this;
                task.execute();
                // } catch (final Exception e) {
                // e.printStackTrace();
                // }

            }

        });
        this.list.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

            @Override
            public void onLastItemVisible() {

                if (!LikedActivity.this.flagLoading) {
                    LikedActivity.this.getMore();
                }
            }
        });

        this.menu.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                final int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, LikedActivity.this
                        .getResources().getDisplayMetrics());
                SlideoutActivity.prepare(LikedActivity.this, R.id.lovedView, width);
                LikedActivity.this.startActivity(new Intent(LikedActivity.this, MenuActivity.class));
                LikedActivity.this.overridePendingTransition(0, 0);
            }
        });
        this.search.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (LikedActivity.this.searchLayout.isShown()) {
                    LikedActivity.this.searchLayout.setVisibility(View.GONE);
                    LikedActivity.this.searchRequest = false;
                } else {
                    LikedActivity.this.searchLayout.setVisibility(View.VISIBLE);
                    LikedActivity.this.searchRequest = true;
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

        final int offset = this.likedList.size();
        if ((offset % LikedActivity.PAGE_SIZE) == 0) {
            this.flagLoading = true;
            final int pageNo = (offset / LikedActivity.PAGE_SIZE) + 1;
            final LikedAsync task = new LikedAsync(this, pageNo, this.videoId, true);
            task.delegate = this;
            task.execute();
        }
    }
}
