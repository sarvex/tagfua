/**
 * Copyright 2010-present Facebook. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package com.TagFu.facebook.widget;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;

import com.TagFu.R;
import com.TagFu.facebook.AppEventsLogger;
import com.TagFu.facebook.FacebookException;
import com.TagFu.facebook.Request;
import com.TagFu.facebook.Response;
import com.TagFu.facebook.Session;
import com.TagFu.facebook.SessionDefaultAudience;
import com.TagFu.facebook.SessionLoginBehavior;
import com.TagFu.facebook.SessionState;
import com.TagFu.facebook.internal.AnalyticsEvents;
import com.TagFu.facebook.internal.SessionAuthorizationType;
import com.TagFu.facebook.internal.SessionTracker;
import com.TagFu.facebook.internal.Utility;
import com.TagFu.facebook.internal.Utility.FetchedAppSettings;
import com.TagFu.facebook.model.GraphUser;

/**
 * A Log In/Log Out button that maintains session state and logs in/out for the app.
 * <p/>
 * This control will create and use the active session upon construction if it has the available data (if the app ID is
 * specified in the manifest). It will also open the active session if it does not require user interaction (i.e. if the
 * session is in the {@link com.TagFu.facebook.SessionState#CREATED_TOKEN_LOADED} state. Developers can override the
 * use of the active session by calling the {@link #setSession(com.TagFu.facebook.Session)} method.
 */
public class LoginButton extends Button {

    protected static final String TAG = LoginButton.class.getName();

    protected String applicationId;
    protected SessionTracker sessionTracker;
    protected GraphUser user;
    private Session userInfoSession;
    protected boolean confirmLogout;
    private boolean fetchUserInfo;
    private String loginText;
    private String logoutText;
    protected UserInfoChangedCallback userInfoChangedCallback;
    protected Fragment parentFragment;
    protected LoginButtonProperties properties = new LoginButtonProperties();
    protected String loginLogoutEventName = AnalyticsEvents.EVENT_LOGIN_VIEW_USAGE;
    protected OnClickListener listenerCallback;
    private boolean nuxChecked;
    private ToolTipPopup.Style nuxStyle = ToolTipPopup.Style.BLUE;
    private ToolTipMode nuxMode = ToolTipMode.DEFAULT;
    private long nuxDisplayTime = ToolTipPopup.DEFAULT_POPUP_DISPLAY_TIME;
    private ToolTipPopup nuxPopup;

    /**
     * Create the LoginButton.
     *
     * @see View#View(Context)
     */
    public LoginButton(final Context context) {

        super(context);
        this.initializeActiveSessionWithCachedToken(context);
        // since onFinishInflate won't be called, we need to finish initialization ourselves
        this.finishInit();
    }

