/**
 * Copyright 2010-present Facebook. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package com.wootag.facebook.widget;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wootag.R;
import com.wootag.facebook.Request;
import com.wootag.facebook.Response;
import com.wootag.facebook.Session;
import com.wootag.facebook.SessionDefaultAudience;
import com.wootag.facebook.SessionLoginBehavior;
import com.wootag.facebook.SessionState;
import com.wootag.facebook.internal.AnalyticsEvents;
import com.wootag.facebook.internal.ImageDownloader;
import com.wootag.facebook.internal.ImageRequest;
import com.wootag.facebook.internal.ImageResponse;
import com.wootag.facebook.model.GraphUser;

/**
 * A Fragment that displays a Login/Logout button as well as the user's profile picture and name when logged in.
 * <p/>
 * This Fragment will create and use the active session upon construction if it has the available data (if the app ID is
 * specified in the manifest). It will also open the active session if it does not require user interaction (i.e. if the
 * session is in the {@link com.wootag.facebook.SessionState#CREATED_TOKEN_LOADED} state. Developers can override the
 * use of the active session by calling the {@link #setSession(com.wootag.facebook.Session)} method.
 */
public class UserSettingsFragment extends FacebookFragment {

    private static final String NAME = "name";
    private static final String ID = "id";
    private static final String PICTURE = "picture";
    private static final String FIELDS = "fields";

    private static final String REQUEST_FIELDS = TextUtils.join(",", new String[] { ID, NAME, PICTURE });

    protected LoginButton loginButton;
    private final LoginButton.LoginButtonProperties loginButtonProperties = new LoginButton.LoginButtonProperties();
    private TextView connectedStateLabel;
    protected GraphUser user;
    private Session userInfoSession; // the Session used to fetch the current user info
    private Drawable userProfilePic;
    private String userProfilePicID;
    private Session.StatusCallback sessionStatusCallback;

    /**
     * Clears the permissions currently associated with this LoginButton.
     */
    public void clearPermissions() {

        this.loginButtonProperties.clearPermissions();
    }

    /**
     * Gets the default audience to use when the session is opened. This value is only useful when specifying write
     * permissions for the native login dialog.
     *
     * @return the default audience value to use
     */
    public SessionDefaultAudience getDefaultAudience() {

        return this.loginButtonProperties.getDefaultAudience();
    }

    /**
     * Gets the login behavior for the session that will be opened. If null is returned, the default (
     * {@link SessionLoginBehavior SessionLoginBehavior.SSO_WITH_FALLBACK} will be used.
     *
     * @return loginBehavior The {@link SessionLoginBehavior SessionLoginBehavior} that specifies what behaviors should
     *         be attempted during authorization.
     */
    public SessionLoginBehavior getLoginBehavior() {

        return this.loginButtonProperties.getLoginBehavior();
    }

    /**
     * Returns the current OnErrorListener for this instance of UserSettingsFragment.
     *
     * @return The OnErrorListener
     */
    public LoginButton.OnErrorListener getOnErrorListener() {

        return this.loginButtonProperties.getOnErrorListener();
    }

    /**
     * Sets the callback interface that will be called whenever the status of the Session associated with this
     * LoginButton changes.
     *
     * @return the callback interface
     */
    public Session.StatusCallback getSessionStatusCallback() {

        return this.sessionStatusCallback;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setRetainInstance(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.com_facebook_usersettingsfragment, container, false);
        this.loginButton = (LoginButton) view.findViewById(R.id.com_facebook_usersettingsfragment_login_button);
        this.loginButton.setProperties(this.loginButtonProperties);
        this.loginButton.setFragment(this);
        this.loginButton.setLoginLogoutEventName(AnalyticsEvents.EVENT_USER_SETTINGS_USAGE);

        final Session session = this.getSession();
        if ((session != null) && !session.equals(Session.getActiveSession())) {
            this.loginButton.setSession(session);
        }
        this.connectedStateLabel = (TextView) view.findViewById(R.id.com_facebook_usersettingsfragment_profile_name);

        // if no background is set for some reason, then default to Facebook blue
        if (view.getBackground() == null) {
            view.setBackgroundColor(this.getResources().getColor(R.color.com_facebook_blue));
        } else {
            view.getBackground().setDither(true);
        }
        return view;
    }

    /**
     * @throws com.wootag.facebook.FacebookException if errors occur during the loading of user information
     */
    @Override
    public void onResume() {

        super.onResume();
        this.fetchUserInfo();
        this.updateUi();
    }

