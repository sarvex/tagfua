/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONException;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wootag.Constant;
import com.wootag.MenuActivity;
import com.wootag.R;
import com.wootag.async.FollowAsyncTask;
import com.wootag.dto.ErrorResponse;
import com.wootag.dto.Notification;
import com.wootag.dto.VideoDetails;
import com.wootag.model.Backend;
import com.wootag.pulltorefresh.PullToRefreshBase;
import com.wootag.pulltorefresh.PullToRefreshBase.OnRefreshListener;
import com.wootag.pulltorefresh.PullToRefreshScrollView;
import com.wootag.slideout.SlideoutActivity;
import com.wootag.ui.Image;
import com.wootag.util.Alerts;
import com.wootag.util.Config;
import com.wootag.util.FollowInterface;
import com.wootag.util.MainManager;
import com.wootag.util.Util;

public class NotificationsFragment extends BaseFragment implements OnClickListener, FollowInterface {

    public static NotificationsFragment notificationFragment;

    private static final String _B = "</b> ";
    private static final String B = "<b>";
    private static final String FONT = "</font>";
    private static final String FONT_COLOR_10A2E7 = "<font color='#10a2e7'>";
    private static final Logger LOG = LoggerManager.getLogger();
    private static final String NO_SENDER_ID_FOR_THIS_NOTIFICATION = "No sender id for this notification";
    private static final String NOTIFICATION = "notification";
    private static final String NOTIFICATION_VISITED = "notificationvisited";
    private static final String NOTIFICATIONS = "Notifications";
    private static final String USER_ID = "userid";
    private static final String X = "x";

    protected Context context;
    protected TextView errorMessageTextView;
    private TextView heading;
    private LayoutInflater inflater;
    private Button menu;
    private LinearLayout notificationsLayout;
    protected Notification privateGroupNotification;
    protected PullToRefreshScrollView pullRefreshScrollView;
    protected boolean pullToRefresh;
    private Button search;

