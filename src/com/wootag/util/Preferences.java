/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.util;

import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wootag.Constant;
import com.wootag.VideoPlayerApp;

public class Preferences {

    private static final Logger LOG = LoggerManager.getLogger();

    // Shared Preferences Name
    private final String preferencesName;

    private final Context context;

    private final SharedPreferences myPrefs;
    private final SharedPreferences.Editor prefsEditor;

    public Preferences(final String preferencesName) {

        this.preferencesName = preferencesName;
        this.context = VideoPlayerApp.getInstance();
        this.myPrefs = this.context.getSharedPreferences(this.preferencesName, Context.MODE_WORLD_READABLE);
        this.prefsEditor = this.myPrefs.edit();
    }

    public Map<String, ?> getAll() {

        return this.myPrefs.getAll();
    }

    public boolean getBoolean(final String key) {

        return this.myPrefs.getBoolean(key, false);
    }

    public float getFloat(final String key) {

        return this.myPrefs.getFloat(key, 0);
    }

    public int getInt(final String key) {

        return this.myPrefs.getInt(key, 0);
    }

    public long getLong(final String key) {

        return this.myPrefs.getLong(key, 0);
    }

    public String getString(final String key) {

        return this.myPrefs.getString(key, null);
    }

    public void putBoolean(final String key, final boolean value) {

        this.prefsEditor.putBoolean(key, value);
        this.prefsEditor.commit();
    }

    public void putFloat(final String key, final float value) {

        this.prefsEditor.putFloat(key, value);
        this.prefsEditor.commit();
    }

    public void putInt(final String key, final int value) {

        this.prefsEditor.putInt(key, value);
        this.prefsEditor.commit();
    }

    public void putLong(final String key, final long value) {

        this.prefsEditor.putLong(key, value);
        this.prefsEditor.commit();
    }

    public void putString(final String key, final String value) {

        this.prefsEditor.putString(key, value);
        this.prefsEditor.commit();
    }

    public void removeAllPreferenceValues() {

        this.prefsEditor.clear();
        this.prefsEditor.commit();
        final Map<String, ?> map = this.myPrefs.getAll();
    }

    public void removeAllTwitterKeys() {

        this.prefsEditor.remove(Constant.TWITTER_OAUTH_TOKEN);
        this.prefsEditor.remove(Constant.TWITTER_SECRET_KEY);
        this.prefsEditor.commit();
        // TwitterFactory().getInstance().shutdown();
        // System.out.println("map size "+map.size());
    }
}
