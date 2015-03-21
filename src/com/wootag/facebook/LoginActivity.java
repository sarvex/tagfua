/**
 * Copyright 2010-present Facebook. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package com.TagFu.facebook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.wootag.R;

/**
 * This Activity is a necessary part of the overall Facebook login process but is not meant to be used directly. Add
 * this activity to your AndroidManifest.xml to ensure proper handling of Facebook login.
 *
 * <pre>
 * {@code
 * <activity android:name="com.facebook.LoginActivity"
 *           android:theme="@android:style/Theme.Translucent.NoTitleBar"
 *           android:label="@string/app_name" />
 * }
 * </pre>
 *
 * Do not start this activity directly.
 */
public class LoginActivity extends Activity {

    static final String RESULT_KEY = "com.facebook.LoginActivity:Result";

    private static final String TAG = LoginActivity.class.getName();
    private static final String NULL_CALLING_PKG_ERROR_MSG = "Cannot call LoginActivity with a null calling package. "
            + "This can occur if the launchMode of the caller is singleInstance.";
    private static final String SAVED_CALLING_PKG_KEY = "callingPackage";
    private static final String SAVED_AUTH_CLIENT = "authorizationClient";
    private static final String EXTRA_REQUEST = "request";

    private String callingPackage;
    private AuthorizationClient authorizationClient;
    private AuthorizationClient.AuthorizationRequest request;

    static Bundle populateIntentExtras(final AuthorizationClient.AuthorizationRequest request) {

        final Bundle extras = new Bundle();
        extras.putSerializable(EXTRA_REQUEST, request);
        return extras;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.com_facebook_login_activity_layout);

        if (savedInstanceState != null) {
            this.callingPackage = savedInstanceState.getString(SAVED_CALLING_PKG_KEY);
            this.authorizationClient = (AuthorizationClient) savedInstanceState.getSerializable(SAVED_AUTH_CLIENT);
        } else {
            this.callingPackage = this.getCallingPackage();
            this.authorizationClient = new AuthorizationClient();
            this.request = (AuthorizationClient.AuthorizationRequest) this.getIntent().getSerializableExtra(
                    EXTRA_REQUEST);
        }

        this.authorizationClient.setContext(this);
        this.authorizationClient.setOnCompletedListener(new AuthorizationClient.OnCompletedListener() {

            @Override
            public void onCompleted(final AuthorizationClient.Result outcome) {

                LoginActivity.this.onAuthClientCompleted(outcome);
            }
        });
        this.authorizationClient
                .setBackgroundProcessingListener(new AuthorizationClient.BackgroundProcessingListener() {

                    @Override
                    public void onBackgroundProcessingStarted() {

                        LoginActivity.this.findViewById(R.id.com_facebook_login_activity_progress_bar).setVisibility(
                                View.VISIBLE);
                    }

                    @Override
                    public void onBackgroundProcessingStopped() {

                        LoginActivity.this.findViewById(R.id.com_facebook_login_activity_progress_bar).setVisibility(
                                View.GONE);
                    }
                });
    }

    @Override
    public void onPause() {

        super.onPause();

        this.authorizationClient.cancelCurrentHandler();
        this.findViewById(R.id.com_facebook_login_activity_progress_bar).setVisibility(View.GONE);
    }

    @Override
    public void onResume() {

        super.onResume();

        // If the calling package is null, this generally means that the callee was started
        // with a launchMode of singleInstance. Unfortunately, Android does not allow a result
        // to be set when the callee is a singleInstance, so we log an error and return.
        if (this.callingPackage == null) {
            Log.e(TAG, NULL_CALLING_PKG_ERROR_MSG);
            this.finish();
            return;
        }

        this.authorizationClient.startOrContinueAuth(this.request);
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putString(SAVED_CALLING_PKG_KEY, this.callingPackage);
        outState.putSerializable(SAVED_AUTH_CLIENT, this.authorizationClient);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {

        this.authorizationClient.onActivityResult(requestCode, resultCode, data);
    }

    void onAuthClientCompleted(final AuthorizationClient.Result outcome) {

        this.request = null;

        final int resultCode = (outcome.code == AuthorizationClient.Result.Code.CANCEL) ? RESULT_CANCELED : RESULT_OK;

        final Bundle bundle = new Bundle();
        bundle.putSerializable(RESULT_KEY, outcome);

        final Intent resultIntent = new Intent();
        resultIntent.putExtras(bundle);
        this.setResult(resultCode, resultIntent);

        this.finish();
    }
}
