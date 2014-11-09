/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.wootag;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wootag.adapter.CommentAdapter;
import com.wootag.adapter.FacebookFriendsListAdapter;
import com.wootag.adapter.ImageAdapter;
import com.wootag.dto.Comment;
import com.wootag.dto.ErrorResponse;
import com.wootag.dto.Friend;
import com.wootag.model.Backend;
import com.wootag.pulltorefresh.PullToRefreshBase.OnLastItemVisibleListener;
import com.wootag.pulltorefresh.PullToRefreshListView;
import com.wootag.slideout.SlideoutActivity;
import com.wootag.util.Alerts;
import com.wootag.util.Config;
import com.wootag.util.MainManager;
import com.wootag.util.Util;

public class SeeAllCommentsActivity extends Activity {

    private static final String USER_NAME = "user_name";

    private static final String COMMENTS = "comments";

    private static final String USERID2 = "userid";

    private static final String VIDEO_ID = "video_id";

    private static final String COMMENT_TEXT = "comment_text";

    private static final String USER = "user";

    public static SeeAllCommentsActivity seeAllCommentsActivity;

    protected static final Logger LOG = LoggerManager.getLogger();
    private static final String getAllCommentsURL = Constant.COMMON_URL_MOBILE + "getallcomments/";
    private static final String postComment = Constant.COMMON_URL_MOBILE + "commentvideo";
    private static final int COMMENTS_PER_PAGE = 10;

    private Button deleteEmoticon;
    protected Button search, menu;
    private Button send;
    protected Button showEmoticonsButton;
    protected CommentAdapter adapter;
    private EditText searchEdit;
    protected EditText writecomment;
    private FacebookFriendsListAdapter filterAdapter;
    protected FacebookFriendsListAdapter userAdapter;
    private GridView gridView;
    private ImageButton deleteButton;
    private ImageButton searchSend;
    protected LinearLayout emoticonLayout;
    protected List<Comment> list;
    private List<Friend> filterdList;
    protected List<Friend> userList;
    protected List<ImageSpan> emoticonsToRemove;
    protected ListView usersListView;
    protected PullToRefreshListView commentList;
    private RelativeLayout searchLayout;
    private RelativeLayout seeAllCommentview;
    private SeeAllCommentsActivity context;
    private String screentype = "";
    protected String type;
    protected String videoId;
    protected String userId = "";
    private String[] drawables;
    private TextView heading;
    boolean flagLoading;
    boolean friendSearch;
    private boolean searchRequest;

    public static String getAtUserJsonRequest(final String name) throws JSONException {

        final JSONObject obj = new JSONObject();
        final JSONArray array = new JSONArray();
        final JSONObject values = new JSONObject();
        values.put(USER_NAME, name);
        array.put(values);
        obj.put(COMMENTS, array);

        return obj.toString();
    }

