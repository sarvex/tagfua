/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.TagFu;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.base.Strings;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.TagFu.cache.CacheManager;
import com.TagFu.connectivity.VideoDataBase;
import com.TagFu.dto.VideoInfo;
import com.TagFu.fragments.BaseFragment;
import com.TagFu.fragments.BrowseFragment;
import com.TagFu.fragments.FriendFinderFragment;
import com.TagFu.fragments.NewMyPageFragment;
import com.TagFu.fragments.NotificationsFragment;
import com.TagFu.fragments.VideoFeedsFragment;
import com.TagFu.ui.Image;
import com.TagFu.util.Config;
import com.TagFu.util.MainManager;
import com.TagFu.util.Util;

public class MenuFragment extends Fragment {

    private static final String EMPTY = "";

    private static final Logger LOG = LoggerManager.getLogger();

    private LinearLayout feedbackLay;

    private LinearLayout friendFinder;
    private LinearLayout home;
    private LinearLayout logout;
    private TextView pendingVideos;
    private LinearLayout pendingVideosLay;
    private LinearLayout purchaseRequests;
    private LinearLayout setting;
    private LinearLayout userDetails;
    private ImageView userImage;
    private TextView userName;

    public static void callFacebookLogout(final Context context) {

    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setRetainInstance(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.activity_quick_link, container, false);
        this.userName = (TextView) view.findViewById(R.id.userName);
        this.userImage = (ImageView) view.findViewById(R.id.profileImageView);
        if (MainManager.getInstance().getUserName() != null) {
            this.userName.setText(MainManager.getInstance().getUserName());
        }
        if (Strings.isNullOrEmpty(MainManager.getInstance().getUserPick())) {
            Image.displayImage(MainManager.getInstance().getUserPick(), this.getActivity(), this.userImage, 0);
        }

        this.friendFinder = (LinearLayout) view.findViewById(R.id.friendfinder);
        this.home = (LinearLayout) view.findViewById(R.id.home);
        this.userDetails = (LinearLayout) view.findViewById(R.id.userDetails);
        this.setting = (LinearLayout) view.findViewById(R.id.settings);
        this.logout = (LinearLayout) view.findViewById(R.id.logout);
        this.pendingVideosLay = (LinearLayout) view.findViewById(R.id.pendingVideosLay);
        this.feedbackLay = (LinearLayout) view.findViewById(R.id.feedback);
        this.pendingVideos = (TextView) view.findViewById(R.id.pendingvideos);

        final int pendingUploads = this.getPendingUploads();
        if (pendingUploads > 0) {
            this.pendingVideos.setText(pendingUploads + " Pending videos");
            this.pendingVideosLay.setVisibility(View.VISIBLE);
        } else {
            this.pendingVideosLay.setVisibility(View.GONE);
        }
        this.pendingVideosLay.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                ((MenuActivity) MenuFragment.this.getActivity()).slideoutHelper.close();
                final Intent intent = new Intent(MenuFragment.this.getActivity(), UploadingFileQueueActivity.class);
                MenuFragment.this.startActivity(intent);

            }
        });

        this.home.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                ((MenuActivity) MenuFragment.this.getActivity()).slideoutHelper.close();
                BaseFragment.tabActivity.setCurrentTab(0);
            }
        });
        this.feedbackLay.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                ((MenuActivity) MenuFragment.this.getActivity()).slideoutHelper.close();
                final Intent myPageIntent = new Intent(MenuFragment.this.getActivity(), ReportAProblemActivity.class);
                MenuFragment.this.startActivity(myPageIntent);
            }
        });
        this.userDetails.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                ((MenuActivity) MenuFragment.this.getActivity()).slideoutHelper.close();
                BaseFragment.tabActivity.setCurrentTab(4);
            }
        });
        this.setting.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                ((MenuActivity) MenuFragment.this.getActivity()).slideoutHelper.close();
                final Intent accountSetting = new Intent(MenuFragment.this.getActivity(), SettingActivity.class);
                MenuFragment.this.startActivity(accountSetting);
            }
        });
        this.friendFinder.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                ((MenuActivity) MenuFragment.this.getActivity()).slideoutHelper.close();
                // Intent accountSetting = new Intent(getActivity(),FriendFinderActivity.class);
                // startActivity(accountSetting);
                final int index = BaseFragment.tabActivity.getCurrentTab();
                MenuFragment.this.gotToFriendFinderPage(index);
            }
        });
        this.logout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                ((MenuActivity) MenuFragment.this.getActivity()).slideoutHelper.close();
                MainManager.getInstance().setLoginType(0);
                MainManager.getInstance().setUserId(EMPTY);
                MainManager.getInstance().setUserName(EMPTY);
                MainManager.getInstance().setUserPick(EMPTY);
                CacheManager.getInstance(VideoPlayerApp.getAppContext());
                final File file = new File(CacheManager.cacheDir);
                if (file.exists()) {
                    Util.deleteRecursive(file);
                }

                MenuFragment.this.logout();
                Image.clearImageFromCache();

                final Intent intent = new Intent(MenuFragment.this.getActivity(), SignInFragment.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                TagFuTabActivity.TagFuTabActivity.finish();
                MenuFragment.this.startActivity(intent);
            }

        });

        return view;
    }

    private int getPendingUploads() {

        int pendingUploads = 0;
        final ArrayList<VideoInfo> pendingVideos = new ArrayList<VideoInfo>();
        final List<VideoInfo> videoInfos = VideoDataBase.getInstance(this.getActivity().getApplicationContext())
                .getAllNonUploadList();
        if ((videoInfos != null) && !videoInfos.isEmpty()) {
            for (int i = 0; i < videoInfos.size(); i++) {
                final VideoInfo video = videoInfos.get(i);
                if (MainManager.getInstance().getUserId() != null) {
                    final int userId = Integer.parseInt(MainManager.getInstance().getUserId());
                    if ((video.getUserid() == userId) && (video.getUploadStatus() != 1)) {
                        pendingVideos.add(video);
                    }
                }
            }
            pendingUploads = pendingVideos.size();

        }
        return pendingUploads;
    }

    void gotToFriendFinderPage(final int index) {

        final FriendFinderFragment fragment = new FriendFinderFragment(); // object of next fragment
        final Bundle bundle = new Bundle();
        switch (index) {
        case 0:
            bundle.putString(Constant.ROOT_FRAGMENT, Constant.VIDEO_FEEDS);
            fragment.setArguments(bundle);
            BaseFragment.tabActivity.pushFragments(R.id.feedTab, fragment, Constant.FRIEND_FINDER,
                    VideoFeedsFragment.videoFeeds, Constant.HOME);
            break;
        case 1:
            bundle.putString(Constant.ROOT_FRAGMENT, Constant.BROWSE_PAGE);
            fragment.setArguments(bundle);
            BaseFragment.tabActivity.pushFragments(R.id.browsTab, fragment, Constant.FRIEND_FINDER,
                    BrowseFragment.browseFragment, Constant.BROWSE);
            break;
        case 3:
            bundle.putString(Constant.ROOT_FRAGMENT, Constant.NOTIFICATIONS_PAGE);
            fragment.setArguments(bundle);
            BaseFragment.tabActivity.pushFragments(R.id.notificationTab, fragment, Constant.FRIEND_FINDER,
                    NotificationsFragment.notificationFragment, Constant.NOTIFICATIONS);
            break;
        case 4:
            bundle.putString(Constant.ROOT_FRAGMENT, Constant.MY_PAGE);
            fragment.setArguments(bundle);
            BaseFragment.tabActivity.pushFragments(R.id.mypageTab, fragment, Constant.FRIEND_FINDER,
                    NewMyPageFragment.newMyPageFragment, Constant.MYPAGE);
            break;
        default:
            break;
        }
    }

    void logout() {

        final Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {

                Config.setFacebookAccessToken(EMPTY);
                callFacebookLogout(VideoPlayerApp.getAppContext());
                CookieSyncManager.createInstance(MenuFragment.this.getActivity());
                final CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.removeSessionCookie();
                MainManager.getInstance().setTwitterOAuthtoken(null);
                MainManager.getInstance().setTwitterSecretKey(null);
                MainManager.getInstance().setTwitterAuthorization(0);
            }
        });
        thread.start();

    }

}
