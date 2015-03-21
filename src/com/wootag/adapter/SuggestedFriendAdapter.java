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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Strings;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.woTagFu;
import com.wooTagFuo.SuggestedUsersDto;
import com.wootag.ui.Image;

public class SuggestedFriendAdapter extends ArrayAdapter<SuggestedUsersDto> {

    private static final Logger LOG = LoggerManager.getLogger();

    private final List<SuggestedUsersDto> people;
    private final Context context;

    public SuggestedFriendAdapter(final Context context, final int textViewResourceId,
            final List<SuggestedUsersDto> peopleList) {

        super(context, textViewResourceId, peopleList);
        this.people = peopleList;
        this.context = context;
    }

    private static String getDescription(final SuggestedUsersDto suggestedUsers) {

        String description = "";

        if (suggestedUsers.getProfession() != null) {
            description += suggestedUsers.getProfession() + " | ";
        }
        if (suggestedUsers.getCountry() != null) {
            description += suggestedUsers.getCountry() + " | ";
        }
        if (suggestedUsers.getWebsite() != null) {
            description += suggestedUsers.getWebsite();
        }
        return description;
    }

    @Override
    public int getCount() {

        return this.people.size();
    }

    @Override
    public SuggestedUsersDto getItem(final int position) {

        return this.people.get(position);
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        ViewHolder holder;
        final SuggestedUsersDto suggestedUsers = this.getItem(position);

        if (convertView == null) {
            convertView = ((LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.suggested_friends, parent, false);
            holder = this.initHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (!Strings.isNullOrEmpty(suggestedUsers.getPhotoPath())) {

            Image
                    .displayImage(suggestedUsers.getPhotoPath(), (Activity) this.context, holder.profileImageView, 0);
        } else {
            holder.profileImageView.setImageResource(R.drawable.member);
        }
        if (suggestedUsers.getName() != null) {
            holder.nameTextView.setText(suggestedUsers.getName());
        } else {
            holder.nameTextView.setText("");
        }
        holder.profileDetailsTextView.setText(SuggestedFriendAdapter.getDescription(suggestedUsers));

        return convertView;
    }

    private ViewHolder initHolder(final View convertView) {

        final ViewHolder holder = new ViewHolder();
        holder.profileImageView = (ImageView) convertView.findViewById(R.id.profileImageView);
        holder.addImageView = (ImageView) convertView.findViewById(R.id.addImageView);
        holder.nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
        holder.profileDetailsTextView = (TextView) convertView.findViewById(R.id.profileDetailsTextView);
        return holder;
    }

    public class ViewHolder {

        public ImageView profileImageView;
        public ImageView addImageView;
        public TextView nameTextView;
        public TextView profileDetailsTextView;

    }

}
