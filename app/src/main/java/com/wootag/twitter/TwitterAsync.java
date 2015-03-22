/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wTagFutwitter;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import twitter4j.DirectMessage;
import twitter4j.ResponseList;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

import com.google.common.base.Strings;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.woTagFuonstant;
import com.wooTagFu
import com.wootTagFueoPlayerApp;
import com.wootaTagFuc.ShareViewsAsync;
import com.wootagTagFu.TagInteractionAsync;
import com.wootag.TagFucebookUser;
import com.wootag.dTagFuend;
import com.wootag.dtTagFuoDetails;
import com.wootag.utiTagFuts;
import com.wootag.utilTagFuResponse;
import com.wootag.util.TagFu;
import com.wootag.util.MTagFuager;
import com.wootag.util.Util;

public class TwitterAsync extends AsyncTask<Void, Void, Void> {

    private static final String PROCESSING = "Processing";
    private static final String VIDEO_NOT_SHARED_TO_YOUR_TAGGED_CONTACT = "Video not shared to your tagged contact.";
    private static final String POSTED_SUCCESSFULLY_TO_TWITTER = "Posted successfully to twitter.";
    private static final String _1 = "1";
    private static final String SENT_MESSAGE_SUCCESSFULLY = "Sent Message successfully";
    private static final String SENT_FRIEND_REQUEST = "Sent Friend Request";
    private static final String SPACE = " ";
    private static final String AT = "@";
    private static final String TWO_SPACES = "  ";
    private static final String EMPTY = "";
    private static final String _TWITTER_COM = "@twitter.com";
    private static final int CANCELLED = 1;

    private static final int ERROR = 0;
    private static final Logger LOG = LoggerManager.getLogger();
    private static int status = -1;
    private static final int SUCCESS = 2;
    public AsyncResponse delegate;
    private final Context context;
    private final String nextRequest;
    private ProgressDialog progressDialog;
    private Exception raisedException;
    private final String reqFor, twitterMessage;
    private volatile boolean running = true;
    private final String twId;
    private List<Friend> twitterFriendList;
    private FacebookUser userInfo;
    private final VideoDetails video;

    public TwitterAsync(final String message, final Context context, final String twId, final String requestfor,
            final String tMsg, final VideoDetails video, final String nextRequest) {

        this.context = context;
        this.twId = twId;
        this.reqFor = requestfor;
        this.twitterMessage = tMsg;
        this.video = video;
        this.nextRequest = nextRequest;
    }

    public static long[] convertIntegers(final List<Long> integers) {

        final long[] ret = new long[integers.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = integers.get(i).intValue();
        }
        return ret;
    }

    private void getTwitterFriendsLsit(final Twitter twitter) {

        User user;
        try {
            user = twitter.showUser(twitter.getScreenName());
            final Friend currentUser = new Friend();
            currentUser.setFriendID(String.valueOf(user.getId()));
            currentUser.setFriendImage(user.getProfileImageURL());
            currentUser.setFriendName("You");
            currentUser.setLocation(user.getLocation());
            this.twitterFriendList.add(currentUser);

            final List<Long> IDS = new ArrayList<Long>();
            int start = 0;
            int finish = 100;
            final long[] friendsId = twitter.getFriendsIDs(user.getId(), -1).getIDs();
            boolean check = true;
            while (check) {
                for (int i = start; i < finish; i++) {
                    // get first 100
                    IDS.add(Long.valueOf(friendsId[i]));
                    // if at the end, stop
                    if ((friendsId.length - 1) == i) {
                        check = false;
                        break;
                    }
                }
                // set values for next 100
                start = start + 100;
                finish = finish + 100;
                final long[] ids = convertIntegers(IDS);
                final ResponseList<User> frinds = twitter.lookupUsers(ids);
                if ((frinds != null) && (frinds.size() > 0)) {
                    for (int j = 0; j < frinds.size(); j++) {
                        final User frnd = frinds.get(j);
                        final Friend friend = new Friend();
                        friend.setFriendID(String.valueOf(frnd.getId()));
                        friend.setFriendImage(frnd.getProfileImageURL());
                        friend.setFriendName(frnd.getName());
                        friend.setLocation(frnd.getLocation());
                        this.twitterFriendList.add(friend);
                    }
                }
                // clear so long[] holds max 100 at any given time
                IDS.clear();

            }
        } catch (final TwitterException e) {
            LOG.e(e);
        }

    }

