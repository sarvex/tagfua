/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.wootag.adapter.BrowseAdapter;
import com.wootag.adapter.PeopleAdapter;
import com.wootag.adapter.TrendsAdapterBrowse;
import com.wootag.dto.ErrorResponse;
import com.wootag.dto.People;
import com.wootag.dto.Trends;
import com.wootag.dto.VideoDetails;
import com.wootag.dto.VideoProfile;
import com.wootag.model.Backend;
import com.wootag.pulltorefresh.PullToRefreshBase;
import com.wootag.pulltorefresh.PullToRefreshBase.OnLastItemVisibleListener;
import com.wootag.pulltorefresh.PullToRefreshBase.OnRefreshListener;
import com.wootag.pulltorefresh.PullToRefreshListView;
import com.wootag.slideout.SlideoutActivity;
import com.wootag.util.Alerts;
import com.wootag.util.Config;
import com.wootag.util.MainManager;

public class BrowseFragment extends BaseFragment {

    private static final String COMMENT_LIST_SET_ADAPTOR = "CommentList SetAdaptor";
    private static final String BROWSE2 = "browse";
    private static final String VIDEO_NO = "videoNo";
    private static final String LIST = "list";
    private static final String TRENDNAME = "trendname";
    private static final String ENTER_TEXT_TO_SEARCH = "Enter text to search";
    private static final String EMPTY = "";
    private static final String BROWSE = "Browse";
    private static final String X = "x";
    private static final String USER = "user";
    private static final String PAGE_NO = "page_no";
    private static final String USERID = "userid";
    private static final String BROWSE_BY = "browse_by";
    private static final String NAME = "name";
    public static BrowseFragment browseFragment;
    protected static final Logger LOG = LoggerManager.getLogger();

    private static final String PEOPLE = "people";
    private static final String TAGS = "tags";
    private static final String TRENDS = "trends";
    private static final String VIDEOS = "videos";
    private static final int VIDEOS_PER_PAGE = 10;
    protected BrowseAdapter adapter;
    protected Context context;
    protected boolean flagLoading;
    private TextView heading;
    protected boolean peoplesTab;
    protected boolean pullToRefreshList;
    protected boolean searchRequest;
    protected boolean tagsTab;

    protected boolean trendsTab;
    protected boolean videosTab;
    protected PullToRefreshListView list;
    protected PeopleAdapter peopleAdapter;
    private List<People> peopleList;
    protected List<People> peopleTabList;
    protected Object response;
    protected Button search, menu;
    protected EditText searchEdit;
    protected RelativeLayout searchLayout;
    private List<People> searchPeopleList;
    private List<VideoProfile> searchTagsList;
    protected TextView searchTextView;
    private List<Trends> searchTrendList;

    private List<VideoProfile> searchVideosList;
    private List<VideoProfile> tagsList;
    protected TrendsAdapterBrowse trendAdapterBrowse;
    private List<Trends> trendList;
    protected List<Trends> trendTabList;
    protected List<VideoProfile> userVideos;
    private RelativeLayout videos, pages, people, tags;
    protected ImageView videosImage, pagesImage, peopleImage, tagsImage;
    protected List<VideoProfile> videosList;
    private View view;

    public static JSONObject getJSONRequest(final String tabName, final int pageNo) throws JSONException {

        final JSONObject request = new JSONObject();
        final JSONObject obj = new JSONObject();
        obj.put(BROWSE_BY, tabName);
        obj.put(USERID, Config.getUserId());
        obj.put(PAGE_NO, pageNo);
        obj.put(Constant.DEVICE_TYPE, Constant.DEVICE_MODEL);
        obj.put(Constant.RESOLUTION, Config.getDeviceResolutionValue());// "1080 x 1920"//VideoPlayerConstants.deviceResolutionValue
        request.put(USER, obj);
        return request;
    }

    public static JSONObject getJSONTrendsRequest(final int pageNo) throws JSONException {

        final JSONObject request = new JSONObject();
        final JSONObject obj = new JSONObject();
        obj.put(PAGE_NO, pageNo);
        obj.put(Constant.RESOLUTION, Config.getDeviceResolutionValue());// "1080 x 1920"//VideoPlayerConstants.deviceResolutionValue
        request.put(USER, obj);
        return request;
    }

    static List<VideoDetails> getAllVideos(final List<VideoProfile> videos) {

        final List<VideoDetails> allvideos = new ArrayList<VideoDetails>();
        for (int i = 0; i < videos.size(); i++) {
            final VideoProfile dto = videos.get(i);
            final VideoDetails video = new VideoDetails();
            video.setUserId(dto.getUserId());
            video.setVideoID(dto.getVideoID());
            video.setVideothumbPath(dto.getVideoBannerURL());
            video.setVideoURL(dto.getVideoURL());
            video.setShareUrl(dto.getShareUrl());
            allvideos.add(video);
        }
        return allvideos;

    }

    public String getReloution() {

        final DisplayMetrics displaymetrics = new DisplayMetrics();
        this.getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        final int screenHeight = displaymetrics.heightPixels;
        final int screenWidth = displaymetrics.widthPixels;
        return screenWidth + X + screenHeight;
    }

    public JSONObject getSearchJSONRequest(final String tabName, final int pageNo) throws JSONException {

        final JSONObject request = new JSONObject();
        final JSONObject obj = new JSONObject();
        obj.put(NAME, this.searchEdit.getText().toString());
        obj.put(BROWSE_BY, tabName);
        obj.put(USERID, Config.getUserId());
        obj.put(PAGE_NO, pageNo);
        obj.put(Constant.RESOLUTION, Config.getDeviceResolutionValue());
        obj.put(Constant.DEVICE_TYPE, Constant.DEVICE_MODEL);
        request.put(USER, obj);
        return request;
    }

