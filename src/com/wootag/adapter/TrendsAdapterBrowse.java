/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.adapter;

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

import com.wootag.R;
import com.wootag.dto.Trends;
import com.wootag.ui.Image;

public class TrendsAdapterBrowse extends BaseAdapter {

    private static final Logger LOG = LoggerManager.getLogger();

    private final Context context;
    private final List<Trends> trendList;

    public TrendsAdapterBrowse(final Context context, final List<Trends> list) {

        this.context = context;
        this.trendList = list;
    }

    @Override
    public int getCount() {

        return this.trendList.size();
    }

    @Override
    public Object getItem(final int position) {

        return this.trendList.get(position);
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
        final TextView taginf = (TextView) convertView.findViewById(R.id.taginf);
        final Trends currentVideo = this.trendList.get(position);

        if (currentVideo.getTagName() != null) {
            taginf.setText(currentVideo.getTagName());
        } else {
            taginf.setText("");
        }
        // tagImage.setVisibility(View.GONE);
        if (currentVideo.getVideoThumbPath() != null) {

            Image.displayImage(currentVideo.getVideoThumbPath(), (Activity) this.context, videoImage, 1);
        } else {
            videoImage.setImageResource(R.drawable.member);
        }

        return convertView;
    }
}
