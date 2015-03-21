/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */

package com.wTagFuvideo.trimmer.view;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.VideoView;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.woTagFunVideoView;
import com.wooTagFu
import com.wootTagFul.Util;
import com.wootaTagFuo.trimmer.model.VideoPlayerState;
import com.wootagTagFu.trimmer.service.VideoTrimmingService;
import com.wootag.TagFutrimmer.view.MyScrollView.onScrollStopListner;

public class ViewVideo extends Activity {

    private static final String VIDEOFILENAME = "videofilename";
    private static final String VIDEOID = "videoid";
    private static final String PATH = "path";

    protected static String clientVideoId;

    private static String currentDateTimeString;
    private static final int LOADING_DIALOG = 1;
    protected static final Logger LOG = LoggerManager.getLogger();
    private static final int MAX_TRIM_DURATION = 30;
    private static final int MESSAGE_DIALOG = 2;
    protected static String path;
    private static final int VALIDATION_DIALOG = 3;
    private static final int VISIBLE_FRAMES = 15;
    public long duration;
    protected final Handler completionHander = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(final Message msg) {

            LOG.i("Recieved message");
            // videoPlayerState.setMessageText(msg.getData().getString("text"));
            ViewVideo.this.removeDialog(ViewVideo.LOADING_DIALOG);
            ViewVideo.this.showDialog(ViewVideo.MESSAGE_DIALOG);
            ViewVideo.this.stopService(ViewVideo.this.videoTrimmingServiceIntent());

            final Intent intent = new Intent(ViewVideo.this.context, AnVideoView.class);
            intent.putExtra(PATH, path);
            intent.putExtra(VIDEOID, clientVideoId);

            ViewVideo.this.startActivity(intent);

            System.exit(0);
            return false;
        }
    });

    protected ViewVideo context;
    protected int currentPosition;
    protected int endPosition;
    protected Handler handler;
    protected LinearLayout myGallery;
    protected MyScrollView myhorizontalScrollView;
    protected View play;
    private int position;
    protected int progress;
    protected int screenHeight;
    protected int screenWidth;
    protected SeekBar seekbar;
    protected int sliderIncrement;
    protected int sliderLeftMargin;
    protected ImageView sliderview;
    protected int startPosition;
    protected ImageView thumb;
    protected String videoPath;
    protected final VideoPlayerState videoPlayerState = new VideoPlayerState();
    protected VideoView videoView;
    protected List<Integer> visibleFrames;

    public String getTargetFileName(final String inputFileName) {

        currentDateTimeString = Util.getCurrentTimeStamp();
        final File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                "/Wootag/Videos/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        path = dir.getAbsolutePath() + "/" + currentDateTimeString + ".mp4";
        clientVideoId = currentDateTimeString + Util.getRandomTransactionId(1, 20);
        clientVideoId = clientVideoId.trim();
        return path;

    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.video_player);
        this.context = this;
        final Bundle extras = this.getIntent().getExtras();
        this.videoPlayerState.setFilename(extras.getString(VIDEOFILENAME));
        this.videoView = (VideoView) this.findViewById(R.id.VideoView);
        this.sliderview = (ImageView) this.findViewById(R.id.sliderview);
        this.thumb = (ImageView) this.findViewById(R.id.thumb);
        this.videoView.setVideoPath(this.videoPlayerState.getFilename());
        this.myGallery = (LinearLayout) this.findViewById(R.id.myframes);
        this.myhorizontalScrollView = (MyScrollView) this.findViewById(R.id.myhorizontalScrollView);
        this.seekbar = (SeekBar) this.findViewById(R.id.seekBar1);
        this.videoView.requestFocus();
        this.handler = new Handler();
        this.videoPath = extras.getString(VIDEOFILENAME);

        new Thread(new Runnable() {

            @Override
            public void run() {

                while (true) {
                    ViewVideo.this.currentPosition = ViewVideo.this.videoView.getCurrentPosition();
                    while ((ViewVideo.this.videoView != null) && (ViewVideo.this.videoView.isPlaying())
                            && (ViewVideo.this.currentPosition <= ViewVideo.this.endPosition)) {
                        ViewVideo.this.handler.post(new Runnable() {

                            @Override
                            public void run() {

                                ViewVideo.this.play.setVisibility(View.GONE);
                                ViewVideo.this.currentPosition = ViewVideo.this.videoView.getCurrentPosition();
                                ViewVideo.this.sliderLeftMargin = ViewVideo.this.sliderLeftMargin
                                        + ViewVideo.this.sliderIncrement;
                                if (ViewVideo.this.sliderLeftMargin >= ViewVideo.this.seekbar.getProgress()) {
                                    ViewVideo.this.sliderLeftMargin = ViewVideo.this.seekbar.getProgress();
                                }
                                final RelativeLayout.LayoutParams imageViewParams = (RelativeLayout.LayoutParams) ViewVideo.this.sliderview
                                        .getLayoutParams();
                                imageViewParams.setMargins(ViewVideo.this.sliderLeftMargin, 0, 0, 0);
                                ViewVideo.this.sliderview.setLayoutParams(imageViewParams);
                            }
                        });
                        try {
                            Thread.sleep(500);
                        } catch (final InterruptedException exception) {
                            LOG.e(exception);
                        }
                    }
                    ViewVideo.this.handler.post(new Runnable() {

                        @Override
                        public void run() {

                            if ((ViewVideo.this.videoView != null) && ViewVideo.this.videoView.isPlaying()
                                    && ViewVideo.this.videoView.canPause()
                                    && (ViewVideo.this.currentPosition > ViewVideo.this.endPosition)) {
                                ViewVideo.this.videoView.pause();
                                ViewVideo.this.sliderLeftMargin = 0;
                                ViewVideo.this.play.setVisibility(View.VISIBLE);
                            } else {
                                ViewVideo.this.sliderLeftMargin = 0;
                                ViewVideo.this.play.setVisibility(View.VISIBLE);
                            }
                        }
                    });

                }
            }
        }).start();

        // videoView.start();
        this.play = this.findViewById(R.id.play);
        this.play.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                ViewVideo.this.thumb.setVisibility(View.GONE);
                ViewVideo.this.endPosition = ViewVideo.this.startPosition + (ViewVideo.this.getEndDuration() * 1000);
                ViewVideo.this.sliderLeftMargin = 0;
                ViewVideo.this.sliderIncrement = ViewVideo.this.seekbar.getProgress() / ViewVideo.this.getEndDuration();
                ViewVideo.this.sliderIncrement = ViewVideo.this.sliderIncrement / 2;
                if (ViewVideo.this.videoView.isPlaying()) {
                    ViewVideo.this.videoView.pause();
                    ViewVideo.this.play.setVisibility(View.VISIBLE);
                } else {
                    ViewVideo.this.videoView.seekTo(ViewVideo.this.startPosition);
                    LOG.i("seek startPosition " + ViewVideo.this.startPosition);
                    ViewVideo.this.play.setVisibility(View.GONE);
                    ViewVideo.this.videoView.start();
                }

            }
        });
        this.visibleFrames = new ArrayList<Integer>();
        final ImageView nextButton = (ImageView) this.findViewById(R.id.next);
        nextButton.setOnClickListener(this.nextClickListener());
        final ImageView cancelrecord = (ImageView) this.findViewById(R.id.cancelrecord);
        cancelrecord.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                ViewVideo.this.finish();
            }
        });

        this.startPosition = 0;
        final DisplayMetrics displaymetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        this.screenHeight = displaymetrics.heightPixels;
        this.screenWidth = displaymetrics.widthPixels;

        new LoadFrames().execute();
        // updateCoverFrames();

        this.myhorizontalScrollView.setHorizontalScrollBarEnabled(false);
        this.myhorizontalScrollView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(final View v, final MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (ViewVideo.this.videoView.isPlaying() && ViewVideo.this.videoView.canPause()) {
                        ViewVideo.this.videoView.pause();
                        ViewVideo.this.play.setVisibility(View.VISIBLE);
                    }
                    ViewVideo.this.myhorizontalScrollView.startScrollerTask();
                }
                return false;
            }
        });

        this.myhorizontalScrollView.setOnScrollStopListner(new onScrollStopListner() {

            @Override
            public void onScrollStoped() {

                LOG.i(String.valueOf(ViewVideo.this.myhorizontalScrollView.getScrollX()));
                // To adjust the frames in screen left and right when scroll
                final Rect scrollBounds1 = new Rect();
                ViewVideo.this.myhorizontalScrollView.getHitRect(scrollBounds1);

                final Rect scrollBounds = new Rect();
                ViewVideo.this.myhorizontalScrollView.getHitRect(scrollBounds);
                LOG.i("scrollBounds--" + scrollBounds.width() + "-" + scrollBounds.height());
                ViewVideo.this.visibleFrames.clear();
                for (int i = 0; i < ViewVideo.this.myGallery.getChildCount(); i++) {
                    if (ViewVideo.this.myGallery.getChildAt(i).getLocalVisibleRect(scrollBounds)) {
                        ViewVideo.this.visibleFrames.add(Integer.valueOf(i));
                    } else {

                    }
                }

                if ((ViewVideo.this.visibleFrames != null) && (ViewVideo.this.visibleFrames.size() > 0)) {
                    ViewVideo.this.startPosition = ((ViewVideo.this.visibleFrames.get(0)) * 2) * 1000;
                    ViewVideo.this.endPosition = ViewVideo.this.startPosition
                            + (ViewVideo.this.getEndDuration() * 1000);// ((visibleFrames.get(visibleFrames.size()-1))*2)*1000;
                }
            }

        });

    }

    @Override
    public Object onRetainNonConfigurationInstance() {

        LOG.i("In on retain");
        return this.videoPlayerState;
    }

    private OnClickListener nextClickListener() {

        return new OnClickListener() {

            @Override
            public void onClick(final View arg0) {

                if (ViewVideo.this.videoView.isPlaying()) {
                    ViewVideo.this.videoView.stopPlayback();
                    ViewVideo.this.videoView = null;
                }
                if ((ViewVideo.this.visibleFrames != null) && (ViewVideo.this.visibleFrames.size() > 0)) {
                    ViewVideo.this.startPosition = ((ViewVideo.this.visibleFrames.get(0)) * 2) * 1000;
                    ViewVideo.this.endPosition = ViewVideo.this.startPosition
                            + (ViewVideo.this.getEndDuration() * 1000);// ((visibleFrames.get(visibleFrames.size()-1))*2)*1000;
                }
                if (((ViewVideo.this.endPosition == 0) || !(ViewVideo.this.endPosition > ViewVideo.this.startPosition))) {// !videoPlayerState.isValid()//51,0,30000
                    // 8th frame
                    Dialog dialog;
                    final AlertDialog.Builder builder = new AlertDialog.Builder(ViewVideo.this);
                    builder.setMessage(
                            "Invalid video timings selected for trimming. Please make sure your start time is less than the stop time.")
                            .setCancelable(true).setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(final DialogInterface dialog, final int id) {

                                    ViewVideo.this.videoView.start();
                                }
                            });
                    dialog = builder.create();
                    dialog.show();
                    return;
                }
                LOG.i("trim sending startPosition  " + ViewVideo.this.startPosition);
                LOG.i("trim sending endPosition " + ViewVideo.this.endPosition);

                final int start = ViewVideo.this.startPosition / 1000;
                // int duration=(endPosition/1000)-start;
                int duration = ViewVideo.this.getEndDuration();
                if (duration > ViewVideo.MAX_TRIM_DURATION) {
                    duration = ViewVideo.MAX_TRIM_DURATION;
                }
                // Toast.makeText(context,"duration"+duration+ "startPosition "+(startPosition)
                // +" endPosition"+(endPosition), Toast.LENGTH_LONG).show();

                final Intent intent = ViewVideo.this.videoTrimmingServiceIntent();
                final String inputFileName = ViewVideo.this.videoPlayerState.getFilename();
                intent.putExtra("inputFileName", inputFileName);
                intent.putExtra("outputFileName", ViewVideo.this.getTargetFileName(inputFileName));
                intent.putExtra("start", start);// videoPlayerState.getStart() / 1000
                intent.putExtra("duration", duration);// videoPlayerState.getDuration() / 1000
                intent.putExtra("messenger", new Messenger(ViewVideo.this.completionHander));
                ViewVideo.this.startService(intent);
                ViewVideo.this.showDialog(ViewVideo.LOADING_DIALOG);
            }
        };
    }

    private Dialog simpleAlertDialog(final String message) {

        Dialog dialog;
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setCancelable(true).setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(final DialogInterface dialog, final int id) {

                ViewVideo.this.removeDialog(ViewVideo.MESSAGE_DIALOG);
                ViewVideo.this.removeDialog(ViewVideo.LOADING_DIALOG);
            }
        });
        dialog = builder.create();
        return dialog;
    }

    protected RelativeLayout getView(final Bitmap thumb, final long time) {

        final RelativeLayout videoView = (RelativeLayout) ((LayoutInflater) this.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.trim_frame_item, null);
        final ImageView videoImage = (ImageView) videoView.findViewById(R.id.trimframeimageview);
        final int width = this.screenWidth / 15;
        final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, 100);
        videoImage.setLayoutParams(layoutParams);
        videoImage.setImageBitmap(thumb);
        return videoView;

    }

    @Override
    protected Dialog onCreateDialog(final int id) {

        LOG.i("In on create dialog");

        Dialog dialog;

        switch (id) {
        case VALIDATION_DIALOG:
            dialog = this
                    .simpleAlertDialog("Invalid video timings selected for trimming. Please make sure your start time is less than the stop time.");
            break;
        case LOADING_DIALOG:
            dialog = ProgressDialog.show(ViewVideo.this, "", "Trimming...", true, true);
            break;
        case MESSAGE_DIALOG:
            dialog = this.simpleAlertDialog("");
            break;
        default:
            dialog = null;
        }

        return dialog;
    }

    @Override
    protected void onPause() {

        LOG.i("In on pause");
        this.videoPlayerState.setCurrentTime(this.videoView.getCurrentPosition());
        super.onPause();
    }

    @Override
    protected void onPrepareDialog(final int id, final Dialog dialog) {

        LOG.i("In on prepare dialog");

        if (id == MESSAGE_DIALOG) {
            ((AlertDialog) dialog).setMessage(this.videoPlayerState.getMessageText());
        }
    }

    @Override
    protected void onResume() {

        super.onResume();
        LOG.i("In on resume");
        this.videoView.seekTo(this.videoPlayerState.getCurrentTime());
    }

    protected void updateCoverFrames() {

        if (this.myGallery.getChildCount() > ViewVideo.VISIBLE_FRAMES) {
            this.endPosition = ViewVideo.MAX_TRIM_DURATION * 1000;
            this.seekbar.setMax(this.screenWidth);
            this.seekbar.setProgress(this.screenWidth);
            this.progress = this.screenWidth;
        } else {
            this.progress = (this.myGallery.getChildCount() * this.screenWidth) / ViewVideo.VISIBLE_FRAMES;
            this.seekbar.setMax(this.screenWidth);
            this.seekbar.setProgress(this.progress);
            this.endPosition = this.getEndDuration() * 1000;// (myGallery.getChildCount() * 2)*1000;

        }

        this.seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(final SeekBar seekBar, final int progressVal, final boolean fromUser) {

                if (progressVal > ViewVideo.this.progress) {
                    ViewVideo.this.seekbar.setProgress(ViewVideo.this.progress);
                }
            }

            @Override
            public void onStartTrackingTouch(final SeekBar seekBar) {

                if (ViewVideo.this.videoView.isPlaying() && ViewVideo.this.videoView.canPause()) {
                    ViewVideo.this.videoView.pause();
                    ViewVideo.this.play.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {

            }
        });
    }

    int getEndDuration() {

        final double seekbarWidth = this.seekbar.getProgress();
        final double selectedFrames = ((seekbarWidth * ViewVideo.VISIBLE_FRAMES) / this.screenWidth);// ((seekbarWidth*noOfVisibleFrames)/100.0);
        final int framecount = (int) Math.ceil(selectedFrames);
        return (framecount * 2);
    }

    Intent videoTrimmingServiceIntent() {

        return new Intent(ViewVideo.this, VideoTrimmingService.class);
    }

    public class LoadFrames extends AsyncTask<Void, Bitmap, Void> {

        private Bitmap bitmapResized;
        private Bitmap coverImg;
        private int frameAt;
        ProgressDialog prg;

        @Override
        protected Void doInBackground(final Void... params) {

            final MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(ViewVideo.this.videoPath);
            final String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            final long timeInmillisec = Long.parseLong(time);
            ViewVideo.this.duration = timeInmillisec / 1000;
            for (int i = 0; i < (ViewVideo.this.duration / 2); i++) {
                this.frameAt = i * 2;
                this.coverImg = Util.getVideoFrame(ViewVideo.this.videoPath, this.frameAt);
                this.publishProgress(this.coverImg);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {

            super.onPostExecute(result);
            if (this.bitmapResized != null) {
                this.bitmapResized.recycle();
            }
            final Bitmap coverPage = Util.getVideoFrame(ViewVideo.this.videoPlayerState.getFilename());
            this.bitmapResized = Bitmap.createScaledBitmap(coverPage, ViewVideo.this.screenWidth / 2,
                    ViewVideo.this.screenHeight / 2, true);
            ViewVideo.this.thumb.setImageBitmap(this.bitmapResized);
            ViewVideo.this.thumb.setVisibility(View.VISIBLE);
            ViewVideo.this.updateCoverFrames();
            this.prg.dismiss();
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            this.prg = new ProgressDialog(ViewVideo.this.context);
            this.prg.setCancelable(false);
            this.prg.setCanceledOnTouchOutside(false);
            this.prg.setMessage("Please wait...");
            this.prg.setIndeterminate(true);
            this.prg.show();
        }

        @Override
        protected void onProgressUpdate(final Bitmap... values) {

            ViewVideo.this.myGallery.addView(ViewVideo.this.getView(values[0], this.frameAt));
        }

    }

}