    public void loadList(final List<VideoProfile> currentList, final boolean clear) {

        this.userVideos.clear();

        if ((currentList != null) && !currentList.isEmpty()) {
            this.userVideos.addAll(currentList);
        }
        if (this.adapter != null) {
            if (clear) {
                this.list.setAdapter(this.adapter);
            }
            this.adapter.notifyDataSetChanged();
        }

        if (this.searchRequest && (this.userVideos != null) && (this.userVideos.size() <= 0)) {
            this.searchTextView.setVisibility(View.VISIBLE);
        } else {
            this.searchTextView.setVisibility(View.GONE);
        }

    }

    public void loadPeopleList(final List<People> currentList, final boolean clear) {

        this.peopleTabList.clear();

        if ((currentList != null) && !currentList.isEmpty()) {
            this.peopleTabList.addAll(currentList);
        }

        if (this.peopleAdapter != null) {
            if (clear) {
                this.list.setAdapter(this.peopleAdapter);
            }
            this.peopleAdapter.notifyDataSetChanged();
        }

        if (this.searchRequest && (this.peopleTabList != null) && (this.peopleTabList.size() <= 0)) {
            this.searchTextView.setVisibility(View.VISIBLE);
        } else {
            this.searchTextView.setVisibility(View.GONE);
        }

    }

