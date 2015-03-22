/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.TagFu.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.TagFu.Constant;
import com.TagFu.util.Stream;

public class MyPage implements Serializable {

    private static final Logger LOG = LoggerManager.getLogger();

    private static final long serialVersionUID = -438557456473626807L;

    private List<MoreVideos> moreVideos;
    private List<MyPageDto> videoList;
    private List<SuggestedUsersDto> suggestedUsers;
    private String bannerPath;
    private String bio;
    private String country;
    private String isAddToPrivateGroup;
    private String isFollow;
    private String isPrivateReqSent;
    private String isRespToPvtReq;
    private String lastUpdate;
    private String pendingPrivateGroupRequests;
    private String profession;
    private String pthotoPath;
    private String totalNoOfFollowing;
    private String totalNoOfLikes;
    private String totalNoOfPrivateGroupPeople;
    private String totalNoOfTags;
    private String totalNoOfVideos;
    private String totalNoOffollowers;
    private String userPickView;
    private String userid;
    private String username;
    private String website;

    public MyPage() {

        this.videoList = new ArrayList<MyPageDto>();
        this.moreVideos = new ArrayList<MoreVideos>();
        this.suggestedUsers = new ArrayList<SuggestedUsersDto>();

    }

    public static String getJsonUserPhotoView() {

        return Constant.PROF_PHOTO_PATH;
    }

    public String getBannerPath() {

        return this.bannerPath;
    }

    public String getBio() {

        return this.bio;
    }

    public String getCountry() {

        return this.country;
    }

    public String getIsAddToPrivateGroup() {

        return this.isAddToPrivateGroup;
    }

    public String getIsFollow() {

        return this.isFollow;
    }

    public String getIsPrivateReqSent() {

        return this.isPrivateReqSent;
    }

    public String getIsRespToPvtReq() {

        return this.isRespToPvtReq;
    }

    public String getLastUpdate() {

        return this.lastUpdate;
    }

    public List<MoreVideos> getMoreVideos() {

        return this.moreVideos;
    }

    public String getPendingPrivateGroupRequests() {

        return this.pendingPrivateGroupRequests;
    }

    public String getProfession() {

        return this.profession;
    }

    public String getPthotoPath() {

        return this.pthotoPath;
    }

    public List<SuggestedUsersDto> getSuggestedUsers() {

        return this.suggestedUsers;
    }

    public String getTotalNoOffollowers() {

        return this.totalNoOffollowers;
    }

    public String getTotalNoOfFollowing() {

        return this.totalNoOfFollowing;
    }

    public String getTotalNoOfLikes() {

        return this.totalNoOfLikes;
    }

    public String getTotalNoOfPrivateGroupPeople() {

        return this.totalNoOfPrivateGroupPeople;
    }

    public String getTotalNoOfTags() {

        return this.totalNoOfTags;
    }

    public String getTotalNoOfVideos() {

        return this.totalNoOfVideos;
    }

    public String getUserid() {

        return this.userid;
    }

    public String getUsername() {

        return this.username;
    }

    public String getUserPickView() {

        return this.userPickView;
    }

    public List<MyPageDto> getVideoList() {

        return this.videoList;
    }

    public String getWebsite() {

        return this.website;
    }

