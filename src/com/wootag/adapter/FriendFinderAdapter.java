/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.adapter;

import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Strings;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wootag.Constant;
import com.wootag.R;
import com.wootag.async.FollowAsyncTask;
import com.wootag.dto.FacebookUser;
import com.wootag.dto.Friend;
import com.wootag.dto.VideoDetails;
import com.wootag.facebook.FacebookException;
import com.wootag.facebook.FacebookOperationCanceledException;
import com.wootag.facebook.Session;
import com.wootag.facebook.widget.WebDialog;
import com.wootag.facebook.widget.WebDialog.OnCompleteListener;
import com.wootag.twitter.TwitterAsync;
import com.wootag.ui.Image;
import com.wootag.util.Alerts;
import com.wootag.util.AsyncResponse;
import com.wootag.util.Config;
import com.wootag.util.FollowInterface;
import com.wootag.util.InviteInterface;

public class FriendFinderAdapter extends ArrayAdapter<Friend> implements AsyncResponse, FollowInterface {

    private static final String PUBLISH_CANCELLED = "Publish cancelled.";

    private static final String ERROR_POSTING_LINK = "Error posting link.";

    private static final String INVITE_TO_FRIEND_REQUEST_FAILED = "Invite to friend request failed.";

    private static final String INVITED_SUCCESSFULLY = "Invited successfully.";

    private static final String POST_ID = "post_id";

    private static final String RECORD_TAG_SELF_PEOPLE_PLACE_PRODUCT_INSIDE_YOUR_VIDEOS_AND_SHARE = "Record, Tag - self,people, place, product inside your videos and Share.";

    private static final String TO = "to";

    private static final String PICTURE = "picture";

    private static final String LINK = "link";

    private static final String NAME = "name";

    private static final String EMPTY = "";

    private static final String WWW_WOOTAG_COM_INVITE_HTML = "www.wootag.com/invite.html";

    private static final String HTTP_WOOTAG_COM_INVITE_JPG = "http://wootag.com/invite.jpg";

    private static final String YOU = "you";

    private static final String NULL = "null";

    private static final String FOLLOW = "follow";

    private static final Logger LOG = LoggerManager.getLogger();

    public InviteInterface delegate;
    protected final Context context;
    Friend currentFollower;
    private final Fragment currentFragment;
    private final List<Friend> facebookFriendList;
    private LayoutInflater inflater;
    private final String screenType;
    protected final String socialSite;

    public FriendFinderAdapter(final Context context, final int resource, final List<Friend> objects,
            final String socialSite, final Fragment currentFragment, final String screenType) {

        super(context, resource, objects);
        this.context = context;
        this.facebookFriendList = objects;
        this.socialSite = socialSite;
        this.currentFragment = currentFragment;
        this.screenType = screenType;
    }

    @Override
    public void follow(final String type) {

        if (FOLLOW.equalsIgnoreCase(type)) {
            this.currentFollower.setIsFollow(Constant.YES);
            this.notifyDataSetChanged();
            Alerts.showInfoOnly(Constant.FOLLOWED_SUCCESSFULLY, this.context);
        }
        if (Constant.UNFOLLOW.equalsIgnoreCase(type)) {
            this.currentFollower.setIsFollow(Constant.NO);
            this.notifyDataSetChanged();
            Alerts.showInfoOnly(Constant.UNFOLLOWED_SUCCESSFULLY, this.context);
        }

    }

