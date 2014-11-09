/**
 * Copyright 2010-present Facebook. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package com.wootag.facebook;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.CookieSyncManager;

import org.json.JSONException;
import org.json.JSONObject;

import com.wootag.R;
import com.wootag.facebook.internal.AnalyticsEvents;
import com.wootag.facebook.internal.NativeProtocol;
import com.wootag.facebook.internal.ServerProtocol;
import com.wootag.facebook.internal.Utility;
import com.wootag.facebook.model.GraphUser;
import com.wootag.facebook.widget.WebDialog;

class AuthorizationClient implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String TAG = "Facebook-AuthorizationClient";
    private static final String WEB_VIEW_AUTH_HANDLER_STORE = "com.facebook.AuthorizationClient.WebViewAuthHandler.TOKEN_STORE_KEY";
    private static final String WEB_VIEW_AUTH_HANDLER_TOKEN_KEY = "TOKEN";

    // Constants for logging login-related data. Some of these are only used by Session, but grouped here for
    // maintainability.
    private static final String EVENT_NAME_LOGIN_METHOD_START = "fb_mobile_login_method_start";
    private static final String EVENT_NAME_LOGIN_METHOD_COMPLETE = "fb_mobile_login_method_complete";
    private static final String EVENT_PARAM_METHOD_RESULT_SKIPPED = "skipped";
    static final String EVENT_NAME_LOGIN_START = "fb_mobile_login_start";
    static final String EVENT_NAME_LOGIN_COMPLETE = "fb_mobile_login_complete";
    // Note: to ensure stability of column mappings across the four different event types, we prepend a column
    // index to each name, and we log all columns with all events, even if they are empty.
    static final String EVENT_PARAM_AUTH_LOGGER_ID = "0_auth_logger_id";
    static final String EVENT_PARAM_TIMESTAMP = "1_timestamp_ms";
    static final String EVENT_PARAM_LOGIN_RESULT = "2_result";
    static final String EVENT_PARAM_METHOD = "3_method";
    static final String EVENT_PARAM_ERROR_CODE = "4_error_code";
    static final String EVENT_PARAM_ERROR_MESSAGE = "5_error_message";
    static final String EVENT_PARAM_EXTRAS = "6_extras";
    static final String EVENT_EXTRAS_TRY_LOGIN_ACTIVITY = "try_login_activity";
    static final String EVENT_EXTRAS_TRY_LEGACY = "try_legacy";
    static final String EVENT_EXTRAS_LOGIN_BEHAVIOR = "login_behavior";
    static final String EVENT_EXTRAS_REQUEST_CODE = "request_code";
    static final String EVENT_EXTRAS_IS_LEGACY = "is_legacy";
    static final String EVENT_EXTRAS_PERMISSIONS = "permissions";
    static final String EVENT_EXTRAS_DEFAULT_AUDIENCE = "default_audience";
    static final String EVENT_EXTRAS_MISSING_INTERNET_PERMISSION = "no_internet_permission";
    static final String EVENT_EXTRAS_NOT_TRIED = "not_tried";
    static final String EVENT_EXTRAS_NEW_PERMISSIONS = "new_permissions";
    static final String EVENT_EXTRAS_SERVICE_DISABLED = "service_disabled";
    static final String EVENT_EXTRAS_APP_CALL_ID = "call_id";
    static final String EVENT_EXTRAS_PROTOCOL_VERSION = "protocol_version";
    static final String EVENT_EXTRAS_WRITE_PRIVACY = "write_privacy";

    List<AuthHandler> handlersToTry;
    AuthHandler currentHandler;
    transient Context context;
    transient StartActivityDelegate startActivityDelegate;
    transient OnCompletedListener onCompletedListener;
    transient BackgroundProcessingListener backgroundProcessingListener;
    transient boolean checkedInternetPermission;
    AuthorizationRequest pendingRequest;
    Map<String, String> loggingExtras;
    private transient AppEventsLogger appEventsLogger;

    static String getE2E() {

        final JSONObject e2e = new JSONObject();
        try {
            e2e.put("init", System.currentTimeMillis());
        } catch (final JSONException e) {
        }
        return e2e.toString();
    }

    static Bundle newAuthorizationLoggingBundle(final String authLoggerId) {

        // We want to log all parameters for all events, to ensure stability of columns across different event types.
        final Bundle bundle = new Bundle();
        bundle.putLong(EVENT_PARAM_TIMESTAMP, System.currentTimeMillis());
        bundle.putString(EVENT_PARAM_AUTH_LOGGER_ID, authLoggerId);
        bundle.putString(EVENT_PARAM_METHOD, "");
        bundle.putString(EVENT_PARAM_LOGIN_RESULT, "");
        bundle.putString(EVENT_PARAM_ERROR_MESSAGE, "");
        bundle.putString(EVENT_PARAM_ERROR_CODE, "");
        bundle.putString(EVENT_PARAM_EXTRAS, "");
        return bundle;
    }

    private void addLoggingExtra(final String key, String value, final boolean accumulate) {

        if (this.loggingExtras == null) {
            this.loggingExtras = new HashMap<String, String>();
        }
        if (this.loggingExtras.containsKey(key) && accumulate) {
            value = this.loggingExtras.get(key) + "," + value;
        }
        this.loggingExtras.put(key, value);
    }

    private void completeWithFailure() {

        this.complete(Result.createErrorResult(this.pendingRequest, "Login attempt failed.", null));
    }

    private AppEventsLogger getAppEventsLogger() {

        if ((this.appEventsLogger == null)
                || !(this.appEventsLogger.getApplicationId().equals(this.pendingRequest.getApplicationId()))) {
            this.appEventsLogger = AppEventsLogger.newLogger(this.context, this.pendingRequest.getApplicationId());
        }
        return this.appEventsLogger;
    }

    private List<AuthHandler> getHandlerTypes(final AuthorizationRequest request) {

        final ArrayList<AuthHandler> handlers = new ArrayList<AuthHandler>();

        final SessionLoginBehavior behavior = request.getLoginBehavior();
        if (behavior.allowsKatanaAuth()) {
            if (!request.isLegacy()) {
                handlers.add(new GetTokenAuthHandler());
            }
            handlers.add(new KatanaProxyAuthHandler());
        }

        if (behavior.allowsWebViewAuth()) {
            handlers.add(new WebViewAuthHandler());
        }

        return handlers;
    }

    private void logAuthorizationMethodComplete(final String method, final Result result,
            final Map<String, String> loggingExtras) {

        this.logAuthorizationMethodComplete(method, result.code.getLoggingValue(), result.errorMessage,
                result.errorCode, loggingExtras);
    }

    private void logAuthorizationMethodComplete(final String method, final String result, final String errorMessage,
            final String errorCode, final Map<String, String> loggingExtras) {

        Bundle bundle = null;
        if (this.pendingRequest == null) {
            // We don't expect this to happen, but if it does, log an event for diagnostic purposes.
            bundle = newAuthorizationLoggingBundle("");
            bundle.putString(EVENT_PARAM_LOGIN_RESULT, Result.Code.ERROR.getLoggingValue());
            bundle.putString(EVENT_PARAM_ERROR_MESSAGE,
                    "Unexpected call to logAuthorizationMethodComplete with null pendingRequest.");
        } else {
            bundle = newAuthorizationLoggingBundle(this.pendingRequest.getAuthId());
            if (result != null) {
                bundle.putString(EVENT_PARAM_LOGIN_RESULT, result);
            }
            if (errorMessage != null) {
                bundle.putString(EVENT_PARAM_ERROR_MESSAGE, errorMessage);
            }
            if (errorCode != null) {
                bundle.putString(EVENT_PARAM_ERROR_CODE, errorCode);
            }
            if ((loggingExtras != null) && !loggingExtras.isEmpty()) {
                final JSONObject jsonObject = new JSONObject(loggingExtras);
                bundle.putString(EVENT_PARAM_EXTRAS, jsonObject.toString());
            }
        }
        bundle.putString(EVENT_PARAM_METHOD, method);
        bundle.putLong(EVENT_PARAM_TIMESTAMP, System.currentTimeMillis());

        this.getAppEventsLogger().logSdkEvent(EVENT_NAME_LOGIN_METHOD_COMPLETE, null, bundle);
    }

    private void logAuthorizationMethodStart(final String method) {

        final Bundle bundle = newAuthorizationLoggingBundle(this.pendingRequest.getAuthId());
        bundle.putLong(EVENT_PARAM_TIMESTAMP, System.currentTimeMillis());
        bundle.putString(EVENT_PARAM_METHOD, method);

        this.getAppEventsLogger().logSdkEvent(EVENT_NAME_LOGIN_METHOD_START, null, bundle);
    }

    private void notifyOnCompleteListener(final Result outcome) {

        if (this.onCompletedListener != null) {
            this.onCompletedListener.onCompleted(outcome);
        }
    }

    void authorize(final AuthorizationRequest request) {

        if (request == null) {
            return;
        }

        if (this.pendingRequest != null) {
            throw new FacebookException("Attempted to authorize while a request is pending.");
        }

        if (request.needsNewTokenValidation() && !this.checkInternetPermission()) {
            // We're going to need INTERNET permission later and don't have it, so fail early.
            return;
        }
        this.pendingRequest = request;
        this.handlersToTry = this.getHandlerTypes(request);
        this.tryNextHandler();
    }

    void cancelCurrentHandler() {

        if (this.currentHandler != null) {
            this.currentHandler.cancel();
        }
    }

    boolean checkInternetPermission() {

        if (this.checkedInternetPermission) {
            return true;
        }

        final int permissionCheck = this.checkPermission(Manifest.permission.INTERNET);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            final String errorType = this.context.getString(R.string.com_facebook_internet_permission_error_title);
            final String errorDescription = this.context
                    .getString(R.string.com_facebook_internet_permission_error_message);
            this.complete(Result.createErrorResult(this.pendingRequest, errorType, errorDescription));

            return false;
        }

        this.checkedInternetPermission = true;
        return true;
    }

    int checkPermission(final String permission) {

        return this.context.checkCallingOrSelfPermission(permission);
    }

    void complete(final Result outcome) {

        // This might be null if, for some reason, none of the handlers were successfully tried (in which case
        // we already logged that).
        if (this.currentHandler != null) {
            this.logAuthorizationMethodComplete(this.currentHandler.getNameForLogging(), outcome,
                    this.currentHandler.methodLoggingExtras);
        }

        if (this.loggingExtras != null) {
            // Pass this back to the caller for logging at the aggregate level.
            outcome.loggingExtras = this.loggingExtras;
        }

        this.handlersToTry = null;
        this.currentHandler = null;
        this.pendingRequest = null;
        this.loggingExtras = null;

        this.notifyOnCompleteListener(outcome);
    }

    void completeAndValidate(final Result outcome) {

        // Do we need to validate a successful result (as in the case of a reauth)?
        if ((outcome.token != null) && this.pendingRequest.needsNewTokenValidation()) {
            this.validateSameFbidAndFinish(outcome);
        } else {
            // We're done, just notify the listener.
            this.complete(outcome);
        }
    }

    void continueAuth() {

        if ((this.pendingRequest == null) || (this.currentHandler == null)) {
            throw new FacebookException("Attempted to continue authorization without a pending request.");
        }

        if (this.currentHandler.needsRestart()) {
            this.currentHandler.cancel();
            this.tryCurrentHandler();
        }
    }

    Request createGetPermissionsRequest(final String accessToken) {

        final Bundle parameters = new Bundle();
        parameters.putString("access_token", accessToken);
        return new Request(null, "me/permissions", parameters, HttpMethod.GET, null);
    }

    Request createGetProfileIdRequest(final String accessToken) {

        final Bundle parameters = new Bundle();
        parameters.putString("fields", "id");
        parameters.putString("access_token", accessToken);
        return new Request(null, "me", parameters, HttpMethod.GET, null);
    }

    RequestBatch createReauthValidationBatch(final Result pendingResult) {

        // We need to ensure that the token we got represents the same fbid as the old one. We issue
        // a "me" request using the current token, a "me" request using the new token, and a "me/permissions"
        // request using the current token to get the permissions of the user.

        final ArrayList<String> fbids = new ArrayList<String>();
        final ArrayList<String> grantedPermissions = new ArrayList<String>();
        final ArrayList<String> declinedPermissions = new ArrayList<String>();
        final String newToken = pendingResult.token.getToken();

        final Request.Callback meCallback = new Request.Callback() {

            @Override
            public void onCompleted(final Response response) {

                try {
                    final GraphUser user = response.getGraphObjectAs(GraphUser.class);
                    if (user != null) {
                        fbids.add(user.getId());
                    }
                } catch (final Exception ex) {
                }
            }
        };

        final String validateSameFbidAsToken = this.pendingRequest.getPreviousAccessToken();
        final Request requestCurrentTokenMe = this.createGetProfileIdRequest(validateSameFbidAsToken);
        requestCurrentTokenMe.setCallback(meCallback);

        final Request requestNewTokenMe = this.createGetProfileIdRequest(newToken);
        requestNewTokenMe.setCallback(meCallback);

        final Request requestCurrentTokenPermissions = this.createGetPermissionsRequest(validateSameFbidAsToken);
        requestCurrentTokenPermissions.setCallback(new Request.Callback() {

            @Override
            public void onCompleted(final Response response) {

                try {
                    final Session.PermissionsPair permissionsPair = Session.handlePermissionResponse(response);
                    if (permissionsPair != null) {
                        grantedPermissions.addAll(permissionsPair.getGrantedPermissions());
                        declinedPermissions.addAll(permissionsPair.getDeclinedPermissions());
                    }
                } catch (final Exception ex) {
                }
            }
        });

        final RequestBatch batch = new RequestBatch(requestCurrentTokenMe, requestNewTokenMe,
                requestCurrentTokenPermissions);
        batch.setBatchApplicationId(this.pendingRequest.getApplicationId());
        batch.addCallback(new RequestBatch.Callback() {

            @Override
            public void onBatchCompleted(final RequestBatch batch) {

                try {
                    Result result = null;
                    if ((fbids.size() == 2) && (fbids.get(0) != null) && (fbids.get(1) != null)
                            && fbids.get(0).equals(fbids.get(1))) {
                        // Modify the token to have the right permission set.
                        final AccessToken tokenWithPermissions = AccessToken.createFromTokenWithRefreshedPermissions(
                                pendingResult.token, grantedPermissions, declinedPermissions);
                        result = Result
                                .createTokenResult(AuthorizationClient.this.pendingRequest, tokenWithPermissions);
                    } else {
                        result = Result.createErrorResult(AuthorizationClient.this.pendingRequest,
                                "User logged in as different Facebook user.", null);
                    }
                    AuthorizationClient.this.complete(result);
                } catch (final Exception ex) {
                    AuthorizationClient.this.complete(Result.createErrorResult(AuthorizationClient.this.pendingRequest,
                            "Caught exception", ex.getMessage()));
                } finally {
                    AuthorizationClient.this.notifyBackgroundProcessingStop();
                }
            }
        });

        return batch;
    }

    BackgroundProcessingListener getBackgroundProcessingListener() {

        return this.backgroundProcessingListener;
    }

    boolean getInProgress() {

        return (this.pendingRequest != null) && (this.currentHandler != null);
    }

    OnCompletedListener getOnCompletedListener() {

        return this.onCompletedListener;
    }

    StartActivityDelegate getStartActivityDelegate() {

        if (this.startActivityDelegate != null) {
            return this.startActivityDelegate;
        } else if (this.pendingRequest != null) {
            // Wrap the request's delegate in our own.
            return new StartActivityDelegate() {

                @Override
                public Activity getActivityContext() {

                    return AuthorizationClient.this.pendingRequest.getStartActivityDelegate().getActivityContext();
                }

                @Override
                public void startActivityForResult(final Intent intent, final int requestCode) {

                    AuthorizationClient.this.pendingRequest.getStartActivityDelegate().startActivityForResult(intent,
                            requestCode);
                }
            };
        }
        return null;
    }

    void logWebLoginCompleted(final String applicationId, final String e2e) {

        final AppEventsLogger appEventsLogger = AppEventsLogger.newLogger(this.context, applicationId);

        final Bundle parameters = new Bundle();
        parameters.putString(AnalyticsEvents.PARAMETER_WEB_LOGIN_E2E, e2e);
        parameters.putLong(AnalyticsEvents.PARAMETER_WEB_LOGIN_SWITCHBACK_TIME, System.currentTimeMillis());
        parameters.putString(AnalyticsEvents.PARAMETER_APP_ID, applicationId);

        appEventsLogger.logSdkEvent(AnalyticsEvents.EVENT_WEB_LOGIN_COMPLETE, null, parameters);
    }

    void notifyBackgroundProcessingStart() {

        if (this.backgroundProcessingListener != null) {
            this.backgroundProcessingListener.onBackgroundProcessingStarted();
        }
    }

    void notifyBackgroundProcessingStop() {

        if (this.backgroundProcessingListener != null) {
            this.backgroundProcessingListener.onBackgroundProcessingStopped();
        }
    }

    boolean onActivityResult(final int requestCode, final int resultCode, final Intent data) {

        if (requestCode == this.pendingRequest.getRequestCode()) {
            return this.currentHandler.onActivityResult(requestCode, resultCode, data);
        }
        return false;
    }

    void setBackgroundProcessingListener(final BackgroundProcessingListener backgroundProcessingListener) {

        this.backgroundProcessingListener = backgroundProcessingListener;
    }

    void setContext(final Activity activity) {

        this.context = activity;

        // If we are used in the context of an activity, we will always use that activity to
        // call startActivityForResult.
        this.startActivityDelegate = new StartActivityDelegate() {

            @Override
            public Activity getActivityContext() {

                return activity;
            }

            @Override
            public void startActivityForResult(final Intent intent, final int requestCode) {

                activity.startActivityForResult(intent, requestCode);
            }
        };
    }

    void setContext(final Context context) {

        this.context = context;
        // We rely on individual requests to tell us how to start an activity.
        this.startActivityDelegate = null;
    }

    void setOnCompletedListener(final OnCompletedListener onCompletedListener) {

        this.onCompletedListener = onCompletedListener;
    }

    void startOrContinueAuth(final AuthorizationRequest request) {

        if (this.getInProgress()) {
            this.continueAuth();
        } else {
            this.authorize(request);
        }
    }

    boolean tryCurrentHandler() {

        if (this.currentHandler.needsInternetPermission() && !this.checkInternetPermission()) {
            this.addLoggingExtra(EVENT_EXTRAS_MISSING_INTERNET_PERMISSION, AppEventsConstants.EVENT_PARAM_VALUE_YES,
                    false);
            return false;
        }

        final boolean tried = this.currentHandler.tryAuthorize(this.pendingRequest);
        if (tried) {
            this.logAuthorizationMethodStart(this.currentHandler.getNameForLogging());
        } else {
            // We didn't try it, so we don't get any other completion notification -- log that we skipped it.
            this.addLoggingExtra(EVENT_EXTRAS_NOT_TRIED, this.currentHandler.getNameForLogging(), true);
        }

        return tried;
    }

    void tryNextHandler() {

        if (this.currentHandler != null) {
            this.logAuthorizationMethodComplete(this.currentHandler.getNameForLogging(),
                    EVENT_PARAM_METHOD_RESULT_SKIPPED, null, null, this.currentHandler.methodLoggingExtras);
        }

        while ((this.handlersToTry != null) && !this.handlersToTry.isEmpty()) {
            this.currentHandler = this.handlersToTry.remove(0);

            final boolean started = this.tryCurrentHandler();

            if (started) {
                return;
            }
        }

        if (this.pendingRequest != null) {
            // We went through all handlers without successfully attempting an auth.
            this.completeWithFailure();
        }
    }

    void validateSameFbidAndFinish(final Result pendingResult) {

        if (pendingResult.token == null) {
            throw new FacebookException("Can't validate without a token");
        }

        final RequestBatch batch = this.createReauthValidationBatch(pendingResult);

        this.notifyBackgroundProcessingStart();

        batch.executeAsync();
    }

    static class AuthDialogBuilder extends WebDialog.Builder {

        private static final String OAUTH_DIALOG = "oauth";
        static final String REDIRECT_URI = "fbconnect://success";
        private String e2e;
        private boolean isRerequest;

        public AuthDialogBuilder(final Context context, final String applicationId, final Bundle parameters) {

            super(context, applicationId, OAUTH_DIALOG, parameters);
        }

        @Override
        public WebDialog build() {

            final Bundle parameters = this.getParameters();
            parameters.putString(ServerProtocol.DIALOG_PARAM_REDIRECT_URI, REDIRECT_URI);
            parameters.putString(ServerProtocol.DIALOG_PARAM_CLIENT_ID, this.getApplicationId());
            parameters.putString(ServerProtocol.DIALOG_PARAM_E2E, this.e2e);
            parameters.putString(ServerProtocol.DIALOG_PARAM_RESPONSE_TYPE, ServerProtocol.DIALOG_RESPONSE_TYPE_TOKEN);
            parameters.putString(ServerProtocol.DIALOG_PARAM_RETURN_SCOPES, ServerProtocol.DIALOG_RETURN_SCOPES_TRUE);

            // Only set the rerequest auth type for non legacy requests
            if (this.isRerequest && !Settings.getPlatformCompatibilityEnabled()) {
                parameters.putString(ServerProtocol.DIALOG_PARAM_AUTH_TYPE, ServerProtocol.DIALOG_REREQUEST_AUTH_TYPE);
            }

            return new WebDialog(this.getContext(), OAUTH_DIALOG, parameters, this.getTheme(), this.getListener());
        }

        public AuthDialogBuilder setE2E(final String e2e) {

            this.e2e = e2e;
            return this;
        }

        public AuthDialogBuilder setIsRerequest(final boolean isRerequest) {

            this.isRerequest = isRerequest;
            return this;
        }
    }

    abstract class AuthHandler implements Serializable {

        private static final long serialVersionUID = 1L;

        Map<String, String> methodLoggingExtras;

        protected void addLoggingExtra(final String key, final Object value) {

            if (this.methodLoggingExtras == null) {
                this.methodLoggingExtras = new HashMap<String, String>();
            }
            this.methodLoggingExtras.put(key, value == null ? null : value.toString());
        }

        void cancel() {

        }

        abstract String getNameForLogging();

        boolean needsInternetPermission() {

            return false;
        }

        boolean needsRestart() {

            return false;
        }

        boolean onActivityResult(final int requestCode, final int resultCode, final Intent data) {

            return false;
        }

        abstract boolean tryAuthorize(AuthorizationRequest request);
    }

    static class AuthorizationRequest implements Serializable {

        private static final long serialVersionUID = 1L;

        private transient final StartActivityDelegate startActivityDelegate;
        private final SessionLoginBehavior loginBehavior;
        private final int requestCode;
        private boolean legacy;
        private List<String> permissions;
        private final SessionDefaultAudience defaultAudience;
        private final String applicationId;
        private final String previousAccessToken;
        private final String authId;
        private boolean reRequest;

        AuthorizationRequest(final SessionLoginBehavior loginBehavior, final int requestCode, final boolean isLegacy,
                final List<String> permissions, final SessionDefaultAudience defaultAudience,
                final String applicationId, final String validateSameFbidAsToken,
                final StartActivityDelegate startActivityDelegate, final String authId) {

            this.loginBehavior = loginBehavior;
            this.requestCode = requestCode;
            this.legacy = isLegacy;
            this.permissions = permissions;
            this.defaultAudience = defaultAudience;
            this.applicationId = applicationId;
            this.previousAccessToken = validateSameFbidAsToken;
            this.startActivityDelegate = startActivityDelegate;
            this.authId = authId;
        }

        String getApplicationId() {

            return this.applicationId;
        }

        String getAuthId() {

            return this.authId;
        }

        SessionDefaultAudience getDefaultAudience() {

            return this.defaultAudience;
        }

        SessionLoginBehavior getLoginBehavior() {

            return this.loginBehavior;
        }

        List<String> getPermissions() {

            return this.permissions;
        }

        String getPreviousAccessToken() {

            return this.previousAccessToken;
        }

        int getRequestCode() {

            return this.requestCode;
        }

        StartActivityDelegate getStartActivityDelegate() {

            return this.startActivityDelegate;
        }

        boolean isLegacy() {

            return this.legacy;
        }

        boolean isRerequest() {

            return this.reRequest;
        }

        boolean needsNewTokenValidation() {

            return (this.previousAccessToken != null) && !this.legacy;
        }

        void setIsLegacy(final boolean isLegacy) {

            this.legacy = isLegacy;
        }

        void setPermissions(final List<String> permissions) {

            this.permissions = permissions;
        }

        void setRerequest(final boolean isRerequest) {

            this.reRequest = isRerequest;
        }
    }

    interface BackgroundProcessingListener {

        void onBackgroundProcessingStarted();

        void onBackgroundProcessingStopped();
    }

    class GetTokenAuthHandler extends AuthHandler {

        private static final long serialVersionUID = 1L;
        private transient GetTokenClient getTokenClient;

        @Override
        void cancel() {

            if (this.getTokenClient != null) {
                this.getTokenClient.cancel();
                this.getTokenClient = null;
            }
        }

        @Override
        String getNameForLogging() {

            return "get_token";
        }

        void getTokenCompleted(final AuthorizationRequest request, final Bundle result) {

            this.getTokenClient = null;

            AuthorizationClient.this.notifyBackgroundProcessingStop();

            if (result != null) {
                final ArrayList<String> currentPermissions = result
                        .getStringArrayList(NativeProtocol.EXTRA_PERMISSIONS);
                final List<String> permissions = request.getPermissions();
                if ((currentPermissions != null)
                        && ((permissions == null) || currentPermissions.containsAll(permissions))) {
                    // We got all the permissions we needed, so we can complete the auth now.
                    final AccessToken token = AccessToken.createFromNativeLogin(result,
                            AccessTokenSource.FACEBOOK_APPLICATION_SERVICE);
                    final Result outcome = Result.createTokenResult(AuthorizationClient.this.pendingRequest, token);
                    AuthorizationClient.this.completeAndValidate(outcome);
                    return;
                }

                // We didn't get all the permissions we wanted, so update the request with just the permissions
                // we still need.
                final List<String> newPermissions = new ArrayList<String>();
                for (final String permission : permissions) {
                    if (!currentPermissions.contains(permission)) {
                        newPermissions.add(permission);
                    }
                }
                if (!newPermissions.isEmpty()) {
                    this.addLoggingExtra(EVENT_EXTRAS_NEW_PERMISSIONS, TextUtils.join(",", newPermissions));
                }

                request.setPermissions(newPermissions);
            }

            AuthorizationClient.this.tryNextHandler();
        }

        @Override
        boolean needsRestart() {

            // if the getTokenClient is null, that means an orientation change has occurred, and we need
            // to recreate the GetTokenClient, so return true to indicate we need a restart
            return this.getTokenClient == null;
        }

        @Override
        boolean tryAuthorize(final AuthorizationRequest request) {

            this.getTokenClient = new GetTokenClient(AuthorizationClient.this.context, request.getApplicationId());
            if (!this.getTokenClient.start()) {
                return false;
            }

            AuthorizationClient.this.notifyBackgroundProcessingStart();

            final GetTokenClient.CompletedListener callback = new GetTokenClient.CompletedListener() {

                @Override
                public void completed(final Bundle result) {

                    GetTokenAuthHandler.this.getTokenCompleted(request, result);
                }
            };

            this.getTokenClient.setCompletedListener(callback);
            return true;
        }
    }

    abstract class KatanaAuthHandler extends AuthHandler {

        private static final long serialVersionUID = 1L;

        protected boolean tryIntent(final Intent intent, final int requestCode) {

            if (intent == null) {
                return false;
            }

            try {
                AuthorizationClient.this.getStartActivityDelegate().startActivityForResult(intent, requestCode);
            } catch (final ActivityNotFoundException e) {
                // We don't expect this to happen, since we've already validated the intent and bailed out before
                // now if it couldn't be resolved.
                return false;
            }

            return true;
        }
    }

    class KatanaProxyAuthHandler extends KatanaAuthHandler {

        private static final long serialVersionUID = 1L;
        private String applicationId;

        private Result handleResultOk(final Intent data) {

            final Bundle extras = data.getExtras();
            String error = extras.getString("error");
            if (error == null) {
                error = extras.getString("error_type");
            }
            final String errorCode = extras.getString("error_code");
            String errorMessage = extras.getString("error_message");
            if (errorMessage == null) {
                errorMessage = extras.getString("error_description");
            }

            final String e2e = extras.getString(NativeProtocol.FACEBOOK_PROXY_AUTH_E2E_KEY);
            if (!Utility.isNullOrEmpty(e2e)) {
                AuthorizationClient.this.logWebLoginCompleted(this.applicationId, e2e);
            }

            if ((error == null) && (errorCode == null) && (errorMessage == null)) {
                final AccessToken token = AccessToken.createFromWebBundle(
                        AuthorizationClient.this.pendingRequest.getPermissions(), extras,
                        AccessTokenSource.FACEBOOK_APPLICATION_WEB);
                return Result.createTokenResult(AuthorizationClient.this.pendingRequest, token);
            } else if (ServerProtocol.errorsProxyAuthDisabled.contains(error)) {
                return null;
            } else if (ServerProtocol.errorsUserCanceled.contains(error)) {
                return Result.createCancelResult(AuthorizationClient.this.pendingRequest, null);
            } else {
                return Result
                        .createErrorResult(AuthorizationClient.this.pendingRequest, error, errorMessage, errorCode);
            }
        }

        @Override
        String getNameForLogging() {

            return "katana_proxy_auth";
        }

        @Override
        boolean onActivityResult(final int requestCode, final int resultCode, final Intent data) {

            // Handle stuff
            Result outcome;

            if (data == null) {
                // This happens if the user presses 'Back'.
                outcome = Result.createCancelResult(AuthorizationClient.this.pendingRequest, "Operation canceled");
            } else if (resultCode == Activity.RESULT_CANCELED) {
                outcome = Result.createCancelResult(AuthorizationClient.this.pendingRequest,
                        data.getStringExtra("error"));
            } else if (resultCode != Activity.RESULT_OK) {
                outcome = Result.createErrorResult(AuthorizationClient.this.pendingRequest,
                        "Unexpected resultCode from authorization.", null);
            } else {
                outcome = this.handleResultOk(data);
            }

            if (outcome != null) {
                AuthorizationClient.this.completeAndValidate(outcome);
            } else {
                AuthorizationClient.this.tryNextHandler();
            }
            return true;
        }

        @Override
        boolean tryAuthorize(final AuthorizationRequest request) {

            this.applicationId = request.getApplicationId();

            final String e2e = getE2E();
            final Intent intent = NativeProtocol.createProxyAuthIntent(AuthorizationClient.this.context,
                    request.getApplicationId(), request.getPermissions(), e2e, request.isRerequest());

            this.addLoggingExtra(ServerProtocol.DIALOG_PARAM_E2E, e2e);

            return this.tryIntent(intent, request.getRequestCode());
        }
    }

    interface OnCompletedListener {

        void onCompleted(Result result);
    }

    static class Result implements Serializable {

        private static final long serialVersionUID = 1L;

        final Code code;

        final AccessToken token;
        final String errorMessage;
        final String errorCode;
        final AuthorizationRequest request;
        Map<String, String> loggingExtras;

        private Result(final AuthorizationRequest request, final Code code, final AccessToken token,
                final String errorMessage, final String errorCode) {

            this.request = request;
            this.token = token;
            this.errorMessage = errorMessage;
            this.code = code;
            this.errorCode = errorCode;
        }

        static Result createCancelResult(final AuthorizationRequest request, final String message) {

            return new Result(request, Code.CANCEL, null, message, null);
        }

        static Result createErrorResult(final AuthorizationRequest request, final String errorType,
                final String errorDescription) {

            return createErrorResult(request, errorType, errorDescription, null);
        }

        static Result createErrorResult(final AuthorizationRequest request, final String errorType,
                final String errorDescription, final String errorCode) {

            final String message = TextUtils.join(": ", Utility.asListNoNulls(errorType, errorDescription));
            return new Result(request, Code.ERROR, null, message, errorCode);
        }

        static Result createTokenResult(final AuthorizationRequest request, final AccessToken token) {

            return new Result(request, Code.SUCCESS, token, null, null);
        }

        enum Code {
            SUCCESS("success"), CANCEL("cancel"), ERROR("error");

            private final String loggingValue;

            Code(final String loggingValue) {

                this.loggingValue = loggingValue;
            }

            // For consistency across platforms, we want to use specific string values when logging these results.
            String getLoggingValue() {

                return this.loggingValue;
            }
        }
    }

    interface StartActivityDelegate {

        Activity getActivityContext();

        void startActivityForResult(Intent intent, int requestCode);
    }

    class WebViewAuthHandler extends AuthHandler {

        private static final long serialVersionUID = 1L;
        private transient WebDialog loginDialog;
        private String applicationId;
        private String e2e;

        private String loadCookieToken() {

            final Context context = AuthorizationClient.this.getStartActivityDelegate().getActivityContext();
            final SharedPreferences sharedPreferences = context.getSharedPreferences(WEB_VIEW_AUTH_HANDLER_STORE,
                    Context.MODE_PRIVATE);
            return sharedPreferences.getString(WEB_VIEW_AUTH_HANDLER_TOKEN_KEY, "");
        }

        private void saveCookieToken(final String token) {

            final Context context = AuthorizationClient.this.getStartActivityDelegate().getActivityContext();
            final SharedPreferences sharedPreferences = context.getSharedPreferences(WEB_VIEW_AUTH_HANDLER_STORE,
                    Context.MODE_PRIVATE);
            final SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(WEB_VIEW_AUTH_HANDLER_TOKEN_KEY, token);
            if (!editor.commit()) {
                // Utility.logd(TAG, "Could not update saved web view auth handler token.");
            }
        }

        @Override
        void cancel() {

            if (this.loginDialog != null) {
                this.loginDialog.dismiss();
                this.loginDialog = null;
            }
        }

        @Override
        String getNameForLogging() {

            return "web_view";
        }

        @Override
        boolean needsInternetPermission() {

            return true;
        }

        @Override
        boolean needsRestart() {

            // Because we are presenting WebView UI within the current context, we need to explicitly
            // restart the process if the context goes away and is recreated.
            return true;
        }

        void onWebDialogComplete(final AuthorizationRequest request, final Bundle values, final FacebookException error) {

            Result outcome;
            if (values != null) {
                // Actual e2e we got from the dialog should be used for logging.
                if (values.containsKey(ServerProtocol.DIALOG_PARAM_E2E)) {
                    this.e2e = values.getString(ServerProtocol.DIALOG_PARAM_E2E);
                }

                final AccessToken token = AccessToken.createFromWebBundle(request.getPermissions(), values,
                        AccessTokenSource.WEB_VIEW);
                outcome = Result.createTokenResult(AuthorizationClient.this.pendingRequest, token);

                // Ensure any cookies set by the dialog are saved
                // This is to work around a bug where CookieManager may fail to instantiate if CookieSyncManager
                // has never been created.
                final CookieSyncManager syncManager = CookieSyncManager
                        .createInstance(AuthorizationClient.this.context);
                syncManager.sync();
                this.saveCookieToken(token.getToken());
            } else {
                if (error instanceof FacebookOperationCanceledException) {
                    outcome = Result.createCancelResult(AuthorizationClient.this.pendingRequest,
                            "User canceled log in.");
                } else {
                    // Something went wrong, don't log a completion event since it will skew timing results.
                    this.e2e = null;

                    String errorCode = null;
                    String errorMessage = error.getMessage();
                    if (error instanceof FacebookServiceException) {
                        final FacebookRequestError requestError = ((FacebookServiceException) error).getRequestError();
                        errorCode = String.format(Locale.getDefault(), "%d",
                                Integer.valueOf(requestError.getErrorCode()));
                        errorMessage = requestError.toString();
                    }
                    outcome = Result.createErrorResult(AuthorizationClient.this.pendingRequest, null, errorMessage,
                            errorCode);
                }
            }

            if (!Utility.isNullOrEmpty(this.e2e)) {
                AuthorizationClient.this.logWebLoginCompleted(this.applicationId, this.e2e);
            }

            AuthorizationClient.this.completeAndValidate(outcome);
        }

        @Override
        boolean tryAuthorize(final AuthorizationRequest request) {

            this.applicationId = request.getApplicationId();
            final Bundle parameters = new Bundle();
            if (!Utility.isNullOrEmpty(request.getPermissions())) {
                final String scope = TextUtils.join(",", request.getPermissions());
                parameters.putString(ServerProtocol.DIALOG_PARAM_SCOPE, scope);
                this.addLoggingExtra(ServerProtocol.DIALOG_PARAM_SCOPE, scope);
            }

            final String previousToken = request.getPreviousAccessToken();
            if (!Utility.isNullOrEmpty(previousToken) && (previousToken.equals(this.loadCookieToken()))) {
                parameters.putString(ServerProtocol.DIALOG_PARAM_ACCESS_TOKEN, previousToken);
                // Don't log the actual access token, just its presence or absence.
                this.addLoggingExtra(ServerProtocol.DIALOG_PARAM_ACCESS_TOKEN, AppEventsConstants.EVENT_PARAM_VALUE_YES);
            } else {
                // The call to clear cookies will create the first instance of CookieSyncManager if necessary
                Utility.clearFacebookCookies(AuthorizationClient.this.context);
                this.addLoggingExtra(ServerProtocol.DIALOG_PARAM_ACCESS_TOKEN, AppEventsConstants.EVENT_PARAM_VALUE_NO);
            }

            final WebDialog.OnCompleteListener listener = new WebDialog.OnCompleteListener() {

                @Override
                public void onComplete(final Bundle values, final FacebookException error) {

                    WebViewAuthHandler.this.onWebDialogComplete(request, values, error);
                }
            };

            this.e2e = getE2E();
            this.addLoggingExtra(ServerProtocol.DIALOG_PARAM_E2E, this.e2e);

            final WebDialog.Builder builder = new AuthDialogBuilder(AuthorizationClient.this.getStartActivityDelegate()
                    .getActivityContext(), this.applicationId, parameters).setE2E(this.e2e)
                    .setIsRerequest(request.isRerequest()).setOnCompleteListener(listener);
            this.loginDialog = builder.build();
            this.loginDialog.show();

            return true;
        }
    }
}
