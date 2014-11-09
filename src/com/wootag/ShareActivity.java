/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.wootag;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.android.gms.plus.model.people.Person;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wootag.adapter.ContactAdapter;
import com.wootag.adapter.ShareAdapter;
import com.wootag.async.ContactAsync;
import com.wootag.dto.Contact;
import com.wootag.dto.Friend;
import com.wootag.dto.User;
import com.wootag.dto.VideoDetails;
import com.wootag.facebook.Session;
import com.wootag.twitter.TwitterAsync;
import com.wootag.ui.Image;
import com.wootag.util.Alerts;
import com.wootag.util.Config;
import com.wootag.util.ContactInterface;

public class ShareActivity extends FriendsListActivity implements TextWatcher, ContactInterface {

    private static final String ALL = "All";

    private static final String THIS_VIDEO_IS_CLICKABLE_WATCH_AND_CLICK_THE_TAGS_ICONS_INSIDE_THE_VIDEO_AND_DISCOVER_MORE_ITS_GREAT_TRY_THIS_APP_WWW_WOOTAG_COM_INVITE_HTML = " \n \nThis video is clickable, Watch and click the tags(icons) inside the video and discover more. \nIts great!Try this app www.wootag.com/invite.html";

    private static final String LOVED_THIS_VIDEO_ON_WOOTAG_THIS_IS_MY_TAGS_INSIDE_THE_WOOTAG_VIDEO = ", Loved this video on Wootag \n \nThis is my tags inside the wootag video! \n ";

    private static final String HI = "Hi ";

    private static final String SMS_BODY = "sms_body";

    private static final String HTTP_WWW_TAGMOMENTS_COM = "http://www.tagmoments.com/";

    private static final String EMPTY = "";

    public static ShareActivity shareActivity;

    private static List<Friend> friendList;
    protected static final Logger LOG = LoggerManager.getLogger();
    protected List<Friend> adapterList;
    private Button back;
    protected ContactAdapter contactAdapter;
    protected List<Contact> contactFilterdList;
    protected List<Contact> contactList;
    private ListView contactListView;
    protected Context context;
    private Button facebook;
    private ImageButton fbbackButton;
    protected LinearLayout fbFriendListLayout;
    private ListView fbfrndList;
    private String[] fbIds;
    private AutoCompleteTextView fbsearch;
    private boolean fbShareUrl;
    protected List<Friend> filterdList;
    private Button google;
    private String gPlusFriendID = EMPTY;
    private String[] gPlusIds;
    private List<Person> gPlusPeople;
    private boolean gplusShareUrl;
    private Friend loggedInUser;
    private Button mail;
    private ProgressDialog pDialog;
    private boolean pendingRequest;
    private ImageView searchIcon;
    private Session session;
    private ShareAdapter shareAdapter;
    private View shareCancel;
    private RelativeLayout shareContacts;
    private View shareDone;
    private RelativeLayout shareGoogle, shareTwitter, shareMail, sharewootag, shareFacebook;
    protected String shareUserID = EMPTY;
    protected LinearLayout socialActionsLayout;
    private Button twitter;
    private String twitterFriendId = EMPTY;
    private String[] twitterIds;
    private boolean twitterShareUrl;
    protected VideoDetails video;
    private ImageView videoImage;
    private Button wootag;

    @Override
    public void afterTextChanged(final Editable arg0) {

        final String text = this.fbsearch.getText().toString();
        if (Constant.FACEBOOK.equalsIgnoreCase(Config.getSocialSite())) {
            this.setSearchAdapter(VideoPlayerApp.getInstance().getFbFriendsList(), text, Constant.FACEBOOK);
        } else if (Constant.GOOGLE_PLUS.equalsIgnoreCase(Config.getSocialSite())) {
            this.setSearchAdapter(VideoPlayerApp.getInstance().getGoogleFriendList(), text, Constant.GOOGLE_PLUS);
        } else if (Constant.TWITTER.equalsIgnoreCase(Config.getSocialSite())) {
            this.setSearchAdapter(VideoPlayerApp.getInstance().getTwitterFriendList(), text, Constant.TWITTER);
        } else if (Constant.CONTACTS.equalsIgnoreCase(Config.getSocialSite())) {
            this.setSearchContactAdapter(VideoPlayerApp.getInstance().getContactsList(), text);
        }

    }

