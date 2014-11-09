/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.dto;

import com.google.android.gms.plus.model.people.Person.Image;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

public class Friend {

    private static final Logger LOG = LoggerManager.getLogger();

    private Image image;
    private String follow;
    private String friendId;
    private String friendImage;
    private String friendLargeImage;
    private String friendName;
    private String location;
    private String wootagId;
    private boolean editMode;
    private boolean next;
    private boolean selected;
    private boolean taggedUser;

    public String getFriendId() {

        return this.friendId;
    }

    public String getFriendImage() {

        return this.friendImage;
    }

    public String getFriendLargeImage() {

        return this.friendLargeImage;
    }

    public String getFriendName() {

        return this.friendName;
    }

    public Image getImage() {

        return this.image;
    }

    public String getIsFollow() {

        return this.follow;
    }

    public String getLocation() {

        return this.location;
    }

    public String getWootagId() {

        return this.wootagId;
    }

    public boolean isEditMode() {

        return this.editMode;
    }

    public boolean isNext() {

        return this.next;
    }

    public boolean isSelected() {

        return this.selected;
    }

    public boolean isTaggedUser() {

        return this.taggedUser;
    }

    public void setEditMode(final boolean editMode) {

        this.editMode = editMode;
    }

    public void setFriendID(final String friendId) {

        this.friendId = friendId;
    }

    public void setFriendImage(final String friendImage) {

        this.friendImage = friendImage;
    }

    public void setFriendLargeImage(final String friendLargeImage) {

        this.friendLargeImage = friendLargeImage;
    }

    public void setFriendName(final String friendName) {

        this.friendName = friendName;
    }

    public void setImage(final Image image) {

        this.image = image;
    }

    public void setIsFollow(final String isFollow) {

        this.follow = isFollow;
    }

    public void setLocation(final String location) {

        this.location = location;
    }

    public void setNext(final boolean next) {

        this.next = next;
    }

    public void setSelected(final boolean selected) {

        this.selected = selected;
    }

    public void setTaggedUser(final boolean taggedUser) {

        this.taggedUser = taggedUser;
    }

    public void setWootagId(final String wootagId) {

        this.wootagId = wootagId;
    }

}