    /**
     * Create the LoginButton by inflating from XML
     *
     * @see View#View(Context, AttributeSet)
     */
    public LoginButton(final Context context, final AttributeSet attrs) {

        super(context, attrs);

        if (attrs.getStyleAttribute() == 0) {
            // apparently there's no method of setting a default style in xml,
            // so in case the users do not explicitly specify a style, we need
            // to use sensible defaults.
            this.setGravity(Gravity.CENTER);
            this.setTextColor(this.getResources().getColor(R.color.com_facebook_loginview_text_color));
            this.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    this.getResources().getDimension(R.dimen.com_facebook_loginview_text_size));
            this.setTypeface(Typeface.DEFAULT_BOLD);
            if (this.isInEditMode()) {
                // cannot use a drawable in edit mode, so setting the background color instead
                // of a background resource.
                this.setBackgroundColor(this.getResources().getColor(R.color.com_facebook_blue));
                // hardcoding in edit mode as getResources().getString() doesn't seem to work in IntelliJ
                this.loginText = "Log in with Facebook";
            } else {
                this.setBackgroundResource(R.drawable.com_facebook_button_blue);
                this.setCompoundDrawablesWithIntrinsicBounds(R.drawable.com_facebook_inverse_icon, 0, 0, 0);
                this.setCompoundDrawablePadding(this.getResources().getDimensionPixelSize(
                        R.dimen.com_facebook_loginview_compound_drawable_padding));
                this.setPadding(this.getResources().getDimensionPixelSize(R.dimen.com_facebook_loginview_padding_left),
                        this.getResources().getDimensionPixelSize(R.dimen.com_facebook_loginview_padding_top), this
                                .getResources().getDimensionPixelSize(R.dimen.com_facebook_loginview_padding_right),
                        this.getResources().getDimensionPixelSize(R.dimen.com_facebook_loginview_padding_bottom));
            }
        }
        this.parseAttributes(attrs);
        if (!this.isInEditMode()) {
            this.initializeActiveSessionWithCachedToken(context);
        }
    }

    /**
     * Create the LoginButton by inflating from XML and applying a style.
     *
     * @see View#View(Context, AttributeSet, int)
     */
    public LoginButton(final Context context, final AttributeSet attrs, final int defStyle) {

        super(context, attrs, defStyle);
        this.parseAttributes(attrs);
        this.initializeActiveSessionWithCachedToken(context);
    }

    /**
     * Clears the permissions currently associated with this LoginButton.
     */
    public void clearPermissions() {

        this.properties.clearPermissions();
    }

    /**
     * Dismisses the Nux Tooltip if it is currently visible
     */
    public void dismissToolTip() {

        if (this.nuxPopup != null) {
            this.nuxPopup.dismiss();
            this.nuxPopup = null;
        }
    }

    /**
     * Gets the default audience to use when the session is opened. This value is only useful when specifying write
     * permissions for the native login dialog.
     *
     * @return the default audience value to use
     */
    public SessionDefaultAudience getDefaultAudience() {

        return this.properties.getDefaultAudience();
    }

    /**
     * Gets the login behavior for the session that will be opened. If null is returned, the default (
     * {@link SessionLoginBehavior SessionLoginBehavior.SSO_WITH_FALLBACK} will be used.
     *
     * @return loginBehavior The {@link SessionLoginBehavior SessionLoginBehavior} that specifies what behaviors should
     *         be attempted during authorization.
     */
    public SessionLoginBehavior getLoginBehavior() {

        return this.properties.getLoginBehavior();
    }

    /**
     * Returns the current OnErrorListener for this instance of LoginButton.
     *
     * @return The OnErrorListener
     */
    public OnErrorListener getOnErrorListener() {

        return this.properties.getOnErrorListener();
    }

    /**
     * Sets the callback interface that will be called whenever the status of the Session associated with this
     * LoginButton changes.
     *
     * @return the callback interface
     */
    public Session.StatusCallback getSessionStatusCallback() {

        return this.properties.getSessionStatusCallback();
    }

    /**
     * Gets the current amount of time (in ms) that the tool tip will be displayed to the user
     *
     * @return
     */
    public long getToolTipDisplayTime() {

        return this.nuxDisplayTime;
    }

    /**
     * Return the current {@link ToolTipMode} for this LoginButton
     *
     * @return The {@link ToolTipMode}
     */
    public ToolTipMode getToolTipMode() {

        return this.nuxMode;
    }

    /**
     * Gets the callback interface that will be called when the current user changes.
     *
     * @return the callback interface
     */
    public UserInfoChangedCallback getUserInfoChangedCallback() {

        return this.userInfoChangedCallback;
    }

    /**
     * Provides an implementation for {@link Activity#onActivityResult onActivityResult} that updates the Session based
     * on information returned during the authorization flow. The Activity containing this view should forward the
     * resulting onActivityResult call here to update the Session state based on the contents of the resultCode and
     * data.
     *
     * @param requestCode The requestCode parameter from the forwarded call. When this onActivityResult occurs as part
     *            of Facebook authorization flow, this value is the activityCode passed to open or authorize.
     * @param resultCode An int containing the resultCode parameter from the forwarded call.
     * @param data The Intent passed as the data parameter from the forwarded call.
     * @return A boolean indicating whether the requestCode matched a pending authorization request for this Session.
     * @see Session#onActivityResult(Activity, int, int, Intent)
     */
    public boolean onActivityResult(final int requestCode, final int resultCode, final Intent data) {

        final Session session = this.sessionTracker.getSession();
        if (session != null) {
            return session.onActivityResult((Activity) this.getContext(), requestCode, resultCode, data);
        }
        return false;
    }

    @Override
    public void onFinishInflate() {

        super.onFinishInflate();
        this.finishInit();
    }

    /**
     * Set the application ID to be used to open the session.
     *
     * @param applicationId the application ID to use
     */
    public void setApplicationId(final String applicationId) {

        this.applicationId = applicationId;
    }

    /**
     * Sets the default audience to use when the session is opened. This value is only useful when specifying write
     * permissions for the native login dialog.
     *
     * @param defaultAudience the default audience value to use
     */
    public void setDefaultAudience(final SessionDefaultAudience defaultAudience) {

        this.properties.setDefaultAudience(defaultAudience);
    }

    /**
     * Sets the fragment that contains this control. This allows the LoginButton to be embedded inside a Fragment, and
     * will allow the fragment to receive the {@link Fragment#onActivityResult(int, int, android.content.Intent)
     * onActivityResult} call rather than the Activity.
     *
     * @param fragment the fragment that contains this control
     */
    public void setFragment(final Fragment fragment) {

        this.parentFragment = fragment;
    }

    /**
     * Sets the login behavior for the session that will be opened. If null is specified, the default (
     * {@link SessionLoginBehavior SessionLoginBehavior.SSO_WITH_FALLBACK} will be used.
     *
     * @param loginBehavior The {@link SessionLoginBehavior SessionLoginBehavior} that specifies what behaviors should
     *            be attempted during authorization.
     */
    public void setLoginBehavior(final SessionLoginBehavior loginBehavior) {

        this.properties.setLoginBehavior(loginBehavior);
    }

    /**
     * Allow a developer to set the OnClickListener for the button. This will be called back after we do any handling
     * internally for login
     *
     * @param clickListener
     */
    @Override
    public void setOnClickListener(final OnClickListener clickListener) {

        this.listenerCallback = clickListener;
    }

    /**
     * Sets an OnErrorListener for this instance of LoginButton to call into when certain exceptions occur.
     *
     * @param onErrorListener The listener object to set
     */
    public void setOnErrorListener(final OnErrorListener onErrorListener) {

        this.properties.setOnErrorListener(onErrorListener);
    }

    /**
     * Set the permissions to use when the session is opened. The permissions here should only be publish permissions.
     * If any read permissions are included, the login attempt by the user may fail. The LoginButton can only be
     * associated with either read permissions or publish permissions, but not both. Calling both setReadPermissions and
     * setPublishPermissions on the same instance of LoginButton will result in an exception being thrown unless
     * clearPermissions is called in between.
     * <p/>
     * This method is only meaningful if called before the session is open. If this is called after the session is
     * opened, and the list of permissions passed in is not a subset of the permissions granted during the
     * authorization, it will log an error.
     * <p/>
     * Since the session can be automatically opened when the LoginButton is constructed, it's important to always pass
     * in a consistent set of permissions to this method, or manage the setting of permissions outside of the
     * LoginButton class altogether (by managing the session explicitly).
     *
     * @param permissions the read permissions to use
     * @throws UnsupportedOperationException if setReadPermissions has been called
     * @throws IllegalArgumentException if permissions is null or empty
     */
    public void setPublishPermissions(final List<String> permissions) {

        this.properties.setPublishPermissions(permissions, this.sessionTracker.getSession());
    }

    /**
     * Set the permissions to use when the session is opened. The permissions here should only be publish permissions.
     * If any read permissions are included, the login attempt by the user may fail. The LoginButton can only be
     * associated with either read permissions or publish permissions, but not both. Calling both setReadPermissions and
     * setPublishPermissions on the same instance of LoginButton will result in an exception being thrown unless
     * clearPermissions is called in between.
     * <p/>
     * This method is only meaningful if called before the session is open. If this is called after the session is
     * opened, and the list of permissions passed in is not a subset of the permissions granted during the
     * authorization, it will log an error.
     * <p/>
     * Since the session can be automatically opened when the LoginButton is constructed, it's important to always pass
     * in a consistent set of permissions to this method, or manage the setting of permissions outside of the
     * LoginButton class altogether (by managing the session explicitly).
     *
     * @param permissions the read permissions to use
     * @throws UnsupportedOperationException if setReadPermissions has been called
     * @throws IllegalArgumentException if permissions is null or empty
     */
    public void setPublishPermissions(final String... permissions) {

        this.properties.setPublishPermissions(Arrays.asList(permissions), this.sessionTracker.getSession());
    }

    /**
     * Set the permissions to use when the session is opened. The permissions here can only be read permissions. If any
     * publish permissions are included, the login attempt by the user will fail. The LoginButton can only be associated
     * with either read permissions or publish permissions, but not both. Calling both setReadPermissions and
     * setPublishPermissions on the same instance of LoginButton will result in an exception being thrown unless
     * clearPermissions is called in between.
     * <p/>
     * This method is only meaningful if called before the session is open. If this is called after the session is
     * opened, and the list of permissions passed in is not a subset of the permissions granted during the
     * authorization, it will log an error.
     * <p/>
     * Since the session can be automatically opened when the LoginButton is constructed, it's important to always pass
     * in a consistent set of permissions to this method, or manage the setting of permissions outside of the
     * LoginButton class altogether (by managing the session explicitly).
     *
     * @param permissions the read permissions to use
     * @throws UnsupportedOperationException if setPublishPermissions has been called
     */
    public void setReadPermissions(final List<String> permissions) {

        this.properties.setReadPermissions(permissions, this.sessionTracker.getSession());
    }

    /**
     * Set the permissions to use when the session is opened. The permissions here can only be read permissions. If any
     * publish permissions are included, the login attempt by the user will fail. The LoginButton can only be associated
     * with either read permissions or publish permissions, but not both. Calling both setReadPermissions and
     * setPublishPermissions on the same instance of LoginButton will result in an exception being thrown unless
     * clearPermissions is called in between.
     * <p/>
     * This method is only meaningful if called before the session is open. If this is called after the session is
     * opened, and the list of permissions passed in is not a subset of the permissions granted during the
     * authorization, it will log an error.
     * <p/>
     * Since the session can be automatically opened when the LoginButton is constructed, it's important to always pass
     * in a consistent set of permissions to this method, or manage the setting of permissions outside of the
     * LoginButton class altogether (by managing the session explicitly).
     *
     * @param permissions the read permissions to use
     * @throws UnsupportedOperationException if setPublishPermissions has been called
     */
    public void setReadPermissions(final String... permissions) {

        this.properties.setReadPermissions(Arrays.asList(permissions), this.sessionTracker.getSession());
    }

    /**
     * Set the Session object to use instead of the active Session. Since a Session cannot be reused, if the user logs
     * out from this Session, and tries to log in again, a new Active Session will be used instead.
     * <p/>
     * If the passed in session is currently opened, this method will also attempt to load some user information for
     * display (if needed).
     *
     * @param newSession the Session object to use
     * @throws FacebookException if errors occur during the loading of user information
     */
    public void setSession(final Session newSession) {

        this.sessionTracker.setSession(newSession);
        this.fetchUserInfo();
        this.setButtonText();
    }

    /**
     * Sets the callback interface that will be called whenever the status of the Session associated with this
     * LoginButton changes. Note that updates will only be sent to the callback while the LoginButton is actually
     * attached to a window.
     *
     * @param callback the callback interface
     */
    public void setSessionStatusCallback(final Session.StatusCallback callback) {

        this.properties.setSessionStatusCallback(callback);
    }

    /**
     * Sets the amount of time (in milliseconds) that the tool tip will be shown to the user. The default is
     * {@value ToolTipPopup#DEFAULT_POPUP_DISPLAY_TIME}. Any value that is less than or equal to zero will cause the
     * tool tip to be displayed indefinitely.
     *
     * @param displayTime The amount of time (in milliseconds) that the tool tip will be displayed to the user
     */
    public void setToolTipDisplayTime(final long displayTime) {

        this.nuxDisplayTime = displayTime;
    }

    /**
     * Sets the mode of the Tool Tip popup. Currently supported modes are default (normal behavior), always_on (popup
     * remains up until forcibly dismissed), and always_off (popup doesn't show)
     *
     * @param nuxMode The new mode for the tool tip
     */
    public void setToolTipMode(final ToolTipMode nuxMode) {

        this.nuxMode = nuxMode;
    }

    /**
     * Sets the style (background) of the Tool Tip popup. Currently a blue style and a black style are supported. Blue
     * is default
     *
     * @param nuxStyle The style of the tool tip popup.
     */
    public void setToolTipStyle(final ToolTipPopup.Style nuxStyle) {

        this.nuxStyle = nuxStyle;
    }

    /**
     * Sets the callback interface that will be called when the current user changes.
     *
     * @param userInfoChangedCallback the callback interface
     */
    public void setUserInfoChangedCallback(final UserInfoChangedCallback userInfoChangedCallback) {

        this.userInfoChangedCallback = userInfoChangedCallback;
    }

    private void checkNuxSettings() {

        if (this.nuxMode == ToolTipMode.DISPLAY_ALWAYS) {
            final String nuxString = this.getResources().getString(R.string.com_facebook_tooltip_default);
            this.displayNux(nuxString);
        } else {
            // kick off an async request
            final String appId = Utility.getMetadataApplicationId(this.getContext());
            final AsyncTask<Void, Void, FetchedAppSettings> task = new AsyncTask<Void, Void, Utility.FetchedAppSettings>() {

                @Override
                protected FetchedAppSettings doInBackground(final Void... params) {

                    final FetchedAppSettings settings = Utility.queryAppSettings(appId, false);
                    return settings;
                }

                @Override
                protected void onPostExecute(final FetchedAppSettings result) {

                    LoginButton.this.showNuxPerSettings(result);
                }
            };
            task.execute((Void[]) null);
        }

    }

    private void displayNux(final String nuxString) {

        this.nuxPopup = new ToolTipPopup(nuxString, this);
        this.nuxPopup.setStyle(this.nuxStyle);
        this.nuxPopup.setNuxDisplayTime(this.nuxDisplayTime);
        this.nuxPopup.show();
    }

    private void finishInit() {

        super.setOnClickListener(new LoginClickListener());
        this.setButtonText();
        if (!this.isInEditMode()) {
            this.sessionTracker = new SessionTracker(this.getContext(), new LoginButtonCallback(), null, false);
            this.fetchUserInfo();
        }
    }

    private boolean initializeActiveSessionWithCachedToken(final Context context) {

        if (context == null) {
            return false;
        }

        final Session session = Session.getActiveSession();
        if (session != null) {
            return session.isOpened();
        }

        final String applicationId = Utility.getMetadataApplicationId(context);
        if (applicationId == null) {
            return false;
        }

        return Session.openActiveSessionFromCache(context) != null;
    }

    private void parseAttributes(final AttributeSet attrs) {

        final TypedArray a = this.getContext().obtainStyledAttributes(attrs, R.styleable.com_facebook_login_view);
        this.confirmLogout = a.getBoolean(R.styleable.com_facebook_login_view_confirm_logout, true);
        this.fetchUserInfo = a.getBoolean(R.styleable.com_facebook_login_view_fetch_user_info, true);
        this.loginText = a.getString(R.styleable.com_facebook_login_view_login_text);
        this.logoutText = a.getString(R.styleable.com_facebook_login_view_logout_text);
        a.recycle();
    }

    @Override
    protected void onAttachedToWindow() {

        super.onAttachedToWindow();
        if ((this.sessionTracker != null) && !this.sessionTracker.isTracking()) {
            this.sessionTracker.startTracking();
            this.fetchUserInfo();
            this.setButtonText();
        }
    }

    @Override
    protected void onDetachedFromWindow() {

        super.onDetachedFromWindow();
        if (this.sessionTracker != null) {
            this.sessionTracker.stopTracking();
        }
        this.dismissToolTip();
    }

    @Override
    protected void onDraw(final Canvas canvas) {

        super.onDraw(canvas);

        if (!this.nuxChecked && (this.nuxMode != ToolTipMode.NEVER_DISPLAY) && !this.isInEditMode()) {
            this.nuxChecked = true;
            this.checkNuxSettings();
        }
    }

    @Override
    protected void onVisibilityChanged(final View changedView, final int visibility) {

        super.onVisibilityChanged(changedView, visibility);
        // If the visibility is not VISIBLE, we want to dismiss the nux if it is there
        if (visibility != VISIBLE) {
            this.dismissToolTip();
        }
    }

    void fetchUserInfo() {

        if (this.fetchUserInfo) {
            final Session currentSession = this.sessionTracker.getOpenSession();
            if (currentSession != null) {
                if (currentSession != this.userInfoSession) {
                    final Request request = Request.newMeRequest(currentSession, new Request.GraphUserCallback() {

                        @Override
                        public void onCompleted(final GraphUser me, final Response response) {

                            if (currentSession == LoginButton.this.sessionTracker.getOpenSession()) {
                                LoginButton.this.user = me;
                                if (LoginButton.this.userInfoChangedCallback != null) {
                                    LoginButton.this.userInfoChangedCallback.onUserInfoFetched(LoginButton.this.user);
                                }
                            }
                            if (response.getError() != null) {
                                LoginButton.this.handleError(response.getError().getException());
                            }
                        }
                    });
                    Request.executeBatchAsync(request);
                    this.userInfoSession = currentSession;
                }
            } else {
                this.user = null;
                if (this.userInfoChangedCallback != null) {
                    this.userInfoChangedCallback.onUserInfoFetched(this.user);
                }
            }
        }
    }

    // For testing purposes only
    List<String> getPermissions() {

        return this.properties.getPermissions();
    }

    void handleError(final Exception exception) {

        if (this.properties.onErrorListener != null) {
            if (exception instanceof FacebookException) {
                this.properties.onErrorListener.onError((FacebookException) exception);
            } else {
                this.properties.onErrorListener.onError(new FacebookException(exception));
            }
        }
    }

    void setButtonText() {

        if ((this.sessionTracker != null) && (this.sessionTracker.getOpenSession() != null)) {
            this.setText((this.logoutText != null) ? this.logoutText : this.getResources().getString(
                    R.string.com_facebook_loginview_log_out_button));
        } else {
            this.setText((this.loginText != null) ? this.loginText : this.getResources().getString(
                    R.string.com_facebook_loginview_log_in_button));
        }
    }

    void setLoginLogoutEventName(final String eventName) {

        this.loginLogoutEventName = eventName;
    }

    void setProperties(final LoginButtonProperties properties) {

        this.properties = properties;
    }

    void showNuxPerSettings(final FetchedAppSettings settings) {

        if ((settings != null) && settings.getNuxEnabled() && (this.getVisibility() == View.VISIBLE)) {
            final String nuxString = settings.getNuxContent();
            this.displayNux(nuxString);
        }
    }

    /**
     * Callback interface that will be called when a network or other error is encountered while logging in.
     */
    public interface OnErrorListener {

        /**
         * Called when a network or other error is encountered.
         *
         * @param error a FacebookException representing the error that was encountered.
         */
        void onError(FacebookException error);
    }

    public static enum ToolTipMode {
        /**
         * Default display mode. A server query will determine if the tool tip should be displayed and, if so, what the
         * string shown to the user should be.
         */
        DEFAULT,

        /**
         * Display the tool tip with a local string--regardless of what the server returns
         */
        DISPLAY_ALWAYS,

        /**
         * Never display the tool tip--regardless of what the server says
         */
        NEVER_DISPLAY
    }

    /**
     * Specifies a callback interface that will be called when the button's notion of the current user changes (if the
     * fetch_user_info attribute is true for this control).
     */
    public interface UserInfoChangedCallback {

        /**
         * Called when the current user changes.
         *
         * @param user the current user, or null if there is no user
         */
        void onUserInfoFetched(GraphUser user);
    }

    private class LoginButtonCallback implements Session.StatusCallback {

        @Override
        public void call(final Session session, final SessionState state, final Exception exception) {

            LoginButton.this.fetchUserInfo();
            LoginButton.this.setButtonText();

            // if the client has a status callback registered, call it, otherwise
            // call the default handleError method, but don't call both
            if (LoginButton.this.properties.sessionStatusCallback != null) {
                LoginButton.this.properties.sessionStatusCallback.call(session, state, exception);
            } else if (exception != null) {
                LoginButton.this.handleError(exception);
            }
        }
    }

    private class LoginClickListener implements OnClickListener {

        @Override
        public void onClick(final View v) {

            final Context context = LoginButton.this.getContext();
            final Session openSession = LoginButton.this.sessionTracker.getOpenSession();

            if (openSession != null) {
                // If the Session is currently open, it must mean we need to log out
                if (LoginButton.this.confirmLogout) {
                    // Create a confirmation dialog
                    final String logout = LoginButton.this.getResources().getString(
                            R.string.com_facebook_loginview_log_out_action);
                    final String cancel = LoginButton.this.getResources().getString(
                            R.string.com_facebook_loginview_cancel_action);
                    String message;
                    if ((LoginButton.this.user != null) && (LoginButton.this.user.getName() != null)) {
                        message = String
                                .format(LoginButton.this.getResources().getString(
                                        R.string.com_facebook_loginview_logged_in_as), LoginButton.this.user.getName());
                    } else {
                        message = LoginButton.this.getResources().getString(
                                R.string.com_facebook_loginview_logged_in_using_facebook);
                    }
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(message).setCancelable(true)
                            .setPositiveButton(logout, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(final DialogInterface dialog, final int which) {

                                    openSession.closeAndClearTokenInformation();
                                }
                            }).setNegativeButton(cancel, null);
                    builder.create().show();
                } else {
                    openSession.closeAndClearTokenInformation();
                }
            } else {
                Session currentSession = LoginButton.this.sessionTracker.getSession();
                if ((currentSession == null) || currentSession.getState().isClosed()) {
                    LoginButton.this.sessionTracker.setSession(null);
                    final Session session = new Session.Builder(context).setApplicationId(
                            LoginButton.this.applicationId).build();
                    Session.setActiveSession(session);
                    currentSession = session;
                }
                if (!currentSession.isOpened()) {
                    Session.OpenRequest openRequest = null;
                    if (LoginButton.this.parentFragment != null) {
                        openRequest = new Session.OpenRequest(LoginButton.this.parentFragment);
                    } else if (context instanceof Activity) {
                        openRequest = new Session.OpenRequest((Activity) context);
                    }

                    if (openRequest != null) {
                        openRequest.setDefaultAudience(LoginButton.this.properties.defaultAudience);
                        openRequest.setPermissions(LoginButton.this.properties.permissions);
                        openRequest.setLoginBehavior(LoginButton.this.properties.loginBehavior);

                        if (SessionAuthorizationType.PUBLISH.equals(LoginButton.this.properties.authorizationType)) {
                            currentSession.openForPublish(openRequest);
                        } else {
                            currentSession.openForRead(openRequest);
                        }
                    }
                }
            }

            final AppEventsLogger logger = AppEventsLogger.newLogger(LoginButton.this.getContext());

            final Bundle parameters = new Bundle();
            parameters.putInt("logging_in", (openSession != null) ? 0 : 1);

            logger.logSdkEvent(LoginButton.this.loginLogoutEventName, null, parameters);

            if (LoginButton.this.listenerCallback != null) {
                LoginButton.this.listenerCallback.onClick(v);
            }
        }
    }

    static class LoginButtonProperties {

        protected SessionDefaultAudience defaultAudience = SessionDefaultAudience.FRIENDS;
        protected List<String> permissions = Collections.<String> emptyList();
        protected SessionAuthorizationType authorizationType;
        protected OnErrorListener onErrorListener;
        protected SessionLoginBehavior loginBehavior = SessionLoginBehavior.SSO_WITH_FALLBACK;
        protected Session.StatusCallback sessionStatusCallback;

        public void clearPermissions() {

            this.permissions = null;
            this.authorizationType = null;
        }

        public SessionDefaultAudience getDefaultAudience() {

            return this.defaultAudience;
        }

        public SessionLoginBehavior getLoginBehavior() {

            return this.loginBehavior;
        }

        public OnErrorListener getOnErrorListener() {

            return this.onErrorListener;
        }

        public Session.StatusCallback getSessionStatusCallback() {

            return this.sessionStatusCallback;
        }

        public void setDefaultAudience(final SessionDefaultAudience defaultAudience) {

            this.defaultAudience = defaultAudience;
        }

        public void setLoginBehavior(final SessionLoginBehavior loginBehavior) {

            this.loginBehavior = loginBehavior;
        }

        public void setOnErrorListener(final OnErrorListener onErrorListener) {

            this.onErrorListener = onErrorListener;
        }

        public void setPublishPermissions(final List<String> permissions, final Session session) {

            if (SessionAuthorizationType.READ.equals(this.authorizationType)) {
                throw new UnsupportedOperationException(
                        "Cannot call setPublishPermissions after setReadPermissions has been called.");
            }
            if (this.validatePermissions(permissions, SessionAuthorizationType.PUBLISH, session)) {
                this.permissions = permissions;
                this.authorizationType = SessionAuthorizationType.PUBLISH;
            }
        }

        public void setReadPermissions(final List<String> permissions, final Session session) {

            if (SessionAuthorizationType.PUBLISH.equals(this.authorizationType)) {
                throw new UnsupportedOperationException(
                        "Cannot call setReadPermissions after setPublishPermissions has been called.");
            }
            if (this.validatePermissions(permissions, SessionAuthorizationType.READ, session)) {
                this.permissions = permissions;
                this.authorizationType = SessionAuthorizationType.READ;
            }
        }

        public void setSessionStatusCallback(final Session.StatusCallback callback) {

            this.sessionStatusCallback = callback;
        }

        private boolean validatePermissions(final List<String> permissions, final SessionAuthorizationType authType,
                final Session currentSession) {

            if (SessionAuthorizationType.PUBLISH.equals(authType)) {
                if (Utility.isNullOrEmpty(permissions)) {
                    throw new IllegalArgumentException("Permissions for publish actions cannot be null or empty.");
                }
            }
            if ((currentSession != null) && currentSession.isOpened()) {
                if (!Utility.isSubset(permissions, currentSession.getPermissions())) {
                    Log.e(TAG, "Cannot set additional permissions when session is already open.");
                    return false;
                }
            }
            return true;
        }

        List<String> getPermissions() {

            return this.permissions;
        }
    }
}
