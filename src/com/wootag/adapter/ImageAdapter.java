/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wootag.R;

public class ImageAdapter extends BaseAdapter {

    private static final String DRAWABLE2 = "drawable";

    private static final String E = "e";

    private static final String EMOTICON = "emoticon";

    private static final String EMPTY = "";

    private static final Logger LOG = LoggerManager.getLogger();

    private final Context context;
    private final String[] mobileValues;

    public ImageAdapter(final Context context, final String[] mobileValues) {

        this.context = context;
        this.mobileValues = mobileValues;
        // assets = this.context.getAssets();
    }

    @Override
    public int getCount() {

        return this.mobileValues.length;
    }

    @Override
    public Object getItem(final int position) {

        return null;
    }

    @Override
    public long getItemId(final int position) {

        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        if (convertView == null) {
            convertView = ((LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
                    R.layout.emoticon_item, null);
        }

        final ImageView imageView = (ImageView) convertView.findViewById(R.id.grid_item_image);
        String emoticonName = EMPTY;
        emoticonName = EMOTICON + (position + 1) + E;
        final Drawable drawable = this.context.getResources().getDrawable(
                this.context.getResources().getIdentifier(emoticonName, DRAWABLE2, this.context.getPackageName()));
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        imageView.setImageDrawable(drawable);
        return convertView;
    }

}