    @Override
    public void beforeTextChanged(final CharSequence arg0, final int arg1, final int arg2, final int arg3) {

    }

    public void clearContactList() {

        if ((this.contactList != null) && (this.contactList.size() > 0)) {
            this.contactList.clear();
            ((BaseAdapter) this.contactListView.getAdapter()).notifyDataSetChanged();
        }

    }

    public void clearList() {

        if ((this.adapterList != null) && (this.adapterList.size() > 0)) {
            this.adapterList.clear();
            ((BaseAdapter) this.fbfrndList.getAdapter()).notifyDataSetChanged();
        }

    }

    @Override
    public void contacts(final List<Contact> contacts) {

        if ((contacts != null) && (contacts.size() > 0)) {
            VideoPlayerApp.getInstance().setContacts(contacts);
            this.setContactAdapter(contacts);
        }
    }

    /** If launching default mail app to send a video url */
    public void mail() {

        // try {
        final Intent emailIntent = new Intent(Intent.ACTION_SEND);
        final String[] recipients = new String[] { EMPTY, EMPTY, };
        emailIntent.putExtra(Intent.EXTRA_EMAIL, recipients);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Test");
        if (this.video.getVideoURL() != null) {
            emailIntent.putExtra(Intent.EXTRA_TEXT, this.video.getShareUrl());
        } else {
            emailIntent.putExtra(Intent.EXTRA_TEXT, "video url sharing");
        }
        emailIntent.setType("text/plain");
        this.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
        // } catch (final Exception e) {
        // VideoPlayerApp.getInstance().writeStackTraceToLog(e, this.context);
        // LOG.i(this.Tag, " " + e.toString());
        // }
    }

    @Override
    public void onCancel(final DialogInterface dialog) {

    }

    @Override
    public void onClick(final View v) {

        // try {
        LOG.i(" on click ");
        if (v.getId() == R.id.mailshare) {
            this.mail();
        } else if (v.getId() == R.id.fbshare) {
            LOG.i("fb on click ");
            this.clearList();
            if (this.contactListView != null) {
                this.contactListView.setVisibility(View.GONE);
            }
            this.fbfrndList.setVisibility(View.VISIBLE);
            Config.setSocialSite(Constant.FACEBOOK);
            this.socialActionsLayout.setVisibility(View.GONE);
            this.fbsearch.setText(EMPTY);
            this.searchIcon.setImageResource(R.drawable.sharefacebook);
            this.fbFriendListLayout.setVisibility(View.VISIBLE);
            if (((VideoPlayerApp.getInstance().getFbFriendsList() != null) && (VideoPlayerApp.getInstance()
                    .getFbFriendsList().size() > 0))) {
                friendList = VideoPlayerApp.getInstance().getFbFriendsList();
                this.setFriendListAdapter(friendList, Constant.FACEBOOK, false);
            } else {
                super.onClick(v);
            }
        } else if (v.getId() == R.id.twittershare) {
            this.clearList();
            if (this.contactListView != null) {
                this.contactListView.setVisibility(View.GONE);
            }
            this.fbfrndList.setVisibility(View.VISIBLE);
            Config.setSocialSite(Constant.TWITTER);
            if ((VideoPlayerApp.getInstance().getTwitterFriendList() != null)
                    && (VideoPlayerApp.getInstance().getTwitterFriendList().size() > 0)) {
                friendList = VideoPlayerApp.getInstance().getTwitterFriendList();
                this.setFriendListAdapter(friendList, Constant.TWITTER, false);
            } else {
                super.onClick(v);
            }
        }

        if (v.getId() == R.id.googleshare) {
            if (this.contactListView != null) {
                this.contactListView.setVisibility(View.GONE);
            }
            this.fbfrndList.setVisibility(View.VISIBLE);
            Config.setSocialSite(Constant.GOOGLE_PLUS);
            super.onClick(v);
        } else if (v.getId() == R.id.contactshare) {
            if (this.fbfrndList != null) {
                this.fbfrndList.setVisibility(View.GONE);
            }
            this.contactListView.setVisibility(View.VISIBLE);
            Config.setSocialSite(Constant.CONTACTS);
            if ((VideoPlayerApp.getInstance().getContactsList() != null)
                    && (VideoPlayerApp.getInstance().getContactsList().size() > 0)) {
                for (int i = 0; i < VideoPlayerApp.getInstance().getContactsList().size(); i++) {
                    VideoPlayerApp.getInstance().getContactsList().get(i).setChecked(false);
                }
                this.setContactAdapter(VideoPlayerApp.getInstance().getContactsList());
            } else {
                final ContactAsync async = new ContactAsync(this.context);
                async.delegate = ShareActivity.this;
                async.execute();
            }
        }
        // } catch (final Exception e) {
        // VideoPlayerApp.getInstance().writeStackTraceToLog(e, this.context);
        // LOG.i("exception on click " + e.toString());
        // }

    }

