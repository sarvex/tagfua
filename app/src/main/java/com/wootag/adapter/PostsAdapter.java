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
import com.TagFu.R;
import com.TagFu.ReportActivity;
import com.TagFu.SeeAllCommentsActivity;
import com.TagFu.ShareActivity;
import com.TagFu.async.PlaybackAsync;
import com.TagFu.async.VideoAsyncTask;
import com.TagFu.dto.Comment;
import com.TagFu.dto.MyPageDto;
import com.TagFu.dto.RecentLikes;
import com.TagFu.dto.VideoDetails;
import com.TagFu.fragments.BaseFragment;
import com.TagFu.fragments.LikedFragment;
import com.TagFu.fragments.OtherUserFragment;
import com.TagFu.ui.CustomDialog;
import com.TagFu.ui.Image;
import com.TagFu.ui.RoundedImageView;
import com.TagFu.util.Alerts;
import com.TagFu.util.Config;
import com.TagFu.util.Util;
import com.TagFu.util.VideoActionInterface;

public class PostsAdapter extends ArrayAdapter<MyPageDto> implements OnClickListener, VideoActionInterface {

    private static final String COUNT = "count";

    private static final String COLON = ": ";

    private static final String YOU__ = "You: ";

    private static final String COMMA = ",";

    private static final String _YOU = ",You";

    private static final String YOU_ = "You ";

    private static final String EMPTY = "";

    private static final String DELETED_SUCCESSFULLY = "Deleted successfully";

    private static final Logger LOG = LoggerManager.getLogger();

    private final Fragment currentFragment;
    private final List<MyPageDto> videoInfos;
    private final String screenType;
    protected final Context context;

    private ImageButton deleteImageButton;
    private ImageButton optionsDropDown;
    private ImageView optionLikeImageView;
    private ImageView optionsComment;
    private ImageView optionsLike;
    private ImageView playImageButton;
    private ImageView postCommentImageView;
    private ImageView postLovedImageView;
    private ImageView postTagsImageView;
    private ImageView postThumbnail;
    private LinearLayout commentDetailsLayout;
    private LinearLayout commentLayout;
    private LinearLayout firstTableLayout;
    private LinearLayout likeLayout;
    private LinearLayout loveLayout;
    private LinearLayout secondTableLayout;
    private LinearLayout tagLayout;
    private MyPageDto likeDto;
    private RelativeLayout profileImageView;
    private RelativeLayout userDetailsLL;
    private RoundedImageView profileImage;
    private TextView commentCount;
    private TextView createdDate;
    private TextView dateTextView;
    private TextView firstComment;
    private TextView firstName;
    private TextView loveCount;
    private TextView secondComment;
    private TextView secondName;
    private TextView tagCount;
    private TextView userNameTextView;
    private TextView videoTitleTextView;
    private TextView viewsTextView;
    protected int currentVideoId;

    public PostsAdapter(final Context context, final int textViewResourceId, final List<MyPageDto> videoInfos,
            final String screenType, final Fragment currentFragment) {

        super(context, textViewResourceId, videoInfos);

        this.videoInfos = videoInfos;
        this.screenType = screenType;
        this.context = context;
        this.currentFragment = currentFragment;

    }

    private static JSONObject getVideoLikeJsonReq(final String videoId) throws JSONException {

        final JSONObject json = new JSONObject();
        json.put(Constant.VIDEOID, videoId);
        json.put(Constant.USERID, Config.getUserId());
        return json;

    }

