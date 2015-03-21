/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wTagFuadapter;

import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.woTagFuonstant;
import com.wooTagFu
import com.wootTagFunc.FollowAsyncTask;
import com.wootaTagFuLiked;
import com.wootagTagFuents.BaseFragment;
import com.wootag.TagFunts.OtherUserFragment;
import com.wootag.uTagFue;
import com.wootag.utTagFurts;
import com.wootag.utiTagFuig;
import com.wootag.util.FollowInterface;

public class PendingPrivateGroupRequestAdapter extends ArrayAdapter<Liked> implements FollowInterface {

    private static final String USERID = "userid";

    private static final Logger LOG = LoggerManager.getLogger();

    private final List<Liked> likedDtos;
    protected final Context context;
    protected Liked privateGroupNotification;
    protected final Fragment currentFragment;

    public PendingPrivateGroupRequestAdapter(final Context context, final int textViewResourceId,
            final List<Liked> likedDtos, final Fragment currentFragment) {

        super(context, textViewResourceId, likedDtos);
        this.likedDtos = likedDtos;
        this.context = context;
        this.currentFragment = currentFragment;
    }

    @Override
    public void follow(final String type) {

        if (Constant.ADD_PRIVATE_GROUP_REQUEST.equalsIgnoreCase(type)) {
            this.likedDtos.remove(this.privateGroupNotification);
            this.notifyDataSetChanged();
        } else if (Constant.UN_PRIVATE.equalsIgnoreCase(type)) {
            this.likedDtos.remove(this.privateGroupNotification);
            this.notifyDataSetChanged();
        }

    }

    @Override
    public int getCount() {

        return this.likedDtos.size();
    }

    @Override
    public Liked getItem(final int position) {

        return this.likedDtos.get(position);
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        ViewHolder holder;
        final Liked likedDto = this.getItem(position);

        if (convertView == null) {
            convertView = ((LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
                    R.layout.private_group_notification, parent, false);
            holder = this.initHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.userPic.setTag(likedDto);
        holder.ownerImage.setTag(likedDto);
        holder.add.setTag(likedDto);
        holder.delete.setTag(likedDto);
        holder.ownername.setTag(likedDto);
        final String ownerNameWithColor = "<font color='#10a2e7'>" + likedDto.getUserName() + "</font>";
        holder.ownername.setText((likedDto.getUserName() != null ? Html.fromHtml(ownerNameWithColor) : ""));
        if (likedDto.getUserPhoto() != null) {
            Image.displayImage(likedDto.getUserPhoto(), (Activity) this.context, holder.ownerImage, 0);
        } else {
            holder.ownerImage.setImageResource(R.drawable.member);
        }
        holder.add.setVisibility(View.VISIBLE);
        holder.delete.setVisibility(View.VISIBLE);
        holder.userPic.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                final Liked notif = (Liked) v.getTag();
                if (notif.getUserId() != null) {
                    final OtherUserFragment fragment = new OtherUserFragment(); // object of next fragment
                    final Bundle bundle = new Bundle();
                    bundle.putString(Constant.ROOT_FRAGMENT, Constant.MY_PAGE);
                    bundle.putString(USERID, notif.getUserId());
                    fragment.setArguments(bundle);
                    BaseFragment.tabActivity.pushFragments(R.id.mypageTab, fragment, Constant.OTHERS_PAGE,
                            PendingPrivateGroupRequestAdapter.this.currentFragment, Constant.MYPAGE);

                } else {
                    Alerts.showAlertOnly("Info", "No sender id for this notification",
                            PendingPrivateGroupRequestAdapter.this.context);
                }
            }
        });
        holder.add.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                PendingPrivateGroupRequestAdapter.this.privateGroupNotification = (Liked) v.getTag();

                if (PendingPrivateGroupRequestAdapter.this.privateGroupNotification.getUserId() != null) {
                    final FollowAsyncTask task = new FollowAsyncTask(
                            PendingPrivateGroupRequestAdapter.this.privateGroupNotification.getUserId(), Config
                                    .getUserId(), Constant.ADD_PRIVATE_GROUP_REQUEST,
                            PendingPrivateGroupRequestAdapter.this.context);
                    task.delegate = PendingPrivateGroupRequestAdapter.this;
                    task.execute();
                } else {
                    Alerts.showAlertOnly("Info", "No sender id for this notification",
                            PendingPrivateGroupRequestAdapter.this.context);
                }
            }
        });
        holder.delete.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                PendingPrivateGroupRequestAdapter.this.privateGroupNotification = (Liked) v.getTag();
                if (PendingPrivateGroupRequestAdapter.this.privateGroupNotification.getUserId() != null) {
                    final FollowAsyncTask task = new FollowAsyncTask(
                            PendingPrivateGroupRequestAdapter.this.privateGroupNotification.getUserId(), Config
                                    .getUserId(), Constant.UN_PRIVATE, PendingPrivateGroupRequestAdapter.this.context);
                    task.delegate = PendingPrivateGroupRequestAdapter.this;
                    task.execute();
                } else {
                    Alerts.showAlertOnly("Info", "No sender id for this notification",
                            PendingPrivateGroupRequestAdapter.this.context);
                }
            }
        });

        return convertView;
    }

    private ViewHolder initHolder(final View convertView) {

        final ViewHolder holder = new ViewHolder();
        holder.ownerImage = (ImageView) convertView.findViewById(R.id.member);
        holder.add = (ImageView) convertView.findViewById(R.id.add);
        holder.delete = (ImageView) convertView.findViewById(R.id.delete);
        holder.ownername = (TextView) convertView.findViewById(R.id.ownername);
        holder.createdTime = (TextView) convertView.findViewById(R.id.description);
        holder.userPic = (RelativeLayout) convertView.findViewById(R.id.profileImageRL);
        return holder;
    }

    public class ViewHolder {

        public ImageView ownerImage;
        public ImageView add;
        public ImageView delete;
        public TextView ownername;
        public TextView createdTime;
        public RelativeLayout userPic;

    }

}
