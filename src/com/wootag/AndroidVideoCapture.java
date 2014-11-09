/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.wootag;

import java.io.File;
import java.io.IOException;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wootag.util.Config;
import com.wootag.util.MainManager;
import com.wootag.util.Util;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AndroidVideoCapture extends Activity implements OnClickListener, Camera.PreviewCallback,
        MediaRecorder.OnInfoListener {

    protected static final Logger LOG = LoggerManager.getLogger();
    public static AndroidVideoCapture androidVideoCapture;

    private static String currentDateTimeString;
    private static String path;

    private Camera myCamera;
    private ImageView cameraView;
    private ImageView myButton, cancel, done;
    private MediaRecorder mediaRecorder;
    private MyCameraSurfaceView myCameraSurfaceView;
    private RelativeLayout recordView;
    private String clientVideoId;
    private SurfaceHolder surfaceHolder;
    private TextView recordInstruction;
    private ViewGroup myCameraPreview;
    private boolean isAvailable;
    private boolean recording;
    private int camreaId;

    protected Camera.Size size;
    protected Chronometer chrono;
    protected LinearLayout recordInstructionView;

    @Override
    public void onClick(final View view) {

        switch (view.getId()) {
        case R.id.cancelrecord:
            this.onCancelRecordClick();
            break;
        case R.id.done:
            this.onDoneClick();
            break;
        case R.id.record:
            this.onRecordClick();
            break;
        case R.id.camera:
            this.onCameraClick();
            break;
        default:
            break;
        }

    }

    /** Called when the activity is first created. */

    @Override
    public void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.recording = false;
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.setContentView(R.layout.record);
        androidVideoCapture = this;
        this.myButton = (ImageView) this.findViewById(R.id.record);
        this.cancel = (ImageView) this.findViewById(R.id.cancelrecord);
        this.done = (ImageView) this.findViewById(R.id.done);
        this.cameraView = (ImageView) this.findViewById(R.id.camera);
        this.recordInstruction = (TextView) this.findViewById(R.id.recordinstruction);
        this.recordInstructionView = (LinearLayout) this.findViewById(R.id.recordinstructionview);
        this.recordView = (RelativeLayout) this.findViewById(R.id.recordView);
        this.recordInstructionView.setVisibility(View.GONE);
        this.myButton.setOnClickListener(this);
        this.cancel.setOnClickListener(this);
        this.done.setOnClickListener(this);
        this.cameraView.setOnClickListener(this);
        this.chrono = (Chronometer) this.findViewById(R.id.chronometer);
        this.recordView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(final View ignored, final MotionEvent event) {

                AndroidVideoCapture.this.recordInstructionView.setVisibility(View.GONE);
                return false;
            }
        });

    }

    @Override
    public void onInfo(final MediaRecorder mr, final int what, final int extra) {

        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
            LOG.v("VIDEOCAPTURE Maximum Duration Reached");
            // mr.stop();
            try {
                this.chrono.stop();
                Config.setNewlyCreatedVideo(true);
                if (this.recording) {
                    // stop recording and release camera
                    mr.stop(); // stop the recording
                    this.releaseMediaRecorder(); // release the MediaRecorder object
                    final Intent intent = new Intent(AndroidVideoCapture.this, AnVideoView.class);
                    intent.putExtra("path", path);
                    intent.putExtra("videoid", this.clientVideoId);
                    this.finish();
                    this.startActivity(intent);

                }
            } catch (final IllegalStateException e) {
                LOG.e("exception while releasing camera resources " + e.toString());
            }

        }
    }

    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {

        final Size size = this.myCamera.getParameters().getPreviewSize();
        LOG.i("onpreviewframe camera supported size " + size.height);
    }

    private Camera getCameraInstance() {

        Camera camera = null;
        try {
            camera = Camera.open(this.camreaId);
        } catch (final RuntimeException e) {
            LOG.e(e);
        }
        return camera;
    }

    /**
     * @return true : Front facing camera is available. false : Front facing camera is not available.
     */
    private boolean isFrontCameraAvailable() {

        int cameraCount = 0;
        boolean isFrontCameraAvailable = false;
        cameraCount = Camera.getNumberOfCameras();

        while (cameraCount > 0) {
            final CameraInfo cameraInfo = new CameraInfo();
            cameraCount--;
            Camera.getCameraInfo(cameraCount, cameraInfo);
            if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
                isFrontCameraAvailable = true;
                break;
            }

        }

        return isFrontCameraAvailable;
    }

    /**
     *
     */
    private void onCameraClick() {

        if (this.recordInstructionView.isShown()) {
            this.recordInstructionView.setVisibility(View.GONE);
        }
        if (this.camreaId == 0) {
            this.camreaId = 1;
        } else {
            this.camreaId = 0;
        }
        try {
            if (this.mediaRecorder != null) {
                this.mediaRecorder.stop(); // stop the recording
                this.releaseMediaRecorder();
            }

            this.myCameraPreview.removeView(this.myCameraSurfaceView);
            this.releaseCamera();
            this.myCamera = this.getCameraInstance();

            this.myCameraSurfaceView = new MyCameraSurfaceView(this, this.myCamera);
            this.myCameraPreview.addView(this.myCameraSurfaceView);
        }

        catch (final IllegalStateException e) {
            LOG.e(e);
        }
    }

    /**
     *
     */
    private void onCancelRecordClick() {

        if (this.recordInstructionView.isShown()) {
            this.recordInstructionView.setVisibility(View.GONE);
        }
        try {
            if (this.mediaRecorder != null) {
                this.mediaRecorder.stop(); // stop the recording
                this.releaseMediaRecorder();
            }
        } catch (final IllegalStateException e) {
            LOG.e(e);
        }
        this.finish();
    }

    // ending

    /**
     *
     */
    private void onDoneClick() {

        if (this.recordInstructionView.isShown()) {
            this.recordInstructionView.setVisibility(View.GONE);
        }
        try {
            this.chrono.stop();
            Config.setNewlyCreatedVideo(true);
            if (this.recording) {
                // stop recording and release camera
                this.mediaRecorder.stop(); // stop the recording
                this.releaseMediaRecorder(); // release the MediaRecorder object
                final Intent intent = new Intent(AndroidVideoCapture.this, AnVideoView.class);
                intent.putExtra("path", path);
                intent.putExtra("videoid", this.clientVideoId);
                this.finish();
                this.startActivity(intent);

            }
        } catch (final IllegalStateException e) {
            LOG.e("exception while releasing camera resources " + e.toString());
        }
    }

    /**
     *
     */
    private void onRecordClick() {

        if (this.recordInstructionView.isShown()) {
            this.recordInstructionView.setVisibility(View.GONE);
        }
        this.cameraView.setVisibility(View.GONE);
        this.myButton.setImageResource(R.drawable.record_disable);
        this.myButton.setEnabled(false);
        this.done.setEnabled(true);
        this.done.setImageResource(R.drawable.done_record);
        this.chrono.setText("00");
        this.chrono.setBase(SystemClock.elapsedRealtime());
        this.chrono.start();
        this.releaseCamera();
        if (!this.prepareMediaRecorder()) {
            Toast.makeText(AndroidVideoCapture.this, "Fail in prepareMediaRecorder()!\n - Ended -", Toast.LENGTH_LONG)
                    .show();
            this.finish();
        }
        try {
            this.mediaRecorder.start();
            this.recording = true;
        } catch (final IllegalStateException e) {
            LOG.e("exception raised while starting the camera :" + e.toString());
        }
    }

    // only for front cam
    private Camera openFrontFacingCameraGingerbread() {

        int cameraCount = 0;
        Camera cam = null;
        final Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    cam = Camera.open(camIdx);
                } catch (final RuntimeException e) {
                    LOG.e("Camera failed to open: " + e.getLocalizedMessage());
                }
            }
        }

        return cam;
    }

    private boolean prepareMediaRecorder() {

        this.myCamera = this.getCameraInstance();
        this.mediaRecorder = new MediaRecorder();

        this.myCamera.unlock();
        this.mediaRecorder.setCamera(this.myCamera);

        this.mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        this.mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        /**
         * checking 480p quality availble on current device
         */
        this.isAvailable = CamcorderProfile.hasProfile(this.camreaId, CamcorderProfile.QUALITY_480P);// QUALITY_720P
        if (this.isAvailable) {
            this.mediaRecorder.setProfile(CamcorderProfile.get(this.camreaId, CamcorderProfile.QUALITY_480P));
            // try {
            if (this.camreaId == 0) {
                this.mediaRecorder.setVideoSize(this.size.width, this.size.height);
            }
            // } catch (final IllegalStateException e) {
            // LOG.e("video resolution", e.toString());
            // }
        } else {
            /**
             * setting quality high if quality 480p not availble
             */
            this.mediaRecorder.setProfile(CamcorderProfile.get(this.camreaId, CamcorderProfile.QUALITY_HIGH));
        }

        // mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        // mediaRecorder.setProfile(CamcorderProfile.get(camreaId,
        // CamcorderProfile.QUALITY_720P));
        this.mediaRecorder.setOnInfoListener(this);
        // set path to output file
        currentDateTimeString = Util.getCurrentTimeStamp();
        // Changed by vasanth 12/02/2013 2:25AM
        final File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                "/Wootag/Videos/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        path = dir.getAbsolutePath() + "/" + currentDateTimeString + ".mp4";
        this.clientVideoId = currentDateTimeString + Util.getRandomTransactionId(1, 20);
        this.clientVideoId = this.clientVideoId.trim();
        LOG.i(" path :" + path + "  id :" + this.clientVideoId);
        // end
        this.mediaRecorder.setOutputFile(path);
        this.mediaRecorder.setMaxDuration(30000); // Set max duration 60 sec.
        this.mediaRecorder.setPreviewDisplay(this.myCameraSurfaceView.getHolder().getSurface());

        try {
            this.mediaRecorder.prepare();
        } catch (final IllegalStateException e) {
            LOG.e(e);
            this.releaseMediaRecorder();
            return false;
        } catch (final IOException e) {
            this.releaseMediaRecorder();
            LOG.e(e);
            return false;
        }
        return true;

    }

    private void releaseCamera() {

        if (this.myCamera != null) {
            this.myCamera.release();
            // release the camera for other applications
            this.myCamera = null;
        }
    }

    private void releaseMediaRecorder() {

        if (this.mediaRecorder != null) {
            this.mediaRecorder.reset(); // clear recorder configuration
            this.mediaRecorder.release(); // release the recorder object
            this.mediaRecorder = null;
            this.myCamera.lock(); // lock camera for later use
        }
    }

    @Override
    protected void onPause() {

        if (this.myCamera != null) {
            this.myCamera.setPreviewCallback(null);
            this.myCameraSurfaceView.getHolder().removeCallback(this.myCameraSurfaceView);
        }
        this.releaseMediaRecorder(); // if you are using MediaRecorder, release it
        // first
        this.releaseCamera(); // release the camera immediately on pause event
        super.onPause();
    }

    @Override
    protected void onResume() {

        super.onResume();
        this.myCamera = this.getCameraInstance();
        if (this.myCamera == null) {
            Toast.makeText(AndroidVideoCapture.this, "Fail to get Camera", Toast.LENGTH_LONG).show();
        }
        this.myCameraSurfaceView = new MyCameraSurfaceView(this, this.myCamera);
        this.myCameraPreview = (FrameLayout) this.findViewById(R.id.videoview);
        this.myCameraPreview.addView(this.myCameraSurfaceView);
        this.myButton.setEnabled(true);
        this.myButton.setImageResource(R.drawable.record_video);
        this.done.setEnabled(false);
        this.done.setImageResource(R.drawable.done_disable);
        this.chrono.setText("00");
        if (!this.isFrontCameraAvailable()) {
            this.cameraView.setVisibility(View.GONE);
        } else {
            this.cameraView.setVisibility(View.VISIBLE);
        }
        this.chrono.setOnChronometerTickListener(new OnChronometerTickListener() {

            @Override
            public void onChronometerTick(final Chronometer chronometer) {

                final long seconds = ((SystemClock.elapsedRealtime() - AndroidVideoCapture.this.chrono.getBase()) / 1000) % 60;
                String s = String.valueOf(seconds);
                if (seconds < 10) {
                    s = AndroidVideoCapture.this.getString(R.string._0) + seconds;
                }
                final String currentTime = s;
                chronometer.setText(currentTime);
            }
        });
        if (!MainManager.getInstance().isFirstTimeRecord()) {
            MainManager.getInstance().setFirstTimeRecord(true);
            this.recordInstructionView.setVisibility(View.VISIBLE);
            this.recordInstruction.setText("Find your moment to record and then click on red button");
            // showUserGuideDialog("Record your favourite moment and click on camera red button to record.");
        }
    }

    Camera.Size getBestPreviewSize(final int width, final int height, final Camera.Parameters parameters) {

        Camera.Size result = null;

        for (final Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if ((size.width <= width) && (size.height <= height)) {
                if (result == null) {
                    result = size;
                } else {
                    final int resultArea = result.width * result.height;
                    final int newArea = size.width * size.height;

                    if (newArea > resultArea) {
                        result = size;
                    }
                }
            }
        }
        return result;
    }

    public class MyCameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback, PreviewCallback {

        private final Camera mCamera;
        private final SurfaceHolder mHolder;

        public MyCameraSurfaceView(final Context context, final Camera camera) {

            super(context);
            this.mCamera = camera;
            // this.context = context;
            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            this.mHolder = this.getHolder();
            this.mHolder.addCallback(this);
            // deprecated setting, but required on Android versions prior to 3.0
            this.mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        @Override
        public void onPreviewFrame(final byte[] data, final Camera camera) {

            if (data != null) {
                LOG.i("onPreviewFrame onPreviewFrame " + data.length);
            }
            LOG.i("onPreviewFrame onPreviewFrame ");

        }

        @Override
        public void surfaceChanged(final SurfaceHolder holder, final int format, final int weight, final int height) {

            if (this.mHolder.getSurface() == null) {
                return;
            }

            if (this.mCamera == null) {
                return;
            }

            // stop preview before making changes
            this.mCamera.stopPreview();

            final Camera.Parameters parameters = this.mCamera.getParameters();
            final Size s = this.getBestSupportedSize(parameters.getSupportedPreviewSizes(), weight, height);
            parameters.setPreviewSize(s.width, s.height);
            AndroidVideoCapture.this.size = AndroidVideoCapture.this.getBestPreviewSize(s.width, s.height,
                    this.mCamera.getParameters());
            this.mCamera.setParameters(parameters);
            this.mCamera.startPreview();

        }

        @Override
        public void surfaceCreated(final SurfaceHolder holder) {

            try {
                if (this.mCamera != null) {
                    this.mCamera.setPreviewDisplay(holder);
                    this.mCamera.startPreview();
                }
            } catch (final IOException exception) {
                LOG.e("Error setting up preview display" + exception);
            }
        }

        @Override
        public void surfaceDestroyed(final SurfaceHolder holder) {

            LOG.i("surface destroy surface view destroyed");
            if (this.mCamera != null) {
                this.mCamera.stopPreview();
            }
        }

        private Size getBestSupportedSize(final List<Size> sizes, final int width, final int height) {

            Size bestSize = sizes.get(0);
            int largestArea = bestSize.width * bestSize.height;
            for (final Size s : sizes) {
                final int area = s.width * s.height;
                if (area > largestArea) {
                    bestSize = s;
                    largestArea = area;
                }
            }
            return bestSize;
        }

    }
}
