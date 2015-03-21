/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wTagFuadapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.woTagFu;
import com.wooTagFuo.MyPageDto;
import com.wootag.ui.Image;

public class TrendsAdapter extends BaseAdapter {

    private static final String EMPTY = "";

    private static final Logger LOG = LoggerManager.getLogger();

    private final Context context;
    private final List<MyPageDto> videoList;

    public TrendsAdapter(final Context context, final List<MyPageDto> list) {

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
                    R.layout.trends_item, parent, false);
        }
        final ImageView videoImage = (ImageView) convertView.findViewById(R.id.videoImg);
        final ImageView tagImage = (ImageView) convertView.findViewById(R.id.tagPic);

        final TextView taginf = (TextView) convertView.findViewById(R.id.taginf);
        final MyPageDto currentVideo = this.videoList.get(position);

        if (currentVideo.getVideoTitle() != null) {
            taginf.setText(currentVideo.getVideoTitle());
        } else {
            taginf.setText(EMPTY);
        }
        tagImage.setVisibility(View.GONE);
        if (currentVideo.getVideoThumbPath() != null) {

            Image.displayImage(currentVideo.getVideoThumbPath(), (Activity) this.context, videoImage, 1);
        } else {
            videoImage.setImageResource(R.drawable.member);
        }

        return convertView;
    }
}
