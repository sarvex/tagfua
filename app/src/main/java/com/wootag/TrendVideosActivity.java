package com.TagFu;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
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

public class TrendVideosActivity extends Activity implements MoreVideos {

    private static final String USER = "user";

    private static final String PAGE_NO = "page_no";

    private static final String BROWSE_BY = "browse_by";

    private static final String USERID2 = "userid";

    private static final String NAME = "name";

    private static final String TRENDS2 = "Trends";

    private static final String EMPTY = "";

    private static final String TRENDNAME = "trendname";

    public static TrendVideosActivity searchVideosActivity;

    public static TrendVideosActivity trendVideosActivity;
    private static final Logger LOG = LoggerManager.getLogger();
    private PostsAdapter adapter;
    boolean flagLoading;
    private TextView heading;
    boolean pullToRefresh;
    protected Button search;
    protected Button menu;
    private static final String TRENDS = "trends";
    private Context context;
    private PullToRefreshListView moreVideosListView;
    protected List<MyPageDto> myPageDtos;
    protected EditText searchEdit;
    protected RelativeLayout searchLayout;
    protected String trendname = EMPTY;
    protected String userid;
    private String userId;
    private static final int PAGE_SIZE = 10;

    public static JSONObject getJSONRequest(final int pageNo, final String text) throws JSONException {

        JSONObject request = null;
        request = new JSONObject();
        final JSONObject obj = new JSONObject();
        obj.put(NAME, text);
        obj.put(USERID2, Config.getUserId());
        obj.put(Constant.DEVICE_TYPE, Constant.DEVICE_MODEL);
        obj.put(Constant.RESOLUTION, Config.getDeviceResolutionValue());
        obj.put(BROWSE_BY, TrendVideosActivity.TRENDS);
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
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (this.pullToRefresh) {
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

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.my_more_videos);
        searchVideosActivity = this;
        this.context = this;
        trendVideosActivity = this;
        this.menu = (Button) this.findViewById(R.id.menu);
        this.search = (Button) this.findViewById(R.id.settings);
        this.searchEdit = (EditText) this.findViewById(R.id.searchEditText);
        this.searchLayout = (RelativeLayout) this.findViewById(R.id.searchRL);
        this.moreVideosListView = (PullToRefreshListView) this.findViewById(R.id.moreVideosListView);
        this.myPageDtos = new ArrayList<MyPageDto>();
        this.heading = (TextView) this.findViewById(R.id.heading);

        final LinearLayout bodyLayout = (LinearLayout) this.findViewById(R.id.bodyLayout);

        this.searchLayout.setVisibility(View.GONE);
        this.search.setVisibility(View.GONE);
        this.menu.setVisibility(View.GONE);
        final Button back = (Button) this.findViewById(R.id.back);
        back.setVisibility(View.VISIBLE);

        final Bundle bundle = this.getIntent().getExtras();
        if ((bundle != null) && bundle.containsKey(TRENDNAME)) {
            this.trendname = bundle.getString(TRENDNAME);
        }
        try {
            final SearchVideoAsync task = new SearchVideoAsync(this, TrendVideosActivity.getJSONRequest(1,
                    this.trendname).toString(), TrendVideosActivity.TRENDS, true, null);
            task.delegate = this;
            task.execute();
        } catch (final JSONException exception) {
            LOG.e(exception);
        }
        if (!this.trendname.equalsIgnoreCase(EMPTY)) {
            this.heading.setText(this.trendname);
        } else {
            this.heading.setText(TRENDS2);
        }
        back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                TrendVideosActivity.this.finish();
            }
        });

        this.menu.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                final int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60,
                        TrendVideosActivity.this.getResources().getDisplayMetrics());
                SlideoutActivity.prepare(TrendVideosActivity.this, R.id.moreVideosView, width);
                TrendVideosActivity.this.startActivity(new Intent(TrendVideosActivity.this, MenuActivity.class));
                TrendVideosActivity.this.overridePendingTransition(0, 0);
            }
        });

        this.search.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                if (TrendVideosActivity.this.searchLayout.isShown()) {
                    TrendVideosActivity.this.searchLayout.setVisibility(View.GONE);
                    TrendVideosActivity.this.search.setBackgroundResource(R.drawable.search1);
                    // isSearch = false;

                } else {
                    TrendVideosActivity.this.searchLayout.setVisibility(View.VISIBLE);
                    // isSearch = true;
                    TrendVideosActivity.this.search.setBackgroundResource(R.drawable.cancelbutton);
                }
            }
        });
        // Set a listener to be invoked when the list should be refreshed.
        this.moreVideosListView.setOnRefreshListener(new OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(final PullToRefreshBase<ListView> refreshView) {

                TrendVideosActivity.this.pullToRefresh = true;
                try {
                    final SearchVideoAsync task = new SearchVideoAsync(TrendVideosActivity.this, TrendVideosActivity
                            .getJSONRequest(1, TrendVideosActivity.this.userid).toString(),
                            TrendVideosActivity.this.trendname, false, null);
                    task.delegate = TrendVideosActivity.this;
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

                if (!TrendVideosActivity.this.flagLoading) {
                    TrendVideosActivity.this.flagLoading = true;
                    final int offset = TrendVideosActivity.this.myPageDtos.size();
                    if ((offset % TrendVideosActivity.PAGE_SIZE) == 0) {
                        final int pageNo = (offset / TrendVideosActivity.PAGE_SIZE) + 1;
                        try {
                            final SearchVideoAsync task = new SearchVideoAsync(TrendVideosActivity.this,
                                    TrendVideosActivity.getJSONRequest(pageNo,
                                            TrendVideosActivity.this.searchEdit.getText().toString()).toString(),
                                    TrendVideosActivity.this.trendname, true, null);
                            task.delegate = TrendVideosActivity.this;
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
