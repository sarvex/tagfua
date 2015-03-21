/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wTagFufragments;

import java.util.List;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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

import com.woTagFuccountSettingActivity;
import com.wooTagFunstant;
import com.wootTagFuuActivity;
import com.wootaTagFugeVideos;
import com.wootagTagFulePickViewActivity;
import com.wootag.TagFuort com.wootag.WTagFuActivity;
import com.wootag.asTagFullowAsyncTask;
import com.wootag.asyTagFuybackAsync;
import com.wootag.dto.TagFuesponse;
import com.wootag.dto.MTagFueos;
import com.wootag.dto.MyTagFuimport com.wootag.dto.MyPTagFu;
import com.wootag.dto.SuggTagFusersDto;
import com.wootag.model.BacTagFuimport com.wootag.pulltorefrTagFullToRefreshBase;
import com.wootag.pulltorefreTagFulToRefreshBase.OnRefreshListener;
import com.wootag.pulltorefresTagFuToRefreshScrollView;
import com.wootag.slideout.SlidTagFutivity;
import com.wootag.ui.CustomDialoTagFuort com.wootag.ui.Image;
imporTagFuwootag.ui.RoundedImageVTagFumport com.wootag.util.Alerts;
impoTagFu.wootag.util.Config;
imporTagFuwootag.util.FollowInterfacTagFuort com.wootag.util.MainManager;

public class NewMyPageFragment extends BaseFragment implements OnClickListener, FollowInterface, OnTouchListener {

    private static final String NO_ID_FOR_THIS_USER = "No Id for this user";

    private static final String UNFOLLOW = "unfollow";

    private static final String PRIVATEPENDINGREQUEST = "privatependingrequest";

    private static final String PVTGROUP = "pvtgroup";

    private static final String COUNT = "count";

    private static final String FROM = "from";

    private static final String ID = "id";

    private static final String X = "x";

    private static final String VIDEOS_PER_PAGE = "videos_per_page";

    private static final String PAGE_NO = "page_no";

    private static final String USER2 = "user";

    private static final String YES = "yes";

    private static final String FOLLOW = "follow";

    private static final String VIDEO = "video";

    public static NewMyPageFragment newMyPageFragment;

    private static final String EMPTY = "";
    private static final int INTRO_SCREEN = 1;
    protected static final Logger LOG = LoggerManager.getLogger();
    private static final String MYPAGE = "mypage";
    protected static LinearLayout myVideosLinearLayout;
    private static final String NO = "no";
    private static final int PAGE_SIZE = 10;
    private static final String PIPE = " | ";
    private static final String TYPE = "type";
    protected static String userId;
    private static final String USERID2 = "userid";
    protected static View view;

