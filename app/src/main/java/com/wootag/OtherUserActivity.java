/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.TagFu;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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

import com.TagFu.adapter.OtherUserAdapter;
import com.TagFu.dto.ErrorResponse;
import com.TagFu.dto.MyPage;
import com.TagFu.dto.MyPageDto;
import com.TagFu.model.Backend;
import com.TagFu.pulltorefresh.PullToRefreshBase;
import com.TagFu.pulltorefresh.PullToRefreshBase.OnLastItemVisibleListener;
import com.TagFu.pulltorefresh.PullToRefreshBase.OnRefreshListener;
import com.TagFu.pulltorefresh.PullToRefreshListView;
import com.TagFu.slideout.SlideoutActivity;
import com.TagFu.util.Alerts;
import com.TagFu.util.Config;
import com.TagFu.util.MainManager;
import com.TagFu.util.Util;

public class OtherUserActivity extends Activity {

    private static final String ID = "id";
    private static final String OTHER = "other";
    private static final String TYPE = "type";
    private static final String TEXT2 = "text";
    private static final String USER = "user";
    private static final String PAGE_NO = "page_no";
    private static final String LOGIN_ID = "login_id";
    private static final String USERID = "userid";

    public static OtherUserActivity otherUserActivity;

    protected static final Logger LOG = LoggerManager.getLogger();
    protected OtherUserActivity context;
    protected boolean flagLoading;
    protected TextView heading;
    protected boolean pullToRefresh;
    private Button menu;// search,
    protected OtherUserAdapter otherPageAdapter;
    private LinearLayout otherVideosLayout;
    private static final int VIDEOS_PER_PAGE = 10;
    private LinearLayout followerLL, followingLL;
    protected PullToRefreshListView othersVideos;
    protected String otherUserId = "";
    protected EditText searchEdit;
    protected RelativeLayout searchLayout;
    protected Button settingButton;
    private MyPage user;
    protected List<MyPageDto> videosList;

    public JSONObject getJSONRequest(final int pageNo) throws JSONException {

        final JSONObject request = new JSONObject();
        final JSONObject obj = new JSONObject();
        obj.put(USERID, this.otherUserId);
        obj.put(LOGIN_ID, Config.getUserId());
        obj.put(PAGE_NO, pageNo);
        obj.put(Constant.DEVICE_TYPE, Constant.DEVICE_MODEL);
        obj.put(Constant.RESOLUTION, Config.getDeviceResolutionValue());
        request.put(USER, obj);

        return request;

    }

