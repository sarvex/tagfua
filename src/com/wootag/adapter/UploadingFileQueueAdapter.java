/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wTagFuadapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.woTagFu;
import com.wooTagFunnectivity.VideoDataBase;
import com.wootTagFunectivity.WootaTagFudService;
import com.wootagTagFuideoInfo;
import com.wootag.TagFuonfig;
import com.wootag.uTagFuil;

public class UploadingFileQueueAdapter extends ArrayAdapter<VideoInfo> {

    protected static final Logger LOG = LoggerManager.getLogger();

    protected List<VideoInfo> videoInfos;
    protected final Context context;

    public UploadingFileQueueAdapter(final Context context, final int resource, final List<VideoInfo> videoInfos) {

        super(context, resource, videoInfos);
        this.context = context;
        this.videoInfos = videoInfos;
    }

    @Override
    public int getCount() {

        return this.videoInfos.size();
    }

    @Override
    public VideoInfo getItem(final int position) {

        return this.videoInfos.get(position);
    }

    public List<VideoInfo> getVideoInfos() {

        return this.videoInfos;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        ViewHolder holder;
        final VideoInfo videoInfo = this.getItem(position);

        if (convertView == null) {
            convertView = ((LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.uploading_file_queue_item, parent, false);
            holder = this.initHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.fileTitleTextView.setText(videoInfo.getTitle());
        final String time = Util.getTZLocalTime(videoInfo.getUploadDate());
        holder.createdTimeTextView.setText(time);
        holder.delete.setTag(videoInfo);
        holder.retry.setTag(videoInfo);
        /**
         * 2 current video parts uploading 3 waiting for publish 0 waiting for uplaod 1 uploaded video
         */
        if (videoInfo.getUploadStatus() == 2) {
            holder.percentageTextView.setText(Config.getUploadedPercentage() + "%");
            holder.percentageTextView.setVisibility(View.VISIBLE);
            holder.fileStateTextView.setText("Uploaded");
        } else if (videoInfo.getUploadStatus() == 3) {
            holder.percentageTextView.setText(videoInfo.getUploadPercentage() + "%");
            holder.percentageTextView.setVisibility(View.VISIBLE);// gone
            if (videoInfo.getRetry() == 1) {
                holder.fileStateTextView.setText("Uploaded.Failed to publish");
            } else {
                holder.fileStateTextView.setText("Uploaded.Waiting to publish");
            }
        } else if (videoInfo.getUploadStatus() == 1) {
            holder.percentageTextView.setText(videoInfo.getUploadPercentage() + "%");
            holder.percentageTextView.setVisibility(View.VISIBLE);// gone
            holder.fileStateTextView.setText("Uploaded");
        } else {
            holder.percentageTextView.setText(videoInfo.getUploadPercentage() + "%");
            holder.percentageTextView.setVisibility(View.VISIBLE);// gone
            if (videoInfo.getRetry() == 1) {
                holder.fileStateTextView.setText("Failed to upload");
            } else {
                holder.fileStateTextView.setText("Waiting to upload");// inprocess
            }
        }

        if (videoInfo.getRetry() == 1) {
            holder.retry.setVisibility(View.VISIBLE);
            holder.delete.setVisibility(View.VISIBLE);
        } else {
            holder.retry.setVisibility(View.GONE);
        }
        if ((((videoInfo.getUploadStatus() == 2) || (videoInfo.getUploadStatus() == 3)) && (videoInfo.getRetry() == 0))) {
            holder.delete.setVisibility(View.GONE);
        } else {
            holder.delete.setVisibility(View.VISIBLE);
        }

        holder.delete.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                final VideoInfo currentVideo = (VideoInfo) v.getTag();
                VideoDataBase.getInstance(UploadingFileQueueAdapter.this.context).removeContentFromDownloadQueue(
                        currentVideo);
                UploadingFileQueueAdapter.this.videoInfos.remove(currentVideo);
                UploadingFileQueueAdapter.this.notifyDataSetChanged();
            }
        });
        /**
         * Define retry action .If upload status 3 need to hit upload api .if it is 2 or 0 need to hit parts upload api
         */
        holder.retry.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                final VideoInfo currentVideo = (VideoInfo) v.getTag();
                VideoDataBase.getInstance(UploadingFileQueueAdapter.this.context).updateVideoData(0,
                        currentVideo.getLocalMediaPath(), currentVideo.getVideoClientId(),
                        currentVideo.getUploadStatus(), 0);
                currentVideo.setRetry(0);
                UploadingFileQueueAdapter.this.notifyDataSetChanged();

                if (Util.isServiceRunning(UploadingFileQueueAdapter.this.context, "BackgroundFileTransferService")) {
                    LOG.d("HomeActivity", "BFTS is already running.");
                } else {
                    LOG.d("HomeActivity", "Starting BFTS.");
                    WakefulIntentService.sendWakefulWork(UploadingFileQueueAdapter.this.context,
                            WootagUploadService.class);
                }
            }
        });
        return convertView;
    }

    public void setVideoInfos(final List<VideoInfo> videoInfos) {

        this.videoInfos = videoInfos;
    }

    private ViewHolder initHolder(final View convertView) {

        final ViewHolder holder = new ViewHolder();

        holder.fileTitleTextView = (TextView) convertView.findViewById(R.id.videoTitleTextView);
        holder.createdTimeTextView = (TextView) convertView.findViewById(R.id.videocreatedAtTextView);
        holder.fileStateTextView = (TextView) convertView.findViewById(R.id.filestateTextView);
        holder.percentageTextView = (TextView) convertView.findViewById(R.id.percentageTextView);
        holder.retry = (TextView) convertView.findViewById(R.id.retry);
        holder.delete = (TextView) convertView.findViewById(R.id.delete);

        return holder;
    }

    public class ViewHolder {

        public TextView fileTitleTextView;
        public TextView createdTimeTextView;
        public TextView fileStateTextView;
        public TextView percentageTextView;
        public TextView retry;
        public TextView delete;
    }

}
