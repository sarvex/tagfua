/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wTagFufragments;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Strings;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.woTagFuonstant;
import com.wooTagFunuActivity;
import com.wootTagFuimport com.wootaTagFurtActivity;
import com.wootagTagFulCommentsActivity;
import com.wootag.TagFuctivity;
import com.wootag.aTagFulaybackAsync;
import com.wootag.asTagFudeoAsyncTask;
import com.wootag.dtoTagFuResponse;
import com.wootag.dto.TagFudeos;
import com.wootag.dto.MTagFuto;
import com.wootag.dto.ViTagFuails;
import com.wootag.dto.VidTagFuile;
import com.wootag.model.BaTagFu
import com.wootag.pulltorefTagFuullToRefreshBase;
import com.wootag.pulltorefrTagFullToRefreshBase.OnRefreshListener;
import com.wootag.pulltorefreTagFulToRefreshScrollView;
import com.wootag.slideout.SliTagFuctivity;
import com.wootag.ui.Image;
impTagFum.wootag.util.Alerts;
iTagFucom.wootag.util.Config;
imTagFuom.wootag.util.MainManagerTagFurt com.wootag.util.Util;
importTagFuootag.util.VideoActionInterface;

public class VideoDetailsFragment extends BaseFragment implements VideoActionInterface {

    private static final String DELETE = "delete";

    private static final String LIKED_SUCCESSFULLY = "Liked successfully.";

    private static final String LIKE2 = "like";

    protected static final Logger LOG = LoggerManager.getLogger();

    private static final String NO_VIDEO_ID = "No video id";
    private static final int PAGE_SIZE = 10;

    public static VideoDetailsFragment videoDetailsActivity;

    protected Button previousButton;
    protected Button next;
    private Button remember;
    private Button search;
    private Button menu;
    protected Context context;
    private EditText searchEdit;
    protected HorizontalScrollView otherStuffScrollView;
    private ImageButton play;
    private ImageButton videoPlayButton;
    private ImageView share, like, comment;
    private ImageView videoImage;
    private ImageView videoThumb, ownerImage;
    protected LayoutInflater inflater;
    private LinearLayout commentImageLayout;
    private LinearLayout lovedImageLayout;
    private LinearLayout publicVideoLayout;
    private LinearLayout userDetails;
    private LinearLayout videoDetailsView;
    protected List<MoreVideos> userPublicVideos;
    protected List<VideoDetails> videos;
    private Object response;
    protected PullToRefreshScrollView pullToRefreshScrollView;
    private RelativeLayout searchLayout;
    protected String noOfLikesForCurrentVideo;
    protected String userId = "";
    private TextView createdDate;
    protected TextView errorMessageTextView;
    private TextView heading;
    private TextView noOfComments;
    private TextView noOfLikes;
    private TextView noOfTags;
    private TextView ownerName;
    private TextView videoDescription;
    private TextView videotitle;
    private TextView views;
    protected VideoDetails currentVideo;
    private View view;
    private boolean hasNext;
    private boolean hasPrevious;
    protected boolean loading;
    protected boolean pullToRefresh;
    private boolean singleVideo;
    protected int currentVideoNo;
    protected int totalNo;
    private int noOfVideos;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        this.view = inflater.inflate(R.layout.activity_video_details, container, false);
        Config.setUserID(MainManager.getInstance().getUserId());

        this.lovedImageLayout = (LinearLayout) this.view.findViewById(R.id.loveLL);
        this.commentImageLayout = (LinearLayout) this.view.findViewById(R.id.commentLL);

        videoDetailsActivity = this;
        this.inflater = inflater;
        this.context = this.getActivity();

        final Bundle bn = this.getArguments();
        if (bn != null) {
            if (bn.containsKey("navigation")) {
                if (bn.containsKey(Constant.VIDEO)) {
                    this.singleVideo = true;
                    this.currentVideo = (VideoDetails) bn.getSerializable(Constant.VIDEO);
                }
            } else {
                this.singleVideo = false;
                if (bn.containsKey("totalno")) {
                    this.totalNo = bn.getInt("totalno");
                }
                if (bn.containsKey("videoNo")) {
                    this.currentVideoNo = bn.getInt("videoNo");
                }
                if (bn.containsKey("videos")) {
                    this.videos = (ArrayList<VideoDetails>) bn.getSerializable("videos");
                }
                this.currentVideo = this.videos.get(this.currentVideoNo);
            }
        }

