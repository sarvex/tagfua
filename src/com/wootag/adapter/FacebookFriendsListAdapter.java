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
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.common.base.Strings;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wootag.Constant;
import com.wootag.R;
import com.wootag.dto.Friend;
import com.wootag.ui.Image;
import com.wootag.util.Config;

public class FacebookFriendsListAdapter extends ArrayAdapter<Friend> {

    private static final String TAG_YOUR_PRIVATE_FOLLOWING_CONNECTION = "Tag your private/following connection";

    private static final String TAG_YOUR_CIRCLE = "Tag your circle";

    private static final String TAG_MY_FOLLOWING_CONNECTION = "Tag my following connection";

    private static final String TAG_YOUR_FRIEND = "Tag your friend";

    private static final String YOU = "You";

    private static final String TAG_YOURSELF = "Tag yourself";

    private static final Logger LOG = LoggerManager.getLogger();

    private final List<Friend> facebookFriendList;
    private final Context context;
    private LayoutInflater inflater;
    private final String socialSite;
    private final boolean fromPlayer;

    public FacebookFriendsListAdapter(final Context context, final int resource, final List<Friend> objects,
            final String socialSite, final boolean multichoice, final boolean fromPlayer) {

        super(context, resource, objects);
        this.context = context;
        this.facebookFriendList = objects;
        this.socialSite = socialSite;
        this.fromPlayer = fromPlayer;
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
            convertView = ((LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.facebook_user, parent, false);
            holder = this.initHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (this.fromPlayer) {
            if (Constant.FACEBOOK.equalsIgnoreCase(Config.getSocialSite())) {
                if ((position == 0) && ((friendsObj != null) && YOU.equalsIgnoreCase(friendsObj.getFriendName()))) {
                    holder.headerText.setText(TAG_YOURSELF);
                    holder.itemHeader.setVisibility(View.VISIBLE);
                } else if (position == 1) {
                    holder.headerText.setText(TAG_YOUR_FRIEND);
                    holder.itemHeader.setVisibility(View.VISIBLE);
                } else {
                    holder.itemHeader.setVisibility(View.GONE);
                }
            } else if (Constant.TWITTER.equalsIgnoreCase(Config.getSocialSite())) {
                if ((position == 0) && (YOU.equalsIgnoreCase(friendsObj.getFriendName()))) {
                    holder.headerText.setText(TAG_YOURSELF);
                    holder.itemHeader.setVisibility(View.VISIBLE);
                } else if (position == 1) {
                    holder.headerText.setText(TAG_MY_FOLLOWING_CONNECTION);
                    holder.itemHeader.setVisibility(View.VISIBLE);
                } else {
                    holder.itemHeader.setVisibility(View.GONE);
                }
            } else if (Constant.GOOGLE_PLUS.equalsIgnoreCase(Config.getSocialSite())) {
                if ((position == 0) && (YOU.equalsIgnoreCase(friendsObj.getFriendName()))) {
                    holder.headerText.setText(TAG_YOURSELF);
                    holder.itemHeader.setVisibility(View.VISIBLE);
                } else if (position == 1) {
                    holder.headerText.setText(TAG_YOUR_CIRCLE);
                    holder.itemHeader.setVisibility(View.VISIBLE);
                } else {
                    holder.itemHeader.setVisibility(View.GONE);
                }
            } else if (Constant.WOOTAG.equalsIgnoreCase(Config.getSocialSite())) {
                if ((position == 0) && (YOU.equalsIgnoreCase(friendsObj.getFriendName()))) {
                    holder.headerText.setText(TAG_YOURSELF);
                    holder.itemHeader.setVisibility(View.VISIBLE);
                } else if (position == 1) {
                    holder.headerText.setText(TAG_YOUR_PRIVATE_FOLLOWING_CONNECTION);
                    holder.itemHeader.setVisibility(View.VISIBLE);
                } else {
                    holder.itemHeader.setVisibility(View.GONE);
                }
            }
        } else {
            holder.itemHeader.setVisibility(View.GONE);
        }

        if (!Strings.isNullOrEmpty(friendsObj.getFriendImage())) {
            Image.displayImage(friendsObj.getFriendImage(), (Activity) this.context, holder.profImage, 0);
        } else {
            holder.profImage.setImageResource(R.drawable.member);
        }
        holder.profName.setText(friendsObj.getFriendName());
        holder.profuserId.setText(friendsObj.getLocation());

        if (!Strings.isNullOrEmpty(friendsObj.getFriendName()) && !Strings.isNullOrEmpty(friendsObj.getFriendId())) {
            if (friendsObj.isTaggedUser()) {
                holder.userview.setVisibility(View.GONE);
            } else {
                holder.userview.setVisibility(View.VISIBLE);
            }
        } else {
            holder.userview.setVisibility(View.GONE);
        }
        return convertView;
    }

    private ViewHolder initHolder(final View row) {

        final ViewHolder holder = new ViewHolder();
        holder.profImage = (ImageView) row.findViewById(R.id.image);
        holder.profName = (TextView) row.findViewById(R.id.name);
        holder.userview = (RelativeLayout) row.findViewById(R.id.userview);
        holder.selectFriendCheckbox = (CheckBox) row.findViewById(R.id.selectFriend);
        holder.profileView = (RelativeLayout) row.findViewById(R.id.profileRR);
        holder.profuserId = (TextView) row.findViewById(R.id.userID);
        holder.itemHeader = (LinearLayout) row.findViewById(R.id.fbHeader);
        holder.headerText = (TextView) row.findViewById(R.id.fbHeaderText);
        return holder;
    }

    public class ViewHolder {

        public ImageView profImage;
        public TextView profName;
        public CheckBox selectFriendCheckbox;
        public RelativeLayout userview;
        public RelativeLayout profileView;
        public TextView profuserId;
        public LinearLayout itemHeader;
        public TextView headerText;
    }

}
