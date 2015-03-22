/**
 * Copyright 2010-present Facebook. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package com.TagFu.facebook.widget;

import java.util.Date;
import java.util.List;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

import com.TagFu.facebook.Session;
import com.TagFu.facebook.SessionLoginBehavior;
import com.TagFu.facebook.SessionState;
import com.TagFu.facebook.internal.SessionAuthorizationType;
import com.TagFu.facebook.internal.SessionTracker;

/**
 * <p>
 * Basic implementation of a Fragment that uses a Session to perform Single Sign On (SSO). This class is package
 * private, and is not intended to be consumed by external applications.
 * </p>
 * <p>
 * The method {@link android.support.v4.app.Fragment#onActivityResult} is used to manage the session information, so if
 * you override it in a subclass, be sure to call {@code super.onActivityResult}.
 * </p>
 * <p>
 * The methods in this class are not thread-safe.
 * </p>
 */
class FacebookFragment extends Fragment {

    private SessionTracker sessionTracker;

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        this.sessionTracker = new SessionTracker(this.getActivity(), new DefaultSessionStatusCallback());
    }

    /**
     * Called when the activity that was launched exits. This method manages session information when a session is
     * opened. If this method is overridden in subclasses, be sure to call {@code super.onActivityResult(...)} first.
     */
    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        this.sessionTracker.getSession().onActivityResult(this.getActivity(), requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        this.sessionTracker.stopTracking();
    }

    /**
     * Use the supplied Session object instead of the active Session.
     *
     * @param newSession the Session object to use
     */
    public void setSession(final Session newSession) {

        if (this.sessionTracker != null) {
            this.sessionTracker.setSession(newSession);
        }
    }

    // METHOD TO BE OVERRIDDEN

    private void openSession(final String applicationId, final List<String> permissions,
            final SessionLoginBehavior behavior, final int activityCode, final SessionAuthorizationType authType) {

        if (this.sessionTracker != null) {
            Session currentSession = this.sessionTracker.getSession();
            if ((currentSession == null) || currentSession.getState().isClosed()) {
                final Session session = new Session.Builder(this.getActivity()).setApplicationId(applicationId).build();
                Session.setActiveSession(session);
                currentSession = session;
            }
            if (!currentSession.isOpened()) {
                final Session.OpenRequest openRequest = new Session.OpenRequest(this).setPermissions(permissions)
                        .setLoginBehavior(behavior).setRequestCode(activityCode);
                if (SessionAuthorizationType.PUBLISH.equals(authType)) {
                    currentSession.openForPublish(openRequest);
                } else {
                    currentSession.openForRead(openRequest);
                }
            }
        }
    }

    // ACCESSORS (CANNOT BE OVERRIDDEN)

    /**
     * Closes the current session.
     */
    protected final void closeSession() {

        if (this.sessionTracker != null) {
            final Session currentSession = this.sessionTracker.getOpenSession();
            if (currentSession != null) {
                currentSession.close();
            }
        }
    }

    /**
     * Closes the current session as well as clearing the token cache.
     */
    protected final void closeSessionAndClearTokenInformation() {

        if (this.sessionTracker != null) {
            final Session currentSession = this.sessionTracker.getOpenSession();
            if (currentSession != null) {
                currentSession.closeAndClearTokenInformation();
            }
        }
    }

    /**
     * Gets the access token associated with the current session or null if no session has been created.
     *
     * @return the access token
     */
    protected final String getAccessToken() {

        if (this.sessionTracker != null) {
            final Session currentSession = this.sessionTracker.getOpenSession();
            return (currentSession != null) ? currentSession.getAccessToken() : null;
        }
        return null;
    }

    /**
     * Gets the date at which the current session will expire or null if no session has been created.
     *
     * @return the date at which the current session will expire
     */
    protected final Date getExpirationDate() {

        if (this.sessionTracker != null) {
            final Session currentSession = this.sessionTracker.getOpenSession();
            return (currentSession != null) ? currentSession.getExpirationDate() : null;
        }
        return null;
    }

    /**
     * Gets the current Session.
     *
     * @return the current Session object.
     */
    protected final Session getSession() {

        if (this.sessionTracker != null) {
            return this.sessionTracker.getSession();
        }
        return null;
    }

    /**
     * Gets the permissions associated with the current session or null if no session has been created.
     *
     * @return the permissions associated with the current session
     */
    protected final List<String> getSessionPermissions() {

        if (this.sessionTracker != null) {
            final Session currentSession = this.sessionTracker.getSession();
            return (currentSession != null) ? currentSession.getPermissions() : null;
        }
        return null;
    }

    /**
     * Gets the current state of the session or null if no session has been created.
     *
     * @return the current state of the session
     */
    protected final SessionState getSessionState() {

        if (this.sessionTracker != null) {
            final Session currentSession = this.sessionTracker.getSession();
            return (currentSession != null) ? currentSession.getState() : null;
        }
        return null;
    }

    /**
     * Determines whether the current session is open.
     *
     * @return true if the current session is open
     */
    protected final boolean isSessionOpen() {

        if (this.sessionTracker != null) {
            return this.sessionTracker.getOpenSession() != null;
        }
        return false;
    }

    /**
     * Called when the session state changes. Override this method to take action on session state changes.
     *
     * @param state the new state
     * @param exception any exceptions that occurred during the state change
     */
    protected void onSessionStateChange(final SessionState state, final Exception exception) {

    }

    /**
     * Opens a new session. This method will use the application id from the associated meta-data value and an empty
     * list of permissions.
     */
    protected final void openSession() {

        this.openSessionForRead(null, null);
    }

    /**
     * Opens a new session with publish permissions. If either applicationID is null, this method will default to using
     * the value from the associated meta-data value. The permissions list cannot be null.
     *
     * @param applicationId the applicationID, can be null
     * @param permissions the permissions list, cannot be null
     */
    protected final void openSessionForPublish(final String applicationId, final List<String> permissions) {

        this.openSessionForPublish(applicationId, permissions, SessionLoginBehavior.SSO_WITH_FALLBACK,
                Session.DEFAULT_AUTHORIZE_ACTIVITY_CODE);
    }

    /**
     * Opens a new session with publish permissions. If either applicationID is null, this method will default to using
     * the value from the associated meta-data value. The permissions list cannot be null.
     *
     * @param applicationId the applicationID, can be null
     * @param permissions the permissions list, cannot be null
     * @param behavior the login behavior to use with the session
     * @param activityCode the activity code to use for the SSO activity
     */
    protected final void openSessionForPublish(final String applicationId, final List<String> permissions,
            final SessionLoginBehavior behavior, final int activityCode) {

        this.openSession(applicationId, permissions, behavior, activityCode, SessionAuthorizationType.PUBLISH);
    }

    /**
     * Opens a new session with read permissions. If either applicationID or permissions is null, this method will
     * default to using the values from the associated meta-data value and an empty list respectively.
     *
     * @param applicationId the applicationID, can be null
     * @param permissions the permissions list, can be null
     */
    protected final void openSessionForRead(final String applicationId, final List<String> permissions) {

        this.openSessionForRead(applicationId, permissions, SessionLoginBehavior.SSO_WITH_FALLBACK,
                Session.DEFAULT_AUTHORIZE_ACTIVITY_CODE);
    }

    /**
     * Opens a new session with read permissions. If either applicationID or permissions is null, this method will
     * default to using the values from the associated meta-data value and an empty list respectively.
     *
     * @param applicationId the applicationID, can be null
     * @param permissions the permissions list, can be null
     * @param behavior the login behavior to use with the session
     * @param activityCode the activity code to use for the SSO activity
     */
    protected final void openSessionForRead(final String applicationId, final List<String> permissions,
            final SessionLoginBehavior behavior, final int activityCode) {

        this.openSession(applicationId, permissions, behavior, activityCode, SessionAuthorizationType.READ);
    }

    /**
     * The default callback implementation for the session.
     */
    private class DefaultSessionStatusCallback implements Session.StatusCallback {

        @Override
        public void call(final Session session, final SessionState state, final Exception exception) {

            FacebookFragment.this.onSessionStateChange(state, exception);
        }

    }
}