    private void loademoticonsViews() {

        this.gridView = (GridView) this.findViewById(R.id.gridView1);
        this.emoticonLayout = (LinearLayout) this.findViewById(R.id.emoticonLayout);
        this.deleteEmoticon = (Button) this.findViewById(R.id.deleteEmoticon);
        this.showEmoticonsButton = (Button) this.findViewById(R.id.emoticons);
        this.emoticonLayout.setVisibility(View.GONE);
        this.drawables = new String[72];// assets.list("images");
        this.gridView.setAdapter(new ImageAdapter(this, this.drawables));
        this.showEmoticonsButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View view) {

                if (SeeAllCommentsActivity.this.emoticonLayout.isShown()) {
                    SeeAllCommentsActivity.this.emoticonLayout.setVisibility(View.GONE);
                    SeeAllCommentsActivity.this.showEmoticonsButton.setBackgroundResource(R.drawable.emoticonbutton);
                } else {
                    SeeAllCommentsActivity.this.emoticonLayout.setVisibility(View.VISIBLE);
                    SeeAllCommentsActivity.this.showEmoticonsButton.setBackgroundResource(R.drawable.emoticoncancel);
                    final InputMethodManager mgr = (InputMethodManager) SeeAllCommentsActivity.this
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    mgr.hideSoftInputFromWindow(SeeAllCommentsActivity.this.writecomment.getWindowToken(), 0);
                }
            }
        });
        this.deleteEmoticon.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View view) {

                final int length = SeeAllCommentsActivity.this.writecomment.getText().length();
                if (length > 0) {
                    SeeAllCommentsActivity.this.writecomment.getText().delete(length - 1, length);
                }
            }
        });

        this.gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(final AdapterView<?> parent, final View ignored, final int position, final long id) {

                // Toast.makeText(
                // getApplicationContext(),""+position, Toast.LENGTH_SHORT).show();

                final int selectionStart = SeeAllCommentsActivity.this.writecomment.getSelectionStart();
                final int selectionEnd = SeeAllCommentsActivity.this.writecomment.getSelectionEnd();
                final String textToInsert = "[emoticon" + (position + 1) + "e]";
                SeeAllCommentsActivity.this.writecomment.getText().replace(Math.min(selectionStart, selectionEnd),
                        Math.max(selectionStart, selectionEnd), textToInsert, 0, textToInsert.length());

            }
        });

    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        seeAllCommentsActivity = this;
        this.setContentView(R.layout.activity_see_all_comments);
        this.context = this;
        this.emoticonsToRemove = new ArrayList<ImageSpan>();
        if ((Config.getUserId() != null) && !Config.getUserId().equalsIgnoreCase("")) {

        } else {
            Config.setUserID(MainManager.getInstance().getUserId());
        }

        // VideoPlayerConstants.isPrivateGroupEditMode=true;

        final Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            if (bundle.containsKey("videoid")) {
                this.videoId = bundle.getString("videoid");
            }
            if (bundle.containsKey(Constant.USERID)) {
                this.userId = bundle.getString(Constant.USERID);
            }
            if (bundle.containsKey(Constant.SCREEN)) {
                this.screentype = bundle.getString(Constant.SCREEN);
            }
        }
        this.seeAllCommentview = (RelativeLayout) this.findViewById(R.id.seeallcomments);
        this.seeAllCommentview.setBackgroundColor(this.getResources().getColor(R.color.white));
        this.menu = (Button) this.findViewById(R.id.menu);
        this.search = (Button) this.findViewById(R.id.settings);
        this.search.setBackgroundResource(R.drawable.edit_button);
        // search.setVisibility(View.GONE );
        this.heading = (TextView) this.findViewById(R.id.heading);
        this.heading.setText("See All Comments");
        this.commentList = (PullToRefreshListView) this.findViewById(R.id.commentlist);
        this.writecomment = (EditText) this.findViewById(R.id.comment);
        this.send = (Button) this.findViewById(R.id.send);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        this.writecomment.requestFocus();
        this.list = new ArrayList<Comment>();
        new UserListAsyncTask(1).execute();
        this.menu.setVisibility(View.GONE);
        final Button back = (Button) this.findViewById(R.id.back);
        back.setVisibility(View.VISIBLE);
        Config.setPrivateGroupEditMode(false);
        this.usersListView = (ListView) this.findViewById(R.id.wootagfriendslist);
        this.userList = new ArrayList<Friend>();
        this.loademoticonsViews();
        this.search.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                if (Config.isPrivateGroupEditMode()) {
                    Config.setPrivateGroupEditMode(false);
                } else {
                    Config.setPrivateGroupEditMode(true);
                }
                if (SeeAllCommentsActivity.this.adapter != null) {
                    SeeAllCommentsActivity.this.adapter.notifyDataSetChanged();
                }
            }
        });
        back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                SeeAllCommentsActivity.this.finish();
            }
        });
        this.writecomment.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(final View ignored, final MotionEvent event) {

                if (SeeAllCommentsActivity.this.emoticonLayout.isShown()) {
                    SeeAllCommentsActivity.this.emoticonLayout.setVisibility(View.GONE);
                    SeeAllCommentsActivity.this.showEmoticonsButton.setBackgroundResource(R.drawable.emoticonbutton);
                    final InputMethodManager mgr = (InputMethodManager) SeeAllCommentsActivity.this
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    mgr.hideSoftInputFromWindow(SeeAllCommentsActivity.this.writecomment.getWindowToken(), 0);
                }
                return false;
            }
        });
        this.writecomment.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(final Editable s) {

                Util.emotifySpannable(s);

                final Editable message = s;
                for (final ImageSpan span : SeeAllCommentsActivity.this.emoticonsToRemove) {
                    final int start = message.getSpanStart(span);
                    final int end = message.getSpanEnd(span);
                    message.removeSpan(span);
                    if (start != end) {
                        message.delete(start, end);
                    }
                }
                SeeAllCommentsActivity.this.emoticonsToRemove.clear();

                if (s != null) {
                    final String name = s.toString();
                    if (name.indexOf('@') != -1) {
                        if ((VideoPlayerApp.getInstance().getWootagFriendsList() != null)
                                && (VideoPlayerApp.getInstance().getWootagFriendsList().size() > 0)) {
                            final String actualStirng = name.substring(name.indexOf('@') + 1, name.length());
                            SeeAllCommentsActivity.this.setFilterList(VideoPlayerApp.getInstance()
                                    .getWootagFriendsList(), actualStirng);
                        } else {
                            SeeAllCommentsActivity.this.getUsers();
                        }
                    }
                }
            }

            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

                if (count > 0) {
                    final int end = start + count;
                    final Editable message = SeeAllCommentsActivity.this.writecomment.getEditableText();
                    final ImageSpan[] list = message.getSpans(start, end, ImageSpan.class);
                    /** To remove emoticons from edittext while user clicks on back space */
                    for (final ImageSpan span : list) {
                        final int spanStart = message.getSpanStart(span);
                        final int spanEnd = message.getSpanEnd(span);
                        if ((spanStart < end) && (spanEnd > start)) {
                            SeeAllCommentsActivity.this.emoticonsToRemove.add(span);
                        }
                    }
                }
            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {

            }
        });

        this.menu.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                final int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60,
                        SeeAllCommentsActivity.this.getResources().getDisplayMetrics());
                SlideoutActivity.prepare(SeeAllCommentsActivity.this, R.id.seeallcomments, width);
                SeeAllCommentsActivity.this.startActivity(new Intent(SeeAllCommentsActivity.this, MenuActivity.class));
                SeeAllCommentsActivity.this.overridePendingTransition(0, 0);
            }
        });
        this.commentList.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

            @Override
            public void onLastItemVisible() {

                if (!SeeAllCommentsActivity.this.flagLoading) {
                    SeeAllCommentsActivity.this.flagLoading = true;
                    final int offset = SeeAllCommentsActivity.this.list.size();
                    if ((offset % SeeAllCommentsActivity.COMMENTS_PER_PAGE) == 0) {
                        final int currentPageNo = (SeeAllCommentsActivity.this.list.size() / SeeAllCommentsActivity.COMMENTS_PER_PAGE) + 1;
                        new UserListAsyncTask(currentPageNo).execute();
                    }
                }
            }
        });

        this.send.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                SeeAllCommentsActivity.this.emoticonLayout.setVisibility(View.GONE);
                SeeAllCommentsActivity.this.showEmoticonsButton.setBackgroundResource(R.drawable.emoticonbutton);
                final String comment = SeeAllCommentsActivity.this.writecomment.getText().toString().trim();
                if ((comment != null) && (comment.length() > 0)) {
                    final InputMethodManager mgr = (InputMethodManager) SeeAllCommentsActivity.this
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    mgr.hideSoftInputFromWindow(SeeAllCommentsActivity.this.writecomment.getWindowToken(), 0);
                    if ((Config.getUserId() != null) && !Config.getUserId().equalsIgnoreCase("")) {
                        new CommentAsyncTask(SeeAllCommentsActivity.this.videoId).execute();
                    } else {
                        Alerts.showAlertOnly("Info", "User id not available.", SeeAllCommentsActivity.this);
                    }
                } else {
                    Alerts.showAlertOnly("Info", "Empty comment should not be post.", SeeAllCommentsActivity.this);
                }
            }
        });

    }

    @Override
    protected void onDestroy() {

        Util.clearImageCache(this);
        super.onDestroy();
    }

    String getCommentJsonRequest(final String videoId) throws JSONException {

        final String comment = this.writecomment.getText().toString();
        final JSONObject obj = new JSONObject();
        final JSONObject values = new JSONObject();
        values.put(USERID2, Config.getUserId());// video.getUserId()
        values.put(VIDEO_ID, videoId);

        final String sending = Util.encodedBase64(comment);
        values.put(COMMENT_TEXT, sending);

        obj.put(USER, values);
        return obj.toString();
    }

    void getUsers() {

        final String comment = this.writecomment.getText().toString().trim();
        if ((comment != null) && (comment.length() > 0)) {
            new AtUsersAsyncTask("").execute();
        }
    }

    void loadUsersList(final List<Friend> list) {

        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {

                SeeAllCommentsActivity.this.commentList.setVisibility(View.GONE);
                SeeAllCommentsActivity.this.usersListView.setVisibility(View.VISIBLE);

                SeeAllCommentsActivity.this.userList.clear();
                if ((list != null) && (list.size() > 0)) {
                    SeeAllCommentsActivity.this.userList.addAll(list);
                }
                if (SeeAllCommentsActivity.this.userAdapter == null) {
                    SeeAllCommentsActivity.this.userAdapter = new FacebookFriendsListAdapter(
                            SeeAllCommentsActivity.this, R.layout.facebook_user, SeeAllCommentsActivity.this.userList,
                            Config.getSocialSite(), false, false);
                    SeeAllCommentsActivity.this.usersListView.setAdapter(SeeAllCommentsActivity.this.userAdapter);
                }
                SeeAllCommentsActivity.this.userAdapter.notifyDataSetChanged();

                SeeAllCommentsActivity.this.usersListView.setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(final AdapterView<?> arg0, final View arg1, final int arg2, final long arg3) {

                        final Friend friendItem = SeeAllCommentsActivity.this.userList.get(arg2);
                        SeeAllCommentsActivity.this.friendSearch = true;
                        SeeAllCommentsActivity.this.usersListView.setVisibility(View.GONE);
                        SeeAllCommentsActivity.this.commentList.setVisibility(View.VISIBLE);
                        final String commentText = SeeAllCommentsActivity.this.writecomment.getText().toString();
                        String setNewCommentText = "";
                        if (commentText.indexOf('@') != -1) {
                            setNewCommentText = commentText.substring(0, commentText.indexOf('@'));
                        }
                        SeeAllCommentsActivity.this.writecomment.setText(setNewCommentText + friendItem.getFriendName());
                        SeeAllCommentsActivity.this.writecomment.setSelection(SeeAllCommentsActivity.this.writecomment
                                .getText().length());

                    }

                });

            }
        });

    }

    /** set the users list while user clicks on @ in write comment edit text */
    void setFilterList(final List<Friend> wtList, final String text) {

        new Thread(new Runnable() {

            @Override
            public void run() {

                final ArrayList<Friend> filterdList = new ArrayList<Friend>();
                for (int i = 0; i < wtList.size(); i++) {
                    final Friend frnd = wtList.get(i);
                    /*
                     * if ((frnd.getFriendName().toString().toLowerCase()).startsWith(text.toLowerCase())) {
                     * filterdList.add(frnd); }
                     */
                    if (frnd.getFriendName().toString().toLowerCase(Locale.getDefault())
                            .indexOf(text.toLowerCase(Locale.getDefault())) != -1) {
                        filterdList.add(frnd);
                    }
                }
                SeeAllCommentsActivity.this.loadUsersList(filterdList);
            }
        }).start();
        try {
            Thread.sleep(50);
        } catch (final InterruptedException e) {
            Alerts.showAlert("Exception", e.toString(), SeeAllCommentsActivity.this);
        }

    }

    public class AtUsersAsyncTask extends AsyncTask<Void, Void, Void> {

        private final String name;
        ProgressDialog progressDialog;
        Object returnObj;

        public AtUsersAsyncTask(final String name) {

            this.name = name;
        }

        @Override
        protected Void doInBackground(final Void... params) {

            LOG.v(SeeAllCommentsActivity.this.type);
            try {
                this.returnObj = Backend.getAtUsers(SeeAllCommentsActivity.this,
                        SeeAllCommentsActivity.getAtUserJsonRequest(this.name));
            } catch (final JSONException exception) {
                LOG.e(exception);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {

            super.onPostExecute(result);
            if (this.returnObj instanceof List<?>) {
                final List<Friend> list = (ArrayList<Friend>) this.returnObj;
                if ((list != null) && (list.size() > 0)) {
                    VideoPlayerApp.getInstance().setWootagFriendsList(list);
                    SeeAllCommentsActivity.this.loadUsersList(list);
                }
            }
        }
    }

    public class CommentAsyncTask extends AsyncTask<Void, Void, Void> {

        private static final String EMPTY = "";
        String currentVideoId;
        Comment dto;
        ProgressDialog progressDialog;
        Object returnObject;

        public CommentAsyncTask(final String videoId) {

            this.currentVideoId = videoId;
        }

        @Override
        protected Void doInBackground(final Void... params) {

            LOG.v(SeeAllCommentsActivity.this.type);
            try {
                this.returnObject = Backend.postComment(SeeAllCommentsActivity.this,
                        SeeAllCommentsActivity.this.getCommentJsonRequest(this.currentVideoId));
            } catch (final JSONException exception) {
                LOG.e(exception);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {

            super.onPostExecute(result);
            SeeAllCommentsActivity.this.writecomment.setText(EMPTY);
            if (this.progressDialog != null) {
                this.progressDialog.dismiss();
                if (this.returnObject != null) {
                    if (this.returnObject instanceof Comment) {
                        if (SeeAllCommentsActivity.this.list != null) {
                            SeeAllCommentsActivity.this.search.setVisibility(View.VISIBLE);
                            this.dto = (Comment) this.returnObject;
                            SeeAllCommentsActivity.this.list.add(0, this.dto);
                            if (SeeAllCommentsActivity.this.adapter == null) {
                                SeeAllCommentsActivity.this.adapter = new CommentAdapter(SeeAllCommentsActivity.this,
                                        SeeAllCommentsActivity.this.list, SeeAllCommentsActivity.this.videoId, true,
                                        SeeAllCommentsActivity.this.userId, EMPTY, null);
                                SeeAllCommentsActivity.this.commentList.setAdapter(SeeAllCommentsActivity.this.adapter);
                            } else {
                                SeeAllCommentsActivity.this.adapter.notifyDataSetChanged();
                                // commentList.setSelection((list.size() - 1));
                            }
                        }
                    } else if (this.returnObject instanceof ErrorResponse) {
                        final ErrorResponse response = (ErrorResponse) this.returnObject;
                        Alerts.showAlertOnly("Info", response.getMessage(), SeeAllCommentsActivity.this);
                    }
                }
            }

        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            this.progressDialog = ProgressDialog.show(SeeAllCommentsActivity.this, EMPTY, EMPTY, true);
            this.progressDialog.setContentView(((LayoutInflater) SeeAllCommentsActivity.this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.progress_bar, null, false));
            this.progressDialog.setCancelable(false);
            this.progressDialog.setCanceledOnTouchOutside(false);
            this.progressDialog.show();

        }

    }

    private class UserListAsyncTask extends AsyncTask<Void, Void, Void> {

        private static final String EMPTY = "";
        List<Comment> currentList;
        int pageNo;
        ProgressDialog progressDialog;

        public UserListAsyncTask(final int pageNo) {

            this.pageNo = pageNo;
        }

        @Override
        protected Void doInBackground(final Void... params) {

            LOG.v(SeeAllCommentsActivity.this.type);
            try {
                this.currentList = Backend.getCommentList(SeeAllCommentsActivity.this,
                        SeeAllCommentsActivity.getAllCommentsURL + SeeAllCommentsActivity.this.videoId + "/"
                                + this.pageNo);
            } catch (final JSONException exception) {
                LOG.e(exception);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {

            super.onPostExecute(result);
            if (this.progressDialog != null) {
                SeeAllCommentsActivity.this.flagLoading = false;
                this.progressDialog.dismiss();
                if ((this.currentList != null) && (this.currentList.size() > 0)) {
                    SeeAllCommentsActivity.this.search.setVisibility(View.VISIBLE);
                    if (SeeAllCommentsActivity.this.adapter == null) {
                        for (int i = 0; i < this.currentList.size(); i++) {
                            SeeAllCommentsActivity.this.list.add(this.currentList.get(i));
                        }
                        SeeAllCommentsActivity.this.adapter = new CommentAdapter(SeeAllCommentsActivity.this,
                                SeeAllCommentsActivity.this.list, SeeAllCommentsActivity.this.videoId, true,
                                SeeAllCommentsActivity.this.userId, EMPTY, null);
                        LOG.i("commentList set adaptere");
                        SeeAllCommentsActivity.this.commentList.setAdapter(SeeAllCommentsActivity.this.adapter);
                    } else {
                        for (int i = 0; i < this.currentList.size(); i++) {
                            SeeAllCommentsActivity.this.list.add(this.currentList.get(i));
                        }
                        SeeAllCommentsActivity.this.adapter.notifyDataSetChanged();
                    }
                } else {
                    SeeAllCommentsActivity.this.search.setVisibility(View.GONE);
                }
            }

        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            this.progressDialog = ProgressDialog.show(SeeAllCommentsActivity.this, EMPTY, EMPTY, true);
            this.progressDialog.setContentView(((LayoutInflater) SeeAllCommentsActivity.this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.progress_bar, null, false));
            this.progressDialog.setCancelable(false);
            this.progressDialog.setCanceledOnTouchOutside(false);
            this.progressDialog.show();

        }

    }

}
