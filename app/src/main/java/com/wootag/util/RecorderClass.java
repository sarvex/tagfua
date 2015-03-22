/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.AlertDialog;
import android.content.Context;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Chronometer;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

public class RecorderClass extends SurfaceView implements SurfaceHolder.Callback, MediaRecorder.OnErrorListener,
        MediaRecorder.OnInfoListener {

    private static final int CIF_HEIGHT = 240;
    private static final int CIF_WIDTH = 320;
    private static final int FRAME_RATE = 2;
    private static final Logger LOG = LoggerManager.getLogger();

    public Camera camera;

    public String filepath = "";

    public boolean flash;

    public MediaRecorder recorder;

    private AudioManager audioManager;
    private final Context context;
    private FileOutputStream fos;

    private final SurfaceHolder holder;

    private boolean initialized;
    private boolean started;

    private int maxduration;

    private String packageName = "";

    public RecorderClass(final Context context, final AttributeSet attrs) {

        super(context, attrs);
        this.context = context;
        this.holder = this.getHolder();
        this.holder.addCallback(this);
        this.holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        this.packageName = context.getPackageName();

    }

    public static String getCurrentTimeStamp() {

        return android.text.format.DateFormat.format("yyyy-MM-dd,hh-mm-ss", new java.util.Date()).toString();
    }

    public String getPath() {

        return this.filepath;
    }

    public void initializePriview(final boolean flash) {

        this.flash = flash;

        this.camera = Camera.open();
        this.camera.unlock();

        this.recorder = new MediaRecorder();
        this.recorder.setCamera(this.camera);

        if (this.recorder != null) {
            this.audioManager = (AudioManager) this.context.getSystemService(Context.AUDIO_SERVICE);
            this.audioManager.setMode(AudioManager.STREAM_VOICE_CALL);
            this.audioManager.startBluetoothSco();

            this.recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
            if (android.os.Build.VERSION.SDK_INT >= 8) {
                // recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
                this.recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
                final CamcorderProfile camcoderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
                this.recorder.setProfile(camcoderProfile);
            } else {
                // recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                this.recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                this.recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                this.recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                this.recorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
                this.recorder.setVideoSize(CIF_WIDTH, CIF_HEIGHT);
                this.recorder.setVideoFrameRate(FRAME_RATE);
            }
            this.recorder.setMaxDuration(this.maxduration * 1000);
            this.recorder.setPreviewDisplay(this.holder.getSurface());

            final String data_folder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/data/";
            final File file = new File(data_folder);
            if (!file.exists()) {
                file.mkdirs();
            }
            final String path = data_folder + getCurrentTimeStamp() + ".3gp";

            this.filepath = path;

            this.recorder.setOutputFile(path);
            try {
                this.recorder.prepare();
            } catch (final IOException e) {
                LOG.e(e);
            } finally {
                try {
                    this.releaseRecorder();
                    this.recorder.prepare();
                } catch (final IOException exception) {
                    LOG.e(exception);
                }
            }
        }

        this.initialized = true;

    }

    public String initializeRecording(final Context context, final Chronometer chrono) {// , final ImageView stop

        if (!this.initialized) {
            this.initializePriview(false);
        }

        this.recorder.setOnErrorListener(new OnErrorListener() {

            @Override
            public void onError(final MediaRecorder mr, final int what, final int extra) {

                // final AlertDialog alert = new AlertDialog.Builder(context).create();
                // alert.setTitle("Error occured");
                // alert.setMessage(what+" ERROR CODE "+extra);
                // alert.setButton("OK", new DialogInterface.OnClickListener() {
                // public void onClick(DialogInterface dialog, int which) {
                // return;
                // } });
                //
                // //alert.setIcon(R.drawable.application_icon_50x50);
                // alert.setCancelable(false);
                // alert.show();

            }
        });

        this.recorder.setOnInfoListener(new OnInfoListener() {

            @Override
            public void onInfo(final MediaRecorder mr, final int what, final int extra) {

                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {

                    RecorderClass.this.stopRecording();
                    chrono.stop();

                    final AlertDialog alert = new AlertDialog.Builder(context).create();
                    alert.setTitle("Maximum time reached");
                    alert.setMessage("You reached the maximum time limit");
                    // alert.setButton("OK", new DialogInterface.OnClickListener() {
                    // public void onClick(DialogInterface dialog, int which) {
                    //
                    // stop.performClick();
                    // } });

                    // alert.setIcon(R.drawable.application_icon_50x50);
                    alert.setCancelable(false);
                    alert.show();

                }

            }
        });

        return this.filepath;
    }

    public boolean isStarted() {

        return this.started;
    }

    @Override
    public void onError(final MediaRecorder mr, final int what, final int extra) {

    }

    @Override
    public void onInfo(final MediaRecorder mr, final int what, final int extra) {

    }

    public void releaseRecorder() throws IOException {

        if (this.recorder != null) {
            this.started = false;
            this.camera.release();
            this.camera = null;
            this.recorder.reset();
            this.recorder.release();
        }
        if (this.fos != null) {
            this.fos.close();
        }
        this.initialized = false;
    }

    public void startRecording() {

        this.started = true;
        this.recorder.start();
    }

    public void stopRecording() {

        this.started = false;
        this.recorder.stop();

        // camera.stopPreview();
    }

    @Override
    public void surfaceChanged(final SurfaceHolder holder, final int format, final int width, final int height) {

    }

    @Override
    public void surfaceCreated(final SurfaceHolder holder) {

        this.initializePriview(false);

    }

    @Override
    public void surfaceDestroyed(final SurfaceHolder holder) {

        if (this.camera != null) {
            this.camera.release();
            this.camera = null;
        }

        this.audioManager.stopBluetoothSco();
    }
}
