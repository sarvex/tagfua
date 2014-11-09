package com.wootag;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
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

import com.wootag.adapter.PeopleAdapter;
import com.wootag.dto.ErrorResponse;
import com.wootag.dto.People;
import com.wootag.model.Backend;
import com.wootag.pulltorefresh.PullToRefreshBase;
import com.wootag.pulltorefresh.PullToRefreshBase.OnLastItemVisibleListener;
import com.wootag.pulltorefresh.PullToRefreshBase.OnRefreshListener;
import com.wootag.pulltorefresh.PullToRefreshListView;
import com.wootag.slideout.SlideoutActivity;
import com.wootag.util.Alerts;
import com.wootag.util.Config;
import com.wootag.util.Util;

public class SuggestedUserActivity extends Activity {

    public static SuggestedUserActivity suggestedUserActivity;

    protected static final Logger LOG = LoggerManager.getLogger();
    public boolean searchRequest;
    public PeopleAdapter peopleAdapter;
    public Object response;
    private List<People> adapterFriendsList;
    boolean flagLoading;
    boolean pullToRefresh;
    protected PullToRefreshListView list;
    protected Button searchButton, menuButton;
    protected EditText searchEdit;
    protected RelativeLayout searchLayout;
    protected TextView searchTextView;
    private static final int VIDEOS_PER_PAGE = 10;
    private LinearLayout view;
    protected List<People> wootagFriendsList;
    protected List<People> wootagSearchFriendsList;
    // private LinearLayout bodyLayout;

    protected Context context;
    private String screenType = "";
    private String userId = "";

    public JSONObject getJSONRequest(final String tabName, final int pageNo) throws JSONException {

        final JSONObject request = new JSONObject();
        final JSONObject obj = new JSONObject();
        // obj.put("name", "");
        obj.put("browse_by", tabName);
        obj.put("page_no", pageNo);
        request.put("user", obj);
        return request;
    }

    public JSONObject getSearchJSONRequest(final int pageNo) throws JSONException {

        final JSONObject request = new JSONObject();
        final JSONObject obj = new JSONObject();
        obj.put("name", this.searchEdit.getText().toString());
        obj.put("browse_by", "people");
        obj.put("userid", Config.getUserId());
        obj.put("page_no", pageNo);
        obj.put(Constant.RESOLUTION, Config.getDeviceResolutionValue());
        obj.put(Constant.DEVICE_TYPE, Constant.DEVICE_MODEL);
        request.put("user", obj);
        return request;
    }

    public JSONObject getSearchJSONRequest(final String tabName, final int pageNo) throws JSONException {

        final JSONObject request = new JSONObject();
        final JSONObject obj = new JSONObject();
        obj.put("name", "");
        obj.put("browse_by", tabName);
        obj.put("page_no", pageNo);
        request.put("user", obj);
        return request;
    }

    @Override
    public void onBackPressed() {

        this.finish();
        super.onBackPressed();

    }

