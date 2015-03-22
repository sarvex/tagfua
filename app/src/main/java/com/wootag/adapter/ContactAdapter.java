/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.TagFu.adapter;

import java.util.List;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.TagFu.R;
import com.TagFu.dto.Contact;

public class ContactAdapter extends BaseAdapter {

    private static final Logger LOG = LoggerManager.getLogger();

    private final List<Contact> contactList;
    private final Context context;
    private final String socialSite;
    private final boolean headerVisible;

    public ContactAdapter(final Context context, final int resource, final List<Contact> objects,
            final String socialSite, final boolean headerVisible) {

        // super(context, resource, objects);
        this.context = context;
        this.contactList = objects;
        this.socialSite = socialSite;
        this.headerVisible = headerVisible;
    }

    @Override
    public int getCount() {

        return this.contactList.size();
    }

    @Override
    public Contact getItem(final int index) {

        return this.contactList.get(index);
    }

    @Override
    public long getItemId(final int position) {

        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        ViewHolder holder;
        final Contact friendsObj = this.getItem(position);

        if (convertView == null) {
            convertView = ((LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
                    R.layout.contact_item, parent, false);
            holder = this.initHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (this.headerVisible) {
            if (position == 0) {
                holder.headerText.setText("My Friends");
                holder.itemHeader.setVisibility(View.VISIBLE);
            } else {
                holder.itemHeader.setVisibility(View.GONE);
            }
        } else {
            holder.itemHeader.setVisibility(View.GONE);
        }

        if (friendsObj.getContactNumber() != null) {
            holder.number.setText(friendsObj.getContactNumber());
        }
        if (friendsObj.getImagePath() != null) {
            holder.profImage.setImageURI(Uri.parse(friendsObj.getImagePath()));
        } else {
            holder.profImage.setImageResource(R.drawable.member);
        }
        if (friendsObj.isChecked()) {
            holder.checkView.setImageResource(R.drawable.checked);
        } else {
            holder.checkView.setImageResource(R.drawable.unchecked);
        }
        holder.profName.setText(friendsObj.getContactName());
        return convertView;
    }

    private ViewHolder initHolder(final View row) {

        final ViewHolder holder = new ViewHolder();
        // holder.layout=(RelativeLayout)row.findViewById(R.id.lineItem);
        holder.profImage = (ImageView) row.findViewById(R.id.image);
        holder.profName = (TextView) row.findViewById(R.id.name);
        holder.number = (TextView) row.findViewById(R.id.number);
        holder.checkView = (ImageView) row.findViewById(R.id.contactCheckbox);
        holder.itemHeader = (LinearLayout) row.findViewById(R.id.contactHeader);
        holder.headerText = (TextView) row.findViewById(R.id.contactHeaderText);
        return holder;
    }

    public class ViewHolder {

        // public RelativeLayout layout;
        public ImageView profImage;
        public TextView profName;
        public ImageView checkView;
        public TextView number;
        public TextView headerText;
        public LinearLayout itemHeader;

    }

}
