/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

public class SharedPref {

    private static final String SAY_CHEESE = "SayCheese";

    private static final Logger LOG = LoggerManager.getLogger();

    private Context context;
    private static SharedPreferences myPrefs;

    public static void writeToPref(final Context context, final String key, final String value) {

        SharedPref.myPrefs = context.getSharedPreferences(SAY_CHEESE, Context.MODE_WORLD_WRITEABLE);
        final SharedPreferences.Editor prefsEditor = SharedPref.myPrefs.edit();
        prefsEditor.putString(key, value);
        prefsEditor.commit();
    }

    public static void writeToPref(final Context context, final String key, final int value) {

        SharedPref.myPrefs = context.getSharedPreferences(SAY_CHEESE, Context.MODE_WORLD_WRITEABLE);
        final SharedPreferences.Editor prefsEditor = SharedPref.myPrefs.edit();
        prefsEditor.putInt(key, value);
        prefsEditor.commit();
    }

    public static int getIntFromPref(final Context context, final String key) {

        SharedPref.myPrefs = context.getSharedPreferences(SAY_CHEESE, Context.MODE_WORLD_WRITEABLE);
        return SharedPref.myPrefs.getInt(key, 0);
    }

    public static String getStringFromPref(final Context context, final String key) {

        SharedPref.myPrefs = context.getSharedPreferences(SAY_CHEESE, Context.MODE_WORLD_WRITEABLE);
        return SharedPref.myPrefs.getString(key, null);
    }

}
