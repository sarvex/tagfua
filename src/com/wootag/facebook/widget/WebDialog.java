/**
 * Copyright 2010-present Facebook. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package com.wootag.facebook.widget;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.wootag.Constant;
import com.wootag.R;
import com.wootag.facebook.FacebookDialogException;
import com.wootag.facebook.FacebookException;
import com.wootag.facebook.FacebookOperationCanceledException;
import com.wootag.facebook.FacebookRequestError;
import com.wootag.facebook.FacebookServiceException;
import com.wootag.facebook.Session;
import com.wootag.facebook.internal.Logger;
import com.wootag.facebook.internal.ServerProtocol;
import com.wootag.facebook.internal.Utility;
import com.wootag.facebook.internal.Validate;

/**
 * This class provides a mechanism for displaying Facebook Web dialogs inside a Dialog. Helper methods are provided to
 * construct commonly-used dialogs, or a caller can specify arbitrary parameters to call other dialogs.
 */
public class WebDialog extends Dialog {

    private static final String LOG_TAG = Logger.LOG_TAG_BASE + "WebDialog";
    private static final String DISPLAY_TOUCH = "touch";
    private static final String USER_AGENT = "user_agent";
    static final String REDIRECT_URI = "fbconnect://success";
    static final String CANCEL_URI = "fbconnect://cancel";
    static final boolean DISABLE_SSL_CHECK_FOR_TESTING = false;

    // width below which there are no extra margins
    private static final int NO_PADDING_SCREEN_WIDTH = 480;
    // width beyond which we're always using the MIN_SCALE_FACTOR
    private static final int MAX_PADDING_SCREEN_WIDTH = 800;
    // height below which there are no extra margins
    private static final int NO_PADDING_SCREEN_HEIGHT = 800;
    // height beyond which we're always using the MIN_SCALE_FACTOR
    private static final int MAX_PADDING_SCREEN_HEIGHT = 1280;

    // the minimum scaling factor for the web dialog (50% of screen size)
    private static final double MIN_SCALE_FACTOR = 0.5;
    // translucent border around the webview
    private static final int BACKGROUND_GRAY = 0xCC000000;

    public static final int DEFAULT_THEME = android.R.style.Theme_Translucent_NoTitleBar;

    private final String url;
    private OnCompleteListener onCompleteListener;
    protected WebView webView;
    protected ProgressDialog spinner;
    protected ImageView crossImageView;
    protected FrameLayout contentFrameLayout;
    private boolean listenerCalled;
    protected boolean detached;

    /**
     * Constructor which can be used to display a dialog with an already-constructed URL.
     *
     * @param context the context to use to display the dialog
     * @param url the URL of the Web Dialog to display; no validation is done on this URL, but it should be a valid URL
     *            pointing to a Facebook Web Dialog
     */
    public WebDialog(final Context context, final String url) {

        this(context, url, DEFAULT_THEME);
    }

    /**
     * Constructor which will construct the URL of the Web dialog based on the specified parameters.
     *
     * @param context the context to use to display the dialog
     * @param action the portion of the dialog URL following "dialog/"
     * @param parameters parameters which will be included as part of the URL
     * @param theme identifier of a theme to pass to the Dialog class
     * @param listener the listener to notify, or null if no notification is desired
     */
    public WebDialog(final Context context, final String action, Bundle parameters, final int theme,
            final OnCompleteListener listener) {

        super(context, theme);

        if (parameters == null) {
            parameters = new Bundle();
        }

        // our webview client only handles the redirect uri we specify, so just hard code it here
        parameters.putString(ServerProtocol.DIALOG_PARAM_REDIRECT_URI, REDIRECT_URI);

        parameters.putString(ServerProtocol.DIALOG_PARAM_DISPLAY, DISPLAY_TOUCH);

        final Uri uri = Utility.buildUri(ServerProtocol.getDialogAuthority(), ServerProtocol.getAPIVersion() + "/"
                + ServerProtocol.DIALOG_PATH + action, parameters);
        this.url = uri.toString();
        this.onCompleteListener = listener;
    }

    /**
     * Constructor which can be used to display a dialog with an already-constructed URL and a custom theme.
     *
     * @param context the context to use to display the dialog
     * @param url the URL of the Web Dialog to display; no validation is done on this URL, but it should be a valid URL
     *            pointing to a Facebook Web Dialog
     * @param theme identifier of a theme to pass to the Dialog class
     */
    public WebDialog(final Context context, final String url, final int theme) {

        super(context, theme);
        this.url = url;
    }

