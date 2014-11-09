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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.base.Strings;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wootag.Constant;
import com.wootag.R;
import com.wootag.dto.Friend;
import com.wootag.ui.Image;
import com.wootag.util.Config;

public class ShareAdapter extends BaseAdapter {

    private static final String YOU = "You";

    private static final String POST_IT_TO_MY_CIRCLE = "Post It To My Circle";

    private static final String MY_POST = "My Post";

    private static final String MY_FOLLOWING = "My Following";

    private static final String TWEET = "Tweet";

    private static final String MY_FRIENDS = "My Friends";

    private static final String MY_WALL = "My Wall";

    private static final Logger LOG = LoggerManager.getLogger();

    private final List<Friend> facebookFriendList;
    private final Context context;
    private final String socialSite;
    private final boolean search;

    public ShareAdapter(final Context context, final int resource, final List<Friend> objects, final String socialSite,
            final boolean search) {

        // super(context, resource, objects);
        this.context = context;
        this.facebookFriendList = objects;
        this.socialSite = socialSite;
        this.search = search;
    }

    @Override
    public int getCount() {

        return this.facebookFriendList.size();
    }

    @Override
    public Friend getItem(final int index) {

        return this.facebookFriendList.get(index);
    }

    @Override
    public long getItemId(final int position) {

        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        ViewHolder holder;
        final Friend friendsObj = this.getItem(position);

        if (convertView == null) {
            convertView = ((LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
                    R.layout.share_user_item, parent, false);
            holder = this.initHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (this.search) {
            holder.itemHeader.setVisibility(View.GONE);
        } else {
            if (Constant.FACEBOOK.equalsIgnoreCase(Config.getSocialSite())) {
                if ((position == 0) && (YOU.equalsIgnoreCase(friendsObj.getFriendName()))) {
                    holder.headerText.setText(MY_WALL);
                    holder.itemHeader.setVisibility(View.VISIBLE);
                } else if (position == 1) {
                    holder.headerText.setText(MY_FRIENDS);
                    holder.itemHeader.setVisibility(View.VISIBLE);
                } else {
                    holder.itemHeader.setVisibility(View.GONE);
                }
            } else if (Constant.TWITTER.equalsIgnoreCase(Config.getSocialSite())) {
                if ((position == 0) && (YOU.equalsIgnoreCase(friendsObj.getFriendName()))) {
                    holder.headerText.setText(TWEET);
                    holder.itemHeader.setVisibility(View.VISIBLE);
                } else if (position == 1) {
                    holder.headerText.setText(MY_FOLLOWING);
                    holder.itemHeader.setVisibility(View.VISIBLE);
                } else {
                    holder.itemHeader.setVisibility(View.GONE);
                }
            } else if (Constant.GOOGLE_PLUS.equalsIgnoreCase(Config.getSocialSite())) {
                if ((position == 0) && (YOU.equalsIgnoreCase(friendsObj.getFriendName()))) {
                    holder.headerText.setText(MY_POST);
                    holder.itemHeader.setVisibility(View.VISIBLE);
                } else if (position == 1) {
                    holder.headerText.setText(POST_IT_TO_MY_CIRCLE);
                    holder.itemHeader.setVisibility(View.VISIBLE);
                } else {
                    holder.itemHeader.setVisibility(View.GONE);
                }
            }
        }

        if (!Strings.isNullOrEmpty(friendsObj.getFriendImage())) {
            Image.displayImage(friendsObj.getFriendImage(), (Activity) this.context, holder.profImage, 0);
        } else {
            holder.profImage.setImageResource(R.drawable.member);
        }
        holder.profName.setText(friendsObj.getFriendName());
        holder.profuserId.setText(friendsObj.getLocation());
        return convertView;
    }

    private ViewHolder initHolder(final View convertView) {

        final ViewHolder holder = new ViewHolder();
        holder.profImage = (ImageView) convertView.findViewById(R.id.image);
        holder.profName = (TextView) convertView.findViewById(R.id.name);
        holder.profuserId = (TextView) convertView.findViewById(R.id.userID);
        holder.itemHeader = (LinearLayout) convertView.findViewById(R.id.itemHeader);
        holder.headerText = (TextView) convertView.findViewById(R.id.headerText);
        return holder;
    }

    public class ViewHolder {

        public ImageView profImage;
        public TextView profName;
        public TextView profuserId;
        public TextView headerText;
        public LinearLayout itemHeader;

    }

}
