/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.TagFu.adapter;

import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.SpannableString;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Strings;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.TagFu.AccessPermissionActivity;
import com.TagFu.Constant;
import com.TagFu.LikedActivity;
import com.TagFu.OtherUserActivity;
import com.TagFu.ProfilePickViewActivity;
import com.TagFu.R;
import com.TagFu.ReportActivity;
import com.TagFu.SeeAllCommentsActivity;
import com.TagFu.ShareActivity;
import com.TagFu.WebViewActivity;
import com.TagFu.async.FollowAsyncTask;
import com.TagFu.async.PlaybackAsync;
import com.TagFu.async.VideoAsyncTask;
import com.TagFu.dto.Comment;
import com.TagFu.dto.MyPage;
import com.TagFu.dto.MyPageDto;
import com.TagFu.dto.RecentLikes;
import com.TagFu.dto.VideoDetails;
import com.TagFu.fragments.BaseFragment;
import com.TagFu.fragments.LikedFragment;
import com.TagFu.fragments.OtherUserFragment;
import com.TagFu.fragments.UsersListFragment;
import com.TagFu.ui.CustomDialog;
import com.TagFu.ui.Image;
import com.TagFu.ui.RoundedImageView;
import com.TagFu.util.Alerts;
import com.TagFu.util.Config;
import com.TagFu.util.FollowInterface;
import com.TagFu.util.MainManager;
import com.TagFu.util.Util;
import com.TagFu.util.VideoActionInterface;