    public static Bundle decodeUrl(final String s) {

        final Bundle params = new Bundle();
        if (s != null) {
            final String array[] = s.split("&");
            for (final String parameter : array) {
                final String v[] = parameter.split("=");

                try {
                    if (v.length == 2) {
                        params.putString(URLDecoder.decode(v[0], Constant.UTF_8),
                                URLDecoder.decode(v[1], Constant.UTF_8));
                    } else if (v.length == 1) {
                        params.putString(URLDecoder.decode(v[0], Constant.UTF_8), "");
                    }
                } catch (final UnsupportedEncodingException e) {
                    // shouldn't happen
                }
            }
        }
        return params;
    }

    static Bundle parseUrl(String url) {

        // hack to prevent MalformedURLException
        url = url.replace("fbconnect", "http");
        try {
            final URL u = new URL(url);
            final Bundle bundle = decodeUrl(u.getQuery());
            bundle.putAll(decodeUrl(u.getRef()));
            return bundle;
        } catch (final MalformedURLException e) {
            return new Bundle();
        }
    }

    @Override
    public void dismiss() {

        if (this.webView != null) {
            this.webView.stopLoading();
        }
        if (!this.detached) {
            if (this.spinner.isShowing()) {
                this.spinner.dismiss();
            }
            super.dismiss();
        }
    }

    /**
     * Gets the listener which will be notified when the dialog finishes.
     *
     * @return the listener, or null if none has been specified
     */
    public OnCompleteListener getOnCompleteListener() {

        return this.onCompleteListener;
    }

    @Override
    public void onAttachedToWindow() {

        this.detached = false;
        super.onAttachedToWindow();
    }

    @Override
    public void onDetachedFromWindow() {

        this.detached = true;
        super.onDetachedFromWindow();
    }

    /**
     * Sets the listener which will be notified when the dialog finishes.
     *
     * @param listener the listener to notify, or null if no notification is desired
     */
    public void setOnCompleteListener(final OnCompleteListener listener) {

        this.onCompleteListener = listener;
    }

    private void calculateSize() {

        final WindowManager wm = (WindowManager) this.getContext().getSystemService(Context.WINDOW_SERVICE);
        final Display display = wm.getDefaultDisplay();
        final DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        // always use the portrait dimensions to do the scaling calculations so we always get a portrait shaped
        // web dialog
        final int width = metrics.widthPixels < metrics.heightPixels ? metrics.widthPixels : metrics.heightPixels;
        final int height = metrics.widthPixels < metrics.heightPixels ? metrics.heightPixels : metrics.widthPixels;

        final int dialogWidth = Math.min(
                this.getScaledSize(width, metrics.density, NO_PADDING_SCREEN_WIDTH, MAX_PADDING_SCREEN_WIDTH),
                metrics.widthPixels);
        final int dialogHeight = Math.min(
                this.getScaledSize(height, metrics.density, NO_PADDING_SCREEN_HEIGHT, MAX_PADDING_SCREEN_HEIGHT),
                metrics.heightPixels);

        this.getWindow().setLayout(dialogWidth, dialogHeight);
    }