    public void loadTrendList(final List<Trends> currentList, final boolean clear) {

        this.trendTabList.clear();

        if ((currentList != null) && !currentList.isEmpty()) {
            this.trendTabList.addAll(currentList);
        }

        if (this.trendAdapterBrowse != null) {
            if (clear) {
                this.list.setAdapter(this.trendAdapterBrowse);
            }
            this.trendAdapterBrowse.notifyDataSetChanged();
        }

        if (this.searchRequest && (this.trendTabList != null) && (this.trendTabList.size() <= 0)) {
            this.searchTextView.setVisibility(View.VISIBLE);
        } else {
            this.searchTextView.setVisibility(View.GONE);
        }

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        this.view = inflater.inflate(R.layout.activity_browse, container, false);
        this.pullToRefreshList = false;
        this.context = this.getActivity();
        browseFragment = this;
        Config.setUserID(MainManager.getInstance().getUserId());
        Config.setDeviceResolutionValue(this.getReloution());
        this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        this.videosImage = (ImageView) this.view.findViewById(R.id.videoImg);
        this.pagesImage = (ImageView) this.view.findViewById(R.id.pagesImg);
        this.tagsImage = (ImageView) this.view.findViewById(R.id.tagImg);
        this.peopleImage = (ImageView) this.view.findViewById(R.id.peopleImg);

        this.videos = (RelativeLayout) this.view.findViewById(R.id.videoTab);
        this.pages = (RelativeLayout) this.view.findViewById(R.id.pagesTab);
        this.tags = (RelativeLayout) this.view.findViewById(R.id.tagTab);
        this.people = (RelativeLayout) this.view.findViewById(R.id.peopleTab);
        this.list = (PullToRefreshListView) this.view.findViewById(R.id.browselist);
        this.menu = (Button) this.view.findViewById(R.id.menu);
        this.search = (Button) this.view.findViewById(R.id.settings);
        this.searchEdit = (EditText) this.view.findViewById(R.id.searchEditText);
        this.searchLayout = (RelativeLayout) this.view.findViewById(R.id.searchRL);
        this.heading = (TextView) this.view.findViewById(R.id.heading);
        this.searchTextView = (TextView) this.view.findViewById(R.id.browsesearchView);

        this.heading.setText(BROWSE);
        this.searchRequest = false;
        this.userVideos = new ArrayList<VideoProfile>();
        this.peopleTabList = new ArrayList<People>();
        this.trendTabList = new ArrayList<Trends>();

        this.tagsList = new ArrayList<VideoProfile>();
        this.peopleList = new ArrayList<People>();
        this.videosList = new ArrayList<VideoProfile>();
        this.trendList = new ArrayList<Trends>();

        this.searchTagsList = new ArrayList<VideoProfile>();
        this.searchPeopleList = new ArrayList<People>();
        this.searchVideosList = new ArrayList<VideoProfile>();
        this.searchTrendList = new ArrayList<Trends>();
        this.videosTab = true;

        new videoAsyncTask(true, false).execute();

        this.menu.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View view) {

                final int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, BrowseFragment.this
                        .getResources().getDisplayMetrics());
                SlideoutActivity.prepare(BrowseFragment.this.getActivity(), R.id.browseview, width);
                BrowseFragment.this.startActivity(new Intent(BrowseFragment.this.getActivity(), MenuActivity.class));
                BrowseFragment.this.getActivity().overridePendingTransition(0, 0);
            }
        });
        this.search.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View view) {

                if (BrowseFragment.this.searchLayout.isShown()) {
                    BrowseFragment.this.searchLayout.setVisibility(View.GONE);
                    BrowseFragment.this.searchTextView.setVisibility(View.GONE);
                    BrowseFragment.this.search.setBackgroundResource(R.drawable.search1);
                    BrowseFragment.this.searchRequest = false;
                    BrowseFragment.this.searchEdit.setText(EMPTY);
                    BrowseFragment.this.getSearchResponse();
                } else {
                    final Animation bottomUp = AnimationUtils.loadAnimation(BrowseFragment.this.getActivity(),
                            R.anim.bottom_up);
                    BrowseFragment.this.searchLayout.startAnimation(bottomUp);
                    BrowseFragment.this.searchLayout.setVisibility(View.VISIBLE);
                    BrowseFragment.this.search.setBackgroundResource(R.drawable.cancelbutton);
                    BrowseFragment.this.searchRequest = true;
                }
            }
        });
        this.searchEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(final TextView view, final int actionId, final KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    this.performSearch();
                    return true;
                }
                return false;
            }

            private void performSearch() {

                final String text = BrowseFragment.this.searchEdit.getText().toString();
                final InputMethodManager mgr = (InputMethodManager) BrowseFragment.this.getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(BrowseFragment.this.searchEdit.getWindowToken(), 0);
                if ((text != null) && (text.trim().length() > 0)) {
                    BrowseFragment.this.loadPeopleList(null, false);
                    BrowseFragment.this.loadList(null, false);
                    BrowseFragment.this.getSearchResponse();
                } else {
                    Alerts.showInfoOnly(ENTER_TEXT_TO_SEARCH, BrowseFragment.this.getActivity());
                }

            }
        });

        /**
         * Navigating to trends page or video details page if item is clicked
         */
        this.list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(final AdapterView<?> arg0, final View arg1, final int arg2, final long arg3) {

                if (BrowseFragment.this.peoplesTab) {
                } else if (BrowseFragment.this.trendsTab) {
                    final Trends dto = BrowseFragment.this.trendTabList.get(arg2 - 1);
                    final TrendVideosFragment fragment = new TrendVideosFragment();
                    final Bundle bundle = new Bundle();
                    bundle.putString(TRENDNAME, dto.getTagName());
                    fragment.setArguments(bundle);

                    BaseFragment.tabActivity.pushFragments(R.id.browsTab, fragment, Constant.TREND_PAGE,
                            BrowseFragment.browseFragment, Constant.BROWSE);

                } else {
                    final VideoDetailsFragment fragment = new VideoDetailsFragment();
                    final Bundle bundle = new Bundle();
                    bundle.putSerializable(LIST, (ArrayList<VideoProfile>) BrowseFragment.this.userVideos);
                    bundle.putInt(VIDEO_NO, arg2 - 1);
                    bundle.putSerializable(VIDEOS,
                            (ArrayList<VideoDetails>) BrowseFragment.getAllVideos(BrowseFragment.this.userVideos));
                    fragment.setArguments(bundle);

                    BaseFragment.tabActivity.pushFragments(R.id.browsTab, fragment, Constant.VIDEO_DETAILS_PAGE,
                            BrowseFragment.browseFragment, Constant.BROWSE);

                }

            }
        });
        /**
         * making auto refresh
         */
        this.list.setOnRefreshListener(new OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(final PullToRefreshBase<ListView> refreshView) {

                BrowseFragment.this.pullToRefreshList = true;
                BrowseFragment.this.getPullToRefreshList();

            }

        });
        /**
         * getting the more videos if reached end of the scroll view
         */
        this.list.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

            @Override
            public void onLastItemVisible() {

                if (!BrowseFragment.this.flagLoading) {
                    BrowseFragment.this.getMore();
                }
            }
        });

        this.videos.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View view) {

                BrowseFragment.this.videosImage.setImageResource(R.drawable.videotab_f);
                BrowseFragment.this.pagesImage.setImageResource(R.drawable.trendstab);
                BrowseFragment.this.peopleImage.setImageResource(R.drawable.peopletab);
                BrowseFragment.this.tagsImage.setImageResource(R.drawable.tagstab);

                BrowseFragment.this.videosTab = true;
                BrowseFragment.this.trendsTab = false;
                BrowseFragment.this.peoplesTab = false;
                BrowseFragment.this.tagsTab = false;
                BrowseFragment.this.flagLoading = false;
                BrowseFragment.this.list.setAdapter(BrowseFragment.this.adapter);
                BrowseFragment.this.loadPeopleList(null, false);
                BrowseFragment.this.loadList(null, false);
                BrowseFragment.this.getSearchResponse();

            }
        });

        this.people.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View view) {

                BrowseFragment.this.videosImage.setImageResource(R.drawable.videotab);
                BrowseFragment.this.pagesImage.setImageResource(R.drawable.trendstab);
                BrowseFragment.this.peopleImage.setImageResource(R.drawable.peopletab_f);
                BrowseFragment.this.tagsImage.setImageResource(R.drawable.tagstab);

                BrowseFragment.this.videosTab = false;
                BrowseFragment.this.trendsTab = false;
                BrowseFragment.this.peoplesTab = true;
                BrowseFragment.this.tagsTab = false;
                BrowseFragment.this.flagLoading = false;
                BrowseFragment.this.list.setAdapter(BrowseFragment.this.peopleAdapter);
                BrowseFragment.this.loadPeopleList(null, false);
                BrowseFragment.this.loadList(null, false);
                BrowseFragment.this.getSearchResponse();
            }
        });

        this.tags.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View view) {

                BrowseFragment.this.videosImage.setImageResource(R.drawable.videotab);
                BrowseFragment.this.pagesImage.setImageResource(R.drawable.trendstab);
                BrowseFragment.this.peopleImage.setImageResource(R.drawable.peopletab);
                BrowseFragment.this.tagsImage.setImageResource(R.drawable.tagstab_f);

                BrowseFragment.this.videosTab = false;
                BrowseFragment.this.trendsTab = false;
                BrowseFragment.this.peoplesTab = false;
                BrowseFragment.this.tagsTab = true;
                BrowseFragment.this.flagLoading = false;
                BrowseFragment.this.list.setAdapter(BrowseFragment.this.adapter);
                BrowseFragment.this.loadPeopleList(null, false);
                BrowseFragment.this.loadList(null, false);
                BrowseFragment.this.getSearchResponse();

            }
        });

        this.pages.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View view) {

                BrowseFragment.this.videosImage.setImageResource(R.drawable.videotab);
                BrowseFragment.this.pagesImage.setImageResource(R.drawable.trendstab_f);
                BrowseFragment.this.peopleImage.setImageResource(R.drawable.peopletab);
                BrowseFragment.this.tagsImage.setImageResource(R.drawable.tagstab);

                BrowseFragment.this.videosTab = false;
                BrowseFragment.this.trendsTab = true;
                BrowseFragment.this.peoplesTab = false;
                BrowseFragment.this.tagsTab = false;
                BrowseFragment.this.flagLoading = false;
                BrowseFragment.this.list.setAdapter(BrowseFragment.this.trendAdapterBrowse);
                BrowseFragment.this.loadTrendList(null, false);
                BrowseFragment.this.loadList(null, false);
                BrowseFragment.this.getSearchResponse();

            }
        });

        return this.view;
    }

    @Override
    public void onPause() {

        this.getActivity().overridePendingTransition(0, 0);
        super.onPause();
    }

    @Override
    public void onResume() {

        if (MainManager.getInstance().getUserId() != null) {
            Config.setUserID(MainManager.getInstance().getUserId());
        }
        this.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.getActivity().overridePendingTransition(0, 0);
        super.onResume();
    }

    protected void clearListAndAddNewPeople(final List<People> newPeople) {

        this.list.onRefreshComplete();
        this.pullToRefreshList = false;
        if ((newPeople != null) && (newPeople.size() > 0)) {
            this.peopleTabList = newPeople;
        }
        this.peopleAdapter = new PeopleAdapter(this.getActivity(), 0, this.peopleTabList, BROWSE2, this);
        LOG.i(COMMENT_LIST_SET_ADAPTOR);
        this.list.setAdapter(this.peopleAdapter);

        if (this.searchRequest && (newPeople != null) && (newPeople.size() <= 0)) {
            this.searchTextView.setVisibility(View.VISIBLE);
        } else {
            this.searchTextView.setVisibility(View.GONE);
        }

    }

    /**
     * clear all trends and add new trend this will be done with pull to refresh of trend tab
     */
    protected void clearListAndAddNewTrend(final List<Trends> newTrend) {

        this.pullToRefreshList = false;
        this.trendTabList.clear();
        if ((newTrend != null) && (newTrend.size() > 0)) {
            this.trendTabList = newTrend;
        }
        this.trendAdapterBrowse = new TrendsAdapterBrowse(this.getActivity(), this.trendTabList);
        LOG.i(COMMENT_LIST_SET_ADAPTOR);
        this.list.setAdapter(this.trendAdapterBrowse);

        if (this.searchRequest && (this.trendTabList != null) && (this.trendTabList.size() <= 0)) {
            this.searchTextView.setVisibility(View.VISIBLE);
        } else {
            this.searchTextView.setVisibility(View.GONE);
        }

    }

    protected void clearListAndAddNewVideos(final List<VideoProfile> videos) {

        this.list.onRefreshComplete();
        this.pullToRefreshList = false;
        this.userVideos.clear();
        if ((videos != null) && (videos.size() > 0)) {
            this.userVideos.addAll(videos);
        }
        this.adapter = new BrowseAdapter(this.getActivity(), this.userVideos);
        this.list.setAdapter(this.adapter);
        if (this.searchRequest && (this.userVideos != null) && (this.userVideos.size() <= 0)) {
            this.searchTextView.setVisibility(View.VISIBLE);
        } else {
            this.searchTextView.setVisibility(View.GONE);
        }

    }

    protected void getMore() {

        if (this.peoplesTab) {
            if (this.searchRequest) {
                final int offset = this.searchPeopleList.size();
                if ((offset % BrowseFragment.VIDEOS_PER_PAGE) == 0) {
                    this.flagLoading = true;
                    new LoadPeople(this.searchPeopleList, offset, BrowseFragment.PEOPLE, this.searchRequest, true)
                            .execute();
                }
            } else {
                final int offset = this.peopleList.size();
                if ((offset % BrowseFragment.VIDEOS_PER_PAGE) == 0) {
                    this.flagLoading = true;
                    new LoadPeople(this.peopleList, offset, BrowseFragment.PEOPLE, this.searchRequest, true).execute();
                }
            }

        } else if (this.trendsTab) {
            if (this.searchRequest) {
                final int offset = this.searchTrendList.size();
                if ((offset % BrowseFragment.VIDEOS_PER_PAGE) == 0) {
                    this.flagLoading = true;
                    new LoadTrends(this.trendList, offset, BrowseFragment.TRENDS, this.searchRequest, true).execute();
                }
            } else {
                final int offset = this.trendList.size();
                if ((offset % BrowseFragment.VIDEOS_PER_PAGE) == 0) {
                    this.flagLoading = true;
                    new LoadTrends(this.trendList, offset, BrowseFragment.TRENDS, this.searchRequest, true).execute();
                }
            }
        } else if (this.tagsTab) {
            if (this.searchRequest) {
                final int offset = this.searchTagsList.size();
                if ((offset % BrowseFragment.VIDEOS_PER_PAGE) == 0) {
                    this.flagLoading = true;
                    new LoadVideos(this.searchTagsList, offset, BrowseFragment.TAGS, this.searchRequest, true, false,
                            false).execute();
                }
            } else {
                final int offset = this.tagsList.size();
                if ((offset % BrowseFragment.VIDEOS_PER_PAGE) == 0) {
                    this.flagLoading = true;
                    new LoadVideos(this.tagsList, offset, BrowseFragment.TAGS, this.searchRequest, true, false, false)
                            .execute();
                }
            }
        } else {
            if (this.searchRequest) {
                final int offset = this.searchVideosList.size();
                if ((offset % BrowseFragment.VIDEOS_PER_PAGE) == 0) {
                    this.flagLoading = true;
                    new LoadVideos(this.searchVideosList, offset, BrowseFragment.VIDEOS, this.searchRequest, true,
                            false, false).execute();
                }
            } else {
                final int offset = this.videosList.size();
                if ((offset % BrowseFragment.VIDEOS_PER_PAGE) == 0) {
                    this.flagLoading = true;
                    new LoadVideos(this.videosList, offset, BrowseFragment.VIDEOS, this.searchRequest, true, false,
                            false).execute();
                }
            }
        }
    }

    protected void getPullToRefreshList() {

        if (this.peoplesTab) {
            if (this.searchRequest) {
                final int offset = this.searchPeopleList.size();
                new LoadPeople(this.searchPeopleList, offset, BrowseFragment.PEOPLE, this.searchRequest, false)
                        .execute();
            } else {
                final int offset = this.peopleList.size();
                new LoadPeople(this.peopleList, offset, BrowseFragment.PEOPLE, this.searchRequest, false).execute();
            }

        } else if (this.trendsTab) {
            if (this.searchRequest) {
                final int offset = this.searchTrendList.size();
                new LoadTrends(this.trendList, offset, BrowseFragment.TRENDS, this.searchRequest, false).execute();
            } else {
                final int offset = this.trendList.size();
                new LoadTrends(this.trendList, offset, BrowseFragment.TRENDS, this.searchRequest, false).execute();
            }
        } else if (this.tagsTab) {
            if (this.searchRequest) {
                final int offset = this.searchTagsList.size();
                new LoadVideos(this.searchTagsList, offset, BrowseFragment.TAGS, this.searchRequest, false, false,
                        false).execute();
            } else {
                final int offset = this.tagsList.size();
                new LoadVideos(this.tagsList, offset, BrowseFragment.TAGS, this.searchRequest, false, false, false)
                        .execute();
            }
        } else {
            if (this.searchRequest) {
                final int offset = this.searchVideosList.size();
                new LoadVideos(this.searchVideosList, offset, BrowseFragment.VIDEOS, this.searchRequest, false, false,
                        false).execute();
            } else {
                final int offset = this.videosList.size();
                new LoadVideos(this.videosList, offset, BrowseFragment.VIDEOS, this.searchRequest, false, false, true)
                        .execute();
            }
        }
    }

    protected void getSearchResponse() {

        if (this.peoplesTab) {
            if (this.searchRequest) {
                this.searchPeopleList.clear();
                new LoadPeople(this.searchPeopleList, 0, BrowseFragment.PEOPLE, this.searchRequest, true).execute();
            } else {
                final int offset = this.peopleList.size();
                if (offset == 0) {
                    new LoadPeople(this.peopleList, offset, BrowseFragment.PEOPLE, this.searchRequest, true).execute();
                } else {
                    this.loadPeopleList(this.peopleList, true);
                    this.list.getChildAt(0).setSelected(true);
                }
            }

        } else if (this.trendsTab) {
            if (this.searchRequest) {
                this.searchTrendList.clear();
                new LoadTrends(this.searchTrendList, 0, BrowseFragment.TRENDS, this.searchRequest, true).execute();
            } else {
                final int offset = this.trendList.size();
                if (offset == 0) {
                    new LoadTrends(this.trendList, offset, BrowseFragment.TRENDS, this.searchRequest, true).execute();
                } else {
                    this.loadTrendList(this.trendList, true);
                    // list.setSelection(0);
                    this.list.getChildAt(0).setSelected(true);
                }
            }
        } else if (this.tagsTab) {
            if (this.searchRequest) {
                this.searchTagsList.clear();
                new LoadVideos(this.searchTagsList, 0, BrowseFragment.TAGS, this.searchRequest, true, false, false)
                        .execute();
            } else {
                final int offset = this.tagsList.size();
                if (offset == 0) {
                    new LoadVideos(this.tagsList, offset, BrowseFragment.TAGS, this.searchRequest, true, false, false)
                            .execute();
                } else {
                    this.loadList(this.tagsList, true);
                    // list.setSelection(0);
                    this.list.getChildAt(0).setSelected(true);
                }
            }
        } else {
            if (this.searchRequest) {
                this.searchVideosList.clear();
                new LoadVideos(this.searchVideosList, 0, BrowseFragment.VIDEOS, this.searchRequest, true, false, false)
                        .execute();
            } else {
                final int offset = this.videosList.size();
                if (offset == 0) {
                    new LoadVideos(this.videosList, offset, BrowseFragment.VIDEOS, this.searchRequest, true, false,
                            false).execute();
                } else {
                    this.loadList(this.videosList, true);
                    this.list.getChildAt(0).setSelected(true);
                }
            }
        }
    }

    /**
     * load the people
     */
    public class LoadPeople extends AsyncTask<Void, Void, Object> {

        private final boolean searchRequest;
        private final boolean progressVisible;
        private final int offset;
        private ProgressDialog progressDialog;
        private List<People> people;
        private final String tab;

        public LoadPeople(final List<People> list, final int offset, final String tabType, final boolean searchRequest,
                final boolean progressVisible) {

            this.people = list;
            this.offset = offset;
            this.tab = tabType;
            this.progressVisible = progressVisible;
            this.searchRequest = searchRequest;

        }

        @Override
        protected Object doInBackground(final Void... params) {

            try {
                final int pageNo = (this.offset / BrowseFragment.VIDEOS_PER_PAGE) + 1;

                if (BrowseFragment.this.searchRequest) {
                    if (BrowseFragment.this.pullToRefreshList) {
                        BrowseFragment.this.response = Backend.search(BrowseFragment.this.context,
                                BrowseFragment.this.getSearchJSONRequest(this.tab, 1), this.tab);
                    } else {
                        BrowseFragment.this.response = Backend.search(BrowseFragment.this.context,
                                BrowseFragment.this.getSearchJSONRequest(this.tab, pageNo), this.tab);
                    }
                } else {
                    if (BrowseFragment.this.pullToRefreshList) {
                        BrowseFragment.this.response = Backend.browseVideos(BrowseFragment.this.context,
                                BrowseFragment.getJSONRequest(this.tab, 1), this.tab, false, false);
                    } else {
                        BrowseFragment.this.response = Backend.browseVideos(BrowseFragment.this.context,
                                BrowseFragment.getJSONRequest(this.tab, pageNo), this.tab, false, false);
                    }
                }
            } catch (final JSONException exception) {
                LOG.e(exception);
            }
            return BrowseFragment.this.response;
        }

        @Override
        protected void onPostExecute(final Object result) {

            super.onPostExecute(result);
            BrowseFragment.this.flagLoading = false;
            if ((this.progressDialog != null) && this.progressVisible) {
                this.progressDialog.dismiss();
            }
            if (BrowseFragment.this.list != null) {
                BrowseFragment.this.list.onRefreshComplete();
            }
            if (result != null) {
                if (BrowseFragment.this.response instanceof ErrorResponse) {
                    final ErrorResponse res = (ErrorResponse) BrowseFragment.this.response;
                    Alerts.showInfoOnly(res.getMessage(), BrowseFragment.this.getActivity());
                    BrowseFragment.this.searchTextView.setVisibility(View.GONE);
                    BrowseFragment.this.searchTextView.setText(R.string.no_search_text);
                    BrowseFragment.this.loadPeopleList(this.people, false);

                } else if (BrowseFragment.this.response instanceof List<?>) {
                    BrowseFragment.this.searchTextView.setVisibility(View.GONE);
                    BrowseFragment.this.searchTextView.setText(R.string.no_search_text);
                    final List<People> newList = (ArrayList<People>) BrowseFragment.this.response;
                    if (BrowseFragment.this.pullToRefreshList) {
                        if ((newList != null) && (newList.size() > 0)) {
                            this.people = newList;
                            BrowseFragment.this.clearListAndAddNewPeople(newList);
                        }
                    } else {
                        if (BrowseFragment.this.peopleAdapter == null) {
                            if ((newList != null) && (newList.size() > 0)) {
                                for (int i = 0; i < newList.size(); i++) {
                                    BrowseFragment.this.peopleTabList.add(newList.get(i));
                                    this.people.add(newList.get(i));
                                }
                            }
                            BrowseFragment.this.peopleAdapter = new PeopleAdapter(BrowseFragment.this.getActivity(), 0,
                                    BrowseFragment.this.peopleTabList, BROWSE2, BrowseFragment.this);
                            LOG.i(COMMENT_LIST_SET_ADAPTOR);
                            BrowseFragment.this.list.setAdapter(BrowseFragment.this.peopleAdapter);

                            if (BrowseFragment.this.searchRequest && (BrowseFragment.this.peopleTabList != null)
                                    && (BrowseFragment.this.peopleTabList.size() <= 0)) {
                                BrowseFragment.this.searchTextView.setVisibility(View.VISIBLE);
                            } else {
                                BrowseFragment.this.searchTextView.setVisibility(View.GONE);
                            }
                        } else {
                            if ((newList != null) && (newList.size() > 0)) {
                                for (int i = 0; i < newList.size(); i++) {
                                    this.people.add(newList.get(i));
                                }
                                BrowseFragment.this.loadPeopleList(this.people, false);
                            }

                        }
                    }
                }
            } else {
                Alerts.showInfoOnly(Constant.NO_RESPONSE_FROM_SERVER, BrowseFragment.this.context);

            }
            BrowseFragment.this.flagLoading = false;
        }

        @Override
        protected void onPreExecute() {

            if (this.progressVisible) {
                this.progressDialog = ProgressDialog.show(BrowseFragment.this.context, EMPTY, EMPTY, true);
                this.progressDialog
                        .setContentView(((LayoutInflater) BrowseFragment.this.context
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.progress_bar,
                                null, false));
                this.progressDialog.setCancelable(false);
                this.progressDialog.setCanceledOnTouchOutside(false);
                this.progressDialog.show();
            }
        }
    }

    /**
     * to load trends while changing to trend tab
     */
    public class LoadTrends extends AsyncTask<Void, Void, Object> {

        private final boolean searchRequest;
        private final boolean progressVisible;
        private final int offset;
        private ProgressDialog progressDialog;
        private final String tab;
        private List<Trends> trend;

        public LoadTrends(final List<Trends> list, final int offset, final String tabType, final boolean searchRequest,
                final boolean progressVisible) {

            this.trend = list;
            this.offset = offset;
            this.tab = tabType;
            this.progressVisible = progressVisible;
            this.searchRequest = searchRequest;

        }

        @Override
        protected Object doInBackground(final Void... params) {

            try {
                final int pageNo = (this.offset / BrowseFragment.VIDEOS_PER_PAGE) + 1;

                if (BrowseFragment.this.searchRequest) {
                    if (BrowseFragment.this.pullToRefreshList) {
                        BrowseFragment.this.response = Backend.search(BrowseFragment.this.context,
                                BrowseFragment.this.getSearchJSONRequest(this.tab, 1), this.tab);
                    } else {
                        BrowseFragment.this.response = Backend.search(BrowseFragment.this.context,
                                BrowseFragment.this.getSearchJSONRequest(this.tab, pageNo), this.tab);
                    }
                } else {
                    if (BrowseFragment.this.pullToRefreshList) {
                        BrowseFragment.this.response = Backend.browseVideos(BrowseFragment.this.context,
                                BrowseFragment.getJSONTrendsRequest(1), this.tab, false, false);
                    } else {
                        BrowseFragment.this.response = Backend.browseVideos(BrowseFragment.this.context,
                                BrowseFragment.getJSONTrendsRequest(pageNo), this.tab, false, false);
                    }
                }
            } catch (final JSONException exception) {
                LOG.e(exception);
            }
            return BrowseFragment.this.response;
        }

        @Override
        protected void onPostExecute(final Object result) {

            super.onPostExecute(result);
            BrowseFragment.this.flagLoading = false;
            if ((this.progressDialog != null) && this.progressVisible) {
                this.progressDialog.dismiss();
            }
            if (result != null) {
                if (BrowseFragment.this.list != null) {
                    BrowseFragment.this.list.onRefreshComplete();
                }
                if (BrowseFragment.this.response instanceof ErrorResponse) {
                    final ErrorResponse res = (ErrorResponse) BrowseFragment.this.response;
                    Alerts.showInfoOnly(res.getMessage(), BrowseFragment.this.getActivity());
                    BrowseFragment.this.searchTextView.setVisibility(View.GONE);
                    BrowseFragment.this.searchTextView.setText(R.string.no_search_text);
                    BrowseFragment.this.loadTrendList(this.trend, false);

                } else if (BrowseFragment.this.response instanceof List<?>) {
                    BrowseFragment.this.searchTextView.setVisibility(View.GONE);
                    BrowseFragment.this.searchTextView.setText(R.string.no_search_text);
                    final List<Trends> newList = (ArrayList<Trends>) BrowseFragment.this.response;
                    if (BrowseFragment.this.pullToRefreshList) {
                        BrowseFragment.this.list.onRefreshComplete();
                        this.trend = newList;
                        BrowseFragment.this.clearListAndAddNewTrend(newList);
                    } else {
                        if (BrowseFragment.this.trendAdapterBrowse == null) {
                            if ((newList != null) && (newList.size() > 0)) {
                                for (int i = 0; i < newList.size(); i++) {
                                    BrowseFragment.this.trendTabList.add(newList.get(i));
                                    this.trend.add(newList.get(i));
                                }
                            }
                            BrowseFragment.this.trendAdapterBrowse = new TrendsAdapterBrowse(
                                    BrowseFragment.this.getActivity(), BrowseFragment.this.trendTabList);
                            LOG.i(COMMENT_LIST_SET_ADAPTOR);
                            BrowseFragment.this.list.setAdapter(BrowseFragment.this.trendAdapterBrowse);

                            if (BrowseFragment.this.searchRequest && (BrowseFragment.this.trendTabList != null)
                                    && (BrowseFragment.this.trendTabList.size() <= 0)) {
                                BrowseFragment.this.searchTextView.setVisibility(View.VISIBLE);
                            } else {
                                BrowseFragment.this.searchTextView.setVisibility(View.GONE);
                            }

                        } else {
                            if ((newList != null) && (newList.size() > 0)) {
                                for (int i = 0; i < newList.size(); i++) {
                                    this.trend.add(newList.get(i));
                                }
                            }
                            BrowseFragment.this.loadTrendList(this.trend, false);
                            /*
                             * else { Alerts.ShowAlertOnly("Info", "No Trends", context); }
                             */
                        }
                    }
                }
            } else {
                BrowseFragment.this.list.onRefreshComplete();
                Alerts.showInfoOnly(Constant.NO_RESPONSE_FROM_SERVER, BrowseFragment.this.context);

            }
            BrowseFragment.this.flagLoading = false;
        }

        @Override
        protected void onPreExecute() {

            if (this.progressVisible) {
                this.progressDialog = ProgressDialog.show(BrowseFragment.this.context, EMPTY, EMPTY, true);
                this.progressDialog
                        .setContentView(((LayoutInflater) BrowseFragment.this.context
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.progress_bar,
                                null, false));
                this.progressDialog.setCancelable(false);
                this.progressDialog.setCanceledOnTouchOutside(false);
                this.progressDialog.show();
            }
        }
    }

    public class LoadVideos extends AsyncTask<Void, Void, Object> {

        private final boolean firstTime;
        private final boolean pullTorefresh;
        private final boolean searchRequest;
        private final boolean progressVisible;
        private final int offset;
        private ProgressDialog progressDialog;
        private final String tab;
        private List<VideoProfile> videos;

        public LoadVideos(final List<VideoProfile> list, final int offset, final String tabType,
                final boolean searchRequest, final boolean showProgress, final boolean firstTime,
                final boolean pullToRefresh) {

            this.videos = list;
            this.offset = offset;
            this.tab = tabType;
            this.progressVisible = showProgress;
            this.searchRequest = searchRequest;
            this.pullTorefresh = pullToRefresh;
            this.firstTime = firstTime;

        }

        @Override
        protected Object doInBackground(final Void... params) {

            try {
                final int pageNo = (this.offset / BrowseFragment.VIDEOS_PER_PAGE) + 1;
                if (BrowseFragment.this.searchRequest) {
                    if (BrowseFragment.this.pullToRefreshList) {
                        BrowseFragment.this.response = Backend.search(BrowseFragment.this.context,
                                BrowseFragment.this.getSearchJSONRequest(this.tab, 1), this.tab);
                    } else {

                        BrowseFragment.this.response = Backend.search(BrowseFragment.this.context,
                                BrowseFragment.this.getSearchJSONRequest(this.tab, pageNo), this.tab);
                    }
                } else {
                    if (BrowseFragment.this.pullToRefreshList) {
                        BrowseFragment.this.response = Backend.browseVideos(BrowseFragment.this.context,
                                BrowseFragment.getJSONRequest(this.tab, 1), this.tab, this.firstTime,
                                this.pullTorefresh);
                    } else if (BrowseFragment.this.trendsTab) {
                        BrowseFragment.this.response = Backend.browseVideos(BrowseFragment.this.context,
                                BrowseFragment.getJSONTrendsRequest(1), this.tab, this.firstTime, this.pullTorefresh);
                    } else {
                        BrowseFragment.this.response = Backend.browseVideos(BrowseFragment.this.context,
                                BrowseFragment.getJSONRequest(this.tab, pageNo), this.tab, this.firstTime,
                                this.pullTorefresh);
                    }
                }

            } catch (final JSONException exception) {
                LOG.e(exception);
            }
            return BrowseFragment.this.response;
        }

        @Override
        protected void onPostExecute(final Object result) {

            super.onPostExecute(result);
            BrowseFragment.this.flagLoading = false;
            if (this.progressVisible) {
                this.progressDialog.dismiss();
            }
            if (BrowseFragment.this.list != null) {
                BrowseFragment.this.list.onRefreshComplete();
            }
            if (BrowseFragment.this.response != null) {
                if (BrowseFragment.this.response instanceof ErrorResponse) {
                    final ErrorResponse res = (ErrorResponse) BrowseFragment.this.response;
                    Alerts.showInfoOnly(res.getMessage(), BrowseFragment.this.getActivity());
                    BrowseFragment.this.searchTextView.setVisibility(View.GONE);
                    BrowseFragment.this.searchTextView.setText(R.string.no_search_text);
                    BrowseFragment.this.loadList(this.videos, false);

                } else if (BrowseFragment.this.response instanceof List<?>) {
                    BrowseFragment.this.searchTextView.setVisibility(View.GONE);
                    BrowseFragment.this.searchTextView.setText(R.string.no_search_text);
                    if (BrowseFragment.this.pullToRefreshList) {
                        BrowseFragment.this.pullToRefreshList = false;
                        final List<VideoProfile> newVideos = (ArrayList<VideoProfile>) BrowseFragment.this.response;
                        this.videos = newVideos;
                        BrowseFragment.this.clearListAndAddNewVideos(newVideos);

                    } else {
                        if (BrowseFragment.this.adapter == null) {
                            BrowseFragment.this.userVideos = (ArrayList<VideoProfile>) BrowseFragment.this.response;
                            if ((BrowseFragment.this.userVideos != null) && (BrowseFragment.this.userVideos.size() > 0)) {
                                for (int i = 0; i < BrowseFragment.this.userVideos.size(); i++) {
                                    this.videos.add(BrowseFragment.this.userVideos.get(i));
                                }
                                BrowseFragment.this.adapter = new BrowseAdapter(BrowseFragment.this.getActivity(),
                                        BrowseFragment.this.userVideos);
                                LOG.i(COMMENT_LIST_SET_ADAPTOR);
                                BrowseFragment.this.list.setAdapter(BrowseFragment.this.adapter);
                            }

                            if (BrowseFragment.this.searchRequest && (BrowseFragment.this.userVideos != null)
                                    && (BrowseFragment.this.userVideos.size() <= 0)) {
                                BrowseFragment.this.searchTextView.setVisibility(View.VISIBLE);
                            } else {
                                BrowseFragment.this.searchTextView.setVisibility(View.GONE);
                            }

                        } else {
                            final List<VideoProfile> newList = (ArrayList<VideoProfile>) BrowseFragment.this.response;
                            if ((newList != null) && (newList.size() > 0)) {
                                for (int i = 0; i < newList.size(); i++) {
                                    this.videos.add(newList.get(i));
                                }
                                BrowseFragment.this.loadList(this.videos, false);
                            } else {
                                if (BrowseFragment.this.searchRequest && (this.videos != null)
                                        && (this.videos.size() <= 0)) {
                                    BrowseFragment.this.searchTextView.setVisibility(View.VISIBLE);
                                } else {
                                    BrowseFragment.this.searchTextView.setVisibility(View.GONE);
                                }
                                // Alerts.ShowAlertOnly("Info", "No Videos", context);
                            }
                        }
                    }
                }
                // }
            } else {
                Alerts.showInfoOnly(Constant.NO_RESPONSE_FROM_SERVER, BrowseFragment.this.context);
            }
            BrowseFragment.this.flagLoading = false;
        }

        @Override
        protected void onPreExecute() {

            if (this.progressVisible) {
                this.progressDialog = ProgressDialog.show(BrowseFragment.this.context, EMPTY, EMPTY, true);
                this.progressDialog
                        .setContentView(((LayoutInflater) BrowseFragment.this.context
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.progress_bar,
                                null, false));
                this.progressDialog.setCancelable(false);
                this.progressDialog.setCanceledOnTouchOutside(false);
                this.progressDialog.show();
            }
        }
    }

    public class videoAsyncTask extends AsyncTask<Integer, Void, Object> {

        private static final String NO_VIDEOS = "No Videos";
        private final boolean firstTime;
        private final boolean pullToRefresh;
        private ProgressDialog progressDialog;

        public videoAsyncTask(final boolean firstTime, final boolean pullToRefresh) {

            this.pullToRefresh = pullToRefresh;
            this.firstTime = firstTime;
        }

        @Override
        protected Object doInBackground(final Integer... params) {

            try {
                BrowseFragment.this.response = Backend.browseVideos(BrowseFragment.this.context,
                        BrowseFragment.getJSONRequest(BrowseFragment.VIDEOS, 1), BrowseFragment.VIDEOS, this.firstTime,
                        this.pullToRefresh);
            } catch (final JSONException exception) {
                LOG.e(exception);
            }

            return BrowseFragment.this.response;
        }

        @Override
        protected void onPostExecute(final Object result) {

            super.onPostExecute(result);
            BrowseFragment.this.flagLoading = false;
            if (BrowseFragment.this.list != null) {
                BrowseFragment.this.list.onRefreshComplete();
            }
            if (result != null) {
                if (BrowseFragment.this.response instanceof ErrorResponse) {
                    final ErrorResponse res = (ErrorResponse) BrowseFragment.this.response;
                    Alerts.showInfoOnly(res.getMessage(), BrowseFragment.this.getActivity());
                    BrowseFragment.this.searchTextView.setVisibility(View.GONE);
                    BrowseFragment.this.searchTextView.setText(R.string.no_search_text);

                } else if (BrowseFragment.this.response instanceof List<?>) {
                    BrowseFragment.this.searchTextView.setVisibility(View.GONE);
                    BrowseFragment.this.searchTextView.setText(R.string.no_search_text);
                    BrowseFragment.this.userVideos = (ArrayList<VideoProfile>) BrowseFragment.this.response;
                    if ((BrowseFragment.this.userVideos != null) && (BrowseFragment.this.userVideos.size() > 0)) {
                        for (int i = 0; i < BrowseFragment.this.userVideos.size(); i++) {
                            final VideoProfile dto = BrowseFragment.this.userVideos.get(i);
                            BrowseFragment.this.videosList.add(BrowseFragment.this.userVideos.get(i));
                        }
                        BrowseFragment.this.adapter = new BrowseAdapter(BrowseFragment.this.context,
                                BrowseFragment.this.userVideos);
                        LOG.i(COMMENT_LIST_SET_ADAPTOR);
                        BrowseFragment.this.list.setAdapter(BrowseFragment.this.adapter);
                        BrowseFragment.this.adapter.notifyDataSetChanged();
                    } else {
                        Alerts.showInfoOnly(NO_VIDEOS, BrowseFragment.this.context);
                    }

                }
            } else {
                Alerts.showInfoOnly(Constant.NO_RESPONSE_FROM_SERVER, BrowseFragment.this.context);
            }

            this.progressDialog.dismiss();
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            this.progressDialog = ProgressDialog.show(BrowseFragment.this.context, EMPTY, EMPTY, true);
            this.progressDialog.setContentView(((LayoutInflater) BrowseFragment.this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.progress_bar, null, false));
            this.progressDialog.setCancelable(false);
            this.progressDialog.setCanceledOnTouchOutside(false);
            this.progressDialog.show();
        }
    }

}
