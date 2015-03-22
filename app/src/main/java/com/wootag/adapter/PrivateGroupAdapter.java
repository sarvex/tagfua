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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.TagFu.Constant;
import com.TagFu.R;
import com.TagFu.async.FollowAsyncTask;
import com.TagFu.dto.Friend;
import com.TagFu.ui.Image;
import com.TagFu.util.Alerts;
import com.TagFu.util.Config;
import com.TagFu.util.FollowInterface;

public class PrivateGroupAdapter extends BaseAdapter implements FollowInterface {

    private static final String NO = "no";
    private static final String UNFOLLOW = "unfollow";
    private static final String YES = "yes";
    private static final String FOLLOW = "follow";
    private static final Logger LOG = LoggerManager.getLogger();

    protected final Context context;
    private final List<Friend> friendsList;
    private final String type;
    ImageView currentImageview;
    Friend currentFollower;

    public PrivateGroupAdapter(final Context context, final List<Friend> friendsList, final String type,
            final boolean editMode) {

        this.context = context;
        this.friendsList = friendsList;
        this.type = type;

    }

    @Override
    public void follow(final String type) {

        if (FOLLOW.equalsIgnoreCase(type)) {
            this.currentFollower.setIsFollow(YES);
            this.notifyDataSetChanged();
        } else if (UNFOLLOW.equalsIgnoreCase(type)) {
            this.currentFollower.setIsFollow(NO);
            this.notifyDataSetChanged();
        } else if (Constant.UN_PRIVATE.equalsIgnoreCase(type)) {
            this.friendsList.remove(this.currentFollower);
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
                    R.layout.private_group_item, null);
            holder = new ViewHolder();
            holder.unshare = (ImageView) convertView.findViewById(R.id.follow);
            holder.editShare = (ImageView) convertView.findViewById(R.id.editshare);
            holder.friendProfileImage = (ImageView) convertView.findViewById(R.id.friendListProfileImage);
            holder.friendProfileName = (TextView) convertView.findViewById(R.id.friendListProfileName);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Friend friend = this.friendsList.get(position);
        // imageLoader.DisplayImage(friend.getFriendImage(),holder.friendProfileImage,50,50);

        if (Config.isPrivateGroupEditMode()) {
            holder.editShare.setVisibility(View.VISIBLE);
            if (friend.isEditMode()) {
                holder.editShare.setImageResource(R.drawable.check);
                holder.unshare.setVisibility(View.VISIBLE);

            } else {
                holder.editShare.setImageResource(R.drawable.uncheck);
                holder.unshare.setVisibility(View.GONE);
            }
        } else {
            holder.editShare.setVisibility(View.GONE);
            holder.unshare.setVisibility(View.GONE);
        }
        holder.friendProfileName.setText(friend.getFriendName());
        holder.editShare.setTag(friend);
        holder.unshare.setTag(friend);
        if (friend.getFriendImage() != null) {
            Image.displayImage(friend.getFriendImage(), (Activity) this.context, holder.friendProfileImage, 0);
        } else {
            holder.friendProfileImage.setImageResource(R.drawable.member);
        }

        holder.editShare.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                final Friend friend = (Friend) v.getTag();
                // ImageView imageView = (ImageView) v;
                if (friend.isEditMode()) {
                    friend.setEditMode(false);
                    PrivateGroupAdapter.this.notifyDataSetChanged();
                } else {
                    friend.setEditMode(true);
                    // imageView.setImageResource(R.drawable.check);
                    PrivateGroupAdapter.this.notifyDataSetChanged();
                }

            }
        });

        holder.unshare.setOnClickListener(new OnClickListener() {

            private int id;

            @Override
            public void onClick(final View v) {

                PrivateGroupAdapter.this.currentFollower = (Friend) v.getTag();
                final ImageView imageView = (ImageView) v;
                PrivateGroupAdapter.this.currentImageview = imageView;
                final String userId = PrivateGroupAdapter.this.currentFollower.getFriendId();
                if (userId != null) {
                    this.id = Integer.parseInt(userId);
                }
                if (this.id > 0) {
                    final FollowAsyncTask task = new FollowAsyncTask(userId, Config.getUserId(), Constant.UN_PRIVATE,
                            PrivateGroupAdapter.this.context);
                    task.delegate = PrivateGroupAdapter.this;
                    task.execute();
                } else {
                    Alerts.showAlertOnly("Info", "No Id for this user", PrivateGroupAdapter.this.context);
                }

            }
        });

        return convertView;
    }

    static class ViewHolder {

        ImageView friendProfileImage;
        ImageView unshare;
        ImageView editShare;
        TextView friendProfileName;
    }

}
