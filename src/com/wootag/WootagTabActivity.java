/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.wootag;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore.MediaColumns;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wootag.fragments.BaseFragment;
import com.wootag.fragments.BrowseFragment;
import com.wootag.fragments.NewMyPageFragment;
import com.wootag.fragments.NotificationsFragment;
import com.wootag.fragments.VideoFeedsFragment;
import com.wootag.model.Backend;
import com.wootag.ui.CustomDialog;
import com.wootag.util.Config;
import com.wootag.util.MainManager;
import com.wootag.video.trimmer.view.VideoActivity;
import com.wootag.video.trimmer.view.ViewVideo;

public class WootagTabActivity extends Activity {

    private static final String CONTENT = "content";
    private static final String FROM = "from";
    private static final String VIDEO_FILE_NAME = "videofilename";
    private static final String ITEM_PICKED_IS_NOT_A_VIDEO = "The item you picked is not a video. Please pick a video.";
    private static final String VIDEO_ = "video/";
    private static final String _3GP = "3gp";
    private static final String MP4 = "mp4";
    private static final String VIDEO_FORMAT_WAS_NOT_SUPPORTED = "This video format was not supported by this app.Please try mp4 or 3gp.";
    private static final String FILE = "file";
    private static final String COULD_NOT_GET_THE_LOCAL_MEDIA_PATH = "Could not get the local media path. Please pick another video, or use another video source.";
    private static final String MYPAGE = "Mypage";
    private static final String NOTIFICATIONS = "Notifications";
    private static final String EMPTY = "";
    private static final String BROWSE = "Browse";
    private static final String HOME = "Home";
    private static final String USER = "user";
    private static final String USERID = "userid";
    private static final int ACTIVITY_CHOOSE_FILE = 1;

    protected static final Logger LOG = LoggerManager.getLogger();

    public static WootagTabActivity wootagTabActivity;
    public static int var = 1;

    protected ImageView homeNotificationsIcon;
    protected ImageView notificationIcon;
    protected LinearLayout browseTab;
    protected LinearLayout feedTab;
    protected LinearLayout myPageTab;
    protected LinearLayout notificationTab;
    protected Map<String, Stack<Fragment>> tabStack;
    protected String currentTab;
    private String notificationGenerated;
    private TabHost tabHost;
    private View view;
    private WootagTabActivity context;
    private int index;
    private int leftMarginForNotificationView;

