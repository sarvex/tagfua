/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.TagFu.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.TagFu.R;
import com.TagFu.util.CropOption;

/**
 * Adapter for crop option list.
 *
 * @author sarvex
 */
public class CropOptionAdapter extends ArrayAdapter<CropOption> {

    private static final Logger LOG = LoggerManager.getLogger();

    private final List<CropOption> mOptions;
    private final LayoutInflater mInflater;

    public CropOptionAdapter(final Context context, final List<CropOption> options) {

        super(context, R.layout.crop_selector, options);

        this.mOptions = options;

        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup group) {

        if (convertView == null) {
            convertView = this.mInflater.inflate(R.layout.crop_selector, null);
        }

        final CropOption item = this.mOptions.get(position);

        if (item != null) {
            ((ImageView) convertView.findViewById(R.id.iv_icon)).setImageDrawable(item.icon);
            ((TextView) convertView.findViewById(R.id.tv_name)).setText(item.title);

            return convertView;
        }

        return null;
    }
}
