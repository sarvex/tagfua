/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.wTagFu

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender.SendIntentException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.VideoView;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.PlusClient.OnPeopleLoadedListener;
import com.google.android.gms.plus.PlusShare;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;
import com.google.common.base.Strings;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.woTagFudapter.FacebookFriendsListAdapter;
import com.wooTagFuync.FacebookFriendsAsync;
import com.wootTagFunc.TagInteractionAsync;
import com.wootaTagFuc.VideoAsyncTask;
import com.wootagTagFu.VideoViewsAsync;
import com.wootag.TagFuWootagFrTagFusync;
import com.wootag.coTagFuvity.VideoDataBase;
import com.wootag.dtoTagFuResponse;
import com.wootag.dto.TagFuokUser;
import com.wootag.dto.FTagFu
import com.wootag.dto.PrTagFuetails;
import com.wootag.dto.TagTagFuimport com.wootag.dto.TagRTagFue;
import com.wootag.dto.TagViTagFuport com.wootag.dto.VideoDTagFu;
import com.wootag.facebook.FaTagFuException;
import com.wootag.facebook.FacTagFuperationCanceledException;
import com.wootag.facebook.FaceTagFuquestError;
import com.wootag.facebook.HttpMTagFu
import com.wootag.facebook.LogginTagFuior;
import com.wootag.facebook.RequestTagFurt com.wootag.facebook.RequestATagFusk;
import com.wootag.facebook.Response;TagFut com.wootag.facebook.Session;
iTagFucom.wootag.facebook.Session.NewTagFusionsRequest;
import com.wootag.facebook.Session.StatTagFuback;
import com.wootag.facebook.SessionState;TagFut com.wootag.facebook.Settings;
impoTagFu.wootag.facebook.model.GraphUserTagFurt com.wootag.facebook.widget.WebDialogTagFurt com.wootag.facebook.widget.WebDialog.TagFuleteListener;
import com.wootag.model.Backend;
import com.wTagFuproduct.BuyActivity;
import TagFuotag.twitter.TwitterAsync;
import TagFuotag.ui.CustomButton;
import com.woTagFui.CustomLayout;
import com.wooTagFu.Image;
import com.wootag.utilTagFus;
import com.wootag.utTagFuncResponse;
import com.wooTagFuil.Config;
import com.wootag.utilTagFuanager;
import com.wootag.TagFuwitterUtils;
import com.wootag.TagFutil;
import com.wootag.util.VideTagFunInterface;