    private final BroadcastReceiver VideoUploadNotificationReciver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {

            final String action = intent.getAction();
            if (action != null) {
                if (Constant.NOTIFICATION.equalsIgnoreCase(action)) {
                    WootagTabActivity.this.notificationIcon.setVisibility(View.VISIBLE);

                } else if (Constant.FEED_NOTIFICATION.equalsIgnoreCase(action)) {
                    WootagTabActivity.this.homeNotificationsIcon.setVisibility(View.VISIBLE);

                } else if (Constant.NOTIFICATION_VISITED.equalsIgnoreCase(action)) {
                    WootagTabActivity.this.notificationIcon.setVisibility(View.GONE);

                } else if (Constant.FEED_NOTIFICATION_VISITED.equalsIgnoreCase(action)) {
                    WootagTabActivity.this.homeNotificationsIcon.setVisibility(View.GONE);

                } else if (Constant.VIDEO_UPLOADED.equalsIgnoreCase(action)) {
                    new MyPageAsyncReq(WootagTabActivity.this).execute();
                }
            }
        }
    };

    /** Defining Tab Change Listener event. This is invoked when tab is changed */
    TabHost.OnTabChangeListener listener = new TabHost.OnTabChangeListener() {

        @Override
        public void onTabChanged(final String tabId) {

            WootagTabActivity.this.currentTab = tabId;
            final FragmentManager fragmentManager = WootagTabActivity.this.getFragmentManager();

            final NewMyPageFragment mypageFragment = (NewMyPageFragment) fragmentManager
                    .findFragmentByTag(Constant.MYPAGE);
            final VideoFeedsFragment videoFeedFragment = (VideoFeedsFragment) fragmentManager
                    .findFragmentByTag(Constant.HOME);
            final BrowseFragment browseFragment = (BrowseFragment) fragmentManager.findFragmentByTag(Constant.BROWSE);
            final NotificationsFragment notificationsFragment = (NotificationsFragment) fragmentManager
                    .findFragmentByTag(Constant.NOTIFICATIONS);
            final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            if (Constant.BROWSE.equalsIgnoreCase(tabId)) {
                Config.setCurrentTabIndex(1);
                if (browseFragment == null) {
                    final Fragment browse = new BrowseFragment();
                    WootagTabActivity.this.tabStack.get(tabId).push(browse);
                    fragmentTransaction.add(R.id.browsTab, browse, Constant.BROWSE);
                    fragmentTransaction.commitAllowingStateLoss();
                }
                WootagTabActivity.this.browseTab.setVisibility(View.VISIBLE);
                WootagTabActivity.this.notificationTab.setVisibility(View.GONE);
                WootagTabActivity.this.myPageTab.setVisibility(View.GONE);
                WootagTabActivity.this.feedTab.setVisibility(View.GONE);

            } else if (Constant.NOTIFICATIONS.equalsIgnoreCase(tabId)) {
                Config.setCurrentTabIndex(3);
                if (notificationsFragment == null) {
                    final Fragment notiifcation = new NotificationsFragment();
                    WootagTabActivity.this.tabStack.get(tabId).push(notiifcation);
                    fragmentTransaction.add(R.id.notificationTab, notiifcation, Constant.NOTIFICATIONS);
                    fragmentTransaction.commitAllowingStateLoss();
                }
                WootagTabActivity.this.browseTab.setVisibility(View.GONE);
                WootagTabActivity.this.notificationTab.setVisibility(View.VISIBLE);
                WootagTabActivity.this.myPageTab.setVisibility(View.GONE);
                WootagTabActivity.this.feedTab.setVisibility(View.GONE);
                if (WootagTabActivity.this.notificationIcon != null) {
                    WootagTabActivity.this.notificationIcon.setVisibility(View.GONE);
                }

            } else if (Constant.MYPAGE.equalsIgnoreCase(tabId)) {
                Config.setCurrentTabIndex(4);
                if (mypageFragment == null) {
                    final Fragment myPage = new NewMyPageFragment();
                    WootagTabActivity.this.tabStack.get(tabId).push(myPage);
                    fragmentTransaction.add(R.id.mypageTab, myPage, Constant.MYPAGE);
                    fragmentTransaction.commitAllowingStateLoss();
                }
                WootagTabActivity.this.browseTab.setVisibility(View.GONE);
                WootagTabActivity.this.notificationTab.setVisibility(View.GONE);
                WootagTabActivity.this.myPageTab.setVisibility(View.VISIBLE);
                WootagTabActivity.this.feedTab.setVisibility(View.GONE);

            } else if (Constant.HOME.equalsIgnoreCase(tabId)) {
                Config.setCurrentTabIndex(0);
                if (videoFeedFragment == null) {
                    final Fragment feed = new VideoFeedsFragment();
                    WootagTabActivity.this.tabStack.get(tabId).push(feed);
                    fragmentTransaction.add(R.id.feedTab, feed, Constant.HOME);
                    fragmentTransaction.commitAllowingStateLoss();
                }
                WootagTabActivity.this.browseTab.setVisibility(View.GONE);
                WootagTabActivity.this.notificationTab.setVisibility(View.GONE);
                WootagTabActivity.this.myPageTab.setVisibility(View.GONE);
                WootagTabActivity.this.feedTab.setVisibility(View.VISIBLE);
                if (WootagTabActivity.this.homeNotificationsIcon != null) {
                    WootagTabActivity.this.homeNotificationsIcon.setVisibility(View.GONE);
                }
            } else if (tabId.equalsIgnoreCase(Constant.RECORD)) {
                try {
                    WootagTabActivity.this.setCurrentTab(Config.getCurrentTabIndex());
                    final Intent recordIntent = new Intent(WootagTabActivity.this, VideoActivity.class);
                    WootagTabActivity.this.startActivity(recordIntent);
                } catch (final ActivityNotFoundException e) {
                    LOG.i("exception " + e.toString());
                }
            }

        }
    };

    public int getCurrentTab() {

        return Config.getCurrentTabIndex();
    }

    public void initializeTabs() {

        TabHost.TabSpec spec = this.tabHost.newTabSpec(Constant.HOME);
        this.tabHost.setCurrentTab(-3);
        spec.setContent(new TabHost.TabContentFactory() {

            @Override
            public View createTabContent(final String tag) {

                return WootagTabActivity.this.findViewById(R.id.feedTab);
            }
        });

        spec.setIndicator(this.createTabView(R.drawable.tab_home_selector, HOME));
        this.tabHost.addTab(spec);

        spec = this.tabHost.newTabSpec(Constant.BROWSE);
        spec.setContent(new TabHost.TabContentFactory() {

            @Override
            public View createTabContent(final String tag) {

                return WootagTabActivity.this.findViewById(R.id.browsTab);
            }
        });
        spec.setIndicator(this.createTabView(R.drawable.tab_browse_selector, BROWSE));
        this.tabHost.addTab(spec);

        // business
        spec = this.tabHost.newTabSpec(Constant.RECORD);
        spec.setContent(new TabHost.TabContentFactory() {

            @Override
            public View createTabContent(final String tag) {

                return WootagTabActivity.this.findViewById(R.id.recordTab);
            }
        });
        spec.setIndicator(this.createTabView(R.drawable.tab_record_selector, EMPTY));
        this.tabHost.addTab(spec);

        // kabulll
        spec = this.tabHost.newTabSpec(Constant.NOTIFICATIONS);
        spec.setContent(new TabHost.TabContentFactory() {

            @Override
            public View createTabContent(final String tag) {

                return WootagTabActivity.this.findViewById(R.id.notificationTab);
            }
        });
        spec.setIndicator(this.createTabView(R.drawable.tab_notifications_selector, NOTIFICATIONS));
        this.tabHost.addTab(spec);

        // messages
        spec = this.tabHost.newTabSpec(Constant.MYPAGE);
        spec.setContent(new TabHost.TabContentFactory() {

            @Override
            public View createTabContent(final String tag) {

                return WootagTabActivity.this.findViewById(R.id.mypageTab);
            }
        });
        spec.setIndicator(this.createTabView(R.drawable.tab_my_page_selector, MYPAGE));
        this.tabHost.addTab(spec);

    }

    @Override
    public void onBackPressed() {

        this.removeFromBackStack();

    }

    public void popFragments() {

        final Fragment fragment = this.tabStack.get(this.currentTab).elementAt(
                this.tabStack.get(this.currentTab).size() - 1);
        final Fragment fragmentOne = this.tabStack.get(this.currentTab).elementAt(
                this.tabStack.get(this.currentTab).size() - 2);
        this.tabStack.get(this.currentTab).pop();

        final FragmentManager manager = this.getFragmentManager();
        final FragmentTransaction fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.detach(fragment);
        fragmentTransaction.remove(fragment);
        fragmentTransaction.show(fragmentOne);
        fragmentTransaction.commit();

    }

    public void pushFragments(final int tabId, final Fragment showFragment, final String tag,
            final Fragment hideFragment, final String backStackTag) {

        this.tabStack.get(backStackTag).push(showFragment);
        final FragmentManager fragmentManager = this.getFragmentManager();
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(tabId, showFragment, tag);
        fragmentTransaction.hide(hideFragment);
        fragmentTransaction.show(showFragment);
        fragmentTransaction.addToBackStack(backStackTag);
        fragmentTransaction.commitAllowingStateLoss();

    }

    public void removeFromBackStack() {

        if ((!this.tabStack.get(this.currentTab).isEmpty())
                && !(((BaseFragment) this.tabStack.get(this.currentTab).lastElement()).onBackPressed())) {
            if (this.tabStack.get(this.currentTab).size() == 1) {
                final FragmentManager manager = this.getFragmentManager();
                final FragmentManager.BackStackEntry first = manager.getBackStackEntryAt(0);
                manager.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);

                super.onBackPressed();
            } else {
                this.popFragments();
            }
        }
    }

    /* Might be useful if we want to switch tab programmatically, from inside any of the fragment. */
    public void setCurrentTab(final int val) {

        this.tabHost.setCurrentTab(val);
    }

    private View createTabView(final int id, final String text) {

        this.view = LayoutInflater.from(this).inflate(R.layout.tabs_icon, null);
        final ImageView imageView = (ImageView) this.view.findViewById(R.id.tab_icon);
        final ImageView notificationView = (ImageView) this.view.findViewById(R.id.tab_notification_dot);

        this.browseTab = (LinearLayout) this.findViewById(R.id.browsTab);
        this.myPageTab = (LinearLayout) this.findViewById(R.id.mypageTab);
        this.notificationTab = (LinearLayout) this.findViewById(R.id.notificationTab);
        this.feedTab = (LinearLayout) this.findViewById(R.id.feedTab);
        imageView.setImageDrawable(this.getResources().getDrawable(id));
        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) notificationView.getLayoutParams();
        params.setMargins(this.leftMarginForNotificationView, 10, 0, 0);
        notificationView.setLayoutParams(params);
        if (text.equalsIgnoreCase(HOME)) {
            notificationView.setId(R.id.home_notification_id);
            notificationView.setVisibility(View.GONE);
            this.homeNotificationsIcon = (ImageView) this.view.findViewById(R.id.home_notification_id);
        } else if (text.equalsIgnoreCase(NOTIFICATIONS)) {
            notificationView.setId(R.id.notification_id);
            notificationView.setVisibility(View.GONE);
            this.notificationIcon = (ImageView) this.view.findViewById(R.id.notification_id);
        } else {
            notificationView.setVisibility(View.GONE);
        }

        return this.view;
    }

    private void registerBroadcastReceiver() {

        final IntentFilter messageNotificationAndSubscriptionFilter = new IntentFilter();
        messageNotificationAndSubscriptionFilter.addAction(Constant.NOTIFICATION);
        messageNotificationAndSubscriptionFilter.addAction(Constant.FEED_NOTIFICATION);
        messageNotificationAndSubscriptionFilter.addAction(Constant.NOTIFICATION_VISITED);
        messageNotificationAndSubscriptionFilter.addAction(Constant.FEED_NOTIFICATION_VISITED);
        messageNotificationAndSubscriptionFilter.addAction(Constant.VIDEO_UPLOADED);
        this.getApplicationContext().registerReceiver(this.VideoUploadNotificationReciver,
                messageNotificationAndSubscriptionFilter);
    }

    private void showOptionDialogs() {

        final View view = LayoutInflater.from(this.context).inflate(R.layout.record_options, null);
        final CustomDialog alertDialog = new CustomDialog(this.context, R.style.CustomStyle);
        alertDialog.setContentView(view);
        final WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        layoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;

        alertDialog.getWindow().setAttributes(layoutParams);
        alertDialog.show();
        alertDialog.setCanceledOnTouchOutside(false);

        final RelativeLayout record = (RelativeLayout) view.findViewById(R.id.recordVideo);
        final RelativeLayout chooseFromLibrary = (RelativeLayout) view.findViewById(R.id.chooseFromLibrary);
        final RelativeLayout cancel = (RelativeLayout) view.findViewById(R.id.cancelUploadvideo);

        cancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                alertDialog.dismiss();
            }
        });

        chooseFromLibrary.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                alertDialog.dismiss();

                final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("video/*");
                WootagTabActivity.this.startActivityForResult(Intent.createChooser(intent, "Pick video from"),
                        WootagTabActivity.ACTIVITY_CHOOSE_FILE);
            }
        });

        record.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View view) {

                try {
                    alertDialog.dismiss();
                    final Intent recordIntent = new Intent(WootagTabActivity.this, AndroidVideoCapture.class);
                    WootagTabActivity.this.startActivity(recordIntent);
                } catch (final ActivityNotFoundException e) {
                    LOG.e(e);
                }
            }
        });

        alertDialog.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(final DialogInterface dialog) {

                alertDialog.dismiss();
            }
        });
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {

        if (this.tabStack.get(this.currentTab).size() == 0) {
            return;
        }
        if ((requestCode == WootagTabActivity.ACTIVITY_CHOOSE_FILE) && (resultCode == RESULT_OK)) {
            String mimeType = null;
            String videoPath = null;
            if (resultCode == RESULT_OK) {

                LOG.d("Pick Video Intent data: " + data);
                if ((data != null) && (data.getData() != null)) {
                    final Uri uri = data.getData();

                    if ((uri != null) && CONTENT.equals(uri.getScheme())) {
                        final Cursor cursor = this.getContentResolver().query(uri,
                                new String[] { MediaColumns.DATA, MediaColumns.MIME_TYPE }, null, null, null);

                        if (cursor != null) {
                            final int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
                            final int mime_column_index = cursor.getColumnIndexOrThrow(MediaColumns.MIME_TYPE);
                            cursor.moveToFirst();
                            videoPath = cursor.getString(column_index);
                            mimeType = cursor.getString(mime_column_index);
                            if (TextUtils.isEmpty(videoPath)) {
                                Toast.makeText(this, COULD_NOT_GET_THE_LOCAL_MEDIA_PATH, Toast.LENGTH_LONG).show();
                                cursor.close();
                                return;
                            }
                            cursor.close();
                        }

                    } else if ((uri != null) && FILE.equals(uri.getScheme())) {
                        mimeType = data.getType();
                        videoPath = uri.getPath();
                    }

                    if (((mimeType != null) && (videoPath != null)) && !mimeType.startsWith(VIDEO_)) {
                        Toast.makeText(this, ITEM_PICKED_IS_NOT_A_VIDEO, Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                if (mimeType.contains(MP4) || mimeType.contains(_3GP)) {
                    // System.gc();
                    final Intent intent = new Intent(WootagTabActivity.this, ViewVideo.class);
                    intent.putExtra(VIDEO_FILE_NAME, videoPath);
                    this.startActivity(intent);
                } else {
                    Toast.makeText(WootagTabActivity.this, VIDEO_FORMAT_WAS_NOT_SUPPORTED, Toast.LENGTH_LONG).show();
                }

            }
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_wootag_tab);
        wootagTabActivity = this;
        this.context = this;
        final Bundle bundle = this.getIntent().getExtras();
        if ((bundle != null) && bundle.containsKey(FROM)) {
            this.notificationGenerated = bundle.getString(FROM);
        }
        final Drawable drawable = this.getResources().getDrawable(R.drawable.browseicon_f);
        this.leftMarginForNotificationView = (drawable.getIntrinsicWidth() / 2) + (drawable.getIntrinsicWidth() / 5);

        this.tabStack = new HashMap<String, Stack<Fragment>>();
        this.tabStack.put(Constant.HOME, new Stack<Fragment>());
        this.tabStack.put(Constant.BROWSE, new Stack<Fragment>());
        this.tabStack.put(Constant.RECORD, new Stack<Fragment>());
        this.tabStack.put(Constant.NOTIFICATIONS, new Stack<Fragment>());
        this.tabStack.put(Constant.MYPAGE, new Stack<Fragment>());
        this.registerBroadcastReceiver();

        this.tabHost = (TabHost) this.findViewById(android.R.id.tabhost);
        this.tabHost.setOnTabChangedListener(this.listener);
        this.tabHost.setup();
        this.initializeTabs();

        if (Constant.BACKGROUND.equalsIgnoreCase(this.notificationGenerated)) {
            this.setCurrentTab(3);
        } else {
            this.setCurrentTab(0);
        }
    }

    @Override
    protected void onDestroy() {

        if (this.VideoUploadNotificationReciver != null) {
            this.getApplicationContext().unregisterReceiver(this.VideoUploadNotificationReciver);
        }
        super.onDestroy();
    }

    public class MyPageAsyncReq extends AsyncTask<Void, Void, Void> {

        private final Context context;
        private Object myPageResponse;
        private volatile boolean running = true;

        public MyPageAsyncReq(final Context context) {

            this.context = context;
        }

        public JSONObject getJSONRequest() throws JSONException {

            Config.setUserID(MainManager.getInstance().getUserId());
            final JSONObject request = new JSONObject();
            final JSONObject object = new JSONObject();

            object.put(USERID, Config.getUserId());
            object.put(Constant.DEVICE_TYPE, Constant.DEVICE_MODEL);
            object.put(Constant.RESOLUTION, Config.getDeviceResolutionValue());
            request.put(USER, object);

            return request;

        }

        @Override
        protected Void doInBackground(final Void... params) {

            while (this.running) {
                try {
                    this.myPageResponse = Backend.myPageVideos(this.context, this.getJSONRequest(), false, true);
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }
                this.running = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {

            super.onPostExecute(result);
            WootagTabActivity.this.sendBroadcast(new Intent(Constant.MY_PAGE_REFRESH));

        }
    }
}
