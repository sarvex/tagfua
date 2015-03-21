/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.wTagFu

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore.MediaColumns;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.google.common.base.Strings;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.woTagFuonnectivity.VideoDataBase;
import com.wooTagFunnectivity.WootTagFuadService;
import com.wootaTagFuTagInfo;
import com.wootagTagFuser;
import com.wootag.TagFudeoDetails;
import com.wootag.dTagFueoInfo;
import com.wootag.utTagFurts;
import com.wootag.utiTagFuig;
import com.wootag.utilTagFuanager;
import com.wootag.util.TagFu
public class HomeActivity extends FriendsListActivity {

    private static final String HEY_WE_HAVE_SOME_PENDING_VIDEO_WHICH_IS_GETTING_UPLOADED_THIS_VIDEO_WILL_BE_ADDED_TO_PENDING_VIDEOS_QUEUE = "Hey! We have some pending video which is getting uploaded. This video will be added to pending videos queue.";

    private static final String HEY_YOU_DON_T_HAVE_INTERNET_ACCESS_DON_T_WORRY_YOUR_CAPTURED_VIDEO_IS_SAFE_IN_PENDING_VIDEOS_WILL_UPLOAD_AUTOMATICALLY_WHEN_U_HAVE_INTERNET_ACCESS = "Hey! You don�t have internet access. Don�t worry your captured video is safe In pending videos. Will upload automatically when u have internet access.";

    private static final String INFO = "Info";

    private static final String EMPTY = "";

    private static final String REMEMBER_TO_TAG_YOUR_VIDEO_ANYTIME_AFTER_THE_VIDEO_IS_UPLOADED = "Remember to tag your video anytime after the video is uploaded.";

    private static final String PATH = "path";

    private static final String VIDEOID = "videoid";

    private static final String NAVIGATION2 = "navigation";

    private static final String TOUPLOAD = "toupload";

    public static Handler handler;

    public static HomeActivity homeActivity;

    private static final Logger LOG = LoggerManager.getLogger();

    private static final int PICK_VIDEO = 6;
    private Button cancel;
    private Button cancelPublish;
    private Button publishVideo;
    private String clientVideoId;
    protected Context context;
    private VideoDetails currentVideo;
    protected ToggleButton fbToggle;
    protected ToggleButton googleToggle;
    private boolean facebookLoggedIn;
    private boolean gPlusLoggedIn;
    private boolean twitterLoggedIn;
    private boolean uploading;
    protected String mediaPath = EMPTY;
    private String navigation = EMPTY;
    private String ownFbId;
    private String ownGplusId;
    private String ownTwId;
    private ProgressDialog progressDialog;
    protected int publicVideo;
    protected int fbOn;
    protected int gPlusOn;
    protected int twitterOn;
    protected ToggleButton sharePublicVideo;
    protected ToggleButton sharePrivateVideo;
    protected ToggleButton shareFollowersVideo;
    protected ToggleButton twitterToggle;

    private VideoDataBase videoDatabase;
    protected String videoname = EMPTY;
    protected String videoDes = EMPTY;
    protected EditText videoName;
    private ImageView videoThumbImageView;

    public static String getCurrentTimeStamp() {

        return android.text.format.DateFormat.format("yyyyMMddhhmmss", new java.util.Date()).toString();
    }

