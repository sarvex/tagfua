/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.TagFu.async;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.TagFu.Constant;
import com.TagFu.R;
import com.TagFu.connectivity.Parser;
import com.TagFu.dto.ErrorResponse;
import com.TagFu.dto.MyPageDto;
import com.TagFu.model.Backend;
import com.TagFu.util.Alerts;
import com.TagFu.util.MoreVideos;
import com.TagFu.util.Util;

public class SearchVideoAsync extends AsyncTask<Void, Void, Void> {

    private static final String EMPTY = "";

    private static final String COM_AYANSYS_SAMPLEVIDEOPLAYER_TEMP_MYPAGESEARCH_JSON = "/com/ayansys/samplevideoplayer/temp/mypagesearch.json";

    private static final String PROBLEM_WITH_SERVER = "Problem with Server";

    private static final String _100 = "100";

    private static final String TRENDS = "trends";

    private static final String MYPAGE = "mypage";

    private static final Logger LOG = LoggerManager.getLogger();

    private ProgressDialog progressDialog;
    private final String request;
    private final Context context;
    private Object returnObject;
    public MoreVideos delegate;
    private boolean serverRequest;
    private final String type;
    private final boolean progressVisible;
    private final TextView errorMessageTextView;

    public SearchVideoAsync(final Context context, final String request, final String type,
            final boolean progressVisible, final TextView errorMsgView) {

        this.request = request;
        this.context = context;
        this.type = type;
        this.errorMessageTextView = errorMsgView;
        this.progressVisible = progressVisible;

    }

    @Override
    protected Void doInBackground(final Void... params) {

        if (Constant.IS_SERVER_REQUEST) {
            if (MYPAGE.equalsIgnoreCase(this.type)) {
                try {
                    this.returnObject = Backend.myPageSearch(this.context, this.request);
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }
            } else if (TRENDS.equalsIgnoreCase(this.type)) {
                try {
                    this.returnObject = Backend.myTrendVideos(this.context, this.request);
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }
            } else {
                try {
                    this.returnObject = Backend.otherSearch(this.context, this.request);
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }
            }
        } else {
            final String resp = Util.jsontoString(COM_AYANSYS_SAMPLEVIDEOPLAYER_TEMP_MYPAGESEARCH_JSON);
            if (resp != null) {
                try {
                    this.returnObject = Parser.myPageVideos(resp);
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(final Void result) {

        super.onPostExecute(result);
        if (this.progressVisible) {
            this.progressDialog.dismiss();
        }
        if (this.errorMessageTextView != null) {
            this.errorMessageTextView.setVisibility(View.GONE);
            this.errorMessageTextView.setText(R.string.no_search_text);
        }
        if (this.returnObject != null) {
            if (this.returnObject instanceof ErrorResponse) {
                final ErrorResponse res = (ErrorResponse) this.returnObject;
                if (Util.isConnected(this.context)) {
                    if (this.errorMessageTextView != null) {
                        this.errorMessageTextView.setVisibility(View.GONE);
                        this.errorMessageTextView.setText(R.string.no_search_text);
                    }
                    Alerts.showInfoOnly(res.getMessage(), this.context);
                } else {
                    if ((this.errorMessageTextView != null) && _100.equalsIgnoreCase(res.getErrorCode())) {
                        this.errorMessageTextView.setText(R.string.no_connectivity_text);
                        this.errorMessageTextView.setVisibility(View.VISIBLE);
                    } else {
                        Alerts.showInfoOnly(res.getMessage(), this.context);
                    }
                }
                this.delegate.videoList(null);
            } else if (this.returnObject instanceof List<?>) {
                final List<MyPageDto> currentList = (ArrayList<MyPageDto>) this.returnObject;
                this.delegate.videoList(currentList);
            } else {
                Toast.makeText(this.context, PROBLEM_WITH_SERVER, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onPreExecute() {

        if (this.progressVisible) {
            this.progressDialog = ProgressDialog.show(this.context, EMPTY, EMPTY, true);
            this.progressDialog.setContentView(((LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.progress_bar, null, false));
            this.progressDialog.setCancelable(false);
            this.progressDialog.setCanceledOnTouchOutside(false);
            this.progressDialog.show();
            // pd = ProgressDialog.show(context, "Loading..", "Please wait");
        }
    }
}
