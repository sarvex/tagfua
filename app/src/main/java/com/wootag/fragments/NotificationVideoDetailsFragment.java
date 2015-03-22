/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.TagFu.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.TagFu.Constant;
import com.TagFu.R;
import com.TagFu.SeeAllCommentsActivity;
import com.TagFu.adapter.CommentAdapter;
import com.TagFu.async.PlaybackAsync;
import com.TagFu.dto.Comment;
import com.TagFu.dto.ErrorResponse;
import com.TagFu.dto.VideoDetails;
import com.TagFu.dto.VideoProfile;
import com.TagFu.model.Backend;
import com.TagFu.pulltorefresh.PullToRefreshListView;
import com.TagFu.pulltorefresh.PullToRefreshScrollView;
import com.TagFu.ui.Image;
import com.TagFu.util.Alerts;
import com.TagFu.util.Config;
import com.TagFu.util.MainManager;
import com.TagFu.util.VideoActionInterface;

public class NotificationVideoDetailsFragment extends BaseFragment implements VideoActionInterface {

    private static final String COUNT = "count";
    private static final Logger LOG = LoggerManager.getLogger();
    private static final String NO_VIDEO_ID = "No video id";

    private static NotificationVideoDetailsFragment notificationVideoDetailsActivity;

    private static NotificationVideoDetailsFragment videoDetailsActivity;
    private static final String VIDEOID2 = "videoid";
    private static final int VIDEOS_PER_PAGE = 10;
    public String getAllCommentsURL = Constant.COMMON_URL_MOBILE + "getallcomments/";
    private CommentAdapter adapter;
    private LinearLayout commentImageLayout;
    private List<Comment> commentList;
    private PullToRefreshListView commentListView;
    protected Context context;
    protected VideoDetails currentVideo;
    boolean flagLoading;
    boolean hasNext;
    private TextView heading;
    private LayoutInflater inflater;
    protected boolean pullToRefresh;
    private boolean searchRequest;
    protected int likes;
    private LinearLayout lovedImageLayout;
    private PullToRefreshScrollView pullToRefreshScrollView;
    protected int notificationType;
    private TextView ownerName, createdDate, views, noOfTags, noOfLikes, noOfComments;
    private ImageButton play;
    private Button search, menu;
    private EditText searchEdit;
    private RelativeLayout searchLayout;
    private LinearLayout userDetails;
    private TextView videoDescription;
    private LinearLayout videoDetailsView;
    protected String videoId = "";
    protected String userId = "";
    private ImageView videoImage;
    private ImageButton videoPlayButton;
    private List<VideoDetails> videos;
    private ImageView videoThumb, ownerImage;
    private TextView videotitle;
    private View view;

