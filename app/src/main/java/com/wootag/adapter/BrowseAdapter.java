/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.TagFu.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.base.Strings;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.TagFu.Constant;
import com.TagFu.R;
import com.TagFu.dto.VideoProfile;
import com.TagFu.fragments.BaseFragment;
import com.TagFu.fragments.BrowseFragment;
import com.TagFu.fragments.OtherUserFragment;
import com.TagFu.ui.Image;
import com.TagFu.util.Alerts;
import com.TagFu.util.Config;

public class BrowseAdapter extends BaseAdapter {

    private static final String EMPTY = "";
    private static final String NO_USER_ID_AVAILABLE_FOR_THIS_USER = "No user id available for this user.";
    private static final String USERID = "userid";
    private static final Logger LOG = LoggerManager.getLogger();

    protected final Context context;
    private final List<VideoProfile> videoList;

    public BrowseAdapter(final Context context, final List<VideoProfile> list) {

        this.context = context;
        this.videoList = list;

    }

    @Override
    public int getCount() {

        return this.videoList.size();
    }

    @Override
    public Object getItem(final int position) {

        return this.videoList.get(position);
    }

    @Override
    public long getItemId(final int position) {

        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        if (convertView == null) {
            convertView = ((LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
                    R.layout.browse_item, parent, false);
        }

        final ImageView videoImage = (ImageView) convertView.findViewById(R.id.videoImg);
        final ImageView tagImage = (ImageView) convertView.findViewById(R.id.tagPic);
        final ImageView userImage = (ImageView) convertView.findViewById(R.id.userpic);
        final TextView userName = (TextView) convertView.findViewById(R.id.usrName);
        final TextView taginf = (TextView) convertView.findViewById(R.id.taginf);
        final LinearLayout userInfo = (LinearLayout) convertView.findViewById(R.id.userinfo);
        userInfo.setVisibility(View.GONE);
        final VideoProfile currentVideo = this.videoList.get(position);
        userName.setText(currentVideo.getUserName());
        if (currentVideo.getLatestTag() != null) {
            taginf.setText(currentVideo.getLatestTag());
            tagImage.setVisibility(View.VISIBLE);
        } else {
            if (currentVideo.getVideoTitle() != null) {
                taginf.setText(currentVideo.getVideoTitle());
            } else {
                taginf.setText(EMPTY);
            }
            tagImage.setVisibility(View.GONE);
        }
        if (!Strings.isNullOrEmpty(currentVideo.getVideoBannerURL())) {

            Image.displayImage(currentVideo.getVideoBannerURL(), (Activity) this.context, videoImage, 1);

        } else {
            videoImage.setImageResource(R.drawable.profile_banner);
        }
        if (!Strings.isNullOrEmpty(currentVideo.getUserPickUrl())) {

            Image.displayImage(currentVideo.getUserPickUrl(), (Activity) this.context, userImage, 0);

        } else {
            userImage.setImageResource(R.drawable.member);

        }
        userImage.setTag(currentVideo);
        userName.setTag(currentVideo);
        userImage.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View view) {

                final VideoProfile videoInfo = (VideoProfile) view.getTag();
                if (!Config.getUserId().equalsIgnoreCase(videoInfo.getUserId())) {
                    final int id = Integer.parseInt(videoInfo.getUserId());
                    if (id > 0) {

                        final OtherUserFragment fragment = new OtherUserFragment(); // object of next fragment
                        final Bundle bundle = new Bundle();
                        bundle.putString(USERID, String.valueOf(id));
                        bundle.putString(Constant.ROOT_FRAGMENT, Constant.BROWSE_PAGE);
                        fragment.setArguments(bundle);

                        BaseFragment.tabActivity.pushFragments(R.id.browsTab, fragment, Constant.OTHERS_PAGE,
                                BrowseFragment.browseFragment, Constant.BROWSE);

                    } else {
                        Alerts.showInfoOnly(NO_USER_ID_AVAILABLE_FOR_THIS_USER, BrowseAdapter.this.context);
                    }
                }

            }
        });
        userName.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View view) {

                final VideoProfile videoInfo = (VideoProfile) view.getTag();
                if (!Config.getUserId().equalsIgnoreCase(videoInfo.getUserId())) {
                    final int id = Integer.parseInt(videoInfo.getUserId());
                    if (id > 0) {
                        final OtherUserFragment fragment = new OtherUserFragment(); // object of next fragment
                        final Bundle bundle = new Bundle();
                        bundle.putString(USERID, String.valueOf(id));
                        bundle.putString(Constant.ROOT_FRAGMENT, Constant.BROWSE_PAGE);
                        fragment.setArguments(bundle);

                        BaseFragment.tabActivity.pushFragments(R.id.browsTab, fragment, Constant.OTHERS_PAGE,
                                BrowseFragment.browseFragment, Constant.BROWSE);

                    } else {
                        Alerts.showInfoOnly(NO_USER_ID_AVAILABLE_FOR_THIS_USER, BrowseAdapter.this.context);
                    }
                }

            }
        });

        return convertView;
    }
}
