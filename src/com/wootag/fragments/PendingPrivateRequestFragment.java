/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wTagFufragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.woTagFu;
import com.wooTagFuapter.PendingPrivateGroupRequestAdapter;
import com.wootTagFu.ErrorResponse;
import com.wootaTagFuLiked;
import com.wootagTagFu.Backend;
import com.wootag.TagFurefresh.PullToRefreshBase;
import com.wootag.pTagFuefresh.PullToRefreshBase.OnLastItemVisibleListener;
import com.wootag.puTagFufresh.PullToRefreshBase.OnRefreshListener;
import com.wootag.pulTagFuresh.PullToRefreshListView;
import com.wootag.utilTagFus;
import com.wootag.util.TagFu;
import com.wootag.util.MainManager;

public class PendingPrivateRequestFragment extends BaseFragment {

    private static final Logger LOG = LoggerManager.getLogger();

    protected Activity context;
    protected boolean flagLoading;
    private TextView heading;
    private boolean isPullToRefresh;
    protected boolean isPullToRefreshList;
    private Button menu;
    private PendingPrivateGroupRequestAdapter pvtAdapter;
    private PullToRefreshListView pvtpendingsList;
    private Button search;
    private View view;
    private int currentPageNumber;
    protected List<Liked> requestList;
    private static final int PAGE_SIZE = 10;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        this.view = inflater.inflate(R.layout.activity_pending_private_request, container, false);
        this.context = this.getActivity();
        Config.setUserID(MainManager.getInstance().getUserId());
        this.pvtpendingsList = (PullToRefreshListView) this.view.findViewById(R.id.pvtpendingsList);
        this.menu = (Button) this.view.findViewById(R.id.menu);
        this.search = (Button) this.view.findViewById(R.id.settings);
        this.heading = (TextView) this.view.findViewById(R.id.heading);
        this.heading.setText("Private Group Requests");
        this.search.setVisibility(View.GONE);
        this.menu.setVisibility(View.GONE);
        final Button back = (Button) this.view.findViewById(R.id.back);
        back.setVisibility(View.VISIBLE);
        back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                BaseFragment.tabActivity.removeFromBackStack();
            }
        });

        this.requestList = new ArrayList<Liked>();
        new NotificationAsync(Config.getUserId(), true, 1).execute();
        this.pvtpendingsList.setOnRefreshListener(new OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(final PullToRefreshBase<ListView> refreshView) {

                PendingPrivateRequestFragment.this.isPullToRefreshList = true;
                PendingPrivateRequestFragment.this.flagLoading = true;
                new NotificationAsync(Config.getUserId(), false, 1).execute();
            }

        });
        this.pvtpendingsList.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

            @Override
            public void onLastItemVisible() {

                if (!PendingPrivateRequestFragment.this.flagLoading) {
                    PendingPrivateRequestFragment.this.flagLoading = true;
                    final int offset = PendingPrivateRequestFragment.this.requestList.size();
                    if ((offset % PendingPrivateRequestFragment.PAGE_SIZE) == 0) {
                        PendingPrivateRequestFragment.this.flagLoading = true;
                        final int pageNo = (offset / PendingPrivateRequestFragment.PAGE_SIZE) + 1;
                        new NotificationAsync(Config.getUserId(), true, pageNo).execute();
                    }
                }
            }
        });

        return this.view;
    }

    void loadData(final List<Liked> pendingRequests) {

        this.flagLoading = false;
        if (this.isPullToRefreshList) {
            this.isPullToRefreshList = false;
            this.pvtpendingsList.onRefreshComplete();
            this.requestList.clear();
            if ((pendingRequests != null) && (pendingRequests.size() > 0)) {
                this.requestList.addAll(pendingRequests);
                this.pvtAdapter = new PendingPrivateGroupRequestAdapter(this.context, 0, this.requestList, this);
                this.pvtpendingsList.setAdapter(this.pvtAdapter);
            }
            this.pvtAdapter.notifyDataSetChanged();
        } else {

            if ((pendingRequests != null) && (pendingRequests.size() > 0)) {
                if (this.pvtAdapter == null) {
                    for (int i = 0; i < pendingRequests.size(); i++) {
                        this.requestList.add(pendingRequests.get(i));
                    }
                    this.pvtAdapter = new PendingPrivateGroupRequestAdapter(this.context, 0, this.requestList, this);
                    this.pvtpendingsList.setAdapter(this.pvtAdapter);
                } else {
                    for (int i = 0; i < pendingRequests.size(); i++) {
                        this.requestList.add(pendingRequests.get(i));
                    }
                    this.pvtAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    private class NotificationAsync extends AsyncTask<Void, Void, Void> {

        private static final String EMPTY = "";
        private boolean firstTime;
        private boolean pullToRefresh;
        private final boolean showProgress;
        private final int pageNo;
        private ProgressDialog progressDialog;
        private Object response;
        private final String userId;

        public NotificationAsync(final String userId, final boolean showProgress, final int pageNo) {

            this.userId = userId;
            this.showProgress = showProgress;
            this.pageNo = pageNo;
        }

        @Override
        protected Void doInBackground(final Void... params) {

            try {
                this.response = Backend.getPVTPenidngRequestsList(PendingPrivateRequestFragment.this.context,
                        this.userId, this.pageNo);
            } catch (final JSONException exception) {
                LOG.e(exception);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {

            super.onPostExecute(result);
            if ((this.progressDialog != null) && this.showProgress) {
                this.progressDialog.dismiss();
            }
            if (this.response != null) {
                if (this.response instanceof List<?>) {
                    final List<Liked> list = (ArrayList<Liked>) this.response;
                    PendingPrivateRequestFragment.this.loadData(list);
                } else {
                    if (this.response instanceof ErrorResponse) {
                        final ErrorResponse resp = (ErrorResponse) this.response;
                        Alerts.showAlertOnly("Info", resp.getMessage(), PendingPrivateRequestFragment.this.context);
                    }
                }
            } else {
                Alerts.showAlertOnly("Info", "Network problem.Please try again",
                        PendingPrivateRequestFragment.this.context);
            }
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            if (this.showProgress) {
                this.progressDialog = ProgressDialog.show(PendingPrivateRequestFragment.this.context, EMPTY, EMPTY, true);
                this.progressDialog
                        .setContentView(((LayoutInflater) PendingPrivateRequestFragment.this.context
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.progress_bar,
                                null, false));
                this.progressDialog.setCancelable(false);
                this.progressDialog.setCanceledOnTouchOutside(false);
                this.progressDialog.show();
            }

        }
    }

}
