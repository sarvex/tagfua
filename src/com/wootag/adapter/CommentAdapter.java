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
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wootag.Constant;
import com.wootag.R;
import com.wootag.async.DeleteAsync;
import com.wootag.dto.Comment;
import com.wootag.fragments.BaseFragment;
import com.wootag.fragments.OtherUserFragment;
import com.wootag.ui.Image;
import com.wootag.util.Alerts;
import com.wootag.util.Config;
import com.wootag.util.FollowInterface;
import com.wootag.util.Util;

public class CommentAdapter extends BaseAdapter implements FollowInterface {

    private static final String USERID = "userid";

    private static final Logger LOG = LoggerManager.getLogger();

    public Context context;
    public List<Comment> commentList;
    protected final String videoId;
    private final boolean isDeleteComment;
    private final String ownerId;
    protected final Fragment currentFragmnet;
    private final String rootFragmnet;
    protected String deleteCommentId;

    public CommentAdapter(final Context context, final List<Comment> comentList, final String videoID,
            final boolean deleteComment, final String ownerId, final String rootFragmnet, final Fragment currentFragment) {

        this.context = context;
        this.videoId = videoID;
        this.commentList = comentList;
        this.ownerId = ownerId;
        this.isDeleteComment = deleteComment;
        this.currentFragmnet = currentFragment;
        this.rootFragmnet = rootFragmnet;
    }

