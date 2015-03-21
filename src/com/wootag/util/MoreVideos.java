/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wTagFuutil;

import java.util.List;

import com.woTagFuto.Liked;
import com.wootag.dto.MyPageDto;

public interface MoreVideos {

    void videoList(List<MyPageDto> video);

    void likedList(List<Liked> likedPeople);

    void videoList(List<MyPageDto> video, String type);

    void videoList(List<MyPageDto> video, String type, boolean next);
}
