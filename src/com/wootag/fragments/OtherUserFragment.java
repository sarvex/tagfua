/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wTagFufragments;

import java.util.ArrayList;
import java.util.List;

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
import android.view.ViewGroup;
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

import com.woTagFuonstant;
import com.wooTagFunuActivity;
import com.wootTagFuimport com.wootaTagFuter.OtherUserAdapter;
import com.wootagTagFurrorResponse;
import com.wootag.TagFuPage;
import com.wootag.dTagFuageDto;
import com.wootag.moTagFuckend;
import com.wootag.pulTagFuresh.PullToRefreshBase;
import com.wootag.pullTagFuesh.PullToRefreshBase.OnLastItemVisibleListener;
import com.wootag.pulltTagFush.PullToRefreshBase.OnRefreshListener;
import com.wootag.pulltoTagFuh.PullToRefreshListView;
import com.wootag.slideouTagFueoutActivity;
import com.wootag.util.AleTagFumport com.wootag.util.ConfTagFuport com.wootag.util.MainManager;

public class OtherUserFragment extends BaseFragment {

    private static final String OTHER = "other";
    private static final String ID = "id";
    private static final String TYPE = "type";
    private static final String TEXT2 = "text";
    private static final String USER = "user";
    private static final String PAGE_NO = "page_no";
    private static final String LOGIN_ID = "login_id";
    private static final String USERID = "userid";

    protected static final Logger LOG = LoggerManager.getLogger();

    public LayoutInflater inflater;
    protected boolean flagLoading;
    protected TextView heading;
    protected boolean pullToRefresh;
    private Button menu;// search,
    protected OtherUserAdapter otherPageAdapter;
    protected OtherUserFragment otherUserFragment;
    protected String rootFragment = "";
    private static final int VIDEOS_PER_PAGE = 10;
    private View view;
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
        // obj.put("notice", "1" );
        obj.put(Constant.DEVICE_TYPE, Constant.DEVICE_MODEL);
        obj.put(Constant.RESOLUTION, Config.getDeviceResolutionValue());
        request.put(USER, obj);

        return request;

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        this.view = inflater.inflate(R.layout.other_user, container, false);
        this.otherUserFragment = this;
        this.inflater = inflater;
        Config.setUserID(MainManager.getInstance().getUserId());
        this.videosList = new ArrayList<MyPageDto>();
        this.othersVideos = (PullToRefreshListView) this.view.findViewById(R.id.othersPageVideos);
        this.searchEdit = (EditText) this.view.findViewById(R.id.searchEditText);
        this.searchLayout = (RelativeLayout) this.view.findViewById(R.id.searchRL);
        this.menu = (Button) this.view.findViewById(R.id.menu);
        this.heading = (TextView) this.view.findViewById(R.id.heading);
        this.settingButton = (Button) this.view.findViewById(R.id.settings);

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
        final Bundle bundle = this.getArguments();
        if (bundle != null) {
            this.otherUserId = bundle.getString(USERID);
            this.rootFragment = bundle.getString(Constant.ROOT_FRAGMENT);
        }

        new LoadVideoProfile(1, true).execute();

