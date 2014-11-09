/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.util;

import com.wootag.ui.LoadingScrollView;

public interface ScrollChangeListener {

    void onScrollChanged(LoadingScrollView scrollView, int currentX, int currentY, int oldX, int oldY);

}
