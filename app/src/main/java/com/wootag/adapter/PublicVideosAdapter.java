/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.TagFu.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.google.common.base.Strings;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.TagFu.R;
import com.TagFu.dto.VideoProfile;
import com.TagFu.ui.Image;

public class PublicVideosAdapter extends BaseAdapter {

    private static final Logger LOG = LoggerManager.getLogger();

    private final Context context;
    private final List<VideoProfile> videoList;

    public PublicVideosAdapter(final Context context, final List<VideoProfile> list) {

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
                    R.layout.public_video_item, parent, false);
        }
        final ImageView videoImage = (ImageView) convertView.findViewById(R.id.publicvideothumb);
        // ImageButton play=(ImageButton)row.findViewById(R.id.playbutton);
        final VideoProfile currentVideo = this.videoList.get(position);
        if (!Strings.isNullOrEmpty(currentVideo.getVideoBannerURL())) {
            Image.displayImage(currentVideo.getVideoBannerURL(), (Activity) this.context, videoImage, 1);
        } else {
            videoImage.setImageResource(R.drawable.profile_banner);
        }

        return convertView;
    }

}
