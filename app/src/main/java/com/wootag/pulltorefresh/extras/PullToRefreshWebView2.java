/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes. Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 *******************************************************************************/
package com.TagFu.pulltorefresh.extras;

import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

import com.TagFu.pulltorefresh.PullToRefreshWebView;

/**
 * An advanced version of {@link PullToRefreshWebView} which delegates the triggering of the PullToRefresh gesture to
 * the Javascript running within the WebView. This means that you should only use this class if:
 * <p/>
 * <ul>
 * <li>{@link PullToRefreshWebView} doesn't work correctly because you're using <code>overflow:scroll</code> or
 * something else which means {@link WebView#getScrollY()} doesn't return correct values.</li>
 * <li>You control the web content being displayed, as you need to write some Javascript callbacks.</li>
 * </ul>
 * <p/>
 * <p/>
 * The way this call works is that when a PullToRefresh gesture is in action, the following Javascript methods will be
 * called: <code>isReadyForPullDown()</code> and <code>isReadyForPullUp()</code>, it is your job to calculate whether
 * the view is in a state where a PullToRefresh can happen, and return the result via the callback mechanism. An example
 * can be seen below:
 * <p/>
 *
 * <pre>
 * function isReadyForPullDown() {
 *   var result = ...  // Probably using the .scrollTop DOM attribute
 *   ptr.isReadyForPullDownResponse(result);
 * }
 * 
 * function isReadyForPullUp() {
 *   var result = ...  // Probably using the .scrollBottom DOM attribute
 *   ptr.isReadyForPullUpResponse(result);
 * }
 * </pre>
 *
 * @author Chris Banes
 */
public class PullToRefreshWebView2 extends PullToRefreshWebView {

    static final String JS_INTERFACE_PKG = "ptr";
    static final String DEF_JS_READY_PULL_DOWN_CALL = "javascript:isReadyForPullDown();";
    static final String DEF_JS_READY_PULL_UP_CALL = "javascript:isReadyForPullUp();";

    private JsValueCallback mJsCallback;

    final AtomicBoolean mIsReadyForPullDown = new AtomicBoolean(false);

    protected final AtomicBoolean mIsReadyForPullUp = new AtomicBoolean(false);

    public PullToRefreshWebView2(final Context context) {

        super(context);
    }

    public PullToRefreshWebView2(final Context context, final AttributeSet attrs) {

        super(context, attrs);
    }

    public PullToRefreshWebView2(final Context context, final Mode mode) {

        super(context, mode);
    }

    @Override
    protected WebView createRefreshableView(final Context context, final AttributeSet attrs) {

        final WebView webView = super.createRefreshableView(context, attrs);

        // Need to add JS Interface so we can get the response back
        this.mJsCallback = new JsValueCallback();
        webView.addJavascriptInterface(this.mJsCallback, JS_INTERFACE_PKG);

        return webView;
    }

    @Override
    protected boolean isReadyForPullEnd() {

        // Call Javascript...
        this.getRefreshableView().loadUrl(DEF_JS_READY_PULL_UP_CALL);

        // Response will be given to JsValueCallback, which will update
        // mIsReadyForPullUp

        return this.mIsReadyForPullUp.get();
    }

    @Override
    protected boolean isReadyForPullStart() {

        // Call Javascript...
        this.getRefreshableView().loadUrl(DEF_JS_READY_PULL_DOWN_CALL);

        // Response will be given to JsValueCallback, which will update
        // mIsReadyForPullDown

        return this.mIsReadyForPullDown.get();
    }

    /**
     * Used for response from Javascript
     *
     * @author Chris Banes
     */
    final class JsValueCallback {

        public void isReadyForPullDownResponse(final boolean response) {

            PullToRefreshWebView2.this.mIsReadyForPullDown.set(response);
        }

        public void isReadyForPullUpResponse(final boolean response) {

            PullToRefreshWebView2.this.mIsReadyForPullUp.set(response);
        }
    }
}
