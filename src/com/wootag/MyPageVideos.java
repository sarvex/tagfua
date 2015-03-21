/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.wTagFu

import android.app.Activity;
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
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.woTagFusync.PlaybackAsync;
import com.wooTagFuync.VideoAsyncTask;
import com.wootTagFu.Comment;
import com.wootaTagFuMyPageDto;
import com.wootagTagFuecentLikes;
import com.wootag.TagFudeoDetails;
import com.wootag.fTagFuts.BaseFragment;
import com.wootag.frTagFus.LikedFragment;
import com.wootag.fraTagFu.NewMyPageFragment;
import com.wootag.fragTagFuOtherUserFragment;
import com.wootag.ui.CuTagFualog;
import com.wootag.ui.ImaTagFuport com.wootag.ui.RounTagFugeView;
import com.wootag.util.AleTagFumport com.wootag.util.ConfTagFuport com.wootag.util.Util;TagFut com.wootag.util.VideoActionInterface;

public class MyPageVideos implements OnClickListener, VideoActionInterface {

    private static final String COLON = ": ";
    private static final String YOU__ = "You: ";
    private static final String ELLIPSIS = "...";
    private static final String YOU = "You";
    private static final String COMMA = " ,";
    private static final String YOU_ = "You ";
    private static final String EMPTY = "";
    private static final String DISLIKE = "dislike";
    private static final String LIKE = "like";
    private static final String DELETE = "delete";
    private static final String COUNT = "count";
    private static final String INVALID_VIDEO_ID = "Invalid video id";
    private static final Logger LOG = LoggerManager.getLogger();
    private static final String NO_USER_ID = "No user id";
    private static final String USERID = "userid";

    private static final String VIDEOID = "videoid";

    public TextView commentCount;
    public LinearLayout commentDetailsLL;
    public LinearLayout commentLL;
    public TextView dateTextView;
    public ImageButton deleteImageButton;
    public TextView firstComment;
    public TextView firstName;
    public LinearLayout firstTableRow;
    public LinearLayout likeLL;
    public TextView loveCount;
    public TextView loveDetails;
    public LinearLayout loveDetailsLL;
    public ImageView playImageButton;
    public ImageView postCommentImageView;
    public ImageView postLovedImageView;
    public ImageView postTagsImageView;
    public ImageView postThumbnail;
    public RoundedImageView profileImage;
    public TextView secondComment;
    public TextView secondName;
    public LinearLayout secondTableRow;
    public TextView tagCount;
    public LinearLayout tagLL;
    public RelativeLayout userDetailsLL;
    public TextView userNameTextView;
    public TextView videoTitleTextView;
    public TextView viewsTextView;

    protected final Context context;
    protected int currentVideoId;
    private MyPageDto likeDto;
    private ImageView optionLikeImageView;
    private ImageView optionsComment;
    private ImageButton optionsDropDown;
    private ImageView optionsLike;
    protected final String screenType;
    private final String userId;

    public MyPageVideos(final Context context, final String screenType, final String userId) {

        this.screenType = screenType;
        this.context = context;
        this.userId = userId;
    }

    private static JSONObject getVideoLikeJsonReq(final String videoId) throws JSONException {

        final JSONObject json = new JSONObject();
        json.put(VIDEOID, videoId);
        json.put(USERID, Config.getUserId());
        return json;

    }

    static JSONObject getVideoDeleteJsonReq(final String videoId, final String userid) throws JSONException {

        final JSONObject json = new JSONObject();
        json.put(VIDEOID, videoId);
        json.put(USERID, userid);
        return json;
    }

    public View getView(final LayoutInflater inflater, final MyPageDto videoInfo) {

        final LinearLayout convertView = (LinearLayout) inflater.inflate(R.layout.my_post, null);

        this.initHolder(convertView);
        // custonizeViewOnScreenType();
        this.setPostDetails(videoInfo);

        convertView.setTag(videoInfo);

        final int id = Integer.parseInt(videoInfo.getVideoId());
        convertView.setId(id);

        return convertView;
    }