    /**
     * Sets the default audience to use when the session is opened. This value is only useful when specifying write
     * permissions for the native login dialog.
     *
     * @param defaultAudience the default audience value to use
     */
    public void setDefaultAudience(final SessionDefaultAudience defaultAudience) {

        this.loginButtonProperties.setDefaultAudience(defaultAudience);
    }

    /**
     * Sets the login behavior for the session that will be opened. If null is specified, the default (
     * {@link SessionLoginBehavior SessionLoginBehavior.SSO_WITH_FALLBACK} will be used.
     *
     * @param loginBehavior The {@link SessionLoginBehavior SessionLoginBehavior} that specifies what behaviors should
     *            be attempted during authorization.
     */
    public void setLoginBehavior(final SessionLoginBehavior loginBehavior) {

        this.loginButtonProperties.setLoginBehavior(loginBehavior);
    }

    /**
     * Sets an OnErrorListener for this instance of UserSettingsFragment to call into when certain exceptions occur.
     *
     * @param onErrorListener The listener object to set
     */
    public void setOnErrorListener(final LoginButton.OnErrorListener onErrorListener) {

        this.loginButtonProperties.setOnErrorListener(onErrorListener);
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

        this.loginButtonProperties.setPublishPermissions(permissions, this.getSession());
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

        this.loginButtonProperties.setPublishPermissions(Arrays.asList(permissions), this.getSession());
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
     * Since the session can be automatically opened when the UserSettingsFragment is constructed, it's important to
     * always pass in a consistent set of permissions to this method, or manage the setting of permissions outside of
     * the LoginButton class altogether (by managing the session explicitly).
     *
     * @param permissions the read permissions to use
     * @throws UnsupportedOperationException if setPublishPermissions has been called
     */
    public void setReadPermissions(final List<String> permissions) {

        this.loginButtonProperties.setReadPermissions(permissions, this.getSession());
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
     * Since the session can be automatically opened when the UserSettingsFragment is constructed, it's important to
     * always pass in a consistent set of permissions to this method, or manage the setting of permissions outside of
     * the LoginButton class altogether (by managing the session explicitly).
     *
     * @param permissions the read permissions to use
     * @throws UnsupportedOperationException if setPublishPermissions has been called
     */
    public void setReadPermissions(final String... permissions) {

        this.loginButtonProperties.setReadPermissions(Arrays.asList(permissions), this.getSession());
    }

    /**
     * Set the Session object to use instead of the active Session. Since a Session cannot be reused, if the user logs
     * out from this Session, and tries to log in again, a new Active Session will be used instead.
     * <p/>
     * If the passed in session is currently opened, this method will also attempt to load some user information for
     * display (if needed).
     *
     * @param newSession the Session object to use
     * @throws com.wootag.facebook.FacebookException if errors occur during the loading of user information
     */
    @Override
    public void setSession(final Session newSession) {

        super.setSession(newSession);
        if (this.loginButton != null) {
            this.loginButton.setSession(newSession);
        }
        this.fetchUserInfo();
        this.updateUi();
    }

    /**
     * Sets the callback interface that will be called whenever the status of the Session associated with this
     * LoginButton changes.
     *
     * @param callback the callback interface
     */
    public void setSessionStatusCallback(final Session.StatusCallback callback) {

        this.sessionStatusCallback = callback;
    }

    private void fetchUserInfo() {

        final Session currentSession = this.getSession();
        if ((currentSession != null) && currentSession.isOpened()) {
            if (currentSession != this.userInfoSession) {
                final Request request = Request.newMeRequest(currentSession, new Request.GraphUserCallback() {

                    @Override
                    public void onCompleted(final GraphUser me, final Response response) {

                        if (currentSession == UserSettingsFragment.this.getSession()) {
                            UserSettingsFragment.this.user = me;
                            UserSettingsFragment.this.updateUi();
                        }
                        if (response.getError() != null) {
                            UserSettingsFragment.this.loginButton.handleError(response.getError().getException());
                        }
                    }
                });
                final Bundle parameters = new Bundle();
                parameters.putString(FIELDS, REQUEST_FIELDS);
                request.setParameters(parameters);
                Request.executeBatchAsync(request);
                this.userInfoSession = currentSession;
            }
        } else {
            this.user = null;
        }
    }

    private ImageRequest getImageRequest() {

        ImageRequest request = null;
        try {
            final ImageRequest.Builder requestBuilder = new ImageRequest.Builder(this.getActivity(),
                    ImageRequest.getProfilePictureUrl(
                            this.user.getId(),
                            this.getResources().getDimensionPixelSize(
                                    R.dimen.com_facebook_usersettingsfragment_profile_picture_width),
                            this.getResources().getDimensionPixelSize(
                                    R.dimen.com_facebook_usersettingsfragment_profile_picture_height)));

            request = requestBuilder.setCallerTag(this).setCallback(new ImageRequest.Callback() {

                @Override
                public void onCompleted(final ImageResponse response) {

                    UserSettingsFragment.this.processImageResponse(UserSettingsFragment.this.user.getId(), response);
                }
            }).build();
        } catch (final URISyntaxException e) {
        }
        return request;
    }

    @Override
    protected void onSessionStateChange(final SessionState state, final Exception exception) {

        this.fetchUserInfo();
        this.updateUi();

        if (this.sessionStatusCallback != null) {
            this.sessionStatusCallback.call(this.getSession(), state, exception);
        }
    }

    // For Testing Only
    List<String> getPermissions() {

        return this.loginButtonProperties.getPermissions();
    }

    void processImageResponse(final String id, final ImageResponse response) {

        if (response != null) {
            final Bitmap bitmap = response.getBitmap();
            if (bitmap != null) {
                final BitmapDrawable drawable = new BitmapDrawable(UserSettingsFragment.this.getResources(), bitmap);
                drawable.setBounds(
                        0,
                        0,
                        this.getResources().getDimensionPixelSize(
                                R.dimen.com_facebook_usersettingsfragment_profile_picture_width),
                        this.getResources().getDimensionPixelSize(
                                R.dimen.com_facebook_usersettingsfragment_profile_picture_height));
                this.userProfilePic = drawable;
                this.userProfilePicID = id;
                this.connectedStateLabel.setCompoundDrawables(null, drawable, null, null);
                this.connectedStateLabel.setTag(response.getRequest().getImageUri());
            }
        }
    }

    void updateUi() {

        if (!this.isAdded()) {
            return;
        }
        if (this.isSessionOpen()) {
            this.connectedStateLabel.setTextColor(this.getResources().getColor(
                    R.color.com_facebook_usersettingsfragment_connected_text_color));
            this.connectedStateLabel.setShadowLayer(1f, 0f, -1f,
                    this.getResources().getColor(R.color.com_facebook_usersettingsfragment_connected_shadow_color));

            if (this.user != null) {
                final ImageRequest request = this.getImageRequest();
                if (request != null) {
                    final URI requestUrl = request.getImageUri();
                    // Do we already have the right picture? If so, leave it alone.
                    if (!requestUrl.equals(this.connectedStateLabel.getTag())) {
                        if (this.user.getId().equals(this.userProfilePicID)) {
                            this.connectedStateLabel.setCompoundDrawables(null, this.userProfilePic, null, null);
                            this.connectedStateLabel.setTag(requestUrl);
                        } else {
                            ImageDownloader.downloadAsync(request);
                        }
                    }
                }
                this.connectedStateLabel.setText(this.user.getName());
            } else {
                this.connectedStateLabel.setText(this.getResources().getString(
                        R.string.com_facebook_usersettingsfragment_logged_in));
                final Drawable noProfilePic = this.getResources().getDrawable(
                        R.drawable.com_facebook_profile_default_icon);
                noProfilePic.setBounds(
                        0,
                        0,
                        this.getResources().getDimensionPixelSize(
                                R.dimen.com_facebook_usersettingsfragment_profile_picture_width),
                        this.getResources().getDimensionPixelSize(
                                R.dimen.com_facebook_usersettingsfragment_profile_picture_height));
                this.connectedStateLabel.setCompoundDrawables(null, noProfilePic, null, null);
            }
        } else {
            final int textColor = this.getResources().getColor(
                    R.color.com_facebook_usersettingsfragment_not_connected_text_color);
            this.connectedStateLabel.setTextColor(textColor);
            this.connectedStateLabel.setShadowLayer(0f, 0f, 0f, textColor);
            this.connectedStateLabel.setText(this.getResources().getString(
                    R.string.com_facebook_usersettingsfragment_not_logged_in));
            this.connectedStateLabel.setCompoundDrawables(null, null, null, null);
            this.connectedStateLabel.setTag(null);
        }
    }
}
