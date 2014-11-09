/**
 * Copyright 2010-present Facebook. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package com.wootag.facebook.internal;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.wootag.facebook.LoggingBehavior;
import com.wootag.facebook.Settings;

/**
 * com.facebook.internal is solely for the use of other packages within the Facebook SDK for Android. Use of any of the
 * classes in this package is unsupported, and they may be modified or removed without warning at any time.
 */
public class Logger {

    public static final String LOG_TAG_BASE = "FacebookSDK.";
    private static final Map<String, String> stringsToReplace = new HashMap<String, String>();

    private final LoggingBehavior behavior;
    private final String tag;
    private StringBuilder contents;
    private int priority = Log.DEBUG;

    public Logger(final LoggingBehavior behavior, final String tag) {

        Validate.notNullOrEmpty(tag, "tag");

        this.behavior = behavior;
        this.tag = LOG_TAG_BASE + tag;
        this.contents = new StringBuilder();
    }

    public static void log(final LoggingBehavior behavior, final int priority, String tag, String string) {

        if (Settings.isLoggingBehaviorEnabled(behavior)) {
            string = replaceStrings(string);
            if (!tag.startsWith(LOG_TAG_BASE)) {
                tag = LOG_TAG_BASE + tag;
            }
            Log.println(priority, tag, string);

            // Developer errors warrant special treatment by printing out a stack trace, to make both more noticeable,
            // and let the source of the problem be more easily pinpointed.
            if (behavior == LoggingBehavior.DEVELOPER_ERRORS) {
                (new Exception()).printStackTrace();
            }
        }
    }

    public static void log(final LoggingBehavior behavior, final String tag, final String string) {

        log(behavior, Log.DEBUG, tag, string);
    }

    public static void log(final LoggingBehavior behavior, final String tag, final String format, final Object... args) {

        if (Settings.isLoggingBehaviorEnabled(behavior)) {
            final String string = String.format(format, args);
            log(behavior, Log.DEBUG, tag, string);
        }
    }

    public synchronized static void registerAccessToken(final String accessToken) {

        if (!Settings.isLoggingBehaviorEnabled(LoggingBehavior.INCLUDE_ACCESS_TOKENS)) {
            registerStringToReplace(accessToken, "ACCESS_TOKEN_REMOVED");
        }
    }

    // Note that the mapping of replaced strings is never emptied, so it should be used only for things that
    // are not expected to be too numerous, such as access tokens.
    public synchronized static void registerStringToReplace(final String original, final String replace) {

        stringsToReplace.put(original, replace);
    }

    private synchronized static String replaceStrings(String string) {

        for (final Map.Entry<String, String> entry : stringsToReplace.entrySet()) {
            string = string.replace(entry.getKey(), entry.getValue());
        }
        return string;
    }

    public void append(final String string) {

        if (this.shouldLog()) {
            this.contents.append(string);
        }
    }

    public void append(final String format, final Object... args) {

        if (this.shouldLog()) {
            this.contents.append(String.format(format, args));
        }
    }

    public void append(final StringBuilder stringBuilder) {

        if (this.shouldLog()) {
            this.contents.append(stringBuilder);
        }
    }

    public void appendKeyValue(final String key, final Object value) {

        this.append("  %s:\t%s\n", key, value);
    }

    public String getContents() {

        return replaceStrings(this.contents.toString());
    }

    public int getPriority() {

        return this.priority;
    }

    // Writes the accumulated contents, then clears contents to start again.
    public void log() {

        this.logString(this.contents.toString());
        this.contents = new StringBuilder();
    }

    // Immediately logs a string, ignoring any accumulated contents, which are left unchanged.
    public void logString(final String string) {

        log(this.behavior, this.priority, this.tag, string);
    }

    public void setPriority(final int value) {

        Validate.oneOf(Integer.valueOf(value), "value", Integer.valueOf(Log.ASSERT), Integer.valueOf(Log.DEBUG),
                Integer.valueOf(Log.ERROR), Integer.valueOf(Log.INFO), Integer.valueOf(Log.VERBOSE),
                Integer.valueOf(Log.WARN));

        this.priority = value;
    }

    private boolean shouldLog() {

        return Settings.isLoggingBehaviorEnabled(this.behavior);
    }
}