    private ImageView accountSetting;
    protected TextView bioText;
    protected ImageView bioviewDot;
    protected Button button;
    protected Context context;
    private LinearLayout discoverPeopleLinearLayout;
    protected TextView errorMessageTextView;
    private LinearLayout followerLL;
    private TextView followersCountTextView;
    private TextView followingCountTextView;
    private LinearLayout followingLL;
    private ImageView foollowImageView;
    private TextView heading;
    protected LayoutInflater inflater;
    private boolean searchRequest;
    private TextView lastUpdateTextView;
    private LinearLayout moreVideosLinearLayout;
    protected PullToRefreshScrollView pullToRefreshScrollView;
    private ScrollView mScrollView;
    private View myPageLL;
    private LinearLayout mypageProfileView;
    private RoundedImageView myProfileImageView;
    private LinearLayout noVideos;
    private String pendingPrivateGroupRequest;
    private TextView privateGroupCountTextView;
    private LinearLayout privateGroupLL;
    protected RelativeLayout profile;
    private ImageView profileBanner;
    protected TextView profileDetailsTextView;
    protected TextView profileNameTextView;
    private RelativeLayout profilePicLayout;
    protected String profilePicUrl;
    private ImageView profileView;
    protected ImageView profileviewdot;
    private Button search, menu;
    protected EditText searchEdit;
    protected RelativeLayout searchLayout;
    protected RelativeLayout settingLayout;
    private LinearLayout sugegestedFriendLL;
    private LinearLayout suggestedUsersLinearLayout;
    private TextView tagCountTextView;
    private SuggestedUsersDto user;
    private TextView videoCountTextView;
    private LinearLayout videosListGalleryLinearLayout;
    private final BroadcastReceiver VideoUploadNotificationReciver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {

            final String action = intent.getAction();
            if (action != null) {
                if (Constant.MY_PAGE_REFRESH.equalsIgnoreCase(action)) {
                    new LoadVideoProfile(0, false, true, false).execute();
                    // new LoadVideoProfile(0,false,false,true).execute();
                } else if (Constant.VIDEO_UPTATED.equalsIgnoreCase(action) && intent.getExtras().containsKey(VIDEO)) {
                    final MyPageDto dto = (MyPageDto) intent.getExtras().getSerializable(VIDEO);
                    final int id = Integer.parseInt(dto.getVideoId());
                    final LinearLayout videoView = (LinearLayout) view.findViewById(id);
                    if (videoView != null) {
                        final MyPageVideos myPageVideos = new MyPageVideos(NewMyPageFragment.this.getActivity(),
                                Constant.MY_PAGE, userId);
                        final int index = myVideosLinearLayout.indexOfChild(videoView);
                        myVideosLinearLayout.removeViewAt(index);
                        myVideosLinearLayout.addView(myPageVideos.getView(NewMyPageFragment.this.inflater, dto), index);
                    }

                }

            }

        }
    };

    /**
     * remove the video view from main layout once user delete the video
     */
    public static void removeView(final int currentVideoId) {

        final LinearLayout layout = (LinearLayout) view.findViewById(currentVideoId);
        myVideosLinearLayout.removeView(layout);
    }

    @Override
    public void follow(final String type) {

        if (FOLLOW.equalsIgnoreCase(type)) {
            this.user.setFollwoing(YES);
            this.foollowImageView.setImageResource(R.drawable.unfollow);
        } else {
            this.user.setFollwoing(NO);
            this.foollowImageView.setImageResource(R.drawable.add1);
        }
    }

    public JSONObject getJSONRequest() throws JSONException {

        final JSONObject request = new JSONObject();
        final JSONObject obj = new JSONObject();
        obj.put(USERID2, Config.getUserId());
        obj.put(Constant.DEVICE_TYPE, Constant.DEVICE_MODEL);
        obj.put(Constant.RESOLUTION, Config.getDeviceResolutionValue());
        request.put(USER2, obj);

        return request;

    }

    public JSONObject getPaginationRequest(final int pageNo) throws JSONException {

        final JSONObject request = new JSONObject();
        final JSONObject obj = new JSONObject();
        obj.put(USERID2, EMPTY + Config.getUserId());
        obj.put(PAGE_NO, EMPTY + pageNo);// videos_per_page
        obj.put(VIDEOS_PER_PAGE, EMPTY + NewMyPageFragment.PAGE_SIZE);
        request.put(USER2, obj);
        return request;
    }

    public String getReloution() {

        final DisplayMetrics displaymetrics = new DisplayMetrics();
        this.getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        final int screenHeight = displaymetrics.heightPixels;
        final int screenWidth = displaymetrics.widthPixels;
        return screenWidth + X + screenHeight;
    }

    public void loadResponse(final Object myPageResponse) {

        if (myPageResponse instanceof ErrorResponse) {
            final ErrorResponse res = (ErrorResponse) myPageResponse;
            this.errorMessageTextView.setVisibility(View.GONE);
            Alerts.showInfoOnly(res.getMessage(), this.context);
        } else if (myPageResponse instanceof MyPage) {
            this.errorMessageTextView.setVisibility(View.GONE);
            if ((myVideosLinearLayout != null) && (myVideosLinearLayout.getChildCount() > 0)) {
                myVideosLinearLayout.removeAllViews();
            }
            if ((this.videosListGalleryLinearLayout != null)
                    && (this.videosListGalleryLinearLayout.getChildCount() > 0)) {
                this.videosListGalleryLinearLayout.removeAllViews();
            }
            if ((this.sugegestedFriendLL != null) && (this.sugegestedFriendLL.getChildCount() > 0)) {
                this.sugegestedFriendLL.removeAllViews();
            }
            final MyPage response = (MyPage) myPageResponse;
            this.loadMyPage(response);
        }
    }

    @Override
    public void onClick(final View childView) {

        switch (childView.getId()) {
        case R.id.privateGropupLL:
            if (userId == null) {
                return;
            }

            final UsersListFragment privateGpFragment = new UsersListFragment();
            final Bundle pvtUsers = new Bundle();
            pvtUsers.putString(TYPE, PVTGROUP);
            pvtUsers.putString(ID, userId);
            pvtUsers.putString(FROM, MYPAGE);
            pvtUsers.putString(Constant.ROOT_FRAGMENT, Constant.MY_PAGE);
            pvtUsers.putString(COUNT, this.privateGroupCountTextView.getText().toString());
            if (this.pendingPrivateGroupRequest != null) {
                pvtUsers.putString(PRIVATEPENDINGREQUEST, this.pendingPrivateGroupRequest);
            }
            privateGpFragment.setArguments(pvtUsers);
            BaseFragment.tabActivity.pushFragments(R.id.mypageTab, privateGpFragment, Constant.PRIVATE_USER,
                    NewMyPageFragment.this, Constant.MYPAGE);
            break;
        case R.id.addImageView:
            this.user = ((SuggestedUsersDto) childView.getTag());
            this.foollowImageView = (ImageView) childView;
            if (NO.equalsIgnoreCase(this.user.getFollwoing())) {
                final FollowAsyncTask task = new FollowAsyncTask(this.user.getId(), Config.getUserId(), FOLLOW,
                        this.getActivity());
                task.delegate = NewMyPageFragment.this;
                task.execute();
            } else {
                final FollowAsyncTask task = new FollowAsyncTask(this.user.getId(), Config.getUserId(), UNFOLLOW,
                        this.getActivity());
                task.delegate = NewMyPageFragment.this;
                task.execute();
            }
            break;

        case R.id.menu:

            final int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, this.getResources()
                    .getDisplayMetrics());
            SlideoutActivity.prepare(this.getActivity(), R.id.mypageView, width);
            this.getActivity().startActivity(new Intent(this.getActivity(), MenuActivity.class));
            this.getActivity().overridePendingTransition(0, 0);

            break;
        case R.id.settings:
            if (this.searchLayout.getVisibility() == View.GONE) {
                final Animation bottomUp = AnimationUtils.loadAnimation(this.getActivity(), R.anim.bottom_up);
                this.searchLayout.startAnimation(bottomUp);
                this.searchLayout.setVisibility(View.VISIBLE);
                this.button.setBackgroundResource(R.drawable.cancelbutton);
            } else {
                this.searchLayout.setVisibility(View.GONE);
                this.button.setBackgroundResource(R.drawable.search1);
            }

            break;

        case R.id.moreVideosLL:
            final MoreVideosFragment fragment = new MoreVideosFragment();
            BaseFragment.tabActivity.pushFragments(R.id.mypageTab, fragment, Constant.MORE_VIDEOS,
                    NewMyPageFragment.this, Constant.MYPAGE);

            break;

        case R.id.suggestedUsersLL:
            final SuggestedUserFragment suggestedUsersFragment = new SuggestedUserFragment();
            final Bundle bundle = new Bundle();
            bundle.putString(Constant.USERID, userId);
            bundle.putString(Constant.SCREEN, Constant.MY_PAGE);
            suggestedUsersFragment.setArguments(bundle);
            BaseFragment.tabActivity.pushFragments(R.id.mypageTab, suggestedUsersFragment, Constant.SUGGESTED_USERS,
                    NewMyPageFragment.this, Constant.MYPAGE);

            break;

        case R.id.discoverPeopleLL:
            // Intent discoverPeopleIntent = new Intent(getActivity(),
            // SuggestedUserActivity.class);
            // discoverPeopleIntent.putExtra(VideoPlayerConstants.videoUserId, userId);
            // startActivity(discoverPeopleIntent);

            final SuggestedUserFragment moreUsersFragment = new SuggestedUserFragment();
            final Bundle b = new Bundle();
            b.putString(Constant.USERID, userId);
            b.putString(Constant.SCREEN, Constant.MY_PAGE);
            moreUsersFragment.setArguments(b);
            BaseFragment.tabActivity.pushFragments(R.id.mypageTab, moreUsersFragment, Constant.SUGGESTED_USERS,
                    NewMyPageFragment.this, Constant.MYPAGE);

            break;

        case R.id.followersLL:
            if (userId == null) {
                return;
            }

            final UsersListFragment usersFragment = new UsersListFragment();
            final Bundle users = new Bundle();
            users.putString(TYPE, "followers");
            users.putString(ID, userId);
            users.putString(Constant.ROOT_FRAGMENT, Constant.MY_PAGE);
            users.putString(FROM, MYPAGE);
            users.putString(COUNT, this.followersCountTextView.getText().toString());
            usersFragment.setArguments(users);
            BaseFragment.tabActivity.pushFragments(R.id.mypageTab, usersFragment, Constant.FOLLOWERS,
                    NewMyPageFragment.this, Constant.MYPAGE);

            // Intent followerIntent = new Intent(getActivity(), UsersListActivity.class);
            // followerIntent.putExtra("type", "followers");
            // followerIntent.putExtra("id", userId);
            // followerIntent.putExtra("count", followersCountTextView.getText().toString());
            // startActivity(followerIntent);
            break;

        case R.id.followingLL:
            if (userId == null) {
                return;
            }
            final UsersListFragment followingsFragment = new UsersListFragment();
            final Bundle followingsusers = new Bundle();
            followingsusers.putString(TYPE, "followings");
            followingsusers.putString(ID, userId);
            followingsusers.putString(Constant.ROOT_FRAGMENT, Constant.MY_PAGE);
            followingsusers.putString(FROM, MYPAGE);
            followingsusers.putString(COUNT, this.followersCountTextView.getText().toString());
            followingsFragment.setArguments(followingsusers);
            BaseFragment.tabActivity.pushFragments(R.id.mypageTab, followingsFragment, Constant.FOLLOWERS,
                    NewMyPageFragment.this, Constant.MYPAGE);

            // Intent followingIntent = new Intent(getActivity(), UsersListActivity.class);
            // followingIntent.putExtra("type", "followings");
            // followingIntent.putExtra("id", userId);
            // followingIntent.putExtra("count", followingCountTextView.getText().toString());
            // startActivity(followingIntent);
            break;
        case R.id.settingImageButton:
            if (userId == null) {
                return;
            }
            final Intent accountSetting = new Intent(this.getActivity(), AccountSettingActivity.class);
            this.startActivity(accountSetting);
            // AccountSettingFragment accountSettingFragment = new AccountSettingFragment();
            // BaseFragment.mActivity.pushFragments(R.id.mypageTab,
            // accountSettingFragment,VideoPlayerConstants.TAB_MYACCOUNT_PAGE, NewMyPageFragment.this,
            // VideoPlayerConstants.TAB_MYPAGE);

            break;
        default:
            break;
        }

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.new_my_page, container, false);
        this.context = this.getActivity();
        Config.setUserID(MainManager.getInstance().getUserId());
        Config.setDeviceResolutionValue(this.getReloution());
        this.myPageLL = view.findViewById(R.id.myPageLL);
        this.mypageProfileView = (LinearLayout) view.findViewById(R.id.mypagedetailsview);
        newMyPageFragment = this;
        this.inflater = inflater;
        this.loadViews();
        this.heading = (TextView) view.findViewById(R.id.heading);
        this.heading.setText("My Page");
        this.registerBroadcastReceiver();
        MainManager.getInstance().setProfileUpdateFlag(0);
        this.pullToRefreshScrollView = (PullToRefreshScrollView) view.findViewById(R.id.scrollView1);
        this.noVideos = (LinearLayout) view.findViewById(R.id.novideos);
        this.pullToRefreshScrollView.setOnRefreshListener(new OnRefreshListener<ScrollView>() {

            @Override
            public void onRefresh(final PullToRefreshBase<ScrollView> refreshView) {

                new LoadVideoProfile(0, false, false, true).execute();

            }
        });

        this.mScrollView = this.pullToRefreshScrollView.getRefreshableView();

        this.menu.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                final int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60,
                        NewMyPageFragment.this.getResources().getDisplayMetrics());
                SlideoutActivity.prepare(NewMyPageFragment.this.getActivity(), R.id.mypageView, width);
                NewMyPageFragment.this.startActivity(new Intent(NewMyPageFragment.this.getActivity(),
                        MenuActivity.class));
                NewMyPageFragment.this.getActivity().overridePendingTransition(0, 0);
            }
        });

        this.searchEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    this.performSearch();
                    return true;
                }
                return false;
            }

            private void performSearch() {

                final String text = NewMyPageFragment.this.searchEdit.getText().toString();
                final InputMethodManager mgr = (InputMethodManager) NewMyPageFragment.this.getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(NewMyPageFragment.this.searchEdit.getWindowToken(), 0);
                if ((text != null) && (text.trim().length() > 0)) {
                    NewMyPageFragment.this.searchLayout.setVisibility(View.GONE);
                    NewMyPageFragment.this.button.setBackgroundResource(R.drawable.search1);

                    final SearchVideosFragment fragment = new SearchVideosFragment();
                    final Bundle bundle = new Bundle();
                    bundle.putString(Constant.ROOT_FRAGMENT, Constant.MY_PAGE);
                    bundle.putString("text", text);
                    bundle.putString(TYPE, MYPAGE);
                    bundle.putString(ID, EMPTY + userId);
                    fragment.setArguments(bundle);
                    BaseFragment.tabActivity.pushFragments(R.id.mypageTab, fragment, Constant.SEARCH_VIDEOS,
                            NewMyPageFragment.this, Constant.MYPAGE);

                } else {
                    Alerts.showInfoOnly("Enter text to search", NewMyPageFragment.this.getActivity());
                }

            }
        });

        // new LoadVideoProfile(0,true,true,false).execute();

        // getMyPageResponse();

        new LoadVideosFromCache().execute();

        return view;
    }

    @Override
    public void onDestroy() {

        if (this.VideoUploadNotificationReciver != null) {
            this.getActivity().unregisterReceiver(this.VideoUploadNotificationReciver);
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

        this.searchLayout.setVisibility(View.GONE);
        this.getActivity().overridePendingTransition(0, 0);
        this.button.setBackgroundResource(R.drawable.search1);
        if (MainManager.getInstance().getUserId() != null) {
            Config.setUserID(MainManager.getInstance().getUserId());
        }
        if (MainManager.getInstance().getProfileUpdateFlag() == 1) {
            new LoadVideoProfile(0, false, false, true).execute();
            MainManager.getInstance().setProfileUpdateFlag(0);
        }

        super.onResume();
    }

    @Override
    public boolean onTouch(final View v, final MotionEvent event) {

        return false;
    }

    private void addBioView() {

        this.bioviewDot.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                NewMyPageFragment.this.settingLayout.setVisibility(View.INVISIBLE);
                NewMyPageFragment.this.profile.setVisibility(View.INVISIBLE);
                NewMyPageFragment.this.profileNameTextView.setVisibility(View.INVISIBLE);
                NewMyPageFragment.this.profileDetailsTextView.setVisibility(View.INVISIBLE);

                NewMyPageFragment.this.bioviewDot.setImageResource(R.drawable.breadcrumb_enable);
                NewMyPageFragment.this.profileviewdot.setImageResource(R.drawable.breadcrumb_disable);
                NewMyPageFragment.this.bioText.setVisibility(View.VISIBLE);
                // profileViewDetails.setVisibility(View.GONE);

            }
        });
        this.profileviewdot.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                NewMyPageFragment.this.settingLayout.setVisibility(View.VISIBLE);
                NewMyPageFragment.this.profile.setVisibility(View.VISIBLE);
                NewMyPageFragment.this.profileNameTextView.setVisibility(View.VISIBLE);
                NewMyPageFragment.this.profileDetailsTextView.setVisibility(View.VISIBLE);

                NewMyPageFragment.this.profileviewdot.setImageResource(R.drawable.breadcrumb_enable);
                NewMyPageFragment.this.bioviewDot.setImageResource(R.drawable.breadcrumb_disable);
                NewMyPageFragment.this.bioText.setVisibility(View.GONE);

            }
        });

    }

    private String getDescription(final MyPage profileDetails) {

        String description = EMPTY;

        if (profileDetails.getProfession() != null) {
            description += profileDetails.getProfession() + PIPE;
        }
        if (profileDetails.getCountry() != null) {
            description += profileDetails.getCountry() + PIPE;
        }
        if (profileDetails.getWebsite() != null) {
            description += profileDetails.getWebsite();
        }

        return description;
    }

    private String getDescription(final SuggestedUsersDto suggestedUsers) {

        String description = EMPTY;

        if (suggestedUsers.getProfession() != null) {
            description += suggestedUsers.getProfession() + PIPE;
        }
        if (suggestedUsers.getCountry() != null) {
            description += suggestedUsers.getCountry() + PIPE;
        }
        if (suggestedUsers.getWebsite() != null) {
            description += suggestedUsers.getWebsite();
        }

        return description;
    }

    private RelativeLayout getMoreVideoItem(final MoreVideos video) {

        RelativeLayout videoView = null;
        videoView = (RelativeLayout) this.inflater.inflate(R.layout.public_video_item, null);
        final ImageView videoImage = (ImageView) videoView.findViewById(R.id.publicvideothumb);
        final ImageButton videoPlayButton = (ImageButton) videoView.findViewById(R.id.playbutton);
        videoPlayButton.setTag(video);
        if (video.getVideothumbPath() != null) {
            Image.displayImage(video.getVideothumbPath(), this.getActivity(), videoImage, 1);
        }

        videoPlayButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                final MoreVideos video = (MoreVideos) v.getTag();
                if (video.getVideoId() != null) {
                    new PlaybackAsync(NewMyPageFragment.this.getActivity(), video.getVideoId()).execute();
                } else {
                    Alerts.showInfoOnly("No video id for this video", NewMyPageFragment.this.getActivity());
                }
            }
        });

        return videoView;

    }

    /**
     * set the suggested users view
     */
    private View getSuggestedFriensView(final SuggestedUsersDto suggestedUsers) {

        final View convertView = this.inflater.inflate(R.layout.suggested_friends, null);
        final ImageView profileImageView = (ImageView) convertView.findViewById(R.id.profileImageView);
        final ImageView addImageView = (ImageView) convertView.findViewById(R.id.addImageView);
        final TextView nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
        final TextView profileDetailsTextView = (TextView) convertView.findViewById(R.id.profileDetailsTextView);
        // final LinearLayout browseuserDetails = (LinearLayout) convertView.findViewById(R.id.browseuserDetails);

        if (!Strings.isNullOrEmpty(suggestedUsers.getPhotoPath())) {

            Image.displayImage(suggestedUsers.getPhotoPath(), this.getActivity(), profileImageView, 0);
        } else {
            profileImageView.setImageResource(R.drawable.member);
        }
        if (suggestedUsers.getName() != null) {
            nameTextView.setText(suggestedUsers.getName());
        } else {
            nameTextView.setText(EMPTY);
        }

        profileDetailsTextView.setText(this.getDescription(suggestedUsers));

        if (NO.equalsIgnoreCase(suggestedUsers.getFollwoing())) {
            addImageView.setImageResource(R.drawable.add1);
        } else {
            addImageView.setImageResource(R.drawable.unfollow);
        }
        addImageView.setTag(suggestedUsers);
        nameTextView.setTag(suggestedUsers);
        profileImageView.setTag(suggestedUsers);
        addImageView.setOnClickListener(this);
        nameTextView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View view) {

                final SuggestedUsersDto peopleDetails = (SuggestedUsersDto) view.getTag();

                final String Id = peopleDetails.getId();
                if (!Config.getUserId().equalsIgnoreCase(Id)) {
                    int id = 0;
                    if (Id != null) {
                        id = Integer.parseInt(Id);
                    }
                    if (id > 0) {
                        final OtherUserFragment fragment = new OtherUserFragment(); // object of next fragment
                        final Bundle bundle = new Bundle();
                        bundle.putString(Constant.ROOT_FRAGMENT, Constant.MY_PAGE);
                        bundle.putString(USERID2, EMPTY + id);
                        fragment.setArguments(bundle);
                        BaseFragment.tabActivity.pushFragments(R.id.mypageTab, fragment, Constant.OTHERS_PAGE,
                                NewMyPageFragment.this, Constant.MYPAGE);

                    } else {
                        Alerts.showInfoOnly(NO_ID_FOR_THIS_USER, NewMyPageFragment.this.context);
                    }
                }
            }
        });
        profileImageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                final SuggestedUsersDto peopleDetails = (SuggestedUsersDto) v.getTag();

                final String Id = peopleDetails.getId();
                if (!Config.getUserId().equalsIgnoreCase(Id)) {
                    int id = 0;
                    if (Id != null) {
                        id = Integer.parseInt(Id);
                    }
                    if (id > 0) {
                        final OtherUserFragment fragment = new OtherUserFragment(); // object of next fragment
                        final Bundle bundle = new Bundle();
                        bundle.putString(Constant.ROOT_FRAGMENT, Constant.MY_PAGE);
                        bundle.putString(USERID2, EMPTY + id);
                        fragment.setArguments(bundle);
                        BaseFragment.tabActivity.pushFragments(R.id.mypageTab, fragment, Constant.OTHERS_PAGE,
                                NewMyPageFragment.this, Constant.MYPAGE);
                    } else {
                        Alerts.showInfoOnly(NO_ID_FOR_THIS_USER, NewMyPageFragment.this.context);
                    }
                }
            }
        });

        return convertView;
    }

    private void loadMyPage(final MyPage mypageDetails) {

        this.pullToRefreshScrollView.onRefreshComplete();
        userId = mypageDetails.getUserid();
        this.setProdileDetails(mypageDetails);
        if ((mypageDetails.getVideoList() != null) && (mypageDetails.getVideoList().size() > 0)) {
            this.noVideos.setVisibility(View.GONE);
            this.setMyVideos(mypageDetails.getVideoList());
        } else {
            this.noVideos.setVisibility(View.VISIBLE);
        }
        this.setMoreVideos(mypageDetails.getMoreVideos());
        this.setSuggestedFriends(mypageDetails.getSuggestedUsers());
        if (mypageDetails != null) {
            this.mypageProfileView.setVisibility(View.VISIBLE);
        }

    }

    private void loadViews() {

        this.errorMessageTextView = (TextView) view.findViewById(R.id.mypageerrormessageView);
        this.searchEdit = (EditText) view.findViewById(R.id.searchEditText);
        this.searchLayout = (RelativeLayout) view.findViewById(R.id.searchRL);
        this.accountSetting = (ImageView) view.findViewById(R.id.settingImageButton);
        this.accountSetting.setOnClickListener(this);
        this.profileBanner = (ImageView) view.findViewById(R.id.profilebanner);
        this.myProfileImageView = (RoundedImageView) view.findViewById(R.id.profileImageView);
        this.myProfileImageView.setImageResource(R.drawable.member);
        this.menu = (Button) view.findViewById(R.id.menu);
        this.lastUpdateTextView = (TextView) view.findViewById(R.id.lastUpdateTextView);
        this.profileNameTextView = (TextView) view.findViewById(R.id.profileNameTextView);
        this.profileDetailsTextView = (TextView) view.findViewById(R.id.profileDetailsTextView);
        this.followersCountTextView = (TextView) view.findViewById(R.id.followersCountTextView);
        this.followingCountTextView = (TextView) view.findViewById(R.id.followingCountTextView);
        this.videoCountTextView = (TextView) view.findViewById(R.id.videosCountTextView);
        this.tagCountTextView = (TextView) view.findViewById(R.id.tagCountTextView);
        this.followerLL = (LinearLayout) view.findViewById(R.id.followersLL);
        this.followerLL.setOnClickListener(this);
        this.followingLL = (LinearLayout) view.findViewById(R.id.followingLL);
        this.followingLL.setOnClickListener(this);
        this.moreVideosLinearLayout = (LinearLayout) view.findViewById(R.id.moreVideosLL);
        this.moreVideosLinearLayout.setOnClickListener(this);
        this.suggestedUsersLinearLayout = (LinearLayout) view.findViewById(R.id.suggestedUsersLL);
        this.suggestedUsersLinearLayout.setOnClickListener(this);
        this.discoverPeopleLinearLayout = (LinearLayout) view.findViewById(R.id.discoverPeopleLL);
        this.discoverPeopleLinearLayout.setOnClickListener(this);
        this.videosListGalleryLinearLayout = (LinearLayout) view.findViewById(R.id.videosListGalleryLL);

        this.privateGroupCountTextView = (TextView) view.findViewById(R.id.privateGroupCountTextView);
        this.privateGroupLL = (LinearLayout) view.findViewById(R.id.privateGropupLL);
        this.privateGroupLL.setOnClickListener(this);
        this.sugegestedFriendLL = (LinearLayout) view.findViewById(R.id.sugegestedFriendLL);
        this.button = (Button) view.findViewById(R.id.settings);
        this.button.setOnClickListener(this);

        this.settingLayout = (RelativeLayout) view.findViewById(R.id.settingLay);
        this.profile = (RelativeLayout) view.findViewById(R.id.userProfilePick);
        this.bioText = (TextView) view.findViewById(R.id.bioText);
        this.profileviewdot = (ImageView) view.findViewById(R.id.profileviewdot);
        this.bioviewDot = (ImageView) view.findViewById(R.id.bioviewDot);
        this.addBioView();

    }

    private void registerBroadcastReceiver() {

        final IntentFilter messageNotificationAndSubscriptionFilter = new IntentFilter();
        // messageNotificationAndSubscriptionFilter
        // .addAction(VideoPlayerConstants.ACTION_VIDEO_UPLOADED);
        messageNotificationAndSubscriptionFilter.addAction(Constant.MY_PAGE_REFRESH);
        messageNotificationAndSubscriptionFilter.addAction(Constant.VIDEO_UPTATED);
        this.getActivity().registerReceiver(this.VideoUploadNotificationReciver,
                messageNotificationAndSubscriptionFilter);
    }

    /**
     * getting the more videos view and add it to main layout
     */
    private void setMoreVideos(final List<MoreVideos> moreVideos) {

        if ((moreVideos != null) && !moreVideos.isEmpty()) {
            this.moreVideosLinearLayout.setVisibility(View.VISIBLE);
            for (int i = 0; i < moreVideos.size(); i++) {
                this.videosListGalleryLinearLayout.addView(this.getMoreVideoItem(moreVideos.get(i)));
            }
        } else {
            this.moreVideosLinearLayout.setVisibility(View.GONE);
        }

    }

    private void setMyVideos(final List<MyPageDto> videoList) {

        final MyPageVideos myPageVideos = new MyPageVideos(this.getActivity(), Constant.MY_PAGE, userId);
        myVideosLinearLayout = (LinearLayout) view.findViewById(R.id.myVideosLL);

        if ((videoList != null) && !videoList.isEmpty()) {

            for (int i = 0; i < videoList.size(); i++) {
                myVideosLinearLayout.addView(myPageVideos.getView(this.inflater, videoList.get(i)));

            }

        }

    }

    /**
     * setting the profile details
     */
    private void setProdileDetails(final MyPage profileDetails) {

        if (profileDetails.getPthotoPath() != null) {
            MainManager.getInstance().setUserPick(profileDetails.getPthotoPath());
            // VideoPlayerApp.getInstance().setUserPhotoURL(profileDetails.getPthotoPath());
            Image.displayImage(profileDetails.getPthotoPath(), this.getActivity(), this.myProfileImageView, 0);
        } else {
            this.myProfileImageView.setImageResource(R.drawable.member);
        }
        if (!Strings.isNullOrEmpty(profileDetails.getUserPickView())) {
            this.profilePicUrl = profileDetails.getUserPickView();
        }
        if (profileDetails.getBannerPath() != null) {
            Image.displayImage(profileDetails.getBannerPath(), this.getActivity(), this.profileBanner, 3);
        } else {
            this.myProfileImageView.setImageResource(R.drawable.defaultpicture);
        }
        if (profileDetails.getPendingPrivateGroupRequests() != null) {
            this.pendingPrivateGroupRequest = profileDetails.getPendingPrivateGroupRequests();
        }

        userId = profileDetails.getUserid();
        Config.setUserID(userId);
        this.lastUpdateTextView.setText("Last Update: " + profileDetails.getLastUpdate());
        if (profileDetails.getUsername() != null) {
            // VideoPlayerApp.getInstance().setUserName(profileDetails.getUsername());
            MainManager.getInstance().setUserName(profileDetails.getUsername());
            this.profileNameTextView.setText(profileDetails.getUsername());
        }
        if (profileDetails.getBio() != null) {
            this.bioText.setText(profileDetails.getBio());
            this.profileviewdot.setVisibility(View.VISIBLE);
            this.bioviewDot.setVisibility(View.VISIBLE);
        } else {
            this.bioText.setText(EMPTY);
            this.profileviewdot.setVisibility(View.GONE);
            this.bioviewDot.setVisibility(View.GONE);
        }

        if (profileDetails.getWebsite() != null) {
            this.profileDetailsTextView.setText(profileDetails.getWebsite());
            this.profileDetailsTextView.setTag(profileDetails.getWebsite());
            this.profileDetailsTextView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View v) {

                    String link = EMPTY + v.getTag().toString();
                    if (link.startsWith("http")) {
                    } else {
                        link = "http://" + link;
                    }
                    final Intent intent = new Intent(NewMyPageFragment.this.context, WebViewActivity.class);
                    intent.putExtra("link", link);
                    intent.putExtra("heading", "Domain");
                    NewMyPageFragment.this.startActivity(intent);
                }
            });
        } else {
            this.profileDetailsTextView.setText(EMPTY);
        }

        this.followersCountTextView.setText((profileDetails.getTotalNoOffollowers() != null ? profileDetails
                .getTotalNoOffollowers() : "0"));
        this.followingCountTextView.setText((profileDetails.getTotalNoOfFollowing() != null ? profileDetails
                .getTotalNoOfFollowing() : "0"));
        if (profileDetails.getTotalNoOfFollowing() != null) {
            Config.setFollowingCount(Integer.parseInt(profileDetails.getTotalNoOfFollowing()));
        }
        if (profileDetails.getTotalNoOfPrivateGroupPeople() != null) {
            Config.setPrivateGroupCount(Integer.parseInt(profileDetails.getTotalNoOfPrivateGroupPeople()));
        }

        if (profileDetails.getTotalNoOfVideos() != null) {
            final int videos = Integer.parseInt(profileDetails.getTotalNoOfVideos());
            if (videos > 1) {
                this.videoCountTextView.setText(EMPTY + profileDetails.getTotalNoOfVideos() + " Videos");
            } else {
                this.videoCountTextView.setText(EMPTY + profileDetails.getTotalNoOfVideos() + " Video");
            }
        }
        if (profileDetails.getTotalNoOfTags() != null) {
            final int videos = Integer.parseInt(profileDetails.getTotalNoOfTags());
            if (videos > 1) {
                this.tagCountTextView.setText(EMPTY + profileDetails.getTotalNoOfTags() + " Tags");
            } else {
                this.tagCountTextView.setText(EMPTY + profileDetails.getTotalNoOfTags() + " Tag");
            }
        }
        this.myProfileImageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (NewMyPageFragment.this.profilePicUrl != null) {
                    NewMyPageFragment.this.showOptionDialogs();

                } else {
                    Alerts.showInfoOnly("Profile pick is not available", NewMyPageFragment.this.getActivity());
                }
            }
        });

        this.privateGroupCountTextView
                .setText((profileDetails.getTotalNoOfPrivateGroupPeople() != null ? profileDetails
                        .getTotalNoOfPrivateGroupPeople() : "0"));
    }

    private void setSuggestedFriends(final List<SuggestedUsersDto> suggestedUsers) {

        if ((suggestedUsers != null) && !suggestedUsers.isEmpty()) {
            this.suggestedUsersLinearLayout.setVisibility(View.VISIBLE);
            this.discoverPeopleLinearLayout.setVisibility(View.VISIBLE);
            for (int i = 0; i < suggestedUsers.size(); i++) {
                this.sugegestedFriendLL.addView(this.getSuggestedFriensView(suggestedUsers.get(i)));
            }
        } else {
            this.suggestedUsersLinearLayout.setVisibility(View.GONE);
            this.discoverPeopleLinearLayout.setVisibility(View.GONE);
        }

    }

    protected void showOptionDialogs() {

        final View view = LayoutInflater.from(this.getActivity()).inflate(R.layout.profile_options, null);
        final CustomDialog alertDialog = new CustomDialog(this.getActivity(), R.style.CustomStyle);
        alertDialog.setContentView(view);
        final WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;

        alertDialog.getWindow().setAttributes(layoutParams);
        alertDialog.show();
        alertDialog.setCanceledOnTouchOutside(false);

        final RelativeLayout edit = (RelativeLayout) view.findViewById(R.id.editProfilePick);
        final RelativeLayout viewPic = (RelativeLayout) view.findViewById(R.id.viewProfilePic);
        final RelativeLayout cancel = (RelativeLayout) view.findViewById(R.id.cancelprofileoptions);

        edit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                alertDialog.dismiss();
                if (userId == null) {
                    return;
                }
                final Intent accountSetting = new Intent(NewMyPageFragment.this.getActivity(),
                        AccountSettingActivity.class);
                NewMyPageFragment.this.startActivity(accountSetting);
            }
        });
        viewPic.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                alertDialog.dismiss();
                final Intent intent = new Intent(NewMyPageFragment.this.getActivity(), ProfilePickViewActivity.class);
                intent.putExtra("url", EMPTY + NewMyPageFragment.this.profilePicUrl);
                NewMyPageFragment.this.startActivity(intent);
            }
        });
        cancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                alertDialog.dismiss();
            }
        });

        alertDialog.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(final DialogInterface dialog) {

                alertDialog.dismiss();
            }
        });

    }

    public class LoadVideoProfile extends AsyncTask<Void, Void, Void> {

        private final boolean firstTime;
        private final boolean pullToRefresh;
        private final int pageNumber;
        private volatile boolean running = true;
        private boolean status;
        boolean progressVisible;
        Object myPageResponse;
        ProgressDialog progressDialog;

        public LoadVideoProfile(final int pageNo, final boolean progressVisible, final boolean firstTime,
                final boolean pullToRefresh) {

            this.pageNumber = pageNo;
            this.progressVisible = progressVisible;
            this.pullToRefresh = pullToRefresh;
            this.firstTime = firstTime;
        }

        @Override
        protected Void doInBackground(final Void... params) {

            try {
                while (this.running) {
                    this.myPageResponse = Backend.myPageVideos(NewMyPageFragment.this.getActivity(),
                            NewMyPageFragment.this.getJSONRequest(), this.firstTime, this.pullToRefresh);
                    this.running = false;
                    this.status = true;
                }
            } catch (final JSONException exception) {
                LOG.e(exception);
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
            if (this.progressVisible) {
                this.progressDialog.dismiss();
            }
            NewMyPageFragment.this.pullToRefreshScrollView.onRefreshComplete();
            if (this.status) {
                if (this.myPageResponse != null) {
                    NewMyPageFragment.this.loadResponse(this.myPageResponse);
                } else {
                    NewMyPageFragment.this.errorMessageTextView.setText(R.string.no_connectivity_text);
                    NewMyPageFragment.this.errorMessageTextView.setVisibility(View.VISIBLE);
                }
            } else {
                NewMyPageFragment.this.errorMessageTextView.setText(R.string.no_connectivity_text);
                NewMyPageFragment.this.errorMessageTextView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            if (this.progressVisible) {
                this.progressDialog = ProgressDialog.show(NewMyPageFragment.this.getActivity(), EMPTY, EMPTY, true);
                final View v = NewMyPageFragment.this.inflater.inflate(R.layout.progress_bar, null, false);
                this.progressDialog.setContentView(v);
                this.progressDialog.setCancelable(true);
                this.progressDialog.setCanceledOnTouchOutside(false);
                this.progressDialog.show();
            }
        }
    }

    private class LoadVideosFromCache extends AsyncTask<Void, Void, Void> {

        private Object returnObject;
        private volatile boolean running = true;
        Object myPageResponse;
        ProgressDialog progressDialog;

        @Override
        protected Void doInBackground(final Void... params) {

            while (this.running) {
                try {
                    this.returnObject = Backend.mypageVideosFromCache();
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }
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
            if (this.returnObject == null) {
                if (this.progressDialog != null) {
                    this.progressDialog.dismiss();
                }
                new LoadVideoProfile(0, true, true, false).execute();
            } else {
                NewMyPageFragment.this.loadResponse(this.returnObject);
                if (this.progressDialog != null) {
                    this.progressDialog.dismiss();
                }
                // new LoadVideoProfile(0, false, false, true).execute();
            }
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            this.progressDialog = ProgressDialog.show(NewMyPageFragment.this.getActivity(), EMPTY, EMPTY, true);
            final View v = NewMyPageFragment.this.inflater.inflate(R.layout.progress_bar, null, false);
            this.progressDialog.setContentView(v);
            this.progressDialog.setCancelable(true);
            this.progressDialog.setCanceledOnTouchOutside(false);
            this.progressDialog.show();
        }
    }

}

class OnBioSwipeListener implements OnTouchListener {

    private static final Logger LOG = LoggerManager.getLogger();

    private final GestureDetector gestureDetector;

    public OnBioSwipeListener(final Context ctx) {

        this.gestureDetector = new GestureDetector(ctx, new GestureListener());
    }

    public void onSwipeBottom() {

    }

    public void onSwipeLeft() {

    }

    public void onSwipeRight() {

    }

    public void onSwipeTop() {

    }

    @Override
    public boolean onTouch(final View v, final MotionEvent event) {

        return this.gestureDetector.onTouchEvent(event);
    }

    private class GestureListener extends SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 20;// 100
        private static final int SWIPE_VELOCITY_THRESHOLD = 20;

        @Override
        public boolean onDown(final MotionEvent event) {

            return true;
        }

        @Override
        public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX, final float velocityY) {

            final float diffY = e2.getY() - e1.getY();
            final float diffX = e2.getX() - e1.getX();
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if ((Math.abs(diffX) > SWIPE_THRESHOLD) && (Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD)) {
                    if (diffX > 0) {
                        OnBioSwipeListener.this.onSwipeRight();
                    } else {
                        OnBioSwipeListener.this.onSwipeLeft();
                    }
                }
            } else {
                if ((Math.abs(diffY) > SWIPE_THRESHOLD) && (Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD)) {
                    if (diffY > 0) {
                        OnBioSwipeListener.this.onSwipeBottom();
                    } else {
                        OnBioSwipeListener.this.onSwipeTop();
                    }
                }
            }
            return true;
        }
    }
}