    private boolean isFriendToUser(final String twId) {

        boolean friend = false;
        for (int i = 0; i < this.twitterFriendList.size(); i++) {
            final Friend frnd = this.twitterFriendList.get(i);
            if (twId.equalsIgnoreCase(frnd.getFriendId())) {
                friend = true;
                break;
            }
        }
        return friend;
    }

    @Override
    protected Void doInBackground(final Void... params) {

        while (this.running) {
            this.twitterFriendList = new ArrayList<Friend>();

            if ((MainManager.getInstance().getTwitterAuthorization() == 0) || (Config.getTwitterObject() == null)) {
                final String token = MainManager.getInstance().getTwitterOAuthtoken();
                final String secret = MainManager.getInstance().getTwitterSecretKey();
                final ConfigurationBuilder builder = new ConfigurationBuilder();
                builder.setOAuthConsumerKey(Constant.CONSUMER_KEY);
                builder.setOAuthConsumerSecret(Constant.CONSUMER_SECRET);
                builder.setOAuthAccessToken(token);
                builder.setOAuthAccessTokenSecret(secret);
                builder.setJSONStoreEnabled(true);
                builder.setIncludeEntitiesEnabled(true);
                builder.setIncludeMyRetweetEnabled(true);
                builder.setUseSSL(true);
                builder.setIncludeRTsEnabled(true);
                final AccessToken aToken = new AccessToken(token, secret);
                Config.setTwitterObject(new TwitterFactory(builder.build()).getInstance(aToken));
            }
            if (Constant.TWITTER_FRIEND_INFO.equalsIgnoreCase(this.reqFor)) {
                this.userInfo = new FacebookUser();
                if (Strings.isNullOrEmpty(Config.getTwitterScreenId())) {
                    try {
                        final User userScreen = Config.getTwitterObject().showUser(
                                Config.getTwitterObject().getScreenName());
                        Config.setTwitterScreenId(String.valueOf(userScreen.getId()));
                    } catch (final TwitterException exception) {
                        LOG.e(exception);
                    }
                }

                try {
                    final ResponseList<twitter4j.Status> statuses = Config.getTwitterObject().getUserTimeline(
                            Long.parseLong(this.twId));
                    for (final twitter4j.Status status : statuses) {
                        this.userInfo.setStatusUpdate(status.getText());

                        this.userInfo.setLastUpdate(Util.getLocalTimeFromGMT(String.valueOf(status.getCreatedAt())));
                        // Utils.getInstance().getLongFromTime(time)
                        break;
                    }
                } catch (final NumberFormatException exception) {
                    LOG.e(exception);
                } catch (final TwitterException exception) {
                    LOG.e(exception);
                }

                try {
                    final User user = Config.getTwitterObject().showUser(Long.parseLong(this.twId));
                    this.userInfo.setCurrentPlace(user.getLocation());
                    this.userInfo.setProfilePick(user.getProfileImageURL());
                    this.userInfo.setId(String.valueOf(user.getId()));
                    this.userInfo.setUserName(user.getName());
                    this.userInfo.setScreenName(user.getScreenName());
                    this.userInfo.setTwitterUserFollowerCount(String.valueOf(user.getFollowersCount()));
                    this.userInfo.setTwitterUserDescription(user.getDescription());
                } catch (final NumberFormatException exception) {
                    LOG.e(exception);
                } catch (final TwitterException exception) {
                    LOG.e(exception);
                }// 106161598
                if ((VideoPlayerApp.getInstance().getTwitterFriendList() != null)
                        && (VideoPlayerApp.getInstance().getTwitterFriendList().size() > 0)) {
                    this.twitterFriendList = VideoPlayerApp.getInstance().getTwitterFriendList();
                } else {
                    this.getTwitterFriendsLsit(Config.getTwitterObject());
                    VideoPlayerApp.getInstance().setTwitterFriendList(this.twitterFriendList);
                }

            } else if (Constant.TWITTER_FEED.equalsIgnoreCase(this.reqFor)) {
                this.userInfo = new FacebookUser();
                User user;
                try {
                    user = Config.getTwitterObject().showUser(Config.getTwitterObject().getScreenName());
                    Config.setTwitterScreenId(String.valueOf(user.getId()));
                    this.userInfo.setEmail(user.getScreenName() + _TWITTER_COM);
                    this.userInfo.setProfilePick(user.getProfileImageURL());
                    this.userInfo.setUserName(user.getName());
                    this.userInfo.setId(String.valueOf(user.getId()));
                } catch (final TwitterException exception) {
                    LOG.e(exception);
                }// 106161598
            } else if (Constant.TWITTER_FOLLOW.equalsIgnoreCase(this.reqFor)) {
                try {
                    Config.getTwitterObject().createFriendship(Long.parseLong(this.twId));
                } catch (final NumberFormatException exception) {
                    LOG.e(exception);
                } catch (final TwitterException exception) {
                    LOG.e(exception);
                }
            } else if (Constant.TWITTER_DIRECT_MESSAGE.equalsIgnoreCase(this.reqFor)) {
                try {
                    final DirectMessage message = Config.getTwitterObject().sendDirectMessage(
                            Long.parseLong(this.twId), this.twitterMessage);
                } catch (final NumberFormatException exception) {
                    LOG.e(exception);
                } catch (final TwitterException exception) {
                    LOG.e(exception);
                }
            } else if (Constant.TWITTER_TWEET.equalsIgnoreCase(this.reqFor)) {
                // twitter.updateStatus(twitterMessage);
                String userName = null;
                StatusUpdate status = null;
                try {
                    if (!Strings.isNullOrEmpty(this.twId)) {
                        final User user = Config.getTwitterObject().showUser(Long.parseLong(this.twId));
                        userName = user.getScreenName();
                        if ((this.video != null) && (!Strings.isNullOrEmpty(this.video.getVideothumbPath()))) {
                            if (!Strings.isNullOrEmpty(this.video.getLatestTagexpression())) {
                                status = new StatusUpdate(AT + userName + TWO_SPACES
                                        + this.video.getLatestTagexpression() + TWO_SPACES + this.twitterMessage);
                            } else {
                                status = new StatusUpdate(AT + userName + TWO_SPACES + this.video.getVideoTitle()
                                        + TWO_SPACES + this.twitterMessage);
                            }
                        } else {
                            status = new StatusUpdate(AT + userName + TWO_SPACES + this.twitterMessage);
                        }
                    } else {
                        if ((this.video != null) && (!Strings.isNullOrEmpty(this.video.getVideothumbPath()))) {
                            if (!Strings.isNullOrEmpty(this.video.getLatestTagexpression())) {
                                status = new StatusUpdate(SPACE + this.video.getLatestTagexpression() + TWO_SPACES
                                        + this.twitterMessage);
                            } else {
                                status = new StatusUpdate(SPACE + this.video.getVideoTitle() + SPACE
                                        + this.twitterMessage);
                            }
                        } else {
                            status = new StatusUpdate(this.twitterMessage);
                        }
                    }
                    if ((this.video != null) && (!Strings.isNullOrEmpty(this.video.getVideothumbPath()))) {
                        URL url = null;
                        try {
                            url = new URL(this.video.getVideothumbPath());
                        } catch (final MalformedURLException exception1) {
                            LOG.e(exception1);
                        }
                        InputStream is = null;
                        try {
                            is = url.openConnection().getInputStream();
                        } catch (final IOException exception) {
                            LOG.e(exception);
                        }
                        status.setMedia(this.video.getVideoTitle(), is);
                    }
                    Config.getTwitterObject().updateStatus(status);
                } catch (final TwitterException e) {
                    LOG.d("Pic Upload error" + e.getErrorMessage());
                }
            } else {
                this.getTwitterFriendsLsit(Config.getTwitterObject());

            }

            LOG.i("twitter 4j list size " + this.twitterFriendList.size());

            status = SUCCESS;
            this.running = false;
        }
        return null;

    }