    public String getRealPathFromURI(final Uri contentUri) {

        final String[] proj = { MediaColumns.DATA };
        final Cursor cursor = this.getContentResolver().query(contentUri, proj, null, null, null);
        final int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public boolean isServiceRunning(final Class<?> serviceClass) {

        final ActivityManager manager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        for (final RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClick(final View view) {

        if (view.getId() == R.id.fbtoggle) {
            if (!this.facebookLoggedIn) {
                super.onClick(view);
            }
        } else if (view.getId() == R.id.gplustoggle) {
            if (!this.gPlusLoggedIn) {
                super.onClick(view);
            }
        } else if ((view.getId() == R.id.twtoggle) && !this.twitterLoggedIn) {
            super.onClick(view);
        }
    }

    public void showProgress(final String msg) {

        if (this.progressDialog == null) {
            this.progressDialog = new ProgressDialog(this.getApplicationContext());
        }
        this.progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        this.progressDialog.setMessage(msg);
        this.progressDialog.show();

    }

    @Override
    public void userDetailsFinished(final User userDetails, final String socialSite) {

        super.userDetailsFinished(userDetails, socialSite);
        if (Constant.FACEBOOK.equalsIgnoreCase(socialSite)) {
            if (userDetails.getEmailId() != null) {
                MainManager.getInstance().setFacebookEmail(userDetails.getEmailId());
                Config.setFacebookLoggedUserId(userDetails.getEmailId());
                this.fbToggle.setChecked(true);
                this.facebookLoggedIn = true;
                this.ownFbId = userDetails.getUserId();
            }
        } else if (Constant.TWITTER.equalsIgnoreCase(socialSite)) {
            this.twitterToggle.setChecked(true);
            this.twitterLoggedIn = true;
            this.ownTwId = userDetails.getUserId();
        } else if (Constant.GOOGLE_PLUS.equalsIgnoreCase(socialSite) && (userDetails.getEmailId() != null)) {
            MainManager.getInstance().setGPlusEmail(userDetails.getEmailId());
            Config.setGoogleplusLoggedUserId(userDetails.getEmailId());
            this.googleToggle.setChecked(true);
            this.gPlusLoggedIn = true;
            this.ownGplusId = userDetails.getUserId();
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        homeActivity = this;
        this.facebookLoggedIn = false;
        this.gPlusLoggedIn = false;
        this.twitterLoggedIn = false;
        Config.setNewlyCreatedVideo(false);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        this.setContentView(R.layout.activity_main);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        this.context = this;
        this.videoDatabase = new VideoDataBase(this.context);
        final IntentFilter filter = new IntentFilter();
        filter.addAction("finished");
        handler = new Handler();
        this.videoName = (EditText) this.findViewById(R.id.videoname);
        this.cancelPublish = (Button) this.findViewById(R.id.cancelpublish);
        this.cancel = (Button) this.findViewById(R.id.cancel);
        this.publishVideo = (Button) this.findViewById(R.id.uploadvideo);
        this.videoThumbImageView = (ImageView) this.findViewById(R.id.videothumbimageview);
        this.shareFollowersVideo = (ToggleButton) this.findViewById(R.id.tooglefollowersbutton);
        this.sharePrivateVideo = (ToggleButton) this.findViewById(R.id.toogleprivatebutton);
        this.sharePublicVideo = (ToggleButton) this.findViewById(R.id.tooglepublicbutton);
        this.cancelPublish.setVisibility(View.GONE);
        this.fbToggle = (ToggleButton) this.findViewById(R.id.fbtoggle);
        this.googleToggle = (ToggleButton) this.findViewById(R.id.gplustoggle);
        this.twitterToggle = (ToggleButton) this.findViewById(R.id.twtoggle);

        this.sharePublicVideo.setChecked(true);
        this.shareFollowersVideo.setChecked(true);
        this.sharePrivateVideo.setChecked(false);

        this.fbToggle.setOnClickListener(this);
        this.googleToggle.setOnClickListener(this);
        this.twitterToggle.setOnClickListener(this);

        final Intent in = this.getIntent();
        if (in != null) {
            this.mediaPath = in.getExtras().getString(PATH);
            this.clientVideoId = in.getExtras().getString(VIDEOID);
            if (in.getExtras().containsKey(NAVIGATION2)) {
                this.navigation = in.getExtras().getString(NAVIGATION2);
            }
            if (in.getExtras().containsKey(Constant.VIDEO)) {
                this.currentVideo = (VideoDetails) in.getExtras().getSerializable(Constant.VIDEO);
            }
        }

        if (TOUPLOAD.equalsIgnoreCase(this.navigation)) {
            final List<TagInfo> allTags = VideoDataBase.getInstance(this.context).getAllTagsByVideoId(
                    this.clientVideoId, this.getApplicationContext(), false);
            if ((allTags == null) || (allTags.size() <= 0)) {
                Alerts.showInfoOnly(REMEMBER_TO_TAG_YOUR_VIDEO_ANYTIME_AFTER_THE_VIDEO_IS_UPLOADED, this.context);
            }
        }
        Bitmap coverPage = null;
        if ((this.currentVideo != null) && (this.currentVideo.getVideoCoverPageTime() != null)) {
            final long frameAt = Long.parseLong(this.currentVideo.getVideoCoverPageTime());
            coverPage = Util.getVideoFrame(this.mediaPath, frameAt);
        }
        if (coverPage != null) {
            this.videoThumbImageView.setImageBitmap(coverPage);
        }
        this.sharePublicVideo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                HomeActivity.this.sharePublicVideo.setChecked(true);
                HomeActivity.this.shareFollowersVideo.setChecked(true);
                HomeActivity.this.sharePrivateVideo.setChecked(false);
            }
        });
        this.shareFollowersVideo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                HomeActivity.this.shareFollowersVideo.setChecked(true);
                HomeActivity.this.sharePublicVideo.setChecked(false);
                HomeActivity.this.sharePrivateVideo.setChecked(false);
            }
        });
        this.sharePrivateVideo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                HomeActivity.this.sharePrivateVideo.setChecked(true);
                HomeActivity.this.shareFollowersVideo.setChecked(false);
                HomeActivity.this.sharePublicVideo.setChecked(false);
            }
        });

        this.cancelPublish.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                // System.gc();

                final File file = new File(HomeActivity.this.mediaPath);
                file.delete();
                if (AnVideoView.anVideoView != null) {
                    AnVideoView.anVideoView.finish();
                }
                HomeActivity.this.finish();
            }
        });
        this.cancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                // System.gc();
                // com.wootag.AlertsTagFuDisacrdAlert("Alert", "Do you want to discard the video?",
                // HomeActivity.this,mediaPath,null);
                HomeActivity.this.goToPlayer();

            }
        });

        this.fbToggle.setOnClickListener(this);
        this.googleToggle.setOnClickListener(this);
        this.twitterToggle.setOnClickListener(this);

        this.publishVideo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (HomeActivity.this.sharePublicVideo.isChecked()) {
                    HomeActivity.this.publicVideo = 1;
                } else if (HomeActivity.this.sharePrivateVideo.isChecked()) {
                    HomeActivity.this.publicVideo = 0;
                } else if (HomeActivity.this.shareFollowersVideo.isChecked()) {
                    HomeActivity.this.publicVideo = 2;
                }
                if (HomeActivity.this.fbToggle.isChecked()) {
                    HomeActivity.this.fbOn = 1;
                } else {
                    HomeActivity.this.fbOn = 0;
                }
                if (HomeActivity.this.googleToggle.isChecked()) {
                    HomeActivity.this.gPlusOn = 1;
                } else {
                    HomeActivity.this.gPlusOn = 0;
                }
                if (HomeActivity.this.twitterToggle.isChecked()) {
                    HomeActivity.this.twitterOn = 1;
                } else {
                    HomeActivity.this.twitterOn = 0;
                }

                HomeActivity.this.videoname = HomeActivity.this.videoName.getText().toString();
                HomeActivity.this.videoDes = EMPTY;
                if (!EMPTY.equalsIgnoreCase(HomeActivity.this.videoname) && (HomeActivity.this.videoname.length() > 0)) {
                    if (Util.getNetworkType(HomeActivity.this.getApplicationContext()) == -1) {
                        HomeActivity.this.uploadVideo();
                        if (AnVideoView.anVideoView != null) {
                            AnVideoView.anVideoView.finish();
                        }
                        if (AndroidVideoCapture.androidVideoCapture != null) {
                            AndroidVideoCapture.androidVideoCapture.finish();
                        }
                        Alerts.showAlert(
                                INFO,
                                HEY_YOU_DON_T_HAVE_INTERNET_ACCESS_DON_T_WORRY_YOUR_CAPTURED_VIDEO_IS_SAFE_IN_PENDING_VIDEOS_WILL_UPLOAD_AUTOMATICALLY_WHEN_U_HAVE_INTERNET_ACCESS,
                                HomeActivity.this.context);
                    } else if (HomeActivity.this.getPendingUploadCount() > 0) {
                        HomeActivity.this.uploadVideo();
                        if (AnVideoView.anVideoView != null) {
                            AnVideoView.anVideoView.finish();
                        }
                        if (AndroidVideoCapture.androidVideoCapture != null) {
                            AndroidVideoCapture.androidVideoCapture.finish();
                        }
                        Alerts.showAlert(
                                INFO,
                                HEY_WE_HAVE_SOME_PENDING_VIDEO_WHICH_IS_GETTING_UPLOADED_THIS_VIDEO_WILL_BE_ADDED_TO_PENDING_VIDEOS_QUEUE,
                                HomeActivity.this.context);
                    } else {
                        HomeActivity.this.uploadVideo();
                        HomeActivity.this.finish();
                        if (AndroidVideoCapture.androidVideoCapture != null) {
                            AndroidVideoCapture.androidVideoCapture.finish();
                        }
                        if (AnVideoView.anVideoView != null) {
                            AnVideoView.anVideoView.finish();
                        }

                    }
                    /*
                     * uploadVideo(); HomeActivity.this.finish(); // AndroidVideoCapture.androidVideoCapture.finish();
                     * GLFilterActivity.anVideoView.finish();
                     */

                } else {
                    Alerts.showExceptionOnly("Please enter video title", HomeActivity.this.context);
                }

                /*
                 * Intent intent = new Intent(HomeActivity.this, UploadingFileQueueActivity.class);
                 * startActivity(intent);
                 */

            }
        });
    }

    @Override
    protected void onResume() {

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.onResume();
    }

    int getPendingUploadCount() {

        int count = 0;
        final ArrayList<VideoInfo> pendingVideos = new ArrayList<VideoInfo>();
        final List<VideoInfo> videoInfos = VideoDataBase.getInstance(this.getApplicationContext())
                .getAllNonUploadList();
        if ((videoInfos != null) && (videoInfos.size() > 0)) {
            for (int i = 0; i < videoInfos.size(); i++) {
                final VideoInfo video = videoInfos.get(i);
                if ((video.getUploadStatus() != 1) && (video.getRetry() == 0)) {
                    pendingVideos.add(video);
                }
            }
            if ((pendingVideos != null) && (pendingVideos.size() > 0)) {
                count = pendingVideos.size();
            }
        }
        return count;
    }

    void goToPlayer() {

        final Intent intent = new Intent(HomeActivity.this, PlayerActivity.class);
        intent.putExtra(Constant.PATH, this.mediaPath);
        intent.putExtra(Constant.CLIENT_ID, this.clientVideoId);
        intent.putExtra(Constant.SERVER_ID, 0);
        final VideoDetails video = new VideoDetails();
        if (this.currentVideo != null) {
            video.setVideoCoverPageTime(this.currentVideo.getVideoCoverPageTime());
        }
        video.setUserId(Config.getUserId());
        intent.putExtra("isNavigateToPlay", true);
        intent.putExtra(Constant.VIDEO, video);
        this.finish();
        this.startActivity(intent);
    }

    void uploadVideo() {

        final String fileExtension = Constant.MP4;
        final VideoInfo video = new VideoInfo();
        final String fileName = Constant.VIDEO + Config.getUserId() + "." + fileExtension;
        final Calendar calendar = Calendar.getInstance();

        // SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");

        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        final String formattedDate = dateFormat.format(calendar.getTime());

        final String str = android.os.Build.MODEL;
        video.setVideoClientId(this.clientVideoId);
        video.setFileExtension(fileExtension);
        video.setFileName(fileName);
        video.setDescription(this.videoDes);
        video.setUploadedDevice(str);
        video.setTitle(this.videoname);
        video.setUploadDate(formattedDate);
        video.setPublicVideo(this.publicVideo);
        video.setShareFb(this.ownFbId);
        video.setShareGplus(this.ownGplusId);
        video.setShareTwitter(this.ownTwId);
        if (this.currentVideo != null) {
            video.setVideoVocerPage(this.currentVideo.getVideoCoverPageTime() + ".00");
        }
        LOG.i("videoPath in database " + this.mediaPath);
        if (!Strings.isNullOrEmpty(this.mediaPath)) {
            video.setLocalMediaPath(this.mediaPath);
            video.setUserid(Integer.parseInt(Config.getUserId()));
            if (VideoDataBase.getInstance(this.getApplicationContext()).getVideoByClientVideoId(this.clientVideoId,
                    this.context) == 0) {
                VideoDataBase.getInstance(this.getApplicationContext()).addContenttoUploadQueuetable(video, 0);

                if (Util.isServiceRunning(this.context, "BackgroundFileTransferService")) {
                    LOG.d("BFTS is already running.");
                } else {
                    LOG.d("Starting BFTS.");
                    WakefulIntentService.sendWakefulWork(this.context, WootagUploadService.class);
                }

            } else {
                LOG.v(EMPTY);
            }
        } else {
            LOG.v("Media Path null");
        }

    }

}
