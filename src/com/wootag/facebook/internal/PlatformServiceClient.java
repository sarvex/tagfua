/**
 * Copyright 2010-present Facebook. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package com.TagFu.facebook.internal;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

/**
 * com.facebook.internal is solely for the use of other packages within the Facebook SDK for Android. Use of any of the
 * classes in this package is unsupported, and they may be modified or removed without warning at any time.
 */
abstract public class PlatformServiceClient implements ServiceConnection {

    private final Context context;
    private final Handler handler;
    private CompletedListener listener;
    private boolean running;
    private Messenger sender;
    private final int requestMessage;
    private final int replyMessage;
    private final String applicationId;
    private final int protocolVersion;

    public PlatformServiceClient(final Context context, final int requestMessage, final int replyMessage,
            final int protocolVersion, final String applicationId) {

        final Context applicationContext = context.getApplicationContext();

        this.context = (applicationContext != null) ? applicationContext : context;
        this.requestMessage = requestMessage;
        this.replyMessage = replyMessage;
        this.applicationId = applicationId;
        this.protocolVersion = protocolVersion;

        this.handler = new Handler() {

            @Override
            public void handleMessage(final Message message) {

                PlatformServiceClient.this.handleMessage(message);
            }
        };
    }

    public void cancel() {

        this.running = false;
    }

    @Override
    public void onServiceConnected(final ComponentName name, final IBinder service) {

        this.sender = new Messenger(service);
        this.sendMessage();
    }

    @Override
    public void onServiceDisconnected(final ComponentName name) {

        this.sender = null;
        this.context.unbindService(this);
        this.callback(null);
    }

    public void setCompletedListener(final CompletedListener listener) {

        this.listener = listener;
    }

    public boolean start() {

        if (this.running) {
            return false;
        }

        // Make sure that the service can handle the requested protocol version
        final int availableVersion = NativeProtocol.getLatestAvailableProtocolVersionForService(this.context,
                this.protocolVersion);
        if (availableVersion == NativeProtocol.NO_PROTOCOL_AVAILABLE) {
            return false;
        }

        final Intent intent = NativeProtocol.createPlatformServiceIntent(this.context);
        if (intent == null) {
            return false;
        }
        this.running = true;
        this.context.bindService(intent, this, Context.BIND_AUTO_CREATE);
        return true;
    }

    private void callback(final Bundle result) {

        if (!this.running) {
            return;
        }
        this.running = false;

        final CompletedListener callback = this.listener;
        if (callback != null) {
            callback.completed(result);
        }
    }

    private void sendMessage() {

        final Bundle data = new Bundle();
        data.putString(NativeProtocol.EXTRA_APPLICATION_ID, this.applicationId);

        this.populateRequestBundle(data);

        final Message request = Message.obtain(null, this.requestMessage);
        request.arg1 = this.protocolVersion;
        request.setData(data);
        request.replyTo = new Messenger(this.handler);

        try {
            this.sender.send(request);
        } catch (final RemoteException e) {
            this.callback(null);
        }
    }

    protected Context getContext() {

        return this.context;
    }

    protected void handleMessage(final Message message) {

        if (message.what == this.replyMessage) {
            final Bundle extras = message.getData();
            final String errorType = extras.getString(NativeProtocol.STATUS_ERROR_TYPE);
            if (errorType != null) {
                this.callback(null);
            } else {
                this.callback(extras);
            }
            this.context.unbindService(this);
        }
    }

    protected abstract void populateRequestBundle(Bundle data);

    public interface CompletedListener {

        void completed(Bundle result);
    }
}