    @Override
    public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {

    }

    @Override
    public void processFinish(final List<Friend> friendList, final String socialSite) {

        if ((friendList != null) && (friendList.size() > 0)) {
            if (Constant.TWITTER.equalsIgnoreCase(socialSite)) {

                this.socialActionsLayout.setVisibility(View.GONE);
                this.fbsearch.setText(EMPTY);
                this.searchIcon.setImageResource(R.drawable.sharetwitter);
                this.fbFriendListLayout.setVisibility(View.VISIBLE);

                this.setFriendListAdapter(friendList, socialSite, false);
                VideoPlayerApp.getInstance().setTwitterFriendList(friendList);
            } else if (Constant.FACEBOOK.equalsIgnoreCase(socialSite)) {
                super.processFinish(friendList, socialSite);
            } else if (Constant.GOOGLE_PLUS.equalsIgnoreCase(socialSite)) {
                VideoPlayerApp.getInstance().setGoogleFriendList(friendList);
                this.setFriendListAdapter(friendList, socialSite, false);
            } else if (Constant.G_PLUS_SHARE_VIDEO.equalsIgnoreCase(socialSite)) {
                VideoPlayerApp.getInstance().setGoogleFriendList(friendList);
                if (this.loggedInUser != null) {
                    friendList.add(this.loggedInUser);
                }
                for (int i = 0; i < this.gPlusIds.length; i++) {
                    if (!this.gPlusIds[i].trim().equalsIgnoreCase(EMPTY) && (friendList != null)
                            && (friendList.size() > 0)) {
                        for (int j = 0; j < friendList.size(); j++) {
                            final Friend frnd = friendList.get(j);
                            if (this.gPlusIds[i].trim().equalsIgnoreCase(frnd.getFriendId().trim())) {
                                this.gPlusShare(frnd.getFriendId(), frnd.getFriendName());
                            }
                        }
                    }
                }
            }
        } else {
            Alerts.showAlertOnly("Info", "No friends available.", this.context);
        }

    }

    @Override
    public void sendList(final List<Friend> list) {

        VideoPlayerApp.getInstance().setFacebookFriendsList(list);
        LOG.i("fb oncomplete frnds.size() " + friendList.size());
        this.setFriendListAdapter(list, Constant.FACEBOOK, false);
    }

    @Override
    public void userDetailsFinished(final User userDetails, final String socialSite) {

        super.userDetailsFinished(userDetails, socialSite);
        if (userDetails != null) {
            if (Constant.FACEBOOK.equalsIgnoreCase(socialSite)) {
                if ((this.fbIds != null) && (this.fbIds.length > 0)) {
                    for (int i = 0; i < this.fbIds.length; i++) {
                        if (!this.fbIds[i].trim().equalsIgnoreCase(EMPTY)) {
                            this.publishFeedDialog(this.fbIds[i]);
                        }
                    }

                }
            } else if (Constant.GOOGLE_PLUS.equalsIgnoreCase(socialSite)) {
                this.loggedInUser = new Friend();
                this.loggedInUser.setFriendID(userDetails.getUserId());
                this.loggedInUser.setFriendName(userDetails.getUserName());

            }
        }
    }

