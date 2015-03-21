/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wTagFuasync;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;

import org.json.JSONException;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.woTagFu;
import com.wooTagFunnectivity.VideoDataBase;
import com.wootTagFu.TagInfo;
import com.wootaTagFuTagResponse;
import com.wootag.model.Backend;

public class TagAsync extends AsyncTask<Void, Void, Boolean> {

    private static final String EMPTY = "";

    private static final String DELETETAG = "deletetag";

    private static final String UPDATETAG = "updatetag";

    private static final String ADDTAG = "addtag";

    private static final Logger LOG = LoggerManager.getLogger();

    private final Context context;

    private boolean status;
    private List<TagResponse> uploadedTags;
    private final String userId;
    private final String deleteTagId;
    private final String reqFor;
    private volatile boolean running = true;
    private ProgressDialog progressDialog;
    private final List<TagInfo> saveTags;
    private Object tagResponse;

    public TagAsync(final Context mcontext, final List<TagInfo> info, final String userid, final String reqestFor) {

        this.context = mcontext;
        this.reqFor = reqestFor;
        this.saveTags = info;
        this.userId = userid;
        this.deleteTagId = userid;
    }

    @Override
    protected Boolean doInBackground(final Void... params) {

        while (this.running) {
            if (ADDTAG.equalsIgnoreCase(this.reqFor)) {
                for (int j = 0; j < this.saveTags.size(); j++) {
                    final TagInfo tag = this.saveTags.get(j);
                    // clientVideoId = tag.getClientVideoId();
                    VideoDataBase.getInstance(this.context).updateTagWithVideoServerId(tag.getServerVideoId(),
                            tag.getClientVideoId(), this.context);
                }
                try {
                    this.tagResponse = Backend.addTags(this.context, this.saveTags);
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }
                if (this.tagResponse instanceof List<?>) {
                    this.uploadedTags = (ArrayList<TagResponse>) this.tagResponse;
                    if ((this.uploadedTags != null) && (this.uploadedTags.size() > 0)) {
                        for (int i = 0; i < this.uploadedTags.size(); i++) {
                            final TagResponse response = this.uploadedTags.get(i);
                            VideoDataBase.getInstance(this.context).updateTagWithServerId(response.getServerTagId(),
                                    response.getClientTagId(), 1, this.context);
                        }
                        this.status = true;
                    }
                }
                this.running = false;
            } else if (UPDATETAG.equalsIgnoreCase(this.reqFor)) {
                try {
                    this.status = Backend.updateTags(this.context, this.saveTags);
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }
                this.running = false;
            } else if (DELETETAG.equalsIgnoreCase(this.reqFor)) {
                try {
                    this.status = Backend.deleteTag(this.context, this.userId);
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }
                this.running = false;
            }
        }
        return Boolean.valueOf(this.status);
    }

    @Override
    protected void onCancelled() {

        if (this.progressDialog != null) {
            this.progressDialog.dismiss();
        }
        this.status = false;
        this.running = false;
    }

    @Override
    protected void onPostExecute(final Boolean result) {

        super.onPostExecute(result);
        if (this.progressDialog != null) {
            this.progressDialog.dismiss();
        }

    }

    @Override
    protected void onPreExecute() {

        super.onPreExecute();

        this.progressDialog = ProgressDialog.show(this.context, EMPTY, EMPTY, true);
        this.progressDialog.setContentView(((LayoutInflater) this.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.progress_bar, null, false));
        this.progressDialog.setCancelable(false);
        this.progressDialog.setCanceledOnTouchOutside(false);
        this.progressDialog.show();
    }

}
