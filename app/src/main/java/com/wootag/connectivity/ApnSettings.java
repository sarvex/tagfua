/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.TagFu.connectivity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

public class ApnSettings {

    private static final Logger LOG = LoggerManager.getLogger();

    private static final String APN_ID = "apn_id";
    private static final String QUESTION = "?";
    private static final String EQUALS = "=";
    private static final String PORT = "port";
    private static final String PROXY = "proxy";
    private static final String ID = "_id";
    private static final String NUMERIC = "numeric";
    private static final String MNC = "mnc";
    private static final String APN = "apn";
    private static final String NAME = "name";
    private static final String MCC = "mcc";

    private static final Uri APN_TABLE_URI = Uri.parse("content://telephony/carriers");
    private static final Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");

    // TODO check for the static context
    private static Context context;

    public ApnSettings(final Context context) {

        ApnSettings.context = context;
    }

    public int createApn(final String name, final String apnName, final String mcc, final String mnc) {

        int result = 0;
        final ContentResolver resolver = context.getContentResolver();

        final ContentValues values = new ContentValues();
        values.put(NAME, name);
        values.put(APN, apnName);
        values.put(MCC, mcc);
        values.put(MNC, mnc);
        values.put(NUMERIC, mcc + mnc);

        final Uri newRow = resolver.insert(APN_TABLE_URI, values);

        Cursor cursor = null;
        try {
            if (newRow != null) {
                cursor = resolver.query(newRow, null, null, null, null);

                final int index = cursor.getColumnIndex(ID);

                cursor.moveToFirst();

                result = cursor.getShort(index);

                LOG.i("New ID: " + result + ": Inserting new APN succeeded!");
            }
        } catch (final SQLException exception) {
            LOG.i(exception.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        LOG.i("Created apn with name as: " + apnName + " and with id as: " + result);
        return result;
    }

    public int getApnId(final String apnName) {

        int index = -1;

        final Cursor cursor = context.getContentResolver().query(APN_TABLE_URI, new String[] { ID },
                APN + EQUALS + QUESTION, new String[] { apnName }, null);

        if (cursor.moveToFirst()) {
            index = cursor.getInt(cursor.getColumnIndex(ID));

        }

        cursor.close();

        LOG.i("Apnname is: " + apnName + " and its id is: " + index);

        return index;

    }

    public ApnDto getDefaultApn() {

        ApnDto result = null;

        final Cursor cursor = context.getContentResolver().query(PREFERRED_APN_URI,
                new String[] { ID, NAME, APN, PROXY, PORT }, null, null, null);

        if (cursor.moveToFirst()) {
            result = new ApnDto();
            final String name = cursor.getString(cursor.getColumnIndex(NAME));
            final String apnName = cursor.getString(cursor.getColumnIndex(APN));
            final int index = cursor.getInt(cursor.getColumnIndex(ID));

            result.setPort(cursor.getInt(cursor.getColumnIndex(PORT)));
            result.setProxy(cursor.getString(cursor.getColumnIndex(PROXY)));
            result.setApnName(apnName);
            result.setName(name);
            result.setId(index);
        }

        cursor.close();

        return result;
    }

    public boolean isApnCreated(final String apnName) {

        boolean result = false;
        final ContentResolver resolver = context.getContentResolver();

        Cursor cursor = null;
        cursor = resolver.query(APN_TABLE_URI, new String[] { ID, NAME, APN }, APN + EQUALS + QUESTION,
                new String[] { apnName }, null);

        result = cursor.moveToFirst();

        cursor.close();

        LOG.i("APN WITH NAME: " + apnName + " check result is: " + result);

        return result;

    }

    public boolean setApn(final int index) {

        boolean result = false;
        final ContentResolver resolver = context.getContentResolver();
        final ContentValues values = new ContentValues();
        values.put(APN_ID, Integer.valueOf(index));

        LOG.i("Setting the apn with id as: " + index);

        resolver.update(PREFERRED_APN_URI, values, null, null);
        final Cursor cursor = resolver.query(PREFERRED_APN_URI, new String[] { NAME, APN }, ID + EQUALS + index, null,
                null);
        if (cursor != null) {
            result = true;
        }

        if (cursor != null) {
            cursor.close();
        }

        return result;
    }

}