    private void loadImage(final VideoDetails currentVideo) {

        if ((currentVideo != null) && (currentVideo.getVideothumbPath() != null)) {
            Image.displayImage(currentVideo.getVideothumbPath(), this, this.videoImage, 1);
        }
    }

    private void setContactAdapter(final List<Contact> contacts) {

        this.socialActionsLayout.setVisibility(View.GONE);
        this.searchIcon.setImageResource(R.drawable.contact);
        this.fbFriendListLayout.setVisibility(View.VISIBLE);
        if ((contacts != null) && (contacts.size() > 0)) {
            this.contactList.clear();
            this.contactList.addAll(contacts);
        }
        this.contactAdapter = new ContactAdapter(this.context, R.layout.facebook_user, this.contactList,
                Config.getSocialSite(), true);
        this.contactListView.setAdapter(this.contactAdapter);
        this.contactListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(final AdapterView<?> arg0, final View ignored, final int arg2, final long arg3) {

                final Contact selectedContact = ShareActivity.this.contactList.get(arg2);
                if (selectedContact.isChecked()) {
                    selectedContact.setChecked(false);
                } else {
                    selectedContact.setChecked(true);
                }
                if (ShareActivity.this.contactAdapter != null) {
                    ShareActivity.this.contactAdapter.notifyDataSetChanged();
                }
            }
        });

    }

    private void setFriendListAdapter(final List<Friend> friendlist, final String socialSite, final boolean search) {

        this.socialActionsLayout.setVisibility(View.GONE);
        if (Constant.FACEBOOK.equalsIgnoreCase(socialSite)) {
            this.searchIcon.setImageResource(R.drawable.sharefacebook);
        } else if (Constant.TWITTER.equalsIgnoreCase(socialSite)) {
            this.searchIcon.setImageResource(R.drawable.sharetwitter);
        }
        this.fbFriendListLayout.setVisibility(View.VISIBLE);

        if ((friendlist != null) && (friendlist.size() > 0)) {
            this.adapterList.clear();
            this.adapterList.addAll(friendlist);
        }
        this.shareAdapter = new ShareAdapter(this.context, R.layout.facebook_user, this.adapterList,
                Config.getSocialSite(), search);
        this.fbfrndList.setAdapter(this.shareAdapter);
        this.fbfrndList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(final AdapterView<?> arg0, final View arg1, final int arg2, final long arg3) {

                final Friend facebookFriend = ShareActivity.this.adapterList.get(arg2);
                ShareActivity.this.shareUserID = facebookFriend.getFriendId();
                LOG.i("friend details name " + facebookFriend.getFriendName() + "id " + facebookFriend.getFriendId());
            }
        });

    }

    private void setSearchAdapter(final List<Friend> frndList, final String text, final String socialSite) {

        // try {
        if ((text != null) && (frndList != null) && (frndList.size() > 0)) {
            this.clearList();
            new Thread(new Runnable() {

                @Override
                public void run() {

                    ShareActivity.this.filterdList = new ArrayList<Friend>();
                    for (int i = 0; i < frndList.size(); i++) {
                        final Friend frnd = frndList.get(i);
                        if ((frnd.getFriendName().toLowerCase(Locale.getDefault())).indexOf(text.toLowerCase(Locale
                                .getDefault())) != -1) {
                            ShareActivity.this.filterdList.add(frnd);
                        }
                    }
                }
            }).start();
            try {
                Thread.sleep(50);
            } catch (final InterruptedException e) {
                Alerts.showAlert("Exception", e.toString(), this.context);
            }
            if ((text.trim().length() > 0) && (this.filterdList != null) && (this.filterdList.size() > 0)) {
                this.setFriendListAdapter(this.filterdList, socialSite, true);
            } else if ((this.filterdList != null) && (this.filterdList.size() > 0)) {
                this.setFriendListAdapter(this.filterdList, socialSite, false);
            }

        }
        // } catch (final Exception e) {
        // VideoPlayerApp.getInstance().writeStackTraceToLog(e, this.context);
        // Alerts.showAlert("Exception", e.toString(), this.context);
        // }
    }

    private void setSearchContactAdapter(final List<Contact> frndList, final String text) {

        // try {
        if ((text != null) && (frndList != null) && (frndList.size() > 0)) {
            this.clearContactList();
            new Thread(new Runnable() {

                @Override
                public void run() {

                    ShareActivity.this.contactFilterdList = new ArrayList<Contact>();
                    for (int i = 0; i < frndList.size(); i++) {
                        final Contact frnd = frndList.get(i);
                        if ((frnd.getContactName().toLowerCase(Locale.getDefault())).indexOf(text.toLowerCase(Locale
                                .getDefault())) != -1) {
                            ShareActivity.this.contactFilterdList.add(frnd);
                        }
                    }
                }
            }).start();
            try {
                Thread.sleep(50);
            } catch (final InterruptedException e) {
                Alerts.showAlert("Exception", e.toString(), this.context);
            }
            if ((this.contactFilterdList != null) && (this.contactFilterdList.size() > 0)) {
                this.setContactAdapter(this.contactFilterdList);
            }

        }
        // } catch (final Exception e) {
        // VideoPlayerApp.getInstance().writeStackTraceToLog(e, this.context);
        // Alerts.showAlert("Exception", e.toString(), this.context);
        // }
    }

    private void sharePublishedVideoOnWall() {

        if (this.gplusShareUrl) {
            this.gPlusRequest = Constant.G_PLUS_SHARE_VIDEO;
            this.gPlusLogin();
        }
        // try {
        if (this.twitterShareUrl && (this.video.getVideoURL() != null)) {
            for (int i = 0; i < this.twitterIds.length; i++) {
                if ((this.twitterIds[i] != null) && !this.twitterIds[i].trim().equalsIgnoreCase(EMPTY)) {
                    final TwitterAsync asyncTask = new TwitterAsync(EMPTY, this.context, this.twitterIds[i],
                            Constant.TWITTER_TWEET, this.video.getShareUrl(), this.video, Constant.SHARE_VIDEO);
                    asyncTask.execute();
                }
            }
        }
        // } catch (final Exception e) {
        // VideoPlayerApp.getInstance().writeStackTraceToLog(e, this.context);
        // }
        // try {
        if (this.fbShareUrl) {
            this.facebookRequest = Constant.FACEBOOK_SHARE;
            this.facebookLogin();
        }

        // } catch (final Exception e) {
        // VideoPlayerApp.getInstance().writeStackTraceToLog(e, this.context);
        // }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        // try {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_friends_list);
        shareActivity = this;
        this.context = this;

        Bundle bundle = new Bundle();
        bundle = this.getIntent().getExtras();
        if ((bundle != null) && bundle.containsKey("video")) {
            this.video = (VideoDetails) bundle.getSerializable("video");
            this.setVideoDetails(this.video);

            if (bundle.containsKey("fb")) {
                final String fbIdsString = bundle.getString("fb");
                if (!fbIdsString.trim().equalsIgnoreCase(EMPTY)) {
                    this.fbShareUrl = true;
                    this.fbIds = fbIdsString.split(",");
                } else {
                    this.fbShareUrl = false;
                }
            }
            if (bundle.containsKey("gplus")) {
                final String gplusIdsString = bundle.getString("gplus");
                if (!gplusIdsString.trim().equalsIgnoreCase(EMPTY)) {
                    this.gplusShareUrl = true;
                    this.gPlusIds = gplusIdsString.split(",");
                } else {
                    this.gplusShareUrl = false;
                }
            }
            if (bundle.containsKey("twitter")) {
                final String twitterIdsString = bundle.getString("twitter");
                if (!twitterIdsString.trim().equalsIgnoreCase(EMPTY)) {
                    this.twitterIds = twitterIdsString.split(",");
                    this.twitterShareUrl = true;
                } else {
                    this.twitterShareUrl = false;
                }
            }
        }

        this.socialActionsLayout = (LinearLayout) this.findViewById(R.id.socialactions);
        this.fbFriendListLayout = (LinearLayout) this.findViewById(R.id.fbfriendListLayout);

        this.shareGoogle = (RelativeLayout) this.findViewById(R.id.googleshare);
        this.shareFacebook = (RelativeLayout) this.findViewById(R.id.fbshare);
        this.shareTwitter = (RelativeLayout) this.findViewById(R.id.twittershare);
        this.shareMail = (RelativeLayout) this.findViewById(R.id.mailshare);
        this.sharewootag = (RelativeLayout) this.findViewById(R.id.wootagshare);
        this.shareContacts = (RelativeLayout) this.findViewById(R.id.contactshare);

        this.back = (Button) this.findViewById(R.id.back);
        this.facebook = (Button) this.findViewById(R.id.fb);
        this.google = (Button) this.findViewById(R.id.gplusshare);
        this.twitter = (Button) this.findViewById(R.id.twitter);
        this.fbfrndList = (ListView) this.findViewById(R.id.fbfriendslist);
        this.fbbackButton = (ImageButton) this.findViewById(R.id.fbback);
        this.shareDone = this.findViewById(R.id.sharedone);
        this.shareCancel = this.findViewById(R.id.sharecancel);
        this.fbbackButton.setVisibility(View.GONE);
        this.shareCancel.setVisibility(View.VISIBLE);
        this.shareDone.setVisibility(View.VISIBLE);

        this.fbsearch = (AutoCompleteTextView) this.findViewById(R.id.fbsearch);
        this.searchIcon = (ImageView) this.findViewById(R.id.fbimg);
        this.mail = (Button) this.findViewById(R.id.mail);
        this.wootag = (Button) this.findViewById(R.id.wootag);
        this.fbsearch.addTextChangedListener(this);
        friendList = new ArrayList<Friend>();
        this.adapterList = new ArrayList<Friend>();

        this.contactListView = (ListView) this.findViewById(R.id.contactsList);
        this.contactList = new ArrayList<Contact>();
        this.shareContacts.setVisibility(View.VISIBLE);

        this.shareGoogle.setOnClickListener(this);
        this.shareFacebook.setOnClickListener(this);
        this.shareTwitter.setOnClickListener(this);
        this.shareMail.setOnClickListener(this);
        this.shareContacts.setOnClickListener(this);

        // try {
        this.videoImage = (ImageView) this.findViewById(R.id.videoImage);
        this.loadImage(this.video);
        // } catch (final Exception e) {
        // VideoPlayerApp.getInstance().writeStackTraceToLog(e, this.context);
        // LOG.e("share activity", "exception video" + e.toString());
        // }
        this.shareCancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                ShareActivity.this.shareUserID = EMPTY;
                ShareActivity.this.fbFriendListLayout.setVisibility(View.GONE);
                ShareActivity.this.socialActionsLayout.setVisibility(View.VISIBLE);
            }
        });
        this.shareDone.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (Constant.CONTACTS.equalsIgnoreCase(Config.getSocialSite())) {
                    if ((ShareActivity.this.contactList != null) && (ShareActivity.this.contactList.size() > 0)) {
                        final ArrayList<Contact> selectedContacts = new ArrayList<Contact>();
                        for (int i = 0; i < ShareActivity.this.contactList.size(); i++) {
                            final Contact contact = ShareActivity.this.contactList.get(i);
                            if (contact.isChecked()) {
                                selectedContacts.add(contact);
                            }
                        }
                        if ((selectedContacts != null) && (selectedContacts.size() > 0)) {
                            ShareActivity.this.sendSms(selectedContacts, ShareActivity.this.video);
                        } else {
                            Alerts.showAlertOnly("Info", "Select atleast one contact to share video",
                                    ShareActivity.this.context);
                        }
                    }
                } else {
                    if (!ShareActivity.this.shareUserID.equalsIgnoreCase(EMPTY)) {
                        ShareActivity.this.shareVideoToFriend(ShareActivity.this.shareUserID);
                    } else {
                        Alerts.showAlertOnly("Info", "Please select one friend to share the video",
                                ShareActivity.this.context);
                    }
                }
            }
        });
        this.back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                ShareActivity.this.finish();
            }
        });
        if (this.gplusShareUrl || this.twitterShareUrl || this.fbShareUrl) {
            this.sharePublishedVideoOnWall();
        }

        // } catch (final Exception e) {
        // VideoPlayerApp.getInstance().writeStackTraceToLog(e, this.context);
        // LOG.e("share activity", e.toString());
        // }

    }

    /** sending sms (sahring video url )to selcted conatcst */
    void sendSms(final List<Contact> selectedContacts, final VideoDetails currentVideo) {

        StringBuilder uri = null;
        String contactName = EMPTY;
        uri = new StringBuilder("smsto:");
        for (int i = 0; i < selectedContacts.size(); i++) {
            if (i == 0) {
                contactName = selectedContacts.get(i).getContactName();
            }
            uri.append(selectedContacts.get(i).getContactNumber()).append(';');
        }
        final Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(uri.toString()));
        if (selectedContacts.size() <= 1) {
            intent.putExtra(
                    SMS_BODY,
                    HI
                            + contactName
                            + LOVED_THIS_VIDEO_ON_WOOTAG_THIS_IS_MY_TAGS_INSIDE_THE_WOOTAG_VIDEO
                            + this.video.getShareUrl()
                            + THIS_VIDEO_IS_CLICKABLE_WATCH_AND_CLICK_THE_TAGS_ICONS_INSIDE_THE_VIDEO_AND_DISCOVER_MORE_ITS_GREAT_TRY_THIS_APP_WWW_WOOTAG_COM_INVITE_HTML);
        } else {
            intent.putExtra(
                    SMS_BODY,
                    HI
                            + ALL
                            + LOVED_THIS_VIDEO_ON_WOOTAG_THIS_IS_MY_TAGS_INSIDE_THE_WOOTAG_VIDEO
                            + this.video.getShareUrl()
                            + THIS_VIDEO_IS_CLICKABLE_WATCH_AND_CLICK_THE_TAGS_ICONS_INSIDE_THE_VIDEO_AND_DISCOVER_MORE_ITS_GREAT_TRY_THIS_APP_WWW_WOOTAG_COM_INVITE_HTML);
        }
        this.startActivity(intent);
    }

    void shareVideoToFriend(final String friendFacebookId) {

        if (Config.getSocialSite().equalsIgnoreCase(Constant.FACEBOOK)) {
            this.socialActionsLayout.setVisibility(View.VISIBLE);
            this.fbFriendListLayout.setVisibility(View.GONE);
            this.publishFeedDialog(friendFacebookId);
        } else if (Config.getSocialSite().equalsIgnoreCase(Constant.TWITTER)) {
            this.twitterFriendId = friendFacebookId;
            this.socialActionsLayout.setVisibility(View.VISIBLE);
            this.fbFriendListLayout.setVisibility(View.GONE);
            TwitterAsync asyncTask = null;
            if (this.video.getShareUrl() != null) {
                asyncTask = new TwitterAsync(EMPTY, this.context, this.twitterFriendId, Constant.TWITTER_TWEET,
                        this.video.getShareUrl(), this.video, EMPTY);
            } else {
                asyncTask = new TwitterAsync(EMPTY, this.context, this.twitterFriendId, Constant.TWITTER_TWEET,
                        HTTP_WWW_TAGMOMENTS_COM, this.video, EMPTY);
            }
            asyncTask.delegate = ShareActivity.this;
            asyncTask.execute();
        } else if (Config.getSocialSite().equalsIgnoreCase(Constant.GOOGLE_PLUS)) {
            this.gPlusFriendID = friendFacebookId;
        }
    }

}