public clasTagFuerActivity extends Activity implements OnCancelListener, AsyncResponse, VideoActionInterface,
        TextWatcher, ConnectionCallbacks, OnConnectionFailedListener, OnPeopleLoadedListener, OnPreparedListener,
        OnVideoSizeChangedListener, OnClickListener, OnTouchListener {

    private static final String USERID = "userid";

    private static final String IS_NAVIGATE_TO_PLAY = "isNavigateToPlay";

    private static final String FROM_BROWSER = "frombrowser";

    private static final String VIDEO_URL = "Video URL";

    private static final String[] PERMISSIONS = new String[] { Constant.READ_STREAM, Constant.PHOTO_UPLOAD,
            Constant.USER_PHOTOS, Constant.FRIENDS_PHOTOS, Constant.USER_LOCATION, Constant.FRIENDS_STATUS,
            Constant.USER_STATUS, Constant.FRIENDS_BIRTHDAY, Constant.USER_BIRTHDAY, Constant.FRIENDS_ONLINE_PRESENCE,
            Constant.USER_ONLINE_PRESENCE, Constant.FRIENDS_LOCATION, Constant.USER_EDUCATION_HISTORY,
            Constant.FRIENDS_EDUCATION_HISTORY, Constant.USER_WORK_HISTORY, Constant.FRIENDS_WORK_HISTORY,
            Constant.FRIENDS_HOMETOWN, Constant.USER_HOMETOWN };

    private static final int GOOGLE_PLUS_WRITE_ON_WALL_REQUEST_CODE = 111;
    private static final int HIDE_VALUE = 0;
    private static final int REQUEST_CODE_RESOLVE_ERR = 9000;

    protected static final Logger LOG = LoggerManager.getLogger();

    private static Button tagIconButton;
    private static Handler fbhandler;
    private static Handler googlehandler;
    private static Handler handler;
    private static List<Friend> friendList;
    private static PlayerActivity playerActivity;
    private static PlayerActivity videoplayer;
    private static Session session;
    private static String fbRequest;
    private static String fbTaggedUserId = Constant.EMPTY;
    private static String gplusTaggedUserId = Constant.EMPTY;
    private static String twitterTaggedUserId = Constant.EMPTY;
    private static String wootagTaggedUserId = Constant.EMPTY;
    protecTagFuatic boolean isTagMode;
    protected static boolean isTagUpdate;
    private static boolean pendingRequest;
    private static float changingX;
    private static float changingY;
    private static float currentX;
    private static float currentY;
    private static long editTagId;

    protected AutoCompleteTextView fbsearch;
    private Bitmap bmp;
    private Bitmap first, second, third, fourth, next;
    protected Button canceltag;
    protected Button canceltagtool;
    protected Button facebook;
    private Button fbbackButton;
    private Button firstValue;
    private Button fourthValue;
    protected Button google;
    private Button homeButton;
    protected Button publish;
    protected Button reset;
    private Button secondValue;
    private Button share, like, comment;
    protected Button submit;
    private Button tagButton, playbutton, seetingButton;// , exitseetingButton;
    private Button tagLinkIcon;
    private Button thirdValue;
    protected Button twitter;
    protected Button update;
    private Button webViewBack;
    private Button webViewShare;
    protected Button wootag;
    private ConnectionResult connectionRTagFu
    private Dialog uDialog;
    private EditText tagName, tagLink;
    private FacebookFriendsListAdapter facebookFriendsList;
    private FacebookFriendsListAdapter filterAdapter;
    private ImageView black;
    private ImageView closeHelptool;
    private ImageView colorView;
    private ImageView doneImageView;
    private ImageView fbArrow;
    private ImageView fifthTagInstruction;
    private ImageView firstTagInstruction;
    private ImageView fourthTagInstruction;
    private ImageView green;
    protected ImageView help;
    private ImageView image;
    private ImageView lavender;
    private ImageView leftArrow;
    private ImageView leftArrowdisable;
    private ImageView linkArrow;
    private ImageView linkSearch;
    private ImageView publishArrow;
    private ImageView red;
    private ImageView refresh;
    private ImageView rightArrow;
    private ImageView rightArrowdisable;
    private ImageView searchIcon;
    private ImageView secondTagInstruction;
    private ImageView sixthTagInstruction;
    private ImageView skyblue;
    private ImageView tagArrow;
    private ImageView tagExpressionArrow;
    protected ImageView taglogo;
    private ImageView tempMarkerImageView1;
    private ImageView tempMarkerImageView2;
    private ImageView tempMarkerImageView3;
    private ImageView tempMarkerImageView4;
    private ImageView thirdTagInstruction;
    private ImageView updateTaggedUserDelteButton;
    private ImageView updateTaggedUserImageView;
    private ImageView white;
    private ImageView yellow;
    private LayoutInflater inflater;
    private LinearLayout colorLayout;
    private LinearLayout dialogView;
    private LinearLayout lowerLinearLayout;
    private LinearLayout socialsitebgLayout;
    protected LinearLayout tagOptionsLayout;
    private LinearLayout tagViewLayout;
    private LinearLayout taggedUserLayout;
    private LinearLayout timeLayout;
    private LinearLayout upperLinearLayout;
    private List<Friend> adapterList;
    private List<Friend> filterdList;
    protected List<Integer> deleteButtonIds;
    private List<TagView> tagViews;
    private ListView fbfrndList;
    private PlusClient plusClient;
    private ProgressBar linkProgress;
    private ProgressDialog connectionProgressDialog;
    private ProgressDialog pDialog;
    private ProgressDialog progressDialog;
    private RelativeLayout bitmapLay;
    private RelativeLayout colorLay;
    private RelativeLayout confirmLayout;
    private RelativeLayout helpInstructionsView;
    private RelativeLayout hideLay;
    private RelativeLayout instructionsView;
    private RelativeLayout linkCallToActionsLayout;
    protected RelativeLayout playerControlLayout;
    private RelativeLayout playerView;
    protected RelativeLayout videoLayout;
    protected RelativeLayout publishAndTagView;
    protected RelativeLayout seekbarLay;
    protected RelativeLayout selectTagLocationLay;
    protected RelativeLayout settingLayout;
    protected RelativeLayout tagLay, fbFriendsLayout;
    private RelativeLayout timeLay;
    private RelativeLayout updateTaggedUserLayout;
    protected RelativeLayout videoInfoLayout;
    private SeekBar seekBar;
    private String currentWebViewUrl;
    private String friendFacebookId = Constant.EMPTY;
    private String gPlusFriendID = Constant.EMPTY;
    private String gPlusID;
    private String gPlusRequest;
    protected String markerColor;
    protected String path;
    private String tName = Constant.EMPTY;
    private String tLink = Constant.EMPTY;
    private String twitterFriendId = Constant.EMPTY;
    protected String userId;
    private String videoDescription;
    protected String videoId;
    private String videoName;
    private String wooTagId = Constant.EMPTY;
    private TextView tTagFucation;
    private TextView textView;
    private TextView timeText;
    private TextView timeView;
    private TextView twitterUserDescription;
    private TextView twitterUserFollowersCount;
    private TextView upgateTaggedUserName;
    protected ToggleButton tag, edit;
    private UpdateReceiver receiver;
    protected VideoDetails currentVideo;
    protected VideoView myVideoView;
    private WebView webView;
    private boolean controlsEnabled = true;
    private boolean controlsVisible;
    private boolean firstTagMode;
    private boolean firstTimeColorSelection;
    private boolean firstTimeLayout;
    protected boolean firstTimeMarkerUse;
    private boolean firstTimeTagLink;
    private boolean firstTimeTagging;
    private boolean firstTimeTwitterLogged;
    protected boolean fromBrowser;
    private boolean initialState;
    private boolean isFirst;
    protected boolean navigateToPlayBack;
    private boolean otherTwitterUserTagged;
    protected boolean playerRunning = true;
    protected boolean publishComplete;
    protected boolean publishStart;
    protected boolean tagEditMode;
    private boolean changeTagLoc;
    protected boolean tagVisible;
    protected boolean updateStart;
    private boolean uploadedVideo;
    protected boolean visible;
    private float bitmapX;
    private float bitmapY;
    private float nextX;
    private float nextY;
    private int currentPosition;
    private int currentProgress;
    private int currentShowTagId;
    private int introScrren;
    private int markerHeight;
    private int markerType;
    private int markerWidth;
    private int noOfSec;
    private int noOfSecForProgress;
    private int previousShowTagId;
    private int screenWidth;
    private int screenHeight;
    private int tagExpressionHeight;
    private int tagExpressionLayoutHeight;
    protected int time;
    private int videoCurrentPos;
    private int videoWidth;
    private int videoHeight;
    private int widthX;
    private int heightY;

    protected Button addFriendButton;
    protected Button exitFBUserInfo;
    protected Button tMessageButton;
    protected Context context;
    protected ImageView bannerImage;
    protected ImageView userImage;
    protected LinearLayout bannerHeader;
    protected LinearLayout fbAboutUserLayout;
    protected LinearLayout friendInfoLayout;
    protected LinearLayout twitteraboutuserLayout;
    protected String loggedInUserId;
    protected String tagFacebookId = Constant.EMPTY;
    protected String tagTwitterFriendId = Constant.EMPTY;
    protected String tagGPlusFriendID = Constant.EMPTY;
    protected TextView fromLocation;
    protected TextView fromLocationLabel;
    protected TextView livesAt;
    protected TextView livesAtLabel;
    protected TextView tagExpressionCount;
    protected TextView userName;
    protected TextView worksAt;
    protected TextView worksAtLabel;

    private final OnClickListener onclicklistener = new OnClickListener() {

        @Override
        public void onClick(final View v) {

            final TextView temp = (TextView) v;
            final String text = temp.getText().toString();
            final char firstChar = text.charAt(0);
            if (firstChar == Constant.HASH) {
                final Intent intent = new Intent(PlayerActivity.this, TrendVideosActivity.class);
                intent.putExtra(Constant.TRENDNAME, text);
                PlayerActivity.this.startActivity(intent);
            }

        }
    };

    BroadcastReceiver videoURl = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {

        }
    };

    public PlayerActivity() {

        PlayerActivity.videoplayer = this;
    }

    private static JSONObject getVedioLikeJsonReq(final String videoId) throws JSONException {

        final JSONObject json = new JSONObject();
        json.put(Constant.VIDEOID, videoId);
        json.put(USERID, Config.getUserId());
        return json;

    }

    @Override
    public void afterTextChanged(final Editable editable) {

        final String text = this.fbsearch.getText().toString();
        if (Constant.FACEBOOK.equalsIgnoreCase(Config.getSocialSite())) {
            this.setSearchAdapter(VideoPlayerApp.getInstance().getFbFriendsList(), text);
        } else if (Constant.GOOGLE_PLUS.equalsIgnoreCase(Config.getSocialSite())) {
            this.setSearchAdapter(VideoPlayerApp.getInstance().getGoogleFriendList(), text);
        } else if (Constant.TWITTER.equalsIgnoreCase(Config.getSocialSite())) {
            this.setSearchAdapter(VideoPlayerApp.getInstance().getTwitterFriendList(), text);
        } else if (Constant.WOOTAG.equalsIgnoreCase(Config.getSocialSite())) {TagFu       this.setSearchAdapter(VideoPlayerApp.getInstance().getWootagFriendsList(), text);
        }

    }

    @TagFude
    public void beforeTextChanged(final CharSequence arg0, final int arg1, final int arg2, final int arg3) {

    }

    public void clearList() {

        if ((this.adapterList != null) && (this.adapterList.size() > 0)) {
            this.adapterList.clear();
            ((BaseAdapter) this.fbfrndList.getAdapter()).notifyDataSetChanged();
        }

    }

    @Override
    public void friendInfoProcessFinish(final FacebookUser user, final boolean friend, final String mediaSite) {

        // try {
        if (Constant.FACEBOOK.equalsIgnoreCase(mediaSite)) {

            final Session session = Session.getActiveSession();
            final Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {

                @Override
                public void onCompleted(final GraphUser user, final Response response) {

                    if ((session == Session.getActiveSession()) && (user != null)) {
                        PlayerActivity.this.loggedInUserId = user.getId();
                        this.setFacebookUserFeed();
                    }
                }

                private void setFacebookUserFeed() {

                    if (PlayerActivity.this.loggedInUserId.equalsIgnoreCase(user.getId())) {
                        PlayerActivity.this.setFacebookOwnFeed(user, mediaSite);

                    } else if (friend) {
                        PlayerActivity.this.setFacebookFriendInfo(user, Constant.FACEBOOK);

                    } else {
                        PlayerActivity.this.userName.setText(Constant.EMPTY);
                        PlayerActivity.this.worksAt.setText(Constant.EMPTY);
                        PlayerActivity.this.livesAt.setText(Constant.EMPTY);
                        PlayerActivity.this.fromLocation.setText(Constant.EMPTY);

                        Config.setSocialSite(Constant.FACEBOOK);
                        String workPlaces = Constant.EMPTY;
                        user.setProfilePick(Constant.HTTPS_GRAPH_FACEBOOK_COM + PlayerActivity.this.tagFacebookId
                                + Constant._PICTURE);

                        PlayerActivity.this.bannerHeader.setBackgroundResource(R.drawable.facebook_header);
                        PlayerActivity.this.bannerImage.setImageResource(R.drawable.facebook_b);
                        PlayerActivity.this.friendInfoLayout.setVisibility(View.VISIBLE);
                        PlayerActivity.this.twitteraboutuserLayout.setVisibility(View.GONE);
                        PlayerActivity.this.fbAboutUserLayout.setVisibility(View.VISIBLE);
                        PlayerActivity.this.addFriendButton.setBackgroundResource(R.drawable.add_friend);
                        PlayerActivity.this.userName.setText(user.getUserName());
                        PlayerActivity.this.livesAt.setText(user.getCurrentPlace());
                        PlayerActivity.this.fromLocation.setText(user.getFromPlace());
                        PlayerActivity.this.worksAtLabel.setTextColor(PlayerActivity.this.context.getResources()
                                .getColor(R.color.fb_bg_color));
                        PlayerActivity.this.livesAtLabel.setTextColor(PlayerActivity.this.context.getResources()
                                .getColor(R.color.fb_bg_color));
                        PlayerActivity.this.fromLocationLabel.setTextColor(PlayerActivity.this.context.getResources()
                                .getColor(R.color.fb_bg_color));
                        PlayerActivity.this.setProfile(user, PlayerActivity.this.userImage);

                        if (friend) {
                            PlayerActivity.this.addFriendButton.setVisibility(View.GONE);
                        } else {
                            PlayerActivity.this.addFriendButton.setVisibility(View.VISIBLE);
                        }

                        final String[] employer = user.getEmployer();
                        if ((employer != null) && (employer.length > 0)) {
                            for (int i = 0; i < employer.length; i++) {
                                if (i == 0) {
                                    workPlaces = employer[i];
                                } else {
                                    workPlaces = workPlaces + Constant.NEWLINE + employer[i];
                                }
                            }
                            PlayerActivity.this.worksAt.setText(workPlaces);
                        }

                    }

                }
            });
            Request.executeBatchAsync(request);

        } else if (Constant.TWITTER.equalsIgnoreCase(mediaSite)) {
            Config.setSocialSite(Constant.TWITTER);

            if (Config.getTwitterScreenId().equalsIgnoreCase(user.getId())) {
                this.setFacebookOwnFeed(user, mediaSite);

            } else if (friend) {
                this.setTwitterFriendInfo(user);

            } else {
                this.otherTwitterUserTagged = true;
                this.friendInfoLayout.setVisibility(View.VISIBLE);
                this.bannerHeader.setBackgroundResource(R.drawable.twitter_header);
                this.bannerImage.setImageResource(R.drawable.twitter_b);
                this.fbAboutUserLayout.setVisibility(View.GONE);
                this.twitteraboutuserLayout.setVisibility(View.VISIBLE);
                this.addFriendButton.setBackgroundResource(R.drawable.follow);
                this.twitterUserDescription.setText(user.getTwitterUserDescription());
                this.twitterUserFollowersCount.setText(user.getTwitterUserFollowerCount());
                this.tUserLocation.setText(user.getCurrentPlace());
                this.userName.setText(user.getUserName());
                this.setProfile(user, this.userImage);

                if (friend) {
                    this.addFriendButton.setVisibility(View.GONE);
                } else {
                    this.addFriendButton.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
        this.myVideoView.stopPlayback();
        this.playerRunning = false;

        if (this.navigateToPlayBack) {
            if (this.firstTimeMarkerUse) {
                MainManager.getInstance().setISFirstTimePlay(true);
            }

            final Intent intent = new Intent(PlayerActivity.this, HomeActivity.class);
            intent.putExtra(Constant.PATH, this.path);
            intent.putExtra(Constant.VIDEOID, this.videoId);
            intent.putExtra(Constant.NAVIGATION, Constant.TOUPLOAD);
            this.finish();
            this.startActivity(intent);
        }
        this.clearViews();
    }

    @Override
    public void onCancel(final DialogInterface dialog) {

        PlayerActivity.this.finish();

    }

    @Override
    public void onClick(final View view) {

        switch (view.getId()) {
        case R.id.tagSearch:
            this.linkSearch();
            break;

        case R.id.timetext:
            this.onTimeTextClick();
            break;

        case R.id.colorLay:
            this.onColorLayoutClick();
            break;

        case R.id.timeLay:
            this.onTimeTextClick();
            break;

        case R.id.firstValue:
            this.onFirstClick();
            break;

        case R.id.secondValue:
            this.onSecondClick();
            break;

        case R.id.thirdValue:
            this.onThirdClick();
            break;

        case R.id.fourthValue:
            this.onFourthClick();
            break;

        case R.id.red:
            this.onRedClick();
            break;

        case R.id.green:
            this.onGreenClick();
            break;

        case R.id.yellow:
            this.onYelloClick();
            break;

        case R.id.skyblue:
            this.onSkyblueClick();
            break;

        case R.id.black:
            this.onBlackClick();
            break;

        case R.id.white:
            this.onWhileClick();
            break;

        case R.id.lavender:
            this.onLavendarClick();
            break;

        case R.id.colorView:
            this.colorLayout.setVisibility(View.VISIBLE);
            break;

        case R.id.share:
            this.onShareClick();
            break;

        case R.id.comment:
            this.onCommentClick();
            break;

        case R.id.like:
            this.onLikeClick();
            break;

        case R.id.playergoogle:
            this.onPlayerGoogleClick();
            break;

        case R.id.playerfb:
            this.onPlayerFacebookClick();
            break;

        case R.id.playerwootag:
            this.onPlayerWootagClick();
    TagFu  break;

        case R.id.TagFu            break;

        case R.id.playertwitter:
            this.onPlayerTwitterClick();
            break;

        case R.id.update:
            this.update();
            break;

        case R.id.reset:
            this.reset();
            break;

        case R.id.submit:
            this.publish();
            break;

        case R.id.twittermessage:
            this.onTwitterMessageClick();
            break;

        case R.id.messagebutton:
            this.onMessgeClick(view);
            break;

        default:
            break;
        }
    }

    @Override
    public void onConnected(final Bundle connectionHint) {

        final String accountName = this.plusClient.getAccountName();
        final Person loggedUser = this.plusClient.getCurrentPerson();
        if (loggedUser.hasId()) {
            Config.setGoogleplusLoggedUserId(loggedUser.getId());
        }
        if (Constant.G_PLUS_FRIEND_LIST.equalsIgnoreCase(this.gPlusRequest)) {
            this.saveGPlusLoggedInDetails(this.plusClient.getCurrentPerson());
            this.connectionProgressDialog.setMessage(Constant.LOADING);
            this.plusClient.loadVisiblePeople(this, null);
        } else if (Constant.G_PLUS_LOAD_PERSON.equalsIgnoreCase(this.gPlusRequest)) {
            this.connectionProgressDialog.setMessage(Constant.LOADING);
            this.plusClient.loadVisiblePeople(this, null);
        } else if (Constant.G_PLUS_SHARE.equalsIgnoreCase(this.gPlusRequest)) {
            this.connectionProgressDialog.dismiss();
            if (this.gPlusID != null) {
                final String[] ids = new String[] { this.gPlusID };
                this.gPlusShare(ids);
            }
        } else if (Constant.G_PLUS_WRITE_ON_WALL.equalsIgnoreCase(this.gPlusRequest)) {
            this.connectionProgressDialog.dismiss();
            if (this.gPlusID != null) {
                final String[] ids = new String[] { this.gPlusID };
                this.gPlusWriteOnWall(ids);
            }
        }
        LOG.i("connection estbslished" + accountName);
    }

    @Override
    public void onConnectionFailed(final ConnectionResult result) {

        if (result.hasResolution()) {
            try {
                result.startResolutionForResult(this, REQUEST_CODE_RESOLVE_ERR);
            } catch (final SendIntentException e) {
                this.plusClient.connect();
            }
        }
        this.connectionResult = result;
    }

    @Override
    public void onDisconnected() {

        Alerts.showInfoOnly(Constant.CONNECTION_DISCONNECTED_PLEASE_TRY_AGIN, this.context);
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            PlayerActivity.this.finish();

        }
        return super.onKeyDown(keyCode, event);

    }

    @Override
    public void onPeopleLoaded(final ConnectionResult status, final PersonBuffer personBuffer,
            final String nextPageToken) {

        if (status.isSuccess()) {
            final List<Friend> gPlusFriendList = new ArrayList<Friend>();
            final int count = personBuffer.getCount();
            Friend friend = null;
            for (int i = 0; i < count; i++) {
                friend = new Friend();
                if (personBuffer.get(i).getId() != null) {
                    friend.setFriendID(personBuffer.get(i).getId());
                }
                if (personBuffer.get(i).getImage() != null) {
                    friend.setFriendImage(personBuffer.get(i).getImage().getUrl());
                }
                if (personBuffer.get(i).getDisplayName() != null) {
                    friend.setFriendName(personBuffer.get(i).getDisplayName().toString());
                }
                if (personBuffer.get(i).getCurrentLocation() != null) {
                    friend.setLocation(personBuffer.get(i).getCurrentLocation());
                }
                gPlusFriendList.add(friend);
            }

            friendList = gPlusFriendList;
            friendList.add(0, VideoPlayerApp.getInstance().getGPlusLoggedInUser());
            VideoPlayerApp.getInstance().setGoogleFriendList(friendList);

            if (Constant.G_PLUS_LOAD_PERSON.equalsIgnoreCase(this.gPlusRequest)) {
                this.plusClient.loadPeople(this, this.tagGPlusFriendID);
            } else {
                this.setFriendListAdapter(friendList);
            }
            this.connectionProgressDialog.dismiss();

        } else {
            this.connectionProgressDialog.dismiss();
            Alerts.showExceptionOnly(String.valueOf(status.getErrorCode()), this.context);
        }
    }

    @Override
    public void onPrepared(final MediaPlayer mediaPlayer) {

        if ((this.progressDialog != null) && this.progressDialog.isShowing()) {
            this.progressDialog.dismiss();
        }

        this.videoWidth = mediaPlayer.getVideoWidth();
        this.videoHeight = mediaPlayer.getVideoHeight();

        this.setVideoViewLayout();
        if (Config.getVideoCurrentPosition() != 0) {
            if (!this.initialState) {
                this.initialState = false;
                this.myVideoView.start();
                try {
                    Thread.sleep(1000);
                } catch (final InterruptedException e) {
                    LOG.e(e);
                }
                this.myVideoView.seekTo(Config.getVideoCurrentPosition());
                this.myVideoView.pause();
                this.playbutton.setBackgroundResource(R.drawable.play1_f);
            }

        } else {
            this.initialState = false;

            if (!Config.getUserId().equalsIgnoreCase(this.currentVideo.getUserId())
                    && !MainManager.getInstance().isFirstTimePlayOthersVideo()) {
                this.myVideoView.start();
                try {
                    Thread.sleep(200);
                } catch (final InterruptedException e) {
                    LOG.e(e);
                }
                this.myVideoView.pause();
                this.playbutton.setBackgroundResource(R.drawable.play1_f);
                this.dialogView = (LinearLayout) this.findViewById(R.id.dialogView);
                this.dialogView.setVisibility(View.VISIBLE);

            } else if (Config.getUserId().equalsIgnoreCase(this.currentVideo.getUserId())
                    && !MainManager.getInstance().isFirstTimePlay()) {
                this.myVideoView.start();
                this.controlsEnabled = false;
                try {
                    Thread.sleep(200);
                } catch (final InterruptedException e) {
                    LOG.e(e);
                }
                this.myVideoView.pause();
                this.playbutton.setBackgroundResource(R.drawable.play1_f);
                this.showTagTool();
                this.showHelpTagTool();
                this.publishAndTagView.setVisibility(View.VISIBLE);
                this.selectTagLocationLay.setVisibility(View.GONE);
                this.canceltagtool.setVisibility(View.GONE);

            } else {
                this.myVideoView.start();
                this.playbutton.setBackgroundResource(R.drawable.pause1_f);
            }
        }
        this.seekBar.setMax(this.myVideoView.getDuration());
        this.setTime(this.myVideoView.getDuration(), this.currentPosition);
    }

    @Override
    public void onTextChanged(final CharSequence arg0, final int arg1, final int arg2, final int arg3) {

    }

    @Override
    public boolean onTouch(final View view, final MotionEvent event) {

        return false;
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {

        if (!Config.getUserId().equalsIgnoreCase(this.currentVideo.getUserId())
                && !MainManager.getInstance().isFirstTimePlayOthersVideo()) {
            MainManager.getInstance().setISFirstTimePlayOthersVideo(true);
            if ((this.dialogView != null) && this.dialogView.isShown()) {
                this.dialogView.setVisibility(View.GONE);
            }
            this.myVideoView.start();
            this.playbutton.setBackgroundResource(R.drawable.pause1_f);

        } else {
            this.changeTagLoc = false;
            final Drawable drawable = this.getResources().getDrawable(R.drawable.next_arrow);
            final int nextButtonWidth = drawable.getIntrinsicWidth();
            final int nextButtonHeight = drawable.getIntrinsicHeight();

            if (isTagMode) {
                final float nextbuttonPosX = event.getX();
                final float nextbuttonPosY = event.getY();

                if ((nextbuttonPosX >= this.nextX)
                        && (nextbuttonPosX <= (this.nextX + nextButtonWidth + (nextButtonWidth / 4)))
                        && (nextbuttonPosY >= this.nextY)
                        && (nextbuttonPosY <= (this.nextY + nextButtonHeight + (nextButtonHeight / 4)))) {
                    if (this.tagVisible) {
                        this.showTagTool();
                    }

                } else {
                    currentX = event.getX();
                    currentY = event.getY();
                }

            } else {
                if (this.controlsEnabled) {
                    this.tagOptionsLayout.setVisibility(View.VISIBLE);
                    this.playerControlLayout.setVisibility(View.VISIBLE);
                    this.videoInfoLayout.setVisibility(View.VISIBLE);
                    this.controlsVisible = true;
                    this.noOfSec = 0;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onVideoSizeChanged(final MediaPlayer mp, final int width, final int height) {

    }

    public void postOnFaceBookWall(final String fbId, final String videoUrl, final String videoThumbPath) {

        final Bundle params = new Bundle();

        if (!Strings.isNullOrEmpty(this.currentVideo.getLatestTagexpression())) {
            params.putString(Constant.NAME, this.currentVideo.getLatestTagexpression());
        } else {
            params.putString(Constant.NAME, this.currentVideo.getVideoTitle());
        }

        params.putString(Constant.LINK, videoUrl);
        params.putString(Constant.PICTURE, videoThumbPath);
        params.putString(Constant.TO, fbId);

        final Request.Callback callback = new Request.Callback() {

            @Override
            public void onCompleted(final Response response) {

                if (response.getError() == null) {
                    final JSONObject graphResponse = response.getGraphObject().getInnerJSONObject();
                    String postId = null;
                    try {
                        postId = graphResponse.getString(Constant.ID);
                        Toast.makeText(PlayerActivity.this.context, Constant.POST_SUCCESSSFULLY_ON_YOUR_WALL,
                                Toast.LENGTH_LONG).show();
                    } catch (final JSONException e) {
                        LOG.i("Facebook Error JSON error " + e.getMessage());
                    }
                    final FacebookRequestError error = response.getError();
                }
            }
        };

        final Request request = new Request(session, Constant.ME_FEED, params, HttpMethod.POST, callback);
        final RequestAsyncTask task = new RequestAsyncTask(request);
        task.execute();

    }

    @Override
    public void processDone(final boolean status, final String action) {

    }

    @Override
    public void processFinish(final List<Friend> friendList, final String socialSite) {

        if (Constant.WOOTAG.equalsIgnoreCase(socialSite)) {
            VidTagFuerApp.getInstance().setWootagFriendsList(friendList);
            this.addWootTagFunContactToFriendList(friendList);
            TagFuetFriendListAdapter(friendList);

        } else if (Constant.TWITTER.equalsIgnoreCase(socialSite)) {
            VideoPlayerApp.getInstance().setTwitterFriendList(friendList);
            this.setFriendListAdapter(friendList);

        } else if (Constant.FACEBOOK.equalsIgnoreCase(socialSite)) {
            this.requestForFacebookFeed(friendList);

        } else if (Constant.GOOGLE_PLUS.equalsIgnoreCase(socialSite)) {
            VideoPlayerApp.getInstance().setGoogleFriendList(friendList);
            this.setFriendListAdapter(friendList);
        }

    }

    public void publishFeedDialog(final String fbId, final String videoUrl, final String videoThumbPath,
            final boolean writeOnWall) {

        if (!writeOnWall && !Config.getFacebookLoggedUserId().trim().equalsIgnoreCase(Constant.EMPTY)
                && fbId.trim().equalsIgnoreCase(Config.getFacebookLoggedUserId().trim())) {
            this.postOnFaceBookWall(fbId, videoUrl, videoThumbPath);
        } else {
            final Bundle params = new Bundle();

            if (!Strings.isNullOrEmpty(this.currentVideo.getLatestTagexpression())) {
                params.putString(Constant.NAME, this.currentVideo.getLatestTagexpression());
            } else {
                params.putString(Constant.NAME, this.currentVideo.getVideoTitle());
            }

            params.putString(Constant.LINK, videoUrl);
            params.putString(Constant.PICTURE, videoThumbPath);
            params.putString(Constant.TO, fbId);

            final WebDialog feedDialog = new WebDialog.FeedDialogBuilder(this, Session.getActiveSession(), params)
                    .setOnCompleteListener(new OnCompleteListener() {

                        @Override
                        public void onComplete(final Bundle values, final FacebookException error) {

                            if (error == null) {
                                final String postId = values.getString(Constant.POST_ID);
                                if (postId != null) {
                                    Alerts.showInfoOnly(Constant.POSTED_LINK, PlayerActivity.this.context);
                                    if (writeOnWall) {
                                        new TagInteractionAsync(PlayerActivity.this.currentVideo.getVideoID(),
                                                Constant.FACEBOOK_PLATFORM, Constant.WRITE_ON_WALL,
                                                PlayerActivity.this.userId, PlayerActivity.this.context).execute();
                                    }
                                } else {
                                    Alerts.showInfoOnly(Constant.VIDEO_NOT_SHARED_TO_YOUR_TAGGED_CONTACT,
                                            PlayerActivity.this.context);
                                }
                            } else if (error instanceof FacebookOperationCanceledException) {
                                Alerts.showInfoOnly(Constant.VIDEO_NOT_SHARED_TO_YOUR_TAGGED_CONTACT,
                                        PlayerActivity.this.context);
                            } else {
                                Alerts.showInfoOnly(Constant.ERROR_POSTING_LINK, PlayerActivity.this.context);
                            }
                        }

                    }).build();
            feedDialog.show();
        }
    }

    /**
     * sharing tag to social sites walls
     */
    public void shareUrlToTaggedUser(final TagInfo currentTag) {

        this.gPlusID = null;
        if (!Strings.isNullOrEmpty(currentTag.getFbId()) && (session != null) && session.isOpened()
                && (session.getState() == SessionState.OPENED)) {
            this.publishFeedDialog(currentTag.getFbId(), this.currentVideo.getFbShareUrl(),
                    this.currentVideo.getVideothumbPath(), false);
        }

        if (!Strings.isNullOrEmpty(currentTag.getgPlusId())) {
            this.gPlusID = currentTag.getgPlusId();
            this.gPlusRequest = Constant.G_PLUS_SHARE;
            this.gPlusLogin();
        }

        if (!Strings.isNullOrEmpty(currentTag.getTwId()) && (this.currentVideo.getShareUrl() != null)) {
            final TwitterAsync asyncTask = new TwitterAsync(Constant.EMPTY, this.context, currentTag.getTwId(),
                    Constant.TWITTER_TWEET, this.currentVideo.getShareUrl(), this.currentVideo, Constant.EMPTY);
            asyncTask.execute();
        }

    }

    private void addWootagLoginContactToFriendList(final List<Friend> friendLTagFu

        Friend fbContact = null;
        if (!Strings.isNullOrEmpty(Config.getUserId())) {
            fbContact = new Friend();
            fbContact.setFriendID(Config.getUserId());
            fbContact.setFriendName(Constant.YOU);
            fbContact.setFriendImage(MainManager.getInstance().getUserPick());
        }
        if (friendList == null) {
            final List<Friend> list = new ArrayList<Friend>();
            list.add(fbContact);
            VideoPlayerApp.getInstance().setWootagFriendsList(list);
        } else {
            frieTagFu.add(0, fbContact);
            VideoPlayerApp.getInstance().setWootagFriendsList(friendList);
        }

    }

    /** ChTagFu whether twitter user is authenticated or not */
    private void authentication() {

        LOG.i(Constant.TAG_TWITTER_FRIEND_ID + this.tagTwitterFriendId);
        if (Constant.TWITTER_FRIEND_INFO.equalsIgnoreCase(Config.getTwitterRequestFor())) {
            final TwitterAsync asyncTask = new TwitterAsync(Constant.EMPTY, this.context, this.tagTwitterFriendId,
                    Constant.TWITTER_FRIEND_INFO, Constant.EMPTY, null, Constant.EMPTY);
            asyncTask.delegate = PlayerActivity.this;
            asyncTask.execute();

        } else {
            final TwitterAsync asyncTask = new TwitterAsync(Constant.EMPTY, this.context, Constant.EMPTY,
                    Constant.EMPTY, Constant.EMPTY, null, Constant.EMPTY);
            asyncTask.delegate = PlayerActivity.this;
            asyncTask.execute();
        }
    }

    private void disableTagToolViews() {

        this.colorLay.setEnabled(false);
        this.timeLay.setEnabled(false);
        this.tagName.setEnabled(false);
        this.tagLink.setEnabled(false);
        this.facebook.setEnabled(false);
        this.google.setEnabled(false);
        this.twitter.setEnabled(false);
        this.wootag.setEnabled(false);
        this.submit.setEnabled(falTagFu       this.publish.setEnabled(false);
        this.reset.setEnabled(false);
        this.update.setEnabled(false);
        this.help.setEnabled(false);
    }

    private void displayShowTagView(final TagInfo tag, final int tagIndex) {

        if (handler != null) {
            handler.post(new Runnable() {

                @Override
                public void run() {

                    if (PlayerActivity.this.myVideoView.isPlaying()) {
                        // try {
                        final int id = tag.getViewId();
                        if (id == 0) {
                            LinearLayout tagView = null;
                            tagView = PlayerActivity.this.getTagView(tag, tagIndex);
                            if (!tagView.isShown()) {
                                PlayerActivity.this.videoLayout.addView(tagView);
                            }
                        } else {
                            LinearLayout tagView = null;

                            final View view = PlayerActivity.this.findViewById(id);

                            if (view instanceof LinearLayout) {
                                tagView = (LinearLayout) PlayerActivity.this.findViewById(id);

                                if (tagView == null) {
                                    tagView = PlayerActivity.this.getTagView(tag, tagIndex);
                                    PlayerActivity.this.videoLayout.addView(tagView);

                                } else if (!tagView.isShown()) {
                                    PlayerActivity.this.videoLayout.addView(tagView);
                                }

                            } else {

                                tagView = PlayerActivity.this.getTagView(tag, tagIndex);
                                LOG.i("AddTag second time " + tagView.isShown());

                                if (!tagView.isShown()) {
                                    PlayerActivity.this.videoLayout.addView(tagView);

                                }
                            }
                        }
                    }
                }
            });
        }

    }

    private void displayTagMarker(final TagInfo tag) {

        if (handler != null) {
            handler.post(new Runnable() {

                @Override
                public void run() {

                    PlayerActivity.this.seekbarLay.addView(PlayerActivity.this.updateSeekBarWithDots(tag,
                            CustomLayout.getWidthOfSlider()));
                    LOG.i("add tagViewLayout tagicon added" + tag.getVideoPlaybackTime());
                }
            });
        }
    }

    private void enableTagToolViews() {

        this.colorLay.setEnabled(true);
        this.timeLay.setEnabled(true);
        this.tagName.setEnabled(true);
        this.tagLink.setEnabled(true);
        this.facebook.setEnabled(true);
        this.google.setEnabled(true);
        this.twitter.setEnabled(true);
        this.wootag.setEnabled(true);
        this.submit.setEnabled(true)TagFu    this.publish.setEnabled(true);
        this.reset.setEnabled(true);
        this.update.setEnabled(true);
        this.help.setEnabled(true);
    }

    private void facebookAction() {

        Config.setSocialSite(Constant.FACEBOOK);

        this.tagLay.setVisibility(View.GONE);
        this.canceltagtool.setVisibility(View.GONE);
        this.taglogo.setVisibility(View.GONE);
        this.help.setVisibility(View.GONE);
        this.fbsearch.setText(Constant.EMPTY);
        this.searchIcon.setImageResource(R.drawable.sharefacebook);
        this.fbFriendsLayout.setVisibility(View.VISIBLE);
        if ((VideoPlayerApp.getInstance().getFbFriendsList() != null)
                && (VideoPlayerApp.getInstance().getFbFriendsList().size() > 0)) {
            friendList = VideoPlayerApp.getInstance().getFbFriendsList();
            this.setFriendListAdapter(friendList);

        } else {
            // emptyListView.setVisibility(View.VISIBLE);
            this.fbfrndList.setVisibility(View.GONE);
            // fbfilterList.setVisibility(View.GONE);
            session = this.createSession();
            Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
            if (Session.getActiveSession().isOpened()) {
                this.setFacebookFriendList();
            } else {
                LOG.i("login trying......");
                final StatusCallback callback = new StatusCallback() {

                    @Override
                    public void call(final Session session, final SessionState state, final Exception exception) {

                        if (exception != null) {
                            new AlertDialog.Builder(PlayerActivity.this).setTitle("Login failed")
                                    .setMessage(exception.getMessage()).setPositiveButton("OK", null).show();
                            PlayerActivity.session = PlayerActivity.this.createSession();
                        }
                    }
                };
                pendingRequest = true;
                session.openForRead(new Session.OpenRequest(PlayerActivity.this).setCallback(callback));
                LOG.i("fb session" + Session.getActiveSession().isOpened());
                this.setFacebookFriendList();
            }
        }

    }

    private void facebookFriendInfo(final String fbid) {

        Config.setSocialSite(Constant.FACEBOOK);
        fbRequest = Constant.FACEBOOK_USER_INFO;
        session = this.createSession();
        Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
        if (Session.getActiveSession().isOpened()) {
            Config.setFacebookAccessToken(session.getAccessToken());
            final FacebookFriendsAsync async = new FacebookFriendsAsync(this.context, Constant.FRIEND_INFO, fbid);
            async.delegate = PlayerActivity.this;
            async.execute();
        } else {
            final StatusCallback callback = new StatusCallback() {

                @Override
                public void call(final Session session, final SessionState state, final Exception exception) {

                    if (exception != null) {
                        new AlertDialog.Builder(PlayerActivity.this).setTitle("Login failed")
                                .setMessage(exception.getMessage()).setPositiveButton("OK", null).show();
                        PlayerActivity.session = PlayerActivity.this.createSession();
                    }
                }
            };
            pendingRequest = true;
            session.openForRead(new Session.OpenRequest(PlayerActivity.this).setCallback(callback));
            if (Session.getActiveSession().isOpened()) {
                Config.setFacebookAccessToken(session.getAccessToken());
                final FacebookFriendsAsync async = new FacebookFriendsAsync(this.context, Constant.FRIEND_INFO, fbid);
                async.delegate = PlayerActivity.this;
                async.execute();
            }
        }
    }

    private void getTagLinkButtonHeight() {

        final LinearLayout view = (LinearLayout) LayoutInflater.from(this.context).inflate(R.layout.tag_view_three,
                null);
        final LinearLayout tagLinkButtonthree = (LinearLayout) view.findViewById(R.id.tagLinkButtonthree);
        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tagLinkButtonthree.getLayoutParams();
        this.tagExpressionLayoutHeight = params.height;
    }

    private void googleAction() {

        Config.setSocialSite(Constant.GOOGLE_PLUS);
        this.tagLay.setVisibility(View.GONE);
        this.canceltagtool.setVisibility(View.GONE);
        this.taglogo.setVisibility(View.GONE);
        this.help.setVisibility(View.GONE);
        this.fbsearch.setText(Constant.EMPTY);
        this.searchIcon.setImageResource(R.drawable.sharegoogleplus);
        this.fbFriendsLayout.setVisibility(View.VISIBLE);
        if ((VideoPlayerApp.getInstance().getGoogleFriendList() != null)
                && (VideoPlayerApp.getInstance().getGoogleFriendList().size() > 0)) {
            friendList = VideoPlayerApp.getInstance().getGoogleFriendList();
            this.setFriendListAdapter(friendList);
        } else {
            // emptyListView.setVisibility(View.VISIBLE);
            this.fbfrndList.setVisibility(View.GONE);
            // fbfilterList.setVisibility(View.GONE);
            this.gPlusRequest = Constant.G_PLUS_FRIEND_LIST;
            this.gPlusLogin();
        }
    }

    private void gPlusFriendInfo(final String gPlusID) {

        this.gPlusRequest = Constant.G_PLUS_LOAD_PERSON;
        this.gPlusLogin();
    }

    // TODO Figure out a way to merge with onPeopleLoaded
    // @Override
    // public void onPersonLoaded(final ConnectionResult status, final Person person) {
    //
    // LOG.i("on people  load");
    //
    // final boolean friend = this.isFriendToUser(this.tagGPlusFriendID);
    // Config.setSocialSite(Constant.GOOGLE_PLUS);
    // if (status.getErrorCode() == ConnectionResult.SUCCESS) {
    // try {
    // final FacebookUserInfo user = new FacebookUserInfo();
    // user.setProfilePick(person.getImage().getUrl());
    // user.setId(person.getId());
    // String companyNames = "";
    // String educationPlaces = "";
    // String livingPlaces = "";
    // final List<PlacesLived> places = person.getPlacesLived();
    // if (places != null) {
    //
    // for (int i = 0; i < places.size(); i++) {
    // final PlacesLived place = places.get(i);
    // if (i == 0) {
    // livingPlaces = place.getValue();
    // } else {
    // livingPlaces = livingPlaces + "\n" + place.getValue();
    // }
    // }
    // }
    // final List<Organizations> organizations = person.getOrganizations();
    // if ((organizations != null) && (organizations.size() > 0)) {
    // for (int i = 0; i < organizations.size(); i++) {
    // final Organizations org = organizations.get(i);
    // if (org.getType() == 0) {
    // if (i == 0) {
    // companyNames = org.getName();
    // } else {
    // companyNames = companyNames + "\n" + org.getName();
    // }
    // } else if (org.getType() == 1) {
    // if (i == 0) {
    // educationPlaces = org.getName();
    // } else {
    // educationPlaces = educationPlaces + "\n" + org.getName();
    // }
    // }
    //
    // }
    // }
    // if (person.hasBirthday()) {
    // user.setBirthDay(person.getBirthday());
    // }
    // if (person.hasTagline()) {
    // user.setStatusUpdate(person.getTagline());
    // }
    // user.setCurrentPlace(livingPlaces);
    // if (person.hasDisplayName()) {
    // user.setUserName(person.getDisplayName());
    // } else {
    // if ((person != null) && (person.getName().getGivenName() != null)) {
    // user.setUserName(person.getName().getGivenName());
    // } else if ((person != null) && (person.getName() != null)) {
    // user.setUserName(person.getName().toString());
    // }
    // }
    //
    // if ((Config.getGoogleplusLoggedUserId() != null)
    // && Config.getGoogleplusLoggedUserId().equalsIgnoreCase(user.getId())) {
    // this.setFacebookOwnFeed(user, Constant.GOOGLE_PLUS);
    // } else if (friend) {
    // this.setFacebookFriendInfo(user, Constant.GOOGLE_PLUS);
    // } else {
    // this.userName.setText("");
    // this.worksAt.setText("");
    // this.livesAt.setText("");
    // this.fromLocation.setText("");
    //
    // // messageButton.setTag(user);
    // this.friendInfoLayout.setVisibility(View.VISIBLE);
    // this.bannerHeader.setBackgroundResource(R.drawable.google_header);
    // this.bannerImage.setImageResource(R.drawable.google_b);
    // this.fbAboutUserLayout.setVisibility(View.VISIBLE);
    // this.twitteraboutuserLayout.setVisibility(View.GONE);
    // // messageButton.setVisibility(View.GONE);
    // this.addFriendButton.setVisibility(View.GONE);
    // // try {
    // this.addFriendButton.setBackgroundResource(R.drawable.add_friend);
    // // } catch (final Exception e) {
    // // e.printStackTrace();
    // // }
    //
    // this.livesAtLabel.setTextColor(this.context.getResources().getColor(R.color.gplus_bg_color));
    // this.worksAtLabel.setTextColor(this.context.getResources().getColor(R.color.gplus_bg_color));
    //
    // this.fromLocationLabel.setTextColor(this.context.getResources().getColor(R.color.gplus_bg_color));
    // if ((person != null) && (person.getName().getGivenName() != null)) {
    // this.userName.setText(person.getName().getGivenName());
    // } else if ((person != null) && (person.getName() != null)) {
    // this.userName.setText(person.getName().toString());
    // }
    // this.fromLocation.setText("");
    // this.worksAt.setText(companyNames);
    // this.livesAt.setText(livingPlaces);
    // this.setProfile(user, this.userImage);
    // }
    // } catch (final Exception e) {
    // LOG.i("exception on person load" + e.toString());
    // this.connectionProgressDialog.dismiss();
    // }
    // } else {
    // this.connectionProgressDialog.dismiss();
    // Alerts.showConfirmAlert("Google Exeption", String.valueOf(status.getErrorCode()), this.context);
    // LOG.e("on person : " + status.getErrorCode());
    //
    // }
    // }

    private void gPlusLogin() {

        Config.setSocialSite(Constant.GOOGLE_PLUS);
        this.plusClient = new PlusClient.Builder(PlayerActivity.this, PlayerActivity.this, PlayerActivity.this)
                .setScopes(Scopes.PLUS_LOGIN)
                .setActions(Constant.HTTP_SCHEMAS_GOOGLE_COM_ADD_ACTIVITY,
                        Constant.HTTP_SCHEMAS_GOOGLE_COM_BUY_ACTIVITY).build();
        this.plusClient.connect();
        this.connectionProgressDialog = new ProgressDialog(PlayerActivity.this);
        this.connectionProgressDialog.setMessage("Signing in...");
        if (this.connectionResult == null) {
            this.connectionProgressDialog.show();
        } else {
            try {
                LOG.i("connection starting");
                this.connectionResult.startResolutionForResult(PlayerActivity.this, REQUEST_CODE_RESOLVE_ERR);
            } catch (final SendIntentException e) {
                // Try connecting again.
                LOG.i("trying connection restarting");
                this.connectionResult = null;
                this.plusClient.connect();
            }
        }
    }

    /**
     * To publish video url on gplus
     */
    private void gPlusShare(final String[] ids) {

        final ArrayList<Person> recipients = new ArrayList<Person>();
        for (int i = 0; i < ids.length; i++) {
            if (!ids[i].trim().equalsIgnoreCase(Constant.EMPTY)) {
                recipients.add(PlusShare.createPerson(ids[i], ids[i]));
            }
        }
        final Intent shareIntent = new PlusShare.Builder(PlayerActivity.this, this.plusClient)
                .setType(Constant.TEXT_PLAIN)
                .setText(this.currentVideo.getVideoTitle())
                .setContentDeepLinkId("/wootag/video", this.currentVideo.getVideoTitle(), Constant.EMPTagFu                      Uri.parse(this.currentVideo.getVideothumbPath())).setRecipients(recipients)
                .setContentUrl(Uri.parse(this.currentVideo.getShareUrl())).getIntent();
        this.startActivityForResult(shareIntent, 0);
    }

    private void gPlusWriteOnWall(final String ids[]) {

        final ArrayList<Person> recipients = new ArrayList<Person>();
        for (int i = 0; i < ids.length; i++) {
            if (!ids[i].trim().equalsIgnoreCase(Constant.EMPTY)) {
                recipients.add(PlusShare.createPerson(ids[i], ids[i]));
            }
        }

        final Intent shareIntent = new PlusShare.Builder(PlayerActivity.this, this.plusClient)
                .setType(Constant.TEXT_PLAIN).setText(Constant.EMPTY).setRecipients(recipients).getIntent();
        this.startActivityForResult(shareIntent, PlayerActivity.GOOGLE_PLUS_WRITE_ON_WALL_REQUEST_CODE);

    }

    /**
     * checking for # seperator in tag expression and if it is avilable making portion of the tag expression consider as
     * trend and giving click action for that text.
     */
    private StringBuilder hashSeperator(final String text, final float tagX, final float tagY, final TagInfo tag,
            final LinearLayout upperLinearLay, final LinearLayout lowerLinearLay) {

        // LinearLayout linkbuttonView=(LinearLayout)v;
        boolean textViewAddedToLowerLinearLayout = false;
        final StringBuilder answer = new StringBuilder();
        final String s = text;
        int tempStringLength = 0;
        int currentStringLength = 0;
        int splitWordLen = 0;
        final String[] words = s.split("\\s+");
        if (words != null) {
            // New #starbucks quite a different one
            final TextView tv[] = new TextView[words.length];
            for (int i = 0; i < words.length; i++) {
                final char firstChar = words[i].charAt(0);
                splitWordLen = splitWordLen + words[i].length();
                tv[i] = new TextView(PlayerActivity.this);
                if (i < words.length) {
                    if (firstChar == '#') {
                        final String txt = words[i] + " ";
                        final SpannableString content = new SpannableString(words[i] + " ");
                        content.setSpan(new UnderlineSpan(), 0, txt.length(), 0);
                        tv[i].setText(content);
                        tv[i].setTextColor(this.getResources().getColor(R.color.twitter_bg_color));
                        tempStringLength = tempStringLength + (words[i] + " ").trim().length();
                        currentStringLength = (words[i] + " ").trim().length();
                    } else {
                        tempStringLength = tempStringLength + (words[i] + " ").trim().length();
                        currentStringLength = (words[i] + " ").trim().length();
                        tv[i].setText(words[i] + " ");
                    }
                } else {
                    if (firstChar == '#') {
                        final String txt = words[i];
                        tempStringLength = tempStringLength + words[i].trim().length();
                        currentStringLength = words[i].trim().length();
                        final SpannableString content = new SpannableString(words[i] + " ");
                        content.setSpan(new UnderlineSpan(), 0, txt.length(), 0);
                        tv[i].setText(content);
                        tv[i].setTextColor(this.getResources().getColor(R.color.twitter_bg_color));
                        // tv[i].setText(words[i]);
                    } else {
                        tempStringLength = tempStringLength + words[i].trim().length();
                        currentStringLength = words[i].trim().length();
                        tv[i].setText(words[i]);
                    }
                }
                if (tempStringLength < 20) {
                    if ((tempStringLength > 15) && (currentStringLength >= 10)) {
                        textViewAddedToLowerLinearLayout = true;
                        lowerLinearLay.addView(tv[i]);
                    } else {
                        upperLinearLay.addView(tv[i]);
                    }
                } else {
                    textViewAddedToLowerLinearLayout = true;
                    lowerLinearLay.addView(tv[i]);
                }

                // linkbuttonView.addView(tv[i]);
                if (firstChar == '#') {
                    answer.append(" " + words[i]);

                    tv[i].setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(final View v) {

                            if (PlayerActivity.this.edit.isChecked()) {
                                PlayerActivity.this.showUpdateTagTool(tag, tagX, tagY);
                            } else {
                                final TextView temp = (TextView) v;
                                final String text = temp.getText().toString();
                                final char firstChar = text.charAt(0);
                                if (firstChar == '#') {
                                    PlayerActivity.this.handlePause();
                                    final Intent intent = new Intent(PlayerActivity.this, TrendVideosActivity.class);
                                    intent.putExtra(Constant.TRENDNAME, text);
                                    PlayerActivity.this.startActivity(intent);
                                }
                            }
                        }
                    });
                }
            }
        }
        if (!textViewAddedToLowerLinearLayout) {
            final TextView view = new TextView(PlayerActivity.this);
            view.setText(Constant.EMPTY);
            lowerLinearLay.addView(view);
        }
        return answer;
    }

    private void intializeAllPlayerView() {

        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {

                final Uri uri = Uri.parse(PlayerActivity.this.path);
                LOG.i("Player on create");
                PlayerActivity.this.adapterList = new ArrayList<Friend>();
                handler = new Handler();
                PlayerActivity.this.tagViews = new ArrayList<TagView>();
                final DisplayMetrics displaymetrics = new DisplayMetrics();
                PlayerActivity.this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                PlayerActivity.this.screenHeight = displaymetrics.heightPixels;
                PlayerActivity.this.screenWidth = displaymetrics.widthPixels;
                PlayerActivity.this.getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                        android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                PlayerActivity.this.initializePlayerViews();
                // end
                final IntentFilter filter = new IntentFilter();
                filter.addAction(Constant.TWITTER_FRIEND_LIST);
                filter.addAction(Constant.TWITTER_EXCEPTION);
                filter.addAction(Constant.CANCEL_OPERATION);
                if (PlayerActivity.this.receiver == null) {
                    PlayerActivity.this.receiver = new UpdateReceiver();
                    PlayerActivity.this.registerReceiver(PlayerActivity.this.receiver, filter);
                }
                if (PlayerActivity.this.path != null) {
                    PlayerActivity.this.myVideoView.setVideoURI(uri);
                    PlayerActivity.this.seekBar.setProgress(0);
                } else {
                    Alerts.showInfo("Video url not found", PlayerActivity.this);
                }

                PlayerActivity.this.deleteButtonIds = new ArrayList<Integer>();
                VideoPlayerApp.tagInfo = VideoDataBase.getInstance(PlayerActivity.this.context).getAllTagsByVideoId(
                        PlayerActivity.this.videoId, PlayerActivity.this.context, PlayerActivity.this.uploadedVideo);// VideoPlayerConstants.clientVideoID

                PlayerActivity.this.intializeTagLayoutVariables();
                PlayerActivity.this.initializeFriendInfoLayoutVariables();
                PlayerActivity.this.fbsearch.addTextChangedListener(PlayerActivity.this);

                if (PlayerActivity.this.currentVideo.getUserId() != null) {
                    PlayerActivity.this.tag.setEnabled(true);
                    PlayerActivity.this.tag.setChecked(true);
                    if (Config.getUserId().equalsIgnoreCase(PlayerActivity.this.currentVideo.getUserId())) {
                        PlayerActivity.this.edit.setEnabled(true);
                    } else {
                        PlayerActivity.this.edit.setEnabled(false);
                    }
                } else {
                    PlayerActivity.this.edit.setEnabled(false);
                }

                PlayerActivity.this.myVideoView.setOnPreparedListener(PlayerActivity.this);

                PlayerActivity.this.progressDialog = ProgressDialog.show(PlayerActivity.this.context, Constant.EMPTY,
                        Constant.EMPTY, true);
                final View v = PlayerActivity.this.inflater.inflate(R.layout.progress_bar, null, false);
                final TextView progressText = (TextView) v.findViewById(R.id.progressText);
                progressText.setText("Buffering");
                PlayerActivity.this.progressDialog.setContentView(v);
                PlayerActivity.this.progressDialog.setCancelable(true);
                PlayerActivity.this.progressDialog.setCanceledOnTouchOutside(false);
                PlayerActivity.this.progressDialog.show();
                PlayerActivity.this.progressDialog.setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(final DialogInterface dialog) {

                        PlayerActivity.this.progressDialog.dismiss();
                        PlayerActivity.this.playerRunning = false;
                        // System.gc();
                        PlayerActivity.this.finish();

                    }
                });

                PlayerActivity.this.actionEvents();
                /**
                 * running a thread to update player progress and check tags availble at particular moment
                 */
                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        while (PlayerActivity.this.playerRunning) {
                            int total = 0;
                            // try {
                            // try {
                            total = PlayerActivity.this.myVideoView.getDuration();
                            // } catch (final Exception e) {
                            // LOG.i("player", "duration " + total);
                            // }
                            while ((PlayerActivity.this.myVideoView != null)
                                    && (PlayerActivity.this.currentPosition < total)) {

                                if ((CustomLayout.getWidthOfSlider() > 0) && PlayerActivity.this.isFirst) {
                                    PlayerActivity.this.isFirst = false;
                                    if (VideoPlayerApp.tagInfo != null) {
                                        for (int i = 0; i < VideoPlayerApp.tagInfo.size(); i++) {
                                            PlayerActivity.this.displayTagMarker(VideoPlayerApp.tagInfo.get(i));
                                        }
                                    }
                                }

                                try {
                                    Thread.sleep(5);
                                } catch (final InterruptedException e) {
                                    LOG.e(e);
                                }
                                PlayerActivity.this.setVisibilty();
                                PlayerActivity.this.setProgress();

                                if (PlayerActivity.this.tag.isChecked() && (VideoPlayerApp.tagInfo != null)
                                        && (VideoPlayerApp.tagInfo.size() > 0)) {
                                    for (int i = 0; i < VideoPlayerApp.tagInfo.size(); i++) {
                                        final TagInfo tag = VideoPlayerApp.tagInfo.get(i);
                                        if ((PlayerActivity.this.currentPosition - tag.getTagTimeOutFrame()) >= 0) {
                                            PlayerActivity.this.removeTagView(tag, i);
                                        }
                                        if (((PlayerActivity.this.currentPosition - tag.getVideoPlaybackTime()) >= 0)
                                                && ((PlayerActivity.this.currentPosition - tag.getVideoPlaybackTime()) <= 1000)) {// 1
                                            PlayerActivity.this.displayShowTagView(tag, i);
                                        }
                                    }
                                }
                            }
                            // } catch (final Exception e) {
                            // e.printStackTrace();
                            // LOG.i("exception player seek", " " + e.toString());
                            // }
                        }

                    }
                }).start();

                PlayerActivity.this.seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {

                        if (!fromUser) {
                            return;
                        }

                        PlayerActivity.this.currentProgress = progress;
                        PlayerActivity.this.myVideoView.seekTo(PlayerActivity.this.currentProgress);
                        PlayerActivity.this.setTime(PlayerActivity.this.myVideoView.getDuration(), progress);
                        PlayerActivity.this.currentPosition = PlayerActivity.this.myVideoView.getDuration();

                    }

                    @Override
                    public void onStartTrackingTouch(final SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(final SeekBar seekBar) {

                        PlayerActivity.this.currentProgress = PlayerActivity.this.seekBar.getProgress();
                        PlayerActivity.this.currentPosition = PlayerActivity.this.myVideoView.getDuration();
                        PlayerActivity.this.myVideoView.seekTo(PlayerActivity.this.currentProgress);
                        PlayerActivity.this.setProgress();
                    }
                });

                PlayerActivity.this.myVideoView.setOnCompletionListener(new OnCompletionListener() {

                    @Override
                    public void onCompletion(final MediaPlayer mp) {

                        if (handler != null) {
                            handler.post(new Runnable() {

                                @Override
                                public void run() {

                                    PlayerActivity.this.seekBar.setProgress(PlayerActivity.this.myVideoView
                                            .getDuration());
                                    LOG.i("player on complete listener ");
                                    Config.setVideoCurrentPosition(0);
                                    PlayerActivity.this.playbutton.setBackgroundResource(R.drawable.play1_f);
                                    Config.setPlaybackEnd(true);

                                    LOG.i("no.of tag in play back " + VideoPlayerApp.tagInfo.size());
                                    PlayerActivity.this.seekBar.setProgress(0);
                                    PlayerActivity.this.setTime(PlayerActivity.this.myVideoView.getDuration(), 0);

                                }
                            });
                        }

                    }
                });

                PlayerActivity.this.myVideoView.setOnTouchListener(new OnTouchListener() {

                    @Override
                    public boolean onTouch(final View v, final MotionEvent event) {

                        return false;

                    }
                });

                PlayerActivity.this.myVideoView.setOnErrorListener(new OnErrorListener() {

                    @Override
                    public boolean onError(final MediaPlayer mp, final int what, final int extra) {

                        Alerts.showInfo("Sorry,error occurred while playing.", PlayerActivity.this);

                        return true;
                    }
                });
            }
        });
    }

    private void intializeHelpToolViews() {

        this.helpInstructionsView = (RelativeLayout) this.findViewById(R.id.helpInstructionsLayout);
        this.instructionsView = (RelativeLayout) this.findViewById(R.id.instructionLayout);
        this.textView = (TextView) this.findViewById(R.id.instructiontext);
        this.closeHelptool = (ImageView) this.findViewById(R.id.closehelptool);
        this.tagArrow = (ImageView) this.findViewById(R.id.tagarrow);
        this.linkArrow = (ImageView) this.findViewById(R.id.linkarrow);
        this.tagExpressionArrow = (ImageView) this.findViewById(R.id.tagexpressionarrow);
        this.publishArrow = (ImageView) this.findViewById(R.id.publisharrow);
        this.fbArrow = (ImageView) this.findViewById(R.id.fbarrow);
        this.firstTagInstruction = (ImageView) this.findViewById(R.id.first);
        this.secondTagInstruction = (ImageView) this.findViewById(R.id.second);
        this.thirdTagInstruction = (ImageView) this.findViewById(R.id.third);
        this.fourthTagInstruction = (ImageView) this.findViewById(R.id.fourth);
        this.fifthTagInstruction = (ImageView) this.findViewById(R.id.fifth);
        this.sixthTagInstruction = (ImageView) this.findViewById(R.id.sixth);
    }

    private void intializeLinkCallToActionView() {

        this.linkCallToActionsLayout = (RelativeLayout) this.findViewById(R.id.linkcalltoaction);
        this.webView = (WebView) this.findViewById(R.id.linkactionwebview);
        this.webViewBack = (Button) this.findViewById(R.id.linkactionback);
        this.linkProgress = (ProgressBar) this.findViewById(R.id.linkprogress);
        this.leftArrow = (ImageView) this.findViewById(R.id.leftarrow);
        this.rightArrow = (ImageView) this.findViewById(R.id.rightarrow);
        this.leftArrowdisable = (ImageView) this.findViewById(R.id.leftdisable);
        this.rightArrowdisable = (ImageView) this.findViewById(R.id.rightarrowdisable);
        this.refresh = (ImageView) this.findViewById(R.id.refreshwebview);
        this.webViewShare = (Button) this.findViewById(R.id.sharelinkitoption);
    }

    private boolean isFriendToUser(final String gplusId) {

        boolean friend = false;
        if (VideoPlayerApp.getInstance().getGoogleFriendList() != null) {
            for (int i = 0; i < VideoPlayerApp.getInstance().getGoogleFriendList().size(); i++) {
                final Friend frnd = VideoPlayerApp.getInstance().getGoogleFriendList().get(i);
                if ((frnd != null) && gplusId.equalsIgnoreCase(frnd.getFriendId())) {
                    friend = true;
                    break;
                }
            }
        }
        return friend;
    }

    @JavascriptInterface
    private void linkCallToaction(final String link, final boolean callToAction) {

        // try {
        this.linkCallToActionsLayout.setVisibility(View.VISIBLE);
        if (!callToAction) {
            this.webViewShare.setBackgroundResource(R.drawable.tag_linkit);
        } else {
            this.webViewShare.setBackgroundResource(R.drawable.share_video);
        }
        this.leftArrowdisable.setVisibility(View.VISIBLE);
        this.rightArrowdisable.setVisibility(View.VISIBLE);
        this.leftArrow.setVisibility(View.GONE);
        this.rightArrow.setVisibility(View.GONE);
        this.refresh.setVisibility(View.GONE);

        this.linkProgress.setVisibility(View.VISIBLE);
        this.loadLink(link, this.webView, this.linkProgress, this.refresh, false);

        this.webViewShare.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View arg0) {

                if (callToAction) {
                    final Intent intent = new Intent(PlayerActivity.this, ShareActivity.class);
                    intent.putExtra(Constant.VIDEO, PlayerActivity.this.currentVideo);
                    PlayerActivity.this.startActivity(intent);
                } else {
                    PlayerActivity.this.currentWebViewUrl = PlayerActivity.this.webView.getUrl();
                    // try {
                    PlayerActivity.this.releaseWebView(PlayerActivity.this.webView);
                    // } catch (final Exception e) {
                    // VideoPlayerApp.getInstance().writeStackTraceToLog(e, PlayerActivity.this.context);
                    // }
                    PlayerActivity.this.linkItAction(PlayerActivity.this.currentWebViewUrl);
                }
            }
        });
        this.refresh.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View arg0) {

                // try {
                // System.gc();
                PlayerActivity.this.refresh.setVisibility(View.GONE);
                PlayerActivity.this.linkProgress.setVisibility(View.VISIBLE);
                // mWebView.loadUrl("javascript:window.location.reload( true )");
                PlayerActivity.this.webView.reload();

                // } catch (final Exception e) {
                // VideoPlayerApp.getInstance().writeMessageToLog(
                // "exception while click on refresh:" + e.toString(), PlayerActivity.this.context);
                // VideoPlayerApp.getInstance().writeStackTraceToLog(e, PlayerActivity.this.context);
                // e.printStackTrace();
                // }
            }
        });
        this.webViewBack.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                // try {
                PlayerActivity.this.releaseWebView(PlayerActivity.this.webView);
                // } catch (final Exception e) {
                // VideoPlayerApp.getInstance().writeStackTraceToLog(e, PlayerActivity.this.context);
                // }
                PlayerActivity.this.linkCallToActionsLayout.setVisibility(View.GONE);
            }
        });
        this.leftArrow.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View arg0) {

                if (PlayerActivity.this.webView.canGoBack()) {
                    PlayerActivity.this.webView.goBack();
                    PlayerActivity.this.linkProgress.setVisibility(View.VISIBLE);
                    PlayerActivity.this.refresh.setVisibility(View.GONE);

                } else {
                    PlayerActivity.this.leftArrow.setVisibility(View.GONE);
                    PlayerActivity.this.leftArrowdisable.setVisibility(View.VISIBLE);
                }
            }
        });
        this.rightArrow.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View arg0) {

                if (PlayerActivity.this.webView.canGoForward()) {
                    PlayerActivity.this.webView.goForward();
                    PlayerActivity.this.linkProgress.setVisibility(View.VISIBLE);
                    PlayerActivity.this.refresh.setVisibility(View.GONE);

                } else {
                    PlayerActivity.this.rightArrow.setVisibility(View.GONE);
                    PlayerActivity.this.rightArrowdisable.setVisibility(View.VISIBLE);
                }
            }
        });
        // } catch (final Exception e) {
        // VideoPlayerApp.getInstance().writeMessageToLog("exception in link it action method:" + e.toString(),
        // this.context);
        // VideoPlayerApp.getInstance().writeStackTraceToLog(e, this.context);
        // }
    }

    private void linkItAction(final String url) {

        // try {
        this.linkCallToActionsLayout.setVisibility(View.GONE);
        this.tagLink.setText(url);
        if (!MainManager.getInstance().isFirstTimePlay() && !this.firstTimeLayout) {
            this.firstTimeLayout = true;
        }
        // } catch (final Exception e) {
        // VideoPlayerApp.getInstance().writeStackTraceToLog(e, this.context);
        // }

    }

    private void linkSearch() {

        // System.gc();
        if ((this.tagLink.getText() != null) && (this.tagLink.getText().toString().trim().length() > 0)) {
            String link = this.tagLink.getText().toString();
            link = "http://www.google.com/#q=" + link;
            this.linkCallToaction(link, false);
        } else {
            this.linkCallToaction("http://www.google.com", false);
        }
    }

    @JavascriptInterface
    private void loadLink(final String link, final WebView webView, final ProgressBar pd, final ImageView refresh,
            final boolean fefresh) {

        final WebSettings settings = webView.getSettings();
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(true);
        settings.setSupportZoom(true);

        webView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);

        pd.setVisibility(View.VISIBLE);
        refresh.setVisibility(View.GONE);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(final WebView view, final String url) {

                LOG.i("Finished loading URL: " + URLDecoder.decode(url));
                PlayerActivity.this.currentWebViewUrl = url;
                pd.setVisibility(View.GONE);
                refresh.setVisibility(View.VISIBLE);
                if (view.canGoBack()) {
                    PlayerActivity.this.leftArrow.setVisibility(View.VISIBLE);
                    PlayerActivity.this.leftArrowdisable.setVisibility(View.GONE);
                } else {
                    PlayerActivity.this.leftArrow.setVisibility(View.GONE);
                    PlayerActivity.this.leftArrowdisable.setVisibility(View.VISIBLE);

                }
                if (view.canGoForward()) {
                    PlayerActivity.this.rightArrow.setVisibility(View.VISIBLE);
                    PlayerActivity.this.rightArrowdisable.setVisibility(View.GONE);
                } else {
                    PlayerActivity.this.rightArrow.setVisibility(View.GONE);
                    PlayerActivity.this.rightArrowdisable.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onReceivedError(final WebView view, final int errorCode, final String description,
                    final String failingUrl) {

                LOG.e("Error: " + description);
                pd.setVisibility(View.GONE);
                refresh.setVisibility(View.VISIBLE);
                /*
                 * Toast.makeText(PlayerActivity.this, "Oh no! " + description, Toast.LENGTH_SHORT).show();
                 */
            }

            @Override
            public boolean shouldOverrideUrlLoading(final WebView view, final String url) {

                LOG.i("Processing webview url click...");
                pd.setVisibility(View.VISIBLE);
                refresh.setVisibility(View.GONE);
                // view.loadUrl(url);
                return false;
            }
        });
        webView.loadUrl(link);

    }

    private void onBlackClick() {

        this.colorView.setBackgroundResource(R.drawable.black_color_view);
        this.markerColor = Constant.BLACK;
        this.colorLayout.setVisibility(View.GONE);
    }

    private void onColorLayoutClick() {

        this.timeLayout.setVisibility(View.GONE);
        this.colorLayout.setVisibility(View.VISIBLE);
    }

    private void onCommentClick() {

        try {
            this.handlePause();
            if (this.settingLayout.isShown()) {
                this.visible = false;
                this.settingLayout.setVisibility(View.GONE);
            }
            final Intent commentintent = new Intent(this.context, SeeAllCommentsActivity.class);
            commentintent.putExtra(Constant.VIDEOID, this.videoId);
            commentintent.putExtra(Constant.USERID, this.userId);

            this.context.startActivity(commentintent);
        } catch (final ActivityNotFoundException e) {
            LOG.i("Player exception " + e.toString());
        }
    }

    private void onFirstClick() {

        this.time = 5;
        this.timeText.setText(R.string._5_sec);
        this.timeLayout.setVisibility(View.GONE);
    }

    private void onFourthClick() {

        this.time = 20;
        this.timeText.setText(R.string._20_sec);
        this.timeLayout.setVisibility(View.GONE);
    }

    private void onGreenClick() {

        this.markerColor = Constant.GREEN;
        this.colorLayout.setVisibility(View.GONE);
    }

    private void onLavendarClick() {

        this.colorView.setBackgroundResource(R.drawable.lavender_color_view);
        this.markerColor = Constant.LAVENDER;
        this.colorLayout.setVisibility(View.GONE);
    }

    private void onLikeClick() {

        this.handlePause();
        if (this.settingLayout.isShown()) {
            this.visible = false;
            this.settingLayout.setVisibility(View.GONE);
        }
        try {
            final VideoAsyncTask asyncTask = new VideoAsyncTask(this.context, Constant.LIKE,
                    PlayerActivity.getVedioLikeJsonReq(this.videoId), false);
            asyncTask.delegate = PlayerActivity.this;
            asyncTask.execute();
        } catch (final JSONException exception) {
            LOG.e(exception);
        }
    }

    /**
     * @param view
     */
    private void onMessgeClick(final View view) {

        final FacebookUser user = (FacebookUser) view.getTag();
        if (Constant.GOOGLE_PLUS.equalsIgnoreCase(Config.getSocialSite())) {
            final Intent shareIntent = new PlusShare.Builder(PlayerActivity.this).setType(Constant.TEXT_PLAIN)
                    .setText(VIDEO_URL).setContentUrl(Uri.parse(Constant.HTTP_WWW_TAGMOMENTS_COM)).getIntent();
            this.startActivityForResult(shareIntent, 0);
        } else if (Constant.FACEBOOK.equalsIgnoreCase(Config.getSocialSite())) {
            this.publishFeedDialog(user.getId(), Constant.EMPTY, Constant.EMPTY, true);
        }
    }

    private void onPlayerFacebookClick() {

        this.colorLayout.setVisibility(View.GONE);
        this.timeLayout.setVisibility(View.GONE);
        this.facebookAction();
    }

    private void onPlayerGoogleClick() {

        this.colorLayout.setVisibility(View.GONE);
        this.timeLayout.setVisibility(View.GONE);
        this.googleAction();
    }

    private void onPlayerTwitterClick() {

        this.colorLayout.setVisibility(View.GONE);
        this.timeLayout.setVisibility(View.GONE);
        this.twitterAction();
    }

    private void onPlayerWootagClick() {

        this.colorLayout.setVisibility(View.GOTagFu       this.timeLayout.setVisibility(View.GONE);
        this.wootagAction();
    }

    private void onRedClick() {

        TagFuolorView.setBackgroundResource(R.drawable.red_color_view);
        this.markerColor = Constant.RED;
        this.colorLayout.setVisibility(View.GONE);
    }

    private void onSecondClick() {

        this.time = 10;
        this.timeText.setText(R.string._10_sec);
        this.timeLayout.setVisibility(View.GONE);
    }

    private void onShareClick() {

        this.handlePause();
        if (this.settingLayout.isShown()) {
            this.visible = false;
            this.settingLayout.setVisibility(View.GONE);
        }
        final Intent intent = new Intent(PlayerActivity.this, ShareActivity.class);
        intent.putExtra(Constant.VIDEO, this.currentVideo);
        this.startActivity(intent);
    }

    private void onSkyblueClick() {

        this.colorView.setBackgroundResource(R.drawable.blue_color_view);
        this.markerColor = Constant.SKYBLUE;
        this.colorLayout.setVisibility(View.GONE);
    }

    private void onThirdClick() {

        this.time = 15;
        this.timeText.setText(R.string._15_sec);
        this.timeLayout.setVisibility(View.GONE);
    }

    private void onTimeTextClick() {

        this.colorLayout.setVisibility(View.GONE);
        this.timeLayout.setVisibility(View.VISIBLE);
    }

    private void onTwitterMessageClick() {

        Config.setTwitterRequestFor(Constant.TWITTER_DIRECT_MESSAGE);
        this.sendTwitterMessage(this.tagTwitterFriendId, this.friendInfoLayout);
    }

    private void onWhileClick() {

        this.colorView.setBackgroundResource(R.drawable.white_color_view);
        this.markerColor = Constant.WHITE;
        this.colorLayout.setVisibility(View.GONE);
    }

    private void onYelloClick() {

        this.colorView.setBackgroundResource(R.drawable.yellow_color_view);
        this.markerColor = Constant.YELLOW;
        this.colorLayout.setVisibility(View.GONE);
    }

    private void publish() {

        isTagMode = false;
        // markerColor=color;
        this.tName = this.tagName.getText().toString();
        this.tLink = this.tagLink.getText().toString();
        if (!this.tName.equalsIgnoreCase(Constant.EMPTY)) {
            this.publishComplete = true;
            this.tagLay.setVisibility(View.GONE);
            this.canceltagtool.setVisibility(View.GONE);
            this.taglogo.setVisibility(View.GONE);
            this.help.setVisibility(View.GONE);
            this.fbFriendsLayout.setVisibility(View.GONE);
            this.publishAndTagView.setVisibility(View.GONE);
            this.selectTagLocationLay.setVisibility(View.GONE);
            new SaveAsyncTask().execute();

        } else {
            Alerts.showExceptionOnly("Please enter tag name", this.context);
        }

    }

    private void releaseWebView(final WebView mWebView) {

        /*
         * if(mWebView != null){ mWebView.setTag(null); mWebView.clearHistory(); mWebView.removeAllViews();
         * mWebView.clearView(); mWebView.destroy(); mWebView = null; }
         */
    }

    private void removeTagFromPlayer(final TagInfo removeTag) {

        this.removeTagView(removeTag, 0);
        for (int i = 0; i < VideoPlayerApp.tagInfo.size(); i++) {
            final TagInfo tag = VideoPlayerApp.tagInfo.get(i);
            if (tag.getTagId() == removeTag.getTagId()) {
                VideoPlayerApp.tagInfo.remove(i);

            }
        }
    }

    /**
     * remove tag marker from slider once user delete the tag
     */
    private void removeTagMarker(final TagInfo tag) {

        if (handler != null) {
            handler.post(new Runnable() {

                @Override
                public void run() {

                    final int id = tag.getVideoPlaybackTime();
                    CustomButton tagView = (CustomButton) PlayerActivity.this.findViewById(id);
                    while ((tagView != null) && tagView.isShown()) {
                        PlayerActivity.this.seekbarLay.removeView(tagView);
                        tagView = (CustomButton) PlayerActivity.this.findViewById(id);
                    }
                }
            });
        }
    }

    private void requestForFacebookFeed(final List<Friend> friendList) {

        Config.setFacebookAccessToken(session.getAccessToken());

        Request.newMeRequest(session, new Request.GraphUserCallback() {

            @Override
            public void onCompleted(final GraphUser user, final Response response) {

                if (user != null) {
                    final Friend friend = new Friend();
                    friend.setFriendName(Constant.YOU);
                    friend.setFriendID(user.getId());
                    Config.setFacebookLoggedUserId(user.getId());
                    friend.setFriendImage(Constant.HTTPS_GRAPH_FACEBOOK_COM + user.getId() + Constant._PICTURE);
                    if (user.getLocation().getLocation().getCity() != null) {
                        friend.setLocation(user.getLocation().getLocation().getCity());
                    } else {
                        friend.setLocation(Constant.EMPTY);
                    }
                    friendList.add(0, friend);
                    VideoPlayerApp.getInstance().setFacebookFriendsList(friendList);
                    LOG.i("fb oncomplete frnds.size() " + friendList.size());
                    PlayerActivity.this.setFriendListAdapter(friendList);
                }
            }

        }).executeAsync();
    }

    private void saveGPlusLoggedInDetails(final Person currentPerson) {

        // try {
        final Friend friend = new Friend();
        if (currentPerson != null) {
            if (currentPerson.getId() != null) {
                friend.setFriendID(currentPerson.getId());
            }
            if (currentPerson.getCurrentLocation() != null) {
                friend.setLocation(currentPerson.getCurrentLocation());
            }
            friend.setFriendName(Constant.YOU);
            if (currentPerson.getImage().getUrl() != null) {
                friend.setFriendImage(currentPerson.getImage().getUrl());
            }
            VideoPlayerApp.getInstance().setGooglePlusLoggedInUser(friend);
        }
        // } catch (final Exception e) {
        // VideoPlayerApp.getInstance().writeStackTraceToLog(e, this.context);
        // e.printStackTrace();
        // }

    }

    private TagInfo saveTagDetails(final int serverTagid, final TagInfo tag) {//

        final TagInfo info = new TagInfo();
        info.setName(this.tName);
        if (this.uploadedVideo) {
            info.setServerVideoId(this.videoId);
            info.setClientVideoId(this.getString(R.string._0));
        } else {
            info.setServerVideoId(this.getString(R.string._0));
            info.setClientVideoId(this.videoId);
        }
        LOG.i("server tagid " + serverTagid);
        info.setServertagId(serverTagid);
        // info.setClientVideoId(currentVideoClientId);
        if (!this.tLink.equalsIgnoreCase(Constant.EMPTY)) {
            info.setLink(this.tLink);
        }
        info.setDisplayTime(String.valueOf(this.time));
        info.setColor(this.markerColor);
        if (!this.friendFacebookId.equalsIgnoreCase(Constant.EMPTY)) {
            info.setFbId(this.friendFacebookId);
        } else {
            if ((tag != null) && (tag.getFbId() != null) && !tag.getFbId().equalsIgnoreCase(Constant.EMPTY)) {
                info.setFbId(tag.getFbId());
            }
        }
        if (!this.twitterFriendId.equalsIgnoreCase(Constant.EMPTY)) {
            info.setTwId(this.twitterFriendId);
        } else {
            if ((tag != null) && (tag.getTwId() != null) && !tag.getTwId().equalsIgnoreCase(Constant.EMPTY)) {
                info.setTwId(tag.getTwId());
            }
        }
        if (!this.wooTagId.equalsIgnoreCase(Constant.EMPTY)) {
            info.setTagFuId(this.wooTagId);
        } else {
            if ((tag != TagFu&& (tag.gTagFuagId() != null) && !tag.getWooTagId().equalsIgnoreCase(Constant.TagFu) {
                info.sTagFuagId(tag.getWooTagId());
            }
        }
        if (!thisTagFuFriendID.eqTagFunoreCase(Constant.EMPTY)) {
            info.setgPlusId(this.gPlusFriendID);
        } else {
            if ((tag != null) && (tag.getgPlusId() != null) && !tag.getgPlusId().equalsIgnoreCase(Constant.EMPTY)) {
                info.setgPlusId(tag.getgPlusId());
            }
        }
        info.setVideoPlaybackTime(this.currentPosition);

        // %logic
        final float x = (changingX / this.screenWidth) * 100;
        final float y = (changingY / this.screenHeight) * 100;

        info.setTagX(x);
        info.setTagY(y);

        // info.setTagX(changingX);
        // info.setTagY(changingY);
        info.setScreenWidth(this.screenWidth);
        info.setScreenHeight(this.screenHeight);
        info.setVideoHeight(this.videoHeight);
        info.setVideoWidth(this.videoWidth);
        info.setScreenResX(0);
        info.setScreenResY(0);
        info.setVideoResX(0);
        info.setVideoResY(0);
        info.setUploadStatus(0);
        info.setTagTimeOutFrame(this.currentPosition + (1000 * this.time));
        LOG.i("tag info save new tag id at save time" + info.getTagId() + "time out " + info.getTagTimeOutFrame());
        if (Config.getProductDetails() != null) {
            if (Config.getProductDetails().getCurrencyCategory() != null) {
                info.setProductCurrency(Config.getProductDetails().getCurrencyCategory());
            } else if ((tag != null) && (tag.getProductCurrency() != null)) {
                info.setProductCurrency(tag.getProductCurrency());
            }
            if (Config.getProductDetails().getProductCategory() != null) {
                info.setProductCategory(Config.getProductDetails().getProductCategory());
            } else if ((tag != null) && (tag.getProductCategory() != null)) {
                info.setProductCategory(tag.getProductCategory());
            }
            if (Config.getProductDetails().getProductDescription() != null) {
                info.setProductDescription(Config.getProductDetails().getProductDescription());
            } else if ((tag != null) && (tag.getProductDescription() != null)) {
                info.setProductDescription(tag.getProductDescription());
            }
            if (Config.getProductDetails().getProductLink() != null) {
                info.setProductLink(Config.getProductDetails().getProductLink());
            } else if ((tag != null) && (tag.getProductLink() != null)) {
                info.setProductLink(tag.getProductLink());
            }
            if (Config.getProductDetails().getProductPrice() != null) {
                info.setProductPrice(Config.getProductDetails().getProductPrice());
            } else if ((tag != null) && (tag.getProductPrice() != null)) {
                info.setProductPrice(tag.getProductPrice());
            }
            if (Config.getProductDetails().getSold() != null) {
                info.setProductSold(Config.getProductDetails().getSold());
            } else if ((tag != null) && (tag.getProductSold() != null)) {
                info.setProductSold(tag.getProductSold());
            }
            if (Config.getProductDetails().getProductName() != null) {
                info.setProductName(Config.getProductDetails().getProductName());
            } else if ((tag != null) && (tag.getProductName() != null)) {
                info.setProductName(tag.getProductName());
            }
        }
        return info;
    }

    private void sendTweet(final FacebookUser user, final View view) {

        view.setVisibility(View.GONE);
        final LinearLayout sendtweetlayout = (LinearLayout) this.findViewById(R.id.sendtweetLayout);
        final EditText tweetEditText = (EditText) this.findViewById(R.id.tweeteditview);
        final Button sendTweet = (Button) this.findViewById(R.id.sendtweet);
        final Button cancelTweet = (Button) this.findViewById(R.id.canceltweet);
        sendtweetlayout.setVisibility(View.VISIBLE);
        sendTweet.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if ((tweetEditText.getText().toString() != null)
                        && !tweetEditText.getText().toString().equalsIgnoreCase(Constant.EMPTY)) {
                    final TwitterAsync asyncTask = new TwitterAsync(Constant.EMPTY, PlayerActivity.this.context,
                            PlayerActivity.this.tagTwitterFriendId, Constant.TWITTER_TWEET, tweetEditText.getText()
                                    .toString(), null, Constant.WRITE_ON_WALL);
                    asyncTask.delegate = PlayerActivity.this;
                    asyncTask.execute();
                    view.setVisibility(View.VISIBLE);
                    sendtweetlayout.setVisibility(View.GONE);
                } else {
                    Alerts.showInfoOnly("should not be sent empty twwet!", PlayerActivity.this.context);
                }

            }
        });
        cancelTweet.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                sendtweetlayout.setVisibility(View.GONE);
                view.setVisibility(View.VISIBLE);
            }
        });

    }

    /**
     * displaying a custom layout to send a tweet to twitter friend
     */
    private void sendTwitterMessage(final String id, final View view) {

        final LinearLayout twitterMessageLayout = (LinearLayout) this.findViewById(R.id.twittermessageLayout);
        final EditText msgEditText = (EditText) this.findViewById(R.id.tmsg);
        final Button twitterUserMessageButton = (Button) this.findViewById(R.id.sendmsg);
        final Button cancelmsg = (Button) this.findViewById(R.id.cancelmsg);
        view.setVisibility(View.GONE);
        twitterMessageLayout.setVisibility(View.VISIBLE);

        cancelmsg.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                view.setVisibility(View.VISIBLE);
                twitterMessageLayout.setVisibility(View.GONE);

            }
        });
        twitterUserMessageButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                Config.setTwitterRequestFor(Constant.TWITTER_DIRECT_MESSAGE);
                final String message = msgEditText.getText().toString();
                twitterMessageLayout.setVisibility(View.GONE);
                if ((message != null) && (message.length() > 0)) {
                    final TwitterAsync asyncTask = new TwitterAsync(Constant.EMPTY, PlayerActivity.this.context, id,
                            Constant.TWITTER_DIRECT_MESSAGE, message, null, Constant.EMPTY);
                    asyncTask.delegate = PlayerActivity.this;
                    asyncTask.execute();
                } else {
                    Alerts.showExceptionOnly("Please enter message", PlayerActivity.this.context);
                }

                view.setVisibility(View.VISIBLE);
                twitterMessageLayout.setVisibility(View.GONE);

            }
        });

    }

    private void setBg(final RelativeLayout viewBg2, final int screenType) {

        if (screenType == 1) {
            this.textView.setText(R.string.create_a_tag_anytime_during_the_video_click_on_the_tag_tool);
            this.updateBreadscrumImage(screenType);
        } else if (screenType == 2) {
            this.textView
                    .setText("Set the tag marker with touch of your finger on people, place or product inside your video");
            this.updateBreadscrumImage(screenType);
        } else if (screenType == 3) {
            this.textView.setText("Say something about the tagged moment in 40 char");
            this.updateBreadscrumImage(screenType);
        } else if (screenType == 4) {
            this.textView.setText("Tag yourself or your connections from your favorite social networks");
            this.updateBreadscrumImage(screenType);
        } else if (screenType == 5) {
            this.textView.setText("Link your blog, site or product page which you want viewers to interact");
            this.updateBreadscrumImage(screenType);
        } else if (screenType == 6) {
            this.updateBreadscrumImage(screenType);
            this.textView.setText("Click publish to publish your tags");
        }
    }

    private void setFacebookFriendList() {

        Config.setFacebookAccessToken(session.getAccessToken());
        final FacebookFriendsAsync async = new FacebookFriendsAsync(this.context, Constant.FRIEND_LIST, Constant.EMPTY);
        async.delegate = PlayerActivity.this;
        async.execute();
    }

    /**
     * set the friend list to adapter and giving option to tag a friend
     */
    private void setFriendListAdapter(final List<Friend> friendlist) {

        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {

                if (PlayerActivity.this.tagEditMode && (editTagId != 0)) {
                    if (Constant.GOOGLE_PLUS.equalsIgnoreCase(Config.getSocialSite())) {
                        if ((gplusTaggedUserId != null) && !gplusTaggedUserId.equalsIgnoreCase(Constant.EMPTY)
                                && (VideoPlayerApp.getInstance().getGoogleFriendList() != null)) {
                            for (int i = 0; i < VideoPlayerApp.getInstance().getGoogleFriendList().size(); i++) {
                                final Friend frnd = VideoPlayerApp.getInstance().getGoogleFriendList().get(i);
                                if (gplusTaggedUserId.equalsIgnoreCase(frnd.getFriendId())) {
                                    PlayerActivity.this.updateTaggedUserLayout.setVisibility(View.VISIBLE);
                                    PlayerActivity.this.updateTaggedUserDelteButton.setTag(frnd);
                                    PlayerActivity.this.upgateTaggedUserName.setText(frnd.getFriendName());
                                    frnd.setTaggedUser(true);
                                    // updateTaggedUserImageView.setImageResource(resId);
                                    if ((frnd.getFriendImage() != null)
                                            && !frnd.getFriendImage().equalsIgnoreCase(Constant.EMPTY)) {
                                        Image.displayImage(frnd.getFriendImage(),
                                                (Activity) PlayerActivity.this.context,
                                                PlayerActivity.this.updateTaggedUserImageView, 0);
                                    }
                                    break;
                                }
                            }
                        }
                    } else if (Constant.FACEBOOK.equalsIgnoreCase(Config.getSocialSite())) {
                        if ((fbTaggedUserId != null) && !fbTaggedUserId.equalsIgnoreCase(Constant.EMPTY)
                                && (VideoPlayerApp.getInstance().getFbFriendsList() != null)) {
                            for (int i = 0; i < VideoPlayerApp.getInstance().getFbFriendsList().size(); i++) {
                                final Friend frnd = VideoPlayerApp.getInstance().getFbFriendsList().get(i);
                                if (fbTaggedUserId.equalsIgnoreCase(frnd.getFriendId())) {
                                    PlayerActivity.this.updateTaggedUserLayout.setVisibility(View.VISIBLE);
                                    PlayerActivity.this.updateTaggedUserDelteButton.setTag(frnd);
                                    frnd.setTaggedUser(true);
                                    PlayerActivity.this.upgateTaggedUserName.setText(frnd.getFriendName());
                                    if ((frnd.getFriendImage() != null)
                                            && !frnd.getFriendImage().equalsIgnoreCase(Constant.EMPTY)) {
                                        Image.displayImage(frnd.getFriendImage(),
                                                (Activity) PlayerActivity.this.context,
                                                PlayerActivity.this.updateTaggedUserImageView, 0);
                                    }
                                    break;
                                }
                            }
                        }
                    } else if (Constant.TWITTER.equalsIgnoreCase(Config.getSocialSite())) {
                        if ((twitterTaggedUserId != null) && !twitterTaggedUserId.equalsIgnoreCase(Constant.EMPTY)
                                && (VideoPlayerApp.getInstance().getTwitterFriendList() != null)) {
                            for (int i = 0; i < VideoPlayerApp.getInstance().getTwitterFriendList().size(); i++) {
                                final Friend frnd = VideoPlayerApp.getInstance().getTwitterFriendList().get(i);
                                if (twitterTaggedUserId.equalsIgnoreCase(frnd.getFriendId())) {
                                    PlayerActivity.this.updateTaggedUserLayout.setVisibility(View.VISIBLE);
                                    PlayerActivity.this.updateTaggedUserDelteButton.setTag(frnd);
                                    frnd.setTaggedUser(true);
                                    PlayerActivity.this.upgateTaggedUserName.setText(frnd.getFriendName());
                                    if ((frnd.getFriendImage() != null)
                                            && !frnd.getFriendImage().equalsIgnoreCase(Constant.EMPTY)) {
                                        Image.displayImage(frnd.getFriendImage(),
                                                (Activity) PlayerActivity.this.context,
                                                PlayerActivity.this.updateTaggedUserImageView, 0);
                                    }
                                    break;
                                }
                            }
                        }
                    } else if (Constant.WOOTAG.equalsIgnoreCase(Config.getSocialSite()) && (wootagTaggedUserId !TagFu)
                            && !wootagTaggedUTagFuequalsIgnoreCase(Constant.EMPTY)
                      TagFu&& (VideoPlayerApp.getInstance().getWootagFriendsList() != null)) {
                        for (int i = 0; i <TagFuPlayerApp.getInstance().getWootagFriendsList().size(); i++) {
                            final Friend TagFu VideoPlayerApp.getInstance().getWootagFriendsList().get(i);
                            if (wootagTaggedUserITagFulsIgnoreCase(frnd.getFriendId())) {
                   TagFu       PlayerActivity.this.updateTaggedUserLayout.setVisibility(View.VISIBLE);
                                PlayerActivity.this.updateTaggedUserDelteButton.setTag(frnd);
                                PlayerActivity.this.upgateTaggedUserName.setText(frnd.getFriendName());
                                frnd.setTaggedUser(true);
                                if ((frnd.getFriendImage() != null)
                                        && !frnd.getFriendImage().equalsIgnoreCase(Constant.EMPTY)) {
                                    Image.displayImage(frnd.getFriendImage(),
                                            (Activity) PlayerActivity.this.context,
                                            PlayerActivity.this.updateTaggedUserImageView, 0);
                                }
                                break;
                            }
                        }
                    }

                    PlayerActivity.this.updateTaggedUserDelteButton.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(final View v) {

                            TagInfo currentTag = null;
                            if ((VideoPlayerApp.tagInfo != null) && PlayerActivity.this.tagEditMode && (editTagId != 0)) {
                                for (int i = 0; i < VideoPlayerApp.tagInfo.size(); i++) {
                                    final TagInfo tag = VideoPlayerApp.tagInfo.get(i);
                                    if (tag.getTagId() == editTagId) {
                                        currentTag = tag;
                                        break;
                                    }
                                }
                            }

                            if (currentTag != null) {

                                final Friend taggedUserObj = (Friend) v.getTag();
                                final String taggedaggedUserId = taggedUserObj.getFriendId();
                                if (Config.getSocialSite().equalsIgnoreCase(Constant.GOOGLE_PLUS)) {
                                    if ((taggedaggedUserId != null)
                                            && !taggedaggedUserId.equalsIgnoreCase(Constant.EMPTY)) {
                                        for (int i = 0; i < VideoPlayerApp.getInstance().getGoogleFriendList().size(); i++) {
                                            final Friend frnd = VideoPlayerApp.getInstance().getGoogleFriendList()
                                                    .get(i);
                                            if ((frnd != null) && (frnd.getFriendId() != null)
                                                    && taggedaggedUserId.equalsIgnoreCase(frnd.getFriendId())) {
                                                frnd.setTaggedUser(false);
                                                currentTag.setgPlusId(Constant.EMPTY);
                                                PlayerActivity.this.updateTaggedUserLayout.setVisibility(View.GONE);
                                                PlayerActivity.this.facebookFriendsList.notifyDataSetChanged();
                                                break;
                                            }
                                        }
                                    }
                                } else if (Config.getSocialSite().equalsIgnoreCase(Constant.FACEBOOK)) {
                                    if ((taggedaggedUserId != null)
                                            && !taggedaggedUserId.equalsIgnoreCase(Constant.EMPTY)) {
                                        for (int i = 0; i < VideoPlayerApp.getInstance().getFbFriendsList().size(); i++) {
                                            final Friend frnd = VideoPlayerApp.getInstance().getFbFriendsList().get(i);
                                            if (taggedaggedUserId.equalsIgnoreCase(frnd.getFriendId())) {
                                                frnd.setTaggedUser(false);
                                                currentTag.setFbId(Constant.EMPTY);
                                                PlayerActivity.this.updateTaggedUserLayout.setVisibility(View.GONE);
                                                PlayerActivity.this.facebookFriendsList.notifyDataSetChanged();
                                                break;
                                            }
                                        }
                                    }
                                } else if (Config.getSocialSite().equalsIgnoreCase(Constant.TWITTER)) {
                                    if ((taggedaggedUserId != null)
                                            && !taggedaggedUserId.equalsIgnoreCase(Constant.EMPTY)) {
                                        for (int i = 0; i < VideoPlayerApp.getInstance().getTwitterFriendList().size(); i++) {
                                            final Friend frnd = VideoPlayerApp.getInstance().getTwitterFriendList()
                                                    .get(i);
                                            if (taggedaggedUserId.equalsIgnoreCase(frnd.getFriendId())) {
                                                frnd.setTaggedUser(false);
                                                currentTag.setTwId(Constant.EMPTY);
                                                PlayerActivity.this.updateTaggedUserLayout.setVisibility(View.GONE);
                                                PlayerActivity.this.facebookFriendsList.notifyDataSetChanged();
                                                break;
                                            }
                                        }
                                    }
                                } else if (Config.getSocialSite().equalsIgnoreCase(Constant.WOOTAG)
                                        && (taggedaggedUserId != null)
TagFu                                  && !taggedaggedUserId.equalsIgnoreCase(Constant.EMPTY)) {
                                    for (int i = 0; i < VideoPlayerApp.getInstance().getWootagFriendsList().size(); i++) {
                                        finalTagFud frnd = VideoPlayerApp.getInstance().getWootagFriendsList().get(i);
                                        if (taggedaggTagFuId.equalsIgnoreCase(frnd.getFriendId())) {
                                            frnd.setTaggedUser(false);
                                            currentTag.setWooTagId(Constant.EMPTY);
                                            PlayerActiviTagFus.updateTaggedUserLayout.setVisibility(View.GONE);
                                            PlayerActivity.this.facebookFriendsList.notifyDataSetChanged();
                                            break;
                                        }
                                    }
                                }
                            }

                        }
                    });
                }
                fbhandler.post(new Runnable() {

                    @Override
                    public void run() {

                        PlayerActivity.this.fbfrndList.setVisibility(View.VISIBLE);
                    }
                });

                if ((friendlist != null) && (friendlist.size() > 0)) {
                    PlayerActivity.this.adapterList.clear();
                    PlayerActivity.this.adapterList.addAll(friendlist);
                }
                PlayerActivity.this.facebookFriendsList = new FacebookFriendsListAdapter(PlayerActivity.this.context,
                        R.layout.facebook_user, PlayerActivity.this.adapterList, Config.getSocialSite(), false, true);
                PlayerActivity.this.fbfrndList.setAdapter(PlayerActivity.this.facebookFriendsList);
                PlayerActivity.this.facebookFriendsList.notifyDataSetChanged();
                PlayerActivity.this.fbfrndList.setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(final AdapterView<?> arg0, final View arg1, final int arg2, final long arg3) {

                        // try {
                        final Friend friendItem = PlayerActivity.this.adapterList.get(arg2);
                        if ((friendItem != null) && (friendItem.getFriendId() != null)) {
                            LOG.i("friend details name " + friendItem.getFriendName() + "id "
                                    + friendItem.getFriendId());

                            if (Config.getSocialSite().equalsIgnoreCase(Constant.GOOGLE_PLUS)) {
                                PlayerActivity.this.gPlusFriendID = friendItem.getFriendId();
                                PlayerActivity.this.google.setBackgroundResource(R.drawable.tag_googleplus_f);
                            } else if (Config.getSocialSite().equalsIgnoreCase(Constant.FACEBOOK)) {
                                PlayerActivity.this.friendFacebookId = friendItem.getFriendId();
                                PlayerActivity.this.facebook.setBackgroundResource(R.drawable.tag_facebook_f);
                            } else if (Config.getSocialSite().equalsIgnoreCase(Constant.TWITTER)) {
                                PlayerActivity.this.twitterFriendId = friendItem.getFriendId();
                                PlayerActivity.this.twitter.setBackgroundResource(R.drawable.tag_twitter_f);
                            } else if (Config.getSocialSite().equalsIgnoreCase(Constant.WOOTAG)) {
                                PlayerActivity.this.wooTagId = friendIteTagFuriendId();
                                PlayerActivity.TagFuootag.setBackgroundResource(R.drawable.tag_wootag_f);
                            }
TagFu                      PlayerActivity.thTagFuriendsLayout.setVisibility(View.GONE);
                            PlayerActivity.this.tagLay.setVisibility(View.VISIBLE);
                            PlayerActivity.this.canceltagtool.setVisibility(View.VISIBLE);
                            PlayerActivity.this.taglogo.setVisibility(View.VISIBLE);
                            PlayerActivity.this.help.setVisibility(View.VISIBLE);
                        }
                    }

                });
            }
        });
    }

    private void setProductDeatils(final TagInfo product) {

        Config.setProductDetails(new ProductDetails());
        if (product.getProductName() != null) {
            Config.getProductDetails().setProductName(product.getProductName());
        }
        if (product.getProductPrice() != null) {
            Config.getProductDetails().setProductPrice(product.getProductPrice());
        }
        Config.getProductDetails().setProductCategory(product.getProductCategory());
        Config.getProductDetails().setCurrencyCategory(product.getProductCurrency());
        if (product.getProductDescription() != null) {
            Config.getProductDetails().setProductDescription(product.getProductDescription());
        }
    }

    private synchronized void setProgress() {

        this.currentPosition = this.myVideoView.getCurrentPosition();
        this.seekBar.setProgress(this.currentPosition);
        this.setTime(this.myVideoView.getDuration(), this.currentPosition);
    }

    /**
     * filtering the friends list based onn text entered in serach field while tagging a friend
     */
    private void setSearchAdapter(final List<Friend> frndList, final String text) {

        if ((text != null) && (frndList != null) && (frndList.size() > 0)) {
            this.clearList();
            new Thread(new Runnable() {

                @Override
                public void run() {

                    PlayerActivity.this.filterdList = new ArrayList<Friend>();
                    for (int i = 0; i < frndList.size(); i++) {
                        final Friend frnd = frndList.get(i);
                        if ((frnd != null)
                                && (frnd.getFriendName() != null)
                                && (frnd.getFriendName().toLowerCase(Locale.getDefault())
                                        .indexOf(text.toLowerCase(Locale.getDefault())) != -1)) {
                            PlayerActivity.this.filterdList.add(frnd);
                        }
                    }
                }
            }).start();
            try {
                Thread.sleep(20);
            } catch (final InterruptedException e) {
                Alerts.showAlertOnly("Exception", e.toString(), this.context);
            }
            if ((this.filterdList != null) && (this.filterdList.size() > 0)) {
                this.setFriendListAdapter(this.filterdList);
            }

        }
    }

    private void setTime(final int totalDuration, final int currentPosition) {

        if (handler != null) {
            handler.post(new Runnable() {

                @Override
                public void run() {

                    final String totalTime = Util.getPlayerTimeString(totalDuration);
                    final String currentTime = Util.getPlayerTimeString(currentPosition);
                    PlayerActivity.this.timeView.setText(currentTime + "/" + totalTime);
                }
            });
        }
    }

    /**
     * showing a twiiter user info when twitter call to action event occurred of particular tag
     */
    private void setTwitterFriendInfo(final FacebookUser user) {

        this.taggedUserLayout = (LinearLayout) this.findViewById(R.id.twitterfriendcalltoaction);
        this.taggedUserLayout.setVisibility(View.VISIBLE);
        final TextView fbFriendName = (TextView) this.findViewById(R.id.twitterfriendname);
        final ImageView fbFriendImg = (ImageView) this.findViewById(R.id.twitterfriendImage);
        final Button birthday = (Button) this.findViewById(R.id.twitterbirthdaybutton);
        final Button writeOnWall = (Button) this.findViewById(R.id.tweet);
        final Button messageToTwitterFriend = (Button) this.findViewById(R.id.messagetotwitterfriend);
        final Button exit = (Button) this.findViewById(R.id.exittwitterfriendinfo);
        final TextView latestTweetTextView = (TextView) this.findViewById(R.id.latesttweetText);
        final TextView twitterLastUpdateTimeText = (TextView) this.findViewById(R.id.twitterlastupdatetimeText);
        final TextView location = (TextView) this.findViewById(R.id.twitteruserlivesText);

        latestTweetTextView.setText(user.getStatusUpdate() != null ? user.getStatusUpdate() : Constant.EMPTY);
        twitterLastUpdateTimeText.setText(user.getLastUpdate() != null ? user.getLastUpdate() : Constant.EMPTY);
        location.setText(user.getCurrentPlace() != null ? user.getCurrentPlace() : Constant.EMPTY);
        fbFriendName.setText(user.getUserName() != null ? user.getUserName() : Constant.EMPTY);
        fbFriendName.setText(user.getUserName() != null ? user.getUserName() : Constant.EMPTY);

        this.setProfile(user, fbFriendImg);
        birthday.setVisibility(View.GONE);

        exit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                PlayerActivity.this.taggedUserLayout.setVisibility(View.GONE);
            }
        });
        writeOnWall.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                PlayerActivity.this.sendTweet(user, PlayerActivity.this.taggedUserLayout);

            }
        });
        birthday.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                Config.setTwitterRequestFor(Constant.TWITTER_DIRECT_MESSAGE);
                PlayerActivity.this.taggedUserLayout.setVisibility(View.GONE);
                PlayerActivity.this.sendTwitterMessage(user.getId(), PlayerActivity.this.taggedUserLayout);
            }
        });
        messageToTwitterFriend.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                Config.setTwitterRequestFor(Constant.TWITTER_DIRECT_MESSAGE);
                PlayerActivity.this.taggedUserLayout.setVisibility(View.GONE);
                PlayerActivity.this.sendTwitterMessage(user.getId(), PlayerActivity.this.taggedUserLayout);
            }
        });

    }

    /** This callback will be invoked when the file is ready to play */

    private void setVideoViewLayout() {

    }

    private void setVisibilty() {

        if (handler != null) {
            handler.post(new Runnable() {

                @Override
                public void run() {

                    if (PlayerActivity.this.controlsVisible) {
                        PlayerActivity.this.noOfSec = PlayerActivity.this.noOfSec + 1;
                        if ((PlayerActivity.this.noOfSec >= 500) && !PlayerActivity.this.visible) {
                            PlayerActivity.this.controlsVisible = false;
                            PlayerActivity.this.noOfSec = 0;
                            PlayerActivity.this.tagOptionsLayout.setVisibility(View.GONE);
                            PlayerActivity.this.playerControlLayout.setVisibility(View.INVISIBLE);
                            PlayerActivity.this.videoInfoLayout.setVisibility(View.GONE);
                        }
                    }
                }
            });
        }
    }

    private void setWootagFriendList() {

        final WootagFriendsAsync asyncTask = new WootagFriendsAsyTagFue, this.context);
        asyncTagFuelegate = PlayerActivity.this;TagFu   asyncTask.execute();
    }

    private void showHelpTagTool() {

        this.introScrren = 1;
        this.disableTagToolViews();
        this.helpInstructionsView.setVisibility(View.VISIBLE);
        this.instructionsView.setVisibility(View.VISIBLE);
        this.tagArrow.setVisibility(View.INVISIBLE);
        this.linkArrow.setVisibility(View.INVISIBLE);
        this.tagExpressionArrow.setVisibility(View.INVISIBLE);
        this.publishArrow.setVisibility(View.INVISIBLE);
        this.fbArrow.setVisibility(View.INVISIBLE);

        this.textView.setText(R.string.create_a_tag_anytime_during_the_video_click_on_the_tag_tool);
        this.updateBreadscrumImage(1);

        this.closeHelptool.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                PlayerActivity.this.controlsEnabled = true;
                if (Config.getUserId().equalsIgnoreCase(PlayerActivity.this.currentVideo.getUserId())
                        && !MainManager.getInstance().isFirstTimePlay()) {
                    PlayerActivity.this.publishAndTagView.setVisibility(View.GONE);
                    PlayerActivity.this.tagLay.setVisibility(View.GONE);
                    PlayerActivity.this.canceltagtool.setVisibility(View.GONE);
                    PlayerActivity.this.taglogo.setVisibility(View.GONE);
                    PlayerActivity.this.help.setVisibility(View.GONE);
                    PlayerActivity.this.fbFriendsLayout.setVisibility(View.GONE);
                    PlayerActivity.this.publishAndTagView.setVisibility(View.GONE);
                    PlayerActivity.this.selectTagLocationLay.setVisibility(View.GONE);
                    MainManager.getInstance().setISFirstTimePlay(true);
                    PlayerActivity.this.myVideoView.start();
                    PlayerActivity.this.playbutton.setBackgroundResource(R.drawable.pause1_f);
                }

                PlayerActivity.this.closeHelpTool();

            }
        });

        this.instructionsView.setOnTouchListener(new OnInstructionSwipeListener(PlayerActivity.this) {

            @Override
            public void onSwipeBottom() {

            }

            @Override
            public void onSwipeLeft() {

                if (PlayerActivity.this.introScrren < 6) {
                    PlayerActivity.this.introScrren = PlayerActivity.this.introScrren + 1;
                    PlayerActivity.this.setBg(PlayerActivity.this.instructionsView, PlayerActivity.this.introScrren);
                }
            }

            @Override
            public void onSwipeRight() {

                if (PlayerActivity.this.introScrren > 1) {
                    PlayerActivity.this.introScrren = PlayerActivity.this.introScrren - 1;
                    PlayerActivity.this.setBg(PlayerActivity.this.instructionsView, PlayerActivity.this.introScrren);
                }
            }

            @Override
            public void onSwipeTop() {

            }

        });
        this.firstTagInstruction.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                PlayerActivity.this.introScrren = 1;
                PlayerActivity.this.setBg(PlayerActivity.this.instructionsView, PlayerActivity.this.introScrren);

            }
        });
        this.secondTagInstruction.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                PlayerActivity.this.introScrren = 2;
                PlayerActivity.this.setBg(PlayerActivity.this.instructionsView, PlayerActivity.this.introScrren);

            }
        });
        this.sixthTagInstruction.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                PlayerActivity.this.introScrren = 6;
                PlayerActivity.this.setBg(PlayerActivity.this.instructionsView, PlayerActivity.this.introScrren);

            }
        });
        this.thirdTagInstruction.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                PlayerActivity.this.introScrren = 3;
                PlayerActivity.this.setBg(PlayerActivity.this.instructionsView, PlayerActivity.this.introScrren);

            }
        });
        this.fourthTagInstruction.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                PlayerActivity.this.introScrren = 4;
                PlayerActivity.this.setBg(PlayerActivity.this.instructionsView, PlayerActivity.this.introScrren);

            }
        });
        this.fifthTagInstruction.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                PlayerActivity.this.introScrren = 5;
                PlayerActivity.this.setBg(PlayerActivity.this.instructionsView, PlayerActivity.this.introScrren);

            }
        });

    }

    private void showTag(final TagInfo tag) {

        final LinearLayout view = this.getTagView(tag, 0);
        if (view != null) {
            this.videoLayout.addView(view);
            this.displayTagMarker(tag);
        }
    }

    private void showTagTool() {

        this.tagVisible = false;
        this.canceltag.setVisibility(View.GONE);
        this.submit.setVisibility(View.GONE);
        this.tagLay.setVisibility(View.VISIBLE);
        this.canceltagtool.setVisibility(View.VISIBLE);
        this.taglogo.setVisibility(View.VISIBLE);
        this.help.setVisibility(View.VISIBLE);
        this.publishStart = true;
        this.time = 5;
        this.markerColor = Constant.SKYBLUE;
        this.facebook.setBackgroundResource(R.drawable.tag_facebook);
        this.twitter.setBackgroundResource(R.drawable.tag_twitter);
        this.google.setBackgroundResource(R.drawable.tag_googleplus);
        this.wootag.setBackgroundResource(R.drawable.tag_wootag);
    }

    private void twitterActionTagFu        Config.setSocialSite(Constant.TTagFu);

        this.tagLay.setVisibility(View.GONE);
        this.canceltagtool.setVisibility(View.GONE);
        this.taglogo.setVisibility(View.GONE);
        this.fbsearch.setText(Constant.EMPTY);
        this.searchIcon.setImageResource(R.drawable.sharetwitter);
        this.fbFriendsLayout.setVisibility(View.VISIBLE);

        if (changingX < this.widthX) {
            final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) this.fbFriendsLayout
                    .getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            this.fbFriendsLayout.setLayoutParams(params);
        }
        if ((VideoPlayerApp.getInstance().getTwitterFriendList() != null)
                && (VideoPlayerApp.getInstance().getTwitterFriendList().size() > 0)) {
            friendList = VideoPlayerApp.getInstance().getTwitterFriendList();
            this.setFriendListAdapter(friendList);
        } else {
            this.fbfrndList.setVisibility(View.GONE);
            if (TwitterUtils.isAuthenticated(this.context)) {
                final TwitterAsync asyncTask = new TwitterAsync(Constant.EMPTY, this.context, Constant.EMPTY,
                        Constant.EMPTY, Constant.EMPTY, null, Constant.EMPTY);
                asyncTask.delegate = PlayerActivity.this;
                asyncTask.execute();
            } else {
                LOG.i("Navigate to prepare");
                TwitterUtils.navigateToPrepare(this.context);
            }
        }

    }

    private void twitterFriendInfo(final String twID) {

        Config.setTwitterRequestFor(Constant.TWITTER_FRIEND_INFO);
        if (TwitterUtils.isAuthenticated(this.context)) {
            final TwitterAsync asyncTask = new TwitterAsync(Constant.EMPTY, this.context, twID,
                    Constant.TWITTER_FRIEND_INFO, Constant.EMPTY, null, Constant.EMPTY);
            asyncTask.delegate = PlayerActivity.this;
            asyncTask.execute();
        } else {
            TwitterUtils.navigateToPrepare(this.context);
        }

    }

    private void update() {

        LOG.i("update is edit mosde" + this.tagEditMode + "  edit tag id " + editTagId);
        if (this.tagEditMode && (editTagId != 0)) {
            this.updateStart = false;
            isTagMode = false;
            isTagUpdate = false;
            this.tName = this.tagName.getText().toString();
            this.tLink = this.tagLink.getText().toString();
            if (!this.tName.equalsIgnoreCase(Constant.EMPTY)) {
                this.publishComplete = true;
                this.tagLay.setVisibility(View.GONE);
                this.canceltagtool.setVisibility(View.GONE);
                this.taglogo.setVisibility(View.GONE);
                this.help.setVisibility(View.GONE);
                this.publishAndTagView.setVisibility(View.GONE);
                this.selectTagLocationLay.setVisibility(View.GONE);
                this.update.setVisibility(View.GONE);
                this.reset.setVisibility(View.VISIBLE);
                this.publish.setVisibility(View.VISIBLE);
                if (VideoPlayerApp.tagInfo != null) {
                    for (int i = 0; i < VideoPlayerApp.tagInfo.size(); i++) {
                        final TagInfo tag = VideoPlayerApp.tagInfo.get(i);
                        if (tag.getTagId() == editTagId) {
                            VideoPlayerApp.tagInfo.remove(i);
                            this.removeTagView(tag, i);
                            new UpdateAsyncTask(tag).execute();
                        }
                    }

                }

            } else {
                Alerts.showExceptionOnly("Please enter tag name", this.context);
            }

        } else {
            Alerts.showAlertOnly(Constant.ALERT, "tag id not available", this.context);
        }
    }

    /**
     * while showing help tool enable the buttons to navigate other views
     */
    private void updateBreadscrumImage(final int screenType) {

        this.tagArrow.setVisibility(View.INVISIBLE);
        this.linkArrow.setVisibility(View.INVISIBLE);
        this.tagExpressionArrow.setVisibility(View.INVISIBLE);
        this.publishArrow.setVisibility(View.INVISIBLE);
        this.fbArrow.setVisibility(View.INVISIBLE);

        this.firstTagInstruction.setImageResource(R.drawable.breadcrumb_disable);
        this.secondTagInstruction.setImageResource(R.drawable.breadcrumb_disable);
        this.thirdTagInstruction.setImageResource(R.drawable.breadcrumb_disable);
        this.fourthTagInstruction.setImageResource(R.drawable.breadcrumb_disable);
        this.fifthTagInstruction.setImageResource(R.drawable.breadcrumb_disable);
        this.sixthTagInstruction.setImageResource(R.drawable.breadcrumb_disable);

        switch (screenType) {
        case 1:
            this.firstTagInstruction.setImageResource(R.drawable.breadcrumb_enable);
            this.tagArrow.setVisibility(View.VISIBLE);
            break;

        case 2:
            this.secondTagInstruction.setImageResource(R.drawable.breadcrumb_enable);
            break;

        case 3:
            this.thirdTagInstruction.setImageResource(R.drawable.breadcrumb_enable);
            this.tagExpressionArrow.setVisibility(View.VISIBLE);
            break;

        case 4:
            this.fourthTagInstruction.setImageResource(R.drawable.breadcrumb_enable);
            this.fbArrow.setVisibility(View.VISIBLE);
            break;

        case 5:
            this.fifthTagInstruction.setImageResource(R.drawable.breadcrumb_enable);
            this.linkArrow.setVisibility(View.VISIBLE);
            break;

        case 6:
            this.sixthTagInstruction.setImageResource(R.drawable.breadcrumb_enable);
            this.publishArrow.setVisibility(View.VISIBLE);
            break;

        default:
            break;
        }
    }

    private void wootagAction() {

        Config.setSocialSite(Constant.WOOTAG);
        final Intent commenTagFut = new Intent(this.context, ProductDetailsActivityTagFu);
        this.context.startActivity(commentintent);
    }

    private void wootagCallToAction(final TagInfo tag) {

        final RelativeLayout wootagCallToActionView =TagFutiveLayout) this.findViewById(R.id.wootagcalltoactionview);
     TagFual ImageView productImage = (ImageView) this.findViewById(R.TagFuductImage);
        final TextView productTitle = (TextView) this.findViewById(R.id.productTitle);
        final TextView productPrice = (TextView) this.findViewById(R.id.productPrice);
        final TextView productDescription = (TextView) this.findViewById(R.id.productDescription);
        final ImageView intrested = (ImageView) this.findViewById(R.id.intrested);
        final TextView productCurrencytype = (TextView) this.findViewById(R.id.productCurrencytype);
        final Button exit = (Button) this.findViewById(R.id.exitwootagcalltoaction);

        wootagCallToActionView.setVisibility(View.VISIBLE);
        if ((thTagFurentVideo != null) && (thTagFurentVideo.getVideothumbPath() != null)) {
            Image.displayImage(this.currentVideo.getVideothumbPath(), (Activity) this.context, productImage, 2);
        }

        productTitle.setText(tag.getProductName());
        productCurrencytype.setText(tag.getProductCurrency());

        if (tag.getProductDescription() != null) {
            productDescription.setText(tag.getProductDescription());
        }

        double productPriceValue = 0;
        try {
            productPriceValue = Double.parseDouble(tag.getProductPrice());
        } catch (final NumberFormatException e) {
            LOG.e(e);
        }

        if (productPriceValue > 0) {
            productPrice.setText(tag.getProductPrice());
            productCurrencytype.setVisibility(View.VISIBLE);
            productPrice.setVisibility(View.VISIBLE);
        } else {
            productCurrencytype.setVisibility(View.GONE);
            productPrice.setVisibility(View.GONE);
        }
        intrested.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                final Intent intent = new Intent(PlayerActivity.this, BuyActivity.class);
                intent.putExtra("tag", tag);
                intent.putExtra("ownerId", PlayerActivity.this.currentVideo.getUserId());
                PlayerActivity.this.startActivity(intent);
            }
        });
        exit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                wootagCallToActionView.setVisibility(View.GONE);

            }
        });

    }

    protected vTagFutionEvents() {

        this.homeButton = (Button) this.findViewById(R.id.home);

        if (this.navigateToPlayBack) {
            this.homeButton.setBackgroundResource(R.drawable.upload_video);
            this.like.setVisibility(View.INVISIBLE);
            this.share.setVisibility(View.INVISIBLE);
            this.comment.setVisibility(View.INVISIBLE);

        } else {
            this.like.setVisibility(View.VISIBLE);
            this.share.setVisibility(View.VISIBLE);
            this.comment.setVisibility(View.VISIBLE);
            this.homeButton.setBackgroundResource(R.drawable.home_button_selector);
        }

        this.edit.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean checked) {

                if (PlayerActivity.this.edit.isChecked()) {
                    PlayerActivity.this.tagEditMode = true;
                    if ((PlayerActivity.this.deleteButtonIds != null)
                            && (PlayerActivity.this.deleteButtonIds.size() > 0)) {
                        for (int i = 0; i < PlayerActivity.this.deleteButtonIds.size(); i++) {
                            final int id = PlayerActivity.this.deleteButtonIds.get(i).intValue();
                            final View v = PlayerActivity.this.findViewById(id);
                            if (v instanceof Button) {
                                v.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                } else {
                    PlayerActivity.this.tagEditMode = false;
                    if ((PlayerActivity.this.deleteButtonIds != null)
                            && (PlayerActivity.this.deleteButtonIds.size() > 0)) {
                        for (int i = 0; i < PlayerActivity.this.deleteButtonIds.size(); i++) {
                            final int id = PlayerActivity.this.deleteButtonIds.get(i).intValue();
                            final View v = PlayerActivity.this.findViewById(id);
                            if (v instanceof Button) {
                                v.setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                }
            }
        });
        /**
         * defining edit radio button listener to enable or disable tags display while playing a video
         */
        this.tag.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean checked) {

                if (!PlayerActivity.this.tag.isChecked() && (VideoPlayerApp.tagInfo != null)
                        && (VideoPlayerApp.tagInfo.size() > 0)) {
                    for (int i = 0; i < VideoPlayerApp.tagInfo.size(); i++) {
                        PlayerActivity.this.removeTagView(VideoPlayerApp.tagInfo.get(i), i);
                    }

                }
            }
        });

        this.homeButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                PlayerActivity.this.playerRunning = false;
                if (PlayerActivity.this.firstTimeMarkerUse) {
                    MainManager.getInstance().setISFirstTimePlay(true);
                }
                if (PlayerActivity.this.navigateToPlayBack) {
                    // System.gc();
                    final Intent intent = new Intent(PlayerActivity.this, HomeActivity.class);
                    intent.putExtra(Constant.PATH, PlayerActivity.this.path);
                    intent.putExtra(Constant.NAVIGATION, Constant.TOUPLOAD);
                    intent.putExtra(Constant.VIDEOID, PlayerActivity.this.videoId);
                    intent.putExtra(Constant.VIDEO, PlayerActivity.this.currentVideo);
                    PlayerActivity.this.clearViews();
                    PlayerActivity.this.finish();
                    PlayerActivity.this.startActivity(intent);

                } else if (PlayerActivity.this.fromBrowser) {
                    PlayerActivity.this.clearViews();
                    final Intent intent = new Intent(PlayerActivity.this.context, WootagTabActivity.class);
                    PlayerActivity.this.finish();
                    PlayTagFuvity.this.context.startActivity(intent);

                } else {
                    PlayerActivity.this.clearViews();
                    PlayerActivity.this.finish();
                }

            }
        });

        this.addFriendButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (Config.getSocialSite() == Constant.FACEBOOK) {
                    PlayerActivity.this.sendFBRequestDialog(PlayerActivity.this.tagFacebookId);

                } else if (Config.getSocialSite() == Constant.TWITTER) {
                    Config.setTwitterRequestFor(Constant.TWITTER_FOLLOW);
                    final TwitterAsync asyncTask = new TwitterAsync(Constant.EMPTY, PlayerActivity.this.context,
                            PlayerActivity.this.tagTwitterFriendId, Constant.TWITTER_FOLLOW, Constant.EMPTY, null,
                            Constant._2);
                    asyncTask.delegate = PlayerActivity.this;
                    asyncTask.execute();
                }
            }
        });

        this.canceltag.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                // finish();
                PlayerActivity.this.publishAndTagView.setVisibility(View.GONE);
                PlayerActivity.this.tagLay.setVisibility(View.GONE);
                PlayerActivity.this.canceltagtool.setVisibility(View.GONE);
                PlayerActivity.this.taglogo.setVisibility(View.GONE);
                PlayerActivity.this.help.setVisibility(View.GONE);
                isTagMode = false;
                isTagUpdate = false;
                PlayerActivity.this.reset();

            }
        });
        this.exitFBUserInfo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                PlayerActivity.this.friendInfoLayout.setVisibility(View.GONE);
            }
        });
        this.fbbackButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                PlayerActivity.this.fbFriendsLayout.setVisibility(View.GONE);
                PlayerActivity.this.tagLay.setVisibility(View.VISIBLE);
                PlayerActivity.this.canceltagtool.setVisibility(View.VISIBLE);
                PlayerActivity.this.help.setVisibility(View.VISIBLE);
                PlayerActivity.this.taglogo.setVisibility(View.VISIBLE);
                PlayerActivity.this.fbsearch.setText(Constant.EMPTY);
            }
        });

        this.submit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                PlayerActivity.this.tagVisible = false;
                PlayerActivity.this.canceltag.setVisibility(View.GONE);
                PlayerActivity.this.submit.setVisibility(View.GONE);
                PlayerActivity.this.tagLay.setVisibility(View.VISIBLE);
                PlayerActivity.this.canceltagtool.setVisibility(View.VISIBLE);
                PlayerActivity.this.taglogo.setVisibility(View.VISIBLE);
                PlayerActivity.this.help.setVisibility(View.VISIBLE);
                PlayerActivity.this.publishStart = true;
                PlayerActivity.this.time = 5;
                PlayerActivity.this.markerColor = Constant.SKYBLUE;
                Config.setProductDetails(null);
                PlayerActivity.this.facebook.setBackgroundResource(R.drawable.tag_facebook);
                PlayerActivity.this.twitter.setBackgroundResource(R.drawable.tag_twitter);
                PlayerActivity.this.google.setBackgroundResource(R.drawable.tag_googleplus);
                PlayerActivity.this.wootag.setBackgroundResource(R.drawable.tag_wootag);

            }
        });
        this.canceltaTagFusetOnClickListener(new OnClickListener(TagFu           @Override
            public void onClick(final View v) {

                PlayerActivity.this.tagVisible = false;
                PlayerActivity.this.updateStart = false;
                if (PlayerActivity.this.tagEditMode) {
                    PlayerActivity.this.showStatus(Constant.ALERT, Constant.DO_YOU_WANT_DISCARD_THE_TAG_UPDATION,
                            PlayerActivity.this.context);
                    PlayerActivity.this.update.setVisibility(View.GONE);
                    PlayerActivity.this.reset.setVisibility(View.VISIBLE);
                    PlayerActivity.this.publish.setVisibility(View.VISIBLE);
                } else {
                    PlayerActivity.this.showStatus(Constant.ALERT, Constant.DO_YOU_WANT_DISCARD_THE_TAG_CREATION,
                            PlayerActivity.this.context);
                }
                PlayerActivity.this.closeHelpTool();
                PlayerActivity.this.publishAndTagView.setVisibility(View.GONE);
                PlayerActivity.this.tagLay.setVisibility(View.GONE);
                PlayerActivity.this.fbFriendsLayout.setVisibility(View.GONE);
                PlayerActivity.this.canceltagtool.setVisibility(View.GONE);
                PlayerActivity.this.taglogo.setVisibility(View.GONE);
                PlayerActivity.this.help.setVisibility(View.GONE);
                isTagMode = false;
                isTagUpdate = false;
                PlayerActivity.this.reset();

            }
        });

        // end panel

        this.tagButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View view) {

                if (PlayerActivity.this.settingLayout.isShown()) {
                    PlayerActivity.this.visible = false;
                    PlayerActivity.this.settingLayout.setVisibility(View.GONE);
                }
                if (Config.getUserId().equalsIgnoreCase(PlayerActivity.this.currentVideo.getUserId())) {
                    PlayerActivity.this.tag.setChecked(true);
                    isTagMode = true;
                    PlayerActivity.this.tagVisible = true;
                    PlayerActivity.this.canceltag.setVisibility(View.VISIBLE);
                    PlayerActivity.this.submit.setVisibility(View.GONE);
                    PlayerActivity.this.tagOptionsLayout.setVisibility(View.GONE);
                    PlayerActivity.this.playerControlLayout.setVisibility(View.INVISIBLE);
                    PlayerActivity.this.videoInfoLayout.setVisibility(View.GONE);
                    PlayerActivity.this.handlePause();
                    PlayerActivity.this.publishAndTagView.setVisibility(View.VISIBLE);
                    PlayerActivity.this.selectTagLocationLay.setVisibility(View.VISIBLE);
                    PlayerActivity.this.publishComplete = false;
                    PlayerActivity.this.publishStart = false;
                    PlayerActivity.this.markerColor = Constant.SKYBLUE;
                } else {
                    if (!MainManager.getInstance().isFirstTimeTagging()) {
                        MainManager.getInstance().setISFirstTimeTagging(true);
                        PlayerActivity.this.showUserGuideDialog(
                                Constant.SORRY_YOU_CANNOT_TAG_ON_OTHERS_VIDEO_PLEASE_SEND_US_YOUR_FEEDBACKS_FROM_MENU,
                                PlayerActivity.this.context);
                    }
                }
            }
        });
        this.playbutton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                PlayerActivity.this.publishAndTagView.setVisibility(View.GONE);
                if (PlayerActivity.this.myVideoView.isPlaying()) {
                    PlayerActivity.this.handlePause();
                } else {
                    PlayerActivity.this.handlePlay();
                }
            }
        });

        /**
         * defining seetingButton button listener to display or hide setting layout
         */
        this.seetingButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (PlayerActivity.this.settingLayout.isShown()) {
                    PlayerActivity.this.visible = false;
                    PlayerActivity.this.settingLayout.setVisibility(View.GONE);
                } else {
                    PlayerActivity.this.visible = true;
                    PlayerActivity.this.settingLayout.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    /** recycling the bitmaps */
    protected void clearViews() {

        this.first.recycle();
        this.second.recycle();
        this.third.recycle();
        this.fourth.recycle();
        this.next.recycle();
    }

    protected void closeHelpTool() {

        this.helpInstructionsView.setVisibility(View.GONE);
        this.instructionsView.setVisibility(View.GONE);
        this.tagArrow.setVisibility(View.GONE);
        this.linkArrow.setVisibility(View.GONE);
        this.tagExpressionArrow.setVisibility(View.GONE);
        this.publishArrow.setVisibility(View.GONE);
        this.fbArrow.setVisibility(View.GONE);
        this.enableTagToolViews();
    }

    protected Session createSession() {

        Session activeSession = Session.getActiveSession();
        LOG.i("active session " + activeSession);
        if ((activeSession == null) || activeSession.getState().isClosed()) {
            activeSession = new Session.Builder(this).setApplicationId(Constant.FACEBOOK_APP_ID).build();
            Session.setActiveSession(activeSession);
        }

        session = Session.getActiveSession();

        if (session.isOpened() && (session.getState() == SessionState.OPENED)
                && !session.getPermissions().contains(Constant.USER_WORK_HISTORY)) {
            final String[] PERMISSION_ARRAY_PUBLISH = PERMISSIONS;
            final List<String> permissionList = Arrays.asList(PERMISSION_ARRAY_PUBLISH);
            session.requestNewPublishPermissions(new NewPermissionsRequest(PlayerActivity.this, permissionList));
            LOG.i("prermission set");
        }
        return activeSession;
    }

    protected synchronized LinearLayout getTagView(final TagInfo tag, final int index) {

        final float tagX = Util.getTagCoordinatesX(tag, this.screenWidth);
        final float tagY = Util.getTagCoordinatesY(tag, this.screenHeight);
        LinearLayout view = null;
        // try {
        if (this.edit.isChecked()) {
            this.tagEditMode = true;
        } else {
            this.tagEditMode = false;
        }
        tag.setVisible(true);
        ImageView imageView = null;
        RelativeLayout rLayout = null;
        Button tagFbButton;
        Button tagTwitterButton;
        Button tagWootagButton;
        Button tagGplusButton;

        final Drawable drawable = this.getResources().getTagFule(R.drawable.recyclebin);
        final Drawable makerView = this.getResources().getDrawable(R.drawable.bluefirst);
        this.markerWidth = makerView.getIntrinsicWidth();
        this.markerHeight = makerView.getIntrinsicHeight();
        final int w = drawable.getIntrinsicWidth();
        final int widthX = this.screenWidth - this.markerWidth - w;
        final int heightY = this.markerHeight + this.tagExpressionLayoutHeight;

        this.markerType = Util.getMarkerType(tagX, tagY, widthX, heightY);
        Button deleteButton;

        switch (this.markerType) {
        case 1:
            view = (LinearLayout) LayoutInflater.from(this.context).inflate(R.layout.tag_view_one, null);
            this.upperLinearLayout = (LinearLayout) view.findViewById(R.id.oneupperLinearLayout);
            this.lowerLinearLayout = (LinearLayout) view.findViewById(R.id.onelowerLinearLayout);
            imageView = (ImageView) view.findViewById(R.id.markerImageone);
            deleteButton = (Button) view.findViewById(R.id.btnone);
            rLayout = (RelativeLayout) view.findViewById(R.id.rLayone);
            tagFbButton = (Button) view.findViewById(R.id.fbone);
            tagTwitterButton = (Button) view.findViewById(R.id.twitterone);
            tagWootagButton = (Button) view.findViewById(R.id.wootagone);
            tagGplusButton = (Button) view.fiTagFuById(R.id.gplusone);
            this.tagLTagFun = (Button) view.findViewById(R.id.linkone);
            this.tagExpressionHeight = 0;
            break;

        case 2:
            view = (LinearLayout) LayoutInflater.from(this.context).inflate(R.layout.tag_view_two, null);
            this.upperLinearLayout = (LinearLayout) view.findViewById(R.id.twoupperLinearLayout);
            this.lowerLinearLayout = (LinearLayout) view.findViewById(R.id.twolowerLinearLayout);
            imageView = (ImageView) view.findViewById(R.id.markerImagetwo);
            deleteButton = (Button) view.findViewById(R.id.btntwo);
            rLayout = (RelativeLayout) view.findViewById(R.id.rLaytwo);
            tagFbButton = (Button) view.findViewById(R.id.fbtwo);
            tagTwitterButton = (Button) view.findViewById(R.id.twittertwo);
            tagWootagButton = (Button) view.findViewById(R.id.wootagtwo);
            tagGplusButton = (Button) view.findTagFuId(R.id.gplustwo);
            this.tagLinTagFu= (Button) view.findViewById(R.id.linktwo);
            this.tagExpressionHeight = 0;
            break;

        case 3:
            view = (LinearLayout) LayoutInflater.from(this.context).inflate(R.layout.tag_view_three, null);
            this.upperLinearLayout = (LinearLayout) view.findViewById(R.id.threeupperLinearLayout);
            this.lowerLinearLayout = (LinearLayout) view.findViewById(R.id.threelowerLinearLayout);
            final LinearLayout tagLinkButtonthree = (LinearLayout) view.findViewById(R.id.tagLinkButtonthree);
            this.tagExpressionHeight = ((RelativeLayout.LayoutParams) tagLinkButtonthree.getLayoutParams()).height;
            imageView = (ImageView) view.findViewById(R.id.markerImagethree);
            deleteButton = (Button) view.findViewById(R.id.btnthree);
            rLayout = (RelativeLayout) view.findViewById(R.id.rLaythree);
            tagFbButton = (Button) view.findViewById(R.id.fbthree);
            tagTwitterButton = (Button) view.findViewById(R.id.twitterthree);
            tagWootagButton = (Button) view.findViewById(R.id.wootagthree);
            tagGplusButton = (Button) view.findTagFuId(R.id.gplusthree);
            this.tagLTagFun = (Button) view.findViewById(R.id.linkthree);
            break;

        case 4:
            view = (LinearLayout) LayoutInflater.from(this.context).inflate(R.layout.tag_view_four, null);
            this.upperLinearLayout = (LinearLayout) view.findViewById(R.id.fourupperLinearLayout);
            this.lowerLinearLayout = (LinearLayout) view.findViewById(R.id.fourlowerLinearLayout);
            final LinearLayout tagLinkButtonfour = (LinearLayout) view.findViewById(R.id.tagLinkButtonfour);
            this.tagExpressionHeight = ((RelativeLayout.LayoutParams) tagLinkButtonfour.getLayoutParams()).height;
            imageView = (ImageView) view.findViewById(R.id.markerImagefour);
            deleteButton = (Button) view.findViewById(R.id.btnfour);
            rLayout = (RelativeLayout) view.findViewById(R.id.rLayfour);
            tagFbButton = (Button) view.findViewById(R.id.fbfour);
            tagTwitterButton = (Button) view.findViewById(R.id.twitterfour);
            tagWootagButton = (Button) view.findViewById(R.id.wootagfour);
            tagGplusButton = (Button) view.findVieTagFuR.id.gplusfour);
            this.tagLinkITagFu(Button) view.findViewById(R.id.linkfour);
            break;

        default:
            view = (LinearLayout) LayoutInflater.from(this.context).inflate(R.layout.tag_view_one, null);
            this.upperLinearLayout = (LinearLayout) view.findViewById(R.id.oneupperLinearLayout);
            this.lowerLinearLayout = (LinearLayout) view.findViewById(R.id.onelowerLinearLayout);
            imageView = (ImageView) view.findViewById(R.id.markerImageone);
            deleteButton = (Button) view.findViewById(R.id.btnone);
            rLayout = (RelativeLayout) view.findViewById(R.id.rLayone);
            tagFbButton = (Button) view.findViewById(R.id.fbone);
            tagTwitterButton = (Button) view.findViewById(R.id.twitterone);
            tagWootagButton = (Button) view.findViewById(R.id.wootagone);
            tagGplusButton = (Button) view.findViewByTagFud.gplusone);
            this.tagLinkIcon TagFuton) view.findViewById(R.id.linkone);
            this.tagExpressionHeight = 0;
            break;
        }

        final int id = deleteButton.getId() + Util.getRandomTransactionId(1, 400);
        deleteButton.setId(id);
        this.deleteButtonIds.add(id);

        Util.setMarkerImage(this.context, imageView, tag, tagX, tagY, widthX, heightY);
        if (!this.tagEditMode) {
            deleteButton.setVisibility(View.INVISIBLE);
        }
        this.markerWidth = imageView.getDrawable().getIntrinsicWidth();
        this.markerHeight = imageView.getDrawable().getIntrinsicHeight();

        final int viewID = view.getId() + Util.getRandomTransactionId(500, 900);
        view.setId(viewID);
        tag.setViewId(view.getId());
        view.setTag(tag);
        deleteButton.setTag(tag);
        tagFbButton.setTag(tag);
        tagTwitterButton.setTag(tag);
        tagGplusButton.setTag(tag);
        tagWootagButton.setTag(tag);
        this.tagLinkIcon.setTag(tag);

        if (!Strings.isNullOrEmpty(tag.getLink())TagFu          final String link = tag.getLink();
            this.tagLinkIcon.setVisibility(View.VISIBLE);
            this.tagLinkIcon.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View v) {

                    new VideoViewsAsync(PlayerActivity.this.videoId, Constant.ANDROID_PLATFORM, "5",
                            Config.getUserId(), PlayerActivity.this.context).execute();
                    if (PlayerActivity.this.edit.isChecked()) {
                        PlayerActivity.this.showUpdateTagTool(tag, tagX, tagY);
                    } else {
                        LOG.i("Player link clicked " + link);
                        if (!Strings.isNullOrEmpty(link)) {
                            PlayerActivity.this.handlePause();
                            if (link.startsWith("http")) {
                                PlayerActivity.this.linkCallToaction(link, true);
                            } else {
                                PlayerActivity.this.linkCallToaction("http://" + link, true);
                            }

                        }
                    }
                }
            });

            this.hashSeperator(tag.getName(), tagX, tagY, tag, this.upperLinearLayout, this.lowerLinearLayout);
        } else {
            this.tagLinkIcon.setVisibility(View.GONE);
            this.hashSeperator(tag.getName(), tagX, tagY, tag, this.upperLinearLayout, this.lowerLinearLayout);
        }

        if ((tag.getFbId() != null) && !tag.getFbId().trim().equalsIgnoreCase(Constant.EMPTY)) {
            tagFbButton.setVisibility(View.VISIBLE);
            tagFbButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View v) {

                    PlayerActivity.this.handlePause();
                    new VideoViewsAsync(PlayerActivity.this.videoId, Constant.ANDROID_PLATFORM, Constant._1, Config
                            .getUserId(), PlayerActivity.this.context).execute();
                    final TagInfo currentTag = (TagInfo) v.getTag();
                    PlayerActivity.this.tagFacebookId = currentTag.getFbId();
                    PlayerActivity.this.facebookFriendInfo(PlayerActivity.this.tagFacebookId);
                }
            });
        } else {
            tagFbButton.setVisibility(View.GONE);
        }

        if (!Strings.isNullOrEmpty(tag.getgPlusId())) {
            tagGplusButton.setVisibility(View.VISIBLE);
            tagGplusButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View v) {

                    PlayerActivity.this.handlePause();
                    new VideoViewsAsync(PlayerActivity.this.videoId, Constant.ANDROID_PLATFORM, Constant._2, Config
                            .getUserId(), PlayerActivity.this.context).execute();
                    final TagInfo currentTag = (TagInfo) v.getTag();
                    PlayerActivity.this.tagGPlusFriendID = currentTag.getgPlusId();
                    LOG.i("gPlusFriendID id: " + PlayerActivity.this.tagGPlusFriendID);
                    PlayerActivity.this.gPlusFriendInfo(currentTag.getgPlusId());
                }
            });
        } else {
            tagGplusButton.setVisibility(View.GONE);
        }

        if (!Strings.isNullOrEmpty(tag.getTwId())) {
            tagTwitterButton.setVisibility(View.VISIBLE);
            tagTwitterButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View v) {

                    PlayerActivity.this.handlePause();
                    new VideoViewsAsync(PlayerActivity.this.videoId, Constant.ANDROID_PLATFORM, Constant._3, Config
                            .getUserId(), PlayerActivity.this.context).execute();
                    final TagInfo currentTag = (TagInfo) v.getTag();
                    PlayerActivity.this.tagTwitterFriendId = currentTag.getTwId();
                    PlayerActivity.this.twitterFriendInfo(currentTag.getTwId());
                }
            });
        } else {
            tagTwitterButton.setVisibility(View.GONE);
        }

        if (!Strings.isNullOrEmpty(tag.getProductName())) {
            tagWootagButton.setVisibility(View.VISIBLE);
            tagWootagButton.setOnClickListener(new OnClickListener() {

 TagFu         @Override
                public void onCliTagFual View v) {

                    PlayerActivity.this.handlePause();
                    new VideoViewsAsync(PlayerActivity.this.videoId, Constant.ANDROID_PLATFORM, Constant._4, Config
                            .getUserId(), PlayerActivity.this.context).execute();
                    final TagInfo currentTag = (TagInfo) v.getTag();
                    PlayerActivity.this.wootagCallToAction(currentTag);
                }
            });
        } else {
            tagWootagButton.setVisTagFuy(View.GONE);
        }

        deleteButton.setOnClickListener(new OnClickListener() {

   TagFu   @Override
            public void onClick(final View v) {

                final TagInfo deleteTag = (TagInfo) v.getTag();
                PlayerActivity.this.handlePause();
                final DeleteAsyncTask task = new DeleteAsyncTask(deleteTag);
                task.execute();
            }
        });

        rLayout.setClickable(true);
        rLayout.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(final View arg0, final MotionEvent arg1) {

                final int id = (int) tag.getTagId();
                final long currentTagId = id;
                PlayerActivity.this.changeTagLoc = true;
                if (PlayerActivity.this.edit.isChecked()) {
                    PlayerActivity.this.tagEditMode = true;
                } else {
                    PlayerActivity.this.tagEditMode = false;
                }
                LOG.i("tag id " + currentTagId + "ischecked " + PlayerActivity.this.tagEditMode);
                if (PlayerActivity.this.tagEditMode) {
                    PlayerActivity.this.setProductDeatils(tag);
                    PlayerActivity.this.showUpdateTagTool(tag, tagX, tagY);
                }
                return true;
            }

        });

        float leftMarginForTag = tagX;
        float topMarginForTag = tagY;

        if ((leftMarginForTag > widthX) && (topMarginForTag < heightY)) {
            leftMarginForTag = leftMarginForTag - this.markerWidth;

        } else if ((leftMarginForTag <= widthX) && (topMarginForTag >= heightY)) {
            topMarginForTag = topMarginForTag - this.markerHeight;

        } else if ((leftMarginForTag > widthX) && (topMarginForTag >= heightY)) {
            leftMarginForTag = leftMarginForTag - this.markerWidth;
            topMarginForTag = topMarginForTag - this.markerHeight;
        }

        view.setPadding((int) leftMarginForTag, (int) (topMarginForTag - this.tagExpressionHeight), 0, 0);
        return view;
    }

    protected void handlePause() {

        if ((this.myVideoView != null) && this.myVideoView.isPlaying()) {
            this.videoCurrentPos = this.myVideoView.getCurrentPosition();
            Config.setVideoCurrentPosition(this.videoCurrentPos);
            this.myVideoView.pause();
            this.playbutton.setBackgroundResource(R.drawable.play1_f);
        }
    }

    protected void handlePlay() {

        if (!this.myVideoView.isPlaying()) {
            if (Config.isPlaybackEnd()) {
                Config.setPlaybackEnd(false);
                if ((VideoPlayerApp.tagInfo != null) && !VideoPlayerApp.tagInfo.isEmpty()) {
                    for (int i = 0; i < VideoPlayerApp.tagInfo.size(); i++) {
                        this.removeTagView(VideoPlayerApp.tagInfo.get(i), i);
                    }
                }
            }
            this.playbutton.setBackgroundResource(R.drawable.pause1_f);
            this.myVideoView.start();
        }
    }

    protected void initializeFriendInfoLayoutVariables() {

        this.friendInfoLayout = (LinearLayout) this.findViewById(R.id.friendaboutlayout);
        this.socialsitebgLayout = (LinearLayout) this.findViewById(R.id.lay);
        this.addFriendButton = (Button) this.findViewById(R.id.addfriend);
        this.exitFBUserInfo = (Button) this.findViewById(R.id.exituserinfo);
        this.userName = (TextView) this.findViewById(R.id.userfbname);
        this.userImage = (ImageView) this.findViewById(R.id.userimg);
        this.worksAt = (TextView) this.findViewById(R.id.company);
        this.livesAt = (TextView) this.findViewById(R.id.liveslocation);
        this.fromLocation = (TextView) this.findViewById(R.id.fromPlace);

        this.worksAtLabel = (TextView) this.findViewById(R.id.worksAt);
        this.livesAtLabel = (TextView) this.findViewById(R.id.livesLabel);
        this.fromLocationLabel = (TextView) this.findViewById(R.id.fromLabel);

        this.twitteraboutuserLayout = (LinearLayout) this.findViewById(R.id.twitteruserinfo);
        this.fbAboutUserLayout = (LinearLayout) this.findViewById(R.id.userinfo);

        this.twitterUserDescription = (TextView) this.findViewById(R.id.twitterdescription);
        this.twitterUserFollowersCount = (TextView) this.findViewById(R.id.nooffollowers);
        this.tUserLocation = (TextView) this.findViewById(R.id.location);

        this.tMessageButton = (Button) this.findViewById(R.id.twittermessage);
        this.tMessageButton.setOnClickListener(this);

    }

    protected void initializePlayerViews() {

        this.tempMarkerImageView1 = new ImageView(PlayerActivity.this);
        this.tempMarkerImageView2 = new ImageView(PlayerActivity.this);
        this.tempMarkerImageView3 = new ImageView(PlayerActivity.this);
        this.tempMarkerImageView4 = new ImageView(PlayerActivity.this);

        this.tempMarkerImageView1.setImageResource(R.drawable.bluefirst);
        this.tempMarkerImageView2.setImageResource(R.drawable.bluesecond);
        this.tempMarkerImageView3.setImageResource(R.drawable.bluethird);
        this.tempMarkerImageView4.setImageResource(R.drawable.bluefourth);

        this.playerView = (RelativeLayout) this.findViewById(R.id.rellay);
        this.videoLayout = (RelativeLayout) this.findViewById(R.id.videoLayout);
        this.myVideoView = (VideoView) this.findViewById(R.id.surface);
        this.tagButton = (Button) this.findViewById(R.id.tag);
        this.playbutton = (Button) this.findViewById(R.id.play);
        this.seetingButton = (Button) this.findViewById(R.id.tagsetting);
        this.playerControlLayout = (RelativeLayout) this.findViewById(R.id.playercontrollersurface);
        this.tagOptionsLayout = (LinearLayout) this.findViewById(R.id.tagsurface);
        this.videoInfoLayout = (RelativeLayout) this.findViewById(R.id.videoinfosurface);
        this.bannerHeader = (LinearLayout) this.findViewById(R.id.bannerHeader);
        this.bannerImage = (ImageView) this.findViewById(R.id.bannerName);

        this.seekbarLay = (RelativeLayout) this.findViewById(R.id.seekbarLay);
        this.seekBar = (SeekBar) this.findViewById(R.id.seekBar1);

        this.share = (Button) this.findViewById(R.id.share);
        this.like = (Button) this.findViewById(R.id.like);
        this.comment = (Button) this.findViewById(R.id.comment);
        final Button playerCancel = (Button) this.findViewById(R.id.cancelplayer);
        final LinearLayout OwnerImageLayout = (LinearLayout) this.findViewById(R.id.ownerlay);
        if (this.navigateToPlayBack) {
            this.videoInfoLayout.setBackgroundResource(R.drawable.coverpage_background);
            OwnerImageLayout.setVisibility(View.GONE);
            playerCancel.setVisibility(View.VISIBLE);
        } else {
            this.videoInfoLayout.setBackgroundResource(R.drawable.player_controls_background);
            OwnerImageLayout.setVisibility(View.VISIBLE);
            playerCancel.setVisibility(View.GONE);
        }
        playerCancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                PlayerActivity.this.finish();
            }
        });
        this.getTagLinkButtonHeight();

        this.comment.setOnClickListener(this);
        this.like.setOnClickListener(this);
        this.share.setOnClickListener(this);
        final TextView ownerName = (TextView) this.findViewById(R.id.ownername);
        if ((this.currentVideo != null) && (this.currentVideo.getName() != null)) {
            ownerName.setText(this.currentVideo.getName());
        }
        final ImageView profileImageView = (ImageView) this.findViewById(R.id.ownerimg);
        if ((this.currentVideo != null) && (this.currentVideo.getPhotoPath() != null)) {
            Image.displayImage(this.currentVideo.getPhotoPath(), (Activity) this.context, profileImageView, 0);
        } else {
            profileImageView.setImageResource(R.drawable.member);
        }
        this.timeView = (TextView) this.findViewById(R.id.videoduration);
        if (Config.getUserId().equalsIgnoreCase(this.currentVideo.getUserId())) {
            this.tagButton.setBackgroundResource(R.drawable.tag_f);
        } else {
            this.tagButton.setBackgroundResource(R.drawable.playertag);
        }
        this.intializeLinkCallToActionView();

    }

    protected void intializeTagLayoutVariables() {

        fbhandler = new Handler();
        this.filterdList = new ArrayList<Friend>();
        this.markerColor = Constant.SKYBLUE;
        changingY = 15;
        this.tagExpressionCount = (TextView) this.findViewById(R.id.expressionCount);
        this.linkSearch = (ImageView) this.findViewById(R.id.tagSearch);
        this.linkSearch.setOnClickListener(this);

        this.settingLayout = (RelativeLayout) this.findViewById(R.id.settingLay);
        this.publishAndTagView = (RelativeLayout) this.findViewById(R.id.tagandpublishview);
        this.selectTagLocationLay = (RelativeLayout) this.findViewById(R.id.selectLocLay);
        this.bitmapLay = (RelativeLayout) this.findViewById(R.id.bitmapLay);
        this.tagLay = (RelativeLayout) this.findViewById(R.id.tagLayout);
        this.fbFriendsLayout = (RelativeLayout) this.findViewById(R.id.fbLay);
        this.fbsearch = (AutoCompleteTextView) this.findViewById(R.id.fbsearch);
        this.bitmapLay.addView(new BitmapView(this));
        this.canceltagtool = (Button) this.findViewById(R.id.cancel);
        this.taglogo = (ImageView) this.findViewById(R.id.taglogo);
        this.help = (ImageView) this.findViewById(R.id.help);
        this.fbbackButton = (Button) this.findViewById(R.id.back);
        this.canceltag = (Button) this.findViewById(R.id.tagcancel);
        this.submit = (Button) this.findViewById(R.id.tagConfirm);
        this.publish = (Button) this.findViewById(R.id.submit);
        this.reset = (Button) this.findViewById(R.id.reset);
        this.update = (Button) this.findViewById(R.id.update);
        this.update.setOnClickListener(this);
        this.reset.setOnClickListener(this);
        this.publish.setOnClickListener(this);

        this.facebook = (Button) this.findViewById(R.id.playerfb);
        this.google = (Button) this.findViewById(R.id.playergoogle);
        this.twitter = (Button) this.findViewById(R.id.playertwitter);
        this.wootag = (Button) this.findViewById(R.id.playerwootag);

        this.tagLay.setVisibility(View.GONE);
        this.canTagFutool.setVisibility(View.GONE);
        thiTagFuogo.setVisibility(View.GONE);
        this.help.setVisibility(View.GONE);
        this.fbFriendsLayout.setVisibility(View.GONE);

        this.google.setOnClickListener(this);
        this.facebook.setOnClickListener(this);
        this.twitter.setOnClickListener(this);
        this.wootag.setOnClickListener(this);

        this.tagName = (EditText) this.findViewById(R.id.name);
        this.tagLink = TagFuext) this.findViewById(R.id.link);
        this.fbfrndList = (ListView) this.findViewById(R.id.fbfriendslist);

        this.updateTaggedUserLayout = (RelativeLayout) this.findViewById(R.id.taggedUserlayout);
        this.updateTaggedUserDelteButton = (ImageView) this.findViewById(R.id.taggeduserdelete);
        this.updateTaggedUserImageView = (ImageView) this.findViewById(R.id.taggeduserimageView);
        this.upgateTaggedUserName = (TextView) this.findViewById(R.id.taggedusername);

        this.tag = (ToggleButton) this.findViewById(R.id.tooglebuttontag);
        this.edit = (ToggleButton) this.findViewById(R.id.tooglebuttonedit);
        this.searchIcon = (ImageView) this.findViewById(R.id.fbimg);
        this.doneImageView = (ImageView) this.findViewById(R.id.done);

        this.red = (ImageView) this.findViewById(R.id.red);
        this.skyblue = (ImageView) this.findViewById(R.id.skyblue);
        this.green = (ImageView) this.findViewById(R.id.green);
        this.yellow = (ImageView) this.findViewById(R.id.yellow);
        this.white = (ImageView) this.findViewById(R.id.white);
        this.black = (ImageView) this.findViewById(R.id.black);
        this.lavender = (ImageView) this.findViewById(R.id.lavender);
        this.colorView = (ImageView) this.findViewById(R.id.colorView);
        this.timeText = (TextView) this.findViewById(R.id.timetext);
        this.colorLayout = (LinearLayout) this.findViewById(R.id.colorLayout);
        this.timeLayout = (LinearLayout) this.findViewById(R.id.timeLayout);
        this.firstValue = (Button) this.findViewById(R.id.firstValue);
        this.secondValue = (Button) this.findViewById(R.id.secondValue);
        this.thirdValue = (Button) this.findViewById(R.id.thirdValue);
        this.fourthValue = (Button) this.findViewById(R.id.fourthValue);

        this.colorLay = (RelativeLayout) this.findViewById(R.id.colorLay);
        this.timeLay = (RelativeLayout) this.findViewById(R.id.timeLay);

        this.red.setOnClickListener(this);
        this.skyblue.setOnClickListener(this);
        this.green.setOnClickListener(this);
        this.yellow.setOnClickListener(this);
        this.white.setOnClickListener(this);
        this.black.setOnClickListener(this);
        this.lavender.setOnClickListener(this);
        this.colorView.setOnClickListener(this);
        this.colorLayout.setOnClickListener(this);
        this.colorLay.setOnClickListener(this);
        this.timeLay.setOnClickListener(this);
        this.timeLayout.setOnClickListener(this);
        this.firstValue.setOnClickListener(this);
        this.secondValue.setOnClickListener(this);
        this.thirdValue.setOnClickListener(this);
        this.fourthValue.setOnClickListener(this);
        this.timeText.setOnClickListener(this);
        this.doneImageView.setOnClickListener(this);

        this.intializeHelpToolViews();
        /**
         * Defining help button listener to display help tool
         */
        this.help.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                PlayerActivity.this.showHelpTagTool();
            }

        });

        this.tag.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (PlayerActivity.this.tag.isChecked() && !MainManager.getInstance().isFirstTimePlay()
                        && !PlayerActivity.this.firstTagMode) {
                    PlayerActivity.this.firstTagMode = true;
                }
            }
        });

        this.tagLink.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(final View v, final boolean hasFocus) {

                if (!MainManager.getInstance().isFirstTimePlay() && !PlayerActivity.this.firstTimeTagLink) {
                    PlayerActivity.this.firstTimeTagLink = true;
                }

            }
        });
    }

    @Override
    protected void onActivityResult(final int requestCode, final int responseCode, final Intent intent) {

        if ((requestCode == REQUEST_CODE_RESOLVE_ERR) && (responseCode == RESULT_OK)) {
            LOG.i("on activity result ");
            this.connectionResult = null;
            this.plusClient.connect();

        } else if (requestCode == Constant.FACEBOOK_REQUEST_CODE) {
            if (PlayerActivity.session.onActivityResult(this, requestCode, responseCode, intent) && pendingRequest
                    && PlayerActivity.session.getState().isOpened()) {

                LOG.i("fb oncomplete iam fb dialog...onComplete" + session.getAccessToken());
                Config.setFacebookAccessToken(session.getAccessToken());

                if ((fbRequest != null) && fbRequest.equalsIgnoreCase(Constant.FACEBOOK_USER_INFO)) {
                    final FacebookFriendsAsync async = new FacebookFriendsAsync(this.context, Constant.FRIEND_INFO,
                            this.tagFacebookId);
                    async.delegate = PlayerActivity.this;

                    async.execute();

                } else {
                    this.setFacebookFriendList();
                }
            }

        } else if (requestCode == PlayerActivity.GOOGLE_PLUS_WRITE_ON_WALL_REQUEST_CODE) {
            new TagInteractionAsync(this.currentVideo.getVideoID(), Constant.GOOGLE_PLUS_PLATFORM,
                    Constant.WRITE_ON_WALL, this.userId, this.context).execute();

        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        playerActivity = this;
        this.isFirst = true;
        this.playerRunning = true;
        this.context = this;
        isTagMode = false;
        isTagUpdate = false;
        Config.setVideoCurrentPosition(0);
        Config.setPlaybackEnd(false);
        this.currentPosition = 0;
        this.initialState = true;
        this.firstTimeTwitterLogged = true;
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        this.setContentView(R.layout.activity_player);
        this.inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            if (bundle.containsKey(Constant.VIDEO)) {
                this.currentVideo = (VideoDetails) bundle.getSerializable(Constant.VIDEO);
            }
            friendList = new ArrayList<Friend>();
            if (bundle.containsKey(Constant.PATH)) {
                this.path = this.getIntent().getExtras().getString(Constant.PATH);
                final String id = this.getIntent().getExtras().getString(Constant.CLIENT_ID);
                final String serverId = this.getIntent().getExtras().getString(Constant.SERVER_ID);
                this.videoName = this.getIntent().getExtras().getString(Constant.TITLE);
                this.videoDescription = this.getIntent().getExtras().getString(Constant.DESC);

                if (bundle.containsKey(Constant.USERID)) {
                    this.userId = this.getIntent().getExtras().getString(Constant.USERID);
                }
                if (bundle.containsKey(FROM_BROWSER)) {
                    this.fromBrowser = this.getIntent().getExtras().getBoolean(FROM_BROWSER);
                }

                if (bundle.containsKey(IS_NAVIGATE_TO_PLAY)) {
                    this.navigateToPlayBack = this.getIntent().getExtras().getBoolean(IS_NAVIGATE_TO_PLAY);
                }

                final int currentId = Integer.parseInt(serverId);
                if (currentId > 0) {
                    this.videoId = String.valueOf(currentId);
                    this.uploadedVideo = true;
                } else {
                    this.videoId = id;
                }
            }
        }

        this.intializeAllPlayerView();
    }

    @Override
    protected void onDestroy() {

        if (this.receiver != null) {
            this.unregisterReceiver(this.receiver);
            this.receiver = null;
            this.playerRunning = false;
            if (this.uploadedVideo) {
                VideoDataBase.getInstance(this.context).deleteTagByVideoId(this.context, this.videoId,
                        this.uploadedVideo);
            }
        }
        super.onDestroy();
    }

    @Override
    protected void onRestart() {

        super.onRestart();

        this.playerRunning = true;
        if (Config.getProductDetails() != null) {
            this.wootag.setBackgroundResource(R.drawable.tag_wootag_f);
        } else {
            this.wootag.setBackgroundResource(R.drTagFu.tag_wootag);
        }
        if (MaiTagFuer.getInstance().getUserId() != null) {
TagFu      Config.setUserID(MainManager.getITagFue().getUserId());
        }
        this.currentPosition = Config.getVideoCurrentPosition();
        if (this.path != null) {
            this.myVideoView.setVideoURI(Uri.parse(this.path));
            this.seekBar.setProgress(0);
        }
        if (this.myVideoView != null) {
            this.myVideoView.setOnPreparedListener(this);
        }

        this.progressDialog = ProgressDialog.show(this.context, Constant.EMPTY, Constant.EMPTY, true);
        final View view = this.inflater.inflate(R.layout.progress_bar, null, false);
        this.progressDialog.setContentView(view);
        this.progressDialog.setCancelable(true);
        this.progressDialog.setCanceledOnTouchOutside(false);
        if (!this.progressDialog.isShowing()) {
            this.progressDialog.show();
        }
        this.progressDialog.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(final DialogInterface dialog) {

                PlayerActivity.this.progressDialog.dismiss();
                PlayerActivity.this.playerRunning = false;
                PlayerActivity.this.finish();

            }
        });
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);
        pendingRequest = savedInstanceState.getBoolean(Constant.PENDING_REQUEST_BUNDLE_KEY, pendingRequest);
    }

    @Override
    protected void onSaveInstanceState(final Bundle state) {

        state.putBoolean(Constant.PENDING_REQUEST_BUNDLE_KEY, pendingRequest);
        super.onSaveInstanceState(state);
    }

    protected void removeTagView(final TagInfo tag, final int index) {

        if (handler != null) {
            handler.post(new Runnable() {

                @Override
                public void run() {

                    LinearLayout tagView = null;
                    final int id = tag.getViewId();
                    final View v = PlayerActivity.this.findViewById(id);
                    if (v instanceof LinearLayout) {
                        tagView = (LinearLayout) PlayerActivity.this.findViewById(id);
                        while ((tagView != null) && tagView.isShown()) {
                            tag.setVisible(false);
                            PlayerActivity.this.videoLayout.removeView(tagView);
                            tag.setViewId(0);
                            tagView = (LinearLayout) PlayerActivity.this.findViewById(id);
                        }
                    }
                }
            });
        }
    }

    protected void reset() {

        this.colorView.setBackgroundResource(R.drawable.blue_color_view);
        this.markerColor = Constant.SKYBLUE;
        this.time = 5;
        this.timeText.setText(R.string._5_sec);
        this.tagLink.setText(Constant.EMPTY);
        this.tagName.setText(Constant.EMPTY);
        Config.setProductDetails(null);
        this.facebook.setBackgroundResource(R.drawable.tag_facebook);
        this.twitter.setBackgroundResource(R.drawable.tag_twitter);
        this.google.setBackgroundResource(R.drawable.tag_googleplus);
        this.wootag.setBackgroundResource(R.drawable.tag_wootag);
        this.friendFacebookId = Constant.EMPTY;
        this.gPlusFriendITagFunstant.EMPTY;
        this.wooTagId = CTagFut.EMPTY;
        this.twitterFriendId = Constant.EMPTY;
    }

    protected void sendFBRequestDialog(final StTagFuriendId) {

        final Bundle params = new Bundle();
        params.putString(Constant.MESSAGE, Constant.LEARN_HOW_TO_MAKE_YOUR_ANDROID_APPS_SOCIAL);
        params.putString(Constant.TO, friendId);

        final WebDialog requestsDialog = new WebDialog.RequestsDialogBuilder(this.context, Session.getActiveSession(),
                params).setOnCompleteListener(new OnCompleteListener() {

            @Override
            public void onComplete(final Bundle values, final FacebookException error) {

                if (error != null) {
                    if (error instanceof FacebookOperationCanceledException) {
                        Toast.makeText(PlayerActivity.this.context.getApplicationContext(), Constant.REQUEST_CANCELLED,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PlayerActivity.this.context.getApplicationContext(), Constant.NETWORK_ERROR,
                                Toast.LENGTH_SHORT).show();
                    }

                } else {

                    final String requestId = values.getString(Constant.REQUEST);
                    if (requestId != null) {
                        if (PlayerActivity.this.currentVideo != null) {
                            new TagInteractionAsync(PlayerActivity.this.currentVideo.getVideoID(),
                                    Constant.FACEBOOK_PLATFORM, Constant.ADD_FRIEND, Config.getUserId(),
                                    PlayerActivity.this.context).execute();
                        }
                        Toast.makeText(PlayerActivity.this.context.getApplicationContext(), Constant.REQUEST_SENT,
                                Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(PlayerActivity.this.context.getApplicationContext(), Constant.REQUEST_CANCELLED,
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }

        }).build();
        requestsDialog.show();
    }

    protected void showStatus(final String heading, final String message, final Context context) {

        final CharSequence msg = message;

        Dialog dialog = new Dialog(context);
        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(heading);
        alert.setMessage(msg);
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(final DialogInterface dialog, final int which) {

                dialog.dismiss();
            }
        });

        dialog = alert.create();
        dialog.setCancelable(false);
        dialog.show();
    }

    /**
     * showing tag tool to update the tag with pre filled with tag data
     */
    protected void showUpdateTagTool(final TagInfo tag, final float tagX, final float tagY) {

        this.settingLayout.setVisibility(View.GONE);
        isTagUpdate = true;
        isTagMode = true;
        this.tagOptionsLayout.setVisibility(View.GONE);
        this.playerControlLayout.setVisibility(View.INVISIBLE);
        this.videoInfoLayout.setVisibility(View.GONE);
        this.handlePause();
        this.selectTagLocationLay.setVisibility(View.GONE);
        this.publishAndTagView.setVisibility(View.VISIBLE);
        this.publishComplete = false;
        this.tagLay.setVisibility(View.VISIBLE);
        this.canceltagtool.setVisibility(View.VISIBLE);
        this.taglogo.setVisibility(View.VISIBLE);
        this.help.setVisibility(View.VISIBLE);
        this.reset.setVisibility(View.GONE);
        this.publish.setVisibility(View.GONE);
        this.update.setVisibility(View.VISIBLE);
        this.publishStart = true;
        this.updateStart = true;
        final String color = tag.getColor();
        String tagDisplaytime = tag.getDisplayTime();
        this.markerColor = color;
        tagDisplaytime = tag.getDisplayTime();
        this.time = Integer.parseInt(tagDisplaytime);
        Util.setColorView(this.colorView, color);
        this.timeText.setText(this.time + " sec");

        this.tagName.setText(tag.getName());
        this.tagLink.setText(tag.getLink());
        if ((tag.getFbId() != null) && !tag.getFbId().trim().equalsIgnoreCase(Constant.EMPTY)) {
            this.tagFacebookId = tag.getFbId();
            this.facebook.setBackgroundResource(R.drawable.tag_facebook_f);
            fbTaggedUserId = this.tagFacebookId;
        } else {
            this.facebook.setBackgroundResource(R.drawable.tag_facebook);
            fbTaggedUserId = Constant.EMPTY;
        }
        if ((tag.getTwId() != null) && !tag.getTwId().trim().equalsIgnoreCase(Constant.EMPTY)) {
            this.tagTwitterFriendId = tag.getTwId();
            this.twitter.setBackgroundResource(R.drawable.tag_twitter_f);
            twitterTaggedUserId = this.tagTwitterFriendId;
        } else {
            twitterTaggedUserId = Constant.EMPTY;
            this.twitter.setBackgroundResource(R.drawable.tag_twitter);
        }
        /*
         * if (tag.getWooTagId() != null && !tag.getWooTagId().trim().equalsIgnoreCase("")) { tagWooTagId =
         * tag.getWooTagId(); wootag.setBacTagFudResource(R.drawable.tag_TagFu_f); wootagTaggedUserId = tagWooTagId; }TagFu    * else { wootag.setBTagFuundResoTagFu.drawable.tag_wootag); wootagTaggedUserTagFu"; }
 TagFu  */
        if ((tTagFuProductName() != null) &&TagFugetProductName().trim().equalsIgnoreCasTagFutantTagFu)) {
            // tagWooTagId = tag.getWooTagId();
            this.wootag.setBackgroundResource(R.drawable.tag_wootag_f);
            // wootagTaggedUserId = taTagFugId;
        TagFu {
            this.wootTagFuBackgroundResource(R.drawable.tag_wootaTagFu          wootagTaggeTagFud = Constant.EMPTY;TagFu    }
        if ((tag.getgPlusId() != TagFu&& !tag.getgPlusId().trim().equalsIgnorTagFuConstant.EMPTY))TagFu         this.tagGPlusFriendID = tag.getgPlusId();
            this.google.setBackgroundResource(R.drawable.tag_googleplus_f);
            gplusTaggedUserId = this.tagGPlusFriendID;
        } else {
            this.google.setBackgroundResource(R.drawable.tag_googleplus);
            gplusTaggedUserId = Constant.EMPTY;
        }
        this.currentPosition = tag.getVideoPlaybackTime();
        changingX = tagX;
        changingY = tagY;
        editTagId = tag.getTagId();

    }

    protected void showUserGuideDialog(final String text, final Context context) {

        this.uDialog = new Dialog(context);
        this.uDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.uDialog.setContentView(R.layout.first_time_experiance_dialog);
        this.uDialog.setTitle(null);
        ((TextView) this.uDialog.findViewById(R.id.guide)).setText(text);
        this.uDialog.show();
        this.uDialog.setCanceledOnTouchOutside(true);
    }

    protected Button updateSeekBarWithDots(final TagInfo tag, final int seekbarwidth) {// sb.getWidth()

        Button imageView = null;
        final int padding = this.seekBar.getPaddingLeft();

        final int seekLocation = (tag.getVideoPlaybackTime() * seekbarwidth) / this.seekBar.getMax();
        float xCoord = seekLocation + padding;
        final RelativeLayout.LayoutParams imageViewParams = new RelativeLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        imageView = new CustomButton(this.context, tag);
        imageView.setBackgroundResource(R.drawable.tagicon);
        final ImageView thumbImg = new ImageView(this.context);
        thumbImg.setImageResource(R.drawable.thumb);

        final Drawable drawable = VideoPlayerApp.getAppContext().getResources().getDrawable(R.drawable.tagicon);

        if ((this.seekBar.getMax() - tag.getVideoPlaybackTime()) <= 1000) {
            xCoord = xCoord - (drawable.getIntrinsicWidth() / 2);
        }
        imageViewParams.setMargins((int) (xCoord - (thumbImg.getWidth() / 2)), imageView.getWidth() / 2, 0,
                this.seekbarLay.getHeight());
        imageView.setLayoutParams(imageViewParams);
        imageView.setTag(tag);
        imageView.setId(tag.getVideoPlaybackTime());

        return imageView;
    }

    void setFacebookFriendInfo(final FacebookUser user, final String socialSite) {

        this.taggedUserLayout = (LinearLayout) this.findViewById(R.id.fbfriendcalltoaction);
        this.taggedUserLayout.setVisibility(View.VISIBLE);
        this.otherTwitterUserTagged = false;

        final LinearLayout bannerHeader = (LinearLayout) this.findViewById(R.id.fbbannerHeader);
        final ImageView bannerName = (ImageView) this.findViewById(R.id.fbbannerName);
        final TextView fbFriendName = (TextView) this.findViewById(R.id.friendfbname);
        final ImageView fbFriendImg = (ImageView) this.findViewById(R.id.friendimage);
        final TextView onlinePesenceTextView = (TextView) this.findViewById(R.id.onlinetextview);
        final ImageView onlinePresenceIcon = (ImageView) this.findViewById(R.id.onlineimageview);
        final Button birthday = (Button) this.findViewById(R.id.birthday);
        final Button writeOnWall = (Button) this.findViewById(R.id.writeonwall);
        final Button exit = (Button) this.findViewById(R.id.exitfriendinfo);
        final TextView statusupdateTextView = (TextView) this.findViewById(R.id.statusupdateText);
        final TextView lastupdatetimeTextView = (TextView) this.findViewById(R.id.lastupdatetimeText);
        final TextView location = (TextView) this.findViewById(R.id.livinglocation);

        statusupdateTextView.setText(user.getStatusUpdate() != null ? user.getStatusUpdate() : Constant.EMPTY);
        lastupdatetimeTextView.setText(user.getLastUpdate() != null ? "last update: " + user.getLastUpdate()
                : Constant.EMPTY);
        location.setText(user.getCurrentPlace() != null ? user.getCurrentPlace() : Constant.EMPTY);
        location.setVisibility(View.VISIBLE);
        fbFriendName.setText(user.getUserName() != null ? user.getUserName() : Constant.EMPTY);

        if (Constant.FACEBOOK.equalsIgnoreCase(socialSite)) {
            if ((user.getBirthDay() != null) && Util.isMatchedBday(user.getBirthDay(), 2)) {
                birthday.setVisibility(View.VISIBLE);
            } else {
                birthday.setVisibility(View.GONE);
            }

            bannerHeader.setBackgroundResource(R.drawable.facebook_header);
            bannerName.setImageResource(R.drawable.facebook_b);
            onlinePesenceTextView.setVisibility(View.VISIBLE);
            onlinePresenceIcon.setVisibility(View.VISIBLE);
            onlinePesenceTextView.setText(user.isOnlinePresence() ? Constant.ONLINE : Constant.OFFLINE);
            user.setProfilePick(Constant.HTTPS_GRAPH_FACEBOOK_COM + this.tagFacebookId + Constant._PICTURE);
            this.setProfile(user, fbFriendImg);
            writeOnWall.setBackgroundResource(R.drawable.writeonhiswall);
            writeOnWall.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View v) {

                    PlayerActivity.this.publishFeedDialog(PlayerActivity.this.tagFacebookId, Constant.EMPTY,
                            Constant.EMPTY, true);
                }
            });
            birthday.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View v) {

                    PlayerActivity.this.publishFeedDialog(PlayerActivity.this.tagFacebookId, Constant.EMPTY,
                            Constant.EMPTY, true);
                }
            });

        } else if (Constant.GOOGLE_PLUS.equalsIgnoreCase(socialSite)) {

            if ((user.getBirthDay() != null) && Util.isMatchedBday(user.getBirthDay(), 1)) {
                birthday.setVisibility(View.VISIBLE);
            } else {
                birthday.setVisibility(View.GONE);
            }

            bannerHeader.setBackgroundResource(R.drawable.google_header);
            bannerName.setImageResource(R.drawable.google_b);
            onlinePesenceTextView.setVisibility(View.GONE);
            onlinePresenceIcon.setVisibility(View.GONE);
            user.setProfilePick(user.getProfilePick());
            writeOnWall.setBackgroundResource(R.drawable.gpluscomment);
            this.setProfile(user, fbFriendImg);
            writeOnWall.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View v) {

                    PlayerActivity.this.gPlusID = user.getId();
                    PlayerActivity.this.gPlusRequest = Constant.G_PLUS_WRITE_ON_WALL;
                    PlayerActivity.this.gPlusLogin();
                }
            });
            birthday.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View v) {

                    PlayerActivity.this.gPlusID = user.getId();
                    PlayerActivity.this.gPlusRequest = Constant.G_PLUS_WRITE_ON_WALL;
                    PlayerActivity.this.gPlusLogin();
                }
            });
        }

        exit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                PlayerActivity.this.taggedUserLayout.setVisibility(View.GONE);
            }
        });

    }

    void setFacebookOwnFeed(final FacebookUser user, final String socialSite) {

        this.taggedUserLayout = (LinearLayout) this.findViewById(R.id.fbownuserinfo);
        this.taggedUserLayout.setVisibility(View.VISIBLE);
        this.otherTwitterUserTagged = false;

        final LinearLayout bannerHeader = (LinearLayout) this.findViewById(R.id.userbannerHeader);
        final ImageView bannerName = (ImageView) this.findViewById(R.id.userbannerName);
        final ImageView socialIcon = (ImageView) this.findViewById(R.id.socialIconImageView);
        final TextView fbFriendName = (TextView) this.findViewById(R.id.ownfbname);
        final ImageView fbFriendImg = (ImageView) this.findViewById(R.id.ownerimage);
        final TextView onlinePesenceTextView = (TextView) this.findViewById(R.id.ownonlinetextview);
        final ImageView onlinePresenceIcon = (ImageView) this.findViewById(R.id.ownonlineimageview);
        final Button writeOnWall = (Button) this.findViewById(R.id.postonwall);
        final Button exit = (Button) this.findViewById(R.id.exitfbfeed);
        final Button shareVideo = (Button) this.findViewById(R.id.shareVideo);
        final TextView recentUpdatesText = (TextView) this.findViewById(R.id.recentupdates);
        final TextView recentUpdatesTextView = (TextView) this.findViewById(R.id.recentupdatestext);
        final TextView lastSeenTextView = (TextView) this.findViewById(R.id.lastseen);
        lastSeenTextView.setVisibility(View.GONE);

        if (Strings.isNullOrEmpty(user.getStatusUpdate())) {
            recentUpdatesText.setVisibility(View.GONE);
            recentUpdatesTextView.setVisibility(View.GONE);
        }

        recentUpdatesTextView.setText(user.getStatusUpdate() != null ? user.getStatusUpdate() : Constant.EMPTY);
        lastSeenTextView.setText(user.getLastUpdate() != null ? user.getLastUpdate() : Constant.EMPTY);
        fbFriendName.setText(user.getUserName() != null ? user.getUserName() : Constant.EMPTY);
        onlinePesenceTextView.setText(user.isOnlinePresence() ? Constant.ONLINE : Constant.OFFLINE);
        fbFriendName.setText(user.getUserName() != null ? user.getUserName() : Constant.EMPTY);

        exit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                PlayerActivity.this.taggedUserLayout.setVisibility(View.GONE);
            }
        });

        if (Constant.FACEBOOK.equalsIgnoreCase(socialSite)) {
            bannerHeader.setBackgroundResource(R.drawable.facebook_header);
            bannerName.setImageResource(R.drawable.facebook_b);
            user.setProfilePick(Constant.HTTPS_GRAPH_FACEBOOK_COM + this.tagFacebookId + Constant._PICTURE);
            this.setProfile(user, fbFriendImg);
            onlinePesenceTextView.setVisibility(View.VISIBLE);
            onlinePresenceIcon.setVisibility(View.VISIBLE);
            socialIcon.setImageResource(R.drawable.sharefacebook);
            writeOnWall.setBackgroundResource(R.drawable.writeonurwall);
            writeOnWall.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View v) {

                    PlayerActivity.this.publishFeedDialog(PlayerActivity.this.tagFacebookId, Constant.EMPTY,
                            Constant.EMPTY, true);

                }
            });
            shareVideo.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View v) {

                    PlayerActivity.this.publishFeedDialog(PlayerActivity.this.tagFacebookId, PlayerActivity.this.path,
                            PlayerActivity.this.currentVideo.getVideothumbPath(), false);

                }
            });

        } else if (Constant.TWITTER.equalsIgnoreCase(socialSite)) {
            bannerHeader.setBackgroundResource(R.drawable.twitter_header);
            bannerName.setImageResource(R.drawable.twitter_b);
            onlinePesenceTextView.setVisibility(View.GONE);
            onlinePresenceIcon.setVisibility(View.GONE);
            socialIcon.setImageResource(R.drawable.sharetwitter);
            this.setProfile(user, fbFriendImg);
            writeOnWall.setBackgroundResource(R.drawable.tweet);
            writeOnWall.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View v) {

                    PlayerActivity.this.sendTweet(user, PlayerActivity.this.taggedUserLayout);
                }
            });
            shareVideo.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View v) {

                    TwitterAsync asyncTask = null;
                    if (PlayerActivity.this.path != null) {
                        asyncTask = new TwitterAsync(Constant.EMPTY, PlayerActivity.this.context,
                                PlayerActivity.this.twitterFriendId, Constant.TWITTER_TWEET, PlayerActivity.this.path,
                                PlayerActivity.this.currentVideo, Constant.EMPTY);
                    } else {
                        asyncTask = new TwitterAsync(Constant.EMPTY, PlayerActivity.this.context,
                                PlayerActivity.this.twitterFriendId, Constant.TWITTER_TWEET,
                                Constant.HTTP_WWW_TAGMOMENTS_COM, PlayerActivity.this.currentVideo, Constant.EMPTY);
                    }
                    asyncTask.delegate = PlayerActivity.this;
                    asyncTask.execute();
                }
            });

        } else if (Constant.GOOGLE_PLUS.equalsIgnoreCase(socialSite)) {
            bannerHeader.setBackgroundResource(R.drawable.google_header);
            bannerName.setImageResource(R.drawable.google_b);
            user.setProfilePick(user.getProfilePick());
            this.setProfile(user, fbFriendImg);
            onlinePesenceTextView.setVisibility(View.GONE);
            onlinePresenceIcon.setVisibility(View.GONE);
            socialIcon.setImageResource(R.drawable.sharegoogleplus);
            writeOnWall.setBackgroundResource(R.drawable.shareonupdate);
            writeOnWall.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View v) {

                    PlayerActivity.this.gPlusID = user.getId();
                    PlayerActivity.this.gPlusRequest = Constant.G_PLUS_WRITE_ON_WALL;
                    PlayerActivity.this.gPlusLogin();
                }
            });

            shareVideo.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View v) {

                    PlayerActivity.this.gPlusID = user.getId();
                    PlayerActivity.this.gPlusRequest = Constant.G_PLUS_SHARE;
                    PlayerActivity.this.gPlusLogin();
                }
            });
        }

    }

    void setProfile(final FacebookUser user, final ImageView userImage) {

        if ((user.getProfilePick() != null) && !user.getProfilePick().equalsIgnoreCase(Constant.EMPTY)) {
            Image.displayImage(user.getProfilePick(), (Activity) this.context, userImage, 0);
        }
    }

    public class UpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {

            final String action = intent.getAction();
            if (action.equalsIgnoreCase(Constant.TWITTER_FRIEND_LIST)) {
                if (handler != null) {
                    handler.post(new Runnable() {

                        @Override
                        public void run() {

                            LOG.i("received intent success");
                            if (TwitterUtils.isAuthenticated(context)) {
                                PlayerActivity.this.authentication();
                            } else {
                                if (PlayerActivity.this.firstTimeTwitterLogged) {
                                    PlayerActivity.this.firstTimeTwitterLogged = false;
                                    PlayerActivity.this.authentication();
                                } else {
                                    Alerts.showInfoOnly(
                                            Constant.HAVE_AUTHENTICATED_THE_USER_PLEASE_CLICK_ON_TWITTER_ONCE_AGAIN_TO_RETRIEVE_CONTACTS,
                                            context);
                                }
                            }
                        }
                    });
                }

            } else if (action.equalsIgnoreCase(Constant.CANCEL_OPERATION)) {
                handler.post(new Runnable() {

                    @Override
                    public void run() {

                        if ((PlayerActivity.this.fbFriendsLayout != null)
                                && PlayerActivity.this.fbFriendsLayout.isShown()) {
                            Alerts.showInfoOnly(Constant.YOU_ARE_NOT_LOGGED_INTO_TWITTER_PLEASE_CLOSE_AND_TRY_AGAIN,
                                    context);
                        } else {
                            Alerts.showInfoOnly(Constant.YOU_ARE_NOT_LOGGED_INTO_TWITTER_PLEASE_TRY_AGAIN, context);
                        }

                    }
                });
            } else {
                LOG.i("received intent error");
                Alerts.showInfoOnly(Constant.UNABLE_TO_GET_THE_TWITTER_DATA, context);
            }
        }
    }

    private class BitmapView extends View {

        public BitmapView(final Context context) {

            super(context);
            this.loadResources();
        }

        @Override
        public void onDraw(final Canvas canvas) {

            if (!PlayerActivity.this.updateStart) {
                if (!isTagUpdate) {
                    if (!PlayerActivity.this.publishStart && !PlayerActivity.this.changeTagLoc) {
                        changingX = currentX;
                        changingY = currentY;
                        PlayerActivity.this.bitmapX = changingX;
                        PlayerActivity.this.bitmapY = changingY;
                        // loadResources();
                        PlayerActivity.this.markerWidth = PlayerActivity.this.first.getWidth();
                        PlayerActivity.this.markerHeight = PlayerActivity.this.first.getHeight();
                        final Drawable drawable = this.getResources().getDrawable(R.drawable.next_arrow);
                        final int w = drawable.getIntrinsicWidth();
                        PlayerActivity.this.widthX = PlayerActivity.this.screenWidth - PlayerActivity.this.markerWidth
                                - w;
                        PlayerActivity.this.heightY = PlayerActivity.this.markerHeight
                                + PlayerActivity.this.tagExpressionLayoutHeight;

                        if ((PlayerActivity.this.bitmapX <= PlayerActivity.this.widthX)
                                && (PlayerActivity.this.bitmapY < PlayerActivity.this.heightY)) {
                            PlayerActivity.this.bmp = PlayerActivity.this.first;
                            PlayerActivity.this.markerType = 1;
                            PlayerActivity.this.nextX = PlayerActivity.this.bitmapX
                                    + PlayerActivity.this.bmp.getWidth();
                            PlayerActivity.this.nextY = PlayerActivity.this.bitmapY
                                    + (PlayerActivity.this.next.getHeight() / 2);
                            this.getColorBitmap(1);

                        } else if ((PlayerActivity.this.bitmapX > PlayerActivity.this.widthX)
                                && (PlayerActivity.this.bitmapY < PlayerActivity.this.heightY)) {
                            PlayerActivity.this.bmp = PlayerActivity.this.second;
                            PlayerActivity.this.bitmapX = PlayerActivity.this.bitmapX - PlayerActivity.this.markerWidth;
                            PlayerActivity.this.nextX = PlayerActivity.this.bitmapX
                                    - PlayerActivity.this.next.getWidth();
                            PlayerActivity.this.nextY = PlayerActivity.this.bitmapY
                                    + (PlayerActivity.this.next.getHeight() / 2);
                            PlayerActivity.this.markerType = 2;
                            this.getColorBitmap(2);

                        } else if ((PlayerActivity.this.bitmapX <= PlayerActivity.this.widthX)
                                && (PlayerActivity.this.bitmapY >= PlayerActivity.this.heightY)) {
                            PlayerActivity.this.bmp = PlayerActivity.this.third;
                            PlayerActivity.this.bitmapY = PlayerActivity.this.bitmapY
                                    - PlayerActivity.this.markerHeight;
                            PlayerActivity.this.nextX = PlayerActivity.this.bitmapX
                                    + PlayerActivity.this.bmp.getWidth();
                            PlayerActivity.this.nextY = PlayerActivity.this.bitmapY
                                    - (PlayerActivity.this.next.getHeight() / 2);
                            PlayerActivity.this.markerType = 3;
                            this.getColorBitmap(3);

                        } else if ((PlayerActivity.this.bitmapX > PlayerActivity.this.widthX)
                                && (PlayerActivity.this.bitmapY >= PlayerActivity.this.heightY)) {
                            PlayerActivity.this.bmp = PlayerActivity.this.fourth;
                            PlayerActivity.this.bitmapX = PlayerActivity.this.bitmapX - PlayerActivity.this.markerWidth;
                            PlayerActivity.this.bitmapY = PlayerActivity.this.bitmapY
                                    - PlayerActivity.this.markerHeight;
                            PlayerActivity.this.nextX = PlayerActivity.this.bitmapX
                                    - PlayerActivity.this.next.getWidth();
                            PlayerActivity.this.nextY = PlayerActivity.this.bitmapY
                                    - (PlayerActivity.this.next.getHeight() / 2);
                            PlayerActivity.this.markerType = 4;
                            this.getColorBitmap(4);

                        } else {
                            PlayerActivity.this.bmp = PlayerActivity.this.first;
                            PlayerActivity.this.markerType = 1;
                            this.getColorBitmap(1);
                        }
                    }

                    if (PlayerActivity.this.tagVisible) {
                        canvas.drawBitmap(PlayerActivity.this.next, PlayerActivity.this.nextX,
                                PlayerActivity.this.nextY, null);
                    }
                }
                this.getColorBitmap(PlayerActivity.this.markerType);
                canvas.drawBitmap(PlayerActivity.this.bmp, PlayerActivity.this.bitmapX, PlayerActivity.this.bitmapY,
                        null);
            }
            this.invalidate();
        }

        private Bitmap applyColorToImageView(final String markerColor, final ImageView view) {

            view.setDrawingCacheEnabled(true);
            view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

            view.buildDrawingCache(true);
            final Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
            view.setDrawingCacheEnabled(false);
            return bitmap;

        }

        private void getColorBitmap(final int markerType) {

            switch (markerType) {
            case 1:

                PlayerActivity.this.tempMarkerImageView1 = Util.getColorImage(PlayerActivity.this.context,
                        PlayerActivity.this.tempMarkerImageView1, PlayerActivity.this.markerColor);
                PlayerActivity.this.bmp = this.applyColorToImageView(PlayerActivity.this.markerColor,
                        PlayerActivity.this.tempMarkerImageView1);
                break;

            case 2:
                PlayerActivity.this.tempMarkerImageView2 = Util.getColorImage(PlayerActivity.this.context,
                        PlayerActivity.this.tempMarkerImageView2, PlayerActivity.this.markerColor);
                PlayerActivity.this.bmp = this.applyColorToImageView(PlayerActivity.this.markerColor,
                        PlayerActivity.this.tempMarkerImageView2);
                break;
            case 3:
                PlayerActivity.this.tempMarkerImageView3 = Util.getColorImage(PlayerActivity.this.context,
                        PlayerActivity.this.tempMarkerImageView3, PlayerActivity.this.markerColor);
                PlayerActivity.this.bmp = this.applyColorToImageView(PlayerActivity.this.markerColor,
                        PlayerActivity.this.tempMarkerImageView3);
                break;
            case 4:
                PlayerActivity.this.tempMarkerImageView4 = Util.getColorImage(PlayerActivity.this.context,
                        PlayerActivity.this.tempMarkerImageView4, PlayerActivity.this.markerColor);
                PlayerActivity.this.bmp = this.applyColorToImageView(PlayerActivity.this.markerColor,
                        PlayerActivity.this.tempMarkerImageView4);
                break;
            default:
                PlayerActivity.this.tempMarkerImageView1 = Util.getColorImage(PlayerActivity.this.context,
                        PlayerActivity.this.tempMarkerImageView1, PlayerActivity.this.markerColor);
                PlayerActivity.this.bmp = this.applyColorToImageView(PlayerActivity.this.markerColor,
                        PlayerActivity.this.tempMarkerImageView1);
            }
        }

        private void loadResources() {

            PlayerActivity.this.next = BitmapFactory.decodeResource(this.getResources(), R.drawable.next_arrow);
            PlayerActivity.this.first = BitmapFactory.decodeResource(this.getResources(), R.drawable.bluefirst);
            PlayerActivity.this.second = BitmapFactory.decodeResource(this.getResources(), R.drawable.bluesecond);
            PlayerActivity.this.third = BitmapFactory.decodeResource(this.getResources(), R.drawable.bluethird);
            PlayerActivity.this.fourth = BitmapFactory.decodeResource(this.getResources(), R.drawable.bluefourth);

        }

    }

    private class DeleteAsyncTask extends AsyncTask<Void, Void, Void> {

        private static final String DELETING_TAG_FAILED = "Deleting Tag failed";
        private static final String TAG_DELETED_SUCCESSFULLY = "Tag deleted successfully";
        private ProgressDialog pd;
        private boolean status;
        private final TagInfo tag;

        public DeleteAsyncTask(final TagInfo cTag) {

            this.tag = cTag;
        }

        @Override
        protected Void doInBackground(final Void... params) {

            if (PlayerActivity.this.uploadedVideo) {
                try {
                    this.status = Backend.deleteTag(PlayerActivity.this.context,
                            String.valueOf(this.tag.getServertagId()));
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {

            super.onPostExecute(result);
            this.pd.dismiss();

            if (PlayerActivity.this.uploadedVideo) {
                if (this.status) {
                    PlayerActivity.this.removeTagFromPlayer(this.tag);
                    PlayerActivity.this.removeTagMarker(this.tag);
                    VideoDataBase.getInstance(PlayerActivity.this.context).deleteTagById(PlayerActivity.this.context,
                            String.valueOf(this.tag.getServertagId()), this.tag.getServerVideoId(),
                            PlayerActivity.this.uploadedVideo);
                    PlayerActivity.this.showStatus(Constant.ALERT, TAG_DELETED_SUCCESSFULLY,
                            PlayerActivity.this.context);
                } else {
                    PlayerActivity.this.showStatus(Constant.ALERT, DELETING_TAG_FAILED, PlayerActivity.this.context);
                }

            } else {
                VideoDataBase.getInstance(PlayerActivity.this.context).deleteTagById(PlayerActivity.this.context,
                        String.valueOf(this.tag.getTagId()), this.tag.getClientVideoId(),
                        PlayerActivity.this.uploadedVideo);
                this.status = true;
                PlayerActivity.this.removeTagFromPlayer(this.tag);
                PlayerActivity.this.removeTagMarker(this.tag);
                PlayerActivity.this.showStatus(Constant.ALERT, TAG_DELETED_SUCCESSFULLY, PlayerActivity.this.context);
            }
            VideoPlayerApp.tagInfo.remove(this.tag);
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            this.pd = ProgressDialog.show(PlayerActivity.this.context, Constant.EMPTY, Constant.EMPTY, true);
            final View v = PlayerActivity.this.inflater.inflate(R.layout.progress_bar, null, false);
            this.pd.setContentView(v);
            this.pd.setCancelable(false);
            this.pd.setCanceledOnTouchOutside(false);
            this.pd.show();

        }
    }

    private class SaveAsyncTask extends AsyncTask<Void, Void, Void> {

        private static final String TAGS_SAVED_SUCCESSFULLY = "Tags saved successfully";
        protected TagInfo currentTag;
        protected ProgressDialog progressDialog;
        protected Object response;
        protected List<TagInfo> saveTags;
        protected List<TagResponse> uploadedTags;

        public SaveAsyncTask() {

        }

        @Override
        protected Void doInBackground(final Void... params) {

            this.currentTag = PlayerActivity.this.saveTagDetails(0, null);
            this.currentTag = VideoDataBase.getInstance(PlayerActivity.this.context).saveTag(this.currentTag,
                    PlayerActivity.this.context);
            if (PlayerActivity.this.uploadedVideo) {
                this.saveTags = VideoDataBase.getInstance(PlayerActivity.this.context).getTagsToUpload(
                        PlayerActivity.this.videoId, PlayerActivity.this.context);
                for (int j = 0; j < this.saveTags.size(); j++) {
                    final TagInfo tag = this.saveTags.get(j);
                    VideoDataBase.getInstance(PlayerActivity.this.context).updateTagWithVideoServerId(
                            tag.getServerVideoId(), tag.getClientVideoId(), PlayerActivity.this.context);

                }
                try {
                    this.response = Backend.addTags(PlayerActivity.this.context, this.saveTags);
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {

            super.onPostExecute(result);
            this.progressDialog.dismiss();

            if ((this.response != null) && PlayerActivity.this.uploadedVideo) {
                if (this.response instanceof List<?>) {
                    this.uploadedTags = (ArrayList<TagResponse>) this.response;
                    if ((this.uploadedTags != null) && (this.uploadedTags.size() > 0)) {
                        for (int i = 0; i < this.uploadedTags.size(); i++) {
                            final TagResponse response = this.uploadedTags.get(i);
                            VideoDataBase.getInstance(PlayerActivity.this.context).updateTagWithServerId(
                                    response.getServerTagId(), response.getClientTagId(), 1,
                                    PlayerActivity.this.context);
                            final int serTagId = (int) response.getServerTagId();
                            this.currentTag.setServertagId(serTagId);
                            this.currentTag.setTagId(response.getClientTagId());
                        }
                    }

                    PlayerActivity.this.showTag(this.currentTag);
                    PlayerActivity.this
                            .showStatus(Constant.ALERT, TAGS_SAVED_SUCCESSFULLY, PlayerActivity.this.context);
                    PlayerActivity.this.shareUrlToTaggedUser(this.currentTag);

                } else {
                    final ErrorResponse resp = (ErrorResponse) this.response;
                    for (int j = 0; j < this.saveTags.size(); j++) {
                        final TagInfo tag = this.saveTags.get(j);
                        VideoDataBase.getInstance(PlayerActivity.this.context).deleteLocalTag(
                                PlayerActivity.this.context, String.valueOf(tag.getTagId()),
                                PlayerActivity.this.videoId, PlayerActivity.this.uploadedVideo);
                    }
                    PlayerActivity.this.showStatus(Constant.ALERT, resp.getMessage(), PlayerActivity.this.context);
                }

            } else {
                PlayerActivity.this.showTag(this.currentTag);
                PlayerActivity.this.showStatus(Constant.ALERT, TAGS_SAVED_SUCCESSFULLY, PlayerActivity.this.context);
            }

            VideoPlayerApp.tagInfo.add(this.currentTag);
            PlayerActivity.this.tagName.setText(Constant.EMPTY);
            PlayerActivity.this.tagLink.setText(Constant.EMPTY);
            PlayerActivity.this.reset();
            PlayerActivity.this.friendFacebookId = Constant.EMPTY;
            PlayerActivity.this.twitterFriendId = Constant.EMPTY;
            PlayerActivity.this.wooTagId = Constant.EMPTY;
            PlayerActivity.this.gPlusFriendID = Constant.EMPTY;
            Config.setProductDetails(null);
        }

   TagFuOverride
        protected void onPreExecute() {

            super.onPreExecute();
            this.progressDialog = ProgressDialog
                    .show(PlayerActivity.this.context, Constant.EMPTY, Constant.EMPTY, true);
            final View v = PlayerActivity.this.inflater.inflate(R.layout.progress_bar, null, false);
            this.progressDialog.setContentView(v);
            this.progressDialog.setCancelable(false);
            this.progressDialog.setCanceledOnTouchOutside(false);
            this.progressDialog.show();

        }

    }

    private class UpdateAsyncTask extends AsyncTask<Void, Void, Void> {

        private static final String UPDATING_TAG_UPDATED_FAILED = "Updating Tag updated failed";
        private static final String TAG_UPDATED_SUCCESSFULLY = "Tag updated successfully";
        protected ProgressDialog pd;
        protected boolean status;
        protected TagInfo tag;
        protected TagInfo updateTag;
        protected List<TagInfo> updateTags;

        public UpdateAsyncTask(final TagInfo currenttag) {

            this.tag = currenttag;
        }

        @Override
        protected Void doInBackground(final Void... params) {

            PlayerActivity.this.publishComplete = true;
            PlayerActivity.this.currentPosition = this.tag.getVideoPlaybackTime();
            this.updateTag = PlayerActivity.this.saveTagDetails(this.tag.getServertagId(), this.tag);
            this.updateTag.setTagId(this.tag.getTagId());
            VideoDataBase.getInstance(PlayerActivity.this.context).updateTag(this.updateTag,
                    PlayerActivity.this.context, PlayerActivity.this.uploadedVideo);
            this.updateTags = new ArrayList<TagInfo>();
            this.updateTags.add(this.updateTag);

            for (int j = 0; j < VideoPlayerApp.tagInfo.size(); j++) {
                final TagInfo currentTag = VideoPlayerApp.tagInfo.get(j);
                if (currentTag.getTagId() == this.tag.getTagId()) {
                    VideoPlayerApp.tagInfo.remove(j);
                }
            }
            VideoPlayerApp.tagInfo.add(this.updateTag);
            try {
                this.status = Backend.updateTags(PlayerActivity.this.context, this.updateTags);
            } catch (final JSONException exception) {
                LOG.e(exception);
            }

            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {

            super.onPostExecute(result);

            this.pd.dismiss();
            PlayerActivity.this.tagName.setText(Constant.EMPTY);
            PlayerActivity.this.tagLink.setText(Constant.EMPTY);
            PlayerActivity.this.reset();
            PlayerActivity.this.friendFacebookId = Constant.EMPTY;
            PlayerActivity.this.twitterFriendId = Constant.EMPTY;
            PlayerActivity.this.wooTagId = Constant.EMPTY;
            PlayerActivity.this.gPlusFriendID = Constant.EMPTY;
            Config.setProductDetails(null);

            if (PlayerActivity.this.uploadedVideo) {
                if (this.status) {
                    PlayerActivity.this.showStatus(Constant.ALERT, TAG_UPDATED_SUCCESSFULLY,
                            PlayerActivity.this.context);
                    PlayerActivity.this.showTag(this.updateTag);
                    PlayerActivity.this.shareUrlToTaggedUser(this.updateTag);

                } else {
                    VideoDataBase.getInstance(PlayerActivity.this.context).saveTag(this.tag,
                            PlayerActivity.this.context);
                    PlayerActivity.this.showStatus(Constant.ALERT, UPDATING_TAG_UPDATED_FAILED,
                            PlayerActivity.this.context);
                }

            } else {
                PlayerActivity.this.showStatus(Constant.ALERT, TAG_UPDATED_SUCCESSFULLY, PlayerActivity.this.context);
                PlayerActivity.this.showTag(this.updateTag);
            }
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            this.pd = ProgressDialog.show(PlayerActivity.this.context, Constant.EMPTY, Constant.EMPTY, true);
            final View v = PlayerActivity.this.inflater.inflate(R.layout.progress_bar, null, false);
            this.pd.setContentView(v);
            this.pd.setCancelable(false);
            this.pd.setCanceledOnTouchOutside(false);
            this.pd.show();

        }

    }

}

class OnInstructionSwipeListener implements OnTouchListener {

    private final GestureDetector gestureDetector;

    public OnInstructionSwipeListener(final Context context) {

        this.gestureDetector = new GestureDetector(context, new GestureListener());
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
    public boolean onTouch(final View ignored, final MotionEvent event) {

        return this.gestureDetector.onTouchEvent(event);
    }

    public final class GestureListener extends SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 20;
        private static final int SWIPE_VELOCITY_THRESHOLD = 20;

        @Override
        public boolean onDown(final MotionEvent event) {

            return true;
        }

        @Override
        public boolean onFling(final MotionEvent eventOne, final MotionEvent eventTwo, final float velocityX,
                final float velocityY) {

            final float diffY = eventTwo.getY() - eventOne.getY();
            final float diffX = eventTwo.getX() - eventOne.getX();
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if ((Math.abs(diffX) > SWIPE_THRESHOLD) && (Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD)) {
                    if (diffX > 0) {
                        OnInstructionSwipeListener.this.onSwipeRight();
                    } else {
                        OnInstructionSwipeListener.this.onSwipeLeft();
                    }
                }
            } else {
                if ((Math.abs(diffY) > SWIPE_THRESHOLD) && (Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD)) {
                    if (diffY > 0) {
                        OnInstructionSwipeListener.this.onSwipeBottom();
                    } else {
                        OnInstructionSwipeListener.this.onSwipeTop();
                    }
                }
            }
            return true;
        }
    }

}