    @Override
    public void onClick(final View v) {

        switch (v.getId()) {
        case R.id.commentImageView:
            final MyPageDto CommentInfo = (MyPageDto) v.getTag();
            if (CommentInfo.getVideoId() != null) {
                final int videoId = Integer.parseInt(CommentInfo.getVideoId());
                if (videoId > 0) {

                    final Intent seeAllComments = new Intent(this.context, SeeAllCommentsActivity.class);
                    seeAllComments.putExtra(VIDEOID, CommentInfo.getVideoId());
                    seeAllComments.putExtra(Constant.USERID, this.userId);

                    this.context.startActivity(seeAllComments);
                } else {
                    Alerts.showInfoOnly(INVALID_VIDEO_ID, this.context);
                }
            }
            break;

        case R.id.optionsButton:
            final MyPageDto options = (MyPageDto) v.getTag();
            this.showOptionDialogs(options);

            break;

        case R.id.likeImageView:
            this.likeDto = (MyPageDto) v.getTag();
            this.optionLikeImageView = (ImageView) v;
            if (this.likeDto.getVideoId() != null) {
                final int videoId = Integer.parseInt(this.likeDto.getVideoId());
                if (videoId > 0) {

                    String like = DISLIKE;
                    if (!this.likeDto.hasLiked()) {
                        like = LIKE;
                    }

                    try {
                        final VideoAsyncTask asyncTask = new VideoAsyncTask(this.context, like,
                                MyPageVideos.getVideoLikeJsonReq(String.valueOf(videoId)), false);
                        asyncTask.delegate = MyPageVideos.this;
                        asyncTask.execute();
                    } catch (final JSONException exception) {
                        LOG.e(exception);
                    }

                } else {
                    Alerts.showInfoOnly(INVALID_VIDEO_ID, this.context);
                }
            }
            break;

        case R.id.playButtonImage:
            final MyPageDto videoInfo = (MyPageDto) v.getTag();
            this.currentVideoId = Integer.parseInt(videoInfo.getVideoId());
            new PlaybackAsync(this.context, videoInfo.getVideoId()).execute();
            break;

        case R.id.commentLL:
            final MyPageDto myPageDto = (MyPageDto) v.getTag();

            final Intent seeAllComments = new Intent(this.context, SeeAllCommentsActivity.class);
            seeAllComments.putExtra(VIDEOID, myPageDto.getVideoId());
            seeAllComments.putExtra(Constant.USERID, this.userId);
            seeAllComments.putExtra(Constant.SCREEN, Constant.MY_PAGE);
            this.context.startActivity(seeAllComments);

            break;

        case R.id.loveLL:
            final MyPageDto myPageDto2 = (MyPageDto) v.getTag();
            final int videoId = Integer.parseInt(myPageDto2.getVideoId());
            if (videoId > 0) {
                final LikedFragment fragment = new LikedFragment(); // object of next fragment
                final Bundle bundle = new Bundle();
                bundle.putString(VIDEOID, myPageDto2.getVideoId());
                bundle.putString(COUNT, myPageDto2.getNumberOfLikes());
                fragment.setArguments(bundle);
                if (Constant.MY_PAGE.equalsIgnoreCase(this.screenType)) {
                    BaseFragment.tabActivity.pushFragments(R.id.mypageTab, fragment, Constant.LIKED,
                            NewMyPageFragment.newMyPageFragment, Constant.MYPAGE);
                }

            } else {
                Alerts.showInfoOnly(INVALID_VIDEO_ID, this.context);
            }
            break;

        case R.id.postTagsImageView:
            // MyPageDto myPageDto3 = (MyPageDto) v.getTag();
            break;

        case R.id.firstUserName:
            final Comment firstCommentDto = (Comment) v.getTag();
            if (firstCommentDto.getUserId() != null) {
                final OtherUserFragment fragment = new OtherUserFragment(); // object of next fragment
                final Bundle bundle = new Bundle();
                bundle.putString(USERID, firstCommentDto.getUserId());
                fragment.setArguments(bundle);
                if (Constant.MY_PAGE.equalsIgnoreCase(this.screenType)) {
                    BaseFragment.tabActivity.pushFragments(R.id.mypageTab, fragment, Constant.OTHERS_PAGE,
                            NewMyPageFragment.newMyPageFragment, Constant.MYPAGE);
                } else if (Constant.MY_PAGE_MORE_FEEDS.equalsIgnoreCase(this.screenType)) {
                    BaseFragment.tabActivity.pushFragments(R.id.mypageTab, fragment, Constant.OTHERS_PAGE,
                            NewMyPageFragment.newMyPageFragment, Constant.MYPAGE);
                } else if (Constant.MORE_VIDEOS.equalsIgnoreCase(this.screenType)) {
                    BaseFragment.tabActivity.pushFragments(R.id.mypageTab, fragment, Constant.OTHERS_PAGE,
                            NewMyPageFragment.newMyPageFragment, Constant.MYPAGE);
                } else if (Constant.VIDEO_FEEDS.equalsIgnoreCase(this.screenType)) {
                    BaseFragment.tabActivity.pushFragments(R.id.feedTab, fragment, Constant.OTHERS_PAGE,
                            NewMyPageFragment.newMyPageFragment, Constant.HOME);
                }
            } else {
                Alerts.showInfoOnly(NO_USER_ID, this.context);
            }
            break;

        case R.id.secondUserName:
            final Comment secondCommentDto = (Comment) v.getTag();
            if (secondCommentDto.getUserId() != null) {
                final OtherUserFragment fragment = new OtherUserFragment(); // object of next fragment
                final Bundle bundle = new Bundle();
                bundle.putString(USERID, secondCommentDto.getUserId());
                fragment.setArguments(bundle);
                if (Constant.MY_PAGE.equalsIgnoreCase(this.screenType)) {
                    BaseFragment.tabActivity.pushFragments(R.id.mypageTab, fragment, Constant.OTHERS_PAGE,
                            NewMyPageFragment.newMyPageFragment, Constant.MYPAGE);
                } else if (Constant.MY_PAGE_MORE_FEEDS.equalsIgnoreCase(this.screenType)) {
                    BaseFragment.tabActivity.pushFragments(R.id.mypageTab, fragment, Constant.OTHERS_PAGE,
                            NewMyPageFragment.newMyPageFragment, Constant.MYPAGE);
                } else if (Constant.MORE_VIDEOS.equalsIgnoreCase(this.screenType)) {
                    BaseFragment.tabActivity.pushFragments(R.id.mypageTab, fragment, Constant.OTHERS_PAGE,
                            NewMyPageFragment.newMyPageFragment, Constant.MYPAGE);
                } else if (Constant.VIDEO_FEEDS.equalsIgnoreCase(this.screenType)) {
                    BaseFragment.tabActivity.pushFragments(R.id.feedTab, fragment, Constant.OTHERS_PAGE,
                            NewMyPageFragment.newMyPageFragment, Constant.HOME);
                }

            } else {
                Alerts.showInfoOnly(NO_USER_ID, this.context);
            }

            break;
        case R.id.deleteImageButton:
            // try {
            final MyPageDto video = (MyPageDto) v.getTag();
            this.currentVideoId = Integer.parseInt(video.getVideoId());

            VideoAsyncTask task = null;
            try {
                task = new VideoAsyncTask(this.context, DELETE, MyPageVideos.getVideoDeleteJsonReq(video.getVideoId(),
                        Config.getUserId()), true);
                task.delegate = MyPageVideos.this;
                task.execute();
            } catch (final JSONException exception) {
                LOG.e(exception);
            }

            break;

        default:
            break;
        }

    }