    public void load(final JSONObject response) throws JSONException {

        this.userid = Stream.getString(response, Constant.USER_ID);
        this.username = Stream.getString(response, Constant.NAME);
        this.bio = Stream.getString(response, Constant.BIO);
        this.website = Stream.getString(response, Constant.WEBSITE);
        this.profession = Stream.getString(response, Constant.PROFESSION);
        this.lastUpdate = Stream.getString(response, Constant.LAST_UPDATE);
        this.totalNoOffollowers = Stream.getString(response, Constant.TOTAL_FOLLOWERS);
        this.totalNoOfFollowing = Stream.getString(response, Constant.TOTAL_FOLLOWINGS);
        this.totalNoOfLikes = Stream.getString(response, Constant.TOTAL_LIKES);
        this.totalNoOfTags = Stream.getString(response, Constant.TOTAL_TAGS);
        this.totalNoOfVideos = Stream.getString(response, Constant.TOTAL_VIDEOS);
        this.pthotoPath = Stream.getString(response, Constant.PHOTO_PATH);
        this.userPickView = Stream.getString(response, Constant.PROF_PHOTO_PATH);
        this.bannerPath = Stream.getString(response, Constant.BANNER_PATH);
        this.country = Stream.getString(response, Constant.COUNTRY);
        this.isFollow = Stream.getString(response, Constant.FOLLOWING);
        this.isAddToPrivateGroup = Stream.getString(response, Constant.PRIVATE_GROUP);
        this.isPrivateReqSent = Stream.getString(response, Constant.PRIVATE_REQUEST_SENT);
        this.isRespToPvtReq = Stream.getString(response, Constant.RESPOND_PRIVATE_REQUEST);
        this.totalNoOfPrivateGroupPeople = Stream.getString(response, Constant.TOTAL_PRIVATE_GROUP_COUNT);
        this.pendingPrivateGroupRequests = Stream.getString(response, Constant.PENDING_PRIVATE_GROUP_REQUEST);

        if (this.userid == null) {
            this.userid = Stream.getString(response, Constant.ID);
        }

        if (this.lastUpdate != null) {
            this.lastUpdate = com.TagFu.util.Util.getLocalTime(this.lastUpdate);
        }

        if (this.totalNoOfFollowing == null) {
            this.totalNoOfFollowing = Stream.getString(response, Constant.TOTAL_FOLLOWING);
        }

    }

    public void setBannerPath(final String bannerPath) {

        this.bannerPath = bannerPath;
    }

    public void setBio(final String bio) {

        this.bio = bio;
    }

    public void setCountry(final String country) {

        this.country = country;
    }

    public void setIsAddToPrivateGroup(final String isAddToPrivateGroup) {

        this.isAddToPrivateGroup = isAddToPrivateGroup;
    }

    public void setIsFollow(final String isFollow) {

        this.isFollow = isFollow;
    }

    public void setIsPrivateReqSent(final String isPrivateReqSent) {

        this.isPrivateReqSent = isPrivateReqSent;
    }

    public void setIsRespToPvtReq(final String isRespToPvtReq) {

        this.isRespToPvtReq = isRespToPvtReq;
    }

    public void setLastUpdate(final String lastUpdate) {

        this.lastUpdate = lastUpdate;
    }

    public void setMoreVideos(final List<MoreVideos> moreVideos) {

        this.moreVideos = moreVideos;
    }

    public void setPendingPrivateGroupRequests(final String pendingPrivateGroupRequests) {

        this.pendingPrivateGroupRequests = pendingPrivateGroupRequests;
    }

    public void setProfession(final String profession) {

        this.profession = profession;
    }

    public void setPthotoPath(final String pthotoPath) {

        this.pthotoPath = pthotoPath;
    }

    public void setSuggestedUsers(final List<SuggestedUsersDto> suggestedUsers) {

        this.suggestedUsers = suggestedUsers;
    }

    public void setTotalNoOffollowers(final String totalNoOffollowers) {

        this.totalNoOffollowers = totalNoOffollowers;
    }

    public void setTotalNoOfFollowing(final String totalNoOfFollowing) {

        this.totalNoOfFollowing = totalNoOfFollowing;
    }

    public void setTotalNoOfLikes(final String totalNoOfLikes) {

        this.totalNoOfLikes = totalNoOfLikes;
    }

    public void setTotalNoOfPrivateGroupPeople(final String totalNoOfPrivateGroupPeople) {

        this.totalNoOfPrivateGroupPeople = totalNoOfPrivateGroupPeople;
    }

    public void setTotalNoOfTags(final String totalNoOfTags) {

        this.totalNoOfTags = totalNoOfTags;
    }

    public void setTotalNoOfVideos(final String totalNoOfVideos) {

        this.totalNoOfVideos = totalNoOfVideos;
    }

    public void setUserid(final String userid) {

        this.userid = userid;
    }

    public void setUsername(final String username) {

        this.username = username;
    }

    public void setUserPickView(final String userPickView) {

        this.userPickView = userPickView;
    }

    public void setVideoList(final List<MyPageDto> videoList) {

        this.videoList = videoList;
    }

    public void setWebsite(final String website) {

        this.website = website;
    }

}