    @Override
    public void follow(final String type) {

        if (Constant.DELETE_COMMENT.equalsIgnoreCase(type) && (this.commentList != null) && !this.commentList.isEmpty()) {
            for (int i = 0; i < this.commentList.size(); i++) {
                final Comment comment = this.commentList.get(i);
                if (comment.getCommentId().equalsIgnoreCase(this.deleteCommentId)) {
                    this.commentList.remove(comment);
                    this.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public int getCount() {

        return this.commentList.size();
    }

    @Override
    public Object getItem(final int position) {

        return this.commentList.get(position);
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
                    R.layout.comment, null);
            holder = new ViewHolder();
            holder.userImage = (ImageView) convertView.findViewById(R.id.member);
            holder.deleteComment = (ImageView) convertView.findViewById(R.id.deleteComment);
            holder.userName = (TextView) convertView.findViewById(R.id.ownername);
            holder.comment = (TextView) convertView.findViewById(R.id.description);
            holder.commentView = (LinearLayout) convertView.findViewById(R.id.details);
            holder.imageView = (RelativeLayout) convertView.findViewById(R.id.profileImageRL);

            holder.unComment = (ImageView) convertView.findViewById(R.id.deleteComment);
            holder.editComment = (ImageView) convertView.findViewById(R.id.editcomment);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final Comment currentComment = this.commentList.get(position);
        holder.deleteComment.setTag(currentComment);
        holder.imageView.setTag(currentComment);
        holder.commentView.setTag(currentComment);

        if (Config.isPrivateGroupEditMode()) {
            // holder.editComment.setVisibility(View.VISIBLE);

            if (this.isDeleteComment) {
                if (Config.getUserId().equalsIgnoreCase(this.ownerId)) {
                    holder.editComment.setVisibility(View.VISIBLE);
                } else if (Config.getUserId().equalsIgnoreCase(currentComment.getUserId())) {
                    holder.editComment.setVisibility(View.VISIBLE);
                } else {
                    holder.editComment.setVisibility(View.INVISIBLE);
                }
            } else {
                holder.editComment.setVisibility(View.GONE);
            }
            if (currentComment.isEditMode()) {
                holder.editComment.setImageResource(R.drawable.check);
                holder.unComment.setVisibility(View.VISIBLE);

            } else {
                holder.editComment.setImageResource(R.drawable.uncheck);
                holder.unComment.setVisibility(View.GONE);
            }
        } else {
            holder.editComment.setVisibility(View.GONE);
            holder.unComment.setVisibility(View.GONE);
        }
        holder.editComment.setTag(currentComment);
        holder.unComment.setTag(currentComment);
        holder.editComment.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View view) {

                final Comment comment = (Comment) view.getTag();
                if (comment.isEditMode()) {
                    comment.setEditMode(false);
                    CommentAdapter.this.notifyDataSetChanged();
                } else {
                    comment.setEditMode(true);
                    CommentAdapter.this.notifyDataSetChanged();
                }

            }
        });

        if ((this.currentFragmnet != null) && Constant.NOTIFICATIONS_PAGE.equalsIgnoreCase(this.rootFragmnet)) {
            holder.commentView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View view) {

                    final Comment comment = (Comment) view.getTag();
                    if (comment.getUserId() != null) {
                        final OtherUserFragment fragment = new OtherUserFragment();
                        final Bundle bundle = new Bundle();
                        bundle.putString(Constant.ROOT_FRAGMENT, Constant.NOTIFICATIONS_PAGE);
                        bundle.putString(USERID, comment.getUserId());
                        fragment.setArguments(bundle);
                        BaseFragment.tabActivity.pushFragments(R.id.notificationTab, fragment, Constant.OTHERS_PAGE,
                                CommentAdapter.this.currentFragmnet, Constant.NOTIFICATIONS);

                    } else {
                        Alerts.showAlertOnly("Info", "No sender id for this notification", CommentAdapter.this.context);
                    }

                }
            });
            holder.imageView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View view) {

                    final Comment comment = (Comment) view.getTag();
                    if (comment.getUserId() != null) {
                        final OtherUserFragment fragment = new OtherUserFragment();
                        final Bundle bundle = new Bundle();
                        bundle.putString(Constant.ROOT_FRAGMENT, Constant.NOTIFICATIONS_PAGE);
                        bundle.putString(USERID, comment.getUserId());
                        fragment.setArguments(bundle);
                        BaseFragment.tabActivity.pushFragments(R.id.notificationTab, fragment, Constant.OTHERS_PAGE,
                                CommentAdapter.this.currentFragmnet, Constant.NOTIFICATIONS);

                    } else {
                        Alerts.showAlertOnly("Info", "No sender id for this notification", CommentAdapter.this.context);
                    }

                }
            });
        }
        holder.deleteComment.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View view) {

                final Comment comment = (Comment) view.getTag();
                if (comment.getCommentId() != null) {
                    CommentAdapter.this.deleteCommentId = comment.getCommentId();
                    final int id = Integer.parseInt(CommentAdapter.this.deleteCommentId);
                    if (id > 0) {
                        final DeleteAsync task = new DeleteAsync(comment.getCommentId(), CommentAdapter.this.videoId,
                                Constant.DELETE_COMMENT, CommentAdapter.this.context);
                        task.delegate = CommentAdapter.this;

                        task.execute();
                    }
                }
            }
        });
        if (currentComment.getUserPicUrl() != null) {
            Image.displayImage(currentComment.getUserPicUrl(), (Activity) this.context, holder.userImage, 0);
        } else {
            holder.userImage.setImageResource(R.drawable.member);
        }

        if (currentComment.getUserName() != null) {
            holder.userName.setText(currentComment.getUserName());
        }
        if (currentComment.getComment() != null) {
            holder.comment.setVisibility(View.VISIBLE);
            // convert string into spannable and replacede tags with images and set it to comment textview
            final String original = Util.decodeBase64(currentComment.getComment());
            final SpannableString spannable = new SpannableString(original);
            Util.emotifySpannable(spannable);
            holder.comment.setText(spannable);
        } else {
            holder.comment.setVisibility(View.GONE);
        }

        return convertView;

    }

    static class ViewHolder {

        ImageView userImage;
        ImageView deleteComment;
        TextView userName;
        TextView comment;
        LinearLayout commentView;
        RelativeLayout imageView;
        ImageView unComment;
        ImageView editComment;
    }

}