    @Override
    protected void onCreate(final Bundle arg0) {

        super.onCreate(arg0);
        this.setContentView(R.layout.suggested_user);
        this.context = this;
        suggestedUserActivity = this;
        this.wootagFriendsList = new ArrayList<People>();
        this.wootagSearchFriendsList = new ArrayList<People>();
        this.adapterFriendsList = new ArrayList<People>();
        this.list = (PullToRefreshListView) this.findViewById(R.id.suggestedUserListView);
        this.view = (LinearLayout) this.findViewById(R.id.suggestedusersview);
        this.menuButton = (Button) this.findViewById(R.id.menu);
        this.searchButton = (Button) this.findViewById(R.id.settings);
        // send = (ImageButton) findViewById(R.id.seatchImageButton);
        this.searchTextView = (TextView) this.findViewById(R.id.searchView);
        this.searchLayout = (RelativeLayout) this.findViewById(R.id.searchRL);
        final TextView heading = (TextView) this.findViewById(R.id.heading);
        heading.setText(R.string.suggested_users);
        this.searchEdit = (EditText) this.findViewById(R.id.searchEditText);
        this.searchButton.setVisibility(View.VISIBLE);
        this.menuButton.setVisibility(View.GONE);
        // userId="10058";
        final Button back = (Button) this.findViewById(R.id.back);
        back.setVisibility(View.VISIBLE);
        back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                SuggestedUserActivity.this.finish();
            }
        });

        final Intent in = this.getIntent();
        if (in != null) {
            this.userId = in.getStringExtra(Constant.USERID);
        }

        if (in.getStringExtra(Constant.SCREEN) != null) {
            this.screenType = in.getStringExtra(Constant.SCREEN);
        }

        new FriendFinderAsync(Config.getUserId(), 1, true, this.searchRequest).execute();

        this.list.setOnRefreshListener(new OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(final PullToRefreshBase<ListView> refreshView) {

                SuggestedUserActivity.this.pullToRefresh = true;

                new FriendFinderAsync(Config.getUserId(), 1, false, SuggestedUserActivity.this.searchRequest).execute();

            }

        });
        this.list.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

            @Override
            public void onLastItemVisible() {

                if (!SuggestedUserActivity.this.flagLoading) {
                    SuggestedUserActivity.this.getMore();
                }
            }
        });

        this.list.setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount,
                    final int totalItemCount) {

                if (((firstVisibleItem + visibleItemCount) == totalItemCount) && (totalItemCount != 0)
                        && !SuggestedUserActivity.this.flagLoading) {
                    SuggestedUserActivity.this.flagLoading = true;
                    SuggestedUserActivity.this.getMore();
                }
            }

            @Override
            public void onScrollStateChanged(final AbsListView view, final int scrollState) {

                SuggestedUserActivity.this.flagLoading = false;

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

                final String text = SuggestedUserActivity.this.searchEdit.getText().toString();
                final InputMethodManager mgr = (InputMethodManager) SuggestedUserActivity.this
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(SuggestedUserActivity.this.searchEdit.getWindowToken(), 0);
                if ((text != null) && (text.trim().length() > 0)) {
                    SuggestedUserActivity.this.wootagSearchFriendsList.clear();
                    // new LoadPeople(searchPeopleList, 1,true).execute();
                    new FriendFinderAsync(Config.getUserId(), 1, true, SuggestedUserActivity.this.searchRequest)
                            .execute();
                } else {
                    Alerts.showAlertOnly("Info", "Enter text to search", SuggestedUserActivity.this);
                }

            }
        });
        this.menuButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                final int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60,
                        SuggestedUserActivity.this.getResources().getDisplayMetrics());
                SlideoutActivity.prepare(SuggestedUserActivity.this, R.id.suggestedView, width);
                SuggestedUserActivity.this.startActivity(new Intent(SuggestedUserActivity.this, MenuActivity.class));
                SuggestedUserActivity.this.overridePendingTransition(0, 0);
            }
        });
        this.searchButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (SuggestedUserActivity.this.searchLayout.isShown()) {
                    SuggestedUserActivity.this.searchLayout.setVisibility(View.GONE);
                    SuggestedUserActivity.this.searchTextView.setVisibility(View.GONE);
                    SuggestedUserActivity.this.searchButton.setBackgroundResource(R.drawable.search1);
                    SuggestedUserActivity.this.searchRequest = false;
                    SuggestedUserActivity.this.searchEdit.setText("");
                    SuggestedUserActivity.this.loadData(SuggestedUserActivity.this.wootagFriendsList);
                } else {
                    SuggestedUserActivity.this.searchLayout.setVisibility(View.VISIBLE);
                    SuggestedUserActivity.this.searchButton.setBackgroundResource(R.drawable.cancelbutton);
                    SuggestedUserActivity.this.searchRequest = true;
                }
            }
        });

    }

    @Override
    protected void onDestroy() {

        Util.clearImageCache(this.context);
        super.onDestroy();
    }

    void getMore() {

        if (!this.searchRequest) {
            final int offset = this.wootagFriendsList.size();
            final int pageNo = (offset / SuggestedUserActivity.VIDEOS_PER_PAGE) + 1;
            if ((offset % SuggestedUserActivity.VIDEOS_PER_PAGE) == 0) {
                this.flagLoading = true;
                new FriendFinderAsync(Config.getUserId(), pageNo, true, this.searchRequest).execute();
            }
        } else {
            final int offset = this.wootagSearchFriendsList.size();
            final int pageNo = (offset / SuggestedUserActivity.VIDEOS_PER_PAGE) + 1;
            if ((offset % SuggestedUserActivity.VIDEOS_PER_PAGE) == 0) {
                this.flagLoading = true;
                new FriendFinderAsync(Config.getUserId(), pageNo, true, this.searchRequest).execute();
            }
        }
    }

    void loadData(final List<People> peoplelist) {

        this.adapterFriendsList.clear();
        if ((peoplelist != null) && (peoplelist.size() > 0)) {
            this.adapterFriendsList.addAll(peoplelist);
        }
        if (this.peopleAdapter == null) {
            this.peopleAdapter = new PeopleAdapter(SuggestedUserActivity.this, 0, this.adapterFriendsList,
                    "suggestedusers", null);
            this.list.setAdapter(this.peopleAdapter);
        } else {
            this.peopleAdapter.notifyDataSetChanged();
        }

        if (this.searchRequest && (this.adapterFriendsList != null) && (this.adapterFriendsList.size() <= 0)) {
            this.searchTextView.setVisibility(View.VISIBLE);
        } else {
            this.searchTextView.setVisibility(View.GONE);
        }

    }

    private class FriendFinderAsync extends AsyncTask<Void, Void, Void> {

        private static final String EMPTY = "";
        private static final String NETWORK_PROBLEM_PLEASE_TRY_AGAIN = "Network problem.Please try again";
        private final boolean search;
        private final boolean progressVisible;
        private final int pageNo;
        private ProgressDialog progress;
        private Object response;
        private final String userId;

        public FriendFinderAsync(final String userId, final int pageNo, final boolean progressVisible,
                final boolean search) {

            this.userId = userId;
            this.pageNo = pageNo;
            this.progressVisible = progressVisible;
            this.search = search;
        }

        @Override
        protected Void doInBackground(final Void... params) {

            if (this.search) {
                try {
                    this.response = Backend.search(SuggestedUserActivity.this.context,
                            SuggestedUserActivity.this.getSearchJSONRequest(this.pageNo), "people");
                } catch (final JSONException e) {
                    LOG.e(e);
                }

            } else {
                try {
                    this.response = Backend.getWootagFriendFinderList(SuggestedUserActivity.this.context, this.userId,
                            this.pageNo);
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {

            super.onPostExecute(result);
            if ((this.progress != null) && this.progressVisible) {
                this.progress.dismiss();
            }
            SuggestedUserActivity.this.list.onRefreshComplete();
            if (this.response != null) {
                if (this.response instanceof List<?>) {
                    final List<People> list = (ArrayList<People>) this.response;
                    if (!this.search) {
                        if (SuggestedUserActivity.this.pullToRefresh) {
                            SuggestedUserActivity.this.pullToRefresh = false;
                            SuggestedUserActivity.this.wootagFriendsList.clear();
                        }

                        if (list != null) {
                            SuggestedUserActivity.this.wootagFriendsList.addAll(list);
                        }
                        SuggestedUserActivity.this.loadData(SuggestedUserActivity.this.wootagFriendsList);
                    } else {
                        if (SuggestedUserActivity.this.pullToRefresh) {
                            SuggestedUserActivity.this.pullToRefresh = false;
                            SuggestedUserActivity.this.wootagSearchFriendsList.clear();
                        }

                        if (list != null) {
                            SuggestedUserActivity.this.wootagSearchFriendsList.addAll(list);
                        }
                        SuggestedUserActivity.this.loadData(SuggestedUserActivity.this.wootagSearchFriendsList);
                    }

                    LOG.i("suggested user list size " + list.size());
                } else {
                    if (this.response instanceof ErrorResponse) {
                        final ErrorResponse resp = (ErrorResponse) this.response;
                        Alerts.showInfoOnly(resp.getMessage(), SuggestedUserActivity.this.context);
                    }
                }
            } else {
                Alerts.showInfoOnly(NETWORK_PROBLEM_PLEASE_TRY_AGAIN, SuggestedUserActivity.this.context);
            }
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            if (this.progressVisible) {
                this.progress = ProgressDialog.show(SuggestedUserActivity.this.context, EMPTY, EMPTY, true);
                this.progress
                        .setContentView(((LayoutInflater) SuggestedUserActivity.this.context
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.progress_bar,
                                null, false));
                this.progress.setCancelable(false);
                this.progress.setCanceledOnTouchOutside(false);
                this.progress.show();
            }
        }
    }
}