    @Override
    public void friendInfoProcessFinish(final FacebookUser info, final boolean friend, final String socialsite) {

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

        if (convertView == null) {
            convertView = ((LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
                    R.layout.facebook_user, parent, false);
        }

        final Friend friendsObj = this.getItem(position);
        final ImageView profImage = (ImageView) convertView.findViewById(R.id.image);
        final TextView profName = (TextView) convertView.findViewById(R.id.name);
        final TextView profuserId = (TextView) convertView.findViewById(R.id.userID);
        final ImageButton invite = (ImageButton) convertView.findViewById(R.id.invite);
        final ImageButton follow = (ImageButton) convertView.findViewById(R.id.follow);
        invite.setTag(friendsObj);
        follow.setTag(friendsObj);

        if (!NULL.equalsIgnoreCase(friendsObj.getWootagId())) {
            invite.setVisibility(View.GONE);
            if (Constant.NO.equalsIgnoreCase(friendsObj.getIsFollow())) {
                follow.setImageResource(R.drawable.add1);
            } else {
                follow.setImageResource(R.drawable.follows);
            }
            follow.setVisibility(View.VISIBLE);
        } else {
            invite.setVisibility(View.VISIBLE);
            follow.setVisibility(View.GONE);
        }

        if (YOU.equalsIgnoreCase(friendsObj.getFriendName())) {
            invite.setVisibility(View.GONE);
            follow.setVisibility(View.GONE);
        }

        follow.setOnClickListener(new OnClickListener() {

            private int id;

            @Override
            public void onClick(final View view) {

                FriendFinderAdapter.this.currentFollower = (Friend) view.getTag();
                final String userId = FriendFinderAdapter.this.currentFollower.getWootagId();
                if (userId != null) {
                    this.id = Integer.parseInt(userId);
                }
                if (this.id > 0) {
                    String follow = Constant.UNFOLLOW;

                    if (FriendFinderAdapter.this.currentFollower.getIsFollow() != null) {
                        if (Constant.NO.equalsIgnoreCase(FriendFinderAdapter.this.currentFollower.getIsFollow())) {
                            follow = Constant.UNFOLLOW;
                        }
                        final FollowAsyncTask task = new FollowAsyncTask(String.valueOf(this.id), Config.getUserId(),
                                follow, FriendFinderAdapter.this.context);
                        task.delegate = FriendFinderAdapter.this;

                        task.execute();
                    }
                } else {
                    Alerts.showInfoOnly("No Id for this user", FriendFinderAdapter.this.context);
                }

            }
        });

        profuserId.setVisibility(View.GONE);
        if (!Strings.isNullOrEmpty(friendsObj.getFriendImage())) {
            Image.displayImage(friendsObj.getFriendImage(), (Activity) this.context, profImage, 0);
        } else {
            profImage.setImageResource(R.drawable.member);
        }
        profName.setText(friendsObj.getFriendName());
        profuserId.setText(friendsObj.getLocation());
        invite.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                final Friend friend = (Friend) v.getTag();
                if (Constant.FACEBOOK.equalsIgnoreCase(FriendFinderAdapter.this.socialSite)) {
                    FriendFinderAdapter.this.publishFeedDialog(friend.getFriendId());
                } else if (Constant.TWITTER.equalsIgnoreCase(FriendFinderAdapter.this.socialSite)) {
                    final VideoDetails video = new VideoDetails();
                    video.setVideothumbPath(HTTP_WOOTAG_COM_INVITE_JPG);
                    TwitterAsync asyncTask = null;
                    asyncTask = new TwitterAsync(EMPTY, FriendFinderAdapter.this.context, friend.getFriendId(),
                            Constant.TWITTER_TWEET, WWW_WOOTAG_COM_INVITE_HTML, video, EMPTY);
                    asyncTask.delegate = FriendFinderAdapter.this;
                    asyncTask.execute();
                } else if (Constant.GOOGLE_PLUS.equalsIgnoreCase(FriendFinderAdapter.this.socialSite)) {
                    FriendFinderAdapter.this.delegate.invite(friend.getFriendId(), friend.getFriendName());
                }
            }
        });
        return convertView;
    }

    @Override
    public void processFinish(final List<Friend> output, final String socialMediasite) {

    }

    public void publishFeedDialog(final String fbId) {

        final Bundle params = new Bundle();
        params.putString(NAME, RECORD_TAG_SELF_PEOPLE_PLACE_PRODUCT_INSIDE_YOUR_VIDEOS_AND_SHARE);
        params.putString(LINK, WWW_WOOTAG_COM_INVITE_HTML);
        params.putString(PICTURE, HTTP_WOOTAG_COM_INVITE_JPG);
        params.putString(TO, fbId);
        final WebDialog feedDialog = new WebDialog.FeedDialogBuilder(this.context, Session.getActiveSession(), params)
                .setOnCompleteListener(new OnCompleteListener() {

                    @Override
                    public void onComplete(final Bundle values, final FacebookException error) {

                        if (error == null) {
                            final String postId = values.getString(POST_ID);
                            if (postId != null) {
                                Alerts.showInfoOnly(INVITED_SUCCESSFULLY, FriendFinderAdapter.this.context);
                            } else {
                                Alerts.showInfoOnly(INVITE_TO_FRIEND_REQUEST_FAILED, FriendFinderAdapter.this.context);
                            }
                        } else if (error instanceof FacebookOperationCanceledException) {
                            Alerts.showInfoOnly(PUBLISH_CANCELLED, FriendFinderAdapter.this.context);
                        } else {
                            Alerts.showInfoOnly(ERROR_POSTING_LINK, FriendFinderAdapter.this.context);
                        }
                    }

                }).build();
        feedDialog.show();
    }

}