    static JSONObject getVideoDeleteJsonReq(final String videoId, final String userid) throws JSONException {

        final JSONObject json = new JSONObject();
        json.put(Constant.VIDEOID, videoId);
        json.put(Constant.USERID, userid);
        return json;
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

        if (convertView == null) {
            convertView = ((LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.my_post, parent, false);
        }

        this.initHolder(convertView);
        this.custonizeViewOnScreenType(this.getItem(position));
        this.setPostDetails(this.getItem(position));

        convertView.setId(Integer.parseInt(this.getItem(position).getVideoId()));

        return convertView;
    }

    @Override
    public void onClick(final View view) {

        switch (view.getId()) {
        case R.id.commentImageView:
            this.onCommentImageClick(view);
            break;

        case R.id.optionsButton:
            this.onOptionClick(view);
            break;

        case R.id.likeImageView:
            this.onLikeImageClick(view);
            break;

        case R.id.playButtonImage:
            this.onPlayClick(view);
            break;

        case R.id.commentLL:
            this.onCommentImageClick(view);
            break;

        case R.id.loveLL:
            this.onLoveClick(view);
            break;

        case R.id.postTagsImageView:
            break;

        case R.id.firstUserName:
            this.onFirstUserNameClick(view);
            break;

        case R.id.secondUserName:
            this.onSecondUserNameClick(view);
            break;

        case R.id.deleteImageButton:
            this.onDeleteImageClick(view);
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
            Alerts.showInfoOnly(DELETED_SUCCESSFULLY, this.context);

        } else if (Constant.LIKE.equalsIgnoreCase(action)) {
            this.likeDto.setLiked(true);
            this.optionLikeImageView.setImageResource(R.drawable.loved_new_f);

        } else if (Constant.DISLIKE.equalsIgnoreCase(action)) {
            this.optionLikeImageView.setImageResource(R.drawable.loved_new);
            this.likeDto.setLiked(false);
        }
    }

    /**
     * customizing screen based on screen type parameter (set visibility and gone for views)
     */
    private void custonizeViewOnScreenType(final MyPageDto myPage) {

        if (this.screenType == Constant.VIDEO_FEEDS) {
            this.viewsTextView.setVisibility(View.GONE);
            this.dateTextView.setVisibility(View.GONE);
            this.createdDate.setVisibility(View.VISIBLE);
            this.userDetailsLL.setVisibility(View.VISIBLE);
            this.profileImageView.setTag(myPage);
            this.userDetailsLL.setTag(myPage);
            this.userNameTextView.setTag(myPage);
            this.userDetailsLL.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View view) {

                    final MyPageDto videoInfo = (MyPageDto) view.getTag();
                    if (!Config.getUserId().equalsIgnoreCase(videoInfo.getUserId())) {
                        final int id = Integer.parseInt(videoInfo.getUserId());
                        if (id > 0) {
                            PostsAdapter.this.gotToOtherPage(id);
                        } else {
                            Alerts.showInfoOnly(Constant.NO_USER_ID_AVAILABLE_FOR_THIS_USER, PostsAdapter.this.context);
                        }
                    }
                }
            });
            this.userNameTextView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View view) {

                    final MyPageDto videoInfo = (MyPageDto) view.getTag();
                    if (!Config.getUserId().equalsIgnoreCase(videoInfo.getUserId())) {
                        final int id = Integer.parseInt(videoInfo.getUserId());
                        if (id > 0) {
                            PostsAdapter.this.gotToOtherPage(id);
                        } else {
                            Alerts.showInfoOnly(Constant.NO_USER_ID_AVAILABLE_FOR_THIS_USER, PostsAdapter.this.context);
                        }
                    }
                }
            });
            if (!Strings.isNullOrEmpty(myPage.getUserPickUrl())) {
                Image.displayImage(myPage.getUserPickUrl(), (Activity) this.context, this.profileImage, 0);

            } else {
                this.profileImage.setImageResource(R.drawable.member);
            }

            if (myPage.getUserName() != null) {
                this.userNameTextView.setText(myPage.getUserName());
            } else {
                this.userNameTextView.setText(EMPTY);
            }

        } else if (this.screenType == Constant.MY_PAGE_MORE_FEEDS) {

            this.viewsTextView.setVisibility(View.VISIBLE);
            this.dateTextView.setVisibility(View.VISIBLE);
            this.createdDate.setVisibility(View.GONE);
            this.userDetailsLL.setVisibility(View.GONE);
            this.profileImageView.setTag(myPage);
            this.userDetailsLL.setTag(myPage);
            this.userNameTextView.setTag(myPage);

            this.userDetailsLL.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View view) {

                    final MyPageDto videoInfo = (MyPageDto) view.getTag();
                    if (!Config.getUserId().equalsIgnoreCase(videoInfo.getUserId())) {
                        final int id = Integer.parseInt(videoInfo.getUserId());
                        if (id > 0) {
                            PostsAdapter.this.gotToOtherPage(id);
                        } else {
                            Alerts.showInfoOnly(Constant.NO_USER_ID_AVAILABLE_FOR_THIS_USER, PostsAdapter.this.context);
                        }
                    }
                }
            });
            this.userNameTextView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View view) {

                    final MyPageDto videoInfo = (MyPageDto) view.getTag();
                    if (!Config.getUserId().equalsIgnoreCase(videoInfo.getUserId())) {
                        final int id = Integer.parseInt(videoInfo.getUserId());
                        if (id > 0) {
                            PostsAdapter.this.gotToOtherPage(id);
                        } else {
                            Alerts.showInfoOnly(Constant.NO_USER_ID_AVAILABLE_FOR_THIS_USER, PostsAdapter.this.context);
                        }
                    }
                }
            });
            if (!Strings.isNullOrEmpty(myPage.getUserPickUrl())) {
                Image.displayImage(myPage.getUserPickUrl(), (Activity) this.context, this.profileImage, 0);

            } else {
                this.profileImage.setImageResource(R.drawable.member);
            }

            if (myPage.getUserName() != null) {
                this.userNameTextView.setText(myPage.getUserName());
            } else {
                this.userNameTextView.setText(EMPTY);
            }

        } else if (this.screenType == Constant.MORE_VIDEOS) {
            this.deleteImageButton.setVisibility(View.GONE);
        } else {
            this.deleteImageButton.setVisibility(View.GONE);
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
        this.profileImageView = (RelativeLayout) convertView.findViewById(R.id.profileImageRL);
        this.userNameTextView = (TextView) convertView.findViewById(R.id.userNameTextView);
        this.postCommentImageView = (ImageView) convertView.findViewById(R.id.postCommentImageView);
        this.postLovedImageView = (ImageView) convertView.findViewById(R.id.postLovedImageView);
        this.postTagsImageView = (ImageView) convertView.findViewById(R.id.postTagsImageView);
        this.videoTitleTextView = (TextView) convertView.findViewById(R.id.videoTitleTextView);
        this.dateTextView = (TextView) convertView.findViewById(R.id.dateTextView);
        this.createdDate = (TextView) convertView.findViewById(R.id.videocreated);
        this.viewsTextView = (TextView) convertView.findViewById(R.id.viewsTextView);
        this.loveLayout = (LinearLayout) convertView.findViewById(R.id.loveDetailsLL);
        this.firstTableLayout = (LinearLayout) convertView.findViewById(R.id.firstTableRow);
        this.secondTableLayout = (LinearLayout) convertView.findViewById(R.id.secondTableRow);
        this.commentDetailsLayout = (LinearLayout) convertView.findViewById(R.id.commentDetailsLL);
        this.tagLayout = (LinearLayout) convertView.findViewById(R.id.tagLL);
        this.likeLayout = (LinearLayout) convertView.findViewById(R.id.loveLL);
        this.commentLayout = (LinearLayout) convertView.findViewById(R.id.commentLL);

        this.optionsLike = (ImageView) convertView.findViewById(R.id.likeImageView);
        this.optionsComment = (ImageView) convertView.findViewById(R.id.commentImageView);
        this.optionsDropDown = (ImageButton) convertView.findViewById(R.id.optionsButton);
    }

    /**
     * @param view
     */
    private void onCommentImageClick(final View view) {

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
    }

    /**
     * @param view
     * @throws NumberFormatException
     */
    private void onDeleteImageClick(final View view) throws NumberFormatException {

        final MyPageDto video = (MyPageDto) view.getTag();
        this.currentVideoId = Integer.parseInt(video.getVideoId());
        if (this.currentVideoId > 0) {
            try {
                final VideoAsyncTask task = new VideoAsyncTask(this.context, Constant.DELETE,
                        PostsAdapter.getVideoDeleteJsonReq(video.getVideoId(), Config.getUserId()), true);
                task.delegate = PostsAdapter.this;
                task.execute();
            } catch (final JSONException exception) {
                LOG.e(exception);
            }
        } else {
            Alerts.showInfoOnly(Constant.NO_USER_ID, this.context);
        }
    }

    /**
     * @param view
     * @throws NumberFormatException
     */
    private void onFirstUserNameClick(final View view) throws NumberFormatException {

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
        } else {
            Alerts.showInfoOnly(Constant.NO_USER_ID, this.context);
        }
    }

    /**
     * @param view
     * @throws NumberFormatException
     */
    private void onLikeImageClick(final View view) throws NumberFormatException {

        this.likeDto = (MyPageDto) view.getTag();
        this.optionLikeImageView = (ImageView) view;
        if (this.likeDto.getVideoId() != null) {
            final int videoId = Integer.parseInt(this.likeDto.getVideoId());
            if (videoId > 0) {
                if (!this.likeDto.hasLiked()) {
                    try {
                        final VideoAsyncTask asyncTask = new VideoAsyncTask(this.context, Constant.LIKE,
                                PostsAdapter.getVideoLikeJsonReq(String.valueOf(videoId)), false);
                        asyncTask.delegate = PostsAdapter.this;
                        asyncTask.execute();
                    } catch (final JSONException exception) {
                        LOG.e(exception);
                    }
                } else {
                    try {
                        VideoAsyncTask asyncTask;
                        asyncTask = new VideoAsyncTask(this.context, Constant.DISLIKE,
                                PostsAdapter.getVideoLikeJsonReq(String.valueOf(videoId)), false);
                        asyncTask.delegate = PostsAdapter.this;
                        asyncTask.execute();
                    } catch (final JSONException exception) {
                        LOG.e(exception);
                    }
                }

            } else {
                Alerts.showInfoOnly(Constant.INVALID_VIDEO_ID, this.context);
            }
        }
    }

    /**
     * @param view
     * @throws NumberFormatException
     */
    private void onLoveClick(final View view) throws NumberFormatException {

        final MyPageDto myPageDto2 = (MyPageDto) view.getTag();
        if (myPageDto2.getVideoId() != null) {
            final int videoId = Integer.parseInt(myPageDto2.getVideoId());
            if (videoId > 0) {
                this.gotoLikePage(String.valueOf(videoId), myPageDto2.getNumberOfLikes());

            } else {
                Alerts.showInfoOnly(Constant.INVALID_VIDEO_ID, this.context);
            }
        }
    }

    /**
     * @param view
     */
    private void onOptionClick(final View view) {

        final MyPageDto options = (MyPageDto) view.getTag();
        this.showOptionDialogs(options);
    }

    /**
     * @param view
     * @throws NumberFormatException
     */
    private void onPlayClick(final View view) throws NumberFormatException {

        final MyPageDto videoInfo = (MyPageDto) view.getTag();
        this.currentVideoId = Integer.parseInt(videoInfo.getVideoId());
        new PlaybackAsync(this.context, videoInfo.getVideoId()).execute();
    }

    /**
     * @param view
     * @throws NumberFormatException
     */
    private void onSecondUserNameClick(final View view) throws NumberFormatException {

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
    }

    private void setPostDetails(final MyPageDto videoInfo) {

        this.commentLayout.setTag(videoInfo);
        this.commentLayout.setOnClickListener(this);
        this.likeLayout.setTag(videoInfo);
        this.likeLayout.setOnClickListener(this);
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
            final String title = videoInfo.getVideoTitle();
            /*
             * if (videoInfo.getVideoTitle().length() > 10) { title = videoInfo.getVideoTitle().substring(0, 9); }
             */

            if (this.screenType == Constant.VIDEO_FEEDS) {
                final InputFilter[] filterArray = new InputFilter[1];
                filterArray[0] = new InputFilter.LengthFilter(50);
                this.videoTitleTextView.setFilters(filterArray);
                this.videoTitleTextView.setTypeface(Typeface.createFromAsset(this.context.getAssets(),
                        this.context.getString(R.string.app_italic_font_name)));
                if (!Strings.isNullOrEmpty(videoInfo.getLatestTagExpression())) {
                    this.videoTitleTextView.setText(videoInfo.getLatestTagExpression());
                } else {
                    this.videoTitleTextView.setText(title);
                }
            } else {
                this.videoTitleTextView.setText(title);
            }
        }
        if (videoInfo.getUploadDate() != null) {
            this.dateTextView.setText("| Created: " + videoInfo.getUploadDate());
        }
        if (videoInfo.getUploadDate() != null) {
            this.createdDate.setText(videoInfo.getUploadDate());
        }
        if (videoInfo.getNumberOfViews() != null) {
            this.viewsTextView.setText(R.string.views + videoInfo.getNumberOfViews());
        }

        if ((videoInfo.getNumberOfTags() != null) && (Integer.parseInt(videoInfo.getNumberOfTags().trim()) > 0)) {
            this.tagLayout.setVisibility(View.VISIBLE);
            if (Integer.parseInt(videoInfo.getNumberOfTags().trim()) > 1) {
                this.tagCount.setText(videoInfo.getNumberOfTags() + " Tags");
            } else {
                this.tagCount.setText(videoInfo.getNumberOfTags() + " Tag");
            }
        } else {
            this.tagLayout.setVisibility(View.GONE);
        }

        if ((videoInfo.getNumberOfLikes() != null) && (Integer.parseInt(videoInfo.getNumberOfLikes().trim()) > 0)) {
            this.likeLayout.setVisibility(View.VISIBLE);

            if (Integer.parseInt(videoInfo.getNumberOfLikes().trim()) > 1) {
                this.loveCount.setText(videoInfo.getNumberOfLikes() + " Likes");
            } else {
                this.loveCount.setText(videoInfo.getNumberOfLikes() + " Liked");
            }

            // loveCount.setText(videoInfo.getNo_of_likes()+" Loved");
        } else {
            this.likeLayout.setVisibility(View.GONE);
        }

        if ((videoInfo.getNumberOfComments() != null) && (Integer.parseInt(videoInfo.getNumberOfComments().trim()) > 0)) {
            this.commentLayout.setVisibility(View.VISIBLE);
            // commentCount.setText(videoInfo.getNo_of_comments());
            if (Integer.parseInt(videoInfo.getNumberOfComments().trim()) > 1) {
                this.commentCount.setText(videoInfo.getNumberOfComments() + " Comments");
            } else {
                this.commentCount.setText(videoInfo.getNumberOfComments() + " Comment");
            }
        } else {
            this.commentLayout.setVisibility(View.GONE);
        }

        this.loveLayout.removeAllViews();

        if ((videoInfo.getRecentLikedBy() != null) && !videoInfo.getRecentLikedBy().isEmpty()) {
            this.loveLayout.setVisibility(View.VISIBLE);
            final ImageView view = new ImageView(this.context);
            view.setBackgroundResource(R.drawable.loved);
            final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
            lp.setMargins(6, 5, 6, 5);
            view.setLayoutParams(lp);
            this.loveLayout.addView(view);
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
                        name = _YOU;
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
                        nameText = name.substring(0, 11) + "...";
                    } else {
                        nameText = name;
                    }
                }

                textView.setText(nameText);// name
                textView.setTag(videoInfo.getRecentLikedBy().get(i));
                textView.setTextColor(this.context.getResources().getColor(R.color.twitter_bg_color));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        this.context.getResources().getDimension(R.dimen.usernametextsize));
                // textView.setTextColor(Color.parseColor("#33ccff"));
                textView.setTag(recentLikes);
                textView.setTextAppearance(this.context, R.style.lovediewStyle);
                textView.setTypeface(Typeface.createFromAsset(this.context.getAssets(),
                        this.context.getString(R.string.app_bold_font_name)));

                textView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(final View v) {

                        final RecentLikes recentLikes = (RecentLikes) v.getTag();
                        if (recentLikes.getUserId() != null) {
                            final int id = Integer.parseInt(recentLikes.getUserId());
                            if (id > 0) {
                                PostsAdapter.this.gotToOtherPage(id);
                            }
                        } else {
                            Alerts.showInfoOnly(Constant.NO_USER_ID, PostsAdapter.this.context);
                        }
                    }
                });
                this.loveLayout.addView(textView);
                if (i == 1) {
                    break;
                }
            }
            final int noOflikes = Integer.parseInt(videoInfo.getNumberOfLikes().trim());
            if (noOflikes > 2) {// videoInfo.getRecentLikedBy().size()
                final TextView textView = new TextView(this.context);
                textView.setTag(videoInfo);
                // textView.setTextColor(Color.parseColor("#33ccff"));
                textView.setTextColor(this.context.getResources().getColor(R.color.twitter_bg_color));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        this.context.getResources().getDimension(R.dimen.usernametextsize));
                textView.setText(" and " + (noOflikes - 2) + " others  Liked");// getRecentLikedBy().size()
                textView.setTextAppearance(this.context, R.style.lovediewStyle);
                textView.setTypeface(Typeface.createFromAsset(this.context.getAssets(),
                        this.context.getString(R.string.app_bold_font_name)));
                textView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(final View v) {

                        // TODO Show the loved list
                        final MyPageDto myPageDto2 = (MyPageDto) v.getTag();
                        final int videoId = Integer.parseInt(myPageDto2.getVideoId());
                        if (videoId > 0) {
                            PostsAdapter.this.gotoLikePage(myPageDto2.getVideoId(), myPageDto2.getNumberOfLikes());
                        } else {
                            Alerts.showInfoOnly(Constant.INVALID_VIDEO_ID, PostsAdapter.this.context);
                        }

                    }
                });
                this.loveLayout.addView(textView);
            }

        } else {
            this.loveLayout.setVisibility(View.GONE);
        }

        if ((videoInfo.getRecentComments() != null) && !videoInfo.getRecentComments().isEmpty()) {

            if (videoInfo.getRecentComments().size() >= 1) {
                this.firstTableLayout.setVisibility(View.VISIBLE);
                this.secondTableLayout.setVisibility(View.GONE);
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

            } else {
                this.firstTableLayout.setVisibility(View.GONE);
                this.secondTableLayout.setVisibility(View.GONE);
            }

            if (videoInfo.getRecentComments().size() >= 2) {
                this.firstTableLayout.setVisibility(View.VISIBLE);
                this.secondTableLayout.setVisibility(View.VISIBLE);
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
                this.secondTableLayout.setVisibility(View.GONE);
            }

        } else {
            this.commentDetailsLayout.setVisibility(View.GONE);
        }

    }

    /**
     * showing options dialog while click on option button of video
     *
     * @param current video object
     */
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
        final RelativeLayout reportVideo = (RelativeLayout) view.findViewById(R.id.report);
        final RelativeLayout share = (RelativeLayout) view.findViewById(R.id.share);
        final RelativeLayout copyURL = (RelativeLayout) view.findViewById(R.id.copysahreurl);
        final RelativeLayout tag = (RelativeLayout) view.findViewById(R.id.tag);
        final RelativeLayout cancel = (RelativeLayout) view.findViewById(R.id.cancel);
        final RelativeLayout updateAccessPermission = (RelativeLayout) view.findViewById(R.id.updateAccessPermission);

        if (Constant.MY_PAGE_MORE_FEEDS.equalsIgnoreCase(this.screenType)) {
            delete.setVisibility(View.VISIBLE);
            updateAccessPermission.setVisibility(View.VISIBLE);
            tag.setVisibility(View.VISIBLE);
            reportVideo.setVisibility(View.GONE);
        } else if (Config.getUserId().equalsIgnoreCase(video.getUserId())) {
            delete.setVisibility(View.VISIBLE);
            updateAccessPermission.setVisibility(View.VISIBLE);
            tag.setVisibility(View.VISIBLE);
            reportVideo.setVisibility(View.GONE);
        } else {
            delete.setVisibility(View.GONE);
            tag.setVisibility(View.GONE);
            updateAccessPermission.setVisibility(View.GONE);
            reportVideo.setVisibility(View.VISIBLE);
        }
        if ((video.getPublicVideo() == 0) && !(Constant.MY_PAGE_MORE_FEEDS.equalsIgnoreCase(this.screenType))
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
                final Intent intent = new Intent(PostsAdapter.this.context, AccessPermissionActivity.class);
                intent.putExtra("video", currentObj);
                PostsAdapter.this.context.startActivity(intent);
            }
        });
        reportVideo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                alertDialog.dismiss();
                if (((video != null) && (video.getVideoId() != null))) {
                    PostsAdapter.this.currentVideoId = Integer.parseInt(video.getVideoId());
                    if (PostsAdapter.this.currentVideoId > 0) {
                        /*
                         * currentVideoId = Integer.parseInt(video.getVideoID()); VideoAsyncTask task = new
                         * VideoAsyncTask(context, "report",getVedioReportJsonReq(video));// video.getUserId()
                         * task.delegate = PostsAdapter.this; task.execute();
                         */
                        final Intent intent = new Intent(PostsAdapter.this.context, ReportActivity.class);
                        intent.putExtra("video", video);
                        PostsAdapter.this.context.startActivity(intent);

                    } else {
                        Alerts.showInfoOnly(Constant.NO_USER_ID, PostsAdapter.this.context);
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
                        new PlaybackAsync(PostsAdapter.this.context, video.getVideoId()).execute();
                    } else {
                        Alerts.showInfoOnly(Constant.NO_VIDEO_ID, PostsAdapter.this.context);
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
                currentObj.setShareUrl(video.getShareUrl());
                currentObj.setFbShareUrl(video.getFbShareUrl());
                if (video.getLatestTagExpression() != null) {
                    currentObj.setLatestTagexpression(video.getLatestTagExpression());
                }
                final Intent intent = new Intent(PostsAdapter.this.context, ShareActivity.class);
                intent.putExtra("video", currentObj);
                PostsAdapter.this.context.startActivity(intent);
            }
        });
        copyURL.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                alertDialog.dismiss();
                PostsAdapter.this.currentVideoId = Integer.parseInt(video.getVideoId());
                if (PostsAdapter.this.currentVideoId > 0) {
                    final ClipboardManager ClipMan = (ClipboardManager) PostsAdapter.this.context
                            .getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipMan.setText(video.getShareUrl());
                    Alerts.showInfoOnly("URL has been copied to clipboard.", PostsAdapter.this.context);
                } else {
                    Alerts.showInfoOnly(Constant.NO_USER_ID, PostsAdapter.this.context);
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
                PostsAdapter.this.currentVideoId = Integer.parseInt(video.getVideoId());
                if (PostsAdapter.this.currentVideoId > 0) {
                    try {
                        final VideoAsyncTask task = new VideoAsyncTask(PostsAdapter.this.context, Constant.DELETE,
                                PostsAdapter.getVideoDeleteJsonReq(video.getVideoId(), Config.getUserId()), true);
                        task.delegate = PostsAdapter.this;
                        task.execute();
                    } catch (final JSONException exception) {
                        LOG.e(exception);
                    }
                } else {
                    Alerts.showInfoOnly(Constant.NO_VIDEO_ID, PostsAdapter.this.context);
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
        bundle.putString(COUNT, likes);

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
        }

    }

    /**
     * Navigation for other user profile
     *
     * @param id other userid
     */
    void gotToOtherPage(final int id) {

        final OtherUserFragment fragment = new OtherUserFragment(); // object of next fragment
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
        }
    }

}