    private final BroadcastReceiver VideoUploadNotificationReciver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {

            final String action = intent.getAction();
            if (NOTIFICATION.equalsIgnoreCase(action)) {
                NotificationsFragment.this.pullToRefresh = true;
                Config.setUserID(MainManager.getInstance().getUserId());
                final NotificationAsync req = new NotificationAsync(Config.getUserId(), false, false, true);
                req.execute();
            }
        }
    };
    private View view;

    @Override
    public void follow(final String type) {

        new removeNotificationAsync(this.privateGroupNotification).execute();
        final LinearLayout view1 = (LinearLayout) this.view.findViewById(this.privateGroupNotification.getViewId());
        this.notificationsLayout.removeView(view1);

    }

    public String getReloution() {

        final DisplayMetrics displaymetrics = new DisplayMetrics();
        this.getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        final int screenHeight = displaymetrics.heightPixels;
        final int screenWidth = displaymetrics.widthPixels;
        return screenWidth + X + screenHeight;
    }

    public void loadResponse(final Object response) {

        if (response instanceof List<?>) {
            this.errorMessageTextView.setVisibility(View.GONE);
            final List<Notification> list = (ArrayList<Notification>) response;
            this.loadData(list);
        } else {
            if (response instanceof ErrorResponse) {
                final ErrorResponse res = (ErrorResponse) response;
                // errorMessageTextView.setText(res.getMessage());
                // errorMessageTextView.setVisibility(View.VISIBLE);
                Alerts.showInfoOnly(res.getMessage(), this.context);

            }
        }
    }

    @Override
    public void onClick(final View ignored) {

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        this.view = inflater.inflate(R.layout.notifications, container, false);
        this.context = this.getActivity();
        notificationFragment = this;
        Config.setUserID(MainManager.getInstance().getUserId());
        Config.setDeviceResolutionValue(this.getReloution());
        this.inflater = inflater;
        this.loadViews(this.view);
        this.registerBroadcastReceiver();
        new getResponseFromCacheAsync().execute();

        return this.view;
    }

    @Override
    public void onDestroy() {

        if (this.VideoUploadNotificationReciver != null) {
            this.getActivity().getApplicationContext().unregisterReceiver(this.VideoUploadNotificationReciver);
        }
        super.onDestroy();
    }

    @Override
    public void onPause() {

        this.getActivity().overridePendingTransition(0, 0);
        super.onPause();
    }

    @Override
    public void onResume() {

        this.getActivity().overridePendingTransition(0, 0);
        super.onResume();
    }

    private LinearLayout getCommentNotification(final Notification notification) {

        LinearLayout notificationView = null;
        if (this.inflater != null) {
            notificationView = (LinearLayout) this.inflater.inflate(R.layout.comment_notification, null);
            final TextView ownername = (TextView) notificationView.findViewById(R.id.ownername);
            final TextView commentText = (TextView) notificationView.findViewById(R.id.description);
            final ImageView videoImage1 = (ImageView) notificationView.findViewById(R.id.videothumb);
            final ImageView ownerImage = (ImageView) notificationView.findViewById(R.id.member);

            if (notification.getUserPickUrl() == null) {
                ownerImage.setImageResource(R.drawable.member);

            } else {
                Image.displayImage(notification.getUserPickUrl(), this.getActivity(), ownerImage, 0);
            }

            final String ownerNameWithColor = FONT_COLOR_10A2E7 + notification.getSenderName() + FONT;
            final String sourceString = B + ownerNameWithColor + _B + notification.getMessage();
            ownername.setText(Html.fromHtml(sourceString));

            switch (notification.getType()) {
            case 2:
            case 6:
                final SpannableString spannable = new SpannableString(notification.getCommentDescription());
                Util.emotifySpannable(spannable);
                commentText.setText(spannable);
                break;
            case 3:
            case 5:
                commentText.setText(notification.getCreatedTime());
                break;
            default:
                break;
            }

            ownername.setTag(notification);
            ownerImage.setTag(notification);
            notificationView.setTag(notification);

            if (notification.getVideoThumbPath() == null) {
                videoImage1.setImageResource(R.drawable.notif_banner);
            } else {
                Image.displayImage(notification.getVideoThumbPath(), this.getActivity(), videoImage1, 2);
            }

            ownerImage.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View v) {

                    final Notification notif = (Notification) v.getTag();
                    if (notif.getSenderId() == null) {
                        Alerts.showInfoOnly(NO_SENDER_ID_FOR_THIS_NOTIFICATION, NotificationsFragment.this.context);

                    } else {
                        final OtherUserFragment fragment = new OtherUserFragment();
                        final Bundle bundle = new Bundle();
                        bundle.putString(USER_ID, notif.getSenderId());
                        bundle.putString(Constant.ROOT_FRAGMENT, Constant.NOTIFICATIONS_PAGE);
                        fragment.setArguments(bundle);
                        BaseFragment.tabActivity.pushFragments(R.id.notificationTab, fragment, Constant.OTHERS_PAGE,
                                NotificationsFragment.this, Constant.NOTIFICATIONS);
                    }
                }
            });

            notificationView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View v) {

                    final Notification notif = (Notification) v.getTag();
                    if (notif.getSenderId() == null) {
                        Alerts.showInfoOnly(NO_SENDER_ID_FOR_THIS_NOTIFICATION, NotificationsFragment.this.context);

                    } else {
                        final VideoDetails video = new VideoDetails();
                        video.setVideoID(notif.getVideoId());
                        video.setUserId(notif.getSenderId());

                        final NotificationVideoDetailsFragment fragment = new NotificationVideoDetailsFragment();
                        final Bundle bundle = new Bundle();
                        bundle.putSerializable("video", video);
                        bundle.putInt("notificationtype", notif.getType());
                        fragment.setArguments(bundle);

                        BaseFragment.tabActivity.pushFragments(R.id.notificationTab, fragment,
                                Constant.NOTIFICATION_DETAILS, NotificationsFragment.this, Constant.NOTIFICATIONS);
                    }
                }
            });
        }

        return notificationView;

    }

    /**
     * get the comment notification view and add it to mainlayout
     */
    private void getCommentNotificationView(final Notification notification) {

        final LinearLayout view = this.getCommentNotification(notification);
        if (view != null) {
            this.notificationsLayout.addView(view);
        }
    }

    private LinearLayout getFollowNotification(final Notification notification) {

        LinearLayout notificationView = null;
        notificationView = (LinearLayout) this.inflater.inflate(R.layout.follow_notification, null);
        final ImageView ownerImage = (ImageView) notificationView.findViewById(R.id.member);
        final TextView ownername = (TextView) notificationView.findViewById(R.id.ownername);
        final TextView notificationcreated = (TextView) notificationView.findViewById(R.id.notificationcreatedtime);
        ownerImage.setTag(notification);
        ownername.setTag(notification);
        notificationView.setTag(notification);

        final String ownerNameWithColor = FONT_COLOR_10A2E7 + notification.getSenderName() + FONT;
        final String sourceString = B + ownerNameWithColor + _B + notification.getMessage();
        ownername.setText(Html.fromHtml(sourceString));

        // ownername.setText(notification.getSender_name() + " "+notification.getMessage());
        notificationcreated.setText(notification.getCreatedTime());
        if (notification.getUserPickUrl() == null) {
            ownerImage.setImageResource(R.drawable.member);
        } else {
            Image.displayImage(notification.getUserPickUrl(), this.getActivity(), ownerImage, 0);
        }

        notificationView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                final Notification notif = (Notification) v.getTag();
                if (notif.getSenderId() == null) {
                    Alerts.showInfoOnly(NO_SENDER_ID_FOR_THIS_NOTIFICATION, NotificationsFragment.this.context);

                } else {
                    final OtherUserFragment fragment = new OtherUserFragment();
                    final Bundle bundle = new Bundle();
                    bundle.putString(USER_ID, notif.getSenderId());
                    bundle.putString(Constant.ROOT_FRAGMENT, Constant.NOTIFICATIONS_PAGE);
                    fragment.setArguments(bundle);
                    BaseFragment.tabActivity.pushFragments(R.id.notificationTab, fragment, Constant.OTHERS_PAGE,
                            NotificationsFragment.this, Constant.NOTIFICATIONS);
                }
            }
        });

        return notificationView;

    }

    /**
     * get the follow notification view and add it to mainlayout
     */
    private void getFollowNotificationView(final Notification notification) {

        final LinearLayout view = this.getFollowNotification(notification);
        if (view != null) {
            this.notificationsLayout.addView(view);
        }
    }

    /**
     * get the private group notification view and add it to mainlayout
     */
    private void getPrivateGroupAcceptNotificationView(final Notification notification) {

        final LinearLayout view = this.getPrivateGroupRequestNotification(notification, true);
        if (view != null) {
            this.notificationsLayout.addView(view);
        }

    }

    private void getPrivateGroupNotificationView(final Notification notification) {

        final LinearLayout view = this.getPrivateGroupRequestNotification(notification, false);
        if (view != null) {
            this.notificationsLayout.addView(view);
        }
    }

    private LinearLayout getPrivateGroupRequestNotification(final Notification notification, final boolean isAccepted) {

        LinearLayout notificationView = null;
        notificationView = (LinearLayout) this.inflater.inflate(R.layout.private_group_notification, null);
        final ImageView ownerImage = (ImageView) notificationView.findViewById(R.id.member);
        final ImageView add = (ImageView) notificationView.findViewById(R.id.add);
        final ImageView delete = (ImageView) notificationView.findViewById(R.id.delete);
        final TextView ownername = (TextView) notificationView.findViewById(R.id.ownername);
        final TextView createdTime = (TextView) notificationView.findViewById(R.id.description);
        final RelativeLayout userPic = (RelativeLayout) notificationView.findViewById(R.id.profileImageRL);
        final int id = Integer.parseInt(notification.getNoticeId());
        notificationView.setId(id);
        userPic.setTag(notification);
        notification.setViewId(id);
        ownerImage.setTag(notification);
        add.setTag(notification);
        delete.setTag(notification);
        ownername.setTag(notification);
        notificationView.setTag(notification);

        final String ownerNameWithColor = FONT_COLOR_10A2E7 + notification.getSenderName() + FONT;
        final String sourceString = B + ownerNameWithColor + _B + notification.getMessage();
        ownername.setText(Html.fromHtml(sourceString));

        createdTime.setText(notification.getCreatedTime());

        if (isAccepted) {
            add.setVisibility(View.GONE);
            delete.setVisibility(View.GONE);
        } else {
            add.setVisibility(View.VISIBLE);
            delete.setVisibility(View.VISIBLE);
        }

        if (notification.getUserPickUrl() == null) {
            ownerImage.setImageResource(R.drawable.member);
        } else {
            Image.displayImage(notification.getUserPickUrl(), this.getActivity(), ownerImage, 1);
        }

        userPic.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View view) {

                final Notification notification = (Notification) view.getTag();
                if (notification.getSenderId() == null) {
                    Alerts.showInfoOnly(NO_SENDER_ID_FOR_THIS_NOTIFICATION, NotificationsFragment.this.context);

                } else {
                    final OtherUserFragment fragment = new OtherUserFragment();
                    final Bundle bundle = new Bundle();
                    bundle.putString(Constant.ROOT_FRAGMENT, Constant.NOTIFICATIONS_PAGE);
                    bundle.putString(USER_ID, notification.getSenderId());
                    fragment.setArguments(bundle);
                    BaseFragment.tabActivity.pushFragments(R.id.notificationTab, fragment, Constant.OTHERS_PAGE,
                            NotificationsFragment.this, Constant.NOTIFICATIONS);
                }
            }
        });
        add.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                NotificationsFragment.this.privateGroupNotification = (Notification) v.getTag();

                if (NotificationsFragment.this.privateGroupNotification.getSenderId() == null) {
                    Alerts.showInfoOnly(NO_SENDER_ID_FOR_THIS_NOTIFICATION, NotificationsFragment.this.context);

                } else {
                    final FollowAsyncTask task = new FollowAsyncTask(
                            NotificationsFragment.this.privateGroupNotification.getSenderId(), Config.getUserId(),
                            Constant.ADD_PRIVATE_GROUP_REQUEST, NotificationsFragment.this.context);
                    task.delegate = NotificationsFragment.this;
                    task.execute();
                }
            }
        });
        delete.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                NotificationsFragment.this.privateGroupNotification = (Notification) v.getTag();
                if (NotificationsFragment.this.privateGroupNotification.getSenderId() == null) {
                    Alerts.showInfoOnly(NO_SENDER_ID_FOR_THIS_NOTIFICATION, NotificationsFragment.this.context);

                } else {
                    final FollowAsyncTask task = new FollowAsyncTask(
                            NotificationsFragment.this.privateGroupNotification.getSenderId(), Config.getUserId(),
                            Constant.UN_PRIVATE, NotificationsFragment.this.context);
                    task.delegate = NotificationsFragment.this;
                    task.execute();
                }
            }
        });

        return notificationView;
    }

    private void loadData(final List<Notification> notifications) {

        this.notificationsLayout.removeAllViews();
        if ((notifications != null) && (notifications.size() > 0)) {
            for (int i = 0; i < notifications.size(); i++) {
                final Notification notification = notifications.get(i);

                switch (notification.getType()) {
                case 1:
                    this.getFollowNotificationView(notification);
                    break;
                case 2:
                case 3:
                case 5:
                    this.getCommentNotificationView(notification);
                    break;
                case 4:
                    this.getPrivateGroupNotificationView(notification);
                    break;
                case 6:
                    this.getPrivateGroupAcceptNotificationView(notification);
                    break;
                default:
                    break;

                }
            }
        } else {
            this.errorMessageTextView.setText("No new notifications");
            this.errorMessageTextView.setVisibility(View.VISIBLE);
        }

    }

    private void loadViews(final View view) {

        this.errorMessageTextView = (TextView) view.findViewById(R.id.notificationserrormessageView);
        this.pullRefreshScrollView = (PullToRefreshScrollView) view.findViewById(R.id.scrollView1);
        this.notificationsLayout = (LinearLayout) view.findViewById(R.id.notificationLayout);
        this.menu = (Button) view.findViewById(R.id.menu);
        this.search = (Button) view.findViewById(R.id.settings);
        this.search.setVisibility(View.GONE);
        this.heading = (TextView) view.findViewById(R.id.heading);
        this.heading.setText(NOTIFICATIONS);

        this.menu.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                final int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60,
                        NotificationsFragment.this.getResources().getDisplayMetrics());
                SlideoutActivity.prepare(NotificationsFragment.this.getActivity(), R.id.notificationsView, width);
                NotificationsFragment.this.startActivity(new Intent(NotificationsFragment.this.getActivity(),
                        MenuActivity.class));
                NotificationsFragment.this.getActivity().overridePendingTransition(0, 0);
            }
        });
        this.pullRefreshScrollView.setOnRefreshListener(new OnRefreshListener<ScrollView>() {

            @Override
            public void onRefresh(final PullToRefreshBase<ScrollView> refreshView) {

                NotificationsFragment.this.pullToRefresh = true;
                final Intent intent = new Intent(NOTIFICATION_VISITED);
                NotificationsFragment.this.getActivity().sendBroadcast(intent);
                final NotificationAsync req = new NotificationAsync(Config.getUserId(), false, false, true);
                req.execute();

            }
        });
    }

    private void registerBroadcastReceiver() {

        final IntentFilter messageNotificationAndSubscriptionFilter = new IntentFilter();
        messageNotificationAndSubscriptionFilter.addAction(NOTIFICATION);
        this.getActivity().getApplicationContext()
                .registerReceiver(this.VideoUploadNotificationReciver, messageNotificationAndSubscriptionFilter);
    }

    private class getResponseFromCacheAsync extends AsyncTask<Void, Void, Void> {

        private static final String EMPTY = "";
        private ProgressDialog pro;
        private Object returnObject;
        private boolean running = true;

        @Override
        protected Void doInBackground(final Void... params) {

            while (this.running) {
                try {
                    this.returnObject = Backend.getAllNotificationsFromCache();
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }
                this.running = false;
            }
            return null;
        }

        @Override
        protected void onCancelled() {

            if (this.pro != null) {
                this.pro.dismiss();
            }
            this.running = false;
        }

        @Override
        protected void onPostExecute(final Void result) {

            super.onPostExecute(result);
            if (this.returnObject == null) {
                if (this.pro != null) {
                    this.pro.dismiss();
                }
                final NotificationAsync req = new NotificationAsync(Config.getUserId(), true, true, false);
                req.execute();
            } else {
                NotificationsFragment.this.loadResponse(this.returnObject);
                if (this.pro != null) {
                    this.pro.dismiss();
                }
                final NotificationAsync req = new NotificationAsync(Config.getUserId(), false, false, true);
                req.execute();
            }
            NotificationsFragment.this.pullRefreshScrollView.onRefreshComplete();
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            this.pro = ProgressDialog.show(NotificationsFragment.this.context, EMPTY, EMPTY, true);
            this.pro.setContentView(((LayoutInflater) NotificationsFragment.this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.progress_bar, null, false));
            this.pro.setCancelable(true);
            this.pro.setCanceledOnTouchOutside(false);

            this.pro.show();

        }
    }

    private class NotificationAsync extends AsyncTask<Void, Void, Void> {

        private static final String EMPTY = "";
        private final boolean firstTime;
        private final boolean pullTorefresh;
        private ProgressDialog progressDialog;
        private Object response;
        private volatile boolean running = true;
        private final boolean showProgress;
        private boolean status;
        private final String userId;

        public NotificationAsync(final String userId, final boolean showProgress, final boolean firstTime,
                final boolean pullToRefresh) {

            this.userId = userId;
            this.showProgress = showProgress;
            this.firstTime = firstTime;
            this.pullTorefresh = pullToRefresh;
        }

        @Override
        protected Void doInBackground(final Void... params) {

            while (this.running) {
                try {
                    this.response = Backend.getAllNotifications(NotificationsFragment.this.context, this.userId,
                            this.firstTime, this.pullTorefresh);
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }
                this.status = true;
                this.running = false;
            }
            return null;
        }

        @Override
        protected void onCancelled() {

            if (this.progressDialog != null) {
                this.progressDialog.dismiss();
            }
            this.running = false;
        }

        @Override
        protected void onPostExecute(final Void result) {

            super.onPostExecute(result);
            if ((this.progressDialog != null) && this.showProgress) {
                this.progressDialog.dismiss();
            }
            NotificationsFragment.this.pullRefreshScrollView.onRefreshComplete();
            if (this.status) {
                if (this.response == null) {
                    NotificationsFragment.this.errorMessageTextView.setText(R.string.no_connectivity_text);
                    NotificationsFragment.this.errorMessageTextView.setVisibility(View.VISIBLE);

                } else {
                    NotificationsFragment.this.loadResponse(this.response);
                }
            }

        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            if (this.showProgress) {
                this.progressDialog = ProgressDialog.show(NotificationsFragment.this.context, EMPTY, EMPTY, true);
                this.progressDialog
                        .setContentView(((LayoutInflater) NotificationsFragment.this.context
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.progress_bar,
                                null, false));
                this.progressDialog.setCancelable(true);
                this.progressDialog.setCanceledOnTouchOutside(false);

                this.progressDialog.show();
            }

        }
    }

    private class removeNotificationAsync extends AsyncTask<Void, Void, Void> {

        private final Notification notification;

        public removeNotificationAsync(final Notification notification) {

            this.notification = notification;
        }

        @Override
        protected Void doInBackground(final Void... params) {

            try {
                Backend.removeNotification(NotificationsFragment.this.context, Config.getUserId(), this.notification);
            } catch (final JSONException exception) {
                LOG.e(exception);
            }
            return null;
        }

    }
}