        // currentVideo = videos.get(currentVideoNo);
        this.loadViews();
        this.heading = (TextView) this.view.findViewById(R.id.heading);
        // heading.setText("Video Details");
        this.publicVideoLayout = (LinearLayout) this.view.findViewById(R.id.userPublicvideos);
        this.userPublicVideos = new ArrayList<MoreVideos>();

        new LoadVideoDetails(true).execute();

        this.pullToRefreshScrollView = (PullToRefreshScrollView) this.view.findViewById(R.id.videodetailsScroll);
        this.pullToRefreshScrollView.setOnRefreshListener(new OnRefreshListener<ScrollView>() {

            @Override
            public void onRefresh(final PullToRefreshBase<ScrollView> refreshView) {

                VideoDetailsFragment.this.pullToRefresh = true;
                new LoadVideoDetails(false).execute();

            }
        });

        // ScrollView mScrollView = mPullRefreshScrollView.getRefreshableView();

        this.menu.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                final int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60,
                        VideoDetailsFragment.this.getResources().getDisplayMetrics());
                SlideoutActivity.prepare(VideoDetailsFragment.this.getActivity(), R.id.detailsView, width);
                VideoDetailsFragment.this.startActivity(new Intent(VideoDetailsFragment.this.getActivity(),
                        MenuActivity.class));
                VideoDetailsFragment.this.getActivity().overridePendingTransition(0, 0);
            }
        });

        this.previousButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                VideoDetailsFragment.this.currentVideoNo = VideoDetailsFragment.this.currentVideoNo - 1;
                if (VideoDetailsFragment.this.videos.get(VideoDetailsFragment.this.currentVideoNo) != null) {
                    VideoDetailsFragment.this.currentVideo = VideoDetailsFragment.this.videos
                            .get(VideoDetailsFragment.this.currentVideoNo);
                    VideoDetailsFragment.this.next.setVisibility(View.VISIBLE);
                    if (VideoDetailsFragment.this.currentVideoNo == 0) {
                        VideoDetailsFragment.this.previousButton.setVisibility(View.GONE);
                        new LoadVideoDetails(true).execute();
                    } else {
                        new LoadVideoDetails(true).execute();
                    }
                }
            }
        });
        this.next.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                // try {
                VideoDetailsFragment.this.currentVideoNo = VideoDetailsFragment.this.currentVideoNo + 1;
                if (VideoDetailsFragment.this.videos.get(VideoDetailsFragment.this.currentVideoNo) != null) {
                    VideoDetailsFragment.this.currentVideo = VideoDetailsFragment.this.videos
                            .get(VideoDetailsFragment.this.currentVideoNo);
                    VideoDetailsFragment.this.previousButton.setVisibility(View.VISIBLE);
                    if (VideoDetailsFragment.this.currentVideoNo == (VideoDetailsFragment.this.totalNo - 1)) {
                        VideoDetailsFragment.this.next.setVisibility(View.GONE);
                        new LoadVideoDetails(true).execute();
                    } else {
                        new LoadVideoDetails(true).execute();
                    }
                }
            }
        });
        this.share.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                final Intent intent = new Intent(VideoDetailsFragment.this.getActivity(), ShareActivity.class);
                intent.putExtra(Constant.VIDEO, VideoDetailsFragment.this.currentVideo);
                VideoDetailsFragment.this.startActivity(intent);

            }
        });
        this.remember.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (((VideoDetailsFragment.this.currentVideo != null) && (VideoDetailsFragment.this.currentVideo
                        .getVideoID() != null))) {
                    final int currentVideoId = Integer.parseInt(VideoDetailsFragment.this.currentVideo.getVideoID());
                    if (currentVideoId > 0) {
                        final MyPageDto video = new MyPageDto();
                        video.setVideoId(VideoDetailsFragment.this.currentVideo.getVideoID());
                        final Intent intent = new Intent(VideoDetailsFragment.this.context, ReportActivity.class);
                        intent.putExtra(Constant.VIDEO, video);
                        VideoDetailsFragment.this.context.startActivity(intent);
                    } else {
                        Alerts.showInfoOnly("No user id", VideoDetailsFragment.this.context);
                    }
                }

            }
        });
        this.comment.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (VideoDetailsFragment.this.currentVideo.getVideoID() != null) {
                    final Intent intent = new Intent(VideoDetailsFragment.this.context, SeeAllCommentsActivity.class);
                    intent.putExtra(Constant.VIDEOID, VideoDetailsFragment.this.currentVideo.getVideoID());
                    intent.putExtra(Constant.USERID, VideoDetailsFragment.this.currentVideo.getUserId());
                    VideoDetailsFragment.this.context.startActivity(intent);
                } else {
                    Alerts.showInfoOnly(NO_VIDEO_ID, VideoDetailsFragment.this.context);
                }
            }
        });
        this.commentImageLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (VideoDetailsFragment.this.currentVideo.getVideoID() != null) {
                    final Intent intent = new Intent(VideoDetailsFragment.this.context, SeeAllCommentsActivity.class);
                    intent.putExtra(Constant.VIDEOID, VideoDetailsFragment.this.currentVideo.getVideoID());
                    intent.putExtra(Constant.USERID, VideoDetailsFragment.this.currentVideo.getUserId());

                    VideoDetailsFragment.this.context.startActivity(intent);
                } else {
                    Alerts.showInfoOnly(NO_VIDEO_ID, VideoDetailsFragment.this.context);
                }
            }
        });
        this.like.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (VideoDetailsFragment.this.currentVideo.getVideoID() != null) {

                    VideoAsyncTask asyncTask = null;
                    try {
                        asyncTask = new VideoAsyncTask(VideoDetailsFragment.this.context, LIKE2,
                                VideoDetailsFragment.this.getVideoLikeJsonReq(VideoDetailsFragment.this.currentVideo
                                        .getVideoID()), false);
                    } catch (final JSONException exception) {
                        LOG.e(exception);
                    }

                    asyncTask.delegate = VideoDetailsFragment.this;
                    asyncTask.execute();
                } else {
                    Alerts.showInfoOnly(NO_VIDEO_ID, VideoDetailsFragment.this.context);
                }
            }
        });
        this.lovedImageLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (VideoDetailsFragment.this.currentVideo.getVideoID() != null) {

                    final LikedFragment fragment = new LikedFragment();
                    final Bundle bundle = new Bundle();
                    bundle.putString(Constant.ROOT_FRAGMENT, Constant.BROWSE_PAGE);
                    bundle.putString(Constant.VIDEOID, VideoDetailsFragment.this.currentVideo.getVideoID());
                    bundle.putString(Constant.COUNT, VideoDetailsFragment.this.noOfLikesForCurrentVideo);
                    fragment.setArguments(bundle);
                    BaseFragment.tabActivity.pushFragments(R.id.browsTab, fragment, Constant.LIKED,
                            VideoDetailsFragment.this, Constant.BROWSE);
                } else {
                    Alerts.showInfoOnly(NO_VIDEO_ID, VideoDetailsFragment.this.context);
                }
            }
        });
        this.play.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (VideoDetailsFragment.this.currentVideo.getVideoID() != null) {
                    new PlaybackAsync(VideoDetailsFragment.this.getActivity(), VideoDetailsFragment.this.currentVideo
                            .getVideoID()).execute();
                } else {
                    Alerts.showInfoOnly(NO_VIDEO_ID, VideoDetailsFragment.this.context);
                }
            }
        });

        return this.view;
    }

    @Override
    public void processDone(final boolean status, final String action) {

        if (LIKE2.equalsIgnoreCase(action)) {
            if (status) {
                final int likes = Integer.parseInt(this.currentVideo.getNumberOfLikes());
                this.noOfLikes.setText((likes + 1));
                Alerts.showInfoOnly(LIKED_SUCCESSFULLY, this.context);
            }
        } else if (DELETE.equalsIgnoreCase(action)) {

        }

    }

    private RelativeLayout getView(final MoreVideos video) {

        RelativeLayout videoView = null;
        videoView = (RelativeLayout) this.inflater.inflate(R.layout.public_video_item, null);
        this.videoImage = (ImageView) videoView.findViewById(R.id.publicvideothumb);
        this.videoPlayButton = (ImageButton) videoView.findViewById(R.id.playbutton);
        this.videoPlayButton.setTag(video);

        if (video.getVideothumbPath() != null) {

            Image.displayImage(video.getVideothumbPath(), this.getActivity(), this.videoImage, 1);
        } else {
            this.videoImage.setImageResource(R.drawable.profile_banner);
        }

        this.videoPlayButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                final MoreVideos video = (MoreVideos) v.getTag();
                if (video.getVideoId() != null) {
                    new PlaybackAsync(VideoDetailsFragment.this.getActivity(), video.getVideoId()).execute();
                } else {
                    Alerts.showInfoOnly("No video id for this video", VideoDetailsFragment.this.getActivity());
                }

            }
        });
        return videoView;

    }

    private void loadViews() {

        this.errorMessageTextView = (TextView) this.view.findViewById(R.id.videodetailserrormessageView);
        this.videoThumb = (ImageView) this.view.findViewById(R.id.videoThumb);
        this.ownerImage = (ImageView) this.view.findViewById(R.id.videoOwnerImg);
        this.previousButton = (Button) this.view.findViewById(R.id.previous);
        this.next = (Button) this.view.findViewById(R.id.next);
        this.share = (ImageView) this.view.findViewById(R.id.share);
        this.like = (ImageView) this.view.findViewById(R.id.like);
        this.comment = (ImageView) this.view.findViewById(R.id.comment);
        this.remember = (Button) this.view.findViewById(R.id.remember);
        this.play = (ImageButton) this.view.findViewById(R.id.playVideo);
        this.ownerName = (TextView) this.view.findViewById(R.id.videoOwnerName);
        this.createdDate = (TextView) this.view.findViewById(R.id.createdtext);
        this.views = (TextView) this.view.findViewById(R.id.noofviews);
        this.noOfTags = (TextView) this.view.findViewById(R.id.videoTag);
        this.noOfLikes = (TextView) this.view.findViewById(R.id.videoLike);
        this.noOfComments = (TextView) this.view.findViewById(R.id.videoComment);
        this.videotitle = (TextView) this.view.findViewById(R.id.videoTitle);
        this.videoDescription = (TextView) this.view.findViewById(R.id.videoDescription);
        this.videoDetailsView = (LinearLayout) this.view.findViewById(R.id.videodetailsView);
        this.otherStuffScrollView = (HorizontalScrollView) this.view.findViewById(R.id.publicvideos);
        this.userDetails = (LinearLayout) this.view.findViewById(R.id.otherpage);
        this.menu = (Button) this.view.findViewById(R.id.menu);
        this.search = (Button) this.view.findViewById(R.id.settings);
        this.searchEdit = (EditText) this.view.findViewById(R.id.searchEditText);
        this.searchLayout = (RelativeLayout) this.view.findViewById(R.id.searchRL);
        this.searchLayout.setVisibility(View.GONE);
        this.search.setVisibility(View.GONE);
        this.menu.setVisibility(View.GONE);
        final Button back = (Button) this.view.findViewById(R.id.back);
        back.setVisibility(View.VISIBLE);
        if (this.singleVideo) {
            this.previousButton.setVisibility(View.GONE);
            this.next.setVisibility(View.GONE);
        } else {
            this.previousButton.setVisibility(View.VISIBLE);
            this.next.setVisibility(View.VISIBLE);
        }
        this.userDetails.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (VideoDetailsFragment.this.userId != null) {
                    final OtherUserFragment fragment = new OtherUserFragment(); // object of next fragment
                    final Bundle bundle = new Bundle();
                    bundle.putString("userid", VideoDetailsFragment.this.userId);
                    bundle.putString(Constant.ROOT_FRAGMENT, Constant.BROWSE_PAGE);
                    fragment.setArguments(bundle);
                    BaseFragment.tabActivity.pushFragments(R.id.browsTab, fragment, Constant.OTHERS_PAGE,
                            VideoDetailsFragment.this, Constant.BROWSE);

                    // Intent secondUserIntent = new Intent(getActivity(), OtherUserActivity.class);
                    // secondUserIntent.putExtra("userid", "" +userId);
                    // startActivity(secondUserIntent);
                } else {
                    Alerts.showInfoOnly("User id not available", VideoDetailsFragment.this.getActivity());
                }

            }
        });
        back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                BaseFragment.tabActivity.removeFromBackStack();
                // getActivity().getSupportFragmentManager().popBackStackImmediate();
            }
        });
        this.otherStuffScrollView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(final View v, final MotionEvent event) {

                final int maxScrollX = VideoDetailsFragment.this.otherStuffScrollView.getChildAt(0).getMeasuredWidth()
                        - VideoDetailsFragment.this.otherStuffScrollView.getMeasuredWidth();
                final int differnce = VideoDetailsFragment.this.otherStuffScrollView.getScrollX() - maxScrollX;
                if ((differnce >= 0) && (differnce <= 1) && !VideoDetailsFragment.this.loading) {
                    if ((VideoDetailsFragment.this.userPublicVideos.size() % VideoDetailsFragment.PAGE_SIZE) == 0) {
                        VideoDetailsFragment.this.loading = true;
                        new LoadVideos(VideoDetailsFragment.this.userPublicVideos, 0).execute();
                    }

                }
                return false;
            }
        });
    }

    protected String getBrowseDetailJSONRequest() throws JSONException {

        final JSONObject request = new JSONObject();
        final JSONObject obj = new JSONObject();
        obj.put(Constant.DEVICE_TYPE, Constant.DEVICE_MODEL);
        obj.put(Constant.RESOLUTION, Config.getDeviceResolutionValue());
        obj.put("video_id", this.currentVideo.getVideoID());
        obj.put("user_id", this.currentVideo.getUserId());
        request.put("user", obj);

        return request.toString();
    }

    protected JSONObject getVideoLikeJsonReq(final String videoId) throws JSONException {

        final JSONObject json = new JSONObject();
        json.put(Constant.VIDEOID, videoId);
        json.put("userid", Config.getUserId());
        return json;

    }

    protected void loadList(final List<MoreVideos> list) {

        if (this.pullToRefresh) {
            this.pullToRefresh = false;
            this.publicVideoLayout.removeAllViews();
        }
        if ((list != null) && (list.size() > 0)) {
            for (int i = 0; i < list.size(); i++) {
                if (this.getView(list.get(i)) != null) {
                    this.publicVideoLayout.addView(this.getView(list.get(i)));
                }
            }
        }
    }

    protected void loadVideoData(final VideoDetails video) {

        if (video != null) {
            this.videoDetailsView.setVisibility(View.VISIBLE);
        }

        LOG.i("current videwo id " + video.getVideoID());

        // videoId = video.getVideoID();
        this.userId = video.getUserId();
        this.currentVideo = video;

        if (video.getName() != null) {
            this.heading.setText(video.getVideoTitle());
        } else {
            this.heading.setText("Video Details");
        }

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

        if (this.currentVideoNo == 0) {
            this.previousButton.setVisibility(View.GONE);
        } else if (this.currentVideoNo == (this.totalNo - 1)) {
            this.next.setVisibility(View.GONE);
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
            final int likes = Integer.parseInt(video.getNumberOfLikes());
            this.noOfLikesForCurrentVideo = String.valueOf(likes);
            if (likes > 1) {
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

        if (Strings.isNullOrEmpty(video.getLatestTagexpression())) {
            this.videotitle.setText((video.getLatestTagexpression() == null) ? "Video Title" : video
                    .getLatestTagexpression());
        } else {
            this.videotitle.setText((video.getVideoTitle() == null) ? "Video Title" : video.getVideoTitle());
        }
        this.videoDescription.setText((video.getVideoDesc() == null) ? "" : video.getVideoDesc());
        if (video.getVideoDesc() == null) {
            this.videoDescription.setVisibility(View.GONE);
        } else {
            this.videoDescription.setVisibility(View.VISIBLE);
        }
        if ((video.getOtherStuff() != null) && (video.getOtherStuff().size() > 0)) {
            this.userPublicVideos.clear();
            this.publicVideoLayout.removeAllViews();
            this.userPublicVideos.addAll(video.getOtherStuff());
            this.loadList(video.getOtherStuff());
        }
    }

    public class LoadVideoDetails extends AsyncTask<Void, Void, Void> {

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
                this.response = Backend.browseDetails(VideoDetailsFragment.this.context,
                        VideoDetailsFragment.this.getBrowseDetailJSONRequest());
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
            VideoDetailsFragment.this.errorMessageTextView.setVisibility(View.GONE);
            if (VideoDetailsFragment.this.pullToRefresh) {
                VideoDetailsFragment.this.pullToRefreshScrollView.onRefreshComplete();
            }

            if (this.response != null) {
                if (this.response instanceof VideoDetails) {
                    final VideoDetails details = (VideoDetails) this.response;
                    if (details != null) {
                        VideoDetailsFragment.this.loadVideoData(details);
                    }
                } else if (this.response instanceof ErrorResponse) {

                    final ErrorResponse res = (ErrorResponse) this.response;
                    if (Util.isConnected(VideoDetailsFragment.this.context)) {
                        VideoDetailsFragment.this.errorMessageTextView.setVisibility(View.GONE);
                        Alerts.showInfoOnly(res.getMessage(), VideoDetailsFragment.this.context);
                    } else {
                        if ((VideoDetailsFragment.this.errorMessageTextView != null)
                                && res.getErrorCode().equalsIgnoreCase("100")) {
                            VideoDetailsFragment.this.errorMessageTextView.setText(R.string.no_connectivity_text);
                            VideoDetailsFragment.this.errorMessageTextView.setVisibility(View.VISIBLE);
                        }
                    }

                    // ErrorResponse res = (ErrorResponse) response;
                    // Alerts.ShowAlertOnly("Info", res.getMessage(),
                    // VideoDetailsActivity.this);
                }
            } else {
                Alerts.showInfoOnly("Network problem.Please try again", VideoDetailsFragment.this.getActivity());
            }
        }

        @Override
        protected void onPreExecute() {

            if (this.showProgress) {

                this.pro = ProgressDialog.show(VideoDetailsFragment.this.getActivity(), "", "", true);
                final View v = VideoDetailsFragment.this.inflater.inflate(R.layout.progress_bar, null, false);
                this.pro.setContentView(v);
                this.pro.setCancelable(false);
                this.pro.setCanceledOnTouchOutside(false);
                this.pro.show();
            }
        }
    }

    public class LoadVideos extends AsyncTask<Void, Void, Void> {

        private final int offset;
        private ProgressDialog progressDialog;
        private Object response;
        private final List<MoreVideos> videos;

        public LoadVideos(final List<MoreVideos> list, final int offset) {

            this.videos = list;
            this.offset = offset;

        }

        @Override
        protected Void doInBackground(final Void... params) {

            final int pageNo = (this.videos.size() / VideoDetailsFragment.PAGE_SIZE) + 2;
            try {
                this.response = Backend.otherStuff(VideoDetailsFragment.this.context,
                        String.valueOf(VideoDetailsFragment.this.userId), String.valueOf(pageNo));
            } catch (final JSONException exception) {
                LOG.e(exception);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {

            super.onPostExecute(result);
            this.progressDialog.dismiss();
            VideoDetailsFragment.this.loading = false;

            if (this.response instanceof List<?>) {
                final List<MoreVideos> newList = (ArrayList<MoreVideos>) this.response;
                if ((newList != null) && (newList.size() > 0)) {
                    for (int i = 0; i < newList.size(); i++) {
                        this.videos.add(newList.get(i));

                    }
                    VideoDetailsFragment.this.loadList(newList);
                }
            } else if (this.response instanceof ErrorResponse) {
                final ErrorResponse res = (ErrorResponse) this.response;
                Alerts.showInfoOnly(res.getMessage(), VideoDetailsFragment.this.getActivity());
            }
        }

        @Override
        protected void onPreExecute() {

            this.progressDialog = ProgressDialog.show(VideoDetailsFragment.this.getActivity(), "", "", true);
            this.progressDialog.setContentView(((LayoutInflater) VideoDetailsFragment.this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.progress_bar, null, false));
            this.progressDialog.setCancelable(false);
            this.progressDialog.setCanceledOnTouchOutside(false);
            this.progressDialog.show();
        }
    }

}