    private void createCrossImage() {

        this.crossImageView = new ImageView(this.getContext());
        // Dismiss the dialog when user click on the 'x'
        this.crossImageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {

                WebDialog.this.sendCancelToListener();
                WebDialog.this.dismiss();
            }
        });
        final Drawable crossDrawable = this.getContext().getResources().getDrawable(R.drawable.com_facebook_close);
        this.crossImageView.setImageDrawable(crossDrawable);
        /*
         * 'x' should not be visible while webview is loading make it visible only after webview has fully loaded
         */
        this.crossImageView.setVisibility(View.INVISIBLE);
    }

    /**
     * Returns a scaled size (either width or height) based on the parameters passed.
     *
     * @param screenSize a pixel dimension of the screen (either width or height)
     * @param density density of the screen
     * @param noPaddingSize the size at which there's no padding for the dialog
     * @param maxPaddingSize the size at which to apply maximum padding for the dialog
     * @return a scaled size.
     */
    private int getScaledSize(final int screenSize, final float density, final int noPaddingSize,
            final int maxPaddingSize) {

        final int scaledSize = (int) (screenSize / density);
        double scaleFactor;
        if (scaledSize <= noPaddingSize) {
            scaleFactor = 1.0;
        } else if (scaledSize >= maxPaddingSize) {
            scaleFactor = MIN_SCALE_FACTOR;
        } else {
            // between the noPadding and maxPadding widths, we take a linear reduction to go from 100%
            // of screen size down to MIN_SCALE_FACTOR
            scaleFactor = MIN_SCALE_FACTOR
                    + ((((double) (maxPaddingSize - scaledSize)) / ((double) (maxPaddingSize - noPaddingSize))) * (1.0 - MIN_SCALE_FACTOR));
        }
        return (int) (screenSize * scaleFactor);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setUpWebView(final int margin) {

        final LinearLayout webViewContainer = new LinearLayout(this.getContext());
        this.webView = new WebView(this.getContext());
        this.webView.setVerticalScrollBarEnabled(false);
        this.webView.setHorizontalScrollBarEnabled(false);
        this.webView.setWebViewClient(new DialogWebViewClient());
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.loadUrl(this.url);
        this.webView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        this.webView.setVisibility(View.INVISIBLE);
        this.webView.getSettings().setSavePassword(false);
        this.webView.getSettings().setSaveFormData(false);

        webViewContainer.setPadding(margin, margin, margin, margin);
        webViewContainer.addView(this.webView);
        webViewContainer.setBackgroundColor(BACKGROUND_GRAY);
        this.contentFrameLayout.addView(webViewContainer);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        this.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(final DialogInterface dialogInterface) {

                WebDialog.this.sendCancelToListener();
            }
        });

        this.spinner = new ProgressDialog(this.getContext());
        this.spinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.spinner.setMessage(this.getContext().getString(R.string.com_facebook_loading));
        this.spinner.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(final DialogInterface dialogInterface) {

                WebDialog.this.sendCancelToListener();
                WebDialog.this.dismiss();
            }
        });

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.contentFrameLayout = new FrameLayout(this.getContext());

        // First calculate how big the frame layout should be
        this.calculateSize();
        this.getWindow().setGravity(Gravity.CENTER);

        // resize the dialog if the soft keyboard comes up
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        /*
         * Create the 'x' image, but don't add to the contentFrameLayout layout yet at this point, we only need to know
         * its drawable width and height to place the webview
         */
        this.createCrossImage();

        /*
         * Now we know 'x' drawable width and height, layout the webview and add it the contentFrameLayout layout
         */
        final int crossWidth = this.crossImageView.getDrawable().getIntrinsicWidth();

        this.setUpWebView((crossWidth / 2) + 1);

        /*
         * Finally add the 'x' image to the contentFrameLayout layout and add contentFrameLayout to the Dialog view
         */
        this.contentFrameLayout.addView(this.crossImageView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        this.setContentView(this.contentFrameLayout);
    }

    void sendCancelToListener() {

        this.sendErrorToListener(new FacebookOperationCanceledException());
    }

    void sendErrorToListener(final Throwable error) {

        if ((this.onCompleteListener != null) && !this.listenerCalled) {
            this.listenerCalled = true;
            FacebookException facebookException = null;
            if (error instanceof FacebookException) {
                facebookException = (FacebookException) error;
            } else {
                facebookException = new FacebookException(error);
            }
            this.onCompleteListener.onComplete(null, facebookException);
        }
    }

    void sendSuccessToListener(final Bundle values) {

        if ((this.onCompleteListener != null) && !this.listenerCalled) {
            this.listenerCalled = true;
            this.onCompleteListener.onComplete(values, null);
        }
    }

    /**
     * Provides a builder that allows construction of an arbitary Facebook web dialog.
     */
    public static class Builder extends BuilderBase<Builder> {

        /**
         * Constructor that builds a dialog for an authenticated user.
         *
         * @param context the Context within which the dialog will be shown.
         * @param session the Session representing an authenticating user to use for showing the dialog; must not be
         *            null, and must be opened.
         * @param action the portion of the dialog URL following www.facebook.com/dialog/. See
         *            https://developers.facebook.com/docs/reference/dialogs/ for details.
         * @param parameters a Bundle containing parameters to pass as part of the URL.
         */
        public Builder(final Context context, final Session session, final String action, final Bundle parameters) {

            super(context, session, action, parameters);
        }

        /**
         * Constructor that builds a dialog using either the active session, or the application id specified in the
         * application/meta-data.
         *
         * @param context the Context within which the dialog will be shown.
         * @param action the portion of the dialog URL following www.facebook.com/dialog/. See
         *            https://developers.facebook.com/docs/reference/dialogs/ for details.
         */
        public Builder(final Context context, final String action) {

            super(context, action);
        }

        /**
         * Constructor that builds a dialog without an authenticated user.
         *
         * @param context the Context within which the dialog will be shown.
         * @param applicationId the application ID to be included in the dialog URL.
         * @param action the portion of the dialog URL following www.facebook.com/dialog/. See
         *            https://developers.facebook.com/docs/reference/dialogs/ for details.
         * @param parameters a Bundle containing parameters to pass as part of the URL.
         */
        public Builder(final Context context, final String applicationId, final String action, final Bundle parameters) {

            super(context, applicationId, action, parameters);
        }
    }

    /**
     * Provides a builder that allows construction of the parameters for showing the <a
     * href="https://developers.facebook.com/docs/reference/dialogs/feed">Feed Dialog</a>.
     */
    public static class FeedDialogBuilder extends BuilderBase<FeedDialogBuilder> {

        private static final String FEED_DIALOG = "feed";
        private static final String FROM_PARAM = "from";
        private static final String TO_PARAM = "to";
        private static final String LINK_PARAM = "link";
        private static final String PICTURE_PARAM = "picture";
        private static final String SOURCE_PARAM = "source";
        private static final String NAME_PARAM = "name";
        private static final String CAPTION_PARAM = "caption";
        private static final String DESCRIPTION_PARAM = "description";

        /**
         * Constructor that builds a Feed Dialog using either the active session, or the application ID specified in the
         * application/meta-data.
         *
         * @param context the Context within which the dialog will be shown.
         */
        public FeedDialogBuilder(final Context context) {

            super(context, FEED_DIALOG);
        }

        /**
         * Constructor that builds a Feed Dialog using the provided session.
         *
         * @param context the Context within which the dialog will be shown.
         * @param session the Session representing an authenticating user to use for showing the dialog; must not be
         *            null, and must be opened.
         */
        public FeedDialogBuilder(final Context context, final Session session) {

            super(context, session, FEED_DIALOG, null);
        }

        /**
         * Constructor that builds a Feed Dialog using the provided session and parameters.
         *
         * @param context the Context within which the dialog will be shown.
         * @param session the Session representing an authenticating user to use for showing the dialog; must not be
         *            null, and must be opened.
         * @param parameters a Bundle containing parameters to pass as part of the dialog URL. No validation is done on
         *            these parameters; it is the caller's responsibility to ensure they are valid. For more
         *            information, see <a href="https://developers.facebook.com/docs/reference/dialogs/feed/">
         *            https://developers.facebook.com/docs/reference/dialogs/feed/</a>.
         */
        public FeedDialogBuilder(final Context context, final Session session, final Bundle parameters) {

            super(context, session, FEED_DIALOG, parameters);
        }

        /**
         * Constructor that builds a Feed Dialog using the provided application ID and parameters.
         *
         * @param context the Context within which the dialog will be shown.
         * @param applicationId the application ID to use. If null, the application ID specified in the
         *            application/meta-data will be used instead.
         * @param parameters a Bundle containing parameters to pass as part of the dialog URL. No validation is done on
         *            these parameters; it is the caller's responsibility to ensure they are valid. For more
         *            information, see <a href="https://developers.facebook.com/docs/reference/dialogs/feed/">
         *            https://developers.facebook.com/docs/reference/dialogs/feed/</a>.
         */
        public FeedDialogBuilder(final Context context, final String applicationId, final Bundle parameters) {

            super(context, applicationId, FEED_DIALOG, parameters);
        }

        /**
         * Sets the caption to be displayed.
         *
         * @param caption the caption
         * @return the builder
         */
        public FeedDialogBuilder setCaption(final String caption) {

            this.getParameters().putString(CAPTION_PARAM, caption);
            return this;
        }

        /**
         * Sets the description to be displayed.
         *
         * @param description the description
         * @return the builder
         */
        public FeedDialogBuilder setDescription(final String description) {

            this.getParameters().putString(DESCRIPTION_PARAM, description);
            return this;
        }

        /**
         * Sets the ID of the profile that is posting to Facebook. If none is specified, the default is "me". This
         * profile must be either the authenticated user or a Page that the user is an administrator of.
         *
         * @param id Facebook ID of the profile to post from
         * @return the builder
         */
        public FeedDialogBuilder setFrom(final String id) {

            this.getParameters().putString(FROM_PARAM, id);
            return this;
        }

        /**
         * Sets the URL of a link to be shared.
         *
         * @param link the URL
         * @return the builder
         */
        public FeedDialogBuilder setLink(final String link) {

            this.getParameters().putString(LINK_PARAM, link);
            return this;
        }

        /**
         * Sets the name of the item being shared.
         *
         * @param name the name
         * @return the builder
         */
        public FeedDialogBuilder setName(final String name) {

            this.getParameters().putString(NAME_PARAM, name);
            return this;
        }

        /**
         * Sets the URL of a picture to be shared.
         *
         * @param picture the URL of the picture
         * @return the builder
         */
        public FeedDialogBuilder setPicture(final String picture) {

            this.getParameters().putString(PICTURE_PARAM, picture);
            return this;
        }

        /**
         * Sets the URL of a media file attached to this post. If this is set, any picture set via setPicture will be
         * ignored.
         *
         * @param source the URL of the media file
         * @return the builder
         */
        public FeedDialogBuilder setSource(final String source) {

            this.getParameters().putString(SOURCE_PARAM, source);
            return this;
        }

        /**
         * Sets the ID of the profile that the story will be published to. If not specified, it will default to the same
         * profile that the story is being published from. The ID must be a friend who also uses your app.
         *
         * @param id Facebook ID of the profile to post to
         * @return the builder
         */
        public FeedDialogBuilder setTo(final String id) {

            this.getParameters().putString(TO_PARAM, id);
            return this;
        }
    }

    /**
     * Interface that implements a listener to be called when the user's interaction with the dialog completes, whether
     * because the dialog finished successfully, or it was cancelled, or an error was encountered.
     */
    public interface OnCompleteListener {

        /**
         * Called when the dialog completes.
         *
         * @param values on success, contains the values returned by the dialog
         * @param error on an error, contains an exception describing the error
         */
        void onComplete(Bundle values, FacebookException error);
    }

    /**
     * Provides a builder that allows construction of the parameters for showing the <a
     * href="https://developers.facebook.com/docs/reference/dialogs/requests">Requests Dialog</a>.
     */
    public static class RequestsDialogBuilder extends BuilderBase<RequestsDialogBuilder> {

        private static final String APPREQUESTS_DIALOG = "apprequests";
        private static final String MESSAGE_PARAM = "message";
        private static final String TO_PARAM = "to";
        private static final String DATA_PARAM = "data";
        private static final String TITLE_PARAM = "title";

        /**
         * Constructor that builds a Requests Dialog using either the active session, or the application ID specified in
         * the application/meta-data.
         *
         * @param context the Context within which the dialog will be shown.
         */
        public RequestsDialogBuilder(final Context context) {

            super(context, APPREQUESTS_DIALOG);
        }

        /**
         * Constructor that builds a Requests Dialog using the provided session.
         *
         * @param context the Context within which the dialog will be shown.
         * @param session the Session representing an authenticating user to use for showing the dialog; must not be
         *            null, and must be opened.
         */
        public RequestsDialogBuilder(final Context context, final Session session) {

            super(context, session, APPREQUESTS_DIALOG, null);
        }

        /**
         * Constructor that builds a Requests Dialog using the provided session and parameters.
         *
         * @param context the Context within which the dialog will be shown.
         * @param session the Session representing an authenticating user to use for showing the dialog; must not be
         *            null, and must be opened.
         * @param parameters a Bundle containing parameters to pass as part of the dialog URL. No validation is done on
         *            these parameters; it is the caller's responsibility to ensure they are valid. For more
         *            information, see <a href="https://developers.facebook.com/docs/reference/dialogs/requests/">
         *            https://developers.facebook.com/docs/reference/dialogs/requests/</a>.
         */
        public RequestsDialogBuilder(final Context context, final Session session, final Bundle parameters) {

            super(context, session, APPREQUESTS_DIALOG, parameters);
        }

        /**
         * Constructor that builds a Requests Dialog using the provided application ID and parameters.
         *
         * @param context the Context within which the dialog will be shown.
         * @param applicationId the application ID to use. If null, the application ID specified in the
         *            application/meta-data will be used instead.
         * @param parameters a Bundle containing parameters to pass as part of the dialog URL. No validation is done on
         *            these parameters; it is the caller's responsibility to ensure they are valid. For more
         *            information, see <a href="https://developers.facebook.com/docs/reference/dialogs/requests/">
         *            https://developers.facebook.com/docs/reference/dialogs/requests/</a>.
         */
        public RequestsDialogBuilder(final Context context, final String applicationId, final Bundle parameters) {

            super(context, applicationId, APPREQUESTS_DIALOG, parameters);
        }

        /**
         * Sets optional data which can be used for tracking; maximum length is 255 characters.
         *
         * @param data the data
         * @return the builder
         */
        public RequestsDialogBuilder setData(final String data) {

            this.getParameters().putString(DATA_PARAM, data);
            return this;
        }

        /**
         * Sets the string users receiving the request will see. The maximum length is 60 characters.
         *
         * @param message the message
         * @return the builder
         */
        public RequestsDialogBuilder setMessage(final String message) {

            this.getParameters().putString(MESSAGE_PARAM, message);
            return this;
        }

        /**
         * Sets an optional title for the dialog; maximum length is 50 characters.
         *
         * @param title the title
         * @return the builder
         */
        public RequestsDialogBuilder setTitle(final String title) {

            this.getParameters().putString(TITLE_PARAM, title);
            return this;
        }

        /**
         * Sets the user ID or user name the request will be sent to. If this is not specified, a friend selector will
         * be displayed and the user can select up to 50 friends.
         *
         * @param id the id or user name to send the request to
         * @return the builder
         */
        public RequestsDialogBuilder setTo(final String id) {

            this.getParameters().putString(TO_PARAM, id);
            return this;
        }
    }

    private static class BuilderBase<CONCRETE extends BuilderBase<?>> {

        private Context context;
        private Session session;
        private String applicationId;
        private String action;
        private int theme = DEFAULT_THEME;
        private OnCompleteListener listener;
        private Bundle parameters;

        protected BuilderBase(final Context context, final Session session, final String action, final Bundle parameters) {

            Validate.notNull(session, "session");
            if (!session.isOpened()) {
                throw new FacebookException("Attempted to use a Session that was not open.");
            }
            this.session = session;

            this.finishInit(context, action, parameters);
        }

        protected BuilderBase(final Context context, final String action) {

            final Session activeSession = Session.getActiveSession();
            if ((activeSession != null) && activeSession.isOpened()) {
                this.session = activeSession;
            } else {
                final String applicationId = Utility.getMetadataApplicationId(context);
                if (applicationId != null) {
                    this.applicationId = applicationId;
                } else {
                    throw new FacebookException("Attempted to create a builder without an open"
                            + " Active Session or a valid default Application ID.");
                }
            }
            this.finishInit(context, action, null);
        }

        protected BuilderBase(final Context context, String applicationId, final String action, final Bundle parameters) {

            if (applicationId == null) {
                applicationId = Utility.getMetadataApplicationId(context);
            }
            Validate.notNullOrEmpty(applicationId, "applicationId");
            this.applicationId = applicationId;

            this.finishInit(context, action, parameters);
        }

        /**
         * Constructs a WebDialog using the parameters provided. The dialog is not shown, but is ready to be shown by
         * calling Dialog.show().
         *
         * @return the WebDialog
         */
        public WebDialog build() {

            if ((this.session != null) && this.session.isOpened()) {
                this.parameters.putString(ServerProtocol.DIALOG_PARAM_APP_ID, this.session.getApplicationId());
                this.parameters.putString(ServerProtocol.DIALOG_PARAM_ACCESS_TOKEN, this.session.getAccessToken());
            } else {
                this.parameters.putString(ServerProtocol.DIALOG_PARAM_APP_ID, this.applicationId);
            }

            return new WebDialog(this.context, this.action, this.parameters, this.theme, this.listener);
        }

        /**
         * Sets the listener which will be notified when the dialog finishes.
         *
         * @param listener the listener to notify, or null if no notification is desired
         * @return the builder
         */
        public CONCRETE setOnCompleteListener(final OnCompleteListener listener) {

            this.listener = listener;
            @SuppressWarnings("unchecked")
            final CONCRETE result = (CONCRETE) this;
            return result;
        }

        /**
         * Sets a theme identifier which will be passed to the underlying Dialog.
         *
         * @param theme a theme identifier which will be passed to the Dialog class
         * @return the builder
         */
        public CONCRETE setTheme(final int theme) {

            this.theme = theme;
            @SuppressWarnings("unchecked")
            final CONCRETE result = (CONCRETE) this;
            return result;
        }

        private void finishInit(final Context context, final String action, final Bundle parameters) {

            this.context = context;
            this.action = action;
            if (parameters != null) {
                this.parameters = parameters;
            } else {
                this.parameters = new Bundle();
            }
        }

        protected String getApplicationId() {

            return this.applicationId;
        }

        protected Context getContext() {

            return this.context;
        }

        protected WebDialog.OnCompleteListener getListener() {

            return this.listener;
        }

        protected Bundle getParameters() {

            return this.parameters;
        }

        protected int getTheme() {

            return this.theme;
        }
    }

    private class DialogWebViewClient extends WebViewClient {

        @Override
        public void onPageFinished(final WebView view, final String url) {

            super.onPageFinished(view, url);
            if (!WebDialog.this.detached) {
                WebDialog.this.spinner.dismiss();
            }
            /*
             * Once web view is fully loaded, set the contentFrameLayout background to be transparent and make visible
             * the 'x' image.
             */
            WebDialog.this.contentFrameLayout.setBackgroundColor(Color.TRANSPARENT);
            WebDialog.this.webView.setVisibility(View.VISIBLE);
            WebDialog.this.crossImageView.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageStarted(final WebView view, final String url, final Bitmap favicon) {

            // Utility.logd(LOG_TAG, "Webview loading URL: " + url);
            super.onPageStarted(view, url, favicon);
            if (!WebDialog.this.detached) {
                WebDialog.this.spinner.show();
            }
        }

        @Override
        public void onReceivedError(final WebView view, final int errorCode, final String description,
                final String failingUrl) {

            super.onReceivedError(view, errorCode, description, failingUrl);
            WebDialog.this.sendErrorToListener(new FacebookDialogException(description, errorCode, failingUrl));
            WebDialog.this.dismiss();
        }

        @Override
        public void onReceivedSslError(final WebView view, final SslErrorHandler handler, final SslError error) {

            if (DISABLE_SSL_CHECK_FOR_TESTING) {
                handler.proceed();
            } else {
                super.onReceivedSslError(view, handler, error);

                WebDialog.this.sendErrorToListener(new FacebookDialogException(null, ERROR_FAILED_SSL_HANDSHAKE, null));
                handler.cancel();
                WebDialog.this.dismiss();
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, final String url) {

            // Utility.logd(LOG_TAG, "Redirect URL: " + url);
            if (url.startsWith(WebDialog.REDIRECT_URI)) {
                final Bundle values = parseUrl(url);

                String error = values.getString("error");
                if (error == null) {
                    error = values.getString("error_type");
                }

                String errorMessage = values.getString("error_msg");
                if (errorMessage == null) {
                    errorMessage = values.getString("error_description");
                }
                final String errorCodeString = values.getString("error_code");
                int errorCode = FacebookRequestError.INVALID_ERROR_CODE;
                if (!Utility.isNullOrEmpty(errorCodeString)) {
                    try {
                        errorCode = Integer.parseInt(errorCodeString);
                    } catch (final NumberFormatException ex) {
                        errorCode = FacebookRequestError.INVALID_ERROR_CODE;
                    }
                }

                if (Utility.isNullOrEmpty(error) && Utility.isNullOrEmpty(errorMessage)
                        && (errorCode == FacebookRequestError.INVALID_ERROR_CODE)) {
                    WebDialog.this.sendSuccessToListener(values);
                } else if ((error != null)
                        && (error.equals("access_denied") || error.equals("OAuthAccessDeniedException"))) {
                    WebDialog.this.sendCancelToListener();
                } else {
                    final FacebookRequestError requestError = new FacebookRequestError(errorCode, error, errorMessage);
                    WebDialog.this.sendErrorToListener(new FacebookServiceException(requestError, errorMessage));
                }

                WebDialog.this.dismiss();
                return true;
            } else if (url.startsWith(WebDialog.CANCEL_URI)) {
                WebDialog.this.sendCancelToListener();
                WebDialog.this.dismiss();
                return true;
            } else if (url.contains(DISPLAY_TOUCH)) {
                return false;
            }
            // launch non-dialog URLs in a full browser
            WebDialog.this.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            return true;
        }
    }
}