    @Override
    public void processDone(final boolean status, final String action) {

        if (DELETE.equalsIgnoreCase(action)) {
            NewMyPageFragment.removeView(this.currentVideoId);
        } else if (LIKE.equalsIgnoreCase(action)) {
            this.likeDto.setLiked(true);
            this.optionLikeImageView.setImageResource(R.drawable.loved_new_f);
        } else if (DISLIKE.equalsIgnoreCase(action)) {
            this.optionLikeImageView.setImageResource(R.drawable.loved_new);
            this.likeDto.setLiked(false);
        }
    }

    private void initHolder(final View convertView) {

        this.postThumbnail = (ImageView) convertView.findViewById(R.id.postThumbnail);
        this.playImageButton = (ImageView) convertView.findViewById(R.id.playButtonImage);
        this.tagCount = (TextView) convertView.findViewById(R.id.tagTextView);
        this.loveCount = (TextView) convertView.findViewById(R.id.loveTextView);
        this.commentCount = (TextView) convertView.findViewById(R.id.commentTextView);
        this.firstName = (TextView) convertView.findViewById(R.id.firstUserName);
        this.secondName = (TextView) convertView.findViewById(R.id.secondUserName);
        this.firstComment = (TextView) convertView.findViewById(R.id.firstComment);
        this.secondComment = (TextView) convertView.findViewById(R.id.secondComment);

        this.userDetailsLL = (RelativeLayout) convertView.findViewById(R.id.userDetailsLL);
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

        if (videoInfo.getVideoThumbPath() != null) {

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
            this.dateTextView.setText("| Created: " + videoInfo.getUploadDate());// + " | "
        }
        if (videoInfo.getNumberOfViews() != null) {
            this.viewsTextView.setText(R.string.views + videoInfo.getNumberOfViews());
        }

        if ((videoInfo.getNumberOfTags() != null) && (Integer.parseInt(videoInfo.getNumberOfTags()) > 0)) {
            // tagCount.setText(videoInfo.getNo_of_tags());
            if (Integer.parseInt(videoInfo.getNumberOfTags().trim()) > 1) {
                this.tagCount.setText(videoInfo.getNumberOfTags() + " Tags");
            } else {
                this.tagCount.setText(videoInfo.getNumberOfTags() + " Tag");
            }
        } else {
            this.tagLL.setVisibility(View.GONE);
        }

        if ((videoInfo.getNumberOfLikes() != null) && (Integer.parseInt(videoInfo.getNumberOfLikes()) > 0)) {
            // loveCount.setText(videoInfo.getNo_of_likes()+" Loved");
            if (Integer.parseInt(videoInfo.getNumberOfLikes().trim()) > 1) {
                this.loveCount.setText(videoInfo.getNumberOfLikes() + " Likes");
            } else {
                this.loveCount.setText(videoInfo.getNumberOfLikes() + " Liked");
            }
        } else {
            this.likeLL.setVisibility(View.GONE);
        }

        if ((videoInfo.getNumberOfComments() != null) && (Integer.parseInt(videoInfo.getNumberOfComments()) > 0)) {
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
            // view.setBackground(context.getResources().getDrawable(R.drawable.loved));
            view.setBackgroundResource(R.drawable.loved);

            final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
            lp.setMargins(6, 5, 6, 5);
            view.setLayoutParams(lp);
            this.loveDetailsLL.addView(view);
            for (int i = 0; i < videoInfo.getRecentLikedBy().size(); i++) {
                final RecentLikes recentLikes = videoInfo.getRecentLikedBy().get(i);
                String name = EMPTY;
                if (i < 1) {
                    if (Config.getUserId().equalsIgnoreCase(recentLikes.getUserId())) {
                        name = YOU_;
                    } else {
                        name = recentLikes.getUserName();
                    }
                } else {
                    if (Config.getUserId().equalsIgnoreCase(recentLikes.getUserId())) {
                        name = COMMA + YOU;
                    } else {
                        name = COMMA + recentLikes.getUserName();
                    }
                }

                final TextView textView = new TextView(this.context);

                final InputFilter[] filterArray = new InputFilter[1];
                filterArray[0] = new InputFilter.LengthFilter(14);
                textView.setFilters(filterArray);
                String nameText = EMPTY;
                if (name != null) {
                    name = name.trim();
                    if (name.length() > 12) {
                        nameText = name.substring(0, 11) + ELLIPSIS;
                    } else {
                        nameText = name;
                    }
                }

                textView.setText(nameText);
                // textView.setTextColor(Color.parseColor("#33ccff"));
                textView.setTextColor(this.context.getResources().getColor(R.color.twitter_bg_color));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        this.context.getResources().getDimension(R.dimen.usernametextsize));
                textView.setTextAppearance(this.context, R.style.lovediewStyle);
                textView.setTypeface(Typeface.createFromAsset(this.context.getAssets(),
                        this.context.getString(R.string.app_bold_font_name)));
                textView.setTag(recentLikes);
                textView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(final View view) {

                        final RecentLikes recentLikes = (RecentLikes) view.getTag();
                        if ((recentLikes != null) && (recentLikes.getUserId() != null)) {
                            final OtherUserFragment fragment = new OtherUserFragment(); // object of next fragment
                            final Bundle bundle = new Bundle();

                            if (Constant.MY_PAGE.equalsIgnoreCase(MyPageVideos.this.screenType)) {
                                bundle.putString(USERID, recentLikes.getUserId());
                                bundle.putString(Constant.ROOT_FRAGMENT, Constant.MY_PAGE);
                                fragment.setArguments(bundle);
                                BaseFragment.tabActivity.pushFragments(R.id.mypageTab, fragment, Constant.OTHERS_PAGE,
                                        NewMyPageFragment.newMyPageFragment, Constant.MYPAGE);
                            }

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
                textView.setTag(videoInfo);
                // textView.setTextColor(Color.parseColor("#33ccff"));
                textView.setTextColor(this.context.getResources().getColor(R.color.twitter_bg_color));

                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        this.context.getResources().getDimension(R.dimen.usernametextsize));
                textView.setTextAppearance(this.context, R.style.lovediewStyle);
                textView.setTypeface(Typeface.createFromAsset(this.context.getAssets(),
                        this.context.getString(R.string.app_bold_font_name)));
                textView.setText(" and " + (noOflikes - 2) + " others  Liked");
                textView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(final View v) {

                        final MyPageDto myPageDto2 = (MyPageDto) v.getTag();
                        final int videoId = Integer.parseInt(myPageDto2.getVideoId());
                        if (videoId > 0) {
                            final LikedFragment fragment = new LikedFragment(); // object of next fragment
                            final Bundle bundle = new Bundle();
                            bundle.putString(VIDEOID, myPageDto2.getVideoId());
                            bundle.putString(COUNT, myPageDto2.getNumberOfLikes());
                            fragment.setArguments(bundle);
                            if (Constant.VIDEO_FEEDS.equalsIgnoreCase(MyPageVideos.this.screenType)
                                    && (NewMyPageFragment.newMyPageFragment != null)) {
                                BaseFragment.tabActivity.pushFragments(R.id.feedTab, fragment, Constant.LIKED,
                                        NewMyPageFragment.newMyPageFragment, Constant.HOME);
                            }

                        } else {
                            Alerts.showInfoOnly(INVALID_VIDEO_ID, MyPageVideos.this.context);
                        }

                    }
                });
                this.loveDetailsLL.addView(textView);
            }

        }

        if ((videoInfo.getRecentComments() != null) && !videoInfo.getRecentComments().isEmpty()) {

            if (videoInfo.getRecentComments().size() >= 1) {
                final Comment commentDto = videoInfo.getRecentComments().get(0);
                this.firstName.setTag(commentDto);
                if (Config.getUserId().equalsIgnoreCase(commentDto.getUserId())) {
                    this.firstName.setText(YOU__);
                } else {
                    this.firstName.setText(commentDto.getUserName() + COLON);
                }
                this.firstName.setOnClickListener(this);

                final SpannableString spannable = new SpannableString(commentDto.getComment());
                Util.emotifySpannable(spannable);
                this.firstComment.setText(spannable);

                // firstComment.setText(commentDto.getComment());
            } else {
                this.firstTableRow.setVisibility(View.GONE);
            }

            if (videoInfo.getRecentComments().size() >= 2) {
                final Comment commentDto = videoInfo.getRecentComments().get(1);
                this.secondName.setTag(commentDto);
                if (Config.getUserId().equalsIgnoreCase(commentDto.getUserId())) {
                    this.secondName.setText(YOU__);
                } else {
                    this.secondName.setText(commentDto.getUserName() + COLON);
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

    private void showOptionDialogs(final MyPageDto video) {

        final View view = LayoutInflater.from(this.context).inflate(R.layout.options_list, null);
        final CustomDialog alertDialog = new CustomDialog(this.context, R.style.CustomStyle);
        alertDialog.setContentView(view);
        final WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;

        alertDialog.getWindow().setAttributes(layoutParams);
        alertDialog.show();
        alertDialog.setCanceledOnTouchOutside(false);

        final RelativeLayout delete = (RelativeLayout) view.findViewById(R.id.delete);
        final RelativeLayout updateAccessPermission = (RelativeLayout) view.findViewById(R.id.updateAccessPermission);
        final RelativeLayout reportVideo = (RelativeLayout) view.findViewById(R.id.report);
        final RelativeLayout share = (RelativeLayout) view.findViewById(R.id.share);
        final RelativeLayout copyURL = (RelativeLayout) view.findViewById(R.id.copysahreurl);
        final RelativeLayout tag = (RelativeLayout) view.findViewById(R.id.tag);
        final RelativeLayout cancel = (RelativeLayout) view.findViewById(R.id.cancel);

        reportVideo.setVisibility(View.GONE);
        delete.setVisibility(View.VISIBLE);
        updateAccessPermission.setVisibility(View.VISIBLE);
        updateAccessPermission.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                alertDialog.dismiss();
                // try {
                final VideoDetails currentObj = new VideoDetails();
                currentObj.setVideoID(video.getVideoId());
                currentObj.setVideoURL(video.getVideoUrl());
                currentObj.setVideothumbPath(video.getVideoThumbPath());
                currentObj.setVideoTitle(video.getVideoTitle());
                currentObj.setVideoDesc(video.getVideoDescription());
                currentObj.setShareUrl(video.getShareUrl());
                currentObj.setPublicVideo(video.getPublicVideo());
                final Intent intent = new Intent(MyPageVideos.this.context, AccessPermissionActivity.class);
                intent.putExtra("video", currentObj);
                MyPageVideos.this.context.startActivity(intent);
                // } catch (final Exception e) {
                // LOG.i(MyPageVideos.this.className, "exception " + e.toString());
                // }
            }
        });

        reportVideo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                alertDialog.dismiss();
                if (((video != null) && (video.getVideoId() != null))) {
                    final Intent intent = new Intent(MyPageVideos.this.context, ReportActivity.class);
                    intent.putExtra("video", video);
                    MyPageVideos.this.context.startActivity(intent);

                    /*
                     * currentVideoId = Integer.parseInt(video.getVideoID()); if (currentVideoId > 0) { currentVideoId =
                     * Integer.parseInt(video.getVideoID()); VideoAsyncTask task = new VideoAsyncTask(context,
                     * "report",getVedioReportJsonReq(video));// video.getUserId() task.delegate = MyPageVideos.this;
                     * task.execute(); } else { Alerts.ShowAlertOnly("Info", "No user id", context); }
                     */
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

                        new PlaybackAsync(MyPageVideos.this.context, video.getVideoId()).execute();

                    } else {
                        Alerts.showInfoOnly("No video id", MyPageVideos.this.context);
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
                currentObj.setVideoDesc(video.getVideoDescription());
                if (video.getLatestTagExpression() != null) {
                    currentObj.setLatestTagexpression(video.getLatestTagExpression());
                }
                currentObj.setShareUrl(video.getShareUrl());
                currentObj.setFbShareUrl(video.getFbShareUrl());
                final Intent intent = new Intent(MyPageVideos.this.context, ShareActivity.class);
                intent.putExtra("video", currentObj);
                MyPageVideos.this.context.startActivity(intent);
            }
        });
        copyURL.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                alertDialog.dismiss();
                MyPageVideos.this.currentVideoId = Integer.parseInt(video.getVideoId());
                if (MyPageVideos.this.currentVideoId > 0) {
                    final ClipboardManager ClipMan = (ClipboardManager) MyPageVideos.this.context
                            .getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipMan.setText(video.getShareUrl());
                    Alerts.showInfoOnly("URL has been copied to clipboard.", MyPageVideos.this.context);
                } else {
                    Alerts.showInfoOnly(NO_USER_ID, MyPageVideos.this.context);
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
                MyPageVideos.this.currentVideoId = Integer.parseInt(video.getVideoId());
                if (MyPageVideos.this.currentVideoId > 0) {
                    try {
                        final VideoAsyncTask task = new VideoAsyncTask(MyPageVideos.this.context, DELETE, MyPageVideos
                                .getVideoDeleteJsonReq(video.getVideoId(), Config.getUserId()), true);
                        task.delegate = MyPageVideos.this;
                        task.execute();
                    } catch (final JSONException exception) {
                        LOG.e(exception);
                    }// video.getUserId()
                } else {
                    Alerts.showInfoOnly(NO_USER_ID, MyPageVideos.this.context);
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

}