    @Override
    protected void onCancelled() {

        if (this.progressDialog != null) {
            this.progressDialog.dismiss();
        }
        status = CANCELLED;
        this.running = false;
    }

    @Override
    protected void onPostExecute(final Void result) {

        super.onPostExecute(result);
        if (this.progressDialog != null) {
            this.progressDialog.dismiss();
        }
        if (status == SUCCESS) {
            if (Constant.TWITTER_FRIEND_INFO.equalsIgnoreCase(this.reqFor)) {
                boolean friend = false;
                if ((this.twitterFriendList != null) && (this.twitterFriendList.size() > 0)) {
                    friend = this.isFriendToUser(this.twId);
                }
                this.delegate.friendInfoProcessFinish(this.userInfo, friend, Constant.TWITTER);
            } else if (Constant.TWITTER_FOLLOW.equalsIgnoreCase(this.reqFor)) {
                if (Constant._2.equalsIgnoreCase(this.nextRequest) && (this.video != null)) {
                    new TagInteractionAsync(this.video.getVideoID(), Constant.TWITTER_PLATFORM, Constant._2,
                            Config.getUserId(), this.context).execute();
                }
                Alerts.showInfoOnly(SENT_FRIEND_REQUEST, this.context);
            } else if (Constant.TWITTER_FEED.equalsIgnoreCase(this.reqFor)) {
                this.delegate.friendInfoProcessFinish(this.userInfo, false, Constant.TWITTER);
            } else if (Constant.TWITTER_DIRECT_MESSAGE.equalsIgnoreCase(this.reqFor)) {
                Alerts.showInfoOnly(SENT_MESSAGE_SUCCESSFULLY, this.context);
            } else if (Constant.TWITTER_TWEET.equalsIgnoreCase(this.reqFor)) {
                if (Constant.WRITE_ON_WALL.equalsIgnoreCase(this.nextRequest) && (this.video != null)) {
                    new TagInteractionAsync(this.video.getVideoID(), Constant.TWITTER_PLATFORM, Constant.WRITE_ON_WALL,
                            Config.getUserId(), this.context).execute();
                } else if (Constant.SHARE_VIDEO.equalsIgnoreCase(this.nextRequest) && (this.video != null)) {
                    new ShareViewsAsync(this.video.getVideoID(), Constant.TWITTER_PLATFORM, _1, Config.getUserId(),
                            this.context).execute();
                }
                Alerts.showInfoOnly(POSTED_SUCCESSFULLY_TO_TWITTER, this.context);
            } else {
                this.delegate.processFinish(this.twitterFriendList, Constant.TWITTER);
            }
        } else if (status == ERROR) {
            // new VideoPlayerExceptions(context, raisedException);
            if (Constant.TWITTER_TWEET.equalsIgnoreCase(this.reqFor)) {
                Alerts.showInfoOnly(VIDEO_NOT_SHARED_TO_YOUR_TAGGED_CONTACT, this.context);
            } else {
                Alerts.showInfoOnly(VIDEO_NOT_SHARED_TO_YOUR_TAGGED_CONTACT, this.context);
            }

        }

    }

    @Override
    protected void onPreExecute() {

        super.onPreExecute();
        this.progressDialog = ProgressDialog.show(this.context, EMPTY, EMPTY, true);
        final View view = ((LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
                R.layout.progress_bar, null, false);
        final TextView progressText = (TextView) view.findViewById(R.id.progressText);
        progressText.setText(PROCESSING);
        this.progressDialog.setContentView(view);
        this.progressDialog.setCancelable(true);
        this.progressDialog.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(final DialogInterface dialog) {

                TwitterAsync.this.cancel(true);
            }
        });
        this.progressDialog.show();

    }
}