    @Override
    protected void onCreate(final Bundle bundle) {

        super.onCreate(bundle);
        this.setContentView(R.layout.other_user);
        otherUserActivity = this;
        this.context = this;
        Config.setUserID(MainManager.getInstance().getUserId());
        this.videosList = new ArrayList<MyPageDto>();
        this.othersVideos = (PullToRefreshListView) this.findViewById(R.id.othersPageVideos);
        // send = (ImageButton) findViewById(R.id.seatchImageButton);
        this.searchEdit = (EditText) this.findViewById(R.id.searchEditText);
        this.searchLayout = (RelativeLayout) this.findViewById(R.id.searchRL);
        this.menu = (Button) this.findViewById(R.id.menu);
        this.heading = (TextView) this.findViewById(R.id.heading);
        // heading.setText("Other User Page");
        this.settingButton = (Button) this.findViewById(R.id.settings);
        this.otherVideosLayout = (LinearLayout) this.findViewById(R.id.bodyLayout);

        this.menu.setVisibility(View.GONE);
        final Button back = (Button) this.findViewById(R.id.back);
        back.setVisibility(View.VISIBLE);
        back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                OtherUserActivity.this.finish();
            }
        });

        final Bundle b = this.getIntent().getExtras();
        if ((b != null) && b.containsKey(USERID)) {
            this.otherUserId = b.getString(USERID);
        }

        this.settingButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (OtherUserActivity.this.searchLayout.getVisibility() == View.GONE) {
                    final Animation bottomUp = AnimationUtils.loadAnimation(OtherUserActivity.this, R.anim.bottom_up);
                    OtherUserActivity.this.searchLayout.startAnimation(bottomUp);
                    OtherUserActivity.this.searchLayout.setVisibility(View.VISIBLE);
                    OtherUserActivity.this.settingButton.setBackgroundResource(R.drawable.cancelbutton);
                } else {
                    OtherUserActivity.this.searchLayout.setVisibility(View.GONE);
                    OtherUserActivity.this.settingButton.setBackgroundResource(R.drawable.search1);
                }
                if (OtherUserActivity.this.otherPageAdapter != null) {
                    OtherUserActivity.this.otherPageAdapter.notifyDataSetChanged();
                }
            }
        });
        this.menu.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                final int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60,
                        OtherUserActivity.this.getResources().getDisplayMetrics());
                SlideoutActivity.prepare(OtherUserActivity.this, R.id.otherUserPage, width);
                OtherUserActivity.this.startActivity(new Intent(OtherUserActivity.this, MenuActivity.class));
                OtherUserActivity.this.overridePendingTransition(0, 0);
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

                final String text = OtherUserActivity.this.searchEdit.getText().toString();
                final InputMethodManager mgr = (InputMethodManager) OtherUserActivity.this
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(OtherUserActivity.this.searchEdit.getWindowToken(), 0);
                if ((text != null) && (text.trim().length() > 0)) {
                    final Intent intent = new Intent(OtherUserActivity.this, SearchVideosActivity.class);
                    intent.putExtra(TEXT2, text);
                    intent.putExtra(TYPE, OTHER);
                    intent.putExtra(ID, OtherUserActivity.this.otherUserId);
                    OtherUserActivity.this.startActivity(intent);
                } else {
                    Alerts.showAlertOnly("Info", "Enter text to search", OtherUserActivity.this);
                }

            }
        });

        // Set a listener to be invoked when the list should be refreshed.
        this.othersVideos.setOnRefreshListener(new OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(final PullToRefreshBase<ListView> refreshView) {

                OtherUserActivity.this.pullToRefresh = true;
                new LoadVideoProfile(1, false).execute();
            }

        });
        // Add an end-of-list listener
        this.othersVideos.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

            @Override
            public void onLastItemVisible() {

                if (!OtherUserActivity.this.flagLoading) {
                    OtherUserActivity.this.flagLoading = true;
                    final int offset = (OtherUserActivity.this.videosList.size() - 1);
                    if ((offset % OtherUserActivity.VIDEOS_PER_PAGE) == 0) {
                        final int pageNo = (offset / OtherUserActivity.VIDEOS_PER_PAGE) + 1;

                        new LoadVideoProfile(pageNo, true).execute();

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

    @Override
    protected void onResume() {

        super.onResume();
        this.searchLayout.setVisibility(View.GONE);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.settingButton.setBackgroundResource(R.drawable.search1);
    }

    public class LoadVideoProfile extends AsyncTask<Void, Void, Void> {

        private static final String EMPTY = "";
        private final boolean isProgressShow;
        private final int pageNumber;
        private Object myPageResponse;
        private ProgressDialog progressDialog;

        public LoadVideoProfile(final int pageNo, final boolean isProgressShow) {

            this.pageNumber = pageNo;
            this.isProgressShow = isProgressShow;
        }

        @Override
        protected Void doInBackground(final Void... params) {

            try {
                this.myPageResponse = Backend.otherUserVideos(OtherUserActivity.this,
                        OtherUserActivity.this.getJSONRequest(this.pageNumber));
            } catch (final JSONException exception) {
                LOG.e(exception);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {

            super.onPostExecute(result);
            OtherUserActivity.this.flagLoading = false;
            // try {
            if (this.isProgressShow) {
                this.progressDialog.dismiss();
            }
            if (OtherUserActivity.this.othersVideos != null) {
                OtherUserActivity.this.othersVideos.onRefreshComplete();
            }
            if (this.myPageResponse != null) {
                if (this.myPageResponse instanceof ErrorResponse) {
                    final ErrorResponse res = (ErrorResponse) this.myPageResponse;
                    Alerts.showAlertOnly("Info", res.getMessage(), OtherUserActivity.this);

                } else if (this.myPageResponse instanceof MyPage) {
                    final MyPage response = (MyPage) this.myPageResponse;
                    final List<MyPageDto> newList = response.getVideoList();
                    if (OtherUserActivity.this.pullToRefresh) {
                        OtherUserActivity.this.pullToRefresh = false;
                        OtherUserActivity.this.othersVideos.onRefreshComplete();
                        OtherUserActivity.this.videosList = new ArrayList<MyPageDto>();
                        final MyPageDto dto = new MyPageDto();
                        OtherUserActivity.this.videosList.add(dto);
                        if ((newList != null) && (newList.size() > 0)) {
                            for (int i = 0; i < newList.size(); i++) {
                                OtherUserActivity.this.videosList.add(newList.get(i));
                            }
                        }
                        OtherUserActivity.this.otherPageAdapter = new OtherUserAdapter(OtherUserActivity.this.context,
                                0, OtherUserActivity.this.videosList, Constant.OTHERS_PAGE, response,
                                OtherUserActivity.this.heading, null, EMPTY);
                        OtherUserActivity.this.othersVideos.setAdapter(OtherUserActivity.this.otherPageAdapter);
                    } else {
                        if (OtherUserActivity.this.otherPageAdapter == null) {

                            OtherUserActivity.this.videosList = new ArrayList<MyPageDto>();
                            final MyPageDto dto = new MyPageDto();
                            OtherUserActivity.this.videosList.add(dto);
                            if ((newList != null) && (newList.size() > 0)) {
                                for (int i = 0; i < newList.size(); i++) {
                                    OtherUserActivity.this.videosList.add(newList.get(i));
                                }
                            }
                            OtherUserActivity.this.otherPageAdapter = new OtherUserAdapter(
                                    OtherUserActivity.this.context, 0, OtherUserActivity.this.videosList,
                                    Constant.OTHERS_PAGE, response, OtherUserActivity.this.heading, null, EMPTY);
                            OtherUserActivity.this.othersVideos.setAdapter(OtherUserActivity.this.otherPageAdapter);
                        } else {
                            if ((newList != null) && (newList.size() > 0)) {
                                for (int i = 0; i < newList.size(); i++) {
                                    OtherUserActivity.this.videosList.add(newList.get(i));
                                }
                            }
                            OtherUserActivity.this.otherPageAdapter.notifyDataSetChanged();
                        }
                    }
                }
            } else {
                Alerts.showAlertOnly("Info", "No videos available", OtherUserActivity.this);
            }
            // } catch (final Exception e) {
            // LOG.e("OthersVideos", e.toString());
            // }
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            if (this.isProgressShow) {
                this.progressDialog = ProgressDialog.show(OtherUserActivity.this, EMPTY, EMPTY, true);
                this.progressDialog
                        .setContentView(((LayoutInflater) OtherUserActivity.this
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.progress_bar,
                                null, false));
                this.progressDialog.setCancelable(false);
                this.progressDialog.setCanceledOnTouchOutside(false);
                this.progressDialog.show();
            }
        }

    }

}
