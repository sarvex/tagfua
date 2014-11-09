/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.wootag;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wootag.dto.VideoDetails;
import com.wootag.util.Config;
import com.wootag.util.Util;

public class AnVideoView extends Activity {

    private static final Logger LOG = LoggerManager.getLogger();

    public static AnVideoView anVideoView;
    public static Bitmap bitmapResized;

    private Bundle bundle;
    private ImageButton back;
    private ImageButton later;
    private String clientVideoId;

    protected AnVideoView context;
    protected ImageView coverImageBackground;
    protected LinearLayout coverSelectionLayout;
    protected String videoPath;
    protected int index;
    protected int screenHeight;
    protected int screenWidth;
    protected long frameAt;

    public static void videoDisacrdAlert(final String heading, final String message, final Context context,
            final String path, final Dialog dialog) {

        // try {
        final CharSequence msg = message;
        final AlertDialog alert = new AlertDialog.Builder(context).create();
        alert.setTitle(heading);
        alert.setMessage(msg);
        alert.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(final DialogInterface dialog, final int which) {

                final File file = new File(path);
                file.delete();
                if (bitmapResized != null) {
                    bitmapResized.recycle();
                }

                if (dialog != null) {
                    dialog.dismiss();
                }
                ((Activity) context).finish();
            }
        });

        alert.setButton(DialogInterface.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(final DialogInterface dialog, final int which) {

                alert.dismiss();
            }
        });
        alert.setCancelable(false);
        alert.show();
        // } catch (final Exception e) {
        // e.printStackTrace();
        // LOG.i("SPLASHSCREEN", "Something went wrong with the alert. i am here to catch.");
        // }

    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.my_video_view);
        anVideoView = this;
        this.frameAt = 0;
        this.index = 0;
        this.context = this;
        this.bundle = this.getIntent().getExtras();

        this.videoPath = this.bundle.getString("path");
        this.clientVideoId = this.bundle.getString("videoid");
        this.coverImageBackground = (ImageView) this.findViewById(R.id.coverImageBackground);
        this.back = (ImageButton) this.findViewById(R.id.back);
        this.later = (ImageButton) this.findViewById(R.id.later);
        this.coverSelectionLayout = (LinearLayout) this.findViewById(R.id.coverpages);
        final DisplayMetrics displaymetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        this.screenHeight = displaymetrics.heightPixels;
        this.screenWidth = displaymetrics.widthPixels;
        LOG.i("H=" + this.screenHeight + "W=" + this.screenWidth);
        /**
         * retriving cover pages from video path and setting it to cover page layout
         */

        new LoadFrames().execute();

        this.later.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                AnVideoView.this.goToPlayer();
            }
        });

        this.back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                videoDisacrdAlert("Alert", "Do you want to discard this video?", AnVideoView.this,
                        AnVideoView.this.videoPath, null);
            }
        });

    }

    RelativeLayout getView(final Bitmap thumb) {

        final RelativeLayout videoView = (RelativeLayout) ((LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.filter_item, null);
        final ImageView videoImage = (ImageView) videoView.findViewById(R.id.filterimageview);

        videoImage.setTag(R.id.coverpageimageview_tag_id, Integer.valueOf(this.index));
        videoImage.setImageBitmap(thumb);

        if (this.index == 0) {
            videoImage.setBackground(this.getResources().getDrawable(R.drawable.coverpage_selection));
        }
        videoImage.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View view) {

                for (int i = 0; i < AnVideoView.this.coverSelectionLayout.getChildCount(); i++) {
                    final RelativeLayout videoView = (RelativeLayout) AnVideoView.this.coverSelectionLayout
                            .getChildAt(i);
                    final ImageView videoImage = (ImageView) videoView.findViewById(R.id.filterimageview);
                    videoImage.setBackground(null);
                }
                view.setBackground(AnVideoView.this.getResources().getDrawable(R.drawable.coverpage_selection));

                AnVideoView.this.frameAt = ((Integer) view.getTag(R.id.coverpageimageview_tag_id)).longValue();

                final Bitmap coverPage = Util.getVideoFrame(AnVideoView.this.videoPath, AnVideoView.this.frameAt);

                if (bitmapResized != null) {
                    bitmapResized.recycle();
                }

                bitmapResized = Bitmap.createScaledBitmap(coverPage, AnVideoView.this.screenWidth / 2,
                        AnVideoView.this.screenHeight / 2, false);
                AnVideoView.this.coverImageBackground.setImageBitmap(bitmapResized);

            }
        });
        this.index++;
        return videoView;

    }

    void goToPlayer() {

        /*
         * if(bitmapResized!=null){ bitmapResized.recycle(); }
         */
        final Intent intent = new Intent(AnVideoView.this, PlayerActivity.class);
        intent.putExtra(Constant.PATH, this.videoPath);
        intent.putExtra(Constant.CLIENT_ID, this.clientVideoId);
        intent.putExtra(Constant.SERVER_ID, 0);
        final VideoDetails video = new VideoDetails();
        video.setVideoCoverPageTime(String.valueOf(this.frameAt));
        video.setUserId(Config.getUserId());
        intent.putExtra("isNavigateToPlay", true);
        intent.putExtra("video", video);
        // finish();
        this.startActivity(intent);
    }

    public class LoadFrames extends AsyncTask<Void, Bitmap, Void> {

        private ProgressDialog prg;

        @Override
        protected Void doInBackground(final Void... params) {

            final MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(AnVideoView.this.videoPath);
            final String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            final long timeInmillisec = Long.parseLong(time);
            final long duration = timeInmillisec / 1000;
            final int count = (int) duration;
            for (int i = 0; i <= count; i++) {
                final Bitmap bitmap = Util.getVideoFrame(AnVideoView.this.videoPath, i);
                this.publishProgress(bitmap);
            }

            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {

            super.onPostExecute(result);
            final Bitmap coverPage = Util.getVideoFrame(AnVideoView.this.videoPath);

            if (bitmapResized != null) {
                bitmapResized.recycle();
            }
            bitmapResized = Bitmap.createScaledBitmap(coverPage, AnVideoView.this.screenWidth / 2,
                    AnVideoView.this.screenHeight / 2, false);
            AnVideoView.this.coverImageBackground.setImageBitmap(bitmapResized);
            this.prg.dismiss();
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            AnVideoView.this.index = 0;
            this.prg = ProgressDialog.show(AnVideoView.this.context, "", "", true);
            this.prg.setContentView(((LayoutInflater) AnVideoView.this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.progress_bar, null, false));
            this.prg.setCancelable(false);
            this.prg.setCanceledOnTouchOutside(false);
            this.prg.show();
        }

        @Override
        protected void onProgressUpdate(final Bitmap... values) {

            AnVideoView.this.coverSelectionLayout.addView(AnVideoView.this.getView(values[0]));
        }

    }

}
