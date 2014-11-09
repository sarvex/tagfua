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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wootag.Constant;
import com.wootag.R;
import com.wootag.async.FollowAsyncTask;
import com.wootag.dto.Friend;
import com.wootag.ui.Image;
import com.wootag.util.Alerts;
import com.wootag.util.Config;
import com.wootag.util.FollowInterface;
import com.wootag.util.MainManager;

public class FollowersListAdapter extends BaseAdapter implements FollowInterface {

    private static final String FOLLOW = "follow";

    private static final Logger LOG = LoggerManager.getLogger();

    protected final Context context;
    private final List<Friend> friendsList;
    protected ImageView currentImageview;
    protected Friend currentFollower;

    public FollowersListAdapter(final Context context, final List<Friend> friendsList) {

        this.context = context;
        this.friendsList = friendsList;

    }

    @Override
    public void follow(final String type) {

        if (FOLLOW.equalsIgnoreCase(type)) {
            this.currentFollower.setIsFollow(Constant.YES);
            this.notifyDataSetChanged();
        }
        if (Constant.UNFOLLOW.equalsIgnoreCase(type)) {
            this.currentFollower.setIsFollow(Constant.NO);
            this.notifyDataSetChanged();
        }

    }

    @Override
    public int getCount() {

        return this.friendsList.size();
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

        ViewHolder holder;

        if (convertView == null) {
            convertView = ((LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
                    R.layout.layout_friend_list_row, null);
            holder = new ViewHolder();
            holder.follow = (ImageView) convertView.findViewById(R.id.follow);
            holder.friendProfileImage = (ImageView) convertView.findViewById(R.id.friendListProfileImage);
            holder.friendProfileName = (TextView) convertView.findViewById(R.id.friendListProfileName);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Friend friend = this.friendsList.get(position);
        if (friend.getFriendImage() != null) {
            Image.displayImage(friend.getFriendImage(), (Activity) this.context, holder.friendProfileImage, 0);
        } else {
            holder.friendProfileImage.setImageResource(R.drawable.member);
        }
        if (friend.getFriendId().equalsIgnoreCase(MainManager.getInstance().getUserId())) {
            holder.follow.setVisibility(View.GONE);
        } else {
            holder.follow.setVisibility(View.VISIBLE);
        }
        holder.friendProfileName.setText(friend.getFriendName());
        holder.follow.setTag(friend);
        // holder.follow.setVisibility(View.VISIBLE);
        if (Constant.NO.equalsIgnoreCase(friend.getIsFollow())) {
            holder.follow.setImageResource(R.drawable.add1);
        } else {
            holder.follow.setImageResource(R.drawable.follows);
        }
        holder.follow.setOnClickListener(new OnClickListener() {

            private int id;

            @Override
            public void onClick(final View v) {

                FollowersListAdapter.this.currentFollower = (Friend) v.getTag();
                final ImageView imageView = (ImageView) v;
                FollowersListAdapter.this.currentImageview = imageView;
                final String userId = FollowersListAdapter.this.currentFollower.getFriendId();
                if (userId != null) {
                    this.id = Integer.parseInt(userId);
                }
                if (this.id > 0) {
                    if (FollowersListAdapter.this.currentFollower.getIsFollow() != null) {
                        String follow = Constant.UNFOLLOW;
                        if (Constant.NO.equalsIgnoreCase(FollowersListAdapter.this.currentFollower.getIsFollow())) {
                            follow = FOLLOW;
                        }

                        final FollowAsyncTask task = new FollowAsyncTask(String.valueOf(this.id), Config.getUserId(),
                                follow, FollowersListAdapter.this.context);
                        task.delegate = FollowersListAdapter.this;

                        task.execute();
                    }
                } else {
                    Alerts.showInfoOnly("No Id for this user", FollowersListAdapter.this.context);
                }

            }
        });

        return convertView;
    }

    static class ViewHolder {

        ImageView friendProfileImage;
        ImageView follow;
        TextView friendProfileName;
    }

}