    private static JSONObject getVedioLikeJsonReq(final String videoId) throws JSONException {

        final JSONObject json = new JSONObject();
        json.put(VIDEOID2, videoId);
        json.put("userid", Config.getUserId());
        return json;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        this.view = inflater.inflate(R.layout.notification_video_details, container, false);

        Config.setUserID(MainManager.getInstance().getUserId());
        notificationVideoDetailsActivity = this;

        inflater = (LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = this.getActivity();
        this.hasNext = true;
        videoDetailsActivity = this;
        final Bundle bundle = this.getArguments();
        if (bundle != null) {
            if (bundle.containsKey("video")) {
                this.currentVideo = (VideoDetails) bundle.getSerializable("video");
            }
            if (bundle.containsKey("notificationtype")) {
                this.notificationType = bundle.getInt("notificationtype");
            }
        }
        this.loadViews();
        this.heading = (TextView) this.view.findViewById(R.id.heading);
        this.heading.setText("Video Details");

        new LoadVideoDetails(true).execute();

        this.commentImageLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (NotificationVideoDetailsFragment.this.currentVideo.getVideoID() != null) {
                    final Intent intent = new Intent(NotificationVideoDetailsFragment.this.getActivity(),
                            SeeAllCommentsActivity.class);
                    intent.putExtra(VIDEOID2, NotificationVideoDetailsFragment.this.currentVideo.getVideoID());
                    intent.putExtra(Constant.USERID, NotificationVideoDetailsFragment.this.currentVideo.getUserId());
                    NotificationVideoDetailsFragment.this.context.startActivity(intent);
                } else {
                    Alerts.showInfoOnly(NO_VIDEO_ID, NotificationVideoDetailsFragment.this.getActivity());
                }
            }
        });

        this.lovedImageLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (NotificationVideoDetailsFragment.this.currentVideo.getVideoID() != null) {

                    final LikedFragment fragment = new LikedFragment();
                    final Bundle bundle = new Bundle();
                    bundle.putString(Constant.ROOT_FRAGMENT, Constant.NOTIFICATIONS_PAGE);
                    bundle.putString(VIDEOID2, NotificationVideoDetailsFragment.this.currentVideo.getVideoID());
                    bundle.putString(COUNT, String.valueOf(NotificationVideoDetailsFragment.this.likes));
                    fragment.setArguments(bundle);
                    BaseFragment.tabActivity.pushFragments(R.id.notificationTab, fragment, Constant.LIKED,
                            NotificationVideoDetailsFragment.this, Constant.NOTIFICATIONS);

                } else {
                    Alerts.showInfoOnly(NO_VIDEO_ID, NotificationVideoDetailsFragment.this.context);
                }
            }
        });
        this.play.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (NotificationVideoDetailsFragment.this.currentVideo.getVideoID() != null) {
                    new PlaybackAsync(NotificationVideoDetailsFragment.this.getActivity(),
                            NotificationVideoDetailsFragment.this.currentVideo.getVideoID()).execute();
                } else {
                    Alerts.showInfoOnly(NO_VIDEO_ID, NotificationVideoDetailsFragment.this.context);
                }
            }
        });

        return this.view;
    }

    @Override
    public void processDone(final boolean status, final String action) {

        if (action.equalsIgnoreCase("like")) {
            if (status) {
                final int likes = Integer.parseInt(this.currentVideo.getNumberOfLikes());
                this.noOfLikes.setText((likes + 1));
                // Alerts.ShowAlertOnly("Info", "Liked successfully.", context);
            }
        } else if (action.equalsIgnoreCase("delete")) {

        }

    }

    private void loadViews() {

        this.videoThumb = (ImageView) this.view.findViewById(R.id.videoThumb);
        this.play = (ImageButton) this.view.findViewById(R.id.playVideo);
        this.ownerImage = (ImageView) this.view.findViewById(R.id.videoOwnerImg);
        this.ownerName = (TextView) this.view.findViewById(R.id.videoOwnerName);
        this.createdDate = (TextView) this.view.findViewById(R.id.createdtext);
        this.views = (TextView) this.view.findViewById(R.id.noofviews);
        this.noOfTags = (TextView) this.view.findViewById(R.id.videoTag);
        this.noOfLikes = (TextView) this.view.findViewById(R.id.videoLike);
        this.noOfComments = (TextView) this.view.findViewById(R.id.videoComment);
        this.videoDetailsView = (LinearLayout) this.view.findViewById(R.id.videodetailsView);
        this.lovedImageLayout = (LinearLayout) this.view.findViewById(R.id.loveLL);
        this.commentImageLayout = (LinearLayout) this.view.findViewById(R.id.commentLL);
        this.videotitle = (TextView) this.view.findViewById(R.id.videoTitle);
        this.videoDescription = (TextView) this.view.findViewById(R.id.videoDescription);
        this.menu = (Button) this.view.findViewById(R.id.menu);
        this.search = (Button) this.view.findViewById(R.id.settings);
        this.searchEdit = (EditText) this.view.findViewById(R.id.searchEditText);
        this.searchLayout = (RelativeLayout) this.view.findViewById(R.id.searchRL);
        this.userDetails = (LinearLayout) this.view.findViewById(R.id.userDetails);
        this.searchLayout.setVisibility(View.GONE);
        this.search.setVisibility(View.GONE);
        this.menu.setVisibility(View.GONE);
        final Button back = (Button) this.view.findViewById(R.id.back);
        back.setVisibility(View.VISIBLE);
        this.commentListView = (PullToRefreshListView) this.view.findViewById(R.id.commentlist);
        this.commentList = new ArrayList<Comment>();
        back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                BaseFragment.tabActivity.removeFromBackStack();
                // getActivity().getSupportFragmentManager().popBackStackImmediate();
            }
        });
        this.userDetails.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (NotificationVideoDetailsFragment.this.userId != null) {

                    final OtherUserFragment fragment = new OtherUserFragment();
                    final Bundle bundle = new Bundle();
                    bundle.putString(Constant.ROOT_FRAGMENT, Constant.NOTIFICATIONS_PAGE);
                    bundle.putString("userid", NotificationVideoDetailsFragment.this.userId);
                    fragment.setArguments(bundle);
                    BaseFragment.tabActivity.pushFragments(R.id.notificationTab, fragment, Constant.OTHERS_PAGE,
                            NotificationVideoDetailsFragment.this, Constant.NOTIFICATIONS);

                    // Intent secondUserIntent = new Intent(
                    // getActivity(), OtherUserActivity.class);
                    // secondUserIntent.putExtra("userid", "" +userId);
                    // startActivity(secondUserIntent);
                } else {
                    Alerts.showInfoOnly("User id not available", NotificationVideoDetailsFragment.this.getActivity());
                }

            }
        });

    }

    void loadCommentList(final List<Comment> comments) {

        if (this.pullToRefresh) {
            this.pullToRefresh = false;
            this.commentListView.onRefreshComplete();
            this.commentList.clear();
        }
        if ((comments != null) && (comments.size() > 0)) {
            for (int i = 0; i < comments.size(); i++) {
                this.commentList.add(comments.get(i));
            }
        }
        if (this.adapter == null) {
            this.adapter = new CommentAdapter(this.getActivity(), this.commentList, this.videoId, false, this.userId,
                    Constant.NOTIFICATIONS_PAGE, NotificationVideoDetailsFragment.this);
            this.commentListView.setAdapter(this.adapter);
        } else {
            this.adapter.notifyDataSetChanged();
        }
    }

    void loadVideoData(final VideoDetails video) {

        if (video != null) {
            this.videoDetailsView.setVisibility(View.VISIBLE);
        }
        LOG.i("current videwo id " + video.getVideoID());

        this.videoId = video.getVideoID();
        this.userId = video.getUserId();
        this.currentVideo = video;
        if (video.getVideothumbPath() != null) {
            Image.displayImage(video.getVideothumbPath(), this.getActivity(), this.videoThumb, 1);
        } else {
            this.videoThumb.setImageResource(R.drawable.profile_banner);
        }
        if (video.getPhotoPath() != null) {
            Image.displayImage(video.getPhotoPath(), this.getActivity(), this.ownerImage, 0);
        } else {
            this.ownerImage.setImageResource(R.drawable.member);
        }
        final String ownrName = ((video.getName() == null) ? "Owner Name" : video.getName());
        this.ownerName.setText(ownrName);
        this.createdDate.setText((video.getUploadDate() == null) ? "" : video.getUploadDate());
        this.views.setText((video.getNumberOfViews() == null) ? "0" : video.getNumberOfViews());

        if (video.getNumberOfTags() != null) {
            final int tags = Integer.parseInt(video.getNumberOfTags());
            if (tags > 1) {
                this.noOfTags.setText(video.getNumberOfTags() + " Tags");
            } else {
                this.noOfTags.setText(video.getNumberOfTags() + " Tag");
            }
        }
        if (video.getNumberOfLikes() != null) {
            this.likes = Integer.parseInt(video.getNumberOfLikes());
            if (this.likes > 1) {
                this.noOfLikes.setText(video.getNumberOfLikes() + " Likes");
            } else {
                this.noOfLikes.setText(video.getNumberOfLikes() + " Liked");
            }
        }
        if (video.getNumberOfComments() != null) {
            final int comments = Integer.parseInt(video.getNumberOfComments());
            if (comments > 1) {
                this.noOfComments.setText(video.getNumberOfComments() + " Comments");
            } else {
                this.noOfComments.setText(video.getNumberOfComments() + " Comment");
            }
        }
        this.videotitle.setText((video.getVideoTitle() == null) ? "Video Title" : video.getVideoTitle());
        this.videoDescription.setText((video.getVideoDesc() == null) ? "" : video.getVideoDesc());
        if ((video.getComments() != null) && (video.getComments().size() > 0)) {
            this.commentList.clear();
            // commentList.addAll(video.getComments());
            this.loadCommentList(video.getComments());
        }
        if ((video.getLikes() != null) && (video.getLikes().size() > 0)) {
            this.commentList.clear();
            // commentList.addAll(video.getComments());
            this.loadCommentList(video.getLikes());
        }

    }

    public class LoadVideoDetails extends AsyncTask<Void, Void, Void> {

        private static final String EMPTY = "";
        private static final String NETWORK_PROBLEM_PLEASE_TRY_AGAIN = "Network problem.Please try again";
        private boolean serverRequest;
        private final boolean showProgress;
        private int offset;
        private ProgressDialog pro;
        private Object response;
        private List<VideoProfile> videos;

        public LoadVideoDetails(final boolean showProgress) {

            this.showProgress = showProgress;
        }

        @Override
        protected Void doInBackground(final Void... params) {

            try {
                this.response = Backend.notificationVideoDetails(NotificationVideoDetailsFragment.this.context,
                        NotificationVideoDetailsFragment.this.currentVideo.getVideoID(),
                        NotificationVideoDetailsFragment.this.notificationType);
            } catch (final JSONException exception) {
                LOG.e(exception);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {

            super.onPostExecute(result);
            if (this.showProgress) {
                this.pro.dismiss();
            }
            if (this.response != null) {
                if (this.response instanceof VideoDetails) {
                    final VideoDetails details = (VideoDetails) this.response;
                    if (details != null) {
                        NotificationVideoDetailsFragment.this.loadVideoData(details);
                    }
                } else if (this.response instanceof ErrorResponse) {
                    final ErrorResponse res = (ErrorResponse) this.response;
                    Alerts.showInfoOnly(res.getMessage(), NotificationVideoDetailsFragment.this.getActivity());
                }
            } else {
                Alerts.showInfoOnly(NETWORK_PROBLEM_PLEASE_TRY_AGAIN,
                        NotificationVideoDetailsFragment.this.getActivity());
            }
        }

        @Override
        protected void onPreExecute() {

            if (this.showProgress) {
                this.pro = ProgressDialog.show(NotificationVideoDetailsFragment.this.getActivity(), EMPTY, EMPTY, true);
                this.pro.setContentView(((LayoutInflater) NotificationVideoDetailsFragment.this.context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.progress_bar, null, false));
                this.pro.setCancelable(false);
                this.pro.setCanceledOnTouchOutside(false);
                this.pro.show();
            }
        }
    }

    public class UserListAsyncTask extends AsyncTask<Void, Void, Void> {

        private static final String SLASH = "/";
        List<Comment> currentList;
        int pageNo;
        ProgressDialog progressDialog;

        public UserListAsyncTask(final int pageNo) {

            this.pageNo = pageNo;
        }

        @Override
        protected Void doInBackground(final Void... params) {

            try {
                this.currentList = Backend.getCommentList(NotificationVideoDetailsFragment.this.getActivity(),
                        NotificationVideoDetailsFragment.this.getAllCommentsURL
                                + NotificationVideoDetailsFragment.this.videoId + SLASH + this.pageNo);
            } catch (final JSONException exception) {
                LOG.e(exception);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {

            super.onPostExecute(result);
            NotificationVideoDetailsFragment.this.flagLoading = false;
            if ((this.progressDialog != null) && !NotificationVideoDetailsFragment.this.pullToRefresh) {
                this.progressDialog.dismiss();
            }
            if ((this.currentList != null) && (this.currentList.size() > 0)) {
                NotificationVideoDetailsFragment.this.hasNext = true;
                NotificationVideoDetailsFragment.this.loadCommentList(this.currentList);
            } else {
                NotificationVideoDetailsFragment.this.hasNext = false;
            }

        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            if (!NotificationVideoDetailsFragment.this.pullToRefresh) {
                this.progressDialog = ProgressDialog.show(NotificationVideoDetailsFragment.this.context, "", "", true);
                this.progressDialog
                        .setContentView(((LayoutInflater) NotificationVideoDetailsFragment.this.context
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.progress_bar,
                                null, false));
                this.progressDialog.setCancelable(false);
                this.progressDialog.setCanceledOnTouchOutside(false);
                this.progressDialog.show();
            }

        }
    }

}
