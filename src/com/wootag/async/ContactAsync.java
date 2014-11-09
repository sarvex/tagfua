/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.async;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wootag.R;
import com.wootag.dto.Contact;
import com.wootag.util.ContactInterface;

public class ContactAsync extends AsyncTask<Void, Void, Void> {

    private static final Logger LOG = LoggerManager.getLogger();

    private ProgressDialog progressDialog;
    private String type;
    private List<Contact> response;
    private final Context context;
    public ContactInterface delegate;

    public ContactAsync(final Context context) {

        this.context = context;
    }

    public String loadPhoto(final long id) {

        Uri photo = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
        photo = Uri.withAppendedPath(photo, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        return photo.toString();

    }

    /**
     * Retrieving all phone contacts
     */
    public List<Contact> readContacts() {

        final List<Contact> contacts = new ArrayList<Contact>();
        final ContentResolver contentResolver = this.context.getContentResolver();
        final Cursor cur = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                final String id = cur.getString(cur.getColumnIndex(BaseColumns._ID));
                final String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    LOG.i("conatcts", "name : " + name + ", ID : " + id);
                    // get the phone number
                    final Cursor pCur = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { id }, null);
                    while (pCur.moveToNext()) {
                        final Contact contact = new Contact();
                        final String phone = pCur.getString(pCur
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        final String image = pCur.getString(pCur
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_ID));
                        if (image != null) {
                            final String path = this.loadPhoto(Long.parseLong(id));
                            if (path != null) {
                                contact.setImagePath(this.loadPhoto(Long.parseLong(id)));
                            }
                        }
                        contact.setContactName(name);
                        contact.setContactNumber(phone);
                        contacts.add(contact);
                    }
                    pCur.close();

                }
            }
        }
        return contacts;

    }

    @Override
    protected Void doInBackground(final Void... params) {

        this.response = this.readContacts();
        return null;
    }

    @Override
    protected void onPostExecute(final Void result) {

        super.onPostExecute(result);
        if (this.progressDialog != null) {
            this.progressDialog.dismiss();

        }
        if (this.response != null) {
            this.delegate.contacts(this.response);
        }

    }

    @Override
    protected void onPreExecute() {

        super.onPreExecute();
        this.progressDialog = ProgressDialog.show(this.context, "", "", true);
        final View view = ((LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
                R.layout.progress_bar, null, false);
        this.progressDialog.setContentView(view);
        this.progressDialog.setCancelable(false);
        this.progressDialog.setCanceledOnTouchOutside(false);
        this.progressDialog.show();

    }
}