        this.settingButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (OtherUserFragment.this.searchLayout.getVisibility() == View.GONE) {
                    final Animation bottomUp = AnimationUtils.loadAnimation(OtherUserFragment.this.getActivity(),
                            R.anim.bottom_up);
                    OtherUserFragment.this.searchLayout.startAnimation(bottomUp);
                    OtherUserFragment.this.searchLayout.setVisibility(View.VISIBLE);
                    OtherUserFragment.this.settingButton.setBackgroundResource(R.drawable.cancelbutton);
                } else {
                    OtherUserFragment.this.searchLayout.setVisibility(View.GONE);
                    OtherUserFragment.this.settingButton.setBackgroundResource(R.drawable.search1);
                }
                if (OtherUserFragment.this.otherPageAdapter != null) {
                    OtherUserFragment.this.otherPageAdapter.notifyDataSetChanged();
                }
            }
        });
        this.menu.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                final int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60,
                        OtherUserFragment.this.getResources().getDisplayMetrics());
                SlideoutActivity.prepare(OtherUserFragment.this.getActivity(), R.id.otherUserPage, width);
                OtherUserFragment.this.startActivity(new Intent(OtherUserFragment.this.getActivity(),
                        MenuActivity.class));
                OtherUserFragment.this.getActivity().overridePendingTransition(0, 0);
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

                final String text = OtherUserFragment.this.searchEdit.getText().toString();
                final InputMethodManager mgr = (InputMethodManager) OtherUserFragment.this.getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(OtherUserFragment.this.searchEdit.getWindowToken(), 0);
                if ((text != null) && (text.trim().length() > 0)) {
                    // searchLayout.setVisibility(View.GONE);
                    // settingButton.setBackgroundResource(R.drawable.search1);

                    final SearchVideosFragment fragment = new SearchVideosFragment();
                    final Bundle bundle = new Bundle();
                    bundle.putString(Constant.ROOT_FRAGMENT, OtherUserFragment.this.rootFragment);
                    bundle.putString(TEXT2, text);
                    bundle.putString(TYPE, OTHER);
                    bundle.putString(ID, OtherUserFragment.this.otherUserId);
                    fragment.setArguments(bundle);

                    if (Constant.MY_PAGE.equalsIgnoreCase(OtherUserFragment.this.rootFragment)) {
                        BaseFragment.tabActivity.pushFragments(R.id.mypageTab, fragment, Constant.SEARCH_VIDEOS,
                                OtherUserFragment.this, Constant.MYPAGE);
                    } else if (Constant.MY_PAGE_MORE_FEEDS.equalsIgnoreCase(OtherUserFragment.this.rootFragment)) {
                        BaseFragment.tabActivity.pushFragments(R.id.mypageTab, fragment, Constant.SEARCH_VIDEOS,
                                OtherUserFragment.this, Constant.MYPAGE);
                    } else if (Constant.MORE_VIDEOS.equalsIgnoreCase(OtherUserFragment.this.rootFragment)) {
                        BaseFragment.tabActivity.pushFragments(R.id.mypageTab, fragment, Constant.SEARCH_VIDEOS,
                                OtherUserFragment.this, Constant.MYPAGE);
                    } else if (Constant.VIDEO_FEEDS.equalsIgnoreCase(OtherUserFragment.this.rootFragment)) {
                        BaseFragment.tabActivity.pushFragments(R.id.feedTab, fragment, Constant.SEARCH_VIDEOS,
                                OtherUserFragment.this, Constant.HOME);
                    } else if (Constant.NOTIFICATIONS_PAGE.equalsIgnoreCase(OtherUserFragment.this.rootFragment)) {
                        BaseFragment.tabActivity.pushFragments(R.id.notificationTab, fragment, Constant.SEARCH_VIDEOS,
                                OtherUserFragment.this, Constant.NOTIFICATIONS);
                    } else if (Constant.BROWSE_PAGE.equalsIgnoreCase(OtherUserFragment.this.rootFragment)) {
                        BaseFragment.tabActivity.pushFragments(R.id.browsTab, fragment, Constant.SEARCH_VIDEOS,
                                OtherUserFragment.this, Constant.BROWSE);
                    }

                } else {
                    Alerts.showAlertOnly("Info", "Enter text to search", OtherUserFragment.this.getActivity());
                }

            }
        });

        // Set a listener to be invoked when the list should be refreshed.
        this.othersVideos.setOnRefreshListener(new OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(final PullToRefreshBase<ListView> refreshView) {

                OtherUserFragment.this.pullToRefresh = true;
                new LoadVideoProfile(1, false).execute();
            }

        });
        // Add an end-of-list listener
        this.othersVideos.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

            @Override
            public void onLastItemVisible() {

                if (!OtherUserFragment.this.flagLoading) {
                    OtherUserFragment.this.flagLoading = true;
                    final int offset = (OtherUserFragment.this.videosList.size() - 1);
                    if ((offset % OtherUserFragment.VIDEOS_PER_PAGE) == 0) {
                        final int pageNo = (offset / OtherUserFragment.VIDEOS_PER_PAGE) + 1;
                        new LoadVideoProfile(pageNo, true).execute();
                    }
                }
            }
        });

        return this.view;
    }

    @Override
    public void onResume() {

        this.searchLayout.setVisibility(View.GONE);
        this.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.settingButton.setBackgroundResource(R.drawable.search1);
        super.onResume();
    }

    public class LoadVideoProfile extends AsyncTask<Void, Void, Void> {

        private final boolean progressVisible;
        private final int pageNumber;
        private Object myPageResponse;
        private ProgressDialog progressDialog;

        public LoadVideoProfile(final int pageNo, final boolean progressVisible) {

            this.pageNumber = pageNo;
            this.progressVisible = progressVisible;
        }

        @Override
        protected Void doInBackground(final Void... params) {

            try {
                this.myPageResponse = Backend.otherUserVideos(OtherUserFragment.this.getActivity(),
                        OtherUserFragment.this.getJSONRequest(this.pageNumber));
            } catch (final JSONException exception) {
                LOG.e(exception);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {

            super.onPostExecute(result);
            OtherUserFragment.this.flagLoading = false;
            if (this.progressVisible) {
                this.progressDialog.dismiss();
            }
            if (OtherUserFragment.this.othersVideos != null) {
                OtherUserFragment.this.othersVideos.onRefreshComplete();
            }
            if (this.myPageResponse != null) {
                if (this.myPageResponse instanceof ErrorResponse) {
                    final ErrorResponse res = (ErrorResponse) this.myPageResponse;
                    Alerts.showAlertOnly("Info", res.getMessage(), OtherUserFragment.this.getActivity());

                } else if (this.myPageResponse instanceof MyPage) {
                    final MyPage response = (MyPage) this.myPageResponse;
                    final List<MyPageDto> newList = response.getVideoList();
                    if (OtherUserFragment.this.pullToRefresh) {
                        OtherUserFragment.this.pullToRefresh = false;
                        OtherUserFragment.this.othersVideos.onRefreshComplete();
                        OtherUserFragment.this.videosList = new ArrayList<MyPageDto>();
                        final MyPageDto dto = new MyPageDto();
                        OtherUserFragment.this.videosList.add(dto);
                        if ((newList != null) && (newList.size() > 0)) {
                            for (int i = 0; i < newList.size(); i++) {
                                OtherUserFragment.this.videosList.add(newList.get(i));
                            }
                        }
                        OtherUserFragment.this.otherPageAdapter = new OtherUserAdapter(
                                OtherUserFragment.this.getActivity(), 0, OtherUserFragment.this.videosList,
                                Constant.OTHERS_PAGE, response, OtherUserFragment.this.heading,
                                OtherUserFragment.this.otherUserFragment, OtherUserFragment.this.rootFragment);
                        OtherUserFragment.this.othersVideos.setAdapter(OtherUserFragment.this.otherPageAdapter);
                    } else {
                        if (OtherUserFragment.this.otherPageAdapter == null) {

                            OtherUserFragment.this.videosList = new ArrayList<MyPageDto>();
                            final MyPageDto dto = new MyPageDto();
                            OtherUserFragment.this.videosList.add(dto);
                            if ((newList != null) && (newList.size() > 0)) {
                                for (int i = 0; i < newList.size(); i++) {
                                    OtherUserFragment.this.videosList.add(newList.get(i));
                                }
                            }
                            OtherUserFragment.this.otherPageAdapter = new OtherUserAdapter(
                                    OtherUserFragment.this.getActivity(), 0, OtherUserFragment.this.videosList,
                                    Constant.OTHERS_PAGE, response, OtherUserFragment.this.heading,
                                    OtherUserFragment.this.otherUserFragment, OtherUserFragment.this.rootFragment);
                            OtherUserFragment.this.othersVideos.setAdapter(OtherUserFragment.this.otherPageAdapter);
                        } else {
                            if ((newList != null) && (newList.size() > 0)) {
                                for (int i = 0; i < newList.size(); i++) {
                                    OtherUserFragment.this.videosList.add(newList.get(i));
                                }
                            }
                            OtherUserFragment.this.otherPageAdapter.notifyDataSetChanged();
                        }
                    }
                }
            } else {
                Alerts.showAlertOnly("Info", "No videos available", OtherUserFragment.this.getActivity());
            }
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            if (this.progressVisible) {
                // pd = ProgressDialog.show(OtherUserActivity.this, "Loading..",
                // "Please wait");
                this.progressDialog = ProgressDialog.show(OtherUserFragment.this.getActivity(), "", "", true);
                final View v = OtherUserFragment.this.inflater.inflate(R.layout.progress_bar, null, false);
                this.progressDialog.setContentView(v);
                this.progressDialog.setCancelable(false);
                this.progressDialog.setCanceledOnTouchOutside(false);
                this.progressDialog.show();
            }
        }

    }

}
