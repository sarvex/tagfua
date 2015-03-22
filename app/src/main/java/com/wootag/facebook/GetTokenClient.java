/**
 * Copyright 2010-present Facebook. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package com.TagFu.facebook;

import android.content.Context;
import android.os.Bundle;

import com.TagFu.facebook.internal.NativeProtocol;
import com.TagFu.facebook.internal.PlatformServiceClient;

final class GetTokenClient extends PlatformServiceClient {

    GetTokenClient(final Context context, final String applicationId) {

        super(context, NativeProtocol.MESSAGE_GET_ACCESS_TOKEN_REQUEST, NativeProtocol.MESSAGE_GET_ACCESS_TOKEN_REPLY,
                NativeProtocol.PROTOCOL_VERSION_20121101, applicationId);
    }

    @Override
    protected void populateRequestBundle(final Bundle data) {

    }
}