public class OtherUserAdapter extends ArrayAdapter<MyPageDto> implements OnClickListener, VideoActionInterface,
        FollowInterface {

    private static final String FOLLOW = "follow";
    private static final Logger LOG = LoggerManager.getLogger();

    public TextView commentCount;
    public TextView dateTextView;
    public TextView loveCount;
    public LinearLayout loveDetailsLL;
    public ImageView playImageButton;
    public ImageView postCommentImageView;
    public ImageView postLovedImageView;
    public ImageView postTagsImageView;
    public ImageView postThumbnail;
    public TextView tagCount;
    public TextView videoTitleTextView;
    public TextView viewsTextView;
    private TextView bioText;
    private ImageView bioviewDot;
    private LinearLayout commentDetailsLL;
    private LinearLayout commentLL;
    protected final Context context;
    private final Fragment currentFragment;
    protected int currentVideoId;
    private ImageButton deleteImageButton;
    private LinearLayout dots;
    private ImageView editImageView;
    private TextView firstComment;
    private TextView firstName;
    private LinearLayout firstTableRow;
    private LinearLayout followerLL;
    private TextView followersCountTextView;
    private TextView followingCountTextView;
    private ImageView followingImageView;
    private LinearLayout followingLL;
    private final TextView heading;
    protected int introScrren = 1;
    private TextView lastUpdateTextView;
    private MyPageDto likeDto;
    private LinearLayout likeLL;
    private RoundedImageView myProfileImageView;
    private ImageView optionLikeImageView;
    private ImageView optionsComment;
    private ImageButton optionsDropDown;
    private ImageView optionsLike;
    protected String otherUserId;
    private TextView privateGroupCountTextView;
    private ImageView privateGroupImageView;
    private LinearLayout privateGroupLL;
    private RelativeLayout profile;
    private ImageView profileBanner;
    private final MyPage profileDetails;
    private TextView profileDetailsTextView;
    private RoundedImageView profileImage;
    private TextView profileNameTextView;
    protected String profilePicUrl;
    private RelativeLayout profileView;
    private ImageView profileviewdot;
    private final String screenType;
    private TextView secondComment;
    private TextView secondName;
    private LinearLayout secondTableRow;
    private RelativeLayout settingLayout;
    private LinearLayout statusLL;
    private TextView tagCountTextView;
    private LinearLayout tagLL;
    private MyPage user;
    private LinearLayout userDetailsLL;
    private TextView userNameTextView;
    private TextView videoCountTextView;
    private LinearLayout videoDetails;
    private final List<MyPageDto> videoInfos;

    public OtherUserAdapter(final Context context, final int textViewResourceId, final List<MyPageDto> videoInfos,
            final String screenType, final MyPage profileDetails, final TextView heading,
            final Fragment currentFragment, final String fromPage) {

        super(context, textViewResourceId, videoInfos);
        this.videoInfos = videoInfos;
        this.screenType = fromPage;
        this.context = context;
        this.heading = heading;
        this.currentFragment = currentFragment;
        this.profileDetails = profileDetails;
    }

    private static JSONObject getVedioLikeJsonReq(final String videoId) throws JSONException {

        final JSONObject json = new JSONObject();
        json.put(Constant.VIDEOID, videoId);
        json.put(Constant.USERID, Config.getUserId());
        return json;

    }

    protected static JSONObject getVideoDeleteJsonReq(final String videoId, final String userid) throws JSONException {

        final JSONObject json = new JSONObject();
        json.put(Constant.VIDEOID, videoId);
        json.put(Constant.USERID, userid);
        return json;
    }

    @Override
    public void follow(final String type) {

        if (FOLLOW.equalsIgnoreCase(type)) {
            this.followingImageView.setImageResource(R.drawable.following);
            this.notifyDataSetChanged();
            this.user.setIsFollow(Constant.YES);

        } else if (Constant.UNFOLLOW.equalsIgnoreCase(type)) {
            this.followingImageView.setImageResource(R.drawable.follow);
            this.notifyDataSetChanged();
            this.user.setIsFollow(Constant.NO);

        } else if (Constant.PRIVATE_GROUP_REQUEST.equalsIgnoreCase(type)) {
            this.privateGroupImageView.setImageResource(R.drawable.sentpvtreq);
            this.profileDetails.setIsPrivateReqSent(Constant.YES);
            this.notifyDataSetChanged();
            this.user.setIsAddToPrivateGroup(Constant.YES);

        } else if (Constant.UN_PRIVATE.equalsIgnoreCase(type)) {
            this.privateGroupImageView.setImageResource(R.drawable.addtoprivate);
            this.notifyDataSetChanged();
            this.user.setIsAddToPrivateGroup(Constant.NO);

        } else if (Constant.ADD_PRIVATE_GROUP_REQUEST.equalsIgnoreCase(type)) {
            this.privateGroupImageView.setImageResource(R.drawable.addedtoprivate);
            this.notifyDataSetChanged();
            this.user.setIsAddToPrivateGroup(Constant.YES);
            this.user.setIsPrivateReqSent(Constant.NO);
            this.user.setIsRespToPvtReq(Constant.NO);
        }
    }

    @Override
    public int getCount() {

        return this.videoInfos.size();
    }

    @Override
    public MyPageDto getItem(final int position) {

        return this.videoInfos.get(position);
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        final MyPageDto videoInfo = this.getItem(position);
        if (convertView == null) {
            convertView = ((LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.other_user_item, parent, false);
        }

        this.initHolder(convertView);
        this.custonizeViewOnScreenType(videoInfo);
        this.setView(videoInfo, position, convertView);

        return convertView;
    }

    @Override
    public void onClick(final View view) {

        switch (view.getId()) {
        case R.id.commentImageView:
            final MyPageDto CommentInfo = (MyPageDto) view.getTag();
            if (CommentInfo.getVideoId() != null) {
                final int videoId = Integer.parseInt(CommentInfo.getVideoId());
                if (videoId > 0) {

                    final Intent seeAllComments = new Intent(this.context, SeeAllCommentsActivity.class);
                    seeAllComments.putExtra(Constant.VIDEOID, CommentInfo.getVideoId());
                    seeAllComments.putExtra(Constant.USERID, CommentInfo.getUserId());
                    this.context.startActivity(seeAllComments);
                } else {
                    Alerts.showInfoOnly(Constant.INVALID_VIDEO_ID, this.context);
                }
            }
            break;

        case R.id.optionsButton:
            final MyPageDto options = (MyPageDto) view.getTag();
            this.showOptionDialogs(options);

            // Alerts.ShowAlertOnly("Info", "Not implemented",
            // context);
            break;

        case R.id.likeImageView:
            this.likeDto = (MyPageDto) view.getTag();
            this.optionLikeImageView = (ImageView) view;
            if (this.likeDto.getVideoId() != null) {
                final int videoId = Integer.parseInt(this.likeDto.getVideoId());
                if (videoId > 0) {

                    if (!this.likeDto.hasLiked()) {
                        try {
                            final VideoAsyncTask asyncTask = new VideoAsyncTask(this.context, Constant.LIKE,
                                    OtherUserAdapter.getVedioLikeJsonReq(String.valueOf(videoId)), false);
                            asyncTask.delegate = OtherUserAdapter.this;
                            asyncTask.execute();
                        } catch (final JSONException exception) {
                            LOG.e(exception);
                        }
                    } else {
                        try {
                            final VideoAsyncTask asyncTask = new VideoAsyncTask(this.context, Constant.DISLIKE,
                                    OtherUserAdapter.getVedioLikeJsonReq(String.valueOf(videoId)), false);
                            asyncTask.delegate = OtherUserAdapter.this;
                            asyncTask.execute();
                        } catch (final JSONException exception) {
                            LOG.e(exception);
                        }
                    }

                } else {
                    Alerts.showInfoOnly(Constant.INVALID_VIDEO_ID, this.context);
                }
            }
            break;

        case R.id.followingImgView:
            this.user = this.profileDetails;// (MyPage) followingStatusLL.getTag();

            if (this.user != null) {
                if (Constant.NO.equalsIgnoreCase(this.user.getIsFollow())) {
                    final FollowAsyncTask task = new FollowAsyncTask(this.user.getUserid(), Config.getUserId(), FOLLOW,
                            this.context);
                    task.delegate = OtherUserAdapter.this;
                    task.execute();
                } else {
                    final FollowAsyncTask task = new FollowAsyncTask(this.user.getUserid(), Config.getUserId(),
                            Constant.UNFOLLOW, this.context);
                    task.delegate = OtherUserAdapter.this;
                    task.execute();
                }
            }
            break;
        case R.id.privateGroupImageView:
            this.user = this.profileDetails;
            if (this.user != null) {
                if (Constant.YES.equalsIgnoreCase(this.profileDetails.getIsRespToPvtReq())) {
                    final FollowAsyncTask task = new FollowAsyncTask(this.user.getUserid(), Config.getUserId(),
                            Constant.ADD_PRIVATE_GROUP_REQUEST, this.context);
                    task.delegate = OtherUserAdapter.this;
                    task.execute();
                } else if (Constant.YES.equalsIgnoreCase(this.user.getIsPrivateReqSent())) {
                    Alerts.showInfoOnly(Constant.PENDING_STATE, this.context);
                } else {
                    if (Constant.NO.equalsIgnoreCase(this.user.getIsAddToPrivateGroup())) {
                        final FollowAsyncTask task = new FollowAsyncTask(this.user.getUserid(), Config.getUserId(),
                                Constant.PRIVATE_GROUP_REQUEST, this.context);
                        task.delegate = OtherUserAdapter.this;
                        task.execute();
                    } else {
                        final FollowAsyncTask task = new FollowAsyncTask(this.user.getUserid(), Config.getUserId(),
                                Constant.UN_PRIVATE, this.context);
                        task.delegate = OtherUserAdapter.this;
                        task.execute();
                    }
                }
            }
            break;
        case R.id.playButtonImage:
            final MyPageDto videoInfo = (MyPageDto) view.getTag();
            this.currentVideoId = Integer.parseInt(videoInfo.getVideoId());
            new PlaybackAsync(this.context, videoInfo.getVideoId()).execute();
            break;

        case R.id.commentLL:
            final MyPageDto myPageDto = (MyPageDto) view.getTag();
            final Intent seeAllComments = new Intent(this.context, SeeAllCommentsActivity.class);
            seeAllComments.putExtra(Constant.VIDEOID, myPageDto.getVideoId());
            seeAllComments.putExtra(Constant.USERID, myPageDto.getUserId());

            this.context.startActivity(seeAllComments);
            break;

        case R.id.loveLL:
            final MyPageDto myPageDto2 = (MyPageDto) view.getTag();
            final int videoId = Integer.parseInt(myPageDto2.getVideoId());
            if (videoId > 0) {
                this.gotoLikePage(String.valueOf(videoId), myPageDto2.getNumberOfLikes());
            } else {
                Alerts.showInfoOnly(Constant.INVALID_VIDEO_ID, this.context);
            }
            break;

        case R.id.postTagsImageView:
            // MyPageDto myPageDto3 = (MyPageDto) v.getTag();
            break;

        case R.id.firstUserName:
            final Comment firstCommentDto = (Comment) view.getTag();
            if (firstCommentDto.getUserId() != null) {

                if (this.currentFragment != null) {
                    final int id = Integer.parseInt(firstCommentDto.getUserId());
                    if (id > 0) {
                        this.gotToOtherPage(id);
                    } else {
                        Alerts.showInfoOnly(Constant.NO_USER_ID_AVAILABLE_FOR_THIS_USER, this.context);
                    }
                }

                // Intent otherUserIntent = new Intent(context,
                // OtherUserActivity.class);
                // otherUserIntent.putExtra("userid",
                // firstCommentDto.getUserId());
                // context.startActivity(otherUserIntent);
            } else {
                Alerts.showInfoOnly(Constant.NO_USER_ID, this.context);
            }
            break;

        case R.id.secondUserName:
            final Comment secondCommentDto = (Comment) view.getTag();
            if (secondCommentDto.getUserId() != null) {
                final int id = Integer.parseInt(secondCommentDto.getUserId());
                if (id > 0) {
                    this.gotToOtherPage(id);
                } else {
                    Alerts.showInfoOnly(Constant.NO_USER_ID_AVAILABLE_FOR_THIS_USER, this.context);
                }

            } else {
                Alerts.showInfoOnly(Constant.NO_USER_ID, this.context);
            }
            break;
        case R.id.deleteImageButton:
            final MyPageDto video = (MyPageDto) view.getTag();
            this.currentVideoId = Integer.parseInt(video.getVideoId());
            if (this.currentVideoId > 0) {
                try {
                    final VideoAsyncTask task = new VideoAsyncTask(this.context, Constant.DELETE,
                            OtherUserAdapter.getVideoDeleteJsonReq(video.getVideoId(), Config.getUserId()), true);// video.getUserId()
                    task.delegate = OtherUserAdapter.this;
                    task.execute();
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }
            } else {
                Alerts.showInfoOnly(Constant.NO_USER_ID, this.context);
            }
            break;

        default:
            break;
        }

    }

    @Override
    public void processDone(final boolean status, final String action) {

        if (Constant.DELETE.equalsIgnoreCase(action)) {
            for (int i = 0; i < this.videoInfos.size(); i++) {
                final MyPageDto video = this.videoInfos.get(i);
                final int videoId = Integer.parseInt(video.getVideoId().trim());
                if (videoId == this.currentVideoId) {
                    this.videoInfos.remove(i);
                    this.notifyDataSetChanged();
                    break;
                }
            }
            Alerts.showInfoOnly(Constant.DELETED_SUCCESSFULLY, this.context);
        } else if (Constant.LIKE.equalsIgnoreCase(action)) {
            this.likeDto.setLiked(true);
            this.optionLikeImageView.setImageResource(R.drawable.loved_new_f);
        } else if (Constant.DISLIKE.equalsIgnoreCase(action)) {
            this.likeDto.setLiked(false);
            this.optionLikeImageView.setImageResource(R.drawable.loved_new);
        }
    }

    private void custonizeViewOnScreenType(final MyPageDto videoInfo) {

        if (this.screenType == Constant.VIDEO_FEEDS) {
            this.userDetailsLL.setVisibility(View.VISIBLE);
            this.profileImage.setTag(videoInfo);
            this.profileImage.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View v) {

                    final MyPageDto videoInfo = (MyPageDto) v.getTag();
                    if (!Config.getUserId().equalsIgnoreCase(videoInfo.getUserId())) {
                        final int id = Integer.parseInt(videoInfo.getUserId());
                        if (id > 0) {
                            final Intent secondUserIntent = new Intent(OtherUserAdapter.this.context,
                                    OtherUserActivity.class);
                            secondUserIntent.putExtra(Constant.USERID, id);
                            OtherUserAdapter.this.context.startActivity(secondUserIntent);
                        } else {
                            Alerts.showInfoOnly(Constant.NO_USER_ID_AVAILABLE_FOR_THIS_USER,
                                    OtherUserAdapter.this.context);
                        }
                    }
                }
            });
            if (!Strings.isNullOrEmpty(videoInfo.getUserPickUrl())) {
                Image.displayImage(videoInfo.getUserPickUrl(), (Activity) this.context, this.profileImage, 0);

            } else {
                this.profileImage.setImageResource(R.drawable.member);
            }

            if (videoInfo.getUserName() != null) {
                this.userNameTextView.setText(videoInfo.getUserName());
            } else {
                this.userNameTextView.setText(Constant.EMPTY);
            }

        }
        if (this.screenType == Constant.MORE_VIDEOS) {
            this.deleteImageButton.setVisibility(View.VISIBLE);
        } else {
            this.deleteImageButton.setVisibility(View.GONE);
        }

    }

    private void initHolder(final View convertView) {

        this.dots = (LinearLayout) convertView.findViewById(R.id.bioviewdots);
        this.dots.setVisibility(View.VISIBLE);
        this.videoDetails = (LinearLayout) convertView.findViewById(R.id.videoDetails);
        this.profileView = (RelativeLayout) convertView.findViewById(R.id.profile);

        // profile views
        this.settingLayout = (RelativeLayout) convertView.findViewById(R.id.settingLay);
        this.profile = (RelativeLayout) convertView.findViewById(R.id.userProfilePick);
        this.bioText = (TextView) convertView.findViewById(R.id.bioText);
        // profileViewDetails=(LinearLayout)convertView.findViewById(R.id.profileView);
        this.profileviewdot = (ImageView) convertView.findViewById(R.id.profileviewdot);
        this.bioviewDot = (ImageView) convertView.findViewById(R.id.bioviewDot);

        this.privateGroupCountTextView = (TextView) convertView.findViewById(R.id.privateGroupCountTextView);
        this.myProfileImageView = (RoundedImageView) convertView.findViewById(R.id.profileImageView);
        this.myProfileImageView.setImageResource(R.drawable.member);
        this.profileBanner = (ImageView) convertView.findViewById(R.id.profilebanner);
        this.lastUpdateTextView = (TextView) convertView.findViewById(R.id.lastUpdateTextView);
        this.profileNameTextView = (TextView) convertView.findViewById(R.id.profileNameTextView);
        this.profileDetailsTextView = (TextView) convertView.findViewById(R.id.profileDetailsTextView);
        this.followersCountTextView = (TextView) convertView.findViewById(R.id.followersCountTextView);
        this.followingCountTextView = (TextView) convertView.findViewById(R.id.followingCountTextView);
        this.videoCountTextView = (TextView) convertView.findViewById(R.id.videosCountTextView);
        this.tagCountTextView = (TextView) convertView.findViewById(R.id.tagCountTextView);
        this.editImageView = (ImageView) convertView.findViewById(R.id.settingImageButton);
        this.editImageView.setVisibility(View.GONE);
        this.privateGroupLL = (LinearLayout) convertView.findViewById(R.id.privateGropupLL);

        this.statusLL = (LinearLayout) convertView.findViewById(R.id.statusLL);
        this.statusLL.setVisibility(View.VISIBLE);

        this.followingImageView = (ImageView) convertView.findViewById(R.id.followingImgView);
        this.followingImageView.setVisibility(View.VISIBLE);
        this.followingImageView.setOnClickListener(this);

        this.privateGroupImageView = (ImageView) convertView.findViewById(R.id.privateGroupImageView);
        this.privateGroupImageView.setVisibility(View.VISIBLE);
        this.privateGroupImageView.setOnClickListener(this);

        this.followerLL = (LinearLayout) convertView.findViewById(R.id.followersLL);
        this.followingLL = (LinearLayout) convertView.findViewById(R.id.followingLL);

        // video views

        this.postThumbnail = (ImageView) convertView.findViewById(R.id.postThumbnail);
        this.playImageButton = (ImageView) convertView.findViewById(R.id.playButtonImage);
        this.tagCount = (TextView) convertView.findViewById(R.id.tagTextView);
        this.loveCount = (TextView) convertView.findViewById(R.id.loveTextView);
        this.commentCount = (TextView) convertView.findViewById(R.id.commentTextView);
        this.firstName = (TextView) convertView.findViewById(R.id.firstUserName);
        this.secondName = (TextView) convertView.findViewById(R.id.secondUserName);
        this.firstComment = (TextView) convertView.findViewById(R.id.firstComment);
        this.secondComment = (TextView) convertView.findViewById(R.id.secondComment);
        this.userDetailsLL = (LinearLayout) convertView.findViewById(R.id.userDetailsLL);
        this.deleteImageButton = (ImageButton) convertView.findViewById(R.id.deleteImageButton);
        this.profileImage = (RoundedImageView) convertView.findViewById(R.id.profileImageView);
        this.userNameTextView = (TextView) convertView.findViewById(R.id.userNameTextView);
        this.postCommentImageView = (ImageView) convertView.findViewById(R.id.postCommentImageView);
        this.postLovedImageView = (ImageView) convertView.findViewById(R.id.postLovedImageView);
        this.postTagsImageView = (ImageView) convertView.findViewById(R.id.postTagsImageView);
        this.videoTitleTextView = (TextView) convertView.findViewById(R.id.videoTitleTextView);
        this.dateTextView = (TextView) convertView.findViewById(R.id.dateTextView);
        this.viewsTextView = (TextView) convertView.findViewById(R.id.viewsTextView);
        this.loveDetailsLL = (LinearLayout) convertView.findViewById(R.id.loveDetailsLL);
        this.firstTableRow = (LinearLayout) convertView.findViewById(R.id.firstTableRow);
        this.secondTableRow = (LinearLayout) convertView.findViewById(R.id.secondTableRow);
        this.commentDetailsLL = (LinearLayout) convertView.findViewById(R.id.commentDetailsLL);
        this.tagLL = (LinearLayout) convertView.findViewById(R.id.tagLL);
        this.likeLL = (LinearLayout) convertView.findViewById(R.id.loveLL);
        this.commentLL = (LinearLayout) convertView.findViewById(R.id.commentLL);

        this.optionsLike = (ImageView) convertView.findViewById(R.id.likeImageView);
        this.optionsComment = (ImageView) convertView.findViewById(R.id.commentImageView);
        this.optionsDropDown = (ImageButton) convertView.findViewById(R.id.optionsButton);

        // return holder;
    }

    private void setPostDetails(final MyPageDto videoInfo) {

        this.commentLL.setTag(videoInfo);
        this.commentLL.setOnClickListener(this);
        this.likeLL.setTag(videoInfo);
        this.likeLL.setOnClickListener(this);

        this.playImageButton.setTag(videoInfo);
        this.playImageButton.setOnClickListener(this);
        this.postTagsImageView.setTag(videoInfo);
        this.postTagsImageView.setOnClickListener(this);
        this.postCommentImageView.setTag(videoInfo);
        // postCommentImageView.setOnClickListener(this);
        this.postLovedImageView.setTag(videoInfo);
        // postLovedImageView.setOnClickListener(this);
        this.deleteImageButton.setTag(videoInfo);
        this.deleteImageButton.setOnClickListener(this);

        this.optionsComment.setTag(videoInfo);
        this.optionsComment.setOnClickListener(this);
        this.optionsLike.setTag(videoInfo);
        this.optionsLike.setOnClickListener(this);
        this.optionsDropDown.setTag(videoInfo);
        this.optionsDropDown.setOnClickListener(this);

        if (videoInfo.hasLiked()) {
            this.optionsLike.setImageResource(R.drawable.loved_new_f);
        } else {
            this.optionsLike.setImageResource(R.drawable.loved_new);
        }
        if (videoInfo.hasCommented()) {
            this.optionsComment.setImageResource(R.drawable.comments_new_f);
        } else {
            this.optionsComment.setImageResource(R.drawable.comments_new);
        }

        if (!Strings.isNullOrEmpty(videoInfo.getVideoThumbPath())) {
            Image.displayImage(videoInfo.getVideoThumbPath(), (Activity) this.context, this.postThumbnail, 1);
        } else {
            this.postThumbnail.setImageResource(R.drawable.profile_banner);
        }
        if (videoInfo.getVideoTitle() != null) {
            String title = videoInfo.getVideoTitle();
            if (videoInfo.getVideoTitle().length() > 10) {
                title = videoInfo.getVideoTitle().substring(0, 9);
            }
            this.videoTitleTextView.setText(title);// + " | "
        }
        if (videoInfo.getUploadDate() != null) {
            this.dateTextView.setText(R.string.created_ + videoInfo.getUploadDate());// +
            // " | "
        }
        if (videoInfo.getNumberOfViews() != null) {
            this.viewsTextView.setText("Views: " + videoInfo.getNumberOfViews());
        }

        if ((videoInfo.getNumberOfTags() != null) && (Integer.parseInt(videoInfo.getNumberOfTags().trim()) > 0)) {
            this.tagLL.setVisibility(View.VISIBLE);
            // tagCount.setText(videoInfo.getNo_of_tags());
            if (Integer.parseInt(videoInfo.getNumberOfTags().trim()) > 1) {
                this.tagCount.setText(videoInfo.getNumberOfTags() + " Tags");
            } else {
                this.tagCount.setText(videoInfo.getNumberOfTags() + " Tag");
            }
        } else {
            this.tagLL.setVisibility(View.GONE);
        }

        if ((videoInfo.getNumberOfLikes() != null) && (Integer.parseInt(videoInfo.getNumberOfLikes().trim()) > 0)) {
            this.likeLL.setVisibility(View.VISIBLE);
            // loveCount.setText(videoInfo.getNo_of_likes()+" Loved");
            if (Integer.parseInt(videoInfo.getNumberOfLikes().trim()) > 1) {
                this.loveCount.setText(videoInfo.getNumberOfLikes() + " Likes");
            } else {
                this.loveCount.setText(videoInfo.getNumberOfLikes() + " Liked");
            }
        } else {
            this.likeLL.setVisibility(View.GONE);
        }

        if ((videoInfo.getNumberOfComments() != null) && (Integer.parseInt(videoInfo.getNumberOfComments().trim()) > 0)) {
            this.commentLL.setVisibility(View.VISIBLE);
            // commentCount.setText(videoInfo.getNo_of_comments());

            if (Integer.parseInt(videoInfo.getNumberOfComments().trim()) > 1) {
                this.commentCount.setText(videoInfo.getNumberOfComments() + " Comments");
            } else {
                this.commentCount.setText(videoInfo.getNumberOfComments() + " Comment");
            }
        } else {
            this.commentLL.setVisibility(View.GONE);
        }

        this.loveDetailsLL.removeAllViews();

        if ((videoInfo.getRecentLikedBy() != null) && !videoInfo.getRecentLikedBy().isEmpty()) {
            final ImageView view = new ImageView(this.context);
            view.setBackgroundResource(R.drawable.loved);
            final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
            lp.setMargins(6, 5, 6, 5);
            view.setLayoutParams(lp);
            this.loveDetailsLL.addView(view);
            for (int i = 0; i < videoInfo.getRecentLikedBy().size(); i++) {
                final RecentLikes recentLikes = videoInfo.getRecentLikedBy().get(i);
                String name = Constant.EMPTY;
                if (i < 1) {
                    if (Config.getUserId().equalsIgnoreCase(recentLikes.getUserId())) {
                        name = Constant.YOU_;
                    } else {
                        name = recentLikes.getUserName();
                    }
                } else {
                    if (Config.getUserId().equalsIgnoreCase(recentLikes.getUserId())) {
                        name = Constant._YOU;
                    } else {
                        name = Constant.COMMA + recentLikes.getUserName();
                    }
                }

                final TextView textView = new TextView(this.context);

                final InputFilter[] filterArray = new InputFilter[1];
                filterArray[0] = new InputFilter.LengthFilter(14);
                textView.setFilters(filterArray);
                String nameText = Constant.EMPTY;
                if (name != null) {
                    name = name.trim();
                    if (name.length() > 12) {
                        nameText = name.substring(0, 11) + Constant.ELLIPSIS;
                    } else {
                        nameText = name;
                    }
                }

                textView.setText(nameText);
                textView.setTag(videoInfo.getRecentLikedBy().get(i));
                // textView.setTextColor(Color.parseColor("#33ccff"));
                textView.setTextColor(this.context.getResources().getColor(R.color.twitter_bg_color));
                textView.setTag(recentLikes);
                textView.setTextAppearance(this.context, R.style.lovediewStyle);
                textView.setTypeface(Typeface.createFromAsset(this.context.getAssets(),
                        this.context.getString(R.string.app_bold_font_name)));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        this.context.getResources().getDimension(R.dimen.usernametextsize));
                textView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(final View v) {

                        final RecentLikes recentLikes = (RecentLikes) v.getTag();
                        if (recentLikes.getUserId() != null) {
                            final int id = Integer.parseInt(recentLikes.getUserId());
                            if (id > 0) {
                                OtherUserAdapter.this.gotToOtherPage(id);
                            } else {
                                Alerts.showInfoOnly(Constant.NO_USER_ID_AVAILABLE_FOR_THIS_USER,
                                        OtherUserAdapter.this.context);
                            }
                        } else {
                            Alerts.showInfoOnly(Constant.NO_USER_ID, OtherUserAdapter.this.context);
                        }
                    }
                });
                this.loveDetailsLL.addView(textView);
                if (i == 1) {
                    break;
                }
            }
            final int noOflikes = Integer.parseInt(videoInfo.getNumberOfLikes().trim());
            if (noOflikes > 2) {
                final TextView textView = new TextView(this.context);
                // textView.setTextColor(Color.parseColor("#33ccff"));
                textView.setTextColor(this.context.getResources().getColor(R.color.twitter_bg_color));
                textView.setTag(videoInfo);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        this.context.getResources().getDimension(R.dimen.usernametextsize));
                textView.setTextAppearance(this.context, R.style.lovediewStyle);
                textView.setText(" and " + (noOflikes - 2) + " others  Liked");
                textView.setTypeface(Typeface.createFromAsset(this.context.getAssets(),
                        this.context.getString(R.string.app_bold_font_name)));
                textView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(final View v) {

                        final MyPageDto myPageDto2 = (MyPageDto) v.getTag();
                        final int videoId = Integer.parseInt(myPageDto2.getVideoId());
                        if (videoId > 0) {
                            OtherUserAdapter.this.gotoLikePage(String.valueOf(videoId), myPageDto2.getNumberOfLikes());

                            // Intent likedIntent = new
                            // Intent(context,LikedActivity.class);
                            // likedIntent.putExtra("videoid",
                            // myPageDto2.getVideoID());
                            // likedIntent.putExtra("count",
                            // myPageDto2.getNo_of_likes());
                            // context.startActivity(likedIntent);
                        } else {
                            Alerts.showInfoOnly(Constant.INVALID_VIDEO_ID, OtherUserAdapter.this.context);
                        }

                        /*
                         * // TODO Show the loved list RecentLikes recentLikes = (RecentLikes) v.getTag(); if
                         * (recentLikes.getUserId() != null) { Intent otherUserIntent = new Intent(context,
                         * OtherUserActivity.class); otherUserIntent.putExtra("userid", recentLikes.getUserId());
                         * context.startActivity(otherUserIntent); } else { Alerts.ShowAlertOnly("Info", "No user id",
                         * context); }
                         */
                    }
                });
                this.loveDetailsLL.addView(textView);
            }

        }

        if ((videoInfo.getRecentComments() != null) && !videoInfo.getRecentComments().isEmpty()) {

            if (videoInfo.getRecentComments().size() >= 1) {
                this.firstTableRow.setVisibility(View.VISIBLE);
                final Comment commentDto = videoInfo.getRecentComments().get(0);
                this.firstName.setTag(commentDto);
                if (Config.getUserId().equalsIgnoreCase(commentDto.getUserId())) {
                    this.firstName.setText(Constant.YOU__);
                } else {
                    this.firstName.setText(commentDto.getUserName() + Constant.COLON);
                }
                this.firstName.setOnClickListener(this);
                final SpannableString spannable = new SpannableString(commentDto.getComment());
                Util.emotifySpannable(spannable);
                this.firstComment.setText(spannable);
            } else {
                this.firstTableRow.setVisibility(View.GONE);
            }

            if (videoInfo.getRecentComments().size() >= 2) {
                this.firstTableRow.setVisibility(View.VISIBLE);
                final Comment commentDto = videoInfo.getRecentComments().get(1);
                this.secondName.setTag(commentDto);
                if (Config.getUserId().equalsIgnoreCase(commentDto.getUserId())) {
                    this.secondName.setText(Constant.YOU__);
                } else {
                    this.secondName.setText(commentDto.getUserName() + Constant.COLON);
                }
                this.secondName.setOnClickListener(this);
                final SpannableString spannable = new SpannableString(commentDto.getComment());
                Util.emotifySpannable(spannable);
                this.secondComment.setText(spannable);
            } else {
                this.secondTableRow.setVisibility(View.GONE);
            }

        } else {
            this.commentDetailsLL.setVisibility(View.GONE);
        }

    }

    private void setProdileDetails(final MyPage profileDetails) {

        if (profileDetails.getBio() != null) {
            this.bioText.setText(profileDetails.getBio());
            this.dots.setVisibility(View.VISIBLE);
        } else {
            this.bioText.setText(Constant.EMPTY);
            this.dots.setVisibility(View.GONE);
        }
        this.bioviewDot.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (OtherUserAdapter.this.introScrren == 1) {
                    OtherUserAdapter.this.introScrren = 2;
                    OtherUserAdapter.this.notifyDataSetChanged();
                }

            }
        });
        this.profileviewdot.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (OtherUserAdapter.this.introScrren == 2) {
                    OtherUserAdapter.this.introScrren = 1;
                    OtherUserAdapter.this.notifyDataSetChanged();
                }

            }
        });

        if (profileDetails.getUserid() != null) {
            this.otherUserId = profileDetails.getUserid();
        }

        if (!Strings.isNullOrEmpty(profileDetails.getPthotoPath())) {
            Image.displayImage(profileDetails.getPthotoPath(), (Activity) this.context, this.myProfileImageView,
                    0);

        } else {
            this.myProfileImageView.setImageResource(R.drawable.member);
        }
        if (!Strings.isNullOrEmpty(profileDetails.getUserPickView())) {
            this.profilePicUrl = profileDetails.getUserPickView();
        }
        if (profileDetails.getBannerPath() != null) {
            Image.displayImage(profileDetails.getBannerPath(), (Activity) this.context, this.profileBanner, 3);
        } else {
            this.myProfileImageView.setImageResource(R.drawable.defaultpicture);
        }
        this.lastUpdateTextView.setText("Last Update: "
                + (profileDetails.getLastUpdate() != null ? profileDetails.getLastUpdate() : "0"));

        if (profileDetails.getUsername() != null) {
            this.profileNameTextView.setText(profileDetails.getUsername());
            this.heading.setText(profileDetails.getUsername());// +" Page"
        }

        if (profileDetails.getWebsite() != null) {
            this.profileDetailsTextView.setText(profileDetails.getWebsite());
            this.profileDetailsTextView.setTag(profileDetails.getWebsite());
            this.profileDetailsTextView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View v) {

                    String link = v.getTag().toString();
                    if (link.startsWith("http")) {
                    } else {
                        link = "http://" + link;
                    }

                    final Intent intent = new Intent(OtherUserAdapter.this.context, WebViewActivity.class);
                    intent.putExtra("link", link);
                    intent.putExtra("heading", "Domain");
                    OtherUserAdapter.this.context.startActivity(intent);
                }
            });
        } else {
            this.profileDetailsTextView.setText(Constant.EMPTY);
        }

        this.followersCountTextView.setText((profileDetails.getTotalNoOffollowers() != null ? profileDetails
                .getTotalNoOffollowers() : "0"));
        this.followingCountTextView.setText((profileDetails.getTotalNoOfFollowing() != null ? profileDetails
                .getTotalNoOfFollowing() : "0"));
        this.privateGroupCountTextView
                .setText((profileDetails.getTotalNoOfPrivateGroupPeople() != null ? profileDetails
                        .getTotalNoOfPrivateGroupPeople() : "0"));

        if (profileDetails.getTotalNoOfVideos() != null) {
            final int videos = Integer.parseInt(profileDetails.getTotalNoOfVideos());
            if (videos > 1) {
                this.videoCountTextView.setText(profileDetails.getTotalNoOfVideos() + " Videos");
            } else {
                this.videoCountTextView.setText(profileDetails.getTotalNoOfVideos() + " Video");
            }
        }
        if (profileDetails.getTotalNoOfTags() != null) {
            final int videos = Integer.parseInt(profileDetails.getTotalNoOfTags());
            if (videos > 1) {
                this.tagCountTextView.setText(profileDetails.getTotalNoOfTags() + " Tags");
            } else {
                this.tagCountTextView.setText(profileDetails.getTotalNoOfTags() + " Tag");
            }
        }

        this.followingImageView.setTag(profileDetails);
        this.privateGroupImageView.setTag(profileDetails);

        if (Constant.NO.equalsIgnoreCase(profileDetails.getIsFollow())) {
            this.followingImageView.setImageResource(R.drawable.otherfollow);
        } else {
            this.followingImageView.setImageResource(R.drawable.following);
        }

        if (Constant.YES.equalsIgnoreCase(profileDetails.getIsRespToPvtReq())) {
            this.privateGroupImageView.setImageResource(R.drawable.respond_pvt);
        } else if (Constant.YES.equalsIgnoreCase(profileDetails.getIsPrivateReqSent())) {
            this.privateGroupImageView.setImageResource(R.drawable.sentpvtreq);
        } else {
            if (Constant.NO.equalsIgnoreCase(profileDetails.getIsAddToPrivateGroup())) {
                this.privateGroupImageView.setImageResource(R.drawable.addtoprivate);
            } else {
                this.privateGroupImageView.setImageResource(R.drawable.addedtoprivate);
            }
        }
        if (profileDetails.getUserid().equalsIgnoreCase(MainManager.getInstance().getUserId())) {
            this.privateGroupImageView.setVisibility(View.GONE);
            this.followingImageView.setVisibility(View.GONE);
        } else {
            this.privateGroupImageView.setVisibility(View.VISIBLE);
            this.followingImageView.setVisibility(View.VISIBLE);
        }
        // profile pic views

        this.myProfileImageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (OtherUserAdapter.this.profilePicUrl != null) {
                    final Intent intent = new Intent(OtherUserAdapter.this.context, ProfilePickViewActivity.class);
                    intent.putExtra("url", OtherUserAdapter.this.profilePicUrl);
                    OtherUserAdapter.this.context.startActivity(intent);
                } else {
                    Alerts.showInfoOnly("Profile pick is not available", OtherUserAdapter.this.context);
                }
            }
        });

        final String followersCount = profileDetails.getTotalNoOffollowers();
        final String followingCount = profileDetails.getTotalNoOfFollowing();
        final String privateCount = profileDetails.getTotalNoOfPrivateGroupPeople();
        this.privateGroupLL.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (OtherUserAdapter.this.otherUserId != null) {
                    final int userId = Integer.parseInt(OtherUserAdapter.this.otherUserId);
                    if (userId > 0) {
                        String count = privateCount;
                        if (count == null) {
                            count = OtherUserAdapter.this.getContext().getString(R.string._0);
                        }

                    }

                }
            }
        });
        this.followerLL.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (OtherUserAdapter.this.otherUserId != null) {
                    final int userId = Integer.parseInt(OtherUserAdapter.this.otherUserId);
                    if (userId > 0) {
                        String count = followersCount;
                        if (count == null) {
                            count = OtherUserAdapter.this.getContext().getString(R.string._0);
                        }
                        OtherUserAdapter.this.goToUsersListPage(userId, count, "followers");
                        // Intent followerIntent = new Intent(context,
                        // UsersListActivity.class);
                        // followerIntent.putExtra("type", "followers");
                        // followerIntent.putExtra("id", ""+userId);
                        // String count=followersCount;
                        // if(count==null){
                        // count="0";
                        // }
                        // followerIntent.putExtra("count", count);
                        // context.startActivity(followerIntent);
                    }
                }

            }
        });
        this.followingLL.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (OtherUserAdapter.this.otherUserId != null) {
                    final int userId = Integer.parseInt(OtherUserAdapter.this.otherUserId);
                    if (userId > 0) {
                        String count = followingCount;
                        if (count == null) {
                            count = OtherUserAdapter.this.getContext().getString(R.string._0);
                        }
                        OtherUserAdapter.this.goToUsersListPage(userId, count, Constant.FOLLOWINGS);
                    }
                }
            }
        });

        if (this.introScrren == 1) {
            this.settingLayout.setVisibility(View.VISIBLE);
            this.profile.setVisibility(View.VISIBLE);
            this.profileNameTextView.setVisibility(View.VISIBLE);
            this.profileDetailsTextView.setVisibility(View.VISIBLE);
            this.profileviewdot.setImageResource(R.drawable.breadcrumb_enable);
            this.bioviewDot.setImageResource(R.drawable.breadcrumb_disable);
            this.bioText.setVisibility(View.GONE);
            this.statusLL.setVisibility(View.VISIBLE);
        } else {
            this.settingLayout.setVisibility(View.INVISIBLE);
            this.profile.setVisibility(View.INVISIBLE);
            this.profileNameTextView.setVisibility(View.INVISIBLE);
            this.profileDetailsTextView.setVisibility(View.INVISIBLE);
            this.bioviewDot.setImageResource(R.drawable.breadcrumb_enable);
            this.profileviewdot.setImageResource(R.drawable.breadcrumb_disable);
            this.bioText.setVisibility(View.VISIBLE);
            this.statusLL.setVisibility(View.INVISIBLE);
        }
    }

    private void setView(final MyPageDto videoInfo, final int position, final View convertView) {

        if (position == 0) {
            if (this.profileDetails != null) {
                this.setProdileDetails(this.profileDetails);
                this.videoDetails.setVisibility(View.GONE);
                this.profileView.setVisibility(View.VISIBLE);
            }
        } else {
            this.setPostDetails(videoInfo);
            final int id = Integer.parseInt(videoInfo.getVideoId());
            convertView.setId(id);
            this.videoDetails.setVisibility(View.VISIBLE);
            this.profileView.setVisibility(View.GONE);
        }
    }

    private void showOptionDialogs(final MyPageDto video) {

        final View view = LayoutInflater.from(this.context).inflate(R.layout.options_list, null);
        final CustomDialog alertDialog = new CustomDialog(this.context, R.style.CustomStyle);
        // final AlertDialog alertDialog=new
        // AlertDialog.Builder(context).create();
        alertDialog.setContentView(view);
        final WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;

        alertDialog.getWindow().setAttributes(layoutParams);
        alertDialog.show();
        alertDialog.setCanceledOnTouchOutside(false);

        final RelativeLayout delete = (RelativeLayout) view.findViewById(R.id.delete);
        final RelativeLayout reportVideo = (RelativeLayout) view.findViewById(R.id.report);
        final RelativeLayout share = (RelativeLayout) view.findViewById(R.id.share);
        final RelativeLayout copyURL = (RelativeLayout) view.findViewById(R.id.copysahreurl);
        final RelativeLayout tag = (RelativeLayout) view.findViewById(R.id.tag);
        final RelativeLayout cancel = (RelativeLayout) view.findViewById(R.id.cancel);
        final RelativeLayout updateAccessPermission = (RelativeLayout) view.findViewById(R.id.updateAccessPermission);
        if (Config.getUserId().equalsIgnoreCase(video.getUserId())) {
            delete.setVisibility(View.VISIBLE);
            tag.setVisibility(View.VISIBLE);
            updateAccessPermission.setVisibility(View.VISIBLE);
            reportVideo.setVisibility(View.GONE);
        } else {
            delete.setVisibility(View.GONE);
            tag.setVisibility(View.GONE);
            updateAccessPermission.setVisibility(View.GONE);
            reportVideo.setVisibility(View.VISIBLE);
        }
        if ((video.getPublicVideo() == 0) && !(Constant.MY_PAGE_MORE_FEEDS).equalsIgnoreCase(this.screenType)
                && !(Config.getUserId().equalsIgnoreCase(video.getUserId()))
                && !(Constant.MORE_VIDEOS.equalsIgnoreCase(this.screenType))) {
            share.setVisibility(View.GONE);
            copyURL.setVisibility(View.GONE);
        } else {
            share.setVisibility(View.VISIBLE);
            copyURL.setVisibility(View.VISIBLE);
        }

        updateAccessPermission.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                alertDialog.dismiss();
                final VideoDetails currentObj = new VideoDetails();
                currentObj.setVideoID(video.getVideoId());
                currentObj.setVideoURL(video.getVideoUrl());
                currentObj.setVideothumbPath(video.getVideoThumbPath());
                currentObj.setVideoTitle(video.getVideoTitle());
                currentObj.setVideoDesc(video.getVideoDescription());
                currentObj.setShareUrl(video.getShareUrl());
                currentObj.setPublicVideo(video.getPublicVideo());
                final Intent intent = new Intent(OtherUserAdapter.this.context, AccessPermissionActivity.class);
                intent.putExtra("video", currentObj);
                OtherUserAdapter.this.context.startActivity(intent);
            }
        });
        reportVideo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                alertDialog.dismiss();
                if (((video != null) && (video.getVideoId() != null))) {
                    OtherUserAdapter.this.currentVideoId = Integer.parseInt(video.getVideoId());
                    if (OtherUserAdapter.this.currentVideoId > 0) {
                        final Intent intent = new Intent(OtherUserAdapter.this.context, ReportActivity.class);
                        intent.putExtra("video", video);
                        OtherUserAdapter.this.context.startActivity(intent);
                        /*
                         * currentVideoId = Integer.parseInt(video.getVideoID()); VideoAsyncTask task = new
                         * VideoAsyncTask(context, "report",getVedioReportJsonReq(video));// video.getUserId()
                         * task.delegate = OtherUserAdapter.this; task.execute();
                         */
                    } else {
                        Alerts.showInfoOnly(Constant.NO_USER_ID, OtherUserAdapter.this.context);
                    }
                }

            }
        });
        tag.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                alertDialog.dismiss();
                if (video.getVideoId() != null) {
                    final int currentVideoId = Integer.parseInt(video.getVideoId());
                    if (currentVideoId > 0) {
                        new PlaybackAsync(OtherUserAdapter.this.context, video.getVideoId()).execute();
                    } else {
                        Alerts.showInfoOnly("No video id", OtherUserAdapter.this.context);
                    }
                }
            }
        });
        share.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                alertDialog.dismiss();
                final VideoDetails currentObj = new VideoDetails();
                currentObj.setVideoID(video.getVideoId());
                currentObj.setVideoURL(video.getVideoUrl());
                currentObj.setVideothumbPath(video.getVideoThumbPath());
                currentObj.setVideoTitle(video.getVideoTitle());
                if (video.getLatestTagExpression() != null) {
                    currentObj.setLatestTagexpression(video.getLatestTagExpression());
                }
                currentObj.setVideoDesc(video.getVideoDescription());
                currentObj.setShareUrl(video.getShareUrl());
                currentObj.setFbShareUrl(video.getFbShareUrl());
                final Intent intent = new Intent(OtherUserAdapter.this.context, ShareActivity.class);
                intent.putExtra("video", currentObj);
                OtherUserAdapter.this.context.startActivity(intent);
            }
        });
        copyURL.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                alertDialog.dismiss();
                OtherUserAdapter.this.currentVideoId = Integer.parseInt(video.getVideoId());
                if (OtherUserAdapter.this.currentVideoId > 0) {
                    final ClipboardManager ClipMan = (ClipboardManager) OtherUserAdapter.this.context
                            .getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipMan.setText(video.getShareUrl());
                    Alerts.showInfoOnly("URL has been copied to clipboard.", OtherUserAdapter.this.context);
                } else {
                    Alerts.showInfoOnly(Constant.NO_USER_ID, OtherUserAdapter.this.context);
                }
            }
        });
        cancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                alertDialog.dismiss();
            }
        });

        delete.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                alertDialog.dismiss();
                OtherUserAdapter.this.currentVideoId = Integer.parseInt(video.getVideoId());
                if (OtherUserAdapter.this.currentVideoId > 0) {
                    try {
                        final VideoAsyncTask task = new VideoAsyncTask(OtherUserAdapter.this.context, Constant.DELETE,
                                OtherUserAdapter.getVideoDeleteJsonReq(video.getVideoId(), Config.getUserId()), true);
                        task.delegate = OtherUserAdapter.this;
                        task.execute();
                    } catch (final JSONException exception) {
                        LOG.e(exception);
                    }
                } else {
                    Alerts.showInfoOnly(Constant.NO_USER_ID, OtherUserAdapter.this.context);
                }
            }
        });

        alertDialog.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(final DialogInterface dialog) {

                alertDialog.dismiss();
            }
        });

    }

    void gotoLikePage(final String videoId, final String likes) {

        final LikedFragment fragment = new LikedFragment(); // object of next fragment
        final Bundle bundle = new Bundle();
        bundle.putString(Constant.VIDEOID, videoId);
        bundle.putString(Constant.COUNT, likes);
        if (Constant.VIDEO_FEEDS.equalsIgnoreCase(this.screenType) && (this.currentFragment != null)) {
            bundle.putString(Constant.ROOT_FRAGMENT, Constant.VIDEO_FEEDS);
            fragment.setArguments(bundle);
            BaseFragment.tabActivity.pushFragments(R.id.feedTab, fragment, Constant.LIKED, this.currentFragment,
                    Constant.HOME);
        } else if (Constant.MY_PAGE_MORE_FEEDS.equalsIgnoreCase(this.screenType) && (this.currentFragment != null)) {
            bundle.putString(Constant.ROOT_FRAGMENT, Constant.MY_PAGE);
            fragment.setArguments(bundle);
            BaseFragment.tabActivity.pushFragments(R.id.mypageTab, fragment, Constant.LIKED, this.currentFragment,
                    Constant.MYPAGE);
        } else if (Constant.MORE_VIDEOS.equalsIgnoreCase(this.screenType) && (this.currentFragment != null)) {
            bundle.putString(Constant.ROOT_FRAGMENT, Constant.MY_PAGE);
            fragment.setArguments(bundle);
            BaseFragment.tabActivity.pushFragments(R.id.mypageTab, fragment, Constant.LIKED, this.currentFragment,
                    Constant.MYPAGE);
        } else if (Constant.MY_PAGE.equalsIgnoreCase(this.screenType) && (this.currentFragment != null)) {
            bundle.putString(Constant.ROOT_FRAGMENT, Constant.MY_PAGE);
            fragment.setArguments(bundle);
            BaseFragment.tabActivity.pushFragments(R.id.mypageTab, fragment, Constant.LIKED, this.currentFragment,
                    Constant.MYPAGE);
        } else if (Constant.BROWSE_PAGE.equalsIgnoreCase(this.screenType) && (this.currentFragment != null)) {
            bundle.putString(Constant.ROOT_FRAGMENT, Constant.BROWSE_PAGE);
            fragment.setArguments(bundle);
            BaseFragment.tabActivity.pushFragments(R.id.browsTab, fragment, Constant.LIKED, this.currentFragment,
                    Constant.BROWSE);
        } else if (Constant.NOTIFICATIONS_PAGE.equalsIgnoreCase(this.screenType) && (this.currentFragment != null)) {
            bundle.putString(Constant.ROOT_FRAGMENT, Constant.NOTIFICATIONS_PAGE);
            fragment.setArguments(bundle);
            BaseFragment.tabActivity.pushFragments(R.id.notificationTab, fragment, Constant.LIKED,
                    this.currentFragment, Constant.NOTIFICATIONS);
        } else {
            final Intent otherUserIntent = new Intent(this.context, LikedActivity.class);
            otherUserIntent.putExtra(Constant.VIDEOID, videoId);
            otherUserIntent.putExtra(Constant.COUNT, likes);
            this.context.startActivity(otherUserIntent);
        }

    }

    void goToUsersListPage(final int userId, final String count, final String type) {

        final UsersListFragment usersFragment = new UsersListFragment();
        final Bundle users = new Bundle();
        users.putString(Constant.TYPE, type);
        users.putString(Constant.ID, String.valueOf(userId));
        users.putString(Constant.COUNT, count);

        if (Constant.VIDEO_FEEDS.equalsIgnoreCase(this.screenType) && (this.currentFragment != null)) {
            users.putString(Constant.ROOT_FRAGMENT, Constant.VIDEO_FEEDS);
            usersFragment.setArguments(users);
            BaseFragment.tabActivity.pushFragments(R.id.feedTab, usersFragment, Constant.LIKED, this.currentFragment,
                    Constant.HOME);
        } else if (Constant.MY_PAGE_MORE_FEEDS.equalsIgnoreCase(this.screenType) && (this.currentFragment != null)) {
            users.putString(Constant.ROOT_FRAGMENT, Constant.MY_PAGE);
            usersFragment.setArguments(users);
            BaseFragment.tabActivity.pushFragments(R.id.mypageTab, usersFragment, Constant.LIKED, this.currentFragment,
                    Constant.MYPAGE);
        } else if (Constant.MORE_VIDEOS.equalsIgnoreCase(this.screenType) && (this.currentFragment != null)) {
            users.putString(Constant.ROOT_FRAGMENT, Constant.MY_PAGE);
            usersFragment.setArguments(users);
            BaseFragment.tabActivity.pushFragments(R.id.mypageTab, usersFragment, Constant.LIKED, this.currentFragment,
                    Constant.MYPAGE);
        } else if (Constant.MY_PAGE.equalsIgnoreCase(this.screenType) && (this.currentFragment != null)) {
            users.putString(Constant.ROOT_FRAGMENT, Constant.MY_PAGE);
            usersFragment.setArguments(users);
            BaseFragment.tabActivity.pushFragments(R.id.mypageTab, usersFragment, Constant.LIKED, this.currentFragment,
                    Constant.MYPAGE);
        } else if (Constant.NOTIFICATIONS_PAGE.equalsIgnoreCase(this.screenType) && (this.currentFragment != null)) {
            users.putString(Constant.ROOT_FRAGMENT, Constant.NOTIFICATIONS_PAGE);
            usersFragment.setArguments(users);
            BaseFragment.tabActivity.pushFragments(R.id.notificationTab, usersFragment, Constant.LIKED,
                    this.currentFragment, Constant.NOTIFICATIONS);
        } else if (Constant.BROWSE_PAGE.equalsIgnoreCase(this.screenType) && (this.currentFragment != null)) {
            users.putString(Constant.ROOT_FRAGMENT, Constant.BROWSE_PAGE);
            usersFragment.setArguments(users);
            BaseFragment.tabActivity.pushFragments(R.id.browsTab, usersFragment, Constant.LIKED, this.currentFragment,
                    Constant.BROWSE);
        } else {
        }

    }

    void gotToOtherPage(final int id) {

        final OtherUserFragment fragment = new OtherUserFragment(); // object of next
        // fragment
        final Bundle bundle = new Bundle();
        if (Constant.MY_PAGE.equalsIgnoreCase(this.screenType)) {
            bundle.putString(Constant.ROOT_FRAGMENT, Constant.MY_PAGE);
            bundle.putString(Constant.USERID, String.valueOf(id));
            fragment.setArguments(bundle);
            BaseFragment.tabActivity.pushFragments(R.id.mypageTab, fragment, Constant.OTHERS_PAGE,
                    this.currentFragment, Constant.MYPAGE);

        } else if (Constant.MY_PAGE_MORE_FEEDS.equalsIgnoreCase(this.screenType)) {
            bundle.putString(Constant.ROOT_FRAGMENT, Constant.MY_PAGE);
            bundle.putString(Constant.USERID, String.valueOf(id));
            fragment.setArguments(bundle);
            BaseFragment.tabActivity.pushFragments(R.id.mypageTab, fragment, Constant.OTHERS_PAGE,
                    this.currentFragment, Constant.MYPAGE);

        } else if (Constant.MORE_VIDEOS.equalsIgnoreCase(this.screenType)) {
            bundle.putString(Constant.ROOT_FRAGMENT, Constant.MY_PAGE);
            bundle.putString(Constant.USERID, String.valueOf(id));
            fragment.setArguments(bundle);
            BaseFragment.tabActivity.pushFragments(R.id.mypageTab, fragment, Constant.OTHERS_PAGE,
                    this.currentFragment, Constant.MYPAGE);

        } else if (Constant.VIDEO_FEEDS.equalsIgnoreCase(this.screenType)) {
            bundle.putString(Constant.ROOT_FRAGMENT, Constant.VIDEO_FEEDS);
            bundle.putString(Constant.USERID, String.valueOf(id));
            fragment.setArguments(bundle);
            BaseFragment.tabActivity.pushFragments(R.id.feedTab, fragment, Constant.OTHERS_PAGE, this.currentFragment,
                    Constant.HOME);

        } else if (Constant.NOTIFICATIONS_PAGE.equalsIgnoreCase(this.screenType)) {
            bundle.putString(Constant.ROOT_FRAGMENT, Constant.NOTIFICATIONS_PAGE);
            bundle.putString(Constant.USERID, String.valueOf(id));
            fragment.setArguments(bundle);
            BaseFragment.tabActivity.pushFragments(R.id.notificationTab, fragment, Constant.OTHERS_PAGE,
                    this.currentFragment, Constant.NOTIFICATIONS);

        } else if (Constant.BROWSE_PAGE.equalsIgnoreCase(this.screenType)) {
            bundle.putString(Constant.ROOT_FRAGMENT, Constant.BROWSE_PAGE);
            bundle.putString(Constant.USERID, String.valueOf(id));
            fragment.setArguments(bundle);
            BaseFragment.tabActivity.pushFragments(R.id.browsTab, fragment, Constant.OTHERS_PAGE, this.currentFragment,
                    Constant.BROWSE);

        } else {
            final Intent otherUserIntent = new Intent(this.context, OtherUserActivity.class);
            otherUserIntent.putExtra(Constant.USERID, id);
            this.context.startActivity(otherUserIntent);
        }
    }
}
