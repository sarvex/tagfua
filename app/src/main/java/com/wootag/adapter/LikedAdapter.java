/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.TagFu.adapter;

import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.TagFu.Constant;
import com.TagFu.OtherUserActivity;
import com.TagFu.R;
import com.TagFu.dto.Liked;
import com.TagFu.fragments.BaseFragment;
import com.TagFu.fragments.OtherUserFragment;
import com.TagFu.ui.Image;
import com.TagFu.util.Alerts;
import com.TagFu.util.Config;

public class LikedAdapter extends ArrayAdapter<Liked> {

    private static final String NO_ID_FOR_THIS_USER = "No Id for this user";

    private static final String USERID = "userid";

    private static final Logger LOG = LoggerManager.getLogger();

    private final List<Liked> likedDtos;
    protected final Context context;
    private final String screenType;
    protected final Fragment currentFragment;

    public LikedAdapter(final Context context, final int textViewResourceId, final List<Liked> likedDtos,
            final Fragment currentFragment, final String screenType) {

        super(context, textViewResourceId, likedDtos);
        this.likedDtos = likedDtos;
        this.context = context;
        this.screenType = screenType;
        this.currentFragment = currentFragment;
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
                    R.layout.loved_item, parent, false);
            holder = this.initHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.userName.setText((likedDto.getUserName() != null ? likedDto.getUserName() : ""));
        if (likedDto.getUserPhoto() != null) {
            Image.displayImage(likedDto.getUserPhoto(), (Activity) this.context, holder.userImage, 0);
        } else {
            holder.userImage.setImageResource(R.drawable.member);
        }
        holder.userImage.setTag(likedDto);
        holder.userName.setTag(likedDto);
        holder.userImage.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                final Liked peopleDetails = (Liked) v.getTag();

                final String Id = peopleDetails.getUserId();
                if (!Config.getUserId().equalsIgnoreCase(Id)) {
                    int id = 0;
                    id = Integer.parseInt(Id);
                    if (id > 0) {
                        if (LikedAdapter.this.currentFragment != null) {
                            LikedAdapter.this.gotToOtherPage(id);
                        } else {
                            final Intent secondUserIntent = new Intent(LikedAdapter.this.context,
                                    OtherUserActivity.class);
                            secondUserIntent.putExtra(USERID, id);
                            LikedAdapter.this.context.startActivity(secondUserIntent);
                        }
                    } else {
                        Alerts.showInfoOnly(NO_ID_FOR_THIS_USER, LikedAdapter.this.context);
                    }
                }
            }
        });
        holder.userName.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                final Liked peopleDetails = (Liked) v.getTag();

                final String Id = peopleDetails.getUserId();
                if (!Config.getUserId().equalsIgnoreCase(Id)) {
                    int id = 0;
                    if (Id != null) {
                        id = Integer.parseInt(Id);
                    }
                    if (id > 0) {
                        if (LikedAdapter.this.currentFragment != null) {
                            LikedAdapter.this.gotToOtherPage(id);
                        } else {
                            final Intent secondUserIntent = new Intent(LikedAdapter.this.context,
                                    OtherUserActivity.class);
                            secondUserIntent.putExtra(USERID, id);
                            LikedAdapter.this.context.startActivity(secondUserIntent);
                        }
                    } else {
                        Alerts.showInfoOnly(NO_ID_FOR_THIS_USER, LikedAdapter.this.context);
                    }
                }
            }
        });

        return convertView;
    }

    private ViewHolder initHolder(final View convertView) {

        final ViewHolder holder = new ViewHolder();
        holder.userImage = (ImageView) convertView.findViewById(R.id.profileImageView);
        holder.userName = (TextView) convertView.findViewById(R.id.nameTextView);
        return holder;
    }

    void gotToOtherPage(final int id) {

        final OtherUserFragment fragment = new OtherUserFragment(); // object of next fragment
        final Bundle bundle = new Bundle();
        if (Constant.MY_PAGE.equalsIgnoreCase(this.screenType)) {
            bundle.putString(Constant.ROOT_FRAGMENT, Constant.MY_PAGE);
            bundle.putString(USERID, String.valueOf(id));
            fragment.setArguments(bundle);
            BaseFragment.tabActivity.pushFragments(R.id.mypageTab, fragment, Constant.OTHERS_PAGE,
                    this.currentFragment, Constant.MYPAGE);
        } else if (Constant.MY_PAGE_MORE_FEEDS.equalsIgnoreCase(this.screenType)) {
            bundle.putString(Constant.ROOT_FRAGMENT, Constant.MY_PAGE);
            bundle.putString(USERID, String.valueOf(id));
            fragment.setArguments(bundle);
            BaseFragment.tabActivity.pushFragments(R.id.mypageTab, fragment, Constant.OTHERS_PAGE,
                    this.currentFragment, Constant.MYPAGE);
        } else if (Constant.MORE_VIDEOS.equalsIgnoreCase(this.screenType)) {
            bundle.putString(Constant.ROOT_FRAGMENT, Constant.MY_PAGE);
            bundle.putString(USERID, String.valueOf(id));
            fragment.setArguments(bundle);
            BaseFragment.tabActivity.pushFragments(R.id.mypageTab, fragment, Constant.OTHERS_PAGE,
                    this.currentFragment, Constant.MYPAGE);
        } else if (Constant.VIDEO_FEEDS.equalsIgnoreCase(this.screenType)) {
            bundle.putString(Constant.ROOT_FRAGMENT, Constant.VIDEO_FEEDS);
            bundle.putString(USERID, String.valueOf(id));
            fragment.setArguments(bundle);
            BaseFragment.tabActivity.pushFragments(R.id.feedTab, fragment, Constant.OTHERS_PAGE, this.currentFragment,
                    Constant.HOME);
        } else if (Constant.BROWSE_PAGE.equalsIgnoreCase(this.screenType)) {
            bundle.putString(Constant.ROOT_FRAGMENT, Constant.BROWSE_PAGE);
            bundle.putString(USERID, String.valueOf(id));
            fragment.setArguments(bundle);
            BaseFragment.tabActivity.pushFragments(R.id.browsTab, fragment, Constant.OTHERS_PAGE, this.currentFragment,
                    Constant.BROWSE);
        } else if (Constant.NOTIFICATIONS_PAGE.equalsIgnoreCase(this.screenType)) {
            bundle.putString(Constant.ROOT_FRAGMENT, Constant.NOTIFICATIONS_PAGE);
            bundle.putString(USERID, String.valueOf(id));
            fragment.setArguments(bundle);
            BaseFragment.tabActivity.pushFragments(R.id.notificationTab, fragment, Constant.OTHERS_PAGE,
                    this.currentFragment, Constant.NOTIFICATIONS);
        }
    }

    public class ViewHolder {

        public ImageView userImage;
        public TextView userName;

    }
}
